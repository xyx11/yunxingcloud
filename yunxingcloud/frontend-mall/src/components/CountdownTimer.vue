<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'

const props = withDefaults(defineProps<{
  endTime: string | number
  label?: string
  compact?: boolean
}>(), { label: '距结束', compact: false })

const now = ref(Date.now())
let timer: any = null

onMounted(() => { timer = setInterval(() => now.value = Date.now(), 1000) })
onUnmounted(() => clearInterval(timer))

const remaining = computed(() => Math.max(0, new Date(props.endTime).getTime() - now.value))
const h = computed(() => Math.floor(remaining.value / 3600000))
const m = computed(() => Math.floor((remaining.value % 3600000) / 60000))
const s = computed(() => Math.floor((remaining.value % 60000) / 1000))
const isExpired = computed(() => remaining.value <= 0)
const isUrgent = computed(() => remaining.value > 0 && remaining.value < 3600000)
const pad = (n: number) => String(n).padStart(2, '0')

const boxStyle = {
  background: '#1a1a1a',
  color: '#fff',
  padding: '1px 5px',
  borderRadius: '3px',
  fontWeight: '700' as const,
  fontVariantNumeric: 'tabular-nums' as const,
}
</script>

<template>
  <span v-if="isExpired" class="countdown-ended" :style="{color:'#999',fontSize:compact?'11px':'13px'}">
    {{ label }}已结束
  </span>
  <span v-else class="countdown" :class="{ 'pulse-urgent': isUrgent }" :style="{fontSize:compact?'12px':'14px'}">
    <span v-if="label" style="color:#999;margin-right:4px;font-size:11px">{{ label }}</span>
    <span :style="boxStyle">{{ pad(h) }}</span>
    <span style="margin:0 1px;color:#999">:</span>
    <span :style="boxStyle">{{ pad(m) }}</span>
    <span style="margin:0 1px;color:#999">:</span>
    <span :style="[boxStyle, isUrgent ? {background:'#f10215',animation:'pulse-bg .8s ease-in-out infinite'} : {}]">{{ pad(s) }}</span>
  </span>
</template>

<style scoped>
@keyframes pulse-bg {
  0%, 100% { background: #f10215; }
  50% { background: #ff4444; }
}
.pulse-urgent .countdown-ended + span {
  animation: none;
}
</style>