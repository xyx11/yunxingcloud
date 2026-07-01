package com.yunxingcloud.order.entity;

import jakarta.persistence.*;

/** 商品-标签关联 */
@Entity @Table(name = "product_tag_relation")
public class ProductTagRelation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "tag_id", nullable = false) private Long tagId;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; } public void setProductId(Long v) { productId = v; }
    public Long getTagId() { return tagId; } public void setTagId(Long v) { tagId = v; }
}