import{E as he,j as C,G as me,p as P,H as ae,I as ge,e as V,h as x,J as xe,K as T,L as Se,M as ee,V as ye,u as K,O as j,o as le,P as Re,Q as Ce,R as te,S as we,T as _e,U as re,c as E,b as F,W as L,f as U,X as ne,i as se,Y as ze,Z as c,d as W,a as $e,$ as Ne,a0 as ke,a1 as Be,a2 as Y,r as Q,z as Ee,v as b,w as f,m as h,A as X,D as q,t as B,s as Ge}from"./index-CHoUe_2n.js";import{g as Pe,N as Te}from"./Space-ClCUP69q.js";import{N as Ae}from"./Code-DskFhJIQ.js";import{N as D}from"./Card-DyIhfc6P.js";function De(t){if(typeof t=="number")return{"":t.toString()};const e={};return t.split(/ +/).forEach(n=>{if(n==="")return;const[l,o]=n.split(":");o===void 0?e[""]=l:e[l]=o}),e}function G(t,e){var n;if(t==null)return;const l=De(t);if(e===void 0)return l[""];if(typeof e=="string")return(n=l[e])!==null&&n!==void 0?n:l[""];if(Array.isArray(e)){for(let o=e.length-1;o>=0;--o){const a=e[o];if(a in l)return l[a]}return l[""]}else{let o,a=-1;return Object.keys(l).forEach(r=>{const s=Number(r);!Number.isNaN(s)&&e>=s&&s>=a&&(a=s,o=l[r])}),o}}const Oe={xs:0,s:640,m:1024,l:1280,xl:1536,"2xl":1920};function Ve(t){return`(min-width: ${t}px)`}const O={};function Me(t=Oe){if(!he)return C(()=>[]);if(typeof window.matchMedia!="function")return C(()=>[]);const e=P({}),n=Object.keys(t),l=(o,a)=>{o.matches?e.value[a]=!0:e.value[a]=!1};return n.forEach(o=>{const a=t[o];let r,s;O[a]===void 0?(r=window.matchMedia(Ve(a)),r.addEventListener?r.addEventListener("change",u=>{s.forEach(d=>{d(u,o)})}):r.addListener&&r.addListener(u=>{s.forEach(d=>{d(u,o)})}),s=new Set,O[a]={mql:r,cbs:s}):(r=O[a].mql,s=O[a].cbs),s.add(l),r.matches&&s.forEach(u=>{u(r,o)})}),me(()=>{n.forEach(o=>{const{cbs:a}=O[t[o]];a.has(l)&&a.delete(l)})}),C(()=>{const{value:o}=e;return n.filter(a=>o[a])})}function Ie(t){var e;const n=(e=t.dirs)===null||e===void 0?void 0:e.find(({dir:l})=>l===ae);return!!(n&&n.value===!1)}const oe=1,ie=ge("n-grid"),de=1,je={span:{type:[Number,String],default:de},offset:{type:[Number,String],default:0},suffix:Boolean,privateOffset:Number,privateSpan:Number,privateColStart:Number,privateShow:{type:Boolean,default:!0}},H=V({__GRID_ITEM__:!0,name:"GridItem",alias:["Gi"],props:je,setup(){const{isSsrRef:t,xGapRef:e,itemStyleRef:n,overflowRef:l,layoutShiftDisabledRef:o}=xe(ie),a=Se();return{overflow:l,itemStyle:n,layoutShiftDisabled:o,mergedXGap:C(()=>T(e.value||0)),deriveStyle:()=>{t.value;const{privateSpan:r=de,privateShow:s=!0,privateColStart:u=void 0,privateOffset:d=0}=a.vnode.props,{value:m}=e,g=T(m||0);return{display:s?"":"none",gridColumn:`${u??`span ${r}`} / span ${r}`,marginLeft:d?`calc((100% - (${r} - 1) * ${g}) / ${r} * ${d} + ${g} * ${d})`:""}}}},render(){var t,e;if(this.layoutShiftDisabled){const{span:n,offset:l,mergedXGap:o}=this;return x("div",{style:{gridColumn:`span ${n} / span ${n}`,marginLeft:l?`calc((100% - (${n} - 1) * ${o}) / ${n} * ${l} + ${o} * ${l})`:""}},this.$slots)}return x("div",{style:[this.itemStyle,this.deriveStyle()]},(e=(t=this.$slots).default)===null||e===void 0?void 0:e.call(t,{overflow:this.overflow}))}}),Fe={xs:0,s:640,m:1024,l:1280,xl:1536,xxl:1920},ue=24,Z="__ssr__",Le={layoutShiftDisabled:Boolean,responsive:{type:[String,Boolean],default:"self"},cols:{type:[Number,String],default:ue},itemResponsive:Boolean,collapsed:Boolean,collapsedRows:{type:Number,default:1},itemStyle:[Object,String],xGap:{type:[Number,String],default:0},yGap:{type:[Number,String],default:0}},We=V({name:"Grid",inheritAttrs:!1,props:Le,setup(t){const{mergedClsPrefixRef:e,mergedBreakpointsRef:n}=K(t),l=/^\d+$/,o=P(void 0),a=Me((n==null?void 0:n.value)||Fe),r=j(()=>!!(t.itemResponsive||!l.test(t.cols.toString())||!l.test(t.xGap.toString())||!l.test(t.yGap.toString()))),s=C(()=>{if(r.value)return t.responsive==="self"?o.value:a.value}),u=j(()=>{var v;return(v=Number(G(t.cols.toString(),s.value)))!==null&&v!==void 0?v:ue}),d=j(()=>G(t.xGap.toString(),s.value)),m=j(()=>G(t.yGap.toString(),s.value)),g=v=>{o.value=v.contentRect.width},S=v=>{we(g,v)},w=P(!1),_=C(()=>{if(t.responsive==="self")return S}),p=P(!1),y=P();return le(()=>{const{value:v}=y;v&&v.hasAttribute(Z)&&(v.removeAttribute(Z),p.value=!0)}),_e(ie,{layoutShiftDisabledRef:re(t,"layoutShiftDisabled"),isSsrRef:p,itemStyleRef:re(t,"itemStyle"),xGapRef:d,overflowRef:w}),{isSsr:!Re,contentEl:y,mergedClsPrefix:e,style:C(()=>t.layoutShiftDisabled?{width:"100%",display:"grid",gridTemplateColumns:`repeat(${t.cols}, minmax(0, 1fr))`,columnGap:T(t.xGap),rowGap:T(t.yGap)}:{width:"100%",display:"grid",gridTemplateColumns:`repeat(${u.value}, minmax(0, 1fr))`,columnGap:T(d.value),rowGap:T(m.value)}),isResponsive:r,responsiveQuery:s,responsiveCols:u,handleResize:_,overflow:w}},render(){if(this.layoutShiftDisabled)return x("div",ee({ref:"contentEl",class:`${this.mergedClsPrefix}-grid`,style:this.style},this.$attrs),this.$slots);const t=()=>{var e,n,l,o,a,r,s;this.overflow=!1;const u=Ce(Pe(this)),d=[],{collapsed:m,collapsedRows:g,responsiveCols:S,responsiveQuery:w}=this;u.forEach(i=>{var k,z,R,$,A;if(((k=i==null?void 0:i.type)===null||k===void 0?void 0:k.__GRID_ITEM__)!==!0)return;if(Ie(i)){const N=te(i);N.props?N.props.privateShow=!1:N.props={privateShow:!1},d.push({child:N,rawChildSpan:0});return}i.dirs=((z=i.dirs)===null||z===void 0?void 0:z.filter(({dir:N})=>N!==ae))||null,((R=i.dirs)===null||R===void 0?void 0:R.length)===0&&(i.dirs=null);const M=te(i),I=Number((A=G(($=M.props)===null||$===void 0?void 0:$.span,w))!==null&&A!==void 0?A:oe);I!==0&&d.push({child:M,rawChildSpan:I})});let _=0;const p=(e=d[d.length-1])===null||e===void 0?void 0:e.child;if(p!=null&&p.props){const i=(n=p.props)===null||n===void 0?void 0:n.suffix;i!==void 0&&i!==!1&&(_=Number((o=G((l=p.props)===null||l===void 0?void 0:l.span,w))!==null&&o!==void 0?o:oe),p.props.privateSpan=_,p.props.privateColStart=S+1-_,p.props.privateShow=(a=p.props.privateShow)!==null&&a!==void 0?a:!0)}let y=0,v=!1;for(const{child:i,rawChildSpan:k}of d){if(v&&(this.overflow=!0),!v){const z=Number((s=G((r=i.props)===null||r===void 0?void 0:r.offset,w))!==null&&s!==void 0?s:0),R=Math.min(k+z,S);if(i.props?(i.props.privateSpan=R,i.props.privateOffset=z):i.props={privateSpan:R,privateOffset:z},m){const $=y%S;R+$>S&&(y+=S-$),R+y+_>g*S?v=!0:y+=R}}v&&(i.props?i.props.privateShow!==!0&&(i.props.privateShow=!1):i.props={privateShow:!1})}return x("div",ee({ref:"contentEl",class:`${this.mergedClsPrefix}-grid`,style:this.style,[Z]:this.isSsr||void 0},this.$attrs),d.map(({child:i})=>i))};return this.isResponsive&&this.responsive==="self"?x(ye,{onResize:this.handleResize},{default:t}):t()}}),Qe=E("statistic",[F("label",`
 font-weight: var(--n-label-font-weight);
 transition: .3s color var(--n-bezier);
 font-size: var(--n-label-font-size);
 color: var(--n-label-text-color);
 `),E("statistic-value",`
 margin-top: 4px;
 font-weight: var(--n-value-font-weight);
 `,[F("prefix",`
 margin: 0 4px 0 0;
 font-size: var(--n-value-font-size);
 transition: .3s color var(--n-bezier);
 color: var(--n-value-prefix-text-color);
 `,[E("icon",{verticalAlign:"-0.125em"})]),F("content",`
 font-size: var(--n-value-font-size);
 transition: .3s color var(--n-bezier);
 color: var(--n-value-text-color);
 `),F("suffix",`
 margin: 0 0 0 4px;
 font-size: var(--n-value-font-size);
 transition: .3s color var(--n-bezier);
 color: var(--n-value-suffix-text-color);
 `,[E("icon",{verticalAlign:"-0.125em"})])])]),Xe=Object.assign(Object.assign({},U.props),{tabularNums:Boolean,label:String,value:[String,Number]}),J=V({name:"Statistic",props:Xe,slots:Object,setup(t){const{mergedClsPrefixRef:e,inlineThemeDisabled:n,mergedRtlRef:l}=K(t),o=U("Statistic","-statistic",Qe,ze,t,e),a=ne("Statistic",l,e),r=C(()=>{const{self:{labelFontWeight:u,valueFontSize:d,valueFontWeight:m,valuePrefixTextColor:g,labelTextColor:S,valueSuffixTextColor:w,valueTextColor:_,labelFontSize:p},common:{cubicBezierEaseInOut:y}}=o.value;return{"--n-bezier":y,"--n-label-font-size":p,"--n-label-font-weight":u,"--n-label-text-color":S,"--n-value-font-weight":m,"--n-value-font-size":d,"--n-value-prefix-text-color":g,"--n-value-suffix-text-color":w,"--n-value-text-color":_}}),s=n?se("statistic",void 0,r,t):void 0;return{rtlEnabled:a,mergedClsPrefix:e,cssVars:n?void 0:r,themeClass:s==null?void 0:s.themeClass,onRender:s==null?void 0:s.onRender}},render(){var t;const{mergedClsPrefix:e,$slots:{default:n,label:l,prefix:o,suffix:a}}=this;return(t=this.onRender)===null||t===void 0||t.call(this),x("div",{class:[`${e}-statistic`,this.themeClass,this.rtlEnabled&&`${e}-statistic--rtl`],style:this.cssVars},L(l,r=>x("div",{class:`${e}-statistic__label`},this.label||r)),x("div",{class:`${e}-statistic-value`,style:{fontVariantNumeric:this.tabularNums?"tabular-nums":""}},L(o,r=>r&&x("span",{class:`${e}-statistic-value__prefix`},r)),this.value!==void 0?x("span",{class:`${e}-statistic-value__content`},this.value):L(n,r=>r&&x("span",{class:`${e}-statistic-value__content`},r)),L(a,r=>r&&x("span",{class:`${e}-statistic-value__suffix`},r))))}}),qe=c([E("table",`
 font-size: var(--n-font-size);
 font-variant-numeric: tabular-nums;
 line-height: var(--n-line-height);
 width: 100%;
 border-radius: var(--n-border-radius) var(--n-border-radius) 0 0;
 text-align: left;
 border-collapse: separate;
 border-spacing: 0;
 overflow: hidden;
 background-color: var(--n-td-color);
 border-color: var(--n-merged-border-color);
 transition:
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier),
 color .3s var(--n-bezier);
 --n-merged-border-color: var(--n-border-color);
 `,[c("th",`
 white-space: nowrap;
 transition:
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier),
 color .3s var(--n-bezier);
 text-align: inherit;
 padding: var(--n-th-padding);
 vertical-align: inherit;
 text-transform: none;
 border: 0px solid var(--n-merged-border-color);
 font-weight: var(--n-th-font-weight);
 color: var(--n-th-text-color);
 background-color: var(--n-th-color);
 border-bottom: 1px solid var(--n-merged-border-color);
 border-right: 1px solid var(--n-merged-border-color);
 `,[c("&:last-child",`
 border-right: 0px solid var(--n-merged-border-color);
 `)]),c("td",`
 transition:
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier),
 color .3s var(--n-bezier);
 padding: var(--n-td-padding);
 color: var(--n-td-text-color);
 background-color: var(--n-td-color);
 border: 0px solid var(--n-merged-border-color);
 border-right: 1px solid var(--n-merged-border-color);
 border-bottom: 1px solid var(--n-merged-border-color);
 `,[c("&:last-child",`
 border-right: 0px solid var(--n-merged-border-color);
 `)]),W("bordered",`
 border: 1px solid var(--n-merged-border-color);
 border-radius: var(--n-border-radius);
 `,[c("tr",[c("&:last-child",[c("td",`
 border-bottom: 0 solid var(--n-merged-border-color);
 `)])])]),W("single-line",[c("th",`
 border-right: 0px solid var(--n-merged-border-color);
 `),c("td",`
 border-right: 0px solid var(--n-merged-border-color);
 `)]),W("single-column",[c("tr",[c("&:not(:last-child)",[c("td",`
 border-bottom: 0px solid var(--n-merged-border-color);
 `)])])]),W("striped",[c("tr:nth-of-type(even)",[c("td","background-color: var(--n-td-color-striped)")])]),$e("bottom-bordered",[c("tr",[c("&:last-child",[c("td",`
 border-bottom: 0px solid var(--n-merged-border-color);
 `)])])])]),Ne(E("table",`
 background-color: var(--n-td-color-modal);
 --n-merged-border-color: var(--n-border-color-modal);
 `,[c("th",`
 background-color: var(--n-th-color-modal);
 `),c("td",`
 background-color: var(--n-td-color-modal);
 `)])),ke(E("table",`
 background-color: var(--n-td-color-popover);
 --n-merged-border-color: var(--n-border-color-popover);
 `,[c("th",`
 background-color: var(--n-th-color-popover);
 `),c("td",`
 background-color: var(--n-td-color-popover);
 `)]))]),He=Object.assign(Object.assign({},U.props),{bordered:{type:Boolean,default:!0},bottomBordered:{type:Boolean,default:!0},singleLine:{type:Boolean,default:!0},striped:Boolean,singleColumn:Boolean,size:String}),Je=V({name:"Table",props:He,setup(t){const{mergedClsPrefixRef:e,inlineThemeDisabled:n,mergedRtlRef:l,mergedComponentPropsRef:o}=K(t),a=C(()=>{var m,g;return t.size||((g=(m=o==null?void 0:o.value)===null||m===void 0?void 0:m.Table)===null||g===void 0?void 0:g.size)||"medium"}),r=U("Table","-table",qe,Be,t,e),s=ne("Table",l,e),u=C(()=>{const m=a.value,{self:{borderColor:g,tdColor:S,tdColorModal:w,tdColorPopover:_,thColor:p,thColorModal:y,thColorPopover:v,thTextColor:i,tdTextColor:k,borderRadius:z,thFontWeight:R,lineHeight:$,borderColorModal:A,borderColorPopover:M,tdColorStriped:I,tdColorStripedModal:N,tdColorStripedPopover:ce,[Y("fontSize",m)]:fe,[Y("tdPadding",m)]:ve,[Y("thPadding",m)]:pe},common:{cubicBezierEaseInOut:be}}=r.value;return{"--n-bezier":be,"--n-td-color":S,"--n-td-color-modal":w,"--n-td-color-popover":_,"--n-td-text-color":k,"--n-border-color":g,"--n-border-color-modal":A,"--n-border-color-popover":M,"--n-border-radius":z,"--n-font-size":fe,"--n-th-color":p,"--n-th-color-modal":y,"--n-th-color-popover":v,"--n-th-font-weight":R,"--n-th-text-color":i,"--n-line-height":$,"--n-td-padding":ve,"--n-th-padding":pe,"--n-td-color-striped":I,"--n-td-color-striped-modal":N,"--n-td-color-striped-popover":ce}}),d=n?se("table",C(()=>a.value[0]),u,t):void 0;return{rtlEnabled:s,mergedClsPrefix:e,cssVars:n?void 0:u,themeClass:d==null?void 0:d.themeClass,onRender:d==null?void 0:d.onRender}},render(){var t;const{mergedClsPrefix:e}=this;return(t=this.onRender)===null||t===void 0||t.call(this),x("table",{class:[`${e}-table`,this.themeClass,{[`${e}-table--rtl`]:this.rtlEnabled,[`${e}-table--bottom-bordered`]:this.bottomBordered,[`${e}-table--bordered`]:this.bordered,[`${e}-table--single-line`]:this.singleLine,[`${e}-table--single-column`]:this.singleColumn,[`${e}-table--striped`]:this.striped}],style:this.cssVars},this.$slots)}}),et=V({__name:"DashboardView",setup(t){const e=P([{label:"用户数",value:0,color:"#667eea",icon:"👥"},{label:"部门数",value:0,color:"#07C160",icon:"🏢"},{label:"角色数",value:0,color:"#FF9800",icon:"🔑"},{label:"菜单数",value:0,color:"#F44336",icon:"📋"}]),n=[{key:1,name:"授权端点",path:"/oauth2/authorize",desc:"OAuth2 授权码流程"},{key:2,name:"Token 端点",path:"/oauth2/token",desc:"获取 access_token"},{key:3,name:"JWKS 端点",path:"/oauth2/jwks",desc:"RSA 公钥"},{key:4,name:"UserInfo",path:"/userinfo",desc:"OIDC 用户信息"},{key:5,name:"Discovery",path:"/.well-known/openid-configuration",desc:"OIDC 自动发现"}],l=[{title:"端点",key:"name",width:110},{title:"路径",key:"path",width:250,render:a=>x(Ae,{},{default:()=>a.path})},{title:"说明",key:"desc"}];le(async()=>{try{const[a,r,s,u]=await Promise.all([Q.get("/api/users").catch(()=>({data:[]})),Q.get("/api/departments").catch(()=>({data:[]})),Q.get("/api/roles").catch(()=>({data:[]})),Q.get("/api/menus").catch(()=>({data:[]}))]);e.value[0].value=Array.isArray(a.data)?a.data.length:0,e.value[1].value=o(r.data),e.value[2].value=Array.isArray(s.data)?s.data.length:0,e.value[3].value=Array.isArray(u.data)?u.data.length:0}catch{}});function o(a){let r=0;for(const s of a)r++,s.children&&(r+=o(s.children));return r}return(a,r)=>(Ge(),Ee("div",null,[b(h(We),{cols:"4","x-gap":"16","y-gap":"16",responsive:"screen","item-responsive":""},{default:f(()=>[b(h(H),{span:"4 m:2 l:1"},{default:f(()=>[b(h(D),{hoverable:""},{default:f(()=>[b(h(J),{label:e.value[0].label},{prefix:f(()=>[...r[0]||(r[0]=[B("span",{style:{"font-size":"24px"}},"👥",-1)])]),default:f(()=>[X(" "+q(e.value[0].value),1)]),_:1},8,["label"])]),_:1})]),_:1}),b(h(H),{span:"4 m:2 l:1"},{default:f(()=>[b(h(D),{hoverable:""},{default:f(()=>[b(h(J),{label:e.value[1].label},{prefix:f(()=>[...r[1]||(r[1]=[B("span",{style:{"font-size":"24px"}},"🏢",-1)])]),default:f(()=>[X(" "+q(e.value[1].value),1)]),_:1},8,["label"])]),_:1})]),_:1}),b(h(H),{span:"4 m:2 l:1"},{default:f(()=>[b(h(D),{hoverable:""},{default:f(()=>[b(h(J),{label:e.value[2].label},{prefix:f(()=>[...r[2]||(r[2]=[B("span",{style:{"font-size":"24px"}},"🔑",-1)])]),default:f(()=>[X(" "+q(e.value[2].value),1)]),_:1},8,["label"])]),_:1})]),_:1}),b(h(H),{span:"4 m:2 l:1"},{default:f(()=>[b(h(D),{hoverable:""},{default:f(()=>[b(h(J),{label:e.value[3].label},{prefix:f(()=>[...r[3]||(r[3]=[B("span",{style:{"font-size":"24px"}},"📋",-1)])]),default:f(()=>[X(" "+q(e.value[3].value),1)]),_:1},8,["label"])]),_:1})]),_:1})]),_:1}),b(h(D),{title:"系统信息",style:{"margin-top":"16px"}},{default:f(()=>[b(h(Te),{vertical:""},{default:f(()=>[...r[4]||(r[4]=[B("div",{style:{color:"#666","font-size":"13px"}},"版本: yunxingcloud v1.0.0",-1),B("div",{style:{color:"#666","font-size":"13px"}},"架构: Spring Boot 4.0 + OAuth2 + JPA + Vue 3",-1),B("div",{style:{color:"#666","font-size":"13px"}},"OAuth2 / OIDC 端点列表:",-1)])]),_:1}),b(h(Je),{columns:l,data:n,style:{"margin-top":"8px"},size:"small"})]),_:1})]))}});export{et as default};
