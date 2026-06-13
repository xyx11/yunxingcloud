<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSpace, NTag, NPopconfirm, darkTheme, lightTheme } from 'naive-ui'
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
  { title: '客户端ID', key: 'client_id', width: 160 },
  { title: '名称', key: 'client_name', width: 120 },
  { title: '回调地址', key: 'redirect_uris', width: 180, ellipsis: { tooltip: true } },
  { title: '授权范围', key: 'scopes', width: 120 },
  { title: '创建时间', key: 'created_at', width: 150, render: (row: any) => row.created_at?.substring(0,19).replace('T',' ') || '-' },
  { title: '操作', key: 'actions', width: 80, render: (row) => h(NPopconfirm, { onPositiveClick: () => delClient(row.id) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => '删除' }), default: () => '确认删除?' }) },
]

async function loadClients() { loading.value = true; try { const r = await request.get('/api/oauth2/clients'); clients.value = r.data } catch {}; loading.value = false }
async function delClient(id: string) { await request.delete(`/api/oauth2/clients/${id}`); loadClients() }
async function saveClient() {
  saving.value = true
  try {
    const r = await request.post('/api/oauth2/clients', form.value)
    newSecret.value = r.data.clientSecret || ''
    showModal.value = false
    notify.success('创建成功')
    if (newSecret.value) notify.info('Client Secret: ' + newSecret.value)
    loadClients()
  } catch (e: any) { notify.error(e.response?.data?.message || '失败') }
  finally { saving.value = false }
}

onMounted(loadClients)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card title="OAuth2 客户端管理">
        <template #header-extra>
          <n-button type="primary" size="small" @click="showModal = true"><template #icon>＋</template>新增</n-button>
        </template>
        <n-dataTable :columns="columns" :data="clients" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10 }" :row-key="(r: Client) => r.id" />

        <n-modal v-model:show="showModal" title="新增OAuth2客户端" preset="card" display-directive="show" style="width:520px">
          <n-form label-placement="left" label-width="80">
            <n-form-item label="客户端ID"><n-input v-model:value="form.clientId" placeholder="my-app" /></n-form-item>
            <n-form-item label="名称"><n-input v-model:value="form.clientName" placeholder="我的应用" /></n-form-item>
            <n-form-item label="Secret"><n-input v-model:value="form.secret" placeholder="留空自动生成" /></n-form-item>
            <n-form-item label="回调地址"><n-input v-model:value="form.redirectUris" /></n-form-item>
            <n-form-item label="授权范围"><n-input v-model:value="form.scopes" /></n-form-item>
          </n-form>
          <template #footer><n-space justify="end"><n-button @click="showModal = false">取消</n-button><n-button type="primary" :loading="saving" @click="saveClient">创建</n-button></n-space></template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>
