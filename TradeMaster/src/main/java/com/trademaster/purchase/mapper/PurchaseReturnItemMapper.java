package com.trademaster.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.purchase.entity.PurchaseReturnItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PurchaseReturnItemMapper extends BaseMapper<PurchaseReturnItem> {

    @Select("SELECT * FROM purchase_return_item WHERE return_id = #{returnId}")
    List<PurchaseReturnItem> selectByReturnId(@Param("returnId") Long returnId);
}
