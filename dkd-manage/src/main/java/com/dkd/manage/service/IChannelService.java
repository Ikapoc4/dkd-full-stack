package com.dkd.manage.service;

import java.util.List;

import com.dkd.manage.domain.Channel;

/**
 * 货道 服务层
 *
 * @author dkd
 */
public interface IChannelService
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
     * 校验「设备Id + 货道编号」是否唯一
     *
     * @return true=唯一(能用)  false=已存在(不能用)
     */
    public boolean checkChannelUnique(Channel channel);

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
}
