<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NSpace, NTag, NPopconfirm, NSelect } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { fetchTickets, createTicket, updateTicket, deleteTicket, assignTicket, updateTicketStatus, type SysTicket } from '@/api/ticket'
import { useNotify } from '@/composables/useNotify'

const { t } = useI18n()
const notify = useNotify()

const loading = ref(false)
const saving = ref(false)
const items = ref<SysTicket[]>([])
const showModal = ref(false)
const showAssignModal = ref(false)
const showStatusModal = ref(false)
const editingId = ref<number | null>(null)
const form = ref({ title: '', content: '', type: 'other', priority: 'medium' })
const searchKeyword = ref('')
const assignee = ref('')
const newStatus = ref('')
const assignTarget = ref<SysTicket | null>(null)
const statusTarget = ref<SysTicket | null>(null)

const typeOptions = [
  { label: t('ticket.types.fault'), value: 'fault' },
  { label: t('ticket.types.request'), value: 'request' },
  { label: t('ticket.types.consult'), value: 'consult' },
  { label: t('ticket.types.other'), value: 'other' },
]

const priorityOptions = [
  { label: t('ticket.priorities.low'), value: 'low' },
  { label: t('ticket.priorities.medium'), value: 'medium' },
  { label: t('ticket.priorities.high'), value: 'high' },
  { label: t('ticket.priorities.emergency'), value: 'emergency' },
]

const statusOptions = [
  { label: t('ticket.statuses.pending'), value: '0' },
  { label: t('ticket.statuses.processing'), value: '1' },
  { label: t('ticket.statuses.completed'), value: '2' },
  { label: t('ticket.statuses.closed'), value: '3' },
]

const priorityColor: Record<string, string> = { low: 'default', medium: 'info', high: 'warning', emergency: 'error' }
const statusColor: Record<string, string> = { '0': 'warning', '1': 'info', '2': 'success', '3': 'default' }
const typeLabel: Record<string, string> = { fault: t('ticket.types.fault'), request: t('ticket.types.request'), consult: t('ticket.types.consult'), other: t('ticket.types.other') }

const filteredItems = computed(() => {
  if (!searchKeyword.value) return items.value
  const kw = searchKeyword.value.toLowerCase()
  return items.value.filter(t => t.title.toLowerCase().includes(kw) || t.ticketNo.toLowerCase().includes(kw))
})

function getStatusLabel(s: string) {
  const map: Record<string, string> = { '0': 'pending', '1': 'processing', '2': 'completed', '3': 'closed' }
  return `ticket.statuses.${map[s] || 'pending'}`
}

const columns: DataTableColumns<SysTicket> = [
  { title: t('ticket.ticketNo'), key: 'ticketNo', width: 180 },
  { title: t('ticket.title'), key: 'title', width: 200, ellipsis: { tooltip: true } },
  {
    title: t('ticket.type'), key: 'type', width: 80,
    render(row: SysTicket) { return h(NTag, { size: 'small' }, { default: () => typeLabel[row.type] || row.type }) },
  },
  {
    title: t('ticket.priority'), key: 'priority', width: 80,
    render(row: SysTicket) { return h(NTag, { size: 'small', type: priorityColor[row.priority] as any }, { default: () => t(`ticket.priorities.${row.priority}`) }) },
  },
  {
    title: t('ticket.status'), key: 'status', width: 90,
    render(row: SysTicket) { return h(NTag, { size: 'small', type: statusColor[row.status] as any }, { default: () => t(getStatusLabel(row.status)) }) },
  },
  { title: t('ticket.applicant'), key: 'applicant', width: 100 },
  { title: t('ticket.assignee'), key: 'assignee', width: 100 },
  { title: t('common.createdAt'), key: 'createdAt', width: 160, render(row: SysTicket) { return row.createdAt?.substring(0, 16) } },
  {
    title: t('ticket.actions'), key: 'actions', width: 260,
    render(row: SysTicket) {
      return h(NSpace, { size: 'small' }, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => editTicket(row) }, { default: () => t('common.edit') }),
          h(NButton, { size: 'small', onClick: () => openAssign(row) }, { default: () => t('ticket.assign') }),
          h(NButton, { size: 'small', onClick: () => openStatus(row) }, { default: () => t('ticket.updateStatus') }),
          h(NPopconfirm, { onPositiveClick: () => delTicket(row.id) }, {
            trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => t('common.delete') }),
            default: () => t('common.confirmDelete'),
          }),
        ],
      })
    },
  },
]

async function load() {
  loading.value = true
  try {
    const res = await fetchTickets()
    items.value = res.data || []
  } finally { loading.value = false }
}

function addTicket() {
  editingId.value = null
  form.value = { title: '', content: '', type: 'other', priority: 'medium' }
  showModal.value = true
}

function editTicket(row: SysTicket) {
  editingId.value = row.id
  form.value = { title: row.title, content: row.content || '', type: row.type, priority: row.priority }
  showModal.value = true
}

async function saveTicket() {
  saving.value = true
  try {
    if (editingId.value) {
      await updateTicket(editingId.value, form.value)
      notify.success(t('ticket.updateSuccess'))
    } else {
      await createTicket(form.value as any)
      notify.success(t('ticket.createSuccess'))
    }
    showModal.value = false
    load()
  } catch { notify.error(t('common.saveFailed')) } finally { saving.value = false }
}

async function delTicket(id: number) {
  try { await deleteTicket(id); notify.success(t('common.deleted')); load() } catch { notify.error(t('common.saveFailed')) }
}

function openAssign(row: SysTicket) {
  assignTarget.value = row
  assignee.value = row.assignee || ''
  showAssignModal.value = true
}

const submitting = ref(false)
async function doAssign() {
  if (!assignTarget.value) return
  submitting.value = true
  try { await assignTicket(assignTarget.value.id, assignee.value); showAssignModal.value = false; notify.success(t('ticket.updateSuccess')); load() }
  catch { notify.error(t('common.saveFailed')) } finally { submitting.value = false }
}

function openStatus(row: SysTicket) {
  statusTarget.value = row
  newStatus.value = row.status
  showStatusModal.value = true
}

async function doStatus() {
  if (!statusTarget.value) return
  submitting.value = true
  try { await updateTicketStatus(statusTarget.value.id, newStatus.value); showStatusModal.value = false; notify.success(t('ticket.updateSuccess')); load() }
  catch { notify.error(t('common.saveFailed')) } finally { submitting.value = false }
}

const checkedRowKeys = ref<number[]>([])
async function batchDelete() { if(!checkedRowKeys.value.length)return; try{for(const id of checkedRowKeys.value)await deleteTicket(id);checkedRowKeys.value=[];load();notify.success(t('common.deleted'))}catch{notify.error(t('common.saveFailed'))} }
onMounted(load)
</script>

<template>
  <n-card :title="t('nav.tickets')">
    <n-space vertical>
      <n-space justify="space-between">
        <n-input v-model:value="searchKeyword" :placeholder="t('ticket.searchPlaceholder')" clearable class="w-240" />
        <n-space><n-button v-if="checkedRowKeys.length" type="error" size="small" @click="batchDelete">{{ t('common.batchDelete') }} ({{ checkedRowKeys.length }})</n-button><n-button type="primary" @click="addTicket">{{ t('ticket.add') }}</n-button></n-space>
      </n-space>
      <n-dataTable :columns="columns" :data="filteredItems" :loading="loading" :row-key="(r: SysTicket) => r.id" v-model:checked-row-keys="checkedRowKeys" :pagination="{ pageSize: 10 }" />
    </n-space>

    <n-drawer v-model:show="showModal" :width="460" placement="right">
      <n-drawer-content :title="editingId ? t('ticket.edit') : t('ticket.add')" closable>
        <template #footer><n-space justify="end"><n-button @click="showModal = false">Cancel</n-button><n-button type="primary" :loading="saving" @click="saveTicket">Save</n-button></n-space></template>
        <n-form :model="form" label-placement="left" label-width="80" size="small">
          <n-form-item :label="t('ticket.title')" path="title"><n-input v-model:value="form.title" /></n-form-item>
          <n-form-item :label="t('ticket.content')"><n-input v-model:value="form.content" type="textarea" :autosize="{ minRows: 3, maxRows: 8 }" /></n-form-item>
          <n-form-item :label="t('ticket.type')"><n-select v-model:value="form.type" :options="typeOptions" /></n-form-item>
          <n-form-item :label="t('ticket.priority')"><n-select v-model:value="form.priority" :options="priorityOptions" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
    <n-drawer v-model:show="showAssignModal" :width="380" placement="right">
      <n-drawer-content :title="t('ticket.assign')" closable>
        <template #footer><n-space justify="end"><n-button @click="showAssignModal = false">Cancel</n-button><n-button type="primary" :loading="submitting" @click="doAssign">Confirm</n-button></n-space></template>
        <n-form label-placement="left" label-width="80" size="small"><n-form-item :label="t('ticket.assignee')"><n-input v-model:value="assignee" :placeholder="t('ticket.assignPlaceholder')" /></n-form-item></n-form>
      </n-drawer-content>
    </n-drawer>
    <n-drawer v-model:show="showStatusModal" :width="360" placement="right">
      <n-drawer-content :title="t('ticket.updateStatus')" closable>
        <template #footer><n-space justify="end"><n-button @click="showStatusModal = false">Cancel</n-button><n-button type="primary" :loading="submitting" @click="doStatus">Confirm</n-button></n-space></template>
        <n-form label-placement="left" label-width="80" size="small"><n-form-item :label="t('ticket.status')"><n-select v-model:value="newStatus" :options="statusOptions" /></n-form-item></n-form>
      </n-drawer-content>
    </n-drawer>
  </n-card>
</template>

<style scoped>
.w-240 { width: 240px; }
</style>
