<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSpace, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const items = ref<any[]>([])
const showModal = ref(false)
const form = ref({ name:'', contact:'', phone:'', email:'', address:'' })

const columns: DataTableColumns<any> = [
  { title: '名称', key: 'name', width: 160 }, { title: '联系人', key: 'contact', width: 80 },
  { title: '电话', key: 'phone', width: 120 }, { title: '邮箱', key: 'email', width: 160 },
  { title: '地址', key: 'address', width: 200, ellipsis:{tooltip:true} },
]

async function load() { const r = await request.get('/suppliers'); items.value = r.data }
async function save() { await request.post('/suppliers', form.value); showModal.value=false; notify.success('创建成功'); load() }
onMounted(load)
</script>
<template>
  <n-card title="供应商管理">
    <n-space vertical><n-button type="primary" @click="showModal=true">新增供应商</n-button>
      <n-dataTable :columns="columns" :data="items" :pagination="{pageSize:10}" />
    </n-space>
    <n-modal v-model:show="showModal" title="新增供应商" preset="card" style="max-width:500px">
      <n-form :model="form">
        <n-form-item label="名称"><n-input v-model:value="form.name" /></n-form-item>
        <n-form-item label="联系人"><n-input v-model:value="form.contact" /></n-form-item>
        <n-form-item label="电话"><n-input v-model:value="form.phone" /></n-form-item>
        <n-form-item label="邮箱"><n-input v-model:value="form.email" /></n-form-item>
        <n-form-item label="地址"><n-input v-model:value="form.address" /></n-form-item>
      </n-form>
      <template #footer><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save">保存</n-button></template>
    </n-modal>
  </n-card>
</template>