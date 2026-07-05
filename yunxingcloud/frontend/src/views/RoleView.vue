
<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'

import { fetchSysRoles, createRole, updateRole, deleteRole } from '@/api/role'
import { fetchMenuTree } from '@/api/menu'
import { useNotify } from '@/composables/useNotify'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSpace, NPopconfirm, NTree, NSwitch, NPopover, NCheckbox } from 'naive-ui'
import { useColumnManager } from '@/composables/useColumnManager'
import type { DataTableColumns, TreeOption } from 'naive-ui'

interface Role { id: number; name: string; code: string; description: string; permissions: string; enabled: boolean; createdAt?: string }

const { t } = useI18n()
const notify = useNotify()

const roles = ref<Role[]>([])
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editing = ref<Role | null>(null)
const roleSearch = ref('')
function doSearch() { roleSearch.value = roleSearch.value }
const checkedRowKeys = ref<number[]>([])
async function batchDelete() {
  if (!checkedRowKeys.value.length) return
  try { for (const id of checkedRowKeys.value) await deleteRole(id); checkedRowKeys.value = []; loadAll(); notify.success(t('common.deleted')) } catch { notify.error(t('common.saveFailed')) }
}
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
  { title: t('role.userCount'), key: 'user_count', width: 60 },
  { title: t('role.permCount'), key: 'permissions', width: 60, render: (row: any) => row.permissions ? row.permissions.split(',').filter((s:string)=>s.trim()).length : 0 },
  { title: t('role.desc'), key: 'description', width: 120, ellipsis: { tooltip: true } },
  { title: t('role.enabled'), key: 'enabled', width: 70, render: (row) => h(NSwitch, { value: row.enabled, size: 'small', onUpdateValue: () => toggleRole(row) }) },
  { title: t('role.created'), key: 'createdAt', width: 140, render: (row) => row.createdAt || '-' },
  { title: t('user.actions'), key: 'actions', width: 140, render: (row) => h(NSpace, null, { default: () => [
    h(NButton, { size: 'small', type: 'primary', onClick: () => editRole(row) }, { default: () => t('common.edit') }),
    h(NPopconfirm, { onPositiveClick: () => delRole(row.id) }, { trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => t('common.delete') }), default: () => t('common.confirmDelete') })
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
    const [r, m] = await Promise.all([fetchSysRoles(), fetchMenuTree()])
    roles.value = r.data
    menuTree.value = buildPermTree(m.data)
  } catch { notify.error(t('common.error')); }
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
    if (editing.value) await updateRole(editing.value.id, body)
    else await createRole(body)
    showModal.value = false; notify.success(editing.value ? t('role.updateSuccess') : t('role.createSuccess')); await loadAll()
  } catch (e: any) { notify.error(e.response?.data?.message || t('common.error')) } finally { saving.value = false }
}

async function delRole(id: number) { await deleteRole(id); notify.success(t('role.deleteSuccess')); await loadAll() }

async function toggleRole(role: Role) {
  await updateRole(role.id, { name: role.name, code: role.code, description: role.description, permissions: role.permissions, enabled: !role.enabled })
  notify.success(role.enabled ? t('role.toggleDisabled') : t('role.toggleEnabled'))
  await loadAll()
}

onMounted(loadAll)
</script>

<template>
  <div class="view-pad">
    <n-card :title="t('nav.roles')">
      <template #header-extra>
        <n-space>
          <n-button v-if="checkedRowKeys.length" type="error" size="small" @click="batchDelete">{{ t('common.batchDelete') }} ({{ checkedRowKeys.length }})</n-button>
          <n-button type="primary" size="small" @click="addRole"><template #icon>＋</template>{{ t('common.add') }}</n-button>
        </n-space>
      </template>
      <n-space class="mb-12" justify="space-between">
        <n-space>
          <n-input v-model:value="roleSearch" @keyup.enter="doSearch" :placeholder="t('role.placeholder')" clearable style="width:180px" size="small" />
          <n-button type="primary" size="small" @click="doSearch">{{ t('common.search') }}</n-button>
          <n-button size="small" @click="roleSearch = ''">{{ t('common.reset') }}</n-button>
        </n-space>
        <n-space>
          <n-button size="small" @click="loadAll" secondary>{{ t('common.refresh') }}</n-button>
          <n-popover trigger="click" placement="bottom-end" :width="180">
            <template #trigger>
              <n-button size="small" secondary>{{ t('common.columnOptions') }}</n-button>
            </template>
            <div class="scroll-y">
              <div v-for="opt in columnOptions" :key="opt.key" class="py-2">
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
      <n-data-table :columns="visibleColumns" :data="filteredRoles" :loading="loading" size="small" :bordered="false" :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }" :row-key="(row:Role)=>row.id" v-model:checked-row-keys="checkedRowKeys" />

      <n-drawer v-model:show="showModal" :width="500" placement="right">
        <n-drawer-content :title="editing ? t('common.edit') : t('common.add')" closable>
          <template #footer><n-space justify="end"><n-button @click="showModal=false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="saveRole">{{ t('common.save') }}</n-button></n-space></template>
          <n-form label-placement="left" label-width="80" size="small">
            <n-form-item :label="t('role.name')"><n-input v-model:value="form.name" :placeholder="t('role.name')" /></n-form-item>
            <n-form-item :label="t('role.code')"><n-input v-model:value="form.code" placeholder="admin" /></n-form-item>
            <n-form-item :label="t('role.desc')"><n-input v-model:value="form.description" :placeholder="t('role.desc')" /></n-form-item>
            <n-form-item :label="t('role.perms')">
              <n-space style="margin-bottom:4px">
                <n-button size="tiny" @click="selectAllPerms">{{ t('common.selectAll') }}</n-button>
                <n-button size="tiny" @click="deselectAllPerms">{{ t('common.deselectAll') }}</n-button>
              </n-space>
              <div style="max-height:50vh;overflow-y:auto;border:1px solid var(--n-border-color, #e8e8e8);border-radius:4px;padding:8px">
                <n-tree :data="permNodes" checkable :checked-keys="checkedPerms" @update:checked-keys="(ks:string[])=>checkedPerms=ks" default-expand-all block-line />
              </div>
            </n-form-item>
          </n-form>
        </n-drawer-content>
      </n-drawer>
    </n-card>
  </div>
</template>
