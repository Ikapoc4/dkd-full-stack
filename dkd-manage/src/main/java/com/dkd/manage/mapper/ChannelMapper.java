package com.dkd.manage.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dkd.manage.domain.Channel;

/**
 * 货道 数据层
 *
 * @author dkd
 */
public interface ChannelMapper
{
    /**
     * 查询货道列表
     */
    public List<Channel> findAllChannel();

    /**
     * 根据货道Id查询货道
     */
    public Channel findChannelById(Long channelId);

    /**
     * ★ 唯一性校验用：按「设备Id + 货道编号」查一条
     *
     * 注意：这个方法有 2 个参数，必须给每个参数加 @Param，
     *       否则 XML 里 #{vmId} / #{channelCode} 取不到值，
     *       MyBatis 只会把它们叫成 arg0 / arg1。
     */
    public Channel selectChannelByVmIdAndCode(@Param("vmId") Long vmId,
                                             @Param("channelCode") String channelCode);

    /**
     * 新增货道
     */
    public int add(Channel channel);

    /**
     * 修改货道
     */
    public int update(Channel channel);

    /**
     * 批量删除货道
     */
    public int delete(Long[] channelIds);

    /**
     * 根据设备Id 检索通道
     * @param vmId
     * @return
     */
    public List<Channel> selectChannelByVmId(Long vmId);

    /**
     * ★ 阶段 3 建单用：把货道的 task_id 回填成新建的工单Id
     *
     * 2 个参数，必须加 @Param
     */
    public int updateChannelTaskId(@Param("channelId") Long channelId,
                                   @Param("taskId") Long taskId);

    /**
     * 找出所有的低STOCK的channel对应的VmId
     * @return
     */
    public List<Long> selectVmIdsWithLowStock();
}
