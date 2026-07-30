package com.psi.cashier.mapper;

import com.psi.cashier.entity.MemberPriceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 会员价Mapper接口
 */
@Mapper
public interface MemberPriceMapper extends BaseMapper<MemberPriceEntity> {

    /**
     * 根据SKU ID和会员等级查询会员价
     */
    @Select("SELECT * FROM member_price WHERE tenant_id = #{tenantId} AND sku_id = #{skuId} AND member_level = #{memberLevel} AND del_flag = 0 AND status = 1")
    MemberPriceEntity selectBySkuIdAndLevel(@Param("tenantId") String tenantId, @Param("skuId") Integer skuId, @Param("memberLevel") Integer memberLevel);

    /**
     * 根据SKU ID查询该SKU的所有会员价
     */
    @Select("SELECT * FROM member_price WHERE tenant_id = #{tenantId} AND sku_id = #{skuId} AND del_flag = 0 AND status = 1")
    java.util.List<MemberPriceEntity> selectBySkuId(@Param("tenantId") String tenantId, @Param("skuId") Integer skuId);
}