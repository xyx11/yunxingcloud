<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import {
  NConfigProvider, NCard, NDataTable, NButton, NModal, NForm, NFormItem,
  NInput, NInputNumber, NSelect, NSpace, NPopconfirm, NTag,
  darkTheme, lightTheme
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import type { DataTableColumns } from 'naive-ui'

interface Post {
  id: number; postCode: string; postName: string; sortOrder: number
  status: string; remark: string; createdAt: string
}

const { t } = useI18n()
const notify = useNotify()
const currentTheme = computed(() => localStorage.getItem("theme") === "dark" ? darkTheme : lightTheme)
const posts = ref<Post[]>([])
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editing = ref<Post | null>(null)
const form = ref({ postCode: '', postName: '', sortOrder: 0, status: '0', remark: '' })
const searchKeyword = ref('')

const statusOptions = [
  { label: '正常', value: '0' },
  { label: '停用', value: '1' },
]

const filteredPosts = computed(() => {
  const kw = searchKeyword.value.toLowerCase()
  if (!kw) return posts.value
  return posts.value.filter(p => p.postName.toLowerCase().includes(kw) || p.postCode.toLowerCase().includes(kw))
})

const columns: DataTableColumns<Post> = [
  { title: 'ID', key: 'id', width: 50 },
  { title: '岗位编码', key: 'postCode', width: 100 },
  { title: '用户数', key: 'user_count', width: 60 },
  { title: '岗位名称', key: 'postName', width: 140 },
  { title: '排序', key: 'sortOrder', width: 60 },
  {
    title: '状态', key: 'status', width: 60,
    render: (row) => h(NTag, { type: row.status === '0' ? 'success' : 'default', size: 'small' },
      { default: () => row.status === '0' ? '正常' : '停用' })
  },
  { title: '创建时间', key: 'createdAt', width: 150 },
  {
    title: '操作', key: 'actions', width: 120,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'tiny', onClick: () => editPost(row) }, { default: () => '编辑' }),
        h(NPopconfirm, { onPositiveClick: () => delPost(row.id) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => '删除' }),
          default: () => '确认删除?'
        })
      ]
    })
  },
]

async function loadPosts() {
  loading.value = true
  try { const res = await request.get('/api/posts'); posts.value = res.data } catch {}
  loading.value = false
}

function addPost() {
  editing.value = null
  form.value = { postCode: '', postName: '', sortOrder: 0, status: '0', remark: '' }
  showModal.value = true
}

function editPost(post: Post) {
  editing.value = post
  form.value = { postCode: post.postCode, postName: post.postName, sortOrder: post.sortOrder || 0, status: post.status || '0', remark: post.remark || '' }
  showModal.value = true
}

async function savePost() {
  saving.value = true
  try {
    if (editing.value) await request.put(`/api/posts/${editing.value.id}`, form.value)
    else await request.post('/api/posts', form.value)
    showModal.value = false
    notify.success(editing.value ? '更新成功' : '创建成功')
    await loadPosts()
  } catch (e: any) { notify.error(e.response?.data?.message || '保存失败') } finally { saving.value = false }
}

async function delPost(id: number) {
  await request.delete(`/api/posts/${id}`)
  await loadPosts()
}

onMounted(loadPosts)
</script>

<template>
  <n-config-provider :theme="currentTheme">
    <div style="padding:20px">
      <n-card :title="t('nav.posts')">
        <template #header-extra>
          <n-button type="primary" size="small" @click="addPost"><template #icon>＋</template>新增</n-button>
        </template>
        <n-space style="margin-bottom:12px" justify="space-between">
          <n-space>
            <n-input v-model:value="searchKeyword" placeholder="岗位名称/编码" size="small" clearable style="width:180px" />
            <n-button type="primary" size="small" @click="() => {}">搜索</n-button>
            <n-button size="small" @click="searchKeyword = ''">重置</n-button>
          </n-space>
          <n-space><n-button size="small" @click="loadPosts" secondary>刷新</n-button></n-space>
        </n-space>
        <n-dataTable
          :columns="columns" :data="filteredPosts" :loading="loading" size="small"
          :bordered="false" :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }"
          :row-key="(row: Post) => row.id"
        />

        <n-modal v-model:show="showModal" :title="editing ? t('common.edit') : t('common.add')" style="width:480px">
          <n-form label-placement="left" label-width="80">
            <n-form-item label="岗位编码">
              <n-input v-model:value="form.postCode" :disabled="!!editing" />
            </n-form-item>
            <n-form-item label="岗位名称">
              <n-input v-model:value="form.postName" />
            </n-form-item>
            <n-form-item label="排序">
              <n-input-number v-model:value="form.sortOrder" :min="0" style="width:100%" />
            </n-form-item>
            <n-form-item label="状态">
              <n-select v-model:value="form.status" :options="statusOptions" />
            </n-form-item>
            <n-form-item label="备注">
              <n-input v-model:value="form.remark" />
            </n-form-item>
          </n-form>
          <template #footer>
            <n-space justify="end">
              <n-button @click="showModal = false">{{ t('common.cancel') }}</n-button>
              <n-button type="primary" :loading="saving" @click="savePost">{{ t('common.save') }}</n-button>
            </n-space>
          </template>
        </n-modal>
      </n-card>
    </div>
  </n-config-provider>
</template>
