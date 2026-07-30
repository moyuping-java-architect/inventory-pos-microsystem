package service

import (
	"database/sql"
	"fmt"
	"sync"

	"github.com/psi/psi-stock-go/model"
	"github.com/psi/psi-stock-go/repository"
	"github.com/shopspring/decimal"
)

// StockService 库存服务
type StockService struct {
	repo      *repository.StockRepository
	redisRepo *repository.RedisStockRepository
	db        *sql.DB
	mu        sync.Mutex // 进程内互斥，辅助 Redis + DB 级 CAS
	tenancyFn func() int64
}

func NewStockService(db *sql.DB, repo *repository.StockRepository, redisRepo *repository.RedisStockRepository) *StockService {
	return &StockService{
		repo:      repo,
		redisRepo: redisRepo,
		db:        db,
		tenancyFn: defaultTenantID,
	}
}

func defaultTenantID() int64 {
	return 1
}

func (s *StockService) tenantID() int64 {
	if s.tenancyFn != nil {
		return s.tenancyFn()
	}
	return 1
}

// ensureRedisStock 保证 Redis 中有该 SKU 的库存缓存
func (s *StockService) ensureRedisStock(warehouseCode, skuCode string) error {
	qty, err := s.redisRepo.GetStock(warehouseCode, skuCode)
	if err != nil {
		return err
	}
	if qty.IsZero() {
		// 尝试从数据库加载
		entity, err := s.repo.GetStockNoTx(warehouseCode, skuCode, s.tenantID())
		if err != nil {
			return err
		}
		if entity == nil {
			return fmt.Errorf("库存不存在")
		}
		if err := s.redisRepo.InitStock(warehouseCode, skuCode, entity.AvailableQuantity); err != nil {
			return err
		}
	}
	return nil
}

// Increase 新增库存：先写数据库，再更新 Redis
func (s *StockService) Increase(req model.StockOperateRequest) error {
	quantity := decimal.NewFromFloat(req.Quantity)
	costPrice := decimal.NewFromFloat(req.CostPrice)
	if costPrice.IsZero() {
		costPrice = decimal.Zero
	}

	s.mu.Lock()
	defer s.mu.Unlock()

	if err := s.repo.IncreaseStock(req.WarehouseCode, req.GoodsCode, req.SkuCode, quantity, costPrice,
		req.SourceNo, req.SourceType, s.tenantID()); err != nil {
		return err
	}

	// DB 成功后，更新 Redis（Redis 失败不影响主流程）
	if err := s.redisRepo.IncreaseStock(req.WarehouseCode, req.SkuCode, quantity); err != nil {
		fmt.Printf("Redis 库存新增同步失败: %v\n", err)
	}
	return nil
}

// Decrease 扣减库存：先 Redis 预扣，再 DB 落库，失败补偿 Redis
func (s *StockService) Decrease(req model.StockOperateRequest) error {
	quantity := decimal.NewFromFloat(req.Quantity)
	if quantity.LessThanOrEqual(decimal.Zero) {
		return fmt.Errorf("扣减数量必须大于0")
	}

	s.mu.Lock()
	defer s.mu.Unlock()

	// 1. 确保 Redis 缓存存在
	if err := s.ensureRedisStock(req.WarehouseCode, req.SkuCode); err != nil {
		return err
	}

	// 2. Redis 预扣
	if err := s.redisRepo.DecreaseStock(req.WarehouseCode, req.SkuCode, quantity); err != nil {
		return err
	}

	// 3. 查询当前库存用于流水
	entity, err := s.repo.GetStockNoTx(req.WarehouseCode, req.SkuCode, s.tenantID())
	if err != nil {
		_ = s.redisRepo.CompensateDecrease(req.WarehouseCode, req.SkuCode, quantity)
		return err
	}
	if entity == nil {
		_ = s.redisRepo.CompensateDecrease(req.WarehouseCode, req.SkuCode, quantity)
		return fmt.Errorf("库存不存在")
	}

	// 4. DB CAS 扣减
	if err := s.repo.DecreaseStock(req.WarehouseCode, req.SkuCode, quantity, req.SourceNo, req.SourceType, s.tenantID()); err != nil {
		_ = s.redisRepo.CompensateDecrease(req.WarehouseCode, req.SkuCode, quantity)
		return err
	}

	// 5. 记录流水
	after := entity.Quantity.Sub(quantity)
	amount := quantity.Mul(entity.AvgCostPrice)
	s.repo.AddFlow(req.WarehouseCode, entity.GoodsCode, req.SkuCode, 2,
		decimal.Zero, quantity, entity.Quantity, after, entity.AvgCostPrice, amount,
		req.SourceNo, req.SourceType, "出库", s.tenantID())
	return nil
}

// Lock 预占库存：先 Redis 扣减可用，再 DB 预占
func (s *StockService) Lock(req model.StockOperateRequest) error {
	quantity := decimal.NewFromFloat(req.Quantity)
	if quantity.LessThanOrEqual(decimal.Zero) {
		return fmt.Errorf("预占数量必须大于0")
	}

	s.mu.Lock()
	defer s.mu.Unlock()

	if err := s.ensureRedisStock(req.WarehouseCode, req.SkuCode); err != nil {
		return err
	}

	if err := s.redisRepo.DecreaseStock(req.WarehouseCode, req.SkuCode, quantity); err != nil {
		return err
	}

	entity, err := s.repo.GetStockNoTx(req.WarehouseCode, req.SkuCode, s.tenantID())
	if err != nil || entity == nil {
		_ = s.redisRepo.CompensateDecrease(req.WarehouseCode, req.SkuCode, quantity)
		if err != nil {
			return err
		}
		return fmt.Errorf("库存不存在")
	}

	if err := s.repo.LockStock(req.WarehouseCode, req.SkuCode, quantity, s.tenantID()); err != nil {
		_ = s.redisRepo.CompensateDecrease(req.WarehouseCode, req.SkuCode, quantity)
		return err
	}

	s.repo.AddFlow(req.WarehouseCode, entity.GoodsCode, req.SkuCode, 3,
		decimal.Zero, quantity, entity.Quantity, entity.Quantity, entity.AvgCostPrice, decimal.Zero,
		req.SourceNo, req.SourceType, "预占", s.tenantID())
	return nil
}

// Release 释放预占库存：先 DB 释放，再 Redis 加回可用
func (s *StockService) Release(req model.StockOperateRequest) error {
	quantity := decimal.NewFromFloat(req.Quantity)
	if quantity.LessThanOrEqual(decimal.Zero) {
		return fmt.Errorf("释放数量必须大于0")
	}

	s.mu.Lock()
	defer s.mu.Unlock()

	entity, err := s.repo.GetStockNoTx(req.WarehouseCode, req.SkuCode, s.tenantID())
	if err != nil {
		return err
	}
	if entity == nil {
		return fmt.Errorf("库存不存在")
	}

	if err := s.repo.ReleaseStock(req.WarehouseCode, req.SkuCode, quantity, s.tenantID()); err != nil {
		return err
	}

	if err := s.redisRepo.IncreaseStock(req.WarehouseCode, req.SkuCode, quantity); err != nil {
		fmt.Printf("Redis 释放同步失败: %v\n", err)
	}

	s.repo.AddFlow(req.WarehouseCode, entity.GoodsCode, req.SkuCode, 4,
		quantity, decimal.Zero, entity.Quantity, entity.Quantity, entity.AvgCostPrice, decimal.Zero,
		req.SourceNo, req.SourceType, "释放", s.tenantID())
	return nil
}

// Confirm 确认出库：先 Redis 扣减锁定，再 DB 确认出库
func (s *StockService) Confirm(req model.StockOperateRequest) error {
	quantity := decimal.NewFromFloat(req.Quantity)
	if quantity.LessThanOrEqual(decimal.Zero) {
		return fmt.Errorf("确认出库数量必须大于0")
	}

	s.mu.Lock()
	defer s.mu.Unlock()

	if err := s.ensureRedisStock(req.WarehouseCode, req.SkuCode); err != nil {
		return err
	}

	if err := s.redisRepo.DecreaseStock(req.WarehouseCode, req.SkuCode, quantity); err != nil {
		return err
	}

	entity, err := s.repo.GetStockNoTx(req.WarehouseCode, req.SkuCode, s.tenantID())
	if err != nil || entity == nil {
		_ = s.redisRepo.CompensateDecrease(req.WarehouseCode, req.SkuCode, quantity)
		if err != nil {
			return err
		}
		return fmt.Errorf("库存不存在")
	}

	if err := s.repo.ConfirmStock(req.WarehouseCode, req.SkuCode, quantity, s.tenantID()); err != nil {
		_ = s.redisRepo.CompensateDecrease(req.WarehouseCode, req.SkuCode, quantity)
		return err
	}

	after := entity.Quantity.Sub(quantity)
	amount := quantity.Mul(entity.AvgCostPrice)
	s.repo.AddFlow(req.WarehouseCode, entity.GoodsCode, req.SkuCode, 5,
		decimal.Zero, quantity, entity.Quantity, after, entity.AvgCostPrice, amount,
		req.SourceNo, req.SourceType, "确认出库", s.tenantID())
	return nil
}

// BatchDecrease 批量扣减：Redis 逐条预扣 + DB 事务落库
func (s *StockService) BatchDecrease(req model.StockBatchOperateRequest) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	// 先预扣 Redis
	quantities := make(map[string]decimal.Decimal)
	for _, item := range req.Items {
		qty := decimal.NewFromFloat(item.Quantity)
		quantities[item.SkuCode] = qty
		if err := s.ensureRedisStock(item.WarehouseCode, item.SkuCode); err != nil {
			return err
		}
		if err := s.redisRepo.DecreaseStock(item.WarehouseCode, item.SkuCode, qty); err != nil {
			// 回滚已预扣的
			for _, prev := range req.Items[:len(quantities)-1] {
				_ = s.redisRepo.CompensateDecrease(prev.WarehouseCode, prev.SkuCode, quantities[prev.SkuCode])
			}
			return err
		}
	}

	// DB 批量扣减
	if err := s.repo.BatchDecreaseStock(req.Items, s.tenantID()); err != nil {
		// 回滚 Redis
		for _, item := range req.Items {
			_ = s.redisRepo.CompensateDecrease(item.WarehouseCode, item.SkuCode, quantities[item.SkuCode])
		}
		return err
	}

	return nil
}
