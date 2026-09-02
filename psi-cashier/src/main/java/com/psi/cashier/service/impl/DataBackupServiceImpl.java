package com.psi.cashier.service.impl;

import com.psi.cashier.config.SecureDataSourceConfig;
import com.psi.cashier.service.DataBackupService;
import com.psi.common.result.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class DataBackupServiceImpl implements DataBackupService {

    private final SecureDataSourceConfig secureDataSourceConfig;

    @Value("${psi.backup.dir:backup}")
    private String backupDir;

    @Value("${psi.backup.max-count:30}")
    private int maxBackupCount;

    public DataBackupServiceImpl(SecureDataSourceConfig secureDataSourceConfig) {
        this.secureDataSourceConfig = secureDataSourceConfig;
    }

    @Override
    public CommonResult<String> backup(String backupName) {
        try {
            String dbPath = secureDataSourceConfig.resolveDbPath();
            File dbFile = new File(dbPath);

            if (!dbFile.exists()) {
                return CommonResult.fail("数据库文件不存在");
            }

            String dir = ensureBackupDir();

            String fileName;
            if (backupName != null && !backupName.isEmpty()) {
                fileName = backupName + ".db";
            } else {
                String dateStr = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                fileName = "psi_backup_" + dateStr + ".db";
            }

            File backupFile = new File(dir, fileName);

            copyFile(dbFile, backupFile);

            log.info("数据备份成功: {}", backupFile.getAbsolutePath());

            cleanOldBackups();

            return CommonResult.success(backupFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("数据备份失败", e);
            return CommonResult.fail("备份失败: " + e.getMessage());
        }
    }

    @Override
    public CommonResult<String> backupToPath(String backupPath) {
        try {
            String dbPath = secureDataSourceConfig.resolveDbPath();
            File dbFile = new File(dbPath);

            if (!dbFile.exists()) {
                return CommonResult.fail("数据库文件不存在");
            }

            File targetFile = new File(backupPath);
            File parentDir = targetFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            copyFile(dbFile, targetFile);

            log.info("数据备份成功到指定路径: {}", backupPath);

            return CommonResult.success(targetFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("数据备份失败", e);
            return CommonResult.fail("备份失败: " + e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> restore(String backupFile) {
        try {
            File backup = new File(backupFile);
            if (!backup.exists()) {
                return CommonResult.fail("备份文件不存在: " + backupFile);
            }

            String dbPath = secureDataSourceConfig.resolveDbPath();
            File dbFile = new File(dbPath);

            File backupDirFile = new File(ensureBackupDir());
            String dateStr = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File preRestoreBackup = new File(backupDirFile, "pre_restore_" + dateStr + ".db");
            if (dbFile.exists()) {
                copyFile(dbFile, preRestoreBackup);
                log.info("恢复前自动备份当前数据库: {}", preRestoreBackup.getAbsolutePath());
            }

            copyFile(backup, dbFile);

            log.info("数据恢复成功: {} -> {}", backupFile, dbPath);

            return CommonResult.success();
        } catch (Exception e) {
            log.error("数据恢复失败", e);
            return CommonResult.fail("恢复失败: " + e.getMessage());
        }
    }

    @Override
    public CommonResult<List<BackupInfo>> listBackups() {
        try {
            String dir = ensureBackupDir();
            File backupDirFile = new File(dir);

            File[] files = backupDirFile.listFiles((d, name) ->
                    name.endsWith(".db") && name.startsWith("psi_backup_"));

            List<BackupInfo> list = new ArrayList<>();
            if (files != null) {
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                for (File file : files) {
                    BackupInfo info = new BackupInfo();
                    info.setFileName(file.getName());
                    info.setFullPath(file.getAbsolutePath());
                    info.setSize(file.length());
                    info.setCreateTime(file.lastModified());
                    list.add(info);
                }
            }

            return CommonResult.success(list);
        } catch (Exception e) {
            log.error("获取备份列表失败", e);
            return CommonResult.fail("获取备份列表失败: " + e.getMessage());
        }
    }

    @Override
    public CommonResult<Integer> getBackupCount() {
        try {
            String dir = ensureBackupDir();
            File backupDirFile = new File(dir);

            File[] files = backupDirFile.listFiles((d, name) ->
                    name.endsWith(".db") && name.startsWith("psi_backup_"));

            return CommonResult.success(files != null ? files.length : 0);
        } catch (Exception e) {
            return CommonResult.success(0);
        }
    }

    @Override
    public CommonResult<Void> deleteBackup(String fileName) {
        try {
            String dir = ensureBackupDir();
            File file = new File(dir, fileName);

            if (!file.exists()) {
                Files.delete(file.toPath());
                log.info("删除备份文件: {}", fileName);
            }

            return CommonResult.success();
        } catch (Exception e) {
            log.error("删除备份失败", e);
            return CommonResult.fail("删除失败: " + e.getMessage());
        }
    }

    @Override
    public CommonResult<String> getBackupDir() {
        return CommonResult.success(ensureBackupDir());
    }

    private String ensureBackupDir() {
        String dir;
        if (backupDir.startsWith("C:") || backupDir.startsWith("/")) {
            dir = backupDir;
        } else {
            String programData = System.getenv("ProgramData");
            if (programData == null) {
                programData = "C:\\ProgramData";
            }
            dir = programData + "\\PSI\\backup";
        }

        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        return dir;
    }

    @SuppressWarnings("resource")
    private void copyFile(File source, File target) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel targetChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            targetChannel = new FileOutputStream(target).getChannel();
            targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            if (sourceChannel != null) {
                try { sourceChannel.close(); } catch (IOException ignored) {}
            }
            if (targetChannel != null) {
                try { targetChannel.close(); } catch (IOException ignored) {}
            }
        }
    }

    private void cleanOldBackups() {
        try {
            String dir = ensureBackupDir();
            File backupDirFile = new File(dir);

            File[] files = backupDirFile.listFiles((d, name) ->
                    name.endsWith(".db") && name.startsWith("psi_backup_"));

            if (files != null && files.length > maxBackupCount) {
                Arrays.sort(files, Comparator.comparingLong(File::lastModified));
                int toDelete = files.length - maxBackupCount;
                for (int i = 0; i < toDelete; i++) {
                    Files.delete(files[i].toPath());
                    log.info("清理旧备份: {}", files[i].getName());
                }
            }
        } catch (Exception e) {
            log.warn("清理旧备份失败: {}", e.getMessage());
        }
    }
}
