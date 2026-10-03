package com.dkd.manage.service.Impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dkd.common.exception.ServiceException;
import com.dkd.manage.domain.Channel;
import com.dkd.manage.mapper.ChannelMapper;
import com.dkd.manage.service.IChannelService;

/**
 * 货道 服务层处理
 *
 * @author dkd
 */
@Service
public class ChannelServiceImpl implements IChannelService
{
    @Autowired
    private ChannelMapper channelMapper;

    @Override
    public List<Channel> findAllChannel()
    {
        return channelMapper.findAllChannel();
    }

    @Override
    public Channel findChannelById(Long channelId)
    {
        return channelMapper.findChannelById(channelId);
    }

    /**
     * 校验「设备Id + 货道编号」是否唯一
     *
     * 对应数据库的复合唯一键：UNIQUE KEY uk_vm_channel (vm_id, channel_code)
     * 意思是：同一台设备里，货道编号不能重复（不同设备之间可以重名，比如都有 1-1 号货道）
     */
    @Override
    public boolean checkChannelUnique(Channel channel)
    {
        // 新增时 channel.getChannelId() 是 null（自增主键前端不传），用 -1L 顶替
        Long channelId = (channel.getChannelId() == null) ? -1L : channel.getChannelId();

        // 去数据库查：同一台设备下有没有同样的货道编号
        Channel info = channelMapper.selectChannelByVmIdAndCode(
                channel.getVmId(), channel.getChannelCode());

        // ★ 必须用上面算好的 channelId，不能再用 channel.getChannelId()
        //   否则新增时它是 null → 空指针
        if (info != null && info.getChannelId().longValue() != channelId.longValue())
        {
            return false;   // 被"别人"占了 —— NOT_UNIQUE
        }
        return true;        // 没被占，或占的就是我自己 —— UNIQUE
    }

    @Override
    public int add(Channel channel)
    {
        if (!checkChannelUnique(channel))
        {
            throw new ServiceException("新增货道'" + channel.getChannelCode()
                    + "'失败，该设备下货道编号已存在");
        }
        return channelMapper.add(channel);
    }

    @Override
    public int update(Channel channel)
    {
        if (!checkChannelUnique(channel))
        {
            throw new ServiceException("修改货道'" + channel.getChannelCode()
                    + "'失败，该设备下货道编号已存在");
        }
        return channelMapper.update(channel);
    }

    @Override
    public int delete(Long[] channelIds)
    {
        return channelMapper.delete(channelIds);
    }
}
