$.component("coral.toolbar",{version:"4.0.2",castProperties:["data","dataCustom","dropdownOptions"],options:{clickToDisplay:1,id:null,name:null,dataCustom:{},responsive:true,disabled:false,cls:null,url:null,title:false,method:"get",data:null,width:"auto",height:null,isOverflow:true,dropdownOptions:{button:{text:false,label:"更多"},panelPosition:{my:"right top",at:"right bottom"},atGroup:0},align:0,autoDisplay:false,margin:5,onCreate:null,onClick:null,onLoad:null},_jion:function(c,b){var a=[];$.each(c,function(e,f){a.push($(f))});$.each(b,function(e,f){a.push($(f))});return a},_hideMenus:function(){$(".coral-tieredmenu").hide()},_create:function(){var a=this;this.isLoaded=false;this.groupLength=1;this._initElements();this._bindEvents()},_bindEvents:function(){var a=this;this._on(this.document,{mousedown:function(b){if(a.options.clickToDisplay==1&&a.options.autoDisplay){a.uiBox.toggleClass("coral-toolbar-click-active",false)}}})},_initElements:function(){var b=this,a=this.options;this.uiBox=$('<div class="coral-toolbar"></div>');this.uiBorder=$('<div class="coral-toolbar-border"></div>');this.uiBox.append(this.uiBorder);this.uiBox.insertAfter(this.element);this.element.appendTo(this.uiBorder);if(typeof this.element.attr("id")!="undefined"){this.options.id=this.element.attr("id")}else{if(this.options.id){this.element.attr("id",this.options.id)}}if(typeof this.element.attr("name")!="undefined"){this.options.name=this.element.attr("name")}else{if(this.options.name){this.element.attr("name",this.options.name)}}this.uiAfter=$("<button type='button' data-frozen='true' class='coral-toolbar-after-element ctrl-toolbar-element'></button>").menubutton({label:this.options.dropdownOptions.button.label,text:this.options.dropdownOptions.button.text,renderType:"button",icons:"cui-icon-arrow-down3 right",data:[],position:this.options.dropdownOptions.panelPosition});this.uiAfter.menubutton("component").addClass("coral-toolbar-item coral-toolbar-after");this.uiAfter.menubutton("hide");this._loadData();
if(this.options.width){this.uiBox.css({width:this.options.width})}if(this.options.clickToDisplay==0){this.uiBox.addClass("coral-toolbar-click-active")}},reload:function(a){this.isLoaded=false;this.groupLength=1;var b=this.options;if(typeof(a)!=="string"){b.data=a}else{b.url=a}this.element.html("");this.uiAfter=$("<button type='button' data-frozen='true' class='coral-toolbar-after-element ctrl-toolbar-element'></button>").menubutton({label:this.options.dropdownOptions.button.label,text:this.options.dropdownOptions.button.text,renderType:"button",icons:"cui-icon-arrow-down3 right",data:[],position:this.options.dropdownOptions.panelPosition});this.uiAfter.menubutton("component").addClass("coral-toolbar-item coral-toolbar-after");this.uiAfter.menubutton("hide");this._loadData()},_loadData:function(){var b=this,a=this.options;if(a.url){$.ajax({type:a.method,url:a.url,data:{},dataType:"json",success:function(c){b._initData(c)},error:function(){$.alert("Json Format Error!")}})}else{if(a.data){this._initData(a.data)}}},_initData:function(b){var a=this;if(typeof b==="object"){this._addItems(null,b);this._trigger("onLoad",null,{});this.isLoaded=true;this._setFrozenElements();this._position()}if(this.options.disabled){this._setDisabled(this.options.disabled)}},_addItems:function(e,b,j){if(typeof b!=="object"){return}var d=this,g=[],f=b.length;for(var c=0;c<f;c++){var a=b[c];if(a==""||!$.isEmptyObject(a)){g.push(d._createItem(a))}}this._appendItems(e,g,j);this._initItems(g);if(!this.element.find(".coral-toolbar-after-element").length){var k=this._getGroupNameByIndex(this.options.dropdownOptions.atGroup);var h=this.element.find("[group-role='"+k+"']");if(h.length){h.after(this.uiAfter.menubutton("component"))}else{this.element.append(this.uiAfter.menubutton("component"))}}},_createItem:function(a){var f=this,g=a,d=a.type||"button",e=null;if(a==="-"){d="seperator"}else{if(a==="->"){d="grouper"}else{if(a===""){d="blank"}else{if(a==="more"){d="more"}}}}var c=this._createEl(d);e=a;if(d==="button"&&typeof a.icon!=="undefined"&&a.icon!=""){var b=this._getIcon(a.icon);
delete e.icon;if(null!=b.ico1&&null==b.ico2){e.icons=b.ico1}else{if(null==b.ico1&&null!=b.ico2){e.icons=b.ico2+" right"}else{if(null!=b.ico1&&null!=b.ico2){e.icons=b.ico1+" left, "+b.ico2+" right"}}}}return{$el:c,coralType:d,options:e}},_align:function(){if(this.groupLength!=1){return}var b=this.options;if(b.align=="center"){var a=this._createEl("grouper").attr("grouper-role","center"),c=this._createEl("grouper").attr("grouper-role","right");this.element.prepend(a);this.element.find("[group-role='left']").removeClass("group-left").addClass("group-center").attr("group-role","center");this.element.find(".ctrl-toolbar-element").attr("group","center");this.element.append(c)}else{if(b.align=="right"){var c=this._createEl("grouper").attr("grouper-role","right");this.element.find("[group-role='left']").removeClass("group-left").addClass("group-right").attr("group-role","right");this.element.find(".ctrl-toolbar-element").attr("group","right");this.element.prepend(c)}}},_appendItems:function(h,m,o){var g=this;if(!this.isLoaded){var l=0;var a=g._createGrouper(l,this.groupLength);this.element.append(a);$.each(m,function(j,k){if(k.coralType=="grouper"){l+=1;a=g._createGrouper(l,g.groupLength);g.element.append(k.$el.attr("grouper-role",a.attr("group-role")));g.element.append(a)}else{a.append(k.$el.attr("group",a.attr("group-role")));if(k.options&&k.options.frozen==true){k.$el.attr("data-frozen",true)}}});this._align()}else{var q=this._getGroupNameByIndex(o);var n=this.element.find("[group-role='"+q+"']");var c=n.find(".ctrl-toolbar-element:not(.coral-toolbar-separator)").length;if(null==h||h==c){for(var f in m){m[f].$el.attr("group",q);m[f].$el.appendTo(n)}}else{if(h==0){for(var e in m){m[e].$el.attr("group",q);m[e].$el.prependTo(n)}}else{for(var d in m){m[d].$el.attr("group",q);var p=n.find(".ctrl-toolbar-element:eq("+h+")");var b=p.attr("component-role");if(b){p[b]("component").before(m[d].$el)}else{p.before(m[d].$el)}}}}}},_initItems:function(a){var e=this;for(var c in a){if(a[c].coralType==="seperator"||a[c].coralType==="grouper"||a[c].coralType==="blank"||a[c].coralType==="more"){continue
}var b=a[c].$el,f=a[c].coralType.toLowerCase(),d=a[c].options;d.componentCls=" coral-toolbar-item "+d.componentCls||"";if(a[c].coralType==="html"){b.attr("id",d.id).append(d.content);continue}if(e.options.title){d.title=true}b[f](d);b.off(".toolbaronclick").on(f+"onclick.toolbaronclick",function(i,h){h=h||{};h.id=h.id||i.currentTarget.id;$(i.currentTarget).attr("component-role")=="splitbutton";if(e.uiAfter.length){var g=$(i.currentTarget).attr("component-role");if(g=="splitbutton"||g=="button"){e.uiAfter.menubutton("hidePanel")}}e._trigger("onClick",i,h)});b.off(".toolbaronmouseenter").on(f+"onmouseenter.toolbaronmouseenter",function(i,h){var g=$(i.currentTarget).attr("component-role");if(g=="splitbutton"||g=="menubutton"){if(e.options.clickToDisplay==0&&e.options.autoDisplay){$(i.currentTarget)[g]("hideAllMenus");$(i.currentTarget)[g]("showMenu")}}})}},_createEl:function(b){var a;switch(b){case"button":a=$("<button type='button'></button>");break;case"checkbox":a=$("<input type='checkbox' />");break;case"textbox":case"combobox":a=$("<input type='text' />");break;case"datepicker":a=$("<input type='text' />");break;case"splitbutton":case"menubutton":a=$("<button type='button'></button>");break;case"seperator":a=$("<div class='coral-toolbar-item coral-toolbar-separator coral-toolbar-separator-horizontal'></div>");break;case"grouper":this.groupLength+=1;a=$("<div class='coral-toolbar-item coral-toolbar-grouper'></div>");break;case"html":a=$("<div class='coral-toolbar-item coral-toolbar-html'></div>");break;case"blank":a=$("<span class='coral-toolbar-item coral-toolbar-blank'></span>");break;case"more":a=this.uiAfter.menubutton("component");return a;break;default:a=$("<span class='coral-toolbar-item'></span>");break}return a.addClass("ctrl-toolbar-element")},_createGrouper:function(a,b){var c=$();if(b==1||b==3){switch(a){case 0:c=$("<span class='coral-toolbar-group group-left' group-role='left'></span>");break;case 1:c=$("<span class='coral-toolbar-group group-center' group-role='center'></span>");
break;case 2:c=$("<span class='coral-toolbar-group group-right' group-role='right'></span>");break;default:c=$("<span class='coral-toolbar-group' group-role='default'></span>");break}}else{if(b==2){switch(a){case 0:c=$("<span class='coral-toolbar-group group-left' group-role='left'></span>");break;case 1:c=$("<span class='coral-toolbar-group group-right' group-role='right'></span>");break;default:c=$("<span class='coral-toolbar-group' group-role='default'></span>");break}}}return c},_getGroupNameByIndex:function(a){if(typeof a!=="number"){return"left"}if(this.groupLength==1){return"left"}else{if(this.groupLength==2){return a==0?"left":"right"}else{if(this.groupLength==3){switch(a){case 0:return"left";case 1:return"center";case 2:return"right"}}}}},_getGrouper:function(){return this.element.find(".coral-toolbar-grouper")},_resetGrouper:function(){var a=this._getGrouper();if(a.length){a.width(1)}},_resetToolbarItems:function(){var b=this,a=this._getElements(2);this._resetGrouper();$.each(a,function(f,h){var e=$(h),d=b._getComponentByElement(e);d.css({left:""});if(d.hasClass("coral-menubutton-button-item")){var j=e.attr("group"),g=b.element.find("[group-role='"+j+"']");if(g.find("[data-frozen='true']").length){var c=g.find("[data-frozen='true']:eq(0)");var k=b._getComponentByElement(c);k.before(d.removeClass("coral-menubutton-button-item"))}else{g.append(d.removeClass("coral-menubutton-button-item"))}}});this.uiAfter.menubutton("hidePanel");this.uiAfter.menubutton("hide");this.uiBorder.css({width:"auto"})},_position:function(){if(!this.element.is(":visible")||!this.options.responsive){this._resetToolbarItems();this.component().addClass("coral-toolbar-initHidden");return}else{this.component().removeClass("coral-toolbar-initHidden")}var b=this,a=this.options;this._resetGrouper();this.totalWidth=this._totalWidth();if(a.isOverflow&&this.uiBox.width()>0){this.uiBorder.width(this.uiBox.width())}else{this.uiBorder.width(Math.max(this.totalWidth,this.uiBox.width()))}if(this._toolbarWidth()-this.totalWidth<0){this.uiAfter.menubutton("show")
}else{this.uiAfter.menubutton("hide")}this._positionItems(this.element)},_positionItems:function(a){var c=this,b=this.options,f=this.options.margin,e=false,g=0,d=$(),h={right:"auto"};this._prePosition();this.element.find(".coral-toolbar-item:not(.coral-state-hidden)").each(function(i,j){var k=$(j);if(e){return true}if(d.length){g=g+d.outerWidth()+f}h.left=g+"px";k.css(h);d=k});this._handlerDropdownItems()},_handlerDropdownItems:function(){var b=this,a=this._filter(this._getElements(0),".ctrl-init-splitbutton,.ctrl-init-menubutton");b._hideMenus();$.each(a,function(f,g){var e=$(g),d=b._getComponentByElement(e);try{switch(e.attr("component-role")){case"splitbutton":if(d.hasClass("coral-menubutton-button-item")){e.splitbutton("menu").tieredmenu("option",{my:"left top",at:"right top",of:e.splitbutton("uiDropdownButton")})}else{e.splitbutton("menu").tieredmenu("option",{my:"left top",at:"left bottom",of:e})}break;case"menubutton":if(d.hasClass("coral-menubutton-button-item")){e.menubutton("menu").tieredmenu("option",{my:"left top",at:"right top"})}else{e.menubutton("menu").tieredmenu("option",{my:"left top",at:"left bottom"})}break;default:break}}catch(c){console.log(" There is a error happened.")}finally{}})},_getComponentByElement:function(b){var a=$(),c=b.attr("component-role");if(c){a=b[c]("component")}else{a=b}return a},_filter:function(b,a){var c=[];$.each(b,function(e,f){var d=$(f);if(d.is(a)){c.push(d)}});return c},_prePosition:function(){var b=this,a=this.options,f=this.options.margin,h=0,e=false,g=[],d=$(),c=[];if(this.uiAfter.is(":visible")){c=this._getElements(3)}else{c=this._getElements(2)}var c=this._jion(b._getFrozenElements(),b._filter(c,":not([data-frozen='true'])"));$.each(c,function(m,o){var l=b._getComponentByElement($(o));var k=!l.hasClass("coral-menubutton-button-item");if(e){if(!k){return true}if(l.hasClass("coral-toolbar-grouper")||$(o).attr("data-frozen")=="true"){return true}g.push(l);return true}if(d.length&&d.is(":visible")){h=h+d.outerWidth()+f
}if(!k){var n=b.element.find("[group-role='"+$(o).attr("group")+"']");if(n.attr("group-role")==$(o).attr("group")&&n.find("[data-frozen='true']").length){var j=n.find("[data-frozen='true']:eq(0)");if(j.attr("component-role")){j[j.attr("component-role")]("component").before(l.removeClass("coral-menubutton-button-item"))}else{j.before(l.removeClass("coral-menubutton-button-item"))}}else{l.removeClass("coral-menubutton-button-item").appendTo(n)}}if(h+l.outerWidth()>b._toolbarWidth()){e=true;if(l.hasClass("coral-toolbar-grouper")||$(o).attr("data-frozen")=="true"){return true}if(k){g.push(l)}else{b.uiAfter.menubutton("prepend",l)}return true}d=l});if(!this.isLoaded){b.uiAfter.menubutton("append",g)}else{b.uiAfter.menubutton("prepend",g)}this._setGrouperWidth()},_setGrouperWidth:function(){var f=this._getGrouper();var b=(this._toolbarWidth()-this._totalWidth(true,this.uiAfter.is(":visible"))-this.options.margin);var e=this._getWidthByGroupRole("left");var c=this._getWidthByGroupRole("right");var a=(b-e+c)/2;var d=(b-a);if(b<0){f.width(1);return}if(f.length==1){f.width(b)}else{if(f.length==2){if(a>0&&d>0&&!this.uiAfter.is(":visible")){$(f[0]).width(a+1);$(f[1]).width(d+1)}else{$(f[1]).width(b+1);$(f[0]).width(1)}}}},_getWidthByGroupRole:function(e){var d=this.options,c=0,b=this.element.find("[group-role='"+e+"']"),a=b.find(".coral-toolbar-item");if(!b.length||!a.length){return 0}$.each(a,function(h,g){var f=$(g);c+=f.outerWidth()+d.margin;if(h==a.length-1){c-=d.margin}});return c},_toolbarWidth:function(){return this.uiBorder.innerWidth()},_setFrozenElements:function(){var b=this._getElements(3),a=false;$.each(b,function(d,e){var c=$(e);if(c.hasClass("coral-toolbar-after-element")){a=true}if(a&&!c.hasClass("coral-toolbar-grouper")){c.attr("data-frozen",true)}})},_getFrozenElements:function(){if(this.uiAfter.is(":visible")){return this.element.find("[data-frozen='true']")}else{return this.element.find("[data-frozen='true']:not(.coral-toolbar-after-element)")}},_getElements:function(b){var a=$(),c=this.element.find(".ctrl-toolbar-element"),d=this.uiAfter.menubutton("buttons").find(".ctrl-toolbar-element");
a=this._jion(c,d);switch(b){case 0:a=this._filter(a,":not(.coral-toolbar-after-element,.coral-toolbar-grouper,.coral-toolbar-separator)");break;case 1:a=this._filter(a,":not(.coral-toolbar-after-element,.coral-toolbar-grouper)");break;case 2:a=this._filter(a,":not(.coral-toolbar-after-element)");break;case 3:break;default:break}return a},_getComponents:function(c,a){var b=$(),d=this.element.find(".coral-toolbar-item"),e=this.uiAfter.menubutton("buttonElements");b=this._jion(d,e);c=c||1;switch(c){case 0:b=this._filter(b,":not(.coral-toolbar-after,.coral-toolbar-grouper,.coral-toolbar-separator)");break;case 1:b=this._filter(b,":not(.coral-toolbar-after,.coral-toolbar-grouper)");break;case 2:b=this._filter(b,":not(.coral-toolbar-after)");break;case 3:break;default:break}if(!a){$itemAll=this._filter(b,".coral-state-hidden")}return b},getTotalWidth:function(a){return this._totalWidth(a)},_totalWidth:function(c,b){var e=this,d=this.options,a=0;var g=$();if(b){g=this.element.find(".coral-toolbar-item:not(.coral-state-hidden)")}else{g=this.element.find(".coral-toolbar-item:not(.coral-state-hidden,.coral-toolbar-after)")}var f=this.uiAfter.menubutton("buttons").find(".coral-toolbar-item:not(.coral-state-hidden)");g.each(function(i,j){var h=$(j);a=a+h.outerWidth()+d.margin;if(i==g.length-1&&!f.length){a=a-d.margin}});if(c){return a-d.margin}this.uiAfter.menubutton("showPanel");f.each(function(i,j){var h=$(j);a=a+h.outerWidth()+d.margin;if(i==f.length-1){a=a-d.margin}});this.uiAfter.menubutton("hidePanel");return a},_getIcon:function(d){var c={ico1:null,ico2:null},b=[],a;if(d==null){return c}a=$.trim(d);if(a.indexOf(",")>=0){b=a.split(",");c.ico1=b[0]==""?null:b[0];c.ico2=b[1]==""?null:b[1]}else{c.ico1=a}return c},getLength:function(){return this._getElements(0).length},isExist:function(a){return this._getSubCoral(a)?true:false},_getGroupElementsLength:function(b){if(typeof b!=="number"||b>(this.groupLength-1)){return}var c=this._getGroupNameByIndex(b);var a=this.element.find("[group-role='"+c+"']");
return a.find(".ctrl-toolbar-element:not(.coral-toolbar-separator)").length},add:function(b,e,d){if(typeof e!=="object"){return}if(typeof b==="string"){return this._addByParentId(b,e)}d=d||0;var c=this,a=parseInt(b);if(((null!=b)&&isNaN(a))||a<0||a>this._getGroupElementsLength(d)){return}if(!$.isArray(e)){e=[e]}if(0==this._getGroupElementsLength(d)){b=null}this._addItems(b,e,d);this._refresh()},_addByParentId:function(a,d){var c=this._getSubCoral(a);if(c){var b=c.$el;switch(c.type){case"splitbutton":b.splitbutton("menu").tieredmenu("add",null,d);break;case"menubutton":b.menubutton("menu").tieredmenu("add",null,d);break;case"tieredmenu":b.tieredmenu("add",a,d);break;default:break}}},getSubCoral:function(a){var b=this;if(typeof a==="string"){return b._getSubCoral(a)}else{return b._getSubCoralByIndex(a)}},_getSubCoralByIndex:function(b){var c=this,a=parseInt(b);if(isNaN(a)||a<0||a>(this.getLength()-1)){return false}return c._getSubCoral(a)},_findElementsByAttr:function(b,a,d){var c=this,e=[];$.each(b,function(f,h){var g=$(h);if(d==g.attr(a)){e.push(g)}});return e},_getSubCoral:function(a){var c=this,i=this._findElementsByAttr(this._getElements(0),"id",a),e=null,g=this._findElementsByAttr(this._getElements(0),"component-role","splitbutton"),d=this._findElementsByAttr(this._getElements(0),"component-role","menubutton"),b=null,h=null,f=null;if(typeof a==="number"){f=$(this._getElements(0)[a]);h={$el:f,type:f.attr("component-role")};return h}if(i.length){h={$el:i[0],type:i[0].attr("component-role")};return h}b=this._findSubCoralInMenuItems(g,a);if(g.length&&b){h={$el:b,type:"tieredmenu"};return h}b=this._findSubCoralInMenuItems(d,a);if(d.length&&b){h={$el:b,type:"tieredmenu"};return h}return h},_findSubCoralInMenuItems:function(b,c){if(!b.length){return}var a=null;$.each(b,function(e,h){var d=$(h),g=d.attr("component-role"),f=$(h)[g]("menu");if(f.find("[data-id='"+c+"']").length){a=f}});return a},removeAll:function(){var a=this;$.each(this._getElements(1),function(b,d){var c=$(d),e=c.attr("component-role");
if(e){c[e]("destroy")}c.remove()});this._refresh()},remove:function(a){var b=this;if(typeof a==="string"){this._removeById(a)}else{this._removeByIndex(a)}this._refresh()},_removeById:function(e){var b=this,c=this._getSubCoral(e);if(c){var a=c.$el;if(a.hasClass("coral-toolbar-html")){a.remove();return}switch(c.type){case"tieredmenu":a.tieredmenu("removeItem",e);break;default:var d=a.attr("component-role");a[d]("destroy");a.remove();break}}},_removeByIndex:function(b){var a=parseInt(b);if(isNaN(a)||a<0||a>(this.getLength()-1)){return false}var c=$(this._getElements(0)[a]);var d=c.attr("component-role");if(d){c[d]("component").remove()}c.remove()},update:function(b,a){var c=this;if(typeof b==="string"){c._updateById(b,a)}else{c._updateByIndex(b,a)}this._refresh()},_updateById:function(d,a){var c=this._getSubCoral(d);if(c){var b=c.$el;switch(c.type){case"tieredmenu":b.tieredmenu("updateItem",d,a);break;case"button":b.button("update",a);break;case"splitbutton":b.splitbutton("button").button("update",a);break;case"menubutton":b.menubutton("button").button("update",a);break;default:break}}},_updateByIndex:function(c,b){var a=parseInt(c);if(isNaN(a)||a<0||a>(this.getLength()-1)){return false}var e=this._getSubCoral(c);if(e){var d=e.$el;switch(e.type){case"button":d.button("update",b);break;case"splitbutton":d.splitbutton("button").button("update",b);break;case"menubutton":d.menubutton("button").button("update",b);break;default:break}}},component:function(){return this.uiBox},_uiBorder:function(){return this.uiBorder},disable:function(){this._setDisabled(true)},enable:function(){this._setDisabled(false)},_setDisabled:function(a){var c=this;var b=this._getElements(0);$.each(b,function(e,f){var d=$(f);if(d.hasClass("coral-toolbar-html")){return true}if(!d.attr("component-role")){return}if(a){d[d.attr("component-role")]("disable")}else{d[d.attr("component-role")]("enable")}});this.options.disabled=!!a},disableItem:function(a){var b=this;if(typeof a==="string"){b._disableItemById(a)
}else{b._disableItemByIndex(a)}},_disableItemById:function(e){var b=this,d=this._getSubCoral(e);if(d){var a=d.$el;if(a.hasClass("coral-toolbar-html")){return}switch(d.type){case"tieredmenu":a.tieredmenu("disableItem",e);break;default:var c=$(this._findElementsByAttr(this._getElements(0),"id",e)[0]);if(!c.length){return}c[c.attr("component-role")]("disable");break}}},_disableItemByIndex:function(b){var c=this,a=parseInt(b);if(isNaN(a)||a<0||a>(this.getLength()-1)){return false}var d=$(this._getElements(0)[a]);if(d.hasClass("coral-toolbar-html")){return false}if(!d.length){return false}d[d.attr("component-role")]("disable");return true},enableItem:function(a){var b=this;if(typeof a==="string"){b._enableItemById(a)}else{b._enableItemByIndex(a)}},_enableItemById:function(e){var b=this,d=this._getSubCoral(e);if(d){var a=d.$el;if(a.hasClass("coral-toolbar-html")){return}switch(d.type){case"tieredmenu":a.tieredmenu("enableItem",e);break;default:var c=$(this._findElementsByAttr(this._getElements(0),"id",e)[0]);if(!c.length){return}c[c.attr("component-role")]("enable");break}}},_enableItemByIndex:function(b){var c=this,a=parseInt(b);if(isNaN(a)||a<0||a>(this.getLength()-1)){return false}var d=$(this._getElements(0)[a]);if(d.hasClass("coral-toolbar-html")){return}if(!d.length){return false}d[d.attr("component-role")]("enable");return true},hide:function(a){var b=this;if(typeof a==="string"){b._hideById(a)}else{b._hideByIndex(a)}this._refresh()},hideAll:function(){var a=this;$.each(this._getElements(1),function(b,d){var c=$(d).attr("component-role");if(c){$(d)[c]("hide")}else{$(d).hide()}});this._refresh()},_hideById:function(e){var b=this,d=this._getSubCoral(e);if(d){var a=d.$el;if(a.hasClass("coral-toolbar-html")){a.hide();return}switch(d.type){case"tieredmenu":a.tieredmenu("hideItem",e);break;default:var c=$(this._findElementsByAttr(this._getElements(0),"id",e)[0]);if(!c.length){return}c[c.attr("component-role")]("hide");break}}},_hideByIndex:function(b){var c=this,a=parseInt(b);
if(isNaN(a)||a<0||a>(this.getLength()-1)){return false}var d=$(this._getElements(0)[a]);if(!d.length){return false}if(d.hasClass("coral-toolbar-html")){d.hide();return true}d[d.attr("component-role")]("hide");return true},show:function(a){var b=this;if(typeof a==="string"){b._showById(a)}else{b._showByIndex(a)}this._refresh()},showAll:function(){var a=this;$.each(this._getElements(1),function(b,d){var c=$(d).attr("component-role");if(c){$(d)[c]("show")}else{$(d).show()}});this._refresh()},_showById:function(e){var b=this,d=this._getSubCoral(e);if(d){var a=d.$el;if(a.hasClass("coral-toolbar-html")){a.show();return}switch(d.type){case"tieredmenu":a.tieredmenu("showItem",e);break;default:var c=$(this._findElementsByAttr(this._getElements(0),"id",e)[0]);if(!c.length){return}c[c.attr("component-role")]("show");break}}},_showByIndex:function(b){var d=this,a=parseInt(b);if(isNaN(a)||a<0||a>(this.getLength()-1)){return false}var e=$(this._getElements(0)[a]);var c=e.attr("component-role");if(!e.length){return false}if(c){e[c]("show")}else{e.show()}return true},_destroy:function(){this.uiBox.replaceWith(this.element)},_setOption:function(a,c){var b=this;if(a==="id"||a==="name"){return}if(a==="disabled"){this._setDisabled(c)}this._super(a,c)},refresh:function(){this._refresh()},_refresh:function(){this._position();$.coral.refreshChild(this.element)}});