import{a9 as Y,bF as Ie,ao as ge,z as Pe,a8 as be,I as S,K as y,M as K,d as he,O as w,U as le,V as ne,bI as ae,bJ as ve,ap as re,m as O,bK as Ce,J as ee,n as h,bL as pe,bt as ie,aX as ze,S as Me,bp as se,au as te,o as Le,X as We,bM as de,bN as Ae,b6 as fe,a4 as A}from"./index-DRJefXGa.js";function je(t,e,r){var n;const f=Y(t,null);if(f===null)return;const l=(n=Ie())===null||n===void 0?void 0:n.proxy;ge(r,d),d(r.value),Pe(()=>{d(void 0,r.value)});function d(i,a){if(!f)return;const s=f[e];a!==void 0&&g(s,a),i!==void 0&&b(s,i)}function g(i,a){i[a]||(i[a]=[]),i[a].splice(i[a].findIndex(s=>s===l),1)}function b(i,a){i[a]||(i[a]=[]),~i[a].findIndex(s=>s===l)||i[a].push(l)}}const J=be("n-form"),ke=be("n-form-item-insts"),qe=S("form",[y("inline",`
 width: 100%;
 display: inline-flex;
 align-items: flex-start;
 align-content: space-around;
 `,[S("form-item",{width:"auto",marginRight:"18px"},[K("&:last-child",{marginRight:0})])])]);var Fe=function(t,e,r,n){function f(l){return l instanceof r?l:new r(function(d){d(l)})}return new(r||(r=Promise))(function(l,d){function g(a){try{i(n.next(a))}catch(s){d(s)}}function b(a){try{i(n.throw(a))}catch(s){d(s)}}function i(a){a.done?l(a.value):f(a.value).then(g,b)}i((n=n.apply(t,e||[])).next())})};const Oe=Object.assign(Object.assign({},ne.props),{inline:Boolean,labelWidth:[Number,String],labelAlign:String,labelPlacement:{type:String,default:"top"},model:{type:Object,default:()=>{}},rules:Object,disabled:Boolean,size:String,showRequireMark:{type:Boolean,default:void 0},requireMarkPlacement:String,showFeedback:{type:Boolean,default:!0},onSubmit:{type:Function,default:t=>{t.preventDefault()}},showLabel:{type:Boolean,default:void 0},validateMessages:Object}),De=he({name:"Form",props:Oe,setup(t){const{mergedClsPrefixRef:e}=le(t);ne("Form","-form",qe,ve,t,e);const r={},n=O(void 0),f=i=>{const a=n.value;(a===void 0||i>=a)&&(n.value=i)};function l(){var i;for(const a of ae(r)){const s=r[a];for(const v of s)(i=v.invalidateLabelWidth)===null||i===void 0||i.call(v)}}function d(i){return Fe(this,arguments,void 0,function*(a,s=()=>!0){return yield new Promise((v,j)=>{const o=[];for(const $ of ae(r)){const V=r[$];for(const _ of V)_.path&&o.push(_.internalValidate(null,s))}Promise.all(o).then($=>{const V=$.some(P=>!P.valid),_=[],q=[];$.forEach(P=>{var M,I;!((M=P.errors)===null||M===void 0)&&M.length&&_.push(P.errors),!((I=P.warnings)===null||I===void 0)&&I.length&&q.push(P.warnings)}),a&&a(_.length?_:void 0,{warnings:q.length?q:void 0}),V?j(_.length?_:void 0):v({warnings:q.length?q:void 0})})})})}function g(){for(const i of ae(r)){const a=r[i];for(const s of a)s.restoreValidation()}}return re(J,{props:t,maxChildLabelWidthRef:n,deriveMaxChildLabelWidth:f}),re(ke,{formItems:r}),Object.assign({validate:d,restoreValidation:g,invalidateLabelWidth:l},{mergedClsPrefix:e})},render(){const{mergedClsPrefix:t}=this;return w("form",{class:[`${t}-form`,this.inline&&`${t}-form--inline`],onSubmit:this.onSubmit},this.$slots)}}),{cubicBezierEaseInOut:ce}=Ce;function Ve({name:t="fade-down",fromOffset:e="-4px",enterDuration:r=".3s",leaveDuration:n=".3s",enterCubicBezier:f=ce,leaveCubicBezier:l=ce}={}){return[K(`&.${t}-transition-enter-from, &.${t}-transition-leave-to`,{opacity:0,transform:`translateY(${e})`}),K(`&.${t}-transition-enter-to, &.${t}-transition-leave-from`,{opacity:1,transform:"translateY(0)"}),K(`&.${t}-transition-leave-active`,{transition:`opacity ${n} ${l}, transform ${n} ${l}`}),K(`&.${t}-transition-enter-active`,{transition:`opacity ${r} ${f}, transform ${r} ${f}`})]}const Ee=S("form-item",`
 display: grid;
 line-height: var(--n-line-height);
`,[S("form-item-label",`
 grid-area: label;
 align-items: center;
 line-height: 1.25;
 text-align: var(--n-label-text-align);
 font-size: var(--n-label-font-size);
 min-height: var(--n-label-height);
 padding: var(--n-label-padding);
 color: var(--n-label-text-color);
 transition: color .3s var(--n-bezier);
 box-sizing: border-box;
 font-weight: var(--n-label-font-weight);
 `,[ee("asterisk",`
 white-space: nowrap;
 user-select: none;
 -webkit-user-select: none;
 color: var(--n-asterisk-color);
 transition: color .3s var(--n-bezier);
 `),ee("asterisk-placeholder",`
 grid-area: mark;
 user-select: none;
 -webkit-user-select: none;
 visibility: hidden; 
 `)]),S("form-item-blank",`
 grid-area: blank;
 min-height: var(--n-blank-height);
 `),y("auto-label-width",[S("form-item-label","white-space: nowrap;")]),y("left-labelled",`
 grid-template-areas:
 "label blank"
 "label feedback";
 grid-template-columns: auto minmax(0, 1fr);
 grid-template-rows: auto 1fr;
 align-items: flex-start;
 `,[S("form-item-label",`
 display: grid;
 grid-template-columns: 1fr auto;
 min-height: var(--n-blank-height);
 height: auto;
 box-sizing: border-box;
 flex-shrink: 0;
 flex-grow: 0;
 `,[y("reverse-columns-space",`
 grid-template-columns: auto 1fr;
 `),y("left-mark",`
 grid-template-areas:
 "mark text"
 ". text";
 `),y("right-mark",`
 grid-template-areas: 
 "text mark"
 "text .";
 `),y("right-hanging-mark",`
 grid-template-areas: 
 "text mark"
 "text .";
 `),ee("text",`
 grid-area: text; 
 `),ee("asterisk",`
 grid-area: mark; 
 align-self: end;
 `)])]),y("top-labelled",`
 grid-template-areas:
 "label"
 "blank"
 "feedback";
 grid-template-rows: minmax(var(--n-label-height), auto) 1fr;
 grid-template-columns: minmax(0, 100%);
 `,[y("no-label",`
 grid-template-areas:
 "blank"
 "feedback";
 grid-template-rows: 1fr;
 `),S("form-item-label",`
 display: flex;
 align-items: flex-start;
 justify-content: var(--n-label-text-align);
 `)]),S("form-item-blank",`
 box-sizing: border-box;
 display: flex;
 align-items: center;
 position: relative;
 `),S("form-item-feedback-wrapper",`
 grid-area: feedback;
 box-sizing: border-box;
 min-height: var(--n-feedback-height);
 font-size: var(--n-feedback-font-size);
 line-height: 1.25;
 transform-origin: top left;
 `,[K("&:not(:empty)",`
 padding: var(--n-feedback-padding);
 `),S("form-item-feedback",{transition:"color .3s var(--n-bezier)",color:"var(--n-feedback-text-color)"},[y("warning",{color:"var(--n-feedback-text-color-warning)"}),y("error",{color:"var(--n-feedback-text-color-error)"}),Ve({fromOffset:"-3px",enterDuration:".3s",leaveDuration:".2s"})])])]);function Be(t){const e=Y(J,null),{mergedComponentPropsRef:r}=le(t);return{mergedSize:h(()=>{var n,f;if(t.size!==void 0)return t.size;if((e==null?void 0:e.props.size)!==void 0)return e.props.size;const l=(f=(n=r==null?void 0:r.value)===null||n===void 0?void 0:n.Form)===null||f===void 0?void 0:f.size;return l||"medium"})}}function Ne(t){const e=Y(J,null),r=h(()=>{const{labelPlacement:o}=t;return o!==void 0?o:e!=null&&e.props.labelPlacement?e.props.labelPlacement:"top"}),n=h(()=>r.value==="left"&&(t.labelWidth==="auto"||(e==null?void 0:e.props.labelWidth)==="auto")),f=h(()=>{if(r.value==="top")return;const{labelWidth:o}=t;if(o!==void 0&&o!=="auto")return ie(o);if(n.value){const $=e==null?void 0:e.maxChildLabelWidthRef.value;return $!==void 0?ie($):void 0}if((e==null?void 0:e.props.labelWidth)!==void 0)return ie(e.props.labelWidth)}),l=h(()=>{const{labelAlign:o}=t;if(o)return o;if(e!=null&&e.props.labelAlign)return e.props.labelAlign}),d=h(()=>{var o;return[(o=t.labelProps)===null||o===void 0?void 0:o.style,t.labelStyle,{width:f.value}]}),g=h(()=>{const{showRequireMark:o}=t;return o!==void 0?o:e==null?void 0:e.props.showRequireMark}),b=h(()=>{const{requireMarkPlacement:o}=t;return o!==void 0?o:(e==null?void 0:e.props.requireMarkPlacement)||"right"}),i=O(!1),a=O(!1),s=h(()=>{const{validationStatus:o}=t;if(o!==void 0)return o;if(i.value)return"error";if(a.value)return"warning"}),v=h(()=>{const{showFeedback:o}=t;return o!==void 0?o:(e==null?void 0:e.props.showFeedback)!==void 0?e.props.showFeedback:!0}),j=h(()=>{const{showLabel:o}=t;return o!==void 0?o:(e==null?void 0:e.props.showLabel)!==void 0?e.props.showLabel:!0});return{validationErrored:i,validationWarned:a,mergedLabelStyle:d,mergedLabelPlacement:r,mergedLabelAlign:l,mergedShowRequireMark:g,mergedRequireMarkPlacement:b,mergedValidationStatus:s,mergedShowFeedback:v,mergedShowLabel:j,isAutoLabelWidth:n}}function Te(t){const e=Y(J,null),r=h(()=>{const{rulePath:d}=t;if(d!==void 0)return d;const{path:g}=t;if(g!==void 0)return g}),n=h(()=>{const d=[],{rule:g}=t;if(g!==void 0&&(Array.isArray(g)?d.push(...g):d.push(g)),e){const{rules:b}=e.props,{value:i}=r;if(b!==void 0&&i!==void 0){const a=pe(b,i);a!==void 0&&(Array.isArray(a)?d.push(...a):d.push(a))}}return d}),f=h(()=>n.value.some(d=>d.required)),l=h(()=>f.value||t.required);return{mergedRules:n,mergedRequired:l}}var ue=function(t,e,r,n){function f(l){return l instanceof r?l:new r(function(d){d(l)})}return new(r||(r=Promise))(function(l,d){function g(a){try{i(n.next(a))}catch(s){d(s)}}function b(a){try{i(n.throw(a))}catch(s){d(s)}}function i(a){a.done?l(a.value):f(a.value).then(g,b)}i((n=n.apply(t,e||[])).next())})};const He=Object.assign(Object.assign({},ne.props),{label:String,labelWidth:[Number,String],labelStyle:[String,Object],labelAlign:String,labelPlacement:String,path:String,first:Boolean,rulePath:String,required:Boolean,showRequireMark:{type:Boolean,default:void 0},requireMarkPlacement:String,showFeedback:{type:Boolean,default:void 0},rule:[Object,Array],size:String,ignorePathChange:Boolean,validationStatus:String,feedback:String,feedbackClass:String,feedbackStyle:[String,Object],showLabel:{type:Boolean,default:void 0},labelProps:Object,contentClass:String,contentStyle:[String,Object]});function me(t,e){return(...r)=>{try{const n=t(...r);return!e&&(typeof n=="boolean"||n instanceof Error||Array.isArray(n))||n!=null&&n.then?n:(n===void 0||fe("form-item/validate",`You return a ${typeof n} typed value in the validator method, which is not recommended. Please use ${e?"`Promise`":"`boolean`, `Error` or `Promise`"} typed value instead.`),!0)}catch(n){fe("form-item/validate","An error is catched in the validation, so the validation won't be done. Your callback in `validate` method of `n-form` or `n-form-item` won't be called in this validation."),console.error(n);return}}}const Ye=he({name:"FormItem",props:He,slots:Object,setup(t){je(ke,"formItems",te(t,"path"));const{mergedClsPrefixRef:e,inlineThemeDisabled:r}=le(t),n=Y(J,null),f=Be(t),l=Ne(t),{validationErrored:d,validationWarned:g}=l,{mergedRequired:b,mergedRules:i}=Te(t),{mergedSize:a}=f,{mergedLabelPlacement:s,mergedLabelAlign:v,mergedRequireMarkPlacement:j}=l,o=O([]),$=O(se()),V=O(null),_=n?te(n.props,"disabled"):O(!1),q=ne("Form","-form-item",Ee,ve,t,e);ge(te(t,"path"),()=>{t.ignorePathChange||M()});function P(){if(!l.isAutoLabelWidth.value)return;const u=V.value;if(u!==null){const R=u.style.whiteSpace;u.style.whiteSpace="nowrap",u.style.width="",n==null||n.deriveMaxChildLabelWidth(Number(getComputedStyle(u).width.slice(0,-2))),u.style.whiteSpace=R}}function M(){o.value=[],d.value=!1,g.value=!1,t.feedback&&($.value=se())}const I=(...u)=>ue(this,[...u],void 0,function*(R=null,C=()=>!0,p={suppressWarning:!0}){const{path:z}=t;p?p.first||(p.first=t.first):p={};const{value:F}=i,B=n?pe(n.props.model,z||""):void 0,N={},T={},L=(R?F.filter(c=>Array.isArray(c.trigger)?c.trigger.includes(R):c.trigger===R):F).filter(C).map((c,k)=>{const m=Object.assign({},c);if(m.validator&&(m.validator=me(m.validator,!1)),m.asyncValidator&&(m.asyncValidator=me(m.asyncValidator,!0)),m.renderMessage){const Z=`__renderMessage__${k}`;T[Z]=m.message,m.message=Z,N[Z]=m.renderMessage}return m}),W=L.filter(c=>c.level!=="warning"),U=L.filter(c=>c.level==="warning"),x={valid:!0,errors:void 0,warnings:void 0};if(!L.length)return x;const H=z??"__n_no_path__",X=new de({[H]:W}),G=new de({[H]:U}),{validateMessages:D}=(n==null?void 0:n.props)||{};D&&(X.messages(D),G.messages(D));const Q=c=>{o.value=c.map(k=>{const m=(k==null?void 0:k.message)||"";return{key:m,render:()=>m.startsWith("__renderMessage__")?N[m]():m}}),c.forEach(k=>{var m;!((m=k.message)===null||m===void 0)&&m.startsWith("__renderMessage__")&&(k.message=T[k.message])})};if(W.length){const c=yield new Promise(k=>{X.validate({[H]:B},p,k)});c!=null&&c.length&&(x.valid=!1,x.errors=c,Q(c))}if(U.length&&!x.errors){const c=yield new Promise(k=>{G.validate({[H]:B},p,k)});c!=null&&c.length&&(Q(c),x.warnings=c)}return!x.errors&&!x.warnings?M():(d.value=!!x.errors,g.value=!!x.warnings),x});function we(){I("blur")}function Re(){I("change")}function xe(){I("focus")}function ye(){I("input")}function Se(u,R){return ue(this,void 0,void 0,function*(){let C,p,z,F;return typeof u=="string"?(C=u,p=R):u!==null&&typeof u=="object"&&(C=u.trigger,p=u.callback,z=u.shouldRuleBeApplied,F=u.options),yield new Promise((B,N)=>{I(C,z,F).then(({valid:T,errors:L,warnings:W})=>{T?(p&&p(void 0,{warnings:W}),B({warnings:W})):(p&&p(L,{warnings:W}),N(L))})})})}re(Ae,{path:te(t,"path"),disabled:_,mergedSize:f.mergedSize,mergedValidationStatus:l.mergedValidationStatus,restoreValidation:M,handleContentBlur:we,handleContentChange:Re,handleContentFocus:xe,handleContentInput:ye});const $e={validate:Se,restoreValidation:M,internalValidate:I,invalidateLabelWidth:P};Le(P);const oe=h(()=>{var u;const{value:R}=a,{value:C}=s,p=C==="top"?"vertical":"horizontal",{common:{cubicBezierEaseInOut:z},self:{labelTextColor:F,asteriskColor:B,lineHeight:N,feedbackTextColor:T,feedbackTextColorWarning:L,feedbackTextColorError:W,feedbackPadding:U,labelFontWeight:x,[A("labelHeight",R)]:H,[A("blankHeight",R)]:X,[A("feedbackFontSize",R)]:G,[A("feedbackHeight",R)]:D,[A("labelPadding",p)]:Q,[A("labelTextAlign",p)]:c,[A(A("labelFontSize",C),R)]:k}}=q.value;let m=(u=v.value)!==null&&u!==void 0?u:c;return C==="top"&&(m=m==="right"?"flex-end":"flex-start"),{"--n-bezier":z,"--n-line-height":N,"--n-blank-height":X,"--n-label-font-size":k,"--n-label-text-align":m,"--n-label-height":H,"--n-label-padding":Q,"--n-label-font-weight":x,"--n-asterisk-color":B,"--n-label-text-color":F,"--n-feedback-padding":U,"--n-feedback-font-size":G,"--n-feedback-height":D,"--n-feedback-text-color":T,"--n-feedback-text-color-warning":L,"--n-feedback-text-color-error":W}}),E=r?We("form-item",h(()=>{var u;return`${a.value[0]}${s.value[0]}${((u=v.value)===null||u===void 0?void 0:u[0])||""}`}),oe,t):void 0,_e=h(()=>s.value==="left"&&j.value==="left"&&v.value==="left");return Object.assign(Object.assign(Object.assign(Object.assign({labelElementRef:V,mergedClsPrefix:e,mergedRequired:b,feedbackId:$,renderExplains:o,reverseColSpace:_e},l),f),$e),{cssVars:r?void 0:oe,themeClass:E==null?void 0:E.themeClass,onRender:E==null?void 0:E.onRender})},render(){const{$slots:t,mergedClsPrefix:e,mergedShowLabel:r,mergedShowRequireMark:n,mergedRequireMarkPlacement:f,onRender:l}=this,d=n!==void 0?n:this.mergedRequired;l==null||l();const g=()=>{const b=this.$slots.label?this.$slots.label():this.label;if(!b)return null;const i=w("span",{class:`${e}-form-item-label__text`},b),a=d?w("span",{class:`${e}-form-item-label__asterisk`},f!=="left"?" *":"* "):f==="right-hanging"&&w("span",{class:`${e}-form-item-label__asterisk-placeholder`}," *"),{labelProps:s}=this;return w("label",Object.assign({},s,{class:[s==null?void 0:s.class,`${e}-form-item-label`,`${e}-form-item-label--${f}-mark`,this.reverseColSpace&&`${e}-form-item-label--reverse-columns-space`],style:this.mergedLabelStyle,ref:"labelElementRef"}),f==="left"?[a,i]:[i,a])};return w("div",{class:[`${e}-form-item`,this.themeClass,`${e}-form-item--${this.mergedSize}-size`,`${e}-form-item--${this.mergedLabelPlacement}-labelled`,this.isAutoLabelWidth&&`${e}-form-item--auto-label-width`,!r&&`${e}-form-item--no-label`],style:this.cssVars},r&&g(),w("div",{class:[`${e}-form-item-blank`,this.contentClass,this.mergedValidationStatus&&`${e}-form-item-blank--${this.mergedValidationStatus}`],style:this.contentStyle},t),this.mergedShowFeedback?w("div",{key:this.feedbackId,style:this.feedbackStyle,class:[`${e}-form-item-feedback-wrapper`,this.feedbackClass]},w(ze,{name:"fade-down-transition",mode:"out-in"},{default:()=>{const{mergedValidationStatus:b}=this;return Me(t.feedback,i=>{var a;const{feedback:s}=this,v=i||s?w("div",{key:"__feedback__",class:`${e}-form-item-feedback__line`},i||s):this.renderExplains.length?(a=this.renderExplains)===null||a===void 0?void 0:a.map(({key:j,render:o})=>w("div",{key:j,class:`${e}-form-item-feedback__line`},o())):null;return v?b==="warning"?w("div",{key:"controlled-warning",class:`${e}-form-item-feedback ${e}-form-item-feedback--warning`},v):b==="error"?w("div",{key:"controlled-error",class:`${e}-form-item-feedback ${e}-form-item-feedback--error`},v):b==="success"?w("div",{key:"controlled-success",class:`${e}-form-item-feedback ${e}-form-item-feedback--success`},v):w("div",{key:"controlled-default",class:`${e}-form-item-feedback`},v):null})}})):null)}});export{De as N,Ye as a};
