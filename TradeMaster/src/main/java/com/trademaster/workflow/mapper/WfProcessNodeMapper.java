package com.trademaster.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.workflow.entity.WfProcessNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WfProcessNodeMapper extends BaseMapper<WfProcessNode> {

    @Select("SELECT * FROM wf_process_node WHERE process_def_id = #{processDefId} AND status = 1 AND del_flag = 0 ORDER BY sort")
    List<WfProcessNode> selectByProcessDefId(@Param("processDefId") Long processDefId);

    @Select("SELECT * FROM wf_process_node WHERE process_def_id = #{processDefId} AND node_type = 1 AND status = 1 AND del_flag = 0 ORDER BY sort LIMIT 1")
    WfProcessNode selectStartNode(@Param("processDefId") Long processDefId);

    @Select("SELECT * FROM wf_process_node WHERE process_def_id = #{processDefId} AND node_type = 4 AND status = 1 AND del_flag = 0 LIMIT 1")
    WfProcessNode selectEndNode(@Param("processDefId") Long processDefId);
}
