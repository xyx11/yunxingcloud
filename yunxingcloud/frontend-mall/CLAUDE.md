# YXCLOUD 商城 (frontend-mall)

京东风格全品类电商前端，Vue 3 + TypeScript + Vite + Pinia。

## 技术栈

| 层 | 选型 |
|---|------|
| 框架 | Vue 3.4 Composition API + `<script setup>` |
| 语言 | TypeScript 5.4 (strict mode) |
| 构建 | Vite 8 |
| 状态 | Pinia 2 |
| 路由 | Vue Router 4 |
| HTTP | Axios (封装在 `src/api/request.ts`) |
| 样式 | CSS Variables + scoped styles，无 UI 框架 |
| 测试 | Vitest 97 + Playwright 5 E2E |
| PWA | 自维护 Service Worker + 离线预缓存 |

## 目录结构

```
src/
├── api/          # 18 个 API 模块 + request.ts (dedup/retry/401)
├── components/   # 24 个共享组件 (Jd* 基础 + Product* 业务)
├── composables/  # 10 个 composable
├── config/       # A/B 实验配置 (abTests.ts)
├── locales/      # zh.ts / en.ts (1216 行完全对齐)
├── router/       # 路由守卫 + SEO 动态 meta + KeepAlive 滚动恢复
├── stores/       # auth / cart / theme (Pinia)
├── styles/       # reset / tokens(125行+暗色) / utilities / breakpoints
├── types/        # 35+ 接口定义 (types/index.ts)
├── utils/        # format / logger / regionData (44KB 懒加载)
└── views/        # 40 个视图
```

## 设计系统

- **颜色**: 120+ CSS 变量，支持暗色主题切换
- **暗色模式**: `data-theme="dark"` + 自动跟随 `prefers-color-scheme`
- **动效**: `prefers-reduced-motion` 尊重，GPU `will-change` 加速
- **触摸**: 44px 最小触摸目标，`:active` 缩放反馈
- **无障碍**: `:focus-visible` 焦点环，`aria-live` 公告，`skip-link`
- **滚动条**: WebKit 自定义 6px 圆角

## 架构约定

- **API 层**: 视图通过 `src/api/*.ts` 调用后端；一次性端点可用 raw request
- **类型**: 统一定义在 `types/index.ts`，视图用 `import type`
- **i18n**: 所有 UI 文案通过 `t('key')`
- **错误**: 全局 errorHandler + 突发限流(10s/5条) + sendBeacon 上报
- **模态框**: 统一 `JdModal` / `ConfirmDialog`
- **Toast**: 统一 `useToast()`；移动端底部居中
- **加载状态**: 必须有三态 — loading/error/empty

## 常用命令

```bash
npm run dev          # :5174
npm run build        # 生产构建 ~340ms
npm run type-check   # TS 检查 (0 errors)
npm test             # 97 单测
npm run test:e2e     # Playwright (需先 dev)
npm run lint         # ESLint (0/0)
```

## 状态

- TS strict: 0 errors
- ESLint: 0 errors, 0 warnings
- any (视图层): 0
- 测试: 97 passed, 9 files
- 构建: ~340ms, 31 chunks
- API 模块: 18/18 全引用
- 源码: ~18,876 行