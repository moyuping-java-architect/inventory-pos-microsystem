package com.psi.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.customer.entity.SalesScriptMatchRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 话术匹配规则 Mapper
 */
@Mapper
public interface SalesScriptMatchRuleMapper extends BaseMapper<SalesScriptMatchRuleEntity> {

    /**
     * 查询租户默认规则（按创建时间取第一条）
     */
    @Select("SELECT * FROM sales_script_match_rule " +
            "WHERE del_flag = 0 AND tenant_id = #{tenantId} " +
            "ORDER BY id ASC LIMIT 1")
    SalesScriptMatchRuleEntity selectDefaultRule(@Param("tenantId") String tenantId);
}
