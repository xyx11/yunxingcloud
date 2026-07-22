import type { AbExperiment } from '@/composables/useAbTest'

export const abExperiments: AbExperiment[] = [
  {
    id: 'home-layout',
    name: '首页布局实验',
    description: '测试不同的首页商品推荐布局对点击率的影响',
    enabled: true,
    variants: [
      {
        name: 'control',
        weight: 50,
        config: { columns: 4, showRanking: true, layout: 'grid' },
      },
      {
        name: 'variant-a',
        weight: 25,
        config: { columns: 5, showRanking: true, layout: 'grid' },
      },
      {
        name: 'variant-b',
        weight: 25,
        config: { columns: 3, showRanking: false, layout: 'large-card' },
      },
    ],
  },
  {
    id: 'product-card-style',
    name: '商品卡片样式实验',
    description: '测试不同卡片样式对加购率的影响',
    enabled: true,
    variants: [
      {
        name: 'control',
        weight: 60,
        config: { showAddCart: true, showRating: true, layout: 'grid' },
      },
      {
        name: 'compact',
        weight: 40,
        config: { showAddCart: false, showRating: false, layout: 'grid' },
      },
    ],
  },
  {
    id: 'checkout-button-text',
    name: '结算按钮文案实验',
    description: '测试不同按钮文案对下单转化率的影响',
    enabled: false,
    variants: [
      { name: 'control', weight: 50, config: { text: '提交订单' } },
      { name: 'urgent', weight: 25, config: { text: '立即下单，预计{date}送达' } },
      { name: 'secure', weight: 25, config: { text: '安全结算 · 售后无忧' } },
    ],
  },
  {
    id: 'search-sort-default',
    name: '搜索默认排序实验',
    description: '测试默认排序方式对搜索结果点击率的影响',
    enabled: true,
    variants: [
      { name: 'control', weight: 50, config: { defaultSort: '' } },
      { name: 'sales', weight: 50, config: { defaultSort: 'sales' } },
    ],
  },
]

export function getExperimentById(id: string): AbExperiment | undefined {
  return abExperiments.find(e => e.id === id)
}

export function getEnabledExperiments(): AbExperiment[] {
  return abExperiments.filter(e => e.enabled)
}
