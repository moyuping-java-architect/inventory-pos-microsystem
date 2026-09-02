package com.psi.cashier.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收银保存 DTO
 * 支持多种支付方式组合支付（如：现金+微信+会员卡混合支付）
 * 
 * @author PSI
 * @version 1.0.0
 */
@Data
public class CashierMainSaveDTO {

    /**
     * 收银单号
     */
    private String cashierNo;

    /**
     * 店铺编码
     */
    private String storeCode;

    /**
     * 店铺名称
     */
    private String storeName;

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 收银机编号
     */
    @NotBlank(message = "收银机编号不能为空")
    private String posId;

    /**
     * 业务类型：20-零售 21-批发
     */
    @NotNull(message = "业务类型不能为空")
    @Min(value = 20, message = "业务类型值不正确")
    @Max(value = 21, message = "业务类型值不正确")
    private Integer bizType;

    /**
     * 会员ID（可选）
     */
    private Integer memberId;

    /**
     * 会员卡号（可选）
     */
    private String memberCardNo;

    /**
     * 总金额（商品原价总和）
     */
    @NotNull(message = "总金额不能为空")
    @DecimalMin(value = "0.01", message = "总金额必须大于0")
    @Digits(integer = 12, fraction = 2, message = "总金额格式不正确")
    private BigDecimal totalAmount;

    /**
     * 优惠金额（整单优惠、会员折扣等）
     */
    private BigDecimal discountAmount;

    /**
     * 实收金额（实际需要支付的金额）
     */
    @NotNull(message = "实收金额不能为空")
    @DecimalMin(value = "0", message = "实收金额不能为负数")
    @Digits(integer = 12, fraction = 2, message = "实收金额格式不正确")
    private BigDecimal payAmount;

    /**
     * 找零金额
     */
    private BigDecimal changeAmount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作员ID
     */
    @NotNull(message = "操作员ID不能为空")
    @Min(value = 1, message = "操作员ID必须大于0")
    private Integer operatorId;

    /**
     * 操作员名称
     */
    @NotBlank(message = "操作员名称不能为空")
    private String operatorName;

    /**
     * 商品明细列表
     */
    @NotEmpty(message = "商品明细列表不能为空")
    @Valid
    private List<CashierItemSaveDTO> items;

    /**
     * 不含税商品净额（VAT 价外税）
     */
    private BigDecimal netAmount;

    /**
     * VAT 税额
     */
    private BigDecimal taxAmount;

    /**
     * 支付明细列表（支持多种支付方式组合支付）
     * 例如：现金50元 + 微信支付30元 + 会员卡支付20元
     */
    @NotEmpty(message = "支付明细列表不能为空")
    @Valid
    private List<CashierPaySaveDTO> pays;

    /**
     * 结算币种（ZMW/USD），默认 ZMW
     */
    private String currency;

    /**
     * 汇率（本位币 ZMW 对结算币种）
     */
    private BigDecimal exchangeRate;

    /**
     * 原币种应收金额（切换币种前金额，保留痕迹）
     */
    private BigDecimal originalAmount;
}