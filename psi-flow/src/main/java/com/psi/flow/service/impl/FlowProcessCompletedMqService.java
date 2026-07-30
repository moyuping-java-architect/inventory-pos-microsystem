package com.psi.flow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psi.common.async.facade.MqMessageFacade;
import com.psi.common.feign.DocFeignResponse;
import com.psi.common.message.MessageFactory;
import com.psi.common.message.MqCommonMessage;
import com.psi.flow.entity.WfOperationLog;
import com.psi.flow.mapper.WfOperationLogMapper;
import com.psi.flow.strategy.ProcessCompletedMqStrategyFactory;
import com.psi.order.strategy.ProcessCompletedMqStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 流程完成 MQ 异步发送服务
 *
 * <p>把"查询/构建消息 + 发送 MQ"放到异步线程执行，不阻塞审批接口返回</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowProcessCompletedMqService {

    private final MqMessageFacade mqMessageFacade;
    private final ProcessCompletedMqStrategyFactory strategyFactory;
    private final WfOperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    /**
     * 异步发送流程完成 MQ
     *
     * @param docFeignResponse 已查好的单据数据；不为空时会序列化成 JSON 塞进 MQ，消费者可直接使用
     */
    @Async("flowMqExecutor")
    public void sendProcessCompletedMq(String processInstanceId, String title, Integer processStatus,
                                        String bizType, String bizId, DocFeignResponse docFeignResponse) {
        try {
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("bizId", bizId);
            data.put("bizType", bizType);
            data.put("processInstanceId", processInstanceId);
            data.put("title", title);
            data.put("processStatus", processStatus);

            // 把工作流已查好的单据快照塞进 MQ，消费者无需再 Feign 查询
            if (docFeignResponse != null) {
                try {
                    data.put("docData", objectMapper.writeValueAsString(docFeignResponse));
                } catch (Exception e) {
                    log.warn("单据数据序列化失败，MQ将不携带docData: processInstanceId={}, error={}",
                            processInstanceId, e.getMessage());
                }
            }

            ProcessCompletedMqStrategy strategy = strategyFactory.getStrategy(bizType);

            log.info("流程完成MQ(异步): processInstanceId={}, bizType={}, bizId={}, hasDocData={}, strategy={}",
                    processInstanceId, bizType, bizId, docFeignResponse != null,
                    strategy != null ? strategy.getClass().getSimpleName() : "null");

            if (strategy != null && mqMessageFacade != null) {
                MqCommonMessage<Map<String, Object>> message = MessageFactory.create(
                        data,
                        strategy.getExchange(),
                        strategy.getRoutingKey(),
                        "PROCESS_COMPLETED"
                );
                mqMessageFacade.sendAsync(message);
                log.info("流程完成MQ发送成功: processInstanceId={}, exchange={}, routingKey={}",
                        processInstanceId, strategy.getExchange(), strategy.getRoutingKey());
                saveOperationLog(processInstanceId, "流程完成，已发送MQ: " + strategy.getRoutingKey());
            } else {
                saveOperationLog(processInstanceId, "流程完成，MQ策略未配置，跳过");
            }
        } catch (Exception e) {
            log.error("流程完成MQ发送失败: processInstanceId={}, error={}", processInstanceId, e.getMessage(), e);
            saveOperationLog(processInstanceId, "流程完成，MQ发送失败: " + e.getMessage());
        }
    }

    private void saveOperationLog(String processInstanceId, String content) {
        try {
            WfOperationLog operationLog = new WfOperationLog();
            operationLog.setProcessInstanceId(processInstanceId);
            operationLog.setOperateType(5);
            operationLog.setOperatorId("SYSTEM");
            operationLog.setOperatorName("系统");
            operationLog.setOperateContent(content);
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.warn("保存流程操作日志失败: processInstanceId={}, error={}", processInstanceId, e.getMessage());
        }
    }
}
