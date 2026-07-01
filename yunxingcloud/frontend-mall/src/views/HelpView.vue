<script setup lang="ts">
import { ref } from 'vue'

const faqs = ref([
  { q: '如何下单？', a: '选择商品 → 加入购物车 → 去结算 → 填写地址 → 提交订单 → 完成支付。', open: false },
  { q: '支持哪些支付方式？', a: '支持微信支付和支付宝，安全便捷。', open: false },
  { q: '配送范围和时效？', a: '全国包邮（偏远地区除外），通常 1-3 个工作日送达。', open: false },
  { q: '如何退货？', a: '在"我的订单"中选择需要售后的订单，点击"申请售后"，选择退货退款即可。7天无理由退货。', open: false },
  { q: '如何查看物流？', a: '在订单详情页可查看物流信息，也可在"物流追踪"页面输入快递单号查询。', open: false },
  { q: '优惠券怎么使用？', a: '在"优惠券中心"领取优惠券后，结算时输入券码即可抵扣。', open: false },
  { q: '积分有什么用？', a: '积分可在结算时抵扣现金（100积分=1元），也可兑换优惠券。', open: false },
  { q: '如何联系客服？', a: '邮箱: support@yunxingcloud.com，工作时间: 9:00-21:00。', open: false },
])

function toggle(i: number) { faqs.value[i].open = !faqs.value[i].open }
</script>

<template>
  <div style="max-width:700px;margin:0 auto">
    <h2 style="font-size:22px;font-weight:700;margin-bottom:24px">❓ 帮助中心</h2>
    <div v-for="(faq, i) in faqs" :key="i" style="background:#fff;border-radius:10px;margin-bottom:10px;box-shadow:0 1px 4px rgba(0,0,0,.04);overflow:hidden">
      <div @click="toggle(i)" style="padding:16px 20px;cursor:pointer;display:flex;justify-content:space-between;align-items:center;transition:background .15s"
           @mouseenter="(e:any) => e.target.style.background='#fafafa'" @mouseleave="(e:any) => e.target.style.background='#fff'">
        <span style="font-weight:600;font-size:15px">{{ faq.q }}</span>
        <span style="font-size:18px;color:#999;transition:transform .3s" :style="{transform:faq.open?'rotate(45deg)':''}">+</span>
      </div>
      <div v-if="faq.open" style="padding:0 20px 16px;color:#666;font-size:14px;line-height:1.8;border-top:1px solid #f5f5f5;margin:0 20px">{{ faq.a }}</div>
    </div>
    <div style="background:#fff;border-radius:10px;padding:24px;margin-top:24px;box-shadow:0 1px 4px rgba(0,0,0,.04);text-align:center">
      <p style="font-size:16px;font-weight:600;margin-bottom:8px">📧 没有找到答案？</p>
      <p style="color:#999;font-size:13px">联系客服: support@yunxingcloud.com</p>
      <p style="color:#999;font-size:13px">工作时间: 每天 9:00 - 21:00</p>
    </div>
  </div>
</template>