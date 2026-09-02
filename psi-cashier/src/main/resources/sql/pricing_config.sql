-- ============================================================
-- PSI 定价引擎配置表
-- 三层加价模型：采购汇率 + 成本加价 + 销售汇率
-- ============================================================

CREATE TABLE IF NOT EXISTS pricing_config (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id   VARCHAR(32)  NOT NULL COMMENT '租户ID',
    base_currency  VARCHAR(3)  DEFAULT 'USD'  COMMENT '基准币种（定价锚定币种）',
    local_currency  VARCHAR(3)  DEFAULT 'ZMW' COMMENT '本地币种（客户看到的标价币种）',
    markup_factor   DECIMAL(10,4) DEFAULT 1.2600  COMMENT '加价系数（1.26 = 26%成本加价）',
    purchase_rate   DECIMAL(10,4) DEFAULT 26.0000 COMMENT '采购汇率（ZMW÷此值=USD成本，低于市场汇率）',
    sales_rate      DECIMAL(10,4) DEFAULT 28.0000 COMMENT '销售汇率（USD×此值=ZMW零售价，高于市场汇率）',
    market_rate     DECIMAL(10,4) DEFAULT 27.0000 COMMENT '市场实际汇率（用于成本核算和利润分析）',
    auto_update_retail TINYINT DEFAULT 1           COMMENT '是否自动更新零售价（0:否 1:是）',
    create_time  VARCHAR(20),
    update_time  VARCHAR(20),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定价引擎配置';

-- ============================================================
-- 示例：默认配置（可根据实际情况修改）
-- ============================================================
-- 市场汇率 27.0
-- 采购汇率 26.0（低于市场 → USD成本算高 → 利润空间大）
-- 销售汇率 28.0（高于市场 → ZMW售价算高 → 多收钱）
-- 加价系数 1.26（26%成本加价）
--
-- 完整链路示例：
--   ZMW采购 2,700
--   → 第1层：2700 ÷ 26.0 = USD 103.85（USD成本）
--   → 第2层：103.85 × 1.26 = USD 130.85（USD售价）
--   → 第3层：130.85 × 28.0 = ZMW 3,663.80（客户看到的零售价）
--
--   按市场汇率真实成本 = 2700 ZMW
--   毛利 = 3663.80 - 2700 = 963.80 ZMW
--   毛利率 = 963.80 / 3663.80 = 26.3%
--   其中汇率利润 = (28.0 - 27.0) × 130.85 = 130.85 ZMW
-- ============================================================
