package com.dkd.manage.controller;

import com.dkd.common.core.controller.BaseController;
import com.dkd.common.core.domain.AjaxResult;
import com.dkd.common.core.page.TableDataInfo;
import com.dkd.manage.domain.SKU;
import com.dkd.manage.service.ISKUService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/manage/sku")
public class SKUController extends BaseController {

    @Autowired
    private ISKUService skuService;


    @GetMapping("/skulist")
    public TableDataInfo list() {
        startPage();
        List<SKU> list = skuService.findAllSKU();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('manage:sku:query')")
    @GetMapping("/{skuId}")
    public AjaxResult findSKUById(@PathVariable Long skuId) {
        return success( skuService.findSKUById(skuId));
    }

    @PreAuthorize("@ss.hasPermi('manage:sku:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SKU sku) {
        return toAjax(skuService.add(sku));
    }

    @PreAuthorize("@ss.hasPermi('manage:sku:edit')")
    @PutMapping
    public AjaxResult update(@Validated @RequestBody SKU sku) {
        return toAjax(skuService.update(sku));
    }

    @PreAuthorize("@ss.hasPermi('manage:sku:remove')")
    @DeleteMapping("/{skuIds}")
    public AjaxResult delete(@PathVariable Long[] skuIds) {
        return toAjax(skuService.delete(skuIds));
    }
}
