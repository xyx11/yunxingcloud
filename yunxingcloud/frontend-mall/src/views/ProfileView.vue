<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { getAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress, getFavorites, getMyCoupons } from '@/api/order'
import { changePassword } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const toast = inject<any>('toast')
const activeTab = ref<'addresses' | 'coupons' | 'favorites' | 'password'>('addresses')
const addresses = ref<any[]>([])
const coupons = ref<any[]>([])
const favorites = ref<any[]>([])
const shareCopied = ref(false)
async function copyShareLink() { try { await navigator.clipboard.writeText(window.location.origin + '/register?ref=' + (auth.user?.username || '')); shareCopied.value = true; toast.success('邀请链接已复制'); setTimeout(() => shareCopied.value = false, 2000) } catch {} }
const editAddr = ref<any>(null)
const showAddrForm = ref(false)
const loading = ref(true)
const pwForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const addrForm = ref({ name: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })

onMounted(async () => {
  if (!auth.isLoggedIn()) { router.push('/login'); return }
  loadTab()
})

async function loadTab() {
  loading.value = true
  if (activeTab.value === 'addresses') {
    try { const r = await getAddresses(); addresses.value = r.data || [] } catch {}
  } else if (activeTab.value === 'coupons') {
    try { const r = await getMyCoupons(); coupons.value = r.data || [] } catch {}
  } else if (activeTab.value === 'favorites') {
    try { const r = await getFavorites(); favorites.value = r.data || [] } catch {}
  }
  loading.value = false
}

async function saveAddress() {
  try {
    if (editAddr.value) {
      await updateAddress(editAddr.value.id, addrForm.value)
    } else {
      await createAddress(addrForm.value)
    }
    toast.success(editAddr.value ? '已更新' : '已添加')
    showAddrForm.value = false; editAddr.value = null
    loadTab()
  } catch {}
}

function editAddress(addr: any) {
  editAddr.value = addr
  addrForm.value = { name: addr.name, phone: addr.phone, province: addr.province || '', city: addr.city || '', district: addr.district || '', detail: addr.detail || '', isDefault: addr.isDefault }
  showAddrForm.value = true
}

async function deleteAddressById(id: number) {
  if (!confirm('确认删除？')) return
  await deleteAddress(id)
  toast.info('已删除')
  loadTab()
}

async function changePwd() {
  if (!pwForm.value.oldPassword || !pwForm.value.newPassword) { toast.error('请填写完整'); return }
  if (pwForm.value.newPassword !== pwForm.value.confirmPassword) { toast.error('两次密码不一致'); return }
  try {
    await changePassword(pwForm.value.oldPassword, pwForm.value.newPassword)
    toast.success('密码修改成功')
    pwForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e: any) { toast.error(e.response?.data?.message || '修改失败') }
}

async function toggleDefault(addr: any) {
  await setDefaultAddress(addr.id, !addr.isDefault)
  loadTab()
}
</script>

<template>
  <div v-if="auth.isLoggedIn()" style="max-width:900px;margin:0 auto">
    <div style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06);margin-bottom:16px">
      <div style="display:flex;align-items:center;gap:16px">
        <div style="width:64px;height:64px;border-radius:50%;background:#f10215;color:#fff;display:flex;align-items:center;justify-content:center;font-size:28px;font-weight:700">{{ auth.user?.username?.charAt(0)?.toUpperCase() }}</div>
        <div><h2 style="font-size:20px">{{ auth.user?.username }}</h2><p style="color:#999;font-size:13px">YXCLOUD 商城会员</p></div>
      </div>
    </div>
    <div @click="copyShareLink" style="background:linear-gradient(135deg,#fff5f5,#fff0f0);border:1px solid #ffcccc;border-radius:12px;padding:16px 20px;margin-bottom:16px;display:flex;align-items:center;justify-content:space-between;cursor:pointer;box-shadow:0 2px 8px rgba(0,0,0,.06)" @mouseenter="(e:any) => e.target.style.borderColor='#f10215'" @mouseleave="(e:any) => e.target.style.borderColor='#ffcccc'">
      <div>
        <div style="font-weight:700;font-size:14px;margin-bottom:2px">🎁 邀请好友，双方各得 <span style="color:#f10215">50 积分</span></div>
        <div style="font-size:11px;color:#999">分享链接给好友注册即可获得奖励</div>
      </div>
      <button @click.stop="copyShareLink" style="padding:6px 16px;background:#f10215;color:#fff;border:none;border-radius:20px;cursor:pointer;font-size:12px;font-weight:600;white-space:nowrap" :style="shareCopied?{background:'#4caf50'}:{}">{{ shareCopied ? '✓ 已复制' : '立即邀请' }}</button>
    </div>
    <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:8px;margin-bottom:16px">
      <div v-for="l in [{icon:'⭐',label:'积分',path:'/points',color:'#ff9800'},{icon:'🎁',label:'礼品卡',path:'/gift-card',color:'#f10215'},{icon:'🏆',label:'排行榜',path:'/ranking',color:'#ff6b00'},{icon:'❓',label:'帮助',path:'/help',color:'#1677ff'}]" :key="l.path"
           @click="router.push(l.path)" style="background:#fff;border-radius:8px;padding:12px 8px;text-align:center;cursor:pointer;box-shadow:0 1px 4px rgba(0,0,0,.04);transition:transform .2s"
           @mouseenter="(e:any) => e.target.style.transform='translateY(-2px)'" @mouseleave="(e:any) => e.target.style.transform=''">
        <div style="font-size:24px;margin-bottom:4px">{{ l.icon }}</div>
        <div style="font-size:11px;color:#333;font-weight:500">{{ l.label }}</div>
      </div>
    </div>
    <div style="display:flex;gap:0;margin-bottom:16px;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <span v-for="tab in [{key:'addresses',label:'收货地址'},{key:'favorites',label:'我的收藏'},{key:'coupons',label:'优惠券'},{key:'password',label:'修改密码'}]"
            :key="tab.key" @click="activeTab = tab.key; loadTab()"
            style="flex:1;text-align:center;padding:12px;cursor:pointer;font-size:14px;transition:all .2s"
            :style="{background:activeTab===tab.key?'#f10215':'#fff',color:activeTab===tab.key?'#fff':'#666'}">{{ tab.label }}</span>
    </div>
    <div style="background:#fff;border-radius:12px;padding:24px;box-shadow:0 2px 8px rgba(0,0,0,.06);min-height:300px">
      <div v-if="activeTab==='addresses'">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
          <h3 style="font-size:16px;font-weight:600">收货地址</h3>
          <button @click="editAddr=null;addrForm={name:'',phone:'',province:'',city:'',district:'',detail:'',isDefault:false};showAddrForm=true"
                  style="padding:6px 16px;background:#f10215;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:13px">+ 新增</button>
        </div>
        <div v-if="showAddrForm" style="background:#fafafa;padding:20px;border-radius:8px;margin-bottom:16px">
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:12px">
            <input v-model="addrForm.name" placeholder="收货人" style="padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px" />
            <input v-model="addrForm.phone" placeholder="手机号" style="padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px" />
            <input v-model="addrForm.province" placeholder="省份" style="padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px" />
            <input v-model="addrForm.city" placeholder="城市" style="padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px" />
            <input v-model="addrForm.district" placeholder="区/县" style="padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px" />
          </div>
          <input v-model="addrForm.detail" placeholder="详细地址" style="width:100%;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px;box-sizing:border-box;margin-bottom:12px" />
          <div style="display:flex;justify-content:space-between;align-items:center">
            <label style="font-size:13px;cursor:pointer"><input type="checkbox" v-model="addrForm.isDefault" /> 设为默认</label>
            <div style="display:flex;gap:8px">
              <button @click="showAddrForm=false;editAddr=null" style="padding:6px 16px;border:1px solid #ddd;background:#fff;border-radius:6px;cursor:pointer;font-size:13px">取消</button>
              <button @click="saveAddress" style="padding:6px 16px;background:#f10215;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:13px">保存</button>
            </div>
          </div>
        </div>
        <div v-if="addresses.length">
          <div v-for="addr in addresses" :key="addr.id" style="padding:16px;border:1px solid #f0f0f0;border-radius:8px;margin-bottom:12px;position:relative">
            <div v-if="addr.isDefault" style="position:absolute;top:0;right:0;background:#f10215;color:#fff;font-size:11px;padding:2px 8px;border-radius:0 8px 0 8px">默认</div>
            <div style="font-weight:600;margin-bottom:4px">{{ addr.name }} <span style="color:#999;font-weight:400;margin-left:8px">{{ addr.phone }}</span></div>
            <div style="color:#666;font-size:13px">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</div>
            <div style="margin-top:8px;display:flex;gap:8px">
              <button @click="editAddress(addr)" style="padding:2px 10px;border:1px solid #ddd;background:#fff;border-radius:4px;cursor:pointer;font-size:12px">编辑</button>
              <button @click="deleteAddressById(addr.id)" style="padding:2px 10px;border:1px solid #ddd;background:#fff;border-radius:4px;cursor:pointer;font-size:12px;color:#f10215">删除</button>
              <button v-if="!addr.isDefault" @click="toggleDefault(addr)" style="padding:2px 10px;border:1px solid #ddd;background:#fff;border-radius:4px;cursor:pointer;font-size:12px">设为默认</button>
            </div>
          </div>
        </div>
        <div v-else-if="!loading" style="text-align:center;padding:40px;color:#999">暂无收货地址</div>
      </div>
      <div v-if="activeTab==='favorites'">
        <h3 style="font-size:16px;font-weight:600;margin-bottom:16px">我的收藏</h3>
        <div v-if="favorites.length" style="display:grid;grid-template-columns:repeat(3,1fr);gap:12px">
          <div v-for="f in favorites" :key="f.id" @click="router.push(`/product/${f.productId}`)" style="border:1px solid #f0f0f0;border-radius:8px;padding:12px;cursor:pointer;transition:box-shadow .2s"
               @mouseenter="(e:any) => e.target.style.boxShadow='0 2px 12px rgba(0,0,0,.08)'" @mouseleave="(e:any) => e.target.style.boxShadow=''">
            <div style="height:100px;background:#f5f5f5;border-radius:6px;display:flex;align-items:center;justify-content:center;font-size:36px;margin-bottom:8px">📦</div>
            <div style="font-size:13px;text-align:center;color:#666">商品 #{{ f.productId }}</div>
          </div>
        </div>
        <div v-else-if="!loading" style="text-align:center;padding:40px;color:#999">暂无收藏</div>
      </div>
      <div v-if="activeTab==='coupons'">
        <h3 style="font-size:16px;font-weight:600;margin-bottom:16px">我的优惠券</h3>
        <div v-if="coupons.length">
          <div v-for="c in coupons" :key="c.id" style="display:flex;align-items:center;padding:16px;border:1px solid #f0f0f0;border-radius:8px;margin-bottom:12px">
            <div style="width:80px;height:80px;background:linear-gradient(135deg,#f10215,#ff6b6b);color:#fff;display:flex;flex-direction:column;align-items:center;justify-content:center;border-radius:8px;margin-right:16px;flex-shrink:0">
              <span style="font-size:12px">优惠券</span><span style="font-size:18px;font-weight:700">#{{ c.couponId }}</span>
            </div>
            <div><div style="font-weight:600;margin-bottom:4px">优惠券 #{{ c.couponId }}</div><div style="color:#999;font-size:12px">{{ c.status === '0' ? '未使用' : '已使用' }}</div></div>
          </div>
        </div>
        <div v-else-if="!loading" style="text-align:center;padding:40px;color:#999">暂无优惠券</div>
      </div>
      <div v-if="activeTab==='password'">
        <h3 style="font-size:16px;font-weight:600;margin-bottom:16px">修改密码</h3>
        <div style="max-width:400px">
          <div style="margin-bottom:12px"><label style="display:block;font-size:13px;color:#666;margin-bottom:4px">当前密码</label><input v-model="pwForm.oldPassword" type="password" style="width:100%;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px;box-sizing:border-box" /></div>
          <div style="margin-bottom:12px"><label style="display:block;font-size:13px;color:#666;margin-bottom:4px">新密码</label><input v-model="pwForm.newPassword" type="password" style="width:100%;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px;box-sizing:border-box" /></div>
          <div style="margin-bottom:16px"><label style="display:block;font-size:13px;color:#666;margin-bottom:4px">确认新密码</label><input v-model="pwForm.confirmPassword" type="password" style="width:100%;padding:10px;border:1px solid #ddd;border-radius:6px;font-size:13px;box-sizing:border-box" /></div>
          <button @click="changePwd" style="padding:10px 32px;background:#f10215;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:14px;font-weight:600">确认修改</button>
        </div>
      </div>
    </div>
  </div>
</template>