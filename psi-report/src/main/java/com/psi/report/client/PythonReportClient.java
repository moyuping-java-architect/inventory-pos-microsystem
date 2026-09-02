package com.psi.report.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 调用 Python 报表服务的客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PythonReportClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${psi.report.python.base-url}")
    private String pythonBaseUrl;

    /**
     * 向 Python 报表服务请求生成报表，返回文件字节数组
     *
     * @param reportType 报表类型
     * @param format     输出格式
     * @param params     查询参数
     * @param data       报表数据
     * @return 生成的文件内容
     */
    public byte[] generateReport(String reportType, String format, Map<String, Object> params,
                                 java.util.List<Map<String, Object>> data) {
        String url = pythonBaseUrl + "/reports/generate";

        Map<String, Object> request = Map.of(
                "report_type", reportType,
                "format", format,
                "params", params != null ? params : Map.of(),
                "data", data != null ? data : java.util.List.of()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        log.info("Calling Python report service: url={}, reportType={}, format={}", url, reportType, format);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                byte[].class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            log.info("Python report generated, size={} bytes", response.getBody().length);
            return response.getBody();
        }

        throw new RuntimeException("Failed to generate report from Python service: " + response.getStatusCode());
    }
}
