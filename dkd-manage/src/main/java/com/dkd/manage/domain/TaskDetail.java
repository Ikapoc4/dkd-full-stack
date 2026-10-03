package com.dkd.manage.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.dkd.common.annotation.Excel;
import com.dkd.common.core.domain.BaseEntity;

/**
 * 工单明细对象 tb_task_detail
 *
 * ★ 注意：这张表【没有】 create_by / create_time / update_by / update_time / remark 这 5 个公共列。
 *   所以：
 *     1. XML 里不会出现这 5 列（写了会报 Unknown column）
 *     2. Service 的 add/update 里【不要】调 setCreateBy() / setUpdateBy()
 *     3. 继承 BaseEntity 只是为了跟其他实体统一，多出来的字段不会被映射
 *
 * @author dkd
 */
public class TaskDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细Id（自增主键） */
    @Excel(name = "明细Id")
    private Long detailId;

    /** 工单Id */
    @Excel(name = "工单Id")
    private Long taskId;

    /** 货道Id */
    @Excel(name = "货道Id")
    private Long channelId;

    /** 应补数量（= 货道的 max_capacity - stock） */
    @Excel(name = "应补数量")
    private Long expectNum;

    /** 实补数量（运维员补完货后填） */
    @Excel(name = "实补数量")
    private Long realNum;

    // ==========================================================
    //  getter / setter
    // ==========================================================

    public Long getDetailId()
    {
        return detailId;
    }

    public void setDetailId(Long detailId)
    {
        this.detailId = detailId;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public Long getChannelId()
    {
        return channelId;
    }

    public void setChannelId(Long channelId)
    {
        this.channelId = channelId;
    }

    public Long getExpectNum()
    {
        return expectNum;
    }

    public void setExpectNum(Long expectNum)
    {
        this.expectNum = expectNum;
    }

    public Long getRealNum()
    {
        return realNum;
    }

    public void setRealNum(Long realNum)
    {
        this.realNum = realNum;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("detailId", getDetailId())
            .append("taskId", getTaskId())
            .append("channelId", getChannelId())
            .append("expectNum", getExpectNum())
            .append("realNum", getRealNum())
            .toString();
    }
}
