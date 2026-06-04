import{Z as k,c as y,a3 as P,d as B,e as j,h as f,a4 as E,a5 as I,u as D,f as O,i as L,j as x,a6 as q,a7 as W,p as w,a8 as H,K,a2 as F,a9 as M,r as A,o as X,l as T,w as m,m as u,N as Y,n as Z,s as N,t as z,v as g,D as V,A as _,z as G,C as J,F as Q,B as $,aa as U,_ as ee}from"./index-CHoUe_2n.js";import{N as R}from"./Space-ClCUP69q.js";import{N as te}from"./Card-DyIhfc6P.js";const se=k([k("@keyframes spin-rotate",`
 from {
 transform: rotate(0);
 }
 to {
 transform: rotate(360deg);
 }
 `),y("spin-container",`
 position: relative;
 `,[y("spin-body",`
 position: absolute;
 top: 50%;
 left: 50%;
 transform: translateX(-50%) translateY(-50%);
 `,[P()])]),y("spin-body",`
 display: inline-flex;
 align-items: center;
 justify-content: center;
 flex-direction: column;
 `),y("spin",`
 display: inline-flex;
 height: var(--n-size);
 width: var(--n-size);
 font-size: var(--n-size);
 color: var(--n-color);
 `,[B("rotate",`
 animation: spin-rotate 2s linear infinite;
 `)]),y("spin-description",`
 display: inline-block;
 font-size: var(--n-font-size);
 color: var(--n-text-color);
 transition: color .3s var(--n-bezier);
 margin-top: 8px;
 `),y("spin-content",`
 opacity: 1;
 transition: opacity .3s var(--n-bezier);
 pointer-events: all;
 `,[B("spinning",`
 user-select: none;
 -webkit-user-select: none;
 pointer-events: none;
 opacity: var(--n-opacity-spinning);
 `)])]),ne={small:20,medium:18,large:16},ie=Object.assign(Object.assign(Object.assign({},O.props),{contentClass:String,contentStyle:[Object,String],description:String,size:{type:[String,Number],default:"medium"},show:{type:Boolean,default:!0},rotate:{type:Boolean,default:!0},spinning:{type:Boolean,validator:()=>!0,default:void 0},delay:Number}),H),ae=j({name:"Spin",props:ie,slots:Object,setup(s){const{mergedClsPrefixRef:o,inlineThemeDisabled:e}=D(s),n=O("Spin","-spin",se,W,s,o),p=x(()=>{const{size:i}=s,{common:{cubicBezierEaseInOut:d},self:v}=n.value,{opacitySpinning:S,color:h,textColor:t}=v,a=typeof i=="number"?K(i):v[F("size",i)];return{"--n-bezier":d,"--n-opacity-spinning":S,"--n-size":a,"--n-color":h,"--n-text-color":t}}),r=e?L("spin",x(()=>{const{size:i}=s;return typeof i=="number"?String(i):i[0]}),p,s):void 0,c=M(s,["spinning","show"]),l=w(!1);return q(i=>{let d;if(c.value){const{delay:v}=s;if(v){d=window.setTimeout(()=>{l.value=!0},v),i(()=>{clearTimeout(d)});return}}l.value=c.value}),{mergedClsPrefix:o,active:l,mergedStrokeWidth:x(()=>{const{strokeWidth:i}=s;if(i!==void 0)return i;const{size:d}=s;return ne[typeof d=="number"?"medium":d]}),cssVars:e?void 0:p,themeClass:r==null?void 0:r.themeClass,onRender:r==null?void 0:r.onRender}},render(){var s,o;const{$slots:e,mergedClsPrefix:n,description:p}=this,r=e.icon&&this.rotate,c=(p||e.description)&&f("div",{class:`${n}-spin-description`},p||((s=e.description)===null||s===void 0?void 0:s.call(e))),l=e.icon?f("div",{class:[`${n}-spin-body`,this.themeClass]},f("div",{class:[`${n}-spin`,r&&`${n}-spin--rotate`],style:e.default?"":this.cssVars},e.icon()),c):f("div",{class:[`${n}-spin-body`,this.themeClass]},f(E,{clsPrefix:n,style:e.default?"":this.cssVars,stroke:this.stroke,"stroke-width":this.mergedStrokeWidth,radius:this.radius,scale:this.scale,class:`${n}-spin`}),c);return(o=this.onRender)===null||o===void 0||o.call(this),e.default?f("div",{class:[`${n}-spin-container`,this.themeClass],style:this.cssVars},f("div",{class:[`${n}-spin-content`,this.active&&`${n}-spin-content--spinning`,this.contentClass],style:this.contentStyle},e),f(I,{name:"fade-in-transition"},{default:()=>this.active?l:null})):l}});async function oe(s){return(await A.get("/api/oauth2/consent/client",{params:{client_id:s}})).data}const re={class:"consent-page"},le={class:"prompt"},ce=j({__name:"ConsentView",setup(s){const o=Z(),e=o.query.client_id||"",n=o.query.state||"",p=(o.query.scope||"").split(/[+ ]/).filter(Boolean),r=w(e),c=w(!1),l=w(!1),i={openid:"使用您的身份进行登录",profile:"读取您的基本资料",email:"读取您的邮箱地址"};function d(t){return i[t]||t}X(async()=>{if(e){c.value=!0;try{const t=await oe(e);r.value=t.clientName}catch{}finally{c.value=!1}}});function v(){if(l.value)return;l.value=!0;const t=document.createElement("form");t.method="POST",t.action="/oauth2/authorize",h(t,"client_id",e),h(t,"state",n),p.forEach(a=>h(t,"scope",a)),h(t,"consent","true"),document.body.appendChild(t),t.submit()}function S(){window.location.href="/"}function h(t,a,b){const C=document.createElement("input");C.type="hidden",C.name=a,C.value=b,t.appendChild(C)}return(t,a)=>(N(),T(u(Y),null,{default:m(()=>[z("div",re,[g(u(te),{class:"consent-card"},{default:m(()=>[a[3]||(a[3]=z("h1",{class:"title"},"授权确认",-1)),g(u(ae),{show:c.value},{default:m(()=>[z("p",le,[z("strong",null,V(r.value),1),a[0]||(a[0]=_(" 正在请求以下访问权限： ",-1))]),g(u(R),{vertical:"",class:"scopes"},{default:m(()=>[(N(!0),G(Q,null,J(u(p),b=>(N(),T(u(U),{key:b,type:"info",size:"large"},{default:m(()=>[_(V(d(b)),1)]),_:2},1024))),128))]),_:1})]),_:1},8,["show"]),g(u(R),{justify:"center",class:"actions"},{default:m(()=>[g(u($),{size:"large",onClick:S,disabled:l.value},{default:m(()=>[...a[1]||(a[1]=[_("拒绝",-1)])]),_:1},8,["disabled"]),g(u($),{type:"primary",size:"large",onClick:v,loading:l.value},{default:m(()=>[...a[2]||(a[2]=[_("确认授权",-1)])]),_:1},8,["loading"])]),_:1})]),_:1})])]),_:1}))}}),fe=ee(ce,[["__scopeId","data-v-4e5e94ac"]]);export{fe as default};
