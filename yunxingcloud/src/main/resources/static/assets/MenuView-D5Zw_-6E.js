import{d as ye,aa as xe,c as ee,b as s,ah as te,Z as Y,e as k,a as ae,f as ne,ai as q,h as c,X as F,aj as ke,a3 as Ce,u as Se,g as oe,ak as Be,p as m,al as Ne,i as $e,j as L,am as Z,a1 as _,L as G,an as C,W as Re,o as ze,l as Me,w as u,m as o,N as Ve,r as U,t as Te,v as r,B as j,A as J,a8 as Fe,s as _e}from"./index-Bn6z-r_K.js";import{N as Oe,a as le}from"./DataTable-Mu-sMRld.js";import{N as We}from"./Modal-PYjg1Zxx.js";import{N as Pe,a as S}from"./FormItem-CcX-EJ8V.js";import{N as I}from"./Input-CcHPA-_n.js";import{N as Ue}from"./InputNumber-qHBj2ygU.js";import{N as ie}from"./Space-BJe-sY-b.js";import{N as je}from"./Card-BGV--IU_.js";import{N as Ie}from"./Popconfirm-Dz5ZqusU.js";import"./Checkbox-BBNqgYwJ.js";import"./Success-DLqHsYjH.js";const He={buttonHeightSmall:"14px",buttonHeightMedium:"18px",buttonHeightLarge:"22px",buttonWidthSmall:"14px",buttonWidthMedium:"18px",buttonWidthLarge:"22px",buttonWidthPressedSmall:"20px",buttonWidthPressedMedium:"24px",buttonWidthPressedLarge:"28px",railHeightSmall:"18px",railHeightMedium:"22px",railHeightLarge:"26px",railWidthSmall:"32px",railWidthMedium:"40px",railWidthLarge:"48px"};function Le(e){const{primaryColor:b,opacityDisabled:g,borderRadius:h,textColor3:v}=e;return Object.assign(Object.assign({},He),{iconColor:v,textColor:"white",loadingColor:b,opacityDisabled:g,railColor:"rgba(0, 0, 0, .14)",railColorActive:b,buttonBoxShadow:"0 1px 4px 0 rgba(0, 0, 0, 0.3), inset 0 0 1px 0 rgba(0, 0, 0, 0.05)",buttonColor:"#FFF",railBorderRadiusSmall:h,railBorderRadiusMedium:h,railBorderRadiusLarge:h,buttonBorderRadiusSmall:h,buttonBorderRadiusMedium:h,buttonBorderRadiusLarge:h,boxShadowFocus:`0 0 0 2px ${xe(b,{alpha:.2})}`})}const De={common:ye,self:Le},Ke=ee("switch",`
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
 `),ee("base-loading",`
 position: absolute;
 top: 50%;
 left: 50%;
 transform: translateX(-50%) translateY(-50%);
 font-size: calc(var(--n-button-width) - 4px);
 color: var(--n-loading-color);
 transition: color .3s var(--n-bezier);
 `,[te({left:"50%",top:"50%",originalTransform:"translateX(-50%) translateY(-50%)"})]),s("checked, unchecked",`
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
 `),Y("&:focus",[s("rail",`
 box-shadow: var(--n-box-shadow-focus);
 `)]),k("round",[s("rail","border-radius: calc(var(--n-rail-height) / 2);",[s("button","border-radius: calc(var(--n-button-height) / 2);")])]),ae("disabled",[ae("icon",[k("rubber-band",[k("pressed",[s("rail",[s("button","max-width: var(--n-button-width-pressed);")])]),s("rail",[Y("&:active",[s("button","max-width: var(--n-button-width-pressed);")])]),k("active",[k("pressed",[s("rail",[s("button","left: calc(100% - var(--n-offset) - var(--n-button-width-pressed));")])]),s("rail",[Y("&:active",[s("button","left: calc(100% - var(--n-offset) - var(--n-button-width-pressed));")])])])])])]),k("active",[s("rail",[s("button","left: calc(100% - var(--n-button-width) - var(--n-offset))")])]),s("rail",`
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
 `,[te()]),s("button",`
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
 `)])]),Ae=Object.assign(Object.assign({},oe.props),{size:String,value:{type:[String,Number,Boolean],default:void 0},loading:Boolean,defaultValue:{type:[String,Number,Boolean],default:!1},disabled:{type:Boolean,default:void 0},round:{type:Boolean,default:!0},"onUpdate:value":[Function,Array],onUpdateValue:[Function,Array],checkedValue:{type:[String,Number,Boolean],default:!0},uncheckedValue:{type:[String,Number,Boolean],default:!1},railStyle:Function,rubberBand:{type:Boolean,default:!0},spinProps:Object,onChange:[Function,Array]});let H;const Ee=ne({name:"Switch",props:Ae,slots:Object,setup(e){H===void 0&&(typeof CSS<"u"?typeof CSS.supports<"u"?H=CSS.supports("width","max(1px)"):H=!1:H=!0);const{mergedClsPrefixRef:b,inlineThemeDisabled:g,mergedComponentPropsRef:h}=Se(e),v=oe("Switch","-switch",Ke,De,e,b),f=Be(e,{mergedSize(n){var N,$;if(e.size!==void 0)return e.size;if(n)return n.mergedSize.value;const T=($=(N=h==null?void 0:h.value)===null||N===void 0?void 0:N.Switch)===null||$===void 0?void 0:$.size;return T||"medium"}}),{mergedSizeRef:i,mergedDisabledRef:w}=f,y=m(e.defaultValue),M=Re(e,"value"),x=Ne(M,y),B=L(()=>x.value===e.checkedValue),d=m(!1),p=m(!1),V=L(()=>{const{railStyle:n}=e;if(n)return n({focused:p.value,checked:B.value})});function O(n){const{"onUpdate:value":N,onChange:$,onUpdateValue:T}=e,{nTriggerFormInput:D,nTriggerFormChange:K}=f;N&&Z(N,n),T&&Z(T,n),$&&Z($,n),y.value=n,D(),K()}function a(){const{nTriggerFormFocus:n}=f;n()}function t(){const{nTriggerFormBlur:n}=f;n()}function l(){e.loading||w.value||(x.value!==e.checkedValue?O(e.checkedValue):O(e.uncheckedValue))}function re(){p.value=!0,a()}function se(){p.value=!1,t(),d.value=!1}function de(n){e.loading||w.value||n.key===" "&&(x.value!==e.checkedValue?O(e.checkedValue):O(e.uncheckedValue),d.value=!1)}function ue(n){e.loading||w.value||n.key===" "&&(n.preventDefault(),d.value=!0)}const Q=L(()=>{const{value:n}=i,{self:{opacityDisabled:N,railColor:$,railColorActive:T,buttonBoxShadow:D,buttonColor:K,boxShadowFocus:ce,loadingColor:he,textColor:ve,iconColor:fe,[_("buttonHeight",n)]:R,[_("buttonWidth",n)]:pe,[_("buttonWidthPressed",n)]:be,[_("railHeight",n)]:z,[_("railWidth",n)]:P,[_("railBorderRadius",n)]:me,[_("buttonBorderRadius",n)]:ge},common:{cubicBezierEaseInOut:we}}=v.value;let A,E,X;return H?(A=`calc((${z} - ${R}) / 2)`,E=`max(${z}, ${R})`,X=`max(${P}, calc(${P} + ${R} - ${z}))`):(A=G((C(z)-C(R))/2),E=G(Math.max(C(z),C(R))),X=C(z)>C(R)?P:G(C(P)+C(R)-C(z))),{"--n-bezier":we,"--n-button-border-radius":ge,"--n-button-box-shadow":D,"--n-button-color":K,"--n-button-width":pe,"--n-button-width-pressed":be,"--n-button-height":R,"--n-height":E,"--n-offset":A,"--n-opacity-disabled":N,"--n-rail-border-radius":me,"--n-rail-color":$,"--n-rail-color-active":T,"--n-rail-height":z,"--n-rail-width":P,"--n-width":X,"--n-box-shadow-focus":ce,"--n-loading-color":he,"--n-text-color":ve,"--n-icon-color":fe}}),W=g?$e("switch",L(()=>i.value[0]),Q,e):void 0;return{handleClick:l,handleBlur:se,handleFocus:re,handleKeyup:de,handleKeydown:ue,mergedRailStyle:V,pressed:d,mergedClsPrefix:b,mergedValue:x,checked:B,mergedDisabled:w,cssVars:g?void 0:Q,themeClass:W==null?void 0:W.themeClass,onRender:W==null?void 0:W.onRender}},render(){const{mergedClsPrefix:e,mergedDisabled:b,checked:g,mergedRailStyle:h,onRender:v,$slots:f}=this;v==null||v();const{checked:i,unchecked:w,icon:y,"checked-icon":M,"unchecked-icon":x}=f,B=!(q(y)&&q(M)&&q(x));return c("div",{role:"switch","aria-checked":g,class:[`${e}-switch`,this.themeClass,B&&`${e}-switch--icon`,g&&`${e}-switch--active`,b&&`${e}-switch--disabled`,this.round&&`${e}-switch--round`,this.loading&&`${e}-switch--loading`,this.pressed&&`${e}-switch--pressed`,this.rubberBand&&`${e}-switch--rubber-band`],tabindex:this.mergedDisabled?void 0:0,style:this.cssVars,onClick:this.handleClick,onFocus:this.handleFocus,onBlur:this.handleBlur,onKeyup:this.handleKeyup,onKeydown:this.handleKeydown},c("div",{class:`${e}-switch__rail`,"aria-hidden":"true",style:h},F(i,d=>F(w,p=>d||p?c("div",{"aria-hidden":!0,class:`${e}-switch__children-placeholder`},c("div",{class:`${e}-switch__rail-placeholder`},c("div",{class:`${e}-switch__button-placeholder`}),d),c("div",{class:`${e}-switch__rail-placeholder`},c("div",{class:`${e}-switch__button-placeholder`}),p)):null)),c("div",{class:`${e}-switch__button`},F(y,d=>F(M,p=>F(x,V=>c(ke,null,{default:()=>this.loading?c(Ce,Object.assign({key:"loading",clsPrefix:e,strokeWidth:20},this.spinProps)):this.checked&&(p||d)?c("div",{class:`${e}-switch__button-icon`,key:p?"checked-icon":"icon"},p||d):!this.checked&&(V||d)?c("div",{class:`${e}-switch__button-icon`,key:V?"unchecked-icon":"icon"},V||d):null})))),F(i,d=>d&&c("div",{key:"checked",class:`${e}-switch__checked`},d)),F(w,d=>d&&c("div",{key:"unchecked",class:`${e}-switch__unchecked`},d)))))}}),Xe={style:{padding:"24px"}},nt=ne({__name:"MenuView",setup(e){const b=m([]),g=m([]),h=m(!1),v=m(!1),f=m(null),i=m({name:"",parentId:null,sortOrder:0,path:"",component:"",icon:"",menuType:"M",perms:"",visible:!0}),w=[{label:"目录 (M)",value:"M"},{label:"菜单 (C)",value:"C"},{label:"按钮 (F)",value:"F"}],y=m([]);function M(a,t=""){for(const l of a)l.menuType!=="F"&&(y.value.push({label:t+l.name,value:l.id}),l.children&&M(l.children,t+"　"))}const x=[{title:"名称",key:"name",width:160},{title:"图标",key:"icon",width:80},{title:"类型",key:"menuType",width:80,render:a=>c(Fe,{type:a.menuType==="M"?"info":a.menuType==="C"?"success":"default",size:"small"},{default:()=>({M:"目录",C:"菜单",F:"按钮"})[a.menuType]||a.menuType})},{title:"路由",key:"path",width:140},{title:"组件",key:"component",width:140},{title:"权限标识",key:"perms",width:160},{title:"排序",key:"sortOrder",width:60},{title:"可见",key:"visible",width:60,render:a=>a.visible?"是":"否"},{title:"操作",key:"actions",width:140,render:a=>c(ie,null,{default:()=>[c(j,{size:"small",onClick:()=>p(a)},{default:()=>"编辑"}),c(Ie,{onPositiveClick:()=>O(a.id)},{trigger:()=>c(j,{size:"small",type:"error"},{default:()=>"删除"}),default:()=>"确认删除?"})]})}];async function B(){h.value=!0;const a=await U.get("/api/menus/tree");b.value=a.data;const t=await U.get("/api/menus");g.value=t.data,y.value=[{label:"无 (根目录)",value:null}],M(a.data),h.value=!1}function d(){f.value=null,i.value={name:"",parentId:null,sortOrder:0,path:"",component:"",icon:"",menuType:"M",perms:"",visible:!0},v.value=!0}function p(a){f.value=a,i.value={name:a.name,parentId:a.parentId,sortOrder:a.sortOrder,path:a.path||"",component:a.component||"",icon:a.icon||"",menuType:a.menuType,perms:a.perms||"",visible:a.visible},v.value=!0}async function V(){f.value?await U.put(`/api/menus/${f.value.id}`,i.value):await U.post("/api/menus",i.value),v.value=!1,await B()}async function O(a){await U.delete(`/api/menus/${a}`),await B()}return ze(B),(a,t)=>(_e(),Me(o(Ve),null,{default:u(()=>[Te("div",Xe,[r(o(je),{title:"菜单管理"},{"header-extra":u(()=>[r(o(j),{type:"primary",size:"small",onClick:d},{default:u(()=>[...t[11]||(t[11]=[J("新增菜单",-1)])]),_:1})]),default:u(()=>[r(o(Oe),{columns:x,data:b.value,loading:h.value,"default-expand-all":"","row-key":l=>l.id,"children-key":"children"},null,8,["data","loading","row-key"]),r(o(We),{show:v.value,"onUpdate:show":t[10]||(t[10]=l=>v.value=l),title:f.value?"编辑菜单":"新增菜单",style:{width:"600px"}},{footer:u(()=>[r(o(ie),{justify:"end"},{default:u(()=>[r(o(j),{onClick:t[9]||(t[9]=l=>v.value=!1)},{default:u(()=>[...t[12]||(t[12]=[J("取消",-1)])]),_:1}),r(o(j),{type:"primary",onClick:V},{default:u(()=>[...t[13]||(t[13]=[J("保存",-1)])]),_:1})]),_:1})]),default:u(()=>[r(o(Pe),{"label-placement":"left","label-width":"80"},{default:u(()=>[r(o(S),{label:"名称"},{default:u(()=>[r(o(I),{value:i.value.name,"onUpdate:value":t[0]||(t[0]=l=>i.value.name=l)},null,8,["value"])]),_:1}),r(o(S),{label:"类型"},{default:u(()=>[r(o(le),{value:i.value.menuType,"onUpdate:value":t[1]||(t[1]=l=>i.value.menuType=l),options:w},null,8,["value"])]),_:1}),r(o(S),{label:"上级菜单"},{default:u(()=>[r(o(le),{value:i.value.parentId,"onUpdate:value":t[2]||(t[2]=l=>i.value.parentId=l),options:y.value},null,8,["value","options"])]),_:1}),r(o(S),{label:"路由路径"},{default:u(()=>[r(o(I),{value:i.value.path,"onUpdate:value":t[3]||(t[3]=l=>i.value.path=l),placeholder:"/example"},null,8,["value"])]),_:1}),r(o(S),{label:"组件名称"},{default:u(()=>[r(o(I),{value:i.value.component,"onUpdate:value":t[4]||(t[4]=l=>i.value.component=l),placeholder:"ExampleView"},null,8,["value"])]),_:1}),r(o(S),{label:"图标"},{default:u(()=>[r(o(I),{value:i.value.icon,"onUpdate:value":t[5]||(t[5]=l=>i.value.icon=l),placeholder:"icon-name"},null,8,["value"])]),_:1}),r(o(S),{label:"权限标识"},{default:u(()=>[r(o(I),{value:i.value.perms,"onUpdate:value":t[6]||(t[6]=l=>i.value.perms=l),placeholder:"system:example:list"},null,8,["value"])]),_:1}),r(o(S),{label:"排序"},{default:u(()=>[r(o(Ue),{value:i.value.sortOrder,"onUpdate:value":t[7]||(t[7]=l=>i.value.sortOrder=l),min:0},null,8,["value"])]),_:1}),r(o(S),{label:"可见"},{default:u(()=>[r(o(Ee),{value:i.value.visible,"onUpdate:value":t[8]||(t[8]=l=>i.value.visible=l)},null,8,["value"])]),_:1})]),_:1})]),_:1},8,["show","title"])]),_:1})])]),_:1}))}});export{nt as default};
