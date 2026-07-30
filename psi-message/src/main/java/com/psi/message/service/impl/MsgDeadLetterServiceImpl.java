package com.psi.message.service.impl;

import com.psi.message.dto.MsgDeadLetterDTO;
import com.psi.message.dto.MsgDeadLetterQueryDTO;
import com.psi.message.dto.MsgDeadLetterSaveDTO;
import com.psi.message.entity.MsgDeadLetter;
import com.psi.message.mapper.MsgDeadLetterMapper;
import com.psi.message.service.MsgDeadLetterService;
import com.psi.common.message.MqCommonMessage;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.util.BeanUtils;
import com.psi.common.util.JsonUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
public class MsgDeadLetterServiceImpl implements MsgDeadLetterService {

    private final MsgDeadLetterMapper msgDeadLetterMapper;
    private final RabbitTemplate rabbitTemplate;

    public MsgDeadLetterServiceImpl(MsgDeadLetterMapper msgDeadLetterMapper, RabbitTemplate rabbitTemplate) {
        this.msgDeadLetterMapper = msgDeadLetterMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public CommonResult<MsgDeadLetterDTO> getById(Long id) {
        MsgDeadLetter deadLetter = msgDeadLetterMapper.selectById(id);
        if (deadLetter == null) {
            return CommonResult.fail("死信不存在");
        }
        return CommonResult.success(BeanUtils.convert(deadLetter, MsgDeadLetterDTO.class));
    }

    @Override
    public PageResult<MsgDeadLetterDTO> list(MsgDeadLetterQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        LambdaQueryWrapper<MsgDeadLetter> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getMessageId())) {
            queryWrapper.eq(MsgDeadLetter::getMessageId, queryDTO.getMessageId());
        }
        if (StringUtils.hasText(queryDTO.getOriginalTopic())) {
            queryWrapper.like(MsgDeadLetter::getOriginalTopic, queryDTO.getOriginalTopic());
        }
        if (StringUtils.hasText(queryDTO.getSender())) {
            queryWrapper.eq(MsgDeadLetter::getSender, queryDTO.getSender());
        }
        if (StringUtils.hasText(queryDTO.getReceiver())) {
            queryWrapper.eq(MsgDeadLetter::getReceiver, queryDTO.getReceiver());
        }

        IPage<MsgDeadLetter> page = msgDeadLetterMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        return PageResult.success(BeanUtils.convertList(page.getRecords(), MsgDeadLetterDTO.class), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public CommonResult<MsgDeadLetterDTO> save(MsgDeadLetterSaveDTO saveDTO) {
        MsgDeadLetter deadLetter = BeanUtils.convert(saveDTO, MsgDeadLetter.class);
        msgDeadLetterMapper.insert(deadLetter);
        return CommonResult.success(BeanUtils.convert(deadLetter, MsgDeadLetterDTO.class));
    }

    @Override
    public CommonResult<MsgDeadLetterDTO> update(Long id, MsgDeadLetterSaveDTO saveDTO) {
        MsgDeadLetter deadLetter = msgDeadLetterMapper.selectById(id);
        if (deadLetter == null) {
            return CommonResult.fail("死信不存在");
        }
        BeanUtils.copyProperties(saveDTO, deadLetter);
        msgDeadLetterMapper.updateById(deadLetter);
        return CommonResult.success(BeanUtils.convert(deadLetter, MsgDeadLetterDTO.class));
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        MsgDeadLetter deadLetter = msgDeadLetterMapper.selectById(id);
        if (deadLetter == null) {
            return CommonResult.fail("死信不存在");
        }
        msgDeadLetterMapper.deleteById(id);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> retry(Long id) {
        // 1. 获取死信记录
        MsgDeadLetter deadLetter = msgDeadLetterMapper.selectById(id);
        if (deadLetter == null) {
            return CommonResult.fail("死信不存在");
        }

        try {
            // 2. 解析原始交换机和路由键（格式: exchange/routingKey）
            String originalTopic = deadLetter.getOriginalTopic();
            if (!StringUtils.hasText(originalTopic) || !originalTopic.contains("/")) {
                return CommonResult.fail("无法解析原始路由信息");
            }

            String[] parts = originalTopic.split("/", 2);
            String exchange = parts[0];
            String routingKey = parts[1];

            // 3. 解析消息内容为 MqCommonMessage
            String content = deadLetter.getContent();
            MqCommonMessage<?> message = JsonUtils.fromJson(content, MqCommonMessage.class);
            if (message == null) {
                return CommonResult.fail("无法解析消息内容");
            }

            // 4. 重新发送消息到原始队列
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("Dead letter retry success: id={}, messageId={}, exchange={}, routingKey={}",
                    id, deadLetter.getMessageId(), exchange, routingKey);

            // 5. 手动重试成功：失败次数清零，标记为已处理
            deadLetter.setFailedCount(0);
            deadLetter.setRetryable(0);
            deadLetter.setNextRetryTime(null);
            deadLetter.setLastFailedTime(null);
            msgDeadLetterMapper.updateById(deadLetter);

            return CommonResult.success();

        } catch (Exception e) {
            log.error("Failed to retry dead letter: id={}", id, e);
            // 手动重试失败：增加失败次数，5 分钟后可再次手动/自动重试
            deadLetter.setFailedCount(deadLetter.getFailedCount() + 1);
            deadLetter.setNextRetryTime(LocalDateTime.now().plusMinutes(5));
            deadLetter.setLastFailedTime(LocalDateTime.now());
            msgDeadLetterMapper.updateById(deadLetter);
            return CommonResult.fail("重试失败: " + e.getMessage());
        }
    }
}