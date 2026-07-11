<script setup lang="ts">
import { NLayoutSider } from 'naive-ui'
import type { MenuOption } from 'naive-ui'

defineProps<{
  collapsed: boolean
  isMobile: boolean
  mobileOverlay: boolean
  menuOptions: MenuOption[]
  collapsedGroups: Set<string>
  currentKey: string
}>()

defineEmits<{
  'toggle-group': [key: string]
  navigate: [key: string]
}>()
</script>

<template>
  <n-layout-sider
    bordered :collapsed="isMobile ? false : collapsed" collapse-mode="width" :width="220"
    :style="isMobile ? { position: 'fixed', left: mobileOverlay ? '0' : '-220px', top: 0, bottom: 0, zIndex: 100, transition: 'left 0.3s' } : {}"
  >
    <div class="logo">
      <span v-if="!collapsed" class="logo-text">YXCLOUD</span>
      <span v-else class="logo-text-collapsed">YC</span>
      <span v-if="!collapsed" class="logo-version">管理后台 v2.4</span>
    </div>
    <div class="custom-menu menu-scroll" :class="{ collapsed }">
      <template v-for="item in menuOptions" :key="item.key">
        <template v-if="item.children">
          <div v-show="!collapsed" class="menu-group-title menu-group-header" @click="$emit('toggle-group', String(item.key))">
            <span>{{ item.label }}</span>
            <span style="font-size:10px;transition:transform .2s" :style="{transform: collapsedGroups.has(String(item.key))?'rotate(-90deg)':'rotate(0)'}">▼</span>
          </div>
          <div v-show="!collapsed && !collapsedGroups.has(String(item.key))">
            <div
              v-for="child in item.children"
              :key="child.key"
              :class="['menu-item', { active: currentKey === child.key }]"
              @click="$emit('navigate', String(child.key))"
            >
              <span v-if="child.icon" class="menu-icon"><component :is="child.icon" /></span>
              <span v-show="!collapsed" class="menu-label">{{ child.label }}</span>
            </div>
          </div>
        </template>
      </template>
    </div>
  </n-layout-sider>
</template>

<style scoped>
.logo {
  height: 56px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff; font-size: 18px; font-weight: 600; letter-spacing: 1px;
  user-select: none;
}
.logo-text { font-weight: 800; letter-spacing: 1px; background: linear-gradient(135deg, #f10215, #ff6b6b); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.logo-text-collapsed { font-size: 16px; font-weight: 800; color: #f10215; }
.logo-version { font-size: 10px; color: #999; display: block; margin-top: 2px; }
.custom-menu { padding: 8px 0; overflow-x: hidden; }
.custom-menu.collapsed .menu-item { padding: 10px; justify-content: center; }
.custom-menu.collapsed .menu-icon { font-size: 20px; }
.menu-group-title {
  padding: 8px 20px; font-size: 11px; font-weight: 600;
  color: var(--n-text-color-3, #999); white-space: nowrap;
}
.menu-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 24px; font-size: 14px; cursor: pointer;
  color: var(--n-text-color, #333); white-space: nowrap;
  transition: all 0.15s ease; border-left: 3px solid transparent;
}
.menu-item:hover { background: rgba(102,126,234,0.08); }
.menu-item.active {
  color: #667eea; background: rgba(102,126,234,0.12);
  border-left-color: #667eea; font-weight: 500;
}
.menu-icon { font-size: 18px; display: flex; align-items: center; flex-shrink: 0; }
.menu-label { flex: 1; overflow: hidden; text-overflow: ellipsis; }
.menu-scroll { overflow-y: auto; max-height: calc(100vh - 110px); padding-right: 4px; }
.menu-group-header { cursor: pointer; display: flex; justify-content: space-between; align-items: center; }
@media (max-width: 768px) {
  .logo { font-size: 14px; height: 44px; }
}
</style>
