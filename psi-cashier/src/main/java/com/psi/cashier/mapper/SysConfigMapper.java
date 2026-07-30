package com.psi.cashier.mapper;

import com.psi.cashier.entity.SysConfigEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统配置 Mapper 接口
 * 系统配置表只能存储一条记录
 * 
 * @author PSI
 * @version 1.0.0
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfigEntity> {

    /**
     * 查询唯一的系统配置
     *
     * @return 系统配置实体
     */
    @Select("SELECT * FROM sys_config LIMIT 1")
    SysConfigEntity selectFirst();

    /**
     * 根据POS硬件序列号查询配置
     *
     * @param posSn POS硬件序列号
     * @return 系统配置实体
     */
    @Select("SELECT * FROM sys_config WHERE pos_sn = #{posSn}")
    SysConfigEntity selectByPosSn(@Param("posSn") String posSn);

    /**
     * 根据收银机编号查询配置
     *
     * @param posId 收银机编号
     * @return 系统配置实体
     */
    @Select("SELECT * FROM sys_config WHERE pos_id = #{posId}")
    SysConfigEntity selectByPosId(@Param("posId") String posId);

    /**
     * 检查配置是否存在
     *
     * @return 记录数量
     */
    @Select("SELECT COUNT(*) FROM sys_config")
    int count();
}