package com.dkd.manage.task;

import com.dkd.manage.mapper.ChannelMapper;
import com.dkd.manage.service.ITaskService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SupplyTaskJob {
    private static final Logger log = LoggerFactory.getLogger(SupplyTaskJob.class);

    private static final Long SYSTEM_USER_ID = 0L;

    @Autowired
    private ChannelMapper channelMapper;
    @Autowired
    private ITaskService taskService;

    @XxlJob("supplyTaskJob")
    public void execute() {
        autoSupply();
    }

    public void autoSupply() {
        long start = System.currentTimeMillis();

        List<Long> vmIds = channelMapper.selectVmIdsWithLowStock();
        log.info("=== 自动补货任务开始， 共 {} 台设备需要补货 ===", vmIds.size());

        if (vmIds.isEmpty()) {
            log.info("=== 自动补货任务结束， 无设备需要补货， 耗时 {} ms ===", System.currentTimeMillis() - start);
            return;
        }

        int ok = 0;
        int fail = 0;

        for (Long vmId : vmIds) {
            try {
                taskService.createSupplyTask(vmId, SYSTEM_USER_ID);
                ok++;
                log.info("设备 {} 建单成功", vmId);
            } catch (Exception e) {
                fail++;
                log.error("设备 {} 自动建单失败", vmId, e);
            }
        }

        log.info("=== 自动补货任务结束， 成功 {} 台， 失败{} 台，耗时 {} ms", ok, fail, System.currentTimeMillis() - start);

    }

}
