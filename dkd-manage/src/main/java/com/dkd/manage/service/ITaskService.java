package com.dkd.manage.service;

import java.util.List;

import com.dkd.manage.domain.Task;

/**
 * 工单 服务层
 *
 * @author dkd
 */
public interface ITaskService
{
    /**
     * 查询工单列表
     */
    public List<Task> findAllTask();

    /**
     * 根据工单Id查询工单
     */
    public Task findTaskById(Long taskId);

    /**
     * 新增工单
     */
    public int add(Task task);

    /**
     * 修改工单
     */
    public int update(Task task);

    /**
     * 批量删除工单
     */
    public int delete(Long[] taskIds);

    /**
     * ★ 阶段 3：手动为某台设备创建补货工单
     *
     * @param vmId   设备Id
     * @param userId 操作人Id（手动建的记当前人；阶段 4 的定时任务传 0L）
     * @return 创建结果
     */
    public int createSupplyTask(Long vmId, Long userId);
}
