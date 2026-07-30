const I18n = (function() {
    let currentLang = 'zh_CN';
    let messages = {};

    const langResources = {
        'zh_CN': window.messages_zh_CN || {},
        'en': window.messages_en || {},
        'en_US': window.messages_en || {}
    };

    function loadMessages(lang) {
        messages = langResources[lang] || langResources['zh_CN'];
        currentLang = lang;
        localStorage.setItem('psi_lang', lang);
        document.querySelector('html').lang = lang === 'zh_CN' ? 'zh-CN' : 'en';
        updatePageText();
    }

    function t(key, defaultValue = '') {
        const keys = key.split('.');
        let result = messages;
        for (const k of keys) {
            if (result && typeof result === 'object' && k in result) {
                result = result[k];
            } else {
                return defaultValue || key;
            }
        }
        return result;
    }

    function updatePageText() {
        document.querySelectorAll('[data-i18n]').forEach(element => {
            const key = element.getAttribute('data-i18n');
            const text = t(key, key);
            if (element.tagName === 'INPUT' || element.tagName === 'TEXTAREA') {
                element.placeholder = text;
            } else if (element.tagName === 'OPTION') {
                element.textContent = text;
            } else {
                element.textContent = text;
            }
        });
        
        // 更新 placeholder 属性
        document.querySelectorAll('[data-i18n-placeholder]').forEach(element => {
            const key = element.getAttribute('data-i18n-placeholder');
            element.placeholder = t(key, key);
        });
    }

    function init() {
        return new Promise((resolve) => {
            const savedLang = localStorage.getItem('psi_lang') || 'zh_CN';
            loadMessages(savedLang);
            resolve();
        });
    }

    function switchLang(lang) {
        return new Promise((resolve) => {
            loadMessages(lang);
            resolve();
        });
    }

    function getCurrentLang() {
        return currentLang;
    }

    return {
        init,
        t,
        switchLang,
        getCurrentLang,
        updatePageText
    };
})();