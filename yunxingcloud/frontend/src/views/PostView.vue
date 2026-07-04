<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { fetchPosts, createPost, updatePost, deletePost } from '@/api/post'
import { useNotify } from '@/composables/useNotify'
import { useI18n } from 'vue-i18n'
import CrudTable from '@/components/CrudTable.vue'
import { NInput, NInputNumber, NSelect, NSpace, NPopconfirm, NTag } from 'naive-ui'
import type { DataTableColumns, FormRules, FormInst } from 'naive-ui'

interface Post {
  id: number; postCode: string; postName: string; sortOrder: number
  status: string; remark: string; createdAt: string
}

const { t } = useI18n()
const notify = useNotify()
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
        h('span', { onClick: () => editPost(row), style: 'cursor:pointer;color:var(--n-color-target);font-size:13px' }, t('common.edit')),
        h(NPopconfirm, { onPositiveClick: () => delPost(row.id) }, {
          trigger: () => h('span', { style: 'cursor:pointer;color:#d03050;font-size:13px' }, t('common.delete')),
          default: () => t('common.confirmDelete')
        })
      ]
    })
  },
]

async function loadPosts() {
  loading.value = true
  try { const res = await fetchPosts(); posts.value = res.data } catch { notify.error(t('common.error')); }
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
    if (editing.value) await updatePost(editing.value.id, form.value)
    else await createPost(form.value)
    showModal.value = false
    notify.success(editing.value ? t('post.updateSuccess') : t('post.createSuccess'))
    await loadPosts()
  } catch (e: any) { notify.error(e.response?.data?.message || t('common.saveFailed')) } finally { saving.value = false }
}

async function delPost(id: number) {
  await deletePost(id)
  await loadPosts()
}
async function batchDeletePosts(ids: number[]) {
  try { for (const id of ids) await deletePost(id); loadPosts(); notify.success(t('common.deleted')) } catch { notify.error(t('common.saveFailed')) }
}

onMounted(loadPosts)
</script>

<template>
  <CrudTable
    :title="t('nav.posts')"
    :columns="columns"
    :data="filteredPosts"
    :loading="loading"
    :saving="saving"
    :show-modal="showModal"
    @update:show-modal="showModal = $event"
    v-model:search-keyword="searchKeyword"
    :editing="editing"
    @add="addPost"
    @save="savePost"
    @refresh="loadPosts"
    @batch-delete="batchDeletePosts"
    @search="() => {}"
  >
    <template #form>
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
    </template>
    <template #header-extra>
      <n-button type="primary" size="small" @click="addPost">+ {{ t('common.add') }}</n-button>
    </template>
  </CrudTable>
</template>
