<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategories, getBrands, getProducts } from '@/api/product'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { useCompare } from '@/composables/useCompare'
import { useCartFly } from '@/composables/useCartFly'
import QuickViewModal from '@/components/QuickViewModal.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'

const route = useRoute()
const { toggle: toggleCompare, isSelected } = useCompare()
const quickViewProduct = ref<any>(null)
function openQuickView(e: Event, p: any) { e.stopPropagation(); quickViewProduct.value = p }
const router = useRouter()
const toast = useToast()
const products = ref<any[]>([])
const totalPages = ref(0)
const currentPage = ref(0)
const pageSize = 20
const categories = ref<any[]>([])
const brands = ref<any[]>([])
const loading = ref(false)

const filters = ref({
  categoryId: '',
  brandId: '',
  minPrice: '',
  maxPrice: '',
  sort: '',
})

onMounted(async () => {
  try { const r = await getCategories(); categories.value = r.data || [] } catch {}
  try { const r = await getBrands(); brands.value = r.data || [] } catch {}
  filters.value.categoryId = (route.query.categoryId as string) || ''
  loadProducts()
  window.addEventListener('scroll', onScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})

watch(() => route.query, (q) => {
  filters.value.categoryId = (q.categoryId as string) || ''
  filters.value.brandId = (q.brandId as string) || ''
  currentPage.value = 0
  loadProducts()
})

const loadingMore = ref(false)
function onScroll() {
  if (loadingMore.value || currentPage.value >= totalPages.value - 1) return
  const h = document.documentElement; if (h.scrollTop + h.clientHeight >= h.scrollHeight - 300) { currentPage.value++; loadMore() }
}
async function loadProducts() {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, size: pageSize }
    if (filters.value.sort) params.sort = filters.value.sort
    if (filters.value.categoryId) params.categoryId = filters.value.categoryId
    if (filters.value.brandId) params.brandId = filters.value.brandId
    if (filters.value.minPrice) params.minPrice = Number(filters.value.minPrice) * 100
    if (filters.value.maxPrice) params.maxPrice = Number(filters.value.maxPrice) * 100
    const r = await getProducts(params)
    products.value = r.data.content || r.data || []
    totalPages.value = r.data.totalPages || 0
  } catch { products.value = [] } finally { loading.value = false }
}

function applyFilters() { currentPage.value = 0; const q: any = {}; if (filters.value.categoryId) q.categoryId = filters.value.categoryId; if (filters.value.brandId) q.brandId = filters.value.brandId; router.push({ query: q }); loadProducts() }
function clearFilters() { filters.value = { categoryId: '', brandId: '', minPrice: '', maxPrice: '', sort: '' }; router.push({ query: {} }); loadProducts() }
function setSort(s: string) { filters.value.sort = filters.value.sort === s ? '' : s; currentPage.value = 0; products.value = []; loadProducts() }
async function loadMore() {
  if (loadingMore.value) return; loadingMore.value = true
  try { const params: any = { page: currentPage.value, size: pageSize }; if (filters.value.sort) params.sort = filters.value.sort; if (filters.value.categoryId) params.categoryId = filters.value.categoryId; if (filters.value.brandId) params.brandId = filters.value.brandId; if (filters.value.minPrice) params.minPrice = Number(filters.value.minPrice) * 100; if (filters.value.maxPrice) params.maxPrice = Number(filters.value.maxPrice) * 100; const r = await getProducts(params); const data = r.data; products.value = [...products.value, ...(data.content || data || [])]; totalPages.value = data.totalPages || 0 } catch {} finally { loadingMore.value = false }
}
function goDetail(id: number) { router.push(`/product/${id}`) }
function goPage(p: number) { currentPage.value = p; loadProducts(); window.scrollTo(0, 0) }
const { flyToCart } = useCartFly()
async function quickAdd(e: Event, p: any) { e.stopPropagation(); try { await addToCart({ productId: p.id, quantity: 1 }); toast.success('已加入购物车'); p._added = true; setTimeout(() => p._added = false, 1500); flyToCart(e as MouseEvent) } catch { toast.error('添加失败') } }
</script>

<template>
  <div style="display:flex;gap:24px">
    <aside style="width:220px;flex-shrink:0">
      <div style="background:#fff;border-radius:8px;padding:16px;margin-bottom:16px;box-shadow:0 1px 4px rgba(0,0,0,.06)">
        <h4 style="font-size:14px;font-weight:700;margin-bottom:12px">商品分类</h4>
        <div v-for="cat in categories" :key="cat.id" style="margin-bottom:4px">
          <span @click="filters.categoryId = cat.id+''; applyFilters()"
                style="display:block;padding:6px 8px;cursor:pointer;font-size:13px;border-radius:4px;transition:all .2s"
                :style="{background:filters.categoryId===cat.id+''?'#fff0f0':'',color:filters.categoryId===cat.id+''?'#e4393c':'#666'}">{{ cat.name }}</span>
        </div>
      </div>
      <div style="background:#fff;border-radius:8px;padding:16px;box-shadow:0 1px 4px rgba(0,0,0,.06)">
        <h4 style="font-size:14px;font-weight:700;margin-bottom:12px">品牌</h4>
        <div v-for="b in brands" :key="b.id" style="margin-bottom:4px">
          <span @click="filters.brandId = b.id+''; applyFilters()"
                style="display:block;padding:6px 8px;cursor:pointer;font-size:13px;border-radius:4px;transition:all .2s"
                :style="{background:filters.brandId===b.id+''?'#fff0f0':'',color:filters.brandId===b.id+''?'#e4393c':'#666'}">{{ b.name }}</span>
        </div>
      </div>
      <div style="background:#fff;border-radius:8px;padding:16px;margin-top:16px;box-shadow:0 1px 4px rgba(0,0,0,.06)">
        <h4 style="font-size:14px;font-weight:700;margin-bottom:12px">价格区间</h4>
        <div style="display:flex;align-items:center;gap:6px">
          <input v-model="filters.minPrice" placeholder="¥最低" type="number" style="width:70px;padding:6px 8px;border:1px solid #ddd;border-radius:4px;font-size:12px" />
          <span style="color:#999">-</span>
          <input v-model="filters.maxPrice" placeholder="¥最高" type="number" style="width:70px;padding:6px 8px;border:1px solid #ddd;border-radius:4px;font-size:12px" />
          <button @click="applyFilters()" style="padding:6px 10px;background:#e4393c;color:#fff;border:none;border-radius:4px;cursor:pointer;font-size:12px">确定</button>
        </div>
        <div style="display:flex;flex-wrap:wrap;gap:6px;margin-top:10px">
          <span v-for="r in [{label:'¥100以下',min:'',max:'100'},{label:'¥100-500',min:'100',max:'500'},{label:'¥500-1000',min:'500',max:'1000'},{label:'¥1000以上',min:'1000',max:''}]" :key="r.label"
                @click="filters.minPrice=r.min; filters.maxPrice=r.max; applyFilters()"
                style="padding:4px 10px;background:#f5f5f5;border-radius:12px;cursor:pointer;font-size:11px;color:#666;transition:all .2s"
                @mouseenter="(e:any) => e.target.style.background='#fff0f0'; e.target.style.color='#e4393c'"
                @mouseleave="(e:any) => { e.target.style.background='#f5f5f5'; e.target.style.color='#666' }">{{ r.label }}</span>
        </div>
      </div>
    </aside>
    <div style="flex:1">
      <div style="background:#fff;border-radius:8px;padding:12px 16px;margin-bottom:16px;display:flex;gap:16px;align-items:center;box-shadow:0 1px 4px rgba(0,0,0,.06)">
        <span style="font-size:13px;color:#999">排序：</span>
        <span @click="setSort('')" style="cursor:pointer;font-size:13px;padding:4px 8px;border-radius:4px" :style="{color:!filters.sort?'#e4393c':'#666',background:!filters.sort?'#fff0f0':''}">综合</span>
        <span @click="setSort('sales')" style="cursor:pointer;font-size:13px;padding:4px 8px;border-radius:4px" :style="{color:filters.sort==='sales'?'#e4393c':'#666',background:filters.sort==='sales'?'#fff0f0':''}">销量</span>
        <span @click="setSort('price_asc')" style="cursor:pointer;font-size:13px;padding:4px 8px;border-radius:4px" :style="{color:filters.sort==='price_asc'?'#e4393c':'#666',background:filters.sort==='price_asc'?'#fff0f0':''}">价格↑</span>
        <span @click="setSort('price_desc')" style="cursor:pointer;font-size:13px;padding:4px 8px;border-radius:4px" :style="{color:filters.sort==='price_desc'?'#e4393c':'#666',background:filters.sort==='price_desc'?'#fff0f0':''}">价格↓</span>
        <span @click="setSort('newest')" style="cursor:pointer;font-size:13px;padding:4px 8px;border-radius:4px" :style="{color:filters.sort==='newest'?'#e4393c':'#666',background:filters.sort==='newest'?'#fff0f0':''}">最新</span>
      </div>
      <div v-if="loading" style="padding:16px 0"><SkeletonBox :columns="3" :count="6" height="280px" /></div>
      <div v-else style="display:grid;grid-template-columns:repeat(3,1fr);gap:14px">
        <div v-for="p in products" :key="p.id"
             @click="goDetail(p.id)"
             style="background:#fff;border-radius:8px;overflow:hidden;cursor:pointer;transition:box-shadow .3s,transform .3s"
             @mouseenter="(e:any) => { e.target.style.boxShadow='0 4px 20px rgba(0,0,0,.12)'; e.target.style.transform='translateY(-4px)' }"
             @mouseleave="(e:any) => { e.target.style.boxShadow='0 2px 8px rgba(0,0,0,.06)'; e.target.style.transform='' }"
             :style="{boxShadow:'0 2px 8px rgba(0,0,0,.06)'}">
          <div style="height:180px;background:linear-gradient(135deg,#f8f8f8,#eee);display:flex;align-items:center;justify-content:center;font-size:48px;position:relative">
            📦
            <span v-if="p.isNew" style="position:absolute;top:6px;left:6px;background:#4caf50;color:#fff;font-size:10px;padding:1px 6px;border-radius:4px;z-index:1">新品</span>
            <span v-else-if="p.isHot" style="position:absolute;top:6px;left:6px;background:#e4393c;color:#fff;font-size:10px;padding:1px 6px;border-radius:4px;z-index:1">热卖</span>
            <span v-if="p.originalPrice && p.originalPrice > p.price" style="position:absolute;top:6px;right:50px;background:#ff9800;color:#fff;font-size:10px;padding:1px 5px;border-radius:4px;z-index:1">-{{ Math.round((1-p.price/p.originalPrice)*100) }}%</span>
            <button @click.stop="toggleCompare({id:p.id,name:p.name,price:p.price,sales:p.sales,description:p.description})"
                    style="position:absolute;top:6px;right:6px;padding:1px 8px;border:1px solid #e4393c;border-radius:10px;background:#fff;color:#e4393c;cursor:pointer;font-size:10px;z-index:1;transition:all .2s"
                    :style="{background:isSelected(p.id)?'#e4393c':'#fff',color:isSelected(p.id)?'#fff':'#e4393c'}">
              {{ isSelected(p.id) ? '✓ 对比' : '+ 对比' }}
            </button>
            <button @click.stop="openQuickView($event, {id:p.id,name:p.name,price:p.price,sales:p.sales,description:p.description,imageUrl:p.imageUrl,stock:p.stock,rating:p.rating,reviewCount:p.reviewCount})"
                    style="position:absolute;bottom:60px;right:6px;padding:2px 8px;border:1px solid #1677ff;border-radius:10px;background:#fff;color:#1677ff;cursor:pointer;font-size:10px;z-index:1;transition:all .2s"
                    @mouseenter="(e:any) => { e.target.style.background='#1677ff'; e.target.style.color='#fff' }"
                    @mouseleave="(e:any) => { e.target.style.background='#fff'; e.target.style.color='#1677ff' }">
              👁 预览
            </button>
          </div>
          <div style="padding:12px">
            <h4 style="font-size:14px;margin-bottom:6px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ p.name }}</h4>
            <div style="display:flex;align-items:center;justify-content:space-between">
              <div>
                <span style="color:#e4393c;font-size:18px;font-weight:700">¥{{ (p.price / 100).toFixed(2) }}</span>
                <span style="color:#999;font-size:11px;margin-left:4px">已售 {{ p.sales || 0 }}</span>
              </div>
              <button @click="(e: Event) => quickAdd(e, p)"
                      style="width:32px;height:32px;border-radius:50%;border:2px solid #e4393c;background:#fff;color:#e4393c;cursor:pointer;font-size:18px;display:flex;align-items:center;justify-content:center;transition:all .2s;flex-shrink:0"
                      :style="{background: (p as any)._added ? '#e4393c' : '#fff', color: (p as any)._added ? '#fff' : '#e4393c'}"
                      @mouseenter="(e:any) => { if(!(p as any)._added) e.target.style.background='#e4393c'; e.target.style.color='#fff' }"
                      @mouseleave="(e:any) => { if(!(p as any)._added) { e.target.style.background='#fff'; e.target.style.color='#e4393c' } }">
                {{ (p as any)._added ? '✓' : '+' }}
              </button>
            </div>
          </div>
        </div>
      </div>
      <div v-if="!loading && !products.length" style="text-align:center;padding:60px;color:#999">
        <p style="font-size:48px">📭</p><p>没有找到商品</p>
      </div>
      <div v-if="loadingMore" style="text-align:center;padding:16px;color:#999;font-size:13px">加载更多...</div>
      <div v-if="totalPages > 1 && !loadingMore" style="display:flex;justify-content:center;gap:8px;margin-top:24px">
        <button v-for="p in Math.min(totalPages, 10)" :key="p" @click="goPage(p-1)"
                style="width:36px;height:36px;border:1px solid #ddd;border-radius:4px;cursor:pointer;font-size:13px"
                :style="{background:currentPage===p-1?'#e4393c':'#fff',color:currentPage===p-1?'#fff':'#333',borderColor:currentPage===p-1?'#e4393c':'#ddd'}">{{ p }}</button>
      </div>
    </div>
    <QuickViewModal :product="quickViewProduct" :show="!!quickViewProduct" @close="quickViewProduct = null" />
  </div>
</template>