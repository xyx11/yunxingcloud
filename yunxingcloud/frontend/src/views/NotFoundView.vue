<script setup lang="ts">
import { useRouter } from 'vue-router'
import { NButton, NSpace, NTag } from 'naive-ui'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const router = useRouter()
const links = [
  { label: t('nav.home'), path: '/' },
  { label: t('nav.users'), path: '/users' },
  { label: t('nav.roles'), path: '/roles' },
  { label: t('nav.menus'), path: '/menus' },
  { label: t('nav.operlog'), path: '/operlog' },
]
</script>

<template>
  <div class="page">
    <div class="container">
      <div class="code">404</div>
      <div class="divider" />
      <div class="title">{{ t('notFound.title') }}</div>
      <div class="subtitle">{{ t('notFound.subtitle') }}</div>
      <div class="actions-gap">
        <n-space justify="center" size="small">
          <n-tag
            v-for="link in links" :key="link.path"
            type="info" size="medium" class="cursor-pointer"
            @click="router.push(link.path)"
          >
            {{ link.label }}
          </n-tag>
        </n-space>
      </div>
      <n-button type="primary" size="large" @click="router.push('/')">
        {{ t('notFound.backHome') }}
      </n-button>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.container { text-align: center; }
.code {
  font-size: 120px;
  font-weight: 700;
  color: rgba(255,255,255,0.9);
  line-height: 1;
}
.divider {
  width: 60px;
  height: 3px;
  background: rgba(255,255,255,0.5);
  margin: 16px auto;
  border-radius: 2px;
}
.title { font-size: 22px; color: #fff; font-weight: 500; }
.subtitle { font-size: 14px; color: rgba(255,255,255,0.7); margin-top: 8px; }
.actions-gap { margin: 28px 0 20px; }
</style>
