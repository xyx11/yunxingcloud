package com.yunxingcloud.order.entity;

import jakarta.persistence.*;

/** 商品标签 */
@Entity @Table(name = "product_tag")
public class ProductTag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 50) private String name;     // 标签名: 热卖/新品/限时/包邮
    @Column(length = 20) private String color; // 标签颜色
    @Column(name = "sort_order") private Integer sortOrder = 0;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getColor() { return color; } public void setColor(String v) { color = v; }
    public Integer getSortOrder() { return sortOrder; } public void setSortOrder(Integer v) { sortOrder = v; }
}