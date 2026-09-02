package com.psi.cashier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.cashier.entity.PricingConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 定价配置Mapper
 *
 * @author PSI
 * @version 1.0.0
 */
@Mapper
public interface PricingConfigMapper extends BaseMapper<PricingConfigEntity> {

    /**
     * 查询租户的定价配置（取最新一条）
     */
    @Select("SELECT * FROM pricing_config WHERE tenant_id = #{tenantId} ORDER BY id DESC LIMIT 1")
    PricingConfigEntity selectByTenant(@Param("tenantId") String tenantId);
}
