import{a9 as v,c as t,a as te,d as N,b as U,aW as oe,bO as re,e as K,Q as le,h as p,bL as ne,u as se,f as Z,i as ie,j as G,ag as ae,bT as de,af as H,o as ce,r as pe,z as D,v as d,w as s,m as o,p as q,l as j,A as m,D as w,a8 as X,x as be,F as ue,C as ge,B as k,ah as me,s as y}from"./index-Brdb4NIO.js";import{u as he}from"./useNotify-m8QtYtte.js";import{g as fe,N as M}from"./Space-DizjvrNX.js";import{N as E}from"./Card-kqY1HZ0y.js";import{N as ve}from"./Code-BGFO1O3U.js";function Y(a,u="default",g=[]){const{children:l}=a;if(l!==null&&typeof l=="object"&&!Array.isArray(l)){const c=l[u];if(typeof c=="function")return c()}return g}const ye=v([t("descriptions",{fontSize:"var(--n-font-size)"},[t("descriptions-separator",`
 display: inline-block;
 margin: 0 8px 0 2px;
 `),t("descriptions-table-wrapper",[t("descriptions-table",[t("descriptions-table-row",[t("descriptions-table-header",{padding:"var(--n-th-padding)"}),t("descriptions-table-content",{padding:"var(--n-td-padding)"})])])]),te("bordered",[t("descriptions-table-wrapper",[t("descriptions-table",[t("descriptions-table-row",[v("&:last-child",[t("descriptions-table-content",{paddingBottom:0})])])])])]),N("left-label-placement",[t("descriptions-table-content",[v("> *",{verticalAlign:"top"})])]),N("left-label-align",[v("th",{textAlign:"left"})]),N("center-label-align",[v("th",{textAlign:"center"})]),N("right-label-align",[v("th",{textAlign:"right"})]),N("bordered",[t("descriptions-table-wrapper",`
 border-radius: var(--n-border-radius);
 overflow: hidden;
 background: var(--n-merged-td-color);
 border: 1px solid var(--n-merged-border-color);
 `,[t("descriptions-table",[t("descriptions-table-row",[v("&:not(:last-child)",[t("descriptions-table-content",{borderBottom:"1px solid var(--n-merged-border-color)"}),t("descriptions-table-header",{borderBottom:"1px solid var(--n-merged-border-color)"})]),t("descriptions-table-header",`
 font-weight: 400;
 background-clip: padding-box;
 background-color: var(--n-merged-th-color);
 `,[v("&:not(:last-child)",{borderRight:"1px solid var(--n-merged-border-color)"})]),t("descriptions-table-content",[v("&:not(:last-child)",{borderRight:"1px solid var(--n-merged-border-color)"})])])])])]),t("descriptions-header",`
 font-weight: var(--n-th-font-weight);
 font-size: 18px;
 transition: color .3s var(--n-bezier);
 line-height: var(--n-line-height);
 margin-bottom: 16px;
 color: var(--n-title-text-color);
 `),t("descriptions-table-wrapper",`
 transition:
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 `,[t("descriptions-table",`
 width: 100%;
 border-collapse: separate;
 border-spacing: 0;
 box-sizing: border-box;
 `,[t("descriptions-table-row",`
 box-sizing: border-box;
 transition: border-color .3s var(--n-bezier);
 `,[t("descriptions-table-header",`
 font-weight: var(--n-th-font-weight);
 line-height: var(--n-line-height);
 display: table-cell;
 box-sizing: border-box;
 color: var(--n-th-text-color);
 transition:
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 `),t("descriptions-table-content",`
 vertical-align: top;
 line-height: var(--n-line-height);
 display: table-cell;
 box-sizing: border-box;
 color: var(--n-td-text-color);
 transition:
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 `,[U("content",`
 transition: color .3s var(--n-bezier);
 display: inline-block;
 color: var(--n-td-text-color);
 `)]),U("label",`
 font-weight: var(--n-th-font-weight);
 transition: color .3s var(--n-bezier);
 display: inline-block;
 margin-right: 14px;
 color: var(--n-th-text-color);
 `)])])])]),t("descriptions-table-wrapper",`
 --n-merged-th-color: var(--n-th-color);
 --n-merged-td-color: var(--n-td-color);
 --n-merged-border-color: var(--n-border-color);
 `),oe(t("descriptions-table-wrapper",`
 --n-merged-th-color: var(--n-th-color-modal);
 --n-merged-td-color: var(--n-td-color-modal);
 --n-merged-border-color: var(--n-border-color-modal);
 `)),re(t("descriptions-table-wrapper",`
 --n-merged-th-color: var(--n-th-color-popover);
 --n-merged-td-color: var(--n-td-color-popover);
 --n-merged-border-color: var(--n-border-color-popover);
 `))]),ee="DESCRIPTION_ITEM_FLAG";function we(a){return typeof a=="object"&&a&&!Array.isArray(a)?a.type&&a.type[ee]:!1}const xe=Object.assign(Object.assign({},Z.props),{title:String,column:{type:Number,default:3},columns:Number,labelPlacement:{type:String,default:"top"},labelAlign:{type:String,default:"left"},separator:{type:String,default:":"},size:String,bordered:Boolean,labelClass:String,labelStyle:[Object,String],contentClass:String,contentStyle:[Object,String]}),Ce=K({name:"Descriptions",props:xe,slots:Object,setup(a){const{mergedClsPrefixRef:u,inlineThemeDisabled:g,mergedComponentPropsRef:l}=se(a),c=G(()=>{var e,i;return a.size||((i=(e=l==null?void 0:l.value)===null||e===void 0?void 0:e.Descriptions)===null||i===void 0?void 0:i.size)||"medium"}),h=Z("Descriptions","-descriptions",ye,de,a,u),x=G(()=>{const{bordered:e}=a,i=c.value,{common:{cubicBezierEaseInOut:C},self:{titleTextColor:r,thColor:B,thColorModal:$,thColorPopover:O,thTextColor:V,thFontWeight:Q,tdTextColor:F,tdColor:n,tdColorModal:_,tdColorPopover:W,borderColor:f,borderColorModal:S,borderColorPopover:P,borderRadius:R,lineHeight:z,[H("fontSize",i)]:T,[H(e?"thPaddingBordered":"thPadding",i)]:I,[H(e?"tdPaddingBordered":"tdPadding",i)]:A}}=h.value;return{"--n-title-text-color":r,"--n-th-padding":I,"--n-td-padding":A,"--n-font-size":T,"--n-bezier":C,"--n-th-font-weight":Q,"--n-line-height":z,"--n-th-text-color":V,"--n-td-text-color":F,"--n-th-color":B,"--n-th-color-modal":$,"--n-th-color-popover":O,"--n-td-color":n,"--n-td-color-modal":_,"--n-td-color-popover":W,"--n-border-radius":R,"--n-border-color":f,"--n-border-color-modal":S,"--n-border-color-popover":P}}),b=g?ie("descriptions",G(()=>{let e="";const{bordered:i}=a;return i&&(e+="a"),e+=c.value[0],e}),x,a):void 0;return{mergedClsPrefix:u,cssVars:g?void 0:x,themeClass:b==null?void 0:b.themeClass,onRender:b==null?void 0:b.onRender,compitableColumn:ae(a,["columns","column"]),inlineThemeDisabled:g,mergedSize:c}},render(){const a=this.$slots.default,u=a?le(a()):[];u.length;const{contentClass:g,labelClass:l,compitableColumn:c,labelPlacement:h,labelAlign:x,mergedSize:b,bordered:e,title:i,cssVars:C,mergedClsPrefix:r,separator:B,onRender:$}=this;$==null||$();const O=u.filter(n=>we(n)),V={span:0,row:[],secondRow:[],rows:[]},F=O.reduce((n,_,W)=>{const f=_.props||{},S=O.length-1===W,P=["label"in f?f.label:Y(_,"label")],R=[Y(_)],z=f.span||1,T=n.span;n.span+=z;const I=f.labelStyle||f["label-style"]||this.labelStyle,A=f.contentStyle||f["content-style"]||this.contentStyle;if(h==="left")e?n.row.push(p("th",{class:[`${r}-descriptions-table-header`,l],colspan:1,style:I},P),p("td",{class:[`${r}-descriptions-table-content`,g],colspan:S?(c-T)*2+1:z*2-1,style:A},R)):n.row.push(p("td",{class:`${r}-descriptions-table-content`,colspan:S?(c-T)*2:z*2},p("span",{class:[`${r}-descriptions-table-content__label`,l],style:I},[...P,B&&p("span",{class:`${r}-descriptions-separator`},B)]),p("span",{class:[`${r}-descriptions-table-content__content`,g],style:A},R)));else{const J=S?(c-T)*2:z*2;n.row.push(p("th",{class:[`${r}-descriptions-table-header`,l],colspan:J,style:I},P)),n.secondRow.push(p("td",{class:[`${r}-descriptions-table-content`,g],colspan:J,style:A},R))}return(n.span>=c||S)&&(n.span=0,n.row.length&&(n.rows.push(n.row),n.row=[]),h!=="left"&&n.secondRow.length&&(n.rows.push(n.secondRow),n.secondRow=[])),n},V).rows.map(n=>p("tr",{class:`${r}-descriptions-table-row`},n));return p("div",{style:C,class:[`${r}-descriptions`,this.themeClass,`${r}-descriptions--${h}-label-placement`,`${r}-descriptions--${x}-label-align`,`${r}-descriptions--${b}-size`,e&&`${r}-descriptions--bordered`]},i||this.$slots.header?p("div",{class:`${r}-descriptions-header`},i||fe(this,"header")):null,p("div",{class:`${r}-descriptions-table-wrapper`},p("table",{class:`${r}-descriptions-table`},p("tbody",null,h==="top"&&p("tr",{class:`${r}-descriptions-table-row`,style:{visibility:"collapse"}},ne(c*2,p("td",null))),F))))}}),Se={label:String,span:{type:Number,default:1},labelClass:String,labelStyle:[Object,String],contentClass:String,contentStyle:[Object,String]},L=K({name:"DescriptionsItem",[ee]:!0,props:Se,slots:Object,render(){return null}}),ze={style:{padding:"24px","max-width":"800px"}},ke={key:1,style:{color:"#999"}},$e={key:1,style:{color:"#999"}},Ae=K({__name:"ProfileView",setup(a){const u=me(),g=he(),l=q(null),c=q(!1),h=q("");ce(async()=>{const b=await pe.get("/api/user");l.value=b.data;const e=localStorage.getItem("accessToken")||"";h.value=e?e.substring(0,40)+"...":""});function x(){const b=localStorage.getItem("accessToken")||"";navigator.clipboard.writeText(b).then(()=>g.success("Token 已复制到剪贴板"))}return(b,e)=>(y(),D("div",ze,[d(o(E),{title:"个人中心"},{default:s(()=>[l.value?(y(),j(o(Ce),{key:0,bordered:"",column:2,"label-placement":"left"},{default:s(()=>[d(o(L),{label:"用户名"},{default:s(()=>[m(w(l.value.username),1)]),_:1}),d(o(L),{label:"昵称"},{default:s(()=>[m(w(l.value.nickname||"-"),1)]),_:1}),d(o(L),{label:"邮箱"},{default:s(()=>[m(w(l.value.email||"-"),1)]),_:1}),d(o(L),{label:"注册来源"},{default:s(()=>[d(o(X),{type:l.value.registerSource==="local"?"info":"success",size:"small"},{default:s(()=>[m(w(l.value.registerSource==="local"?"本地注册":l.value.registerSource),1)]),_:1},8,["type"])]),_:1})]),_:1})):be("",!0)]),_:1}),d(o(E),{title:"权限列表",style:{"margin-top":"16px"}},{default:s(()=>{var i,C;return[(C=(i=l.value)==null?void 0:i.authorities)!=null&&C.length?(y(),j(o(M),{key:0},{default:s(()=>[(y(!0),D(ue,null,ge(l.value.authorities,r=>(y(),j(o(X),{key:r,type:r.startsWith("ROLE_")?"warning":"info",size:"small"},{default:s(()=>[m(w(r),1)]),_:2},1032,["type"]))),128))]),_:1})):(y(),D("span",ke,"暂无权限数据"))]}),_:1}),d(o(E),{title:"Token 管理",style:{"margin-top":"16px"}},{default:s(()=>[d(o(M),{vertical:""},{default:s(()=>[d(o(M),{align:"center"},{default:s(()=>[c.value?(y(),j(o(ve),{key:0,code:b.localStorage.getItem("accessToken")||"",language:"text",style:{"max-width":"600px"}},null,8,["code"])):(y(),D("span",$e,w(h.value),1)),d(o(k),{size:"small",onClick:e[0]||(e[0]=i=>c.value=!c.value)},{default:s(()=>[m(w(c.value?"隐藏":"查看"),1)]),_:1}),d(o(k),{size:"small",onClick:x},{default:s(()=>[...e[5]||(e[5]=[m("复制",-1)])]),_:1})]),_:1})]),_:1})]),_:1}),d(o(E),{title:"快捷操作",style:{"margin-top":"16px"}},{default:s(()=>[d(o(M),null,{default:s(()=>[d(o(k),{size:"small",onClick:e[1]||(e[1]=i=>o(u).push("/"))},{default:s(()=>[...e[6]||(e[6]=[m("📊 仪表盘",-1)])]),_:1}),d(o(k),{size:"small",onClick:e[2]||(e[2]=i=>o(u).push("/swagger"))},{default:s(()=>[...e[7]||(e[7]=[m("📄 API 文档",-1)])]),_:1}),d(o(k),{size:"small",onClick:e[3]||(e[3]=i=>b.window.open("/actuator/health"))},{default:s(()=>[...e[8]||(e[8]=[m("❤️ 健康检查",-1)])]),_:1}),d(o(k),{size:"small",onClick:e[4]||(e[4]=i=>o(u).push("/operlog"))},{default:s(()=>[...e[9]||(e[9]=[m("📋 操作日志",-1)])]),_:1})]),_:1})]),_:1})]))}});export{Ae as default};
