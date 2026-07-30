package com.trademaster.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trademaster.entity.Customer;
import com.trademaster.mapper.CustomerMapper;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    public Customer findByPhone(String phone) {
        return customerMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Customer>().eq("phone", phone));
    }

    public Customer findById(Long id) {
        return customerMapper.selectById(id);
    }

    public IPage<Customer> findPage(int page, int size, String keyword) {
        Page<Customer> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Customer> query = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            query.like("customer_name", keyword).or().like("phone", keyword);
        }
        query.orderByDesc("created_at");
        return customerMapper.selectPage(pageParam, query);
    }

    public void save(Customer customer) {
        if (customer.getId() == null) {
            customerMapper.insert(customer);
        } else {
            customerMapper.updateById(customer);
        }
    }

    public void updateBalance(Long customerId, java.math.BigDecimal amount) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer != null) {
            customer.setBalance(customer.getBalance().add(amount));
            customerMapper.updateById(customer);
        }
    }

    public void updatePoints(Long customerId, Integer points) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer != null) {
            customer.setPoints(customer.getPoints() + points);
            customerMapper.updateById(customer);
        }
    }
}
