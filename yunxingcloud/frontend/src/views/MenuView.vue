<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { fetchMenuTree, fetchAllMenus, createMenu, updateMenu, deleteMenu, moveMenu } from '@/api/menu'
import { useNotify } from '@/composables/useNotify'
import { useI18n } from 'vue-i18n'

import {
  NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem,
  NInput, NInputNumber, NSelect, NSpace, NPopconfirm, NTag, NSwitch,
} from 'naive-ui'
import type { DataTableColumns, FormRules, FormInst } from 'naive-ui'

interface Menu {
  id: number; name: string; parentId: number | null; sortOrder: number
  path: string; component: string; icon: string; menuType: string
  perms: string; visible: boolean; children: Menu[]
}

const { t } = useI18n()

const menus = ref<Menu[]>([])
const notify = useNotify()
const allMenus = ref<Menu[]>([])
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editing = ref<Menu | null>(null)
const formRef = ref<FormInst>()
const rules: FormRules = {
  name: [{ required: true, message: t('validate.required'), trigger: 'blur' }],
  path: [{ required: true, message: t('validate.required'), trigger: 'blur' }],
}
const form = ref({
  name: '', parentId: null as number | null, sortOrder: 0,
  path: '', component: '', icon: '', menuType: 'M', perms: '', visible: true
})

const iconOptions = [
  {label:`🏠 ${t('menu.icons.home')}`,value:'home'},{label:`👤 ${t('menu.icons.person')}`,value:'person'},{label:`👥 ${t('menu.icons.people')}`,value:'people'},
  {label:`⚙ ${t('menu.icons.settings')}`,value:'settings'},{label:`📁 ${t('menu.icons.folder')}`,value:'folder'},{label:`📄 ${t('menu.icons.document')}`,value:'document'},
  {label:`📊 ${t('menu.icons.chart')}`,value:'chart'},{label:`🖥 ${t('menu.icons.monitor')}`,value:'monitor'},{label:`🔧 ${t('menu.icons.tool')}`,value:'tool'},
  {label:`🔒 ${t('menu.icons.lock')}`,value:'lock'},{label:`🔑 ${t('menu.icons.key')}`,value:'key'},{label:`🛡 ${t('menu.icons.shield')}`,value:'shield'},
  {label:`📋 ${t('menu.icons.list')}`,value:'list'},{label:`📝 ${t('menu.icons.edit')}`,value:'edit'},{label:`🔍 ${t('menu.icons.search')}`,value:'search'},
  {label:`💬 ${t('menu.icons.message')}`,value:'message'},{label:`📢 ${t('menu.icons.notice')}`,value:'notice'},{label:`📖 ${t('menu.icons.book')}`,value:'book'},
  {label:`⭐ ${t('menu.icons.star')}`,value:'star'},{label:`❤ ${t('menu.icons.heart')}`,value:'heart'},{label:`📅 ${t('menu.icons.calendar')}`,value:'calendar'},
  {label:`📈 ${t('menu.icons.trend')}`,value:'trend'},{label:`🏢 ${t('menu.icons.building')}`,value:'building'},{label:`🌐 ${t('menu.icons.globe')}`,value:'globe'},
]
const typeOptions = [
  { label: `${t('menu.typeDir')} (M)`, value: 'M' },
  { label: `${t('menu.typeMenu')} (C)`, value: 'C' },
  { label: `${t('menu.typeBtn')} (F)`, value: 'F' },
]

const parentOptions = ref<{ label: string; value: number | null }[]>([])

function buildParentOptions(list: Menu[], prefix = '') {
  for (const m of list) {
    if (m.menuType !== 'F') {
      parentOptions.value.push({ label: prefix + m.name, value: m.id })
      if (m.children) buildParentOptions(m.children, prefix + '　')
    }
  }
}

const columns: DataTableColumns<Menu> = [
  { title: t('menu.name'), key: 'name', width: 160 },
  { title: t('menu.icon'), key: 'icon', width: 80 },
  {
    title: t('menu.type'), key: 'menuType', width: 80,
    render: (row) => h(NTag, { type: row.menuType === 'M' ? 'info' : row.menuType === 'C' ? 'success' : 'default', size: 'small' },
      { default: () => ({ M: t('menu.typeDir'), C: t('menu.typeMenu'), F: t('menu.typeBtn') }[row.menuType] || row.menuType) })
  },
  { title: t('menu.path'), key: 'path', width: 140 },
  { title: t('menu.component'), key: 'component', width: 140 },
  { title: t('menu.perms'), key: 'perms', width: 160 },
  { title: t('menu.sort'), key: 'sortOrder', width: 60, sorter: true },
  {
    title: t('menu.visible'), key: 'visible', width: 60,
    render: (row) => row.visible ? t('common.yes') : t('common.no')
  },
  {
    title: t('menu.actions'), key: 'actions', width: 180,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'tiny', onClick: () => moveMenuById(row.id, -1) }, { title: t('menu.moveUpTitle'), default: () => '↑' }),
        h(NButton, { size: 'tiny', onClick: () => moveMenuById(row.id, 1) }, { title: t('menu.moveDownTitle'), default: () => '↓' }),
        h(NButton, { size: 'tiny', onClick: () => editMenu(row) }, { default: () => t('common.edit') }),
        h(NPopconfirm, { onPositiveClick: () => delMenu(row.id) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }),
          default: () => t('common.confirmDelete')
        })
      ]
    })
  },
]

async function loadMenus() {
  loading.value = true
  try {
    const res = await fetchMenuTree()
    menus.value = res.data
    const flat = await fetchAllMenus()
    allMenus.value = flat.data
    parentOptions.value = [{ label: t('menu.noneParent'), value: null }]
    buildParentOptions(res.data)
  } catch { notify.error(t('common.error')); }
  loading.value = false
}

function addMenu() {
  editing.value = null
  form.value = { name: '', parentId: null, sortOrder: 0, path: '', component: '', icon: '', menuType: 'M', perms: '', visible: true }
  showModal.value = true
}

function editMenu(menu: Menu) {
  editing.value = menu
  form.value = {
    name: menu.name, parentId: menu.parentId, sortOrder: menu.sortOrder,
    path: menu.path || '', component: menu.component || '', icon: menu.icon || '',
    menuType: menu.menuType, perms: menu.perms || '', visible: menu.visible
  }
  showModal.value = true
}

async function saveMenu() {
  if (formRef.value) { try { await formRef.value.validate() } catch { return } }
  saving.value = true
  try {
    if (editing.value) await updateMenu(editing.value.id, form.value)
    else await createMenu(form.value)
    showModal.value = false
    notify.success(editing.value ? t('menu.updateSuccess') : t('menu.createSuccess'))
    await loadMenus()
  } catch (e: any) { notify.error(e.response?.data?.message || t('common.saveFailed')) } finally { saving.value = false }
}

async function moveMenuById(id: number, direction: number) {
  await moveMenu(id, direction)
  await loadMenus()
}

async function delMenu(id: number) {
  await deleteMenu(id)
  notify.success(t('common.success'))
  await loadMenus()
}

const checkedRowKeys = ref<number[]>([])
async function batchDelete() { if(!checkedRowKeys.value.length)return; try{for(const id of checkedRowKeys.value)await deleteMenu(id);checkedRowKeys.value=[];loadMenus();notify.success(t('common.deleted'))}catch{notify.error(t('common.saveFailed'))} }
onMounted(loadMenus)
</script>

<template>
  <div class="view-pad">
    <n-card :title="t('nav.menus')">
      <template #header-extra>
        <n-space><n-button v-if="checkedRowKeys.length" type="error" size="small" @click="batchDelete">{{ t('common.batchDelete') }} ({{ checkedRowKeys.length }})</n-button><n-button type="primary" size="small" @click="addMenu"><template #icon>＋</template>{{ t('common.add') }}</n-button></n-space>
      </template>
      <n-space class="mb-12"><n-button size="small" @click="loadMenus" secondary>{{ t('common.refresh') }}</n-button></n-space>
      <n-data-table
        :columns="columns" :data="menus" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }" v-model:checked-row-keys="checkedRowKeys"
        default-expand-all :row-key="(row: Menu) => row.id" :children-key="'children'"
      />

      <n-drawer v-model:show="showModal" :width="480" placement="right">
        <n-drawer-content :title="editing ? t('menu.edit') : t('menu.addRoot')" closable>
          <template #footer><n-space justify="end"><n-button @click="showModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="saveMenu">{{ t('common.save') }}</n-button></n-space></template>
          <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80" size="small">
            <n-form-item :label="t('menu.name')"><n-input v-model:value="form.name" /></n-form-item>
            <n-form-item :label="t('menu.type')"><n-select v-model:value="form.menuType" :options="typeOptions" /></n-form-item>
            <n-form-item :label="t('menu.parent')"><n-select v-model:value="form.parentId" :options="parentOptions as any" /></n-form-item>
            <n-form-item :label="t('menu.path')"><n-input v-model:value="form.path" placeholder="/example" /></n-form-item>
            <n-form-item :label="t('menu.component')"><n-input v-model:value="form.component" placeholder="ExampleView" /></n-form-item>
            <n-form-item :label="t('menu.icon')"><n-select v-model:value="form.icon" :options="iconOptions" :placeholder="t('menu.selectIcon')" clearable filterable /></n-form-item>
            <n-form-item :label="t('menu.perms')"><n-input v-model:value="form.perms" placeholder="system:example:list" /></n-form-item>
            <n-form-item :label="t('menu.sort')"><n-input-number v-model:value="form.sortOrder" :min="0" /></n-form-item>
            <n-form-item :label="t('menu.visible')"><n-switch v-model:value="form.visible" /></n-form-item>
          </n-form>
        </n-drawer-content>
      </n-drawer>
    </n-card>
  </div>
</template>