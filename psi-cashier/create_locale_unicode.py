# -*- coding: utf-8 -*-

# 使用Unicode转义序列来避免编码问题
content = """window.messages_zh_CN = {
    "common": {
        "title": "PSI\\u6536\\u94F6\\u7CFB\\u7EDF",
        "cashier": "\\u6536\\u94F6\\u5458",
        "total": "\\u603B\\u8BA1",
        "amount": "\\u91D1\\u989D"
    },
    "button": {
        "upload": "\\u4E0A\\u4F20\\u6570\\u636E",
        "download": "\\u4E0B\\u8F7D\\u6570\\u636E",
        "cashierShift": "\\u6536\\u94F6\\u8F6C\\u73ED",
        "submit": "\\u63D0\\u4EA4",
        "cancel": "\\u53D6\\u6D88",
        "add": "\\u6DFB\\u52A0",
        "delete": "\\u5220\\u9664",
        "clear": "\\u6E05\\u7A7A",
        "search": "\\u641C\\u7D22",
        "print": "\\u6253\\u5370",
        "register": "\\u6210\\u5458\\u6CE8\\u518C",
        "confirm": "\\u786E\\u8BA4",
        "close": "\\u5173\\u95ED",
        "queryTrade": "\\u4EA4\\u6613\\u67E5\\u8BE2",
        "dailySettlement": "\\u65E5\\u7ED3\\u7B97",
        "refund": "\\u9000\\u6B3E",
        "suspend": "\\u6682\\u505C\\u8BA2\\u5355",
        "resume": "\\u6062\\u590D\\u8BA2\\u5355",
        "saveDraft": "\\u4FDD\\u5B58\\u8349\\u7A3F",
        "pay": "\\u7ED3\\u8D26"
    },
    "label": {
        "unit": "\\u5355\\u4F4D",
        "action": "\\u64CD\\u4F5C",
        "detail": "\\u8BE6\\u60C5",
        "reason": "\\u539F\\u56E0",
        "phone": "\\u624B\\u673A\\u53F7",
        "name": "\\u59D3\\u540D",
        "time": "\\u65F6\\u95F4"
    },
    "hint": {
        "enterOrderNo": "\\u8BF7\\u8F93\\u5165\\u8BA2\\u5355\\u53F7",
        "emptyData": "\\u65E0\\u6570\\u636E",
        "emptyProducts": "\\u65E0\\u5546\\u54C1",
        "emptyPayRecords": "\\u65E0\\u652F\\u4ED8\\u8BB0\\u5F55",
        "emptyRefundDetails": "\\u65E0\\u9000\\u6B3E\\u8BE6\\u60C5",
        "emptyOrders": "\\u65E0\\u8BA2\\u5355",
        "enterPhone": "\\u8BF7\\u8F93\\u5165\\u624B\\u673A\\u53F7",
        "enterName": "\\u8BF7\\u8F93\\u5165\\u59D3\\u540D",
        "enterAmount": "\\u8BF7\\u8F93\\u5165\\u91D1\\u989D",
        "sourceOrderAmount": "\\u539F\\u8BA2\\u5355\\u91D1\\u989D:",
        "refundedAmount": "\\u5DF2\\u9000\\u6B3E\\u91D1\\u989D:",
        "availableRefund": "\\u53EF\\u9000\\u6B3E\\u91D1\\u989D:",
        "refundProducts": "\\u9000\\u6B3E\\u5546\\u54C1",
        "purchaseQty": "\\u8D2D\\u4E70\\u6570\\u91CF",
        "refundedQty": "\\u5DF2\\u9000\\u6570\\u91CF",
        "refundQty": "\\u9000\\u6B3E\\u6570\\u91CF",
        "enterSourceOrder": "\\u8BF7\\u5148\\u8F93\\u5165\\u539F\\u8BA2\\u5355\\u53F7",
        "enterRefundReason": "\\u8BF7\\u8F93\\u5165\\u9000\\u6B3E\\u539F\\u56E0",
        "refundMethod": "\\u9000\\u6B3E\\u65B9\\u5F0F",
        "totalRefundAmount": "\\u9000\\u6B3E\\u603B\\u91D1\\u989D:",
        "refundTotal": "\\u9000\\u6B3E\\u603B\\u8BA1:",
        "orderCount": "\\u8BA2\\u5355\\u6570\\u91CF",
        "orderTotal": "\\u8BA2\\u5355\\u603B\\u989D",
        "realAmount": "\\u6536\\u5230\\u91D1\\u989D",
        "refundAmount": "\\u9000\\u6B3E\\u91D1\\u989D",
        "payMethodSummary": "\\u652F\\u4ED8\\u65B9\\u5F0F\\u6C47\\u603B",
        "todayOrders": "\\u4ECA\\u65E5\\u8BA2\\u5355\\u5217\\u8868",
        "count": "\\u6570\\u91CF"
    },
    "modal": {
        "title": {
            "tradeQuery": "\\u4EA4\\u6613\\u67E5\\u8BE2",
            "orderDetail": "\\u8BA2\\u5355\\u8BE6\\u60C5",
            "refund": "\\u9000\\u6B3E",
            "settlement": "\\u65E5\\u7ED3\\u7B97",
            "memberRegister": "\\u6210\\u5458\\u6CE8\\u518C"
        }
    },
    "table": {
        "header": {
            "product": "\\u5546\\u54C1\\u540D\\u79F0",
            "price": "\\u4EF7\\u683C",
            "quantity": "\\u6570\\u91CF",
            "amount": "\\u91D1\\u989D",
            "orderNo": "\\u8BA2\\u5355\\u53F7",
            "amountReceived": "\\u6536\\u5230\\u91D1\\u989D",
            "status": "\\u72B6\\u6001",
            "date": "\\u521B\\u5EFA\\u65F6\\u95F4",
            "method": "\\u652F\\u4ED8\\u65B9\\u5F0F"
        }
    },
    "message": {
        "paySuccess": "\\u652F\\u4ED8\\u6210\\u529F"
    },
    "status": {
        "paid": "\\u5DF2\\u652F\\u4ED8",
        "unpaid": "\\u672A\\u652F\\u4ED8",
        "refunded": "\\u5DF2\\u9000\\u6B3E",
        "partialRefund": "\\u90E8\\u5206\\u9000\\u6B3E"
    },
    "pay": {
        "cash": "\\u73B0\\u91D1",
        "wechat": "\\u5FAE\\u4FE1",
        "alipay": "\\u652F\\u4ED8\\u5B9D",
        "card": "\\u4F1A\\u5458\\u5361"
    },
    "lang": {
        "chinese": "\\u4E2D\\u6587",
        "english": "English"
    }
};"""

with open(r"E:\spring boot\psi-parent\psi-cashier\src\main\resources\static\js\locales\zh_CN.js", "w", encoding="utf-8") as f:
    f.write(content)

print("File written successfully with Unicode escapes")