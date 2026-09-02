package com.psi.cashier.controller;

import com.psi.cashier.service.DataBackupService;
import com.psi.common.result.CommonResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cashier/backup")
public class DataBackupController {

    private final DataBackupService dataBackupService;

    public DataBackupController(DataBackupService dataBackupService) {
        this.dataBackupService = dataBackupService;
    }

    @PostMapping
    public CommonResult<String> backup(
            @RequestParam(required = false) String backupName) {
        return dataBackupService.backup(backupName);
    }

    @PostMapping("/toPath")
    public CommonResult<String> backupToPath(@RequestParam String backupPath) {
        return dataBackupService.backupToPath(backupPath);
    }

    @PostMapping("/restore")
    public CommonResult<Void> restore(@RequestParam String backupFile) {
        return dataBackupService.restore(backupFile);
    }

    @GetMapping("/list")
    public CommonResult<List<DataBackupService.BackupInfo>> listBackups() {
        return dataBackupService.listBackups();
    }

    @GetMapping("/count")
    public CommonResult<Integer> getBackupCount() {
        return dataBackupService.getBackupCount();
    }

    @DeleteMapping("/{fileName}")
    public CommonResult<Void> deleteBackup(@PathVariable String fileName) {
        return dataBackupService.deleteBackup(fileName);
    }

    @GetMapping("/dir")
    public CommonResult<String> getBackupDir() {
        return dataBackupService.getBackupDir();
    }
}
