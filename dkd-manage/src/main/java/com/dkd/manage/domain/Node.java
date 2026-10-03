package com.dkd.manage.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.dkd.common.annotation.Excel;
import com.dkd.common.core.domain.BaseEntity;

// ★ 订正①：缺类注释。RuoYi 每个类都有一块 Javadoc，代码生成器也会生成，建议补上：
//   /**
//    * 点位表 tb_node
//    *
//    * @author dkd
//    */
public class Node extends BaseEntity {
    /** 序列化版本号：固定写 1L 就行，不用管 */
    private static final long serialVersionUID = 1L;

    // ★ 订正②：每个字段上面还该有一行 /** 点位Id */ 这样的注释。
    //   RuoYi 全项目都是这个格式（@Excel 上面先写 /** */），生成器也会生成。
    //   下面 6 个字段同样，建议一起补。
    @Excel(name = "点位序号", cellType = Excel.ColumnType.NUMERIC)
    private Long nodeId;

    @Excel(name = "点位名称")
    @NotBlank(message = "点位名称不能为空")
    @Size(min = 0, max = 64, message = "点位名称长度不能超过64个字符")
    private String nodeName;

    @Excel(name = "详细地址")
    @Size(min = 0, max = 255, message = "详细地址长度不能超过255个字符")
    private String address;

    // ★ 订正③：这里有两个连续空行，删一个。（regionName 后面、status 后面同样）
    private Long regionId;

    @Excel(name = "区域名称")
    @Size(min = 0, max = 64, message = "区域名称长度不能超过64个字符")
    private String regionName;


    private Long partnerId;

    @Excel(name = "状态", readConverterExp = "0=停用,1=启用")
    private Long status;


    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getRegionId() {
        return regionId;
    }

    // ★ 订正④：大括号风格不统一。上面都是 `) {`（行尾大括号），这里换行了。
    //   RuoYi 统一用行尾大括号，改成：public void setRegionId(Long regionId) {
    public void setRegionId(Long regionId)
    {
        this.regionId = regionId;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
    }

    public Long getStatus() {
        return status;
    }
    // ★ 订正⑤：这里少一个空行。其他每一对 getter/setter 之间都有空行，保持一致。
    public void setStatus(Long status) {
        this.status = status;
    }

    // ★ 订正⑥：`{` 换行了（同订正④）；另外下面 .append 的缩进比 RuoYi 多 4 个空格，
    //   RuoYi 是 12 个空格（3 级），你写了 16 个。这个纯格式，不改也能跑。
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("nodeId", getNodeId())
                .append("nodeName", getNodeName())
                .append("address", getAddress())
                .append("regionId", getRegionId())
                .append("regionName", getRegionName())
                .append("partnerId", getPartnerId())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }

    // ★ 订正⑦：结尾多了一个空行，删掉。

}
