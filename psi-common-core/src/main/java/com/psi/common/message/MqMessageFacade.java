package com.psi.common.message;

import java.time.Duration;

public interface MqMessageFacade {

    void send(MqCommonMessage<?> message);

    void sendAsync(MqCommonMessage<?> message);

    void sendWithDelay(MqCommonMessage<?> message, Duration delay);

    void sendAsyncWithDelay(MqCommonMessage<?> message, Duration delay);
}