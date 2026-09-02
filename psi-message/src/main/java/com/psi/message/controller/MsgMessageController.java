package com.psi.message.controller;

import com.psi.message.dto.MsgMessageDTO;
import com.psi.message.dto.MsgMessageQueryDTO;
import com.psi.message.dto.MsgMessageSaveDTO;
import com.psi.message.service.MsgMessageService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/message")
public class MsgMessageController {

    private final MsgMessageService msgMessageService;

    public MsgMessageController(MsgMessageService msgMessageService) {
        this.msgMessageService = msgMessageService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResult<MsgMessageDTO>> getById(@PathVariable Long id) {
        CommonResult<MsgMessageDTO> result = msgMessageService.getById(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/list")
    public ResponseEntity<CommonResult<PageResult<MsgMessageDTO>>> list(MsgMessageQueryDTO queryDTO) {
        PageResult<MsgMessageDTO> result = msgMessageService.list(queryDTO);
        return ResponseEntity.ok(CommonResult.success(result));
    }

    @PostMapping
    public ResponseEntity<CommonResult<MsgMessageDTO>> save(@Valid @RequestBody MsgMessageSaveDTO saveDTO) {
        CommonResult<MsgMessageDTO> result = msgMessageService.save(saveDTO);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResult<MsgMessageDTO>> update(@PathVariable Long id, @Valid @RequestBody MsgMessageSaveDTO saveDTO) {
        CommonResult<MsgMessageDTO> result = msgMessageService.update(id, saveDTO);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResult<Void>> delete(@PathVariable Long id) {
        CommonResult<Void> result = msgMessageService.delete(id);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CommonResult<Void>> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        CommonResult<Void> result = msgMessageService.updateStatus(id, status);
        return ResponseEntity.ok(result);
    }
}