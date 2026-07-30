package com.trademaster.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trademaster.workflow.entity.WfProcessRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WfProcessRelationMapper extends BaseMapper<WfProcessRelation> {

    @Select("SELECT * FROM wf_process_relation WHERE process_def_id = #{processDefId} AND from_node_id = #{fromNodeId}")
    List<WfProcessRelation> selectByFromNode(@Param("processDefId") Long processDefId, @Param("fromNodeId") Long fromNodeId);
}
