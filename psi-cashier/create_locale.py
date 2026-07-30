# -*- coding: utf-8 -*-

content = """window.messages_zh_CN = {
    "common": {
        "title": "PSI收银系统",
        "cashier": "收银员",
        "total": "总计",
        "amount": "金额"
    },
    "button": {
        "upload": "上传数据",
        "download": "下载数据",
        "cashierShift": "收银换班",
        "submit": "提交",
        "cancel": "取消",
        "add": "添加",
        "delete": "删除",
        "clear": "清空",
        "search": "搜索",
        "print": "打印",
        "register": "会员注册",
        "confirm": "确认",
        "close": "关闭",
        "queryTrade": "交易查询",
        "dailySettlement": "日结算",
        "refund": "退款",
        "suspend": "暂停订单",
        "resume": "恢复订单",
        "saveDraft": "保存草稿",
        "pay": "结账"
    },
    "label": {
        "unit": "单位",
        "action": "操作",
        "detail": "详情",
        "reason": "原因",
        "phone": "手机号",
        "name": "姓名",
        "time": "时间"
    },
    "hint": {
        "enterOrderNo": "请输入订单号",
        "emptyData": "无数据",
        "emptyProducts": "无商品",
        "emptyPayRecords": "无支付记录",
        "emptyRefundDetails": "无退款详情",
        "emptyOrders": "无订单",
        "enterPhone": "请输入手机号",
        "enterName": "请输入姓名",
        "enterAmount": "请输入金额",
        "sourceOrderAmount": "原订单金额:",
        "refundedAmount": "已退款金额:",
        "availableRefund": "可退款金额:",
        "refundProducts": "退款商品",
        "purchaseQty": "购买数量",
        "refundedQty": "已退数量",
        "refundQty": "退款数量",
        "enterSourceOrder": "请先输入原订单号",
        "enterRefundReason": "请输入退款原因",
        "refundMethod": "退款方式",
        "totalRefundAmount": "退款总金额:",
        "refundTotal": "退款总计:",
        "orderCount": "订单数量",
        "orderTotal": "订单总额",
        "realAmount": "收到金额",
        "refundAmount": "退款金额",
        "payMethodSummary": "支付方式汇总",
        "todayOrders": "今日订单列表",
        "count": "数量"
    },
    "modal": {
        "title": {
            "tradeQuery": "交易查询",
            "orderDetail": "订单详情",
            "refund": "退款",
            "settlement": "日结算",
            "memberRegister": "会员注册"
        }
    },
    "table": {
        "header": {
            "product": "商品名称",
            "price": "价格",
            "quantity": "数量",
            "amount": "金额",
            "orderNo": "订单号",
            "amountReceived": "收到金额",
            "status": "状态",
            "date": "创建时间",
            "method": "支付方式"
        }
    },
    "message": {
        "paySuccess": "支付成功"
    },
    "status": {
        "paid": "已支付",
        "unpaid": "未支付",
        "refunded": "已退款",
        "partialRefund": "部分退款"
    },
    "pay": {
        "cash": "现金",
        "wechat": "微信",
        "alipay": "支付宝",
        "card": "会员卡"
    },
    "lang": {
        "chinese": "中文",
        "english": "English"
    }
};"""

with open(r"E:\spring boot\psi-parent\psi-cashier\src\main\resources\static\js\locales\zh_CN.js", "w", encoding="utf-8") as f:
    f.write(content)

print("File written successfully")