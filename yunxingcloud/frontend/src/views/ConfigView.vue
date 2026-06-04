<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import {
  NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem,
  NInput, NSelect, NSpace, NPopconfirm, NTag
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

interface SysConfig {
  id: number; name: string; configKey: string; configValue: string
  configType: string; createdAt: string
}

const notify = useNotify()
const configs = ref<SysConfig[]>([])
const loading = ref(false)
const showModal = ref(false)
const editing = ref<SysConfig | null>(null)
const form = ref({ name: '', configKey: '', configValue: '', configType: 'Y' })

const typeOptions = [
  { label: '是 (Y)', value: 'Y' },
  { label: '否 (N)', value: 'N' },
]

const columns: DataTableColumns<SysConfig> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '名称', key: 'name', width: 140 },
  { title: '键名', key: 'configKey', width: 160 },
  { title: '键值', key: 'configValue', width: 200, ellipsis: { tooltip: true } },
  {
    title: '系统内置', key: 'configType', width: 80,
    render: (row) => h(NTag, { type: row.configType === 'Y' ? 'info' : 'default', size: 'small' },
      { default: () => row.configType === 'Y' ? '是' : '否' })
  },
  { title: '创建时间', key: 'createdAt', width: 160 },
  {
    title: '操作', key: 'actions', width: 140,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => editConfig(row) }, { default: () => '编辑' }),
        h(NPopconfirm, { onPositiveClick: () => delConfig(row.id) }, {
          trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
          default: () => '确认删除?'
        })
      ]
    })
  },
]

async function loadConfigs() {
  loading.value = true
  const res = await request.get('/api/config')
  configs.value = res.data
  loading.value = false
}

function addConfig() {
  editing.value = null
  form.value = { name: '', configKey: '', configValue: '', configType: 'Y' }
  showModal.value = true
}

function editConfig(config: SysConfig) {
  editing.value = config
  form.value = { name: config.name, configKey: config.configKey, configValue: config.configValue, configType: config.configType }
  showModal.value = true
}

async function saveConfig() {
  try {
    if (editing.value) {
      await request.put(`/api/config/${editing.value.id}`, form.value)
    } else {
      await request.post('/api/config', form.value)
    }
    showModal.value = false
    await loadConfigs()
  } catch (e: any) {
    notify.error(e.response?.data?.message || '保存失败')
  }
}

async function delConfig(id: number) {
  await request.delete(`/api/config/${id}`)
  await loadConfigs()
}

onMounted(loadConfigs)
</script>

<template>
  <n-config-provider>
    <div style="padding: 24px">
      <n-card title="参数配置">
        <template #header-extra>
          <n-button type="primary" size="small" @click="addConfig">新增参数</n-button>
        </template>
        <n-data-table :columns="columns" :data="configs" :loading="loading" :pagination="{ pageSize: 10 }"
          :row-key="(row: SysConfig) => row.id" />

        <n-modal v-model:show="showModal" :title="editing ? '编辑参数' : '新增参数'">
          <n-form label-placement="left" label-width="80">
            <n-form-item label="参数名称">
              <n-input v-model:value="form.name" />
            </n-form-item>
            <n-form-item label="参数键名">
              <n-input v-model:value="form.configKey" :disabled="!!editing" />
            </n-form-item>
            <n-form-item label="参数键值">
              <n-input v-model:value="form.configValue" />
            </n-form-item>
            <n-form-item label="系统内置">
              <n-select v-model:value="form.configType" :options="typeOptions" />
            </n-form-item>
          </n-form>
          <template #footer>
            <n-space justify="end">
              <n-button @click="showModal = false">取消</n-button>
              <n-button type="primary" @click="saveConfig">保存</n-button>
            </n-space>
          </template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>