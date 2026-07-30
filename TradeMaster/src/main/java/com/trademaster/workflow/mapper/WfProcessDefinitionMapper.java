package com.trademaster.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.workflow.entity.WfProcessDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WfProcessDefinitionMapper extends BaseMapper<WfProcessDefinition> {

    @Select("SELECT * FROM wf_process_definition WHERE process_key = #{processKey} AND status = 1 AND del_flag = 0 ORDER BY version DESC LIMIT 1")
    WfProcessDefinition selectLatestByKey(@Param("processKey") String processKey);
}
