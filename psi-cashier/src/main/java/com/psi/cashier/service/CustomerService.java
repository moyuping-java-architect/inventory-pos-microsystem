package com.psi.cashier.service;

import com.psi.cashier.entity.CustomerEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 客户服务接口
 */
public interface CustomerService extends IService<CustomerEntity> {

    /**
     * 根据ID查询客户
     */
    CustomerEntity getById(Integer id);

    /**
     * 根据关键词搜索客户（名称/手机号/编码）
     */
    List<CustomerEntity> search(String keyword);

    /**
     * 保存或更新客户（用于下行同步）
     */
    void saveOrUpdateEntity(CustomerEntity entity);

    /**
     * 根据dataUuid查询客户
     */
    CustomerEntity getByDataUuid(String dataUuid);

    /**
     * 批量更新客户数据（全量替换）
     */
    void replaceAll(List<CustomerEntity> customerList);

    /**
     * 客户转会员：根据客户信息创建会员
     */
    Integer convertToMember(Integer customerId);
}