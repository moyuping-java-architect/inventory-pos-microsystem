package com.psi.system.mapper;

import com.psi.system.entity.SysDept;
import com.psi.system.dto.SysDeptDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    IPage<SysDeptDTO> selectDeptPageWithShop(
            IPage<SysDeptDTO> page,
            @Param("deptName") String deptName,
            @Param("deptCode") String deptCode,
            @Param("parentId") Long parentId,
            @Param("shopId") Long shopId,
            @Param("status") Integer status
    );

    Long selectDeptCount(
            @Param("deptName") String deptName,
            @Param("deptCode") String deptCode,
            @Param("parentId") Long parentId,
            @Param("shopId") Long shopId,
            @Param("status") Integer status
    );
}