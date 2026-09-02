package com.psi.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.member.entity.MemberInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberInfoMapper extends BaseMapper<MemberInfo> {
}
