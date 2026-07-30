package com.psi.flow.strategy;

/**
 * 单据工作流策略接口
 * 根据单据类型匹配对应的工作流
 */
public interface DocWorkflowStrategy {

    /**
     * 判断是否支持该单据类型
     */
    boolean supports(String docType);

    /**
     * 获取工作流 processKey
     */
    String getProcessKey();
}