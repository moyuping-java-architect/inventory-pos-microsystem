package com.psi.cashier.service.impl;

import com.psi.cashier.dto.*;
import com.psi.cashier.entity.*;
import com.psi.cashier.service.*;
import com.psi.common.context.VirtualThreadContextWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Socket;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 小票打印服务实现类
 * 提供小票打印相关功能
 * 支持两种打印机连接方式：
 * 1. 网络打印机：通过 Socket 发送 ESC/POS 指令
 * 2. USB直连打印机：通过 javax.print 调用系统打印机
 * 优先使用DTO数据直接打印，避免重复查询数据库
 *
 * @author PSI
 * @version 1.0.0
 */
@Slf4j
@Service
public class ReceiptPrintServiceImpl implements ReceiptPrintService {

    private final OperatorService operatorService;
    private final SysConfigService sysConfigService;

    @Value("${psi.cashier.printer.type:network}")
    private String printerType;

    @Value("${psi.cashier.printer.ip:127.0.0.1}")
    private String printerIp;

    @Value("${psi.cashier.printer.port:9100}")
    private int printerPort;

    @Value("${psi.cashier.printer.name:}")
    private String printerName;

    @Value("${psi.cashier.printer.enable:true}")
    private boolean printerEnable;

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.00");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReceiptPrintServiceImpl(OperatorService operatorService,
                                   SysConfigService sysConfigService) {
        this.operatorService = operatorService;
        this.sysConfigService = sysConfigService;
    }

    @Override
    public void print(String orderNo) {
        throw new UnsupportedOperationException("已优化为直接使用DTO打印，请调用 print(CashierMainSaveDTO, String) 方法");
    }

    @Override
    public void print(CashierMainSaveDTO dto, String orderNo) {
        log.info("开始打印小票（直接使用DTO数据），订单号：{}", orderNo);
        try {
            byte[] content = buildReceiptContentFromDTO(dto, orderNo);
            printToPrinter(content);
            log.info("小票打印完成（直接使用DTO数据），订单号：{}", orderNo);
        } catch (Exception e) {
            log.error("小票打印失败（直接使用DTO数据），订单号：{}", orderNo, e);
            throw new RuntimeException("打印失败：" + e.getMessage(), e);
        }
    }

    @Override
    public void printAsync(CashierMainSaveDTO dto, String orderNo) {
        log.info("触发异步打印小票，订单号：{}", orderNo);
        VirtualThreadContextWrapper.executeAsync(() -> {
            try {
                print(dto, orderNo);
            } catch (Exception e) {
                log.error("异步打印小票失败，订单号：{}", orderNo, e);
            }
        });
    }

    @Override
    public boolean isPrinterReady() {
        if (!printerEnable) {
            log.info("打印机功能未启用");
            return false;
        }

        if (isUsbMode()) {
            return findUsbPrinter() != null;
        }

        try (Socket socket = new Socket(printerIp, printerPort)) {
            return socket.isConnected();
        } catch (IOException e) {
            log.warn("打印机连接失败，IP:{} 端口:{}，错误：{}", printerIp, printerPort, e.getMessage());
            return false;
        }
    }

    @Override
    public void printRefund(String refundNo) {
        throw new UnsupportedOperationException("已优化为直接使用DTO打印，请调用 printRefund(RefundMainSaveDTO, String) 方法");
    }

    @Override
    public void printRefund(RefundMainSaveDTO dto, String refundNo) {
        log.info("开始打印退款小票（直接使用DTO数据），退款单号：{}", refundNo);
        try {
            byte[] content = buildRefundReceiptContentFromDTO(dto, refundNo);
            printToPrinter(content);
            log.info("退款小票打印完成（直接使用DTO数据），退款单号：{}", refundNo);
        } catch (Exception e) {
            log.error("退款小票打印失败（直接使用DTO数据），退款单号：{}", refundNo, e);
            throw new RuntimeException("打印失败：" + e.getMessage(), e);
        }
    }

    private boolean isUsbMode() {
        return "usb".equalsIgnoreCase(printerType);
    }

    private byte[] buildReceiptContentFromDTO(CashierMainSaveDTO dto, String orderNo) {
        if (isUsbMode()) {
            return buildUsbReceiptText(dto, orderNo).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        return buildNetworkReceiptEscPos(dto, orderNo);
    }

    private byte[] buildRefundReceiptContentFromDTO(RefundMainSaveDTO dto, String refundNo) {
        if (isUsbMode()) {
            return buildUsbRefundText(dto, refundNo).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        return buildNetworkRefundEscPos(dto, refundNo);
    }

    /**
     * USB模式：构建纯文本小票内容
     * 去除ESC/POS控制字符，使用简单文本排版
     */
    private String buildUsbReceiptText(CashierMainSaveDTO dto, String orderNo) {
        StringBuilder sb = new StringBuilder();

        SysConfigEntity config = sysConfigService.getConfig();
        String shopName = (config != null && config.getShopName() != null) ? config.getShopName() :
                (dto.getStoreName() != null ? dto.getStoreName() : "PSI收银系统");

        sb.append(centerText(shopName)).append("\n");
        sb.append("----------------------------\n");
        sb.append(centerText(getBizTypeName(dto.getBizType()))).append("\n");
        sb.append("订单号：").append(orderNo).append("\n");
        sb.append("时间：").append(LocalDateTime.now().format(DATETIME_FORMATTER)).append("\n");
        sb.append("收银员：").append(dto.getOperatorName()).append("\n");

        if (dto.getMemberCardNo() != null && !dto.getMemberCardNo().isEmpty()) {
            sb.append("会员卡号：").append(dto.getMemberCardNo()).append("\n");
        }

        String currency = dto.getCurrency() != null ? dto.getCurrency() : "ZMW";
        String symbol = getCurrencySymbol(currency);
        sb.append("币种：").append(currency);
        if (!"ZMW".equalsIgnoreCase(currency) && dto.getExchangeRate() != null) {
            sb.append(" 汇率：").append(formatPrice(dto.getExchangeRate()));
        }
        sb.append("\n");

        sb.append("----------------------------\n");
        sb.append(String.format("%-18s %5s %8s\n", "商品", "数量", "金额"));
        sb.append("----------------------------\n");

        for (CashierItemSaveDTO item : dto.getItems()) {
            String productName = item.getGoodsName();
            if (productName.length() > 16) {
                productName = productName.substring(0, 16);
            }
            sb.append(String.format("%-18s %5s %8s\n",
                    productName,
                    formatQuantity(item.getQuantity()),
                    symbol + formatPrice(item.getAmount())));

            if (item.getBarCode() != null && !item.getBarCode().isEmpty()) {
                sb.append("  ").append(item.getBarCode()).append("\n");
            }
            if (item.getBatchNo() != null && !item.getBatchNo().isEmpty()) {
                sb.append("  批次：").append(item.getBatchNo()).append("\n");
            }
        }

        sb.append("----------------------------\n");

        BigDecimal netAmount = dto.getNetAmount();
        BigDecimal taxAmount = dto.getTaxAmount();
        if (netAmount != null && taxAmount != null) {
            BigDecimal totalAmount = netAmount.add(taxAmount);
            sb.append(String.format("%-25s %8s\n", "Subtotal", symbol + formatPrice(netAmount)));
            sb.append(String.format("%-25s %8s\n", "VAT 16%", symbol + formatPrice(taxAmount)));
            sb.append(String.format("%-25s %8s\n", "Total", symbol + formatPrice(totalAmount)));
        } else {
            sb.append(String.format("%-25s %8s\n", "商品金额", symbol + formatPrice(dto.getTotalAmount())));
        }

        BigDecimal discountAmount = dto.getDiscountAmount();
        if (discountAmount == null) {
            BigDecimal baseAmount = netAmount != null ? netAmount.add(taxAmount != null ? taxAmount : BigDecimal.ZERO) : dto.getTotalAmount();
            discountAmount = baseAmount.subtract(dto.getPayAmount());
        }
        if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-25s %8s\n", "优惠金额", "-" + symbol + formatPrice(discountAmount)));
        }

        sb.append(String.format("%-25s %8s\n", "实收金额", symbol + formatPrice(dto.getPayAmount())));

        if (dto.getChangeAmount() != null && dto.getChangeAmount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-25s %8s\n", "找零", symbol + formatPrice(dto.getChangeAmount())));
        }

        sb.append("----------------------------\n");
        sb.append("支付方式：\n");
        for (CashierPaySaveDTO pay : dto.getPays()) {
            String payName = pay.getPayName();
            if (payName == null || payName.isEmpty()) {
                payName = getPayName(pay.getPayId());
            }
            String payExtra = "";
            if (pay.getMobileProvider() != null && !pay.getMobileProvider().isEmpty()) {
                payExtra = " (" + pay.getMobileProvider() + ")";
            }
            sb.append(String.format("  %-15s %8s\n", payName + payExtra, symbol + formatPrice(pay.getPayAmount())));
            if (pay.getMobilePhone() != null && !pay.getMobilePhone().isEmpty()) {
                sb.append(String.format("  %-15s\n", "手机号：" + pay.getMobilePhone()));
            }
        }

        sb.append("----------------------------\n");
        sb.append(centerText("欢迎下次光临!")).append("\n");
        sb.append("\n");
        sb.append("\n");

        return sb.toString();
    }

    /**
     * USB模式：构建纯文本退款小票内容
     */
    private String buildUsbRefundText(RefundMainSaveDTO dto, String refundNo) {
        StringBuilder sb = new StringBuilder();

        SysConfigEntity config = sysConfigService.getConfig();
        String shopName = (config != null && config.getShopName() != null) ? config.getShopName() : "PSI收银系统";

        sb.append(centerText("退货单")).append("\n");
        sb.append(centerText(shopName)).append("\n");
        sb.append("----------------------------\n");
        sb.append("退款单号：").append(refundNo).append("\n");
        sb.append("原订单号：").append(dto.getSourceOrderNo()).append("\n");
        sb.append("时间：").append(LocalDateTime.now().format(DATETIME_FORMATTER)).append("\n");

        if (dto.getOperatorId() != null) {
            OperatorEntity operator = operatorService.getById(dto.getOperatorId());
            if (operator != null) {
                sb.append("操作员：").append(operator.getRealName() != null ? operator.getRealName() : operator.getUsername()).append("\n");
            }
        }

        if (dto.getRemark() != null && !dto.getRemark().isEmpty()) {
            sb.append("原因：").append(dto.getRemark()).append("\n");
        }

        String currency = dto.getCurrency() != null ? dto.getCurrency() : "ZMW";
        String symbol = getCurrencySymbol(currency);
        sb.append("币种：").append(currency);
        if (!"ZMW".equalsIgnoreCase(currency) && dto.getExchangeRate() != null) {
            sb.append(" 汇率：").append(formatPrice(dto.getExchangeRate()));
        }
        sb.append("\n");

        sb.append("----------------------------\n");
        sb.append(String.format("%-18s %5s %8s\n", "商品", "数量", "金额"));
        sb.append("----------------------------\n");

        for (RefundItemSaveDTO item : dto.getItems()) {
            String productName = item.getProductName();
            if (productName.length() > 16) {
                productName = productName.substring(0, 16);
            }
            sb.append(String.format("%-18s %5s %8s\n",
                    productName,
                    "-" + formatQuantity(item.getRefundQuantity()),
                    "-" + symbol + formatPrice(item.getSubtotal())));
            if (item.getBatchNo() != null && !item.getBatchNo().isEmpty()) {
                sb.append("  批次：").append(item.getBatchNo()).append("\n");
            }
        }

        sb.append("----------------------------\n");
        if (dto.getNetRefund() != null && dto.getTaxRefund() != null) {
            sb.append(String.format("%-25s %8s\n", "不含税金额", symbol + formatPrice(dto.getNetRefund())));
            sb.append(String.format("%-25s %8s\n", "VAT 16%", symbol + formatPrice(dto.getTaxRefund())));
            sb.append(String.format("%-25s %8s\n", "退款金额", "-" + symbol + formatPrice(dto.getTotalRefund())));
        } else {
            sb.append(String.format("%-25s %8s\n", "退款金额", "-" + symbol + formatPrice(dto.getTotalRefund())));
        }

        if (dto.getPays() != null && !dto.getPays().isEmpty()) {
            sb.append("----------------------------\n");
            sb.append("退款方式：\n");
            for (RefundPaySaveDTO pay : dto.getPays()) {
                String payName = pay.getPayName();
                if (payName == null || payName.isEmpty()) {
                    payName = getPayName(pay.getPayId());
                }
                sb.append(String.format("  %-15s %8s\n", payName, "-" + symbol + formatPrice(pay.getRefundAmount())));
            }
        }

        sb.append("----------------------------\n");
        sb.append(centerText("感谢您的光临!")).append("\n");
        sb.append("\n");
        sb.append("\n");

        return sb.toString();
    }

    /**
     * 网络模式：构建ESC/POS格式小票内容
     */
    private byte[] buildNetworkReceiptEscPos(CashierMainSaveDTO dto, String orderNo) {
        StringBuilder sb = new StringBuilder();

        SysConfigEntity config = sysConfigService.getConfig();
        String shopName = (config != null && config.getShopName() != null) ? config.getShopName() :
                (dto.getStoreName() != null ? dto.getStoreName() : "PSI收银系统");
        String shopCode = (config != null && config.getShopCode() != null) ? config.getShopCode() : "";

        sb.append(initPrinter());
        sb.append(setAlignCenter());
        sb.append(setFontSizeLarge());
        sb.append(shopName).append("\n");
        sb.append(setFontSizeNormal());

        if (!shopCode.isEmpty()) {
            sb.append("门店编码：").append(shopCode).append("\n");
        }
        if (dto.getPosId() != null) {
            sb.append("收银机：").append(dto.getPosId()).append("\n");
        }
        sb.append(setAlignLeft());
        sb.append("----------------------------\n");

        sb.append(setFontSizeLarge());
        sb.append(getBizTypeName(dto.getBizType())).append("\n");
        sb.append(setFontSizeNormal());

        sb.append("订单号：").append(orderNo).append("\n");
        sb.append("时间：").append(LocalDateTime.now().format(DATETIME_FORMATTER)).append("\n");
        sb.append("收银员：").append(dto.getOperatorName()).append("\n");

        if (dto.getMemberCardNo() != null && !dto.getMemberCardNo().isEmpty()) {
            sb.append("会员卡号：").append(dto.getMemberCardNo()).append("\n");
        }

        String currency = dto.getCurrency() != null ? dto.getCurrency() : "ZMW";
        String symbol = getCurrencySymbol(currency);
        sb.append("币种：").append(currency);
        if (!"ZMW".equalsIgnoreCase(currency) && dto.getExchangeRate() != null) {
            sb.append(" 汇率：").append(formatPrice(dto.getExchangeRate()));
        }
        sb.append("\n");

        sb.append("----------------------------\n");
        sb.append(String.format("%-20s %5s %8s\n", "商品名称", "数量", "金额"));
        sb.append("----------------------------\n");

        for (CashierItemSaveDTO item : dto.getItems()) {
            String productName = item.getGoodsName();
            if (productName.length() > 18) {
                productName = productName.substring(0, 18);
            }
            sb.append(String.format("%-20s %5s %8s\n",
                    productName,
                    formatQuantity(item.getQuantity()),
                    symbol + formatPrice(item.getAmount())));

            if (item.getBarCode() != null && !item.getBarCode().isEmpty()) {
                sb.append("        ").append(item.getBarCode()).append("\n");
            }
            if (item.getBatchNo() != null && !item.getBatchNo().isEmpty()) {
                sb.append("        批次：").append(item.getBatchNo()).append("\n");
            }
        }

        sb.append("----------------------------\n");

        BigDecimal netAmount = dto.getNetAmount();
        BigDecimal taxAmount = dto.getTaxAmount();
        if (netAmount != null && taxAmount != null) {
            BigDecimal totalAmount = netAmount.add(taxAmount);
            sb.append(String.format("%-25s %8s\n", "Subtotal", symbol + formatPrice(netAmount)));
            sb.append(String.format("%-25s %8s\n", "VAT 16%", symbol + formatPrice(taxAmount)));
            sb.append(setFontSizeLarge());
            sb.append(String.format("%-25s %8s\n", "Total", symbol + formatPrice(totalAmount)));
            sb.append(setFontSizeNormal());
        } else {
            sb.append(String.format("%-25s %8s\n", "商品金额", symbol + formatPrice(dto.getTotalAmount())));
        }

        BigDecimal discountAmount = dto.getDiscountAmount();
        if (discountAmount == null) {
            BigDecimal baseAmount = netAmount != null ? netAmount.add(taxAmount != null ? taxAmount : BigDecimal.ZERO) : dto.getTotalAmount();
            discountAmount = baseAmount.subtract(dto.getPayAmount());
        }
        if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-25s %8s\n", "优惠金额", "-" + symbol + formatPrice(discountAmount)));
        }

        sb.append(setFontSizeLarge());
        sb.append(String.format("%-25s %8s\n", "实收金额", symbol + formatPrice(dto.getPayAmount())));
        sb.append(setFontSizeNormal());

        if (dto.getChangeAmount() != null && dto.getChangeAmount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-25s %8s\n", "找零", symbol + formatPrice(dto.getChangeAmount())));
        }

        sb.append("----------------------------\n");
        sb.append("支付方式：\n");
        for (CashierPaySaveDTO pay : dto.getPays()) {
            String payName = pay.getPayName();
            if (payName == null || payName.isEmpty()) {
                payName = getPayName(pay.getPayId());
            }
            String payExtra = "";
            if (pay.getMobileProvider() != null && !pay.getMobileProvider().isEmpty()) {
                payExtra = " (" + pay.getMobileProvider() + ")";
            }
            sb.append(String.format("  %-15s %8s\n", payName + payExtra, symbol + formatPrice(pay.getPayAmount())));
            if (pay.getMobilePhone() != null && !pay.getMobilePhone().isEmpty()) {
                sb.append(String.format("  %-15s\n", "手机号：" + pay.getMobilePhone()));
            }
        }

        sb.append("----------------------------\n");
        sb.append(setAlignCenter());
        sb.append("欢迎下次光临!\n");
        sb.append("\n");
        sb.append("\n");

        sb.append(cutPaper());
        sb.append(endPrinter());

        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 网络模式：构建ESC/POS格式退款小票内容
     */
    private byte[] buildNetworkRefundEscPos(RefundMainSaveDTO dto, String refundNo) {
        StringBuilder sb = new StringBuilder();

        SysConfigEntity config = sysConfigService.getConfig();
        String shopName = (config != null && config.getShopName() != null) ? config.getShopName() : "PSI收银系统";

        sb.append(initPrinter());
        sb.append(setAlignCenter());
        sb.append(setFontSizeLarge());
        sb.append("退货单\n");
        sb.append(setFontSizeNormal());
        sb.append(shopName).append("\n");

        sb.append(setAlignLeft());
        sb.append("----------------------------\n");

        sb.append("退款单号：").append(refundNo).append("\n");
        sb.append("原订单号：").append(dto.getSourceOrderNo()).append("\n");
        sb.append("时间：").append(LocalDateTime.now().format(DATETIME_FORMATTER)).append("\n");

        if (dto.getOperatorId() != null) {
            OperatorEntity operator = operatorService.getById(dto.getOperatorId());
            if (operator != null) {
                sb.append("操作员：").append(operator.getRealName() != null ? operator.getRealName() : operator.getUsername()).append("\n");
            }
        }

        if (dto.getRemark() != null && !dto.getRemark().isEmpty()) {
            sb.append("原因：").append(dto.getRemark()).append("\n");
        }

        String currency = dto.getCurrency() != null ? dto.getCurrency() : "ZMW";
        String symbol = getCurrencySymbol(currency);
        sb.append("币种：").append(currency);
        if (!"ZMW".equalsIgnoreCase(currency) && dto.getExchangeRate() != null) {
            sb.append(" 汇率：").append(formatPrice(dto.getExchangeRate()));
        }
        sb.append("\n");

        sb.append("----------------------------\n");
        sb.append(String.format("%-20s %5s %8s\n", "商品名称", "数量", "金额"));
        sb.append("----------------------------\n");

        for (RefundItemSaveDTO item : dto.getItems()) {
            String productName = item.getProductName();
            if (productName.length() > 18) {
                productName = productName.substring(0, 18);
            }
            sb.append(String.format("%-20s %5s %8s\n",
                    productName,
                    "-" + formatQuantity(item.getRefundQuantity()),
                    "-" + symbol + formatPrice(item.getSubtotal())));
            if (item.getBatchNo() != null && !item.getBatchNo().isEmpty()) {
                sb.append("        批次：").append(item.getBatchNo()).append("\n");
            }
        }

        sb.append("----------------------------\n");
        sb.append(setFontSizeLarge());
        if (dto.getNetRefund() != null && dto.getTaxRefund() != null) {
            sb.append(String.format("%-25s %8s\n", "不含税金额", symbol + formatPrice(dto.getNetRefund())));
            sb.append(String.format("%-25s %8s\n", "VAT 16%", symbol + formatPrice(dto.getTaxRefund())));
            sb.append(String.format("%-25s %8s\n", "退款金额", "-" + symbol + formatPrice(dto.getTotalRefund())));
        } else {
            sb.append(String.format("%-25s %8s\n", "退款金额", "-" + symbol + formatPrice(dto.getTotalRefund())));
        }
        sb.append(setFontSizeNormal());

        if (dto.getPays() != null && !dto.getPays().isEmpty()) {
            sb.append("----------------------------\n");
            sb.append("退款方式：\n");
            for (RefundPaySaveDTO pay : dto.getPays()) {
                String payName = pay.getPayName();
                if (payName == null || payName.isEmpty()) {
                    payName = getPayName(pay.getPayId());
                }
                sb.append(String.format("  %-15s %8s\n", payName, "-" + symbol + formatPrice(pay.getRefundAmount())));
            }
        }

        sb.append("----------------------------\n");
        sb.append(setAlignCenter());
        sb.append("感谢您的光临!\n");
        sb.append("\n");
        sb.append("\n");

        sb.append(cutPaper());
        sb.append(endPrinter());

        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private void printToPrinter(byte[] content) throws Exception {
        if (!printerEnable) {
            log.info("打印机功能未启用，跳过打印");
            log.debug("小票内容：{}", new String(content, java.nio.charset.StandardCharsets.UTF_8));
            return;
        }

        if (isUsbMode()) {
            printToUsbPrinter(content);
        } else {
            printToNetworkPrinter(content);
        }
    }

    private void printToNetworkPrinter(byte[] content) throws IOException {
        try (Socket socket = new Socket(printerIp, printerPort);
             OutputStream outputStream = socket.getOutputStream()) {
            log.info("连接网络打印机成功，IP:{} 端口:{}", printerIp, printerPort);
            outputStream.write(content);
            outputStream.flush();
            log.info("小票数据已发送到网络打印机");
        }
    }

    private void printToUsbPrinter(byte[] content) throws Exception {
        PrintService printService = findUsbPrinter();
        if (printService == null) {
            throw new RuntimeException("未找到USB打印机，请检查打印机是否已连接并安装驱动");
        }

        log.info("使用USB打印机：{}", printService.getName());

        String text = new String(content, java.nio.charset.StandardCharsets.UTF_8);

        // 方式1：尝试直接发送文本到系统打印机
        boolean sentByDoc = sendByDocPrintJob(printService, text);

        if (!sentByDoc) {
            // 方式2：通过 PrinterJob 图形化打印
            sendByPrinterJob(printService, text);
        }

        log.info("小票数据已发送到USB打印机");
    }

    private boolean sendByDocPrintJob(PrintService printService, String text) {
        try {
            DocPrintJob job = printService.createPrintJob();
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            byte[] data = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            Doc doc = new SimpleDoc(data, flavor, null);
            job.print(doc, null);
            log.debug("通过 DocPrintJob 发送打印数据成功");
            return true;
        } catch (Exception e) {
            log.warn("通过 DocPrintJob 发送打印数据失败，将尝试图形化打印：{}", e.getMessage());
            return false;
        }
    }

    private void sendByPrinterJob(PrintService printService, String text) throws PrinterException {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintService(printService);

        PageFormat pageFormat = job.defaultPage();
        Paper paper = new Paper();
        paper.setSize(216, 1440);
        paper.setImageableArea(0, 0, 216, 1440);
        pageFormat.setPaper(paper);
        pageFormat.setOrientation(PageFormat.PORTRAIT);

        job.setPrintable((graphics, format, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(format.getImageableX(), format.getImageableY());
            g2d.setFont(new Font("宋体", Font.PLAIN, 10));
            g2d.setColor(Color.BLACK);

            String[] lines = text.split("\n");
            int y = 20;
            int lineHeight = 15;

            for (String line : lines) {
                g2d.drawString(line, 5, y);
                y += lineHeight;
            }

            return Printable.PAGE_EXISTS;
        }, pageFormat);

        job.print();
    }

    private PrintService findUsbPrinter() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);

        if (services == null || services.length == 0) {
            log.warn("系统未找到任何打印机");
            return null;
        }

        log.debug("系统找到 {} 台打印机", services.length);
        for (PrintService service : services) {
            log.debug("打印机：{}", service.getName());
        }

        if (printerName != null && !printerName.trim().isEmpty()) {
            for (PrintService service : services) {
                if (service.getName().contains(printerName)) {
                    return service;
                }
            }
            log.warn("未找到指定名称的打印机：{}，将使用默认打印机", printerName);
        }

        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
        if (defaultService != null) {
            return defaultService;
        }

        return services[0];
    }

    private String centerText(String text) {
        if (text == null) {
            return "";
        }
        int width = 32;
        if (text.length() >= width) {
            return text;
        }
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }

    private String getBizTypeName(Integer bizType) {
        if (bizType == null) {
            return "零售";
        }
        return switch (bizType) {
            case 20 -> "零售";
            case 21 -> "批发";
            default -> "零售";
        };
    }

    private String getPayName(Integer payId) {
        if (payId == null) {
            return "其他";
        }
        return switch (payId) {
            case 1 -> "现金";
            case 2 -> "微信支付";
            case 3 -> "支付宝";
            case 4 -> "会员卡";
            case 5 -> "Mobile Money";
            default -> "其他";
        };
    }

    private String getCurrencySymbol(String currency) {
        return "USD".equalsIgnoreCase(currency) ? "$" : "ZMW";
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0.00";
        }
        return PRICE_FORMAT.format(price.setScale(2, RoundingMode.HALF_UP));
    }

    private String formatQuantity(BigDecimal quantity) {
        if (quantity == null) {
            return "0";
        }
        if (quantity.scale() <= 0) {
            return quantity.intValue() + "";
        }
        return quantity.toPlainString();
    }

    private String initPrinter() {
        return "\u001B@";
    }

    private String setAlignCenter() {
        return "\u001Ba\u0001";
    }

    private String setAlignLeft() {
        return "\u001Ba\u0000";
    }

    private String setFontSizeLarge() {
        return "\u001B!0";
    }

    private String setFontSizeNormal() {
        return "\u001B!\0";
    }

    private String cutPaper() {
        return "\u001DVA\u0001";
    }

    private String endPrinter() {
        return "\u000C";
    }
}
