package com.psi.flow.mapper;

import com.psi.flow.entity.WfProcessNode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程节点Mapper
 */
@Mapper
public interface WfProcessNodeMapper extends BaseMapper<WfProcessNode> {

    /**
     * 根据流程定义ID查询所有节点
     */
    @Select("SELECT * FROM wf_process_node WHERE process_def_id = #{processDefId} AND status = 1 AND del_flag = 0 ORDER BY sort")
    List<WfProcessNode> selectByProcessDefId(@Param("processDefId") Long processDefId);

    /**
     * 根据流程定义ID查询开始节点（排序最小的审批节点）
     */
    @Select("SELECT * FROM wf_process_node WHERE process_def_id = #{processDefId} AND node_type = 1 AND status = 1 AND del_flag = 0 ORDER BY sort LIMIT 1")
    WfProcessNode selectStartNode(@Param("processDefId") Long processDefId);

    /**
     * 根据流程定义ID查询结束节点
     */
    @Select("SELECT * FROM wf_process_node WHERE process_def_id = #{processDefId} AND node_type = 4 AND status = 1 AND del_flag = 0 LIMIT 1")
    WfProcessNode selectEndNode(@Param("processDefId") Long processDefId);
}