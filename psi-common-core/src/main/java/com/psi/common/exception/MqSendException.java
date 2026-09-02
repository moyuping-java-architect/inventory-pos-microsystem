package com.psi.common.exception;

/**
 * MQ消息发送异常
 * 
 * @author PSI
 * @version 1.0.0
 */
public class MqSendException extends BusinessException {

    public MqSendException(String message) {
        super(message);
    }

    public MqSendException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }
}