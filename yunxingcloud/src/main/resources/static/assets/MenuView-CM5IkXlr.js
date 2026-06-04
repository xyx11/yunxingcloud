import{ac as ye,am as ke,af as xe,c as ee,b as s,an as te,Z as X,d as x,a as ae,e as ie,ao as Y,h as c,W as _,ap as Ce,a4 as Se,u as Be,f as oe,aq as Ne,p as m,ar as $e,i as ze,j as K,as as Z,a2 as M,K as G,at as C,U as Re,o as Ve,l as Te,w as u,m as o,N as Fe,r as j,t as _e,v as r,B as I,A as J,aa as Me,s as Oe}from"./index-CHoUe_2n.js";import{N as Ue,a as le}from"./DataTable-Bh8_8Hm6.js";import{N as Pe}from"./Modal-uzYIuZqw.js";import{N as je,a as S}from"./FormItem-D5eK--w_.js";import{N as D}from"./Input-2RqAz000.js";import{N as Ie}from"./InputNumber-BISnkfsR.js";import{N as ne}from"./Space-ClCUP69q.js";import{N as De}from"./Card-DyIhfc6P.js";import{N as We}from"./Popconfirm-DhFiAE4W.js";import"./Checkbox-CdcVTFMr.js";import"./Success-BgR5vk-b.js";function Ke(e){const{primaryColor:b,opacityDisabled:g,borderRadius:h,textColor3:v}=e;return Object.assign(Object.assign({},ke),{iconColor:v,textColor:"white",loadingColor:b,opacityDisabled:g,railColor:"rgba(0, 0, 0, .14)",railColorActive:b,buttonBoxShadow:"0 1px 4px 0 rgba(0, 0, 0, 0.3), inset 0 0 1px 0 rgba(0, 0, 0, 0.05)",buttonColor:"#FFF",railBorderRadiusSmall:h,railBorderRadiusMedium:h,railBorderRadiusLarge:h,buttonBorderRadiusSmall:h,buttonBorderRadiusMedium:h,buttonBorderRadiusLarge:h,boxShadowFocus:`0 0 0 2px ${xe(b,{alpha:.2})}`})}const Ae={common:ye,self:Ke},He=ee("switch",`
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
 `),X("&:focus",[s("rail",`
 box-shadow: var(--n-box-shadow-focus);
 `)]),x("round",[s("rail","border-radius: calc(var(--n-rail-height) / 2);",[s("button","border-radius: calc(var(--n-button-height) / 2);")])]),ae("disabled",[ae("icon",[x("rubber-band",[x("pressed",[s("rail",[s("button","max-width: var(--n-button-width-pressed);")])]),s("rail",[X("&:active",[s("button","max-width: var(--n-button-width-pressed);")])]),x("active",[x("pressed",[s("rail",[s("button","left: calc(100% - var(--n-offset) - var(--n-button-width-pressed));")])]),s("rail",[X("&:active",[s("button","left: calc(100% - var(--n-offset) - var(--n-button-width-pressed));")])])])])])]),x("active",[s("rail",[s("button","left: calc(100% - var(--n-button-width) - var(--n-offset))")])]),s("rail",`
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
 `)]),x("active",[s("rail","background-color: var(--n-rail-color-active);")]),x("loading",[s("rail",`
 cursor: wait;
 `)]),x("disabled",[s("rail",`
 cursor: not-allowed;
 opacity: .5;
 `)])]),Ee=Object.assign(Object.assign({},oe.props),{size:String,value:{type:[String,Number,Boolean],default:void 0},loading:Boolean,defaultValue:{type:[String,Number,Boolean],default:!1},disabled:{type:Boolean,default:void 0},round:{type:Boolean,default:!0},"onUpdate:value":[Function,Array],onUpdateValue:[Function,Array],checkedValue:{type:[String,Number,Boolean],default:!0},uncheckedValue:{type:[String,Number,Boolean],default:!1},railStyle:Function,rubberBand:{type:Boolean,default:!0},spinProps:Object,onChange:[Function,Array]});let W;const Le=ie({name:"Switch",props:Ee,slots:Object,setup(e){W===void 0&&(typeof CSS<"u"?typeof CSS.supports<"u"?W=CSS.supports("width","max(1px)"):W=!1:W=!0);const{mergedClsPrefixRef:b,inlineThemeDisabled:g,mergedComponentPropsRef:h}=Be(e),v=oe("Switch","-switch",He,Ae,e,b),f=Ne(e,{mergedSize(i){var N,$;if(e.size!==void 0)return e.size;if(i)return i.mergedSize.value;const F=($=(N=h==null?void 0:h.value)===null||N===void 0?void 0:N.Switch)===null||$===void 0?void 0:$.size;return F||"medium"}}),{mergedSizeRef:n,mergedDisabledRef:w}=f,y=m(e.defaultValue),V=Re(e,"value"),k=$e(V,y),B=K(()=>k.value===e.checkedValue),d=m(!1),p=m(!1),T=K(()=>{const{railStyle:i}=e;if(i)return i({focused:p.value,checked:B.value})});function O(i){const{"onUpdate:value":N,onChange:$,onUpdateValue:F}=e,{nTriggerFormInput:A,nTriggerFormChange:H}=f;N&&Z(N,i),F&&Z(F,i),$&&Z($,i),y.value=i,A(),H()}function a(){const{nTriggerFormFocus:i}=f;i()}function t(){const{nTriggerFormBlur:i}=f;i()}function l(){e.loading||w.value||(k.value!==e.checkedValue?O(e.checkedValue):O(e.uncheckedValue))}function re(){p.value=!0,a()}function se(){p.value=!1,t(),d.value=!1}function de(i){e.loading||w.value||i.key===" "&&(k.value!==e.checkedValue?O(e.checkedValue):O(e.uncheckedValue),d.value=!1)}function ue(i){e.loading||w.value||i.key===" "&&(i.preventDefault(),d.value=!0)}const Q=K(()=>{const{value:i}=n,{self:{opacityDisabled:N,railColor:$,railColorActive:F,buttonBoxShadow:A,buttonColor:H,boxShadowFocus:ce,loadingColor:he,textColor:ve,iconColor:fe,[M("buttonHeight",i)]:z,[M("buttonWidth",i)]:pe,[M("buttonWidthPressed",i)]:be,[M("railHeight",i)]:R,[M("railWidth",i)]:P,[M("railBorderRadius",i)]:me,[M("buttonBorderRadius",i)]:ge},common:{cubicBezierEaseInOut:we}}=v.value;let E,L,q;return W?(E=`calc((${R} - ${z}) / 2)`,L=`max(${R}, ${z})`,q=`max(${P}, calc(${P} + ${z} - ${R}))`):(E=G((C(R)-C(z))/2),L=G(Math.max(C(R),C(z))),q=C(R)>C(z)?P:G(C(P)+C(z)-C(R))),{"--n-bezier":we,"--n-button-border-radius":ge,"--n-button-box-shadow":A,"--n-button-color":H,"--n-button-width":pe,"--n-button-width-pressed":be,"--n-button-height":z,"--n-height":L,"--n-offset":E,"--n-opacity-disabled":N,"--n-rail-border-radius":me,"--n-rail-color":$,"--n-rail-color-active":F,"--n-rail-height":R,"--n-rail-width":P,"--n-width":q,"--n-box-shadow-focus":ce,"--n-loading-color":he,"--n-text-color":ve,"--n-icon-color":fe}}),U=g?ze("switch",K(()=>n.value[0]),Q,e):void 0;return{handleClick:l,handleBlur:se,handleFocus:re,handleKeyup:de,handleKeydown:ue,mergedRailStyle:T,pressed:d,mergedClsPrefix:b,mergedValue:k,checked:B,mergedDisabled:w,cssVars:g?void 0:Q,themeClass:U==null?void 0:U.themeClass,onRender:U==null?void 0:U.onRender}},render(){const{mergedClsPrefix:e,mergedDisabled:b,checked:g,mergedRailStyle:h,onRender:v,$slots:f}=this;v==null||v();const{checked:n,unchecked:w,icon:y,"checked-icon":V,"unchecked-icon":k}=f,B=!(Y(y)&&Y(V)&&Y(k));return c("div",{role:"switch","aria-checked":g,class:[`${e}-switch`,this.themeClass,B&&`${e}-switch--icon`,g&&`${e}-switch--active`,b&&`${e}-switch--disabled`,this.round&&`${e}-switch--round`,this.loading&&`${e}-switch--loading`,this.pressed&&`${e}-switch--pressed`,this.rubberBand&&`${e}-switch--rubber-band`],tabindex:this.mergedDisabled?void 0:0,style:this.cssVars,onClick:this.handleClick,onFocus:this.handleFocus,onBlur:this.handleBlur,onKeyup:this.handleKeyup,onKeydown:this.handleKeydown},c("div",{class:`${e}-switch__rail`,"aria-hidden":"true",style:h},_(n,d=>_(w,p=>d||p?c("div",{"aria-hidden":!0,class:`${e}-switch__children-placeholder`},c("div",{class:`${e}-switch__rail-placeholder`},c("div",{class:`${e}-switch__button-placeholder`}),d),c("div",{class:`${e}-switch__rail-placeholder`},c("div",{class:`${e}-switch__button-placeholder`}),p)):null)),c("div",{class:`${e}-switch__button`},_(y,d=>_(V,p=>_(k,T=>c(Ce,null,{default:()=>this.loading?c(Se,Object.assign({key:"loading",clsPrefix:e,strokeWidth:20},this.spinProps)):this.checked&&(p||d)?c("div",{class:`${e}-switch__button-icon`,key:p?"checked-icon":"icon"},p||d):!this.checked&&(T||d)?c("div",{class:`${e}-switch__button-icon`,key:T?"unchecked-icon":"icon"},T||d):null})))),_(n,d=>d&&c("div",{key:"checked",class:`${e}-switch__checked`},d)),_(w,d=>d&&c("div",{key:"unchecked",class:`${e}-switch__unchecked`},d)))))}}),qe={style:{padding:"24px"}},it=ie({__name:"MenuView",setup(e){const b=m([]),g=m([]),h=m(!1),v=m(!1),f=m(null),n=m({name:"",parentId:null,sortOrder:0,path:"",component:"",icon:"",menuType:"M",perms:"",visible:!0}),w=[{label:"目录 (M)",value:"M"},{label:"菜单 (C)",value:"C"},{label:"按钮 (F)",value:"F"}],y=m([]);function V(a,t=""){for(const l of a)l.menuType!=="F"&&(y.value.push({label:t+l.name,value:l.id}),l.children&&V(l.children,t+"　"))}const k=[{title:"名称",key:"name",width:160},{title:"图标",key:"icon",width:80},{title:"类型",key:"menuType",width:80,render:a=>c(Me,{type:a.menuType==="M"?"info":a.menuType==="C"?"success":"default",size:"small"},{default:()=>({M:"目录",C:"菜单",F:"按钮"})[a.menuType]||a.menuType})},{title:"路由",key:"path",width:140},{title:"组件",key:"component",width:140},{title:"权限标识",key:"perms",width:160},{title:"排序",key:"sortOrder",width:60},{title:"可见",key:"visible",width:60,render:a=>a.visible?"是":"否"},{title:"操作",key:"actions",width:140,render:a=>c(ne,null,{default:()=>[c(I,{size:"small",onClick:()=>p(a)},{default:()=>"编辑"}),c(We,{onPositiveClick:()=>O(a.id)},{trigger:()=>c(I,{size:"small",type:"error"},{default:()=>"删除"}),default:()=>"确认删除?"})]})}];async function B(){h.value=!0;const a=await j.get("/api/menus/tree");b.value=a.data;const t=await j.get("/api/menus");g.value=t.data,y.value=[{label:"无 (根目录)",value:null}],V(a.data),h.value=!1}function d(){f.value=null,n.value={name:"",parentId:null,sortOrder:0,path:"",component:"",icon:"",menuType:"M",perms:"",visible:!0},v.value=!0}function p(a){f.value=a,n.value={name:a.name,parentId:a.parentId,sortOrder:a.sortOrder,path:a.path||"",component:a.component||"",icon:a.icon||"",menuType:a.menuType,perms:a.perms||"",visible:a.visible},v.value=!0}async function T(){f.value?await j.put(`/api/menus/${f.value.id}`,n.value):await j.post("/api/menus",n.value),v.value=!1,await B()}async function O(a){await j.delete(`/api/menus/${a}`),await B()}return Ve(B),(a,t)=>(Oe(),Te(o(Fe),null,{default:u(()=>[_e("div",qe,[r(o(De),{title:"菜单管理"},{"header-extra":u(()=>[r(o(I),{type:"primary",size:"small",onClick:d},{default:u(()=>[...t[11]||(t[11]=[J("新增菜单",-1)])]),_:1})]),default:u(()=>[r(o(Ue),{columns:k,data:b.value,loading:h.value,pagination:{pageSize:10},"default-expand-all":"","row-key":l=>l.id,"children-key":"children"},null,8,["data","loading","row-key"]),r(o(Pe),{show:v.value,"onUpdate:show":t[10]||(t[10]=l=>v.value=l),title:f.value?"编辑菜单":"新增菜单",style:{width:"600px"}},{footer:u(()=>[r(o(ne),{justify:"end"},{default:u(()=>[r(o(I),{onClick:t[9]||(t[9]=l=>v.value=!1)},{default:u(()=>[...t[12]||(t[12]=[J("取消",-1)])]),_:1}),r(o(I),{type:"primary",onClick:T},{default:u(()=>[...t[13]||(t[13]=[J("保存",-1)])]),_:1})]),_:1})]),default:u(()=>[r(o(je),{"label-placement":"left","label-width":"80"},{default:u(()=>[r(o(S),{label:"名称"},{default:u(()=>[r(o(D),{value:n.value.name,"onUpdate:value":t[0]||(t[0]=l=>n.value.name=l)},null,8,["value"])]),_:1}),r(o(S),{label:"类型"},{default:u(()=>[r(o(le),{value:n.value.menuType,"onUpdate:value":t[1]||(t[1]=l=>n.value.menuType=l),options:w},null,8,["value"])]),_:1}),r(o(S),{label:"上级菜单"},{default:u(()=>[r(o(le),{value:n.value.parentId,"onUpdate:value":t[2]||(t[2]=l=>n.value.parentId=l),options:y.value},null,8,["value","options"])]),_:1}),r(o(S),{label:"路由路径"},{default:u(()=>[r(o(D),{value:n.value.path,"onUpdate:value":t[3]||(t[3]=l=>n.value.path=l),placeholder:"/example"},null,8,["value"])]),_:1}),r(o(S),{label:"组件名称"},{default:u(()=>[r(o(D),{value:n.value.component,"onUpdate:value":t[4]||(t[4]=l=>n.value.component=l),placeholder:"ExampleView"},null,8,["value"])]),_:1}),r(o(S),{label:"图标"},{default:u(()=>[r(o(D),{value:n.value.icon,"onUpdate:value":t[5]||(t[5]=l=>n.value.icon=l),placeholder:"icon-name"},null,8,["value"])]),_:1}),r(o(S),{label:"权限标识"},{default:u(()=>[r(o(D),{value:n.value.perms,"onUpdate:value":t[6]||(t[6]=l=>n.value.perms=l),placeholder:"system:example:list"},null,8,["value"])]),_:1}),r(o(S),{label:"排序"},{default:u(()=>[r(o(Ie),{value:n.value.sortOrder,"onUpdate:value":t[7]||(t[7]=l=>n.value.sortOrder=l),min:0},null,8,["value"])]),_:1}),r(o(S),{label:"可见"},{default:u(()=>[r(o(Le),{value:n.value.visible,"onUpdate:value":t[8]||(t[8]=l=>n.value.visible=l)},null,8,["value"])]),_:1})]),_:1})]),_:1},8,["show","title"])]),_:1})])]),_:1}))}});export{it as default};
