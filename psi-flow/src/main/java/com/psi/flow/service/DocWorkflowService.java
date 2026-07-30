package com.psi.flow.service;

import com.psi.order.dto.CreateDocRequest;
import com.psi.order.dto.DocResponse;

/**
 * 单据工作流服务
 * 将单据创建/提交与工作流启动合并为一个原子操作
 */
public interface DocWorkflowService {

    /**
     * 创建单据并提交审批，同时启动对应的工作流
     *
     * @param request 单据创建请求
     * @return 单据响应
     */
    DocResponse createAndSubmit(CreateDocRequest request);
}