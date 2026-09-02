package com.psi.flow.mapper;

import com.psi.flow.entity.WfProcessRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 节点流转关系Mapper
 */
@Mapper
public interface WfProcessRelationMapper extends BaseMapper<WfProcessRelation> {

    /**
     * 根据流程定义ID和来源节点ID查询所有目标节点关系
     */
    @Select("SELECT * FROM wf_process_relation WHERE process_def_id = #{processDefId} AND from_node_id = #{fromNodeId}")
    List<WfProcessRelation> selectByFromNode(@Param("processDefId") Long processDefId, @Param("fromNodeId") Long fromNodeId);
}