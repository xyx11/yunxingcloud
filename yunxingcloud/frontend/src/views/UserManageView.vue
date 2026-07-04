<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, h, computed } from 'vue'
import { fetchUsers, fetchDepartments, fetchRoles, fetchPosts, fetchUserSourceDict, createUser, updateUserProfile, batchDeleteUsers, toggleUser, setUserDepartment, setUserPost, setUserRoles, resetUserPassword, type UserInfo, type Dept, type Role, type Post } from '@/api/user'
import { useNotify } from '@/composables/useNotify'

import { NCard, NDataTable, NButton, NModal, NDrawer, NDrawerContent, NForm, NFormItem, NSelect, NSpace, NTag, NInput, NSwitch, NDropdown, NPopover, NCheckbox, NTabs, NTabPane, NDivider, type FormRules, type FormInst } from 'naive-ui'
import { useColumnManager } from '@/composables/useColumnManager'
import request from '@/api/request'

const { t } = useI18n()
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
const filterDateFrom = ref(''); const filterDateTo = ref('')
const showSearch = ref(true)
const loading = ref(false)
const checkedRowKeys = ref<number[]>([])

async function doBatchDelete() {
  if (!checkedRowKeys.value.length) return
  await batchDeleteUsers(checkedRowKeys.value)
  notify.success(t('user.deleteBatch', { count: checkedRowKeys.value.length }))
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
const custOrders = ref<any[]>([]); const custAddresses = ref<any[]>([]); const custCoupons = ref<any[]>([])
const custLoading = ref(false)

async function openCustDetail(user: UserInfo) {
  detailUser.value = user; showDetailModal.value = true; custLoading.value = true
  try {
    const [o, a, c] = await Promise.all([
      request.get('/api/orders?username=' + encodeURIComponent(user.username)).catch(()=>({data:[]})),
      request.get('/api/addresses/' + user.username).catch(()=>({data:[]})),
      request.get('/api/coupons/my/' + user.username).catch(()=>({data:[]})),
    ])
    custOrders.value = o.data || []; custAddresses.value = a.data || []; custCoupons.value = c.data || []
  } catch { custOrders.value = []; custAddresses.value = []; custCoupons.value = [] }
  custLoading.value = false
}

function getPostName(user: UserInfo | null) { return user ? (posts.value.find(p => p.id === user.postId)?.postName || '-') : '-' }

// 表单校验
const addFormRef = ref<FormInst | null>(null)
const editFormRef = ref<FormInst | null>(null)

const addRules: FormRules = {
  username: [{ required: true, message: () => t('user.loginRequired'), trigger: 'blur' }, { min: 2, max: 20, message: () => t('validate.usernameLen'), trigger: 'blur' }],
  password: [{ required: true, message: () => t('user.passwordRequired'), trigger: 'blur' }, { min: 6, max: 30, message: () => t('validate.passwordLen'), trigger: 'blur' }],
  email: [{ type: 'email', message: () => t('validate.emailFormat'), trigger: 'blur' }],
}

const editRules: FormRules = {
  email: [{ type: 'email', message: () => t('validate.emailFormat'), trigger: 'blur' }],
}



function dictLabel(dictType: string, value: string): string {
  return dictMap.value[dictType]?.[value] || value
}

function searchData() { page.value = 1; loadData() }

const actionOptions = (row: UserInfo) => [
  { label: t('user.viewDetail'), key: 'detail' },
  { label: t('user.editInfo'), key: 'edit' },
  { label: t('user.assignDept'), key: 'dept' },
  { label: t('user.assignPost'), key: 'post' },
  { label: t('user.assignRole'), key: 'role' },
  { label: t('user.resetPwd'), key: 'pwd' },
  { label: row.enabled ? t('user.disable') : t('user.enable'), key: 'toggle' },
]

function handleAction(key: string, row: UserInfo) {
  switch (key) {
    case 'detail': openCustDetail(row); break
    case 'edit': selectedUser.value = row; editForm.value = { nickname: row.nickname, email: row.email }; showEditModal.value = true; break
    case 'dept': selectedUser.value = row; selectedDeptId.value = row.departmentId || null; showDeptModal.value = true; break
    case 'post': selectedUser.value = row; selectedPostId.value = row.postId || null; showPostModal.value = true; break
    case 'role': selectedUser.value = row; selectedRoleIds.value = (row.roles || []).map(r => r.id); showRoleModal.value = true; break
    case 'pwd': selectedUser.value = row; showPwdModal.value = true; break
    case 'toggle': toggleUserById(row); break
  }
}

const allColumns = ref([
  { title: t('user.username'), key: 'username', width: 100, sorter: true },
  { title: t('user.nickname'), key: 'nickname', width: 100 },
  { title: t('user.email'), key: 'email', width: 160, ellipsis: { tooltip: true } },
  { title: t('user.source'), key: 'registerSource', width: 80, render: (row: UserInfo) => h(NTag, { type: row.registerSource === 'local' ? 'info' : 'success', size: 'small' }, { default: () => dictLabel('sys_user_source', row.registerSource) }) },
  { title: t('user.dept'), key: 'departmentName', width: 100, ellipsis: { tooltip: true } },
  { title: t('user.post'), key: 'postId', width: 90, render: (row: UserInfo) => { const p = posts.value.find(x => x.id === row.postId); return h('span', p ? p.postName : '-') } },
  { title: t('user.role'), key: 'roles', width: 150, render: (row: UserInfo) => h(NSpace, { size: 4 }, { default: () => (row.roles || []).map(r => h(NTag, { type: 'info', size: 'small' }, { default: () => r.name })) }) },
  { title: t('user.lastLogin'), key: 'lastLoginTime', width: 140, render: (row: any) => row.lastLoginTime ? row.lastLoginTime.substring(0,19).replace('T',' ') : '-' },
  { title: t('user.enabled'), key: 'enabled', width: 60, render: (row: UserInfo) => h(NSwitch, { value: row.enabled, size: 'small', onUpdateValue: () => toggleUserById(row) }) },
  { title: t('user.actions'), key: 'actions', width: 80, render: (row: UserInfo) => h(NDropdown, { options: actionOptions(row), onSelect: (k: string) => handleAction(k, row), size: 'small' }, { default: () => h(NButton, { size: 'small' }, { default: () => t('user.moreActions') }) }) },
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
      fetchUsers({ page: page.value, pageSize: pageSize.value, keyword: searchKeyword.value || undefined }),
      fetchDepartments(),
      fetchRoles(),
      fetchPosts().catch(() => ({ data: [] })),
      fetchUserSourceDict().catch(() => ({ data: [] })),
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
  } catch { /* ignore */ }
  loading.value = false
}

function onPageChange(p: number) { page.value = p; loadData() }
function onPageSizeChange(s: number) { pageSize.value = s; page.value = 1; loadData() }

async function toggleUserById(user: UserInfo) { await toggleUser(user.id); await loadData() }
async function saveAdd() {
  try {
    await addFormRef.value?.validate()
  } catch { return }
  saving.value = true
  try {
    await createUser(addForm.value)
    showAddModal.value = false
    addForm.value = { username: '', password: '', nickname: '', email: '' }
    await loadData()
    notify.success(t('user.createSuccess'))
  } catch (e: any) { notify.error(e.response?.data?.message || t('user.createFailed')) }
  finally { saving.value = false }
}
async function saveEdit() {
  try {
    await editFormRef.value?.validate()
  } catch { return }
  if (!selectedUser.value) return
  saving.value = true
  try {
    await updateUserProfile(selectedUser.value.id, editForm.value)
    showEditModal.value = false
    await loadData()
    notify.success(t('user.updateSuccess'))
  } catch (e: any) { notify.error(e.response?.data?.message || t('user.updateFailed')) }
  finally { saving.value = false }
}
async function saveDept() { if (!selectedUser.value) return; await setUserDepartment(selectedUser.value.id, selectedDeptId.value); showDeptModal.value = false; await loadData(); notify.success(t('user.deptAssigned')) }
async function savePost() { if (!selectedUser.value) return; await setUserPost(selectedUser.value.id, selectedPostId.value); showPostModal.value = false; await loadData(); notify.success(t('user.postAssigned')) }
async function saveRoles() { if (!selectedUser.value) return; await setUserRoles(selectedUser.value.id, selectedRoleIds.value); showRoleModal.value = false; await loadData(); notify.success(t('user.roleAssigned')) }
async function resetPwd() { if (!selectedUser.value) return; await resetUserPassword(selectedUser.value.id); showPwdModal.value = false; notify.success(t('user.pwdResetSuccess')) }

function exportCSV() {
  const head = ['用户名','昵称','邮箱','手机','部门','角色','状态','创建时间']
  const rows = allUsers.value.map((u:any) => [u.username||'',u.nickname||'',u.email||'',u.phone||'',u.deptName||'',u.roleNames||'',u.status==='0'?'正常':'禁用',u.createdAt||''])
  const csv = [head,...rows].map(r=>r.map(c=>'"'+String(c).replace(/"/g,'""')+'"').join(',')).join('\n')
  const blob = new Blob(['﻿'+csv],{type:'text/csv'}); const a = document.createElement('a'); a.href=URL.createObjectURL(blob); a.download='users.csv'; a.click()
}
onMounted(loadData)
</script>

<template>
  <div style="padding:24px">
    <n-card :title="t('nav.users')">
      <template #header-extra>
        <n-button type="primary" size="small" @click="showAddModal = true"><template #icon>＋</template>{{ t('common.add') }}</n-button>
      </template>
      <n-space style="margin-bottom:12px" justify="space-between">
        <n-space>
          <n-button size="small" @click="showSearch = !showSearch" :type="showSearch ? 'primary' : 'default'" secondary>
            {{ showSearch ? t('common.collapseSearch') : t('common.expandSearch') }}
          </n-button>
          <template v-if="showSearch">
            <n-input v-model:value="searchKeyword" :placeholder="t('user.searchPlaceholder')" clearable style="max-width:160px;width:95%" size="small" @keyup:enter="searchData" />
            <n-select v-model:value="filterDept" :options="[{label:t('user.filterDept'),value:null as any},...depts.map(d=>({label:d.name,value:d.id}))]" size="small" style="max-width:120px;width:95%" @update:value="searchData" />
            <n-select v-model:value="filterRole" :options="[{label:t('user.filterRole'),value:''},...allRoles.map(r=>({label:r.name,value:r.code}))]" size="small" style="max-width:110px;width:95%" @update:value="searchData" />
            <n-select v-model:value="filterStatus" :options="[{label:t('user.filterStatus'),value:''},{label:t('user.enabledLabel'),value:'true'},{label:t('user.disabledLabel'),value:'false'}]" size="small" style="max-width:100px;width:95%" @update:value="searchData" />
            <input type="date" v-model="filterDateFrom" @change="searchData" style="width:120px;font-size:12px;padding:2px 6px;border:1px solid #ddd;border-radius:3px" />
            <input type="date" v-model="filterDateTo" @change="searchData" style="width:120px;font-size:12px;padding:2px 6px;border:1px solid #ddd;border-radius:3px" />
            <n-button type="primary" size="small" @click="searchData">{{ t('common.search') }}</n-button>
            <n-button size="small" @click="searchKeyword = ''; filterDept = null; filterRole = ''; filterStatus = ''; filterDateFrom = ''; filterDateTo = ''; searchData()">{{ t('common.reset') }}</n-button>
          </template>
        </n-space>
        <n-space>
          <n-space><n-button size="small" @click="exportCSV">{{ t('operlog.exportCsv') }}</n-button><n-button size="small" @click="loadData" secondary>{{ t('common.refresh') }}</n-button></n-space>
          <n-popover trigger="click" placement="bottom-end" :width="180">
            <template #trigger>
              <n-button size="small" secondary>{{ t('common.columnOptions') }}</n-button>
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
      <n-space v-if="checkedRowKeys.length" style="margin-bottom:8px">
        <n-button type="error" size="small" @click="doBatchDelete">{{ t('common.batchDelete') }}({{ checkedRowKeys.length }})</n-button>
        <n-button size="small" @click="checkedRowKeys = []">{{ t('common.deselect') }}</n-button>
      </n-space>
      <n-data-table
        :columns="visibleColumns" :data="users" :loading="loading" size="small" :bordered="false" :checked-row-keys="checkedRowKeys" @update:checked-row-keys="(ks) => checkedRowKeys = ks as number[]"
        :pagination="{ page: page, pageSize: pageSize, itemCount: total, pageSizes: [10,20,50,100], onChange: onPageChange, onUpdatePageSize: onPageSizeChange }"
        :row-key="(row: UserInfo) => row.id" remote
      />

      <!-- 新增用户 -->
      <n-modal v-model:show="showAddModal" :title="t('user.addUser')" preset="card" display-directive="show" style="max-width:480px;width:95%">
        <n-form ref="addFormRef" :model="addForm" :rules="addRules" label-placement="left" label-width="100">
          <n-form-item :label="t('user.username')" path="username">
            <n-input v-model:value="addForm.username" :placeholder="t('validate.usernameLen')" />
          </n-form-item>
          <n-form-item :label="t('login.password')" path="password">
            <n-input v-model:value="addForm.password" type="password" :placeholder="t('validate.passwordLen')" />
          </n-form-item>
          <n-form-item :label="t('user.nickname')">
            <n-input v-model:value="addForm.nickname" />
          </n-form-item>
          <n-form-item :label="t('user.email')" path="email">
            <n-input v-model:value="addForm.email" :placeholder="t('user.optional')" />
          </n-form-item>
        </n-form>
        <template #footer><n-space justify="end"><n-button @click="showAddModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="saveAdd">{{ t('common.ok') }}</n-button></n-space></template>
      </n-modal>

      <!-- 编辑信息 -->
      <n-modal v-model:show="showEditModal" :title="t('user.editUser')" preset="card" display-directive="show">
        <n-form ref="editFormRef" :model="editForm" :rules="editRules" label-placement="left" label-width="100">
          <n-form-item :label="t('user.username')"><span>{{ selectedUser?.username }}</span></n-form-item>
          <n-form-item :label="t('user.nickname')">
            <n-input v-model:value="editForm.nickname" />
          </n-form-item>
          <n-form-item :label="t('user.email')" path="email">
            <n-input v-model:value="editForm.email" />
          </n-form-item>
        </n-form>
        <template #footer><n-space justify="end"><n-button @click="showEditModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="saveEdit">{{ t('common.save') }}</n-button></n-space></template>
      </n-modal>

      <!-- 分配部门 -->
      <n-modal v-model:show="showDeptModal" :title="t('user.assignDept')" preset="card" display-directive="show">
        <n-form><n-form-item :label="t('user.username')">{{ selectedUser?.username }}</n-form-item><n-form-item :label="t('user.dept')"><n-select v-model:value="selectedDeptId" :options="depts.map(d=>({label:d.name,value:d.id}))" clearable /></n-form-item></n-form>
        <template #footer><n-space justify="end"><n-button @click="showDeptModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" @click="saveDept">{{ t('common.save') }}</n-button></n-space></template>
      </n-modal>

      <!-- 分配岗位 -->
      <n-modal v-model:show="showPostModal" :title="t('user.assignPost')" preset="card" display-directive="show">
        <n-form><n-form-item :label="t('user.username')">{{ selectedUser?.username }}</n-form-item><n-form-item :label="t('user.post')"><n-select v-model:value="selectedPostId" :options="posts.map(p=>({label:p.postName,value:p.id}))" clearable /></n-form-item></n-form>
        <template #footer><n-space justify="end"><n-button @click="showPostModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" @click="savePost">{{ t('common.save') }}</n-button></n-space></template>
      </n-modal>

      <!-- 分配角色 -->
      <n-modal v-model:show="showRoleModal" :title="t('user.assignRole')" preset="card" display-directive="show">
        <n-form><n-form-item :label="t('user.username')">{{ selectedUser?.username }}</n-form-item><n-form-item :label="t('user.role')"><n-select v-model:value="selectedRoleIds" :options="allRoles.map(r=>({label:r.name+'('+r.code+')',value:r.id}))" multiple /></n-form-item></n-form>
        <template #footer><n-space justify="end"><n-button @click="showRoleModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" @click="saveRoles">{{ t('common.save') }}</n-button></n-space></template>
      </n-modal>

      <!-- 重置密码确认 -->
      <n-modal v-model:show="showPwdModal" :title="t('user.resetPwd')" preset="card" display-directive="show">
        <p>{{ t('user.confirmResetPwd', { username: selectedUser?.username }) }}</p>
        <template #footer><n-space justify="end"><n-button @click="showPwdModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" @click="resetPwd">{{ t('user.confirmReset') }}</n-button></n-space></template>
      </n-modal>

      <!-- 用户详情 Drawer -->
      <n-drawer v-model:show="showDetailModal" :width="520" placement="right">
        <n-drawer-content :title="'客户: ' + (detailUser?.username || '')" closable>
          <template v-if="detailUser">
            <n-tabs type="line" size="small" :default-value="'info'">
              <n-tab-pane name="info" tab="基本信息">
                <n-form label-placement="left" label-width="70" size="small">
                  <n-form-item :label="t('user.username')"><span>{{ detailUser.username }}</span></n-form-item>
                  <n-form-item :label="t('user.nickname')"><span>{{ detailUser.nickname || '-' }}</span></n-form-item>
                  <n-form-item :label="t('user.email')"><span>{{ detailUser.email || '-' }}</span></n-form-item>
                  <n-form-item :label="t('user.dept')"><span>{{ detailUser.departmentName || '-' }}</span></n-form-item>
                  <n-form-item :label="t('user.post')"><span>{{ getPostName(detailUser) }}</span></n-form-item>
                  <n-form-item :label="t('user.role')"><span>{{ (detailUser.roles||[]).map(r=>r.name).join(', ') || '-' }}</span></n-form-item>
                  <n-form-item :label="t('user.enabled')"><n-tag :type="detailUser.enabled?'success':'default'" size="small">{{ detailUser.enabled?t('user.enabledLabel'):t('user.disabledLabel') }}</n-tag></n-form-item>
                  <n-form-item label="最后登录"><span>{{ detailUser.lastLoginTime ? detailUser.lastLoginTime.substring(0,19).replace('T',' ') : '-' }}</span></n-form-item>
                </n-form>
              </n-tab-pane>
              <n-tab-pane name="orders" tab="订单">
                <div v-if="custOrders.length" style="max-height:40vh;overflow:auto">
                  <div v-for="o in custOrders.slice(0,10)" :key="o.id" style="display:flex;justify-content:space-between;padding:8px;border-bottom:1px solid #f0f0f0;font-size:13px">
                    <span style="color:#999">{{ o.orderNo }}</span>
                    <span>¥{{ ((o.totalAmount||0)/100).toFixed(2) }}</span>
                    <n-tag size="tiny" :type="o.status==='3'?'success':o.status==='4'?'default':'info'">{{ ['待付','已付','已发','完成','取消'][Number(o.status)]||o.status }}</n-tag>
                  </div>
                </div>
                <div v-else style="text-align:center;color:#ccc;padding:20px">暂无订单</div>
              </n-tab-pane>
              <n-tab-pane name="addr" tab="地址">
                <div v-if="custAddresses.length" style="max-height:40vh;overflow:auto">
                  <div v-for="a in custAddresses" :key="a.id" style="padding:8px;margin-bottom:8px;background:#f9f9f9;border-radius:6px;font-size:13px">
                    <div style="font-weight:600">{{ a.name }} {{ a.phone }}</div>
                    <div style="color:#666">{{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}</div>
                    <n-tag v-if="a.isDefault" size="tiny" type="success">默认</n-tag>
                  </div>
                </div>
                <div v-else style="text-align:center;color:#ccc;padding:20px">暂无地址</div>
              </n-tab-pane>
              <n-tab-pane name="coupon" tab="优惠券">
                <div v-if="custCoupons.length" style="max-height:40vh;overflow:auto">
                  <div v-for="c in custCoupons.slice(0,10)" :key="c.id" style="padding:8px;margin-bottom:8px;background:#fef9e7;border-radius:6px;font-size:13px;display:flex;justify-content:space-between">
                    <span>券ID: {{ c.couponId }}</span>
                    <n-tag size="tiny" :type="c.status==='0'?'warning':'default'">{{ c.status==='0'?'未使用':c.status==='1'?'已使用':'已过期' }}</n-tag>
                  </div>
                </div>
                <div v-else style="text-align:center;color:#ccc;padding:20px">暂无优惠券</div>
              </n-tab-pane>
            </n-tabs>
          </template>
        </n-drawer-content>
      </n-drawer>
    </n-card>
  </div>
</template>
