package com.psi.flow.strategy;

/**
 * 通用工作流策略（兜底）
 * 当没有匹配的策略时使用
 * 注意：不注册为 Spring Bean，由工厂直接实例化
 */
public class CommonDocWorkflowStrategy implements DocWorkflowStrategy {

    @Override
    public boolean supports(String docType) {
        return true;
    }

    @Override
    public String getProcessKey() {
        return "PURCHASE_APPROVAL";
    }
}