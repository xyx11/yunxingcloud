<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, h } from 'vue'
import { fetchGeneratorTables, fetchGeneratorTableColumns, generateCode as genCodeApi } from '@/api/generator'
import { useNotify } from '@/composables/useNotify'
import JSZip from 'jszip'

import {
  NCard, NDataTable, NButton, NInput, NModal, NSpace, NTag, NCode, NSpin,
  
} from 'naive-ui'

const { t } = useI18n()
import type { DataTableColumns } from 'naive-ui'


interface TableInfo { TABLE_NAME: string; TABLE_COMMENT: string }
interface ColumnInfo { COLUMN_NAME: string; DATA_TYPE: string; COLUMN_COMMENT: string; IS_NULLABLE: string; COLUMN_KEY: string }

const notify = useNotify()
const tables = ref<TableInfo[]>([])
const loading = ref(false)
const showColumnsModal = ref(false)
const showCodeModal = ref(false)
const selectedTable = ref('')
const columns = ref<ColumnInfo[]>([])
const generatedCode = ref<Record<string, string>>({})
const packageName = ref('com.yunxingcloud')

const tableColumns: DataTableColumns<TableInfo> = [
  { title: t('generator.tableName'), key: 'TABLE_NAME', width: 200 },
  { title: t('generator.comment'), key: 'TABLE_COMMENT', width: 200 },
  {
    title: t('generator.actions'), key: 'actions', width: 200,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => viewColumns(row.TABLE_NAME) }, { default: () => t('generator.viewColumns') }),
        h(NButton, { size: 'small', type: 'primary', loading: generatingTable.value === row.TABLE_NAME, onClick: () => genCode(row.TABLE_NAME) }, { default: () => t('generator.generate') }),
      ]
    })
  },
]

const colsColumns: DataTableColumns<ColumnInfo> = [
  { title: t('generator.columnName'), key: 'COLUMN_NAME', width: 140 },
  { title: t('generator.dataType'), key: 'DATA_TYPE', width: 100 },
  { title: t('generator.comment'), key: 'COLUMN_COMMENT', width: 160 },
  {
    title: t('generator.nullable'), key: 'IS_NULLABLE', width: 60,
    render: (row) => h(NTag, { type: row.IS_NULLABLE === 'YES' ? 'warning' : 'info', size: 'small' },
      { default: () => row.IS_NULLABLE })
  },
  {
    title: t('generator.key'), key: 'COLUMN_KEY', width: 60,
    render: (row) => row.COLUMN_KEY ? h(NTag, { type: 'success', size: 'small' }, { default: () => row.COLUMN_KEY }) : null
  },
]

const codeTabs = [
  { label: 'Entity', key: 'entity' },
  { label: 'Controller', key: 'controller' },
  { label: 'Repository', key: 'repository' },
  { label: 'Service', key: 'service' },
  { label: 'Migration', key: 'migration' },
  { label: 'MenuSQL', key: 'menuSql' },
  { label: 'VueApi', key: 'vueApi' },
  { label: 'VueView', key: 'vueView' },
]


const activeCodeTab = ref('entity')
const generatingTable = ref('')

function copyCode() {
  const code = generatedCode.value[activeCodeTab.value] || ''
  navigator.clipboard.writeText(code).then(() => notify.success(t('generator.copiedToClipboard')))
}
async function downloadZip() {
  const zip = new JSZip()
  for (const [name, code] of Object.entries(generatedCode.value)) {
    zip.file(name.replace('/','_') + '.java', code as string)
  }
  const blob = await zip.generateAsync({type:'blob'})
  const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = `${selectedTable.value || 'generated'}.zip`; a.click()
  notify.success(t('generator.exportSuccess'))
}

async function loadTables() {
  loading.value = true
  try {
    const res = await fetchGeneratorTables()
    tables.value = res.data
  } catch { notify.error(t('generator.fetchFailed')); }
  loading.value = false
}

async function viewColumns(tableName: string) {
  selectedTable.value = tableName
  const res = await fetchGeneratorTableColumns(tableName)
  columns.value = res.data.columns
  showColumnsModal.value = true
}

async function genCode(tableName: string) {
  selectedTable.value = tableName
  generatingTable.value = tableName
  try {
    const res = await genCodeApi(tableName, packageName.value)
    generatedCode.value = res.data
    activeCodeTab.value = 'entity'
    showCodeModal.value = true
  } catch { notify.error(t('generator.genFailed')) }
  generatingTable.value = ''
}

onMounted(loadTables)
</script>

<template>
  <div style="padding:20px">
    <n-card :title="t('nav.generator')">
      <template #header-extra>
        <n-button size="small" @click="loadTables" style="margin-right:8px" secondary>{{ t('common.refresh') }}</n-button>
        <n-input v-model:value="packageName" :placeholder="t('generator.packageName')" style="width:200px;margin-right:8px" />
      </template>
      <n-data-table
        :columns="tableColumns" :data="tables" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }"
        :row-key="(row: TableInfo) => row.TABLE_NAME"
      />

      <!-- 字段查看弹窗 -->
      <n-modal v-model:show="showColumnsModal" :title="`${t('generator.tableColumns')}: ${selectedTable}`" style="max-width:700px;width:95%">
        <n-data-table
          :columns="colsColumns" :data="columns"
          :row-key="(row: ColumnInfo) => row.COLUMN_NAME" :max-height="400"
        />
      </n-modal>

      <!-- 代码预览弹窗 -->
      <n-modal v-model:show="showCodeModal" :title="`${t('generator.genCodeTitle')}: ${selectedTable}`" style="max-width:800px;width:95%">
        <n-space vertical style="margin-bottom:12px">
          <n-space justify="space-between">
            <n-space>
              <n-button
                v-for="tab in codeTabs" :key="tab.key"
                :type="activeCodeTab === tab.key ? 'primary' : 'default'"
                size="small" @click="activeCodeTab = tab.key"
              >
                {{ tab.label }}
              </n-button>
            </n-space>
            <n-space><n-button size="small" @click="copyCode">{{ t('generator.copyCode') }}</n-button><n-button size="small" type="primary" @click="downloadZip">📥 ZIP</n-button></n-space>
          </n-space>
        </n-space>
        <n-spin :show="!!generatingTable">
          <n-code
            :code="generatedCode[activeCodeTab] || ''"
            :language="activeCodeTab.startsWith('vue') ? 'html' : 'java'"
            style="max-height:500px; overflow:auto"
          />
        </n-spin>
      </n-modal>
    </n-card>
  </div>
</template>
