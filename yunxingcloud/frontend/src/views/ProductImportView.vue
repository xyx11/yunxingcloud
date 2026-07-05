<script setup lang="ts">
import { ref, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NButton, NUpload, NSpace, NDataTable, NTag, NAlert } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const { t } = useI18n()
const notify = useNotify()
const importing = ref(false)
const results = ref<any[]>([])
const summary = ref('')

function parseCSV(text: string): any[] {
  const lines = text.split('\n').filter(l => l.trim())
  if (lines.length < 2) return []
  const headers = lines[0].split(',').map(h => h.trim().replace(/"/g,''))
  return lines.slice(1).map(line => {
    const vals = line.split(',').map(v => v.trim().replace(/"/g,''))
    const obj: any = {}
    headers.forEach((h, i) => { if (vals[i] !== undefined) obj[h] = vals[i] })
    return obj
  })
}

async function handleImport(options: any) {
  const reader = new FileReader()
  reader.onload = async (e) => {
    const text = e.target?.result as string
    const rows = parseCSV(text)
    if (!rows.length) { notify.error(t('productImport.csvError')); return }
    importing.value = true; results.value = []; let ok = 0, fail = 0
    for (const row of rows) {
      try {
        const data = { name: row.name || row['商品名称'], price: Math.round(parseFloat(row.price || row['价格']) * 100) || 100, stock: parseInt(row.stock || row['库存']) || 0, description: row.description || row['描述'] || '', imageUrl: row.imageUrl || row['图片'] || '', status: '0' }
        if (!data.name) { fail++; results.value.push({ ...data, _status: 'fail', _reason: t('productImport.missingName') }); continue }
        await request.post('/api/products', data)
        ok++; results.value.push({ ...data, _status: 'ok' })
      } catch (e: any) { fail++; results.value.push({ ...row, _status: 'fail', _reason: e.response?.data?.message || t('productImport.createFail') }) }
    }
    summary.value = t('productImport.summary', { ok, fail })
    notify.success(summary.value)
    importing.value = false
  }
  reader.readAsText(options.file.file)
}
</script>
<template>
  <div class="view-pad">
    <n-card :title="t('productImport.title')">
      <n-alert type="info" class="import-alert">
        {{ t('productImport.csvFormat') }}<br>
        {{ t('productImport.csvExample') }}
      </n-alert>
      <n-space vertical>
        <n-upload :custom-request="handleImport" accept=".csv" :show-file-list="false">
          <n-button type="primary" :loading="importing" size="large">{{ importing ? t('productImport.importing') : t('productImport.chooseCSV') }}</n-button>
        </n-upload>
      </n-space>
    </n-card>
    <n-card v-if="summary" :title="summary" class="mt-16">
      <n-dataTable :columns="[
        { title: t('common.name'), key: 'name' },
        { title: t('product.price'), key: 'price', width: 80 },
        { title: t('product.status'), key: '_status', width: 80, render: (r: any) => h(NTag, { type: r._status === 'ok' ? 'success' : 'error', size: 'small' }, { default: () => r._status === 'ok' ? t('productImport.success') : t('productImport.fail') }) },
        { title: t('productImport.reason'), key: '_reason', width: 120 }
      ]" :data="results" size="small" :pagination="{ pageSize: 20 }" />
    </n-card>
  </div>
</template>
