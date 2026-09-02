package com.psi.common.feign;

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
 * 进销存微服务通过此客户端从中间同步微服务拉取上行同步数据（POS上传的数据）
 */
@FeignClient(name = "psi-sync", url = "${psi.sync.base-url:http://localhost:8090}")
public interface SyncUpFeignClient {

    /**
     * 拉取待处理的上行数据（增量拉取）
     *
     * @param lastTime 上次拉取时间
     * @return 待处理数据列表
     */
    @GetMapping("/psi/sync/up/pull")
    CommonResult<List<Map<String, Object>>> pullUpSync(@RequestParam(value = "lastTime", required = false) String lastTime);

    /**
     * 批量确认上行数据处理完成
     *
     * @param batchUuids 批次UUID列表
     * @return 确认结果
     */
    @PostMapping("/psi/sync/up/batchConfirm")
    CommonResult<Integer> batchConfirmUpSync(@RequestBody List<String> batchUuids);
}