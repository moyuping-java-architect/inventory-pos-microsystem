package com.psi.message.service.impl;

import com.psi.message.dto.MsgMessageDTO;
import com.psi.message.dto.MsgMessageQueryDTO;
import com.psi.message.dto.MsgMessageSaveDTO;
import com.psi.message.entity.MsgMessage;
import com.psi.message.mapper.MsgMessageMapper;
import com.psi.message.service.MsgMessageService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MsgMessageServiceImpl implements MsgMessageService {

    private final MsgMessageMapper msgMessageMapper;

    public MsgMessageServiceImpl(MsgMessageMapper msgMessageMapper) {
        this.msgMessageMapper = msgMessageMapper;
    }

    @Override
    public CommonResult<MsgMessageDTO> getById(Long id) {
        MsgMessage message = msgMessageMapper.selectById(id);
        if (message == null) {
            return CommonResult.fail("消息不存在");
        }
        return CommonResult.success(BeanUtils.convert(message, MsgMessageDTO.class));
    }

    @Override
    public PageResult<MsgMessageDTO> list(MsgMessageQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        LambdaQueryWrapper<MsgMessage> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getMessageId())) {
            queryWrapper.eq(MsgMessage::getMessageId, queryDTO.getMessageId());
        }
        if (StringUtils.hasText(queryDTO.getTenantId())) {
            queryWrapper.eq(MsgMessage::getTenantId, queryDTO.getTenantId());
        }
        if (StringUtils.hasText(queryDTO.getOperatorId())) {
            queryWrapper.eq(MsgMessage::getOperatorId, queryDTO.getOperatorId());
        }
        if (StringUtils.hasText(queryDTO.getSourceService())) {
            queryWrapper.eq(MsgMessage::getSourceService, queryDTO.getSourceService());
        }
        if (StringUtils.hasText(queryDTO.getExchangeName())) {
            queryWrapper.eq(MsgMessage::getExchangeName, queryDTO.getExchangeName());
        }
        if (StringUtils.hasText(queryDTO.getRoutingKey())) {
            queryWrapper.like(MsgMessage::getRoutingKey, queryDTO.getRoutingKey());
        }
        if (StringUtils.hasText(queryDTO.getEventType())) {
            queryWrapper.eq(MsgMessage::getEventType, queryDTO.getEventType());
        }
        if (queryDTO.getMsgStatus() != null) {
            queryWrapper.eq(MsgMessage::getMsgStatus, queryDTO.getMsgStatus());
        }
        queryWrapper.eq(MsgMessage::getDelFlag, 0);

        IPage<MsgMessage> page = msgMessageMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        return PageResult.success(BeanUtils.convertList(page.getRecords(), MsgMessageDTO.class), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public CommonResult<MsgMessageDTO> save(MsgMessageSaveDTO saveDTO) {
        MsgMessage message = BeanUtils.convert(saveDTO, MsgMessage.class);
        
        // 转换 tenantId
        if (saveDTO.getTenantId() != null && !saveDTO.getTenantId().isEmpty()) {
            try {
                message.setTenantId(Long.parseLong(saveDTO.getTenantId()));
            } catch (NumberFormatException e) {
                message.setTenantId(0L);
            }
        } else {
            message.setTenantId(0L);
        }
        
        if (!StringUtils.hasText(message.getMessageId())) {
            message.setMessageId(java.util.UUID.randomUUID().toString().replace("-", ""));
        }
        if (message.getMsgStatus() == null) {
            message.setMsgStatus(0);
        }
        if (message.getSendTime() == null) {
            message.setSendTime(System.currentTimeMillis());
        }
        
        // 手动设置自动填充字段（解决自动填充可能失效的问题）
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        message.setCreateTime(now);
        message.setUpdateTime(now);
        message.setCreateBy(0L);
        message.setUpdateBy(0L);
        message.setDelFlag(0);
        message.setStatus(1);
        
        msgMessageMapper.insert(message);
        return CommonResult.success(BeanUtils.convert(message, MsgMessageDTO.class));
    }

    @Override
    public CommonResult<MsgMessageDTO> update(Long id, MsgMessageSaveDTO saveDTO) {
        MsgMessage message = msgMessageMapper.selectById(id);
        if (message == null) {
            return CommonResult.fail("消息不存在");
        }
        BeanUtils.copyProperties(saveDTO, message);
        msgMessageMapper.updateById(message);
        return CommonResult.success(BeanUtils.convert(message, MsgMessageDTO.class));
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        MsgMessage message = msgMessageMapper.selectById(id);
        if (message == null) {
            return CommonResult.fail("消息不存在");
        }
        message.setDelFlag(1);
        msgMessageMapper.updateById(message);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        MsgMessage message = msgMessageMapper.selectById(id);
        if (message == null) {
            return CommonResult.fail("消息不存在");
        }
        message.setMsgStatus(status);
        msgMessageMapper.updateById(message);
        return CommonResult.success();
    }
}