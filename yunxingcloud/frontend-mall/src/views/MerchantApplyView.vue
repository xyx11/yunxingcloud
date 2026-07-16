<script setup lang="ts">
import { ref } from 'vue'
import request from '@/api/request'
import JdButton from '@/components/JdButton.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

interface ApplyResult { id?: number; error?: string }
interface StatusResult { status?: string; error?: string }

const form = ref({ name: '', phone: '', description: '' })
const loading = ref(false)
const submitted = ref(false)
const result = ref<ApplyResult | null>(null)
const checkPhone = ref('')
const checkResult = ref<StatusResult | null>(null)
const checking = ref(false)
const toast = useToast()
const { t } = useI18n()

async function doApply() {
  if (!form.value.name || !form.value.phone) return
  if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
    toast.error(t('merchant.phoneInvalid'))
    return
  }
  loading.value = true
  try {
    const r = await request.post('/merchant/apply', form.value)
    result.value = r.data; submitted.value = true
  } catch (e: unknown) { const err = e as { response?: { data?: { message?: string } } }; result.value = { error: err.response?.data?.message || t('merchant.applyFail') }; submitted.value = true } finally { loading.value = false }
}

async function checkStatus() {
  if (!checkPhone.value) return
  checking.value = true
  try { const r = await request.get(`/merchant/status/${checkPhone.value}`); checkResult.value = r.data } catch { checkResult.value = { error: t('merchant.checkError') } } finally { checking.value = false }
}
</script>

<template>
  <div class="merchant-page">
    <div class="merchant-hero">
      <h1>{{ t('merchant.title') }}</h1>
      <p>{{ t('merchant.subtitle') }}</p>
    </div>

    <div class="merchant-card">
      <div v-if="!submitted">
        <div class="form-group">
          <label class="form-label">{{ t('merchant.shopName') }}</label>
          <input v-model="form.name" class="form-input" :placeholder="t('merchant.shopNamePlaceholder')" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ t('merchant.phone') }}</label>
          <input v-model="form.phone" class="form-input" :placeholder="t('merchant.phonePlaceholder')" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ t('merchant.description') }}</label>
          <textarea v-model="form.description" class="form-textarea" :placeholder="t('merchant.descriptionPlaceholder')" />
        </div>
        <JdButton block size="lg" :loading="loading" :disabled="!form.name || !form.phone" @click="doApply">{{ t('merchant.submit') }}</JdButton>
      </div>
      <div v-else class="merchant-result">
        <div v-if="result?.id" class="result-success">
          <div class="success-icon">✅</div>
          <h3>{{ t('merchant.submitted') }}</h3>
          <p>{{ t('merchant.submittedDesc') }}</p>
          <p class="result-id">{{ t('merchant.applicationId') }}{{ result.id }}</p>
        </div>
        <div v-else class="result-error">
          <div class="error-icon">❌</div>
          <h3>{{ t('merchant.applyFail') }}</h3>
          <p>{{ result?.error }}</p>
          <JdButton @click="submitted = false; result = null">{{ t('merchant.reapply') }}</JdButton>
        </div>
      </div>
    </div>

    <div class="merchant-card">
      <h3 class="card-title">{{ t('merchant.checkTitle') }}</h3>
      <div class="form-row">
        <input v-model="checkPhone" class="form-input flex-1" :placeholder="t('merchant.checkPlaceholder')" />
        <JdButton @click="checkStatus" :loading="checking">{{ t('merchant.check') }}</JdButton>
      </div>
      <div v-if="checkResult" class="check-result">
        <p v-if="checkResult.error">{{ checkResult.error }}</p>
        <p v-else>{{ t('merchant.statusLabel') }}{{ checkResult.status === '0' ? t('merchant.statusReviewing') : checkResult.status === '1' ? t('merchant.statusApproved') : t('merchant.statusRejected') }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.merchant-page { max-width: 600px; margin: 0 auto; padding: var(--space-xxl) var(--space-md); }
.merchant-hero { text-align: center; margin-bottom: var(--space-xxl); }
.merchant-hero h1 { font-size: var(--font-h1); font-weight: 800; margin-bottom: var(--space-sm); }
.merchant-hero p { color: var(--text-secondary); font-size: var(--font-md); }
.merchant-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); box-shadow: var(--shadow-sm); margin-bottom: var(--space-lg); }
.card-title { font-size: var(--font-lg); font-weight: 600; margin-bottom: var(--space-lg); }
.form-group { margin-bottom: var(--space-lg); }
.form-label { display: block; font-size: var(--font-base); color: var(--text-secondary); margin-bottom: var(--space-xs); }
.form-input { width: 100%; padding: 10px; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: var(--font-base); box-sizing: border-box; }
.form-textarea { width: 100%; height: 80px; padding: 10px; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: var(--font-base); resize: none; box-sizing: border-box; }
.form-row { display: flex; gap: var(--space-sm); }
.flex-1 { flex: 1; }
.result-success { text-align: center; padding: var(--space-xl) 0; }
.result-success h3 { font-size: var(--font-lg); margin: var(--space-sm) 0; }
.result-success p { color: var(--text-secondary); }
.result-id { font-size: var(--font-sm); color: var(--text-tertiary); margin-top: var(--space-sm); }
.success-icon { font-size: 48px; }
.result-error { text-align: center; padding: var(--space-xl) 0; }
.result-error h3 { font-size: var(--font-lg); margin: var(--space-sm) 0; color: var(--jd-red); }
.error-icon { font-size: 48px; }
.check-result { margin-top: var(--space-md); padding: var(--space-md); background: var(--bg-hover); border-radius: var(--radius-sm); }

@media (max-width: 768px) {
  .merchant-page { padding: var(--space-lg) var(--space-md) 80px; }
}
</style>
