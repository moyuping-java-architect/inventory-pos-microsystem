package com.psi.message.controller;

import com.psi.message.dto.MsgDeadLetterTodoDTO;
import com.psi.message.dto.MsgDeadLetterTodoQueryDTO;
import com.psi.message.dto.MsgDeadLetterTodoSaveDTO;
import com.psi.message.service.MsgDeadLetterTodoService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psi/dead-letter-todo")
public class MsgDeadLetterTodoController {

    private final MsgDeadLetterTodoService msgDeadLetterTodoService;

    public MsgDeadLetterTodoController(MsgDeadLetterTodoService msgDeadLetterTodoService) {
        this.msgDeadLetterTodoService = msgDeadLetterTodoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResult<MsgDeadLetterTodoDTO>> getById(@PathVariable Long id) {
        CommonResult<MsgDeadLetterTodoDTO> result = msgDeadLetterTodoService.getById(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/list")
    public ResponseEntity<CommonResult<PageResult<MsgDeadLetterTodoDTO>>> list(MsgDeadLetterTodoQueryDTO queryDTO) {
        PageResult<MsgDeadLetterTodoDTO> result = msgDeadLetterTodoService.list(queryDTO);
        return ResponseEntity.ok(CommonResult.success(result));
    }

    @PostMapping
    public ResponseEntity<CommonResult<MsgDeadLetterTodoDTO>> save(@Valid @RequestBody MsgDeadLetterTodoSaveDTO saveDTO) {
        CommonResult<MsgDeadLetterTodoDTO> result = msgDeadLetterTodoService.save(saveDTO);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResult<MsgDeadLetterTodoDTO>> update(@PathVariable Long id, @Valid @RequestBody MsgDeadLetterTodoSaveDTO saveDTO) {
        CommonResult<MsgDeadLetterTodoDTO> result = msgDeadLetterTodoService.update(id, saveDTO);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResult<Void>> delete(@PathVariable Long id) {
        CommonResult<Void> result = msgDeadLetterTodoService.delete(id);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/process-status")
    public ResponseEntity<CommonResult<Void>> updateProcessStatus(@PathVariable Long id, @RequestParam Integer processStatus) {
        CommonResult<Void> result = msgDeadLetterTodoService.updateProcessStatus(id, processStatus);
        return ResponseEntity.ok(result);
    }
}