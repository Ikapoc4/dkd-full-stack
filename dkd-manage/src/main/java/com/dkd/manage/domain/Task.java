package com.dkd.manage.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.dkd.common.annotation.Excel;
import com.dkd.common.core.domain.BaseEntity;

/**
 * 工单对象 tb_task
 *
 * 注意：这个实体一个校验注解都不加 ——
 *      工单的字段全是后台代码设的（taskType/taskStatus/createUserId…），
 *      没有一个是"用户在表单上填的"，所以不需要校验。
 *
 * @author dkd
 */
public class Task extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 工单Id（自增主键） */
    @Excel(name = "工单Id")
    private Long taskId;

    /** 工单类型(1投放 2补货 3维修 4撤机)，取值见 DkdContants.TASK_TYPE_* */
    @Excel(name = "工单类型", readConverterExp = "1=投放,2=补货,3=维修,4=撤机")
    private Long taskType;

    /** 工单状态(1创建 2进行 3取消 4完成)，取值见 DkdContants.TASK_STATUS_* */
    @Excel(name = "工单状态", readConverterExp = "1=创建,2=进行,3=取消,4=完成")
    private Long taskStatus;

    /** 点位Id */
    @Excel(name = "点位Id")
    private Long nodeId;

    /** 设备Id */
    @Excel(name = "设备Id")
    private Long vmId;

    /** 创建人Id（★ 0 = 系统自动创建，其他值 = 真实用户id） */
    @Excel(name = "创建人Id")
    private Long createUserId;

    /** 指派给的运维员Id */
    @Excel(name = "指派给")
    private Long assignUserId;

    /** 工单描述 */
    @Excel(name = "工单描述")
    private String taskDesc;

    // ==========================================================
    //  getter / setter
    // ==========================================================

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public Long getTaskType()
    {
        return taskType;
    }

    public void setTaskType(Long taskType)
    {
        this.taskType = taskType;
    }

    public Long getTaskStatus()
    {
        return taskStatus;
    }

    public void setTaskStatus(Long taskStatus)
    {
        this.taskStatus = taskStatus;
    }

    public Long getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }

    public Long getVmId()
    {
        return vmId;
    }

    public void setVmId(Long vmId)
    {
        this.vmId = vmId;
    }

    public Long getCreateUserId()
    {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId)
    {
        this.createUserId = createUserId;
    }

    public Long getAssignUserId()
    {
        return assignUserId;
    }

    public void setAssignUserId(Long assignUserId)
    {
        this.assignUserId = assignUserId;
    }

    public String getTaskDesc()
    {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc)
    {
        this.taskDesc = taskDesc;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("taskType", getTaskType())
            .append("taskStatus", getTaskStatus())
            .append("nodeId", getNodeId())
            .append("vmId", getVmId())
            .append("createUserId", getCreateUserId())
            .append("assignUserId", getAssignUserId())
            .append("taskDesc", getTaskDesc())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
