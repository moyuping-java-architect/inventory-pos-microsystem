package com.psi.message.service.impl;

import com.psi.message.dto.MsgDeadLetterTodoDTO;
import com.psi.message.dto.MsgDeadLetterTodoQueryDTO;
import com.psi.message.dto.MsgDeadLetterTodoSaveDTO;
import com.psi.message.entity.MsgDeadLetterTodo;
import com.psi.message.mapper.MsgDeadLetterTodoMapper;
import com.psi.message.service.MsgDeadLetterTodoService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class MsgDeadLetterTodoServiceImpl implements MsgDeadLetterTodoService {

    private final MsgDeadLetterTodoMapper msgDeadLetterTodoMapper;

    public MsgDeadLetterTodoServiceImpl(MsgDeadLetterTodoMapper msgDeadLetterTodoMapper) {
        this.msgDeadLetterTodoMapper = msgDeadLetterTodoMapper;
    }

    @Override
    public CommonResult<MsgDeadLetterTodoDTO> getById(Long id) {
        MsgDeadLetterTodo todo = msgDeadLetterTodoMapper.selectById(id);
        if (todo == null) {
            return CommonResult.fail("死信待办不存在");
        }
        return CommonResult.success(BeanUtils.convert(todo, MsgDeadLetterTodoDTO.class));
    }

    @Override
    public PageResult<MsgDeadLetterTodoDTO> list(MsgDeadLetterTodoQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        LambdaQueryWrapper<MsgDeadLetterTodo> queryWrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getDeadLetterId() != null) {
            queryWrapper.eq(MsgDeadLetterTodo::getDeadLetterId, queryDTO.getDeadLetterId());
        }
        if (StringUtils.hasText(queryDTO.getMessageId())) {
            queryWrapper.eq(MsgDeadLetterTodo::getMessageId, queryDTO.getMessageId());
        }
        if (StringUtils.hasText(queryDTO.getHandler())) {
            queryWrapper.eq(MsgDeadLetterTodo::getHandler, queryDTO.getHandler());
        }
        if (queryDTO.getProcessStatus() != null) {
            queryWrapper.eq(MsgDeadLetterTodo::getProcessStatus, queryDTO.getProcessStatus());
        }
        if (queryDTO.getHandleType() != null) {
            queryWrapper.eq(MsgDeadLetterTodo::getHandleType, queryDTO.getHandleType());
        }

        IPage<MsgDeadLetterTodo> page = msgDeadLetterTodoMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        return PageResult.success(BeanUtils.convertList(page.getRecords(), MsgDeadLetterTodoDTO.class), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public CommonResult<MsgDeadLetterTodoDTO> save(MsgDeadLetterTodoSaveDTO saveDTO) {
        MsgDeadLetterTodo todo = BeanUtils.convert(saveDTO, MsgDeadLetterTodo.class);
        todo.setProcessStatus(0);
        msgDeadLetterTodoMapper.insert(todo);
        return CommonResult.success(BeanUtils.convert(todo, MsgDeadLetterTodoDTO.class));
    }

    @Override
    public CommonResult<MsgDeadLetterTodoDTO> update(Long id, MsgDeadLetterTodoSaveDTO saveDTO) {
        MsgDeadLetterTodo todo = msgDeadLetterTodoMapper.selectById(id);
        if (todo == null) {
            return CommonResult.fail("死信待办不存在");
        }
        BeanUtils.copyProperties(saveDTO, todo);
        msgDeadLetterTodoMapper.updateById(todo);
        return CommonResult.success(BeanUtils.convert(todo, MsgDeadLetterTodoDTO.class));
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        MsgDeadLetterTodo todo = msgDeadLetterTodoMapper.selectById(id);
        if (todo == null) {
            return CommonResult.fail("死信待办不存在");
        }
        msgDeadLetterTodoMapper.deleteById(id);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateProcessStatus(Long id, Integer processStatus) {
        MsgDeadLetterTodo todo = msgDeadLetterTodoMapper.selectById(id);
        if (todo == null) {
            return CommonResult.fail("死信待办不存在");
        }
        todo.setProcessStatus(processStatus);
        if (processStatus == 2) {
            todo.setHandleTime(LocalDateTime.now());
        }
        msgDeadLetterTodoMapper.updateById(todo);
        return CommonResult.success();
    }
}