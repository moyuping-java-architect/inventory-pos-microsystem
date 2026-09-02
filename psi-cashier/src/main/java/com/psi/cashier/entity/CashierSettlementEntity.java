package com.psi.cashier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 收银员日结表实体类
 * 存储收银员当班期间的销售统计和收款汇总信息
 */
@Data
@TableName("cashier_settlement")
public class CashierSettlementEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日结ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 数据UUID（跨服务比对用）
     */
    private String dataUuid;

    /**
     * 日结单号
     */
    private String settleNo;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 门店编码
     */
    private String shopCode;

    /**
     * 收银机编号
     */
    private String posId;

    /**
     * 业务类型：5-日结
     */
    private Integer bizType;

    /**
     * 收银员ID
     */
    private Integer operatorId;

    /**
     * 收银员账号
     */
    private String username;

    /**
     * 收银员姓名
     */
    private String realName;

    /**
     * 当班开始时间
     */
    private String beginTime;

    /**
     * 当班结束时间
     */
    private String endTime;

    /**
     * 销售笔数
     */
    private Integer totalOrder;

    /**
     * 销售总额
     */
    private BigDecimal totalAmount;

    /**
     * 实收总额
     */
    private BigDecimal totalReal;

    /**
     * 优惠总额
     */
    private BigDecimal totalDiscount;

    /**
     * 现金收款
     */
    private BigDecimal cashAmount;

    /**
     * 微信收款
     */
    private BigDecimal wechatAmount;

    /**
     * 支付宝收款
     */
    private BigDecimal alipayAmount;

    /**
     * 会员卡收款
     */
    private BigDecimal memberAmount;

    /**
     * 其他收款
     */
    private BigDecimal otherAmount;

    /**
     * 日结状态：0-未确认 1-已确认
     */
    private Integer status;

    /**
     * 制单人
     */
    private String createBy;

    /**
     * 日结时间
     */
    private String createTime;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 修改时间
     */
    private String updateTime;
}