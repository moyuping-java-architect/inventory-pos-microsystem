package model

import "github.com/shopspring/decimal"

// StockEntity 库存实体（对应 psi_stock.stock）
type StockEntity struct {
	ID                int64           `db:"id"`
	TenantID          int64           `db:"tenant_id"`
	WarehouseCode     string          `db:"warehouse_code"`
	WarehouseName     string          `db:"warehouse_name"`
	GoodsCode         string          `db:"goods_code"`
	SkuCode           string          `db:"sku_code"`
	GoodsName         string          `db:"goods_name"`
	GoodsSpec         string          `db:"goods_spec"`
	Unit              string          `db:"unit"`
	Quantity          decimal.Decimal `db:"quantity"`
	LockedQuantity    decimal.Decimal `db:"locked_quantity"`
	AvailableQuantity decimal.Decimal `db:"available_quantity"`
	AvgCostPrice      decimal.Decimal `db:"avg_cost_price"`
	TotalAmount       decimal.Decimal `db:"total_amount"`
	Status            int             `db:"status"`
}

// StockOperateRequest 单商品操作请求
type StockOperateRequest struct {
	WarehouseCode string  `json:"warehouseCode" binding:"required"`
	GoodsCode     string  `json:"goodsCode"`
	SkuCode       string  `json:"skuCode" binding:"required"`
	Quantity      float64 `json:"quantity" binding:"required,gt=0"`
	CostPrice     float64 `json:"costPrice"`
	SourceNo      string  `json:"sourceNo"`
	SourceType    string  `json:"sourceType"`
	Remark        string  `json:"remark"`
}

// StockBatchOperateItem 批量操作项
type StockBatchOperateItem struct {
	WarehouseCode string  `json:"warehouseCode" binding:"required"`
	GoodsCode     string  `json:"goodsCode"`
	SkuCode       string  `json:"skuCode" binding:"required"`
	Quantity      float64 `json:"quantity" binding:"required,gt=0"`
}

// StockBatchOperateRequest 批量操作请求
type StockBatchOperateRequest struct {
	Items      []StockBatchOperateItem `json:"items" binding:"required,min=1"`
	SourceNo   string                  `json:"sourceNo"`
	SourceType string                  `json:"sourceType"`
	Remark     string                  `json:"remark"`
}
