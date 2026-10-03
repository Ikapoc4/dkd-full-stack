package com.dkd.manage.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dkd.manage.mapper.ChannelMapper;
import com.dkd.manage.service.ITaskService;
import com.xxl.job.core.handler.annotation.XxlJob;

/**
 * 自动补货工单调度
 *
 * ★★★ 设计要点：业务逻辑和调度器解耦 ★★★
 *
 *   execute()    只负责「什么时候调」—— 唯一跟 XXL-Job 耦合的地方
 *   autoSupply() 只负责「调的时候干什么」—— 纯 Java，不依赖任何调度器
 *
 *   好处：以后换回 @Scheduled 或换成别的调度器，autoSupply() 一个字都不用改。
 *
 * @author dkd
 */
@Component
public class SupplyTaskJob
{
    private static final Logger log = LoggerFactory.getLogger(SupplyTaskJob.class);

    /**
     * 系统自动创建工单时的创建人Id
     *
     * 约定：tb_task.create_user_id == 0 表示"系统建的"，其他值是真实用户id。
     * 这样页面上一眼能分出这张单是人点的还是系统建的。
     */
    private static final Long SYSTEM_USER_ID = 0L;

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private ITaskService taskService;

    /**
     * ★ XXL-Job 任务入口
     *
     * 「supplyTaskJob」这个名字，要和调度中心页面上新增任务时填的
     * 「JobHandler」一模一样，否则调度中心找不到这个方法。
     */
    @XxlJob("supplyTaskJob")
    public void execute()
    {
        autoSupply();
    }

    /**
     * ★ 自动补货：扫出所有缺货设备，逐台建补货工单
     *
     * 逻辑：
     *   ① 扫出「有货道需要补货」的设备Id
     *   ② 一台都没有 → 直接结束
     *   ③ 逐台调 taskService.createSupplyTask() 建单
     *      ★ 每台单独 try/catch —— 一台失败不能拖垮整个任务
     */
    public void autoSupply()
    {
        long start = System.currentTimeMillis();

        // ① 扫出需要补货的设备
        List<Long> vmIds = channelMapper.selectVmIdsWithLowStock();
        log.info("=== 自动补货任务开始，共 {} 台设备需要补货 ===", vmIds.size());

        if (vmIds.isEmpty())
        {
            log.info("=== 自动补货任务结束，无需补货，耗时 {} ms ===",
                    System.currentTimeMillis() - start);
            return;
        }

        // ② 逐台建单
        int ok = 0;
        int fail = 0;
        for (Long vmId : vmIds)
        {
            try
            {
                taskService.createSupplyTask(vmId, SYSTEM_USER_ID);
                ok++;
                log.info("设备 {} 建单成功", vmId);
            }
            catch (Exception e)
            {
                // ★ 关键：一台设备失败，后面的继续跑。
                //   不 try/catch 的话，第 3 台抛异常 → 后面全部都跑不到。
                fail++;
                log.error("设备 {} 自动建单失败", vmId, e);
            }
        }

        log.info("=== 自动补货任务结束，成功 {} 台，失败 {} 台，耗时 {} ms ===",
                ok, fail, System.currentTimeMillis() - start);
    }
}
