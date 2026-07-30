package com.psi.sale.service;

import com.psi.common.feign.DocFeignResponse;
import com.psi.sale.entity.SaleOutSelfUseMainEntity;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SaleOutSelfUseMainService extends IService<SaleOutSelfUseMainEntity> {

    /**
     * 根据审批通过的单据数据生成自用出库正式数据
     *
     * @param doc 工作流单据快照
     * @return 生成的主表记录
     */
    SaleOutSelfUseMainEntity saveFromDraft(DocFeignResponse doc);

    /**
     * 查询自用出库单详情（含明细）
     *
     * @param id 主表ID
     * @return 自用出库单主表记录及明细
     */
    SaleOutSelfUseMainEntity getDetail(Long id);
}
