package com.psi.cashier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psi.cashier.entity.RefundItemEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RefundItemMapper extends BaseMapper<RefundItemEntity> {

    List<RefundItemEntity> selectByReturnUuid(String returnUuid);

    void deleteByReturnUuid(String returnUuid);
}