package com.psi.cashier.feign;

import com.psi.common.dto.sync.DownSyncDTO;
import com.psi.common.result.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 下行同步 Feign 客户端
 * 收银微服务通过此客户端从中间同步微服务拉取下行同步数据
 */
@FeignClient(name = "psi-sync", url = "${psi.sync.base-url}")
public interface SyncDownFeignClient {

    /**
     * 拉取待下载数据
     *
     * @param lastTime 上次拉取时间
     * @return 待下载数据列表
     */
    @GetMapping("/psi/sync/down/pull")
    CommonResult<List<DownSyncDTO>> pullDownSync(@RequestParam("lastTime") String lastTime);

    /**
     * 确认下载完成
     *
     * @param batchUuid 批次UUID
     * @return 确认结果
     */
    @PostMapping("/psi/sync/down/confirm")
    CommonResult<Boolean> confirmDownSync(@RequestParam("batchUuid") String batchUuid);

    /**
     * 批量确认下载完成
     *
     * @param batchUuids 批次UUID列表
     * @return 确认结果
     */
    @PostMapping("/psi/sync/down/batchConfirm")
    CommonResult<Integer> batchConfirmDownSync(@RequestBody List<String> batchUuids);
}