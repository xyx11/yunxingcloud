<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSpace, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const loading = ref(false)
const templates = ref<any[]>([])
const showTest = ref(false); const testForm = ref({ templateKey: '', to: '', subject: '' })
const testing = ref(false)

const columns: DataTableColumns<any> = [
  { title: '模板Key', key: 'key', width: 180 },
  { title: '主题', key: 'subject', width: 200, ellipsis: { tooltip: true } },
  { title: '类型', key: 'type', width: 80, render(r: any) { return h(NTag, { size: 'small' }, { default: () => r.type || 'HTML' }) } },
  { title: '更新时间', key: 'updatedAt', width: 160, render(r: any) { return r.updatedAt?.substring(0, 16) || '-' } },
  { title: '操作', key: 'act', width: 80, render(r: any) { return h(NButton, { size: 'tiny', onClick: () => { testForm.value = { templateKey: r.key, to: '', subject: r.subject }; showTest.value = true } }, { default: () => '测试' }) } },
]

async function load() { loading.value = true; try { const r = await request.get('/api/email/templates'); templates.value = r.data || [] } finally { loading.value = false } }
async function sendTest() {
  testing.value = true
  try { await request.post('/api/email/test', testForm.value); notify.success('测试邮件已发送'); showTest.value = false }
  catch { notify.error('发送失败') } finally { testing.value = false }
}
onMounted(load)
</script>
<template>
  <div style="padding:20px">
    <n-card title="邮件模板"><template #header-extra><n-button size="small" @click="load" secondary>刷新</n-button></template>
      <n-dataTable :columns="columns" :data="templates" :loading="loading" :row-key="(r:any)=>r.key" :pagination="{pageSize:15}" size="small" />
    </n-card>
    <n-modal v-model:show="showTest" title="测试发送" preset="card" style="max-width:400px">
      <n-form :model="testForm">
        <n-form-item label="模板"><n-input v-model:value="testForm.templateKey" readonly /></n-form-item>
        <n-form-item label="收件人"><n-input v-model:value="testForm.to" placeholder="test@example.com" /></n-form-item>
        <n-form-item label="主题"><n-input v-model:value="testForm.subject" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showTest=false">取消</n-button><n-button type="primary" :loading="testing" @click="sendTest">发送测试</n-button></n-space></template>
    </n-modal>
  </div>
</template>
