package com.psi.stock.mapper;

import com.psi.stock.dto.StockBatchOperateItemDTO;
import com.psi.stock.entity.StockEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface StockMapper extends BaseMapper<StockEntity> {

    /**
     * 批量预占库存（一条 SQL）
     *
     * <p>只有所有 SKU 的 available_quantity 都满足时才全部更新成功，否则更新 0 行</p>
     *
     * @return 受影响行数
     */
    int batchLockStock(@Param("items") List<StockBatchOperateItemDTO> items);

    /**
     * 批量扣减实际库存（一条 SQL）
     *
     * <p>只有所有 SKU 的 available_quantity 都满足时才全部更新成功，否则更新 0 行</p>
     *
     * @return 受影响行数
     */
    int batchDecreaseStock(@Param("items") List<StockBatchOperateItemDTO> items);

    /**
     * 批量释放预占库存（一条 SQL）
     *
     * <p>只有所有 SKU 的 locked_quantity 都满足时才全部更新成功，否则更新 0 行</p>
     *
     * @return 受影响行数
     */
    int batchReleaseStock(@Param("items") List<StockBatchOperateItemDTO> items);

    /**
     * 批量确认出库（一条 SQL）
     *
     * <p>只有所有 SKU 的 locked_quantity 都满足时才全部更新成功，否则更新 0 行</p>
     *
     * @return 受影响行数
     */
    int batchConfirmStock(@Param("items") List<StockBatchOperateItemDTO> items);

    /**
     * 根据商品编码批量查询商品基础信息（跨库查询 psi_goods.goods 表）
     *
     * @param goodsCodes 商品编码列表
     * @return List of map: goods_code, goods_name, goods_spec, unit
     */
    @Select({
            "<script>",
            "SELECT goods_code AS goodsCode, goods_name AS goodsName, goods_spec AS goodsSpec, unit ",
            "FROM goods ",
            "WHERE goods_code IN ",
            "<foreach item='code' collection='goodsCodes' open='(' separator=',' close=')'>",
            "#{code}",
            "</foreach>",
            "</script>"
    })
    List<Map<String, Object>> selectGoodsInfoByCodes(@Param("goodsCodes") List<String> goodsCodes);
}
