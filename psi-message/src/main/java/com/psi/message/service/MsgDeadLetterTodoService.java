package com.psi.message.service;

import com.psi.message.dto.MsgDeadLetterTodoDTO;
import com.psi.message.dto.MsgDeadLetterTodoQueryDTO;
import com.psi.message.dto.MsgDeadLetterTodoSaveDTO;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;

public interface MsgDeadLetterTodoService {

    CommonResult<MsgDeadLetterTodoDTO> getById(Long id);

    PageResult<MsgDeadLetterTodoDTO> list(MsgDeadLetterTodoQueryDTO queryDTO);

    CommonResult<MsgDeadLetterTodoDTO> save(MsgDeadLetterTodoSaveDTO saveDTO);

    CommonResult<MsgDeadLetterTodoDTO> update(Long id, MsgDeadLetterTodoSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateProcessStatus(Long id, Integer processStatus);
}