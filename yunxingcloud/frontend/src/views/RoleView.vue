<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSpace, NPopconfirm, NTag, NTree, NCheckbox } from 'naive-ui'
import type { DataTableColumns, TreeOption } from 'naive-ui'

interface Role { id: number; name: string; code: string; description: string; permissions: string; enabled: boolean }

const notify = useNotify()
const roles = ref<Role[]>([])
const loading = ref(false)
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

const columns: DataTableColumns<Role> = [
  { title: '角色编号', key: 'id', width: 80 },
  { title: '角色名称', key: 'name', width: 140, sorter: true },
  { title: '权限字符', key: 'code', width: 120 },
  { title: '显示顺序', key: 'id', width: 80 },
  { title: '状态', key: 'enabled', width: 70, render: (row) => h(NTag, { type: row.enabled ? 'success' : 'default', size: 'small' }, { default: () => row.enabled ? '正常' : '停用' }) },
  { title: '创建时间', key: 'id', width: 100, render: () => '-' },
  { title: '操作', key: 'actions', width: 140, render: (row) => h(NSpace, null, { default: () => [
    h(NButton, { size: 'tiny', onClick: () => editRole(row) }, { default: () => '编辑' }),
    h(NPopconfirm, { onPositiveClick: () => delRole(row.id) }, { trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => '删除' }), default: () => '确认删除?' })
  ]}) },
]

async function loadAll() {
  loading.value = true
  const [r, m] = await Promise.all([request.get('/api/roles'), request.get('/api/menus/tree')])
  roles.value = r.data
  menuTree.value = buildPermTree(m.data)
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

function addRole() { editing.value = null; form.value = { name: '', code: '', description: '', permissions: '' }; checkedPerms.value = []; showModal.value = true }
function editRole(role: Role) { editing.value = role; form.value = { name: role.name, code: role.code, description: role.description, permissions: role.permissions }; parsePerms(role.permissions); showModal.value = true }

async function saveRole() {
  const perms = checkedPerms.value.join(',')
  const body = { ...form.value, permissions: perms }
  try {
    if (editing.value) await request.put(`/api/roles/${editing.value.id}`, body)
    else await request.post('/api/roles', body)
    showModal.value = false; notify.success(editing.value ? '修改成功' : '新增成功'); await loadAll()
  } catch (e: any) { notify.error(e.response?.data?.message || '保存失败') }
}

async function delRole(id: number) { await request.delete(`/api/roles/${id}`); notify.success('删除成功'); await loadAll() }
onMounted(loadAll)
</script>

<template>
  <n-config-provider>
    <div style="padding:24px">
      <n-card title="角色管理">
        <template #header-extra>
          <n-input v-model:value="roleSearch" placeholder="搜索角色名称" clearable style="width:180px;margin-right:8px" size="small" />
          <n-button type="primary" size="small" @click="addRole">新增角色</n-button>
        </template>
        <n-data-table :columns="columns" :data="filteredRoles" :loading="loading" :pagination="{ pageSize: 10 }" :row-key="(row:Role)=>row.id" />

        <n-modal v-model:show="showModal" :title="editing ? '修改角色' : '新增角色'" style="width:560px">
          <n-form label-placement="left" label-width="80">
            <n-form-item label="角色名称"><n-input v-model:value="form.name" placeholder="请输入角色名称" /></n-form-item>
            <n-form-item label="权限字符"><n-input v-model:value="form.code" placeholder="请输入权限字符，如 admin" /></n-form-item>
            <n-form-item label="角色顺序"><n-input v-model:value="form.description" placeholder="描述信息" /></n-form-item>
            <n-form-item label="菜单权限">
              <div style="max-height:300px;overflow-y:auto;border:1px solid #e8e8e8;border-radius:4px;padding:8px">
                <n-tree :data="permNodes" checkable :checked-keys="checkedPerms" @update:checked-keys="(ks:string[])=>checkedPerms=ks" default-expand-all block-line />
              </div>
            </n-form-item>
          </n-form>
          <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="saveRole">确定</n-button></n-space></template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>
