package com.trademaster.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trademaster.entity.Goods;
import com.trademaster.mapper.GoodsMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {
    private final GoodsMapper goodsMapper;

    public GoodsService(GoodsMapper goodsMapper) {
        this.goodsMapper = goodsMapper;
    }

    public IPage<Goods> findPage(int page, int size, String keyword) {
        Page<Goods> pageParam = new Page<>(page, size);
        QueryWrapper<Goods> query = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            query.like("goods_name", keyword).or().like("goods_code", keyword).or().like("bar_code", keyword);
        }
        query.eq("status", 1);
        query.orderByDesc("created_at");
        return goodsMapper.selectPage(pageParam, query);
    }

    public Goods findById(Long id) {
        return goodsMapper.selectById(id);
    }

    public Goods findByBarCode(String barCode) {
        return goodsMapper.selectOne(new QueryWrapper<Goods>().eq("bar_code", barCode).eq("status", 1));
    }

    public Goods findByCode(String goodsCode) {
        return goodsMapper.selectOne(new QueryWrapper<Goods>().eq("goods_code", goodsCode).eq("status", 1));
    }

    public void save(Goods goods) {
        if (goods.getId() == null) {
            goodsMapper.insert(goods);
        } else {
            goodsMapper.updateById(goods);
        }
    }

    public void delete(Long id) {
        goodsMapper.deleteById(id);
    }

    public void updateStock(Long goodsId, java.math.BigDecimal qty) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods != null) {
            goods.setStockQty(goods.getStockQty().add(qty));
            goodsMapper.updateById(goods);
        }
    }

    public List<Goods> findLowStock() {
        return goodsMapper.selectList(new QueryWrapper<Goods>()
                .lt("stock_qty", "min_stock")
                .eq("status", 1));
    }
}
