<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import request from '@/api/request'
import { NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NInputNumber, NSpace, NPopconfirm, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

interface Dept { id: number; name: string; parentId: number | null; sortOrder: number; enabled: boolean; children: Dept[] }

const depts = ref<Dept[]>([])
const loading = ref(false)
const showModal = ref(false)
const editing = ref<Dept | null>(null)
const form = ref({ name: '', parentId: null as number | null, sortOrder: 0 })

const columns: DataTableColumns<Dept> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '名称', key: 'name', width: 160 },
  { title: '排序', key: 'sortOrder', width: 60 },
  {
    title: '状态', key: 'enabled', width: 60,
    render: (row) => h(NTag, { type: row.enabled ? 'success' : 'default', size: 'small' }, { default: () => row.enabled ? '启用' : '停用' })
  },
  {
    title: '操作', key: 'actions', width: 160,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => editDept(row) }, { default: () => '编辑' }),
        h(NPopconfirm, { onPositiveClick: () => delDept(row.id) }, {
          trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
          default: () => '确认删除?'
        })
      ]
    })
  },
]

async function loadDepts() {
  loading.value = true
  const res = await request.get('/api/departments')
  depts.value = res.data
  loading.value = false
}

function addDept() {
  editing.value = null
  form.value = { name: '', parentId: null, sortOrder: 0 }
  showModal.value = true
}

function editDept(dept: Dept) {
  editing.value = dept
  form.value = { name: dept.name, parentId: dept.parentId, sortOrder: dept.sortOrder }
  showModal.value = true
}

async function saveDept() {
  if (editing.value) {
    await request.put(`/api/departments/${editing.value.id}`, form.value)
  } else {
    await request.post('/api/departments', form.value)
  }
  showModal.value = false
  await loadDepts()
}

async function delDept(id: number) {
  await request.delete(`/api/departments/${id}`)
  await loadDepts()
}

onMounted(loadDepts)
</script>

<template>
  <n-config-provider>
    <div style="padding: 24px">
      <n-card title="部门管理">
        <template #header-extra>
          <n-button type="primary" size="small" @click="addDept">新增部门</n-button>
        </template>
        <n-data-table :columns="columns" :data="depts" :loading="loading" :pagination="{ pageSize: 10 }"
          default-expand-all :row-key="(row: Dept) => row.id" :children-key="'children'" />

        <n-modal v-model:show="showModal" :title="editing ? '编辑部门' : '新增部门'">
          <n-form label-placement="left" label-width="80">
            <n-form-item label="名称">
              <n-input v-model:value="form.name" />
            </n-form-item>
            <n-form-item label="上级ID">
              <n-input-number v-model:value="form.parentId" :min="0" placeholder="0为根部门" />
            </n-form-item>
            <n-form-item label="排序">
              <n-input-number v-model:value="form.sortOrder" :min="0" />
            </n-form-item>
          </n-form>
          <template #footer>
            <n-space justify="end">
              <n-button @click="showModal = false">取消</n-button>
              <n-button type="primary" @click="saveDept">保存</n-button>
            </n-space>
          </template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>
