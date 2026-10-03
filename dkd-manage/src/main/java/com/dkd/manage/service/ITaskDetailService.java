package com.dkd.manage.service;

import java.util.List;

import com.dkd.manage.domain.TaskDetail;

/**
 * 工单明细 服务层
 *
 * @author dkd
 */
public interface ITaskDetailService
{
    /**
     * 查询工单明细列表
     */
    public List<TaskDetail> findAllTaskDetail();

    /**
     * 根据明细Id查询工单明细
     */
    public TaskDetail findTaskDetailById(Long detailId);

    /**
     * 新增工单明细
     */
    public int add(TaskDetail taskDetail);

    /**
     * 修改工单明细
     */
    public int update(TaskDetail taskDetail);

    /**
     * 批量删除工单明细
     */
    public int delete(Long[] detailIds);
}
