<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import {
  NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem,
  NInput, NSelect, NSpace, NPopconfirm, NTag, NPopover, NCheckbox,
  darkTheme, lightTheme
} from 'naive-ui'
import { useColumnManager } from '@/composables/useColumnManager'
import { useI18n } from 'vue-i18n'
import type { DataTableColumns } from 'naive-ui'

interface SysConfig {
  id: number; name: string; configKey: string; configValue: string
  configType: string; createdAt: string
}

const { t } = useI18n()
const notify = useNotify()
const currentTheme = computed(() => localStorage.getItem("theme") === "dark" ? darkTheme : lightTheme)
const configs = ref<SysConfig[]>([])
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editing = ref<SysConfig | null>(null)
const form = ref({ name: '', configKey: '', configValue: '', configType: 'Y' })
const cfgSearch = ref("")
const cfgTypeFilter = ref('')
const filteredConfigs = computed(() => {
  let list = configs.value
  if (cfgTypeFilter.value) list = list.filter(c => c.configType === cfgTypeFilter.value)
  const kw = cfgSearch.value.toLowerCase()
  if (kw) list = list.filter(c => c.name.toLowerCase().includes(kw) || c.configKey.toLowerCase().includes(kw))
  return list
})

const typeOptions = [
  { label: '是 (Y)', value: 'Y' },
  { label: '否 (N)', value: 'N' },
]

const allColumns = ref<DataTableColumns<SysConfig>>([
  { title: 'ID', key: 'id', width: 60 },
  { title: '名称', key: 'name', width: 140, sorter: true },
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
])
const { visibleColumns, toggleColumn, hiddenKeys } = useColumnManager(allColumns, 'config')
const columnOptions = computed(() => (allColumns.value as any[])
  .filter((c: any) => c.key && c.key !== 'actions')
  .map((c: any) => ({ key: c.key, title: c.title })),
)

async function loadConfigs() {
  loading.value = true
  try {
    const res = await request.get('/api/config')
    configs.value = res.data
  } catch {}
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
  saving.value = true
  try {
    if (editing.value) await request.put(`/api/config/${editing.value.id}`, form.value)
    else await request.post('/api/config', form.value)
    showModal.value = false
    notify.success(editing.value ? '更新成功' : '创建成功')
    await loadConfigs()
    if (form.value.configKey?.startsWith('feature.')) {
      request.post('/api/config/refresh-flags').catch(() => {})
    }
  } catch (e: any) { notify.error(e.response?.data?.message || '保存失败') } finally { saving.value = false }
}

async function delConfig(id: number) {
  await request.delete(`/api/config/${id}`)
  await loadConfigs()
}

onMounted(loadConfigs)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card :title="t('nav.config')">
        <template #header-extra>
          <n-button type="primary" size="small" @click="addConfig"><template #icon>＋</template>新增</n-button>
        </template>
        <n-space style="margin-bottom:12px" justify="space-between">
          <n-space>
            <n-button size="small" :type="cfgTypeFilter===''?'primary':'default'" @click="cfgTypeFilter=''">全部</n-button>
            <n-button size="small" :type="cfgTypeFilter==='Y'?'primary':'default'" @click="cfgTypeFilter='Y'">系统内置</n-button>
            <n-button size="small" :type="cfgTypeFilter==='N'?'primary':'default'" @click="cfgTypeFilter='N'">用户配置</n-button>
            <n-input v-model:value="cfgSearch" placeholder="参数名称" size="small" clearable style="width:160px" />
            <n-button type="primary" size="small" @click="() => {}">搜索</n-button>
            <n-button size="small" @click="cfgSearch = ''; cfgTypeFilter = ''">重置</n-button>
          </n-space>
          <n-space>
            <n-button size="small" @click="loadConfigs" secondary>刷新</n-button>
            <n-popover trigger="click" placement="bottom-end" :width="180">
              <template #trigger>
                <n-button size="small" secondary>列选项</n-button>
              </template>
              <div style="max-height:300px;overflow-y:auto">
                <div v-for="opt in columnOptions" :key="opt.key" style="padding:2px 0">
                  <n-checkbox :checked="!hiddenKeys.has(opt.key)"
                              @update:checked="toggleColumn(opt.key)">
                    {{ opt.title }}
                  </n-checkbox>
                </div>
              </div>
            </n-popover>
          </n-space>
        </n-space>
        <n-data-table
          :columns="visibleColumns" :data="filteredConfigs" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }"
          :row-key="(row: SysConfig) => row.id"
        />

        <n-modal v-model:show="showModal" :title="editing ? t('common.edit') : t('common.add')" style="width:480px">
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
              <n-button @click="showModal = false">{{ t('common.cancel') }}</n-button>
              <n-button type="primary" :loading="saving" @click="saveConfig">{{ t('common.save') }}</n-button>
            </n-space>
          </template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>