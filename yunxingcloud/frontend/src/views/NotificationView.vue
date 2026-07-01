<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NSelect, NSpace, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchNotifications, sendNotification, type Notification } from '@/api/notification'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const items = ref<Notification[]>([])
const showModal = ref(false)
const form = ref<Notification>({ username:'ALL', title:'', content:'', type:'system' })

const columns: DataTableColumns<Notification> = [
  { title: '接收人', key: 'username', width: 90 }, { title: '标题', key: 'title', width: 200 },
  { title: '类型', key: 'type', width: 70 },
  { title: '已读', key: 'isRead', width: 60, render(r:any){ return r.isRead ? '✓' : '' } },
  { title: '时间', key: 'createdAt', width: 140, render(r:any){ return r.createdAt?.substring(0,16) } },
]

async function load() { const r = await fetchNotifications(); items.value = r.data }
async function send() { await sendNotification(form.value); showModal.value=false; notify.success('已发送'); load() }
onMounted(load)
</script>
<template>
  <n-card title="通知管理">
    <n-space vertical><n-button type="primary" @click="showModal=true">发送通知</n-button>
      <n-dataTable :columns="columns" :data="items" :pagination="{pageSize:10}" />
    </n-space>
    <n-modal v-model:show="showModal" title="发送通知" preset="card" style="max-width:400px">
      <n-form :model="form">
        <n-form-item label="接收人"><n-input v-model:value="form.username" placeholder="ALL=全员" /></n-form-item>
        <n-form-item label="标题"><n-input v-model:value="form.title" /></n-form-item>
        <n-form-item label="内容"><n-input v-model:value="form.content" type="textarea" /></n-form-item>
        <n-form-item label="类型"><n-select v-model:value="form.type" :options="[{label:'系统',value:'system'},{label:'订单',value:'order'},{label:'促销',value:'promotion'}]" /></n-form-item>
      </n-form>
      <template #footer><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="send">发送</n-button></template>
    </n-modal>
  </n-card>
</template>