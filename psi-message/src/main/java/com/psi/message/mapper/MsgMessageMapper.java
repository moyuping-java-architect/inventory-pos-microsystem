package com.psi.message.mapper;

import com.psi.message.entity.MsgMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MsgMessageMapper extends BaseMapper<MsgMessage> {
}