package com.psi.cashier.service.impl;

import com.psi.cashier.entity.CustomerEntity;
import com.psi.cashier.entity.MemberEntity;
import com.psi.cashier.mapper.CustomerMapper;
import com.psi.cashier.mapper.MemberMapper;
import com.psi.cashier.service.CustomerService;
import com.psi.common.context.UserContext;
import com.psi.common.util.IdUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 客户服务实现
 */
@Slf4j
@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, CustomerEntity> implements CustomerService {

    private final MemberMapper memberMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CustomerServiceImpl(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    public CustomerEntity getById(Integer id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<CustomerEntity> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return baseMapper.selectList(null);
        }
        return baseMapper.searchByKeyword(keyword.trim());
    }

    @Override
    public CustomerEntity getByDataUuid(String dataUuid) {
        return baseMapper.selectByDataUuid(dataUuid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateEntity(CustomerEntity entity) {
        if (entity.getDataUuid() == null) {
            entity.setDataUuid(IdUtils.snowflakeIdStr());
        }

        CustomerEntity exist = baseMapper.selectByDataUuid(entity.getDataUuid());
        String now = LocalDateTime.now().format(TIME_FORMATTER);

        if (exist != null) {
            entity.setId(exist.getId());
            entity.setUpdateTime(now);
            baseMapper.updateById(entity);
            log.debug("更新客户: id={}, customerCode={}", entity.getId(), entity.getCustomerCode());
        } else {
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            if (entity.getDelFlag() == null) entity.setDelFlag(0);
            if (entity.getStatus() == null) entity.setStatus(1);
            baseMapper.insert(entity);
            log.debug("新增客户: id={}, customerCode={}", entity.getId(), entity.getCustomerCode());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceAll(List<CustomerEntity> customerList) {
        // 清空旧数据（逻辑删除）
        List<CustomerEntity> all = baseMapper.selectList(null);
        String now = LocalDateTime.now().format(TIME_FORMATTER);
        for (CustomerEntity old : all) {
            old.setDelFlag(1);
            old.setUpdateTime(now);
            baseMapper.updateById(old);
        }

        // 插入新数据
        for (CustomerEntity entity : customerList) {
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            if (entity.getDelFlag() == null) entity.setDelFlag(0);
            if (entity.getStatus() == null) entity.setStatus(1);
            if (entity.getDataUuid() == null) entity.setDataUuid(IdUtils.snowflakeIdStr());
            baseMapper.insert(entity);
        }

        log.info("客户数据全量替换完成: count={}", customerList.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer convertToMember(Integer customerId) {
        CustomerEntity customer = baseMapper.selectById(customerId);
        if (customer == null) {
            throw new RuntimeException("客户不存在: id=" + customerId);
        }

        // 检查是否已存在同手机号的会员
        if (customer.getContactPhone() != null && !customer.getContactPhone().isEmpty()) {
            MemberEntity existMember = memberMapper.selectByPhone(null, customer.getContactPhone());
            if (existMember != null) {
                throw new RuntimeException("该手机号已注册会员: phone=" + customer.getContactPhone());
            }
        }

        // 创建会员
        MemberEntity member = new MemberEntity();
        member.setDataUuid(IdUtils.snowflakeIdStr());
        member.setTenantId(customer.getTenantId());
        member.setName(customer.getCustomerName());
        member.setPhone(customer.getContactPhone());
        member.setBalance(0.0);
        member.setPoint(0);
        member.setLevel(1);
        member.setStatus(1);
        member.setDelFlag(0);
        String now = LocalDateTime.now().format(TIME_FORMATTER);
        member.setCreateTime(now);
        member.setUpdateTime(now);

        memberMapper.insert(member);
        log.info("客户转化为会员成功: customerId={}, memberId={}", customerId, member.getMemberId());

        return member.getMemberId();
    }
}