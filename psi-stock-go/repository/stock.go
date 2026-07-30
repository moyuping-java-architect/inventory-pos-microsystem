package repository

import (
	"database/sql"
	"errors"
	"fmt"
	"log"
	"time"

	"github.com/psi/psi-stock-go/model"
	"github.com/shopspring/decimal"
)

// StockRepository 库存仓储
type StockRepository struct {
	db *sql.DB
}

func NewStockRepository(db *sql.DB) *StockRepository {
	return &StockRepository{db: db}
}

// GetStock 查询库存记录（带行锁，用于单条扣减事务）
func (r *StockRepository) GetStock(tx *sql.Tx, warehouseCode, skuCode string, tenantID int64) (*model.StockEntity, error) {
	query := `SELECT id, tenant_id, warehouse_code, warehouse_name, goods_code, sku_code, goods_name, goods_spec, unit,
		quantity, locked_quantity, available_quantity, avg_cost_price, total_amount, status
		FROM stock WHERE warehouse_code = ? AND sku_code = ? AND tenant_id = ? AND del_flag = 0 FOR UPDATE`
	row := tx.QueryRow(query, warehouseCode, skuCode, tenantID)
	return scanStock(row)
}

// GetStockNoTx 非事务查询
type StockNoTxRepository interface {
	GetStockNoTx(warehouseCode, skuCode string, tenantID int64) (*model.StockEntity, error)
}

func (r *StockRepository) GetStockNoTx(warehouseCode, skuCode string, tenantID int64) (*model.StockEntity, error) {
	query := `SELECT id, tenant_id, warehouse_code, warehouse_name, goods_code, sku_code, goods_name, goods_spec, unit,
		quantity, locked_quantity, available_quantity, avg_cost_price, total_amount, status
		FROM stock WHERE warehouse_code = ? AND sku_code = ? AND tenant_id = ? AND del_flag = 0`
	row := r.db.QueryRow(query, warehouseCode, skuCode, tenantID)
	return scanStock(row)
}

// scanStock 扫描库存记录
func scanStock(row *sql.Row) (*model.StockEntity, error) {
	var e model.StockEntity
	err := row.Scan(&e.ID, &e.TenantID, &e.WarehouseCode, &e.WarehouseName, &e.GoodsCode, &e.SkuCode,
		&e.GoodsName, &e.GoodsSpec, &e.Unit, &e.Quantity, &e.LockedQuantity, &e.AvailableQuantity,
		&e.AvgCostPrice, &e.TotalAmount, &e.Status)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, nil
		}
		return nil, err
	}
	return &e, nil
}

// IncreaseStock 新增库存（CAS + 行锁，防止并发下重复插入/更新不一致）
func (r *StockRepository) IncreaseStock(warehouseCode, goodsCode, skuCode string, quantity, costPrice decimal.Decimal,
	sourceNo, sourceType string, tenantID int64) error {
	tx, err := r.db.Begin()
	if err != nil {
		return err
	}
	defer func() {
		if err != nil {
			_ = tx.Rollback()
		}
	}()

	entity, err := r.GetStock(tx, warehouseCode, skuCode, tenantID)
	if err != nil {
		return err
	}

	now := time.Now().Format("2006-01-02 15:04:05")
	if entity == nil {
		// 新增库存记录
		_, err = tx.Exec(`INSERT INTO stock (tenant_id, warehouse_code, warehouse_name, goods_code, sku_code, goods_name, goods_spec, unit,
			quantity, locked_quantity, available_quantity, avg_cost_price, total_amount, status, del_flag, create_by, create_time, update_by, update_time)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?, 1, 0, 1, ?, 1, ?)`,
			tenantID, warehouseCode, "", goodsCode, skuCode, "", "", "",
			quantity, quantity, costPrice, quantity.Mul(costPrice), now, now)
		if err != nil {
			return err
		}
	} else {
		// CAS 更新：使用当前数量计算新值，并通过 version 思想（WHERE 带上原数量）防止并发覆盖
		newQuantity := entity.Quantity.Add(quantity)
		totalCost := entity.TotalAmount.Add(quantity.Mul(costPrice))
		newAvgCost := totalCost.Div(newQuantity)
		newAvailable := entity.AvailableQuantity.Add(quantity)

		res, err := tx.Exec(`UPDATE stock SET quantity = ?, available_quantity = ?, avg_cost_price = ?, total_amount = ?, update_time = ?
			WHERE id = ? AND quantity = ? AND available_quantity = ?`,
			newQuantity, newAvailable, newAvgCost, totalCost, now,
			entity.ID, entity.Quantity, entity.AvailableQuantity)
		if err != nil {
			return err
		}
		affected, _ := res.RowsAffected()
		if affected == 0 {
			return errors.New("库存更新并发冲突，请重试")
		}
	}

	return tx.Commit()
}

// DecreaseStock 扣减库存（CAS：可用库存必须足够）
func (r *StockRepository) DecreaseStock(warehouseCode, skuCode string, quantity decimal.Decimal,
	sourceNo, sourceType string, tenantID int64) error {
	res, err := r.db.Exec(`UPDATE stock SET quantity = quantity - ?, available_quantity = available_quantity - ?,
		total_amount = total_amount - (avg_cost_price * ?), update_time = ?
		WHERE warehouse_code = ? AND sku_code = ? AND tenant_id = ? AND del_flag = 0 AND available_quantity >= ?`,
		quantity, quantity, quantity, time.Now().Format("2006-01-02 15:04:05"),
		warehouseCode, skuCode, tenantID, quantity)
	if err != nil {
		return err
	}
	affected, err := res.RowsAffected()
	if err != nil {
		return err
	}
	if affected == 0 {
		return errors.New("库存不足或并发冲突")
	}
	return nil
}

// LockStock 预占库存（CAS：可用库存必须足够）
func (r *StockRepository) LockStock(warehouseCode, skuCode string, quantity decimal.Decimal, tenantID int64) error {
	res, err := r.db.Exec(`UPDATE stock SET available_quantity = available_quantity - ?, locked_quantity = locked_quantity + ?, update_time = ?
		WHERE warehouse_code = ? AND sku_code = ? AND tenant_id = ? AND del_flag = 0 AND available_quantity >= ?`,
		quantity, quantity, time.Now().Format("2006-01-02 15:04:05"),
		warehouseCode, skuCode, tenantID, quantity)
	if err != nil {
		return err
	}
	affected, _ := res.RowsAffected()
	if affected == 0 {
		return errors.New("库存不足或并发冲突")
	}
	return nil
}

// ReleaseStock 释放预占库存（CAS：锁定库存必须足够）
func (r *StockRepository) ReleaseStock(warehouseCode, skuCode string, quantity decimal.Decimal, tenantID int64) error {
	res, err := r.db.Exec(`UPDATE stock SET available_quantity = available_quantity + ?, locked_quantity = locked_quantity - ?, update_time = ?
		WHERE warehouse_code = ? AND sku_code = ? AND tenant_id = ? AND del_flag = 0 AND locked_quantity >= ?`,
		quantity, quantity, time.Now().Format("2006-01-02 15:04:05"),
		warehouseCode, skuCode, tenantID, quantity)
	if err != nil {
		return err
	}
	affected, _ := res.RowsAffected()
	if affected == 0 {
		return errors.New("预占库存不足或并发冲突")
	}
	return nil
}

// ConfirmStock 确认出库（CAS：锁定库存必须足够）
func (r *StockRepository) ConfirmStock(warehouseCode, skuCode string, quantity decimal.Decimal, tenantID int64) error {
	res, err := r.db.Exec(`UPDATE stock SET quantity = quantity - ?, locked_quantity = locked_quantity - ?,
		total_amount = total_amount - (avg_cost_price * ?), update_time = ?
		WHERE warehouse_code = ? AND sku_code = ? AND tenant_id = ? AND del_flag = 0 AND locked_quantity >= ?`,
		quantity, quantity, quantity, time.Now().Format("2006-01-02 15:04:05"),
		warehouseCode, skuCode, tenantID, quantity)
	if err != nil {
		return err
	}
	affected, _ := res.RowsAffected()
	if affected == 0 {
		return errors.New("预占库存不足或并发冲突")
	}
	return nil
}

// BatchDecreaseStock 批量扣减库存（事务 + 每条 CAS）
func (r *StockRepository) BatchDecreaseStock(items []model.StockBatchOperateItem, tenantID int64) error {
	if len(items) == 0 {
		return nil
	}
	// 同一 SKU 去重校验
	seen := make(map[string]bool)
	for _, item := range items {
		key := fmt.Sprintf("%s#%s", item.WarehouseCode, item.SkuCode)
		if seen[key] {
			return errors.New("批量扣减库存存在重复SKU")
		}
		seen[key] = true
	}

	tx, err := r.db.Begin()
	if err != nil {
		return err
	}
	defer func() {
		if err != nil {
			_ = tx.Rollback()
		}
	}()

	now := time.Now().Format("2006-01-02 15:04:05")
	for _, item := range items {
		quantity := decimal.NewFromFloat(item.Quantity)
		res, execErr := tx.Exec(`UPDATE stock SET quantity = quantity - ?, available_quantity = available_quantity - ?,
			total_amount = total_amount - (avg_cost_price * ?), update_time = ?
			WHERE warehouse_code = ? AND sku_code = ? AND tenant_id = ? AND del_flag = 0 AND available_quantity >= ?`,
			quantity, quantity, quantity, now,
			item.WarehouseCode, item.SkuCode, tenantID, quantity)
		if execErr != nil {
			err = execErr
			return err
		}
		affected, _ := res.RowsAffected()
		if affected == 0 {
			err = fmt.Errorf("库存不足: warehouse=%s sku=%s", item.WarehouseCode, item.SkuCode)
			return err
		}
	}

	return tx.Commit()
}

// AddFlow 记录库存流水
func (r *StockRepository) AddFlow(warehouseCode, goodsCode, skuCode string, flowType int,
	inQty, outQty, beforeQty, afterQty, costPrice, amount decimal.Decimal,
	sourceNo, sourceType, remark string, tenantID int64) {
	now := time.Now().Format("2006-01-02 15:04:05")
	_, err := r.db.Exec(`INSERT INTO stock_flow
		(tenant_id, warehouse_code, warehouse_name, goods_code, sku_code, goods_name, goods_spec, unit,
		flow_type, in_quantity, out_quantity, before_quantity, after_quantity, cost_price, amount, source_no, source_type, remark,
		del_flag, status, create_by, create_time, update_by, update_time)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 1, 1, ?, 1, ?)`,
		tenantID, warehouseCode, "", goodsCode, skuCode, "", "", "",
		flowType, inQty, outQty, beforeQty, afterQty, costPrice, amount, sourceNo, sourceType, remark, now, now)
	if err != nil {
		log.Printf("记录库存流水失败: %v", err)
	}
}
