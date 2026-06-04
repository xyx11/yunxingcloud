<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import request from '@/api/request'
import { NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSpace, NPopconfirm } from 'naive-ui'

interface Role { id: number; name: string; code: string; description: string; permissions: string; enabled: boolean }

const roles = ref<Role[]>([])
const loading = ref(false)
const showModal = ref(false)
const editing = ref<Role | null>(null)
const form = ref({ name: '', code: '', description: '', permissions: '' })

const columns = [
  { title: '名称', key: 'name', width: 120 },
  { title: '编码', key: 'code', width: 120 },
  { title: '描述', key: 'description', width: 160 },
  { title: '权限', key: 'permissions', width: 200 },
  { title: '状态', key: 'enabled', width: 60, render: (row: Role) => row.enabled ? '启用' : '停用' },
  { title: '操作', key: 'actions', width: 160, render: (row: Role) => h('div', [
    h(NButton, { size: 'small', onClick: () => editRole(row) }, { default: () => '编辑' }),
    h('span', { style: 'margin:0 8px' }),
    h(NPopconfirm, { onPositiveClick: () => delRole(row.id) }, {
      trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
      default: () => '确认删除?'
    })
  ]) },
]

async function loadRoles() {
  loading.value = true
  const res = await request.get('/api/roles')
  roles.value = res.data
  loading.value = false
}

function addRole() {
  editing.value = null
  form.value = { name: '', code: '', description: '', permissions: '' }
  showModal.value = true
}

function editRole(role: Role) {
  editing.value = role
  form.value = { name: role.name, code: role.code, description: role.description, permissions: role.permissions }
  showModal.value = true
}

async function saveRole() {
  try {
    if (editing.value) {
      await request.put(`/api/roles/${editing.value.id}`, form.value)
    } else {
      await request.post('/api/roles', form.value)
    }
    showModal.value = false
    await loadRoles()
  } catch (e: any) {
    alert(e.response?.data?.message || '保存失败')
  }
}

async function delRole(id: number) {
  await request.delete(`/api/roles/${id}`)
  await loadRoles()
}

onMounted(loadRoles)
</script>

<template>
  <n-config-provider>
    <div style="padding: 24px;">
      <n-card title="角色管理">
        <template #header-extra>
          <n-button type="primary" size="small" @click="addRole">新增角色</n-button>
        </template>
        <n-data-table :columns="columns" :data="roles" :loading="loading" :row-key="(row: Role) => row.id" />

        <n-modal v-model:show="showModal" :title="editing ? '编辑角色' : '新增角色'">
          <n-form>
            <n-form-item label="名称">
              <n-input v-model:value="form.name" />
            </n-form-item>
            <n-form-item label="编码">
              <n-input v-model:value="form.code" />
            </n-form-item>
            <n-form-item label="描述">
              <n-input v-model:value="form.description" />
            </n-form-item>
            <n-form-item label="权限(逗号分隔)">
              <n-input v-model:value="form.permissions" placeholder="user:read,user:write,dept:read" />
            </n-form-item>
          </n-form>
          <template #footer>
            <n-space justify="end">
              <n-button @click="showModal = false">取消</n-button>
              <n-button type="primary" @click="saveRole">保存</n-button>
            </n-space>
          </template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>
