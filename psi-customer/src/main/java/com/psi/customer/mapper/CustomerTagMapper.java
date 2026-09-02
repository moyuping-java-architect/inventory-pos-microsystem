package com.psi.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.customer.entity.CustomerTagEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 客户标签字典 Mapper
 */
@Mapper
public interface CustomerTagMapper extends BaseMapper<CustomerTagEntity> {

    /**
     * 查询某客户的所有标签
     */
    @Select("SELECT t.* FROM customer_tag t " +
            "INNER JOIN customer_tag_rel r ON r.tag_code = t.tag_code AND r.tenant_id = t.tenant_id AND r.del_flag = 0 " +
            "WHERE t.del_flag = 0 AND t.tenant_id = #{tenantId} AND r.customer_id = #{customerId} " +
            "ORDER BY t.sort ASC, t.id ASC")
    List<CustomerTagEntity> selectTagsByCustomerId(@Param("tenantId") String tenantId, @Param("customerId") Long customerId);

    /**
     * 查询某客户的标签编码列表
     */
    @Select("SELECT tag_code FROM customer_tag_rel " +
            "WHERE del_flag = 0 AND tenant_id = #{tenantId} AND customer_id = #{customerId}")
    List<String> selectTagCodesByCustomerId(@Param("tenantId") String tenantId, @Param("customerId") Long customerId);

    /**
     * 按编码查询标签
     */
    @Select("SELECT * FROM customer_tag " +
            "WHERE del_flag = 0 AND tenant_id = #{tenantId} AND tag_code = #{tagCode} LIMIT 1")
    CustomerTagEntity selectByCode(@Param("tenantId") String tenantId, @Param("tagCode") String tagCode);
}
