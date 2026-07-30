package com.psi.cashier.service.impl;

import com.psi.cashier.entity.OrderMainEntity;
import com.psi.cashier.mapper.OrderMainMapper;
import com.psi.cashier.service.OrderMainService;
import com.psi.common.context.UserContext;
import com.psi.common.result.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrderMainServiceImpl extends ServiceImpl<OrderMainMapper, OrderMainEntity> implements OrderMainService {

    @Override
    public OrderMainEntity getByOrderNo(String orderNo) {
        return baseMapper.selectByOrderNo(orderNo);
    }

    @Override
    public PageResult<OrderMainEntity> queryPage(int pageNum, int pageSize, Integer payStatus) {
        QueryWrapper<OrderMainEntity> wrapper = new QueryWrapper<>();
        
        // 添加租户条件
        String tenantId = UserContext.getTenantId();
        if (tenantId != null && !tenantId.isEmpty()) {
            wrapper.eq("tenant_id", tenantId);
        }
        
        if (payStatus != null) {
            wrapper.eq("pay_status", payStatus);
        }
        
        wrapper.orderByDesc("create_time");
        
        Page<OrderMainEntity> page = new Page<>(pageNum, pageSize);
        baseMapper.selectPage(page, wrapper);
        
        return PageResult.success(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public PageResult<OrderMainEntity> queryOrders(int pageNum, int pageSize, String orderNo, String date) {
        // 获取租户ID
        String tenantId = UserContext.getTenantId();
        
        // 用于 count 查询的 wrapper
        QueryWrapper<OrderMainEntity> countWrapper = new QueryWrapper<>();
        // 添加租户条件
        if (tenantId != null && !tenantId.isEmpty()) {
            countWrapper.eq("tenant_id", tenantId);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            countWrapper.like("order_no", orderNo);
        }
        if (date != null && !date.isEmpty()) {
            countWrapper.apply("DATE(create_time) = {0}", date);
        }
        
        // 先查询总数
        long total = this.count(countWrapper);
        
        // 用于数据查询的 wrapper
        QueryWrapper<OrderMainEntity> listWrapper = new QueryWrapper<>();
        // 添加租户条件
        if (tenantId != null && !tenantId.isEmpty()) {
            listWrapper.eq("tenant_id", tenantId);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            listWrapper.like("order_no", orderNo);
        }
        if (date != null && !date.isEmpty()) {
            listWrapper.apply("DATE(create_time) = {0}", date);
        }
        listWrapper.orderByDesc("create_time");
        
        // 计算 offset
        int offset = (pageNum - 1) * pageSize;
        
        // 手动添加 SQLite 分页条件
        listWrapper.last("LIMIT " + pageSize + " OFFSET " + offset);
        
        // 查询数据
        java.util.List<OrderMainEntity> records = this.list(listWrapper);
        
        return PageResult.success(records, total, pageNum, pageSize);
    }
}