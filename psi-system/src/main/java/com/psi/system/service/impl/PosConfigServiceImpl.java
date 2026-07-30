package com.psi.system.service.impl;

import com.psi.system.dto.PosConfigDTO;
import com.psi.system.dto.PosConfigQueryDTO;
import com.psi.system.dto.PosConfigSaveDTO;
import com.psi.system.entity.PosConfig;
import com.psi.system.mapper.PosConfigMapper;
import com.psi.system.mq.producer.PosDownSyncProducer;
import com.psi.system.service.PosConfigService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class PosConfigServiceImpl implements PosConfigService {

    private final PosConfigMapper posConfigMapper;
    private final PosDownSyncProducer posDownSyncProducer;

    public PosConfigServiceImpl(PosConfigMapper posConfigMapper, PosDownSyncProducer posDownSyncProducer) {
        this.posConfigMapper = posConfigMapper;
        this.posDownSyncProducer = posDownSyncProducer;
    }

    @Override
    public CommonResult<PosConfigDTO> getById(Long id) {
        PosConfig posConfig = posConfigMapper.selectById(id);
        if (posConfig == null) {
            return CommonResult.fail("收银机配置不存在");
        }
        PosConfigDTO dto = BeanUtils.convert(posConfig, PosConfigDTO.class);
        if (dto == null) {
            dto = new PosConfigDTO();
        }
        return CommonResult.success(dto);
    }

    @Override
    public PageResult<PosConfigDTO> list(PosConfigQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        LambdaQueryWrapper<PosConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getPosSn())) {
            wrapper.like(PosConfig::getPosSn, queryDTO.getPosSn());
        }
        if (StringUtils.hasText(queryDTO.getShopCode())) {
            wrapper.like(PosConfig::getShopCode, queryDTO.getShopCode());
        }
        if (StringUtils.hasText(queryDTO.getPosId())) {
            wrapper.like(PosConfig::getPosId, queryDTO.getPosId());
        }
        if (StringUtils.hasText(queryDTO.getPosName())) {
            wrapper.like(PosConfig::getPosName, queryDTO.getPosName());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(PosConfig::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(PosConfig::getCreateTime);

        IPage<PosConfig> page = posConfigMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<PosConfig> posConfigs = page.getRecords();

        List<PosConfigDTO> dtoList = new ArrayList<>();
        if (posConfigs != null) {
            for (PosConfig posConfig : posConfigs) {
                PosConfigDTO dto = BeanUtils.convert(posConfig, PosConfigDTO.class);
                if (dto == null) {
                    dto = new PosConfigDTO();
                }
                dtoList.add(dto);
            }
        }

        return PageResult.success(dtoList, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public CommonResult<PosConfigDTO> save(PosConfigSaveDTO saveDTO) {
        PosConfig posConfig = BeanUtils.convert(saveDTO, PosConfig.class);
        posConfigMapper.insert(posConfig);
        // 发送下行同步消息
        posDownSyncProducer.sendPosConfig(posConfig);
        return CommonResult.success(BeanUtils.convert(posConfig, PosConfigDTO.class));
    }

    @Override
    public CommonResult<PosConfigDTO> update(Long id, PosConfigSaveDTO saveDTO) {
        PosConfig posConfig = posConfigMapper.selectById(id);
        if (posConfig == null) {
            return CommonResult.fail("收银机配置不存在");
        }
        BeanUtils.copyProperties(saveDTO, posConfig);
        posConfigMapper.updateById(posConfig);
        // 发送下行同步消息
        posDownSyncProducer.sendPosConfig(posConfig);
        return CommonResult.success(BeanUtils.convert(posConfig, PosConfigDTO.class));
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        PosConfig posConfig = posConfigMapper.selectById(id);
        if (posConfig == null) {
            return CommonResult.fail("收银机配置不存在");
        }
        posConfig.setDelFlag(1);
        posConfigMapper.updateById(posConfig);
        // 发送下行同步消息（删除标记）
        posDownSyncProducer.sendPosConfig(posConfig);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        PosConfig posConfig = posConfigMapper.selectById(id);
        if (posConfig == null) {
            return CommonResult.fail("收银机配置不存在");
        }
        posConfig.setStatus(status);
        posConfigMapper.updateById(posConfig);
        // 发送下行同步消息
        posDownSyncProducer.sendPosConfig(posConfig);
        return CommonResult.success();
    }
}