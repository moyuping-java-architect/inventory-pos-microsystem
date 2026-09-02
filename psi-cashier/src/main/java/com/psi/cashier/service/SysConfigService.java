package com.psi.cashier.service;

import com.psi.cashier.entity.SysConfigEntity;

/**
 * 系统配置服务接口
 * 系统配置表只能存储一条记录
 * 
 * @author PSI
 * @version 1.0.0
 */
public interface SysConfigService {

    /**
     * 获取系统配置（唯一记录）
     *
     * @return 系统配置实体，如果不存在返回null
     */
    SysConfigEntity getConfig();

    /**
     * 根据POS硬件序列号查询配置
     *
     * @param posSn POS硬件序列号
     * @return 系统配置实体
     */
    SysConfigEntity getByPosSn(String posSn);

    /**
     * 根据收银机编号查询配置
     *
     * @param posId 收银机编号
     * @return 系统配置实体
     */
    SysConfigEntity getByPosId(String posId);

    /**
     * 检查配置是否存在
     *
     * @return 是否存在配置
     */
    boolean exists();

    /**
     * 保存或更新配置
     * 如果已存在则更新，否则新增（自动设置 onlyOne = 1）
     *
     * @param entity 系统配置实体
     * @return 保存后的实体
     */
    SysConfigEntity saveOrUpdateConfig(SysConfigEntity entity);
}