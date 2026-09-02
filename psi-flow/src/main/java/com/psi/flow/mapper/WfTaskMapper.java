package com.psi.flow.mapper;

import com.psi.flow.entity.WfTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 任务Mapper
 */
@Mapper
public interface WfTaskMapper extends BaseMapper<WfTask> {

    /**
     * 根据流程实例ID查询待处理任务
     */
    @Select("SELECT * FROM wf_task WHERE process_instance_id = #{processInstanceId} AND status = 1 AND del_flag = 0")
    List<WfTask> selectPendingByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    /**
     * 根据流程实例ID和节点ID查询任务
     */
    @Select("SELECT * FROM wf_task WHERE process_instance_id = #{processInstanceId} AND node_id = #{nodeId} AND status = 1 AND del_flag = 0")
    List<WfTask> selectByProcessInstanceAndNode(@Param("processInstanceId") String processInstanceId, @Param("nodeId") Long nodeId);

    /**
     * 根据处理人ID查询待办任务列表
     */
    @Select("SELECT * FROM wf_task WHERE handler_user_id = #{handlerUserId} AND status = 1 AND del_flag = 0 ORDER BY create_time DESC")
    List<WfTask> selectTodoByHandler(@Param("handlerUserId") String handlerUserId);
}