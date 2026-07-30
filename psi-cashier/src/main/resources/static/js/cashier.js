const CashierModule = (function() {
    const cart = [];
    const payDetails = [];
    const refundPayDetails = [];
    let suspendedOrders = [];
    
    const payMethodNames = {
        cash: '现金',
        wechat: '微信',
        alipay: '支付宝',
        memberCard: '会员卡',
        mobileMoney: 'Mobile Money'
    };
    
    const refundPayMethodNames = {
        '1': '现金',
        '2': '微信',
        '3': '支付宝',
        '4': '会员卡'
    };
    
    // 当前会员信息
    let currentMember = null;

    // 当前币种（本位币 ZMW）
    let currentCurrency = localStorage.getItem('cashier_currency') || 'ZMW';

    // 汇率缓存 { ZMW: 1, USD: 0.037 }
    let exchangeRates = { ZMW: 1 };

    // 网络状态
    let isOnline = navigator.onLine;

    // IndexedDB 数据库名
    const DB_NAME = 'psi_cashier_db';
    const DB_VERSION = 1;
    const STORE_ORDERS = 'offline_orders';

    // 日结状态缓存
    let settlementCache = {
        canSell: null,
        unsettledDate: null,
        lastCheckTime: null
    };

    function formatNumber(num) {
        const parsed = parseFloat(num);
        if (isNaN(parsed)) return '0';
        return parsed.toFixed(4).replace(/\.?0+$/, '');
    }

    function formatMoney(amount) {
        const symbol = currentCurrency === 'USD' ? '$' : 'ZMW';
        const value = (parseFloat(amount) || 0).toFixed(2);
        return symbol + value;
    }

    function getCurrencySymbol() {
        return currentCurrency === 'USD' ? '$' : 'ZMW';
    }

    function updateCurrencySymbols() {
        document.querySelectorAll('.currency-symbol').forEach(el => {
            el.textContent = getCurrencySymbol();
        });
    }

    function calculateVat(netAmount, taxRate) {
        const rate = taxRate != null ? parseFloat(taxRate) : 0.16;
        const net = parseFloat(netAmount) || 0;
        const tax = (net * rate);
        return {
            net: net,
            tax: Math.round(tax * 100) / 100,
            total: Math.round((net + tax) * 100) / 100,
            rate: rate
        };
    }

    function initIndexedDB() {
        return new Promise((resolve, reject) => {
            const request = indexedDB.open(DB_NAME, DB_VERSION);
            request.onerror = () => reject(request.error);
            request.onsuccess = () => resolve(request.result);
            request.onupgradeneeded = (event) => {
                const db = event.target.result;
                if (!db.objectStoreNames.contains(STORE_ORDERS)) {
                    const store = db.createObjectStore(STORE_ORDERS, { keyPath: 'localId', autoIncrement: true });
                    store.createIndex('synced', 'synced', { unique: false });
                    store.createIndex('createTime', 'createTime', { unique: false });
                }
            };
        });
    }

    function saveOfflineOrder(orderData) {
        return initIndexedDB().then(db => {
            return new Promise((resolve, reject) => {
                const tx = db.transaction(STORE_ORDERS, 'readwrite');
                const store = tx.objectStore(STORE_ORDERS);
                const record = {
                    orderData: orderData,
                    synced: 0,
                    retryCount: 0,
                    createTime: Date.now()
                };
                const request = store.add(record);
                request.onsuccess = () => resolve(request.result);
                request.onerror = () => reject(request.error);
            });
        });
    }

    function getPendingOrders() {
        return initIndexedDB().then(db => {
            return new Promise((resolve, reject) => {
                const tx = db.transaction(STORE_ORDERS, 'readonly');
                const store = tx.objectStore(STORE_ORDERS);
                const index = store.index('synced');
                const request = index.getAll(0);
                request.onsuccess = () => resolve(request.result || []);
                request.onerror = () => reject(request.error);
            });
        });
    }

    function markOrderSynced(localId) {
        return initIndexedDB().then(db => {
            return new Promise((resolve, reject) => {
                const tx = db.transaction(STORE_ORDERS, 'readwrite');
                const store = tx.objectStore(STORE_ORDERS);
                const getReq = store.get(localId);
                getReq.onsuccess = () => {
                    const record = getReq.result;
                    if (record) {
                        record.synced = 1;
                        record.syncTime = Date.now();
                        store.put(record);
                    }
                    resolve();
                };
                getReq.onerror = () => reject(getReq.error);
            });
        });
    }

    function deleteSyncedOrders() {
        return initIndexedDB().then(db => {
            return new Promise((resolve, reject) => {
                const tx = db.transaction(STORE_ORDERS, 'readwrite');
                const store = tx.objectStore(STORE_ORDERS);
                const index = store.index('synced');
                const request = index.openCursor(1);
                request.onsuccess = (event) => {
                    const cursor = event.target.result;
                    if (cursor) {
                        cursor.delete();
                        cursor.continue();
                    } else {
                        resolve();
                    }
                };
                request.onerror = () => reject(request.error);
            });
        });
    }

    function updatePendingBadge() {
        getPendingOrders().then(list => {
            const badge = document.getElementById('offlinePendingBadge');
            if (!badge) return;
            badge.textContent = list.length;
            badge.style.display = list.length > 0 ? 'inline-block' : 'none';
        }).catch(err => console.error('更新离线角标失败:', err));
    }

    function loadExchangeRates() {
        fetch('/psi/cashier/exchange-rate/current', { headers: getRequestHeaders() })
            .then(response => response.json())
            .then(data => {
                if (data.code === 200 && data.data) {
                    exchangeRates.ZMW = 1;
                    data.data.forEach(rate => {
                        exchangeRates[rate.fromCurrency] = parseFloat(rate.rate);
                    });
                }
            })
            .catch(error => {
                console.error('加载汇率失败，使用默认汇率:', error);
                exchangeRates.ZMW = 1;
                exchangeRates.USD = 0.037;
            });
    }

    function convertCurrency(amount, fromCurrency, toCurrency) {
        if (fromCurrency === toCurrency) return parseFloat(amount) || 0;
        const fromRate = exchangeRates[fromCurrency] || 1;
        const toRate = exchangeRates[toCurrency] || 1;
        return (parseFloat(amount) || 0) * (toRate / fromRate);
    }

    function switchCurrency(currency) {
        currentCurrency = currency.toUpperCase();
        localStorage.setItem('cashier_currency', currentCurrency);
        updateCurrencySymbols();
        renderCart();
        updatePaySummary();
        updateVatDisplay();
    }

    function checkNetworkStatus() {
        fetch('/psi/cashier/health', { method: 'GET', cache: 'no-store', headers: getRequestHeaders() })
            .then(response => {
                const nowOnline = response.ok;
                if (nowOnline && !isOnline) {
                    isOnline = true;
                    syncPendingOrders();
                }
                isOnline = nowOnline;
                updateNetworkStatusUI();
            })
            .catch(() => {
                isOnline = false;
                updateNetworkStatusUI();
            });
    }

    function updateNetworkStatusUI() {
        const el = document.getElementById('networkStatus');
        if (!el) return;
        if (isOnline) {
            el.classList.remove('offline');
            el.classList.add('online');
            el.textContent = I18n.t('label.online', '在线');
        } else {
            el.classList.remove('online');
            el.classList.add('offline');
            el.textContent = I18n.t('label.offline', '离线');
        }
    }

    function startNetworkMonitoring() {
        window.addEventListener('online', () => checkNetworkStatus());
        window.addEventListener('offline', () => {
            isOnline = false;
            updateNetworkStatusUI();
        });
        setInterval(checkNetworkStatus, 15000);
        checkNetworkStatus();
    }

    function syncPendingOrders() {
        getPendingOrders().then(orders => {
            if (orders.length === 0) return;
            const payload = orders.map(o => o.orderData);
            fetch('/psi/cashier/offline-sync', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...getRequestHeaders()
                },
                body: JSON.stringify(payload)
            })
            .then(response => response.json())
            .then(data => {
                if (data.code === 200) {
                    const result = data.data || {};
                    if (result.failCount === 0) {
                        deleteSyncedOrders().then(() => {
                            updatePendingBadge();
                            alert(I18n.t('message.offlineSyncSuccess', '离线订单同步完成'));
                        });
                    } else {
                        console.warn('部分离线订单同步失败:', result.failedOrderNos);
                        updatePendingBadge();
                        alert(I18n.t('message.offlineSyncPartial', '部分离线订单同步失败，请检查日志'));
                    }
                }
            })
            .catch(error => {
                console.error('离线订单同步请求失败:', error);
            });
        }).catch(error => {
            console.error('读取离线订单失败:', error);
        });
    }

    function getRequestHeaders() {
        const headers = {
            'Authorization': localStorage.getItem('cashier_token') || ''
        };
        const tenantId = localStorage.getItem('cashier_tenant_id');
        if (tenantId) {
            headers['X-Tenant-Id'] = tenantId;
        }
        const shopCode = localStorage.getItem('cashier_shop_code');
        if (shopCode) {
            headers['X-Shop-Id'] = shopCode;
        }
        const userId = localStorage.getItem('cashier_user_id');
        if (userId) {
            headers['X-Update-User-Id'] = userId;
        }
        const userName = localStorage.getItem('cashier_user_name');
        if (userName) {
            headers['X-Update-User-Name'] = userName;
        }
        return headers;
    }

    function init() {
        updateTime();
        setInterval(updateTime, 1000);
        // 初始化国际化
        I18n.init().then(() => {
            bindEvents();
            bindI18nEvents();
            bindCurrencyEvent();
            loadCurrentUser();
            loadSuspendedOrders();
            loadExchangeRates();
            startNetworkMonitoring();
            updatePendingBadge();
            document.getElementById('currencySelect').value = currentCurrency;
            updateCurrencySymbols();
            // 初始化时检查日结状态
            checkSettlementStatus();
        });
    }

    function bindCurrencyEvent() {
        const select = document.getElementById('currencySelect');
        if (select) {
            select.addEventListener('change', function() {
                switchCurrency(this.value);
            });
        }
    }
    
    function bindI18nEvents() {
        const langSelector = document.getElementById('langSelector');
        if (langSelector) {
            langSelector.addEventListener('change', function() {
                const lang = this.value;
                I18n.switchLang(lang).then(() => {
                    // 更新支付方式名称
                    updatePayMethodNames();
                    // 更新页面标题
                    document.querySelector('html').lang = lang === 'zh_CN' ? 'zh-CN' : 'en';
                });
            });
            // 设置当前语言选择
            const currentLang = I18n.getCurrentLang();
            langSelector.value = currentLang;
        }
    }
    
    function updatePayMethodNames() {
        payMethodNames.cash = I18n.t('pay.cash', '现金');
        payMethodNames.wechat = I18n.t('pay.wechat', '微信');
        payMethodNames.alipay = I18n.t('pay.alipay', '支付宝');
        payMethodNames.memberCard = I18n.t('pay.card', '会员卡');
        payMethodNames.mobileMoney = I18n.t('pay.mobileMoney', 'Mobile Money');
        
        refundPayMethodNames['1'] = I18n.t('pay.cash', '现金');
        refundPayMethodNames['2'] = I18n.t('pay.wechat', '微信');
        refundPayMethodNames['3'] = I18n.t('pay.alipay', '支付宝');
        refundPayMethodNames['4'] = I18n.t('pay.card', '会员卡');
        refundPayMethodNames['5'] = I18n.t('pay.mobileMoney', 'Mobile Money');
    }
    
    // 检查日结状态
    function checkSettlementStatus() {
        fetch('/psi/cashier/settlement/check', {
            method: 'GET',
            headers: getRequestHeaders()
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                settlementCache.canSell = data.data.canSell;
                settlementCache.unsettledDate = data.data.unsettledDate;
                settlementCache.lastCheckTime = new Date().getTime();
                
                if (!settlementCache.canSell && settlementCache.unsettledDate) {
                    alert('请先完成 ' + settlementCache.unsettledDate + ' 的日结，然后才能进行销售');
                }
            }
        })
        .catch(error => {
            console.error('检查日结状态失败:', error);
        });
    }
    
    // 更新日结状态缓存（日结完成后调用）
    function updateSettlementCache() {
        settlementCache.canSell = null;
        settlementCache.unsettledDate = null;
        settlementCache.lastCheckTime = null;
        checkSettlementStatus();
    }

    function updateTime() {
        const now = new Date();
        const timeStr = now.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
        document.getElementById('currentTime').textContent = timeStr;
    }

    function bindEvents() {
        console.log('开始绑定事件...');
        
        // 搜索按钮
        const btnSearch = document.getElementById('btnSearch');
        if (btnSearch) btnSearch.addEventListener('click', searchProduct);
        else console.error('未找到 btnSearch 元素');
        
        const searchInput = document.getElementById('searchInput');
        if (searchInput) searchInput.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') searchProduct();
        });
        else console.error('未找到 searchInput 元素');

        // 会员搜索
        const btnSearchMember = document.getElementById('btnSearchMember');
        if (btnSearchMember) btnSearchMember.addEventListener('click', searchMember);
        else console.error('未找到 btnSearchMember 元素');
        
        const memberInput = document.getElementById('memberInput');
        if (memberInput) memberInput.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') searchMember();
        });
        else console.error('未找到 memberInput 元素');
        
        const btnClearMember = document.getElementById('btnClearMember');
        if (btnClearMember) btnClearMember.addEventListener('click', clearMember);
        else console.error('未找到 btnClearMember 元素');
        
        const btnRegisterMember = document.getElementById('btnRegisterMember');
        if (btnRegisterMember) btnRegisterMember.addEventListener('click', showMemberRegisterModal);
        else console.error('未找到 btnRegisterMember 元素');

        const btnCloseMemberRegister = document.getElementById('btnCloseMemberRegister');
        if (btnCloseMemberRegister) btnCloseMemberRegister.addEventListener('click', closeMemberRegisterModal);
        else console.error('未找到 btnCloseMemberRegister 元素');

        // 添加支付明细
        const btnAddPay = document.getElementById('btnAddPay');
        if (btnAddPay) {
            btnAddPay.addEventListener('click', addPayDetail);
            console.log('btnAddPay 事件绑定成功');
        } else {
            console.error('未找到 btnAddPay 元素');
        }
        
        const newPayAmount = document.getElementById('newPayAmount');
        if (newPayAmount) newPayAmount.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') addPayDetail();
        });
        else console.error('未找到 newPayAmount 元素');

        const newPayMethod = document.getElementById('newPayMethod');
        if (newPayMethod) newPayMethod.addEventListener('change', toggleMobileMoneyFields);
        else console.error('未找到 newPayMethod 元素');

        const mobilePhone = document.getElementById('mobilePhone');
        if (mobilePhone) mobilePhone.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') addPayDetail();
        });

        const btnConfirmBatch = document.getElementById('btnConfirmBatch');
        if (btnConfirmBatch) btnConfirmBatch.addEventListener('click', confirmBatchSelection);
        else console.error('未找到 btnConfirmBatch 元素');
        
        console.log('事件绑定完成');
        
        // 操作按钮
        const btnSaveDraft = document.getElementById('btnSaveDraft');
        if (btnSaveDraft) btnSaveDraft.addEventListener('click', saveDraft);
        else console.error('未找到 btnSaveDraft 元素');
        
        const btnClear = document.getElementById('btnClear');
        if (btnClear) btnClear.addEventListener('click', clearCart);
        else console.error('未找到 btnClear 元素');
        
        const btnConfirm = document.getElementById('btnConfirm');
        if (btnConfirm) btnConfirm.addEventListener('click', confirmOrder);
        else console.error('未找到 btnConfirm 元素');

        // 支付模态框
        const btnCancelPay = document.getElementById('btnCancelPay');
        if (btnCancelPay) btnCancelPay.addEventListener('click', closePayModal);
        else console.error('未找到 btnCancelPay 元素');
        
        const btnConfirmPay = document.getElementById('btnConfirmPay');
        if (btnConfirmPay) btnConfirmPay.addEventListener('click', processPayment);
        else console.error('未找到 btnConfirmPay 元素');
        
        const inputAmount = document.getElementById('inputAmount');
        if (inputAmount) inputAmount.addEventListener('input', updatePayModal);
        else console.error('未找到 inputAmount 元素');
        
        // 新增功能按钮
        const btnQueryTrade = document.getElementById('btnQueryTrade');
        if (btnQueryTrade) btnQueryTrade.addEventListener('click', queryTrade);
        else console.error('未找到 btnQueryTrade 元素');
        
        const btnDailySettlement = document.getElementById('btnDailySettlement');
        if (btnDailySettlement) btnDailySettlement.addEventListener('click', dailySettlement);
        else console.error('未找到 btnDailySettlement 元素');
        
        const btnRefund = document.getElementById('btnRefund');
        if (btnRefund) btnRefund.addEventListener('click', showRefund);
        else console.error('未找到 btnRefund 元素');
        
        // 顶部按钮
        const btnUploadData = document.getElementById('btnUploadData');
        if (btnUploadData) btnUploadData.addEventListener('click', uploadData);
        else console.error('未找到 btnUploadData 元素');
        
        const btnDownloadData = document.getElementById('btnDownloadData');
        if (btnDownloadData) btnDownloadData.addEventListener('click', downloadData);
        else console.error('未找到 btnDownloadData 元素');
        
        const btnCashierShift = document.getElementById('btnCashierShift');
        if (btnCashierShift) btnCashierShift.addEventListener('click', cashierShift);
        else console.error('未找到 btnCashierShift 元素');
        
        // 交易查询按钮
        const btnQuerySearch = document.getElementById('btnQuerySearch');
        if (btnQuerySearch) btnQuerySearch.addEventListener('click', () => {
            currentTradePage = 1;
            loadTradeList(currentTradePage);
        });
        else console.error('未找到 btnQuerySearch 元素');
        
        // 订单详情按钮
        const btnCreateRefund = document.getElementById('btnCreateRefund');
        if (btnCreateRefund) btnCreateRefund.addEventListener('click', createRefundFromDetail);
        else console.error('未找到 btnCreateRefund 元素');
        
        // 退货按钮
        const btnSubmitRefund = document.getElementById('btnSubmitRefund');
        if (btnSubmitRefund) btnSubmitRefund.addEventListener('click', submitRefund);
        else console.error('未找到 btnSubmitRefund 元素');
        
        const btnAddRefundPay = document.getElementById('btnAddRefundPay');
        if (btnAddRefundPay) btnAddRefundPay.addEventListener('click', addRefundPayDetail);
        else console.error('未找到 btnAddRefundPay 元素');
        
        const refundPayAmount = document.getElementById('refundPayAmount');
        if (refundPayAmount) refundPayAmount.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') addRefundPayDetail();
        });
        else console.error('未找到 refundPayAmount 元素');
        
        // 日结按钮
        const btnSettlementSearch = document.getElementById('btnSettlementSearch');
        if (btnSettlementSearch) btnSettlementSearch.addEventListener('click', loadSettlement);
        else console.error('未找到 btnSettlementSearch 元素');
        
        const btnCreateSettlement = document.getElementById('btnCreateSettlement');
        if (btnCreateSettlement) btnCreateSettlement.addEventListener('click', () => {
            const btn = document.getElementById('btnCreateSettlement');
            if (btn.disabled) return;
            
            const date = document.getElementById('settlementDate').value;
            btn.disabled = true;
            btn.textContent = '处理中...';
            
            fetch('/psi/cashier/settlement', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...getRequestHeaders()
                },
                body: JSON.stringify({ settleDate: date })
            })
            .then(response => response.json())
            .then(data => {
                if (data.code === 200) {
                    alert('日结生成成功');
                    loadSettlement();
                    // 日结成功后更新缓存
                    updateSettlementCache();
                } else {
                    alert(data.message || '生成失败');
                }
            })
            .finally(() => {
                btn.disabled = false;
                btn.textContent = '生成日结';
            });
        });
    }

    function searchProduct() {
        // 检查日结状态
        if (settlementCache.canSell === false && settlementCache.unsettledDate) {
            alert('请先完成 ' + settlementCache.unsettledDate + ' 的日结，然后才能进行销售');
            return;
        }
        
        // 如果缓存为空或过期，先检查日结状态
        if (settlementCache.canSell === null || 
            (settlementCache.lastCheckTime && 
             new Date().getTime() - settlementCache.lastCheckTime > 60000)) {
            checkSettlementStatus();
            // 如果缓存仍为空，允许查询（首次加载时）
            if (settlementCache.canSell === false && settlementCache.unsettledDate) {
                alert('请先完成 ' + settlementCache.unsettledDate + ' 的日结，然后才能进行销售');
                return;
            }
        }
        
        const type = document.getElementById('searchType').value;
        const keyword = document.getElementById('searchInput').value.trim();
        
        if (!keyword) {
            alert('请输入搜索内容');
            return;
        }

        const url = type === 'barcode' 
            ? `/psi/cashier/product-sku-sale-unit/barcode/${encodeURIComponent(keyword)}`
            : `/psi/cashier/product-sku-sale-unit/search?goodsName=${encodeURIComponent(keyword)}`;

        const token = localStorage.getItem('cashier_token');
        
        fetch(url, {
            headers: getRequestHeaders()
        })
            .then(response => response.json())
            .then(data => {
                if (data.code === 200 && data.data && data.data.length > 0) {
                    const product = data.data[0];
                    const productData = {
                        id: product.skuNo + '-' + product.saleUnitId,
                        name: product.goodsName,
                        price: parseFloat(product.salePrice),
                        priceUsd: product.salePriceUsd != null ? parseFloat(product.salePriceUsd) : null,
                        unit: product.saleUnitSymbol || product.saleUnitName,
                        skuNo: product.skuNo,
                        skuId: product.skuId,
                        barcode: product.barcode,
                        saleUnitId: product.saleUnitId,
                        taxRate: product.taxRate != null ? parseFloat(product.taxRate) : 0.16,
                        isTaxInclusive: product.isTaxInclusive === 1 || product.isTaxInclusive === true,
                        hasBatch: product.batchManaged === 1 || product.batchManaged === true || product.hasBatch === true
                    };
                    if (productData.hasBatch) {
                        openBatchSelection(productData);
                    } else {
                        addToCart(productData);
                    }
                    document.getElementById('searchInput').value = '';
                } else if (data.code === 500 && data.message === '请先登录') {
                    alert('请先登录');
                    window.location.href = 'index.html';
                } else if (data.message && data.message.includes('日结')) {
                    // 后端返回日结错误，更新缓存
                    settlementCache.canSell = false;
                    const match = data.message.match(/(\d{4}-\d{2}-\d{2})/);
                    if (match) {
                        settlementCache.unsettledDate = match[1];
                    }
                    alert(data.message);
                } else {
                    alert(data.message || '未找到商品');
                }
            })
            .catch(error => {
                console.error('查询商品失败:', error);
                alert('查询商品失败，请重试');
            });
    }

    function resolveDisplayPrice(product) {
        if (currentCurrency === 'USD' && product.priceUsd != null) {
            return parseFloat(product.priceUsd);
        }
        return parseFloat(product.price);
    }

    function addToCart(product) {
        // 如果商品需要选择批次，先弹窗
        if (product.hasBatch) {
            openBatchSelection(product);
            return;
        }
        doAddToCart(product);
    }

    function doAddToCart(product) {
        const price = resolveDisplayPrice(product);
        const displayTotal = calculateVat(price * product.quantity, product.taxRate).total;
        const existingItem = cart.find(item => item.id === product.id && item.batchNo === (product.batchNo || ''));
        if (existingItem) {
            existingItem.quantity = parseFloat((existingItem.quantity + (product.quantity || 1)).toFixed(4));
            const vat = calculateVat(existingItem.price * existingItem.quantity, existingItem.taxRate);
            existingItem.total = vat.total.toFixed(2);
        } else {
            cart.push({
                id: product.id,
                name: product.name,
                price: price,
                priceUsd: product.priceUsd,
                unit: product.unit,
                skuNo: product.skuNo,
                skuId: product.skuId,
                barcode: product.barcode,
                quantity: product.quantity || 1,
                total: displayTotal.toFixed(2),
                taxRate: product.taxRate != null ? parseFloat(product.taxRate) : 0.16,
                isTaxInclusive: product.isTaxInclusive === true,
                batchNo: product.batchNo || '',
                batchExpireDate: product.batchExpireDate || ''
            });
        }
        renderCart();
        updatePaySummary();
    }

    let batchSelectionProduct = null;
    let selectedBatch = null;

    function openBatchSelection(product) {
        batchSelectionProduct = product;
        selectedBatch = null;
        const content = document.getElementById('batchSelectContent');
        content.innerHTML = '<div class="empty-cart">加载中...</div>';
        document.getElementById('batchSelectModal').classList.add('show');
        fetch(`/psi/cashier/batch/${encodeURIComponent(product.skuNo)}`, { headers: getRequestHeaders() })
            .then(response => response.json())
            .then(data => {
                if (data.code === 200 && data.data && data.data.length > 0) {
                    renderBatchList(data.data);
                } else {
                    content.innerHTML = `<div class="empty-cart">${I18n.t('hint.emptyBatch', '暂无可用批次')}</div>`;
                }
            })
            .catch(error => {
                console.error('查询批次失败:', error);
                content.innerHTML = '<div class="empty-cart">查询批次失败</div>';
            });
    }

    function renderBatchList(batches) {
        const content = document.getElementById('batchSelectContent');
        let html = '<table class="data-table" id="batchSelectTable"><thead><tr>' +
            '<th>' + I18n.t('label.batchNo', '批次号') + '</th>' +
            '<th>' + I18n.t('label.expireDate', '保质期') + '</th>' +
            '<th>' + I18n.t('label.availableQty', '可用库存') + '</th>' +
            '<th>' + I18n.t('label.action', '操作') + '</th>' +
            '</tr></thead><tbody>';
        batches.forEach((batch, index) => {
            html += `<tr data-index="${index}" data-batch="${JSON.stringify(batch).replace(/"/g, '&quot;')}">
                <td>${batch.batchNo || '-'}</td>
                <td>${batch.expireDate || '-'}</td>
                <td>${batch.availableQuantity != null ? batch.availableQuantity : '-'}</td>
                <td><button class="btn-detail" onclick="CashierModule.selectBatch(${index})">选择</button></td>
            </tr>`;
        });
        html += '</tbody></table>';
        content.innerHTML = html;
    }

    function selectBatch(index) {
        const row = document.querySelector(`#batchSelectTable tbody tr[data-index="${index}"]`);
        if (!row) return;
        const batchAttr = row.getAttribute('data-batch');
        try {
            selectedBatch = JSON.parse(batchAttr.replace(/&quot;/g, '"'));
        } catch (e) {
            console.error('解析批次数据失败:', e);
            return;
        }
        document.querySelectorAll('#batchSelectTable tbody tr').forEach(tr => tr.style.background = '');
        row.style.background = '#e8f6ff';
    }

    function confirmBatchSelection() {
        if (!selectedBatch) {
            alert(I18n.t('hint.selectBatchFirst', '请先选择批次'));
            return;
        }
        document.getElementById('batchSelectModal').classList.remove('show');
        const product = { ...batchSelectionProduct };
        product.batchNo = selectedBatch.batchNo;
        product.batchExpireDate = selectedBatch.expireDate;
        doAddToCart(product);
    }

    function toggleMobileMoneyFields() {
        const method = document.getElementById('newPayMethod').value;
        const fields = document.getElementById('mobileMoneyFields');
        if (fields) {
            fields.style.display = method === 'mobileMoney' ? 'flex' : 'none';
        }
    }

    function renderCart() {
        const tbody = document.getElementById('orderItems');
        const totalItems = document.getElementById('totalItems');
        const totalAmount = document.getElementById('totalAmount');
        const finalAmount = document.getElementById('finalAmount');

        if (cart.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="empty-cart">' + I18n.t('hint.emptyCart', '购物车为空，请选择商品') + '</td></tr>';
            totalItems.textContent = '0';
            totalAmount.textContent = '0.00';
            finalAmount.textContent = '0.00';
            updateVatDisplay();
            return;
        }

        let html = '';
        let totalQty = 0;
        let totalPrice = 0;
        let totalNet = 0;
        let totalTax = 0;

        cart.forEach((item, index) => {
            totalQty += item.quantity;
            const vat = calculateVat(item.price * item.quantity, item.taxRate);
            item.total = vat.total.toFixed(2);
            totalPrice += vat.total;
            totalNet += vat.net;
            totalTax += vat.tax;
            const batchLabel = item.batchNo ? `<div style="font-size:11px;color:#888;">${I18n.t('label.batchNo', '批次')}: ${item.batchNo}${item.batchExpireDate ? ' (' + item.batchExpireDate + ')' : ''}</div>` : '';
            html += `
                <tr>
                    <td class="product-index">${index + 1}</td>
                    <td class="product-name">${item.name}${batchLabel}</td>
                    <td>
                        <input type="number" class="price-input" value="${formatNumber(item.price)}" step="0.0001" min="0.01" onchange="CashierModule.updatePrice(${index}, this)" onblur="CashierModule.validatePrice(this)" />
                    </td>
                    <td>
                        <div class="qty-control">
                            <button class="qty-btn" onclick="CashierModule.changeQty(${index}, -1)">-</button>
                            <input type="number" class="qty-input" value="${formatNumber(item.quantity)}" step="0.01" min="0.01" onchange="CashierModule.updateQty(${index}, this)" onblur="CashierModule.validateQty(this)" />
                            <button class="qty-btn" onclick="CashierModule.changeQty(${index}, 1)">+</button>
                        </div>
                    </td>
                    <td>${item.unit}</td>
                    <td class="product-total">${formatMoney(item.total)}</td>
                    <td><button class="btn-delete" onclick="CashierModule.removeItem(${index})">删除</button></td>
                </tr>
            `;
        });

        tbody.innerHTML = html;
        totalItems.textContent = totalQty;
        totalAmount.textContent = totalPrice.toFixed(2);
        updateVatDisplay(totalNet, totalTax, totalPrice);
        updateFinalAmount();
    }

    function updateVatDisplay(net, tax, total) {
        const netEl = document.getElementById('netAmount');
        const vatEl = document.getElementById('vatAmount');
        if (netEl) netEl.textContent = (net || 0).toFixed(2);
        if (vatEl) vatEl.textContent = (tax || 0).toFixed(2);
    }

    function changeQty(index, delta) {
        const item = cart[index];
        item.quantity += delta;
        
        if (item.quantity <= 0) {
            cart.splice(index, 1);
        } else {
            const vat = calculateVat(item.price * item.quantity, item.taxRate);
            item.total = vat.total.toFixed(2);
        }
        renderCart();
        updatePaySummary();
    }

    function updateQty(index, input) {
        const value = parseFloat(input.value);
        
        if (isNaN(value) || value <= 0) {
            input.value = cart[index].quantity;
            input.classList.add('error');
            setTimeout(() => {
                input.classList.remove('error');
            }, 2000);
            return;
        }
        
        const fixedValue = parseFloat(value.toFixed(4));
        if (fixedValue <= 0) {
            input.value = cart[index].quantity;
            input.classList.add('error');
            setTimeout(() => {
                input.classList.remove('error');
            }, 2000);
            return;
        }
        
        cart[index].quantity = fixedValue;
        const vat = calculateVat(cart[index].price * cart[index].quantity, cart[index].taxRate);
        cart[index].total = vat.total.toFixed(2);
        input.classList.remove('error');
        renderCart();
        updatePaySummary();
    }

    function validateQty(input) {
        const value = parseFloat(input.value);
        
        if (isNaN(value) || value <= 0) {
            input.classList.add('error');
            alert('数量必须大于0');
            setTimeout(() => {
                input.classList.remove('error');
            }, 2000);
        } else {
            input.classList.remove('error');
        }
    }

    function updatePrice(index, input) {
        const value = parseFloat(input.value);
        
        if (isNaN(value) || value <= 0) {
            input.value = cart[index].price.toFixed(4);
            input.classList.add('error');
            setTimeout(() => {
                input.classList.remove('error');
            }, 2000);
            return;
        }
        
        const fixedValue = parseFloat(value.toFixed(4));
        if (fixedValue <= 0) {
            input.value = cart[index].price.toFixed(4);
            input.classList.add('error');
            setTimeout(() => {
                input.classList.remove('error');
            }, 2000);
            return;
        }
        
        cart[index].price = fixedValue;
        const vat = calculateVat(cart[index].price * cart[index].quantity, cart[index].taxRate);
        cart[index].total = vat.total.toFixed(2);
        input.classList.remove('error');
        renderCart();
        updatePaySummary();
    }

    function validatePrice(input) {
        const value = parseFloat(input.value);
        
        if (isNaN(value) || value <= 0) {
            input.classList.add('error');
            alert('单价必须大于0');
            setTimeout(() => {
                input.classList.remove('error');
            }, 2000);
        } else {
            input.classList.remove('error');
        }
    }

    function updateAmount(index, input) {
        const value = parseFloat(input.value);
        
        if (isNaN(value) || value <= 0) {
            input.value = cart[index].total;
            input.classList.add('error');
            setTimeout(() => {
                input.classList.remove('error');
            }, 2000);
            return;
        }
        
        const fixedValue = parseFloat(value.toFixed(2));
        if (fixedValue <= 0) {
            input.value = cart[index].total;
            input.classList.add('error');
            setTimeout(() => {
                input.classList.remove('error');
            }, 2000);
            return;
        }
        
        cart[index].total = fixedValue.toFixed(2);
        input.classList.remove('error');
        renderCart();
        updatePaySummary();
    }

    function validateAmount(input) {
        const value = parseFloat(input.value);
        
        if (isNaN(value) || value <= 0) {
            input.classList.add('error');
            alert('金额必须大于0');
            setTimeout(() => {
                input.classList.remove('error');
            }, 2000);
        } else {
            input.classList.remove('error');
        }
    }

    function removeItem(index) {
        cart.splice(index, 1);
        renderCart();
        updatePaySummary();
    }

    function loadCurrentUser() {
        const token = localStorage.getItem('cashier_token');
        if (!token) {
            window.location.href = 'index.html';
            return;
        }
        if (token) {
            fetch('/psi/cashier/auth/validate', {
                headers: getRequestHeaders()
            })
            .then(response => response.json())
            .then(data => {
                if (data.code === 200 && data.data) {
                    const user = data.data;
                    const cashierName = document.getElementById('cashierName');
                    if (cashierName) {
                        cashierName.textContent = '收银员：' + (user.realName || user.username);
                    }
                    const headerStore = document.getElementById('headerStore');
                    if (headerStore) {
                        headerStore.textContent = '店铺：' + (user.shopCode || '默认店铺');
                    }
                }
            })
            .catch(error => {
                console.error('获取用户信息失败:', error);
            });
        }
    }

    function searchMember() {
        const memberInput = document.getElementById('memberInput').value.trim();
        if (!memberInput) {
            alert('请输入会员信息');
            return;
        }
        
        fetch(`/psi/cashier/member/search?keyword=${encodeURIComponent(memberInput)}`, {
            headers: getRequestHeaders()
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200 && data.data) {
                currentMember = data.data;
                showMemberInfo();
                updateFinalAmount();
            } else {
                alert('未找到会员信息');
            }
        })
        .catch(error => {
            console.error('查询会员失败:', error);
            // 如果后端API不可用，使用模拟数据
            currentMember = {
                name: '测试会员',
                level: 'VIP会员',
                discount: 0.9,
                points: 1280
            };
            showMemberInfo();
            updateFinalAmount();
        });
    }
    
    function showMemberInfo() {
        if (currentMember) {
            document.getElementById('memberName').textContent = currentMember.name || '';
            document.getElementById('memberLevel').textContent = currentMember.level || '';
            document.getElementById('memberDiscount').textContent = currentMember.discount ? (currentMember.discount * 10).toFixed(1) + '折' : '无折扣';
            document.getElementById('memberPoints').textContent = currentMember.points != null ? currentMember.points + ' 积分' : '0 积分';
            document.getElementById('memberBalance').textContent = currentMember.balance != null ? '¥' + currentMember.balance.toFixed(2) : '¥0.00';
            document.getElementById('memberInfo').style.display = 'block';
            document.getElementById('btnSearchMember').style.display = 'none';
            document.getElementById('btnRegisterMember').style.display = 'none';
            document.getElementById('btnClearMember').style.display = 'block';
            document.getElementById('memberInput').readOnly = true;
        }
    }
    
    function clearMember() {
        currentMember = null;
        document.getElementById('memberInfo').style.display = 'none';
        document.getElementById('btnSearchMember').style.display = 'block';
        document.getElementById('btnRegisterMember').style.display = 'block';
        document.getElementById('btnClearMember').style.display = 'none';
        document.getElementById('memberInput').readOnly = false;
        document.getElementById('memberInput').value = '';
        updateFinalAmount();
    }

    function showMemberRegisterModal() {
        const phone = document.getElementById('memberInput').value.trim();
        if (phone) {
            document.getElementById('registerPhone').value = phone;
        }
        document.getElementById('registerName').value = '';
        document.getElementById('memberRegisterModal').classList.add('show');
    }

    function closeMemberRegisterModal() {
        document.getElementById('memberRegisterModal').classList.remove('show');
    }

    function clearMemberRegisterForm() {
        document.getElementById('registerPhone').value = '';
        document.getElementById('registerName').value = '';
    }

    function submitMemberRegister() {
        const phone = document.getElementById('registerPhone').value.trim();
        const name = document.getElementById('registerName').value.trim();

        if (!phone) {
            alert('请输入手机号');
            return;
        }

        if (!/^1[3-9]\d{9}$/.test(phone)) {
            alert('请输入有效的手机号');
            return;
        }

        fetch('/psi/cashier/member/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getRequestHeaders()
            },
            body: JSON.stringify({ phone: phone, name: name })
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200 && data.data) {
                alert('会员注册成功！');
                closeMemberRegisterModal();
                // 自动查询并显示新注册的会员
                document.getElementById('memberInput').value = phone;
                searchMember();
            } else {
                alert(data.message || '注册失败');
            }
        })
        .catch(error => {
            console.error('注册会员失败:', error);
            alert('注册失败，请稍后重试');
        });
    }
    
    function updateFinalAmount() {
        const totalAmount = parseFloat(document.getElementById('totalAmount').textContent) || 0;
        let discount = 1;
        let discountAmount = 0;
        
        if (currentMember && currentMember.discount) {
            discount = currentMember.discount;
            discountAmount = totalAmount * (1 - discount);
        }
        
        const finalAmount = totalAmount * discount;
        
        if (discount < 1) {
            document.getElementById('discountItem').style.display = 'flex';
            document.getElementById('discountAmount').textContent = discountAmount.toFixed(2);
        } else {
            document.getElementById('discountItem').style.display = 'none';
        }
        
        document.getElementById('finalAmount').textContent = finalAmount.toFixed(2);
    }

    function addPayDetail() {
        const payMethod = document.getElementById('newPayMethod').value;
        const amount = parseFloat(document.getElementById('newPayAmount').value);

        if (!amount || amount <= 0) {
            alert(I18n.t('hint.enterAmount', '请输入有效的支付金额'));
            return;
        }

        const detail = {
            id: Date.now(),
            payMethod: payMethod,
            amount: amount
        };

        if (payMethod === 'mobileMoney') {
            const provider = document.getElementById('mobileProvider').value;
            const phone = document.getElementById('mobilePhone').value.trim();
            if (!phone) {
                alert(I18n.t('hint.enterPhone', '请输入手机号'));
                return;
            }
            detail.mobileProvider = provider;
            detail.mobilePhone = phone;
        }

        payDetails.push(detail);

        document.getElementById('newPayAmount').value = '';
        document.getElementById('mobilePhone').value = '';
        renderPayDetails();
        updatePaySummary();
    }

    function renderPayDetails() {
        const list = document.getElementById('payDetailsList');
        
        if (payDetails.length === 0) {
            list.innerHTML = '<div class="empty-pay-list">' + I18n.t('hint.emptyPayList', '暂无支付明细') + '</div>';
            return;
        }

        let html = '';
        payDetails.forEach((detail, index) => {
            const extra = detail.mobileProvider ? ` (${detail.mobileProvider})` : '';
            html += `
                <div class="pay-detail-item">
                    <span class="pay-detail-method">${payMethodNames[detail.payMethod]}${extra}</span>
                    <span class="pay-detail-amount">${formatMoney(detail.amount)}</span>
                    <button class="pay-detail-delete" onclick="CashierModule.removePayDetail(${index})">删除</button>
                </div>
            `;
        });
        
        list.innerHTML = html;
    }

    function removePayDetail(index) {
        payDetails.splice(index, 1);
        renderPayDetails();
        updatePaySummary();
    }

    function updatePaySummary() {
        const orderTotal = parseFloat(document.getElementById('totalAmount').textContent) || 0;
        const paidTotal = payDetails.reduce((sum, detail) => sum + detail.amount, 0);
        const remaining = (orderTotal - paidTotal).toFixed(2);
        const change = (paidTotal - orderTotal).toFixed(2);

        document.getElementById('orderTotal').textContent = orderTotal.toFixed(2);
        document.getElementById('paidTotal').textContent = paidTotal.toFixed(2);
        document.getElementById('remainingAmount').textContent = remaining;
        document.getElementById('changeAmount').textContent = change;

        const remainingEl = document.getElementById('remainingAmount');
        const changeEl = document.getElementById('changeAmount');
        
        if (parseFloat(remaining) > 0) {
            remainingEl.style.color = '#e74c3c';
            changeEl.style.color = '#27ae60';
        } else {
            remainingEl.style.color = '#27ae60';
            changeEl.style.color = '#27ae60';
        }

        // 同步更新 currency symbol
        updateCurrencySymbols();
    }

    function saveDraft() {
        if (cart.length === 0) {
            // 购物车为空，查询挂单列表
            loadSuspendedOrders();
            document.getElementById('suspendedModal').classList.add('show');
            return;
        }
        
        const total = parseFloat(document.getElementById('totalAmount').textContent) || 0;
        const draftData = {
            posId: 'POS001',
            operatorId: 1,
            pendingName: '挂单',
            totalAmount: total,
            items: cart.map(item => ({
                skuId: item.skuId || null,
                skuCode: item.skuNo || '',
                barCode: item.barcode || '',
                productName: item.name,
                saleUnitName: item.unit,
                saleQuantity: item.quantity,
                unitPrice: item.price,
                memberPrice: item.price,
                subtotal: parseFloat(item.total)
            }))
        };
        
        fetch('/psi/cashier/draft/save', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getRequestHeaders()
            },
            body: JSON.stringify(draftData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                clearCart();
                alert('挂单成功！');
            } else {
                alert('挂单失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('挂单失败:', error);
            alert('挂单失败，请重试');
        });
    }

    function loadSuspendedOrders() {
        fetch('/psi/cashier/draft/list/operator/1', {
            headers: getRequestHeaders()
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                suspendedOrders = data.data.map(draft => ({
                    id: draft.pendingNo,
                    time: new Date().toLocaleString('zh-CN'),
                    items: draft.items.map(item => ({
                        id: item.skuCode,
                        skuNo: item.skuCode,
                        name: item.productName,
                        price: item.unitPrice,
                        unit: item.saleUnitName,
                        quantity: item.saleQuantity,
                        total: item.subtotal
                    })),
                    totalAmount: draft.totalAmount
                }));
                renderSuspendedOrders();
            }
        })
        .catch(error => {
            console.error('加载挂单列表失败:', error);
        });
    }

    function renderSuspendedOrders() {
        const list = document.getElementById('suspendedOrdersList');
        if (!list) return;
        
        if (suspendedOrders.length === 0) {
            list.innerHTML = '<div class="empty-suspended-list">暂无挂单</div>';
            return;
        }
        
        let html = '';
        suspendedOrders.forEach((order, index) => {
            html += `
                <div class="suspended-item">
                    <div class="suspended-header">
                        <span class="suspended-index">${index + 1}</span>
                        <span class="suspended-order-no">${order.id}</span>
                        <span class="suspended-total">¥${formatNumber(order.totalAmount)}</span>
                    </div>
                    <div class="suspended-items">
                        ${order.items.slice(0, 3).map(item => item.name).join('、')}${order.items.length > 3 ? '...' : ''}
                    </div>
                    <div class="suspended-actions">
                        <button class="btn btn-sm btn-primary" onclick="CashierModule.resumeOrder('${order.id}')">恢复</button>
                        <button class="btn btn-sm btn-secondary" onclick="CashierModule.deleteSuspendedOrder('${order.id}')">删除</button>
                    </div>
                </div>
            `;
        });
        
        list.innerHTML = html;
    }

    function resumeOrder(pendingNo) {
        // 从后端获取挂单明细数据
        fetch(`/psi/cashier/draft/${pendingNo}`, {
            headers: getRequestHeaders()
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200 && data.data) {
                const draft = data.data;
                
                cart.length = 0;
                if (draft.items) {
                    draft.items.forEach(item => {
                        cart.push({
                            id: item.skuId || item.skuCode,
                            skuNo: item.skuCode,
                            skuId: item.skuId,
                            barcode: item.barCode,
                            name: item.productName,
                            price: item.unitPrice,
                            unit: item.saleUnitName || '件',
                            quantity: item.saleQuantity,
                            total: item.subtotal
                        });
                    });
                }
                
                // 删除已恢复的挂单
                fetch(`/psi/cashier/draft/${pendingNo}`, {
                    method: 'DELETE',
                    headers: getRequestHeaders()
                })
                .then(() => {
                    document.getElementById('suspendedModal').classList.remove('show');
                    renderCart();
                    renderPayDetails();
                    updatePaySummary();
                })
                .catch(error => {
                    console.error('删除挂单失败:', error);
                    document.getElementById('suspendedModal').classList.remove('show');
                    renderCart();
                    renderPayDetails();
                    updatePaySummary();
                });
            } else {
                alert('获取挂单详情失败');
            }
        })
        .catch(error => {
            console.error('获取挂单详情失败:', error);
            alert('获取挂单详情失败');
        });
    }

    function deleteSuspendedOrder(pendingNo) {
        if (!confirm('确定要删除此挂单吗？')) return;
        
        fetch(`/psi/cashier/draft/${pendingNo}`, {
            method: 'DELETE',
            headers: getRequestHeaders()
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                loadSuspendedOrders();
                alert('挂单已删除');
            } else {
                alert('删除失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('删除挂单失败:', error);
            alert('删除挂单失败，请重试');
        });
    }

    function clearCart(skipConfirm) {
        if (cart.length === 0 && payDetails.length === 0) return;
        if (skipConfirm || confirm('确定要清空购物车和支付明细吗？')) {
            cart.length = 0;
            payDetails.length = 0;
            renderCart();
            renderPayDetails();
            updatePaySummary();
        }
    }

    function confirmOrder() {
        const total = parseFloat(document.getElementById('totalAmount').textContent) || 0;
        if (total === 0) {
            alert(I18n.t('hint.emptyCart', '请先添加商品'));
            return;
        }

        // 使用应付金额（已扣除会员折扣）
        const finalAmount = parseFloat(document.getElementById('finalAmount').textContent) || 0;
        
        // 如果没有添加支付方式，先显示支付模态框
        if (payDetails.length === 0) {
            alert(I18n.t('hint.emptyPayList', '请先添加支付方式和金额'));
            return;
        }
        
        const paidTotal = payDetails.reduce((sum, detail) => sum + detail.amount, 0);
        if (paidTotal < finalAmount) {
            const diff = (finalAmount - paidTotal).toFixed(2);
            alert(I18n.t('hint.payInsufficient', '支付金额不足，还差') + ' ' + getCurrencySymbol() + diff);
            return;
        }

        // 确认支付
        if (confirm(I18n.t('hint.confirmPay', '确认支付') + ' ' + formatMoney(paidTotal) + '？')) {
            processPayment();
        }
    }

    function updatePayModal() {
        const total = parseFloat(document.getElementById('payAmount').textContent);
        const paid = parseFloat(document.getElementById('inputAmount').value) || 0;
        const change = (paid - total).toFixed(2);
        
        document.getElementById('modalChange').textContent = change;
        document.getElementById('changeInfo').style.display = paid >= total ? 'block' : 'none';
    }

    function closePayModal() {
        document.getElementById('payModal').classList.remove('show');
    }

    function processPayment() {
        const total = parseFloat(document.getElementById('totalAmount').textContent) || 0;
        const finalAmount = parseFloat(document.getElementById('finalAmount').textContent) || 0;
        const paid = payDetails.reduce((sum, detail) => sum + detail.amount, 0);
        const netAmount = parseFloat(document.getElementById('netAmount').textContent) || 0;
        const vatAmount = parseFloat(document.getElementById('vatAmount').textContent) || 0;
        
        if (paid < finalAmount) {
            alert(I18n.t('hint.payInsufficient', '实收金额不足'));
            return;
        }

        const discountAmount = total - finalAmount;
        const memberId = currentMember ? currentMember.memberId : null;

        const orderData = {
            cashierNo: 'ORD' + Date.now(),
            shopCode: 'SH001',
            posId: 'POS001',
            bizType: 20,
            totalAmount: total,
            realAmount: paid,
            payAmount: paid,
            changeAmount: paid > finalAmount ? parseFloat((paid - finalAmount).toFixed(2)) : 0,
            discountAmount: discountAmount,
            memberId: memberId,
            operatorId: 1,
            operatorName: 'admin',
            currency: currentCurrency,
            exchangeRate: exchangeRates[currentCurrency] || 1,
            originalAmount: currentCurrency === 'ZMW' ? total : convertCurrency(total, currentCurrency, 'ZMW'),
            netAmount: netAmount,
            taxAmount: vatAmount,
            items: cart.map(item => {
                const itemVat = calculateVat(item.price * item.quantity, item.taxRate);
                return {
                    goodsCode: item.skuNo || 'SKU000',
                    goodsName: item.name,
                    barCode: item.barcode || '',
                    skuId: item.skuId || null,
                    skuCode: item.skuNo || '',
                    quantity: item.quantity,
                    unit: item.unit,
                    unitPrice: item.price,
                    amount: parseFloat(item.total),
                    taxRate: item.taxRate,
                    isTaxInclusive: item.isTaxInclusive ? 1 : 0,
                    netAmount: itemVat.net,
                    taxAmount: itemVat.tax,
                    batchNo: item.batchNo,
                    currency: currentCurrency,
                    unitPriceUsd: item.priceUsd || null
                };
            }),
            pays: payDetails.map(detail => {
                const base = {
                    payAmount: detail.amount,
                    currency: currentCurrency,
                    payChannel: detail.payMethod.toUpperCase()
                };
                if (detail.payMethod === 'cash') {
                    base.payId = 1;
                    base.payName = '现金';
                } else if (detail.payMethod === 'wechat') {
                    base.payId = 2;
                    base.payName = '微信';
                } else if (detail.payMethod === 'alipay') {
                    base.payId = 3;
                    base.payName = '支付宝';
                } else if (detail.payMethod === 'memberCard') {
                    base.payId = 4;
                    base.payName = '会员卡';
                } else if (detail.payMethod === 'mobileMoney') {
                    base.payId = 5;
                    base.payName = 'Mobile Money';
                    base.mobileProvider = detail.mobileProvider;
                    base.mobilePhone = detail.mobilePhone;
                    base.payChannel = detail.mobileProvider.toUpperCase();
                }
                return base;
            })
        };

        if (!isOnline) {
            saveOfflineOrder(orderData).then(() => {
                updatePendingBadge();
                closePayModal();
                clearCart(true);
                alert(I18n.t('message.offlineSaved', '订单已离线保存，联网后自动同步'));
            }).catch(error => {
                console.error('离线保存失败:', error);
                alert(I18n.t('message.offlineSaveFailed', '离线保存失败'));
            });
            return;
        }
        
        fetch('/psi/cashier/save', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getRequestHeaders()
            },
            body: JSON.stringify(orderData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                closePayModal();
                clearCart(true);
            } else {
                alert(data.message || I18n.t('message.orderSaveFailed', '订单保存失败'));
            }
        })
        .catch(error => {
            console.error('保存订单失败:', error);
            if (confirm(I18n.t('message.saveOrderRetryOrOffline', '订单提交失败，是否转为离线订单？'))) {
                saveOfflineOrder(orderData).then(() => {
                    updatePendingBadge();
                    closePayModal();
                    clearCart(true);
                    alert(I18n.t('message.offlineSaved', '订单已离线保存，联网后自动同步'));
                });
            }
        });
    }

    let currentTradePage = 1;
    let totalTradePages = 1;
    
    function queryTrade() {
        currentTradePage = 1;
        document.getElementById('tradeQueryModal').classList.add('show');
        document.getElementById('queryDate').value = new Date().toISOString().split('T')[0];
        loadTradeList(currentTradePage);
    }
    
    function loadTradeList(pageNum) {
        const orderNo = document.getElementById('queryOrderNo').value;
        const date = document.getElementById('queryDate').value;
        
        let url = `/psi/cashier/order?pageNum=${pageNum}&pageSize=10`;
        if (orderNo) url += `&orderNo=${encodeURIComponent(orderNo)}`;
        if (date) url += `&date=${date}`;
        
        fetch(url, {
            headers: getRequestHeaders()
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderTradeList(data.data);
                currentTradePage = data.data.pageNum;
                totalTradePages = data.data.pages;
                renderTradePagination();
            }
        })
        .catch(error => {
            console.error('查询订单失败:', error);
        });
    }
    
    function renderTradeList(pageData) {
        const tbody = document.getElementById('tradeList');
        const list = pageData.list || [];
        
        if (list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="empty-cart">暂无数据</td></tr>';
            return;
        }
        
        let html = '';
        list.forEach(order => {
            const payStatus = order.payStatus === 1 ? '已支付' : '未支付';
            html += `
                <tr>
                    <td>${order.orderNo}</td>
                    <td>¥${order.totalAmount.toFixed(2)}</td>
                    <td>¥${order.realAmount.toFixed(2)}</td>
                    <td>${payStatus}</td>
                    <td>${order.createTime || '-'}</td>
                    <td>
                        <button class="btn-detail" onclick="CashierModule.viewOrderDetail('${order.orderNo}')">详情</button>
                    </td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
    }
    
    function renderTradePagination() {
        const pagination = document.getElementById('tradePagination');
        if (totalTradePages <= 1) {
            pagination.innerHTML = '';
            return;
        }
        
        let html = '';
        if (currentTradePage > 1) {
            html += `<button onclick="CashierModule.loadTradeList(${currentTradePage - 1})">上一页</button>`;
        }
        for (let i = 1; i <= totalTradePages; i++) {
            html += `<button ${i === currentTradePage ? 'class="active"' : ''} onclick="CashierModule.loadTradeList(${i})">${i}</button>`;
        }
        if (currentTradePage < totalTradePages) {
            html += `<button onclick="CashierModule.loadTradeList(${currentTradePage + 1})">下一页</button>`;
        }
        pagination.innerHTML = html;
    }
    
    function viewOrderDetail(orderNo) {
        document.getElementById('tradeQueryModal').classList.remove('show');
        document.getElementById('orderDetailModal').classList.add('show');
        
        fetch(`/psi/cashier/order/${orderNo}`, {
            headers: getRequestHeaders()
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderOrderDetail(data.data);
            }
        })
        .catch(error => {
            console.error('查询订单详情失败:', error);
        });
    }
    
    function renderOrderDetail(order) {
        document.getElementById('detailOrderNo').textContent = order.orderNo || '-';
        document.getElementById('detailTotalAmount').textContent = '¥' + (order.totalAmount || 0).toFixed(2);
        document.getElementById('detailRealAmount').textContent = '¥' + (order.realAmount || 0).toFixed(2);
        document.getElementById('detailPayStatus').textContent = order.payStatus === 1 ? '已支付' : '未支付';
        document.getElementById('detailCreateTime').textContent = order.createTime || '-';
        
        const items = order.items || [];
        const itemsTbody = document.getElementById('detailItems');
        if (items.length === 0) {
            itemsTbody.innerHTML = '<tr><td colspan="4" class="empty-cart">暂无商品</td></tr>';
        } else {
            let html = '';
            items.forEach(item => {
                html += `
                    <tr>
                        <td>${item.productName || item.goodsName || '-'}</td>
                        <td>¥${(item.unitPrice || 0).toFixed(2)}</td>
                        <td>${item.saleQuantity || item.quantity || 0}</td>
                        <td>¥${(item.subtotal || item.amount || 0).toFixed(2)}</td>
                    </tr>
                `;
            });
            itemsTbody.innerHTML = html;
        }
        
        const pays = order.pays || [];
        const paysTbody = document.getElementById('detailPays');
        if (pays.length === 0) {
            paysTbody.innerHTML = '<tr><td colspan="2" class="empty-cart">暂无支付记录</td></tr>';
        } else {
            let html = '';
            pays.forEach(pay => {
                const payMethod = payMethodNames[pay.payMethod] || pay.payMethod;
                html += `
                    <tr>
                        <td>${payMethod}</td>
                        <td>¥${pay.payAmount.toFixed(2)}</td>
                    </tr>
                `;
            });
            paysTbody.innerHTML = html;
        }
    }
    
    function dailySettlement() {
        document.getElementById('settlementModal').classList.add('show');
        document.getElementById('settlementDate').value = new Date().toISOString().split('T')[0];
        loadSettlement();
    }
    
    function loadSettlement() {
        const date = document.getElementById('settlementDate').value;
        
        fetch(`/psi/cashier/settlement/date/${date}`, {
            headers: getRequestHeaders()
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderSettlement(data.data);
            }
        })
        .catch(error => {
            console.error('查询日结失败:', error);
        });
    }
    
    function renderSettlement(data) {
        const orderCount = data.orderCount || 0;
        const totalAmount = data.totalAmount || 0;
        const realAmount = data.realAmount || 0;
        const refundAmount = data.refundAmount || 0;
        
        document.getElementById('settleOrderCount').textContent = orderCount;
        document.getElementById('settleTotalAmount').textContent = '¥' + totalAmount.toFixed(2);
        document.getElementById('settleRealAmount').textContent = '¥' + realAmount.toFixed(2);
        document.getElementById('settleRefundAmount').textContent = '¥' + refundAmount.toFixed(2);
        
        const payTypes = data.payTypes || [];
        const payTbody = document.getElementById('settlePayTypes');
        if (payTypes.length === 0) {
            payTbody.innerHTML = '<tr><td colspan="3" class="empty-cart">暂无支付数据</td></tr>';
        } else {
            let html = '';
            payTypes.forEach(pay => {
                const payMethod = payMethodNames[pay.payMethod] || pay.payMethod;
                html += `
                    <tr>
                        <td>${payMethod}</td>
                        <td>${pay.count || 0}</td>
                        <td>¥${(pay.amount || 0).toFixed(2)}</td>
                    </tr>
                `;
            });
            payTbody.innerHTML = html;
        }
        
        const orders = data.orders || [];
        const ordersTbody = document.getElementById('settleOrders');
        if (orders.length === 0) {
            ordersTbody.innerHTML = '<tr><td colspan="4" class="empty-cart">暂无订单</td></tr>';
        } else {
            let html = '';
            orders.forEach(order => {
                html += `
                    <tr>
                        <td>${order.orderNo}</td>
                        <td>¥${(order.totalAmount || 0).toFixed(2)}</td>
                        <td>${order.payMethod || '-'}</td>
                        <td>${order.createTime || '-'}</td>
                    </tr>
                `;
            });
            ordersTbody.innerHTML = html;
        }
    }
    
    function showRefund() {
        document.getElementById('refundModal').classList.add('show');
        document.getElementById('refundSourceOrderNo').value = '';
        document.getElementById('refundOrderInfo').style.display = 'none';
        refundPayDetails.length = 0;
        renderRefundPayDetails();
    }
    
    function addRefundPayDetail() {
        const payMethod = document.getElementById('refundPayMethod').value;
        const amount = parseFloat(document.getElementById('refundPayAmount').value);
        
        if (!amount || amount <= 0) {
            alert('请输入有效的退款金额');
            return;
        }

        const maxRefund = parseFloat(document.getElementById('refundTotalAmount').textContent.replace('¥', '')) || 0;
        const currentTotal = refundPayDetails.reduce((sum, detail) => sum + detail.amount, 0);
        
        if (currentTotal + amount > maxRefund) {
            alert(`退款金额超过可退金额，剩余可退：¥${(maxRefund - currentTotal).toFixed(2)}`);
            return;
        }

        refundPayDetails.push({
            id: Date.now(),
            payMethod: payMethod,
            amount: amount
        });
        
        document.getElementById('refundPayAmount').value = '';
        renderRefundPayDetails();
        updateRefundPayTotal();
    }
    
    function renderRefundPayDetails() {
        const list = document.getElementById('refundPayDetailsList');
        
        if (refundPayDetails.length === 0) {
            list.innerHTML = '<div class="empty-pay-list">暂无退款明细</div>';
            return;
        }

        let html = '';
        refundPayDetails.forEach((detail, index) => {
            html += `
                <div class="pay-detail-item">
                    <span class="pay-detail-method">${refundPayMethodNames[detail.payMethod] || detail.payMethod}</span>
                    <span class="pay-detail-amount">¥${detail.amount.toFixed(2)}</span>
                    <button class="pay-detail-delete" onclick="CashierModule.removeRefundPayDetail(${index})">删除</button>
                </div>
            `;
        });
        
        list.innerHTML = html;
    }
    
    function removeRefundPayDetail(index) {
        refundPayDetails.splice(index, 1);
        renderRefundPayDetails();
        updateRefundPayTotal();
    }
    
    function updateRefundPayTotal() {
        const total = refundPayDetails.reduce((sum, detail) => sum + detail.amount, 0);
        document.getElementById('refundPayTotalAmount').textContent = '¥' + total.toFixed(2);
    }
    
    function loadSourceOrder() {
        const orderNo = document.getElementById('refundSourceOrderNo').value.trim();
        if (!orderNo) {
            document.getElementById('refundOrderInfo').style.display = 'none';
            return;
        }
        
        fetch(`/psi/cashier/order/${orderNo}`, {
            headers: getRequestHeaders()
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderRefundOrderInfo(data.data);
            } else {
                alert('订单不存在');
            }
        })
        .catch(error => {
            console.error('查询订单失败:', error);
            alert('查询订单失败');
        });
    }
    
    function renderRefundOrderInfo(order) {
        const totalAmount = parseFloat(order.totalAmount) || 0;
        const refundedAmount = parseFloat(order.refundedAmount) || 0;
        const availableAmount = parseFloat(order.availableAmount) || 0;
        
        document.getElementById('refundSourceTotal').textContent = '¥' + totalAmount.toFixed(2);
        document.getElementById('refundAlready').textContent = '¥' + refundedAmount.toFixed(2);
        document.getElementById('refundAvailable').textContent = '¥' + availableAmount.toFixed(2);
        
        // 保存可退金额到隐藏字段
        document.getElementById('refundAvailableAmount').value = availableAmount;
        
        const items = order.items || [];
        const tbody = document.getElementById('refundItems');
        let html = '';
        items.forEach((item, index) => {
            const qtyValue = item.saleQuantity;
            const totalQty = typeof qtyValue === 'number' ? qtyValue : (qtyValue && qtyValue.value) ? qtyValue.value : (item.quantity || 0);
            
            // 获取已退货数量和可退数量
            const refundedQtyValue = item.refundedQuantity;
            const refundedQty = typeof refundedQtyValue === 'number' ? refundedQtyValue : (refundedQtyValue && refundedQtyValue.value) ? refundedQtyValue.value : 0;
            
            const availableQtyValue = item.availableQuantity;
            const availableQty = typeof availableQtyValue === 'number' ? availableQtyValue : (availableQtyValue && availableQtyValue.value) ? availableQtyValue.value : totalQty;
            
            const name = item.productName || item.goodsName || item.name || item.product_name || item.goods_name || '未知商品';
            const unitPriceValue = item.unitPrice;
            const unitPrice = typeof unitPriceValue === 'number' ? unitPriceValue : (unitPriceValue && unitPriceValue.value) ? unitPriceValue.value : (item.price || 0);
            
            // 默认选中可退数量
            const defaultQty = availableQty > 0 ? availableQty : 0;
            
            html += `
                <tr>
                    <td>${name}</td>
                    <td>¥${unitPrice.toFixed(2)}</td>
                    <td>${totalQty}</td>
                    <td>${refundedQty}</td>
                    <td>
                        <input type="number" class="qty-input" min="0" max="${totalQty}" value="${defaultQty}" 
                               data-total-qty="${totalQty}" data-available-qty="${availableQty}" data-price="${unitPrice}"
                               onchange="CashierModule.updateRefundQty(${index}, this)"
                               onblur="CashierModule.validateRefundQty(this)">
                    </td>
                    <td class="refund-item-amount">¥${(defaultQty * unitPrice).toFixed(2)}</td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
        document.getElementById('refundOrderInfo').style.display = 'block';
        updateRefundTotal();
    }
    
    function validateRefundQty(input) {
        const totalQty = parseFloat(input.dataset.totalQty);
        const availableQty = parseFloat(input.dataset.availableQty);
        const price = parseFloat(input.dataset.price);
        const value = parseFloat(input.value);
        
        if (isNaN(value) || value < 0) {
            input.value = 0;
            input.classList.add('error');
            setTimeout(() => input.classList.remove('error'), 2000);
        } else if (value > totalQty) {
            input.value = availableQty > 0 ? availableQty : 0;
            input.classList.add('error');
            alert(`退货数量不能大于购买数量。购买数量：${totalQty}`);
            setTimeout(() => input.classList.remove('error'), 2000);
        } else if (value > availableQty) {
            input.value = availableQty;
            input.classList.add('error');
            alert(`退货数量不能超过可退数量。可退数量：${availableQty}`);
            setTimeout(() => input.classList.remove('error'), 2000);
        } else {
            input.classList.remove('error');
        }
        updateRefundQtyFromInput(input);
        updateRefundTotal();
    }
    
    function updateRefundQty(index, input) {
        const price = parseFloat(input.dataset.price);
        const qty = parseFloat(input.value) || 0;
        const rows = document.querySelectorAll('#refundItems tr');
        const amountCell = rows[index]?.querySelector('.refund-item-amount');
        if (amountCell) {
            amountCell.textContent = '¥' + (qty * price).toFixed(2);
        }
        updateRefundTotal();
    }
    
    function updateRefundQtyFromInput(input) {
        const price = parseFloat(input.dataset.price);
        const qty = parseFloat(input.value) || 0;
        const amountCell = input.closest('tr')?.querySelector('.refund-item-amount');
        if (amountCell) {
            amountCell.textContent = '¥' + (qty * price).toFixed(2);
        }
    }
    
    function updateRefundTotal() {
        let total = 0;
        document.querySelectorAll('.refund-item-amount').forEach(cell => {
            const amount = parseFloat(cell.textContent.replace('¥', ''));
            if (!isNaN(amount)) total += amount;
        });
        document.getElementById('refundTotalAmount').textContent = '¥' + total.toFixed(2);
    }
    
    function submitRefund() {
        const orderNo = document.getElementById('refundSourceOrderNo').value.trim();
        const reason = document.getElementById('refundReason').value.trim();
        
        if (!orderNo) {
            alert('请输入原订单号');
            return;
        }
        
        // 校验是否已整单退货（可退金额 <= 0 表示已整单退货）
        const availableAmount = parseFloat(document.getElementById('refundAvailableAmount').value) || 0;
        if (availableAmount <= 0) {
            alert('该订单已整单退货，无法再次退货');
            return;
        }
        
        const refundItems = [];
        document.querySelectorAll('#refundItems tr').forEach((row, index) => {
            const qtyInput = row.querySelector('input');
            const qty = parseInt(qtyInput.value) || 0;
            if (qty > 0) {
                refundItems.push({
                    index: index,
                    quantity: qty
                });
            }
        });
        
        if (refundItems.length === 0) {
            alert('请选择退货商品');
            return;
        }
        
        const refundTotal = parseFloat(document.getElementById('refundTotalAmount').textContent.replace('¥', '')) || 0;
        const payTotal = refundPayDetails.reduce((sum, detail) => sum + detail.amount, 0);
        
        // 校验退货金额是否超出可退金额
        if (refundTotal > availableAmount) {
            alert(`退货金额超出可退金额。可退金额：¥${availableAmount.toFixed(2)}，退货金额：¥${refundTotal.toFixed(2)}`);
            return;
        }
        
        if (refundPayDetails.length === 0) {
            alert('请添加退款方式和金额');
            return;
        }
        
        if (payTotal !== refundTotal) {
            alert(`退款金额与退货金额不一致，请检查。退货金额：¥${refundTotal.toFixed(2)}，退款金额：¥${payTotal.toFixed(2)}`);
            return;
        }
        
        const refundData = {
            sourceOrderNo: orderNo,
            refundReason: reason,
            items: refundItems,
            payDetails: refundPayDetails.map(detail => ({
                payType: parseInt(detail.payMethod),
                refundAmount: detail.amount
            }))
        };
        
        fetch('/psi/cashier/refund', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getRequestHeaders()
            },
            body: JSON.stringify(refundData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                alert('退货成功');
                document.getElementById('refundModal').classList.remove('show');
                refundPayDetails.length = 0;
            } else {
                alert(data.message || '退货失败');
            }
        })
        .catch(error => {
            console.error('退货失败:', error);
            alert('退货失败');
        });
    }
    
    function createRefundFromDetail() {
        const orderNo = document.getElementById('detailOrderNo').textContent;
        document.getElementById('orderDetailModal').classList.remove('show');
        showRefund();
        document.getElementById('refundSourceOrderNo').value = orderNo;
        loadSourceOrder();
    }

    function uploadData() {
        const btn = document.getElementById('btnUploadData');
        if (btn) {
            btn.disabled = true;
            btn.textContent = '上传中...';
        }
        
        fetch('/psi/cashier/upload', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getRequestHeaders()
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                alert('数据上传任务已触发，正在后台同步...');
            } else {
                alert(data.message || '上传失败');
            }
        })
        .catch(error => {
            console.error('上传数据失败:', error);
            alert('上传数据失败，请重试');
        })
        .finally(() => {
            if (btn) {
                btn.disabled = false;
                btn.textContent = I18n.t('button.upload', '上传数据');
            }
        });
    }

    function downloadData() {
        alert('下载数据功能开发中...');
    }

    function cashierShift() {
        window.location.href = 'shift.html';
    }

    return {
        init,
        changeQty,
        updateQty,
        validateQty,
        updatePrice,
        validatePrice,
        updateAmount,
        validateAmount,
        removeItem,
        removePayDetail,
        resumeOrder,
        deleteSuspendedOrder,
        loadTradeList,
        viewOrderDetail,
        updateRefundQty,
        validateRefundQty,
        removeRefundPayDetail,
        closeMemberRegisterModal,
        clearMemberRegisterForm,
        submitMemberRegister,
        cashierShift,
        selectBatch,
        confirmBatchSelection,
        switchCurrency,
        syncPendingOrders
    };
})();

document.addEventListener('DOMContentLoaded', CashierModule.init);