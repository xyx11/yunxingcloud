<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSpace, NPopconfirm, NTag, darkTheme, lightTheme } from 'naive-ui'
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
  { title: 'IP地址', key: 'ip', width: 160 },
  { title: '封禁原因', key: 'reason', width: 200, ellipsis: { tooltip: true } },
  { title: '封禁时间', key: 'createdAt', width: 160, render: (row: any) => row.createdAt?.substring(0,19).replace('T',' ') || '-' },
  { title: '操作', key: 'actions', width: 80, render: (row) => h(NPopconfirm, { onPositiveClick: () => delItem(row.id) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => '解除' }), default: () => '确认解除?' }) },
]

async function loadItems() { loading.value = true; try { const r = await request.get('/api/ip-blacklist'); items.value = r.data } catch {}; loading.value = false }
async function delItem(id: number) { await request.delete(`/api/ip-blacklist/${id}`); await loadItems() }
async function saveItem() {
  saving.value = true
  try { await request.post('/api/ip-blacklist', form.value); showModal.value = false; form.value = { ip: '', reason: '' }; notify.success('已封禁'); await loadItems() }
  catch (e: any) { notify.error(e.response?.data?.message || '操作失败') }
  finally { saving.value = false }
}

onMounted(loadItems)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card title="IP黑名单">
        <template #header-extra>
          <n-button type="primary" size="small" @click="showModal = true"><template #icon>＋</template>封禁IP</n-button>
        </template>
        <n-dataTable :columns="columns" :data="items" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10 }" :row-key="(row: IpItem) => row.id" />

        <n-modal v-model:show="showModal" title="封禁IP" preset="card" display-directive="show" style="width:400px">
          <n-form label-placement="left" label-width="80">
            <n-form-item label="IP地址"><n-input v-model:value="form.ip" placeholder="192.168.1.100" /></n-form-item>
            <n-form-item label="原因"><n-input v-model:value="form.reason" placeholder="暴力破解" /></n-form-item>
          </n-form>
          <template #footer><n-space justify="end"><n-button @click="showModal = false">取消</n-button><n-button type="primary" :loading="saving" @click="saveItem">确定</n-button></n-space></template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>
