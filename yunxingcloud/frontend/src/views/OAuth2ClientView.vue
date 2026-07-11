<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, h } from 'vue'
import { fetchOAuth2Clients, createOAuth2Client, updateOAuth2Client, deleteOAuth2Client } from '@/api/oauth2client'
import { useNotify } from '@/composables/useNotify'

import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSpace, NPopconfirm } from 'naive-ui'

const { t } = useI18n()
import type { DataTableColumns } from 'naive-ui'


interface Client { id: string; client_id: string; client_name: string; client_secret: string; redirect_uris: string; scopes: string; created_at: string }

const notify = useNotify()

const clients = ref<Client[]>([])
const loading = ref(false)
const showModal = ref(false)
const form = ref({ clientId: '', clientName: '', secret: '', redirectUris: 'http://127.0.0.1:9090/callback', scopes: 'openid,profile' })
const newSecret = ref('')
const saving = ref(false)
const editingId = ref<string | null>(null)

function renderActions(row: Client) {
  return h(NSpace, { size: 'small' }, { default: () => [
    h(NButton, { size: 'tiny', onClick: () => { editingId.value = row.id; form.value = { clientId: row.client_id, clientName: row.client_name, secret: '', redirectUris: row.redirect_uris, scopes: row.scopes }; showModal.value = true } }, { default: () => t('common.edit') }),
    h(NPopconfirm, { onPositiveClick: () => delClient(row.id) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }), default: () => t('common.confirmDelete') })
  ]})
}

const columns: DataTableColumns<Client> = [
  { title: t('oauth2.clientId'), key: 'client_id', width: 160 },
  { title: t('oauth2.name'), key: 'client_name', width: 120 },
  { title: t('oauth2.redirectUri'), key: 'redirect_uris', width: 180, ellipsis: { tooltip: true } },
  { title: t('oauth2.scopes'), key: 'scopes', width: 120 },
  { title: t('common.createdAt'), key: 'created_at', width: 150, render: (row: Client) => row.created_at?.substring(0,19).replace('T',' ') || '-' },
  { title: t('oauth2.actions'), key: 'actions', width: 80, render: (row) => renderActions(row) },
]

async function loadClients() { loading.value = true; try { const r = await fetchOAuth2Clients(); clients.value = r.data } catch { notify.error(t('common.error')); }; loading.value = false }
async function delClient(id: string) { try { await deleteOAuth2Client(Number(id)); loadClients() } catch { notify.error(t('common.saveFailed')) } }
async function saveClient() {
  saving.value = true
  try {
    if (editingId.value) {
      await updateOAuth2Client(editingId.value, { clientName: form.value.clientName, redirectUris: form.value.redirectUris, scopes: form.value.scopes })
      notify.success(t('common.updateSuccess'))
    } else {
      const r = await createOAuth2Client(form.value)
      newSecret.value = r.data.clientSecret || ''
      notify.success(t('oauth2.createSuccess'))
      if (newSecret.value) notify.info('Client Secret: ' + newSecret.value)
    }
    showModal.value = false; editingId.value = null; loadClients()
  } catch (e: unknown) { const err = e as { response?: { data?: { message?: string } } }; notify.error(err.response?.data?.message || t('common.saveFailed')) }
  finally { saving.value = false }
}

onMounted(loadClients)
</script>

<template>
  <div class="view-pad">
    <n-card :title="t('oauth2.clientTitle')">
      <template #header-extra>
        <n-button type="primary" size="small" @click="editingId=null;showModal=true"><template #icon>＋</template>{{ t('common.add') }}</n-button>
      </template>
      <n-dataTable :columns="columns" :data="clients" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10 }" :row-key="(r: Client) => r.id" />

      <n-drawer v-model:show="showModal" :width="450" placement="right">
        <n-drawer-content :title="editingId ? t('common.edit') : t('oauth2.createClient')" closable>
          <template #footer><n-space justify="end"><n-button @click="showModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="saveClient">{{ t('common.submit') }}</n-button></n-space></template>
          <n-form label-placement="left" label-width="80" size="small">
            <n-form-item :label="t('oauth2.clientId')"><n-input v-model:value="form.clientId" placeholder="my-app" /></n-form-item>
            <n-form-item :label="t('oauth2.name')"><n-input v-model:value="form.clientName" :placeholder="t('oauth2.namePlaceholder')" /></n-form-item>
            <n-form-item :label="t('oauth2.secret')"><n-input v-model:value="form.secret" :placeholder="t('oauth2.secretPlaceholder')" /></n-form-item>
            <n-form-item :label="t('oauth2.redirectUri')"><n-input v-model:value="form.redirectUris" /></n-form-item>
            <n-form-item :label="t('oauth2.scopes')"><n-input v-model:value="form.scopes" /></n-form-item>
          </n-form>
        </n-drawer-content>
      </n-drawer>
    </n-card>
  </div>
</template>
