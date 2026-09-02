package com.psi.system.mapper;

import com.psi.system.entity.SysUser;
import com.psi.system.dto.SysUserDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    IPage<SysUserDTO> selectUserPageWithDeptAndShop(
            IPage<SysUserDTO> page,
            @Param("username") String username,
            @Param("nickname") String nickname,
            @Param("deptId") Long deptId,
            @Param("status") Integer status
    );
}