/*!
 * 组件库4.0： 窗体
 *
 * 依赖JS文件:
 *	jquery.coral.core.js
 *	jquery.coral.component.js
 *	jquery.coral.mouse.js
 *  jquery.coral.button.js
 *	jquery.coral.draggable.js
 *	jquery.coral.position.js
 *	jquery.coral.resizable.js
 */
(function(){var a={buttons:true,height:true,maxHeight:true,maxWidth:true,minHeight:true,minWidth:true,width:true},b={maxHeight:true,maxWidth:true,minHeight:true,minWidth:true},c=null;$.component("coral.dialog",{version:"4.0.1",castProperties:["buttons"],options:{appendTo:"body",autoOpen:true,buttons:[],closeOnEscape:true,closeText:"关闭",closeButtonClass:"cui-icon-cross2",closable:true,loadtext:"加载中，请耐心等候 ...",maximumText:"最大化",maximizable:false,maximized:false,minimizable:false,minimized:false,minimizText:"最小化",restoreWidth:200,restoreHeight:200,dialogClass:"",iframePanel:false,draggable:true,hide:null,height:"auto",percent:false,manualResize:false,maxHeight:null,maxWidth:null,minHeight:150,minWidth:150,modal:false,zIndex:null,timeOut:0,position:{my:"center",at:"center",of:window,collision:"fit",using:function(e){var d=$(this).css(e).offset().top;if(d<0){$(this).css("top",e.top-d)}}},resizable:true,show:null,title:null,subTitle:null,type:null,wtype:"dialog",message:null,width:300,url:"",reLoadOnOpen:false,postData:[],asyncType:"post",beforeClose:null,onCreate:null,onClose:null,onDrag:null,onDragStart:null,onDragStop:null,onFocus:null,onLoad:null,onLoadError:null,onOpen:null,onResize:null,onResizeStart:null,onResizeStop:null,onConfirm:null,onCancel:null,focusInput:false},_compatible:function(){this.options.reloadOnOpen=this.options.reLoadOnOpen},_create:function(){this._compatible();var e=this;var d=/^(\d|[1-9]\d|100)%$/;if(d.test(this.options.height)){this.options.percent=this.options.height;this.options.height=this._percentToPx()}if($.inArray(this.options.wtype,["dialog","message","alert","confirm"])<0){this.options.wtype="dialog"}if(this.options.wtype!=="dialog"){this.options.minHeight=null}this.originalCss={display:this.element[0].style.display,width:this.element[0].style.width,minHeight:this.element[0].style.minHeight,maxHeight:this.element[0].style.maxHeight,height:this.element[0].style.height};
this.originalPosition={parent:this.element.parent(),index:this.element.parent().children().index(this.element)};this.originalTitle=this.element.attr("title");this.options.title=this.options.title||this.originalTitle;this.originalsubTitle=this.element.attr("subTitle");this.options.subTitle=this.options.subTitle||this.originalsubTitle;this._createWrapper();var f=this.options.isMessage?"alert-box":"coral-dialog-content";this.element.show().removeAttr("title").addClass(f+" coral-component-content").appendTo(this.uiDialog);if(this.options.wtype!=="dialog"){this.uiDialog.addClass("coral-messager")}switch(this.options.wtype){case"alert":this._createTitlebar();this._createButtonPanel();break;case"message":break;case"confirm":this._createTitlebar();this._createButtonPanel();break;case"dialog":this._createTitlebar();this._createButtonPanel()}if(this.options.draggable&&$.fn.draggable){this._makeDraggable()}if(this.options.resizable&&$.fn.resizable){this._makeResizable()}this._isOpen=false;this._trackFocus()},_init:function(){if(this.options.autoOpen){this.open()}},_appendTo:function(){var d=this.options.appendTo;if(typeof d=="string"&&d=="parent"){return this.originalPosition.parent}if(d&&(d.jquery||d.nodeType)){return $(d)}return this.document.find(d||"body").eq(0)},forceDestroy:function(){var d=this;this._destroy(true)},_destroy:function(f){var e,d=this.originalPosition;this._destroyOverlay();this.element.removeUniqueId().removeClass("coral-dialog-content coral-component-content").css(this.originalCss).detach();this.uiDialog.stop(true,true).remove();if(this.options.iframePanel){this.iframePanel.stop(true,true).remove()}if(!f){if(this.originalTitle){this.element.attr("title",this.originalTitle)}e=d.parent.children().eq(d.index);if(e.length&&e[0]!==this.element[0]){e.before(this.element)}else{d.parent.append(this.element)}}},component:function(){return this.uiDialog},disable:$.noop,enable:$.noop,minimize:function(){var g,f=this,e,d=this.originalPosition;this._hide(this.uiDialog,this.options.hide,function(){e=d.parent.children().eq(d.index);
if(e.length&&e[0]!==f.element[0]){e.before(f.uiDialog)}else{d.parent.append(f.uiDialog)}f._isOpen=false;f._focusedElement=null;f._destroyOverlay();f._untrackInstance();if($.inArray(f.options.wtype,["dialog","alert","confirm"])>-1){if(f.opener.length&&f.opener[0].tagName.toLowerCase()!="object"&&!f.opener.filter(":focusable").focus().length){try{g=f.document[0].activeElement;if(g&&g.nodeName.toLowerCase()!=="body"){$(g).blur()}}catch(h){}}}if("dialog"!==f.options.wtype){f.element.remove()}f._trigger("onMinimize",event)});if(this.options.iframePanel){this.iframePanel.hide()}},close:function(h){var g,f=this,e,d=this.originalPosition;if(this.options.reLoadOnOpen){this.loaded=false}if(!this._isOpen||this._trigger("beforeClose",h)===false){return}if(f.options.destroyOnClose){f.element.html("")}if(f.options.url!=""&&f.options.reLoadOnOpen&&f.options.autoDestroy){f.element.html("")}this._hide(this.uiDialog,this.options.hide,function(){e=d.parent.children().eq(d.index);if(e.length&&e[0]!==f.element[0]){e.before(f.uiDialog)}else{d.parent.append(f.uiDialog)}f._isOpen=false;f._focusedElement=null;f._destroyOverlay();f._untrackInstance();if($.inArray(f.options.wtype,["dialog","alert","confirm"])>-1){if(f.opener.length&&f.opener[0].tagName.toLowerCase()!="object"&&!f.opener.filter(":focusable").focus().length){try{g=f.document[0].activeElement;if(g&&g.nodeName.toLowerCase()!=="body"){$(g).blur()}}catch(i){}}}if("dialog"!==f.options.wtype){f.element.remove()}f._trigger("onClose",h)});if(this.options.iframePanel){this.iframePanel.hide()}},isOpen:function(){return this._isOpen},moveToTop:function(){this._moveToTop()},_moveToTop:function(h,e){var g=false,d=this.uiDialog.siblings(".coral-front:visible").map(function(){return +$(this).css("z-index")}).get(),f=Math.max.apply(null,d);if(f>=+this.uiDialog.css("z-index")){this.uiDialog.css("z-index",f+1);if(this.options.iframePanel){this.iframePanel.css("z-index",f+1)}g=true}if(g&&!e){this._trigger("onFocus",h)}return g},reload:function(){var e=this,d={panel:e.element};
var f=this.options.url;if(f){$(e.element).loading({position:"overlay",text:"加载中，请耐心等候！"});e.loaded=true;$.ajax({url:this.options.url,type:this.options.asyncType,dataType:"html",data:this.options.postData,success:function(h,g,i){$(e.element).loading("hide");e.element.html(h);$(e.element).loading({text:"渲染中，请耐心等候！",position:"overlay"});$.parser.parse(e.element);$(e.element).loading("hide");e._trigger("onLoad",null,d)},error:function(i,g,h){e._trigger("onLoadError",null,[{xhr:i,st:g,err:h}])},beforeSend:function(h,g){}})}},open:function(){var f=this,e={panel:f.element};this.uiDialog.appendTo(this._appendTo());var g=this.options.url&&!f.loaded;if(g){$(f.element).loading({position:"overlay",text:"加载中，请耐心等候！"});f.loaded=true;$.ajax({url:this.options.url,type:this.options.asyncType,dataType:"html",data:this.options.postData,success:function(i,h,j){$(f.element).loading("hide");f.element.html(i);$(f.element).loading({text:"渲染中，请耐心等候！",position:"overlay"});$.parser.parse(f.element);$(f.element).loading("hide");f._trigger("onLoad",null,e)},error:function(j,h,i){f._trigger("onLoadError",null,[{xhr:j,st:h,err:i}])},beforeSend:function(i,h){}})}c=null;if(this._isOpen){if(this._moveToTop()){if($.inArray(f.options.wtype,["dialog","alert","confirm"])>-1){this._focusTabbable()}}return}this._isOpen=true;this.opener=$(this.document[0].activeElement);this._size();this._position();this._createOverlay();this._moveToTop(null,true);if(this.overlay){this.overlay.css("z-index",this.uiDialog.css("z-index")-1);if(this.options.iframePanel){this.overlay.css("z-index",this.iframePanel.css("z-index")-1)}}this._show(this.uiDialog,this.options.show,function(){if($.inArray(f.options.wtype,["dialog","alert","confirm"])>-1){f._focusTabbable()}if(!g){$.coral.refreshAllComponent(f.element)}else{$(f.element).loading("refresh")}if(f.options.iframePanel){f.iframePanel.show();f.iframePanel.css("width",f.uiDialog.outerWidth())}f._trigger("onFocus")});this._makeFocusTarget();this._trigger("onOpen");if(!isNaN(this.options.timeOut)&&this.options.timeOut>0){c=setTimeout(d,f.options.timeOut);
this.uiDialog.hover(function(){if(c){clearTimeout(c)}},function(){c=setTimeout(d,f.options.timeOut)})}function d(){f.close()}},_focusTabbable:function(){var d=this._focusedElement;if(this.options.focusInput===true){this._focusFirst();return}if(!d){d=this.element.find("[autofocus]")}if(!d.length){d=this.element.find(":tabbable")}if(!d.length&&this.uiDialogButtonPane){d=this.uiDialogButtonPane.find(":tabbable")}if(!d.length&&this.uiDialogTitlebarClose){d=this.uiDialogTitlebarClose.filter(":tabbable")}if(!d.length){d=this.uiDialog}d.eq(0).focus()},_findFields:function(){return $.coral.findComponent(".ctrl-form-element",this.element)},_focusFirst:function(){var g=this,e=this._findFields();for(var f in e){var d=e[f];if(d.focus&&true==d.focus()){return}}},_keepFocus:function(d){function e(){var g=this.document[0].activeElement,f=this.uiDialog[0]===g||$.contains(this.uiDialog[0],g);if(!f){this._focusTabbable()}}d.preventDefault();e.call(this);this._delay(e)},_createWrapper:function(){this.uiDialog=$("<div>").addClass("coral-dialog coral-component coral-component-content coral-corner-all coral-front "+this.options.dialogClass).hide().attr({tabIndex:-1,role:"dialog"}).appendTo(this.element.parent());if(this.options.iframePanel){this.iframePanel=$("<iframe class='coral-dialog-iframePanel' style='position:absolute;'></iframe>").hide().appendTo(this._appendTo())}if(this.options.zIndex){this.uiDialog.css("z-index",this.options.zIndex);if(this.options.iframePanel){this.iframePanel.css("z-index",this.options.zIndex)}}this._on(this.uiDialog,{keydown:function(f){if(this.options.closeOnEscape&&!f.isDefaultPrevented()&&f.keyCode&&f.keyCode===$.coral.keyCode.ESCAPE){f.preventDefault();this.close(f);return}if(f.keyCode!==$.coral.keyCode.TAB||f.isDefaultPrevented()){return}var e=this.uiDialog.find(":tabbable"),g=e.filter(":first"),d=e.filter(":last");if((f.target===d[0]||f.target===this.uiDialog[0])&&!f.shiftKey){this._delay(function(){g.focus()});f.preventDefault()}else{if((f.target===g[0]||f.target===this.uiDialog[0])&&f.shiftKey){this._delay(function(){d.focus()
});f.preventDefault()}}},mousedown:function(d){if(this._moveToTop(d)){this._focusTabbable()}}});if(!this.element.find("[aria-describedby]").length){this.uiDialog.attr({"aria-describedby":this.element.uniqueId().attr("id")})}},_createTitlebar:function(){var e=this.options,d;this.uiDialogTitlebar=$("<div>").addClass("coral-dialog-titlebar coral-component-header coral-corner-all coral-helper-clearfix").prependTo(this.uiDialog);this.uiDialogToolbar=$("<div>").addClass("coral-dialog-toolbar coral-corner-all coral-helper-clearfix").appendTo(this.uiDialogTitlebar);this._on(this.uiDialogTitlebar,{mousedown:function(f){if(!$(f.target).closest(".coral-dialog-toolbar-close")&&!$(f.target).closest(".coral-dialog-toolbar-maximum")){this.uiDialog.focus()}}});if(this.options.minimizable){this.uiDialogTitlebarMaximum=$("<button type='button'></button>").button({label:"&nbsp;",icons:{primary:"cui-icon-minus3"},text:false}).addClass("coral-dialog-toolbar-minimized").appendTo(this.uiDialogToolbar);this._on(this.uiDialogTitlebarMaximum,{click:function(f){f.preventDefault();this.minimize()}})}if(this.options.maximizable){this.uiDialogTitlebarMaximum=$("<button type='button'></button>").button({label:this.options.maximumText,icons:{primary:"cui-icon-enlarge7"},text:false}).addClass("coral-dialog-toolbar-maximum").appendTo(this.uiDialogToolbar);this._on(this.uiDialogTitlebarMaximum,{click:function(f){f.preventDefault();if(!this.uiDialog.hasClass("coral-dialog-maximum")){this.maximize()}else{this.restore()}$.coral.refreshAllComponent(this.element)}})}if(this.options.closable){this.uiDialogTitlebarClose=$("<button type='button'></button>").button({label:this.options.closeText,icons:{primary:this.options.closeButtonClass},text:false}).addClass("coral-dialog-toolbar-close").appendTo(this.uiDialogToolbar);this._on(this.uiDialogTitlebarClose,{click:function(f){var g=$(f.target);g.removeClass("coral-state-hover");f.preventDefault();this.close(f)}})}d=$("<span>").uniqueId().addClass("coral-dialog-title").prependTo(this.uiDialogTitlebar);
this._title(d);uiDialogsubTitle=$("<span>").uniqueId().addClass("coral-dialog-subTitle").prependTo(this.uiDialogTitlebar);this._subTitle(uiDialogsubTitle);this.uiDialog.attr({"aria-labelledby":d.attr("id")});if(this.options.maximizable&&this.options.maximized){this.maximize();$.coral.refreshAllComponent(this.element)}},maximize:function(e){var d=this.options;d.restoreHeight=d.height;d.restoreWidth=d.width;$(this.element).dialog("option","width",$(window).width());$(this.element).dialog("option","height",$(window).height());this.uiDialog.addClass("coral-dialog-maximum");this.uiDialogTitlebarMaximum.find(".cui-icon-enlarge7").removeClass("cui-icon-enlarge7").addClass("cui-icon-shrink7");if(this.options.iframePanel){this.iframePanel.css({width:$(window).width(),height:$(window).height()})}this._trigger("onMaximize",null,{width:$(window).outerWidth(),height:$(window).outerHeight()})},restore:function(){var e=this,d=this.options;$(e.element).dialog("option","width",d.restoreWidth);$(e.element).dialog("option","height",d.restoreHeight);e.uiDialog.removeClass("coral-dialog-maximum");if(this.options.iframePanel){this.iframePanel.css({width:d.restoreWidth,height:d.restoreHeight});this.iframePanel.position(d.position)}this._resetMaximizeIcon();this._trigger("onRestore",null,{width:$(window).outerWidth(),height:$(window).outerHeight()})},_resetMaximizeIcon:function(){if(this.uiDialogTitlebarMaximum){this.uiDialogTitlebarMaximum.find(".cui-icon-shrink7").removeClass("cui-icon-shrink7").addClass("cui-icon-enlarge7")}},_title:function(e){var d=$.coral.toFunction(this.options.titleFormat);if(!this.options.title){e.html("&#160;")}if(d){e.html(d.call(this.element,this.options.title))}else{e.text(this.options.title)}},_subTitle:function(d){if(!this.options.subTitle){d.html("&#160;")}d.text(this.options.subTitle)},_createButtonPanel:function(){this.uiDialogButtonPane=$("<div>").addClass("coral-dialog-buttonpane coral-component-content coral-helper-clearfix");this.uiButtonSet=$("<div>").addClass("coral-dialog-buttonset").appendTo(this.uiDialogButtonPane);
this._createButtons()},_createButtons:function(){var h=this,g=this.options.buttons,d=$.noop,f=$.noop;this.uiDialogButtonPane.remove();this.uiButtonSet.empty();if($.isEmptyObject(g)){g={}}if(($.isArray(g)&&!g.length)){this.uiDialog.removeClass("coral-dialog-buttons");return}var e=true;$.each(g,function(i,j){var k,m,l="coral-btn-primary";j=$.isFunction(j)?{click:j,text:i}:j;j=$.extend({type:"button"},j);k=j.click;j.click=function(){k.apply(h.element[0],arguments)};m={icons:j.icons,icons:j.cls,countdown:j.countdown,text:j.showText};if(j.id){m.id=j.id}if(e){m=$.extend({},m,{cls:l+" "+j.cls})}else{m=$.extend({},m,{cls:j.cls})}delete j.icons;delete j.cls;delete j.showText;$("<button></button>",j).button(m).appendTo(h.uiButtonSet);e=false});this.uiDialog.addClass("coral-dialog-buttons");this.uiDialogButtonPane.appendTo(this.uiDialog)},_makeDraggable:function(){var f=this,e=this.options;function d(g){return{position:g.position,offset:g.offset}}this.uiDialog.draggable({cancel:".coral-dialog-content, .coral-dialog-toolbar-close",handle:".coral-dialog-titlebar",containment:"document",start:function(g,h){$(this).addClass("coral-dialog-dragging");f._blockFrames();if(f.options.iframePanel){f.iframePanel.css(h.position)}f._trigger("onDragStart",g,d(h))},drag:function(g,h){if(f.options.iframePanel){f.iframePanel.css(h.position)}f._trigger("onDrag",g,d(h))},stop:function(g,h){var j=h.offset.left-f.document.scrollLeft(),i=h.offset.top-f.document.scrollTop();e.position={my:"left top",at:"left"+(j>=0?"+":"")+j+" top"+(i>=0?"+":"")+i,of:f.window};$(this).removeClass("coral-dialog-dragging");if(f.options.iframePanel){f.iframePanel.position(e.position)}f._unblockFrames();f._trigger("onDragStop",g,d(h))}})},_makeResizable:function(){var i=this,g=this.options,h=g.resizable,d=this.uiDialog.css("position"),f=typeof h==="string"?h:"n,e,s,w,se,sw,ne,nw";function e(j){return{originalPosition:j.originalPosition,originalSize:j.originalSize,position:j.position,size:j.size}}this.uiDialog.resizable({cancel:".coral-dialog-content",containment:"document",alsoResize:this.element,maxWidth:g.maxWidth,maxHeight:g.maxHeight,minWidth:g.minWidth,minHeight:this._minHeight(),handles:f,start:function(j,k){$(this).addClass("coral-dialog-resizing");
i.manualResize=true;if(i.options.iframePanel){i.iframePanel.css(k.position);i.iframePanel.css(k.size)}i._blockFrames();i._trigger("onResizeStart",j,e(k))},resize:function(j,k){if(i.options.iframePanel){i.iframePanel.css(k.position);i.iframePanel.css(k.size)}$.coral.refreshAllComponent(i.element);i._trigger("onResize",j,e(k))},stop:function(j,k){var n=i.uiDialog.offset(),m=n.left-i.document.scrollLeft(),l=n.top-i.document.scrollTop();g.height=i.uiDialog.height();g.width=i.uiDialog.width();g.position={my:"left top",at:"left"+(m>=0?"+":"")+m+" top"+(l>=0?"+":"")+l,of:i.window};$(this).removeClass("coral-dialog-resizing");if(i.options.iframePanel){i.iframePanel.position(g.position);i.iframePanel.position(k.size)}i._unblockFrames();i._trigger("onResizeStop",j,e(k))}}).css("position",d)},_trackFocus:function(){this._on(this.component(),{focusin:function(d){this._makeFocusTarget();this._focusedElement=$(d.target)}})},_makeFocusTarget:function(){this._untrackInstance();this._trackingInstances().unshift(this)},_untrackInstance:function(){var e=this._trackingInstances(),d=$.inArray(this,e);if(d!==-1){e.splice(d,1)}},_trackingInstances:function(){var d=this.document.data("coral-dialog-instances");if(!d){d=[];this.document.data("coral-dialog-instances",d)}return d},_minHeight:function(){var d=this.options;return d.height==="auto"?d.minHeight:Math.min(d.minHeight,d.height)},_position:function(){var e=this.options.position,h=[0,0],f=[],g,d;if(e){if(typeof e==="string"||(typeof e==="object"&&"0" in e)){f=e.split?e.split(" "):[e[0],e[1]];if(f.length===1){f[1]=f[0]}$.each(["left","top"],function(k,j){if(+f[k]===f[k]){h[k]=f[k];f[k]=j}});e={my:f[0]+(h[0]<0?h[0]:"+"+h[0])+" "+f[1]+(h[1]<0?h[1]:"+"+h[1]),at:f.join(" ")}}e=$.extend({},$.coral.dialog.prototype.options.position,e)}else{e=$.coral.dialog.prototype.options.position}d=this.uiDialog.is(":visible");if(!d){this.uiDialog.show()}if(this.options.iframePanel){g=this.iframePanel.is(":visible");if(!g){this.iframePanel.show()}}if(this.options.queue){$("#coral-msgBox").position(e)
}else{this.uiDialog.position(e);if(this.options.iframePanel){this.iframePanel.position(e)}}if(!d){this.uiDialog.hide()}if(this.options.iframePanel){if(!g){this.iframePanel.hide()}}},_setOptions:function(f){var g=this,e=false,d={};$.each(f,function(h,i){g._setOption(h,i);if(h in a){e=true}if(h in b){d[h]=i}});if(e&&!$(g.element).is(":hidden")){this._size();this._position()}if(this.uiDialog.is(":data(coral-resizable)")){this.uiDialog.resizable("option",d)}},_setOption:function(f,g){var e,h,d=this.uiDialog;if(f==="dialogClass"){d.removeClass(this.options.dialogClass).addClass(g)}if(f==="disabled"){return}this._super(f,g);if(f==="appendTo"){this.uiDialog.appendTo(this._appendTo())}if(f==="buttons"){this._createButtons()}if(f==="maximumText"){this.uiDialogTitlebarMaximum.button({label:""+g})}if(f==="closeText"){this.uiDialogTitlebarClose.button({label:""+g})}if(f==="draggable"){e=d.is(":data(coral-draggable)");if(e&&!g){d.draggable("destroy");if(this.options.iframePanel){this.iframePanel.draggable("destroy")}}if(!e&&g){this._makeDraggable()}}if(f==="position"){this._position()}if(f==="resizable"){h=d.is(":data(coral-resizable)");if(h&&!g){d.resizable("destroy");if(this.options.iframePanel){this.iframePanel.resizable("destroy")}}if(h&&typeof g==="string"){d.resizable("option","handles",g)}if(!h&&g!==false){this._makeResizable()}}if(f==="title"){this._title(this.uiDialogTitlebar.find(".coral-dialog-title"))}if(f==="subTitle"){this._subTitle(this.uiDialogTitlebar.find(".coral-dialog-subTitle"))}if(f==="width"||f==="height"){this._resetMaximizeIcon();this.uiDialog.removeClass("coral-dialog-maximum")}},_size:function(){var d,f,h,e=this.options;this.element.hide().css({width:"auto",minHeight:0,maxHeight:"none",height:0});if(e.minWidth>e.width){e.width=e.minWidth}d=this.uiDialog.css({height:"auto",width:e.width}).outerHeight();this.element.show();f=Math.max(0,e.minHeight-d);h=typeof e.maxHeight==="number"?Math.max(0,e.maxHeight-d):"none";if(e.height==="auto"){this.element.css({minHeight:f,maxHeight:h,height:"auto"})
}else{var g=/^(\d|[1-9]\d|100)%$/;if(g.test(this.options.height)){this.options.percent=this.options.height;this.element.height(Math.max(0,this._percentToPx()-d))}else{this.element.height(Math.max(0,this.options.height-d))}}if(this.uiDialog.is(":data(coral-resizable)")){this.uiDialog.resizable("option","minHeight",this._minHeight())}if(this.options.iframePanel){this.iframePanel.css({width:this.uiDialog.outerWidth(),height:this.uiDialog.outerHeight()})}},_blockFrames:function(){this.iframeBlocks=this.document.find("iframe").map(function(){var d=$(this);return $("<div>").css({position:"absolute",width:d.outerWidth(),height:d.outerHeight()}).appendTo(d.parent()).offset(d.offset())[0]})},_unblockFrames:function(){if(this.iframeBlocks){this.iframeBlocks.remove();delete this.iframeBlocks}},_allowInteraction:function(d){if($(d.target).closest(".coral-dialog").length){return true}return !!$(d.target).closest(".coral-datepicker").length},_createOverlay:function(){if(!this.options.modal){return}var d=true;this._delay(function(){d=false});if(!this.document.data("coral-dialog-overlays")){this._on(this.document,{focusin:function(e){if(d){return}if(!this._allowInteraction(e)){e.preventDefault();var f=this.document.find(".coral-dialog:visible:last .coral-dialog-content").data(this.componentFullName);if(f&&f._focusTabbable){}}}})}this.overlay=$("<div>").addClass("coral-component-overlay coral-front").appendTo(this._appendTo());if(this.options.appendTo=="parent"){this.overlay.css({position:"relative"})}else{if($(this.options.appendTo)[0].tagName.toLowerCase()!="body"){this.overlay.css({position:"relative"})}}this._on(this.overlay,{mousedown:"_keepFocus"});this.document.data("coral-dialog-overlays",(this.document.data("coral-dialog-overlays")||0)+1)},_destroyOverlay:function(){if(!this.options.modal){return}if(this.overlay){var d=this.document.data("coral-dialog-overlays")-1;if(!d){this.document.unbind("focusin").removeData("coral-dialog-overlays")}else{this.document.data("coral-dialog-overlays",d)
}this.overlay.remove();this.overlay=null}},buttonPanel:function(){return this.uiButtonSet},hide:function(){this._isOpen=false;this.component().hide()},_percentToPx:function(){var g=this,d=this.options,f=d.percent,e=$(window);maxHeight=e.height()*parseInt(f.substring(0,f.length-1))/100;return maxHeight},refresh:function(){var g=this,d=this.options,f=d.percent;var e=/^(\d|[1-9]\d|100)%$/;if(e.test(f)&&!d.manualResize){maxHeight=g._percentToPx();d.height=maxHeight;setTimeout(function(){$(g.element).dialog("option","height",maxHeight)},0)}else{}}})}());