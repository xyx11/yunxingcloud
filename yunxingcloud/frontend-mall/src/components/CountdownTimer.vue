<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useI18n } from '@/locales'

const { t } = useI18n()

const props = withDefaults(defineProps<{
  endTime: string | number
  label?: string
  compact?: boolean
}>(), { label: '距结束', compact: false })

const now = ref(Date.now())
let timer: number | null = null

onMounted(() => { timer = window.setInterval(() => now.value = Date.now(), 1000) })
onUnmounted(() => { if (timer) clearInterval(timer) })

const remaining = computed(() => Math.max(0, new Date(props.endTime).getTime() - now.value))
const h = computed(() => Math.floor(remaining.value / 3600000))
const m = computed(() => Math.floor((remaining.value % 3600000) / 60000))
const s = computed(() => Math.floor((remaining.value % 60000) / 1000))
const isExpired = computed(() => remaining.value <= 0)
const isUrgent = computed(() => remaining.value > 0 && remaining.value < 3600000)
const pad = (n: number) => String(n).padStart(2, '0')
</script>

<template>
  <span v-if="isExpired" class="countdown-ended" :class="compact ? 'cd-compact' : 'cd-normal'">
    {{ label }}{{ t('countdown.ended') }}
  </span>
  <span v-else class="countdown" :class="[compact ? 'cd-compact' : 'cd-normal', { 'pulse-urgent': isUrgent }]">
    <span v-if="label" class="countdown-label">{{ label }}</span>
    <span class="cd-box">{{ pad(h) }}</span>
    <span class="countdown-sep">:</span>
    <span class="cd-box">{{ pad(m) }}</span>
    <span class="countdown-sep">:</span>
    <span class="cd-box" :class="{ 'pulse-bg': isUrgent }">{{ pad(s) }}</span>
  </span>
</template>

<style scoped>
.countdown-label { color: #999; margin-right: 4px; font-size: 11px; }
.countdown-sep { margin: 0 1px; color: #999; }
.cd-box { background: #1a1a1a; color: #fff; padding: 1px 5px; border-radius: 3px; font-weight: 700; font-variant-numeric: tabular-nums; }
.cd-compact { font-size: 11px; }
.cd-normal { font-size: 13px; }
.countdown.cd-compact { font-size: 12px; }
.countdown.cd-normal { font-size: 14px; }
.pulse-bg { background: #f10215 !important; animation: pulse-bg .8s ease-in-out infinite; }
@keyframes pulse-bg {
  0%, 100% { background: #f10215; }
  50% { background: #ff4444; }
}
</style>