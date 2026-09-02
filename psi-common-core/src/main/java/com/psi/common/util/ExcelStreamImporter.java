package com.psi.common.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Excel 大数据分批流式导入工具
 * 采用 SAX 流式解析，边读边入库，防止 OOM
 * 适合百万级数据导入场景，适配非洲弱网环境
 * 
 * 核心特性：
 * 1. SAX 流式解析，内存占用恒定（约 100MB 以内）
 * 2. 可配置批处理大小，边读边入库
 * 3. 支持错误跳过和日志记录
 * 4. 支持表头映射和数据转换
 * 5. 支持进度回调
 * 
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
public class ExcelStreamImporter {

    /**
     * 导入配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportConfig {
        /**
         * 批处理大小（默认 1000）
         */
        @Builder.Default
        private int batchSize = 1000;

        /**
         * 是否跳过首行（表头）
         */
        @Builder.Default
        private boolean skipHeader = true;

        /**
         * 最大行数限制（0 表示不限制）
         */
        @Builder.Default
        private long maxRows = 0;

        /**
         * 错误容忍数（超过此数停止导入）
         */
        @Builder.Default
        private int maxErrors = 100;

        /**
         * 是否启用异步写入
         */
        @Builder.Default
        private boolean asyncWrite = false;

        /**
         * 表头映射（列名 -> 字段名）
         */
        private Map<String, String> headerMapping;

        /**
         * 数据转换函数
         */
        private Function<Map<String, String>, Object> dataConverter;
    }

    /**
     * 导入结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportResult {
        /**
         * 总读取行数
         */
        private long totalRowsRead;

        /**
         * 成功导入行数
         */
        private long successRows;

        /**
         * 失败行数
         */
        private long failedRows;

        /**
         * 错误详情列表
         */
        private List<ErrorInfo> errors;

        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 耗时（毫秒）
         */
        private long duration;
    }

    /**
     * 错误信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorInfo {
        /**
         * 行号
         */
        private int rowNum;

        /**
         * 原始数据
         */
        private Map<String, String> rowData;

        /**
         * 错误原因
         */
        private String errorMessage;
    }

    /**
     * 进度回调接口
     */
    public interface ProgressCallback {
        /**
         * 进度更新
         * 
         * @param current 当前行数
         * @param total 总行数（预估）
         * @param status 状态描述
         */
        void onProgress(long current, long total, String status);
    }

    /**
     * 流式导入 Excel（从文件路径）
     * 
     * @param filePath 文件路径
     * @param config 配置
     * @param batchHandler 批次处理器
     * @return 导入结果
     */
    public static ImportResult importFromFile(String filePath, ImportConfig config, 
                                             Consumer<List<Object>> batchHandler) {
        return importFromFile(filePath, config, batchHandler, null);
    }

    /**
     * 流式导入 Excel（从文件路径，带进度回调）
     * 
     * @param filePath 文件路径
     * @param config 配置
     * @param batchHandler 批次处理器
     * @param callback 进度回调
     * @return 导入结果
     */
    public static ImportResult importFromFile(String filePath, ImportConfig config,
                                             Consumer<List<Object>> batchHandler,
                                             ProgressCallback callback) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }

        try (InputStream is = Files.newInputStream(path)) {
            return importFromStream(is, config, batchHandler, callback);
        } catch (IOException e) {
            log.error("读取文件失败: {}", e.getMessage());
            return ImportResult.builder()
                    .success(false)
                    .errors(List.of(ErrorInfo.builder().errorMessage("文件读取失败: " + e.getMessage()).build()))
                    .build();
        }
    }

    /**
     * 流式导入 Excel（从输入流）
     * 
     * @param inputStream 输入流
     * @param config 配置
     * @param batchHandler 批次处理器
     * @return 导入结果
     */
    public static ImportResult importFromStream(InputStream inputStream, ImportConfig config,
                                               Consumer<List<Object>> batchHandler) {
        return importFromStream(inputStream, config, batchHandler, null);
    }

    /**
     * 流式导入 Excel（从输入流，带进度回调）
     * 
     * @param inputStream 输入流
     * @param config 配置
     * @param batchHandler 批次处理器
     * @param callback 进度回调
     * @return 导入结果
     */
    public static ImportResult importFromStream(InputStream inputStream, ImportConfig config,
                                               Consumer<List<Object>> batchHandler,
                                               ProgressCallback callback) {
        long startTime = System.currentTimeMillis();
        AtomicInteger rowCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<ErrorInfo> errors = Collections.synchronizedList(new ArrayList<>());
        List<Object> currentBatch = Collections.synchronizedList(new ArrayList<>());
        
        List<String> headers = Collections.synchronizedList(new ArrayList<>());

        try {
            OPCPackage pkg = OPCPackage.open(inputStream);
            XSSFReader reader = new XSSFReader(pkg);
            ReadOnlySharedStringsTable sst = new ReadOnlySharedStringsTable(pkg);

            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(new DefaultHandler() {
                private String currentCellValue;
                private String currentColumn;
                private Map<String, String> currentRowData = new LinkedHashMap<>();
                private boolean isInRow = false;
                private boolean isInCell = false;
                private int rowNum = 0;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if ("row".equals(qName)) {
                        isInRow = true;
                        rowNum++;
                        currentRowData.clear();
                    } else if ("c".equals(qName)) {
                        isInCell = true;
                        currentColumn = attributes.getValue("r");
                        if (currentColumn != null) {
                            currentColumn = extractColumnName(currentColumn);
                        }
                    } else if ("v".equals(qName)) {
                        currentCellValue = "";
                    }
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    if (isInCell && currentCellValue != null) {
                        currentCellValue += new String(ch, start, length);
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if ("v".equals(qName) && currentColumn != null) {
                        String value = resolveCellValue(currentCellValue, sst);
                        currentRowData.put(currentColumn, value);
                    } else if ("row".equals(qName)) {
                        processRow(rowNum, currentRowData, headers, config, 
                                batchHandler, rowCount, successCount, errorCount, errors, currentBatch, callback);
                        isInRow = false;
                    } else if ("c".equals(qName)) {
                        isInCell = false;
                    }
                }
            });

            // 解析第一个工作表
            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) reader.getSheetsData();
            if (iter.hasNext()) {
                try (InputStream sheetStream = iter.next()) {
                    parser.parse(new InputSource(sheetStream));
                }
            }

            // 处理最后一批数据
            if (!currentBatch.isEmpty()) {
                processBatch(currentBatch, batchHandler);
                successCount.addAndGet(currentBatch.size());
                currentBatch.clear();
            }

            long duration = System.currentTimeMillis() - startTime;
            boolean success = errorCount.get() <= config.getMaxErrors();

            log.info("Excel 导入完成，总行数: {}, 成功: {}, 失败: {}, 耗时: {}ms",
                    rowCount.get(), successCount.get(), errorCount.get(), duration);

            return ImportResult.builder()
                    .totalRowsRead(rowCount.get())
                    .successRows(successCount.get())
                    .failedRows(errorCount.get())
                    .errors(errors)
                    .success(success)
                    .duration(duration)
                    .build();

        } catch (Exception e) {
            log.error("Excel 导入失败: {}", e.getMessage(), e);
            return ImportResult.builder()
                    .totalRowsRead(rowCount.get())
                    .successRows(successCount.get())
                    .failedRows(errorCount.get())
                    .errors(errors)
                    .success(false)
                    .duration(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 处理单行数据
     */
    private static void processRow(int rowNum, Map<String, String> rowData, List<String> headers,
                                   ImportConfig config, Consumer<List<Object>> batchHandler,
                                   AtomicInteger rowCount, AtomicInteger successCount,
                                   AtomicInteger errorCount, List<ErrorInfo> errors,
                                   List<Object> currentBatch, ProgressCallback callback) {
        // 检查行数限制
        if (config.getMaxRows() > 0 && rowCount.get() >= config.getMaxRows()) {
            return;
        }

        // 跳过表头
        if (config.isSkipHeader() && rowNum == 1) {
            headers.addAll(rowData.values());
            return;
        }

        rowCount.incrementAndGet();

        try {
            // 数据转换
            Object entity = null;
            if (config.getDataConverter() != null) {
                Map<String, String> mappedData = new LinkedHashMap<>();
                
                if (config.getHeaderMapping() != null && !headers.isEmpty()) {
                    // 使用表头映射
                    int colIndex = 0;
                    for (Map.Entry<String, String> entry : rowData.entrySet()) {
                        if (colIndex < headers.size()) {
                            String originalHeader = headers.get(colIndex);
                            String mappedField = config.getHeaderMapping().getOrDefault(originalHeader, originalHeader);
                            mappedData.put(mappedField, entry.getValue());
                        }
                        colIndex++;
                    }
                } else {
                    mappedData.putAll(rowData);
                }
                
                entity = config.getDataConverter().apply(mappedData);
            } else {
                entity = rowData;
            }

            if (entity != null) {
                currentBatch.add(entity);

                // 达到批次大小，执行批量入库
                if (currentBatch.size() >= config.getBatchSize()) {
                    processBatch(currentBatch, batchHandler);
                    successCount.addAndGet(currentBatch.size());
                    currentBatch.clear();

                    // 进度回调
                    if (callback != null) {
                        callback.onProgress(rowCount.get(), config.getMaxRows() > 0 ? config.getMaxRows() : rowCount.get() * 2,
                                "处理中...");
                    }
                }
            }

        } catch (Exception e) {
            errorCount.incrementAndGet();
            errors.add(ErrorInfo.builder()
                    .rowNum(rowNum)
                    .rowData(rowData)
                    .errorMessage(e.getMessage())
                    .build());

            log.warn("第 {} 行处理失败: {}", rowNum, e.getMessage());

            // 超过错误容忍数，停止处理
            if (errorCount.get() > config.getMaxErrors()) {
                log.error("错误数超过阈值 {}，停止导入", config.getMaxErrors());
                throw new RuntimeException("错误数超过阈值");
            }
        }
    }

    /**
     * 处理批次数据
     */
    private static void processBatch(List<Object> batch, Consumer<List<Object>> batchHandler) {
        try {
            batchHandler.accept(new ArrayList<>(batch));
            log.debug("批次处理完成，数量: {}", batch.size());
        } catch (Exception e) {
            log.error("批次处理失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 解析单元格值（处理共享字符串）
     */
    private static String resolveCellValue(String value, ReadOnlySharedStringsTable sst) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        try {
            int idx = Integer.parseInt(value);
            return sst.getItemAt(idx).toString();
        } catch (NumberFormatException e) {
            return value;
        }
    }

    /**
     * 提取列名（如 A1 -> A）
     */
    private static String extractColumnName(String cellRef) {
        StringBuilder sb = new StringBuilder();
        for (char c : cellRef.toCharArray()) {
            if (Character.isLetter(c)) {
                sb.append(c);
            } else {
                break;
            }
        }
        return sb.toString();
    }
}