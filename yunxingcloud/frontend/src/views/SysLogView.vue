<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { NCard, NButton, NInput, NSpace, NSpin } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
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
  try { const r = await request.get('/api/system/logs/view/' + encodeURIComponent(name)); content.value = r.data || '' } catch { content.value = '无法加载日志'; notify.error('加载失败') }
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
  <div style="padding:20px">
    <n-card title="系统日志" content-style="padding:0">
      <div style="display:flex;height:calc(100vh - 160px)">
        <!-- 左侧文件列表 -->
        <div style="width:260px;border-right:1px solid #eee;padding:12px;overflow-y:auto">
          <n-space vertical style="margin-bottom:12px">
            <n-input v-model:value="searchKeyword" placeholder="搜索文件..." size="small" clearable />
            <n-button size="small" @click="loadFiles" secondary block>刷新列表</n-button>
          </n-space>
          <div v-for="f in filteredFiles" :key="f" :style="{padding:'8px 12px',cursor:'pointer',borderRadius:'4px',marginBottom:'2px',background:activeFile===f?'#e6f0ff':'transparent',fontSize:'13px'}" @click="viewFile(f)">{{ f }}</div>
          <div v-if="!files.length" style="color:#999;text-align:center;padding:20px">暂无日志文件</div>
        </div>
        <!-- 右侧日志内容 -->
        <div style="flex:1;overflow:hidden;display:flex;flex-direction:column">
          <n-space style="padding:12px;border-bottom:1px solid #eee;justify-content:space-between">
            <span><b>{{ activeFile || '请选择日志文件' }}</b></span>
            <n-space>
              <n-input v-model:value="highlight" placeholder="高亮关键字" size="small" clearable style="width:150px" />
              <n-button size="small" @click="refresh" :disabled="!activeFile" secondary>刷新</n-button>
            </n-space>
          </n-space>
          <div style="flex:1;overflow:auto;padding:12px">
            <n-spin v-if="contentLoading" />
            <pre v-else style="margin:0;font-size:12px;line-height:1.6;white-space:pre-wrap;word-break:break-all" v-html="highlightedContent" />
            <div v-if="!activeFile && !contentLoading" style="color:#999;text-align:center;padding:40px">← 请从左侧选择日志文件</div>
          </div>
        </div>
      </div>
    </n-card>
  </div>
</template>
