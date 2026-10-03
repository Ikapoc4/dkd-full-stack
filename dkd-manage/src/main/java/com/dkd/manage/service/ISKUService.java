package com.dkd.manage.service;

import com.dkd.manage.domain.SKU;

import java.util.List;

public interface ISKUService {
    SKU findSKUById(long skuId);

    List<SKU> findAllSKU();

    int delete(Long[] skuIds);

    int update(SKU sku);

    int add(SKU sku);
}
