<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { listDictTypes, createDictType, updateDictType, deleteDictType, listDictData, createDictData, updateDictData, deleteDictData, type DictType, type DictData } from '@/api/dict'
import { useNotify } from '@/composables/useNotify'

import {
  NCard, NDataTable, NButton, NDrawer, NDrawerContent, NForm, NFormItem,
  NInput, NSelect, NSpace, NPopconfirm, NTag, NGrid, NGridItem,
  NInputNumber, NEmpty,
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import type { DataTableColumns, FormRules, FormInst } from 'naive-ui'

const { t } = useI18n()
const notify = useNotify()

const types = ref<DictType[]>([])
const dataList = ref<DictData[]>([])
const loading = ref(false)
const saving = ref(false)
const showTypeModal = ref(false)
const showDataModal = ref(false)
const editingType = ref<DictType | null>(null)
const editingData = ref<DictData | null>(null)
const selectedType = ref<DictType | null>(null)
const typeFormRef = ref<FormInst>()
const typeRules: FormRules = {
  dictName: [{ required: true, message: t('validate.required'), trigger: 'blur' }],
  dictType: [{ required: true, message: t('validate.required'), trigger: 'blur' }],
}
const typeForm = ref({ dictName: '', dictType: '', status: '0', remark: '' })
const dataFormRef = ref<FormInst>()
const dataRules: FormRules = {
  dictLabel: [{ required: true, message: t('validate.required'), trigger: 'blur' }],
  dictValue: [{ required: true, message: t('validate.required'), trigger: 'blur' }],
}
const dataForm = ref({ dictType: '', dictLabel: '', dictValue: '', cssClass: '', listClass: '', isDefault: 'N', sortOrder: 0, status: '0', remark: '' })
const typeSearch = ref("")
const dataSearch = ref("")

const statusOptions = [
  { label: t('user.enabledLabel'), value: '0' },
  { label: t('user.disabledLabel'), value: '1' },
]

const filteredTypes = computed(() => {
  const kw = typeSearch.value.toLowerCase()
  if (!kw) return types.value
  return types.value.filter(t => t.dictName.toLowerCase().includes(kw) || t.dictType.toLowerCase().includes(kw))
})

const filteredData = computed(() => {
  const kw = dataSearch.value.toLowerCase()
  if (!kw) return dataList.value
  return dataList.value.filter(d => d.dictLabel.toLowerCase().includes(kw) || d.dictValue.toLowerCase().includes(kw))
})

const typeColumns: DataTableColumns<DictType> = [
  { title: 'ID', key: 'id', width: 50 },
  { title: t('dict.dictName'), key: 'dictName', width: 130 },
  { title: t('dict.dictType'), key: 'dictType', width: 140 },
  {
    title: t('dict.status'), key: 'status', width: 60,
    render: (row) => h(NTag, { type: row.status === '0' ? 'success' : 'default', size: 'small' },
      { default: () => row.status === '0' ? t('user.enabledLabel') : t('user.disabledLabel') })
  },
  {
    title: t('dict.actions'), key: 'actions', width: 100,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'tiny', onClick: () => selectType(row) }, { default: () => t('common.view') }),
        h(NButton, { size: 'tiny', onClick: () => editType(row) }, { default: () => t('common.edit') }),
        h(NPopconfirm, { onPositiveClick: () => delType(row.id) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }),
          default: () => t('common.confirmDelete')
        })
      ]
    })
  },
]

const dataColumns: DataTableColumns<DictData> = [
  { title: 'ID', key: 'id', width: 50 },
  { title: t('dict.dictLabel'), key: 'dictLabel', width: 110 },
  { title: t('dict.dictValue'), key: 'dictValue', width: 100 },
  { title: t('dict.sortOrder'), key: 'sortOrder', width: 55 },
  {
    title: t('dict.isDefault'), key: 'isDefault', width: 55,
    render: (row) => h(NTag, { type: row.isDefault === 'Y' ? 'info' : 'default', size: 'small' },
      { default: () => row.isDefault === 'Y' ? t('common.yes') : t('common.no') })
  },
  {
    title: t('dict.status'), key: 'status', width: 55,
    render: (row) => h(NTag, { type: row.status === '0' ? 'success' : 'default', size: 'small' },
      { default: () => row.status === '0' ? t('user.enabledLabel') : t('user.disabledLabel') })
  },
  {
    title: t('dict.actions'), key: 'actions', width: 100,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'tiny', onClick: () => editData(row) }, { default: () => t('common.edit') }),
        h(NPopconfirm, { onPositiveClick: () => delData(row.id) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'error' }, { default: () => t('common.delete') }),
          default: () => t('common.confirmDelete')
        })
      ]
    })
  },
]

async function loadTypes() {
  loading.value = true
  try { const res = await listDictTypes(); types.value = res.data } catch { notify.error(t('common.error')); }
  loading.value = false
}

async function selectType(type: DictType) {
  selectedType.value = type
  try {
    const res = await listDictData(type.dictType)
    dataList.value = res.data
  } catch { notify.error(t('common.error')); }
}

function addType() {
  editingType.value = null
  typeForm.value = { dictName: '', dictType: '', status: '0', remark: '' }
  showTypeModal.value = true
}

function editType(type: DictType) {
  editingType.value = type
  typeForm.value = { dictName: type.dictName, dictType: type.dictType, status: type.status || '0', remark: type.remark || '' }
  showTypeModal.value = true
}

async function saveType() {
  if (typeFormRef.value) { try { await typeFormRef.value.validate() } catch { return } }
  saving.value = true
  try {
    if (editingType.value) await updateDictType(editingType.value.id, typeForm.value)
    else await createDictType(typeForm.value)
    showTypeModal.value = false
    notify.success(editingType.value ? t('dict.updateSuccess') : t('dict.createSuccess'))
    await loadTypes()
  } catch (e: any) { notify.error(e.response?.data?.message || t('common.saveFailed')) } finally { saving.value = false }
}

async function delType(id: number) {
  try { await deleteDictType(id); if (selectedType.value?.id === id) { selectedType.value = null; dataList.value = [] }; await loadTypes() } catch { notify.error(t('common.saveFailed')) }
}

function addData() {
  if (!selectedType.value) { notify.warning(t('dict.selectTypeHint')); return }
  editingData.value = null
  dataForm.value = { dictType: selectedType.value.dictType, dictLabel: '', dictValue: '', cssClass: '', listClass: '', isDefault: 'N', sortOrder: 0, status: '0', remark: '' }
  showDataModal.value = true
}

function editData(data: DictData) {
  editingData.value = data
  dataForm.value = { dictType: data.dictType, dictLabel: data.dictLabel, dictValue: data.dictValue, cssClass: data.cssClass || '', listClass: data.listClass || '', isDefault: data.isDefault || 'N', sortOrder: data.sortOrder || 0, status: data.status || '0', remark: data.remark || '' }
  showDataModal.value = true
}

async function saveData() {
  if (dataFormRef.value) { try { await dataFormRef.value.validate() } catch { return } }
  saving.value = true
  try {
    if (editingData.value) await updateDictData(editingData.value.id, dataForm.value)
    else await createDictData(dataForm.value)
    showDataModal.value = false
    notify.success(editingData.value ? t('dict.updateSuccess') : t('dict.createSuccess'))
    if (selectedType.value) await selectType(selectedType.value)
  } catch (e: any) { notify.error(e.response?.data?.message || t('common.saveFailed')) } finally { saving.value = false }
}

async function delData(id: number) {
  try { await deleteDictData(id); if (selectedType.value) await selectType(selectedType.value) } catch { notify.error(t('common.saveFailed')) }
}

const checkedTypeKeys = ref<number[]>([])
const checkedDataKeys = ref<number[]>([])
async function batchDeleteTypes() { if(!checkedTypeKeys.value.length)return; try{for(const id of checkedTypeKeys.value)await deleteDictType(id);checkedTypeKeys.value=[];selectedType.value=null;dataList.value=[];loadTypes();notify.success(t('common.deleted'))}catch{notify.error(t('common.saveFailed'))} }
async function batchDeleteData() { if(!checkedDataKeys.value.length)return; try{for(const id of checkedDataKeys.value)await deleteDictData(id);checkedDataKeys.value=[];if(selectedType.value)await selectType(selectedType.value);notify.success(t('common.deleted'))}catch{notify.error(t('common.saveFailed'))} }
onMounted(loadTypes)
</script>

<template>
  <div style="padding:20px">
    <n-grid :cols="2" :x-gap="12">
      <n-grid-item>
        <n-card :title="t('dict.type')" size="small">
          <template #header-extra>
            <n-space><n-button v-if="checkedTypeKeys.length" type="error" size="small" @click="batchDeleteTypes">{{ t('common.batchDelete') }} ({{checkedTypeKeys.length}})</n-button><n-button type="primary" size="small" @click="addType"><template #icon>＋</template>{{ t('common.add') }}</n-button></n-space>
          </template>
          <n-space style="margin-bottom:8px">
            <n-input v-model:value="typeSearch" :placeholder="t('dict.searchType')" size="small" clearable style="max-width:140px;width:95%" />
            <n-button size="small" @click="loadTypes" secondary>{{ t('common.refresh') }}</n-button>
          </n-space>
          <n-dataTable
            :columns="typeColumns" :data="filteredTypes" :loading="loading" size="small"
            :bordered="false" :pagination="{ pageSize: 10 }"
            :row-key="(row: DictType) => row.id" v-model:checked-row-keys="checkedTypeKeys"
            :row-props="(row: DictType) => ({ style: selectedType?.id === row.id ? 'background:var(--primary-color-suppl, #e8f0fe)' : '' })"
          />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card :title="selectedType ? `${t('dict.data')} - ${selectedType.dictName}` : t('dict.data')" size="small">
          <template #header-extra>
            <n-space><n-button v-if="checkedDataKeys.length" type="error" size="small" @click="batchDeleteData">{{ t('common.batchDelete') }} ({{checkedDataKeys.length}})</n-button><n-button type="primary" size="small" @click="addData"><template #icon>＋</template>{{ t('common.add') }}</n-button></n-space>
          </template>
          <n-space style="margin-bottom:8px">
            <n-input v-model:value="dataSearch" :placeholder="t('dict.searchData')" size="small" clearable style="max-width:140px;width:95%" />
            <n-button size="small" @click="selectedType && selectType(selectedType)" secondary>{{ t('common.refresh') }}</n-button>
          </n-space>
          <n-dataTable
            :columns="dataColumns" :data="filteredData" size="small"
            :bordered="false" :pagination="{ pageSize: 10 }"
            :row-key="(row: DictData) => row.id" v-model:checked-row-keys="checkedDataKeys"
          />
          <n-empty v-if="!selectedType" :description="t('dict.selectTypeHint')" style="margin-top:40px" />
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-drawer v-model:show="showTypeModal" :width="420" placement="right">
      <n-drawer-content :title="editingType ? t('dict.editType') : t('dict.addType')" closable>
        <template #footer><n-space justify="end"><n-button @click="showTypeModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="saveType">{{ t('common.save') }}</n-button></n-space></template>
        <n-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-placement="left" label-width="80" size="small">
          <n-form-item path="dictName" :label="t('dict.dictName')"><n-input v-model:value="typeForm.dictName" /></n-form-item>
          <n-form-item path="dictType" :label="t('dict.dictType')"><n-input v-model:value="typeForm.dictType" :disabled="!!editingType" :placeholder="t('dict.typePlaceholder')" /></n-form-item>
          <n-form-item :label="t('dict.status')"><n-select v-model:value="typeForm.status" :options="statusOptions" /></n-form-item>
          <n-form-item :label="t('common.remark')"><n-input v-model:value="typeForm.remark" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>

    <n-drawer v-model:show="showDataModal" :width="440" placement="right">
      <n-drawer-content :title="editingData ? t('dict.editData') : t('dict.addData')" closable>
        <template #footer><n-space justify="end"><n-button @click="showDataModal = false">{{ t('common.cancel') }}</n-button><n-button type="primary" :loading="saving" @click="saveData">{{ t('common.save') }}</n-button></n-space></template>
        <n-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-placement="left" label-width="90" size="small">
          <n-form-item :label="t('dict.dictType')"><n-input :value="dataForm.dictType" disabled /></n-form-item>
          <n-form-item path="dictLabel" :label="t('dict.dictLabel')"><n-input v-model:value="dataForm.dictLabel" :placeholder="t('dict.labelPlaceholder')" /></n-form-item>
          <n-form-item path="dictValue" :label="t('dict.dictValue')"><n-input v-model:value="dataForm.dictValue" :placeholder="t('dict.valuePlaceholder')" /></n-form-item>
          <n-form-item :label="t('dict.sortOrder')"><n-input-number v-model:value="dataForm.sortOrder" :min="0" style="width:100%" /></n-form-item>
          <n-form-item :label="t('dict.isDefaultLabel')"><n-select v-model:value="dataForm.isDefault" :options="[{label:t('common.yes'),value:'Y'},{label:t('common.no'),value:'N'}]" /></n-form-item>
          <n-form-item :label="t('dict.status')"><n-select v-model:value="dataForm.status" :options="statusOptions" /></n-form-item>
        </n-form>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>
