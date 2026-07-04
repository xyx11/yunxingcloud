<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, h } from 'vue'
import { fetchIpBlacklist, blockIp, unblockIp } from '@/api/ipblacklist'
import { useNotify } from '@/composables/useNotify'

import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSpace, NPopconfirm } from 'naive-ui'
import type { FormRules, FormInst } from 'naive-ui'

const { t } = useI18n()
import type { DataTableColumns } from 'naive-ui'


interface IpItem { id: number; ip: string; reason: string; createdAt: string }

const notify = useNotify()

const items = ref<IpItem[]>([])
const loading = ref(false)
const showModal = ref(false)
const formRef = ref<FormInst>()
const rules: FormRules = {
  ip: [{ required: true, message: t('validate.required'), trigger: 'blur' }],
}
const form = ref({ ip: '', reason: '' })
const saving = ref(false)

const columns: DataTableColumns<IpItem> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('ipBlacklist.ip'), key: 'ip', width: 160 },
  { title: t('ipBlacklist.reason'), key: 'reason', width: 200, ellipsis: { tooltip: true } },
  { title: t('ipBlacklist.time'), key: 'createdAt', width: 160, render: (row: any) => row.createdAt?.substring(0,19).replace('T',' ') || '-' },
  { title: t('common.actions'), key: 'actions', width: 80, render: (row) => h(NPopconfirm, { onPositiveClick: () => delItem(row.id) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('ipBlacklist.unblock') }), default: () => t('ipBlacklist.unblockConfirm') }) },
]

async function loadItems() { loading.value = true; try { const r = await fetchIpBlacklist(); items.value = r.data } catch { notify.error(t('common.error')); }; loading.value = false }
async function delItem(id: number) { await unblockIp(id); await loadItems() }
async function saveItem() {
  if (formRef.value) { try { await formRef.value.validate() } catch { return } }
  saving.value = true
  try { await blockIp(form.value); showModal.value = false; form.value = { ip: '', reason: '' }; notify.success(t('ipBlacklist.blockSuccess')); await loadItems() }
  catch (e: any) { notify.error(e.response?.data?.message || t('ipBlacklist.blockFailed')) }
  finally { saving.value = false }
}

onMounted(loadItems)
</script>

<template>
  <div style="padding:20px">
    <n-card :title="t('nav.ipBlacklist')">
      <template #header-extra>
        <n-button type="primary" size="small" @click="showModal = true"><template #icon>＋</template>{{ t('ipBlacklist.blockIp') }}</n-button>
      </template>
      <n-dataTable :columns="columns" :data="items" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10 }" :row-key="(row: IpItem) => row.id" />

      <n-drawer v-model:show="showModal" :width="380" placement="right">
        <n-drawer-content :title="t('ipBlacklist.blockIp')" closable>
          <template #footer><n-space justify="end"><n-button @click="showModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="saveItem">{{ t('common.ok') }}</n-button></n-space></template>
          <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80" size="small">
            <n-form-item path="ip" :label="t('ipBlacklist.ip')"><n-input v-model:value="form.ip" placeholder="192.168.1.100" /></n-form-item>
            <n-form-item :label="t('ipBlacklist.reason')"><n-input v-model:value="form.reason" :placeholder="t('ipBlacklist.reason')" /></n-form-item>
          </n-form>
        </n-drawer-content>
      </n-drawer>
    </n-card>
  </div>
</template>
