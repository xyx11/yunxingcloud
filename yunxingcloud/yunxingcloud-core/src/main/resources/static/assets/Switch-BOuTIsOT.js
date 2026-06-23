import{I as H,J as i,aj as I,M as W,K as r,ai as E,d as ce,az as D,O as n,S as v,U as de,V as X,aA as ue,m as O,an as he,X as be,ab as fe,ac as ve,n as F,as as U,aB as ge,a4 as g,ah as M,ak as l,au as we}from"./index-CPhTQfUa.js";const me=H("switch",`
 height: var(--n-height);
 min-width: var(--n-width);
 vertical-align: middle;
 user-select: none;
 -webkit-user-select: none;
 display: inline-flex;
 outline: none;
 justify-content: center;
 align-items: center;
`,[i("children-placeholder",`
 height: var(--n-rail-height);
 display: flex;
 flex-direction: column;
 overflow: hidden;
 pointer-events: none;
 visibility: hidden;
 `),i("rail-placeholder",`
 display: flex;
 flex-wrap: none;
 `),i("button-placeholder",`
 width: calc(1.75 * var(--n-rail-height));
 height: var(--n-rail-height);
 `),H("base-loading",`
 position: absolute;
 top: 50%;
 left: 50%;
 transform: translateX(-50%) translateY(-50%);
 font-size: calc(var(--n-button-width) - 4px);
 color: var(--n-loading-color);
 transition: color .3s var(--n-bezier);
 `,[I({left:"50%",top:"50%",originalTransform:"translateX(-50%) translateY(-50%)"})]),i("checked, unchecked",`
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
 `),i("checked",`
 right: 0;
 padding-right: calc(1.25 * var(--n-rail-height) - var(--n-offset));
 `),i("unchecked",`
 left: 0;
 justify-content: flex-end;
 padding-left: calc(1.25 * var(--n-rail-height) - var(--n-offset));
 `),W("&:focus",[i("rail",`
 box-shadow: var(--n-box-shadow-focus);
 `)]),r("round",[i("rail","border-radius: calc(var(--n-rail-height) / 2);",[i("button","border-radius: calc(var(--n-button-height) / 2);")])]),E("disabled",[E("icon",[r("rubber-band",[r("pressed",[i("rail",[i("button","max-width: var(--n-button-width-pressed);")])]),i("rail",[W("&:active",[i("button","max-width: var(--n-button-width-pressed);")])]),r("active",[r("pressed",[i("rail",[i("button","left: calc(100% - var(--n-offset) - var(--n-button-width-pressed));")])]),i("rail",[W("&:active",[i("button","left: calc(100% - var(--n-offset) - var(--n-button-width-pressed));")])])])])])]),r("active",[i("rail",[i("button","left: calc(100% - var(--n-button-width) - var(--n-offset))")])]),i("rail",`
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
 `,[i("button-icon",`
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
 `,[I()]),i("button",`
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
 `)]),r("active",[i("rail","background-color: var(--n-rail-color-active);")]),r("loading",[i("rail",`
 cursor: wait;
 `)]),r("disabled",[i("rail",`
 cursor: not-allowed;
 opacity: .5;
 `)])]),pe=Object.assign(Object.assign({},X.props),{size:String,value:{type:[String,Number,Boolean],default:void 0},loading:Boolean,defaultValue:{type:[String,Number,Boolean],default:!1},disabled:{type:Boolean,default:void 0},round:{type:Boolean,default:!0},"onUpdate:value":[Function,Array],onUpdateValue:[Function,Array],checkedValue:{type:[String,Number,Boolean],default:!0},uncheckedValue:{type:[String,Number,Boolean],default:!1},railStyle:Function,rubberBand:{type:Boolean,default:!0},spinProps:Object,onChange:[Function,Array]});let C;const ke=ce({name:"Switch",props:pe,slots:Object,setup(e){C===void 0&&(typeof CSS<"u"?typeof CSS.supports<"u"?C=CSS.supports("width","max(1px)"):C=!1:C=!0);const{mergedClsPrefixRef:_,inlineThemeDisabled:p,mergedComponentPropsRef:y}=de(e),k=X("Switch","-switch",me,ge,e,_),w=ue(e,{mergedSize(t){var s,c;if(e.size!==void 0)return e.size;if(t)return t.mergedSize.value;const f=(c=(s=y==null?void 0:y.value)===null||s===void 0?void 0:s.Switch)===null||c===void 0?void 0:c.size;return f||"medium"}}),{mergedSizeRef:x,mergedDisabledRef:h}=w,S=O(e.defaultValue),B=we(e,"value"),b=he(B,S),R=F(()=>b.value===e.checkedValue),a=O(!1),o=O(!1),$=F(()=>{const{railStyle:t}=e;if(t)return t({focused:o.value,checked:R.value})});function V(t){const{"onUpdate:value":s,onChange:c,onUpdateValue:f}=e,{nTriggerFormInput:P,nTriggerFormChange:T}=w;s&&U(s,t),f&&U(f,t),c&&U(c,t),S.value=t,P(),T()}function L(){const{nTriggerFormFocus:t}=w;t()}function Y(){const{nTriggerFormBlur:t}=w;t()}function J(){e.loading||h.value||(b.value!==e.checkedValue?V(e.checkedValue):V(e.uncheckedValue))}function q(){o.value=!0,L()}function G(){o.value=!1,Y(),a.value=!1}function Q(t){e.loading||h.value||t.key===" "&&(b.value!==e.checkedValue?V(e.checkedValue):V(e.uncheckedValue),a.value=!1)}function Z(t){e.loading||h.value||t.key===" "&&(t.preventDefault(),a.value=!0)}const A=F(()=>{const{value:t}=x,{self:{opacityDisabled:s,railColor:c,railColorActive:f,buttonBoxShadow:P,buttonColor:T,boxShadowFocus:ee,loadingColor:te,textColor:ie,iconColor:ae,[g("buttonHeight",t)]:d,[g("buttonWidth",t)]:ne,[g("buttonWidthPressed",t)]:oe,[g("railHeight",t)]:u,[g("railWidth",t)]:z,[g("railBorderRadius",t)]:re,[g("buttonBorderRadius",t)]:le},common:{cubicBezierEaseInOut:se}}=k.value;let j,K,N;return C?(j=`calc((${u} - ${d}) / 2)`,K=`max(${u}, ${d})`,N=`max(${z}, calc(${z} + ${d} - ${u}))`):(j=M((l(u)-l(d))/2),K=M(Math.max(l(u),l(d))),N=l(u)>l(d)?z:M(l(z)+l(d)-l(u))),{"--n-bezier":se,"--n-button-border-radius":le,"--n-button-box-shadow":P,"--n-button-color":T,"--n-button-width":ne,"--n-button-width-pressed":oe,"--n-button-height":d,"--n-height":K,"--n-offset":j,"--n-opacity-disabled":s,"--n-rail-border-radius":re,"--n-rail-color":c,"--n-rail-color-active":f,"--n-rail-height":u,"--n-rail-width":z,"--n-width":N,"--n-box-shadow-focus":ee,"--n-loading-color":te,"--n-text-color":ie,"--n-icon-color":ae}}),m=p?be("switch",F(()=>x.value[0]),A,e):void 0;return{handleClick:J,handleBlur:G,handleFocus:q,handleKeyup:Q,handleKeydown:Z,mergedRailStyle:$,pressed:a,mergedClsPrefix:_,mergedValue:b,checked:R,mergedDisabled:h,cssVars:p?void 0:A,themeClass:m==null?void 0:m.themeClass,onRender:m==null?void 0:m.onRender}},render(){const{mergedClsPrefix:e,mergedDisabled:_,checked:p,mergedRailStyle:y,onRender:k,$slots:w}=this;k==null||k();const{checked:x,unchecked:h,icon:S,"checked-icon":B,"unchecked-icon":b}=w,R=!(D(S)&&D(B)&&D(b));return n("div",{role:"switch","aria-checked":p,class:[`${e}-switch`,this.themeClass,R&&`${e}-switch--icon`,p&&`${e}-switch--active`,_&&`${e}-switch--disabled`,this.round&&`${e}-switch--round`,this.loading&&`${e}-switch--loading`,this.pressed&&`${e}-switch--pressed`,this.rubberBand&&`${e}-switch--rubber-band`],tabindex:this.mergedDisabled?void 0:0,style:this.cssVars,onClick:this.handleClick,onFocus:this.handleFocus,onBlur:this.handleBlur,onKeyup:this.handleKeyup,onKeydown:this.handleKeydown},n("div",{class:`${e}-switch__rail`,"aria-hidden":"true",style:y},v(x,a=>v(h,o=>a||o?n("div",{"aria-hidden":!0,class:`${e}-switch__children-placeholder`},n("div",{class:`${e}-switch__rail-placeholder`},n("div",{class:`${e}-switch__button-placeholder`}),a),n("div",{class:`${e}-switch__rail-placeholder`},n("div",{class:`${e}-switch__button-placeholder`}),o)):null)),n("div",{class:`${e}-switch__button`},v(S,a=>v(B,o=>v(b,$=>n(fe,null,{default:()=>this.loading?n(ve,Object.assign({key:"loading",clsPrefix:e,strokeWidth:20},this.spinProps)):this.checked&&(o||a)?n("div",{class:`${e}-switch__button-icon`,key:o?"checked-icon":"icon"},o||a):!this.checked&&($||a)?n("div",{class:`${e}-switch__button-icon`,key:$?"unchecked-icon":"icon"},$||a):null})))),v(x,a=>a&&n("div",{key:"checked",class:`${e}-switch__checked`},a)),v(h,a=>a&&n("div",{key:"unchecked",class:`${e}-switch__unchecked`},a)))))}});export{ke as N};
