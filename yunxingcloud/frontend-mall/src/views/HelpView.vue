<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from '@/locales'
const { t } = useI18n()

const faqs = ref([
  { q: t('help.q1'), a: '选择商品 → 加入购物车 → 去结算 → 填写地址 → 提交订单 → 完成支付。', open: false },
  { q: t('help.q2'), a: '支持微信支付和支付宝，安全便捷。', open: false },
  { q: t('help.q3'), a: '全国包邮（偏远地区除外），通常 1-3 个工作日送达。', open: false },
  { q: t('help.q4'), a: '在"我的订单"中选择需要售后的订单，点击"申请售后"，选择退货退款即可。7天无理由退货。', open: false },
  { q: t('help.q5'), a: '在订单详情页可查看物流信息，也可在"物流追踪"页面输入快递单号查询。', open: false },
  { q: t('help.q6'), a: '在"优惠券中心"领取优惠券后，结算时输入券码即可抵扣。', open: false },
  { q: t('help.q7'), a: '积分可在结算时抵扣现金（100积分=1元），也可兑换优惠券。', open: false },
  { q: t('help.q8'), a: '邮箱: support@yunxingcloud.com，工作时间: 9:00-21:00。', open: false },
])

function toggle(i: number) { faqs.value[i].open = !faqs.value[i].open }
</script>

<template>
  <div class="help-page">
    <h2 class="help-title">❓ 帮助中心</h2>
    <div v-for="(faq, i) in faqs" :key="i" class="faq-item">
      <div class="faq-q" @click="toggle(i)">
        <span>{{ faq.q }}</span>
        <span class="faq-arrow" :class="{ open: faq.open }">+</span>
      </div>
      <div v-if="faq.open" class="faq-a">{{ faq.a }}</div>
    </div>
    <div class="faq-footer">
      <p class="faq-footer-title">📧 没有找到答案？</p>
      <p class="faq-footer-desc">联系客服: support@yunxingcloud.com (9:00-21:00)</p>
    </div>
  </div>
</template>

<style scoped>
.help-page { max-width: 700px; margin: 0 auto; }
.help-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-xl); }

.faq-item { background: var(--bg-white); border-radius: var(--radius-md); margin-bottom: 10px; box-shadow: var(--shadow-sm); overflow: hidden; }
.faq-q {
  padding: 16px 20px; cursor: pointer; display: flex;
  justify-content: space-between; align-items: center;
  transition: background var(--transition-fast); font-weight: 600; font-size: 15px;
}
.faq-q:hover { background: var(--bg-hover); }
.faq-arrow { font-size: 18px; color: var(--text-tertiary); transition: transform var(--transition); }
.faq-arrow.open { transform: rotate(45deg); }
.faq-a {
  padding: 0 20px 16px; color: var(--text-secondary); font-size: var(--font-md);
  line-height: 1.8; border-top: 1px solid var(--border-light); margin: 0 20px;
}
.faq-footer {
  background: var(--bg-white); border-radius: var(--radius-md); padding: 24px;
  margin-top: var(--space-xl); box-shadow: var(--shadow-sm); text-align: center;
}
.faq-footer-title { font-size: var(--font-lg); font-weight: 600; margin-bottom: var(--space-sm); }
.faq-footer-desc { color: var(--text-secondary); font-size: var(--font-md); }
</style>
