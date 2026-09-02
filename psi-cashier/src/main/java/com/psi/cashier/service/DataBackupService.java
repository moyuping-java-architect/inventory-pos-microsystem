package com.psi.cashier.service;

import com.psi.common.result.CommonResult;

import java.util.List;

public interface DataBackupService {

    CommonResult<String> backup(String backupName);

    CommonResult<String> backupToPath(String backupPath);

    CommonResult<Void> restore(String backupFile);

    CommonResult<List<BackupInfo>> listBackups();

    CommonResult<Integer> getBackupCount();

    CommonResult<Void> deleteBackup(String fileName);

    CommonResult<String> getBackupDir();

    class BackupInfo {
        private String fileName;
        private String fullPath;
        private Long size;
        private Long createTime;

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getFullPath() { return fullPath; }
        public void setFullPath(String fullPath) { this.fullPath = fullPath; }

        public Long getSize() { return size; }
        public void setSize(Long size) { this.size = size; }

        public Long getCreateTime() { return createTime; }
        public void setCreateTime(Long createTime) { this.createTime = createTime; }
    }
}
