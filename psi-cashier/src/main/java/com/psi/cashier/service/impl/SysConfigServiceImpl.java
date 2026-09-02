package com.psi.cashier.service.impl;

import com.psi.cashier.entity.SysConfigEntity;
import com.psi.cashier.mapper.SysConfigMapper;
import com.psi.cashier.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 系统配置服务实现类
 * 系统配置表只能存储一条记录
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Service
public class SysConfigServiceImpl implements SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 强制唯一约束字段的值
     */
    private static final int ONLY_ONE_VALUE = 1;

    public SysConfigServiceImpl(SysConfigMapper sysConfigMapper) {
        this.sysConfigMapper = sysConfigMapper;
    }

    @Override
    public SysConfigEntity getConfig() {
        return sysConfigMapper.selectFirst();
    }

    @Override
    public SysConfigEntity getByPosSn(String posSn) {
        return sysConfigMapper.selectByPosSn(posSn);
    }

    @Override
    public SysConfigEntity getByPosId(String posId) {
        return sysConfigMapper.selectByPosId(posId);
    }

    @Override
    public boolean exists() {
        return sysConfigMapper.count() > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysConfigEntity saveOrUpdateConfig(SysConfigEntity entity) {
        String currentTime = LocalDateTime.now().format(DATETIME_FORMATTER);
        
        // 强制设置唯一约束字段
        entity.setOnlyOne(ONLY_ONE_VALUE);
        entity.setUpdateTime(currentTime);
        
        SysConfigEntity existing = sysConfigMapper.selectFirst();
        
        if (existing != null) {
            // 更新现有配置
            entity.setId(existing.getId());
            sysConfigMapper.updateById(entity);
            log.info("更新系统配置成功，收银机编号：{}", entity.getPosId());
        } else {
            // 新增配置
            sysConfigMapper.insert(entity);
            log.info("新增系统配置成功，收银机编号：{}", entity.getPosId());
        }
        
        return entity;
    }
}