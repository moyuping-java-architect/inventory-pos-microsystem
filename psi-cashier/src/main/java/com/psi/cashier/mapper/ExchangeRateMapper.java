package com.psi.cashier.mapper;

import com.psi.cashier.entity.ExchangeRateEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ExchangeRateMapper extends BaseMapper<ExchangeRateEntity> {

    /**
     * 查询最新有效汇率
     */
    @Select("SELECT * FROM exchange_rate " +
            "WHERE from_currency = #{fromCurrency} AND to_currency = #{toCurrency} " +
            "ORDER BY effective_date DESC, id DESC LIMIT 1")
    ExchangeRateEntity selectLatest(@Param("fromCurrency") String fromCurrency,
                                    @Param("toCurrency") String toCurrency);
}
