package com.dkd.manage.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;

/**
 * XXL-Job 执行器配置
 *
 * 作用：把本项目的「执行器」这一半启动起来。
 *
 * 执行器启动后会做两件事：
 *   ① 起一个 Netty 服务，监听 xxl.job.executor.port（默认 9999）—— 等调度中心来调用
 *   ② 每隔 30 秒向调度中心（xxl.job.admin.addresses）注册一次心跳
 *      —— 调度中心才知道"有这么一台执行器在线"，才能把任务派给它
 *
 * ★ 这个类的内容是 XXL-Job 官方的标准写法，基本不用改，照着抄即可。
 *
 * @author dkd
 */
@Configuration
public class XxlJobConfig
{
    private Logger logger = LoggerFactory.getLogger(XxlJobConfig.class);

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    @Value("${xxl.job.executor.appname}")
    private String appname;

    @Value("${xxl.job.executor.address}")
    private String address;

    @Value("${xxl.job.executor.ip}")
    private String ip;

    @Value("${xxl.job.executor.port}")
    private int port;

    @Value("${xxl.job.executor.logpath}")
    private String logPath;

    @Value("${xxl.job.executor.logretentiondays}")
    private int logRetentionDays;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor()
    {
        logger.info(">>>>>>>>>>> xxl-job 执行器初始化开始");
        logger.info(">>>>>>>>>>> 调度中心地址: {}", adminAddresses);
        logger.info(">>>>>>>>>>> 执行器名称: {}", appname);
        logger.info(">>>>>>>>>>> 执行器端口: {}", port);

        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appname);
        xxlJobSpringExecutor.setAddress(address);
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);

        logger.info(">>>>>>>>>>> xxl-job 执行器初始化完成");
        return xxlJobSpringExecutor;
    }
}
