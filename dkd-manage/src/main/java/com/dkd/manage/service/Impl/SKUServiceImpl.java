package com.dkd.manage.service.Impl;

import com.dkd.manage.domain.SKU;
import com.dkd.manage.mapper.SKUMapper;
import com.dkd.manage.service.ISKUService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SKUServiceImpl implements ISKUService {
    @Autowired
    private SKUMapper skuMapper;


    @Override
    public SKU findSKUById(long skuId) {
        return skuMapper.findSKUById(skuId);
    }

    @Override
    public List<SKU> findAllSKU() {
        return skuMapper.findAllSKU();
    }

    @Override
    public int delete(Long[] skuIds) {
        return skuMapper.delete(skuIds);
    }

    @Override
    public int update(SKU sku) {
        return skuMapper.update(sku);
    }

    @Override
    public int add(SKU sku) {
        return skuMapper.add(sku);
    }
}
