<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem, NSelect, NSpace, NTag, NInput, NInputNumber, NSwitch, NDropdown, NPopconfirm, NPopover, NCheckbox, darkTheme, lightTheme, type FormRules, type FormInst } from 'naive-ui'
import { useColumnManager } from '@/composables/useColumnManager'

interface UserInfo { id: number; username: string; nickname: string; email: string; departmentId: number; departmentName: string; postId: number; registerSource: string; enabled: boolean; lastLoginTime?: string; roles: { id: number; name: string; code: string }[] }
interface Dept { id: number; name: string }
interface Role { id: number; name: string; code: string }
interface Post { id: number; postCode: string; postName: string }

const users = ref<UserInfo[]>([])
const notify = useNotify()
const depts = ref<Dept[]>([])
const allRoles = ref<Role[]>([])
const posts = ref<Post[]>([])
const dictMap = ref<Record<string, Record<string, string>>>({})
const searchKeyword = ref('')
const filterDept = ref<number | null>(null)
const filterRole = ref<string>('')
const filterStatus = ref<string>('')
const showSearch = ref(true)
const loading = ref(false)
const checkedRowKeys = ref<number[]>([])

async function batchDeleteUsers() {
  if (!checkedRowKeys.value.length) return
  await request.delete('/api/users/batch', { data: { ids: checkedRowKeys.value } })
  notify.success(`已删除 ${checkedRowKeys.value.length} 个用户`)
  checkedRowKeys.value = []
  loadData()
}

// 分页
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 弹窗状态
const showAddModal = ref(false); const showEditModal = ref(false)
const showDeptModal = ref(false); const showRoleModal = ref(false); const showPostModal = ref(false); const showPwdModal = ref(false)
const selectedUser = ref<UserInfo | null>(null); const selectedDeptId = ref<number | null>(null)
const selectedPostId = ref<number | null>(null)
const selectedRoleIds = ref<number[]>([])
const addForm = ref({ username: '', password: '', nickname: '', email: '' })
const editForm = ref({ nickname: '', email: '' })
const saving = ref(false)
const showDetailModal = ref(false)
const detailUser = ref<UserInfo | null>(null)

function viewDetail(row: UserInfo) { detailUser.value = row; showDetailModal.value = true }

// 表单校验
const addFormRef = ref<FormInst | null>(null)
const editFormRef = ref<FormInst | null>(null)

const addRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 2, max: 20, message: '2-20个字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 30, message: '至少6位', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

const editRules: FormRules = {
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

const currentTheme = computed(() => localStorage.getItem('theme') === 'dark' ? darkTheme : lightTheme)

function dictLabel(dictType: string, value: string): string {
  return dictMap.value[dictType]?.[value] || value
}

function searchData() { page.value = 1; loadData() }

const actionOptions = (row: UserInfo) => [
  { label: '查看详情', key: 'detail' },
  { label: '编辑信息', key: 'edit' },
  { label: '分配部门', key: 'dept' },
  { label: '分配岗位', key: 'post' },
  { label: '分配角色', key: 'role' },
  { label: '重置密码', key: 'pwd' },
  { label: row.enabled ? '禁用' : '启用', key: 'toggle' },
]

function handleAction(key: string, row: UserInfo) {
  switch (key) {
    case 'detail': detailUser.value = row; showDetailModal.value = true; break
    case 'edit': selectedUser.value = row; editForm.value = { nickname: row.nickname, email: row.email }; showEditModal.value = true; break
    case 'dept': selectedUser.value = row; selectedDeptId.value = row.departmentId || null; showDeptModal.value = true; break
    case 'post': selectedUser.value = row; selectedPostId.value = row.postId || null; showPostModal.value = true; break
    case 'role': selectedUser.value = row; selectedRoleIds.value = (row.roles || []).map(r => r.id); showRoleModal.value = true; break
    case 'pwd': selectedUser.value = row; showPwdModal.value = true; break
    case 'toggle': toggleUser(row); break
  }
}

const allColumns = ref([
  { title: '用户名', key: 'username', width: 100, sorter: true },
  { title: '昵称', key: 'nickname', width: 100 },
  { title: '邮箱', key: 'email', width: 160, ellipsis: { tooltip: true } },
  { title: '来源', key: 'registerSource', width: 80, render: (row: UserInfo) => h(NTag, { type: row.registerSource === 'local' ? 'info' : 'success', size: 'small' }, { default: () => dictLabel('sys_user_source', row.registerSource) }) },
  { title: '部门', key: 'departmentName', width: 100, ellipsis: { tooltip: true } },
  { title: '岗位', key: 'postId', width: 90, render: (row: UserInfo) => { const p = posts.value.find(x => x.id === row.postId); return h('span', p ? p.postName : '-') } },
  { title: '角色', key: 'roles', width: 150, render: (row: UserInfo) => h(NSpace, { size: 4 }, { default: () => (row.roles || []).map(r => h(NTag, { type: 'info', size: 'small' }, { default: () => r.name })) }) },
  { title: '最后登录', key: 'lastLoginTime', width: 140, render: (row: any) => row.lastLoginTime ? row.lastLoginTime.substring(0,19).replace('T',' ') : '-' },
  { title: '状态', key: 'enabled', width: 60, render: (row: UserInfo) => h(NSwitch, { value: row.enabled, size: 'small', onUpdateValue: () => toggleUser(row) }) },
  { title: '操作', key: 'actions', width: 80, render: (row: UserInfo) => h(NDropdown, { options: actionOptions(row), onSelect: (k: string) => handleAction(k, row), size: 'small' }, { default: () => h(NButton, { size: 'small' }, { default: () => '更多 ▾' }) }) },
])
const { visibleColumns, toggleColumn, hiddenKeys } = useColumnManager(allColumns, 'users')
const columnOptions = computed(() => (allColumns.value as any[])
  .filter((c: any) => c.key && c.key !== 'actions')
  .map((c: any) => ({ key: c.key, title: c.title })),
)

async function loadData() {
  loading.value = true
  try {
    const [ures, dres, rres, pres, srcDict] = await Promise.all([
      request.get('/api/users', { params: { page: page.value, pageSize: pageSize.value, keyword: searchKeyword.value || undefined } }),
      request.get('/api/departments'),
      request.get('/api/roles'),
      request.get('/api/posts').catch(() => ({ data: [] })),
      request.get('/api/dict/data/sys_user_source').catch(() => ({ data: [] })),
    ])
    const data = ures.data
    users.value = data.items || data
    total.value = data.total || 0
    allRoles.value = Array.isArray(rres.data) ? rres.data : []
    posts.value = Array.isArray(pres.data) ? pres.data : []
    dictMap.value['sys_user_source'] = Object.fromEntries((srcDict.data || []).map((d: any) => [d.dictValue, d.dictLabel]))
    const flat: Dept[] = []
    function walk(list: any[]) { list.forEach((d: any) => { flat.push({ id: d.id, name: d.name }); if (d.children) walk(d.children) }) }
    walk(dres.data); depts.value = flat
  } catch {}
  loading.value = false
}

function onPageChange(p: number) { page.value = p; loadData() }
function onPageSizeChange(s: number) { pageSize.value = s; page.value = 1; loadData() }

async function toggleUser(user: UserInfo) { await request.put(`/api/users/${user.id}/toggle`); await loadData() }
async function saveAdd() {
  try {
    await addFormRef.value?.validate()
  } catch { return }
  saving.value = true
  try {
    await request.post('/api/users', addForm.value)
    showAddModal.value = false
    addForm.value = { username: '', password: '', nickname: '', email: '' }
    await loadData()
    notify.success('创建成功')
  } catch (e: any) { notify.error(e.response?.data?.message || '创建失败') }
  finally { saving.value = false }
}
async function saveEdit() {
  try {
    await editFormRef.value?.validate()
  } catch { return }
  if (!selectedUser.value) return
  saving.value = true
  try {
    await request.put(`/api/users/${selectedUser.value.id}/profile`, editForm.value)
    showEditModal.value = false
    await loadData()
    notify.success('更新成功')
  } catch (e: any) { notify.error(e.response?.data?.message || '更新失败') }
  finally { saving.value = false }
}
async function saveDept() { if (!selectedUser.value) return; await request.put(`/api/users/${selectedUser.value.id}/department`, { departmentId: selectedDeptId.value }); showDeptModal.value = false; await loadData(); notify.success('部门已分配') }
async function savePost() { if (!selectedUser.value) return; await request.put(`/api/users/${selectedUser.value.id}/post`, { postId: selectedPostId.value }); showPostModal.value = false; await loadData(); notify.success('岗位已分配') }
async function saveRoles() { if (!selectedUser.value) return; await request.put(`/api/users/${selectedUser.value.id}/roles`, { roleIds: selectedRoleIds.value }); showRoleModal.value = false; await loadData(); notify.success('角色已分配') }
async function resetPwd() { if (!selectedUser.value) return; await request.post(`/api/users/${selectedUser.value.id}/reset-pwd`); showPwdModal.value = false; notify.success('密码已重置为 123456') }

onMounted(loadData)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:24px">
      <n-card title="用户管理">
        <template #header-extra>
          <n-button type="primary" size="small" @click="showAddModal = true"><template #icon>＋</template>新增</n-button>
        </template>
        <n-space style="margin-bottom:12px" justify="space-between">
          <n-space>
            <n-button size="small" @click="showSearch = !showSearch" :type="showSearch ? 'primary' : 'default'" secondary>
              {{ showSearch ? '收起搜索' : '展开搜索' }}
            </n-button>
            <template v-if="showSearch">
              <n-input v-model:value="searchKeyword" placeholder="用户名/昵称/邮箱" clearable style="width:160px" size="small" @keyup:enter="searchData" />
              <n-select v-model:value="filterDept" :options="[{label:'全部部门',value:null},...depts.map(d=>({label:d.name,value:d.id}))]" size="small" style="width:120px" @update:value="searchData" />
              <n-select v-model:value="filterRole" :options="[{label:'全部角色',value:''},...allRoles.map(r=>({label:r.name,value:r.code}))]" size="small" style="width:110px" @update:value="searchData" />
              <n-select v-model:value="filterStatus" :options="[{label:'全部状态',value:''},{label:'正常',value:'true'},{label:'停用',value:'false'}]" size="small" style="width:100px" @update:value="searchData" />
              <n-button type="primary" size="small" @click="searchData">搜索</n-button>
              <n-button size="small" @click="searchKeyword = ''; filterDept = null; filterRole = ''; filterStatus = ''; searchData()">重置</n-button>
            </template>
          </n-space>
          <n-space>
            <n-button size="small" @click="loadData" secondary>刷新</n-button>
            <n-popover trigger="click" placement="bottom-end" :width="180">
              <template #trigger>
                <n-button size="small" secondary>列选项</n-button>
              </template>
              <div style="max-height:300px;overflow-y:auto">
                <div v-for="opt in columnOptions" :key="opt.key" style="padding:2px 0">
                  <n-checkbox :checked="!hiddenKeys.has(opt.key)"
                              @update:checked="toggleColumn(opt.key)">
                    {{ opt.title }}
                  </n-checkbox>
                </div>
              </div>
            </n-popover>
          </n-space>
        </n-space>
        <n-space v-if="checkedRowKeys.length" style="margin-bottom:8px">
          <n-button type="error" size="small" @click="batchDeleteUsers">删除选中({{ checkedRowKeys.length }})</n-button>
          <n-button size="small" @click="checkedRowKeys = []">取消选择</n-button>
        </n-space>
        <n-data-table
          :columns="visibleColumns" :data="users" :loading="loading" size="small" :bordered="false" :checked-row-keys="checkedRowKeys" @update:checked-row-keys="(ks: number[]) => checkedRowKeys = ks"
          :pagination="{ page: page, pageSize: pageSize, itemCount: total, pageSizes: [10,20,50,100], onChange: onPageChange, onUpdatePageSize: onPageSizeChange }"
          :row-key="(row: UserInfo) => row.id" remote
        />

        <!-- 新增用户 -->
        <n-modal v-model:show="showAddModal" title="新增用户" preset="card" display-directive="show" style="width:480px">
          <n-form ref="addFormRef" :model="addForm" :rules="addRules" label-placement="left" label-width="100">
            <n-form-item label="用户名" path="username">
              <n-input v-model:value="addForm.username" placeholder="2-20个字符" />
            </n-form-item>
            <n-form-item label="密码" path="password">
              <n-input v-model:value="addForm.password" type="password" placeholder="至少6位" />
            </n-form-item>
            <n-form-item label="昵称">
              <n-input v-model:value="addForm.nickname" />
            </n-form-item>
            <n-form-item label="邮箱" path="email">
              <n-input v-model:value="addForm.email" placeholder="可选" />
            </n-form-item>
          </n-form>
          <template #footer><n-space justify="end"><n-button @click="showAddModal = false">取消</n-button><n-button type="primary" :loading="saving" @click="saveAdd">确定</n-button></n-space></template>
        </n-modal>

        <!-- 编辑信息 -->
        <n-modal v-model:show="showEditModal" title="编辑用户信息" preset="card" display-directive="show">
          <n-form ref="editFormRef" :model="editForm" :rules="editRules" label-placement="left" label-width="100">
            <n-form-item label="用户名"><span>{{ selectedUser?.username }}</span></n-form-item>
            <n-form-item label="昵称">
              <n-input v-model:value="editForm.nickname" />
            </n-form-item>
            <n-form-item label="邮箱" path="email">
              <n-input v-model:value="editForm.email" />
            </n-form-item>
          </n-form>
          <template #footer><n-space justify="end"><n-button @click="showEditModal = false">取消</n-button><n-button type="primary" :loading="saving" @click="saveEdit">保存</n-button></n-space></template>
        </n-modal>

        <!-- 分配部门 -->
        <n-modal v-model:show="showDeptModal" title="分配部门" preset="card" display-directive="show">
          <n-form><n-form-item label="用户">{{ selectedUser?.username }}</n-form-item><n-form-item label="部门"><n-select v-model:value="selectedDeptId" :options="depts.map(d=>({label:d.name,value:d.id}))" clearable /></n-form-item></n-form>
          <template #footer><n-space justify="end"><n-button @click="showDeptModal = false">取消</n-button><n-button type="primary" @click="saveDept">保存</n-button></n-space></template>
        </n-modal>

        <!-- 分配岗位 -->
        <n-modal v-model:show="showPostModal" title="分配岗位" preset="card" display-directive="show">
          <n-form><n-form-item label="用户">{{ selectedUser?.username }}</n-form-item><n-form-item label="岗位"><n-select v-model:value="selectedPostId" :options="posts.map(p=>({label:p.postName,value:p.id}))" clearable /></n-form-item></n-form>
          <template #footer><n-space justify="end"><n-button @click="showPostModal = false">取消</n-button><n-button type="primary" @click="savePost">保存</n-button></n-space></template>
        </n-modal>

        <!-- 分配角色 -->
        <n-modal v-model:show="showRoleModal" title="分配角色" preset="card" display-directive="show">
          <n-form><n-form-item label="用户">{{ selectedUser?.username }}</n-form-item><n-form-item label="角色"><n-select v-model:value="selectedRoleIds" :options="allRoles.map(r=>({label:r.name+'('+r.code+')',value:r.id}))" multiple /></n-form-item></n-form>
          <template #footer><n-space justify="end"><n-button @click="showRoleModal = false">取消</n-button><n-button type="primary" @click="saveRoles">保存</n-button></n-space></template>
        </n-modal>

        <!-- 重置密码确认 -->
        <n-modal v-model:show="showPwdModal" title="重置密码" preset="card" display-directive="show">
          <p>确认将 <strong>{{ selectedUser?.username }}</strong> 的密码重置为 <strong>123456</strong>？</p>
          <template #footer><n-space justify="end"><n-button @click="showPwdModal = false">取消</n-button><n-button type="primary" @click="resetPwd">确认重置</n-button></n-space></template>
        </n-modal>

        <!-- 用户详情 -->
        <n-modal v-model:show="showDetailModal" title="用户详情" preset="card" display-directive="show" style="width:560px">
          <div v-if="detailUser" style="line-height:2">
            <p><strong>用户名：</strong>{{ detailUser.username }}</p>
            <p><strong>昵称：</strong>{{ detailUser.nickname || '-' }}</p>
            <p><strong>邮箱：</strong>{{ detailUser.email || '-' }}</p>
            <p><strong>来源：</strong>{{ dictLabel('sys_user_source', detailUser.registerSource) }}</p>
            <p><strong>部门：</strong>{{ detailUser.departmentName || '-' }}</p>
            <p><strong>岗位：</strong>{{ posts.find(p=>p.id===detailUser.postId)?.postName || '-' }}</p>
            <p><strong>角色：</strong>{{ (detailUser.roles||[]).map(r=>r.name).join(', ') || '-' }}</p>
            <p><strong>状态：</strong><n-tag :type="detailUser.enabled?'success':'default'" size="small">{{ detailUser.enabled?'正常':'停用' }}</n-tag></p>
            <p><strong>最后登录：</strong>{{ detailUser.lastLoginTime ? detailUser.lastLoginTime.substring(0,19).replace('T',' ') : '-' }}</p>
          </div>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>
