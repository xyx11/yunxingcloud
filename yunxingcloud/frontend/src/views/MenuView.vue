<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { useI18n } from 'vue-i18n'
import {
  NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem,
  NInput, NInputNumber, NSelect, NSpace, NPopconfirm, NTag, NSwitch,
  darkTheme, lightTheme
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'

interface Menu {
  id: number; name: string; parentId: number | null; sortOrder: number
  path: string; component: string; icon: string; menuType: string
  perms: string; visible: boolean; children: Menu[]
}

const { t } = useI18n()
const currentTheme = computed(() => localStorage.getItem("theme") === "dark" ? darkTheme : lightTheme)
const menus = ref<Menu[]>([])
const notify = useNotify()
const allMenus = ref<Menu[]>([])
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editing = ref<Menu | null>(null)
const form = ref({
  name: '', parentId: null as number | null, sortOrder: 0,
  path: '', component: '', icon: '', menuType: 'M', perms: '', visible: true
})

const iconOptions = [
  {label:'🏠 首页',value:'home'},{label:'👤 用户',value:'person'},{label:'👥 用户组',value:'people'},
  {label:'⚙ 设置',value:'settings'},{label:'📁 文件夹',value:'folder'},{label:'📄 文档',value:'document'},
  {label:'📊 图表',value:'chart'},{label:'🖥 监控',value:'monitor'},{label:'🔧 工具',value:'tool'},
  {label:'🔒 锁',value:'lock'},{label:'🔑 钥匙',value:'key'},{label:'🛡 盾牌',value:'shield'},
  {label:'📋 列表',value:'list'},{label:'📝 编辑',value:'edit'},{label:'🔍 搜索',value:'search'},
  {label:'💬 消息',value:'message'},{label:'📢 公告',value:'notice'},{label:'📖 书本',value:'book'},
  {label:'⭐ 星标',value:'star'},{label:'❤ 收藏',value:'heart'},{label:'📅 日历',value:'calendar'},
  {label:'📈 趋势',value:'trend'},{label:'🏢 建筑',value:'building'},{label:'🌐 网络',value:'globe'},
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
        h(NButton, { size: 'tiny', onClick: () => moveMenu(row.id, -1) }, { default: () => '↑' }),
        h(NButton, { size: 'tiny', onClick: () => moveMenu(row.id, 1) }, { default: () => '↓' }),
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
    const res = await request.get('/api/menus/tree')
    menus.value = res.data
    const flat = await request.get('/api/menus')
    allMenus.value = flat.data
    parentOptions.value = [{ label: t('menu.noneParent'), value: null }]
    buildParentOptions(res.data)
  } catch {}
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
  saving.value = true
  try {
    if (editing.value) await request.put(`/api/menus/${editing.value.id}`, form.value)
    else await request.post('/api/menus', form.value)
    showModal.value = false
    notify.success(editing.value ? t('menu.updateSuccess') : t('menu.createSuccess'))
    await loadMenus()
  } catch (e: any) { notify.error(e.response?.data?.message || t('common.saveFailed')) } finally { saving.value = false }
}

async function moveMenu(id: number, direction: number) {
  await request.put(`/api/menus/${id}/move`, { direction })
  await loadMenus()
}

async function delMenu(id: number) {
  await request.delete(`/api/menus/${id}`)
  notify.success(t('common.success'))
  await loadMenus()
}

onMounted(loadMenus)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card :title="t('nav.menus')">
        <template #header-extra>
          <n-button type="primary" size="small" @click="addMenu"><template #icon>＋</template>{{ t('common.add') }}</n-button>
        </template>
        <n-space style="margin-bottom:12px"><n-button size="small" @click="loadMenus" secondary>{{ t('common.refresh') }}</n-button></n-space>
        <n-data-table
          :columns="columns" :data="menus" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }"
          default-expand-all :row-key="(row: Menu) => row.id" :children-key="'children'"
        />

        <n-modal v-model:show="showModal" :title="editing ? t('menu.edit') : t('menu.addRoot')" style="width:600px">
          <n-form label-placement="left" label-width="80">
            <n-form-item :label="t('menu.name')">
              <n-input v-model:value="form.name" />
            </n-form-item>
            <n-form-item :label="t('menu.type')">
              <n-select v-model:value="form.menuType" :options="typeOptions" />
            </n-form-item>
            <n-form-item :label="t('menu.parent')">
              <n-select v-model:value="form.parentId" :options="parentOptions as any" />
            </n-form-item>
            <n-form-item :label="t('menu.path')">
              <n-input v-model:value="form.path" placeholder="/example" />
            </n-form-item>
            <n-form-item :label="t('menu.component')">
              <n-input v-model:value="form.component" placeholder="ExampleView" />
            </n-form-item>
            <n-form-item :label="t('menu.icon')">
              <n-select v-model:value="form.icon" :options="iconOptions" :placeholder="t('menu.selectIcon')" clearable filterable />
            </n-form-item>
            <n-form-item :label="t('menu.perms')">
              <n-input v-model:value="form.perms" placeholder="system:example:list" />
            </n-form-item>
            <n-form-item :label="t('menu.sort')">
              <n-input-number v-model:value="form.sortOrder" :min="0" />
            </n-form-item>
            <n-form-item :label="t('menu.visible')">
              <n-switch v-model:value="form.visible" />
            </n-form-item>
          </n-form>
          <template #footer>
            <n-space justify="end">
              <n-button @click="showModal = false">{{ t('common.cancel') }}</n-button>
              <n-button type="primary" :loading="saving" @click="saveMenu">{{ t('common.save') }}</n-button>
            </n-space>
          </template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>