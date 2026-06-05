<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NConfigProvider, NCard, NDataTable, NButton, NModal, NSelect, NSpace, NTag, NInput } from 'naive-ui'

interface UserInfo { id: number; username: string; nickname: string; email: string; departmentId: number; registerSource: string; enabled: boolean; roles: { id: number; name: string; code: string }[] }
interface Dept { id: number; name: string }
interface Role { id: number; name: string; code: string }

const users = ref<UserInfo[]>([])
const notify = useNotify()
const depts = ref<Dept[]>([])
const allRoles = ref<Role[]>([])
const searchKeyword = ref('')

const filteredUsers = computed(() => {
  const kw = searchKeyword.value.toLowerCase()
  if (!kw) return users.value
  return users.value.filter(u =>
    u.username.toLowerCase().includes(kw) ||
    u.nickname.toLowerCase().includes(kw) ||
    u.email.toLowerCase().includes(kw)
  )
})
const loading = ref(false)
const showDeptModal = ref(false)
const showRoleModal = ref(false)
const selectedUser = ref<UserInfo | null>(null)
const selectedDeptId = ref<number | null>(null)
const selectedRoleIds = ref<number[]>([])

const columns = [
  { title: '用户名', key: 'username', width: 100, sorter: true },
  { title: '昵称', key: 'nickname', width: 100 },
  { title: '邮箱', key: 'email', width: 150, sorter: true, ellipsis: { tooltip: true } },
  { title: '来源', key: 'registerSource', width: 70, render: (row: UserInfo) => h(NTag, { type: row.registerSource === 'local' ? 'info' : 'success', size: 'small' }, { default: () => row.registerSource }) },
  { title: '部门', key: 'departmentId', width: 70 },
  {
    title: '角色', key: 'roles', width: 150,
    render: (row: UserInfo) => h(NSpace, { size: 4 }, { default: () => (row.roles || []).map(r => h(NTag, { type: 'info', size: 'small' }, { default: () => r.name })) })
  },
  {
    title: '操作', key: 'actions', width: 160,
    render: (row: UserInfo) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => assignDept(row) }, { default: () => '部门' }),
        h(NButton, { size: 'small', onClick: () => assignRole(row) }, { default: () => '角色' }),
      ]
    })
  },
]

async function loadData() {
  loading.value = true
  const [ures, dres, rres] = await Promise.all([
    request.get('/api/users'),
    request.get('/api/departments'),
    request.get('/api/roles'),
  ])
  users.value = ures.data
  allRoles.value = Array.isArray(rres.data) ? rres.data : []
  const flat: Dept[] = []
  function walk(list: any[]) { list.forEach((d: any) => { flat.push({ id: d.id, name: d.name }); if (d.children) walk(d.children) }) }
  walk(dres.data)
  depts.value = flat
  loading.value = false
}

function assignDept(user: UserInfo) {
  selectedUser.value = user
  selectedDeptId.value = user.departmentId || null
  showDeptModal.value = true
}

function assignRole(user: UserInfo) {
  selectedUser.value = user
  selectedRoleIds.value = (user.roles || []).map(r => r.id)
  showRoleModal.value = true
}

async function saveDept() {
  if (!selectedUser.value) return
  await request.put(`/api/users/${selectedUser.value.id}/department`, { departmentId: selectedDeptId.value })
  showDeptModal.value = false
    notify.success('部门分配成功')
  await loadData()
}

async function saveRoles() {
  if (!selectedUser.value) return
  await request.put(`/api/users/${selectedUser.value.id}/roles`, { roleIds: selectedRoleIds.value })
  showRoleModal.value = false
    notify.success('角色分配成功')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <n-config-provider>
    <div style="padding: 24px;">
      <n-card title="用户管理">
        <template #header-extra>
          <n-input v-model:value="searchKeyword" placeholder="搜索用户名/昵称/邮箱..." clearable style="width:220px" />
        </template>
        <n-data-table :columns="columns" :data="filteredUsers" :loading="loading" :pagination="{ pageSize: 10 }" :row-key="(row: UserInfo) => row.id" />

        <n-modal v-model:show="showDeptModal" title="分配部门">
          <n-form>
            <n-form-item label="用户">{{ selectedUser?.username }}</n-form-item>
            <n-form-item label="部门">
              <n-select v-model:value="selectedDeptId" :options="depts.map(d => ({ label: d.name, value: d.id }))" placeholder="选择部门" clearable />
            </n-form-item>
          </n-form>
          <template #footer>
            <n-space justify="end">
              <n-button @click="showDeptModal = false">取消</n-button>
              <n-button type="primary" @click="saveDept">保存</n-button>
            </n-space>
          </template>
        </n-modal>

        <n-modal v-model:show="showRoleModal" title="分配角色">
          <n-form>
            <n-form-item label="用户">{{ selectedUser?.username }}</n-form-item>
            <n-form-item label="角色">
              <n-select v-model:value="selectedRoleIds" :options="allRoles.map(r => ({ label: `${r.name} (${r.code})`, value: r.id }))" multiple placeholder="选择角色" />
            </n-form-item>
          </n-form>
          <template #footer>
            <n-space justify="end">
              <n-button @click="showRoleModal = false">取消</n-button>
              <n-button type="primary" @click="saveRoles">保存</n-button>
            </n-space>
          </template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>
