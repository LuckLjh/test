/**
 * 组件库4.0：树
 * 
 * 依赖JS文件:
 * jquery.parser.js
 * 
 * 
 */	
$.extend($.coral, { tree: { version: "4.0.2" } });
	
//树组件静态常量
var _final = {
	className: {
		BUTTON: "button",
		LEVEL: "level",
		ICO_LOADING: "ico_loading",
		SWITCH: "switch"
	},
	editIcon: "cui-icon-pencil7",
	removeIcon: "cui-icon-cross2",
	event: {
		NODECREATED: "tree_nodeCreated",
		CLICK: "tree_click",
		EXPAND: "tree_expand",
		COLLAPSE: "tree_collapse",
		ASYNC_SUCCESS: "tree_async_success",
		ASYNC_ERROR: "tree_async_error",
		REMOVE: "tree_remove",
		CHECK: "tree_check",
		DRAG: "tree_drag",
		DROP: "tree_drop",
		RENAME: "tree_rename",
		DRAGMOVE:"tree_dragmove"
	},
	id: {
		A: "_a",
		ICON: "_ico",
		SPAN: "_span",
		SWITCH: "_switch",
		UL: "_ul",
		CHECK: "_check",
		EDIT: "_edit",
		INPUT: "_input",
		REMOVE: "_remove"
	},
	line: {
		CENTER: "centerDottedLine",
		BOTTOM: "bottomDottedLine",
		ROOTS: "topDottedLine",
		NOLINE: "noline",
		LINE: "dottedLine"
	},
	folder: {
		OPEN: "folderOpen",
		CLOSE: "folderClose",
		FILE: "file",
		DOCU: "docu"
	},
	arrow: {
		RIGHT: "arrowRight1",
		DOWN: "arrowDown1"
	},
	node: {
		CURHOVER: "coral-state-hover",
		CURSELECTED: "curSelectedNode",
		CURSELECTED_EDIT: "curSelectedNode_Edit",
		TMPTARGET_TREE: "tmpTargetTree",
		TMPTARGET_NODE: "tmpTargetNode"
	},		
	checkbox: {
		STYLE: "checkbox",
		DEFAULT: "coral-state-default",
		DISABLED: "coral-state-disabled",
		FALSE: "false",
		TRUE: "true",
		UNCHECKED:"unchecked",
		CHECKED: "checked",
		PARTIAL: "Partical",
		FOCUS: "coral-state-focus"
	},
	radio: {
		STYLE: "radio",
		TYPE_ALL: "all",
		TYPE_LEVEL: "level"
	},
	move: {
		TYPE_INNER: "inner",
		TYPE_PREV: "prev",
		TYPE_NEXT: "next"
	}
},
//树组件属性
_option = {
	treeId: "",//Tree的唯一标识，初始化后，等于 用户定义的 Tree 容器的 id 属性值。
	treeObj: null,//Tree 容器的 jQuery 对象
	//view
	clickExpand: false,
	disabled: false,
	addDiyDom: null,//用于在节点上固定显示用户自定义控件
	autoCancelSelected: true,//点击节点时，按下 Ctrl 或 Cmd 键是否允许取消选择操作
	dblClickExpand: true,//双击节点时，是否自动展开父节点的标识
	expandSpeed: "fast",//节点展开、折叠时的动画速度，设置方法同 JQuery 动画效果中 speed 参数
	fontCss: {},//个性化文字样式，只针对 Tree 在节点上显示的<A>对象。
	iconUrl: true,//自定义图标是用url还是用fonts里的图标
	nameIsHTML: false,//设置 name 属性是否支持 HTML 脚本
	selectedMulti: true,//设置是否允许同时选中多个节点
	showIcon: true,//设置 Tree 是否显示节点的图标
	showLine: false,//设置 Tree 是否显示节点之间的连线
	enableHighlight: false,
	showTitle: true,//设置 Tree 是否显示节点的 title 提示信息(即节点 DOM 的 title 属性)
	txtSelectedEnable: false,//设置 Tree 是否允许可以选择 Tree DOM 内的文本
	rootInNode : false,//是否在data属性中提取根节点
	showRootNode : true,//是否显示根节点
	rootNode: false,//设置根节点 boolean或者json节点类型 如果是true则将树的nodes的根作为根节点，如果是json，则将传进来的node作为根节点
	//$.tree._key
	keyChildren: "children",//Tree 节点数据中保存子节点数据的属性名称
	keyName: "name",//Tree 节点数据保存节点名称的属性名称
	keyTitle: "",//Tree 节点数据保存节点提示信息的属性名称
	keyUrl: "url",//Tree 节点数据保存节点链接的目标 URL 的属性名称
	//$.tree._simpleData
	simpleDataEnable: false,//Nodes 数据是否采用简单数据模式
	simpleDataIdKey: "id",//节点数据中保存唯一标识的属性名称
	simpleDataPIdKey: "pId",//节点数据中保存其父节点唯一标识的属性名称
	simpleDataRootPId: null,//用于修正根节点父节点数据，即 pIdKey 指定的属性值
	//$.tree._keep
	keepParent: false,//Tree 的节点父节点属性锁，是否始终保持 isParent = true
	keepLeaf: false,//Tree 的节点叶子节点属性锁，是否始终保持 isParent = false
	//async
	asyncEnable: false,//设置 Tree 是否开启异步加载模式
	asyncContentType: "application/x-www-form-urlencoded",//Ajax 提交参数的数据类型
	asyncType: "post",//Ajax 的 http 请求模式
	asyncDataType: "text",//Ajax 获取的数据类型
	asyncUrl: "",//Ajax 获取数据的 URL 地址
	asyncAutoParam: [],//异步加载时需要自动提交父节点属性的参数
	asyncOtherParam: [],//Ajax 请求提交的静态参数键值对
	asyncDataFilter: null,//用于对 Ajax 返回数据进行预处理的函数
	
	//多选树属性
	checkable: false,//设置 Tree 的节点上是否显示 checkbox / radio
	autoCheckTrigger: false,//设置自动关联勾选时是否触发 beforeCheck / onCheck 事件回调函数
	chkStyle: _final.checkbox.STYLE,//勾选框类型(checkbox 或 radio）
	nocheckInherit: false,//当父节点设置 nocheck = true 时，设置子节点是否自动继承 nocheck = true 
	chkDisabledInherit: false,//当父节点设置 chkDisabled = true 时，设置子节点是否自动继承 chkDisabled = true 
	radioType: _final.radio.TYPE_LEVEL,//radio 的分组范围 level / all
	chkboxType: {//勾选 checkbox 对于父子节点的关联关系
		"Y": "ps",
		"N": "ps"
	},
	//$.tree._key
	keyChecked: "checked",//Tree 节点数据中保存 check 状态的属性名称
	//可编辑状态的属性
	editable: false,//设置 Tree 是否处于编辑状态
	editNameSelectAll: false,//节点编辑名称 input 初次显示时,设置 txt 内容是否为全选状态
	showRemoveBtn: true,//设置是否显示删除按钮
	showRenameBtn: true,//设置是否显示编辑名称按钮
	removeTitle: "remove",//删除按钮的 Title 辅助信息
	renameTitle: "rename",//编辑名称按钮的 Title 辅助信息
	//edit.drag
	dragStyle: "line", //"line" 拖动节点时的提示方式，是箭头提示还是线形提示
	dragAutoExpandTrigger: false,//拖拽时父节点自动展开是否触发 onExpand 事件回调函数
	dragIsCopy: true,//拖拽时, 设置是否允许复制节点
	dragIsMove: true,//拖拽时, 设置是否允许移动节点
	dragPrev: true,//拖拽到目标节点时，设置是否允许移动到目标节点前面的操作
	dragNext: true,//拖拽到目标节点时，设置是否允许移动到目标节点后面的操作
	dragInner: true,//拖拽到目标节点时，设置是否允许成为目标节点的子节点
	dragMinMoveSize: 5,//判定是否拖拽操作的最小位移值 (单位：px)
	dragBorderMax: 10,//拖拽节点成为根节点时的 Tree 内边界范围 (单位：px)
	dragBorderMin: -5,//拖拽节点成为根节点时的 Tree 外边界范围 (单位：px)
	dragMaxShowNodeNum: 5,//拖拽多个兄弟节点时，浮动图层中显示的最大节点数
	dragAutoOpenTime: 500,//拖拽时父节点自动展开的延时间隔
	//view
	addHoverDom: null,//用于当鼠标移动到节点上时，显示用户自定义控件
	removeHoverDom: null,//用于当鼠标移出节点时，隐藏用户自定义控件
	//回调函数
	beforeDrag:null,//用于捕获节点被拖拽之前的事件回调函数，并且根据返回值确定是否允许开启拖拽操作
	beforeDragOpen:null,//用于捕获拖拽节点移动到折叠状态的父节点后，即将自动展开该父节点之前的事件回调函数，并且根据返回值确定是否允许自动展开操作
	beforeDrop:null,//用于捕获节点拖拽操作结束之前的事件回调函数，并且根据返回值确定是否允许此拖拽操作
	beforeEditName:null,//用于捕获节点编辑按钮的 click 事件，并且根据返回值确定是否允许进入名称编辑状态
	beforeReName:null,//用于捕获节点编辑名称结束,更新节点名称数据之前的事件回调函数
	onDrag:null,//用于捕获节点被拖拽的事件回调函数
	onDragMove:null,//用于捕获节点被拖拽过程中移动的事件回调函数
	onDrop:null,//用于捕获节点拖拽操作结束的事件回调函数
	onReName:null,//用于捕获节点编辑名称结束之后的事件回调函数
	
	//callback
	beforeCheck:null,//用于捕获 勾选 或 取消勾选 之前的事件回调函数，并且根据返回值确定是否允许 勾选 或 取消勾选 
	onCheck:null,//用于捕获 checkbox / radio 被勾选 或 取消勾选的事件回调函数
	
	//callback
	beforeAsync:null,//用于捕获异步加载之前的事件回调函数，Tree 根据返回值确定是否允许进行异步加载
	beforeClick:null,//用于捕获单击节点之前的事件回调函数，并且根据返回值确定是否允许单击操作
	beforeDblClick:null,//用于捕获 Tree 上鼠标双击之前的事件回调函数，并且根据返回值确定触发 onDblClick 事件回调函数
	beforeRightClick:null,//用于捕获 Tree 上鼠标右键点击之前的事件回调函数，并且根据返回值确定触发 onRightClick 事件回调函数
	beforeMouseDown:null,//用于捕获 Tree 上鼠标按键按下之前的事件回调函数，并且根据返回值确定触发 onMouseDown 事件回调函数
	beforeMouseUp:null,//用于捕获 Tree 上鼠标按键松开之前的事件回调函数，并且根据返回值确定触发 onMouseUp 事件回调函数
	beforeExpand:null,//用于捕获父节点展开之前的事件回调函数，并且根据返回值确定是否允许展开操作
	beforeCollapse:null,//用于捕获父节点折叠之前的事件回调函数，并且根据返回值确定是否允许折叠操作
	beforeRemove:null,//用于捕获节点被删除之前的事件回调函数，并且根据返回值确定是否允许删除操作

	onLoadError:null,//用于捕获异步加载出现异常错误的事件回调函数
	onLoad:null,//用于捕获异步加载正常结束的事件回调函数
	onNodeCreated:null,//用于捕获节点生成 DOM 后的事件回调函数
	onClick:null,//用于捕获节点被点击的事件回调函数
	onDblClick:null,//用于捕获 Tree 上鼠标双击之后的事件回调函数
	onRightClick:null,//用于捕获 Tree 上鼠标右键点击之后的事件回调函数
	onMouseDown:null,//用于捕获 Tree 上鼠标按键按下后的事件回调函数
	onMouseUp:null,//用于捕获 Tree 上鼠标按键松开后的事件回调函数
	onExpand:null,//用于捕获节点被展开的事件回调函数
	onCollapse:null,//用于捕获节点被折叠的事件回调函数
	onRemove:null//用于捕获删除节点之后的事件回调函数
		
};

$.tree = {
	//树组件使用root来保存全部数据
	//普通树初始化root
	_coreInitRoot : function (setting) {
		var root = $.tree._getRoot(setting);
		if (!root) {
			root ={};
			$.tree._setRoot(setting, root);
		}
		root[setting.keyChildren] = [];
		root.expandTriggerFlag = false;
		root.curSelectedList = [];
		root.noSelection = true;
		root.createdNodes = [];
		root.zId = 0;
		root._ver = (new Date()).getTime();
	},
	//多选树初始化root
	_checkInitRoot : function (setting) {
		var root = $.tree._getRoot(setting);
		root.radioCheckedList = [];
	},
	//可编辑树初始化root
	_editInitRoot : function (setting) {
		var root = $.tree._getRoot(setting), roots = $.tree._getRoots();
		root.curEditNode = null;
		root.curEditInput = null;
		root.curHoverNode = null;
		root.dragFlag = 0;
		root.dragNodeShowBefore = [];
		root.dragMaskList = new Array();
		roots.showHoverDom = true;
	},
	//普通树初始化cache
	_coreInitCache : function(setting) {
		var cache = $.tree._getCache(setting);
		if (!cache) {
			cache = {};
			$.tree._setCache(setting, cache);
		}
		cache.nodes = [];
		cache.doms = [];
	},
	//多选树初始化cache
	_checkInitCache : function(treeId) {},
	//可编辑树初始化cache
	_editInitCache : function(treeId) {},
	//普通树绑定事件
	_coreBindEvent : function(setting) {
		var treeObj = setting.treeObj,
		cache = consts.event;
		treeObj.bind(cache.NODECREATED, function (event, treeId, node) {
			$.tree._apply(setting.onNodeCreated, [event, treeId, node, setting]);
		});

		treeObj.bind(cache.CLICK, function (event, srcEvent, treeId, node, clickFlag) {
			$.tree._apply(setting.onClick, [srcEvent, treeId, node, clickFlag, setting]);
		});

		treeObj.bind(cache.EXPAND, function (event, treeId, node) {
			$.tree._apply(setting.onExpand, [event, treeId, node, setting]);
		});

		treeObj.bind(cache.COLLAPSE, function (event, treeId, node) {
			$.tree._apply(setting.onCollapse, [event, treeId, node, setting]);
		});

		treeObj.bind(cache.ASYNC_SUCCESS, function (event, treeId, node, msg) {
			$.tree._apply(setting.onLoad, [event, treeId, node, msg, setting]);
		});

		treeObj.bind(cache.ASYNC_ERROR, function (event, treeId, node, XMLHttpRequest, textStatus, errorThrown) {
			$.tree._apply(setting.onLoadError, [event, treeId, node, XMLHttpRequest, textStatus, errorThrown, setting]);
		});

		treeObj.bind(cache.REMOVE, function (event, treeId, treeNode) {
			$.tree._apply(setting.onRemove, [event, treeId, treeNode, setting]);
		});
	},
	//多选树绑定事件
	_checkBindEvent : function(setting) {
		var treeObj = setting.treeObj,
		cache = consts.event;
		treeObj.bind(cache.CHECK, function (event, srcEvent, treeId, node) {
			event.srcEvent = srcEvent;
			$.tree._apply(setting.onCheck, [event, treeId, node, setting]);
		});
	},
	//可编辑树绑定事件
	_editBindEvent : function(setting) {
		var treeObj = setting.treeObj;
		var cache = consts.event;
		treeObj.bind(cache.RENAME, function (event, treeId, treeNode, isCancel) {
			$.tree._apply(setting.onReName, [event, treeId, treeNode, isCancel, setting]);
		});

		treeObj.bind(cache.DRAG, function (event, srcEvent, treeId, treeNodes) {
			$.tree._apply(setting.onDrag, [srcEvent, treeId, treeNodes, setting]);
		});

		treeObj.bind(cache.DRAGMOVE,function(event, srcEvent, treeId, treeNodes){
			$.tree._apply(setting.onDragMove,[srcEvent, treeId, treeNodes, setting]);
		});

		treeObj.bind(cache.DROP, function (event, srcEvent, treeId, treeNodes, targetNode, moveType, isCopy) {
			$.tree._apply(setting.onDrop, [srcEvent, treeId, treeNodes, targetNode, moveType, isCopy, setting]);
		});
	},
	//普通树松绑事件
	_coreUnbindEvent : function(setting) {
		var treeObj = setting.treeObj,
		cache = consts.event;
		treeObj.unbind(cache.NODECREATED)
		.unbind(cache.CLICK)
		.unbind(cache.EXPAND)
		.unbind(cache.COLLAPSE)
		.unbind(cache.ASYNC_SUCCESS)
		.unbind(cache.ASYNC_ERROR)
		.unbind(cache.REMOVE);
	},
	//多选树松绑事件
	_checkUnbindEvent : function(setting) {
		var treeObj = setting.treeObj;
		var cache = consts.event;
		treeObj.unbind(cache.CHECK);
	},
	//可编辑树松绑事件
	_editUnbindEvent : function(setting) {
		var treeObj = setting.treeObj;
		var cache = consts.event;
		treeObj.unbind(cache.RENAME);
		treeObj.unbind(cache.DRAG);
		treeObj.unbind(cache.DRAGMOVE);
		treeObj.unbind(cache.DROP);
	},
	//普通树代理事件
	_coreEventProxy : function(event) {
		var target = event.target,
		setting = $.tree._getSetting(event.data.treeId),
		tId = "", node = null,
		nodeEventType = "", treeEventType = "",
		nodeEventCallback = null, treeEventCallback = null,
		tmp = null;

		if ($.tree._eqs(event.type, "mousedown")) {
			treeEventType = "mousedown";
		} else if ($.tree._eqs(event.type, "mouseup")) {
			treeEventType = "mouseup";
		} else if ($.tree._eqs(event.type, "contextmenu")) {
			treeEventType = "contextmenu";
		} else if ($.tree._eqs(event.type, "click")) {
			if ($.tree._eqs(target.tagName, "span") && target.getAttribute("treeNode"+ consts.id.SWITCH) !== null) {
				tId = $.tree._getNodeMainDom(target).id;
				nodeEventType = "switchNode";
			} else {
				tmp = $.tree._getMDom(setting, target, [{tagName:"a", attrName:"treeNode"+consts.id.A}]);
				if (tmp) {
					tId = $.tree._getNodeMainDom(tmp).id;
					nodeEventType = "clickNode";
				}
			}
		} else if ($.tree._eqs(event.type, "dblclick")) {
			treeEventType = "dblclick";
			tmp = $.tree._getMDom(setting, target, [{tagName:"a", attrName:"treeNode"+consts.id.A}]);
			if (tmp) {
				tId = $.tree._getNodeMainDom(tmp).id;
				nodeEventType = "switchNode";
			}
		}
		if (treeEventType.length > 0 && tId.length == 0) {
			tmp = $.tree._getMDom(setting, target, [{tagName:"a", attrName:"treeNode"+consts.id.A}]);
			if (tmp) {tId = $.tree._getNodeMainDom(tmp).id;}
		}
		// event to node
		if (tId.length>0) {
			node = $.tree._getNodeCache(setting, tId);
			switch (nodeEventType) {
				case "switchNode" :
					if (!node.isParent) {
						nodeEventType = "";
					} else if ($.tree._eqs(event.type, "click")
						|| ($.tree._eqs(event.type, "dblclick") && $.tree._apply(setting.dblClickExpand, [setting.treeId, node, setting], setting.dblClickExpand))) {
						nodeEventCallback = $.tree._onSwitchNode;
					} else {
						nodeEventType = "";
					}
					break;
				case "clickNode" :
					nodeEventCallback = $.tree._onClickNode;
					break;
			}
		}
		// event to Tree
		switch (treeEventType) {
			case "mousedown" :
				treeEventCallback = $.tree._onTreeMousedown;
				break;
			case "mouseup" :
				treeEventCallback = $.tree._onTreeMouseup;
				break;
			case "dblclick" :
				treeEventCallback = $.tree._onTreeDblclick;
				break;
			case "contextmenu" :
				treeEventCallback = $.tree._onTreeContextmenu;
				break;
		}
		var proxyResult = {
			stop: false,
			node: node,
			nodeEventType: nodeEventType,
			nodeEventCallback: nodeEventCallback,
			treeEventType: treeEventType,
			treeEventCallback: treeEventCallback
		};
		return proxyResult
	},
	//多选树代理事件
	_checkEventProxy : function(e) {
		var target = e.target,
		setting = $.tree._getSetting(e.data.treeId),
		tId = "", node = null,
		nodeEventType = "", treeEventType = "",
		nodeEventCallback = null, treeEventCallback = null;

		if ($.tree._eqs(e.type, "mouseover")) {
			if (setting.checkable && $.tree._eqs(target.tagName, "span") && target.getAttribute("treeNode"+ consts.id.CHECK) !== null) {
				tId = $.tree._getNodeMainDom(target).id;
				nodeEventType = "mouseoverCheck";
			}
		} else if ($.tree._eqs(e.type, "mouseout")) {
			if (setting.checkable && $.tree._eqs(target.tagName, "span") && target.getAttribute("treeNode"+ consts.id.CHECK) !== null) {
				tId = $.tree._getNodeMainDom(target).id;
				nodeEventType = "mouseoutCheck";
			}
		} else if ($.tree._eqs(e.type, "click")) {
			if (setting.checkable && $.tree._eqs(target.tagName, "span") && target.getAttribute("treeNode"+ consts.id.CHECK) !== null) {
				tId = $.tree._getNodeMainDom(target).id;
				nodeEventType = "checkNode";
			}
		}
		if (tId.length>0) {
			node = $.tree._getNodeCache(setting, tId);
			switch (nodeEventType) {
				case "checkNode" :
					nodeEventCallback = $.tree._onCheckNode;
					break;
				case "mouseoverCheck" :
					nodeEventCallback = $.tree._onMouseoverCheck;
					break;
				case "mouseoutCheck" :
					nodeEventCallback = $.tree._onMouseoutCheck;
					break;
			}
		}
		var proxyResult = {
			stop: nodeEventType === "checkNode",
			node: node,
			nodeEventType: nodeEventType,
			nodeEventCallback: nodeEventCallback,
			treeEventType: treeEventType,
			treeEventCallback: treeEventCallback
		};
		return proxyResult
	},
	//可编辑树代理事件
	_editEventProxy : function(e) {
		var target = e.target,
		setting = $.tree._getSetting(e.data.treeId),
		relatedTarget = e.relatedTarget,
		tId = "", node = null,
		nodeEventType = "", treeEventType = "",
		nodeEventCallback = null, treeEventCallback = null,
		tmp = null;

		if ($.tree._eqs(e.type, "mouseover")) {
			tmp = $.tree._getMDom(setting, target, [{tagName:"a", attrName:"treeNode"+consts.id.A}]);
			if (tmp) {
				tId = $.tree._getNodeMainDom(tmp).id;
				nodeEventType = "hoverOverNode";
			}
		} else if ($.tree._eqs(e.type, "mouseout")) {
			tmp = $.tree._getMDom(setting, relatedTarget, [{tagName:"a", attrName:"treeNode"+consts.id.A}]);
			if (!tmp) {
				tId = "remove";
				nodeEventType = "hoverOutNode";
			}
		} else if ($.tree._eqs(e.type, "mousedown")) {
			tmp = $.tree._getMDom(setting, target, [{tagName:"a", attrName:"treeNode"+consts.id.A}]);
			if (tmp) {
				tId = $.tree._getNodeMainDom(tmp).id;
				nodeEventType = "mousedownNode";
			}
		}
		if (tId.length>0) {
			node = $.tree._getNodeCache(setting, tId);
			switch (nodeEventType) {
				case "mousedownNode" :
					nodeEventCallback = $.tree._onMousedownNode;
					break;
				case "hoverOverNode" :
					nodeEventCallback = $.tree._onHoverOverNode;
					break;
				case "hoverOutNode" :
					nodeEventCallback = $.tree._onHoverOutNode;
					break;
			}
		}
		var proxyResult = {
			stop: false,
			node: node,
			nodeEventType: nodeEventType,
			nodeEventCallback: nodeEventCallback,
			treeEventType: treeEventType,
			treeEventCallback: treeEventCallback
		};
		return proxyResult
	},
	//多选树节点前插入dom元素
	_beforeA : function(setting, node, html) {
		var checkedKey = setting.keyChecked;
		if (setting.checkable) {
			$.tree._makeChkFlag(setting, node);
			html.push("<span ID='", node.tId, consts.id.CHECK, "' class='", $.tree._makeChkClass(setting, node), "' treeNode", consts.id.CHECK, (node.nocheck === true?" style='display:none;'":""),"></span>");
		}
	},
	
	//普通树初始化节点
	_coreInitNode : function(setting, level, node, parentNode, isFirstNode, isLastNode, openFlag) {
		if (!node) return;
		var r = $.tree._getRoot(setting),
		childKey = setting.keyChildren;
		node.level = level;
		node.tId = setting.treeId + "_" + (++r.zId);
		node.parentTId = parentNode ? parentNode.tId : null;
		node.open = (typeof node.open == "string") ? $.tree._eqs(node.open, "true") : !!node.open;
		if (node[childKey] && node[childKey].length > 0) {
			node.isParent = true;
			node.zAsync = true;
		} else {
			node.isParent = (typeof node.isParent == "string") ? $.tree._eqs(node.isParent, "true") : !!node.isParent;
			node.open = (node.isParent && !setting.asyncEnable) ? node.open : false;
			node.zAsync = !node.isParent;
		}
		node.isFirstNode = isFirstNode;
		node.isLastNode = isLastNode;
		node.getParentNode = function() {return $.tree._getNodeCache(setting, node.parentTId);};
		node.getPreNode = function() {return $.tree._getPreNode(setting, node);};
		node.getNextNode = function() {return $.tree._getNextNode(setting, node);};
		node.isAjaxing = false;
		$.tree._fixPIdKeyValue(setting, node);
	},
	
	//多选树初始化节点
	_checkInitNode : function(setting, level, node, parentNode, isFirstNode, isLastNode, openFlag) {
		if (!node) return;
		var checkedKey = setting.keyChecked;
		if (typeof node[checkedKey] == "string") node[checkedKey] = $.tree._eqs(node[checkedKey], "true");
		node[checkedKey] = !!node[checkedKey];
		node.checkedOld = node[checkedKey];
		if (typeof node.nocheck == "string") node.nocheck = $.tree._eqs(node.nocheck, "true");
		node.nocheck = !!node.nocheck || (setting.nocheckInherit && parentNode && !!parentNode.nocheck);
		if (typeof node.chkDisabled == "string") node.chkDisabled = $.tree._eqs(node.chkDisabled, "true");
		node.chkDisabled = !!node.chkDisabled || (setting.chkDisabledInherit && parentNode && !!parentNode.chkDisabled);
		if (typeof node.halfCheck == "string") node.halfCheck = $.tree._eqs(node.halfCheck, "true");
		node.halfCheck = !!node.halfCheck;
		node.check_Child_State = -1;
		node.check_Focus = false;
		node.getCheckStatus = function() {return $.tree._getCheckStatus(setting, node);};

		if (setting.chkStyle == consts.radio.STYLE && setting.radioType == consts.radio.TYPE_ALL && node[checkedKey] ) {
			var r = $.tree._getRoot(setting);
			r.radioCheckedList.push(node);
		}
	},
	//可编辑树初始化节点
	_editInitNode : function(setting, level, node, parentNode, isFirstNode, isLastNode, openFlag) {
		if (!node) return;
		node.isHover = false;
		node.editNameFlag = false;
	},
	//更新 TreeObj, 增加部分多选树的工具方法
	_checkTreeTools : function(setting, treeTools) {
		treeTools.checkNode = function(node, checked, checkTypeFlag, callbackFlag) {
			var checkedKey = this.setting.keyChecked;
			if (node.chkDisabled === true) return;
			if (checked !== true && checked !== false) {
				checked = !node[checkedKey];
			}
			callbackFlag = !!callbackFlag;

			if (node[checkedKey] === checked && !checkTypeFlag) {
				return;
			} else if (callbackFlag && $.tree._apply(this.setting.beforeCheck, [this.setting.treeId, node,this.setting], true) == false) {
				return;
			}
			if ($.tree._uCanDo(this.setting) && this.setting.checkable && node.nocheck !== true) {
				node[checkedKey] = checked;
				var checkObj = $$(node, consts.id.CHECK, this.setting);
				if (checkTypeFlag || this.setting.chkStyle === consts.radio.STYLE) $.tree._checkNodeRelation(this.setting, node);
				$.tree._setChkClass(this.setting, checkObj, node);
				$.tree._repairParentChkClassWithSelf(this.setting, node);
				if (callbackFlag) {
					this.setting.treeObj.trigger(consts.event.CHECK, [null, this.setting.treeId, node]);
				}
			}
		}

		treeTools.checkAllNodes = function(checked) {
			$.tree._repairAllChk(this.setting, !!checked);
		}

		treeTools.getCheckedNodes = function(checked) {
			var childKey = this.setting.keyChildren;
			checked = (checked !== false);
			return $.tree._getTreeCheckedNodes(this.setting, $.tree._getRoot(this.setting)[childKey], checked);
		}

		treeTools.getChangeCheckedNodes = function() {
			var childKey = this.setting.keyChildren;
			return $.tree._getTreeChangeCheckedNodes(this.setting, $.tree._getRoot(this.setting)[childKey]);
		}

		treeTools.setChkDisabled = function(node, disabled, inheritParent, inheritChildren) {
			disabled = !!disabled;
			inheritParent = !!inheritParent;
			inheritChildren = !!inheritChildren;
			$.tree._repairSonChkDisabled(this.setting, node, disabled, inheritChildren);
			$.tree._repairParentChkDisabled(this.setting, node.getParentNode(), disabled, inheritParent);
		}

		var _updateNode = treeTools.updateNode;
		treeTools.updateNode = function(node, checkTypeFlag) {
			if (_updateNode) _updateNode.apply(treeTools, arguments);
			if (!node || !this.setting.checkable) return;
			var nObj = $$(node, this.setting);
			if (nObj.get(0) && $.tree._uCanDo(this.setting)) {
				var checkObj = $$(node, consts.id.CHECK, this.setting);
				if (checkTypeFlag == true || this.setting.chkStyle === consts.radio.STYLE) $.tree._checkNodeRelation(this.setting, node);
				$.tree._setChkClass(this.setting, checkObj, node);
				$.tree._repairParentChkClassWithSelf(this.setting, node);
			}
		}
	},
	//更新 TreeObj, 增加部分可编辑树的工具方法
	_editTreeTools : function(setting, treeTools) {
		treeTools.cancelEditName = function(newName) {
			var root = $.tree._getRoot(this.setting);
			if (!root.curEditNode) return;
			$.tree._cancelCurEditNode(this.setting, newName?newName:null, true);
		}
		treeTools.copyNode = function(targetNode, node, moveType, isSilent) {
			if (!node) return null;
			if (targetNode && !targetNode.isParent && this.setting.keepLeaf && moveType === consts.move.TYPE_INNER) return null;
			var _this = this,
				newNode = $.tree._clone(node);
			if (!targetNode) {
				targetNode = null;
				moveType = consts.move.TYPE_INNER;
			}
			if (moveType == consts.move.TYPE_INNER) {
				function copyCallback() {
					$.tree._addNodes(_this.setting, targetNode, [newNode], isSilent);
				}

				if ($.tree._canAsync(this.setting, targetNode)) {
					$.tree._asyncNode(this.setting, targetNode, isSilent, copyCallback);
				} else {
					copyCallback();
				}
			} else {
				$.tree._addNodes(this.setting, targetNode.parentNode, [newNode], isSilent);
				$.tree._moveNode(this.setting, targetNode, newNode, moveType, false, isSilent);
			}
			return newNode;
		}
		treeTools.editName = function(node) {
			if (!node || !node.tId || node !== $.tree._getNodeCache(this.setting, node.tId)) return;
			if (node.parentTId) $.tree._expandCollapseParentNode(this.setting, node.getParentNode(), true);
			$.tree._editNode(this.setting, node)
		}
		treeTools.moveNode = function(targetNode, node, moveType, isSilent) {
			if (!node) return node;
			if (targetNode && !targetNode.isParent && this.setting.keepLeaf && moveType === consts.move.TYPE_INNER) {
				return null;
			} else if (targetNode && ((node.parentTId == targetNode.tId && moveType == consts.move.TYPE_INNER) || $$(node, this.setting).find("#" + targetNode.tId).length > 0)) {
				return null;
			} else if (!targetNode) {
				targetNode = null;
			}
			var _this = this;
			function moveCallback() {
				$.tree._moveNode(_this.setting, targetNode, node, moveType, false, isSilent);
			}
			if ($.tree._canAsync(this.setting, targetNode) && moveType === consts.move.TYPE_INNER) {
				$.tree._asyncNode(this.setting, targetNode, isSilent, moveCallback);
			} else {
				moveCallback();
			}
			return node;
		}
		treeTools.setEditable = function(editable) {
			this.setting.editable = editable;
			return this.refresh();
		}
	},
	
	//method of operate data
	_addNodeCache: function(setting, node) {
		$.tree._getCache(setting).nodes[$.tree._getNodeCacheId(node.tId)] = node;
	},
	_getNodeCacheId: function(tId) {
		return tId.substring(tId.lastIndexOf("_")+1);
	},
	_addNodesData: function(setting, parentNode, nodes) {
		var childKey = setting.keyChildren;
		if (!parentNode[childKey]) parentNode[childKey] = [];
		if (parentNode[childKey].length > 0) {
			parentNode[childKey][parentNode[childKey].length - 1].isLastNode = false;
			$.tree._setNodeLineIcos(setting, parentNode[childKey][parentNode[childKey].length - 1]);
		}
		parentNode.isParent = true;
		// rootNode根节点设置了为true，并且不显示根节点的时候需要处理node
		// TODO: rootNode 为数组的时候reload是否会报错
		/*if( setting.isInit && !setting.showRootNode && setting.rootNode ){
			var children = (nodes.length&&nodes[0][childKey])?nodes[0][childKey]:[];
			if ( children.length > 0 )
				parentNode[childKey] = parentNode[childKey].concat(children);
		} else {
			parentNode[childKey] = parentNode[childKey].concat(nodes);
		}*/
		parentNode[childKey] = parentNode[childKey].concat(nodes);
	},
	_addSelectedNode: function(setting, node) {
		var root = $.tree._getRoot(setting);
		if (!$.tree._isSelectedNode(setting, node)) {
			root.curSelectedList.push(node);
		}
	},
	_addCreatedNode: function(setting, node) {
		if (!!setting.onNodeCreated || !!setting.addDiyDom) {
			var root = $.tree._getRoot(setting);
			root.createdNodes.push(node);
		}
	},

	_fixPIdKeyValue: function(setting, node) {
		if (setting.simpleDataEnable) {
			node[setting.simpleDataPIdKey] = node.parentTId ? node.getParentNode()[setting.simpleDataIdKey] : setting.simpleDataRootPId;
		}
	},
	_getAfterA: function(setting, node, array) {
		for (var i=0, j=_init.afterA.length; i<j; i++) {
			_init.afterA[i].apply(this, arguments);
		}
	},
	_getBeforeA: function(setting, node, array) {
		for (var i=0, j=_init.beforeA.length; i<j; i++) {
			_init.beforeA[i].apply(this, arguments);
		}
	},
	_getInnerAfterA: function(setting, node, array) {
		for (var i=0, j=_init.innerAfterA.length; i<j; i++) {
			_init.innerAfterA[i].apply(this, arguments);
		}
	},
	_getInnerBeforeA: function(setting, node, array) {
		for (var i=0, j=_init.innerBeforeA.length; i<j; i++) {
			_init.innerBeforeA[i].apply(this, arguments);
		}
	},
	_getCache: function(setting) {
		return caches[setting.treeId];
	},
	_getNextNode: function(setting, node) {
		if (!node) return null;
		var childKey = setting.keyChildren,
		p = node.parentTId ? node.getParentNode() : $.tree._getRoot(setting);
		for (var i=0, l=p[childKey].length-1; i<=l; i++) {
			if (p[childKey][i] === node) {
				return (i==l ? null : p[childKey][i+1]);
			}
		}
		return null;
	},
	_getNodeByParam: function(setting, nodes, key, value) {
		if (!nodes || !key) return null;
		var childKey = setting.keyChildren;
		for (var i = 0, l = nodes.length; i < l; i++) {
			if (nodes[i][key] == value) {
				return nodes[i];
			}
			var tmp = $.tree._getNodeByParam(setting, nodes[i][childKey], key, value);
			if (tmp) return tmp;
		}
		return null;
	},
	_getNodeCache: function(setting, tId) {
		if (!tId) return null;
		var n = caches[setting.treeId].nodes[$.tree._getNodeCacheId(tId)];
		return n ? n : null;
	},
	_getNodeName: function(setting, node) {
		var nameKey = setting.keyName;
		return "" + node[nameKey];
	},
	_getNodeTitle: function(setting, node) {
		var t = setting.keyTitle === "" ? setting.keyName : setting.keyTitle;
		return "" + node[t];
	},
	_getNodes: function(setting) {
		return $.tree._getRoot(setting)[setting.keyChildren];
	},
	_getNodesByParam: function(setting, nodes, key, value) {
		if (!nodes || !key) return [];
		var childKey = setting.keyChildren,
		result = [];
		for (var i = 0, l = nodes.length; i < l; i++) {
			if (nodes[i][key] == value) {
				result.push(nodes[i]);
			}
			result = result.concat($.tree._getNodesByParam(setting, nodes[i][childKey], key, value));
		}
		return result;
	},
	_getNodesByParamFuzzy: function(setting, nodes, key, value) {
		if (!nodes || !key) return [];
		var childKey = setting.keyChildren,
		result = [];
		value = value.toLowerCase();
		for (var i = 0, l = nodes.length; i < l; i++) {
			if (typeof nodes[i][key] == "string" && nodes[i][key].toLowerCase().indexOf(value)>-1) {
				result.push(nodes[i]);
			}
			result = result.concat($.tree._getNodesByParamFuzzy(setting, nodes[i][childKey], key, value));
		}
		return result;
	},
	_getNodesByFilter: function(setting, nodes, filter, isSingle, invokeParam) {
		if (!nodes) return (isSingle ? null : []);
		var childKey = setting.keyChildren,
		result = isSingle ? null : [];
		for (var i = 0, l = nodes.length; i < l; i++) {
			if ($.tree._apply(filter, [nodes[i], invokeParam], false)) {
				if (isSingle) {return nodes[i];}
				result.push(nodes[i]);
			}
			var tmpResult = $.tree._getNodesByFilter(setting, nodes[i][childKey], filter, isSingle, invokeParam);
			if (isSingle && !!tmpResult) {return tmpResult;}
			result = isSingle ? tmpResult : result.concat(tmpResult);
		}
		return result;
	},
	_getPreNode: function(setting, node) {
		if (!node) return null;
		var childKey = setting.keyChildren,
		p = node.parentTId ? node.getParentNode() : $.tree._getRoot(setting);
		for (var i=0, l=p[childKey].length; i<l; i++) {
			if (p[childKey][i] === node) {
				return (i==0 ? null : p[childKey][i-1]);
			}
		}
		return null;
	},
	_getRadioCheckedList: function(setting) {
		var checkedList = $.tree._getRoot(setting).radioCheckedList;
		for (var i=0, j=checkedList.length; i<j; i++) {
			if(!$.tree._getNodeCache(setting, checkedList[i].tId)) {
				checkedList.splice(i, 1);
				i--; j--;
			}
		}
		return checkedList;
	},
	_getCheckStatus: function(setting, node) {
		if (!setting.checkable || node.nocheck || node.chkDisabled) return null;
		var checkedKey = setting.keyChecked,
		r = {
			checked: node[checkedKey],
			half: node.halfCheck ? node.halfCheck : (setting.chkStyle == consts.radio.STYLE ? (node.check_Child_State === 2) : (node[checkedKey] ? (node.check_Child_State > -1 && node.check_Child_State < 2) : (node.check_Child_State > 0)))
		};
		return r;
	},
	_getTreeCheckedNodes: function(setting, nodes, checked, results) {
		if (!nodes) return [];
		var childKey = setting.keyChildren,
		checkedKey = setting.keyChecked,
		onlyOne = (checked && setting.chkStyle == consts.radio.STYLE && setting.radioType == consts.radio.TYPE_ALL);
		results = !results ? [] : results;
		for (var i = 0, l = nodes.length; i < l; i++) {
			if (nodes[i].nocheck !== true && nodes[i].chkDisabled !== true && nodes[i][checkedKey] == checked) {
				results.push(nodes[i]);
				if(onlyOne) {
					break;
				}
			}
			$.tree._getTreeCheckedNodes(setting, nodes[i][childKey], checked, results);
			if(onlyOne && results.length > 0) {
				break;
			}
		}
		return results;
	},
	_getTreeChangeCheckedNodes: function(setting, nodes, results) {
		if (!nodes) return [];
		var childKey = setting.keyChildren,
		checkedKey = setting.keyChecked;
		results = !results ? [] : results;
		for (var i = 0, l = nodes.length; i < l; i++) {
			if (nodes[i].nocheck !== true && nodes[i].chkDisabled !== true && nodes[i][checkedKey] != nodes[i].checkedOld) {
				results.push(nodes[i]);
			}
			$.tree._getTreeChangeCheckedNodes(setting, nodes[i][childKey], results);
		}
		return results;
	},
	_getRoot: function(setting) {
		return setting ? roots[setting.treeId] : null;
	},
	_getRoots: function() {
		return roots;
	},
	_getSetting: function(treeId) {
		return settings[treeId];
	},
	_getSettings: function() {
		return settings;
	},
	_getTreeTools: function(treeId) {
		var r = this._getRoot(this._getSetting(treeId));
		return r ? r.treeTools : null;
	},
	_initCache: function(setting) {
		for (var i=0, j=_init.caches.length; i<j; i++) {
			_init.caches[i].apply(this, arguments);
		}
	},
	_initNode: function(setting, level, node, parentNode, preNode, nextNode) {
		for (var i=0, j=_init.nodes.length; i<j; i++) {
			_init.nodes[i].apply(this, arguments);
		}
	},
	_initRoot: function(setting) {
		for (var i=0, j=_init.roots.length; i<j; i++) {
			_init.roots[i].apply(this, arguments);
		}
	},
	_isSelectedNode: function(setting, node) {
		var root = $.tree._getRoot(setting);
		for (var i=0, j=root.curSelectedList.length; i<j; i++) {
			if(node === root.curSelectedList[i]) return true;
		}
		return false;
	},
	_removeNodeCache: function(setting, node) {
		var childKey = setting.keyChildren;
		if (node[childKey]) {
			for (var i=0, l=node[childKey].length; i<l; i++) {
				arguments.callee(setting, node[childKey][i]);
			}
		}
		$.tree._getCache(setting).nodes[$.tree._getNodeCacheId(node.tId)] = null;
	},
	_removeSelectedNode: function(setting, node) {
		var root = $.tree._getRoot(setting);
		for (var i=0, j=root.curSelectedList.length; i<j; i++) {
			if(node === root.curSelectedList[i] || !$.tree._getNodeCache(setting, root.curSelectedList[i].tId)) {
				root.curSelectedList.splice(i, 1);
				i--;j--;
			}
		}
	},
	_setCache: function(setting, cache) {
		caches[setting.treeId] = cache;
	},
	_setRoot: function(setting, root) {
		roots[setting.treeId] = root;
	},
	_setTreeTools: function(setting, treeTools) {
		for (var i=0, j=_init.treeTools.length; i<j; i++) {
			_init.treeTools[i].apply(this, arguments);
		}
	},
	_makeChkFlag: function(setting, node) {
		if (!node) return;
		var childKey = setting.keyChildren,
		checkedKey = setting.keyChecked,
		chkFlag = -1;
		if (node[childKey]) {
			for (var i = 0, l = node[childKey].length; i < l; i++) {
				var cNode = node[childKey][i];
				var tmp = -1;
				if (setting.chkStyle == consts.radio.STYLE) {
					if (cNode.nocheck === true || cNode.chkDisabled === true) {
						tmp = cNode.check_Child_State;
					} else if (cNode.halfCheck === true) {
						tmp = 2;
					} else if (cNode[checkedKey]) {
						tmp = 2;
					} else {
						tmp = cNode.check_Child_State > 0 ? 2:0;
					}
					if (tmp == 2) {
						chkFlag = 2; break;
					} else if (tmp == 0){
						chkFlag = 0;
					}
				} else if (setting.chkStyle == consts.checkbox.STYLE) {
					if (cNode.nocheck === true || cNode.chkDisabled === true) {
						tmp = cNode.check_Child_State;
					} else if (cNode.halfCheck === true) {
						tmp = 1;
					} else if (cNode[checkedKey] ) {
						tmp = (cNode.check_Child_State === -1 || cNode.check_Child_State === 2) ? 2 : 1;
					} else {
						tmp = (cNode.check_Child_State > 0) ? 1 : 0;
					}
					if (tmp === 1) {
						chkFlag = 1; break;
					} else if (tmp === 2 && chkFlag > -1 && i > 0 && tmp !== chkFlag) {
						chkFlag = 1; break;
					} else if (chkFlag === 2 && tmp > -1 && tmp < 2) {
						chkFlag = 1; break;
					} else if (tmp > -1) {
						chkFlag = tmp;
					}
				}
			}
		}
		node.check_Child_State = chkFlag;
	},
	_setSonNodeLevel: function(setting, parentNode, node) {
		if (!node) return;
		var childKey = setting.keyChildren;
		node.level = (parentNode)? parentNode.level + 1 : 0;
		if (!node[childKey]) return;
		for (var i = 0, l = node[childKey].length; i < l; i++) {
			if (node[childKey][i]) $.tree._setSonNodeLevel(setting, node, node[childKey][i]);
		}
	},
	_transformToArrayFormat: function (setting, nodes) {
		if (!nodes) return [];
		var childKey = setting.keyChildren,
		r = [];
		if ($.tree._isArray(nodes)) {
			for (var i=0, l=nodes.length; i<l; i++) {
				r.push(nodes[i]);
				if (nodes[i][childKey])
					r = r.concat($.tree._transformToArrayFormat(setting, nodes[i][childKey]));
			}
		} else {
			r.push(nodes);
			if (nodes[childKey])
				r = r.concat($.tree._transformToArrayFormat(setting, nodes[childKey]));
		}
		return r;
	},
	_transformToTreeFormat: function(setting, sNodes) {
		var i,l,
		key = setting.simpleDataIdKey,
		parentKey = setting.simpleDataPIdKey,
		childKey = setting.keyChildren;
		if (!key || key=="" || !sNodes) return [];

		if ($.tree._isArray(sNodes)) {
			var r = [];
			var tmpMap = [];
			for (i=0, l=sNodes.length; i<l; i++) {
				tmpMap[sNodes[i][key]] = sNodes[i];
			}
			for (i=0, l=sNodes.length; i<l; i++) {
				if (tmpMap[sNodes[i][parentKey]] && sNodes[i][key] != sNodes[i][parentKey]) {
					if (!tmpMap[sNodes[i][parentKey]][childKey])
						tmpMap[sNodes[i][parentKey]][childKey] = [];
					tmpMap[sNodes[i][parentKey]][childKey].push(sNodes[i]);
				} else {
					r.push(sNodes[i]);
				}
			}
			return r;
		}else {
			return [sNodes];
		}
	},


	//以下是事件代理方法
	_bindEvent: function(setting) {
		for (var i=0, j=_init.bind.length; i<j; i++) {
			_init.bind[i].apply(this, arguments);
		}
	},
	_unbindEvent: function(setting) {
		for (var i=0, j=_init.unbind.length; i<j; i++) {
			_init.unbind[i].apply(this, arguments);
		}
	},
	_bindTree: function(setting) {
		var eventParam = {
			treeId: setting.treeId
		},
		treeObj = setting.treeObj;
		if (!setting.txtSelectedEnable) {
			// for can't select text
			treeObj.bind('selectstart', function(e){
				var node
				var n = e.originalEvent.srcElement.nodeName.toLowerCase();
				return (n === "input" || n === "textarea" );
			}).css({
				"-moz-user-select":"-moz-none"
			});
		}
		treeObj.bind('click', eventParam, $.tree._proxy);
		treeObj.bind('dblclick', eventParam, $.tree._proxy);
		treeObj.bind('mouseover', eventParam, $.tree._proxy);
		treeObj.bind('mouseout', eventParam, $.tree._proxy);
		treeObj.bind('mousedown', eventParam, $.tree._proxy);
		treeObj.bind('mouseup', eventParam, $.tree._proxy);
		treeObj.bind('contextmenu', eventParam, $.tree._proxy);
	},
	_unbindTree: function(setting) {
		var treeObj = setting.treeObj;
		treeObj.unbind('click', $.tree._proxy);
		treeObj.unbind('dblclick', $.tree._proxy);
		treeObj.unbind('mouseover', $.tree._proxy);
		treeObj.unbind('mouseout', $.tree._proxy);
		treeObj.unbind('mousedown', $.tree._proxy);
		treeObj.unbind('mouseup', $.tree._proxy);
		treeObj.unbind('contextmenu', $.tree._proxy);
	},
	_doProxy: function(e) {
		var results = [];
		for (var i=0, j=_init.proxys.length; i<j; i++) {
			var proxyResult = _init.proxys[i].apply(this, arguments);
			results.push(proxyResult);
			if (proxyResult.stop) {
				break;
			}
		}
		return results;
	},
	_proxy: function(e) {
		var setting = $.tree._getSetting(e.data.treeId);
		if (!$.tree._uCanDo(setting, e)) return true;
		var results = $.tree._doProxy(e),
		r = true, x = false;
		for (var i=0, l=results.length; i<l; i++) {
			var proxyResult = results[i];
			if (proxyResult.nodeEventCallback) {
				x = true;
				r = proxyResult.nodeEventCallback.apply(proxyResult, [e, proxyResult.node]) && r;
			}
			if (proxyResult.treeEventCallback) {
				x = true;
				r = proxyResult.treeEventCallback.apply(proxyResult, [e, proxyResult.node]) && r;
			}
		}
		return r;
	},
	//method of event handler
	_onSwitchNode: function (event, node) {
		var setting = $.tree._getSetting(event.data.treeId);
		if (node.open) {
			if ($.tree._apply(setting.beforeCollapse, [setting.treeId, node, setting], true) == false) return true;
			$.tree._getRoot(setting).expandTriggerFlag = true;
			$.tree._switchNode(setting, node);
		} else {
			if ($.tree._apply(setting.beforeExpand, [setting.treeId, node, setting], true) == false) return true;
			$.tree._getRoot(setting).expandTriggerFlag = true;
			$.tree._switchNode(setting, node);
		}
		return true;
	},
	_onClickNode: function (event, node) {
		var setting = $.tree._getSetting(event.data.treeId),
		clickFlag = ( (setting.autoCancelSelected && (event.ctrlKey || event.metaKey)) && $.tree._isSelectedNode(setting, node)) ? 0 : (setting.autoCancelSelected && (event.ctrlKey || event.metaKey) && setting.selectedMulti) ? 2 : 1;
		if ($.tree._apply(setting.beforeClick, [setting.treeId, node, clickFlag, setting], true) == false) return true;
		if (clickFlag === 0) {
			$.tree._cancelPreSelectedNode(setting, node);
		} else {
			$.tree._selectNode(setting, node, clickFlag === 2);
		}
		setting.treeObj.trigger(consts.event.CLICK, [event, setting.treeId, node, clickFlag]);
		if ( setting.clickExpand ){
			$(setting.treeObj).tree("expandNode", node, null, null, null, true);
		}
		return true;
	},
	_onTreeMousedown: function(event, node) {
		var setting = $.tree._getSetting(event.data.treeId);
		if ($.tree._apply(setting.beforeMouseDown, [setting.treeId, node, setting], true)) {
			$.tree._apply(setting.onMouseDown, [event, setting.treeId, node, setting]);
		}
		return true;
	},
	_onTreeMouseup: function(event, node) {
		var setting = $.tree._getSetting(event.data.treeId);
		if ($.tree._apply(setting.beforeMouseUp, [setting.treeId, node, setting], true)) {
			$.tree._apply(setting.onMouseUp, [event, setting.treeId, node, setting]);
		}
		return true;
	},
	_onTreeDblclick: function(event, node) {
		var setting = $.tree._getSetting(event.data.treeId);
		if ($.tree._apply(setting.beforeDblClick, [setting.treeId, node, setting], true)) {
			$.tree._apply(setting.onDblClick, [event, setting.treeId, node, setting]);
		}
		return true;
	},
	_onTreeContextmenu: function(event, node) {
		var setting = $.tree._getSetting(event.data.treeId);
		if ($.tree._apply(setting.beforeRightClick, [setting.treeId, node, setting], true)) {
			$.tree._apply(setting.onRightClick, [event, setting.treeId, node, setting]);
		}
		return (typeof setting.onRightClick) != "function";
	},
	_onCheckNode: function (event, node) {
		if (node.chkDisabled === true) return false;
		var setting = $.tree._getSetting(event.data.treeId),
		checkedKey = setting.keyChecked;
		if ($.tree._apply(setting.beforeCheck, [setting.treeId, node, setting], true) == false) return true;
		node[checkedKey] = !node[checkedKey];
		$.tree._checkNodeRelation(setting, node);
		var checkObj = $$(node, consts.id.CHECK, setting);
		$.tree._setChkClass(setting, checkObj, node);
		$.tree._repairParentChkClassWithSelf(setting, node);
		setting.treeObj.trigger(consts.event.CHECK, [event, setting.treeId, node]);
		return true;
	},
	_onMouseoverCheck: function(event, node) {
		if (node.chkDisabled === true) return false;
		var setting = $.tree._getSetting(event.data.treeId),
		checkObj = $$(node, consts.id.CHECK, setting);
		node.check_Focus = true;
		$.tree._setChkClass(setting, checkObj, node);
		return true;
	},
	_onMouseoutCheck: function(event, node) {
		if (node.chkDisabled === true) return false;
		var setting = $.tree._getSetting(event.data.treeId),
		checkObj = $$(node, consts.id.CHECK, setting);
		node.check_Focus = false;
		$.tree._setChkClass(setting, checkObj, node);
		return true;
	},
	_onHoverOverNode: function(event, node) {
		var setting = $.tree._getSetting(event.data.treeId),
		root = $.tree._getRoot(setting);
		if (root.curHoverNode != node) {
			$.tree._onHoverOutNode(event);
		}
		root.curHoverNode = node;
		$.tree._addHoverDom(setting, node);
	},
	_onHoverOutNode: function(event, node) {
		var setting = $.tree._getSetting(event.data.treeId),
		root = $.tree._getRoot(setting);
		if (root.curHoverNode && !$.tree._isSelectedNode(setting, root.curHoverNode)) {
			$.tree._removeTreeDom(setting, root.curHoverNode);
			root.curHoverNode = null;
		}
	},
	_onMousedownNode: function(eventMouseDown, _node) {
		var i,l,
		setting = $.tree._getSetting(eventMouseDown.data.treeId),
		root = $.tree._getRoot(setting), roots = $.tree._getRoots();
		//右击鼠标不能拖、拽
		if (eventMouseDown.button == 2 || !setting.editable || (!setting.dragIsCopy && !setting.dragIsMove)) return true;

		//节点名称处于可编辑状态下不能拖、拽
		var target = eventMouseDown.target,
		_nodes = $.tree._getRoot(setting).curSelectedList,
		nodes = [];
		if (!$.tree._isSelectedNode(setting, _node)) {
			nodes = [_node];
		} else {
			for (i=0, l=_nodes.length; i<l; i++) {
				if (_nodes[i].editNameFlag && $.tree._eqs(target.tagName, "input") && target.getAttribute("treeNode"+consts.id.INPUT) !== null) {
					return true;
				}
				nodes.push(_nodes[i]);
				if (nodes[0].parentTId !== _nodes[i].parentTId) {
					nodes = [_node];
					break;
				}
			}
		}

		$.tree._editNodeBlur = true;
		$.tree._cancelCurEditNode(setting);

		var doc = $(setting.treeObj.get(0).ownerDocument),
		body = $(setting.treeObj.get(0).ownerDocument.body), curNode, tmpArrow, tmpTarget,
		isOtherTree = false,
		targetSetting = setting,
		sourceSetting = setting,
		preNode, nextNode,
		preTmpTargetNodeId = null,
		preTmpMoveType = null,
		tmpTargetNodeId = null,
		moveType = consts.move.TYPE_INNER,
		mouseDownX = eventMouseDown.clientX,
		mouseDownY = eventMouseDown.clientY,
		startTime = (new Date()).getTime();

		if ($.tree._uCanDo(setting)) {
			doc.bind("mousemove", _docMouseMove);
		}
		function _docMouseMove(event) {
			//避免拖拽节点后，再次点击节点
			if (root.dragFlag == 0 && Math.abs(mouseDownX - event.clientX) < setting.dragMinMoveSize
				&& Math.abs(mouseDownY - event.clientY) < setting.dragMinMoveSize) {
				return true;
			}
			var i, l, tmpNode, tmpDom, tmpNodes,
			childKey = setting.keyChildren;
			body.css("cursor", "pointer");

			if (root.dragFlag == 0) {
				if ($.tree._apply(setting.beforeDrag, [setting.treeId, nodes, setting], true) == false) {
					_docMouseUp(event);
					return true;
				}

				for (i=0, l=nodes.length; i<l; i++) {
					if (i==0) {
						root.dragNodeShowBefore = [];
					}
					tmpNode = nodes[i];
					if (tmpNode.isParent && tmpNode.open) {
						$.tree._expandCollapseNode(setting, tmpNode, !tmpNode.open);
						root.dragNodeShowBefore[tmpNode.tId] = true;
					} else {
						root.dragNodeShowBefore[tmpNode.tId] = false;
					}
				}

				root.dragFlag = 1;
				roots.showHoverDom = false;
				$.tree._showIfameMask(setting, true);

				//sort
				var isOrder = true, lastIndex = -1;
				if (nodes.length>1) {
					var pNodes = nodes[0].parentTId ? nodes[0].getParentNode()[childKey] : $.tree._getNodes(setting);
					tmpNodes = [];
					for (i=0, l=pNodes.length; i<l; i++) {
						if (root.dragNodeShowBefore[pNodes[i].tId] !== undefined) {
							if (isOrder && lastIndex > -1 && (lastIndex+1) !== i) {
								isOrder = false;
							}
							tmpNodes.push(pNodes[i]);
							lastIndex = i;
						}
						if (nodes.length === tmpNodes.length) {
							nodes = tmpNodes;
							break;
						}
					}
				}
				if (isOrder) {
					preNode = nodes[0].getPreNode();
					nextNode = nodes[nodes.length-1].getNextNode();
				}

				//设置节点为选中状态
				curNode = $$("<ul class='treeDragUL'></ul>", setting);
				for (i=0, l=nodes.length; i<l; i++) {
					tmpNode = nodes[i];
					tmpNode.editNameFlag = false;
					$.tree._selectNode(setting, tmpNode, i>0);
					$.tree._removeTreeDom(setting, tmpNode);

					if (i > setting.dragMaxShowNodeNum-1) {
						continue;
					}

					tmpDom = $$("<li id='"+ tmpNode.tId +"_tmp'></li>", setting);
					tmpDom.append($$(tmpNode, consts.id.A, setting).clone());
					tmpDom.css("padding", "0");
					tmpDom.children("#" + tmpNode.tId + consts.id.A).removeClass(consts.node.CURSELECTED);
					curNode.append(tmpDom);
					if (i == setting.dragMaxShowNodeNum-1) {
						tmpDom = $$("<li id='"+ tmpNode.tId +"_moretmp'><a>  ...  </a></li>", setting);
						curNode.append(tmpDom);
					}
				}
				curNode.attr("id", nodes[0].tId + consts.id.UL + "_tmp");
				curNode.addClass(setting.treeObj.attr("class"));
				curNode.appendTo(body);

				tmpArrow = $$("<span class='tmpTreeMove_arrow'></span>", setting);
				tmpArrow.attr("id", "treeMove_arrow_tmp");
				tmpArrow.appendTo(body);

				setting.treeObj.trigger(consts.event.DRAG, [event, setting.treeId, nodes]);
			}

			if (root.dragFlag == 1) {
				if (tmpTarget && tmpArrow.attr("id") == event.target.id && tmpTargetNodeId && (event.clientX + doc.scrollLeft()+2) > ($("#" + tmpTargetNodeId + consts.id.A, tmpTarget).offset().left)) {
					var xT = $("#" + tmpTargetNodeId + consts.id.A, tmpTarget);
					event.target = (xT.length > 0) ? xT.get(0) : event.target;
				} else if (tmpTarget) {
					tmpTarget.removeClass(consts.node.TMPTARGET_TREE);
					if (tmpTargetNodeId) $("#" + tmpTargetNodeId + consts.id.A, tmpTarget).removeClass(consts.node.TMPTARGET_NODE + "_" + consts.move.TYPE_PREV)
						.removeClass(consts.node.TMPTARGET_NODE + "_" + _final.move.TYPE_NEXT).removeClass(consts.node.TMPTARGET_NODE + "_" + _final.move.TYPE_INNER);
				}
				tmpTarget = null;
				tmpTargetNodeId = null;

				//在多选树中判断拖、拽
				isOtherTree = false;
				targetSetting = setting;
				var settings = $.tree._getSettings();
				for (var s in settings) {
					if (settings[s].treeId && settings[s].editable && settings[s].treeId != setting.treeId
						&& (event.target.id == settings[s].treeId || $(event.target).parents("#" + settings[s].treeId).length>0)) {
						isOtherTree = true;
						targetSetting = settings[s];
					}
				}

				var docScrollTop = doc.scrollTop(),
				docScrollLeft = doc.scrollLeft(),
				treeOffset = targetSetting.treeObj.offset(),
				scrollHeight = targetSetting.treeObj.get(0).scrollHeight,
				scrollWidth = targetSetting.treeObj.get(0).scrollWidth,
				dTop = (event.clientY + docScrollTop - treeOffset.top),
				dBottom = (targetSetting.treeObj.height() + treeOffset.top - event.clientY - docScrollTop),
				dLeft = (event.clientX + docScrollLeft - treeOffset.left),
				dRight = (targetSetting.treeObj.width() + treeOffset.left - event.clientX - docScrollLeft),
				isTop = (dTop < setting.dragBorderMax && dTop > setting.dragBorderMin),
				isBottom = (dBottom < setting.dragBorderMax && dBottom > setting.dragBorderMin),
				isLeft = (dLeft < setting.dragBorderMax && dLeft > setting.dragBorderMin),
				isRight = (dRight < setting.dragBorderMax && dRight > setting.dragBorderMin),
				isTreeInner = dTop > setting.dragBorderMin && dBottom > setting.dragBorderMin && dLeft > setting.dragBorderMin && dRight > setting.dragBorderMin,
				isTreeTop = (isTop && targetSetting.treeObj.scrollTop() <= 0),
				isTreeBottom = (isBottom && (targetSetting.treeObj.scrollTop() + targetSetting.treeObj.height()+10) >= scrollHeight),
				isTreeLeft = (isLeft && targetSetting.treeObj.scrollLeft() <= 0),
				isTreeRight = (isRight && (targetSetting.treeObj.scrollLeft() + targetSetting.treeObj.width()+10) >= scrollWidth);

				if (event.target && $.tree._isChildOrSelf(event.target, targetSetting.treeId)) {
					//获取树节点中  <li> 元素dom
					var targetObj = event.target;
					while (targetObj && targetObj.tagName && !$.tree._eqs(targetObj.tagName, "li") && targetObj.id != targetSetting.treeId) {
						targetObj = targetObj.parentNode;
					}

					var canMove = true;
					//不能移动到自身，或自身的子节点中
					for (i=0, l=nodes.length; i<l; i++) {
						tmpNode = nodes[i];
						if (targetObj.id === tmpNode.tId) {
							canMove = false;
							break;
						} else if ($$(tmpNode, setting).find("#" + targetObj.id).length > 0) {
							canMove = false;
							break;
						}
					}
					if (canMove && event.target && $.tree._isChildOrSelf(event.target, targetObj.id + consts.id.A)) {
						tmpTarget = $(targetObj);
						tmpTargetNodeId = targetObj.id;
					}
				}

				//鼠标移动到树组件区域
				tmpNode = nodes[0];
				if (isTreeInner && $.tree._isChildOrSelf(event.target, targetSetting.treeId)) {
					//判断鼠标移动至根节点
					if (!tmpTarget && (event.target.id == targetSetting.treeId || isTreeTop || isTreeBottom || isTreeLeft || isTreeRight) && (isOtherTree || (!isOtherTree && tmpNode.parentTId))) {
						tmpTarget = targetSetting.treeObj;
					}
					//自动滚动到顶部
					if (isTop) {
						targetSetting.treeObj.scrollTop(targetSetting.treeObj.scrollTop()-10);
					} else if (isBottom)  {
						targetSetting.treeObj.scrollTop(targetSetting.treeObj.scrollTop()+10);
					}
					if (isLeft) {
						targetSetting.treeObj.scrollLeft(targetSetting.treeObj.scrollLeft()-10);
					} else if (isRight) {
						targetSetting.treeObj.scrollLeft(targetSetting.treeObj.scrollLeft()+10);
					}
					//auto scroll left
					if (tmpTarget && tmpTarget != targetSetting.treeObj && tmpTarget.offset().left < targetSetting.treeObj.offset().left) {
						targetSetting.treeObj.scrollLeft(targetSetting.treeObj.scrollLeft()+ tmpTarget.offset().left - targetSetting.treeObj.offset().left);
					}
				}

				curNode.css({
					"top": (event.clientY + docScrollTop + 3) + "px",
					"left": (event.clientX + docScrollLeft + 3) + "px"
				});

				var dX = 0;
				var dY = 0;
				if (tmpTarget && tmpTarget.attr("id")!=targetSetting.treeId) {
					var tmpTargetNode = tmpTargetNodeId == null ? null: $.tree._getNodeCache(targetSetting, tmpTargetNodeId),
					isCopy = ((event.ctrlKey || event.metaKey) && setting.dragIsMove && setting.dragIsCopy) || (!setting.dragIsMove && setting.dragIsCopy),
					isPrev = !!(preNode && tmpTargetNodeId === preNode.tId),
					isNext = !!(nextNode && tmpTargetNodeId === nextNode.tId),
					isInner = (tmpNode.parentTId && tmpNode.parentTId == tmpTargetNodeId),
					canPrev = (isCopy || !isNext) && $.tree._apply(targetSetting.dragPrev, [targetSetting.treeId, nodes, tmpTargetNode, targetSetting], !!targetSetting.dragPrev),
					canNext = (isCopy || !isPrev) && $.tree._apply(targetSetting.dragNext, [targetSetting.treeId, nodes, tmpTargetNode, targetSetting], !!targetSetting.dragNext),
					canInner = (isCopy || !isInner) && !(targetSetting.keepLeaf && !tmpTargetNode.isParent) && $.tree._apply(targetSetting.dragInner, [targetSetting.treeId, nodes, tmpTargetNode, targetSetting], !!targetSetting.dragInner);
					if (!canPrev && !canNext && !canInner) {
						tmpTarget = null;
						tmpTargetNodeId = "";
						moveType = consts.move.TYPE_INNER;
						tmpArrow.css({
							"display":"none"
						});
						if (window.TreeMoveTimer) {
							clearTimeout(window.TreeMoveTimer);
							window.TreeMoveTargetNodeTId = null
						}
					} else {
						var tmpTargetA = $("#" + tmpTargetNodeId + consts.id.A, tmpTarget),
						tmpNextA = tmpTargetNode.isLastNode ? null : $("#" + tmpTargetNode.getNextNode().tId + consts.id.A, tmpTarget.next()),
						tmpTop = tmpTargetA.offset().top,
						tmpLeft = tmpTargetA.offset().left,
						prevPercent = canPrev ? (canInner ? 0.25 : (canNext ? 0.5 : 1) ) : -1,
						nextPercent = canNext ? (canInner ? 0.75 : (canPrev ? 0.5 : 0) ) : -1,
						dY_percent = (event.clientY + docScrollTop - tmpTop)/tmpTargetA.height();
						if ((prevPercent==1 ||dY_percent<=prevPercent && dY_percent>=-.2) && canPrev) {
							dX = 1 - tmpArrow.width();
							dY = tmpTop - tmpArrow.height()/2;
							moveType = consts.move.TYPE_PREV;
						} else if ((nextPercent==0 || dY_percent>=nextPercent && dY_percent<=1.2) && canNext) {
							dX = 1 - tmpArrow.width();
							dY = (tmpNextA == null || (tmpTargetNode.isParent && tmpTargetNode.open)) ? (tmpTop + tmpTargetA.height() - tmpArrow.height()/2) : (tmpNextA.offset().top - tmpArrow.height()/2);
							moveType = consts.move.TYPE_NEXT;
						}else {								
							dX = 5 - tmpArrow.width();
							dY = tmpTop;
							moveType = consts.move.TYPE_INNER;								
						}
						tmpArrow.css({
							"display":"block",
							"top": dY + "px",
							"left": (tmpLeft + dX) + "px"
						});
						// 如果dragStyle配置为线形的提示，则显示线性 begin lihaibo added
						if (setting.dragStyle == "line") {
							if ( consts.move.TYPE_INNER == moveType) {
								tmpArrow.removeClass("preNext_line").addClass("inner_line");
							} else {
								tmpArrow.removeClass("inner_line").addClass("preNext_line");
							}
						}
						// 如果dragStyle配置为线形的提示，则显示线性 end
						tmpTargetA.addClass(consts.node.TMPTARGET_NODE + "_" + moveType);

						if (preTmpTargetNodeId != tmpTargetNodeId || preTmpMoveType != moveType) {
							startTime = (new Date()).getTime();
						}
						if (tmpTargetNode && tmpTargetNode.isParent && moveType == consts.move.TYPE_INNER) {
							var startTimer = true;
							if (window.TreeMoveTimer && window.TreeMoveTargetNodeTId !== tmpTargetNode.tId) {
								clearTimeout(window.TreeMoveTimer);
								window.TreeMoveTargetNodeTId = null;
							}else if (window.TreeMoveTimer && window.TreeMoveTargetNodeTId === tmpTargetNode.tId) {
								startTimer = false;
							}
							if (startTimer) {
								window.TreeMoveTimer = setTimeout(function() {
									if (moveType != consts.move.TYPE_INNER) return;
									if (tmpTargetNode && tmpTargetNode.isParent && !tmpTargetNode.open && (new Date()).getTime() - startTime > targetSetting.dragAutoOpenTime
										&& $.tree._apply(targetSetting.beforeDragOpen, [targetSetting.treeId, tmpTargetNode, targetSetting], true)) {
										$.tree._switchNode(targetSetting, tmpTargetNode);
										if (targetSetting.dragAutoExpandTrigger) {
											targetSetting.treeObj.trigger(consts.event.EXPAND, [targetSetting.treeId, tmpTargetNode]);
										}
									}
								}, targetSetting.dragAutoOpenTime+50);
								window.TreeMoveTargetNodeTId = tmpTargetNode.tId;
							}
						}
					}
				} else {
					moveType = consts.move.TYPE_INNER;
					if (tmpTarget && $.tree._apply(targetSetting.dragInner, [targetSetting.treeId, nodes, null, targetSetting], !!targetSetting.dragInner)) {
						tmpTarget.addClass(consts.node.TMPTARGET_TREE);
					} else {
						tmpTarget = null;
					}
					tmpArrow.css({
						"display":"none"
					});
					if (window.TreeMoveTimer) {
						clearTimeout(window.TreeMoveTimer);
						window.TreeMoveTargetNodeTId = null;
					}
				}
				preTmpTargetNodeId = tmpTargetNodeId;
				preTmpMoveType = moveType;

				setting.treeObj.trigger(consts.event.DRAGMOVE, [event, setting.treeId, nodes]);
			}
			return false;
		}

		doc.bind("mouseup", _docMouseUp);
		function _docMouseUp(event) {
			if (window.TreeMoveTimer) {
				clearTimeout(window.TreeMoveTimer);
				window.TreeMoveTargetNodeTId = null;
			}
			preTmpTargetNodeId = null;
			preTmpMoveType = null;
			doc.unbind("mousemove", _docMouseMove);
			doc.unbind("mouseup", _docMouseUp);
			doc.unbind("selectstart", _docSelect);
			body.css("cursor", "auto");
			if (tmpTarget) {
				tmpTarget.removeClass(consts.node.TMPTARGET_TREE);
				if (tmpTargetNodeId) $("#" + tmpTargetNodeId + consts.id.A, tmpTarget).removeClass(consts.node.TMPTARGET_NODE + "_" + consts.move.TYPE_PREV)
						.removeClass(consts.node.TMPTARGET_NODE + "_" + _final.move.TYPE_NEXT).removeClass(consts.node.TMPTARGET_NODE + "_" + _final.move.TYPE_INNER);
			}
			$.tree._showIfameMask(setting, false);

			roots.showHoverDom = true;
			if (root.dragFlag == 0) return;
			root.dragFlag = 0;

			var i, l, tmpNode;
			for (i=0, l=nodes.length; i<l; i++) {
				tmpNode = nodes[i];
				if (tmpNode.isParent && root.dragNodeShowBefore[tmpNode.tId] && !tmpNode.open) {
					$.tree._expandCollapseNode(setting, tmpNode, !tmpNode.open);
					delete root.dragNodeShowBefore[tmpNode.tId];
				}
			}

			if (curNode) curNode.remove();
			if (tmpArrow) tmpArrow.remove();

			var isCopy = ((event.ctrlKey || event.metaKey) && setting.dragIsMove && setting.dragIsCopy) || (!setting.dragIsMove && setting.dragIsCopy);
			if (!isCopy && tmpTarget && tmpTargetNodeId && nodes[0].parentTId && tmpTargetNodeId==nodes[0].parentTId && moveType == consts.move.TYPE_INNER) {
				tmpTarget = null;
			}
			if (tmpTarget) {
				var dragTargetNode = tmpTargetNodeId == null ? null: $.tree._getNodeCache(targetSetting, tmpTargetNodeId);
				if ($.tree._apply(setting.beforeDrop, [targetSetting.treeId, nodes, dragTargetNode, moveType, isCopy,setting], true) == false) {
					$.tree._selectNodes(sourceSetting, nodes);
					return;
				}
				var newNodes = isCopy ? $.tree._clone(nodes) : nodes;

				function dropCallback() {
					if (isOtherTree) {
						if (!isCopy) {
							for(var i=0, l=nodes.length; i<l; i++) {
								$.tree._removeNode(setting, nodes[i]);
							}
						}
						if (moveType == consts.move.TYPE_INNER) {
							$.tree._addNodes(targetSetting, dragTargetNode, newNodes);
						} else {
							$.tree._addNodes(targetSetting, dragTargetNode.getParentNode(), newNodes);
							if (moveType == consts.move.TYPE_PREV) {
								for (i=0, l=newNodes.length; i<l; i++) {
									$.tree._moveNode(targetSetting, dragTargetNode, newNodes[i], moveType, false);
								}
							} else {
								for (i=-1, l=newNodes.length-1; i<l; l--) {
									$.tree._moveNode(targetSetting, dragTargetNode, newNodes[l], moveType, false);
								}
							}
						}
					} else {
						if (isCopy && moveType == consts.move.TYPE_INNER) {
							$.tree._addNodes(targetSetting, dragTargetNode, newNodes);
						} else {
							if (isCopy) {
								$.tree._addNodes(targetSetting, dragTargetNode.getParentNode(), newNodes);
							}
							if (moveType != consts.move.TYPE_NEXT) {
								for (i=0, l=newNodes.length; i<l; i++) {
									$.tree._moveNode(targetSetting, dragTargetNode, newNodes[i], moveType, false);
								}
							} else {
								for (i=-1, l=newNodes.length-1; i<l; l--) {
									$.tree._moveNode(targetSetting, dragTargetNode, newNodes[l], moveType, false);
								}
							}
						}
					}
					$.tree._selectNodes(targetSetting, newNodes);
					$$(newNodes[0], setting).focus().blur();

					setting.treeObj.trigger(consts.event.DROP, [event, targetSetting.treeId, newNodes, dragTargetNode, moveType, isCopy]);
				}

				if (moveType == consts.move.TYPE_INNER && $.tree._canAsync(targetSetting, dragTargetNode)) {
					$.tree._asyncNode(targetSetting, dragTargetNode, false, dropCallback);
				} else {
					dropCallback();
				}

			} else {
				$.tree._selectNodes(sourceSetting, nodes);
				setting.treeObj.trigger(consts.event.DROP, [event, setting.treeId, nodes, null, null, null]);
			}
		}

		doc.bind("selectstart", _docSelect);
		function _docSelect() {
			return false;
		}

		//Avoid FireFox's Bug
		//If Tree Div CSS set 'overflow', so drag node outside of Tree, and event.target is error.
		if(eventMouseDown.preventDefault) {
			eventMouseDown.preventDefault();
		}
		return true;
	},
	
	//以下是树组件工具方法
	_apply: function(fun, param, defaultValue) {	
		if( fun == defaultValue && !$.isFunction(fun) ) {
			return defaultValue;
		}
		
		
		if( typeof(fun) =="undefined" || fun == null){
			return defaultValue;
		}
		var _fn,ret,t;
		 _fn = $.coral.toFunction(fun);
		
		if (!$.isFunction(_fn)) {
			return defaultValue;
		}
		ret = _fn.apply(t||coralTree,param ? param : []);
		
		return typeof(ret)=="undefined"?defaultValue:ret;
	},
	_canAsync: function(setting, node) {
		var childKey = setting.keyChildren;
		return (setting.asyncEnable && node && node.isParent && !(node.zAsync || (node[childKey] && node[childKey].length > 0)));
	},
	_clone: function (obj){
		if (obj === null) return null;
		var o = $.tree._isArray(obj) ? [] : {};
		for(var i in obj){
			o[i] = (obj[i] instanceof Date) ? new Date(obj[i].getTime()) : (typeof obj[i] === "object" ? arguments.callee(obj[i]) : obj[i]);
		}
		return o;
	},
	_eqs: function(str1, str2) {
		return str1.toLowerCase() === str2.toLowerCase();
	},
	_isArray: function(arr) {
		return Object.prototype.toString.apply(arr) === "[object Array]";
	},
	_$: function(node, exp, setting) {
		if (!!exp && typeof exp != "string") {
			setting = exp;
			exp = "";
		}
		if (typeof node == "string") {
			return $(node, setting ? setting.treeObj.get(0).ownerDocument : null);
		} else {
			return $("#" + node.tId + exp, setting ? setting.treeObj : null);
		}
	},
	_getMDom: function (setting, curDom, targetExpr) {
		if (!curDom) return null;
		while (curDom && curDom.id !== setting.treeId) {
			for (var i=0, l=targetExpr.length; curDom.tagName && i<l; i++) {
				if ($.tree._eqs(curDom.tagName, targetExpr[i].tagName) && curDom.getAttribute(targetExpr[i].attrName) !== null) {
					return curDom;
				}
			}
			curDom = curDom.parentNode;
		}
		return null;
	},
	_getNodeMainDom: function(target) {
		return ($(target).parent("li").get(0) || $(target).parentsUntil("li").parent().get(0));
	},
	_isChildOrSelf: function(dom, parentId) {
		return ( $(dom).closest("#" + parentId).length> 0 );
	},
	_uCanDo: function(setting, e) {
		return true;
	},
	_getAbs: function (obj) {
		var oRect = obj.getBoundingClientRect(),
		scrollTop = document.body.scrollTop+document.documentElement.scrollTop,
		scrollLeft = document.body.scrollLeft+document.documentElement.scrollLeft;
		return [oRect.left+scrollLeft,oRect.top+scrollTop];
	},
	_inputFocus: function(inputObj) {
		if (inputObj.get(0)) {
			inputObj.focus();
			$.tree._setCursorPosition(inputObj.get(0), inputObj.val().length);
		}
	},
	_inputSelect: function(inputObj) {
		if (inputObj.get(0)) {
			inputObj.focus();
			inputObj.select();
		}
	},
	_setCursorPosition: function(obj, pos){
		if(obj.setSelectionRange) {
			obj.focus();
			obj.setSelectionRange(pos,pos);
		} else if (obj.createTextRange) {
			var range = obj.createTextRange();
			range.collapse(true);
			range.moveEnd('character', pos);
			range.moveStart('character', pos);
			range.select();
		}
	},
	_showIfameMask: function(setting, showSign) {
		var root = $.tree._getRoot(setting);
		//clear full mask
		while (root.dragMaskList.length > 0) {
			root.dragMaskList[0].remove();
			root.dragMaskList.shift();
		}
		if (showSign) {
			//show mask
			var iframeList = $$("iframe", setting);
			for (var i = 0, l = iframeList.length; i < l; i++) {
				var obj = iframeList.get(i),
				r = $.tree._getAbs(obj),
				dragMask = $$("<div id='treeMask_" + i + "' class='treeMask' style='top:" + r[1] + "px; left:" + r[0] + "px; width:" + obj.offsetWidth + "px; height:" + obj.offsetHeight + "px;'></div>", setting);
				dragMask.appendTo($$("body", setting));
				root.dragMaskList.push(dragMask);
			}
		}
	},
	//以下是操作树形结构中dom元素的方法
	_addNodes: function(setting, parentNode, newNodes, isSilent) {
		if (setting.keepLeaf && parentNode && !parentNode.isParent) {
			return;
		}
		if (!$.tree._isArray(newNodes)) {
			newNodes = [newNodes];
		}
		if (setting.simpleDataEnable) {
			newNodes = $.tree._transformToTreeFormat(setting, newNodes);
		}
		if (parentNode) {
			var target_switchObj = $$(parentNode, consts.id.SWITCH, setting),
			target_icoObj = $$(parentNode, consts.id.ICON, setting),
			target_ulObj = $$(parentNode, consts.id.UL, setting);

			if (!parentNode.open) {
				$.tree._replaceSwitchClass(parentNode, target_switchObj, consts.arrow.RIGHT);
				$.tree._replaceIcoClass(parentNode, target_icoObj, consts.folder.CLOSE );
				parentNode.open = false;
				target_ulObj.css({
					"display": "none"
				});
			}

			$.tree._addNodesData(setting, parentNode, newNodes);
			$.tree._createNodes(setting, parentNode.level + 1, newNodes, parentNode);
			if (!isSilent) {
				$.tree._expandCollapseParentNode(setting, parentNode, true);
			}
		} else {
			$.tree._addNodesData(setting, $.tree._getRoot(setting), newNodes);
			$.tree._createNodes(setting, 0, newNodes, null);
		}
	},
	_appendNodes: function(setting, level, nodes, parentNode, initFlag, openFlag) {
		if (!nodes) return [];
		var html = [],
		childKey = setting.keyChildren;
		for (var i = 0, l = nodes.length; i < l; i++) {
			var node = nodes[i];
			if (initFlag) {
				var tmpPNode = (parentNode) ? parentNode: $.tree._getRoot(setting),
				tmpPChild = tmpPNode[childKey],
				isFirstNode = ((tmpPChild.length == nodes.length) && (i == 0)),
				isLastNode = (i == (nodes.length - 1));
				$.tree._initNode(setting, level, node, parentNode, isFirstNode, isLastNode, openFlag);
				$.tree._addNodeCache(setting, node);
			}
			
			var childHtml = [];
			if (node[childKey] && node[childKey].length > 0) {
				//make child html first, because checkType
				childHtml = $.tree._appendNodes(setting, level + 1, node[childKey], node, initFlag, openFlag && node.open);
			}
			if (openFlag) {

				$.tree._makeDOMNodeMainBefore(html, setting, node);
				$.tree._makeDOMNodeNameBefore(html, setting, node);
				$.tree._addDiyDom(html, setting, node);
				$.tree._makeDOMNodeLine(html, setting, node);
				$.tree._getBeforeA(setting, node, html);
				$.tree._getInnerBeforeA(setting, node, html);
				$.tree._makeDOMNodeIcon(html, setting, node);
				$.tree._getInnerAfterA(setting, node, html);
				$.tree._makeDOMNodeNameAfter(html, setting, node);
				$.tree._getAfterA(setting, node, html);
				if (node.isParent && node.open) {
					$.tree._makeUlHtml(setting, node, html, childHtml.join(''));
				}
				$.tree._makeDOMNodeMainAfter(html, setting, node);
				$.tree._addCreatedNode(setting, node);
			}
		}
		return html;
	},
	_appendParentULDom: function(setting, node) {
		var html = [],
		nObj = $$(node, setting);
		if (!nObj.get(0) && !!node.parentTId) {
			$.tree._appendParentULDom(setting, node.getParentNode());
			nObj = $$(node, setting);
		}
		var ulObj = $$(node, consts.id.UL, setting);
		if (ulObj.get(0)) {
			ulObj.remove();
		}
		var childKey = setting.keyChildren,
		childHtml = $.tree._appendNodes(setting, node.level+1, node[childKey], node, false, true);
		$.tree._makeUlHtml(setting, node, html, childHtml.join(''));
		nObj.append(html.join(''));
	},
	/**
	 * :TODO: 为了不改变结构，临时添加opts参数，待以后改进。
	 */
	_asyncNode: function(setting, node, isSilent, callback, opts) {
		var opts = opts || {};
		var i, l;
		var isInit = setting.isInit;
		if (node && !node.isParent) {
			$.tree._apply(callback);
			return false;
		} else if (node && node.isAjaxing) {
			return false;
		} else if ($.tree._apply(setting.beforeAsync, [setting.treeId, node, setting], true) == false) {
			$.tree._apply(callback);
			return false;
		}
		if (node) {
			node.isAjaxing = true;
			var icoObj = $$(node, consts.id.ICON, setting);
			icoObj.attr({"style":"", "class":consts.className.BUTTON + " " + consts.className.ICO_LOADING});
		}

		var tmpParam = {},
			asyncOtherParam = opts.asyncOtherParam || setting.asyncOtherParam;
		//通过标签传递的参数为字符串类型，需要转换为一个数组对象
		//var autoParams = (new Function('return ' + setting.asyncAutoParam))();
		var autoParams = setting.asyncAutoParam.split(",");
		var isInit = setting.isInit;
		for (i = 0, l = autoParams.length; node && i < l; i++) {
			var pKey = autoParams[i].split("="), spKey = pKey;
			if (pKey.length>1) {
				spKey = pKey[1];
				pKey = pKey[0];
			}
			tmpParam[spKey] = node[pKey];
		}
		if ($.tree._isArray(asyncOtherParam)) {
			for (i = 0, l = asyncOtherParam.length; i < l; i += 2) {
				tmpParam[asyncOtherParam[i]] = asyncOtherParam[i + 1];
			}
		} else {
			for (var p in asyncOtherParam) {
				tmpParam[p] = asyncOtherParam[p];
			}
		}

		var _tmpV = $.tree._getRoot(setting)._ver,
			asyncUrl = $.tree._apply(setting.asyncUrl, [setting.treeId, node, setting], setting.asyncUrl);
		$.ajax({
			contentType: setting.asyncContentType,
			type: setting.asyncType,
			url: asyncUrl,
			data: tmpParam,
			dataType: setting.asyncDataType,
			success: function(msg) {
				if (_tmpV != $.tree._getRoot(setting)._ver) {
					return;
				}
				var newNodes = [];
				try {
					if (!msg || msg.length == 0) {
						newNodes = [];
					} else if (typeof msg == "string") {
						newNodes = eval("(" + msg + ")");
					} else {
						newNodes = msg;
					}
				} catch(err) {
					newNodes = msg;
				}

				if (node) {
					node.isAjaxing = null;
					node.zAsync = true;
				}

				$.tree._setNodeLineIcos(setting, node);
				if (newNodes && newNodes !== "") {
					if ( isInit ){
						newNodes = $.tree._createRootNodes(setting,newNodes);
					}
					newNodes = $.tree._apply(setting.asyncDataFilter, [setting.treeId, node, newNodes, setting], newNodes);
					$.tree._addNodes(setting, node, !!newNodes ? $.tree._clone(newNodes) : [], !!isSilent);
				} else {
					if ( isInit ){
						newNodes = $.tree._createRootNodes(setting,newNodes);
					}
					$.tree._addNodes(setting, node, rootNodes, !!isSilent);
				}
				if ( opts.onLoad ) {
					$.tree._apply( opts.onLoad, [setting.treeId, node, msg]);
				} else {
					setting.treeObj.trigger(consts.event.ASYNC_SUCCESS, [setting.treeId, node, msg]);
				}
				$.tree._apply(callback);
			},
			error: function(XMLHttpRequest, textStatus, errorThrown) {
				if (_tmpV != $.tree._getRoot(setting)._ver) {
					return;
				}
				if (node) node.isAjaxing = null;
				$.tree._setNodeLineIcos(setting, node);
				setting.treeObj.trigger(consts.event.ASYNC_ERROR, [setting.treeId, node, XMLHttpRequest, textStatus, errorThrown]);
			}
		});
		return true;
	},
	_addEditBtn: function(setting, node) {
		if (node.editNameFlag || $$(node, consts.id.EDIT, setting).length > 0) {
			return;
		}
		if (!$.tree._apply(setting.showRenameBtn, [setting.treeId, node, setting], setting.showRenameBtn)) {
			return;
		}
		var aObj = $$(node, consts.id.A, setting),
		editStr = "<span class='" + consts.className.BUTTON + " " + consts.editIcon +" edit' id='" + node.tId + consts.id.EDIT + "' title='"+$.tree._apply(setting.renameTitle, [setting.treeId, node, setting], setting.renameTitle)+"' treeNode"+consts.id.EDIT+" style='display:none;'></span>";
		aObj.append(editStr);

		$$(node, consts.id.EDIT, setting).bind('click',
			function() {
				if (!$.tree._uCanDo(setting) || $.tree._apply(setting.beforeEditName, [setting.treeId, node, setting], true) == false) return false;
				$.tree._editNode(setting, node);
				return false;
			}
			).show();
	},
	_addRemoveBtn: function(setting, node) {
		if (node.editNameFlag || $$(node, consts.id.REMOVE, setting).length > 0) {
			return;
		}
		if (!$.tree._apply(setting.showRemoveBtn, [setting.treeId, node, setting], setting.showRemoveBtn)) {
			return;
		}
		var aObj = $$(node, consts.id.A, setting),
		removeStr = "<span class='" + consts.className.BUTTON + " " + consts.removeIcon + " remove' id='" + node.tId + consts.id.REMOVE + "' title='"+$.tree._apply(setting.removeTitle, [setting.treeId, node, setting], setting.removeTitle)+"' treeNode"+consts.id.REMOVE+" style='display:none;'></span>";
		aObj.append(removeStr);

		$$(node, consts.id.REMOVE, setting).bind('click',
			function() {
				if (!$.tree._uCanDo(setting) || $.tree._apply(setting.beforeRemove, [setting.treeId, node, setting], true) == false) return false;
				$.tree._removeNode(setting, node);
				setting.treeObj.trigger(consts.event.REMOVE, [setting.treeId, node]);
				return false;
			}
			).bind('mousedown',
			function(eventMouseDown) {
				return true;
			}
			).show();
	},
	_addHoverDom: function(setting, node) {
		if ($.tree._getRoots().showHoverDom) {
			node.isHover = true;
			if (setting.editable) {
				$.tree._addEditBtn(setting, node);
				$.tree._addRemoveBtn(setting, node);
			}
			$$(node, consts.id.A, setting).addClass(consts.node.CURHOVER);
			$.tree._apply(setting.addHoverDom, [setting.treeId, node, setting]);
		}
	},
	
	_cancelPreSelectedNode: function (setting, node) {
		var list = $.tree._getRoot(setting).curSelectedList;
		for (var i=0, j=list.length-1; j>=i; j--) {
			if (!node || node === list[j]) {
				$$(list[j], consts.id.A, setting).removeClass(consts.node.CURSELECTED);
				if (node) {
					$.tree._removeSelectedNode(setting, node);
					break;
				}
			}
		}
		if (!node) $.tree._getRoot(setting).curSelectedList = [];
	},
	_createNodeCallback: function(setting) {
		if (!!setting.onNodeCreated || !!setting.addDiyDom) {
			var root = $.tree._getRoot(setting);
			while (root.createdNodes.length>0) {
				var node = root.createdNodes.shift();
				$.tree._apply(setting.addDiyDom, [setting.treeId, node, setting]);
				if (!!setting.onNodeCreated) {
					setting.treeObj.trigger(consts.event.NODECREATED, [setting.treeId, node]);
				}
			}
		}
	},
	/**
	 * @param setting
	 * @param nodes
	 * 
	 * @returns nodes 新构造的树节点（可能带有根节点）
	 */
	_createRootNodes: function( setting, nodes ) {
		if (setting.simpleDataEnable) {
			nodes = $.tree._transformToTreeFormat(setting, nodes);
		}
		//if ( setting.isInit ) {
		if(setting.showRootNode == true){
			//指定根节点的情况
			if ( setting.rootNode && typeof ( setting.rootNode ) !== "boolean" ) {
				setting.rootNode.children = nodes;
				nodes = [setting.rootNode];		
			}
		} else //showRootNode == false: 隐藏根节点
		{
			//如果rootNode为true，则根节点在nodes中取得
			//且根节点只有一层的时候 
			//且children必须大于1
			if ( typeof ( setting.rootNode ) == "boolean" && 
				setting.rootNode ) {
				if ( nodes.length >= 1 ) {
					if ( nodes[0].children && nodes[0].children.length > 0 ) {
						nodes = nodes[0].children;
					} else {
						nodes = [];
					}
				}
			}
		}
		//}
		return nodes;
	},
	/**
	 * @param setting
	 * @param level
	 * @param nodes
	 * @param parentNode
	 * 
	 */
	_createNodes: function( setting, level, nodes, parentNode ) {
		//初始化的时候处理根节点的显示或者隐藏
		//nodes = $.tree._createRootNodes(setting, nodes );
		
		if (!nodes || nodes.length == 0) return;
		var root = $.tree._getRoot(setting),
		childKey = setting.keyChildren,
		openFlag = !parentNode || parentNode.open || !!$$(parentNode[childKey][0], setting).get(0);
		root.createdNodes = [];
		var treeHtml = $.tree._appendNodes(setting, level, nodes, parentNode, true, openFlag);
		if (!parentNode) {
			setting.treeObj.append(treeHtml.join(''));
		} else {
			var ulObj = $$(parentNode, consts.id.UL, setting);
			if (ulObj.get(0)) {
				ulObj.append(treeHtml.join(''));
			}
		}
		// // 20140108 lihaibo added
		setting.isInit = false;
		// // 20140108 lihaibo added
		$.tree._createNodeCallback(setting);
	},
	_checkNodeRelation: function(setting, node) {
		var pNode, i, l,
		childKey = setting.keyChildren,
		checkedKey = setting.keyChecked,
		r = consts.radio;
		if (setting.chkStyle == r.STYLE) {
			var checkedList = $.tree._getRadioCheckedList(setting);
			if (node[checkedKey]) {
				if (setting.radioType == r.TYPE_ALL) {
					for (i = checkedList.length-1; i >= 0; i--) {
						pNode = checkedList[i];
						if (pNode[checkedKey] && pNode != node) {
							pNode[checkedKey] = false;
							checkedList.splice(i, 1);

							$.tree._setChkClass(setting, $$(pNode, consts.id.CHECK, setting), pNode);
							if (pNode.parentTId != node.parentTId) {
								$.tree._repairParentChkClassWithSelf(setting, pNode);
							}
						}
					}
					checkedList.push(node);
				} else {
					var parentNode = (node.parentTId) ? node.getParentNode() : $.tree._getRoot(setting);
					for (i = 0, l = parentNode[childKey].length; i < l; i++) {
						pNode = parentNode[childKey][i];
						if (pNode[checkedKey] && pNode != node) {
							pNode[checkedKey] = false;
							$.tree._setChkClass(setting, $$(pNode, consts.id.CHECK, setting), pNode);
						}
					}
				}
			} else if (setting.radioType == r.TYPE_ALL) {
				for (i = 0, l = checkedList.length; i < l; i++) {
					if (node == checkedList[i]) {
						checkedList.splice(i, 1);
						break;
					}
				}
			}

		} else {
			if (node[checkedKey] && (!node[childKey] || node[childKey].length==0 || setting.chkboxType.Y.indexOf("s") > -1)) {
				$.tree._setSonNodeCheckBox(setting, node, true);
			}
			if (!node[checkedKey] && (!node[childKey] || node[childKey].length==0 || setting.chkboxType.N.indexOf("s") > -1)) {
				$.tree._setSonNodeCheckBox(setting, node, false);
			}
			if (node[checkedKey] && setting.chkboxType.Y.indexOf("p") > -1) {
				$.tree._setParentNodeCheckBox(setting, node, true);
			}
			if (!node[checkedKey] && setting.chkboxType.N.indexOf("p") > -1) {
				$.tree._setParentNodeCheckBox(setting, node, false);
			}
		}
	},
	_cancelCurEditNode: function (setting, forceName, isCancel) {
		var root = $.tree._getRoot(setting),
		nameKey = setting.keyName,
		node = root.curEditNode;

		if (node) {
			var inputObj = root.curEditInput,
			newName = forceName ? forceName:(isCancel ? node[nameKey]: inputObj.val());
			if ($.tree._apply(setting.beforeReName, [setting.treeId, node, newName, isCancel, setting], true) === false) {
				return false;
			} else {
				node[nameKey] = newName;
				setting.treeObj.trigger(consts.event.RENAME, [setting.treeId, node, isCancel]);
			}
			var aObj = $$(node, consts.id.A, setting);
			aObj.removeClass(consts.node.CURSELECTED_EDIT);
			inputObj.unbind();
			$.tree._setNodeName(setting, node);
			node.editNameFlag = false;
			root.curEditNode = null;
			root.curEditInput = null;
			$.tree._selectNode(setting, node, false);
		}
		root.noSelection = true;
		return true;
	},
	_editNode: function(setting, node) {
		var root = $.tree._getRoot(setting);
		$.tree._editNodeBlur = false;
		if ($.tree._isSelectedNode(setting, node) && root.curEditNode == node && node.editNameFlag) {
			setTimeout(function() {$.tree._inputFocus(root.curEditInput);}, 0);
			return;
		}
		var nameKey = setting.keyName;
		node.editNameFlag = true;
		$.tree._removeTreeDom(setting, node);
		$.tree._cancelCurEditNode(setting);
		$.tree._selectNode(setting, node, false);
		$$(node, consts.id.SPAN, setting).html("<input type=text class='rename' id='" + node.tId + consts.id.INPUT + "' treeNode" + consts.id.INPUT + " >");
		var inputObj = $$(node, consts.id.INPUT, setting);
		inputObj.attr("value", node[nameKey]);
		if (setting.editNameSelectAll) {
			$.tree._inputSelect(inputObj);
		} else {
			$.tree._inputFocus(inputObj);
		}

		inputObj.bind('blur', function(event) {
			if (!$.tree._editNodeBlur) {
				$.tree._cancelCurEditNode(setting);
			}
		}).bind('keydown', function(event) {
			if (event.keyCode=="13") {
				$.tree._editNodeBlur = true;
				$.tree._cancelCurEditNode(setting);
			} else if (event.keyCode=="27") {
				$.tree._cancelCurEditNode(setting, null, true);
			}
		}).bind('click', function(event) {
			return false;
		}).bind('dblclick', function(event) {
			return false;
		});

		$$(node, consts.id.A, setting).addClass(consts.node.CURSELECTED_EDIT);
		root.curEditInput = inputObj;
		root.noSelection = false;
		root.curEditNode = node;
	},
	_destroy: function(setting) {
		if (!setting) return;
		$.tree._initCache(setting);
		$.tree._initRoot(setting);
		$.tree._unbindTree(setting);
		$.tree._unbindEvent(setting);
		setting.treeObj.empty();
		delete settings[setting.treeId];
	},
	_expandCollapseNode: function(setting, node, expandFlag, animateFlag, callback) {
		var root = $.tree._getRoot(setting),
		childKey = setting.keyChildren;
		if (!node) {
			$.tree._apply(callback, []);
			return;
		}
		if (root.expandTriggerFlag) {
			var _callback = callback;
			callback = function(){
				if (_callback) _callback();
				if (node.open) {
					setting.treeObj.trigger(consts.event.EXPAND, [setting.treeId, node]);
				} else {
					setting.treeObj.trigger(consts.event.COLLAPSE, [setting.treeId, node]);
				}
			};
			root.expandTriggerFlag = false;
		}
		if (!node.open && node.isParent && ((!$$(node, consts.id.UL, setting).get(0)) || (node[childKey] && node[childKey].length>0 && !$$(node[childKey][0], setting).get(0)))) {
			$.tree._appendParentULDom(setting, node);
			$.tree._createNodeCallback(setting);
		}
		if (node.open == expandFlag) {
			$.tree._apply(callback, []);
			return;
		}
		var ulObj = $$(node, consts.id.UL, setting),
		switchObj = $$(node, consts.id.SWITCH, setting),
		icoObj = $$(node, consts.id.ICON, setting);

		if (node.isParent) {
			node.open = !node.open;
			if (node.iconOpen && node.iconClose) {
				icoObj.attr("style", $.tree._makeNodeIcoStyle(setting, node));
			}

			if (node.open) {
				$.tree._replaceSwitchClass(node, switchObj, consts.arrow.DOWN);
				$.tree._replaceIcoClass(node, icoObj, consts.folder.OPEN);
				if (animateFlag == false || setting.expandSpeed == "") {
					ulObj.show();
					$.tree._apply(callback, []);
				} else {
					if (node[childKey] && node[childKey].length > 0) {
						ulObj.slideDown(setting.expandSpeed, callback);
					} else {
						ulObj.show();
						$.tree._apply(callback, []);
					}
				}
			} else {
				$.tree._replaceSwitchClass(node, switchObj, consts.arrow.RIGHT);
				$.tree._replaceIcoClass(node, icoObj, consts.folder.CLOSE );
				if (animateFlag == false || setting.expandSpeed == "" || !(node[childKey] && node[childKey].length > 0)) {
					ulObj.hide();
					$.tree._apply(callback, []);
				} else {
					ulObj.slideUp(setting.expandSpeed, callback);
				}
			}
		} else {
			$.tree._apply(callback, []);
		}
	},
	_expandCollapseParentNode: function(setting, node, expandFlag, animateFlag, callback) {
		if (!node) return;
		if (!node.parentTId) {
			$.tree._expandCollapseNode(setting, node, expandFlag, animateFlag, callback);
			return;
		} else {
			$.tree._expandCollapseNode(setting, node, expandFlag, animateFlag);
		}
		if (node.parentTId) {
			$.tree._expandCollapseParentNode(setting, node.getParentNode(), expandFlag, animateFlag, callback);
		}
	},
	_expandCollapseSonNode: function(setting, node, expandFlag, animateFlag, callback) {
		var root = $.tree._getRoot(setting),
		childKey = setting.keyChildren,
		treeNodes = (node) ? node[childKey]: root[childKey],
		selfAnimateSign = (node) ? false : animateFlag,
		expandTriggerFlag = $.tree._getRoot(setting).expandTriggerFlag;
		$.tree._getRoot(setting).expandTriggerFlag = false;
		if (treeNodes) {
			for (var i = 0, l = treeNodes.length; i < l; i++) {
				if (treeNodes[i]) $.tree._expandCollapseSonNode(setting, treeNodes[i], expandFlag, selfAnimateSign);
			}
		}
		$.tree._getRoot(setting).expandTriggerFlag = expandTriggerFlag;
		$.tree._expandCollapseNode(setting, node, expandFlag, animateFlag, callback );
	},
	_makeDOMNodeIcon: function(html, setting, node) {
		var nameStr = $.tree._getNodeName(setting, node),
		name = setting.nameIsHTML ? nameStr : nameStr.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
		if ( setting.formatter ) {
			name = $.tree._apply(setting.formatter,[setting,node,name, setting],setting.formatter);
		}
		html.push("<span id='", node.tId, consts.id.ICON,
			"' title='' treeNode", consts.id.ICON," class='", $.tree._makeNodeIcoClass(setting, node),
			"' style='", $.tree._makeNodeIcoStyle(setting, node), "'></span><span id='", node.tId, consts.id.SPAN, 
			"' class='coral-tree-node-name'>",name,"</span>");
	},
	_makeDOMNodeLine: function(html, setting, node) {
		html.push("<span id='", node.tId, consts.id.SWITCH,	"' title='' class='", $.tree._makeNodeLineClass(setting, node), "' treeNode", consts.id.SWITCH,"></span>");
	},
	_makeDOMNodeMainAfter: function(html, setting, node) {
		html.push("</li>");
	},
	/*_makeDOMNodeMainBefore: function(html, setting, node) {
		html.push("<li id='", node.tId, "' class='", consts.className.LEVEL, node.level,"' tabindex='0' hidefocus='true' treenode>");
	},*/
	_makeDOMNodeMainBefore: function(html, setting, node) {
		html.push("<li ", (node.hidden ? "style='display:none;' " : ""), "id='", node.tId, "' class='", consts.className.LEVEL, node.level,"' tabindex='0' hidefocus='true' treenode>");
	},
	_makeDOMNodeNameAfter: function(html, setting, node) {
		html.push("</a>");
	},
	_makeDOMNodeNameBefore: function(html, setting, node) {
		var title = $.tree._getNodeTitle(setting, node),
		url = $.tree._makeNodeUrl(setting, node),
		fontcss = $.tree._makeNodeFontCss(setting, node),
		fontStyle = [];
		for (var f in fontcss) {
			fontStyle.push(f, ":", fontcss[f], ";");
		}
		html.push("<a id='", node.tId, consts.id.A, "' class='", consts.className.LEVEL, node.level,"' treeNode", consts.id.A," onclick=\"", (node.click || ''),
			"\" ", ((url != null && url.length > 0) ? "href='" + url + "'" : ""), " target='",$.tree._makeNodeTarget(node),"' style='", fontStyle.join(''),
			"'");
		if ($.tree._apply(setting.showTitle, [setting.treeId, node, setting], setting.showTitle) && title) {html.push("title='", title.replace(/'/g,"&#39;").replace(/</g,'&lt;').replace(/>/g,'&gt;'),"'");}
		html.push(">");
	},
	_addDiyDom : function( html,setting,node ) {
		if (node.level > 0) {
			html.push($.tree._getIndent(setting,node));
		}
	},
	_getIndent: function(setting, node){
		var spaceWidth = 16;
		if (node.getParentNode()) {
			var html = "", lineCss, lineClass = ["cui-icon"];
			if (node.getParentNode().getNextNode()) {
				if (setting.showLine) {
					lineClass.push(consts.line.LINE);
				} else {
					lineClass.push(consts.line.NOLINE);
				}
				html = "<span class='treeLine "+ lineClass.join("-") + "' style='width:" + spaceWidth + "px;'></span>";	
			} else {
				html = "<span class='treeIndent' style='width:" + spaceWidth + "px;'></span>";
			}
			return $.tree._getIndent(setting,node.getParentNode()) + html;
		} else {
			return "";
		}
	},
	_makeNodeFontCss: function(setting, node) {
		var fontCss = $.tree._apply(setting.fontCss, [setting.treeId, node, setting], setting.fontCss);
		return (fontCss && ((typeof fontCss) != "function")) ? fontCss : {};
	},
	_makeNodeIcoClass: function(setting, node) {
		var icon = (node.isParent && node.iconOpen && node.iconClose) ? (node.open ? node.iconOpen : node.iconClose) : node.icon,
			nodeIcon = node.isParent ? " coral-tree-icon-parent" : " coral-tree-icon-leaf";
		if( !icon ){
			var icoCss = ["cui-icon"];
			if (!node.isAjaxing) {
				icoCss[0] = (node.iconSkin ? node.iconSkin + "-" : "") + icoCss[0];
				if (setting.showIcon) {
					if (node.isParent) {
						icoCss.push(node.open ? consts.folder.OPEN : consts.folder.CLOSE);
					} else {
						icoCss.push(consts.folder.FILE);
					}
				}
			} 
			return consts.className.BUTTON + nodeIcon + " " + icoCss.join('-');
		} else {
			if(setting.iconUrl == true){
				return consts.className.BUTTON;
			} else {
				return consts.className.BUTTON + " " + icon;
			}
		}
	},
	_makeNodeIcoStyle: function(setting, node) {
		var icoStyle = [];
		if (!node.isAjaxing) {
			var icon = (node.isParent && node.iconOpen && node.iconClose) ? (node.open ? node.iconOpen : node.iconClose) : node.icon;
			if (icon) {
				if (setting.iconUrl == true) {
					icoStyle.push("background:url(", icon, ") 0 0 no-repeat;");
				} else {
					$.tree._makeNodeIcoClass(setting,node);
				}
			}
				
			if (setting.showIcon == false || !$.tree._apply(setting.showIcon, [setting.treeId, node, setting], true)) {
				icoStyle.push("width:0px;height:0px;display:none");
			}
		}
		return icoStyle.join('');
	},
	_makeNodeLineClass: function(setting, node) {
		var lineClass = [],	classPix = "cui-icon-";
		if (setting.showLine == true) {
			if (node.isParent) {
				lineClass.push(node.open ? consts.arrow.DOWN : consts.arrow.RIGHT );
			} else if(node.isLastNode) {
				lineClass.push(consts.line.BOTTOM);
				classPix = "cui-icon-";
			} else {
				lineClass.push(consts.line.CENTER);
				classPix = "cui-icon-";
			}
		} else {
			if (node.isParent) {
				lineClass.push(node.open ? consts.arrow.DOWN : consts.arrow.RIGHT );
			} else {
				lineClass.push(consts.folder.DOCU);
			}
		}
		return $.tree._makeNodeLineClassEx(node) + classPix + lineClass.join('-');
	},
	_makeNodeLineClassEx: function(node) {
		return consts.className.BUTTON + " " + consts.className.LEVEL + node.level + " " + consts.className.SWITCH + " ";
	},
	_makeNodeTarget: function(node) {
		return (node.target || "_blank");
	},
	_makeNodeUrl: function(setting, node) {
		var urlKey = setting.keyUrl;
		return node[urlKey] ? node[urlKey] : null;
	},
	_makeUlHtml: function(setting, node, html, content) {
		html.push("<ul id='", node.tId, consts.id.UL, "' class='", consts.className.LEVEL, node.level, " ", $.tree._makeUlLineClass(setting, node), "' style='display:", (node.open ? "block": "none"),"'>");
		html.push(content);
		html.push("</ul>");
	},
	_makeUlLineClass: function(setting, node) {
		return ((setting.showLine && !node.isLastNode) ? consts.line.LINE : "");
	},
	_makeChkClass: function(setting, node) {
		var checkedKey = setting.keyChecked,chkName,
		c = consts.checkbox, r = consts.radio,
		fullStyle = "", stateCls = "";
		if (setting.chkStyle == r.STYLE) {
			fullStyle = (node.check_Child_State < 1)? c.CHECKED:c.PARTIAL;
			chkName = "cui-icon-" + setting.chkStyle + "-" + (node[checkedKey] ? c.CHECKED : c.UNCHECKED);
			chkName += fullStyle == c.PARTIAL ? c.PARTIAL : "";
		} else {
			if (node.halfCheck) {
				fullStyle = c.PARTIAL;
			} else {
				fullStyle = node[checkedKey] ? ((node.check_Child_State === 2 || node.check_Child_State === -1) ? c.CHECKED:c.PARTIAL) : ((node.check_Child_State < 1)? c.UNCHECKED:c.PARTIAL);
			}
			chkName = "cui-icon-" + setting.chkStyle + "-" + (fullStyle == c.PARTIAL ? ((node[checkedKey] ? c.CHECKED : c.UNCHECKED) + c.PARTIAL ) : fullStyle);
		}
		stateCls = (node.check_Focus && node.chkDisabled !== true) ? c.FOCUS : (node.chkDisabled == true ? c.DISABLED : c.DEFAULT);
		return consts.className.BUTTON + " " + stateCls  + " " + chkName;
	},
	//增加treeLine上的线
	_addLine: function(setting, node, oldLevel) {
		var i= typeof(oldLevel)=="undefined" ? 1: 0 ; 
		var diff = "";
		var arr = $.tree._transformToArrayFormat(setting, node);
		if (oldLevel != null && node.level != null) {
			diff = (typeof(oldLevel)=="undefined" ? 0: oldLevel) - (typeof(node.level)=="undefined" ? 0: node.level);
		} 
		for (i; i < arr.length; i++) {
			$$(arr[i], consts.id.A, setting).find("span:lt("+ (arr[i].level + diff) +")").remove();
			$$(arr[i], consts.id.A, setting).prepend($.tree._getIndent(setting, arr[i]));
		}
	},
	//判断switchClass的类型
	_lineClass: function(node,showLine){
		var lineClass = [];
		if (node.isParent) {
			lineClass = node.open ? consts.arrow.DOWN : consts.arrow.RIGHT;
		} else {
			if (showLine) {
				if (node.level == 0 && node.isLastNode && node.isFirstNode) {
					lineClass = consts.line.NOLINE;
				} else if (node.level == 0 && node.isFirstNode && !node.isLastNode) {
					lineClass = consts.line.ROOTS;
				} else if (node.isLastNode) {
					lineClass = consts.line.BOTTOM;
				} else {
					lineClass = consts.line.CENTER;
				}
			} 
		}
		return lineClass;
	},
	_moveNode: function(setting, targetNode, node, moveType, animateFlag, isSilent) {
		var root = $.tree._getRoot(setting),html=[],
		childKey = setting.keyChildren;
		if (targetNode == node) return;
		if (setting.keepLeaf && targetNode && !targetNode.isParent && moveType == consts.move.TYPE_INNER) return;
		var oldParentNode = (node.parentTId ? node.getParentNode(): root),
			oldLevelP = oldParentNode.level;
		targetNodeIsRoot = (targetNode === null || targetNode == root);
		if (targetNodeIsRoot && targetNode === null) targetNode = root;
		if (targetNodeIsRoot) moveType = consts.move.TYPE_INNER;
		var targetParentNode = (targetNode.parentTId ? targetNode.getParentNode() : root);

		if (moveType != consts.move.TYPE_PREV && moveType != consts.move.TYPE_NEXT) {
			moveType = consts.move.TYPE_INNER;
		}

		if (moveType == consts.move.TYPE_INNER) {
			if (targetNodeIsRoot) {
				//根节点的父TId是 null
				node.parentTId = null;
			} else {
				if (!targetNode.isParent) {
					targetNode.isParent = true;
					targetNode.open = !!targetNode.open;
					$.tree._setNodeLineIcos(setting, targetNode);
				}
				node.parentTId = targetNode.tId;
			}
		}
		//移动节点 Dom元素
		var targetObj, target_ulObj;
		if (targetNodeIsRoot) {
			targetObj = setting.treeObj;
			target_ulObj = targetObj;
		} else {
			if (!isSilent && moveType == consts.move.TYPE_INNER) {
				$.tree._expandCollapseNode(setting, targetNode, true, false);
			} else if (!isSilent) {
				$.tree._expandCollapseNode(setting, targetNode.getParentNode(), true, false);
			}
			targetObj = $$(targetNode, setting);
			target_ulObj = $$(targetNode, consts.id.UL, setting);
			if (!!targetObj.get(0) && !target_ulObj.get(0)) {
				var ulstr = [];
				$.tree._makeUlHtml(setting, targetNode, ulstr, '');
				targetObj.append(ulstr.join(''));
			}
			target_ulObj = $$(targetNode, consts.id.UL, setting);
		}
		var nodeDom = $$(node, setting);
		if (!nodeDom.get(0)) {
			nodeDom = $.tree._appendNodes(setting, node.level, [node], null, false, true).join('');
		} else if (!targetObj.get(0)) {
			nodeDom.remove();
		}
		if (target_ulObj.get(0) && moveType == consts.move.TYPE_INNER) {
			target_ulObj.append(nodeDom);
		} else if (targetObj.get(0) && moveType == consts.move.TYPE_PREV) {
			targetObj.before(nodeDom);
		} else if (targetObj.get(0) && moveType == consts.move.TYPE_NEXT) {
			targetObj.after(nodeDom);
		}
		//移动节点后，更新相关节点的数据
		var i,l,
		lineClass = [],
		tmpSrcIndex = -1,
		tmpTargetIndex = 0,
		oldNeighbor = null,
		newNeighbor = null,
		oldNeighborLevel = null,
		newNeighborLevel = null,
		oldLevel = node.level;
		if (node.isFirstNode) {
			tmpSrcIndex = 0;
			if (oldParentNode[childKey].length > 1 ) {
				oldNeighbor = oldParentNode[childKey][1];
				oldNeighbor.isFirstNode = true;
				oldNeighborLevel = oldNeighbor.level;
			}
		} else if (node.isLastNode) {
			tmpSrcIndex = oldParentNode[childKey].length -1;
			oldNeighbor = oldParentNode[childKey][tmpSrcIndex - 1];
			oldNeighbor.isLastNode = true;
			oldNeighborLevel = oldNeighbor.level;
		} else {
			for (i = 0, l = oldParentNode[childKey].length; i < l; i++) {
				if (oldParentNode[childKey][i].tId == node.tId) {
					tmpSrcIndex = i;
					break;
				}
			}
		}
		if (tmpSrcIndex >= 0) {
			oldParentNode[childKey].splice(tmpSrcIndex, 1);
		}
		if (moveType != consts.move.TYPE_INNER) {
			for (i = 0, l = targetParentNode[childKey].length; i < l; i++) {
				if (targetParentNode[childKey][i].tId == targetNode.tId) tmpTargetIndex = i;
			}
		}
		if (moveType == consts.move.TYPE_INNER) {
			if (!targetNode[childKey]) targetNode[childKey] = new Array();
			if (targetNode[childKey].length > 0) {
				newNeighbor = targetNode[childKey][targetNode[childKey].length - 1];
				newNeighbor.isLastNode = false;
				newNeighborLevel = newNeighbor.level;
			}
			targetNode[childKey].splice(targetNode[childKey].length, 0, node);
			node.isLastNode = true;
			node.isFirstNode = (targetNode[childKey].length == 1);
		} else if (targetNode.isFirstNode && moveType == consts.move.TYPE_PREV) {
			targetParentNode[childKey].splice(tmpTargetIndex, 0, node);
			newNeighbor = targetNode;
			newNeighbor.isFirstNode = false;
			newNeighborLevel = newNeighbor.level;
			node.parentTId = targetNode.parentTId;
			node.isFirstNode = true;
			node.isLastNode = false;
		} else if (targetNode.isLastNode && moveType == consts.move.TYPE_NEXT) {
			targetParentNode[childKey].splice(tmpTargetIndex + 1, 0, node);
			newNeighbor = targetNode;
			newNeighbor.isLastNode = false;
			newNeighborLevel = newNeighbor.level;
			node.parentTId = targetNode.parentTId;
			node.isFirstNode = false;
			node.isLastNode = true;
		} else {
			if (moveType == consts.move.TYPE_PREV) {
				targetParentNode[childKey].splice(tmpTargetIndex, 0, node);
			} else {
				targetParentNode[childKey].splice(tmpTargetIndex + 1, 0, node);
			}
			node.parentTId = targetNode.parentTId;
			node.isFirstNode = false;
			node.isLastNode = false;
		}
		$.tree._fixPIdKeyValue(setting, node);
		$.tree._setSonNodeLevel(setting, node.getParentNode(), node);
		//更新被移动的节点数据
		var nodeObj = $$(node, consts.id.SWITCH, setting);
		$.tree._setNodeLineIcos(setting, node);
		$.tree._repairNodeLevelClass(setting, node, oldLevel);
		$.tree._addLine(setting, node,oldLevel);
		lineClass = $.tree._lineClass(node, setting.showLine);
		$.tree._replaceSwitchClass(node, nodeObj, lineClass);
		//更新被移动节点的原来父节点的 dom 元素
		if (!setting.keepParent && oldParentNode[childKey].length < 1) {
			//如果原来的父节点没有子节点元素
			oldParentNode.isParent = false;
			oldParentNode.open = false;
			var tmp_ulObj = $$(oldParentNode, consts.id.UL, setting),
			tmp_switchObj = $$(oldParentNode, consts.id.SWITCH, setting),
			tmp_icoObj = $$(oldParentNode, consts.id.ICON, setting);
			lineClass = $.tree._lineClass(oldParentNode, setting.showLine);
			$.tree._replaceSwitchClass(oldParentNode, tmp_switchObj, lineClass);
			$.tree._replaceIcoClass(oldParentNode, tmp_icoObj, consts.folder.FILE);
			tmp_ulObj.css("display", "none");
			$.tree._addLine(setting, oldParentNode,oldLevelP);
		} else if (oldNeighbor) {
			//原来的相邻节点
			$.tree._setNodeLineIcos(setting, oldNeighbor);
			$.tree._addLine(setting, oldNeighbor,oldNeighborLevel);
		}
		//新的相邻节点
		if (newNeighbor) {
			tmp_switchObj = $$(newNeighbor, consts.id.SWITCH, setting);
			$.tree._setNodeLineIcos(setting, newNeighbor);
			$.tree._addLine(setting, newNeighbor,newNeighborLevel);
			lineClass = $.tree._lineClass(newNeighbor, setting.showLine);
			$.tree._replaceSwitchClass(newNeighbor, tmp_switchObj, lineClass);
		}
		//移动后更新 checkbox / radio
		if (!!setting.check && setting.checkable && $.tree._repairChkClass) {
			$.tree._repairChkClass(setting, oldParentNode);
			$.tree._repairParentChkClassWithSelf(setting, oldParentNode);
			if (oldParentNode != node.parent)
				$.tree._repairParentChkClassWithSelf(setting, node);
		}

		//移动后展开父节点
		if (!isSilent) {
			$.tree._expandCollapseParentNode(setting, node.getParentNode(), true, animateFlag);
		}
	},
	_removeChildNodes: function(setting, node) {
		if (!node) return;
		var childKey = setting.keyChildren,
		nodes = node[childKey];
		if (!nodes) return;

		for (var i = 0, l = nodes.length; i < l; i++) {
			$.tree._removeNodeCache(setting, nodes[i]);
		}
		$.tree._removeSelectedNode(setting);
		delete node[childKey];

		if (!setting.keepParent) {
			node.isParent = false;
			node.open = false;
			var tmp_switchObj = $$(node, consts.id.SWITCH, setting),
			tmp_icoObj = $$(node, consts.id.ICON, setting);
			$.tree._replaceSwitchClass(node, tmp_switchObj, consts.line.CENTER);
			$.tree._replaceIcoClass(node, tmp_icoObj, consts.line.CENTER);
			$$(node, consts.id.UL, setting).remove();
		} else {
			$$(node, consts.id.UL, setting).empty();
		}
	},
	// lihaibo add
	_showNode: function(setting, node, options, keyword) {
		node.hidden = false;
		$.tree._initShowForExCheck(setting, node);
		if(setting.enableHighlight){
			var keywordArr = keyword.replace(/[\s]+/g,' ').split(' '), re;
			if($$(node,setting).find(".coral-tree-node-name").children()[0]) {
				$$(node,setting).find("> a >.coral-tree-node-name").html(node.name);
			}
			var content = $$(node,setting).find("> a >.coral-tree-node-name").html(); 
			for (var n = 0; n < keywordArr.length; n ++) {
				re = new RegExp("" + keywordArr[n] + "","gmi");
				if(keywordArr[n] != ""){
					content = content.replace(re,'<span class="coral-keyword-highlight">' + keywordArr[n] + '</span>');
				}
			}
			$$(node,setting).find("> a >.coral-tree-node-name").html(content);
		}
		$$(node, setting).show();
		if ( typeof options !== "undefined" && typeof options.showParents !== "undefined" && options.showParents && null !== node.getParentNode() ) {
			$.tree._showNode (setting, node.getParentNode(), options, keyword);
		}
	},
	_initShowForExCheck: function(setting, n) {
		if (!n.hidden && setting.checkable) {
			if(typeof n._nocheck != "undefined") {
				n.nocheck = n._nocheck;
				delete n._nocheck;
			}
			if ($.tree._setChkClass) {
				var checkObj = $$(n, consts.id.CHECK, setting);
				$.tree._setChkClass(setting, checkObj, n);
			}
			if ($.tree._repairParentChkClassWithSelf) {
				$.tree._repairParentChkClassWithSelf(setting, n);
			}
		}
	},
	_showNodes: function(setting, nodes, options, v) {
		if (!nodes || nodes.length == 0) {
			return;
		}
		var pList = {}, i, j;
		for (i=0, j=nodes.length; i<j; i++) {
			var n = nodes[i];
			if (!pList[n.parentTId]) {
				var pn = n.getParentNode();
				pList[n.parentTId] = (pn === null) ? $.tree._getRoot(setting) : n.getParentNode();
			}
			//var content = $$(n,setting).find(".coral-tree-node-name").html(); 
			//content = content.replace(n.name,'<span class="coral-keyword-highlight">' + n.name + '</span>');
			//$$(n,setting).find(".coral-tree-node-name").html(content);
			$.tree._showNode(setting, n, options, v);
		}
		for (var tId in pList) {
			var children = pList[tId][setting.keyChildren];
			$.tree._setFirstNodeForShow(setting, children);
			$.tree._setLastNodeForShow(setting, children);
		}
	},
	_setFirstNodeForShow: function(setting, nodes) {
		var n,i,j, first, old;
		for(i=0, j=nodes.length; i<j; i++) {
			n = nodes[i];
			if (!first && !n.hidden && n.isFirstNode) {
				first = n;
				break;
			} else if (!first && !n.hidden && !n.isFirstNode) {
				n.isFirstNode = true;
				first = n;
				$.tree._setNodeLineIcos(setting, n);
			} else if (first && n.isFirstNode) {
				n.isFirstNode = false;
				old = n;
				$.tree._setNodeLineIcos(setting, n);
				break;
			} else {
				n = null;
			}
		}
		return {"new":first, "old":old};
	},
	_setLastNodeForShow: function(setting, nodes) {
		var n,i,j, last, old;
		for (i=nodes.length-1; i>=0; i--) {
			n = nodes[i];
			if (!last && !n.hidden && n.isLastNode) {
				last = n;
				break;
			} else if (!last && !n.hidden && !n.isLastNode) {
				n.isLastNode = true;
				last = n;
				$.tree._setNodeLineIcos(setting, n);
			} else if (last && n.isLastNode) {
				n.isLastNode = false;
				old = n;
				$.tree._setNodeLineIcos(setting, n);
				break;
			} else {
				n = null;
			}
		}
		return {"new":last, "old":old};
	},
	_hideNode: function(setting, node, options) {
		node.hidden = true;
		node.isFirstNode = false;
		node.isLastNode = false;
		$.tree._initHideForExCheck(setting, node);
		$.tree._cancelPreSelectedNode(setting, node);
		$$(node, setting).hide();
	},
	_initHideForExCheck: function(setting, n) {
		if (n.hidden && setting.checkable) {
			if(typeof n._nocheck == "undefined") {
				n._nocheck = !!n.nocheck
				n.nocheck = true;
			}
			n.check_Child_State = -1;
			if ($.tree._repairParentChkClassWithSelf) {
				$.tree._repairParentChkClassWithSelf(setting, n);
			}
		}
	},
	_hideNodes: function(setting, nodes, options) {
		if (!nodes || nodes.length == 0) {
			return;
		}
		var pList = {}, i, j;
		for (i=0, j=nodes.length; i<j; i++) {
			var n = nodes[i];
			if ((n.isFirstNode || n.isLastNode) && !pList[n.parentTId]) {
				var pn = n.getParentNode();
				pList[n.parentTId] = (pn === null) ? $.tree._getRoot(setting) : n.getParentNode();
			}
			$.tree._hideNode(setting, n, options);
		}
		for (var tId in pList) {
			var children = pList[tId][setting.keyChildren];
			$.tree._setFirstNodeForHide(setting, children);
			$.tree._setLastNodeForHide(setting, children);
		}
	},
	_setFirstNodeForHide: function(setting, nodes) {
		var n,i,j;
		for (i=0, j=nodes.length; i<j; i++) {
			n = nodes[i];
			if (n.isFirstNode) {
				break;
			}
			if (!n.hidden && !n.isFirstNode) {
				n.isFirstNode = true;
				$.tree._setNodeLineIcos(setting, n);
				break;
			} else {
				n = null;
			}
		}
		return n;
	},
	_setLastNodeForHide: function(setting, nodes) {
		var n,i;
		for (i=nodes.length-1; i>=0; i--) {
			n = nodes[i];
			if (n.isLastNode) {
				break;
			}
			if (!n.hidden && !n.isLastNode) {
				n.isLastNode = true;
				$.tree._setNodeLineIcos(setting, n);
				break;
			} else {
				n = null;
			}
		}
		return n;
	},
	// lihaibo add end
	_setFirstNode: function(setting, parentNode) {
		var childKey = setting.keyChildren, childLength = parentNode[childKey].length;
		if ( childLength > 0) {
			parentNode[childKey][0].isFirstNode = true;
		}
	},
	_setLastNode: function(setting, parentNode) {
		var childKey = setting.keyChildren, childLength = parentNode[childKey].length;
		if ( childLength > 0) {
			parentNode[childKey][childLength - 1].isLastNode = true;
		}
	},
	_removeNode: function(setting, node) {
		var root = $.tree._getRoot(setting),lineClass=[],
		childKey = setting.keyChildren,
		oldLevel = node.level,
		prevNode = node.getPreNode(),
		nextNode = node.getNextNode(),
		parentNode = (node.parentTId) ? node.getParentNode() : root,
		parentNodeOldlevel = parentNode.level;

		node.isFirstNode = false;
		node.isLastNode = false;
		node.getPreNode = function() {return null;};
		node.getNextNode = function() {return null;};
		if (!$.tree._getNodeCache(setting, node.tId)) {
			return;
		}

		$$(node, setting).remove();
		$.tree._removeNodeCache(setting, node);
		$.tree._removeSelectedNode(setting, node);

		for (var i = 0, l = parentNode[childKey].length; i < l; i++) {
			if (parentNode[childKey][i].tId == node.tId) {
				parentNode[childKey].splice(i, 1);
				break;
			}
		}
		$.tree._setFirstNode(setting, parentNode);
		$.tree._setLastNode(setting, parentNode);

		var tmp_ulObj,tmp_switchObj,tmp_icoObj,
		childLength = parentNode[childKey].length;
		//删除节点后更新原来的父节点
		if (!setting.keepParent && childLength == 0) {
			//如果原来的父节点没有子节点
			parentNode.isParent = false;
			parentNode.open = false;
			tmp_ulObj = $$(parentNode, consts.id.UL, setting);
			tmp_switchObj = $$(parentNode, consts.id.SWITCH, setting);
			tmp_icoObj = $$(parentNode, consts.id.ICON, setting);
			lineClass = $.tree._lineClass(parentNode, setting.showLine);
			$.tree._replaceSwitchClass(parentNode, tmp_switchObj, lineClass);
			$.tree._replaceIcoClass(parentNode, tmp_icoObj, consts.folder.FILE);
			tmp_ulObj.css("display", "none");
		} else if (setting.showLine && childLength > 0) {
			//如果原来的父节点有子节点
			var newLast = parentNode[childKey][childLength - 1];
			tmp_ulObj = $$(newLast, consts.id.UL, setting);
			tmp_switchObj = $$(newLast, consts.id.SWITCH, setting);
			tmp_icoObj = $$(newLast, consts.id.ICON, setting);
			if (parentNode == root) {
				if (parentNode[childKey].length == 1) {
					//如果移除的是树的根节点，并且该树只有这一个根节点时
					$.tree._replaceSwitchClass(newLast, tmp_switchObj, consts.arrow.DOWN);
				} else {
					var tmp_first_switchObj = $$(parentNode[childKey][0], consts.id.SWITCH, setting);
					$.tree._replaceSwitchClass(parentNode[childKey][0], tmp_first_switchObj, (parentNode[childKey][0].isParent ? (parentNode[childKey][0].open == true ? consts.arrow.DOWN: consts.arrow.RIGHT) : consts.line.ROOTS));
					if( !newLast.isParent){
						$.tree._replaceSwitchClass(newLast, tmp_switchObj, consts.line.BOTTOM);
					} else {
						$.tree._replaceSwitchClass(newLast, tmp_switchObj,(node.open == true ? consts.arrow.DOWN: consts.arrow.RIGHT) );
					}
				}
			} else {
				if (newLast.isParent) {
					$.tree._replaceSwitchClass(newLast, tmp_switchObj, (newLast.open ? consts.arrow.DOWN: consts.arrow.RIGHT ));
				} else {
					$.tree._replaceSwitchClass(newLast, tmp_switchObj, consts.line.BOTTOM);
				}
			}
			$.tree._addLine(setting, parentNode, parentNodeOldlevel);
			tmp_ulObj.removeClass(consts.line.LINE);
		}
	},
	_replaceIcoClass: function(node, obj, newName) {
		var icon = (node.isParent && node.iconOpen && node.iconClose) ? (node.open ? node.iconOpen : node.iconClose) : node.icon;
		if (!obj || node.isAjaxing) return;
		var tmpName = obj.attr("class");
		if (tmpName == undefined) return;
		var tmpList = tmpName.split("-");
		switch (newName) {
			case consts.folder.OPEN:
			case consts.folder.CLOSE:
			case consts.folder.FILE:
			case consts.line.CENTER:
			case consts.line.BOTTOM:
			case consts.line.LINE:
				if (icon) {
					obj.attr("class", tmpList);
				} else {
					tmpList[tmpList.length-1] = newName
					obj.attr("class", tmpList.join("-"));
				}
				break;
		}
	},
	_replaceSwitchClass: function(node, obj, newName) {
		if (!obj) return;
		var tmpName = obj.attr("class");
		if (tmpName == undefined) return;
		var tmpList = tmpName.split("-");
		switch (newName) {
			case consts.arrow.DOWN:
			case consts.arrow.RIGHT:
			case consts.line.NOLINE:
				tmpList[tmpList.length-1] = newName;
				break;
			case consts.folder.OPEN:
			case consts.folder.CLOSE:
			case consts.line.ROOTS:
			case consts.line.CENTER:
			case consts.line.BOTTOM:
			case consts.folder.DOCU:
			case consts.line.LINE:
				tmpList[tmpList.length-1] = newName;
				break;
		}
		obj.attr("class", tmpList.join("-"));
		if (newName !== consts.folder.DOCU) {
			obj.removeAttr("disabled");
		} else {
			obj.attr("disabled", "disabled");
		}
	},
	
	_repairAllChk: function(setting, checked) {
		if (setting.checkable && setting.chkStyle === consts.checkbox.STYLE) {
			var checkedKey = setting.keyChecked,
			childKey = setting.keyChildren,
			root = $.tree._getRoot(setting);
			for (var i = 0, l = root[childKey].length; i<l ; i++) {
				var node = root[childKey][i];
				if (node.nocheck !== true && node.chkDisabled !== true) {
					node[checkedKey] = checked;
				}
				$.tree._setSonNodeCheckBox(setting, node, checked);
			}
		}
	},
	_repairChkClass: function(setting, node) {
		if (!node) return;
		$.tree._makeChkFlag(setting, node);
		if (node.nocheck !== true) {
			var checkObj = $$(node, consts.id.CHECK, setting);
			$.tree._setChkClass(setting, checkObj, node);
		}
	},
	_repairParentChkClass: function(setting, node) {
		if (!node || !node.parentTId) return;
		var pNode = node.getParentNode();
		$.tree._repairChkClass(setting, pNode);
		$.tree._repairParentChkClass(setting, pNode);
	},
	_repairParentChkClassWithSelf: function(setting, node) {
		if (!node) return;
		var childKey = setting.keyChildren;
		if (node[childKey] && node[childKey].length > 0) {
			$.tree._repairParentChkClass(setting, node[childKey][0]);
		} else {
			$.tree._repairParentChkClass(setting, node);
		}
	},
	_repairSonChkDisabled: function(setting, node, chkDisabled, inherit) {
		if (!node) return;
		var childKey = setting.keyChildren;
		if (node.chkDisabled != chkDisabled) {
			node.chkDisabled = chkDisabled;
		}
		$.tree._repairChkClass(setting, node);
		if (node[childKey] && inherit) {
			for (var i = 0, l = node[childKey].length; i < l; i++) {
				var sNode = node[childKey][i];
				$.tree._repairSonChkDisabled(setting, sNode, chkDisabled, inherit);
			}
		}
	},
	_repairParentChkDisabled: function(setting, node, chkDisabled, inherit) {
		if (!node) return;
		if (node.chkDisabled != chkDisabled && inherit) {
			node.chkDisabled = chkDisabled;
		}
		$.tree._repairChkClass(setting, node);
		$.tree._repairParentChkDisabled(setting, node.getParentNode(), chkDisabled, inherit);
	},
	
	_selectNode: function(setting, node, addFlag) {
		if (!addFlag) {
			$.tree._cancelPreSelectedNode(setting);
		}
		$$(node, consts.id.A, setting).addClass(consts.node.CURSELECTED);
		$.tree._addSelectedNode(setting, node);
	},
	_setNodeFontCss: function(setting, treeNode) {
		var aObj = $$(treeNode, consts.id.A, setting),
		fontCss = $.tree._makeNodeFontCss(setting, treeNode);
		if (fontCss) {
			aObj.css(fontCss);
		}
	},
	_setNodeLineIcos: function(setting, node) {
		if (!node) return;
		var switchObj = $$(node, consts.id.SWITCH, setting),
		ulObj = $$(node, consts.id.UL, setting),
		icoObj = $$(node, consts.id.ICON, setting),
		ulLine = $.tree._makeUlLineClass(setting, node);
		if (ulLine.length==0) {
			ulObj.removeClass(consts.line.LINE);
		} else {
			ulObj.addClass(ulLine);
		}
		switchObj.attr("class", $.tree._makeNodeLineClass(setting, node));
		if (node.isParent) {
			switchObj.removeAttr("disabled");
		} else {
			switchObj.attr("disabled", "disabled");
		}
		icoObj.removeAttr("style");
		icoObj.attr("style", $.tree._makeNodeIcoStyle(setting, node));
		icoObj.attr("class", $.tree._makeNodeIcoClass(setting, node));
	},
	_setNodeName: function(setting, node) {
		var title = $.tree._getNodeTitle(setting, node),
		nObj = $$(node, consts.id.SPAN, setting);
		nObj.empty();
		var names = "";
		if ( setting.formatter ) {
			names = $.tree._apply(setting.formatter,[setting,node, setting],setting.formatter);
		} else {
			names = $.tree._getNodeName(setting, node);
		}
		if (setting.nameIsHTML) {
			nObj.html(names);
		} else {
			nObj.text(names);
		}
		if ($.tree._apply(setting.showTitle, [setting.treeId, node, setting], setting.showTitle)) {
			var aObj = $$(node, consts.id.A, setting);
			aObj.attr("title", !title ? "" : title);
		}
	},
	_setNodeTarget: function(setting, node) {
		var aObj = $$(node, consts.id.A, setting);
		aObj.attr("target", $.tree._makeNodeTarget(node));
	},
	_setNodeUrl: function(setting, node) {
		var aObj = $$(node, consts.id.A, setting),
		url = $.tree._makeNodeUrl(setting, node);
		if (url == null || url.length == 0) {
			aObj.removeAttr("href");
		} else {
			aObj.attr("href", url);
		}
	},
	_switchNode: function(setting, node) {
		if (node.open || !$.tree._canAsync(setting, node)) {
			$.tree._expandCollapseNode(setting, node, !node.open);
		} else if (setting.asyncEnable) {
			if (!$.tree._asyncNode(setting, node)) {
				$.tree._expandCollapseNode(setting, node, !node.open);
				return;
			}
		} else if (node) {
			$.tree._expandCollapseNode(setting, node, !node.open);
		}
	},
	



	_setChkClass: function(setting, obj, node) {
		if (!obj) return;
		if (node.nocheck === true) {
			obj.hide();
		} else {
			obj.show();
		}
        obj.attr('class', $.tree._makeChkClass(setting, node));
	},
	_setParentNodeCheckBox: function(setting, node, value, srcNode) {
		var childKey = setting.keyChildren,
		checkedKey = setting.keyChecked,
		checkObj = $$(node, consts.id.CHECK, setting);
		if (!srcNode) srcNode = node;
		$.tree._makeChkFlag(setting, node);
		if (node.nocheck !== true && node.chkDisabled !== true) {
			node[checkedKey] = value;
			$.tree._setChkClass(setting, checkObj, node);
			if (setting.autoCheckTrigger && node != srcNode) {
				setting.treeObj.trigger(consts.event.CHECK, [null, setting.treeId, node]);
			}
		}
		if (node.parentTId) {
			var pSign = true;
			if (!value) {
				var pNodes = node.getParentNode()[childKey];
				for (var i = 0, l = pNodes.length; i < l; i++) {
					if ((pNodes[i].nocheck !== true && pNodes[i].chkDisabled !== true && pNodes[i][checkedKey])
					|| ((pNodes[i].nocheck === true || pNodes[i].chkDisabled === true) && pNodes[i].check_Child_State > 0)) {
						pSign = false;
						break;
					}
				}
			}
			if (pSign) {
				$.tree._setParentNodeCheckBox(setting, node.getParentNode(), value, srcNode);
			}
		}
	},
	_setSonNodeCheckBox: function(setting, node, value, srcNode) {
		if (!node) return;
		var childKey = setting.keyChildren,
		checkedKey = setting.keyChecked,
		checkObj = $$(node, consts.id.CHECK, setting);
		if (!srcNode) srcNode = node;

		var hasDisable = false;
		if (node[childKey]) {
			for (var i = 0, l = node[childKey].length; i < l && node.chkDisabled !== true; i++) {
				var sNode = node[childKey][i];
				$.tree._setSonNodeCheckBox(setting, sNode, value, srcNode);
				if (sNode.chkDisabled === true) hasDisable = true;
			}
		}

		if (node != $.tree._getRoot(setting) && node.chkDisabled !== true) {
			if (hasDisable && node.nocheck !== true) {
				$.tree._makeChkFlag(setting, node);
			}
			if (node.nocheck !== true && node.chkDisabled !== true) {
				node[checkedKey] = value;
				if (!hasDisable) node.check_Child_State = (node[childKey] && node[childKey].length > 0) ? (value ? 2 : 0) : -1;
			} else {
				node.check_Child_State = -1;
			}
			$.tree._setChkClass(setting, checkObj, node);
			if (setting.autoCheckTrigger && node != srcNode && node.nocheck !== true && node.chkDisabled !== true) {
				setting.treeObj.trigger(consts.event.CHECK, [null, setting.treeId, node]);
			}
		}

	},



	_removeEditBtn: function(setting, node) {
		$$(node, consts.id.EDIT, setting).unbind().remove();
	},
	_removeRemoveBtn: function(setting, node) {
		$$(node, consts.id.REMOVE, setting).unbind().remove();
	},
	_removeTreeDom: function(setting, node) {
		node.isHover = false;
		$.tree._removeEditBtn(setting, node);
		$.tree._removeRemoveBtn(setting, node);
		$$(node, consts.id.A, setting).removeClass(consts.node.CURHOVER);
		$.tree._apply(setting.removeHoverDom, [setting.treeId, node, setting]);
	},
	_repairNodeLevelClass: function(setting, node, oldLevel) {
		if (oldLevel === node.level) return;
		var liObj = $$(node, setting),
		aObj = $$(node, consts.id.A, setting),
		ulObj = $$(node, consts.id.UL, setting),
		oldClass = consts.className.LEVEL + oldLevel,
		newClass = consts.className.LEVEL + node.level;
		liObj.removeClass(oldClass);
		liObj.addClass(newClass);
		aObj.removeClass(oldClass);
		aObj.addClass(newClass);
		ulObj.removeClass(oldClass);
		ulObj.addClass(newClass);
	},
	_selectNodes : function(setting, nodes) {
		for (var i=0, l=nodes.length; i<l; i++) {
			$.tree._selectNode(setting, nodes[i], i>0);
		}
	}
};		
	
var settings = {}, roots = {}, caches = {},
_init = {
	bind: [$.tree._coreBindEvent,$.tree._checkBindEvent,$.tree._editBindEvent],
	unbind: [$.tree._coreUnbindEvent,$.tree._checkUnbindEvent,$.tree._editUnbindEvent],
	caches: [$.tree._coreInitCache,$.tree._checkInitCache,$.tree._editInitCache],
	nodes: [$.tree._coreInitNode,$.tree._checkInitNode,$.tree._editInitNode],
	proxys: [$.tree._checkEventProxy,$.tree._coreEventProxy,$.tree._editEventProxy],
	roots: [$.tree._coreInitRoot,$.tree._checkInitRoot,$.tree._editInitRoot],
	beforeA: [$.tree._beforeA],
	afterA: [],
	innerBeforeA: [],
	innerAfterA: [],
	treeTools: [$.tree._checkTreeTools,$.tree._editTreeTools]
};
//定义树，并提供常用方法
$.coral.tree = {
	//lihaibo add begin
	componentName: "tree",
	//lihaibo add end
	consts : _final,
	//根据 treeId 获取 Tree 对象的方法
	getTreeObj: function(treeId) {
		var o = $.tree._getTreeTools(treeId);
		return o ? o : null;
	},
	//销毁 Tree 的方法
	destroy: function(treeId) {
		if (!!treeId && treeId.length > 0) {
			$.tree._destroy($.tree._getSetting(treeId));
		} else {
			for(var s in settings) {
				$.tree._destroy(settings[s]);
			}
		}
	},
	//Tree 初始化方法，创建 Tree 必须使用此方法
	init: function(obj, optionData, nodeData) {
		//添加tree组件样式
		if(!obj.hasClass("coral-tree")) obj.addClass("coral-tree");
		obj.attr("component-role", "tree");
		if ( optionData.clickExpand ){
			_option.clickExpand = true;
			_option.dblClickExpand = false;
		}
		var setting = $.tree._clone(_option);
		//兼容 “事件大小写” 写法 
		/*if($.migrate && optionData && $.migrate[this.componentName]) {
			optionData = $.extend(true, {}, optionData, $.migrate[this.componentName](optionData));
		}*/
		$.extend(true, setting, optionData);
		setting.treeId = $(obj).uniqueId().attr("id");
		setting.treeObj = obj;
		setting.treeObj.empty();
		settings[setting.treeId] = setting;
		
		if ( setting.cls ){
			$( obj ).addClass( setting.cls );
		}
		if ( setting.componentCls ){
			$( obj ).addClass( setting.componentCls );
		}
		//For some older browser,(e.g., ie6)
		if(typeof document.body.style.maxHeight === "undefined") {
			setting.expandSpeed = "";
		}
		$.tree._initRoot(setting);
		var root = $.tree._getRoot(setting),
		childKey = setting.keyChildren;
		nodeData = nodeData ? $.tree._clone($.tree._isArray(nodeData)? nodeData : [nodeData]) : [];
		nodeData = $.tree._createRootNodes(setting, nodeData );
		if (setting.simpleDataEnable) {
			root[childKey] = $.tree._transformToTreeFormat(setting, nodeData);
		} else {
			root[childKey] = nodeData;
		}
		$.tree._initCache(setting);
		$.tree._unbindTree(setting);
		$.tree._bindTree(setting);
		$.tree._unbindEvent(setting);
		$.tree._bindEvent(setting);

		var treeTools = {
			// setting 配置数据
			setting: setting,
			option: function( key, value ) {
				var options = key,
					parts,
					curOption,
					i;

				if ( arguments.length === 0 ) {
					// TODO: check the options is reference
					// don't return a reference to the internal hash
					return $.extend( {}, treeTools.setting );
				}

				if ( typeof key === "string" ) {
					// handle nested keys, e.g., "foo.bar" => { foo: { bar: ___ } }
					options = {};
					parts = key.split( "." );
					key = parts.shift();
					if ( parts.length ) {
						curOption = options[ key ] = $.extend( {}, treeTools.setting[ key ] );
						for ( i = 0; i < parts.length - 1; i++ ) {
							curOption[ parts[ i ] ] = curOption[ parts[ i ] ] || {};
							curOption = curOption[ parts[ i ] ];
						}
						key = parts.pop();
						if ( arguments.length === 1 ) {
							return curOption[ key ] === undefined ? null : curOption[ key ];
						}
						curOption[ key ] = value;
					} else {
						if ( arguments.length === 1 ) {
							return treeTools.setting[ key ] === undefined ? null : treeTools.setting[ key ];
						}
						options[ key ] = value;
					}
				}
				
				

				this._setOptions( options );

				return this;
			},
			_setOptions: function( options ) {
				var key;

				for ( key in options ) {
					this._setOption( key, options[ key ] );
				}

				return this;
			},
			_setOption: function( key, value ) {
				treeTools.setting[ key ] = value;
				if ( key === "disabled" ) {
					if ( value ) {
						$("#"+setting.treeId).prepend("<div class='coral-state-disabled tree-disabled'></div>");
					}else{
						$("#"+setting.treeId+" .tree-disabled").remove();
					}
				}
				return this;
			},
			disable: function(){
				return treeTools._setOptions({ disabled: true });
			},
			enable: function(){
				return treeTools._setOptions({ disabled: false });
			},
			//添加节点
			addNodes: function(parentNode, newNodes, isSilent) {
				var oldLevel = parentNode != null ? (typeof(parentNode.level)=="undefined" ? 0 : parentNode.level) : null;
				if (!newNodes) return null;
				if (!parentNode) parentNode = null;
				if (parentNode && !parentNode.isParent && setting.keepLeaf) return null;
				var xNewNodes = $.tree._clone($.tree._isArray(newNodes)? newNodes: [newNodes]);
				function addCallback() {
					$.tree._addNodes(setting, parentNode, xNewNodes, (isSilent==true));
				}

				if ($.tree._canAsync(setting, parentNode)) {
					$.tree._asyncNode(setting, parentNode, isSilent, addCallback);
				} else {
					addCallback();
				}
				$.tree._addLine(setting, parentNode,oldLevel);
				return xNewNodes;
			},
			//取消节点的选中状态
			cancelSelectedNode: function(node) {
				$.tree._cancelPreSelectedNode(setting, node);
			},
			//销毁 Tree 的方法
			destroy: function() {
				$.tree._destroy(setting);
			},
			
			//展开 / 折叠 全部节点
			expandAll: function(expandFlag) {
				expandFlag = !!expandFlag;
				$.tree._expandCollapseSonNode(setting, null, expandFlag, true);
				return expandFlag;
			},
			//展开 / 折叠 指定的节点
			expandNode: function(node, expandFlag, sonSign, focus, callbackFlag) {
				if (!node || !node.isParent) return null;
				if (expandFlag !== true && expandFlag !== false) {
					expandFlag = !node.open;
				}
				callbackFlag = !!callbackFlag;

				if (callbackFlag && expandFlag && ($.tree._apply(setting.beforeExpand, [setting.treeId, node, setting], true) == false)) {
					return null;
				} else if (callbackFlag && !expandFlag && ($.tree._apply(setting.beforeCollapse, [setting.treeId, node, setting], true) == false)) {
					return null;
				}
				if (expandFlag && node.parentTId) {
					$.tree._expandCollapseParentNode(setting, node.getParentNode(), expandFlag, false);
				}
				if (expandFlag === node.open && !sonSign) {
					return null;
				}

				$.tree._getRoot(setting).expandTriggerFlag = callbackFlag;
				if (!$.tree._canAsync(setting, node) && sonSign) {
					$.tree._expandCollapseSonNode(setting, node, expandFlag, true, function() {
						if (focus !== false) {try{$$(node, setting).focus().blur();}catch(e){}}
					});
				} else {
					node.open = !expandFlag;
					$.tree._switchNode(this.setting, node);
					if (focus !== false) {try{$$(node, setting).focus().blur();}catch(e){}}
				}
				return expandFlag;
			},
			//获取 Tree 的全部节点数据
			getNodes: function() {
				return $.tree._getNodes(setting);
			},
			//根据节点数据的属性搜索，获取条件完全匹配的节点数据 JSON 对象
			getNodeByParam: function(key, value, parentNode) {
				if (!key) return null;
				return $.tree._getNodeByParam(setting, parentNode?parentNode[setting.keyChildren]:$.tree._getNodes(setting), key, value);
			},
			//根据 Tree 的唯一标识 tId 快速获取节点 JSON 数据对象
			getNodeByTId: function(tId) {
				return $.tree._getNodeCache(setting, tId);
			},
			//根据节点数据的属性搜索，获取条件完全匹配的节点数据 JSON 对象集合
			getNodesByParam: function(key, value, parentNode) {
				if (!key) return null;
				return $.tree._getNodesByParam(setting, parentNode?parentNode[setting.keyChildren]:$.tree._getNodes(setting), key, value);
			},
			//根据节点数据的属性搜索，获取条件模糊匹配的节点数据 JSON 对象集合
			getNodesByParamFuzzy: function(key, value, parentNode) {
				if (!key) return null;
				return $.tree._getNodesByParamFuzzy(setting, parentNode?parentNode[setting.keyChildren]:$.tree._getNodes(setting), key, value);
			},
			//根据自定义规则搜索节点数据 JSON 对象集合 或 单个节点数据
			getNodesByFilter: function(filter, isSingle, parentNode, invokeParam) {
				isSingle = !!isSingle;
				if (!filter || (typeof filter != "function")) return (isSingle ? null : []);
				return $.tree._getNodesByFilter(setting, parentNode?parentNode[setting.keyChildren]:$.tree._getNodes(setting), filter, isSingle, invokeParam);
			},
			//获取某节点在同级节点中的序号（从0开始）
			getNodeIndex: function(node) {
				if (!node) return null;
				var childKey = setting.keyChildren,
				parentNode = (node.parentTId) ? node.getParentNode() : $.tree._getRoot(setting);
				for (var i=0, l = parentNode[childKey].length; i < l; i++) {
					if (parentNode[childKey][i] == node) return i;
				}
				return -1;
			},
			//获取 Tree 当前被选中的节点数据集合
			getSelectedNodes: function() {
				var r = [], list = $.tree._getRoot(setting).curSelectedList;
				for (var i=0, l=list.length; i<l; i++) {
					r.push(list[i]);
				}
				return r;
			},
			//是否选中节点
			isSelectedNode: function(node) {
				return $.tree._isSelectedNode(setting, node);
			},
			//强行异步加载父节点的子节点
			reAsyncChildNodes: function(parentNode, reloadType, isSilent, opts) {
				if (!this.setting.asyncEnable) return;
				var isRoot = !parentNode;
				if (isRoot) {
					parentNode = $.tree._getRoot(setting);
				}
				if (reloadType=="refresh") {
					var childKey = this.setting.keyChildren;
					for (var i = 0, l = parentNode[childKey] ? parentNode[childKey].length : 0; i < l; i++) {
						$.tree._removeNodeCache(setting, parentNode[childKey][i]);
					}
					$.tree._removeSelectedNode(setting);
					parentNode[childKey] = [];
					if (isRoot) {
						this.setting.treeObj.empty();
					} else {
						var ulObj = $$(parentNode, consts.id.UL, setting);
						ulObj.empty();
					}
				}
				$.tree._asyncNode(this.setting, isRoot? null:parentNode, !!isSilent, null, opts);
			},
			// 是reAsyncChildNodes的数组reload版
			reLoadChildNodes: function(parentNode, reloadType, isSilent, newNodes) {
				//if (this.setting.asyncEnable) return;
				var isRoot = !parentNode;
				if (isRoot) {
					parentNode = $.tree._getRoot(setting);
				}
				if (reloadType=="refresh") {
					var childKey = this.setting.keyChildren;
					for (var i = 0, l = parentNode[childKey] ? parentNode[childKey].length : 0; i < l; i++) {
						$.tree._removeNodeCache(setting, parentNode[childKey][i]);
					}
					$.tree._removeSelectedNode(setting);
					parentNode[childKey] = [];
					if (isRoot) {
						this.setting.treeObj.empty();
					} else {
						var ulObj = $$(parentNode, consts.id.UL, setting);
						ulObj.empty();
					}
				}
				if ( this.setting.isInit ){
					newNodes = $.tree._createRootNodes(this.setting,newNodes);
				}
				treeTools.addNodes(isRoot? null:parentNode, newNodes, !!isSilent);
			},
			//刷新 Tree 
			refresh: function() {
				this.setting.treeObj.empty();
				var root = $.tree._getRoot(setting),
				nodes = root[setting.keyChildren]
				$.tree._initRoot(setting);
				root[setting.keyChildren] = nodes
				$.tree._initCache(setting);
				$.tree._createNodes(setting, 0, root[setting.keyChildren]);
			},
			/**
			 * 重载 Tree，支持数组
			 * 
			 * url不能传进来，因为如果是异步树，点击节点的时候则会请求之前的url
			 */
			reload: function(opts) {
				this.setting.isInit = true;
				if(opts){// 有参数的时候
					opts = opts || {};
					var that = this,
						nodes = [], 
						isUrl = false;
					if ( typeof( opts ) !== "string" ) {
						// 传过来的是object，需要区别是data还是options
						// 如果是options，可能是options.data或者options.url ，否则才为data
						if ( opts.data ) { //传进来的是options对象
							nodes = opts.data;
						} else {
							if(opts instanceof Array){
								nodes = opts;
							} else {
								if ( opts.asyncUrl ) {
									this.setting.asyncUrl = opts.asyncUrl;
								}
								isUrl = true;
							}
						}
					} else {
						isUrl = true;
						this.setting.asyncUrl = opts;
					}
					if ( isUrl && this.setting.asyncEnable) {
						treeTools.reAsyncChildNodes( null, "refresh", null, opts );
					} else {
						treeTools.reLoadChildNodes( null, "refresh", null, nodes );
						$.tree._apply(opts.onLoad, [null, this.setting.treeId]);
						if ( opts.onLoad ) {
							$.tree._apply( opts.onLoad, [setting.treeId, null, nodes]);
						} else {
							setting.treeObj.trigger(consts.event.ASYNC_SUCCESS, [setting.treeId, null, nodes]);
						}
					}
				} else if ( this.setting.asyncEnable ){// 无参数的时候
					opts = opts || {};
					treeTools.reAsyncChildNodes( null, "refresh", null, opts );
				}
			},
			//清空某父节点的子节点
			removeChildNodes: function(node) {
				if (!node) return null;
				var childKey = setting.keyChildren,
				nodes = node[childKey];
				$.tree._removeChildNodes(setting, node);
				return nodes ? nodes : null;
			},	
			// 初始化搜索引擎，创建拼音搜索索引
			/**
			 * key 如： 
			 * dataArr 如： [{id:'1', name:'节点1'},{id:'2', name:'节点2'}]
			 */
			_pinyinEngine: function () {
				return new pinyinEngine();
			},
			_pinyinSetCache: function (engine, key, dataArr) {
				
				for (var i in dataArr) {
					// @param	{Array}	标签
					// @param	{Any}	被索引的内容
					engine.setCache ([dataArr[i][key]], dataArr[i]);
				}
				
				return engine;
			},
			_pinyinSearch: function (engine, keyword, callback) {
				var dataResult = [];
				
				engine.search (keyword, function (data) {
					dataResult.push (data);
				});
				
				callback (dataResult);
			},
			// lihaibo add  exhide.js
			/**
			 * keysObj ({id:testId, name:testName, ... })
			 */				
			filterNodesByParam:  function (keysObj) {
				var that = this,
					nodes = that.getNodes(),
					nodesArr = [],
					nodesFilter = [];
				
				that.expandAll (true);
				nodesArr = that.transformToArray (nodes);	
				var engine = that._pinyinEngine ();
					engine = that._pinyinSetCache (engine, "name", nodesArr);						
				that.hideNodes (nodesArr);
				
				if ( typeof keysObj === "object" ) {
					$.each( keysObj, function (k, v) {
						//nodesFilter = nodesFilter.concat( that.getNodesByParamFuzzy (k, v) );
						if (k == "name") {
							that._pinyinSearch(engine, v, function (dataFilter) { 
								that.showNodes (dataFilter, {showParents: true}, v);	
							});
						}  
					});
				}
			},
			showNodes: function(nodes, options, v) {
				$.tree._showNodes(setting, nodes, options, v);
			},
			showNode: function(node, options) {
				if (!node) {
					return;
				}
				$.tree._showNodes(setting, [node], options);
			},
			hideNodes: function(nodes, options) {
				$.tree._hideNodes(setting, nodes, options);
			},
			hideNode: function(node, options) {
				if (!node) {
					return;
				}
				$.tree._hideNodes(setting, [node], options);
			},
			// lihaibo add end
			//删除节点
			removeNode: function(node, callbackFlag) {
				if (!node) return;
				callbackFlag = !!callbackFlag;
				if (callbackFlag && $.tree._apply(setting.beforeRemove, [setting.treeId, node, setting], true) == false) return;
				$.tree._removeNode(setting, node);
				if (callbackFlag) {
					this.setting.treeObj.trigger(consts.event.REMOVE, [setting.treeId, node]);
				}
			},
			//选中指定节点
			selectNode: function(node, addFlag) {
				if (!node) return;
				if ($.tree._uCanDo(setting)) {
					addFlag = setting.selectedMulti && addFlag;
					if (node.parentTId) {
						$.tree._expandCollapseParentNode(setting, node.getParentNode(), true, false, function() {
							/*try{$$(node, setting).focus().blur();}catch(e){}*/
						});
					} else {
						/*try{$$(node, setting).focus().blur();}catch(e){}*/
					}
					$.tree._selectNode(setting, node, addFlag);
				}
			},
			//将简单 Array 格式数据转换为 Tree 使用的标准 JSON 嵌套数据格式
			transformToTreeNodes: function(simpleNodes) {
				return $.tree._transformToTreeFormat(setting, simpleNodes);
			},
			//将 Tree 使用的标准 JSON 嵌套格式的数据转换为简单 Array 格式
			transformToArray: function(nodes) {
				return $.tree._transformToArrayFormat(setting, nodes);
			},
			//更新某节点数据，主要用于该节点显示属性的更新
			updateNode : function(node, checkTypeFlag) {
				if (!node) return;
				var nObj = $$(node, setting);
				if (nObj.get(0) && $.tree._uCanDo(setting)) {
					$.tree._setNodeName(setting, node);
					$.tree._setNodeTarget(setting, node);
					$.tree._setNodeUrl(setting, node);
					$.tree._setNodeLineIcos(setting, node);
					$.tree._setNodeFontCss(setting, node);
				}
			}
		}
		root.treeTools = treeTools;
		$.tree._setTreeTools(setting, treeTools);
		if ( setting.disabled ){
			treeTools.disable();
		}else{
			treeTools.enable();
		}
		// // 20140108 lihaibo added
		setting.isInit = true;
		setting.dataLoaded = false;
		setting.rootReload = true;
		// // 20140108 lihaibo added
		if (root[childKey] && root[childKey].length > 0) {
			$.tree._createNodes(setting, 0, root[childKey]);
			setting.treeObj.trigger(consts.event.ASYNC_SUCCESS, [setting.treeId, null, root[childKey]]);
		} else if (setting.asyncEnable && setting.asyncUrl && setting.asyncUrl !== '') {
			$.tree._asyncNode(setting);	
		}
		return treeTools;
	}
};

var coralTree = $.coral.tree,
$$ = $.tree._$,
consts = coralTree.consts;


// override 多选树复写部分普通树的方法
var _createNodes = $.tree._createNodes;
$.tree._createNodes = function(setting, level, nodes, parentNode) {
	if (_createNodes) _createNodes.apply($.tree, arguments);
	if (!nodes) return;
	//checkTree
	//$.tree._repairParentChkClassWithSelf(setting, parentNode);
	//editTree
	if ($.tree._repairParentChkClassWithSelf) {
		$.tree._repairParentChkClassWithSelf(setting, parentNode);
	}
}
var _removeNode = $.tree._removeNode;
$.tree._removeNode = function(setting, node) {
	var parentNode = node.getParentNode();
	if (_removeNode) _removeNode.apply($.tree, arguments);
	if (!node || !parentNode) return;
	$.tree._repairChkClass(setting, parentNode);
	$.tree._repairParentChkClass(setting, parentNode);
}

var _appendNodes = $.tree._appendNodes;
$.tree._appendNodes = function(setting, level, nodes, parentNode, initFlag, openFlag) {
	var html = "";
	if (_appendNodes) {
		html = _appendNodes.apply($.tree, arguments);
	}
	if (parentNode) {
		$.tree._makeChkFlag(setting, parentNode);
	}
	return html;
}

//override 可编辑树复写部分普通树的方法
var _cancelPreSelectedNode = $.tree._cancelPreSelectedNode;
$.tree._cancelPreSelectedNode = function (setting, node) {
	var list = $.tree._getRoot(setting).curSelectedList;
	for (var i=0, j=list.length; i<j; i++) {
		if (!node || node === list[i]) {
			$.tree._removeTreeDom(setting, list[i]);
			if (node) break;
		}
	}
	if (_cancelPreSelectedNode) _cancelPreSelectedNode.apply($.tree, arguments);
}

var _makeNodeUrl = $.tree._makeNodeUrl;
$.tree._makeNodeUrl = function(setting, node) {
	return setting.editable ? null : (_makeNodeUrl.apply($.tree, arguments));
}

var _removeNode1 = $.tree._removeNode;
$.tree._removeNode = function(setting, node) {
	var root = $.tree._getRoot(setting);
	if (root.curEditNode === node) root.curEditNode = null;
	if (_removeNode1) {
		_removeNode1.apply($.tree, arguments);
	}
}

var _selectNode = $.tree._selectNode;
$.tree._selectNode = function(setting, node, addFlag) {
	var root = $.tree._getRoot(setting);
	if ($.tree._isSelectedNode(setting, node) && root.curEditNode == node && node.editNameFlag) {
		return false;
	}
	if (_selectNode) _selectNode.apply($.tree, arguments);
	$.tree._addHoverDom(setting, node);
	return true;
}

var _uCanDo = $.tree._uCanDo;
$.tree._uCanDo = function(setting, e) {
	var root = $.tree._getRoot(setting);
	if (e && ($.tree._eqs(e.type, "mouseover") || $.tree._eqs(e.type, "mouseout") || $.tree._eqs(e.type, "mousedown") || $.tree._eqs(e.type, "mouseup"))) {
		return true;
	}
	if (root.curEditNode) {
		$.tree._editNodeBlur = false;
		root.curEditInput.focus();
	}
	return (!root.curEditNode) && (_uCanDo ? _uCanDo.apply($.tree, arguments) : true);
}
/**
 * isMethodCall
 * arg[0]: method
 * arg[1]: options
 * arg[2]: nodes
 * 
 * isInit
 * 
 * arg[0]: options
 * arg[1]: nodes
 * 
 */ 
function coraltree(options, element){
	return element.tree(options);
}
$.fn.tree = function(options){
	//返回值
	var returnValue = this;
	if ( !this.length ) {
		return this;
	}
	//获取其他参数
	var otherArgs = Array.prototype.slice.call(arguments, 1);
	var args = Array.prototype.slice.call(arguments, 0);
	this.each(function() {
		var isMethodCall = typeof options === "string";
		options = options||{};
		//调用方法返回值
		var methodValue,
			parseOptions = $.parser.parseOptions( this, [], ['data','fontCss','onRightClick', 'rootNode', 'chkboxType','formatter','showIcon'] );
		
		_options = $.extend( {}, $.fn['tree'].defaults || {}, parseOptions );
		//调用方法
		if ( isMethodCall ) {

			var treeobj = $.coral.tree.getTreeObj($(this).attr("id"));
			methodValue = treeobj[options].
				apply(treeobj, [].concat(otherArgs));
			if(methodValue !== undefined){
				returnValue = methodValue;
			}
			return ;
		} else {
			//option为对象，并且参数个数为两个时，初始化树(不通过tag标签，直接用js初始化时)
			_options = $.extend( {}, _options, options );
			var node = args[1] || _options.data;
			$.coral.tree.init( $(this), _options , node );
		}
	});
	return returnValue;
};
$.uiplugins["coralui-tree"] = coraltree;
	