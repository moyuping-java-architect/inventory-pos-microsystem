/**
 * 收银系统登录模块
 */
const LoginModule = (function() {
    const BASE_URL = '/psi/cashier';
    
    /**
     * 显示错误消息
     */
    function showError(message) {
        const errorMsg = document.getElementById('errorMsg');
        errorMsg.textContent = message;
        errorMsg.classList.add('show');
        
        setTimeout(() => {
            errorMsg.classList.remove('show');
        }, 3000);
    }
    
    /**
     * 隐藏错误消息
     */
    function hideError() {
        document.getElementById('errorMsg').classList.remove('show');
    }
    
    /**
     * 设置按钮状态
     */
    function setButtonLoading(loading) {
        const btn = document.getElementById('btnLogin');
        const btnText = document.getElementById('btnText');
        
        if (loading) {
            btn.disabled = true;
            btnText.innerHTML = '<span class="loading"></span>' + (window.I18n ? I18n.t('button.submit', '登录中...') : '登录中...');
        } else {
            btn.disabled = false;
            btnText.textContent = I18n ? I18n.t('button.register', '登 录') : '登 录';
        }
    }
    
    /**
     * 发起登录请求
     */
    async function doLogin(username, password, shopCode) {
        try {
            const response = await fetch(`${BASE_URL}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username: username,
                    password: password,
                    shopCode: shopCode || null
                })
            });
            
            // 获取响应头
            const headers = {};
            response.headers.forEach((value, key) => {
                headers[key] = value;
            });
            
            const result = await response.json();
            // 将响应头附加到结果中
            result.headers = headers;
            
            return result;
        } catch (error) {
            console.error('登录请求失败:', error);
            return {
                success: false,
                message: '网络请求失败，请检查网络连接'
            };
        }
    }
    
    /**
     * 登录成功处理
     */
    function handleLoginSuccess(data) {
        localStorage.setItem('cashier_token', data.token);
        localStorage.setItem('cashier_user', JSON.stringify(data.user));
        
        // 从响应头获取信息（优先使用响应头）
        if (data.headers) {
            const tenantId = data.headers['x-tenant-id'] || data.headers['X-Tenant-Id'];
            const shopId = data.headers['x-shop-id'] || data.headers['X-Shop-Id'];
            const userId = data.headers['x-update-user-id'] || data.headers['X-Update-User-Id'];
            const userName = data.headers['x-update-user-name'] || data.headers['X-Update-User-Name'];
            
            if (tenantId) {
                localStorage.setItem('cashier_tenant_id', tenantId);
            }
            if (shopId) {
                localStorage.setItem('cashier_shop_code', shopId);
            }
            if (userId) {
                localStorage.setItem('cashier_user_id', userId);
            }
            if (userName) {
                localStorage.setItem('cashier_user_name', userName);
            }
        }
        
        // 备用：从响应体获取信息
        if (data.user && data.user.tenantId && !localStorage.getItem('cashier_tenant_id')) {
            localStorage.setItem('cashier_tenant_id', data.user.tenantId);
        }
        if (data.user && data.user.shopCode && !localStorage.getItem('cashier_shop_code')) {
            localStorage.setItem('cashier_shop_code', data.user.shopCode);
        }
        
        window.location.href = 'cashier.html';
    }
    
    /**
     * 初始化语言切换
     */
    function initLangSwitch() {
        const langButtons = document.querySelectorAll('.lang-switch button');
        
        langButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                const lang = btn.getAttribute('data-lang');
                
                langButtons.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                
                if (window.I18n) {
                    I18n.switchLang(lang);
                    updatePlaceholders();
                }
            });
        });
    }
    
    /**
     * 更新输入框占位符
     */
    function updatePlaceholders() {
        document.querySelectorAll('[data-i18n-placeholder]').forEach(element => {
            const key = element.getAttribute('data-i18n-placeholder');
            const text = window.I18n ? I18n.t(key, key) : key;
            element.placeholder = text;
        });
    }
    
    /**
     * 初始化登录表单
     */
    function init() {
        if (window.I18n) {
            I18n.init();
            updatePlaceholders();
        }
        
        initLangSwitch();
        
        const form = document.getElementById('loginForm');
        
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            hideError();
            setButtonLoading(true);
            
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;
            const shopCode = document.getElementById('shopCode').value.trim();
            
            const t = window.I18n ? I18n.t.bind(I18n) : (key, def) => def || key;
            
            if (!username) {
                showError(t('hint.enterName', '请输入用户名'));
                setButtonLoading(false);
                return;
            }
            
            if (!password) {
                showError(t('hint.enterAmount', '请输入密码'));
                setButtonLoading(false);
                return;
            }
            
            console.log('发送登录请求:', {username, shopCode});
            const result = await doLogin(username, password, shopCode);
            console.log('登录响应:', result);
            
            if (result.code === 200 && result.data) {
                console.log('登录成功，准备跳转');
                handleLoginSuccess(result.data);
            } else {
                console.log('登录失败:', result.message);
                showError(result.message || t('hint.emptyData', '登录失败'));
                setButtonLoading(false);
            }
        });
        
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                const activeElement = document.activeElement;
                if (activeElement.tagName === 'INPUT') {
                    document.getElementById('btnLogin').click();
                }
            }
        });
    }
    
    return {
        init: init
    };
})();

document.addEventListener('DOMContentLoaded', () => {
    LoginModule.init();
});