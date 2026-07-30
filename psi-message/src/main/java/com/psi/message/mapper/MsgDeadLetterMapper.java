package com.psi.message.mapper;

import com.psi.message.entity.MsgDeadLetter;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MsgDeadLetterMapper extends BaseMapper<MsgDeadLetter> {
}