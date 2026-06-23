<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { fetchClientInfo } from '@/api/consent'
import {
  NConfigProvider, NCard, NButton, NSpace, NTag, NSpin,
} from 'naive-ui'

const { t } = useI18n()

const route = useRoute()

const clientId = (route.query.client_id as string) || ''
const state = (route.query.state as string) || ''
const rawScopes = (route.query.scope as string || '').split(/[+ ]/).filter(Boolean)

const clientName = ref(clientId)
const loading = ref(false)
const submitted = ref(false)

const scopeDescriptions: Record<string, string> = {
  'openid': t('consent.openid'),
  'profile': t('consent.profile'),
  'email': t('consent.email'),
}

function scopeLabel(scope: string): string {
  return scopeDescriptions[scope] || scope
}

onMounted(async () => {
  if (!clientId) return
  loading.value = true
  try {
    const info = await fetchClientInfo(clientId)
    clientName.value = info.clientName
  } catch {
    // fallback to clientId
  } finally {
    loading.value = false
  }
})

function approve() {
  if (submitted.value) return
  submitted.value = true

  const form = document.createElement('form')
  form.method = 'POST'
  form.action = '/oauth2/authorize'

  appendHidden(form, 'client_id', clientId)
  appendHidden(form, 'state', state)
  rawScopes.forEach(s => appendHidden(form, 'scope', s))
  appendHidden(form, 'consent', 'true')

  document.body.appendChild(form)
  form.submit()
}

function deny() {
  window.location.href = '/'
}

function appendHidden(form: HTMLFormElement, name: string, value: string) {
  const input = document.createElement('input')
  input.type = 'hidden'
  input.name = name
  input.value = value
  form.appendChild(input)
}
</script>

<template>
  <n-config-provider>
    <div class="consent-page">
      <n-card class="consent-card">
        <h1 class="title">{{ t('consent.title') }}</h1>

        <n-spin :show="loading">
          <p class="prompt">
            <strong>{{ clientName }}</strong> {{ t('consent.prompt') }}
          </p>

          <n-space vertical class="scopes">
            <n-tag v-for="scope in rawScopes" :key="scope" type="info" size="large">
              {{ scopeLabel(scope) }}
            </n-tag>
          </n-space>
        </n-spin>

        <n-space justify="center" class="actions">
          <n-button size="large" @click="deny" :disabled="submitted">{{ t('consent.deny') }}</n-button>
          <n-button type="primary" size="large" @click="approve" :loading="submitted">{{ t('consent.approve') }}</n-button>
        </n-space>
      </n-card>
    </div>
  </n-config-provider>
</template>

<style scoped>
.consent-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.consent-card {
  width: 460px;
  border-radius: 12px;
  padding: 8px;
}

.title {
  text-align: center;
  color: #333;
  margin-bottom: 20px;
  font-size: 22px;
}

.prompt {
  text-align: center;
  color: #555;
  margin-bottom: 20px;
  font-size: 15px;
}

.scopes {
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
}

.actions {
  margin-top: 28px;
}
</style>
