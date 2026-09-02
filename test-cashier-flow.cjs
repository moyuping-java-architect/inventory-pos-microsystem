/**
 * 收银微服务完整业务流程测试脚本
 *
 * 流程：登录 → 开班 → 商品查询 → 促销试算 → 挂单 → 取单 → 创建订单+支付 → 查询订单 →
 *       退款 → 查询退款 → 班次预览 → 结班 → 日结 → 小票补打
 */

const http = require('http');
const BASE = { host: 'localhost', port: 8088 };

// ========== 工具函数 ==========
function request(method, path, body, headers = {}) {
  return new Promise((resolve, reject) => {
    const opts = { ...BASE, path, method, headers: { ...headers } };
    const bodyStr = body ? JSON.stringify(body) : null;
    if (bodyStr) {
      opts.headers['Content-Type'] = 'application/json';
      opts.headers['Content-Length'] = Buffer.byteLength(bodyStr);
    }
    const req = http.request(opts, (res) => {
      let d = '';
      res.setEncoding('utf-8');
      res.on('data', (c) => (d += c));
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, json: JSON.parse(d), raw: d, headers: res.headers });
        } catch {
          resolve({ status: res.statusCode, text: d, headers: res.headers });
        }
      });
    });
    req.on('error', reject);
    if (bodyStr) req.write(bodyStr);
    req.end();
  });
}

// 构造包含中文的请求头值（raw http 模块以字节方式发送）
function rawHeader(str) {
  const bytes = Buffer.from(str || '', 'utf-8');
  let out = '';
  for (let i = 0; i < bytes.length; i++) out += String.fromCharCode(bytes[i]);
  return out;
}

// 断言辅助
let stepNo = 0;
function step(title) {
  stepNo++;
  console.log(`\n┌─ [Step ${stepNo}] ${title}`);
}
function ok(msg) {
  console.log(`└─ ✅ ${msg}`);
}
function fail(msg) {
  console.log(`└─ ❌ ${msg}`);
}
function info(msg) {
  console.log(`   ${msg}`);
}

// ========== 主流程 ==========
(async () => {
  console.log('╔══════════════════════════════════════════════════════════════╗');
  console.log('║        收银微服务完整业务流程测试                              ║');
  console.log('╚══════════════════════════════════════════════════════════════╝');

  // ====== 1. 登录 ======
  step('收银员登录');
  const login = await request('POST', '/psi/cashier/auth/login', {
    username: 'cashier01',
    password: '123456',
  });
  if (login.status !== 200 || !login.json?.data?.token) {
    fail(`登录失败 HTTP=${login.status} resp=${login.raw}`);
    return;
  }
  const token = login.json.data.token;
  const user = login.json.data.user;
  ok(`登录成功 user=${user.username} name=${user.realName} shop=${user.shopCode} tenant=${user.tenantId}`);
  const authH = {
    Authorization: token,
    'X-Tenant-Id': String(user.tenantId || 1),
    'X-Shop-Id': String(user.shopCode || 'SH001'),
    'X-Update-User-Id': String(user.id || 1),
    'X-Update-User-Name': rawHeader(user.realName || '收银员01'),
  };

  // ====== 2. 健康检查 + 日结状态 ======
  step('健康检查 + 日结状态');
  const health = await request('GET', '/psi/cashier/health', null, authH);
  info(`health: HTTP=${health.status} data=${health.json?.data}`);
  const settle = await request('GET', '/psi/cashier/settlement/check', null, authH);
  info(`settlement check: canSell=${settle.json?.data?.canSell} unsettledDate=${settle.json?.data?.unsettledDate}`);
  if (settle.json?.data?.canSell) {
    ok('当前允许销售（无未日结日期）');
  } else {
    fail(`不允许销售，未日结日期：${settle.json?.data?.unsettledDate}`);
  }

  // ====== 3. 检查未结班 ======
  step('检查未结班（开班前校验）');
  const unfinished = await request(
    'GET',
    `/api/shift/check-unfinished/${user.id || 1}`,
    null,
    authH
  );
  info(`有未完成班次：${unfinished.json?.data}`);
  if (unfinished.json?.data) {
    // 已有未结班次，先结班
    info('存在未结班次，先获取并结班...');
    const lastShift = await request('GET', `/api/shift/last/${user.id || 1}`, null, authH);
    if (lastShift.json?.data) {
      const shiftNo = lastShift.json.data.shiftNo;
      const updateBody = { ...lastShift.json.data, cashReality: lastShift.json.data.cashBegin || 0, remark: '自动结班' };
      await request('PUT', '/api/shift/update', updateBody, authH);
      await request('POST', `/api/shift/confirm/${shiftNo}`, null, authH);
      info(`已结班 shiftNo=${shiftNo}`);
    }
  } else {
    ok('无未完成班次，可以开班');
  }

  // ====== 4. 开班 ======
  step('开班（录入初始现金）');
  const cashBegin = 500;
  const createShift = await request(
    'POST',
    '/api/shift/create',
    {
      operatorId: user.id || 1,
      operatorName: user.realName || '收银员01',
      cashBegin: cashBegin,
    },
    authH
  );
  if (createShift.status !== 200 || createShift.json?.code !== 200) {
    fail(`开班失败 HTTP=${createShift.status} resp=${createShift.raw}`);
    return;
  }
  const shiftNo = createShift.json.data?.shiftNo;
  ok(`开班成功 shiftNo=${shiftNo} cashBegin=${cashBegin}`);
  info(`班次详情: ${JSON.stringify(createShift.json.data)}`);

  // ====== 5. 商品扫码查询 ======
  step('商品扫码查询（条码 6921168500211）');
  const barcode = '6921168500211';
  const sku = await request('GET', `/psi/cashier/product-sku-sale-unit/barcode/${barcode}`, null, authH);
  if (sku.status !== 200 || sku.json?.code !== 200 || !sku.json?.data) {
    fail(`商品查询失败 HTTP=${sku.status} resp=${sku.raw}`);
    return;
  }
  // 接口返回数组，取第一个
  const product = Array.isArray(sku.json.data) ? sku.json.data[0] : sku.json.data;
  ok(`商品查询成功: ${product.goodsName} 规格=${product.packageSpec || '-'} 单位=${product.saleUnitName} 单价=${product.salePrice} skuId=${product.skuId} taxRate=${product.taxRate}`);
  const unitPrice = Number(product.salePrice || 3);
  const goodsCode = product.skuNo || 'SKU20260001';
  const skuId = String(product.skuId || '10001');
  const goodsName = product.goodsName || '可口可乐500ml';
  const unit = product.saleUnitName || '瓶';
  const taxRate = Number(product.taxRate || 0);
  const isTaxInclusive = Number(product.isTaxInclusive || 0);

  // ====== 6. 商品搜索 ======
  step('商品名称搜索');
  const search = await request(
    'GET',
    `/psi/cashier/product-sku-sale-unit/search?goodsName=${encodeURIComponent('可口')}`,
    null,
    authH
  );
  info(`搜索结果数量：${Array.isArray(search.json?.data) ? search.json.data.length : 0}`);

  // ====== 7. 促销试算 ======
  step('促销优惠试算');
  const promoReq = {
    posId: 'POS01',
    bizType: 20,
    totalAmount: unitPrice * 2,
    payAmount: unitPrice * 2,
    operatorId: user.id || 1,
    operatorName: user.realName || '收银员01',
    items: [
      {
        goodsCode: goodsCode,
        goodsName: goodsName,
        unit: unit,
        quantity: 2,
        unitPrice: unitPrice,
        amount: unitPrice * 2,
        skuId: skuId,
      },
    ],
    pays: [{ payId: 1, payAmount: unitPrice * 2 }],
  };
  const promoCalc = await request('POST', '/psi/cashier/promotion/calculate', promoReq, authH);
  info(`促销试算结果: HTTP=${promoCalc.status} data=${JSON.stringify(promoCalc.json?.data)}`);

  // ====== 8. 挂单 ======
  step('创建挂单');
  const pendingNo = 'PND' + Date.now();
  const pendingReq = {
    pendingNo: pendingNo,
    tenantId: String(user.tenantId || 1),
    shopCode: user.shopCode || 'SH001',
    posId: 'POS01',
    operatorId: user.id || 1,
    pendingName: '测试挂单',
    totalAmount: unitPrice * 2,
    items: [
      {
        skuId: skuId,
        barCode: barcode,
        productName: goodsName,
        saleUnitName: unit,
        saleQuantity: 2,
        unitPrice: unitPrice,
        subtotal: unitPrice * 2,
      },
    ],
  };
  const createPending = await request('POST', '/psi/cashier/pending', pendingReq, authH);
  if (createPending.status === 200) {
    ok(`挂单成功 pendingNo=${pendingNo}`);
  } else {
    info(`挂单返回 HTTP=${createPending.status} resp=${createPending.raw?.substring(0, 200)}`);
  }

  // ====== 9. 查询挂单列表 + 取单 ======
  step('查询挂单列表 + 取单详情');
  const pendingList = await request('GET', '/psi/cashier/pending/page?pageNum=1&pageSize=10', null, authH);
  info(`挂单列表数量：${pendingList.json?.list?.length || 0} total=${pendingList.json?.total}`);
  if (pendingList.json?.list?.length > 0) {
    const firstPendingNo = pendingList.json.list[0].pendingNo;
    const pendingDetail = await request('GET', `/psi/cashier/pending/${firstPendingNo}`, null, authH);
    info(`取单详情 pendingNo=${firstPendingNo} totalAmount=${pendingDetail.json?.data?.totalAmount}`);
    const pendingItems = await request('GET', `/psi/cashier/pending/${firstPendingNo}/items`, null, authH);
    info(`取单商品明细数量：${pendingItems.json?.data?.length || 0}`);
    ok('挂单查询成功');
  } else {
    info('无挂单数据可查询');
  }

  // ====== 10. 创建订单 + 支付（核心收银接口） ======
  step('创建订单 + 支付（POST /psi/cashier/save）');
  const orderAmount = unitPrice * 2; // 2瓶
  const payAmount = orderAmount;
  const orderReq = {
    cashierNo: 'POS01_XS_TEST_' + Date.now(),
    posId: 'POS01',
    bizType: 20,
    totalAmount: orderAmount,
    payAmount: payAmount,
    operatorId: user.id || 1,
    operatorName: user.realName || '收银员01',
    items: [
      {
        goodsCode: goodsCode,
        barCode: barcode,
        goodsName: goodsName,
        goodsSpec: product.packageSpec || '',
        unit: unit,
        quantity: 2,
        unitPrice: unitPrice,
        amount: orderAmount,
        skuId: skuId,
        taxRate: taxRate,
        isTaxInclusive: isTaxInclusive,
      },
    ],
    pays: [
      {
        payId: 1,
        payName: '现金',
        payAmount: payAmount,
        payChannel: 'CASH',
        currency: 'ZMW',
      },
    ],
  };
  const createOrder = await request('POST', '/psi/cashier/save', orderReq, authH);
  if (createOrder.status !== 200 || createOrder.json?.code !== 200) {
    fail(`创建订单失败 HTTP=${createOrder.status} resp=${createOrder.raw?.substring(0, 500)}`);
    return;
  }
  const order = createOrder.json.data;
  const orderNo = order.orderNo;
  ok(`订单创建成功 orderNo=${orderNo} totalAmount=${order.totalAmount} payStatus=${order.payStatus}`);
  info(`订单详情: shopCode=${order.shopCode} posId=${order.posId} createTime=${order.createTime}`);

  // ====== 11. 查询订单详情 ======
  step('查询订单详情（含明细、支付、可退金额）');
  const orderDetail = await request('GET', `/psi/cashier/order/${orderNo}`, null, authH);
  if (orderDetail.status === 200 && orderDetail.json?.data) {
    const d = orderDetail.json.data;
    info(`订单详情: totalAmount=${d.totalAmount} 已退款=${d.refundedAmount} 可退=${d.refundableAmount}`);
    info(`商品明细数量：${d.items?.length || 0}`);
    info(`支付明细数量：${d.pays?.length || 0}`);
    if (d.items?.length > 0) {
      info(`  明细[0]: ${d.items[0].goodsName} x${d.items[0].saleQuantity} @${d.items[0].unitPrice} = ${d.items[0].amount}`);
    }
    ok('订单详情查询成功');
  } else {
    fail(`订单详情查询失败 HTTP=${orderDetail.status}`);
  }

  // ====== 12. 订单分页查询 ======
  step('订单分页查询');
  const orderPage = await request('GET', '/psi/cashier/order/page?pageNum=1&pageSize=5', null, authH);
  info(`订单分页: list.length=${orderPage.json?.list?.length || 0} total=${orderPage.json?.total}`);
  ok('订单分页查询成功');

  // ====== 13. 退款 ======
  step('创建退款（退1瓶）');
  const refundReq = {
    sourceOrderNo: orderNo,
    refundReason: '测试退款',
    items: [{ index: 0, quantity: 1 }],
    payDetails: [{ payType: 1, refundAmount: unitPrice }],
  };
  const createRefund = await request('POST', '/psi/cashier/refund', refundReq, authH);
  if (createRefund.status !== 200 || createRefund.json?.code !== 200) {
    fail(`退款失败 HTTP=${createRefund.status} resp=${createRefund.raw?.substring(0, 500)}`);
  } else {
    const refund = createRefund.json.data;
    ok(`退款成功 refundNo=${refund.refundNo} totalRefund=${refund.totalRefund}`);
    info(`退款详情: bizType=${refund.bizType} sourceOrderNo=${refund.sourceOrderNo} refundTime=${refund.refundTime}`);

    // ====== 14. 查询退款详情 ======
    step('查询退款详情（含明细和支付）');
    const refundDetail = await request('GET', `/psi/cashier/refund/${refund.refundNo}`, null, authH);
    info(`退款主表: HTTP=${refundDetail.status} totalRefund=${refundDetail.json?.data?.totalRefund}`);
    const refundItems = await request('GET', `/psi/cashier/refund/${refund.refundNo}/items`, null, authH);
    info(`退货明细数量: ${refundItems.json?.data?.length || 0}`);
    if (refundItems.json?.data?.length > 0) {
      info(`  明细[0]: ${refundItems.json.data[0].productName} x${refundItems.json.data[0].refundQuantity} @${refundItems.json.data[0].refundPrice}`);
    }
    const refundPays = await request('GET', `/psi/cashier/refund/${refund.refundNo}/pays`, null, authH);
    info(`退款支付明细数量: ${refundPays.json?.data?.length || 0}`);
    ok('退款详情查询成功');

    // ====== 15. 退款分页查询 ======
    step('退款分页查询');
    const refundPage = await request('GET', '/psi/cashier/refund/page?pageNum=1&pageSize=5', null, authH);
    info(`退款分页: list.length=${refundPage.json?.list?.length || 0} total=${refundPage.json?.total}`);
    ok('退款分页查询成功');
  }

  // ====== 16. 班次预览 ======
  step('班次统计预览（结班前）');
  const shiftPreview = await request('GET', `/api/shift/preview/${user.id || 1}`, null, authH);
  if (shiftPreview.status === 200 && shiftPreview.json?.data) {
    const p = shiftPreview.json.data;
    info(`预览: totalOrder=${p.totalOrder} totalAmount=${p.totalAmount} cashAmount=${p.cashAmount}`);
    ok('班次预览成功');
  } else {
    info(`班次预览返回: HTTP=${shiftPreview.status} ${shiftPreview.raw?.substring(0, 200)}`);
  }

  // ====== 17. 查询班次内支付明细 ======
  step('查询班次内支付明细');
  const shiftPays = await request('GET', `/api/shift/${shiftNo}/pay`, null, authH);
  info(`班次支付明细数量: ${shiftPays.json?.data?.length || 0}`);
  ok('班次支付明细查询成功');

  // ====== 18. 结班 ======
  step('结班（录入实际现金）');
  const shiftInfo = await request('GET', `/api/shift/${shiftNo}`, null, authH);
  const shiftEntity = shiftInfo.json?.data || {};
  const updateShift = await request(
    'PUT',
    '/api/shift/update',
    {
      ...shiftEntity,
      cashReality: cashBegin + orderAmount - unitPrice, // 现金：初始 + 销售 - 退款
      remark: '测试结班',
    },
    authH
  );
  if (updateShift.status === 200) {
    info(`结班更新结果: HTTP=${updateShift.status} cashDiff=${updateShift.json?.data?.cashDiff}`);
    const confirmShift = await request('POST', `/api/shift/confirm/${shiftNo}`, null, authH);
    if (confirmShift.status === 200) {
      ok(`结班确认成功 shiftNo=${shiftNo} status=${confirmShift.json?.data?.status}`);
    } else {
      fail(`结班确认失败 HTTP=${confirmShift.status}`);
    }
  } else {
    fail(`结班更新失败 HTTP=${updateShift.status} resp=${updateShift.raw?.substring(0, 200)}`);
  }

  // ====== 19. 日结 ======
  step('创建日结单');
  const today = new Date();
  const dateStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
  const createSettle = await request(
    'POST',
    '/psi/cashier/settlement',
    { settleDate: dateStr, operatorId: user.id || 1 },
    authH
  );
  if (createSettle.status === 200 && createSettle.json?.code === 200) {
    const settleNo = createSettle.json.data?.settleNo;
    ok(`日结单创建成功 settleNo=${settleNo}`);
    info(`日结详情: totalOrder=${createSettle.json.data?.totalOrder} totalAmount=${createSettle.json.data?.totalAmount} cashAmount=${createSettle.json.data?.cashAmount}`);
    // 确认日结
    const confirmSettle = await request('PUT', `/psi/cashier/settlement/${settleNo}/confirm`, null, authH);
    if (confirmSettle.status === 200) {
      ok('日结确认成功');
    } else {
      info(`日结确认返回 HTTP=${confirmSettle.status}`);
    }
  } else {
    info(`日结创建返回: HTTP=${createSettle.status} ${createSettle.raw?.substring(0, 300)}`);
    if (createSettle.json?.message?.includes('已日结')) {
      info('今日已日结，跳过');
    }
  }

  // ====== 20. 小票补打 ======
  step('小票补打（异步）');
  const printResp = await request('POST', `/psi/cashier/receipt/print/async/${orderNo}`, orderReq, authH);
  info(`小票补打返回: HTTP=${printResp.status} ${printResp.raw?.substring(0, 200)}`);
  const printerStatus = await request('GET', '/psi/cashier/receipt/printer/status', null, authH);
  info(`打印机状态: HTTP=${printerStatus.status} ${printerStatus.raw?.substring(0, 100)}`);
  ok('小票补打流程完成');

  // ====== 21. 登出 ======
  step('收银员登出');
  const logout = await request('POST', '/psi/cashier/auth/logout', null, authH);
  info(`登出返回: HTTP=${logout.status}`);
  ok('登出完成');

  // ====== 汇总 ======
  console.log('\n╔══════════════════════════════════════════════════════════════╗');
  console.log('║                    全流程测试完成                             ║');
  console.log('╚══════════════════════════════════════════════════════════════╝');
  console.log(`测试订单号: ${orderNo}`);
  console.log(`测试班次号: ${shiftNo}`);
})().catch((e) => console.error('UNEXPECTED ERROR:', e));
