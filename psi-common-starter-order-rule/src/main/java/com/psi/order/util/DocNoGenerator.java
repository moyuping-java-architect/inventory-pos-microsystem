package com.psi.order.util;

import com.psi.order.constant.DocTypeConstant.DocType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 通用单据编号生成工具类
 * 支持采购订单、销售订单、报损单、报溢单、盘点单等所有单据类型
 */
public class DocNoGenerator {

    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 根据单据类型生成单据编号
     * 格式：单据类型前缀 + 年月日时分秒 + 4位随机数
     *
     * @param docType 单据类型
     * @return 单据编号
     */
    public static String generate(DocType docType) {
        String prefix = docType.getPrefix();
        String timestamp = LocalDateTime.now().format(DATETIME_FORMATTER);
        String random = String.format("%04d", RANDOM.nextInt(10000));
        return String.format("%s%s%s", prefix, timestamp, random);
    }

    /**
     * 根据单据类型编码生成单据编号
     *
     * @param docTypeCode 单据类型编码
     * @return 单据编号
     */
    public static String generate(String docTypeCode) {
        DocType docType = DocType.fromCode(docTypeCode);
        return generate(docType);
    }

    /**
     * 生成带业务标识的单据编号
     * 格式：单据类型前缀 + 业务标识 + 年月日时分秒 + 4位随机数
     *
     * @param docType 单据类型
     * @param bizFlag 业务标识（如供应商编码、客户编码等）
     * @return 单据编号
     */
    public static String generateWithBizFlag(DocType docType, String bizFlag) {
        String prefix = docType.getPrefix();
        String flag = bizFlag != null && !bizFlag.isEmpty() ? bizFlag.substring(0, Math.min(bizFlag.length(), 4)) : "";
        String timestamp = LocalDateTime.now().format(DATETIME_FORMATTER);
        String random = String.format("%04d", RANDOM.nextInt(10000));
        return String.format("%s%s%s%s", prefix, flag, timestamp, random);
    }

    /**
     * 生成带流水号的单据编号
     * 格式：单据类型前缀 + 年月日 + 6位流水号
     *
     * @param docType   单据类型
     * @param serialNo  流水号（需要外部维护）
     * @return 单据编号
     */
    public static String generateWithSerialNo(DocType docType, long serialNo) {
        String prefix = docType.getPrefix();
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        String serial = String.format("%06d", serialNo);
        return String.format("%s%s%s", prefix, date, serial);
    }

    /**
     * 解析单据编号获取单据类型
     *
     * @param docNo 单据编号
     * @return 单据类型
     */
    public static DocType parseDocType(String docNo) {
        if (docNo == null || docNo.isEmpty()) {
            return DocType.PURCHASE_ORDER;
        }
        
        for (DocType type : DocType.values()) {
            if (docNo.startsWith(type.getPrefix())) {
                return type;
            }
        }
        return DocType.PURCHASE_ORDER;
    }

    /**
     * 解析单据编号获取日期
     *
     * @param docNo 单据编号
     * @return 日期时间
     */
    public static LocalDateTime parseDocDate(String docNo) {
        if (docNo == null || docNo.length() < 11) {
            return null;
        }
        
        DocType docType = parseDocType(docNo);
        int prefixLength = docType.getPrefix().length();
        
        if (docNo.length() < prefixLength + 14) {
            return null;
        }
        
        String dateStr = docNo.substring(prefixLength, prefixLength + 14);
        try {
            return LocalDateTime.parse(dateStr, DATETIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}