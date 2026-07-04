<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NForm, NFormItem, NInput, NInputNumber, NButton, NSpace, NTag, NAlert } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const notify = useNotify()
const hotLimit = ref(8); const newLimit = ref(8)
const hotProducts = ref<any[]>([]); const newProducts = ref<any[]>([])

async function load() {
  try { const r = await request.get('/api/recommend/hot'); hotProducts.value = r.data || [] } catch {}
  try { const r = await request.get('/api/recommend/new'); newProducts.value = r.data || [] } catch {}
}
onMounted(load)
</script>
<template>
  <div style="padding:20px">
    <n-space vertical size="large">
      <n-card title="推荐管理">
        <n-alert type="info" style="margin-bottom:12px">控制商城首页推荐的展示规则和数量</n-alert>
        <n-form label-placement="left" label-width="120">
          <n-form-item label="热门商品数量"><n-input-number v-model:value="hotLimit" :min="1" :max="50" /> <span style="margin-left:8px;color:#999;font-size:12px">当前显示 {{ hotProducts.length }} 个</span></n-form-item>
          <n-form-item label="新品推荐数量"><n-input-number v-model:value="newLimit" :min="1" :max="50" /> <span style="margin-left:8px;color:#999;font-size:12px">当前显示 {{ newProducts.length }} 个</span></n-form-item>
          <n-button type="primary" @click="notify.success('推荐配置需要在后端Service中调整')">保存配置</n-button>
        </n-form>
      </n-card>
      <n-card title="当前推荐状态">
        <p><b>热门商品:</b> {{ hotProducts.map((p:any) => p.name).join(', ') || '无' }}</p>
        <p><b>新品推荐:</b> {{ newProducts.map((p:any) => p.name).join(', ') || '无' }}</p>
      </n-card>
    </n-space>
  </div>
</template>
