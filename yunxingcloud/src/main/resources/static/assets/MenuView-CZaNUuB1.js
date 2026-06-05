import{ai as ye,as as ke,al as xe,c as te,b as s,at as ae,a9 as q,d as k,a as le,e as oe,au as G,h as c,W as F,av as Ce,ab as Se,u as Be,f as re,aw as Ne,p as y,ax as ze,i as $e,j as K,ay as J,af as _,K as Q,az as x,U as Re,o as Ve,l as Te,w as d,m as o,N as Fe,r as j,t as _e,v as r,B as I,A as Z,a8 as Me,s as Oe}from"./index-Brdb4NIO.js";import{u as Ue}from"./useNotify-m8QtYtte.js";import{N as Pe,a as ie}from"./DataTable-Bejb7Omr.js";import{N as je}from"./Modal-CN93msaK.js";import{N as Ie,a as C}from"./FormItem-Dr1wFX41.js";import{N as D}from"./Input-DFIkIXZM.js";import{N as De}from"./InputNumber-BgDHOUMU.js";import{N as ne}from"./Space-DizjvrNX.js";import{N as We}from"./Card-kqY1HZ0y.js";import{N as Ke}from"./Popconfirm-CSYpjagj.js";import"./Checkbox-ClvCoxBD.js";import"./Success-CAO_Ggpx.js";function Ae(e){const{primaryColor:b,opacityDisabled:g,borderRadius:h,textColor3:m}=e;return Object.assign(Object.assign({},ke),{iconColor:m,textColor:"white",loadingColor:b,opacityDisabled:g,railColor:"rgba(0, 0, 0, .14)",railColorActive:b,buttonBoxShadow:"0 1px 4px 0 rgba(0, 0, 0, 0.3), inset 0 0 1px 0 rgba(0, 0, 0, 0.05)",buttonColor:"#FFF",railBorderRadiusSmall:h,railBorderRadiusMedium:h,railBorderRadiusLarge:h,buttonBorderRadiusSmall:h,buttonBorderRadiusMedium:h,buttonBorderRadiusLarge:h,boxShadowFocus:`0 0 0 2px ${xe(b,{alpha:.2})}`})}const He={common:ye,self:Ae},Ee=te("switch",`
 height: var(--n-height);
 min-width: var(--n-width);
 vertical-align: middle;
 user-select: none;
 -webkit-user-select: none;
 display: inline-flex;
 outline: none;
 justify-content: center;
 align-items: center;
`,[s("children-placeholder",`
 height: var(--n-rail-height);
 display: flex;
 flex-direction: column;
 overflow: hidden;
 pointer-events: none;
 visibility: hidden;
 `),s("rail-placeholder",`
 display: flex;
 flex-wrap: none;
 `),s("button-placeholder",`
 width: calc(1.75 * var(--n-rail-height));
 height: var(--n-rail-height);
 `),te("base-loading",`
 position: absolute;
 top: 50%;
 left: 50%;
 transform: translateX(-50%) translateY(-50%);
 font-size: calc(var(--n-button-width) - 4px);
 color: var(--n-loading-color);
 transition: color .3s var(--n-bezier);
 `,[ae({left:"50%",top:"50%",originalTransform:"translateX(-50%) translateY(-50%)"})]),s("checked, unchecked",`
 transition: color .3s var(--n-bezier);
 color: var(--n-text-color);
 box-sizing: border-box;
 position: absolute;
 white-space: nowrap;
 top: 0;
 bottom: 0;
 display: flex;
 align-items: center;
 line-height: 1;
 `),s("checked",`
 right: 0;
 padding-right: calc(1.25 * var(--n-rail-height) - var(--n-offset));
 `),s("unchecked",`
 left: 0;
 justify-content: flex-end;
 padding-left: calc(1.25 * var(--n-rail-height) - var(--n-offset));
 `),q("&:focus",[s("rail",`
 box-shadow: var(--n-box-shadow-focus);
 `)]),k("round",[s("rail","border-radius: calc(var(--n-rail-height) / 2);",[s("button","border-radius: calc(var(--n-button-height) / 2);")])]),le("disabled",[le("icon",[k("rubber-band",[k("pressed",[s("rail",[s("button","max-width: var(--n-button-width-pressed);")])]),s("rail",[q("&:active",[s("button","max-width: var(--n-button-width-pressed);")])]),k("active",[k("pressed",[s("rail",[s("button","left: calc(100% - var(--n-offset) - var(--n-button-width-pressed));")])]),s("rail",[q("&:active",[s("button","left: calc(100% - var(--n-offset) - var(--n-button-width-pressed));")])])])])])]),k("active",[s("rail",[s("button","left: calc(100% - var(--n-button-width) - var(--n-offset))")])]),s("rail",`
 overflow: hidden;
 height: var(--n-rail-height);
 min-width: var(--n-rail-width);
 border-radius: var(--n-rail-border-radius);
 cursor: pointer;
 position: relative;
 transition:
 opacity .3s var(--n-bezier),
 background .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier);
 background-color: var(--n-rail-color);
 `,[s("button-icon",`
 color: var(--n-icon-color);
 transition: color .3s var(--n-bezier);
 font-size: calc(var(--n-button-height) - 4px);
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 display: flex;
 justify-content: center;
 align-items: center;
 line-height: 1;
 `,[ae()]),s("button",`
 align-items: center; 
 top: var(--n-offset);
 left: var(--n-offset);
 height: var(--n-button-height);
 width: var(--n-button-width-pressed);
 max-width: var(--n-button-width);
 border-radius: var(--n-button-border-radius);
 background-color: var(--n-button-color);
 box-shadow: var(--n-button-box-shadow);
 box-sizing: border-box;
 cursor: inherit;
 content: "";
 position: absolute;
 transition:
 background-color .3s var(--n-bezier),
 left .3s var(--n-bezier),
 opacity .3s var(--n-bezier),
 max-width .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier);
 `)]),k("active",[s("rail","background-color: var(--n-rail-color-active);")]),k("loading",[s("rail",`
 cursor: wait;
 `)]),k("disabled",[s("rail",`
 cursor: not-allowed;
 opacity: .5;
 `)])]),Le=Object.assign(Object.assign({},re.props),{size:String,value:{type:[String,Number,Boolean],default:void 0},loading:Boolean,defaultValue:{type:[String,Number,Boolean],default:!1},disabled:{type:Boolean,default:void 0},round:{type:Boolean,default:!0},"onUpdate:value":[Function,Array],onUpdateValue:[Function,Array],checkedValue:{type:[String,Number,Boolean],default:!0},uncheckedValue:{type:[String,Number,Boolean],default:!1},railStyle:Function,rubberBand:{type:Boolean,default:!0},spinProps:Object,onChange:[Function,Array]});let W;const Xe=oe({name:"Switch",props:Le,slots:Object,setup(e){W===void 0&&(typeof CSS<"u"?typeof CSS.supports<"u"?W=CSS.supports("width","max(1px)"):W=!1:W=!0);const{mergedClsPrefixRef:b,inlineThemeDisabled:g,mergedComponentPropsRef:h}=Be(e),m=re("Switch","-switch",Ee,He,e,b),v=Ne(e,{mergedSize(n){var B,N;if(e.size!==void 0)return e.size;if(n)return n.mergedSize.value;const T=(N=(B=h==null?void 0:h.value)===null||B===void 0?void 0:B.Switch)===null||N===void 0?void 0:N.size;return T||"medium"}}),{mergedSizeRef:f,mergedDisabledRef:a}=v,R=y(e.defaultValue),S=Re(e,"value"),w=ze(S,R),M=K(()=>w.value===e.checkedValue),u=y(!1),p=y(!1),V=K(()=>{const{railStyle:n}=e;if(n)return n({focused:p.value,checked:M.value})});function O(n){const{"onUpdate:value":B,onChange:N,onUpdateValue:T}=e,{nTriggerFormInput:H,nTriggerFormChange:E}=v;B&&J(B,n),T&&J(T,n),N&&J(N,n),R.value=n,H(),E()}function A(){const{nTriggerFormFocus:n}=v;n()}function l(){const{nTriggerFormBlur:n}=v;n()}function t(){e.loading||a.value||(w.value!==e.checkedValue?O(e.checkedValue):O(e.uncheckedValue))}function i(){p.value=!0,A()}function se(){p.value=!1,l(),u.value=!1}function ue(n){e.loading||a.value||n.key===" "&&(w.value!==e.checkedValue?O(e.checkedValue):O(e.uncheckedValue),u.value=!1)}function de(n){e.loading||a.value||n.key===" "&&(n.preventDefault(),u.value=!0)}const ee=K(()=>{const{value:n}=f,{self:{opacityDisabled:B,railColor:N,railColorActive:T,buttonBoxShadow:H,buttonColor:E,boxShadowFocus:ce,loadingColor:he,textColor:ve,iconColor:fe,[_("buttonHeight",n)]:z,[_("buttonWidth",n)]:pe,[_("buttonWidthPressed",n)]:be,[_("railHeight",n)]:$,[_("railWidth",n)]:P,[_("railBorderRadius",n)]:me,[_("buttonBorderRadius",n)]:ge},common:{cubicBezierEaseInOut:we}}=m.value;let L,X,Y;return W?(L=`calc((${$} - ${z}) / 2)`,X=`max(${$}, ${z})`,Y=`max(${P}, calc(${P} + ${z} - ${$}))`):(L=Q((x($)-x(z))/2),X=Q(Math.max(x($),x(z))),Y=x($)>x(z)?P:Q(x(P)+x(z)-x($))),{"--n-bezier":we,"--n-button-border-radius":ge,"--n-button-box-shadow":H,"--n-button-color":E,"--n-button-width":pe,"--n-button-width-pressed":be,"--n-button-height":z,"--n-height":X,"--n-offset":L,"--n-opacity-disabled":B,"--n-rail-border-radius":me,"--n-rail-color":N,"--n-rail-color-active":T,"--n-rail-height":$,"--n-rail-width":P,"--n-width":Y,"--n-box-shadow-focus":ce,"--n-loading-color":he,"--n-text-color":ve,"--n-icon-color":fe}}),U=g?$e("switch",K(()=>f.value[0]),ee,e):void 0;return{handleClick:t,handleBlur:se,handleFocus:i,handleKeyup:ue,handleKeydown:de,mergedRailStyle:V,pressed:u,mergedClsPrefix:b,mergedValue:w,checked:M,mergedDisabled:a,cssVars:g?void 0:ee,themeClass:U==null?void 0:U.themeClass,onRender:U==null?void 0:U.onRender}},render(){const{mergedClsPrefix:e,mergedDisabled:b,checked:g,mergedRailStyle:h,onRender:m,$slots:v}=this;m==null||m();const{checked:f,unchecked:a,icon:R,"checked-icon":S,"unchecked-icon":w}=v,M=!(G(R)&&G(S)&&G(w));return c("div",{role:"switch","aria-checked":g,class:[`${e}-switch`,this.themeClass,M&&`${e}-switch--icon`,g&&`${e}-switch--active`,b&&`${e}-switch--disabled`,this.round&&`${e}-switch--round`,this.loading&&`${e}-switch--loading`,this.pressed&&`${e}-switch--pressed`,this.rubberBand&&`${e}-switch--rubber-band`],tabindex:this.mergedDisabled?void 0:0,style:this.cssVars,onClick:this.handleClick,onFocus:this.handleFocus,onBlur:this.handleBlur,onKeyup:this.handleKeyup,onKeydown:this.handleKeydown},c("div",{class:`${e}-switch__rail`,"aria-hidden":"true",style:h},F(f,u=>F(a,p=>u||p?c("div",{"aria-hidden":!0,class:`${e}-switch__children-placeholder`},c("div",{class:`${e}-switch__rail-placeholder`},c("div",{class:`${e}-switch__button-placeholder`}),u),c("div",{class:`${e}-switch__rail-placeholder`},c("div",{class:`${e}-switch__button-placeholder`}),p)):null)),c("div",{class:`${e}-switch__button`},F(R,u=>F(S,p=>F(w,V=>c(Ce,null,{default:()=>this.loading?c(Se,Object.assign({key:"loading",clsPrefix:e,strokeWidth:20},this.spinProps)):this.checked&&(p||u)?c("div",{class:`${e}-switch__button-icon`,key:p?"checked-icon":"icon"},p||u):!this.checked&&(V||u)?c("div",{class:`${e}-switch__button-icon`,key:V?"unchecked-icon":"icon"},V||u):null})))),F(f,u=>u&&c("div",{key:"checked",class:`${e}-switch__checked`},u)),F(a,u=>u&&c("div",{key:"unchecked",class:`${e}-switch__unchecked`},u)))))}}),Ye={style:{padding:"24px"}},rt=oe({__name:"MenuView",setup(e){const b=y([]),g=Ue(),h=y([]),m=y(!1),v=y(!1),f=y(null),a=y({name:"",parentId:null,sortOrder:0,path:"",component:"",icon:"",menuType:"M",perms:"",visible:!0}),R=[{label:"目录 (M)",value:"M"},{label:"菜单 (C)",value:"C"},{label:"按钮 (F)",value:"F"}],S=y([]);function w(l,t=""){for(const i of l)i.menuType!=="F"&&(S.value.push({label:t+i.name,value:i.id}),i.children&&w(i.children,t+"　"))}const M=[{title:"名称",key:"name",width:160},{title:"图标",key:"icon",width:80},{title:"类型",key:"menuType",width:80,render:l=>c(Me,{type:l.menuType==="M"?"info":l.menuType==="C"?"success":"default",size:"small"},{default:()=>({M:"目录",C:"菜单",F:"按钮"})[l.menuType]||l.menuType})},{title:"路由",key:"path",width:140},{title:"组件",key:"component",width:140},{title:"权限标识",key:"perms",width:160},{title:"排序",key:"sortOrder",width:60,sorter:!0},{title:"可见",key:"visible",width:60,render:l=>l.visible?"是":"否"},{title:"操作",key:"actions",width:140,render:l=>c(ne,null,{default:()=>[c(I,{size:"small",onClick:()=>V(l)},{default:()=>"编辑"}),c(Ke,{onPositiveClick:()=>A(l.id)},{trigger:()=>c(I,{size:"small",type:"error"},{default:()=>"删除"}),default:()=>"确认删除?"})]})}];async function u(){m.value=!0;const l=await j.get("/api/menus/tree");b.value=l.data;const t=await j.get("/api/menus");h.value=t.data,S.value=[{label:"无 (根目录)",value:null}],w(l.data),m.value=!1}function p(){f.value=null,a.value={name:"",parentId:null,sortOrder:0,path:"",component:"",icon:"",menuType:"M",perms:"",visible:!0},v.value=!0}function V(l){f.value=l,a.value={name:l.name,parentId:l.parentId,sortOrder:l.sortOrder,path:l.path||"",component:l.component||"",icon:l.icon||"",menuType:l.menuType,perms:l.perms||"",visible:l.visible},v.value=!0}async function O(){f.value?await j.put(`/api/menus/${f.value.id}`,a.value):await j.post("/api/menus",a.value),v.value=!1,g.success(f.value?"更新成功":"创建成功"),await u()}async function A(l){await j.delete(`/api/menus/${l}`),g.success("删除成功"),await u()}return Ve(u),(l,t)=>(Oe(),Te(o(Fe),null,{default:d(()=>[_e("div",Ye,[r(o(We),{title:"菜单管理"},{"header-extra":d(()=>[r(o(I),{type:"primary",size:"small",onClick:p},{default:d(()=>[...t[11]||(t[11]=[Z("新增菜单",-1)])]),_:1})]),default:d(()=>[r(o(Pe),{columns:M,data:b.value,loading:m.value,pagination:{pageSize:10},"default-expand-all":"","row-key":i=>i.id,"children-key":"children"},null,8,["data","loading","row-key"]),r(o(je),{show:v.value,"onUpdate:show":t[10]||(t[10]=i=>v.value=i),title:f.value?"编辑菜单":"新增菜单",style:{width:"600px"}},{footer:d(()=>[r(o(ne),{justify:"end"},{default:d(()=>[r(o(I),{onClick:t[9]||(t[9]=i=>v.value=!1)},{default:d(()=>[...t[12]||(t[12]=[Z("取消",-1)])]),_:1}),r(o(I),{type:"primary",onClick:O},{default:d(()=>[...t[13]||(t[13]=[Z("保存",-1)])]),_:1})]),_:1})]),default:d(()=>[r(o(Ie),{"label-placement":"left","label-width":"80"},{default:d(()=>[r(o(C),{label:"名称"},{default:d(()=>[r(o(D),{value:a.value.name,"onUpdate:value":t[0]||(t[0]=i=>a.value.name=i)},null,8,["value"])]),_:1}),r(o(C),{label:"类型"},{default:d(()=>[r(o(ie),{value:a.value.menuType,"onUpdate:value":t[1]||(t[1]=i=>a.value.menuType=i),options:R},null,8,["value"])]),_:1}),r(o(C),{label:"上级菜单"},{default:d(()=>[r(o(ie),{value:a.value.parentId,"onUpdate:value":t[2]||(t[2]=i=>a.value.parentId=i),options:S.value},null,8,["value","options"])]),_:1}),r(o(C),{label:"路由路径"},{default:d(()=>[r(o(D),{value:a.value.path,"onUpdate:value":t[3]||(t[3]=i=>a.value.path=i),placeholder:"/example"},null,8,["value"])]),_:1}),r(o(C),{label:"组件名称"},{default:d(()=>[r(o(D),{value:a.value.component,"onUpdate:value":t[4]||(t[4]=i=>a.value.component=i),placeholder:"ExampleView"},null,8,["value"])]),_:1}),r(o(C),{label:"图标"},{default:d(()=>[r(o(D),{value:a.value.icon,"onUpdate:value":t[5]||(t[5]=i=>a.value.icon=i),placeholder:"icon-name"},null,8,["value"])]),_:1}),r(o(C),{label:"权限标识"},{default:d(()=>[r(o(D),{value:a.value.perms,"onUpdate:value":t[6]||(t[6]=i=>a.value.perms=i),placeholder:"system:example:list"},null,8,["value"])]),_:1}),r(o(C),{label:"排序"},{default:d(()=>[r(o(De),{value:a.value.sortOrder,"onUpdate:value":t[7]||(t[7]=i=>a.value.sortOrder=i),min:0},null,8,["value"])]),_:1}),r(o(C),{label:"可见"},{default:d(()=>[r(o(Xe),{value:a.value.visible,"onUpdate:value":t[8]||(t[8]=i=>a.value.visible=i)},null,8,["value"])]),_:1})]),_:1})]),_:1},8,["show","title"])]),_:1})])]),_:1}))}});export{rt as default};
