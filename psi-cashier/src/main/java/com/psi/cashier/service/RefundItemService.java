package com.psi.cashier.service;

import com.psi.cashier.entity.RefundItemEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RefundItemService extends IService<RefundItemEntity> {

    void saveBatch(List<RefundItemEntity> items);

    List<RefundItemEntity> getByReturnUuid(String returnUuid);

    void deleteByReturnUuid(String returnUuid);
}