package com.psi.system.service.impl;

import com.psi.system.dto.WarehouseInfoDTO;
import com.psi.system.dto.WarehouseInfoQueryDTO;
import com.psi.system.dto.WarehouseInfoSaveDTO;
import com.psi.system.entity.WarehouseInfo;
import com.psi.system.mapper.WarehouseInfoMapper;
import com.psi.system.service.WarehouseInfoService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class WarehouseInfoServiceImpl implements WarehouseInfoService {

    private final WarehouseInfoMapper warehouseInfoMapper;

    public WarehouseInfoServiceImpl(WarehouseInfoMapper warehouseInfoMapper) {
        this.warehouseInfoMapper = warehouseInfoMapper;
    }

    @Override
    public CommonResult<WarehouseInfoDTO> getById(Long id) {
        WarehouseInfo warehouse = warehouseInfoMapper.selectById(id);
        if (warehouse == null) {
            return CommonResult.fail("仓库不存在");
        }
        return CommonResult.success(BeanUtils.convert(warehouse, WarehouseInfoDTO.class));
    }

    @Override
    public PageResult<WarehouseInfoDTO> list(WarehouseInfoQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        LambdaQueryWrapper<WarehouseInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getWarehouseName())) {
            wrapper.like(WarehouseInfo::getWarehouseName, queryDTO.getWarehouseName());
        }
        if (StringUtils.hasText(queryDTO.getWarehouseCode())) {
            wrapper.like(WarehouseInfo::getWarehouseCode, queryDTO.getWarehouseCode());
        }
        if (queryDTO.getShopId() != null) {
            wrapper.eq(WarehouseInfo::getShopId, queryDTO.getShopId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(WarehouseInfo::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(WarehouseInfo::getCreateTime);

        IPage<WarehouseInfo> page = warehouseInfoMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.success(
            BeanUtils.convertList(page.getRecords(), WarehouseInfoDTO.class),
            page.getTotal(),
            pageNum,
            pageSize
        );
    }

    @Override
    public CommonResult<WarehouseInfoDTO> save(WarehouseInfoSaveDTO saveDTO) {
        WarehouseInfo warehouse = BeanUtils.convert(saveDTO, WarehouseInfo.class);
        warehouseInfoMapper.insert(warehouse);
        return CommonResult.success(BeanUtils.convert(warehouse, WarehouseInfoDTO.class));
    }

    @Override
    public CommonResult<WarehouseInfoDTO> update(Long id, WarehouseInfoSaveDTO saveDTO) {
        WarehouseInfo warehouse = warehouseInfoMapper.selectById(id);
        if (warehouse == null) {
            return CommonResult.fail("仓库不存在");
        }
        BeanUtils.copyProperties(saveDTO, warehouse);
        warehouseInfoMapper.updateById(warehouse);
        return CommonResult.success(BeanUtils.convert(warehouse, WarehouseInfoDTO.class));
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        WarehouseInfo warehouse = warehouseInfoMapper.selectById(id);
        if (warehouse == null) {
            return CommonResult.fail("仓库不存在");
        }
        warehouseInfoMapper.deleteById(id);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        WarehouseInfo warehouse = warehouseInfoMapper.selectById(id);
        if (warehouse == null) {
            return CommonResult.fail("仓库不存在");
        }
        warehouse.setStatus(status);
        warehouseInfoMapper.updateById(warehouse);
        return CommonResult.success();
    }
}