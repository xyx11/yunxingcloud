<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NButton, NInput, NSpace, NSpin } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const { t } = useI18n()
const files = ref<string[]>([])
const activeFile = ref('')
const content = ref('')
const loading = ref(false)
const contentLoading = ref(false)
const searchKeyword = ref('')
const highlight = ref('')

const filteredFiles = computed(() => {
  if (!searchKeyword.value) return files.value
  return files.value.filter(f => f.toLowerCase().includes(searchKeyword.value.toLowerCase()))
})

async function loadFiles() { loading.value = true; try { const r = await request.get('/api/system/logs/files'); files.value = r.data || [] } finally { loading.value = false } }
async function viewFile(name: string) {
  activeFile.value = name; contentLoading.value = true
  try { const r = await request.get('/api/system/logs/view/' + encodeURIComponent(name)); content.value = r.data || '' } catch { content.value = '无法加载日志'; notify.error(t('common.loadFailed')) }
  finally { contentLoading.value = false }
}
function refresh() { if (activeFile.value) viewFile(activeFile.value) }

// Highlight search keyword in content
const highlightedContent = computed(() => {
  if (!highlight.value || !content.value) return content.value
  const escaped = highlight.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return content.value.replace(new RegExp(escaped, 'gi'), m => `<mark>${m}</mark>`)
})

onMounted(loadFiles)
</script>
<template>
  <div class="view-pad">
    <n-card :title="t('nav.systemLogs')" content-style="padding:0">
      <div class="sl-layout">
        <!-- 左侧文件列表 -->
        <div class="sl-sidebar">
          <n-space vertical class="mb-12">
            <n-input v-model:value="searchKeyword" placeholder="搜索文件..." size="small" clearable />
            <n-button size="small" @click="loadFiles" secondary block>刷新列表</n-button>
          </n-space>
          <div v-for="f in filteredFiles" :key="f" :class="['sl-file-item', { 'sl-file-item--active': activeFile === f }]" @click="viewFile(f)">{{ f }}</div>
          <div v-if="!files.length" class="sl-empty">{{ t('common.noData') }}</div>
        </div>
        <!-- 右侧日志内容 -->
        <div class="sl-main">
          <n-space class="sl-toolbar">
            <span><b>{{ activeFile || '请选择日志文件' }}</b></span>
            <n-space>
              <n-input v-model:value="highlight" placeholder="高亮关键字" size="small" clearable class="sl-input" />
              <n-button size="small" @click="refresh" :disabled="!activeFile" secondary>刷新</n-button>
            </n-space>
          </n-space>
          <div class="sl-content">
            <n-spin v-if="contentLoading" />
            <pre v-else class="sl-log-text" v-html="highlightedContent" />
            <div v-if="!activeFile && !contentLoading" class="sl-placeholder">← 请从左侧选择日志文件</div>
          </div>
        </div>
      </div>
    </n-card>
  </div>
</template>

<style scoped>
.sl-layout { display: flex; height: calc(100vh - 160px); }
.sl-sidebar { width: 260px; border-right: 1px solid #eee; padding: 12px; overflow-y: auto; }
.sl-main { flex: 1; overflow: hidden; display: flex; flex-direction: column; }
.sl-toolbar { padding: 12px; border-bottom: 1px solid #eee; justify-content: space-between; }
.sl-input { width: 150px; }
.sl-content { flex: 1; overflow: auto; padding: 12px; }
.sl-log-text { margin: 0; font-size: 12px; line-height: 1.6; white-space: pre-wrap; word-break: break-all; }
.sl-empty, .sl-placeholder { color: #999; text-align: center; padding: 20px; }
.sl-placeholder { padding: 40px; }
.sl-file-item { padding: 8px 12px; cursor: pointer; border-radius: 4px; margin-bottom: 2px; font-size: 13px; background: transparent; }
.sl-file-item--active { background: #e6f0ff; }
.sl-file-item-text { font-size: 13px; }
</style>
