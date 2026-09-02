import request from '../../utils/request'

/**
 * 工作流 API
 */
export const workflowApi = {
  // 发起流程
  startProcess: data => request.post('/psi/workflow/start', data),
  // 查询待办任务
  getTodoList: userId => request.get(`/psi/workflow/todo?userId=${userId}`),
  // 审批任务
  approveTask: (taskId, data) => request.post(`/psi/workflow/task/${taskId}/approve`, data),
  // 驳回任务
  rejectTask: (taskId, data) => request.post(`/psi/workflow/task/${taskId}/reject`, data),
  // 查询流程实例
  getInstances: params => request.get('/psi/workflow/instance', { params }),
  // 查询流程实例详情
  getInstanceDetail: id => request.get(`/psi/workflow/instance/${id}/detail`),
  // 查询流程历史
  getHistory: params => request.get('/psi/workflow/history', { params }),
  // 查询流程定义
  getDefinitions: params => request.get('/psi/workflow/definition', { params }),
  // 查询流程定义详情（含节点、条件、关系）
  getDefinitionDetail: id => request.get(`/psi/workflow/definition/${id}/detail`),
  // 保存流程定义
  saveDefinition: data => request.post('/psi/workflow/definition', data),
  // 更新流程定义
  updateDefinition: (id, data) => request.put(`/psi/workflow/definition/${id}`, data),
  // 删除流程定义
  deleteDefinition: id => request.delete(`/psi/workflow/definition/${id}`),
  // 保存流程节点
  saveNode: data => request.post('/psi/workflow/node', data),
  // 更新流程节点
  updateNode: (id, data) => request.put(`/psi/workflow/node/${id}`, data),
  // 删除流程节点
  deleteNode: id => request.delete(`/psi/workflow/node/${id}`),
  // 保存节点关系
  saveRelation: data => request.post('/psi/workflow/relation', data),
  // 更新节点关系
  updateRelation: (id, data) => request.put(`/psi/workflow/relation/${id}`, data),
  // 删除节点关系
  deleteRelation: id => request.delete(`/psi/workflow/relation/${id}`),
  // 查询流程操作日志
  getOperationLogs: instanceId => request.get(`/psi/workflow/instance/${instanceId}/logs`),
  // 查询流程实例审批详情（含流程图、节点、任务、日志）
  getApprovalDetail: instanceId => request.get(`/psi/workflow/instance/${instanceId}/approval-detail`)
}
