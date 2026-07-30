package com.trademaster.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.stock.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {

    @Select("SELECT * FROM stock WHERE warehouse_code = #{warehouseCode} AND sku_code = #{skuCode} AND del_flag = 0 LIMIT 1")
    Stock selectByWarehouseAndSku(@Param("warehouseCode") String warehouseCode, @Param("skuCode") String skuCode);

    @Select("SELECT * FROM stock WHERE warehouse_code = #{warehouseCode} AND sku_code = #{skuCode} AND batch_no = #{batchNo} AND del_flag = 0 LIMIT 1")
    Stock selectByWarehouseAndSkuAndBatch(@Param("warehouseCode") String warehouseCode, @Param("skuCode") String skuCode, @Param("batchNo") String batchNo);
}
