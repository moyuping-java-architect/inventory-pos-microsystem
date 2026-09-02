const http = require('http');
const BASE = { host: 'localhost', port: 8088 };

function request(method, path, body, headers = {}) {
  return new Promise((resolve, reject) => {
    const opts = {
      ...BASE,
      path,
      method,
      headers: { ...headers },
    };
    const bodyStr = body ? JSON.stringify(body) : null;
    if (bodyStr) {
      opts.headers['Content-Type'] = 'application/json';
      opts.headers['Content-Length'] = Buffer.byteLength(bodyStr);
    }
    const req = http.request(opts, (res) => {
      let d = '';
      res.setEncoding('utf-8');
      res.on('data', (c) => d += c);
      res.on('end', () => {
        try { resolve({ status: res.statusCode, json: JSON.parse(d), raw: d }); }
        catch { resolve({ status: res.statusCode, text: d }); }
      });
    });
    req.on('error', reject);
    if (bodyStr) req.write(bodyStr);
    req.end();
  });
}

(async () => {
  // 1. 健康检查
  console.log('=== 1. Health Check GET /psi/cashier/health ===');
  const h = await request('GET', '/psi/cashier/health');
  console.log('  HTTP', h.status, 'code:', h.json?.code, 'data:', h.json?.data);

  // 2. 登录
  console.log('\n=== 2. Login POST /psi/cashier/auth/login ===');
  const login = await request('POST', '/psi/cashier/auth/login', {
    username: 'cashier01', password: '123456',
  });
  console.log('  HTTP', login.status, 'code:', login.json?.code, 'success:', login.json?.data?.success);
  const token = login.json?.data?.token;
  const userInfo = login.json?.data?.user;
  if (!token) { console.log('Login FAILED:', JSON.stringify(login.json)); return; }
  console.log('  Token:', token.substring(0, 8) + '...');
  console.log('  User:', userInfo?.username, userInfo?.realName, 'shop:', userInfo?.shopCode, 'tenant:', userInfo?.tenantId);

  // 构建请求头（含中文用户名，使用 raw http 模块以支持非 ASCII）
  const cnName = userInfo?.realName || '收银员01';
  const cnNameBytes = Buffer.from(cnName, 'utf-8');
  let rawCnName = '';
  for (let i = 0; i < cnNameBytes.length; i++) rawCnName += String.fromCharCode(cnNameBytes[i]);

  const authH = {
    'Authorization': token,
    'X-Tenant-Id': String(userInfo?.tenantId || 1),
    'X-Shop-Id': String(userInfo?.shopCode || 'SH001'),
    'X-Update-User-Id': String(userInfo?.id || 1),
    'X-Update-User-Name': rawCnName,
  };

  // 3. Token 验证
  console.log('\n=== 3. Validate Token GET /psi/cashier/auth/validate ===');
  const val = await request('GET', '/psi/cashier/auth/validate', null, authH);
  console.log('  HTTP', val.status, 'code:', val.json?.code, 'user:', val.json?.data?.username);

  // 4. 订单分页查询
  console.log('\n=== 4. Order Page GET /psi/cashier/order/page ===');
  const orders = await request('GET', '/psi/cashier/order/page?pageNum=1&pageSize=10', null, authH);
  console.log('  HTTP', orders.status, 'code:', orders.json?.code,
    'list.length:', (orders.json?.list || []).length, 'total:', orders.json?.total);

  // 5. 挂单分页查询
  console.log('\n=== 5. Pending Page GET /psi/cashier/pending/page ===');
  const pending = await request('GET', '/psi/cashier/pending/page?pageNum=1&pageSize=10', null, authH);
  console.log('  HTTP', pending.status, 'code:', pending.json?.code,
    'list.length:', (pending.json?.list || []).length, 'total:', pending.json?.total);

  // 6. 商品条码查询（使用实际存在的条码）
  console.log('\n=== 6. Product SKU by barcode GET /psi/cashier/product-sku-sale-unit/barcode/6921168500211 ===');
  const sku = await request('GET', '/psi/cashier/product-sku-sale-unit/barcode/6921168500211', null, authH);
  console.log('  HTTP', sku.status, 'code:', sku.json?.code, 'data:', sku.json?.data ? 'found' : 'not found');

  // 7. 商品搜索
  console.log('\n=== 7. Product Search GET /psi/cashier/product-sku-sale-unit/search?goodsName= ===');
  const search = await request('GET', '/psi/cashier/product-sku-sale-unit/search?goodsName=', null, authH);
  console.log('  HTTP', search.status, 'code:', search.json?.code,
    'data.length:', Array.isArray(search.json?.data) ? search.json.data.length : 'N/A');

  // 8. 会员查询
  console.log('\n=== 8. Member Search GET /psi/cashier/member/search?keyword= ===');
  const member = await request('GET', '/psi/cashier/member/search?keyword=', null, authH);
  console.log('  HTTP', member.status, 'code:', member.json?.code,
    'data.length:', Array.isArray(member.json?.data) ? member.json.data.length : 'N/A');

  // 9. 促销活动查询
  console.log('\n=== 9. Promotion Active GET /psi/cashier/promotion/active ===');
  const promo = await request('GET', '/psi/cashier/promotion/active', null, authH);
  console.log('  HTTP', promo.status, 'code:', promo.json?.code,
    'data.length:', Array.isArray(promo.json?.data) ? promo.json.data.length : 'N/A');

  // 10. 汇率查询
  console.log('\n=== 10. Exchange Rate GET /psi/cashier/exchange-rate/current ===');
  const rate = await request('GET', '/psi/cashier/exchange-rate/current', null, authH);
  console.log('  HTTP', rate.status, 'code:', rate.json?.code, 'data:', rate.json?.data ? 'found' : 'not found');

  // 11. 班次分页查询
  console.log('\n=== 11. Shift Page GET /api/shift/page ===');
  const shift = await request('GET', '/api/shift/page?pageNum=1&pageSize=10', null, authH);
  console.log('  HTTP', shift.status, 'code:', shift.json?.code,
    'list.length:', (shift.json?.list || []).length, 'total:', shift.json?.total);

  // 12. 国际化消息
  console.log('\n=== 12. I18n Messages GET /psi/cashier/i18n/messages?lang=zh_CN ===');
  const i18n = await request('GET', '/psi/cashier/i18n/messages?lang=zh_CN', null, authH);
  console.log('  HTTP', i18n.status, 'code:', i18n.json?.code,
    'data keys:', i18n.json?.data ? Object.keys(i18n.json.data).length : 'N/A');

  // 13. 数据同步下载
  console.log('\n=== 13. Download Last Time GET /psi/cashier/download/last-time ===');
  const dlTime = await request('GET', '/psi/cashier/download/last-time', null, authH);
  console.log('  HTTP', dlTime.status, 'code:', dlTime.json?.code, 'data:', dlTime.json?.data);

  // 14. 数据备份列表
  console.log('\n=== 14. Backup List GET /cashier/backup/list ===');
  const backup = await request('GET', '/cashier/backup/list', null, authH);
  console.log('  HTTP', backup.status, 'code:', backup.json?.code,
    'data.length:', Array.isArray(backup.json?.data) ? backup.json.data.length : 'N/A');

  // 15. 退款分页查询
  console.log('\n=== 15. Refund Page GET /psi/cashier/refund/page ===');
  const refund = await request('GET', '/psi/cashier/refund/page?pageNum=1&pageSize=10', null, authH);
  console.log('  HTTP', refund.status, 'code:', refund.json?.code,
    'list.length:', (refund.json?.list || []).length, 'total:', refund.json?.total);

  // 16. 检查未完成班次
  console.log('\n=== 16. Check Unfinished Shift GET /api/shift/check-unfinished/1 ===');
  const unfinished = await request('GET', '/api/shift/check-unfinished/1', null, authH);
  console.log('  HTTP', unfinished.status, 'code:', unfinished.json?.code, 'data:', unfinished.json?.data);

  console.log('\n=== All cashier tests complete ===');
})().catch(e => console.error('UNEXPECTED:', e));
