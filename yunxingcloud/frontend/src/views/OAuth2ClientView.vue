<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSpace, NTag, NPopconfirm, darkTheme, lightTheme } from 'naive-ui'

const { t } = useI18n()
import type { DataTableColumns } from 'naive-ui'


interface Client { id: string; client_id: string; client_name: string; client_secret: string; redirect_uris: string; scopes: string; created_at: string }

const notify = useNotify()
const currentTheme = computed(() => localStorage.getItem("theme") === "dark" ? darkTheme : lightTheme)
const clients = ref<Client[]>([])
const loading = ref(false)
const showModal = ref(false)
const form = ref({ clientId: '', clientName: '', secret: '', redirectUris: 'http://127.0.0.1:9090/callback', scopes: 'openid,profile' })
const newSecret = ref('')
const saving = ref(false)

const columns: DataTableColumns<Client> = [
  { title: t('oauth2.clientId'), key: 'client_id', width: 160 },
  { title: t('oauth2.name'), key: 'client_name', width: 120 },
  { title: t('oauth2.redirectUri'), key: 'redirect_uris', width: 180, ellipsis: { tooltip: true } },
  { title: t('oauth2.scopes'), key: 'scopes', width: 120 },
  { title: t('common.createdAt'), key: 'created_at', width: 150, render: (row: any) => row.created_at?.substring(0,19).replace('T',' ') || '-' },
  { title: t('oauth2.actions'), key: 'actions', width: 80, render: (row) => h(NPopconfirm, { onPositiveClick: () => delClient(row.id) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }), default: () => t('common.confirmDelete') }) },
]

async function loadClients() { loading.value = true; try { const r = await request.get('/api/oauth2/clients'); clients.value = r.data } catch {}; loading.value = false }
async function delClient(id: string) { await request.delete(`/api/oauth2/clients/${id}`); loadClients() }
async function saveClient() {
  saving.value = true
  try {
    const r = await request.post('/api/oauth2/clients', form.value)
    newSecret.value = r.data.clientSecret || ''
    showModal.value = false
    notify.success(t('oauth2.createSuccess'))
    if (newSecret.value) notify.info('Client Secret: ' + newSecret.value)
    loadClients()
  } catch (e: any) { notify.error(e.response?.data?.message || t('oauth2.createFailed')) }
  finally { saving.value = false }
}

onMounted(loadClients)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card :title="t('oauth2.clientTitle')">
        <template #header-extra>
          <n-button type="primary" size="small" @click="showModal = true"><template #icon>＋</template>{{ t('common.add') }}</n-button>
        </template>
        <n-dataTable :columns="columns" :data="clients" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10 }" :row-key="(r: Client) => r.id" />

        <n-modal v-model:show="showModal" :title="t('oauth2.createClient')" preset="card" display-directive="show" style="max-width:520px;width:95%">
          <n-form label-placement="left" label-width="80">
            <n-form-item :label="t('oauth2.clientId')"><n-input v-model:value="form.clientId" placeholder="my-app" /></n-form-item>
            <n-form-item :label="t('oauth2.name')"><n-input v-model:value="form.clientName" :placeholder="t('oauth2.namePlaceholder')" /></n-form-item>
            <n-form-item :label="t('oauth2.secret')"><n-input v-model:value="form.secret" :placeholder="t('oauth2.secretPlaceholder')" /></n-form-item>
            <n-form-item :label="t('oauth2.redirectUri')"><n-input v-model:value="form.redirectUris" /></n-form-item>
            <n-form-item :label="t('oauth2.scopes')"><n-input v-model:value="form.scopes" /></n-form-item>
          </n-form>
          <template #footer><n-space justify="end"><n-button @click="showModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="saveClient">{{ t('common.submit') }}</n-button></n-space></template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>
