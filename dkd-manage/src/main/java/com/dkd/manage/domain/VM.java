package com.dkd.manage.domain;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.dkd.common.annotation.Excel;
import com.dkd.common.core.domain.BaseEntity;

/**
 * 设备对象 tb_vm
 *
 * @author dkd
 */
public class VM extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 设备Id（自增主键，新增时前端不传，所以不加校验注解） */
    @Excel(name = "设备Id")
    private Long vmId;

    /** 设备编号（★ 唯一键，不能重复） */
    @Excel(name = "设备编号")
    @NotBlank(message = "设备编号不能为空")
    @Size(min = 0, max = 32, message = "设备编号长度不能超过32个字符")
    private String vmCode;

    /** 所属点位Id */
    @Excel(name = "所属点位Id")
    private Long nodeId;

    /** 状态(0未投放 1运营 3撤机)，数据库有默认值0，所以不加校验注解 */
    @Excel(name = "状态", readConverterExp = "0=未投放,1=运营,3=撤机")
    private Long status;

    /** 上次补货时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "上次补货时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastSupplyTime;

    // ==========================================================
    //  getter / setter
    // ==========================================================

    public Long getVmId()
    {
        return vmId;
    }

    public void setVmId(Long vmId)
    {
        this.vmId = vmId;
    }

    public String getVmCode()
    {
        return vmCode;
    }

    public void setVmCode(String vmCode)
    {
        this.vmCode = vmCode;
    }

    public Long getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }

    public Long getStatus()
    {
        return status;
    }

    public void setStatus(Long status)
    {
        this.status = status;
    }

    public Date getLastSupplyTime()
    {
        return lastSupplyTime;
    }

    public void setLastSupplyTime(Date lastSupplyTime)
    {
        this.lastSupplyTime = lastSupplyTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("vmId", getVmId())
            .append("vmCode", getVmCode())
            .append("nodeId", getNodeId())
            .append("status", getStatus())
            .append("lastSupplyTime", getLastSupplyTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
