<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { NModal, NInput, NTag } from 'naive-ui'

const router = useRouter()
const visible = ref(false)
const query = ref('')

const commands = [
  { label: '仪表盘', path: '/', icon: '📊', tag: '首页' },
  { label: '用户管理', path: '/users', icon: '👥', tag: '管理' },
  { label: '角色管理', path: '/roles', icon: '🔑', tag: '管理' },
  { label: '部门管理', path: '/departments', icon: '🏢', tag: '管理' },
  { label: '菜单管理', path: '/menus', icon: '📋', tag: '管理' },
  { label: '操作日志', path: '/operlog', icon: '📄', tag: '监控' },
  { label: '定时任务', path: '/job', icon: '⏰', tag: '监控' },
  { label: '参数配置', path: '/config', icon: '⚙️', tag: '系统' },
  { label: '代码生成', path: '/generator', icon: '🔧', tag: '工具' },
  { label: 'API文档', path: '/swagger', icon: '📖', tag: '工具' },
  { label: '个人中心', path: '/profile', icon: '👤', tag: '系统' },
  { label: '暗色模式', action: 'toggle-theme', icon: '🌓', tag: '系统' },
]

const filtered = computed(() => {
  const q = query.value.toLowerCase()
  if (!q) return commands
  return commands.filter(c => c.label.toLowerCase().includes(q) || c.tag.includes(q))
})

const selectedIndex = ref(0)

function onKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    visible.value = true
    query.value = ''
    selectedIndex.value = 0
  }
  if (!visible.value) return
  if (e.key === 'ArrowDown') { e.preventDefault(); selectedIndex.value = Math.min(selectedIndex.value + 1, filtered.value.length - 1) }
  if (e.key === 'ArrowUp') { e.preventDefault(); selectedIndex.value = Math.max(selectedIndex.value - 1, 0) }
  if (e.key === 'Enter') { e.preventDefault(); execute(filtered.value[selectedIndex.value]) }
  if (e.key === 'Escape') visible.value = false
}

function execute(cmd: typeof commands[0]) {
  visible.value = false
  if (cmd.path) router.push(cmd.path)
  if (cmd.action === 'toggle-theme') {
    const current = localStorage.getItem('theme') === 'dark' ? 'light' : 'dark'
    localStorage.setItem('theme', current)
    location.reload()
  }
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))
</script>

<template>
  <n-modal v-model:show="visible" :closable="true" :mask-closable="true" style="width:520px;top:15vh;">
    <div class="palette">
      <n-input v-model:value="query" placeholder="搜索页面..." size="large" :autofocus="true" clearable>
        <template #prefix>🔍</template>
        <template #suffix><n-tag size="small" :bordered="false" style="opacity:.5">Ctrl+K</n-tag></template>
      </n-input>
      <div class="results" v-if="filtered.length">
        <div v-for="(cmd, i) in filtered" :key="cmd.label"
          class="item" :class="{ active: i === selectedIndex }"
          @click="execute(cmd)" @mouseenter="selectedIndex = i">
          <span class="icon">{{ cmd.icon }}</span>
          <span class="label">{{ cmd.label }}</span>
          <n-tag size="tiny" :bordered="false" style="opacity:.6;margin-left:auto;">{{ cmd.tag }}</n-tag>
        </div>
      </div>
      <div v-else class="empty">未找到匹配项</div>
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
