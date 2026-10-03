package com.dkd.manage.controller;

import com.dkd.common.core.controller.BaseController;
import com.dkd.common.core.domain.AjaxResult;
import com.dkd.common.core.page.TableDataInfo;
import com.dkd.manage.domain.VM;
import com.dkd.manage.service.IVmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/manage/vm")
public class VmController extends BaseController {

    @Autowired
    private IVmService vmService;

    @GetMapping("/vmlist")
    public TableDataInfo list() {
        startPage();
        List<VM> vmList = vmService.findAllVM();
        return getDataTable(vmList);
    }

    @PreAuthorize("@ss.hasPermi('manage:vm:query')")
    @GetMapping("/{vmId}")
    public AjaxResult getVmById(@PathVariable Long vmId) {
        return success(vmService.findVMById(vmId));
    }

    @PreAuthorize("@ss.hasPermi('manage:vm:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody VM vm) {
        return toAjax(vmService.add(vm));
    }

    @PreAuthorize("@ss.hasPermi('manage:vm:edit')")
    @PutMapping
    public AjaxResult update(@Validated @RequestBody VM vm) {
        return toAjax(vmService.update(vm));
    }

    @PreAuthorize("@ss.hasPermi('manage:vm:remove')")
    @DeleteMapping("/{vmIds}")
    public AjaxResult delete(@PathVariable Long[] vmIds) {
        return toAjax(vmService.delete(vmIds));
    }




}
