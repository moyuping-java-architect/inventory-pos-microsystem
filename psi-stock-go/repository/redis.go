package repository

import (
	"context"
	"fmt"
	"strconv"
	"strings"
	"time"

	"github.com/redis/go-redis/v9"
	"github.com/shopspring/decimal"
)

const (
	stockKeyPrefix = "psi:stock:qty:"
)

// RedisStockRepository Redis 库存缓存层
type RedisStockRepository struct {
	client *redis.Client
}

func NewRedisStockRepository(client *redis.Client) *RedisStockRepository {
	return &RedisStockRepository{client: client}
}

func stockKey(warehouseCode, skuCode string) string {
	return fmt.Sprintf("%s%s:%s", stockKeyPrefix, warehouseCode, skuCode)
}

// decimalToString 把 decimal 转成 Redis 可识别的普通数字字符串
func decimalToString(d decimal.Decimal) string {
	f, _ := strconv.ParseFloat(d.String(), 64)
	return strconv.FormatFloat(f, 'f', -1, 64)
}

// InitStock 从数据库加载库存到 Redis
func (r *RedisStockRepository) InitStock(warehouseCode, skuCode string, quantity decimal.Decimal) error {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	return r.client.Set(ctx, stockKey(warehouseCode, skuCode), decimalToString(quantity), 0).Err()
}

// GetStock 查询 Redis 库存
func (r *RedisStockRepository) GetStock(warehouseCode, skuCode string) (decimal.Decimal, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	val, err := r.client.Get(ctx, stockKey(warehouseCode, skuCode)).Result()
	if err == redis.Nil {
		return decimal.Zero, nil
	}
	if err != nil {
		return decimal.Zero, err
	}
	return decimal.NewFromString(val)
}

// luaDecrease 原子扣减脚本（支持小数）
const luaDecrease = `
local key = KEYS[1]
local qty = tonumber(ARGV[1])
local current = redis.call('GET', key)
if current == false then
    return -2
end
current = tonumber(current)
if current < qty then
    return -1
end
local newVal = current - qty
redis.call('SET', key, tostring(newVal))
return newVal
`

// DecreaseStock Redis 预扣库存
func (r *RedisStockRepository) DecreaseStock(warehouseCode, skuCode string, quantity decimal.Decimal) error {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	qtyStr := decimalToString(quantity)
	result, err := r.client.Eval(ctx, luaDecrease, []string{stockKey(warehouseCode, skuCode)}, qtyStr).Result()
	if err != nil {
		return fmt.Errorf("Redis 预扣失败: %w", err)
	}

	switch v := result.(type) {
	case int64:
		if v == -2 {
			return fmt.Errorf("Redis 库存未初始化")
		}
		if v < 0 {
			return fmt.Errorf("库存不足")
		}
	case string:
		if strings.HasPrefix(v, "-") {
			return fmt.Errorf("库存不足")
		}
	}
	return nil
}

// luaIncrease 原子增加脚本（支持小数）
const luaIncrease = `
local key = KEYS[1]
local qty = tonumber(ARGV[1])
local current = redis.call('GET', key)
if current == false then
    return 0
end
local newVal = tonumber(current) + qty
redis.call('SET', key, tostring(newVal))
return newVal
`

// IncreaseStock Redis 增加库存
func (r *RedisStockRepository) IncreaseStock(warehouseCode, skuCode string, quantity decimal.Decimal) error {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	_, err := r.client.Eval(ctx, luaIncrease, []string{stockKey(warehouseCode, skuCode)}, decimalToString(quantity)).Result()
	return err
}

// CompensateDecrease Redis 扣减补偿（DB 落库失败时回滚）
func (r *RedisStockRepository) CompensateDecrease(warehouseCode, skuCode string, quantity decimal.Decimal) error {
	return r.IncreaseStock(warehouseCode, skuCode, quantity)
}
