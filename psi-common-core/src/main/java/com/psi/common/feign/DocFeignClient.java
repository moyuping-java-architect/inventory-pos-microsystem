package com.psi.common.feign;

import com.psi.common.result.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * 单据 Feign 客户端
 * 
 * <p>各业务模块通过此客户端远程调用 psi-flow 服务，
 * 查询审批通过的单据数据并更新单据状态
 */
@FeignClient(name = "psi-flow", url = "${psi.flow.base-url:http://localhost:8089}")
public interface DocFeignClient {

    /**
     * 根据单据编号查询单据
     */
    @GetMapping("/psi/flow/doc/{docNo}")
    CommonResult<DocFeignResponse> findByDocNo(@PathVariable("docNo") String docNo);

    /**
     * 标记单据为已完成
     */
    @PutMapping("/psi/flow/doc/{id}/complete")
    CommonResult<Void> complete(@PathVariable("id") Long id);
}