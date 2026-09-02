package com.psi.order.entity;

import com.psi.common.mybatis.entity.BaseEntity;
import com.psi.order.constant.DocTypeConstant.DocStatus;
import com.psi.order.constant.DocTypeConstant.DocType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 通用单据实体类
 * 适用于采购订单、销售订单、报损单、报溢单、盘点单等所有业务单据
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_main_draft")
public class DocEntity extends BaseEntity {

    /**
     * 商铺编码
     */
    private String shopCode;

    /**
     * 商铺名称
     */
    private String shopName;

    /**
     * 单据编号
     */
    private String docNo;

    /**
     * 单据类型
     */
    private String docType;

    /**
     * 单据名称（必填，默认：单据类型+当天日期）
     */
    private String docName;

    /**
     * 单据状态
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private String creatorId;

    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 供应商/客户ID（根据单据类型使用）
     */
    private String partnerId;

    /**
     * 供应商/客户编码
     */
    private String partnerCode;

    /**
     * 供应商/客户名称
     */
    private String partnerName;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 关联订单号（用于入库/出库等关联上游单据）
     */
    private String orderNo;

    /**
     * 销售类型：1-普通销售 2-批发 3-零售
     */
    private Integer saleType;

    /**
     * 付款方式：1-预付定金 2-货到付款 3-月结 4-现金 5-刷卡 6-赊销
     */
    private Integer paymentType;

    /**
     * 货币编码
     */
    private String currencyCode;

    /**
     * 汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 总金额（不含税）
     */
    private BigDecimal totalAmount;

    /**
     * 税额
     */
    private BigDecimal taxAmount;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 实付/实收金额
     */
    private BigDecimal payAmount;

    /**
     * 明细数量
     */
    private Integer itemCount;

    /**
     * 单据日期
     */
    private LocalDateTime docDate;

    /**
     * 交货/预计到货日期
     */
    private LocalDateTime deliveryDate;

    /**
     * 审核状态：0-未审核 1-已审核 2-审核驳回
     */
    private Integer auditStatus;

    /**
     * 审核人ID
     */
    private Long auditBy;

    /**
     * 审批时间
     */
    private LocalDateTime approveTime;

    /**
     * 执行时间
     */
    private LocalDateTime executeTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展字段（JSON格式）
     */
    private String extJson;

    // ========== 业务辅助方法 ==========

    /**
     * 获取单据类型枚举
     */
    public DocType getDocTypeEnum() {
        return DocType.fromCode(docType);
    }

    /**
     * 获取单据状态枚举
     */
    public DocStatus getStatusEnum() {
        return DocStatus.fromValue(status != null ? status : 0);
    }

    /**
     * 判断是否为采购类单据
     */
    public boolean isPurchase() {
        return getDocTypeEnum().isPurchase();
    }

    /**
     * 判断是否为销售类单据
     */
    public boolean isSale() {
        return getDocTypeEnum().isSale();
    }

    /**
     * 判断是否可以提交
     */
    public boolean canSubmit() {
        return getStatusEnum().canSubmit();
    }

    /**
     * 判断是否可以审批
     */
    public boolean canApprove() {
        return getStatusEnum().canApprove();
    }

    /**
     * 判断是否可以执行
     */
    public boolean canExecute() {
        return getStatusEnum().canExecute();
    }

    /**
     * 判断是否可以取消
     */
    public boolean canCancel() {
        return getStatusEnum().canCancel();
    }
}