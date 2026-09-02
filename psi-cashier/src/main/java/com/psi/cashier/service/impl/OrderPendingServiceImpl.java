package com.psi.cashier.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.psi.cashier.dto.PendingItemSaveDTO;
import com.psi.cashier.dto.PendingMainSaveDTO;
import com.psi.cashier.entity.OrderPendingEntity;
import com.psi.cashier.entity.OrderPendingItemEntity;
import com.psi.cashier.mapper.OrderPendingItemMapper;
import com.psi.cashier.mapper.OrderPendingMapper;
import com.psi.cashier.service.OrderPendingService;
import com.psi.cashier.util.OrderNoGenerator;
import com.psi.common.context.UserContext;
import com.psi.common.util.IdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.psi.common.result.PageResult;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPendingServiceImpl implements OrderPendingService {

    private final OrderPendingMapper orderPendingMapper;
    private final OrderPendingItemMapper orderPendingItemMapper;
    
    private final OrderNoGenerator orderNoGenerator;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderPendingEntity savePending(PendingMainSaveDTO dto) {
        String pendingNo = dto.getPendingNo() != null && !dto.getPendingNo().isEmpty() 
                ? dto.getPendingNo() : orderNoGenerator.generatePendingOrder(orderNoGenerator.getDefaultPosCode());
        
        OrderPendingEntity entity = new OrderPendingEntity();
        entity.setDataUuid(IdUtils.snowflakeIdStr());
        entity.setPendingNo(pendingNo);
        entity.setTenantId(UserContext.getTenantId());
        entity.setShopCode(UserContext.get() != null ? UserContext.get().getShopId() : null);
        entity.setPosId(dto.getPosId());
        entity.setBizType(4);
        entity.setOperatorId(dto.getOperatorId());
        entity.setPendingName(dto.getPendingName() != null ? dto.getPendingName() : "挂单");
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setCreateBy(String.valueOf(dto.getOperatorId()));
        entity.setCreateTime(LocalDateTime.now().format(TIME_FORMATTER));
        entity.setUpdateBy(String.valueOf(dto.getOperatorId()));
        entity.setUpdateTime(LocalDateTime.now().format(TIME_FORMATTER));
        
        orderPendingMapper.insert(entity);
        
        if (dto.getItems() != null) {
            for (PendingItemSaveDTO item : dto.getItems()) {
                OrderPendingItemEntity itemEntity = new OrderPendingItemEntity();
                itemEntity.setDataUuid(IdUtils.snowflakeIdStr());
                itemEntity.setTenantId(UserContext.getTenantId());
                itemEntity.setShopCode(UserContext.get() != null ? UserContext.get().getShopId() : null);
                itemEntity.setPosId(dto.getPosId());
                itemEntity.setPendingNo(pendingNo);
                itemEntity.setBizType(4);
                itemEntity.setSkuId(parseIntegerSafely(item.getSkuId()));
                itemEntity.setSkuCode(item.getSkuCode());
                itemEntity.setBarCode(item.getBarCode());
                itemEntity.setProductName(item.getProductName());
                itemEntity.setSaleUnitName(item.getSaleUnitName());
                itemEntity.setSaleQuantity(item.getSaleQuantity());
                itemEntity.setUnitPrice(item.getUnitPrice());
                itemEntity.setMemberPrice(item.getMemberPrice() != null ? item.getMemberPrice() : item.getUnitPrice());
                itemEntity.setSubtotal(item.getSubtotal());
                itemEntity.setCreateBy(String.valueOf(dto.getOperatorId()));
                itemEntity.setCreateTime(LocalDateTime.now().format(TIME_FORMATTER));
                
                orderPendingItemMapper.insert(itemEntity);
            }
        }
        
        log.info("挂单保存成功，挂单号：{}", pendingNo);
        return entity;
    }

    @Override
    public PendingMainSaveDTO getPending(Long id) {
        OrderPendingEntity entity = orderPendingMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        return convertToDTO(entity);
    }

    @Override
    public PendingMainSaveDTO getPendingByNo(String pendingNo) {
        OrderPendingEntity entity = orderPendingMapper.selectOne(
                Wrappers.lambdaQuery(OrderPendingEntity.class)
                        .eq(OrderPendingEntity::getPendingNo, pendingNo)
        );
        if (entity == null) {
            return null;
        }
        return convertToDTO(entity);
    }

    @Override
    public List<PendingMainSaveDTO> listByOperatorId(Integer operatorId) {
        String tenantId = UserContext.getTenantId();
        List<OrderPendingEntity> entities = orderPendingMapper.selectByOperatorIdAndTenantId(operatorId, tenantId);
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PendingMainSaveDTO> listByShopCode(String shopCode) {
        List<OrderPendingEntity> entities = orderPendingMapper.selectByShopCode(shopCode);
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean deletePending(Long id) {
        OrderPendingEntity entity = orderPendingMapper.selectById(id);
        if (entity == null) {
            return false;
        }
        
        orderPendingItemMapper.deleteByPendingNo(entity.getPendingNo());
        orderPendingMapper.deleteById(id);
        
        log.info("挂单删除成功，挂单号：{}", entity.getPendingNo());
        return true;
    }

    private PendingMainSaveDTO convertToDTO(OrderPendingEntity entity) {
        PendingMainSaveDTO dto = new PendingMainSaveDTO();
        dto.setPendingNo(entity.getPendingNo());
        dto.setTenantId(entity.getTenantId());
        dto.setShopCode(entity.getShopCode());
        dto.setPosId(entity.getPosId());
        dto.setOperatorId(entity.getOperatorId());
        dto.setPendingName(entity.getPendingName());
        dto.setTotalAmount(entity.getTotalAmount());
        
        List<OrderPendingItemEntity> items = orderPendingItemMapper.selectByPendingNo(entity.getPendingNo());
        dto.setItems(items.stream()
                .map(item -> {
                    PendingItemSaveDTO itemDTO = new PendingItemSaveDTO();
                    itemDTO.setSkuId(item.getSkuId() != null ? String.valueOf(item.getSkuId()) : null);
                    itemDTO.setSkuCode(item.getSkuCode());
                    itemDTO.setBarCode(item.getBarCode());
                    itemDTO.setProductName(item.getProductName());
                    itemDTO.setSaleUnitName(item.getSaleUnitName());
                    itemDTO.setSaleQuantity(item.getSaleQuantity());
                    itemDTO.setUnitPrice(item.getUnitPrice());
                    itemDTO.setMemberPrice(item.getMemberPrice());
                    itemDTO.setSubtotal(item.getSubtotal());
                    return itemDTO;
                })
                .collect(Collectors.toList()));
        
        return dto;
    }

    @Override
    public PageResult<OrderPendingEntity> queryPage(int pageNum, int pageSize, Integer operatorId) {
        List<OrderPendingEntity> list;
        if (operatorId != null) {
            list = orderPendingMapper.selectByOperatorId(operatorId);
        } else {
            list = orderPendingMapper.selectList(Wrappers.lambdaQuery());
        }
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, list.size());
        List<OrderPendingEntity> pageList = start < list.size() ? list.subList(start, end) : List.of();
        return new PageResult<>(200, "success", pageList, list.size(), pageNum, pageSize);
    }

    @Override
    public List<OrderPendingEntity> getAllPending() {
        return orderPendingMapper.selectList(Wrappers.lambdaQuery());
    }

    @Override
    public OrderPendingEntity getByPendingNo(String pendingNo) {
        return orderPendingMapper.selectOne(
                Wrappers.lambdaQuery(OrderPendingEntity.class)
                        .eq(OrderPendingEntity::getPendingNo, pendingNo)
        );
    }

    @Override
    public List<OrderPendingEntity> getByOperatorId(Integer operatorId) {
        return orderPendingMapper.selectByOperatorId(operatorId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderPendingEntity save(OrderPendingEntity entity) {
        if (entity.getId() == null) {
            entity.setPendingNo(orderNoGenerator.generatePendingOrder(orderNoGenerator.getDefaultPosCode()));
            entity.setCreateTime(LocalDateTime.now().format(TIME_FORMATTER));
            entity.setUpdateTime(LocalDateTime.now().format(TIME_FORMATTER));
            orderPendingMapper.insert(entity);
        } else {
            entity.setUpdateTime(LocalDateTime.now().format(TIME_FORMATTER));
            orderPendingMapper.updateById(entity);
        }
        return entity;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void update(OrderPendingEntity entity) {
        entity.setUpdateTime(LocalDateTime.now().format(TIME_FORMATTER));
        orderPendingMapper.updateById(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean deleteByPendingNo(String pendingNo) {
        OrderPendingEntity entity = getByPendingNo(pendingNo);
        if (entity == null) {
            return false;
        }
        orderPendingItemMapper.deleteByPendingNo(pendingNo);
        orderPendingMapper.deleteById(entity.getId());
        return true;
    }

    /**
     * 安全解析字符串为 Integer
     * 如果字符串为 null、空或无法解析为整数，则返回 null
     *
     * @param value 待解析的字符串
     * @return 解析后的 Integer，解析失败返回 null
     */
    private Integer parseIntegerSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("无法解析字符串为整数: {}", value);
            return null;
        }
    }
}