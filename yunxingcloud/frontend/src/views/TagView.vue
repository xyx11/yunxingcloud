<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NInputNumber, NColorPicker, NSpace, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchTags, createTag, type Tag } from '@/api/tag'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const items = ref<Tag[]>([])
const showModal = ref(false)
const form = ref<Tag>({ name:'', color:'#e4393c', sortOrder:0 })

const columns: DataTableColumns<Tag> = [
  { title: '名称', key: 'name', width: 120 },
  { title: '颜色', key: 'color', width: 80, render(r:any){ return h(NTag,{size:'small',style:{background:r.color,color:'#fff',border:'none'}},{default:()=>r.name}) } },
  { title: '排序', key: 'sortOrder', width: 60 },
]

async function load() { const r = await fetchTags(); items.value = r.data }
async function save() { await createTag(form.value); showModal.value=false; notify.success('创建成功'); load() }
onMounted(load)
</script>
<template>
  <n-card title="标签管理">
    <n-space vertical><n-button type="primary" @click="showModal=true">新建标签</n-button>
      <n-dataTable :columns="columns" :data="items" :pagination="false" />
    </n-space>
    <n-modal v-model:show="showModal" title="新建标签" preset="card" style="max-width:300px">
      <n-form :model="form">
        <n-form-item label="名称"><n-input v-model:value="form.name" /></n-form-item>
        <n-form-item label="颜色"><n-color-picker v-model:value="form.color" /></n-form-item>
        <n-form-item label="排序"><n-input-number v-model:value="form.sortOrder" :min="0" /></n-form-item>
      </n-form>
      <template #footer><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save">保存</n-button></template>
    </n-modal>
  </n-card>
</template>