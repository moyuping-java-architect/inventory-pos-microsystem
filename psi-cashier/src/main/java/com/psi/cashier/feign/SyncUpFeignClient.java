package com.psi.cashier.feign;

import com.psi.common.dto.sync.SyncBatchDTO;
import com.psi.common.result.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 上行同步 Feign 客户端
 * 收银微服务通过此客户端上传数据到中间同步微服务
 */
@FeignClient(name = "psi-sync", url = "${psi.sync.base-url}")
public interface SyncUpFeignClient {

    /**
     * POS上传批量数据
     *
     * @param batchDTO 批次数据
     * @return 上传结果
     */
    @PostMapping("/psi/sync/up/batchUpload")
    CommonResult<Boolean> batchUpload(@RequestBody SyncBatchDTO batchDTO);

    /**
     * POS上传单据数据
     *
     * @param tenantId  租户ID
     * @param shopCode  商铺编码
     * @param posSn     收银机设备编码
     * @param tableName 单据对应表名
     * @param jsonData  单据集合JSON
     * @return 插入结果
     */
    @PostMapping("/psi/sync/up/upload")
    CommonResult<Map<String, Object>> upload(
            @RequestParam("tenantId") String tenantId,
            @RequestParam(value = "shopCode", required = false) String shopCode,
            @RequestParam("posSn") String posSn,
            @RequestParam("tableName") String tableName,
            @RequestBody String jsonData);
}