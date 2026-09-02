package com.psi.cashier.service.impl;

import com.psi.cashier.entity.RefundItemEntity;
import com.psi.cashier.mapper.RefundItemMapper;
import com.psi.cashier.service.RefundItemService;
import com.psi.common.mybatis.util.BatchUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundItemServiceImpl extends ServiceImpl<RefundItemMapper, RefundItemEntity> implements RefundItemService {

    private final BatchUtils batchUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(List<RefundItemEntity> items) {
        if (items != null && !items.isEmpty()) {
            batchUtils.saveBatch(this, items);
            log.info("批量保存退款明细成功，数量：{}", items.size());
        }
    }

    @Override
    public List<RefundItemEntity> getByReturnUuid(String returnUuid) {
        return getBaseMapper().selectByReturnUuid(returnUuid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByReturnUuid(String returnUuid) {
        getBaseMapper().deleteByReturnUuid(returnUuid);
        log.info("删除退款明细成功，returnUuid：{}", returnUuid);
    }
}