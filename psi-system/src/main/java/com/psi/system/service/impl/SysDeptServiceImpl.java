package com.psi.system.service.impl;

import com.psi.common.mybatis.util.BatchUtils;
import com.psi.system.dto.SysDeptDTO;
import com.psi.system.dto.SysDeptQueryDTO;
import com.psi.system.dto.SysDeptSaveDTO;
import com.psi.system.entity.SysDept;
import com.psi.system.mapper.SysDeptMapper;
import com.psi.system.service.SysDeptService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门服务实现类
 * 继承 MyBatis-Plus ServiceImpl，提供部门数据访问能力
 */
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    private final BatchUtils batchUtils;

    public SysDeptServiceImpl(BatchUtils batchUtils) {
        this.batchUtils = batchUtils;
    }

    @Override
    public CommonResult<SysDeptDTO> getById(Long id) {
        SysDept dept = baseMapper.selectById(id);
        if (dept == null) {
            return CommonResult.fail("部门不存在");
        }
        SysDeptDTO dto = BeanUtils.convert(dept, SysDeptDTO.class);
        if (dto != null && dept.getParentId() != null && dept.getParentId() > 0) {
            SysDept parent = baseMapper.selectById(dept.getParentId());
            if (parent != null) {
                dto.setParentName(parent.getDeptName());
            }
        }
        return CommonResult.success(dto);
    }

    @Override
    public PageResult<SysDeptDTO> list(SysDeptQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        IPage<SysDeptDTO> page = baseMapper.selectDeptPageWithShop(
            new Page<>(pageNum, pageSize),
            queryDTO.getDeptName(),
            queryDTO.getDeptCode(),
            queryDTO.getParentId(),
            queryDTO.getShopId(),
            queryDTO.getStatus()
        );

        return PageResult.success(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public CommonResult<SysDeptDTO> save(SysDeptSaveDTO saveDTO) {
        SysDept dept = BeanUtils.convert(saveDTO, SysDept.class);
        super.save(dept);
        return CommonResult.success(BeanUtils.convert(dept, SysDeptDTO.class));
    }

    @Override
    public CommonResult<SysDeptDTO> update(Long id, SysDeptSaveDTO saveDTO) {
        SysDept dept = super.getById(id);
        if (dept == null) {
            return CommonResult.fail("部门不存在");
        }
        BeanUtils.copyProperties(saveDTO, dept);
        super.updateById(dept);
        return CommonResult.success(BeanUtils.convert(dept, SysDeptDTO.class));
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        SysDept dept = super.getById(id);
        if (dept == null) {
            return CommonResult.fail("部门不存在");
        }
        super.removeById(id);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        SysDept dept = super.getById(id);
        if (dept == null) {
            return CommonResult.fail("部门不存在");
        }
        dept.setStatus(status);
        super.updateById(dept);
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<List<SysDeptDTO>> batchSave(List<SysDeptSaveDTO> saveDTOList) {
        if (saveDTOList == null || saveDTOList.isEmpty()) {
            return CommonResult.success(new ArrayList<>());
        }

        List<SysDept> depts = saveDTOList.stream()
                .map(dto -> BeanUtils.convert(dto, SysDept.class))
                .toList();

        List<SysDept> insertList = depts.stream()
                .filter(dept -> dept.getId() == null)
                .toList();

        List<SysDept> updateList = depts.stream()
                .filter(dept -> dept.getId() != null)
                .toList();

        if (!insertList.isEmpty()) {
            batchUtils.saveBatch(this, insertList);
        }

        if (!updateList.isEmpty()) {
            batchUtils.updateBatchById(this, updateList);
        }

        return CommonResult.success(BeanUtils.convertList(depts, SysDeptDTO.class));
    }
}