<script setup lang="ts">
import { ref, inject } from 'vue'
import { ToastInjectionKey } from '@/composables/useToast'
import request from '@/api/request'

const props = defineProps<{ modelValue?: string; multiple?: boolean }>()
const emit = defineEmits(['update:modelValue'])
const toast = inject(ToastInjectionKey)!
const uploading = ref(false)
const previewUrls = ref<string[]>(props.modelValue ? JSON.parse(props.modelValue || '[]') : [])

async function handleFile(e: Event) {
  const files = (e.target as HTMLInputElement).files
  if (!files?.length) return

  for (const file of Array.from(files)) {
    if (!file.type.startsWith('image/')) { toast.error('仅支持图片文件'); continue }
    if (file.size > 2 * 1024 * 1024) { toast.error(`图片不能超过2MB`); continue }
    uploading.value = true
    try {
      const form = new FormData()
      form.append('file', file)
      const res = await request.post('/files/upload', form)
      if (res.data.url) {
        previewUrls.value.push(res.data.url)
      }
    } catch { toast.error('上传失败') }
  }
  uploading.value = false
  emit('update:modelValue', JSON.stringify(previewUrls.value))
}

function remove(index: number) {
  previewUrls.value.splice(index, 1)
  emit('update:modelValue', JSON.stringify(previewUrls.value))
}
</script>

<template>
  <div>
    <div style="display:flex;flex-wrap:wrap;gap:8px;margin-bottom:8px">
      <div v-for="(url, i) in previewUrls" :key="i" style="width:80px;height:80px;border-radius:6px;overflow:hidden;position:relative;border:1px solid #eee">
        <img :src="url" style="width:100%;height:100%;object-fit:cover" />
        <button @click="remove(i)" style="position:absolute;top:2px;right:2px;width:18px;height:18px;border-radius:50%;background:rgba(0,0,0,.6);color:#fff;border:none;cursor:pointer;font-size:10px;line-height:18px">✕</button>
      </div>
      <label v-if="!multiple || previewUrls.length < 9" style="width:80px;height:80px;border:1px dashed #ddd;border-radius:6px;display:flex;align-items:center;justify-content:center;cursor:pointer;color:#999;font-size:24px;transition:border-color .2s"
             @mouseenter="(e:any) => e.target.style.borderColor='#e4393c'" @mouseleave="(e:any) => e.target.style.borderColor='#ddd'">
        <span v-if="uploading">⏳</span>
        <span v-else>+</span>
        <input type="file" accept="image/*" :multiple="multiple" @change="handleFile" style="display:none" />
      </label>
    </div>
    <p style="font-size:11px;color:#999">支持 jpg/png/gif，单张不超过 2MB</p>
  </div>
</template>
