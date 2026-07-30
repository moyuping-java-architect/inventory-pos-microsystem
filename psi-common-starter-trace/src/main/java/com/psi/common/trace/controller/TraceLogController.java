package com.psi.common.trace.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/trace/logs")
public class TraceLogController {

    @Value("${psi.trace.log.dir:./logs}")
    private String logDir;

    @Value("${psi.trace.log.context-lines:10}")
    private int contextLines;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Data
    public static class TraceLogQueryRequest {
        private String traceId;
        private String date;
        private String logType;
        private String serviceName;
        private String logLevel;
        private Integer pageNum;
        private Integer pageSize;
    }

    @Data
    public static class LogEntry {
        private String fileName;
        private String serviceName;
        private int lineNumber;
        private String content;
        private String logLevel;
        private String timestamp;
        private List<String> contextBefore;
        private List<String> contextAfter;
    }

    @Data
    public static class TraceLogQueryResponse {
        private String traceId;
        private String date;
        private List<LogEntry> entries;
        private int totalMatches;
        private int totalFiles;
        private int pageNum;
        private int pageSize;
        private int totalPages;
    }

    @PostMapping("/query")
    public ResponseEntity<TraceLogQueryResponse> queryTraceLogs(@RequestBody TraceLogQueryRequest request) {
        if (request.getTraceId() == null || request.getTraceId().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String traceId = request.getTraceId().trim();
        String dateStr = resolveDate(request.getDate());
        String logType = resolveLogType(request.getLogType());
        String serviceName = request.getServiceName();
        String logLevel = resolveLogLevel(request.getLogLevel());
        
        int pageNum = request.getPageNum() != null ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 20;

        TraceLogQueryResponse response = new TraceLogQueryResponse();
        response.setTraceId(traceId);
        response.setDate(dateStr);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setEntries(new ArrayList<>());

        try {
            Path logPath = Paths.get(logDir);
            if (!Files.exists(logPath) || !Files.isDirectory(logPath)) {
                log.warn("日志目录不存在: {}", logDir);
                return ResponseEntity.ok(response);
            }

            List<Path> logFiles = Files.list(logPath)
                    .filter(path -> Files.isRegularFile(path) && path.toString().toLowerCase().endsWith(".log"))
                    .filter(path -> matchesDate(path, dateStr))
                    .filter(path -> matchesLogType(path, logType))
                    .filter(path -> matchesServiceName(path, serviceName))
                    .sorted()
                    .toList();

            response.setTotalFiles(logFiles.size());

            List<LogEntry> allEntries = new ArrayList<>();
            for (Path file : logFiles) {
                List<LogEntry> entries = searchFile(file, traceId, logLevel);
                allEntries.addAll(entries);
            }

            allEntries.sort(Comparator.comparing(LogEntry::getTimestamp).reversed());
            
            response.setTotalMatches(allEntries.size());
            response.setTotalPages((int) Math.ceil((double) allEntries.size() / pageSize));

            int startIndex = (pageNum - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, allEntries.size());
            if (startIndex < allEntries.size()) {
                response.setEntries(allEntries.subList(startIndex, endIndex));
            }

        } catch (Exception e) {
            log.error("查询日志失败", e);
        }

        return ResponseEntity.ok(response);
    }

    private String resolveLogType(String logType) {
        if (logType == null || logType.trim().isEmpty()) {
            return "all";
        }
        String type = logType.trim().toLowerCase();
        if (type.equals("error") || type.equals("normal") || type.equals("all")) {
            return type;
        }
        return "all";
    }

    private String resolveLogLevel(String logLevel) {
        if (logLevel == null || logLevel.trim().isEmpty()) {
            return "all";
        }
        String level = logLevel.trim().toUpperCase();
        if (level.equals("DEBUG") || level.equals("INFO") || level.equals("WARN") || 
            level.equals("ERROR") || level.equals("ALL")) {
            return level;
        }
        return "all";
    }

    private boolean matchesDate(Path path, String dateStr) {
        String fileName = path.toString().toLowerCase();
        return fileName.contains(dateStr) || fileName.contains(dateStr.substring(2));
    }

    private boolean matchesLogType(Path path, String logType) {
        String fileName = path.toString().toLowerCase();
        return switch (logType) {
            case "error" -> fileName.contains("-error");
            case "normal" -> !fileName.contains("-error");
            default -> true;
        };
    }

    private boolean matchesServiceName(Path path, String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            return true;
        }
        return path.toString().toLowerCase().contains(serviceName.toLowerCase());
    }

    private String resolveDate(String dateInput) {
        if (dateInput == null || dateInput.trim().isEmpty()) {
            return LocalDate.now().format(DATE_FORMAT);
        }

        String input = dateInput.trim().toLowerCase();

        return switch (input) {
            case "today" -> LocalDate.now().format(DATE_FORMAT);
            case "yesterday" -> LocalDate.now().minusDays(1).format(DATE_FORMAT);
            case "tomorrow" -> LocalDate.now().plusDays(1).format(DATE_FORMAT);
            default -> resolveDateInput(input);
        };
    }

    private String resolveDateInput(String input) {
        if (input.matches("^[+-]\\d+d$")) {
            int days = Integer.parseInt(input.substring(0, input.length() - 1));
            return LocalDate.now().plusDays(days).format(DATE_FORMAT);
        }

        if (input.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return input.replace("-", "");
        }

        if (input.matches("^\\d{8}$")) {
            return input;
        }

        return LocalDate.now().format(DATE_FORMAT);
    }

    private List<LogEntry> searchFile(Path filePath, String traceId, String logLevel) {
        List<LogEntry> entries = new ArrayList<>();
        String fileName = filePath.getFileName().toString();
        String serviceName = extractServiceName(filePath);

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            List<String> allLines = reader.lines().collect(Collectors.toList());
            
            for (int i = 0; i < allLines.size(); i++) {
                String line = allLines.get(i);
                if (line.contains("traceId=" + traceId) || line.contains("traceId: " + traceId)) {
                    String level = extractLogLevel(line);
                    if (!logLevel.equals("ALL") && !logLevel.equals(level)) {
                        continue;
                    }
                    
                    LogEntry entry = new LogEntry();
                    entry.setFileName(fileName);
                    entry.setServiceName(serviceName);
                    entry.setLineNumber(i + 1);
                    entry.setContent(line);
                    entry.setLogLevel(level);
                    entry.setTimestamp(extractTimestamp(line));
                    entry.setContextBefore(getContextBefore(allLines, i));
                    entry.setContextAfter(getContextAfter(allLines, i));
                    entries.add(entry);
                }
            }

        } catch (Exception e) {
            log.error("读取日志文件失败: {}", fileName, e);
        }

        return entries;
    }

    private String extractServiceName(Path filePath) {
        Path parentDir = filePath.getParent();
        if (parentDir != null) {
            return parentDir.getFileName().toString();
        }
        return "unknown";
    }

    private String extractLogLevel(String line) {
        Pattern pattern = Pattern.compile("\\[(DEBUG|INFO|WARN|ERROR)\\]|\\-\\s*(DEBUG|INFO|WARN|ERROR)\\s*\\-");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        }
        return "INFO";
    }

    private String extractTimestamp(String line) {
        Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private List<String> getContextBefore(List<String> allLines, int currentIndex) {
        List<String> context = new ArrayList<>();
        int start = Math.max(0, currentIndex - 2);
        for (int i = start; i < currentIndex; i++) {
            context.add(allLines.get(i));
        }
        return context;
    }

    private List<String> getContextAfter(List<String> allLines, int currentIndex) {
        List<String> context = new ArrayList<>();
        int end = Math.min(allLines.size(), currentIndex + 1 + contextLines);
        for (int i = currentIndex + 1; i < end; i++) {
            context.add(allLines.get(i));
        }
        return context;
    }

    @GetMapping("/dates")
    public ResponseEntity<List<String>> getAvailableDates() {
        List<String> dates = new ArrayList<>();

        try {
            Path logPath = Paths.get(logDir);
            if (!Files.exists(logPath) || !Files.isDirectory(logPath)) {
                return ResponseEntity.ok(dates);
            }

            Pattern datePattern = Pattern.compile("\\d{8}");

            Files.list(logPath)
                    .filter(path -> path.toString().toLowerCase().endsWith(".log"))
                    .forEach(path -> {
                        Matcher matcher = datePattern.matcher(path.getFileName().toString());
                        if (matcher.find()) {
                            String date = matcher.group();
                            if (!dates.contains(date)) {
                                dates.add(date);
                            }
                        }
                    });

            dates.sort(Comparator.reverseOrder());

        } catch (Exception e) {
            log.error("获取可用日期失败", e);
        }

        return ResponseEntity.ok(dates);
    }

    @GetMapping("/services")
    public ResponseEntity<List<String>> getAvailableServices() {
        List<String> services = new ArrayList<>();

        try {
            Path logPath = Paths.get(logDir);
            if (!Files.exists(logPath) || !Files.isDirectory(logPath)) {
                return ResponseEntity.ok(services);
            }

            Files.list(logPath)
                    .filter(Files::isDirectory)
                    .forEach(path -> {
                        String serviceName = path.getFileName().toString();
                        if (!serviceName.startsWith(".")) {
                            services.add(serviceName);
                        }
                    });

            services.sort(String::compareToIgnoreCase);

        } catch (Exception e) {
            log.error("获取可用服务失败", e);
        }

        return ResponseEntity.ok(services);
    }

    @GetMapping("/levels")
    public ResponseEntity<List<String>> getAvailableLogLevels() {
        return ResponseEntity.ok(List.of("ALL", "DEBUG", "INFO", "WARN", "ERROR"));
    }
}