
<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { useI18n } from 'vue-i18n'
import { NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSpace, NPopconfirm, NTree, NSwitch, NPopover, NCheckbox, darkTheme, lightTheme } from 'naive-ui'
import { useColumnManager } from '@/composables/useColumnManager'
import type { DataTableColumns, TreeOption } from 'naive-ui'

interface Role { id: number; name: string; code: string; description: string; permissions: string; enabled: boolean; createdAt?: string }

const { t } = useI18n()
const notify = useNotify()
const currentTheme = computed(() => localStorage.getItem('theme') === 'dark' ? darkTheme : lightTheme)
const roles = ref<Role[]>([])
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editing = ref<Role | null>(null)
const roleSearch = ref('')
const form = ref({ name: '', code: '', description: '', permissions: '' })
const menuTree = ref<TreeOption[]>([])
const checkedPerms = ref<string[]>([])

const filteredRoles = computed(() => {
  const kw = roleSearch.value.toLowerCase()
  if (!kw) return roles.value
  return roles.value.filter(r => r.name.toLowerCase().includes(kw) || r.code.toLowerCase().includes(kw))
})

const permNodes = computed(() => {
  return menuTree.value.map(m => ({
    key: m.key as string,
    label: m.label as string,
    children: (m.children as any[])?.map(c => ({
      key: c.key as string,
      label: c.label as string,
      children: (c.children as any[])?.map(b => ({
        key: b.key as string,
        label: b.label as string,
      })) || undefined
    })) || undefined
  })) as TreeOption[]
})

const columns = computed<DataTableColumns<Role>>(() => [
  { title: 'ID', key: 'id', width: 60 },
  { title: t('role.name'), key: 'name', width: 120, sorter: true },
  { title: t('role.code'), key: 'code', width: 100 },
  { title: '用户数', key: 'user_count', width: 60 },
  { title: '权限数', key: 'permissions', width: 60, render: (row: any) => row.permissions ? row.permissions.split(',').filter((s:string)=>s.trim()).length : 0 },
  { title: t('role.desc'), key: 'description', width: 120, ellipsis: { tooltip: true } },
  { title: t('role.enabled'), key: 'enabled', width: 70, render: (row) => h(NSwitch, { value: row.enabled, size: 'small', onUpdateValue: () => toggleRole(row) }) },
  { title: 'Created', key: 'createdAt', width: 140, render: (row) => row.createdAt || '-' },
  { title: t('user.actions'), key: 'actions', width: 140, render: (row) => h(NSpace, null, { default: () => [
    h(NButton, { size: 'small', type: 'primary', onClick: () => editRole(row) }, { default: () => '编辑' }),
    h(NPopconfirm, { onPositiveClick: () => delRole(row.id) }, { trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }), default: () => '确认删除?' })
  ]}) },
])
const { visibleColumns, toggleColumn, hiddenKeys } = useColumnManager(columns, 'roles')
const columnOptions = computed(() => (columns.value as any[])
  .filter((c: any) => c.key && c.key !== 'actions')
  .map((c: any) => ({ key: c.key, title: c.title })),
)

async function loadAll() {
  loading.value = true
  try {
    const [r, m] = await Promise.all([request.get('/api/roles'), request.get('/api/menus/tree')])
    roles.value = r.data
    menuTree.value = buildPermTree(m.data)
  } catch {}
  loading.value = false
}

function buildPermTree(menus: any[]): TreeOption[] {
  return menus.filter((m: any) => m.menuType !== 'F').map((m: any) => ({
    key: m.perms || `menu-${m.id}`,
    label: m.name,
    children: m.children ? buildPermTree(m.children) : undefined,
  }))
}

function parsePerms(perms: string) {
  if (!perms) return []
  const all: string[] = []; permNodes.value.forEach(p => walkKeys(p, all))
  checkedPerms.value = all.filter(k => perms.split(',').map(s=>s.trim()).includes(k))
}

function walkKeys(node: TreeOption, out: string[]) {
  if (node.key) out.push(node.key as string)
  node.children?.forEach(c => walkKeys(c, out))
}

function selectAllPerms() { const all: string[] = []; permNodes.value.forEach(p => walkKeys(p, all)); checkedPerms.value = all }
function deselectAllPerms() { checkedPerms.value = [] }

function addRole() { editing.value = null; form.value = { name: '', code: '', description: '', permissions: '' }; checkedPerms.value = []; showModal.value = true }
function editRole(role: Role) { editing.value = role; form.value = { name: role.name, code: role.code, description: role.description, permissions: role.permissions }; parsePerms(role.permissions); showModal.value = true }

async function saveRole() {
  const perms = checkedPerms.value.join(',')
  const body = { ...form.value, permissions: perms }
  try {
    saving.value = true
    if (editing.value) await request.put(`/api/roles/${editing.value.id}`, body)
    else await request.post('/api/roles', body)
    showModal.value = false; notify.success(editing.value ? '修改成功' : '新增成功'); await loadAll()
  } catch (e: any) { notify.error(e.response?.data?.message || '保存失败') } finally { saving.value = false }
}

async function delRole(id: number) { await request.delete(`/api/roles/${id}`); notify.success('删除成功'); await loadAll() }

function exportCSV() {
  const headers = ['ID', '名称', '编码', '描述', '权限', '状态', '创建时间']
  const rows = filteredRoles.value.map(r => [r.id, r.name, r.code, r.description, r.permissions, r.enabled ? '正常' : '停用', r.createdAt || '-'])
  const csv = [headers, ...rows].map(r => r.map(c => `"${String(c).replace(/"/g, '""')}"`).join(',')).join('\n')
  const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = '角色列表.csv'; a.click(); URL.revokeObjectURL(url)
  notify.success('导出成功')
}

async function toggleRole(role: Role) {
  await request.put(`/api/roles/${role.id}`, { name: role.name, code: role.code, description: role.description, permissions: role.permissions, enabled: !role.enabled })
  notify.success(role.enabled ? '已停用' : '已启用')
  await loadAll()
}

onMounted(loadAll)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card :title="t('nav.roles')">
        <template #header-extra>
          <n-button type="primary" size="small" @click="addRole"><template #icon>＋</template>新增</n-button>
        </template>
        <n-space style="margin-bottom:12px" justify="space-between">
          <n-space>
            <n-input v-model:value="roleSearch" placeholder="角色名称" clearable style="width:180px" size="small" />
            <n-button type="primary" size="small" @click="() => {}">搜索</n-button>
            <n-button size="small" @click="roleSearch = ''">重置</n-button>
          </n-space>
          <n-space>
            <n-button size="small" @click="loadAll" secondary>刷新</n-button>
            <n-popover trigger="click" placement="bottom-end" :width="180">
              <template #trigger>
                <n-button size="small" secondary>列选项</n-button>
              </template>
              <div style="max-height:300px;overflow-y:auto">
                <div v-for="opt in columnOptions" :key="opt.key" style="padding:2px 0">
                  <n-checkbox
                    :checked="!hiddenKeys.has(opt.key)"
                    @update:checked="toggleColumn(opt.key)"
                  >
                    {{ opt.title }}
                  </n-checkbox>
                </div>
              </div>
            </n-popover>
          </n-space>
        </n-space>
        <n-data-table :columns="visibleColumns" :data="filteredRoles" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }" :row-key="(row:Role)=>row.id" />

        <n-modal v-model:show="showModal" :title="editing ? t('common.edit') : t('common.add')" style="width:560px" preset="card" display-directive="show">
          <n-form label-placement="left" label-width="80">
            <n-form-item :label="t('role.name')"><n-input v-model:value="form.name" :placeholder="t('role.name')" /></n-form-item>
            <n-form-item :label="t('role.code')"><n-input v-model:value="form.code" placeholder="admin" /></n-form-item>
            <n-form-item :label="t('role.desc')"><n-input v-model:value="form.description" :placeholder="t('role.desc')" /></n-form-item>
            <n-form-item :label="t('role.perms')">
              <n-space style="margin-bottom:4px">
                <n-button size="tiny" @click="selectAllPerms">{{ t('common.selectAll') }}</n-button>
                <n-button size="tiny" @click="deselectAllPerms">{{ t('common.deselectAll') }}</n-button>
              </n-space>
              <div style="max-height:280px;overflow-y:auto;border:1px solid var(--n-border-color, #e8e8e8);border-radius:4px;padding:8px">
                <n-tree :data="permNodes" checkable :checked-keys="checkedPerms" @update:checked-keys="(ks:string[])=>checkedPerms=ks" default-expand-all block-line />
              </div>
            </n-form-item>
          </n-form>
          <template #footer><n-space justify="end"><n-button @click="showModal=false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="saveRole">{{ t('common.save') }}</n-button></n-space></template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>
