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
import com.dkd.manage.domain.Task;
import com.dkd.manage.service.ITaskService;

/**
 * 工单信息操作处理
 *
 * @author dkd
 */
@RestController
@RequestMapping("/manage/task")
public class TaskController extends BaseController
{
    @Autowired
    private ITaskService taskService;

    /**
     * 查询工单列表
     */
    @PreAuthorize("@ss.hasPermi('manage:task:list')")
    @GetMapping("/tasklist")
    public TableDataInfo list()
    {
        startPage();
        List<Task> list = taskService.findAllTask();
        return getDataTable(list);
    }

    /**
     * 根据工单Id获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:task:query')")
    @GetMapping("/{taskId}")
    public AjaxResult getInfo(@PathVariable Long taskId)
    {
        return success(taskService.findTaskById(taskId));
    }

    /**
     * 新增工单
     */
    @PreAuthorize("@ss.hasPermi('manage:task:add')")
    @Log(title = "工单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Task task)
    {
        task.setCreateBy(getUsername());
        return toAjax(taskService.add(task));
    }

    /**
     * 修改工单
     */
    @PreAuthorize("@ss.hasPermi('manage:task:edit')")
    @Log(title = "工单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Task task)
    {
        task.setUpdateBy(getUsername());
        return toAjax(taskService.update(task));
    }

    /**
     * 删除工单
     */
    @PreAuthorize("@ss.hasPermi('manage:task:remove')")
    @Log(title = "工单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(taskService.delete(taskIds));
    }

    /**
     * ★ 阶段 3：手动为某台设备创建补货工单
     *
     * getUserId() 来自 BaseController，从当前登录 token 里取
     */
    @PreAuthorize("@ss.hasPermi('manage:task:add')")
    @Log(title = "补货工单", businessType = BusinessType.INSERT)
    @PostMapping("/supply/{vmId}")
    public AjaxResult supply(@PathVariable Long vmId)
    {
        taskService.createSupplyTask(vmId, getUserId());
        return success("补货工单创建成功");
    }
}
