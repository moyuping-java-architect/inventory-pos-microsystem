package com.psi.message.controller;

import com.psi.message.dto.MsgDeadLetterDTO;
import com.psi.message.dto.MsgDeadLetterQueryDTO;
import com.psi.message.dto.MsgDeadLetterSaveDTO;
import com.psi.message.service.MsgDeadLetterService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/dead-letter")
public class MsgDeadLetterController {

    private final MsgDeadLetterService msgDeadLetterService;

    public MsgDeadLetterController(MsgDeadLetterService msgDeadLetterService) {
        this.msgDeadLetterService = msgDeadLetterService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResult<MsgDeadLetterDTO>> getById(@PathVariable Long id) {
        CommonResult<MsgDeadLetterDTO> result = msgDeadLetterService.getById(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/list")
    public ResponseEntity<CommonResult<PageResult<MsgDeadLetterDTO>>> list(MsgDeadLetterQueryDTO queryDTO) {
        PageResult<MsgDeadLetterDTO> result = msgDeadLetterService.list(queryDTO);
        return ResponseEntity.ok(CommonResult.success(result));
    }

    @PostMapping
    public ResponseEntity<CommonResult<MsgDeadLetterDTO>> save(@Valid @RequestBody MsgDeadLetterSaveDTO saveDTO) {
        CommonResult<MsgDeadLetterDTO> result = msgDeadLetterService.save(saveDTO);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResult<MsgDeadLetterDTO>> update(@PathVariable Long id, @Valid @RequestBody MsgDeadLetterSaveDTO saveDTO) {
        CommonResult<MsgDeadLetterDTO> result = msgDeadLetterService.update(id, saveDTO);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResult<Void>> delete(@PathVariable Long id) {
        CommonResult<Void> result = msgDeadLetterService.delete(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 重试死信消息
     * 
     * @param id 死信ID
     * @return 操作结果
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<CommonResult<Void>> retry(@PathVariable Long id) {
        CommonResult<Void> result = msgDeadLetterService.retry(id);
        return ResponseEntity.ok(result);
    }
}