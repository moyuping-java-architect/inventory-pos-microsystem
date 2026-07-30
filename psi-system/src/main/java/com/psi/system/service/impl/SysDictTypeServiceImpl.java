package com.psi.system.service.impl;

import com.psi.common.mybatis.util.BatchUtils;
import com.psi.system.dto.SysDictTypeDTO;
import com.psi.system.dto.SysDictTypeQueryDTO;
import com.psi.system.dto.SysDictTypeSaveDTO;
import com.psi.system.entity.SysDictType;
import com.psi.system.mapper.SysDictTypeMapper;
import com.psi.system.service.SysDictTypeService;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.util.BeanUtils;
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
 * 字典类型服务实现类
 * 继承 MyBatis-Plus ServiceImpl，提供字典类型数据访问能力
 */
@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    private final BatchUtils batchUtils;

    public SysDictTypeServiceImpl(BatchUtils batchUtils) {
        this.batchUtils = batchUtils;
    }

    @Override
    public CommonResult<SysDictTypeDTO> getById(Long id) {
        SysDictType dictType = baseMapper.selectById(id);
        if (dictType == null) {
            return CommonResult.fail("字典类型不存在");
        }
        return CommonResult.success(BeanUtils.convert(dictType, SysDictTypeDTO.class));
    }

    @Override
    public PageResult<SysDictTypeDTO> list(SysDictTypeQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getDictCode())) {
            wrapper.like(SysDictType::getDictCode, queryDTO.getDictCode());
        }
        if (StringUtils.hasText(queryDTO.getDictName())) {
            wrapper.like(SysDictType::getDictName, queryDTO.getDictName());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysDictType::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(SysDictType::getCreateTime);

        IPage<SysDictType> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.success(
            BeanUtils.convertList(page.getRecords(), SysDictTypeDTO.class),
            page.getTotal(),
            pageNum,
            pageSize
        );
    }

    @Override
    public CommonResult<SysDictTypeDTO> save(SysDictTypeSaveDTO saveDTO) {
        SysDictType dictType = BeanUtils.convert(saveDTO, SysDictType.class);
        baseMapper.insert(dictType);
        return CommonResult.success(BeanUtils.convert(dictType, SysDictTypeDTO.class));
    }

    @Override
    public CommonResult<SysDictTypeDTO> update(Long id, SysDictTypeSaveDTO saveDTO) {
        SysDictType dictType = baseMapper.selectById(id);
        if (dictType == null) {
            return CommonResult.fail("字典类型不存在");
        }
        BeanUtils.copyProperties(saveDTO, dictType);
        baseMapper.updateById(dictType);
        return CommonResult.success(BeanUtils.convert(dictType, SysDictTypeDTO.class));
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        SysDictType dictType = baseMapper.selectById(id);
        if (dictType == null) {
            return CommonResult.fail("字典类型不存在");
        }
        baseMapper.deleteById(id);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        SysDictType dictType = baseMapper.selectById(id);
        if (dictType == null) {
            return CommonResult.fail("字典类型不存在");
        }
        dictType.setStatus(status);
        baseMapper.updateById(dictType);
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<List<SysDictTypeDTO>> batchSave(List<SysDictTypeSaveDTO> saveDTOList) {
        if (saveDTOList == null || saveDTOList.isEmpty()) {
            return CommonResult.success(new ArrayList<>());
        }

        List<SysDictType> dictTypes = saveDTOList.stream()
                .map(dto -> BeanUtils.convert(dto, SysDictType.class))
                .toList();

        List<SysDictType> insertList = dictTypes.stream()
                .filter(dictType -> dictType.getId() == null)
                .toList();

        List<SysDictType> updateList = dictTypes.stream()
                .filter(dictType -> dictType.getId() != null)
                .toList();

        if (!insertList.isEmpty()) {
            batchUtils.saveBatch(this, insertList);
        }

        if (!updateList.isEmpty()) {
            batchUtils.updateBatchById(this, updateList);
        }

        return CommonResult.success(BeanUtils.convertList(dictTypes, SysDictTypeDTO.class));
    }
}