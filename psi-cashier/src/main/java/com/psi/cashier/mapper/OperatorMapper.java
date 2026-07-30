package com.psi.cashier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.cashier.entity.OperatorEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作员Mapper
 */
@Mapper
public interface OperatorMapper extends BaseMapper<OperatorEntity> {

    /**
     * 根据用户名查询操作员（排除已删除的）
     */
    @Select("SELECT * FROM operator WHERE username = #{username} AND del_flag = 0 AND status = 1")
    OperatorEntity selectByUsername(@Param("username") String username);

    /**
     * 根据用户名和门店编码查询操作员
     */
    @Select("SELECT * FROM operator WHERE username = #{username} AND shop_code = #{shopCode} AND del_flag = 0 AND status = 1")
    OperatorEntity selectByUsernameAndShopCode(@Param("username") String username, @Param("shopCode") String shopCode);

    /**
     * 根据数据UUID查询
     */
    @Select("SELECT * FROM operator WHERE data_uuid = #{dataUuid} AND del_flag = 0")
    OperatorEntity selectByDataUuid(@Param("dataUuid") String dataUuid);

    /**
     * 批量插入操作员（单SQL多VALUES，真正的一次批量写入）
     */
    @Insert("<script>" +
            "INSERT INTO operator (data_uuid, tenant_id, shop_code, username, del_flag, password, real_name, role, status, create_time) VALUES " +
            "<foreach collection='entities' item='e' separator=','>" +
            "(#{e.dataUuid}, #{e.tenantId}, #{e.shopCode}, #{e.username}, #{e.delFlag}, #{e.password}, #{e.realName}, #{e.role}, #{e.status}, #{e.createTime})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("entities") List<OperatorEntity> entities);
}