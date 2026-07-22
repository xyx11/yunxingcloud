<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSelect, NSpace, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchNotifications, sendNotification, type Notification } from '@/api/notification'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const items = ref<Notification[]>([])
const showModal = ref(false)
const form = ref<Notification>({ username:'ALL', title:'', content:'', type:'system' })

const columns: DataTableColumns<Notification> = [
  { title: t('common.recipient'), key: 'username', width: 90 },
  { title: t('common.title'), key: 'title', width: 180 },
  { title: t('common.type'), key: 'type', width: 70, render(r:any){ return h(NTag,{size:'small',type:r.type==='order'?'info':r.type==='promotion'?'warning':'default'},{default:()=>r.type}) } },
  { title: t('common.readStatus'), key: 'isRead', width: 60, render(r:any){ return h(NTag,{size:'small',type:r.isRead?'success':'warning'},{default:()=>r.isRead ? t('common.isRead') : t('common.unread')}) } },
  { title: t('common.createdAt'), key: 'createdAt', width: 140, render(r:any){ return r.createdAt?.substring(0,16) } },
]

async function load() { const r = await fetchNotifications(); items.value = r.data }
async function send() { await sendNotification(form.value); showModal.value=false; notify.success(t('common.sendSuccess')); load() }
onMounted(load)
</script>
<template>
  <n-card :title="t('nav.notifications')">
    <n-space vertical><n-button type="primary" @click="showModal=true">{{ t('common.send') }}</n-button>
      <n-dataTable :columns="columns" :data="items" :pagination="{pageSize:10}" />
    </n-space>
    <n-drawer v-model:show="showModal" :width="400" placement="right">
      <n-drawer-content :title="t('common.send')" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal=false">{{ t('common.cancel') }}</n-button><n-button type="primary" @click="send">{{ t('common.send') }}</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="70" size="small">
          <n-form-item :label="t('common.recipient')"><n-input v-model:value="form.username" placeholder="ALL=All Users" /></n-form-item>
          <n-form-item :label="t('common.title')"><n-input v-model:value="form.title" /></n-form-item>
          <n-form-item :label="t('common.content')"><n-input v-model:value="form.content" type="textarea" /></n-form-item>
          <n-form-item :label="t('common.type')"><n-select v-model:value="form.type" :options="[{label:t('common.system'),value:'system'},{label:t('common.order'),value:'order'},{label:t('common.promotion'),value:'promotion'}]" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </n-card>
</template>