package com.psi.cashier.mapper;

import com.psi.cashier.entity.SysSeqEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysSeqMapper extends BaseMapper<SysSeqEntity> {

    @Select("SELECT * FROM sys_seq WHERE pos_id = #{posId} AND seq_type = #{seqType} AND day = #{day}")
    SysSeqEntity selectForUpdate(@Param("posId") String posId, @Param("seqType") String seqType, @Param("day") String day);

    @Update("UPDATE sys_seq SET curr_no = curr_no + 1 WHERE pos_id = #{posId} AND seq_type = #{seqType} AND day = #{day}")
    int incrementValue(@Param("posId") String posId, @Param("seqType") String seqType, @Param("day") String day);
}