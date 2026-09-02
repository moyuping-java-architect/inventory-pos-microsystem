package com.psi.cashier.service;

import com.psi.cashier.entity.MemberEntity;
import com.psi.cashier.entity.MemberLevelEntity;
import com.psi.cashier.entity.MemberPriceEntity;

/**
 * 会员服务接口
 */
public interface MemberService {

    /**
     * 根据手机号查询会员
     */
    MemberEntity getByPhone(String phone);

    /**
     * 根据数据UUID查询会员
     */
    MemberEntity getByDataUuid(String dataUuid);

    /**
     * 注册新会员
     * 自动分配初始等级
     */
    MemberEntity register(String phone, String name);

    /**
     * 获取会员等级信息
     */
    MemberLevelEntity getMemberLevel(Integer levelId);

    /**
     * 获取商品的会员价
     */
    Double getMemberPrice(Integer skuId, Integer memberLevel);

    /**
     * 获取会员等级折扣
     */
    Double getMemberDiscount(Integer memberLevel);

    /**
     * 增加会员积分
     * @param memberId 会员ID
     * @param points 增加的积分数量
     */
    void addPoints(Integer memberId, Integer points);

    /**
     * 会员储值充值
     * @param memberId 会员ID
     * @param amount 充值金额
     * @return 充值后的余额
     */
    Double recharge(Integer memberId, Double amount);

    /**
     * 会员储值扣款（收银支付时用会员卡余额抵扣）
     * @param memberId 会员ID
     * @param amount 扣减金额
     * @param orderNo 关联订单号
     * @return 扣减后的余额，余额不足返回 null
     */
    Double deductBalance(Integer memberId, Double amount, String orderNo);
}