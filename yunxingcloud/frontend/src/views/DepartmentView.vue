<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'

import { fetchDepartmentTree, createDepartment, updateDepartment, deleteDepartment, moveDepartment } from '@/api/department'
import { useNotify } from '@/composables/useNotify'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NInputNumber, NSpace, NPopconfirm, NTag, NSelect } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

interface Dept { id: number; name: string; parentId: number | null; sortOrder: number; enabled: boolean; children: Dept[] }

const { t } = useI18n()

const depts = ref<Dept[]>([])
const notify = useNotify()
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editing = ref<Dept | null>(null)
const form = ref({ name: '', parentId: null as number | null, sortOrder: 0 })
const deptSearch = ref("")
function doSearch() { deptSearch.value = deptSearch.value }
const flatDepts = computed(() => { const r: {label:string,value:number}[] = []; function walk(ds: Dept[]) { ds.forEach(d => { r.push({label:d.name,value:d.id}); if(d.children) walk(d.children) }) }; walk(depts.value); return r })
function filterTree(list: Dept[], kw: string): Dept[] {
  if (!kw) return list
  return list.reduce((acc: Dept[], d: Dept) => {
    const match = d.name.toLowerCase().includes(kw)
    const kids = d.children ? filterTree(d.children, kw) : []
    if (match || kids.length) acc.push({ ...d, children: kids })
    return acc
  }, [] as Dept[])
}
const filteredDepts = computed(() => filterTree(depts.value, deptSearch.value.toLowerCase()))

const columns = computed<DataTableColumns<Dept>>(() => [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('department.name'), key: 'name', width: 160 },
  { title: t('department.sort'), key: 'sortOrder', width: 60 },
  {
    title: t('department.enabled'), key: 'enabled', width: 60,
    render: (row) => h(NTag, { type: row.enabled ? 'success' : 'default', size: 'small' }, { default: () => row.enabled ? t('user.enabledLabel') : t('user.disabledLabel') })
  },
  {
    title: t('user.actions'), key: 'actions', width: 200,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'tiny', onClick: () => moveDept(row.id, -1) }, { title: t('menu.moveUpTitle'), default: () => '↑' }),
        h(NButton, { size: 'tiny', onClick: () => moveDept(row.id, 1) }, { title: t('menu.moveDownTitle'), default: () => '↓' }),
        h(NButton, { size: 'tiny', onClick: () => editDept(row) }, { default: () => t('common.edit') }),
        h(NPopconfirm, { onPositiveClick: () => delDept(row.id) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }),
          default: () => t('common.confirmDelete')
        })
      ]
    })
  },
])

async function loadDepts() {
  loading.value = true
  try {
    const res = await fetchDepartmentTree()
    depts.value = res.data
  } catch { notify.error(t('common.error')); }
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
  saving.value = true
  try {
    if (editing.value) {
      await updateDepartment(editing.value.id, form.value)
    } else {
      await createDepartment(form.value)
    }
    showModal.value = false
    notify.success(editing.value ? t('department.updateSuccess') : t('department.createSuccess'))
    await loadDepts()
  } catch (e: any) { notify.error(e.response?.data?.message || t('common.saveFailed')) } finally { saving.value = false }
}

async function moveDept(id: number, direction: number) {
  await moveDepartment(id, direction)
  await loadDepts()
}

async function delDept(id: number) {
  await deleteDepartment(id)
  notify.success(t('department.deleteSuccess'))
  await loadDepts()
}

onMounted(loadDepts)
</script>

<template>
  <div style="padding:20px">
    <n-card :title="t('nav.departments')">
      <template #header-extra>
        <n-button type="primary" size="small" @click="addDept"><template #icon>＋</template>{{ t('common.add') }}</n-button>
      </template>
      <n-space style="margin-bottom:12px" justify="space-between">
        <n-space>
          <n-input v-model:value="deptSearch" @keyup.enter="doSearch" :placeholder="t('department.name')" size="small" clearable style="max-width:180px;width:95%" />
          <n-button type="primary" size="small" @click="doSearch">{{ t('common.search') }}</n-button>
          <n-button size="small" @click="deptSearch = ''">{{ t('common.reset') }}</n-button>
        </n-space>
        <n-space>
          <n-button size="small" @click="loadDepts" secondary>{{ t('common.refresh') }}</n-button>
        </n-space>
      </n-space>
      <n-data-table
        :columns="columns" :data="filteredDepts" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }"
        default-expand-all :row-key="(row: Dept) => row.id" :children-key="'children'"
      />

      <n-modal v-model:show="showModal" :title="editing ? t('common.edit') : t('common.add')" style="max-width:480px;width:95%" preset="card" display-directive="show">
        <n-form label-placement="left" label-width="80">
          <n-form-item :label="t('department.name')">
            <n-input v-model:value="form.name" />
          </n-form-item>
          <n-form-item :label="t('department.parent')">
            <n-select v-model:value="form.parentId" :options="flatDepts" clearable placeholder="-" />
          </n-form-item>
          <n-form-item :label="t('department.sort')">
            <n-input-number v-model:value="form.sortOrder" :min="0" />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="showModal = false">{{ t('common.cancel') }}</n-button>
            <n-button type="primary" :loading="saving" @click="saveDept">{{ t('common.save') }}</n-button>
          </n-space>
        </template>
      </n-modal>
    </n-card>
  </div>
</template>
