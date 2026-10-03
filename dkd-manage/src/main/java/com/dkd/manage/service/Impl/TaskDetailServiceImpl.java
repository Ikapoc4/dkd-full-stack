package com.dkd.manage.service.Impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dkd.manage.domain.TaskDetail;
import com.dkd.manage.mapper.TaskDetailMapper;
import com.dkd.manage.service.ITaskDetailService;

/**
 * 工单明细 服务层处理
 *
 * 注意：tb_task_detail 没有 create_by / update_by 列，
 *      所以这里【不要】调 setCreateBy() / setUpdateBy()。
 *
 * @author dkd
 */
@Service
public class TaskDetailServiceImpl implements ITaskDetailService
{
    @Autowired
    private TaskDetailMapper taskDetailMapper;

    @Override
    public List<TaskDetail> findAllTaskDetail()
    {
        return taskDetailMapper.findAllTaskDetail();
    }

    @Override
    public TaskDetail findTaskDetailById(Long detailId)
    {
        return taskDetailMapper.findTaskDetailById(detailId);
    }

    @Override
    public int add(TaskDetail taskDetail)
    {
        return taskDetailMapper.add(taskDetail);
    }

    @Override
    public int update(TaskDetail taskDetail)
    {
        return taskDetailMapper.update(taskDetail);
    }

    @Override
    public int delete(Long[] detailIds)
    {
        return taskDetailMapper.delete(detailIds);
    }

}
