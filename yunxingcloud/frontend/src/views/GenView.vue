<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import request from '@/api/request'
import {
  NConfigProvider, NCard, NDataTable, NButton, NInput, NModal, NSpace, NTag, NCode
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

interface TableInfo { TABLE_NAME: string; TABLE_COMMENT: string }
interface ColumnInfo { COLUMN_NAME: string; DATA_TYPE: string; COLUMN_COMMENT: string; IS_NULLABLE: string; COLUMN_KEY: string }

const tables = ref<TableInfo[]>([])
const loading = ref(false)
const showColumnsModal = ref(false)
const showCodeModal = ref(false)
const selectedTable = ref('')
const columns = ref<ColumnInfo[]>([])
const generatedCode = ref<Record<string, string>>({})
const packageName = ref('com.yunxingcloud')

const tableColumns: DataTableColumns<TableInfo> = [
  { title: '表名', key: 'TABLE_NAME', width: 200 },
  { title: '注释', key: 'TABLE_COMMENT', width: 200 },
  {
    title: '操作', key: 'actions', width: 200,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => viewColumns(row.TABLE_NAME) }, { default: () => '查看字段' }),
        h(NButton, { size: 'small', type: 'primary', onClick: () => genCode(row.TABLE_NAME) }, { default: () => '生成代码' }),
      ]
    })
  },
]

const colsColumns: DataTableColumns<ColumnInfo> = [
  { title: '字段名', key: 'COLUMN_NAME', width: 140 },
  { title: '类型', key: 'DATA_TYPE', width: 100 },
  { title: '注释', key: 'COLUMN_COMMENT', width: 160 },
  {
    title: '可空', key: 'IS_NULLABLE', width: 60,
    render: (row) => h(NTag, { type: row.IS_NULLABLE === 'YES' ? 'warning' : 'info', size: 'small' },
      { default: () => row.IS_NULLABLE })
  },
  {
    title: '键', key: 'COLUMN_KEY', width: 60,
    render: (row) => row.COLUMN_KEY ? h(NTag, { type: 'success', size: 'small' }, { default: () => row.COLUMN_KEY }) : null
  },
]

const codeTabs = [
  { label: 'Entity', key: 'entity' },
  { label: 'Controller', key: 'controller' },
  { label: 'Repository', key: 'repository' },
  { label: 'Service', key: 'service' },
]

const activeCodeTab = ref('entity')

async function loadTables() {
  loading.value = true
  try {
    const res = await request.get('/api/generator/tables')
    tables.value = res.data
  } catch { alert('获取表列表失败，请确保使用MySQL数据库'); }
  loading.value = false
}

async function viewColumns(tableName: string) {
  selectedTable.value = tableName
  const res = await request.get(`/api/generator/table/${tableName}`)
  columns.value = res.data.columns
  showColumnsModal.value = true
}

async function genCode(tableName: string) {
  selectedTable.value = tableName
  const res = await request.post(`/api/generator/generate/${tableName}`, { packageName: packageName.value })
  generatedCode.value = res.data
  activeCodeTab.value = 'entity'
  showCodeModal.value = true
}

onMounted(loadTables)
</script>

<template>
  <n-config-provider>
    <div style="padding: 24px">
      <n-card title="代码生成">
        <template #header-extra>
          <n-input v-model:value="packageName" placeholder="包名" style="width:200px;margin-right:8px" />
        </template>
        <n-data-table :columns="tableColumns" :data="tables" :loading="loading"
          :row-key="(row: TableInfo) => row.TABLE_NAME" />

        <!-- 字段查看弹窗 -->
        <n-modal v-model:show="showColumnsModal" :title="`表字段: ${selectedTable}`" style="width:700px">
          <n-data-table :columns="colsColumns" :data="columns"
            :row-key="(row: ColumnInfo) => row.COLUMN_NAME" :max-height="400" />
        </n-modal>

        <!-- 代码预览弹窗 -->
        <n-modal v-model:show="showCodeModal" :title="`生成代码: ${selectedTable}`" style="width:800px">
          <n-space vertical style="margin-bottom:12px">
            <n-space>
              <n-button v-for="tab in codeTabs" :key="tab.key"
                :type="activeCodeTab === tab.key ? 'primary' : 'default'"
                size="small" @click="activeCodeTab = tab.key">{{ tab.label }}</n-button>
            </n-space>
          </n-space>
          <n-code :code="generatedCode[activeCodeTab] || ''" language="java"
            style="max-height:500px; overflow:auto" />
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>