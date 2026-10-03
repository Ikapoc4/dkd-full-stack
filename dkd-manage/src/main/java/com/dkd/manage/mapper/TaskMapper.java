package com.dkd.manage.mapper;

import java.util.List;

import com.dkd.manage.domain.Task;

/**
 * 工单 数据层
 *
 * @author dkd
 */
public interface TaskMapper
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
     *
     * ★ 注意：XML 里配了 useGeneratedKeys="true" keyProperty="taskId"，
     *   所以这个方法执行完之后，参数 task.getTaskId() 就有值了 ——
     *   阶段 3 建明细时要靠它。
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
}
