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
  { label: '目录 (M)', value: 'M' },
  { label: '菜单 (C)', value: 'C' },
  { label: '按钮 (F)', value: 'F' },
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
  { title: '名称', key: 'name', width: 160 },
  { title: '图标', key: 'icon', width: 80 },
  {
    title: '类型', key: 'menuType', width: 80,
    render: (row) => h(NTag, { type: row.menuType === 'M' ? 'info' : row.menuType === 'C' ? 'success' : 'default', size: 'small' },
      { default: () => ({ M: '目录', C: '菜单', F: '按钮' }[row.menuType] || row.menuType) })
  },
  { title: '路由', key: 'path', width: 140 },
  { title: '组件', key: 'component', width: 140 },
  { title: '权限标识', key: 'perms', width: 160 },
  { title: '排序', key: 'sortOrder', width: 60, sorter: true },
  {
    title: '可见', key: 'visible', width: 60,
    render: (row) => row.visible ? '是' : '否'
  },
  {
    title: '操作', key: 'actions', width: 180,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'tiny', onClick: () => moveMenu(row.id, -1) }, { default: () => '↑' }),
        h(NButton, { size: 'tiny', onClick: () => moveMenu(row.id, 1) }, { default: () => '↓' }),
        h(NButton, { size: 'tiny', onClick: () => editMenu(row) }, { default: () => '编辑' }),
        h(NPopconfirm, { onPositiveClick: () => delMenu(row.id) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => '删除' }),
          default: () => '确认删除?'
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
    parentOptions.value = [{ label: '无 (根目录)', value: null }]
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
    notify.success(editing.value ? '更新成功' : '创建成功')
    await loadMenus()
  } catch (e: any) { notify.error(e.response?.data?.message || '保存失败') } finally { saving.value = false }
}

async function moveMenu(id: number, direction: number) {
  await request.put(`/api/menus/${id}/move`, { direction })
  await loadMenus()
}

async function delMenu(id: number) {
  await request.delete(`/api/menus/${id}`)
  notify.success('删除成功')
  await loadMenus()
}

onMounted(loadMenus)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card :title="t('nav.menus')">
        <template #header-extra>
          <n-button type="primary" size="small" @click="addMenu"><template #icon>＋</template>新增</n-button>
        </template>
        <n-space style="margin-bottom:12px"><n-button size="small" @click="loadMenus" secondary>刷新</n-button></n-space>
        <n-data-table
          :columns="columns" :data="menus" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }"
          default-expand-all :row-key="(row: Menu) => row.id" :children-key="'children'"
        />

        <n-modal v-model:show="showModal" :title="editing ? t('common.edit') : t('common.add')" style="width:600px">
          <n-form label-placement="left" label-width="80">
            <n-form-item label="名称">
              <n-input v-model:value="form.name" />
            </n-form-item>
            <n-form-item label="类型">
              <n-select v-model:value="form.menuType" :options="typeOptions" />
            </n-form-item>
            <n-form-item label="上级菜单">
              <n-select v-model:value="form.parentId" :options="parentOptions as any" />
            </n-form-item>
            <n-form-item label="路由路径">
              <n-input v-model:value="form.path" placeholder="/example" />
            </n-form-item>
            <n-form-item label="组件名称">
              <n-input v-model:value="form.component" placeholder="ExampleView" />
            </n-form-item>
            <n-form-item label="图标">
              <n-select v-model:value="form.icon" :options="iconOptions" placeholder="选择图标" clearable filterable />
            </n-form-item>
            <n-form-item label="权限标识">
              <n-input v-model:value="form.perms" placeholder="system:example:list" />
            </n-form-item>
            <n-form-item label="排序">
              <n-input-number v-model:value="form.sortOrder" :min="0" />
            </n-form-item>
            <n-form-item label="可见">
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