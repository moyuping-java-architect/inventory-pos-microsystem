package com.psi.message.service;

import com.psi.message.dto.MsgMessageDTO;
import com.psi.message.dto.MsgMessageQueryDTO;
import com.psi.message.dto.MsgMessageSaveDTO;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;

public interface MsgMessageService {

    CommonResult<MsgMessageDTO> getById(Long id);

    PageResult<MsgMessageDTO> list(MsgMessageQueryDTO queryDTO);

    CommonResult<MsgMessageDTO> save(MsgMessageSaveDTO saveDTO);

    CommonResult<MsgMessageDTO> update(Long id, MsgMessageSaveDTO saveDTO);

    CommonResult<Void> delete(Long id);

    CommonResult<Void> updateStatus(Long id, Integer status);
}