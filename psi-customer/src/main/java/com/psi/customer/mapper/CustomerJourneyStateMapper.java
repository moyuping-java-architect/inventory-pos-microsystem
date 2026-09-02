package com.psi.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.customer.entity.CustomerJourneyStateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 客户旅程状态 Mapper。同一客户同一旅程只有一条记录。
 *
 * @author PSI
 */
@Mapper
public interface CustomerJourneyStateMapper extends BaseMapper<CustomerJourneyStateEntity> {

    @Select("SELECT * FROM customer_journey_state WHERE subject_id = #{customerId} AND subject_type = 'CUSTOMER' LIMIT 1")
    CustomerJourneyStateEntity selectByCustomerId(@Param("customerId") Long customerId);

    @Select("SELECT t.tag_name FROM customer_tag t " +
            "INNER JOIN customer_tag_rel r ON r.tag_code = t.tag_code " +
            "WHERE r.customer_id = #{customerId}")
    List<String> selectTagNamesByCustomerId(@Param("customerId") Long customerId);
}
