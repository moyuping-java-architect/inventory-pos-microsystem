package com.psi.order.service;

import com.psi.order.dto.CreateDocRequest;
import com.psi.order.dto.DocResponse;
import com.psi.order.entity.DocEntity;
import com.psi.order.entity.DocItemEntity;
import com.psi.order.constant.DocTypeConstant.DocType;
import com.psi.order.constant.DocTypeConstant.DocStatus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 单据服务接口
 */
public interface DocService {

    /**
     * 创建单据
     */
    DocResponse create(CreateDocRequest request);

    /**
     * 根据ID查询单据
     */
    DocResponse findById(Long id);

    /**
     * 根据单据编号查询单据
     */
    DocResponse findByDocNo(String docNo);

    /**
     * 分页查询单据
     */
    IPage<DocResponse> findPage(Page<DocResponse> page, String docType, Integer status, String creatorId);

    /**
     * 按状态列表分页查询单据
     */
    IPage<DocResponse> findPageByStatusList(Page<DocResponse> page, String docType, List<Integer> statusList, String creatorId);

    /**
     * 根据单据类型查询
     */
    List<DocResponse> findByDocType(String docType);

    /**
     * 根据状态查询
     */
    List<DocResponse> findByStatus(Integer status);

    /**
     * 查询待审批单据
     */
    List<DocResponse> findPendingApprove();

    /**
     * 更新单据
     */
    DocResponse update(Long id, CreateDocRequest request);

    /**
     * 删除单据
     */
    void delete(Long id);

    /**
     * 提交单据
     */
    DocResponse submit(Long id);

    /**
     * 审批通过
     */
    DocResponse approve(Long id, String approverId, String approverName);

    /**
     * 审批驳回
     */
    DocResponse reject(Long id, String approverId, String remark);

    /**
     * 执行单据
     */
    DocResponse execute(Long id);

    /**
     * 完成单据
     */
    DocResponse complete(Long id);

    /**
     * 取消单据
     */
    DocResponse cancel(Long id, String remark);

    /**
     * 获取单据类型列表
     */
    List<DocType> getAllDocTypes();

    /**
     * 获取单据状态列表
     */
    List<DocStatus> getAllDocStatus();
}