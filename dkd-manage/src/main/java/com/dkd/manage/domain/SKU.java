package com.dkd.manage.domain;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.dkd.common.annotation.Excel;
import com.dkd.common.core.domain.BaseEntity;

/**
 * 商品对象 tb_sku
 *
 * @author dkd
 */
public class SKU extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 商品Id（自增主键，新增时不用传，所以不加校验注解） */
    @Excel(name = "商品Id")
    private Long skuId;

    /** 商品名称 */
    @Excel(name = "商品名称")
    @NotBlank(message = "商品名称不能为空")
    @Size(min = 0, max = 64, message = "商品名称长度不能超过64个字符")
    private String skuName;

    /** 品牌 */
    @Excel(name = "品牌")
    @Size(min = 0, max = 64, message = "品牌长度不能超过64个字符")
    private String brandName;

    /** 单位(瓶/袋/盒) */
    @Excel(name = "单位")
    @Size(min = 0, max = 16, message = "单位长度不能超过16个字符")
    private String unit;

    /** 售价（钱必须用 BigDecimal，不能用 double） */
    @Excel(name = "售价")
    private BigDecimal price;

    /** 商品图片 */
    @Excel(name = "商品图片")
    @Size(min = 0, max = 255, message = "商品图片长度不能超过255个字符")
    private String skuImage;

    /** 状态(0正常 1下架)，数据库有默认值0，所以不加校验注解 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=下架")
    private Long status;

    // ==========================================================
    //  getter / setter
    // ==========================================================

    public Long getSkuId()
    {
        return skuId;
    }

    public void setSkuId(Long skuId)
    {
        this.skuId = skuId;
    }

    public String getSkuName()
    {
        return skuName;
    }

    public void setSkuName(String skuName)
    {
        this.skuName = skuName;
    }

    public String getBrandName()
    {
        return brandName;
    }

    public void setBrandName(String brandName)
    {
        this.brandName = brandName;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public String getSkuImage()
    {
        return skuImage;
    }

    public void setSkuImage(String skuImage)
    {
        this.skuImage = skuImage;
    }

    public Long getStatus()
    {
        return status;
    }

    public void setStatus(Long status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("skuId", getSkuId())
            .append("skuName", getSkuName())
            .append("brandName", getBrandName())
            .append("unit", getUnit())
            .append("price", getPrice())
            .append("skuImage", getSkuImage())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
