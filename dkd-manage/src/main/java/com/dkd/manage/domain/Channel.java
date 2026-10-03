package com.dkd.manage.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.dkd.common.annotation.Excel;
import com.dkd.common.core.domain.BaseEntity;

/**
 * 货道对象 tb_channel
 *
 * @author dkd
 */
public class Channel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 货道Id（自增主键，新增时前端不传，不加校验注解） */
    @Excel(name = "货道Id")
    private Long channelId;

    /** 设备Id（必填 —— 货道必须属于某台设备） */
    @Excel(name = "设备Id")
    @NotNull(message = "所属设备不能为空")
    private Long vmId;

    /** 商品Id（可以为空 —— 空的表示这个货道还没放商品） */
    @Excel(name = "商品Id")
    private Long skuId;

    /** 货道编号（如 1-2 表示第1行第2列） */
    @Excel(name = "货道编号")
    @NotBlank(message = "货道编号不能为空")
    @Size(min = 0, max = 20, message = "货道编号长度不能超过20个字符")
    private String channelCode;

    /** 最大容量 */
    @Excel(name = "最大容量")
    private Long maxCapacity;

    /** ★★★ 当前库存 —— 定时任务扫的就是这个字段 ★★★ */
    @Excel(name = "当前库存")
    private Long stock;

    /** 补货阈值（stock <= threshold 就触发自动补货） */
    @Excel(name = "补货阈值")
    private Long threshold;

    /** 当前未完成补货工单Id（幂等用，后台维护，前端不用传，所以不导出） */
    private Long taskId;

    // ==========================================================
    //  getter / setter
    // ==========================================================

    public Long getChannelId()
    {
        return channelId;
    }

    public void setChannelId(Long channelId)
    {
        this.channelId = channelId;
    }

    public Long getVmId()
    {
        return vmId;
    }

    public void setVmId(Long vmId)
    {
        this.vmId = vmId;
    }

    public Long getSkuId()
    {
        return skuId;
    }

    public void setSkuId(Long skuId)
    {
        this.skuId = skuId;
    }

    public String getChannelCode()
    {
        return channelCode;
    }

    public void setChannelCode(String channelCode)
    {
        this.channelCode = channelCode;
    }

    public Long getMaxCapacity()
    {
        return maxCapacity;
    }

    public void setMaxCapacity(Long maxCapacity)
    {
        this.maxCapacity = maxCapacity;
    }

    public Long getStock()
    {
        return stock;
    }

    public void setStock(Long stock)
    {
        this.stock = stock;
    }

    public Long getThreshold()
    {
        return threshold;
    }

    public void setThreshold(Long threshold)
    {
        this.threshold = threshold;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("channelId", getChannelId())
            .append("vmId", getVmId())
            .append("skuId", getSkuId())
            .append("channelCode", getChannelCode())
            .append("maxCapacity", getMaxCapacity())
            .append("stock", getStock())
            .append("threshold", getThreshold())
            .append("taskId", getTaskId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
