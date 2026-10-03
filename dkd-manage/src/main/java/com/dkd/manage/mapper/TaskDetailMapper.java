package com.dkd.manage.mapper;

import java.util.List;

import com.dkd.manage.domain.TaskDetail;

/**
 * 工单明细 数据层
 *
 * @author dkd
 */
public interface TaskDetailMapper
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
     *
     * ★ 阶段 3 建单时会循环调用它，每个缺货货道插一条
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
