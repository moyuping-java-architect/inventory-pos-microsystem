package com.psi.flow.mapper;

import com.psi.flow.entity.WfProcessConditionConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程条件配置Mapper
 */
@Mapper
public interface WfProcessConditionConfigMapper extends BaseMapper<WfProcessConditionConfig> {

    /**
     * 根据流程定义ID查询条件配置
     */
    @Select("SELECT * FROM wf_process_condition_config WHERE process_def_id = #{processDefId} AND status = 1 AND del_flag = 0 ORDER BY sort")
    List<WfProcessConditionConfig> selectByProcessDefId(@Param("processDefId") Long processDefId);
}