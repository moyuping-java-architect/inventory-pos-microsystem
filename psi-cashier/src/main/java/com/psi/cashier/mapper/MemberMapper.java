package com.psi.cashier.mapper;

import com.psi.cashier.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 会员Mapper接口
 */
@Mapper
public interface MemberMapper extends BaseMapper<MemberEntity> {

    /**
     * 根据手机号查询会员
     */
    @Select("SELECT * FROM member WHERE tenant_id = #{tenantId} AND phone = #{phone} AND del_flag = 0")
    MemberEntity selectByPhone(@Param("tenantId") String tenantId, @Param("phone") String phone);

    /**
     * 根据数据UUID查询会员
     */
    @Select("SELECT * FROM member WHERE tenant_id = #{tenantId} AND data_uuid = #{dataUuid} AND del_flag = 0")
    MemberEntity selectByDataUuid(@Param("tenantId") String tenantId, @Param("dataUuid") String dataUuid);
}