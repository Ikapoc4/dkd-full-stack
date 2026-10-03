package com.dkd.manage.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dkd.common.annotation.Log;
import com.dkd.common.core.controller.BaseController;
import com.dkd.common.core.domain.AjaxResult;
import com.dkd.common.core.page.TableDataInfo;
import com.dkd.common.enums.BusinessType;
import com.dkd.manage.domain.Channel;
import com.dkd.manage.service.IChannelService;

/**
 * 货道信息操作处理
 *
 * @author dkd
 */
@RestController
@RequestMapping("/manage/channel")
public class ChannelController extends BaseController
{
    @Autowired
    private IChannelService channelService;

    /**
     * 查询货道列表
     */
    @PreAuthorize("@ss.hasPermi('manage:channel:list')")
    @GetMapping("/channellist")
    public TableDataInfo list()
    {
        startPage();
        List<Channel> list = channelService.findAllChannel();
        return getDataTable(list);
    }

    /**
     * 根据货道Id获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:channel:query')")
    @GetMapping("/{channelId}")
    public AjaxResult getInfo(@PathVariable Long channelId)
    {
        return success(channelService.findChannelById(channelId));
    }

    /**
     * 新增货道
     */
    @PreAuthorize("@ss.hasPermi('manage:channel:add')")
    @Log(title = "货道管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Channel channel)
    {
        channel.setCreateBy(getUsername());
        return toAjax(channelService.add(channel));
    }

    /**
     * 修改货道
     */
    @PreAuthorize("@ss.hasPermi('manage:channel:edit')")
    @Log(title = "货道管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Channel channel)
    {
        channel.setUpdateBy(getUsername());
        return toAjax(channelService.update(channel));
    }

    /**
     * 删除货道
     */
    @PreAuthorize("@ss.hasPermi('manage:channel:remove')")
    @Log(title = "货道管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{channelIds}")
    public AjaxResult remove(@PathVariable Long[] channelIds)
    {
        return toAjax(channelService.delete(channelIds));
    }
}
