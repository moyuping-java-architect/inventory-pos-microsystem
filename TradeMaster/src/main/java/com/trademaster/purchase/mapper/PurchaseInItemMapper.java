package com.trademaster.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.purchase.entity.PurchaseInItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PurchaseInItemMapper extends BaseMapper<PurchaseInItem> {

    @Select("SELECT * FROM purchase_in_item WHERE in_id = #{inId}")
    List<PurchaseInItem> selectByInId(@Param("inId") Long inId);
}
