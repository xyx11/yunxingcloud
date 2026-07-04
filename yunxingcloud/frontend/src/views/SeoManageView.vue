<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NForm, NFormItem, NInput, NButton, NSpace, NAlert } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const homeMeta = ref({ title: '', description: '', keywords: '' })
const sitemap = ref('')

async function load() {
  try { const r = await request.get('/api/seo/meta/home'); Object.assign(homeMeta.value, r.data) } catch {}
  try { const r = await request.get('/api/seo/sitemap'); sitemap.value = typeof r.data === 'string' ? r.data : '' } catch {}
}
function copySitemap() { navigator.clipboard.writeText(sitemap.value); notify.success('已复制') }
onMounted(load)
</script>
<template>
  <div style="padding:20px">
    <n-space vertical size="large">
      <n-card title="SEO 管理">
        <n-form :model="homeMeta" label-placement="left" label-width="100">
          <n-form-item label="首页标题"><n-input v-model:value="homeMeta.title" placeholder="Mall - 最佳购物体验" /></n-form-item>
          <n-form-item label="首页描述"><n-input v-model:value="homeMeta.description" type="textarea" placeholder="商城首页 meta description" /></n-form-item>
          <n-form-item label="首页关键词"><n-input v-model:value="homeMeta.keywords" placeholder="keyword1,keyword2" /></n-form-item>
          <n-button type="primary" @click="notify.success('SEO信息需要在后端配置')">预览效果</n-button>
        </n-form>
      </n-card>
      <n-card title="Sitemap">
        <n-alert type="info" style="margin-bottom:8px">搜索引擎爬虫索引文件</n-alert>
        <pre style="background:#f5f5f5;padding:16px;border-radius:8px;font-size:12px;overflow:auto;max-height:300px">{% raw %}{{ sitemap || '加载中...' }}{% endraw %}</pre>
        <n-button size="small" @click="copySitemap" style="margin-top:8px">复制内容</n-button>
      </n-card>
    </n-space>
  </div>
</template>
