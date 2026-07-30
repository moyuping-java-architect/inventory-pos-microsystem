package com.psi.system.service.impl;

import com.psi.system.dto.SysMenuDTO;
import com.psi.system.dto.SysMenuQueryDTO;
import com.psi.system.dto.SysMenuSaveDTO;
import com.psi.system.entity.SysMenu;
import com.psi.system.mapper.SysMenuMapper;
import com.psi.system.service.SysMenuService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.util.BeanUtils;
import com.psi.common.mybatis.util.BatchUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单服务实现类
 * 继承 MyBatis-Plus ServiceImpl，提供菜单数据访问能力
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final BatchUtils batchUtils;

    public SysMenuServiceImpl(BatchUtils batchUtils) {
        this.batchUtils = batchUtils;
    }

    @Override
    public CommonResult<SysMenuDTO> getById(Long id) {
        SysMenu menu = super.getById(id);
        if (menu == null) {
            return CommonResult.fail("菜单不存在");
        }
        return CommonResult.success(BeanUtils.convert(menu, SysMenuDTO.class));
    }

    @Override
    public PageResult<SysMenuDTO> list(SysMenuQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getMenuName())) {
            wrapper.like(SysMenu::getMenuName, queryDTO.getMenuName());
        }
        if (queryDTO.getParentId() != null) {
            wrapper.eq(SysMenu::getParentId, queryDTO.getParentId());
        }
        if (queryDTO.getMenuType() != null) {
            wrapper.eq(SysMenu::getMenuType, queryDTO.getMenuType());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysMenu::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByAsc(SysMenu::getSortOrder).orderByDesc(SysMenu::getCreateTime);

        IPage<SysMenu> page = super.page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.success(
            BeanUtils.convertList(page.getRecords(), SysMenuDTO.class),
            page.getTotal(),
            pageNum,
            pageSize
        );
    }

    @Override
    public CommonResult<SysMenuDTO> save(SysMenuSaveDTO saveDTO) {
        SysMenu menu = BeanUtils.convert(saveDTO, SysMenu.class);
        super.save(menu);
        return CommonResult.success(BeanUtils.convert(menu, SysMenuDTO.class));
    }

    @Override
    public CommonResult<SysMenuDTO> update(Long id, SysMenuSaveDTO saveDTO) {
        SysMenu menu = super.getById(id);
        if (menu == null) {
            return CommonResult.fail("菜单不存在");
        }
        BeanUtils.copyProperties(saveDTO, menu);
        super.updateById(menu);
        return CommonResult.success(BeanUtils.convert(menu, SysMenuDTO.class));
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        SysMenu menu = super.getById(id);
        if (menu == null) {
            return CommonResult.fail("菜单不存在");
        }
        super.removeById(id);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        SysMenu menu = super.getById(id);
        if (menu == null) {
            return CommonResult.fail("菜单不存在");
        }
        menu.setStatus(status);
        super.updateById(menu);
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<List<SysMenuDTO>> batchSave(List<SysMenuSaveDTO> saveDTOList) {
        if (saveDTOList == null || saveDTOList.isEmpty()) {
            return CommonResult.success(new ArrayList<>());
        }

        List<SysMenu> menus = saveDTOList.stream()
                .map(dto -> BeanUtils.convert(dto, SysMenu.class))
                .toList();

        List<SysMenu> insertList = menus.stream()
                .filter(menu -> menu.getId() == null)
                .toList();

        List<SysMenu> updateList = menus.stream()
                .filter(menu -> menu.getId() != null)
                .toList();

        if (!insertList.isEmpty()) {
            batchUtils.saveBatch(this, insertList);
        }

        if (!updateList.isEmpty()) {
            batchUtils.updateBatchById(this, updateList);
        }

        return CommonResult.success(BeanUtils.convertList(menus, SysMenuDTO.class));
    }
}