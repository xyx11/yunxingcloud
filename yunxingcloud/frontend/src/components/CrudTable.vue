<script setup lang="ts">
import { computed, ref, watch, h } from 'vue'
import {
  NCard, NDataTable, NButton, NModal, NForm,
  NInput, NSpace, NPopover, NPopconfirm, NCheckbox,
} from 'naive-ui'

const props = withDefaults(defineProps<{
  title: string
  columns: any[]
  data: any[]
  loading?: boolean
  saving?: boolean
  showModal?: boolean
  editing?: any
  searchable?: boolean
  creatable?: boolean
  deletable?: boolean
  editable?: boolean
  tableId?: string
  rowKey?: string
  pageSize?: number
  searchKeyword?: string
}>(), {
  loading: false, saving: false, showModal: false, editing: null,
  searchable: true, creatable: true, deletable: true, editable: false,
  tableId: '', rowKey: 'id', pageSize: 10, searchKeyword: '',
})

const emit = defineEmits<{
  'update:searchKeyword': [value: string]
  'update:showModal': [value: boolean]
  'add': []
  'edit': [row: any]
  'save': []
  'delete': [id: number]
  'batchDelete': [ids: number[]]
  'refresh': []
  'search': []
}>()

const checkedRowKeys = ref<(string|number)[]>([])
function doBatchDelete() { if (checkedRowKeys.value.length) { emit('batchDelete', checkedRowKeys.value.map(Number)); checkedRowKeys.value = [] } }

const searchKeyword = computed({
  get: () => props.searchKeyword ?? '',
  set: (val) => emit('update:searchKeyword', val),
})

// Column visibility
const allColKeys = computed(() => props.columns.map((c: any) => c.key || c.title))
const visibleKeys = ref<string[]>([])
watch(() => props.columns, (cols) => {
  if (visibleKeys.value.length === 0) visibleKeys.value = cols.map((c: any) => c.key || c.title)
}, { immediate: true })

const visibleCols = computed(() => props.columns.filter((c: any) => visibleKeys.value.includes(c.key || c.title)))

function toggleCol(key: string) {
  const i = visibleKeys.value.indexOf(key)
  if (i >= 0) visibleKeys.value.splice(i, 1); else visibleKeys.value.push(key)
}

// Add delete action column
const displayColumns = computed(() => {
  const cols = [...visibleCols.value]
  if (props.deletable) {
    cols.push({
      title: '', key: '_actions', width: 60, fixed: 'right' as const,
      render: (row: any) => h(NPopconfirm, { onPositiveClick: () => emit('delete', row[props.rowKey]) },
        { trigger: () => h(NButton, { size: 'tiny', type: 'error', text: true }, { default: () => '删除' }),
          default: () => '确认删除？' })
    })
  }
  return cols
})
</script>

<template>
  <div style="padding:20px">
    <n-card :title="title">
      <template v-if="creatable" #header-extra>
        <slot name="header-extra">
          <n-space><n-popconfirm v-if="checkedRowKeys.length" @positive-click="doBatchDelete"><template #trigger><n-button type="error" size="small">批量删除 ({{checkedRowKeys.length}})</n-button></template>确认删除选中项？</n-popconfirm><n-button type="primary" size="small" @click="emit('add')">+</n-button></n-space>
        </slot>
      </template>

      <n-space style="margin-bottom:12px" justify="space-between">
        <n-space>
          <slot name="search-extra">
            <n-input
              v-if="searchable"
              v-model:value="searchKeyword"
              placeholder="搜索"
              size="small"
              clearable
              style="width:180px"
              @keyup.enter="emit('search')"
            />
            <n-button v-if="searchable" type="primary" size="small" @click="emit('search')">搜索</n-button>
            <n-button v-if="searchable" size="small" @click="searchKeyword = ''; emit('search')">重置</n-button>
          </slot>
        </n-space>
        <n-space>
          <slot name="toolbar-extra" />
          <n-popover trigger="click" placement="bottom-end" :width="180">
            <template #trigger>
              <n-button size="small" secondary>列选项</n-button>
            </template>
            <div style="max-height:260px;overflow-y:auto;padding:8px">
              <n-checkbox v-for="k in allColKeys" :key="k" :checked="visibleKeys.includes(k)"
                @update:checked="toggleCol(k)" style="display:block;margin:4px 0;font-size:13px">{{ k }}</n-checkbox>
            </div>
          </n-popover>
          <n-button size="small" @click="emit('refresh')" secondary>刷新</n-button>
        </n-space>
      </n-space>

      <n-dataTable
        :columns="displayColumns"
        :data="data"
        :loading="loading"
        size="small"
        :bordered="false"
        :pagination="{ pageSize, pageSizes: [10,20,50,100] }"
        :row-key="(row: any) => row[rowKey]"
        v-model:checked-row-keys="checkedRowKeys"
        :row-props="editable ? (row: any) => ({ style: 'cursor:pointer', onClick: () => emit('edit', row) }) : undefined"
      >
        <template #empty>暂无数据</template>
      </n-dataTable>

      <n-modal
        :show="showModal"
        @update:show="emit('update:showModal', $event)"
        :title="editing ? '编辑' : '新增'"
        style="max-width:480px;width:95%"
        preset="card"
        display-directive="show"
      >
        <n-form label-placement="left" label-width="80">
          <slot name="form" />
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="emit('update:showModal', false)">取消</n-button>
            <n-button type="primary" :loading="saving" @click="emit('save')">保存</n-button>
          </n-space>
        </template>
      </n-modal>
    </n-card>
  </div>
</template>
