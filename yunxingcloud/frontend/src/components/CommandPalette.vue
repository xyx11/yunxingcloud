<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { NModal, NInput, NTag } from 'naive-ui'

const { t } = useI18n()
const router = useRouter()
const visible = ref(false)
const query = ref('')

const commands = computed(() => [
  { label: t('nav.dashboard'), path: '/', icon: '📊', tag: t('nav.home') },
  { label: t('nav.users'), path: '/users', icon: '👥', tag: t('common.add') },
  { label: t('nav.roles'), path: '/roles', icon: '🔑', tag: t('common.add') },
  { label: t('nav.departments'), path: '/departments', icon: '🏢', tag: t('common.add') },
  { label: t('nav.menus'), path: '/menus', icon: '📋', tag: t('common.add') },
  { label: t('nav.operlog'), path: '/operlog', icon: '📄', tag: t('nav.monitor') },
  { label: t('nav.jobs'), path: '/job', icon: '⏰', tag: t('nav.monitor') },
  { label: t('nav.config'), path: '/config', icon: '⚙️', tag: t('nav.tagSystem') },
  { label: t('nav.generator'), path: '/generator', icon: '🔧', tag: t('nav.tagTool') },
  { label: t('nav.swagger'), path: '/swagger', icon: '📖', tag: t('nav.tagTool') },
  { label: t('nav.monitor'), path: '/monitor', icon: '📈', tag: t('nav.monitor') },
  { label: t('nav.maintenance'), path: '/maintenance', icon: '🧹', tag: t('nav.tagSystem') },
  { label: t('nav.profile'), path: '/profile', icon: '👤', tag: t('nav.tagSystem') },
  { label: t('nav.darkMode'), action: 'toggle-theme', icon: '🌓', tag: t('nav.tagSystem') },
])

const filtered = computed(() => {
  const q = query.value.toLowerCase()
  if (!q) return commands.value
  return commands.value.filter(c => c.label.toLowerCase().includes(q) || c.tag.toLowerCase().includes(q))
})

const selectedIndex = ref(0)

function onKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    openPalette()
    return
  }
  if (e.key === '?' && !visible.value && !isInputFocused()) {
    e.preventDefault()
    openPalette()
    query.value = '?'
    return
  }
  if (!visible.value) return
  if (e.key === 'ArrowDown') { e.preventDefault(); selectedIndex.value = Math.min(selectedIndex.value + 1, filtered.value.length - 1) }
  if (e.key === 'ArrowUp') { e.preventDefault(); selectedIndex.value = Math.max(selectedIndex.value - 1, 0) }
  if (e.key === 'Enter') { e.preventDefault(); execute(filtered.value[selectedIndex.value]) }
  if (e.key === 'Escape') visible.value = false
}

function openPalette() {
  visible.value = true
  query.value = ''
  selectedIndex.value = 0
}

function isInputFocused() {
  const el = document.activeElement
  return el && (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA' || (el as HTMLElement).isContentEditable)
}

function execute(cmd: { label: string; path?: string; action?: string; icon: string; tag: string }) {
  visible.value = false
  if (cmd.path) router.push(cmd.path)
  if (cmd.action === 'toggle-theme') {
    const current = localStorage.getItem('theme') === 'dark' ? 'light' : 'dark'
    localStorage.setItem('theme', current)
    document.documentElement.setAttribute('theme', current)
    window.dispatchEvent(new CustomEvent('theme-change', { detail: current }))
  }
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))
</script>

<template>
  <n-modal v-model:show="visible" :closable="true" :mask-closable="true" style="width:520px;top:15vh;">
    <div class="palette">
      <n-input v-model:value="query" :placeholder="t('nav.searchPlaceholder')" size="large" :autofocus="true" clearable>
        <template #prefix>🔍</template>
        <template #suffix><n-tag size="small" :bordered="false" style="opacity:.5">Ctrl+K / ?</n-tag></template>
      </n-input>
      <div class="results" v-if="filtered.length">
        <div
          v-for="(cmd, i) in filtered" :key="cmd.label"
          class="item" :class="{ active: i === selectedIndex }"
          @click="execute(cmd)" @mouseenter="selectedIndex = i"
        >
          <span class="icon">{{ cmd.icon }}</span>
          <span class="label">{{ cmd.label }}</span>
          <n-tag size="tiny" :bordered="false" style="opacity:.6;margin-left:auto;">{{ cmd.tag }}</n-tag>
        </div>
      </div>
      <div v-else class="empty">{{ t('nav.noMatchResults') }}</div>
    </div>
  </n-modal>
</template>

<style scoped>
.palette { background: var(--n-color); border-radius: 12px; padding: 12px; }
.results { margin-top: 8px; max-height: 320px; overflow-y: auto; }
.item { display: flex; align-items: center; gap: 10px; padding: 10px 12px; border-radius: 8px; cursor: pointer; }
.item:hover, .item.active { background: rgba(102,126,234,0.12); }
.icon { font-size: 18px; }
.label { font-size: 14px; font-weight: 500; }
.empty { text-align: center; padding: 24px; color: #999; font-size: 13px; }
</style>

<style scoped>
.cp-item { display: flex; align-items: center; gap: 12px; padding: 10px 16px; border-radius: 6px; cursor: pointer; transition: background .15s; margin: 0 8px; }
.cp-desc { font-size: 12px; color: #999; }
.cp-hint { font-size: 10px; color: #999; }
</style>
