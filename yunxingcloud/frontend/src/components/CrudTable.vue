<script setup lang="ts">
import { computed } from 'vue'
import {
  NCard, NDataTable, NButton, NModal, NForm,
  NInput, NSpace, NPopover,
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
  tableId?: string
  rowKey?: string
  pageSize?: number
  searchKeyword?: string
}>(), {
  loading: false,
  saving: false,
  showModal: false,
  editing: null,
  searchable: true,
  creatable: true,
  deletable: true,
  tableId: '',
  rowKey: 'id',
  pageSize: 10,
  searchKeyword: '',
})

const emit = defineEmits<{
  'update:searchKeyword': [value: string]
  'update:showModal': [value: boolean]
  'add': []
  'edit': [row: any]
  'save': []
  'delete': [id: number]
  'refresh': []
  'search': []
}>()

const searchKeyword = computed({
  get: () => props.searchKeyword ?? '',
  set: (val) => emit('update:searchKeyword', val),
})
</script>

<template>
  <div style="padding:20px">
    <n-card :title="title">
      <template v-if="creatable" #header-extra>
        <slot name="header-extra">
          <n-button type="primary" size="small" @click="emit('add')">+</n-button>
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
            />
            <n-button v-if="searchable" type="primary" size="small" @click="emit('search')">搜索</n-button>
            <n-button v-if="searchable" size="small" @click="searchKeyword = ''">重置</n-button>
          </slot>
        </n-space>
        <n-space>
          <slot name="toolbar-extra" />
          <n-popover v-if="tableId" trigger="click" placement="bottom-end" :width="180">
            <template #trigger>
              <n-button size="small" secondary>列选项</n-button>
            </template>
            <slot name="column-options" />
          </n-popover>
          <n-button size="small" @click="emit('refresh')" secondary>刷新</n-button>
        </n-space>
      </n-space>

      <n-dataTable
        :columns="columns"
        :data="data"
        :loading="loading"
        size="small"
        :bordered="false"
        :pagination="{ pageSize, pageSizes: [10,20,50,100] }"
        :row-key="(row: any) => row[rowKey]"
      />

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
