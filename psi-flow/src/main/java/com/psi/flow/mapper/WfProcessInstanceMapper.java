package com.psi.flow.mapper;

import com.psi.flow.entity.WfProcessInstance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 流程实例Mapper
 */
@Mapper
public interface WfProcessInstanceMapper extends BaseMapper<WfProcessInstance> {

    /**
     * 根据业务类型和业务ID查询流程实例
     */
    @Select("SELECT * FROM wf_process_instance WHERE process_key = #{processKey} AND id IN (SELECT process_instance_id FROM wf_process_instance_biz WHERE biz_type = #{bizType} AND biz_id = #{bizId}) AND del_flag = 0")
    WfProcessInstance selectByBiz(@Param("processKey") String processKey, @Param("bizType") String bizType, @Param("bizId") String bizId);
}