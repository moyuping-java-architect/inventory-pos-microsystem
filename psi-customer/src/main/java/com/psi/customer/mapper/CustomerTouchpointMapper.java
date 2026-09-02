package com.psi.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.customer.entity.CustomerTouchpointEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 客户触点 Mapper（非购买类触点 CRUD）
 */
@Mapper
public interface CustomerTouchpointMapper extends BaseMapper<CustomerTouchpointEntity> {

    /**
     * 按客户ID查触点（B2B客户）
     */
    @Select("SELECT * FROM customer_touchpoint " +
            "WHERE del_flag = 0 AND customer_id = #{customerId} " +
            "ORDER BY contact_time DESC")
    List<CustomerTouchpointEntity> selectByCustomerId(@Param("customerId") Long customerId);

    /**
     * 按会员ID查触点（B2C会员）
     */
    @Select("SELECT * FROM customer_touchpoint " +
            "WHERE del_flag = 0 AND member_id = #{memberId} " +
            "ORDER BY contact_time DESC")
    List<CustomerTouchpointEntity> selectByMemberId(@Param("memberId") Long memberId);

    /**
     * 待跟进触点列表
     */
    @Select("SELECT * FROM customer_touchpoint " +
            "WHERE del_flag = 0 AND follow_up_done = 0 " +
            "ORDER BY contact_time DESC")
    List<CustomerTouchpointEntity> selectPendingFollowUps();

    // ========== 旅程状态重算用（按时间正序回放触点） ==========

    /**
     * 有触点记录的客户ID集合
     */
    @Select("SELECT DISTINCT customer_id FROM customer_touchpoint " +
            "WHERE del_flag = 0 AND customer_id IS NOT NULL")
    List<Long> selectDistinctCustomerIds();

    /**
     * 有触点记录的会员ID集合
     */
    @Select("SELECT DISTINCT member_id FROM customer_touchpoint " +
            "WHERE del_flag = 0 AND member_id IS NOT NULL")
    List<Long> selectDistinctMemberIds();

    /**
     * 客户触点按时间正序（回放推进旅程用，顺序不能反）
     */
    @Select("SELECT * FROM customer_touchpoint " +
            "WHERE del_flag = 0 AND customer_id = #{customerId} " +
            "ORDER BY contact_time ASC, id ASC")
    List<CustomerTouchpointEntity> selectByCustomerIdAsc(@Param("customerId") Long customerId);

    /**
     * 会员触点按时间正序
     */
    @Select("SELECT * FROM customer_touchpoint " +
            "WHERE del_flag = 0 AND member_id = #{memberId} " +
            "ORDER BY contact_time ASC, id ASC")
    List<CustomerTouchpointEntity> selectByMemberIdAsc(@Param("memberId") Long memberId);
}
