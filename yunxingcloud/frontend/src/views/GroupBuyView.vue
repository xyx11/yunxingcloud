<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NCard, NDataTable, NButton, NModal, NForm, NFormItem, NInput, NInputNumber, NSpace, NTag, NPopconfirm } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchGroupBuys, createGroupBuy, expireGroupBuys, type GroupBuy } from '@/api/groupbuy'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const loading = ref(false)
const items = ref<GroupBuy[]>([])
const showModal = ref(false)
const form = ref<GroupBuy>({ productId: 0, minMembers: 2, groupPrice: 0 })

const columns: DataTableColumns<GroupBuy> = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '商品ID', key: 'productId', width: 80 },
  { title: '拼团价(分)', key: 'groupPrice', width: 100 },
  { title: '成团人数', key: 'minMembers', width: 80 },
  { title: '状态', key: 'status', width: 80, render(r: GroupBuy) { return h(NTag, { size:'small', type: r.status==='0'?'success':'default' }, { default: () => r.status==='0'?'进行中':'已结束' }) } },
  { title: '开始', key: 'startTime', width: 140, render(r:any){ return r.startTime?.substring(0,16) } },
  { title: '结束', key: 'endTime', width: 140, render(r:any){ return r.endTime?.substring(0,16) } },
]

async function load() { loading.value = true; try { const r = await fetchGroupBuys(); items.value = r.data } finally { loading.value = false } }
async function save() { await createGroupBuy(form.value); showModal.value = false; notify.success('创建成功'); load() }
async function expire() { await expireGroupBuys(); notify.success('已处理超时团'); load() }
onMounted(load)
</script>
<template>
  <n-card title="拼团管理">
    <n-space vertical>
      <n-space><n-button type="primary" @click="showModal=true">新增拼团</n-button><n-button @click="expire">处理超时团</n-button></n-space>
      <n-dataTable :columns="columns" :data="items" :loading="loading" :pagination="{pageSize:10}" />
    </n-space>
    <n-modal v-model:show="showModal" title="新增拼团" preset="card" style="max-width:400px">
      <n-form :model="form">
        <n-form-item label="商品ID"><n-input-number v-model:value="form.productId" :min="1" /></n-form-item>
        <n-form-item label="拼团价(分)"><n-input-number v-model:value="form.groupPrice" :min="1" /></n-form-item>
        <n-form-item label="成团人数"><n-input-number v-model:value="form.minMembers" :min="2" /></n-form-item>
      </n-form>
      <template #footer><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save">保存</n-button></template>
    </n-modal>
  </n-card>
</template>