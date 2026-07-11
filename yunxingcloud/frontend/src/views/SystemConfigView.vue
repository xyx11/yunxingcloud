<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSpace, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const { t } = useI18n()
const loading = ref(false); const saving = ref(false)
const items = ref<any[]>([])
const showModal = ref(false)
const form = ref({ key: '', value: '', description: '' })

const columns: DataTableColumns<any> = [
  { title: '配置项', key: 'key', width: 180 },
  { title: '值', key: 'value', width: 250, ellipsis: { tooltip: true } },
  { title: t('common.description'), key: 'description', width: 200, ellipsis: { tooltip: true } },
  { title: t('common.actions'), key: 'act', width: 80, render(r: any) { return h(NButton, { size: 'tiny', onClick: () => { form.value = { key: r.key, value: r.value || '', description: r.description || '' }; showModal.value = true } }, { default: () => t('common.edit') }) } },
]

async function load() { loading.value = true; try { const r = await request.get('/api/system-config'); items.value = r.data || [] } finally { loading.value = false } }
async function save() {
  saving.value = true
  try { await request.put(`/api/system-config/${form.value.key}`, { value: form.value.value, description: form.value.description }); showModal.value = false; notify.success('已保存'); load() }
  catch { notify.error(t('common.saveFailed')) } finally { saving.value = false }
}
onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-card title="系统配置"><template #header-extra><n-button size="small" @click="load" secondary>刷新</n-button></template>
      <n-dataTable :columns="columns" :data="items" :loading="loading" :row-key="(r:any)=>r.key" :pagination="{pageSize:15}" size="small" />
    </n-card>
    <n-modal v-model:show="showModal" title="编辑配置" preset="card" class="max-w-450">
      <n-form :model="form">
        <n-form-item label="配置项"><n-input v-model:value="form.key" readonly /></n-form-item>
        <n-form-item label="值"><n-input v-model:value="form.value" type="textarea" /></n-form-item>
        <n-form-item :label="t('common.description')"><n-input v-model:value="form.description" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" :loading="saving" @click="save">保存</n-button></n-space></template>
    </n-modal>
  </div>
</template>

<style scoped>
.max-w-450 { max-width: 450px; }
</style>
