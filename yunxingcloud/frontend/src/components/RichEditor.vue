<script setup lang="ts">
import { onMounted, onBeforeUnmount, watch, ref, nextTick } from 'vue'
import { createEditor, createToolbar, type IDomEditor } from '@wangeditor/editor'
import '@wangeditor/editor/dist/css/style.css'

const props = defineProps<{ modelValue: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', val: string): void }>()

const editorBox = ref<HTMLDivElement>()
const toolbarBox = ref<HTMLDivElement>()
let editor: IDomEditor | null = null

onMounted(() => {
  if (!editorBox.value) return

  const editorConfig = {
    placeholder: '请输入商品描述...',
    onChange(ed: IDomEditor) {
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
  <div class="editor-wrapper">
    <div ref="toolbarBox" class="editor-toolbar"></div>
    <div ref="editorBox" class="editor-content"></div>
  </div>
</template>

<style scoped>
.re-wrap { display: flex; flex-direction: column; gap: 8px; }
.re-editor { border: 1px solid #e5e7eb; border-radius: 4px; overflow: hidden; z-index: 1; }
.re-content { height: 300px; }
.editor-wrapper { border: 1px solid #e8e8e8; border-radius: 4px; }
.editor-toolbar { border-bottom: 1px solid #e8e8e8; }
.editor-content { height: 300px; overflow-y: auto; }
</style>
