<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from '@/locales'
import request from '@/api/request'

const router = useRouter()
const { t } = useI18n()
const subscribeEmail = ref('')
const subscribeDone = ref(false)
const subscribing = ref(false)

async function subscribe() {
  if (!subscribeEmail.value || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(subscribeEmail.value)) return
  subscribing.value = true
  try { await request.post('/newsletter/subscribe', { email: subscribeEmail.value }); subscribeDone.value = true } catch {}
  finally { subscribing.value = false }
}

function goTo(path: string) { router.push(path) }

const guarantees = [
  { icon: '✅', key: 'footer.promise1', descKey: 'footer.promise1Desc' },
  { icon: '🚚', key: 'footer.promise2', descKey: 'footer.promise2Desc' },
  { icon: '🔄', key: 'footer.promise3', descKey: 'footer.promise3Desc' },
  { icon: '🛡️', key: 'footer.promise4', descKey: 'footer.promise4Desc' },
]

const footerCols = [
  {
    key: 'footer.shopGuide',
    items: [
      { key: 'footer.shopFlow', path: '/help' },
      { key: 'footer.memberIntro', path: '/help' },
      { key: 'footer.faq', path: '/help' },
    ],
  },
  {
    key: 'footer.delivery',
    items: [
      { key: 'footer.deliveryRange', path: '/help' },
      { key: 'footer.deliveryTime', path: '/help' },
      { key: 'footer.inspectSign', path: '/help' },
    ],
  },
  {
    key: 'footer.service',
    items: [
      { key: 'footer.returnPolicy', path: '/after-sale' },
      { key: 'footer.refundInfo', path: '/help' },
      { key: 'footer.contactUs', path: '/help' },
    ],
  },
  {
    key: 'footer.aboutUs',
    items: [
      { key: 'footer.companyIntro', path: '/help' },
      { key: 'footer.contact', path: '/help' },
      { key: 'footer.joinUs', path: '/help' },
    ],
  },
]
</script>

<template>
  <!-- Service Guarantees -->
  <section class="guarantees">
    <div class="guarantees-inner">
      <div v-for="g in guarantees" :key="g.key" class="guarantee-item">
        <span class="guarantee-icon">{{ g.icon }}</span>
        <div>
          <div class="guarantee-title">{{ t(g.key) }}</div>
          <div class="guarantee-desc">{{ t(g.descKey) }}</div>
        </div>
      </div>
    </div>
  </section>

  <!-- Footer -->
  <footer class="footer">
    <div class="footer-grid">
      <div v-for="col in footerCols" :key="col.key" class="footer-col">
        <h4 class="footer-col-title">{{ t(col.key) }}</h4>
        <p v-for="item in col.items" :key="item.key" class="footer-link" @click="goTo(item.path)">{{ t(item.key) }}</p>
      </div>
    </div>
    <!-- Newsletter -->
    <div class="footer-newsletter">
      <div class="nl-inner">
        <div class="nl-text">
          <span class="nl-title">📧 {{ t('footer.newsletterTitle') }}</span>
          <span class="nl-desc">{{ t('footer.newsletterDesc') }}</span>
        </div>
        <div class="nl-form">
          <template v-if="subscribeDone">
            <span class="nl-success">✅ {{ t('footer.newsletterSuccess') }}</span>
          </template>
          <template v-else>
            <input v-model="subscribeEmail" type="email" :placeholder="t('footer.newsletterPlaceholder')" class="nl-input" @keyup.enter="subscribe" />
            <button class="nl-btn" :disabled="subscribing" @click="subscribe">{{ subscribing ? t('footer.newsletterSubmitting') : t('footer.newsletterBtn') }}</button>
          </template>
        </div>
      </div>
    </div>

    <div class="footer-bottom">
      <div class="footer-social">
        <span class="social-icon">📱</span>
        <span class="social-icon">💬</span>
        <span class="social-icon">📷</span>
        <span class="social-icon">🎵</span>
      </div>
      &copy; {{ new Date().getFullYear() }} {{ t('footer.copyright') }} v2.5.0
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
  background: var(--bg-page);
  color: var(--text-secondary);
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

/* Newsletter */
.footer-newsletter { background: #2a2a2a; padding: var(--space-xl); margin-top: var(--space-xl); }
.nl-inner { max-width: var(--max-width); margin: 0 auto; display: flex; justify-content: space-between; align-items: center; gap: var(--space-xl); flex-wrap: wrap; }
.nl-text { display: flex; flex-direction: column; gap: 4px; }
.nl-title { color: #fff; font-weight: 700; font-size: var(--font-md); }
.nl-desc { color: #888; font-size: var(--font-sm); }
.nl-form { display: flex; gap: var(--space-sm); }
.nl-input { padding: var(--space-sm) var(--space-md); border: 1px solid #555; border-radius: var(--radius-md); background: #444; color: #fff; font-size: var(--font-sm); width: 220px; outline: none; }
.nl-input:focus { border-color: var(--jd-red); }
.nl-btn { padding: var(--space-sm) var(--space-xl); background: var(--jd-red); color: #fff; border: none; border-radius: var(--radius-md); cursor: pointer; font-size: var(--font-sm); font-weight: 600; transition: background var(--transition-fast); }
.nl-btn:hover { background: #d63434; }
.nl-success { color: var(--green); font-weight: 600; }

/* Social */
.footer-social { display: flex; justify-content: center; gap: var(--space-lg); margin-bottom: var(--space-md); }
.social-icon { font-size: 22px; cursor: pointer; opacity: .6; transition: opacity var(--transition-fast); }
.social-icon:hover { opacity: 1; }

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
