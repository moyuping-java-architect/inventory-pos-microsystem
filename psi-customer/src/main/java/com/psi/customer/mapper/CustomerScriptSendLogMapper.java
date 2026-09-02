package com.psi.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.customer.entity.CustomerScriptSendLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 话术发送记录 Mapper
 */
@Mapper
public interface CustomerScriptSendLogMapper extends BaseMapper<CustomerScriptSendLogEntity> {

    /**
     * 查询某客户在某阶段某天已发送过的话术ID
     */
    @Select("SELECT script_id FROM customer_script_send_log " +
            "WHERE del_flag = 0 AND tenant_id = #{tenantId} " +
            "AND customer_id = #{customerId} AND stage_code = #{stageCode} AND day_in_stage = #{dayInStage}")
    List<Long> selectSentScriptIds(@Param("tenantId") String tenantId,
                                   @Param("customerId") Long customerId,
                                   @Param("stageCode") String stageCode,
                                   @Param("dayInStage") Integer dayInStage);

    /**
     * 查询某客户的全部发送记录
     */
    @Select("SELECT * FROM customer_script_send_log " +
            "WHERE del_flag = 0 AND tenant_id = #{tenantId} AND customer_id = #{customerId} " +
            "ORDER BY send_time DESC")
    List<CustomerScriptSendLogEntity> selectByCustomer(@Param("tenantId") String tenantId,
                                                       @Param("customerId") Long customerId);
}
