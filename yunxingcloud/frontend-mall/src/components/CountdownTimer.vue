<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'

const props = withDefaults(defineProps<{
  endTime: string | number
  label?: string
  showMs?: boolean
  compact?: boolean
}>(), { label: '距结束', showMs: false, compact: false })

const now = ref(Date.now())
let timer: any = null

onMounted(() => { timer = setInterval(() => now.value = Date.now(), 1000) })
onUnmounted(() => clearInterval(timer))

const remaining = computed(() => Math.max(0, new Date(props.endTime).getTime() - now.value))
const h = computed(() => Math.floor(remaining.value / 3600000))
const m = computed(() => Math.floor((remaining.value % 3600000) / 60000))
const s = computed(() => Math.floor((remaining.value % 60000) / 1000))
const isExpired = computed(() => remaining.value <= 0)
const pad = (n: number) => String(n).padStart(2, '0')
</script>

<template>
  <span v-if="isExpired" class="countdown-expired" style="color:#999;font-size:12px">{{ label }}已结束</span>
  <span v-else class="countdown" :style="{fontSize:compact?'12px':'14px'}">
    <span v-if="label" style="color:#999;margin-right:4px;font-size:11px">{{ label }}</span>
    <span style="background:#333;color:#fff;padding:1px 4px;border-radius:3px;font-weight:700;font-size:inherit">{{ pad(h) }}</span>
    <span style="margin:0 1px">:</span>
    <span style="background:#333;color:#fff;padding:1px 4px;border-radius:3px;font-weight:700;font-size:inherit">{{ pad(m) }}</span>
    <span style="margin:0 1px">:</span>
    <span style="background:#333;color:#fff;padding:1px 4px;border-radius:3px;font-weight:700;font-size:inherit">{{ pad(s) }}</span>
  </span>
</template>