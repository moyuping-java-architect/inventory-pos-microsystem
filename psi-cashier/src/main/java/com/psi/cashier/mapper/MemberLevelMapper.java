package com.psi.cashier.mapper;

import com.psi.cashier.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 会员等级Mapper接口
 */
@Mapper
public interface MemberLevelMapper extends BaseMapper<MemberLevelEntity> {

    /**
     * 根据等级ID查询
     */
    @Select("SELECT * FROM member_level WHERE tenant_id = #{tenantId} AND level_id = #{levelId} AND del_flag = 0")
    MemberLevelEntity selectByLevelId(@Param("tenantId") String tenantId, @Param("levelId") Integer levelId);

    /**
     * 查询最低等级（初始等级）
     */
    @Select("SELECT * FROM member_level WHERE tenant_id = #{tenantId} AND del_flag = 0 ORDER BY level_id ASC LIMIT 1")
    MemberLevelEntity selectMinLevel(@Param("tenantId") String tenantId);
}