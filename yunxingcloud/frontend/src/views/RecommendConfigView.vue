<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NForm, NFormItem, NInputNumber, NButton, NSpace, NAlert } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const { t } = useI18n()
const notify = useNotify()
const hotLimit = ref(8); const newLimit = ref(8)
const hotProducts = ref<any[]>([]); const newProducts = ref<any[]>([])

async function load() {
  try { const r = await request.get('/api/recommend/hot'); hotProducts.value = r.data || [] } catch(e) { console.error(e) }
  try { const r = await request.get('/api/recommend/new'); newProducts.value = r.data || [] } catch(e) { console.error(e) }
}
onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-space vertical size="large">
      <n-card :title="t('recommend.title')">
        <n-alert type="info" class="mb-12">{{ t('recommend.alert') }}</n-alert>
        <n-form label-placement="left" label-width="120">
          <n-form-item :label="t('recommend.hotLimit')"><n-input-number v-model:value="hotLimit" :min="1" :max="50" /> <span class="recommend-count">{{ t('recommend.currently', { n: hotProducts.length }) }}</span></n-form-item>
          <n-form-item :label="t('recommend.newLimit')"><n-input-number v-model:value="newLimit" :min="1" :max="50" /> <span class="recommend-count">{{ t('recommend.currently', { n: newProducts.length }) }}</span></n-form-item>
          <n-button type="primary" @click="notify.success(t('recommend.configHint'))">{{ t('recommend.saveConfig') }}</n-button>
        </n-form>
      </n-card>
      <n-card :title="t('recommend.currentStatus')">
        <p><b>{{ t('recommend.hotProducts') }}</b> {{ hotProducts.map((p:any) => p.name).join(', ') || t('recommend.none') }}</p>
        <p><b>{{ t('recommend.newProducts') }}</b> {{ newProducts.map((p:any) => p.name).join(', ') || t('recommend.none') }}</p>
      </n-card>
    </n-space>
  </div>
</template>
