package com.psi.stock.mapper;

import com.psi.stock.dto.StockBatchOperateItemDTO;
import com.psi.stock.entity.StockEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
}
