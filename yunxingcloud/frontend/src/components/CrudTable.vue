<script setup lang="ts">
import { computed, ref, watch, h } from 'vue'
import { useI18n } from 'vue-i18n'
import type { DataTableColumn } from 'naive-ui'
import {
  NCard, NDataTable, NButton, NModal, NForm,
  NInput, NSpace, NPopover, NPopconfirm, NCheckbox,
} from 'naive-ui'

const { t } = useI18n()

type RowData = Record<string, unknown>

const props = withDefaults(defineProps<{
  title: string
  columns: DataTableColumn<RowData>[]
  data: RowData[]
  loading?: boolean
  saving?: boolean
  showModal?: boolean
  editing?: RowData | null
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
  'edit': [row: RowData]
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

const allColKeys = computed(() => props.columns.map((c: DataTableColumn<RowData>) => c.key || c.title))
const visibleKeys = ref<string[]>([])
watch(() => props.columns, (cols: DataTableColumn<RowData>[]) => {
  if (visibleKeys.value.length === 0) visibleKeys.value = cols.map((c: DataTableColumn<RowData>) => c.key || String(c.title))
}, { immediate: true })

const visibleCols = computed(() => props.columns.filter((c: DataTableColumn<RowData>) => visibleKeys.value.includes(c.key || String(c.title))))

function toggleCol(key: string) {
  const i = visibleKeys.value.indexOf(key)
  if (i >= 0) visibleKeys.value.splice(i, 1); else visibleKeys.value.push(key)
}

const displayColumns = computed(() => {
  const cols = [...visibleCols.value]
  if (props.deletable) {
    cols.push({
      title: '', key: '_actions', width: 60, fixed: 'right' as const,
      render: (row: RowData) => h(NPopconfirm, { onPositiveClick: () => emit('delete', row[props.rowKey] as number) },
        { trigger: () => h(NButton, { size: 'tiny', type: 'error', text: true }, { default: () => t('common.delete') }),
          default: () => t('common.confirmDelete') })
    })
  }
  return cols
})
</script>

<template>
  <div class="crud-wrap">
    <n-card :title="title">
      <template v-if="creatable" #header-extra>
        <slot name="header-extra">
          <n-space>
            <n-popconfirm v-if="checkedRowKeys.length" @positive-click="doBatchDelete">
              <template #trigger><n-button type="error" size="small">{{ t('common.batchDelete') }} ({{ checkedRowKeys.length }})</n-button></template>
              {{ t('common.confirmBatchDelete') }}
            </n-popconfirm>
            <n-button type="primary" size="small" @click="emit('add')">+</n-button>
          </n-space>
        </slot>
      </template>

      <n-space class="crud-toolbar" justify="space-between">
        <n-space>
          <slot name="search-extra">
            <n-input
              v-if="searchable"
              v-model:value="searchKeyword"
              :placeholder="t('common.search')"
              size="small"
              clearable
              class="crud-search-input"
              @keyup.enter="emit('search')"
            />
            <n-button v-if="searchable" type="primary" size="small" @click="emit('search')">{{ t('common.search') }}</n-button>
            <n-button v-if="searchable" size="small" @click="searchKeyword = ''; emit('search')">{{ t('common.reset') }}</n-button>
          </slot>
        </n-space>
        <n-space>
          <slot name="toolbar-extra" />
          <n-popover trigger="click" placement="bottom-end" :width="180">
            <template #trigger>
              <n-button size="small" secondary>{{ t('common.columnOptions') }}</n-button>
            </template>
            <div class="crud-col-options">
              <n-checkbox v-for="k in allColKeys" :key="k" :checked="visibleKeys.includes(k)"
                @update:checked="toggleCol(k)" class="crud-col-check">{{ k }}</n-checkbox>
            </div>
          </n-popover>
          <n-button size="small" @click="emit('refresh')" secondary>{{ t('common.refresh') }}</n-button>
        </n-space>
      </n-space>

      <n-dataTable
        :columns="displayColumns"
        :data="data"
        :loading="loading"
        size="small"
        :bordered="false"
        :pagination="{ pageSize, pageSizes: [10,20,50,100] }"
        :row-key="(row: RowData) => row[rowKey] as string | number"
        v-model:checked-row-keys="checkedRowKeys"
        :row-props="editable ? (row: RowData) => ({ style: 'cursor:pointer', onClick: () => emit('edit', row) }) : undefined"
      >
        <template #empty>{{ t('common.noData') }}</template>
      </n-dataTable>

      <n-modal
        :show="showModal"
        @update:show="emit('update:showModal', $event)"
        :title="editing ? t('common.edit') : t('common.add')"
        class="crud-modal"
        preset="card"
        display-directive="show"
      >
        <n-form label-placement="left" label-width="80">
          <slot name="form" />
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="emit('update:showModal', false)">{{ t('common.cancel') }}</n-button>
            <n-button type="primary" :loading="saving" @click="emit('save')">{{ t('common.save') }}</n-button>
          </n-space>
        </template>
      </n-modal>
    </n-card>
  </div>
</template>

<style scoped>
.crud-wrap { padding: 20px; }
.crud-toolbar { margin-bottom: 12px; }
.crud-search-input { width: 180px; }
.crud-col-options { max-height: 260px; overflow-y: auto; padding: 8px; }
.crud-col-check { display: block; margin: 4px 0; font-size: 13px; }
.crud-modal { max-width: 480px; width: 95%; }
</style>
