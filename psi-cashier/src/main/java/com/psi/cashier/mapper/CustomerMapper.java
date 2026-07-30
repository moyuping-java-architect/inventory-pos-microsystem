package com.psi.cashier.mapper;

import com.psi.cashier.entity.CustomerEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 客户 Mapper
 */
@Mapper
public interface CustomerMapper extends BaseMapper<CustomerEntity> {

    @Select("SELECT * FROM customer WHERE del_flag = 0 AND status = 1 " +
            "AND (customer_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR contact_phone LIKE CONCAT('%', #{keyword}, '%') " +
            "OR customer_code LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY customer_code")
    List<CustomerEntity> searchByKeyword(@Param("keyword") String keyword);

    @Select("SELECT * FROM customer WHERE data_uuid = #{dataUuid} AND del_flag = 0")
    CustomerEntity selectByDataUuid(@Param("dataUuid") String dataUuid);

    /**
     * 批量插入客户（单SQL多VALUES，真正的一次批量写入）
     */
    @Insert("<script>" +
            "INSERT INTO customer (data_uuid, tenant_id, customer_code, customer_name, short_name, " +
            "contact_name, contact_phone, email, address, province, city, district, zip_code, " +
            "tax_no, bank_name, bank_account, customer_type, customer_level, credit_limit, remark, " +
            "del_flag, status, create_time, update_time) VALUES " +
            "<foreach collection='entities' item='e' separator=','>" +
            "(#{e.dataUuid}, #{e.tenantId}, #{e.customerCode}, #{e.customerName}, #{e.shortName}, " +
            "#{e.contactName}, #{e.contactPhone}, #{e.email}, #{e.address}, #{e.province}, #{e.city}, #{e.district}, #{e.zipCode}, " +
            "#{e.taxNo}, #{e.bankName}, #{e.bankAccount}, #{e.customerType}, #{e.customerLevel}, #{e.creditLimit}, #{e.remark}, " +
            "#{e.delFlag}, #{e.status}, #{e.createTime}, #{e.updateTime})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("entities") List<CustomerEntity> entities);
}