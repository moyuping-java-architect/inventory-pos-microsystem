import request from '../../utils/request'

/**
 * 客户旅程 API
 *
 * 后端接口路径: /psi/customer/journey
 * 后端配置接口路径: /psi/customer/journey/config
 */
export const customerJourneyApi = {
  // ===== 业务 API =====

  /** 老板第一屏看板 */
  getDashboard: () => request.get('/psi/customer/journey/dashboard'),

  /** 流失预警列表 */
  getChurnAlerts: () => request.get('/psi/customer/journey/churn-alerts'),

  /** 客户旅程时间线 */
  getCustomerJourney: customerId =>
    request.get(`/psi/customer/journey/${customerId}/timeline`),

  /** Top N 客户 */
  getTopCustomers: (limit = 10) =>
    request.get('/psi/customer/journey/top-customers', { params: { limit } }),

  /** 召回建议 */
  getWinBackSuggestions: () =>
    request.get('/psi/customer/journey/winback-suggestions'),

  /** 录入触点 */
  recordTouchpoint: data =>
    request.post('/psi/customer/journey/touchpoint', data),

  /** 风险收入 */
  getRevenueAtRisk: () => request.get('/psi/customer/journey/revenue-at-risk'),

  /**
   * 客户旅程分组 - 看板"客户旅程"面板：按阶段列出每个阶段的客户
   * @param {number} perStageLimit 每阶段最多返回几个客户
   * @param {string} [journeyCode] 旅程编码，不传取默认旅程
   */
  getStages: (perStageLimit = 10, journeyCode) =>
    request.get('/psi/customer/journey/stages', {
      params: journeyCode ? { perStageLimit, journeyCode } : { perStageLimit }
    }),

  // ===== 配置 API =====

  /** 获取全部配置（按 group 分组） */
  getConfig: () => request.get('/psi/customer/journey/config'),

  /** 获取单组配置 */
  getConfigByGroup: group =>
    request.get(`/psi/customer/journey/config/${group}`),

  /** 获取触点类型下拉选项 */
  getTouchpointTypes: () =>
    request.get('/psi/customer/journey/config/touchpoint-types'),

  /** 获取启用的旅程模板列表（配置页模板选择器） */
  listJourneyTemplates: () =>
    request.get('/psi/customer/journey/config/journey-templates'),

  /** 新增旅程模板（基础元数据） */
  createJourneyTemplate: data =>
    request.post('/psi/customer/journey/config/journey-templates', data),

  /** 更新旅程模板（编码不可改） */
  updateJourneyTemplate: (id, data) =>
    request.put(`/psi/customer/journey/config/journey-templates/${id}`, data),

  /** 删除旅程模板（有阶段时拒绝） */
  deleteJourneyTemplate: id =>
    request.delete(`/psi/customer/journey/config/journey-templates/${id}`),

  /** 获取指定模板的阶段配置项（JSON 兼容旧接口） */
  getJourneyStages: templateCode =>
    request.get('/psi/customer/journey/config/journey-stages', { params: { templateCode } }),

  /** 保存指定模板的阶段配置（JSON 兼容旧接口，upsert） */
  saveJourneyStages: (templateCode, items) =>
    request.put('/psi/customer/journey/config/journey-stages', items, { params: { templateCode } }),

  /** 获取指定模板的全部阶段（v2：直接返回行，不是 JSON） */
  listJourneyStagesEdit: templateCode =>
    request.get('/psi/customer/journey/config/journey-stages-edit', { params: { templateCode } }),

  /** 整批保存指定模板的阶段（v2：差异 upsert，缺失则软删） */
  saveJourneyStagesEdit: (templateCode, stages) =>
    request.put('/psi/customer/journey/config/journey-stages-edit', stages, { params: { templateCode } }),

  /** 批量保存配置 */
  saveConfig: dataList =>
    request.put('/psi/customer/journey/config', dataList),

  /** 一键重置默认 */
  resetConfig: () => request.post('/psi/customer/journey/config/reset'),

  // ===== 引擎零代码配置：业务事件 → 旅程阶段 =====

  /** 配置页下拉选项（事件/触点/意图/渠道/字段/运算符） */
  engineOptions: () =>
    request.get('/psi/customer/journey/engine/options'),

  /** 业务事件 → 阶段 关联列表 */
  listEventStageMappings: () =>
    request.get('/psi/customer/journey/engine/event-stage/mappings'),

  /** 新增 业务事件 → 阶段 关联（零代码创建触点规则） */
  createEventStageMapping: data =>
    request.post('/psi/customer/journey/engine/event-stage/mappings', data),

  /** 删除 业务事件 → 阶段 关联 */
  deleteEventStageMapping: id =>
    request.delete(`/psi/customer/journey/engine/event-stage/mappings/${id}`),

  /** 引擎：旅程模板列表 */
  listEngineTemplates: () =>
    request.get('/psi/customer/journey/engine/templates'),

  /** 引擎：某旅程的阶段列表 */
  listEngineStages: journeyCode =>
    request.get('/psi/customer/journey/engine/stages', { params: { journeyCode } })
}

/**
 * 话术库 * 销冠 API（旅程触达计划版）
 * 后端接口路径: /psi/customer/sales-script
 */
export const salesScriptApi = {
  /** 话术库列表（按节点分组） */
  listScripts: stageCode =>
    request.get('/psi/customer/sales-script/list', { params: stageCode ? { stageCode } : {} }),

  /** 话术详情 */
  getScript: id => request.get(`/psi/customer/sales-script/${id}`),

  /** 新增话术 */
  saveScript: data => request.post('/psi/customer/sales-script/save', data),

  /** 更新话术 */
  updateScript: data => request.post('/psi/customer/sales-script/update', data),

  /** 删除话术 */
  deleteScript: id => request.post(`/psi/customer/sales-script/delete/${id}`),

  /** 今日应触达客户列表 */
  listMatchCustomers: limit =>
    request.get('/psi/customer/sales-script/match/customers', { params: { limit } }),

  /** 查询客户当前可触达话术 */
  matchScriptsForCustomer: customerId =>
    request.get(`/psi/customer/sales-script/match/${customerId}`),

  /** 发送话术（幂等去重） */
  sendScript: data => request.post('/psi/customer/sales-script/send', data),

  /** 客户发送记录 */
  listSendLogs: customerId =>
    request.get(`/psi/customer/sales-script/send-log/${customerId}`),

  /** 标签列表 */
  listTags: () => request.get('/psi/customer/sales-script/tags'),

  /** 保存客户标签 */
  saveCustomerTags: (customerId, tagCodes) =>
    request.post(`/psi/customer/sales-script/customer/${customerId}/tags`, tagCodes)
}
