<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { fetchConfigs, createConfig, updateConfig, deleteConfig, refreshFeatureFlags } from '@/api/config'
import { useNotify } from '@/composables/useNotify'

import {
  NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem,
  NInput, NSelect, NSpace, NPopconfirm, NTag, NPopover, NCheckbox,
  
} from 'naive-ui'
import { useColumnManager } from '@/composables/useColumnManager'
import { useI18n } from 'vue-i18n'
import type { DataTableColumn, DataTableColumns, FormRules, FormInst } from 'naive-ui'

interface SysConfig {
  id: number; name: string; configKey: string; configValue: string
  configType: string; createdAt: string
}

const { t } = useI18n()
const notify = useNotify()

const configs = ref<SysConfig[]>([])
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editing = ref<SysConfig | null>(null)
const formRef = ref<FormInst>()
const rules: FormRules = {
  name: [{ required: true, message: t('validate.required'), trigger: 'blur' }],
  configKey: [{ required: true, message: t('validate.required'), trigger: 'blur' }],
}
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

const typeOptions = computed(() => [
  { label: `${t('common.yes')} (Y)`, value: 'Y' },
  { label: `${t('common.no')} (N)`, value: 'N' },
])

const allColumns = ref<DataTableColumns<SysConfig>>([
  { title: 'ID', key: 'id', width: 60 },
  { title: t('config.name'), key: 'name', width: 140, sorter: true },
  { title: t('config.key'), key: 'configKey', width: 160 },
  { title: t('config.value'), key: 'configValue', width: 200, ellipsis: { tooltip: true } },
  {
    title: t('config.builtin'), key: 'configType', width: 80,
    render: (row) => h(NTag, { type: row.configType === 'Y' ? 'info' : 'default', size: 'small' },
      { default: () => row.configType === 'Y' ? t('common.yes') : t('common.no') })
  },
  { title: t('common.createdAt'), key: 'createdAt', width: 160 },
  {
    title: t('user.actions'), key: 'actions', width: 140,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => editConfig(row) }, { default: () => t('common.edit') }),
        h(NPopconfirm, { onPositiveClick: () => delConfig(row.id) }, {
          trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => t('common.delete') }),
          default: () => t('common.confirmDelete')
        })
      ]
    })
  },
])
const { visibleColumns, toggleColumn, hiddenKeys } = useColumnManager(allColumns, 'config')
const columnOptions = computed(() => (allColumns.value as DataTableColumns<SysConfig>)
  .filter((c: DataTableColumn<SysConfig>) => c.key && c.key !== 'actions')
  .map((c: DataTableColumn<SysConfig>) => ({ key: c.key, title: c.title?.toString() || '' })),
)

async function loadConfigs() {
  loading.value = true
  try {
    const res = await fetchConfigs()
    configs.value = res.data
  } catch { notify.error(t('common.error')); }
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
  if (formRef.value) { try { await formRef.value.validate() } catch { return } }
  saving.value = true
  try {
    if (editing.value) await updateConfig(editing.value.id, form.value)
    else await createConfig(form.value)
    showModal.value = false
    notify.success(editing.value ? t('user.updateSuccess') : t('user.createSuccess'))
    await loadConfigs()
    if (form.value.configKey?.startsWith('feature.')) {
      refreshFeatureFlags().catch(() => {})
    }
  } catch (e: unknown) { const err = e as { response?: { data?: { message?: string } } }; notify.error(err.response?.data?.message || t('common.saveFailed')) } finally { saving.value = false }
}

async function delConfig(id: number) {
  await deleteConfig(id)
  await loadConfigs()
}

onMounted(loadConfigs)
</script>

<template>
  <div class="view-pad">
    <n-card :title="t('nav.config')">
      <template #header-extra>
        <n-button type="primary" size="small" @click="addConfig"><template #icon>＋</template>{{ t('common.add') }}</n-button>
      </template>
      <n-space class="mb-12" justify="space-between">
        <n-space>
          <n-button size="small" :type="cfgTypeFilter===''?'primary':'default'" @click="cfgTypeFilter=''">{{ t('common.all') }}</n-button>
          <n-button size="small" :type="cfgTypeFilter==='Y'?'primary':'default'" @click="cfgTypeFilter='Y'">{{ t('config.builtin') }}</n-button>
          <n-button size="small" :type="cfgTypeFilter==='N'?'primary':'default'" @click="cfgTypeFilter='N'">{{ t('config.userConfig') }}</n-button>
          <n-input v-model:value="cfgSearch" :placeholder="t('config.searchPlaceholder')" size="small" clearable class="w-160" />
          <n-button type="primary" size="small" @click="() => {}">{{ t('common.search') }}</n-button>
          <n-button size="small" @click="cfgSearch = ''; cfgTypeFilter = ''">{{ t('common.reset') }}</n-button>
        </n-space>
        <n-space>
          <n-button size="small" @click="loadConfigs" secondary>{{ t('common.refresh') }}</n-button>
          <n-popover trigger="click" placement="bottom-end" :width="180">
            <template #trigger>
              <n-button size="small" secondary>{{ t('common.columnOptions') }}</n-button>
            </template>
            <div class="scroll-y">
              <div v-for="opt in columnOptions" :key="opt.key" class="py-2">
                <n-checkbox
                  :checked="!hiddenKeys.has(opt.key)"
                  @update:checked="toggleColumn(opt.key)"
                >
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

      <n-drawer v-model:show="showModal" :width="420" placement="right">
        <n-drawer-content :title="editing ? t('common.edit') : t('common.add')" closable>
          <template #footer><n-space justify="end"><n-button @click="showModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="saveConfig">{{ t('common.save') }}</n-button></n-space></template>
          <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80" size="small">
            <n-form-item :label="t('config.nameLabel')"><n-input v-model:value="form.name" /></n-form-item>
            <n-form-item :label="t('config.keyLabel')"><n-input v-model:value="form.configKey" :disabled="!!editing" /></n-form-item>
            <n-form-item :label="t('config.valueLabel')"><n-input v-model:value="form.configValue" /></n-form-item>
            <n-form-item :label="t('config.builtin')"><n-select v-model:value="form.configType" :options="typeOptions" /></n-form-item>
          </n-form>
        </n-drawer-content>
      </n-drawer>
    </n-card>
  </div>
</template>

<style scoped>
.w-160 { width: 160px; }
</style>