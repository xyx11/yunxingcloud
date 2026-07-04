<script setup lang="ts">
import { onMounted, onBeforeUnmount, watch, ref, nextTick } from 'vue'
import { createEditor, createToolbar } from '@wangeditor/editor'
import '@wangeditor/editor/dist/css/style.css'

const props = defineProps<{ modelValue: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', val: string): void }>()

const editorBox = ref<HTMLDivElement>()
const toolbarBox = ref<HTMLDivElement>()
let editor: any = null

onMounted(() => {
  if (!editorBox.value) return

  const editorConfig = {
    placeholder: '请输入商品描述...',
    onChange(ed: any) {
      emit('update:modelValue', ed.getHtml())
    },
  }

  editor = createEditor({
    selector: editorBox.value,
    html: props.modelValue || '',
    config: editorConfig,
    mode: 'default',
  })

  if (toolbarBox.value) {
    createToolbar({
      editor,
      selector: toolbarBox.value,
      config: {
        excludeKeys: ['group-video'],
      },
      mode: 'default',
    })
  }
})

watch(() => props.modelValue, (val) => {
  if (editor && val !== editor.getHtml()) {
    editor.setHtml(val)
  }
})

onBeforeUnmount(() => {
  if (editor) {
    editor.destroy()
    editor = null
  }
})
</script>

<template>
  <div style="border:1px solid #e8e8e8;border-radius:4px">
    <div ref="toolbarBox" style="border-bottom:1px solid #e8e8e8"></div>
    <div ref="editorBox" style="height:300px;overflow-y:auto"></div>
  </div>
</template>
