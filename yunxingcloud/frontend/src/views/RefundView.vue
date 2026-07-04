<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { NCard, NDataTable, NButton, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const items = ref<any[]>([])

const columns: DataTableColumns<any> = [
  { title:'ID', key:'id', width:60 }, { title:'用户', key:'username', width:90 },
  { title:'订单号', key:'orderNo', width:170 },
  { title:'类型', key:'type', width:70, render(r:any){ return ({refund:'退款',return:'退货',exchange:'换货'} as Record<string,string>)[r.type] } },
  { title:'退款金额', key:'refundAmount', width:100, render(r:any){ return r.refundAmount?'¥'+(r.refundAmount/100).toFixed(2):'-' } },
  { title:'原因', key:'reason', width:150, ellipsis:{tooltip:true} },
  { title:'状态', key:'status', width:80, render(r:any){ return h(NTag,{size:'small',type:r.status==='1'||r.status==='4'?'success':r.status==='2'?'error':'warning'},{default:()=>({'0':'待审核','1':'已通过','2':'已拒绝','3':'退款中','4':'已完成'} as Record<string,string>)[r.status]}) } },
  { title:'时间', key:'createdAt', width:140, render(r:any){ return r.createdAt?.substring(0,16) } },
]

async function load() { const r = await request.get('/after-sale'); items.value = r.data }
onMounted(load)
</script>
<template>
  <n-card title="退款管理">
    <n-dataTable :columns="columns" :data="items" :pagination="{pageSize:10}" />
  </n-card>
</template>