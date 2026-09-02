package com.psi.flow.service.impl;

import com.psi.flow.dto.ProcessStartDTO;
import com.psi.flow.service.DocWorkflowService;
import com.psi.flow.service.FlowEngineService;
import com.psi.flow.strategy.DocWorkflowStrategyFactory;
import com.psi.order.dto.CreateDocRequest;
import com.psi.order.dto.DocResponse;
import com.psi.order.service.DocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 单据工作流服务实现
 * 将单据创建、提交审批、工作流启动合并为一个原子操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocWorkflowServiceImpl implements DocWorkflowService {

    private final DocService docService;
    private final FlowEngineService flowEngineService;
    private final DocWorkflowStrategyFactory strategyFactory;

    @Override
    @Transactional
    public DocResponse createAndSubmit(CreateDocRequest request) {
        DocResponse doc = docService.create(request);

        DocResponse submitted = docService.submit(doc.getId());

        String processKey = strategyFactory.getProcessKey(request.getDocType());
        log.info("单据提交审批，匹配工作流: docType={}, processKey={}, docNo={}",
                request.getDocType(), processKey, submitted.getDocNo());

        try {
            ProcessStartDTO startDTO = new ProcessStartDTO();
            startDTO.setProcessKey(processKey);
            startDTO.setBizType(request.getDocType());
            startDTO.setBizId(submitted.getDocNo());
            String docName = request.getDocName() != null ? request.getDocName() : submitted.getDocName();
            startDTO.setTitle(docName != null ? docName : request.getDocType() + " - " + submitted.getDocNo());
            startDTO.setStartUserId(request.getCreatorId());
            startDTO.setStartUserName(request.getCreatorName());

            Map<String, Object> variables = new HashMap<>();
            if (submitted.getTotalAmount() != null) {
                variables.put("amount", submitted.getTotalAmount().doubleValue());
            }
            variables.put("docType", request.getDocType());
            variables.put("docNo", submitted.getDocNo());
            startDTO.setVariables(variables);

            flowEngineService.startProcess(startDTO);
            log.info("工作流已启动: processKey={}, bizId={}, docNo={}",
                    processKey, submitted.getDocNo(), submitted.getDocNo());
        } catch (Exception e) {
            log.warn("工作流启动失败（可能未配置流程定义）: processKey={}, docNo={}, error={}",
                    processKey, submitted.getDocNo(), e.getMessage());
        }

        return submitted;
    }
}