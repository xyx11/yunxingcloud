<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSpace, NPopconfirm, NTag, darkTheme, lightTheme } from 'naive-ui'

const { t } = useI18n()
import type { DataTableColumns } from 'naive-ui'


interface IpItem { id: number; ip: string; reason: string; createdAt: string }

const notify = useNotify()
const currentTheme = computed(() => localStorage.getItem("theme") === "dark" ? darkTheme : lightTheme)
const items = ref<IpItem[]>([])
const loading = ref(false)
const showModal = ref(false)
const form = ref({ ip: '', reason: '' })
const saving = ref(false)

const columns: DataTableColumns<IpItem> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('ipBlacklist.ip'), key: 'ip', width: 160 },
  { title: t('ipBlacklist.reason'), key: 'reason', width: 200, ellipsis: { tooltip: true } },
  { title: t('ipBlacklist.time'), key: 'createdAt', width: 160, render: (row: any) => row.createdAt?.substring(0,19).replace('T',' ') || '-' },
  { title: t('common.actions'), key: 'actions', width: 80, render: (row) => h(NPopconfirm, { onPositiveClick: () => delItem(row.id) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('ipBlacklist.unblock') }), default: () => t('ipBlacklist.unblockConfirm') }) },
]

async function loadItems() { loading.value = true; try { const r = await request.get('/api/ip-blacklist'); items.value = r.data } catch {}; loading.value = false }
async function delItem(id: number) { await request.delete(`/api/ip-blacklist/${id}`); await loadItems() }
async function saveItem() {
  saving.value = true
  try { await request.post('/api/ip-blacklist', form.value); showModal.value = false; form.value = { ip: '', reason: '' }; notify.success(t('ipBlacklist.blockSuccess')); await loadItems() }
  catch (e: any) { notify.error(e.response?.data?.message || t('ipBlacklist.blockFailed')) }
  finally { saving.value = false }
}

onMounted(loadItems)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card :title="t('nav.ipBlacklist')">
        <template #header-extra>
          <n-button type="primary" size="small" @click="showModal = true"><template #icon>＋</template>{{ t('ipBlacklist.blockIp') }}</n-button>
        </template>
        <n-dataTable :columns="columns" :data="items" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10 }" :row-key="(row: IpItem) => row.id" />

        <n-modal v-model:show="showModal" :title="t('ipBlacklist.blockIp')" preset="card" display-directive="show" style="max-width:400px;width:95%">
          <n-form label-placement="left" label-width="80">
            <n-form-item :label="t('ipBlacklist.ip')"><n-input v-model:value="form.ip" placeholder="192.168.1.100" /></n-form-item>
            <n-form-item :label="t('ipBlacklist.reason')"><n-input v-model:value="form.reason" :placeholder="t('ipBlacklist.reason')" /></n-form-item>
          </n-form>
          <template #footer><n-space justify="end"><n-button @click="showModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="saveItem">{{ t('common.ok') }}</n-button></n-space></template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>
