package com.psi.common.message;


import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * 公共MQ消息类
 * 用于在微服务之间传递消息
 * @author PSI
 * @version 1.0.0
 */
@Data
public class MqCommonMessage<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    //==========公共消息头=================
    /**消息唯一ID*/
    private String messageId;
    //租户ID
    private String tenantId;
    //操作人ID
    private String operatorId;
//===========MQ路由原生信息留存=================
    /**生产者所属微服务名称*/
    private String sourceService;
    //交换机名称
    private String exchangeName;
    //路由key名称
    private String routingKey;
//===========业务定义字段=================
    /**消息事件类型*/
    private String messageType;
    //消息生成时间戳
    private Long createTime;
    //自定义扩展附加参数
    private Map<String, String> extParams;
    //业务核心数据
    private T data;

}