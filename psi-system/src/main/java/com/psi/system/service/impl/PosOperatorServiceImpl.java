package com.psi.system.service.impl;

import com.psi.system.dto.PosOperatorDTO;
import com.psi.system.dto.PosOperatorQueryDTO;
import com.psi.system.dto.PosOperatorSaveDTO;
import com.psi.system.entity.PosOperator;
import com.psi.system.mapper.PosOperatorMapper;
import com.psi.system.mq.producer.PosDownSyncProducer;
import com.psi.system.service.PosOperatorService;
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
public class PosOperatorServiceImpl implements PosOperatorService {

    private final PosOperatorMapper posOperatorMapper;
    private final PosDownSyncProducer posDownSyncProducer;

    public PosOperatorServiceImpl(PosOperatorMapper posOperatorMapper, PosDownSyncProducer posDownSyncProducer) {
        this.posOperatorMapper = posOperatorMapper;
        this.posDownSyncProducer = posDownSyncProducer;
    }

    @Override
    public CommonResult<PosOperatorDTO> getById(Long id) {
        PosOperator posOperator = posOperatorMapper.selectById(id);
        if (posOperator == null) {
            return CommonResult.fail("收银员不存在");
        }
        PosOperatorDTO dto = BeanUtils.convert(posOperator, PosOperatorDTO.class);
        if (dto == null) {
            dto = new PosOperatorDTO();
        }
        return CommonResult.success(dto);
    }

    @Override
    public PageResult<PosOperatorDTO> list(PosOperatorQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        LambdaQueryWrapper<PosOperator> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getShopCode())) {
            wrapper.like(PosOperator::getShopCode, queryDTO.getShopCode());
        }
        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(PosOperator::getUsername, queryDTO.getUsername());
        }
        if (StringUtils.hasText(queryDTO.getRealName())) {
            wrapper.like(PosOperator::getRealName, queryDTO.getRealName());
        }
        if (queryDTO.getRole() != null) {
            wrapper.eq(PosOperator::getRole, queryDTO.getRole());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(PosOperator::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(PosOperator::getCreateTime);

        IPage<PosOperator> page = posOperatorMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<PosOperator> posOperators = page.getRecords();

        List<PosOperatorDTO> dtoList = new ArrayList<>();
        if (posOperators != null) {
            for (PosOperator posOperator : posOperators) {
                PosOperatorDTO dto = BeanUtils.convert(posOperator, PosOperatorDTO.class);
                if (dto == null) {
                    dto = new PosOperatorDTO();
                }
                dtoList.add(dto);
            }
        }

        return PageResult.success(dtoList, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public CommonResult<PosOperatorDTO> save(PosOperatorSaveDTO saveDTO) {
        PosOperator posOperator = BeanUtils.convert(saveDTO, PosOperator.class);
        posOperatorMapper.insert(posOperator);
        // 发送下行同步消息
        posDownSyncProducer.sendPosOperator(posOperator);
        return CommonResult.success(BeanUtils.convert(posOperator, PosOperatorDTO.class));
    }

    @Override
    public CommonResult<PosOperatorDTO> update(Long id, PosOperatorSaveDTO saveDTO) {
        PosOperator posOperator = posOperatorMapper.selectById(id);
        if (posOperator == null) {
            return CommonResult.fail("收银员不存在");
        }
        BeanUtils.copyProperties(saveDTO, posOperator);
        posOperatorMapper.updateById(posOperator);
        // 发送下行同步消息
        posDownSyncProducer.sendPosOperator(posOperator);
        return CommonResult.success(BeanUtils.convert(posOperator, PosOperatorDTO.class));
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        PosOperator posOperator = posOperatorMapper.selectById(id);
        if (posOperator == null) {
            return CommonResult.fail("收银员不存在");
        }
        posOperator.setDelFlag(1);
        posOperatorMapper.updateById(posOperator);
        // 发送下行同步消息（删除标记）
        posDownSyncProducer.sendPosOperator(posOperator);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        PosOperator posOperator = posOperatorMapper.selectById(id);
        if (posOperator == null) {
            return CommonResult.fail("收银员不存在");
        }
        posOperator.setStatus(status);
        posOperatorMapper.updateById(posOperator);
        // 发送下行同步消息
        posDownSyncProducer.sendPosOperator(posOperator);
        return CommonResult.success();
    }
}