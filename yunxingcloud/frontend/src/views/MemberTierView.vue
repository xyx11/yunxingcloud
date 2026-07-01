<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NInputNumber, NSpace, NSwitch, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const items = ref<any[]>([])
const showModal = ref(false)
const form = ref({ name:'', minPoints:0, discountRate:100, freeShipping:false, birthdayGift:false, prioritySupport:false, sortOrder:0 })

const columns: DataTableColumns<any> = [
  { title: '等级', key: 'name', width: 80 },
  { title: '所需积分', key: 'minPoints', width: 100 },
  { title: '折扣率', key: 'discountRate', width: 80, render(r:any){ return r.discountRate+'%' } },
  { title: '包邮', key: 'freeShipping', width: 60, render(r:any){ return r.freeShipping?'✓':'' } },
  { title: '生日礼', key: 'birthdayGift', width: 60, render(r:any){ return r.birthdayGift?'✓':'' } },
]

async function load() { const r = await request.get('/member/tiers'); items.value = r.data }
async function save() { await request.post('/member/tiers', form.value); showModal.value=false; notify.success('创建成功'); load() }
onMounted(load)
</script>
<template>
  <n-card title="会员等级">
    <n-space vertical><n-button type="primary" @click="showModal=true">新建等级</n-button>
      <n-dataTable :columns="columns" :data="items" :pagination="false" />
    </n-space>
    <n-modal v-model:show="showModal" title="新建等级" preset="card" style="max-width:400px">
      <n-form :model="form">
        <n-form-item label="等级名"><n-input v-model:value="form.name" /></n-form-item>
        <n-form-item label="所需积分"><n-input-number v-model:value="form.minPoints" :min="0" /></n-form-item>
        <n-form-item label="折扣率(%)"><n-input-number v-model:value="form.discountRate" :min="1" :max="100" /></n-form-item>
        <n-form-item label="包邮"><n-switch v-model:value="form.freeShipping" /></n-form-item>
        <n-form-item label="生日礼"><n-switch v-model:value="form.birthdayGift" /></n-form-item>
        <n-form-item label="优先客服"><n-switch v-model:value="form.prioritySupport" /></n-form-item>
      </n-form>
      <template #footer><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save">保存</n-button></template>
    </n-modal>
  </n-card>
</template>