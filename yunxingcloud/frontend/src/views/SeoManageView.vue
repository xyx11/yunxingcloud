<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NForm, NFormItem, NInput, NButton, NSpace, NAlert } from 'naive-ui'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'

const { t } = useI18n()
const notify = useNotify()
const homeMeta = ref({ title: '', description: '', keywords: '' })
const sitemap = ref('')

async function load() {
  try { const r = await request.get('/api/seo/meta/home'); Object.assign(homeMeta.value, r.data) } catch(e) { console.error(e) }
  try { const r = await request.get('/api/seo/sitemap'); sitemap.value = typeof r.data === 'string' ? r.data : '' } catch(e) { console.error(e) }
}
function copySitemap() { navigator.clipboard.writeText(sitemap.value); notify.success(t('seo.copied')) }
onMounted(load)
</script>
<template>
  <div class="view-pad">
    <n-space vertical size="large">
      <n-card :title="t('seo.title')">
        <n-form :model="homeMeta" label-placement="left" label-width="100">
          <n-form-item :label="t('seo.homeTitle')"><n-input v-model:value="homeMeta.title" placeholder="Mall - 最佳购物体验" /></n-form-item>
          <n-form-item :label="t('seo.homeDesc')"><n-input v-model:value="homeMeta.description" type="textarea" placeholder="商城首页 meta description" /></n-form-item>
          <n-form-item :label="t('seo.homeKeywords')"><n-input v-model:value="homeMeta.keywords" placeholder="keyword1,keyword2" /></n-form-item>
          <n-button type="primary" @click="notify.success(t('seo.preview'))">{{ t('seo.preview') }}</n-button>
        </n-form>
      </n-card>
      <n-card :title="t('seo.sitemap')">
        <n-alert type="info" class="seo-alert">{{ t('seo.sitemapAlert') }}</n-alert>
        <pre class="seo-sitemap">{{ sitemap || t('seo.loading') }}</pre>
        <n-button size="small" @click="copySitemap" class="seo-copy-btn">{{ t('seo.copyContent') }}</n-button>
      </n-card>
    </n-space>
  </div>
</template>
