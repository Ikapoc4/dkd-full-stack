package com.dkd.manage.service.Impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dkd.common.constant.DkdContants;
import com.dkd.common.exception.ServiceException;
import com.dkd.manage.domain.Channel;
import com.dkd.manage.domain.Task;
import com.dkd.manage.domain.TaskDetail;
import com.dkd.manage.mapper.ChannelMapper;
import com.dkd.manage.mapper.TaskDetailMapper;
import com.dkd.manage.mapper.TaskMapper;
import com.dkd.manage.service.ITaskService;

/**
 * 工单 服务层处理
 *
 * @author dkd
 */
@Service
public class TaskServiceImpl implements ITaskService
{
    @Autowired
    private TaskMapper taskMapper;

    /** ★ 阶段 3 建明细要用 */
    @Autowired
    private TaskDetailMapper taskDetailMapper;

    /** ★ 阶段 3 查货道 + 回填 task_id 要用 */
    @Autowired
    private ChannelMapper channelMapper;

    @Override
    public List<Task> findAllTask()
    {
        return taskMapper.findAllTask();
    }

    @Override
    public Task findTaskById(Long taskId)
    {
        return taskMapper.findTaskById(taskId);
    }

    @Override
    public int add(Task task)
    {
        return taskMapper.add(task);
    }

    @Override
    public int update(Task task)
    {
        return taskMapper.update(task);
    }

    @Override
    public int delete(Long[] taskIds)
    {
        return taskMapper.delete(taskIds);
    }

    /**
     * ★ 阶段 3：手动为某台设备创建补货工单
     *
     * 五步：
     *   ① 查出该设备所有货道，收集出「库存 <= 阈值」的
     *   ② 一条都没有 → 抛异常（★ 在插任何数据之前判断）
     *   ③ 插 tb_task → 靠 useGeneratedKeys 回填拿到 taskId
     *   ④ 循环插 tb_task_detail（task_id 用第③步拿到的）
     *   ⑤ 把 tb_channel.task_id 回填成新工单 id
     *
     * ★ 必须加 @Transactional：
     *   工单 / 明细 / 回填这三件事是一个整体，任何一步失败都要全部回滚，
     *   否则会留下"有工单没明细"或"有明细没回填"的残废数据。
     */
    @Override
    @Transactional
    public int createSupplyTask(Long vmId, Long userId)
    {
        // ① 只查，不插 —— 收集需要补货的货道
        List<Channel> channels = channelMapper.selectChannelByVmId(vmId);
        List<Channel> needSupply = new ArrayList<>();
        for (Channel c : channels)
        {
            // 库存 <= 阈值 就要补货（Long 用 <= 会自动拆箱，没问题）
            if (c.getStock() <= c.getThreshold())
            {
                needSupply.add(c);
            }
        }

        // ② 没有需要补货的 → 直接抛，什么都不用做
        if (needSupply.isEmpty())
        {
            throw new ServiceException("该设备下没有需要补货的货道");
        }

        // ③ 插工单 —— ★★★ 这一句执行完之后，task.getTaskId() 才有值 ★★★
        Task task = new Task();
        task.setVmId(vmId);
        task.setCreateUserId(userId);
        task.setTaskType(DkdContants.TASK_TYPE_SUPPLY);      // 2L 补货工单
        task.setTaskStatus(DkdContants.TASK_STATUS_CREATE);  // 1L 创建(待处理)
        taskMapper.add(task);

        // ④ 插明细  ⑤ 回填货道的 task_id
        for (Channel c : needSupply)
        {
            TaskDetail detail = new TaskDetail();
            detail.setTaskId(task.getTaskId());                     // ★ 用第③步拿到的 id
            detail.setChannelId(c.getChannelId());
            detail.setExpectNum(c.getMaxCapacity() - c.getStock()); // 应补数量 = 满容量 - 当前库存
            detail.setRealNum(0L);                                  // 实补数量，运维员补完货再填
            taskDetailMapper.add(detail);

            channelMapper.updateChannelTaskId(c.getChannelId(), task.getTaskId());
        }

        return 1;
    }
}
