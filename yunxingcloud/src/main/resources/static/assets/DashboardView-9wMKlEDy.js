import{E as ge,j as _,G as xe,p as G,H as ae,d as ne,I as $,J as Se,f as D,h as S,K as Ce,L as M,M as ye,O as te,V as Re,u as ee,P as F,o as se,Q as we,R as _e,S as oe,T as ze,U as $e,W as re,c as B,b as L,X as W,g as Y,Y as ie,i as de,Z as c,e as H,a as Ne,$ as Pe,a0 as ke,a1 as Z,r as Q,z as Te,v as m,w as f,m as g,A as X,D as q,t as T,s as Be}from"./index-Bn6z-r_K.js";import{g as Ee,N as Ge}from"./Space-BJe-sY-b.js";import{N as Me}from"./Code-nDfY-Vbu.js";import{N as O}from"./Card-BGV--IU_.js";function Ae(t){if(typeof t=="number")return{"":t.toString()};const e={};return t.split(/ +/).forEach(n=>{if(n==="")return;const[l,r]=n.split(":");r===void 0?e[""]=l:e[l]=r}),e}function E(t,e){var n;if(t==null)return;const l=Ae(t);if(e===void 0)return l[""];if(typeof e=="string")return(n=l[e])!==null&&n!==void 0?n:l[""];if(Array.isArray(e)){for(let r=e.length-1;r>=0;--r){const a=e[r];if(a in l)return l[a]}return l[""]}else{let r,a=-1;return Object.keys(l).forEach(o=>{const s=Number(o);!Number.isNaN(s)&&e>=s&&s>=a&&(a=s,r=l[o])}),r}}const Oe={xs:0,s:640,m:1024,l:1280,xl:1536,"2xl":1920};function Ve(t){return`(min-width: ${t}px)`}const V={};function De(t=Oe){if(!ge)return _(()=>[]);if(typeof window.matchMedia!="function")return _(()=>[]);const e=G({}),n=Object.keys(t),l=(r,a)=>{r.matches?e.value[a]=!0:e.value[a]=!1};return n.forEach(r=>{const a=t[r];let o,s;V[a]===void 0?(o=window.matchMedia(Ve(a)),o.addEventListener?o.addEventListener("change",u=>{s.forEach(i=>{i(u,r)})}):o.addListener&&o.addListener(u=>{s.forEach(i=>{i(u,r)})}),s=new Set,V[a]={mql:o,cbs:s}):(o=V[a].mql,s=V[a].cbs),s.add(l),o.matches&&s.forEach(u=>{u(o,r)})}),xe(()=>{n.forEach(r=>{const{cbs:a}=V[t[r]];a.has(l)&&a.delete(l)})}),_(()=>{const{value:r}=e;return n.filter(a=>r[a])})}function Ie(t){var e;const n=(e=t.dirs)===null||e===void 0?void 0:e.find(({dir:l})=>l===ae);return!!(n&&n.value===!1)}function je(t){const{textColor2:e,textColor3:n,fontSize:l,fontWeight:r}=t;return{labelFontSize:l,labelFontWeight:r,valueFontWeight:r,valueFontSize:"24px",labelTextColor:n,valuePrefixTextColor:e,valueSuffixTextColor:e,valueTextColor:e}}const Fe={common:ne,self:je},Le={thPaddingSmall:"6px",thPaddingMedium:"12px",thPaddingLarge:"12px",tdPaddingSmall:"6px",tdPaddingMedium:"12px",tdPaddingLarge:"12px"};function We(t){const{dividerColor:e,cardColor:n,modalColor:l,popoverColor:r,tableHeaderColor:a,tableColorStriped:o,textColor1:s,textColor2:u,borderRadius:i,fontWeightStrong:p,lineHeight:b,fontSizeSmall:x,fontSizeMedium:C,fontSizeLarge:y}=t;return Object.assign(Object.assign({},Le),{fontSizeSmall:x,fontSizeMedium:C,fontSizeLarge:y,lineHeight:b,borderRadius:i,borderColor:$(n,e),borderColorModal:$(l,e),borderColorPopover:$(r,e),tdColor:n,tdColorModal:l,tdColorPopover:r,tdColorStriped:$(n,o),tdColorStripedModal:$(l,o),tdColorStripedPopover:$(r,o),thColor:$(n,a),thColorModal:$(l,a),thColorPopover:$(r,a),thTextColor:s,tdTextColor:u,thFontWeight:p})}const He={common:ne,self:We},le=1,ue=Se("n-grid"),ce=1,Qe={span:{type:[Number,String],default:ce},offset:{type:[Number,String],default:0},suffix:Boolean,privateOffset:Number,privateSpan:Number,privateColStart:Number,privateShow:{type:Boolean,default:!0}},J=D({__GRID_ITEM__:!0,name:"GridItem",alias:["Gi"],props:Qe,setup(){const{isSsrRef:t,xGapRef:e,itemStyleRef:n,overflowRef:l,layoutShiftDisabledRef:r}=Ce(ue),a=ye();return{overflow:l,itemStyle:n,layoutShiftDisabled:r,mergedXGap:_(()=>M(e.value||0)),deriveStyle:()=>{t.value;const{privateSpan:o=ce,privateShow:s=!0,privateColStart:u=void 0,privateOffset:i=0}=a.vnode.props,{value:p}=e,b=M(p||0);return{display:s?"":"none",gridColumn:`${u??`span ${o}`} / span ${o}`,marginLeft:i?`calc((100% - (${o} - 1) * ${b}) / ${o} * ${i} + ${b} * ${i})`:""}}}},render(){var t,e;if(this.layoutShiftDisabled){const{span:n,offset:l,mergedXGap:r}=this;return S("div",{style:{gridColumn:`span ${n} / span ${n}`,marginLeft:l?`calc((100% - (${n} - 1) * ${r}) / ${n} * ${l} + ${r} * ${l})`:""}},this.$slots)}return S("div",{style:[this.itemStyle,this.deriveStyle()]},(e=(t=this.$slots).default)===null||e===void 0?void 0:e.call(t,{overflow:this.overflow}))}}),Xe={xs:0,s:640,m:1024,l:1280,xl:1536,xxl:1920},fe=24,K="__ssr__",qe={layoutShiftDisabled:Boolean,responsive:{type:[String,Boolean],default:"self"},cols:{type:[Number,String],default:fe},itemResponsive:Boolean,collapsed:Boolean,collapsedRows:{type:Number,default:1},itemStyle:[Object,String],xGap:{type:[Number,String],default:0},yGap:{type:[Number,String],default:0}},Je=D({name:"Grid",inheritAttrs:!1,props:qe,setup(t){const{mergedClsPrefixRef:e,mergedBreakpointsRef:n}=ee(t),l=/^\d+$/,r=G(void 0),a=De((n==null?void 0:n.value)||Xe),o=F(()=>!!(t.itemResponsive||!l.test(t.cols.toString())||!l.test(t.xGap.toString())||!l.test(t.yGap.toString()))),s=_(()=>{if(o.value)return t.responsive==="self"?r.value:a.value}),u=F(()=>{var v;return(v=Number(E(t.cols.toString(),s.value)))!==null&&v!==void 0?v:fe}),i=F(()=>E(t.xGap.toString(),s.value)),p=F(()=>E(t.yGap.toString(),s.value)),b=v=>{r.value=v.contentRect.width},x=v=>{ze(b,v)},C=G(!1),y=_(()=>{if(t.responsive==="self")return x}),h=G(!1),R=G();return se(()=>{const{value:v}=R;v&&v.hasAttribute(K)&&(v.removeAttribute(K),h.value=!0)}),$e(ue,{layoutShiftDisabledRef:re(t,"layoutShiftDisabled"),isSsrRef:h,itemStyleRef:re(t,"itemStyle"),xGapRef:i,overflowRef:C}),{isSsr:!we,contentEl:R,mergedClsPrefix:e,style:_(()=>t.layoutShiftDisabled?{width:"100%",display:"grid",gridTemplateColumns:`repeat(${t.cols}, minmax(0, 1fr))`,columnGap:M(t.xGap),rowGap:M(t.yGap)}:{width:"100%",display:"grid",gridTemplateColumns:`repeat(${u.value}, minmax(0, 1fr))`,columnGap:M(i.value),rowGap:M(p.value)}),isResponsive:o,responsiveQuery:s,responsiveCols:u,handleResize:y,overflow:C}},render(){if(this.layoutShiftDisabled)return S("div",te({ref:"contentEl",class:`${this.mergedClsPrefix}-grid`,style:this.style},this.$attrs),this.$slots);const t=()=>{var e,n,l,r,a,o,s;this.overflow=!1;const u=_e(Ee(this)),i=[],{collapsed:p,collapsedRows:b,responsiveCols:x,responsiveQuery:C}=this;u.forEach(d=>{var k,z,w,N,A;if(((k=d==null?void 0:d.type)===null||k===void 0?void 0:k.__GRID_ITEM__)!==!0)return;if(Ie(d)){const P=oe(d);P.props?P.props.privateShow=!1:P.props={privateShow:!1},i.push({child:P,rawChildSpan:0});return}d.dirs=((z=d.dirs)===null||z===void 0?void 0:z.filter(({dir:P})=>P!==ae))||null,((w=d.dirs)===null||w===void 0?void 0:w.length)===0&&(d.dirs=null);const I=oe(d),j=Number((A=E((N=I.props)===null||N===void 0?void 0:N.span,C))!==null&&A!==void 0?A:le);j!==0&&i.push({child:I,rawChildSpan:j})});let y=0;const h=(e=i[i.length-1])===null||e===void 0?void 0:e.child;if(h!=null&&h.props){const d=(n=h.props)===null||n===void 0?void 0:n.suffix;d!==void 0&&d!==!1&&(y=Number((r=E((l=h.props)===null||l===void 0?void 0:l.span,C))!==null&&r!==void 0?r:le),h.props.privateSpan=y,h.props.privateColStart=x+1-y,h.props.privateShow=(a=h.props.privateShow)!==null&&a!==void 0?a:!0)}let R=0,v=!1;for(const{child:d,rawChildSpan:k}of i){if(v&&(this.overflow=!0),!v){const z=Number((s=E((o=d.props)===null||o===void 0?void 0:o.offset,C))!==null&&s!==void 0?s:0),w=Math.min(k+z,x);if(d.props?(d.props.privateSpan=w,d.props.privateOffset=z):d.props={privateSpan:w,privateOffset:z},p){const N=R%x;w+N>x&&(R+=x-N),w+R+y>b*x?v=!0:R+=w}}v&&(d.props?d.props.privateShow!==!0&&(d.props.privateShow=!1):d.props={privateShow:!1})}return S("div",te({ref:"contentEl",class:`${this.mergedClsPrefix}-grid`,style:this.style,[K]:this.isSsr||void 0},this.$attrs),i.map(({child:d})=>d))};return this.isResponsive&&this.responsive==="self"?S(Re,{onResize:this.handleResize},{default:t}):t()}}),Ue=B("statistic",[L("label",`
 font-weight: var(--n-label-font-weight);
 transition: .3s color var(--n-bezier);
 font-size: var(--n-label-font-size);
 color: var(--n-label-text-color);
 `),B("statistic-value",`
 margin-top: 4px;
 font-weight: var(--n-value-font-weight);
 `,[L("prefix",`
 margin: 0 4px 0 0;
 font-size: var(--n-value-font-size);
 transition: .3s color var(--n-bezier);
 color: var(--n-value-prefix-text-color);
 `,[B("icon",{verticalAlign:"-0.125em"})]),L("content",`
 font-size: var(--n-value-font-size);
 transition: .3s color var(--n-bezier);
 color: var(--n-value-text-color);
 `),L("suffix",`
 margin: 0 0 0 4px;
 font-size: var(--n-value-font-size);
 transition: .3s color var(--n-bezier);
 color: var(--n-value-suffix-text-color);
 `,[B("icon",{verticalAlign:"-0.125em"})])])]),Ye=Object.assign(Object.assign({},Y.props),{tabularNums:Boolean,label:String,value:[String,Number]}),U=D({name:"Statistic",props:Ye,slots:Object,setup(t){const{mergedClsPrefixRef:e,inlineThemeDisabled:n,mergedRtlRef:l}=ee(t),r=Y("Statistic","-statistic",Ue,Fe,t,e),a=ie("Statistic",l,e),o=_(()=>{const{self:{labelFontWeight:u,valueFontSize:i,valueFontWeight:p,valuePrefixTextColor:b,labelTextColor:x,valueSuffixTextColor:C,valueTextColor:y,labelFontSize:h},common:{cubicBezierEaseInOut:R}}=r.value;return{"--n-bezier":R,"--n-label-font-size":h,"--n-label-font-weight":u,"--n-label-text-color":x,"--n-value-font-weight":p,"--n-value-font-size":i,"--n-value-prefix-text-color":b,"--n-value-suffix-text-color":C,"--n-value-text-color":y}}),s=n?de("statistic",void 0,o,t):void 0;return{rtlEnabled:a,mergedClsPrefix:e,cssVars:n?void 0:o,themeClass:s==null?void 0:s.themeClass,onRender:s==null?void 0:s.onRender}},render(){var t;const{mergedClsPrefix:e,$slots:{default:n,label:l,prefix:r,suffix:a}}=this;return(t=this.onRender)===null||t===void 0||t.call(this),S("div",{class:[`${e}-statistic`,this.themeClass,this.rtlEnabled&&`${e}-statistic--rtl`],style:this.cssVars},W(l,o=>S("div",{class:`${e}-statistic__label`},this.label||o)),S("div",{class:`${e}-statistic-value`,style:{fontVariantNumeric:this.tabularNums?"tabular-nums":""}},W(r,o=>o&&S("span",{class:`${e}-statistic-value__prefix`},o)),this.value!==void 0?S("span",{class:`${e}-statistic-value__content`},this.value):W(n,o=>o&&S("span",{class:`${e}-statistic-value__content`},o)),W(a,o=>o&&S("span",{class:`${e}-statistic-value__suffix`},o))))}}),Ze=c([B("table",`
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
 `)]),H("bordered",`
 border: 1px solid var(--n-merged-border-color);
 border-radius: var(--n-border-radius);
 `,[c("tr",[c("&:last-child",[c("td",`
 border-bottom: 0 solid var(--n-merged-border-color);
 `)])])]),H("single-line",[c("th",`
 border-right: 0px solid var(--n-merged-border-color);
 `),c("td",`
 border-right: 0px solid var(--n-merged-border-color);
 `)]),H("single-column",[c("tr",[c("&:not(:last-child)",[c("td",`
 border-bottom: 0px solid var(--n-merged-border-color);
 `)])])]),H("striped",[c("tr:nth-of-type(even)",[c("td","background-color: var(--n-td-color-striped)")])]),Ne("bottom-bordered",[c("tr",[c("&:last-child",[c("td",`
 border-bottom: 0px solid var(--n-merged-border-color);
 `)])])])]),Pe(B("table",`
 background-color: var(--n-td-color-modal);
 --n-merged-border-color: var(--n-border-color-modal);
 `,[c("th",`
 background-color: var(--n-th-color-modal);
 `),c("td",`
 background-color: var(--n-td-color-modal);
 `)])),ke(B("table",`
 background-color: var(--n-td-color-popover);
 --n-merged-border-color: var(--n-border-color-popover);
 `,[c("th",`
 background-color: var(--n-th-color-popover);
 `),c("td",`
 background-color: var(--n-td-color-popover);
 `)]))]),Ke=Object.assign(Object.assign({},Y.props),{bordered:{type:Boolean,default:!0},bottomBordered:{type:Boolean,default:!0},singleLine:{type:Boolean,default:!0},striped:Boolean,singleColumn:Boolean,size:String}),et=D({name:"Table",props:Ke,setup(t){const{mergedClsPrefixRef:e,inlineThemeDisabled:n,mergedRtlRef:l,mergedComponentPropsRef:r}=ee(t),a=_(()=>{var p,b;return t.size||((b=(p=r==null?void 0:r.value)===null||p===void 0?void 0:p.Table)===null||b===void 0?void 0:b.size)||"medium"}),o=Y("Table","-table",Ze,He,t,e),s=ie("Table",l,e),u=_(()=>{const p=a.value,{self:{borderColor:b,tdColor:x,tdColorModal:C,tdColorPopover:y,thColor:h,thColorModal:R,thColorPopover:v,thTextColor:d,tdTextColor:k,borderRadius:z,thFontWeight:w,lineHeight:N,borderColorModal:A,borderColorPopover:I,tdColorStriped:j,tdColorStripedModal:P,tdColorStripedPopover:ve,[Z("fontSize",p)]:pe,[Z("tdPadding",p)]:be,[Z("thPadding",p)]:he},common:{cubicBezierEaseInOut:me}}=o.value;return{"--n-bezier":me,"--n-td-color":x,"--n-td-color-modal":C,"--n-td-color-popover":y,"--n-td-text-color":k,"--n-border-color":b,"--n-border-color-modal":A,"--n-border-color-popover":I,"--n-border-radius":z,"--n-font-size":pe,"--n-th-color":h,"--n-th-color-modal":R,"--n-th-color-popover":v,"--n-th-font-weight":w,"--n-th-text-color":d,"--n-line-height":N,"--n-td-padding":be,"--n-th-padding":he,"--n-td-color-striped":j,"--n-td-color-striped-modal":P,"--n-td-color-striped-popover":ve}}),i=n?de("table",_(()=>a.value[0]),u,t):void 0;return{rtlEnabled:s,mergedClsPrefix:e,cssVars:n?void 0:u,themeClass:i==null?void 0:i.themeClass,onRender:i==null?void 0:i.onRender}},render(){var t;const{mergedClsPrefix:e}=this;return(t=this.onRender)===null||t===void 0||t.call(this),S("table",{class:[`${e}-table`,this.themeClass,{[`${e}-table--rtl`]:this.rtlEnabled,[`${e}-table--bottom-bordered`]:this.bottomBordered,[`${e}-table--bordered`]:this.bordered,[`${e}-table--single-line`]:this.singleLine,[`${e}-table--single-column`]:this.singleColumn,[`${e}-table--striped`]:this.striped}],style:this.cssVars},this.$slots)}}),at=D({__name:"DashboardView",setup(t){const e=G([{label:"用户数",value:0,color:"#667eea",icon:"👥"},{label:"部门数",value:0,color:"#07C160",icon:"🏢"},{label:"角色数",value:0,color:"#FF9800",icon:"🔑"},{label:"菜单数",value:0,color:"#F44336",icon:"📋"}]),n=[{key:1,name:"授权端点",path:"/oauth2/authorize",desc:"OAuth2 授权码流程"},{key:2,name:"Token 端点",path:"/oauth2/token",desc:"获取 access_token"},{key:3,name:"JWKS 端点",path:"/oauth2/jwks",desc:"RSA 公钥"},{key:4,name:"UserInfo",path:"/userinfo",desc:"OIDC 用户信息"},{key:5,name:"Discovery",path:"/.well-known/openid-configuration",desc:"OIDC 自动发现"}],l=[{title:"端点",key:"name",width:110},{title:"路径",key:"path",width:250,render:a=>S(Me,{},{default:()=>a.path})},{title:"说明",key:"desc"}];se(async()=>{try{const[a,o,s,u]=await Promise.all([Q.get("/api/users").catch(()=>({data:[]})),Q.get("/api/departments").catch(()=>({data:[]})),Q.get("/api/roles").catch(()=>({data:[]})),Q.get("/api/menus").catch(()=>({data:[]}))]);e.value[0].value=Array.isArray(a.data)?a.data.length:0,e.value[1].value=r(o.data),e.value[2].value=Array.isArray(s.data)?s.data.length:0,e.value[3].value=Array.isArray(u.data)?u.data.length:0}catch{}});function r(a){let o=0;for(const s of a)o++,s.children&&(o+=r(s.children));return o}return(a,o)=>(Be(),Te("div",null,[m(g(Je),{cols:"4","x-gap":"16","y-gap":"16",responsive:"screen","item-responsive":""},{default:f(()=>[m(g(J),{span:"4 m:2 l:1"},{default:f(()=>[m(g(O),{hoverable:""},{default:f(()=>[m(g(U),{label:e.value[0].label},{prefix:f(()=>[...o[0]||(o[0]=[T("span",{style:{"font-size":"24px"}},"👥",-1)])]),default:f(()=>[X(" "+q(e.value[0].value),1)]),_:1},8,["label"])]),_:1})]),_:1}),m(g(J),{span:"4 m:2 l:1"},{default:f(()=>[m(g(O),{hoverable:""},{default:f(()=>[m(g(U),{label:e.value[1].label},{prefix:f(()=>[...o[1]||(o[1]=[T("span",{style:{"font-size":"24px"}},"🏢",-1)])]),default:f(()=>[X(" "+q(e.value[1].value),1)]),_:1},8,["label"])]),_:1})]),_:1}),m(g(J),{span:"4 m:2 l:1"},{default:f(()=>[m(g(O),{hoverable:""},{default:f(()=>[m(g(U),{label:e.value[2].label},{prefix:f(()=>[...o[2]||(o[2]=[T("span",{style:{"font-size":"24px"}},"🔑",-1)])]),default:f(()=>[X(" "+q(e.value[2].value),1)]),_:1},8,["label"])]),_:1})]),_:1}),m(g(J),{span:"4 m:2 l:1"},{default:f(()=>[m(g(O),{hoverable:""},{default:f(()=>[m(g(U),{label:e.value[3].label},{prefix:f(()=>[...o[3]||(o[3]=[T("span",{style:{"font-size":"24px"}},"📋",-1)])]),default:f(()=>[X(" "+q(e.value[3].value),1)]),_:1},8,["label"])]),_:1})]),_:1})]),_:1}),m(g(O),{title:"系统信息",style:{"margin-top":"16px"}},{default:f(()=>[m(g(Ge),{vertical:""},{default:f(()=>[...o[4]||(o[4]=[T("div",{style:{color:"#666","font-size":"13px"}},"版本: yunxingcloud v1.0.0",-1),T("div",{style:{color:"#666","font-size":"13px"}},"架构: Spring Boot 4.0 + OAuth2 + JPA + Vue 3",-1),T("div",{style:{color:"#666","font-size":"13px"}},"OAuth2 / OIDC 端点列表:",-1)])]),_:1}),m(g(et),{columns:l,data:n,style:{"margin-top":"8px"},size:"small"})]),_:1})]))}});export{at as default};
