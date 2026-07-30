package com.psi.system.service.impl;

import com.psi.common.mybatis.util.BatchUtils;
import com.psi.system.dto.SysDictDataDTO;
import com.psi.system.dto.SysDictDataQueryDTO;
import com.psi.system.dto.SysDictDataSaveDTO;
import com.psi.system.entity.SysDictData;
import com.psi.system.mapper.SysDictDataMapper;
import com.psi.system.service.SysDictDataService;
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
 * 字典数据服务实现类
 * 继承 MyBatis-Plus ServiceImpl，提供字典数据访问能力
 */
@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    private final BatchUtils batchUtils;

    public SysDictDataServiceImpl(BatchUtils batchUtils) {
        this.batchUtils = batchUtils;
    }

    @Override
    public CommonResult<SysDictDataDTO> getById(Long id) {
        SysDictData dictData = baseMapper.selectById(id);
        if (dictData == null) {
            return CommonResult.fail("字典数据不存在");
        }
        return CommonResult.success(BeanUtils.convert(dictData, SysDictDataDTO.class));
    }

    @Override
    public PageResult<SysDictDataDTO> list(SysDictDataQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;

        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getDictCode())) {
            wrapper.eq(SysDictData::getDictCode, queryDTO.getDictCode());
        }
        if (StringUtils.hasText(queryDTO.getDictValue())) {
            wrapper.like(SysDictData::getDictValue, queryDTO.getDictValue());
        }
        if (StringUtils.hasText(queryDTO.getDictLabel())) {
            wrapper.like(SysDictData::getDictLabel, queryDTO.getDictLabel());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysDictData::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByAsc(SysDictData::getSortOrder).orderByDesc(SysDictData::getCreateTime);

        IPage<SysDictData> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.success(
            BeanUtils.convertList(page.getRecords(), SysDictDataDTO.class),
            page.getTotal(),
            pageNum,
            pageSize
        );
    }

    @Override
    public CommonResult<SysDictDataDTO> save(SysDictDataSaveDTO saveDTO) {
        SysDictData dictData = BeanUtils.convert(saveDTO, SysDictData.class);
        super.save(dictData);
        return CommonResult.success(BeanUtils.convert(dictData, SysDictDataDTO.class));
    }

    @Override
    public CommonResult<SysDictDataDTO> update(Long id, SysDictDataSaveDTO saveDTO) {
        SysDictData dictData = super.getById(id);
        if (dictData == null) {
            return CommonResult.fail("字典数据不存在");
        }
        BeanUtils.copyProperties(saveDTO, dictData);
        super.updateById(dictData);
        return CommonResult.success(BeanUtils.convert(dictData, SysDictDataDTO.class));
    }

    @Override
    public CommonResult<Void> delete(Long id) {
        SysDictData dictData = super.getById(id);
        if (dictData == null) {
            return CommonResult.fail("字典数据不存在");
        }
        super.removeById(id);
        return CommonResult.success();
    }

    @Override
    public CommonResult<Void> updateStatus(Long id, Integer status) {
        SysDictData dictData = super.getById(id);
        if (dictData == null) {
            return CommonResult.fail("字典数据不存在");
        }
        dictData.setStatus(status);
        super.updateById(dictData);
        return CommonResult.success();
    }

    @Override
    @Transactional
    public CommonResult<List<SysDictDataDTO>> batchSave(List<SysDictDataSaveDTO> saveDTOList) {
        if (saveDTOList == null || saveDTOList.isEmpty()) {
            return CommonResult.success(new ArrayList<>());
        }

        List<SysDictData> dictDatas = saveDTOList.stream()
                .map(dto -> BeanUtils.convert(dto, SysDictData.class))
                .toList();

        List<SysDictData> insertList = dictDatas.stream()
                .filter(dictData -> dictData.getId() == null)
                .toList();

        List<SysDictData> updateList = dictDatas.stream()
                .filter(dictData -> dictData.getId() != null)
                .toList();

        if (!insertList.isEmpty()) {
            batchUtils.saveBatch(this, insertList);
        }

        if (!updateList.isEmpty()) {
            batchUtils.updateBatchById(this, updateList);
        }

        return CommonResult.success(BeanUtils.convertList(dictDatas, SysDictDataDTO.class));
    }
}