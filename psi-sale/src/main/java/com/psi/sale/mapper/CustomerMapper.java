package com.psi.sale.mapper;

import com.psi.sale.entity.CustomerEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomerMapper extends BaseMapper<CustomerEntity> {

    /**
     * 根据更新时间分页查询（用于数据同步）
     */
    @Select("SELECT * FROM customer WHERE update_time > #{lastTime} ORDER BY update_time LIMIT #{offset}, #{limit}")
    List<CustomerEntity> selectByUpdateTimeAfterPage(@Param("lastTime") String lastTime,
                                                      @Param("offset") int offset,
                                                      @Param("limit") int limit);
}