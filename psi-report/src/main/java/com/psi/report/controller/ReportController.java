package com.psi.report.controller;

import com.psi.report.dto.ReportRequest;
import com.psi.report.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 报表控制器
 */
@Slf4j
@RestController
@RequestMapping("/psi/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 生成并下载报表
     *
     * @param request  报表请求
     * @param httpReq  HTTP 请求，用于透传租户等头部
     * @param response HTTP 响应
     * @throws IOException IO 异常
     */
    @PostMapping("/generate")
    public void generateReport(@RequestBody ReportRequest request,
                               HttpServletRequest httpReq,
                               HttpServletResponse response) throws IOException {
        log.info("Generate report request: reportType={}, format={}", request.getReportType(), request.getFormat());

        Map<String, String> contextHeaders = extractContextHeaders(httpReq);
        byte[] content = reportService.generateReport(request, contextHeaders);
        String filename = buildFilename(request.getReportType(), request.getFormat());

        response.setContentType(getContentType(request.getFormat()));
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename);
        response.setContentLength(content.length);
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }

    /**
     * 提取需要透传给下游业务服务的请求头
     */
    private Map<String, String> extractContextHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        String[] keys = {
                "X-Tenant-Id",
                "X-Shop-Id",
                "X-Warehouse-Id",
                "X-Update-User-Id",
                "X-Update-User-Name",
                "X-Role-Id",
                "X-Role-Name",
                "X-Permissions",
                "Authorization"
        };
        for (String key : keys) {
            String value = request.getHeader(key);
            if (value != null && !value.isEmpty()) {
                headers.put(key, value);
            }
        }
        return headers;
    }

    private String buildFilename(String reportType, String format) {
        String ext = "xlsx";
        if (format != null && format.toLowerCase().contains("pdf")) {
            ext = "pdf";
        }
        String base = reportType != null ? reportType : "report";
        return URLEncoder.encode(base + "_" + System.currentTimeMillis() + "." + ext, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    private String getContentType(String format) {
        if (format != null && format.toLowerCase().contains("pdf")) {
            return MediaType.APPLICATION_PDF_VALUE;
        }
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }
}
