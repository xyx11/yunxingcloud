<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
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
  { title: t('email.templateKey'), key: 'key', width: 180 },
  { title: t('email.subject'), key: 'subject', width: 200, ellipsis: { tooltip: true } },
  { title: t('email.type'), key: 'type', width: 80, render(r: any) { return h(NTag, { size: 'small' }, { default: () => r.type || 'HTML' }) } },
  { title: t('common.updatedAt'), key: 'updatedAt', width: 160, render(r: any) { return r.updatedAt?.substring(0, 16) || '-' } },
  { title: t('common.actions'), key: 'act', width: 80, render(r: any) { return h(NButton, { size: 'tiny', onClick: () => { testForm.value = { templateKey: r.key, to: '', subject: r.subject }; showTest.value = true } }, { default: () => t('email.test') }) } },
]

async function load() { loading.value = true; try { const r = await request.get('/api/email/templates'); templates.value = r.data || [] } finally { loading.value = false } }
async function sendTest() {
  testing.value = true
  try { await request.post('/api/email/test', testForm.value); notify.success(t('email.sendSuccess')); showTest.value = false }
  catch { notify.error(t('email.sendFail')) } finally { testing.value = false }
}
onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card :title="t('email.title')"><template #header-extra><n-button size="small" @click="load" secondary>{{ t('email.refresh') }}</n-button></template>
      <n-dataTable :columns="columns" :data="templates" :loading="loading" :row-key="(r:any)=>r.key" :pagination="{pageSize:15}" size="small" />
    </n-card>
    <n-modal v-model:show="showTest" :title="t('email.testTitle')" preset="card" style="max-width:400px">
      <n-form :model="testForm">
        <n-form-item :label="t('email.template')"><n-input v-model:value="testForm.templateKey" readonly /></n-form-item>
        <n-form-item :label="t('email.recipient')"><n-input v-model:value="testForm.to" placeholder="test@example.com" /></n-form-item>
        <n-form-item :label="t('email.subject')"><n-input v-model:value="testForm.subject" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showTest=false">{{ t('email.cancel') }}</n-button><n-button type="primary" :loading="testing" @click="sendTest">{{ t('email.sendTest') }}</n-button></n-space></template>
    </n-modal>
  </div>
</template>
