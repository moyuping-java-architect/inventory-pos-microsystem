package com.psi.flow.mapper;

import com.psi.flow.entity.WfProcessDefinition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 流程定义Mapper
 */
@Mapper
public interface WfProcessDefinitionMapper extends BaseMapper<WfProcessDefinition> {

    /**
     * 根据流程标识查询最新版本的流程定义
     */
    @Select("SELECT * FROM wf_process_definition WHERE process_key = #{processKey} AND status = 1 AND del_flag = 0 ORDER BY version DESC LIMIT 1")
    WfProcessDefinition selectLatestByKey(@Param("processKey") String processKey);
}