/*!
 * 组件库4.0：分栏符
 * 
 * 依赖JS文件：
 *    jquery.coral.core.js
 *    jquery.coral.component.js
 */
$.component("coral.subfield",{version:"4.0.1",options:{title:"",lineCls:null,textCls:null,onClick:null},_create:function(){if(!this.element.jquery){this.element=$(this.element)}this.element.addClass("coral-subfield");this.fieldset=$('<fieldset class="coral-subfield-fieldset"></fieldset>').appendTo(this.element);if(this.options.lineCls){this.fieldset.addClass(this.options.lineCls)}this.legend=$('<legend class="coral-subfield-legend">'+this.options.title+"</legend>").appendTo(this.fieldset);if(this.options.textCls){this.legend.addClass(this.options.textCls)}this._bindEvent()},_bindEvent:function(){var a=this;this.legend.bind("click"+this.eventNamespace,function(b){if(a.options.disabled){b.preventDefault();b.stopImmediatePropagation()}else{a._trigger("onClick")}})},_setOption:function(a,b){if(a==="title"){this.setTitle(b)}},_destroy:function(){this.uiTitle.remove()},component:function(){return this.element},show:function(){this.component().show()},hide:function(){this.component().hide()},setTitle:function(a){$("legend",this.fieldset).html(a);this.options.title=a},getTitle:function(){return this.options.title}});