package com.psi.system.service.impl;

import com.psi.system.dto.ShopInfoDTO;
import com.psi.system.dto.ShopInfoQueryDTO;
import com.psi.system.dto.ShopInfoSaveDTO;
import com.psi.system.entity.ShopInfo;
import com.psi.system.entity.SysDept;
import com.psi.system.mapper.ShopInfoMapper;
import com.psi.system.mapper.SysDeptMapper;
import com.psi.system.service.ShopInfoService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShopInfoServiceImpl implements ShopInfoService {

    private final ShopInfoMapper shopInfoMapper;
    private final SysDeptMapper sysDeptMapper;

    public ShopInfoServiceImpl(ShopInfoMapper shopInfoMapper, SysDeptMapper sysDeptMapper) {
        this.shopInfoMapper = shopInfoMapper;
        this.sysDeptMapper = sysDeptMapper;
    }

    @Override
    public CommonResult<ShopInfoDTO> getById(Long id) {
        ShopInfo shop = shopInfoMapper.selectById(id);
        if (shop == null) {
            return CommonResult.fail("商铺不存在");
        }
        ShopInfoDTO dto = BeanUtils.convert(shop, ShopInfoDTO.class);
        if (dto == null) {
            dto = new ShopInfoDTO();
        }
        return CommonResult.success(dto);
    }

    @Override
    public PageResult<ShopInfoDTO> list(ShopInfoQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        LambdaQueryWrapper<ShopInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getShopName())) {
            wrapper.like(ShopInfo::getShopName, queryDTO.getShopName());
        }
        if (StringUtils.hasText(queryDTO.getShopCode())) {
            wrapper.like(ShopInfo::getShopCode, queryDTO.getShopCode());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(ShopInfo::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(ShopInfo::getCreateTime);

        IPage<ShopInfo> page = shopInfoMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<ShopInfo> shops = page.getRecords();

        Map<Long, String> shopDeptNamesMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(shops)) {
            List<Long> shopIds = shops.stream()
                .filter(shop -> shop != null && shop.getId() != null)
                .map(ShopInfo::getId)
                .collect(Collectors.toList());
            
            if (!shopIds.isEmpty()) {
                LambdaQueryWrapper<SysDept> deptWrapper = new LambdaQueryWrapper<>();
                deptWrapper.in(SysDept::getShopId, shopIds);
                deptWrapper.eq(SysDept::getDelFlag, 0);
                List<SysDept> depts = sysDeptMapper.selectList(deptWrapper);

                for (SysDept dept : depts) {
                    if (dept != null && dept.getShopId() != null && dept.getDeptName() != null) {
                        String existing = shopDeptNamesMap.get(dept.getShopId());
                        if (existing == null) {
                            shopDeptNamesMap.put(dept.getShopId(), dept.getDeptName());
                        } else {
                            shopDeptNamesMap.put(dept.getShopId(), existing + ", " + dept.getDeptName());
                        }
                    }
                }
            }
        }

        List<ShopInfoDTO> dtoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(shops)) {
            for (ShopInfo shop : shops) {
                ShopInfoDTO dto = BeanUtils.convert(shop, ShopInfoDTO.class);
                if (dto == null) {
                    dto = new ShopInfoDTO();
                }
                Long shopId = shop != null ? shop.getId() : null;
                String deptNames = shopId != null ? shopDeptNamesMap.getOrDefault(shopId, "") : "";
                dto.setDeptNames(deptNames);
                dtoList.add(dto);
            }
        }

        return PageResult.success(dtoList, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public CommonResult<ShopInfoDTO> save(ShopInfoSaveDTO saveDTO) {
        ShopInfo shop = BeanUtils.convert(saveDTO, ShopInfo.class);
        shopInfoMapper.insert(shop);
        return CommonResult.success(BeanUtils.convert(shop, ShopInfoDTO.class));
    }

    @Override
    public CommonResult<ShopInfoDTO> update(Long id, ShopInfoSaveDTO saveDTO) {
        ShopInfo shop = shopInfoMapper.selectById(id);
        if (shop == null) {
            return CommonResult.fail("商铺不存在");
        }
        BeanUtils.copyProperties(saveDTO, shop);
        shopInfoMapper.updateById(shop);
        return CommonResult.success(BeanUtils.convert(shop, ShopInfoDTO.class));
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        ShopInfo shop = shopInfoMapper.selectById(id);
        if (shop == null) {
            return CommonResult.fail("商铺不存在");
        }
        shopInfoMapper.deleteById(id);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        ShopInfo shop = shopInfoMapper.selectById(id);
        if (shop == null) {
            return CommonResult.fail("商铺不存在");
        }
        shop.setStatus(status);
        shopInfoMapper.updateById(shop);
        return CommonResult.success();
    }
}