<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useI18n } from '@/locales'

const { t } = useI18n()

const props = withDefaults(defineProps<{
  endTime: string | number
  label?: string
  compact?: boolean
}>(), { label: '⏱', compact: false })

const now = ref(Date.now())
let timer: number | null = null

onMounted(() => { timer = window.setInterval(() => now.value = Date.now(), 1000) })
onUnmounted(() => { if (timer) clearInterval(timer) })

function parseEndTime(val: string | number): number {
  if (typeof val === 'number') return val
  // Normalize ISO strings without timezone: append Z for consistent UTC interpretation
  const s = val.trim()
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/.test(s)) return new Date(s + 'Z').getTime()
  return new Date(s).getTime()
}
const remaining = computed(() => Math.max(0, parseEndTime(props.endTime) - now.value))
const h = computed(() => Math.floor(remaining.value / 3600000))
const m = computed(() => Math.floor((remaining.value % 3600000) / 60000))
const s = computed(() => Math.floor((remaining.value % 60000) / 1000))
const isExpired = computed(() => remaining.value <= 0)
const isUrgent = computed(() => remaining.value > 0 && remaining.value < 3600000)
const pad = (n: number) => String(n).padStart(2, '0')
</script>

<template>
  <span v-if="isExpired" class="countdown-ended" :class="compact ? 'cd-compact' : 'cd-normal'" role="timer" :aria-label="t('countdown.ended')">
    {{ label }}{{ t('countdown.ended') }}
  </span>
  <span v-else class="countdown" :class="[compact ? 'cd-compact' : 'cd-normal', { 'pulse-urgent': isUrgent }]" role="timer" :aria-label="`${h} ${t('countdown.hours')} ${m} ${t('countdown.minutes')} ${s} ${t('countdown.seconds')}`">
    <span v-if="label" class="countdown-label">{{ label }}</span>
    <span class="cd-box">{{ pad(h) }}</span>
    <span class="countdown-sep">:</span>
    <span class="cd-box">{{ pad(m) }}</span>
    <span class="countdown-sep">:</span>
    <span class="cd-box" :class="{ 'pulse-bg': isUrgent }">{{ pad(s) }}</span>
  </span>
</template>

<style scoped>
.countdown-label { color: var(--text-tertiary); margin-right: 6px; font-size: var(--font-xs); }
.countdown-sep { margin: 0 2px; color: var(--text-tertiary); font-weight: 700; }
.cd-box { background: var(--jd-red); color: #fff; padding: 2px 6px; border-radius: 4px; font-weight: 700; font-variant-numeric: tabular-nums; min-width: 24px; text-align: center; display: inline-block; transition: transform .15s ease; }
.cd-box:not(.pulse-bg) { animation: digitPop .5s ease; }
@keyframes digitPop { 0% { transform: scale(1); } 50% { transform: scale(1.15); } 100% { transform: scale(1); } }
@keyframes timerShake { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.03); } }
.cd-compact { font-size: var(--font-xs); }
.cd-normal { font-size: var(--font-base); }
.countdown.cd-compact { font-size: 13px; }
.countdown.cd-normal { font-size: 16px; }
.pulse-urgent { animation: timerShake .5s ease infinite; }
.pulse-bg { background: var(--jd-red) !important; animation: pulseBg .8s ease-in-out infinite; }
@keyframes pulseBg {
  0%, 100% { background: var(--jd-red); }
  50% { background: var(--jd-red-light); }
}
@keyframes timerShake {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.03); }
}
</style>