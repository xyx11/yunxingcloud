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
import type { DataTableColumns, FormRules, FormInst } from 'naive-ui'

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
const formRef = ref<FormInst>()
const rules: FormRules = {
  postCode: [{ required: true, message: t('validate.required'), trigger: 'blur' }],
  postName: [{ required: true, message: t('validate.required'), trigger: 'blur' }],
}
const form = ref({ postCode: '', postName: '', sortOrder: 0, status: '0', remark: '' })
const searchKeyword = ref('')

const statusOptions = [
  { label: t('user.enabledLabel'), value: '0' },
  { label: t('user.disabledLabel'), value: '1' },
]

const filteredPosts = computed(() => {
  const kw = searchKeyword.value.toLowerCase()
  if (!kw) return posts.value
  return posts.value.filter(p => p.postName.toLowerCase().includes(kw) || p.postCode.toLowerCase().includes(kw))
})

const columns: DataTableColumns<Post> = [
  { title: 'ID', key: 'id', width: 50 },
  { title: t('post.code'), key: 'postCode', width: 100 },
  { title: t('post.userCount'), key: 'user_count', width: 60 },
  { title: t('post.name'), key: 'postName', width: 140 },
  { title: t('post.sort'), key: 'sortOrder', width: 60 },
  {
    title: t('post.status'), key: 'status', width: 60,
    render: (row) => h(NTag, { type: row.status === '0' ? 'success' : 'default', size: 'small' },
      { default: () => row.status === '0' ? t('user.enabledLabel') : t('user.disabledLabel') })
  },
  { title: t('common.createdAt'), key: 'createdAt', width: 150 },
  {
    title: t('post.actions'), key: 'actions', width: 120,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'tiny', onClick: () => editPost(row) }, { default: () => t('common.edit') }),
        h(NPopconfirm, { onPositiveClick: () => delPost(row.id) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }),
          default: () => t('common.confirmDelete')
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
  if (formRef.value) { try { await formRef.value.validate() } catch { return } }
  saving.value = true
  try {
    if (editing.value) await request.put(`/api/posts/${editing.value.id}`, form.value)
    else await request.post('/api/posts', form.value)
    showModal.value = false
    notify.success(editing.value ? t('post.updateSuccess') : t('post.createSuccess'))
    await loadPosts()
  } catch (e: any) { notify.error(e.response?.data?.message || t('common.saveFailed')) } finally { saving.value = false }
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
          <n-button type="primary" size="small" @click="addPost"><template #icon>＋</template>{{ t('common.add') }}</n-button>
        </template>
        <n-space style="margin-bottom:12px" justify="space-between">
          <n-space>
            <n-input v-model:value="searchKeyword" :placeholder="t('post.searchPlaceholder')" size="small" clearable style="width:180px" />
            <n-button type="primary" size="small" @click="() => {}">{{ t('common.search') }}</n-button>
            <n-button size="small" @click="searchKeyword = ''">{{ t('common.reset') }}</n-button>
          </n-space>
          <n-space><n-button size="small" @click="loadPosts" secondary>{{ t('common.refresh') }}</n-button></n-space>
        </n-space>
        <n-dataTable
          :columns="columns" :data="filteredPosts" :loading="loading" size="small"
          :bordered="false" :pagination="{ pageSize: 10, pageSizes: [10,20,50,100] }"
          :row-key="(row: Post) => row.id"
        />

        <n-modal v-model:show="showModal" :title="editing ? t('post.edit') : t('post.add')" preset="card" display-directive="show" style="max-width:480px;width:95%">
          <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80">
            <n-form-item :label="t('post.code')">
              <n-input v-model:value="form.postCode" :disabled="!!editing" />
            </n-form-item>
            <n-form-item :label="t('post.name')">
              <n-input v-model:value="form.postName" />
            </n-form-item>
            <n-form-item :label="t('post.sort')">
              <n-input-number v-model:value="form.sortOrder" :min="0" style="width:100%" />
            </n-form-item>
            <n-form-item :label="t('post.status')">
              <n-select v-model:value="form.status" :options="statusOptions" />
            </n-form-item>
            <n-form-item :label="t('common.remark')">
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
