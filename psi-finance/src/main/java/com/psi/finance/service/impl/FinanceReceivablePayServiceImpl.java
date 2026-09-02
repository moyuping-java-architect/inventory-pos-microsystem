package com.psi.finance.service.impl;

import com.psi.finance.dto.FinanceReceivablePayDTO;
import com.psi.finance.dto.FinancePaySaveDTO;
import com.psi.finance.entity.FinanceReceivableEntity;
import com.psi.finance.entity.FinanceReceivablePayEntity;
import com.psi.finance.mapper.FinanceReceivableMapper;
import com.psi.finance.mapper.FinanceReceivablePayMapper;
import com.psi.finance.mq.producer.FinanceSyncProducer;
import com.psi.finance.service.FinanceReceivablePayService;
import com.psi.common.event.BusinessEvent;
import com.psi.common.event.BusinessEventPublisher;
import com.psi.common.result.CommonResult;
import com.psi.common.result.PageResult;
import com.psi.common.result.ResultCode;
import com.psi.common.util.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class FinanceReceivablePayServiceImpl extends ServiceImpl<FinanceReceivablePayMapper, FinanceReceivablePayEntity> implements FinanceReceivablePayService {

    private final FinanceSyncProducer financeSyncProducer;
    private final FinanceReceivableMapper receivableMapper;
    /** 客户旅程事件出口 */
    private final BusinessEventPublisher eventPublisher;

    public FinanceReceivablePayServiceImpl(@Lazy FinanceSyncProducer financeSyncProducer,
                                           FinanceReceivableMapper receivableMapper,
                                           BusinessEventPublisher eventPublisher) {
        this.financeSyncProducer = financeSyncProducer;
        this.receivableMapper = receivableMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public boolean save(FinanceReceivablePayEntity entity) {
        boolean result = super.save(entity);
        if (result) {
            sendReceivablePaySync(entity);
            publishPaymentEvent(entity);
        }
        return result;
    }

    @Override
    public boolean updateById(FinanceReceivablePayEntity entity) {
        boolean result = super.updateById(entity);
        if (result) {
            sendReceivablePaySync(entity);
        }
        return result;
    }

    @Override
    public boolean saveOrUpdate(FinanceReceivablePayEntity entity) {
        boolean result = super.saveOrUpdate(entity);
        if (result) {
            sendReceivablePaySync(entity);
        }
        return result;
    }

    private void sendReceivablePaySync(FinanceReceivablePayEntity entity) {
        try {
            financeSyncProducer.sendFinanceReceivablePay(entity);
        } catch (Exception e) {
            log.error("应收付款数据实时同步发送失败", e);
        }
    }

    @Override
    public CommonResult<FinanceReceivablePayDTO> getById(Long id) {
        FinanceReceivablePayEntity entity = super.getById(id);
        if (entity == null) {
            return CommonResult.fail(ResultCode.NOT_FOUND);
        }
        return CommonResult.success(BeanUtils.convert(entity, FinanceReceivablePayDTO.class));
    }

    @Override
    public PageResult<FinanceReceivablePayDTO> list(Long receivableId) {
        Page<FinanceReceivablePayEntity> page = new Page<>(1, 100);
        LambdaQueryWrapper<FinanceReceivablePayEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceReceivablePayEntity::getReceivableId, receivableId);
        IPage<FinanceReceivablePayEntity> result = super.page(page, wrapper);
        return PageResult.convert(result, entity -> BeanUtils.convert(entity, FinanceReceivablePayDTO.class));
    }

    @Override
    @Transactional
    public CommonResult<FinanceReceivablePayDTO> pay(FinancePaySaveDTO saveDTO) {
        FinanceReceivablePayEntity entity = BeanUtils.convert(saveDTO, FinanceReceivablePayEntity.class);
        // 事件发布已统一收敛到 save() 重写中，任何入库路径都会触发
        super.save(entity);

        return CommonResult.success(BeanUtils.convert(entity, FinanceReceivablePayDTO.class));
    }

    /**
     * 发布「收到客户回款」业务事件。
     * <p>
     * 回款单本身只带 customerCode，主体ID 需要回查应收单拿 customerId；
     * 拿不到就不发事件（无主体的事件下游无法归属，发了也是垃圾数据）。
     */
    private void publishPaymentEvent(FinanceReceivablePayEntity entity) {
        try {
            if (entity.getReceivableId() == null) {
                return;
            }
            FinanceReceivableEntity receivable = receivableMapper.selectById(entity.getReceivableId());
            if (receivable == null || receivable.getCustomerId() == null) {
                log.debug("回款事件跳过: 应收单无客户ID, receivableId={}", entity.getReceivableId());
                return;
            }

            eventPublisher.publish(
                    BusinessEvent.of("FINANCE.PAYMENT_RECEIVED",
                                    BusinessEvent.SUBJECT_CUSTOMER, receivable.getCustomerId())
                            .bizKey(entity.getPayNo() != null
                                    ? entity.getPayNo() : String.valueOf(entity.getId()))
                            .put("docNo", entity.getPayNo())
                            .put("payAmount", entity.getPayAmount())
                            .put("payMethod", entity.getPayMethod()));
        } catch (Exception e) {
            log.error("发布回款事件失败, receivableId={}", entity.getReceivableId(), e);
        }
    }
}
