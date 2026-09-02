package com.psi.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psi.goods.entity.AdjustPriceItemEntity;
import com.psi.goods.entity.AdjustPriceMainEntity;
import com.psi.goods.entity.GoodsSku;
import com.psi.goods.mapper.AdjustPriceItemMapper;
import com.psi.goods.mapper.AdjustPriceMainMapper;
import com.psi.goods.service.GoodsSkuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 商品调价单服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AdjustPriceServiceImplTest {

    @Mock
    private AdjustPriceMainMapper mainMapper;

    @Mock
    private AdjustPriceItemMapper itemMapper;

    @Mock
    private GoodsSkuService goodsSkuService;

    private AdjustPriceServiceImpl adjustPriceService;

    @BeforeEach
    void setUp() {
        adjustPriceService = new AdjustPriceServiceImpl(itemMapper, goodsSkuService);
        ReflectionTestUtils.setField(adjustPriceService, "baseMapper", mainMapper);
    }

    @Test
    void saveAdjustPrice_shouldSetDefaultValuesAndSaveItems() {
        AdjustPriceMainEntity main = new AdjustPriceMainEntity();
        main.setAdjustNo("TP20240701001");

        AdjustPriceItemEntity item1 = new AdjustPriceItemEntity();
        item1.setGoodsCode("SP001");
        item1.setNewPrice(new BigDecimal("100.00"));

        AdjustPriceItemEntity item2 = new AdjustPriceItemEntity();
        item2.setGoodsCode("SP002");
        item2.setNewPrice(new BigDecimal("200.00"));

        when(mainMapper.insert(any(AdjustPriceMainEntity.class))).thenAnswer(invocation -> {
            AdjustPriceMainEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return 1;
        });

        var result = adjustPriceService.saveAdjustPrice(main, List.of(item1, item2));

        assertTrue(result.isSuccess());
        assertEquals(2, main.getItemCount());
        assertEquals(0, main.getStatus());
        assertEquals(new BigDecimal("100.00"), item1.getAmount());
        assertEquals(new BigDecimal("200.00"), item2.getAmount());
        verify(itemMapper, times(2)).insert(any(AdjustPriceItemEntity.class));
    }

    @Test
    void audit_shouldUpdateSkuPriceAndStatus() {
        AdjustPriceMainEntity main = new AdjustPriceMainEntity();
        main.setId(1L);
        main.setAdjustNo("TP20240701001");
        main.setStatus(1);

        AdjustPriceItemEntity item = new AdjustPriceItemEntity();
        item.setSkuCode("SKU001");
        item.setNewPrice(new BigDecimal("150.00"));

        GoodsSku sku = new GoodsSku();
        sku.setSkuCode("SKU001");
        sku.setSalePrice(new BigDecimal("100.00"));

        when(mainMapper.selectById(1L)).thenReturn(main);
        when(itemMapper.selectByAdjustId(1L)).thenReturn(List.of(item));
        when(goodsSkuService.getOne(any(LambdaQueryWrapper.class))).thenReturn(sku);
        when(mainMapper.updateById(any(AdjustPriceMainEntity.class))).thenReturn(1);

        var result = adjustPriceService.audit(1L);

        assertTrue(result.isSuccess());
        assertEquals(new BigDecimal("150.00"), sku.getSalePrice());
        assertEquals(2, main.getStatus());
        verify(goodsSkuService).updateById(sku);
    }

    @Test
    void audit_shouldSkipWhenAlreadyAudited() {
        AdjustPriceMainEntity main = new AdjustPriceMainEntity();
        main.setId(1L);
        main.setStatus(2);

        when(mainMapper.selectById(1L)).thenReturn(main);

        var result = adjustPriceService.audit(1L);

        assertFalse(result.isSuccess());
        verify(itemMapper, never()).selectByAdjustId(any());
    }
}
