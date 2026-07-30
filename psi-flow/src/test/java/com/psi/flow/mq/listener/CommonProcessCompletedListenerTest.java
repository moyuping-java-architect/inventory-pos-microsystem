package com.psi.flow.mq.listener;

import com.psi.common.idempotent.MessageIdempotencyService;
import com.psi.common.message.MqCommonMessage;
import com.psi.order.dto.DocResponse;
import com.psi.order.service.DocService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommonProcessCompletedListenerTest {

    @Mock
    private DocService docService;

    @Mock
    private MessageIdempotencyService messageIdempotencyService;

    private CommonProcessCompletedListener listener;

    @BeforeEach
    void setUp() {
        when(messageIdempotencyService.execute(any(), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        listener = new CommonProcessCompletedListener(docService, messageIdempotencyService);
    }

    @Test
    void onProcessCompleted_shouldLogWhenDocFound() {
        DocResponse doc = new DocResponse();
        doc.setDocNo("DOC001");
        doc.setStatus(2);

        when(docService.findByDocNo("DOC001")).thenReturn(doc);

        listener.onProcessCompleted(buildMessage("DOC001"));

        verify(docService).findByDocNo("DOC001");
    }

    @Test
    void onProcessCompleted_shouldIgnoreWhenDocNotFound() {
        when(docService.findByDocNo("DOC001")).thenReturn(null);

        listener.onProcessCompleted(buildMessage("DOC001"));

        verify(docService).findByDocNo("DOC001");
    }

    private MqCommonMessage<Map<String, Object>> buildMessage(String bizId) {
        Map<String, Object> data = new HashMap<>();
        data.put("bizId", bizId);

        MqCommonMessage<Map<String, Object>> message = new MqCommonMessage<>();
        message.setData(data);
        return message;
    }
}