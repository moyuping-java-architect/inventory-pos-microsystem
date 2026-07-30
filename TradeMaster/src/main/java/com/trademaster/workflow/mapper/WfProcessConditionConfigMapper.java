package com.trademaster.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.workflow.entity.WfProcessConditionConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WfProcessConditionConfigMapper extends BaseMapper<WfProcessConditionConfig> {

    @Select("SELECT * FROM wf_process_condition_config WHERE process_def_id = #{processDefId} AND status = 1 AND del_flag = 0 ORDER BY sort")
    List<WfProcessConditionConfig> selectByProcessDefId(@Param("processDefId") Long processDefId);
}
