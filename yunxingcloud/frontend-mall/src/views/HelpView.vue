<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from '@/locales'
import { getArticles } from '@/api/cms'
import JdButton from '@/components/JdButton.vue'

interface FaqItem { q: string; a: string }

const { t } = useI18n()

const searchQuery = ref('')
const activeCategory = ref<string>('all')
const loading = ref(true)
const loadError = ref(false)
const openStates = ref<Record<string, boolean>>({})

// Categorized FAQs
const categories = [
  { key: 'all', icon: '📋', label: t('common.all') },
  { key: 'shopping', icon: '🛒', label: t('help.catShopping') },
  { key: 'payment', icon: '💳', label: t('help.catPayment') },
  { key: 'logistics', icon: '🚚', label: t('help.catLogistics') },
  { key: 'afterSale', icon: '🔄', label: t('help.catAfterSale') },
  { key: 'account', icon: '👤', label: t('help.catAccount') },
]

interface CategorizedFaq { category: string; items: FaqItem[] }

const faqGroups = ref<CategorizedFaq[]>([
  {
    category: 'shopping',
    items: [
      { q: t('help.q1'), a: t('help.a1') },
      { q: t('help.q7'), a: t('help.a7') },
    ],
  },
  {
    category: 'payment',
    items: [
      { q: t('help.q5'), a: t('help.a5') },
    ],
  },
  {
    category: 'logistics',
    items: [
      { q: t('help.q2'), a: t('help.a2') },
      { q: t('help.q6'), a: t('help.a6') },
    ],
  },
  {
    category: 'afterSale',
    items: [
      { q: t('help.q3'), a: t('help.a3') },
      { q: t('help.q4'), a: t('help.a4') },
    ],
  },
  {
    category: 'account',
    items: [
      { q: t('help.q8'), a: t('help.a8') },
    ],
  },
])

const catMeta = (key: string) => categories.find(c => c.key === key)

const filteredGroups = computed(() => {
  const q = searchQuery.value.toLowerCase().trim()
  let groups = faqGroups.value

  // Filter by category
  if (activeCategory.value !== 'all') {
    groups = groups.filter(g => g.category === activeCategory.value)
  }

  // Filter by search query
  if (q) {
    return groups
      .map(g => ({
        ...g,
        items: g.items.filter(i =>
          i.q.toLowerCase().includes(q) || i.a.toLowerCase().includes(q)
        ),
      }))
      .filter(g => g.items.length > 0)
  }

  return groups
})

const hasResults = computed(() => filteredGroups.value.some(g => g.items.length > 0))

const services = [
  { icon: '✅', title: t('help.promise1'), desc: t('footer.promise1Desc') },
  { icon: '🚚', title: t('help.promise2'), desc: t('footer.promise2Desc') },
  { icon: '🔄', title: t('help.promise3'), desc: t('footer.promise3Desc') },
  { icon: '💬', title: t('help.promise4'), desc: t('footer.promise4Desc') },
]

function toggle(groupIdx: number, itemIdx: number) {
  const group = filteredGroups.value[groupIdx]
  if (!group || !group.items[itemIdx]) return
  const key = group.items[itemIdx].q
  openStates.value = { ...openStates.value, [key]: !openStates.value[key] }
}

async function loadHelp() {
  loadError.value = false; loading.value = true
  try {
    const r = await getArticles('help')
    const articles = r.data || []
    if (articles.length) {
      const extraItems: FaqItem[] = articles.map((a: { title?: string; content?: string }) => ({
        q: a.title || '', a: a.content || '',
      }))
      const otherGroup = faqGroups.value.find(g => g.category === 'account')
      if (otherGroup) otherGroup.items.push(...extraItems)
      else faqGroups.value.push({ category: 'account', items: extraItems })
    }
    loading.value = false;
  } catch { loadError.value = true; loading.value = false }
}
function retry() { loadHelp() }

onMounted(loadHelp)
</script>

<template>
  <div class="help-page">
    <!-- Header -->
    <div class="help-hero">
      <h1 class="help-hero-title">❓ {{ t('help.title') }}</h1>
      <p class="help-hero-sub">{{ t('help.faq') }}</p>
      <!-- Search -->
      <div class="help-search-wrap">
        <span class="help-search-icon">🔍</span>
        <input
          v-model="searchQuery"
          class="help-search"
          :placeholder="t('help.searchPlaceholder')"
        />
      </div>
    </div>

    <!-- Category tabs -->
    <div class="help-cats">
      <button
        v-for="cat in categories" :key="cat.key"
        class="help-cat"
        :class="{ active: activeCategory === cat.key }"
        @click="activeCategory = cat.key"
      >
        {{ cat.icon }} {{ cat.label }}
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="help-skel">
      <div v-for="i in 5" :key="i" class="sk-line" />
    </div>

    <!-- Error -->
    <div v-else-if="loadError" class="help-error">
      <p>{{ t('help.loadFail') }}</p>
      <JdButton @click="retry">{{ t('common.retry') }}</JdButton>
    </div>

    <!-- FAQ list -->
    <div v-else-if="hasResults">
      <div v-for="(group, gi) in filteredGroups" :key="group.category" class="faq-group">
        <h3 class="faq-group-title">
          {{ catMeta(group.category)?.icon }} {{ catMeta(group.category)?.label }}
        </h3>
        <div v-for="(faq, fi) in group.items" :key="fi" class="faq-item">
          <div class="faq-q" @click="toggle(gi, fi)">
            <span>{{ faq.q }}</span>
            <span class="faq-arrow" :class="{ open: openStates[faq.q] }">+</span>
          </div>
          <Transition name="faq-slide">
            <div v-if="openStates[faq.q]" class="faq-a">{{ faq.a }}</div>
          </Transition>
        </div>
      </div>
    </div>

    <!-- No results -->
    <div v-else class="help-no-results">
      <p class="help-no-icon">🔍</p>
      <p class="help-no-text">{{ t('help.noResults') }}</p>
      <p class="help-no-hint">{{ t('help.noResultsHint') }}</p>
    </div>

    <!-- Service promise -->
    <div class="help-services">
      <h3 class="help-services-title">🛡️ {{ t('help.service') }}</h3>
      <div class="help-services-grid">
        <div v-for="s in services" :key="s.title" class="help-service-card">
          <div class="help-service-icon">{{ s.icon }}</div>
          <div class="help-service-title">{{ s.title }}</div>
          <div class="help-service-desc">{{ s.desc }}</div>
        </div>
      </div>
    </div>

    <!-- Contact footer -->
    <div class="faq-footer">
      <p class="faq-footer-title">{{ t('help.contact') }}</p>
      <p class="faq-footer-desc">{{ t('help.contactDesc') }}</p>
    </div>
  </div>
</template>

<style scoped>
.help-page { max-width: 700px; margin: 0 auto; }

/* Hero */
.help-hero {
  background: linear-gradient(135deg, var(--brand-blue, #1677ff), #4096ff);
  color: #fff; border-radius: var(--radius-xl); padding: 36px; margin-bottom: var(--space-lg);
  text-align: center; box-shadow: 0 4px 20px rgba(22,119,255,.2);
}
.help-hero-title { font-size: var(--font-h1); font-weight: 800; margin-bottom: 4px; }
.help-hero-sub { font-size: var(--font-md); opacity: .85; margin-bottom: var(--space-lg); }

.help-search-wrap {
  position: relative; max-width: 400px; margin: 0 auto;
}
.help-search-icon {
  position: absolute; left: 14px; top: 50%; transform: translateY(-50%); font-size: 16px;
}
.help-search {
  width: 100%; padding: 12px 16px 12px 40px; border: none; border-radius: var(--radius-round);
  font-size: var(--font-md); outline: none; background: rgba(255,255,255,.95);
  color: var(--text-primary); box-sizing: border-box;
}
.help-search::placeholder { color: var(--text-placeholder); }

/* Category tabs */
.help-cats { display: flex; gap: 6px; margin-bottom: var(--space-xl); flex-wrap: wrap; }
.help-cat {
  padding: 6px 14px; border: 1px solid var(--border); background: var(--bg-white);
  border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-sm);
  color: var(--text-secondary); transition: all var(--transition-fast);
  white-space: nowrap;
}
.help-cat.active { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }
.help-cat:not(.active):hover { border-color: var(--jd-red); color: var(--jd-red); }

/* FAQ groups */
.faq-group { margin-bottom: var(--space-xl); }
.faq-group-title {
  font-size: var(--font-lg); font-weight: 700; margin-bottom: var(--space-md);
  color: var(--text-primary);
}

.faq-item { background: var(--bg-white); border-radius: var(--radius-md); margin-bottom: 8px; box-shadow: var(--shadow-sm); overflow: hidden; }
.faq-q {
  padding: 16px 20px; cursor: pointer; display: flex;
  justify-content: space-between; align-items: center;
  transition: background var(--transition-fast); font-weight: 600; font-size: 15px; gap: var(--space-sm);
}
.faq-q:hover { background: var(--bg-hover); }
.faq-arrow { font-size: 18px; color: var(--text-tertiary); transition: transform var(--transition); flex-shrink: 0; }
.faq-arrow.open { transform: rotate(45deg); }
.faq-slide-enter-active { transition: all .25s ease-out; }
.faq-slide-leave-active { transition: all .15s ease-in; }
.faq-slide-enter-from, .faq-slide-leave-to { opacity: 0; max-height: 0; overflow: hidden; }
.faq-slide-enter-to, .faq-slide-leave-from { opacity: 1; max-height: 500px; }
.faq-a {
  padding: 0 20px 16px; color: var(--text-secondary); font-size: var(--font-md);
  line-height: 1.8; border-top: 1px solid var(--border-light); margin: 0 20px;
  overflow: hidden;
}

/* No results */
.help-no-results { text-align: center; padding: 40px var(--space-md); }
.help-no-icon { font-size: 48px; margin-bottom: var(--space-md); }
.help-no-text { font-size: var(--font-lg); font-weight: 600; color: var(--text-secondary); margin-bottom: var(--space-xs); }
.help-no-hint { font-size: var(--font-sm); color: var(--text-placeholder); }

/* Services */
.help-services { margin-top: var(--space-xxl); margin-bottom: var(--space-xxl); }
.help-services-title { font-size: var(--font-lg); font-weight: 700; margin-bottom: var(--space-lg); }
.help-services-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--space-md); }
.help-service-card {
  background: var(--bg-white); border-radius: var(--radius-md); padding: var(--space-lg);
  box-shadow: var(--shadow-sm); text-align: center;
}
.help-service-icon { font-size: 28px; margin-bottom: var(--space-xs); }
.help-service-title { font-size: var(--font-md); font-weight: 600; margin-bottom: 2px; }
.help-service-desc { font-size: var(--font-xs); color: var(--text-tertiary); }

/* Footer */
.faq-footer {
  background: var(--bg-white); border-radius: var(--radius-md); padding: 24px;
  margin-top: var(--space-xl); box-shadow: var(--shadow-sm); text-align: center;
}
.faq-footer-title { font-size: var(--font-lg); font-weight: 600; margin-bottom: var(--space-sm); }
.faq-footer-desc { color: var(--text-secondary); font-size: var(--font-md); }

/* Skeleton */
.help-skel { display: flex; flex-direction: column; gap: var(--space-md); }
.sk-line { height: 48px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-md); }
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }
.help-error { text-align: center; padding: 40px var(--space-md); }

@media (max-width: 768px) {
  .help-page { padding: 0 var(--space-md) 80px; }
  .help-hero { padding: 24px 16px; }
  .help-hero-title { font-size: var(--font-xxl); }
  .help-cats { gap: 4px; }
  .help-cat { padding: 5px 10px; font-size: 12px; }
  .faq-q { padding: 14px 16px; font-size: 14px; }
  .faq-a { padding: 0 16px 14px; margin: 0 16px; }
  .help-services-grid { grid-template-columns: 1fr 1fr; }
}
</style>