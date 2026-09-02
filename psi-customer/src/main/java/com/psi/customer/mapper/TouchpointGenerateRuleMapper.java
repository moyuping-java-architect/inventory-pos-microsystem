package com.psi.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.customer.entity.TouchpointGenerateRuleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 触点生成规则 Mapper。零代码配置「事件 + 条件 → 触点」的存储层。
 *
 * @author PSI
 */
@Mapper
public interface TouchpointGenerateRuleMapper extends BaseMapper<TouchpointGenerateRuleEntity> {
}
