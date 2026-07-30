package com.psi.message.service;

import com.psi.message.dto.MsgDeadLetterDTO;
import com.psi.message.dto.MsgDeadLetterQueryDTO;
import com.psi.message.dto.MsgDeadLetterSaveDTO;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;

public interface MsgDeadLetterService {

    CommonResult<MsgDeadLetterDTO> getById(Long id);

    PageResult<MsgDeadLetterDTO> list(MsgDeadLetterQueryDTO queryDTO);

    CommonResult<MsgDeadLetterDTO> save(MsgDeadLetterSaveDTO saveDTO);

    CommonResult<MsgDeadLetterDTO> update(Long id, MsgDeadLetterSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    /**
     * 重试死信消息
     * 
     * @param id 死信ID
     * @return 操作结果
     */
    CommonResult<Void> retry(Long id);
}