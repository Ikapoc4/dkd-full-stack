package com.dkd.manage.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dkd.common.annotation.Log;
import com.dkd.common.core.controller.BaseController;
import com.dkd.common.core.domain.AjaxResult;
import com.dkd.common.core.page.TableDataInfo;
import com.dkd.common.enums.BusinessType;
import com.dkd.manage.domain.TaskDetail;
import com.dkd.manage.service.ITaskDetailService;

/**
 * 工单明细操作处理
 *
 * 说明：明细一般跟着工单走（在 TaskServiceImpl 里一起创建），
 *      这个 Controller 主要是给维护/排查用的。
 *
 * @author dkd
 */
@RestController
@RequestMapping("/manage/taskdetail")
public class TaskDetailController extends BaseController
{
    @Autowired
    private ITaskDetailService taskDetailService;

    /**
     * 查询工单明细列表
     */
    @PreAuthorize("@ss.hasPermi('manage:taskdetail:list')")
    @GetMapping("/taskdetaillist")
    public TableDataInfo list()
    {
        startPage();
        List<TaskDetail> list = taskDetailService.findAllTaskDetail();
        return getDataTable(list);
    }

    /**
     * 根据明细Id获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:taskdetail:query')")
    @GetMapping("/{detailId}")
    public AjaxResult getInfo(@PathVariable Long detailId)
    {
        return success(taskDetailService.findTaskDetailById(detailId));
    }

    /**
     * 新增工单明细
     */
    @PreAuthorize("@ss.hasPermi('manage:taskdetail:add')")
    @Log(title = "工单明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TaskDetail taskDetail)
    {
        return toAjax(taskDetailService.add(taskDetail));
    }

    /**
     * 修改工单明细
     */
    @PreAuthorize("@ss.hasPermi('manage:taskdetail:edit')")
    @Log(title = "工单明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TaskDetail taskDetail)
    {
        return toAjax(taskDetailService.update(taskDetail));
    }

    /**
     * 删除工单明细
     */
    @PreAuthorize("@ss.hasPermi('manage:taskdetail:remove')")
    @Log(title = "工单明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{detailIds}")
    public AjaxResult remove(@PathVariable Long[] detailIds)
    {
        return toAjax(taskDetailService.delete(detailIds));
    }
}
