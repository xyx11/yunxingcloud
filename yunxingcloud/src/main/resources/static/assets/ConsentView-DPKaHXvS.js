import{d as O,Z as k,c as y,a2 as P,e as T,f as j,h as f,a3 as E,a4 as I,u as D,g as L,i as H,j as x,a5 as q,p as _,a6 as M,L as W,a1 as F,a7 as K,r as A,o as X,l as B,w as m,m as p,N as Y,n as Z,s as N,t as C,v as g,D as V,A as S,z as G,C as J,F as Q,B as $,a8 as U,_ as ee}from"./index-Bn6z-r_K.js";import{N as R}from"./Space-BJe-sY-b.js";import{N as te}from"./Card-BGV--IU_.js";function se(t){const{opacityDisabled:r,heightTiny:e,heightSmall:n,heightMedium:d,heightLarge:l,heightHuge:c,primaryColor:a,fontSize:i}=t;return{fontSize:i,textColor:a,sizeTiny:e,sizeSmall:n,sizeMedium:d,sizeLarge:l,sizeHuge:c,color:a,opacitySpinning:r}}const ne={common:O,self:se},ie=k([k("@keyframes spin-rotate",`
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
 `,[T("rotate",`
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
 `,[T("spinning",`
 user-select: none;
 -webkit-user-select: none;
 pointer-events: none;
 opacity: var(--n-opacity-spinning);
 `)])]),ae={small:20,medium:18,large:16},oe=Object.assign(Object.assign(Object.assign({},L.props),{contentClass:String,contentStyle:[Object,String],description:String,size:{type:[String,Number],default:"medium"},show:{type:Boolean,default:!0},rotate:{type:Boolean,default:!0},spinning:{type:Boolean,validator:()=>!0,default:void 0},delay:Number}),M),re=j({name:"Spin",props:oe,slots:Object,setup(t){const{mergedClsPrefixRef:r,inlineThemeDisabled:e}=D(t),n=L("Spin","-spin",ie,ne,t,r),d=x(()=>{const{size:i}=t,{common:{cubicBezierEaseInOut:u},self:h}=n.value,{opacitySpinning:w,color:v,textColor:s}=h,o=typeof i=="number"?W(i):h[F("size",i)];return{"--n-bezier":u,"--n-opacity-spinning":w,"--n-size":o,"--n-color":v,"--n-text-color":s}}),l=e?H("spin",x(()=>{const{size:i}=t;return typeof i=="number"?String(i):i[0]}),d,t):void 0,c=K(t,["spinning","show"]),a=_(!1);return q(i=>{let u;if(c.value){const{delay:h}=t;if(h){u=window.setTimeout(()=>{a.value=!0},h),i(()=>{clearTimeout(u)});return}}a.value=c.value}),{mergedClsPrefix:r,active:a,mergedStrokeWidth:x(()=>{const{strokeWidth:i}=t;if(i!==void 0)return i;const{size:u}=t;return ae[typeof u=="number"?"medium":u]}),cssVars:e?void 0:d,themeClass:l==null?void 0:l.themeClass,onRender:l==null?void 0:l.onRender}},render(){var t,r;const{$slots:e,mergedClsPrefix:n,description:d}=this,l=e.icon&&this.rotate,c=(d||e.description)&&f("div",{class:`${n}-spin-description`},d||((t=e.description)===null||t===void 0?void 0:t.call(e))),a=e.icon?f("div",{class:[`${n}-spin-body`,this.themeClass]},f("div",{class:[`${n}-spin`,l&&`${n}-spin--rotate`],style:e.default?"":this.cssVars},e.icon()),c):f("div",{class:[`${n}-spin-body`,this.themeClass]},f(E,{clsPrefix:n,style:e.default?"":this.cssVars,stroke:this.stroke,"stroke-width":this.mergedStrokeWidth,radius:this.radius,scale:this.scale,class:`${n}-spin`}),c);return(r=this.onRender)===null||r===void 0||r.call(this),e.default?f("div",{class:[`${n}-spin-container`,this.themeClass],style:this.cssVars},f("div",{class:[`${n}-spin-content`,this.active&&`${n}-spin-content--spinning`,this.contentClass],style:this.contentStyle},e),f(I,{name:"fade-in-transition"},{default:()=>this.active?a:null})):a}});async function le(t){return(await A.get("/api/oauth2/consent/client",{params:{client_id:t}})).data}const ce={class:"consent-page"},de={class:"prompt"},ue=j({__name:"ConsentView",setup(t){const r=Z(),e=r.query.client_id||"",n=r.query.state||"",d=(r.query.scope||"").split(/[+ ]/).filter(Boolean),l=_(e),c=_(!1),a=_(!1),i={openid:"使用您的身份进行登录",profile:"读取您的基本资料",email:"读取您的邮箱地址"};function u(s){return i[s]||s}X(async()=>{if(e){c.value=!0;try{const s=await le(e);l.value=s.clientName}catch{}finally{c.value=!1}}});function h(){if(a.value)return;a.value=!0;const s=document.createElement("form");s.method="POST",s.action="/oauth2/authorize",v(s,"client_id",e),v(s,"state",n),d.forEach(o=>v(s,"scope",o)),v(s,"consent","true"),document.body.appendChild(s),s.submit()}function w(){window.location.href="/"}function v(s,o,b){const z=document.createElement("input");z.type="hidden",z.name=o,z.value=b,s.appendChild(z)}return(s,o)=>(N(),B(p(Y),null,{default:m(()=>[C("div",ce,[g(p(te),{class:"consent-card"},{default:m(()=>[o[3]||(o[3]=C("h1",{class:"title"},"授权确认",-1)),g(p(re),{show:c.value},{default:m(()=>[C("p",de,[C("strong",null,V(l.value),1),o[0]||(o[0]=S(" 正在请求以下访问权限： ",-1))]),g(p(R),{vertical:"",class:"scopes"},{default:m(()=>[(N(!0),G(Q,null,J(p(d),b=>(N(),B(p(U),{key:b,type:"info",size:"large"},{default:m(()=>[S(V(u(b)),1)]),_:2},1024))),128))]),_:1})]),_:1},8,["show"]),g(p(R),{justify:"center",class:"actions"},{default:m(()=>[g(p($),{size:"large",onClick:w,disabled:a.value},{default:m(()=>[...o[1]||(o[1]=[S("拒绝",-1)])]),_:1},8,["disabled"]),g(p($),{type:"primary",size:"large",onClick:h,loading:a.value},{default:m(()=>[...o[2]||(o[2]=[S("确认授权",-1)])]),_:1},8,["loading"])]),_:1})]),_:1})])]),_:1}))}}),he=ee(ue,[["__scopeId","data-v-4e5e94ac"]]);export{he as default};
