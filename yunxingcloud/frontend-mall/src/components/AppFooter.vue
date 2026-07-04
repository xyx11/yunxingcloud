<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
function goTo(path: string) { router.push(path) }

const guarantees = [
  { icon: '✅', title: '品质保证', desc: '正品保障 假一赔十' },
  { icon: '🚚', title: '极速配送', desc: '全国包邮 1-3日达' },
  { icon: '🔄', title: '7天退换', desc: '无忧退货 极速退款' },
  { icon: '🛡️', title: '售后无忧', desc: '专属客服 随时响应' },
]

const footerCols = [
  {
    title: '购物指南',
    items: [
      { label: '购物流程', path: '/help' },
      { label: '会员介绍', path: '/help' },
      { label: '常见问题', path: '/help' },
    ],
  },
  {
    title: '配送方式',
    items: [
      { label: '配送范围', path: '/help' },
      { label: '配送时效', path: '/help' },
      { label: '验货签收', path: '/help' },
    ],
  },
  {
    title: '售后服务',
    items: [
      { label: '退货政策', path: '/after-sale' },
      { label: '退款说明', path: '/help' },
      { label: '联系客服', path: '/help' },
    ],
  },
  {
    title: '关于我们',
    items: [
      { label: '公司介绍', path: '/help' },
      { label: '联系我们', path: '/help' },
      { label: '加入我们', path: '/help' },
    ],
  },
]
</script>

<template>
  <!-- Service Guarantees -->
  <section class="guarantees">
    <div class="guarantees-inner">
      <div v-for="g in guarantees" :key="g.title" class="guarantee-item">
        <span class="guarantee-icon">{{ g.icon }}</span>
        <div>
          <div class="guarantee-title">{{ g.title }}</div>
          <div class="guarantee-desc">{{ g.desc }}</div>
        </div>
      </div>
    </div>
  </section>

  <!-- Footer -->
  <footer class="footer">
    <div class="footer-grid">
      <div v-for="col in footerCols" :key="col.title" class="footer-col">
        <h4 class="footer-col-title">{{ col.title }}</h4>
        <p v-for="item in col.items" :key="item.label" class="footer-link" @click="goTo(item.path)">{{ item.label }}</p>
      </div>
    </div>
    <div class="footer-bottom">
      &copy; {{ new Date().getFullYear() }} YXCLOUD 商城 v2.4.0
    </div>
  </footer>
</template>

<style scoped>
.guarantees {
  background: var(--bg-white);
  border-top: 1px solid var(--border-light);
  padding: var(--space-xxl) var(--space-xl);
}
.guarantees-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  display: flex;
  justify-content: space-around;
  gap: var(--space-xl);
}
.guarantee-item {
  text-align: center;
  flex: 1;
}
.guarantee-icon {
  font-size: 32px;
  margin-bottom: var(--space-sm);
  display: block;
}
.guarantee-title {
  font-weight: 700;
  font-size: var(--font-md);
  color: var(--text-primary);
  margin-bottom: var(--space-xs);
}
.guarantee-desc {
  font-size: var(--font-xs);
  color: var(--text-tertiary);
}

.footer {
  background: #333;
  color: #999;
  padding: var(--space-xxxl) var(--space-xl) var(--space-xl);
}
.footer-grid {
  max-width: var(--max-width);
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-xxl);
  font-size: var(--font-base);
}
.footer-col-title {
  color: #fff;
  margin-bottom: var(--space-md);
  font-size: var(--font-md);
}
.footer-link {
  margin: 6px 0;
  cursor: pointer;
  transition: color var(--transition-fast);
}
.footer-link:hover { color: #fff; }

.footer-bottom {
  max-width: var(--max-width);
  margin: 0 auto;
  text-align: center;
  padding-top: var(--space-xl);
  margin-top: var(--space-xl);
  border-top: 1px solid #444;
  font-size: var(--font-sm);
}

@media (max-width: 768px) {
  .guarantees, .footer { display: none; }
}
</style>
