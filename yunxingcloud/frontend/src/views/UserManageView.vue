<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem, NSelect, NSpace, NTag, NInput, NInputNumber, NSwitch, NDropdown, NPopconfirm } from 'naive-ui'

interface UserInfo { id: number; username: string; nickname: string; email: string; departmentId: number; registerSource: string; enabled: boolean; roles: { id: number; name: string; code: string }[] }
interface Dept { id: number; name: string }
interface Role { id: number; name: string; code: string }

const users = ref<UserInfo[]>([])
const notify = useNotify()
const depts = ref<Dept[]>([])
const allRoles = ref<Role[]>([])
const searchKeyword = ref('')
const loading = ref(false)

// 弹窗状态
const showAddModal = ref(false); const showEditModal = ref(false)
const showDeptModal = ref(false); const showRoleModal = ref(false); const showPwdModal = ref(false)
const selectedUser = ref<UserInfo | null>(null); const selectedDeptId = ref<number | null>(null)
const selectedRoleIds = ref<number[]>([])
const addForm = ref({ username: '', password: '', nickname: '', email: '' })
const editForm = ref({ nickname: '', email: '' })

const filteredUsers = computed(() => {
  const kw = searchKeyword.value.toLowerCase()
  if (!kw) return users.value
  return users.value.filter(u => u.username.toLowerCase().includes(kw) || u.nickname.toLowerCase().includes(kw) || u.email.toLowerCase().includes(kw))
})

const actionOptions = (row: UserInfo) => [
  { label: '编辑信息', key: 'edit' },
  { label: '分配部门', key: 'dept' },
  { label: '分配角色', key: 'role' },
  { label: '重置密码', key: 'pwd' },
  { label: row.enabled ? '禁用' : '启用', key: 'toggle' },
]

function handleAction(key: string, row: UserInfo) {
  switch (key) {
    case 'edit': selectedUser.value = row; editForm.value = { nickname: row.nickname, email: row.email }; showEditModal.value = true; break
    case 'dept': selectedUser.value = row; selectedDeptId.value = row.departmentId || null; showDeptModal.value = true; break
    case 'role': selectedUser.value = row; selectedRoleIds.value = (row.roles || []).map(r => r.id); showRoleModal.value = true; break
    case 'pwd': selectedUser.value = row; showPwdModal.value = true; break
    case 'toggle': toggleUser(row); break
  }
}

const columns = [
  { title: '用户名', key: 'username', width: 100, sorter: true },
  { title: '昵称', key: 'nickname', width: 100 },
  { title: '邮箱', key: 'email', width: 150, sorter: true, ellipsis: { tooltip: true } },
  { title: '来源', key: 'registerSource', width: 70, render: (row: UserInfo) => h(NTag, { type: row.registerSource === 'local' ? 'info' : 'success', size: 'small' }, { default: () => row.registerSource }) },
  { title: '部门', key: 'departmentName', width: 100, ellipsis: { tooltip: true }
  { title: '角色', key: 'roles', width: 150, render: (row: UserInfo) => h(NSpace, { size: 4 }, { default: () => (row.roles || []).map(r => h(NTag, { type: 'info', size: 'small' }, { default: () => r.name })) }) },
  { title: '状态', key: 'enabled', width: 60, render: (row: UserInfo) => h(NSwitch, { value: row.enabled, size: 'small', onUpdateValue: () => toggleUser(row) }) },
  { title: '操作', key: 'actions', width: 80, render: (row: UserInfo) => h(NDropdown, { options: actionOptions(row), onSelect: (k: string) => handleAction(k, row), size: 'small' }, { default: () => h(NButton, { size: 'small' }, { default: () => '更多 ▾' }) }) },
]

async function loadData() {
  loading.value = true
  const [ures, dres, rres] = await Promise.all([request.get('/api/users'), request.get('/api/departments'), request.get('/api/roles')])
  users.value = ures.data; allRoles.value = Array.isArray(rres.data) ? rres.data : []
  const flat: Dept[] = []; function walk(list: any[]) { list.forEach((d: any) => { flat.push({ id: d.id, name: d.name }); if (d.children) walk(d.children) }) }
  walk(dres.data); depts.value = flat; loading.value = false
}

async function toggleUser(user: UserInfo) { await request.put(`/api/users/${user.id}/toggle`); await loadData() }
async function batchToggle(v: boolean) { await Promise.all(users.value.map(u => request.put(`/api/users/${u.id}/toggle`))); await loadData(); notify.success(v ? '已启用全部' : '已禁用全部') }
async function saveAdd() { try { await request.post('/api/users', addForm.value); showAddModal.value = false; addForm.value = { username: '', password: '', nickname: '', email: '' }; await loadData(); notify.success('创建成功') } catch (e: any) { notify.error(e.response?.data?.message || '失败') } }
async function saveEdit() { if (!selectedUser.value) return; await request.put(`/api/users/${selectedUser.value.id}/profile`, editForm.value); showEditModal.value = false; await loadData(); notify.success('更新成功') }
async function saveDept() { if (!selectedUser.value) return; await request.put(`/api/users/${selectedUser.value.id}/department`, { departmentId: selectedDeptId.value }); showDeptModal.value = false; await loadData(); notify.success('部门已分配') }
async function saveRoles() { if (!selectedUser.value) return; await request.put(`/api/users/${selectedUser.value.id}/roles`, { roleIds: selectedRoleIds.value }); showRoleModal.value = false; await loadData(); notify.success('角色已分配') }
async function resetPwd() { if (!selectedUser.value) return; await request.post(`/api/users/${selectedUser.value.id}/reset-pwd`); showPwdModal.value = false; notify.success('密码已重置为 123456') }

onMounted(loadData)
</script>

<template>
  <n-config-provider>
    <div style="padding:24px">
      <n-card title="用户管理">
        <template #header-extra>
          <n-button type="primary" size="small" @click="showAddModal = true" style="margin-right:8px">新增用户</n-button>
          <n-button size="small" @click="batchToggle(true)" style="margin-right:4px" secondary>启用全部</n-button>
          <n-button size="small" @click="batchToggle(false)" style="margin-right:8px" secondary>禁用全部</n-button>
          <n-input v-model:value="searchKeyword" placeholder="搜索..." clearable style="width:160px" size="small" />
        </template>
        <n-data-table :columns="columns" :data="filteredUsers" :loading="loading" :pagination="{ pageSize: 10 }" :row-key="(row: UserInfo) => row.id" />

        <!-- 新增用户 -->
        <n-modal v-model:show="showAddModal" title="新增用户">
          <n-form label-placement="left" label-width="80" style="width:400px">
            <n-form-item label="用户名"><n-input v-model:value="addForm.username" /></n-form-item>
            <n-form-item label="密码"><n-input v-model:value="addForm.password" type="password" placeholder="至少6位" /></n-form-item>
            <n-form-item label="昵称"><n-input v-model:value="addForm.nickname" /></n-form-item>
            <n-form-item label="邮箱"><n-input v-model:value="addForm.email" /></n-form-item>
          </n-form>
          <template #footer><n-space justify="end"><n-button @click="showAddModal = false">取消</n-button><n-button type="primary" @click="saveAdd">确定</n-button></n-space></template>
        </n-modal>

        <!-- 编辑信息 -->
        <n-modal v-model:show="showEditModal" title="编辑用户信息">
          <n-form label-placement="left" label-width="80" style="width:400px">
            <n-form-item label="用户名"><span>{{ selectedUser?.username }}</span></n-form-item>
            <n-form-item label="昵称"><n-input v-model:value="editForm.nickname" /></n-form-item>
            <n-form-item label="邮箱"><n-input v-model:value="editForm.email" /></n-form-item>
          </n-form>
          <template #footer><n-space justify="end"><n-button @click="showEditModal = false">取消</n-button><n-button type="primary" @click="saveEdit">保存</n-button></n-space></template>
        </n-modal>

        <!-- 分配部门 -->
        <n-modal v-model:show="showDeptModal" title="分配部门">
          <n-form><n-form-item label="用户">{{ selectedUser?.username }}</n-form-item><n-form-item label="部门"><n-select v-model:value="selectedDeptId" :options="depts.map(d=>({label:d.name,value:d.id}))" clearable /></n-form-item></n-form>
          <template #footer><n-space justify="end"><n-button @click="showDeptModal = false">取消</n-button><n-button type="primary" @click="saveDept">保存</n-button></n-space></template>
        </n-modal>

        <!-- 分配角色 -->
        <n-modal v-model:show="showRoleModal" title="分配角色">
          <n-form><n-form-item label="用户">{{ selectedUser?.username }}</n-form-item><n-form-item label="角色"><n-select v-model:value="selectedRoleIds" :options="allRoles.map(r=>({label:r.name+'('+r.code+')',value:r.id}))" multiple /></n-form-item></n-form>
          <template #footer><n-space justify="end"><n-button @click="showRoleModal = false">取消</n-button><n-button type="primary" @click="saveRoles">保存</n-button></n-space></template>
        </n-modal>

        <!-- 重置密码确认 -->
        <n-modal v-model:show="showPwdModal" title="重置密码">
          <p>确认将 <strong>{{ selectedUser?.username }}</strong> 的密码重置为 <strong>123456</strong>？</p>
          <template #footer><n-space justify="end"><n-button @click="showPwdModal = false">取消</n-button><n-button type="primary" @click="resetPwd">确认重置</n-button></n-space></template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>
