var iframeContextPath = "../../../../workflow-version";

var t2_setting = {
	check : {
		enable : true,
		chkStyle : "radio",
		//chkboxType: { "Y": "p", "N": "s" }
		radioType : "all"
	},
	view : {
		dblClickExpand : false,
		showLine : true,
		selectedMulti : true
	},
	data : {
		simpleData : {
			enable : true,
			idKey : "id",
			pIdKey : "pId",
			rootPId : ""
		}
	},
	callback : {
		/*beforeClick : function(treeId, treeNode) {
			var zTree = $.fn.zTree.getZTreeObj("tree");
			if (treeNode.isParent) {
				zTree.expandNode(treeNode);
				return false;
			} else {
				return true;
			}
		}*/
	}
};

//获取已经被选中的节点的名称
var typeList = parent.document.getElementById("assigneeHidden").value;
var checkType;
if (typeList != "") {
	if (typeList.indexOf("表达式") == 0) {
		//表达式
		checkType = "processSponsor";
	} else if (typeList.indexOf("上一节点处理人") == 0) {
		//上一节点处理人
		checkType = "dealPeople";
	} else {
		checkType = "user";
	}
}
var t3_setting = {

	check : {
		enable : false,
		chkStyle : "radio",
		//chkboxType: { "Y": "p", "N": "s" }
		radioType : "all"
	},
	view : {
		dblClickExpand : false,
		showLine : true,
		selectedMulti : true
	},
	edit : {
		enable : true,
		showRemoveBtn : true,
		showRenameBtn : false
	},
	data : {
		simpleData : {
			enable : true,
			idKey : "id",
			pIdKey : "pId",
			rootPId : ""
		}
	}
};

if (typeList != '') {
	var typeList2 = typeList.split(',');
	var totalSum = [];
	var selectedNodes = [];
	var typeList3;
	for (var i = 0; i < typeList2.length; i++) {
		typeList3 = typeList2[i].split(':');
		selectedNodes.push({
			name : typeList3[0],
			id : typeList3[1],
			loginName : typeList3[1]
		});
	}
	var t3 = $("#tree3");
	if (selectedNodes[0].name == '流程发起人' || selectedNodes[0].name == '上一节点处理人'
		|| selectedNodes[0].name.split(":")[0] == '表达式') {
		t3 = $.fn.zTree.init(t3, t3_setting, null);
	} else {
		t3 = $.fn.zTree.init(t3, t3_setting, selectedNodes);
	}
}

//获取radio的类型
var radioType = $('input:radio:checked').val();


//默认选中
function checkSelectedValue(type, data) {
	data[0].chkDisabled = true;
	if (!((checkType == "role" || checkType == "user") && (type == "role" || type == "user"))) {
		return ;
	}
	var treeObj = $.fn.zTree.getZTreeObj("tree3");
    var nodes = treeObj.getNodes();
    if (!nodes.length) {
    	return ;
    }
    for (var i = 0; i < data.length; i++) {
    	for (var j = 0; j < nodes.length; j++) {
    		if (nodes[j].id == data[i].id) {
    			data[i].checked = true;
    			continue;
    		}
    	}
    }
}
//
function defaultSelectedTree(type) {
	// 用户和角色选择人都是一样
	if ((checkType == "role" || checkType == "user") && (type == "role" || type == "user")) {
		$.fn.zTree.init($("#tree3"), t3_setting, selectedNodes);
	} else {
		$.fn.zTree.init($("#tree3"), t3_setting, null);
	}
}

// 渲染中间的树
function renderMiddleTree(type, data) {
	checkSelectedValue(type, data);

    $.fn.zTree.init($("#tree2"), t2_setting, data);
}

function closed() {
	var iframe = parent.document.getElementById("iframeTest");
	iframe.parentNode.removeChild(iframe);
}

function save() {
	var radioType = $('input:radio:checked').val();
	var nodeList = $.fn.zTree.getZTreeObj("tree3");
	if (radioType == 'expression') {
		var expre = document.getElementById("expressionId").value;
		if (expre) {
			parent.document.getElementById("assigneeField").value = "表达式:"
					+ expre;
		}
	} else {
		if (nodeList == null) {
			alert("保存的节点不能为空");
		} else {
			var nodes = $.fn.zTree.getZTreeObj("tree3").getNodes(true);
			//console.log(nodes);
			//var str=radioType+':';
			var str = '';
			var str2 = '';
			for (var i = 0; i < nodes.length; i++) {
				if (i == nodes.length - 1) {
					str += nodes[i].name + ":" + nodes[i].id;
					str2 += nodes[i].name;
				} else {
					str += nodes[i].name + ":" + nodes[i].id + ',';
					str2 += nodes[i].name + ",";
				}

			}
			if (radioType == 'processSponsor') {
				parent.document.getElementById("assigneeField").value = '流程发起人';
				parent.document.getElementById("assigneeHidden").value = '表达式:${initiator}';
			} else if (radioType == 'dealPeople') {
				parent.document.getElementById("assigneeField").value = '上一节点处理人';
				parent.document.getElementById("assigneeHidden").value = '上一节点处理人';
			} else {
				if (str != '') {
					parent.document.getElementById("assigneeField").value = str2;
					parent.document.getElementById("assigneeHidden").value = str;
				} else {
					parent.document.getElementById("assigneeField").value = "";
					parent.document.getElementById("assigneeHidden").value = "";
				}

			}
		}
	}

	var iframe = parent.document.getElementById("iframeTest");
	iframe.parentNode.removeChild(iframe);

}

function moveCheck() {
	var t3 = $('#tree3');
	var treeObj = $.fn.zTree.getZTreeObj("tree2");
	var nodes = treeObj.getCheckedNodes(true);
	t3 = $.fn.zTree.init(t3, t3_setting, nodes);

}

function moveAll() {
}

//单选事件改变时触发事件 刷新当前页面的tree
$("[name=user]").change(fun_change);
function fun_change() {
	//获取已经选中的用户的名字
	var typeList = parent.document.getElementById("assigneeHidden").value;
	//获取radio的类型
	var radioType = $('input:radio:checked').val();

	defaultSelectedTree(radioType);

	//获取当前的路径
	if (radioType == "processSponsor" || radioType == "dealPeople") {
		//$('.modal-body').append('<div class="readonly"></div>');
		$('.modal-body').addClass('onlyrow1');
		$('#expressionBox').html('');
		var t2_setting = {
			check : {
				enable : true,
				chkStyle : "checkbox",
				chkboxType : {
					"Y" : "p",
					"N" : "s"
				}
			},
			view : {
				dblClickExpand : false,
				showLine : true,
				selectedMulti : true
			},
			data : {
				simpleData : {
					enable : true,
					idKey : "id",
					pIdKey : "pId",
					rootPId : ""
				}
			},
			callback : {
				beforeClick : function(treeId, treeNode) {
					var zTree = $.fn.zTree.getZTreeObj("tree");
					if (treeNode.isParent) {
						zTree.expandNode(treeNode);
						return false;
					} else {
						return true;
					}
				},
				onClick : function(event, treeId, treeNode) {
					var zTree = $.fn.zTree.getZTreeObj("tree3");
					zTree.checkNode(treeNode, !treeNode.checked, true);
				}
			}
		};

		var hiddenValue = parent.document.getElementById("assigneeHidden").value;
		var checkFlag = false;

		var t3 = $("#tree3");
		if (radioType == "processSponsor") {
			if (hiddenValue && hiddenValue.indexOf("表达式:${initiator}") == 0) {
				checkFlag = true;
			}
			var data = [ {
				"id" : "8b797d01ad254557bb201e20e7aec57a",
				"name" : "流程发起人",
				"checked" : checkFlag
			} ];
		} else {
			if (hiddenValue && hiddenValue == "上一节点处理人") {
				checkFlag = true;
			}
			var data = [ {
				"id" : "8b797d01ad254557bb201e20e7aec57b",
				"name" : "上一节点处理人",
				"checked" : checkFlag
			} ];
		}
		t3 = $.fn.zTree.init(t3, t2_setting, data);

	} else if (radioType == "expression") {
		$('.modal-body').addClass('onlyrow1');
		var t3 = $('#tree3');
		t3 = $.fn.zTree.init(t3, t2_setting, null);
		var html = '<input type="text" id="expressionId">'
				+ '<div class="expressContnt clearfix">'
				+ '<p><span class="iconfont icon-bangzhux">？</span><span class="expresstext">这里可以使用uel表达式，UEL是java EE6规范的一部分，UEL（Unified Expression Language）即统一表达式语言，activiti支持两个UEL表达式：UEL-value和UEL-method。<br/>1.使用uel-value,设置处理人表达式${assignee},然后在启动时动态在流程变量中设置assignee的值，processEngine.getRuntimeService().setVariable(executionId, "${assignee}",处理人);同时此处可以使用对象，${user.userId}，然后在流程变量中放入user对象即可。<br/>2.使用uel-method，设置节点的执行人为${method.getLoginNameByUserId(userId)}，其中method方法是我们注入到spring中的一个类，userId是我 们设置的全局变量，将method方法注入到activiti的SpringProcessEngineConfiguration的bean中，最后启动一个流程设置全局变量userId作为启动参数。</span></p>'
				+ '</div>';
		var v = parent.document.getElementById("userField").value;
		if (v.startsWith("表达式:")) {
			v = v.substring(4)
			html = '<input type="text" id="expressionId" value="'
					+ v
					+ '">'
					+ '<div class="expressContnt clearfix">'
					+ '<p><span class="iconfont icon-bangzhux">？</span><span class="expresstext">这里可以使用uel表达式，UEL是java EE6规范的一部分，UEL（Unified Expression Language）即统一表达式语言，activiti支持两个UEL表达式：UEL-value和UEL-method。<br/>1.使用uel-value,设置处理人表达式${assignee},然后在启动时动态在流程变量中设置assignee的值，processEngine.getRuntimeService().setVariable(executionId, "${assignee}",处理人);同时此处可以使用对象，${user.userId}，然后在流程变量中放入user对象即可。<br/>2.使用uel-method，设置节点的执行人为${method.getLoginNameByUserId(userId)}，其中method方法是我们注入到spring中的一个类，userId是我 们设置的全局变量，将method方法注入到activiti的SpringProcessEngineConfiguration的bean中，最后启动一个流程设置全局变量userId作为启动参数。</span></p>'
					+ '</div>';
		}
		$('#expressionBox').html(html);
	} else {
		//$('.readonly').remove();
		$('.modal-body').removeClass('onlyrow1');
		$('#expressionBox').html('');
		var url = iframeContextPath;
		if ("user" == radioType || "depart" == radioType) {
			url += "/assignment/dept-root?access_token=" + window.parent.token;
		} else {
			url += "/assignment/role-root?access_token=" + window.parent.token;
		}

		//根据当前的radio类型去判断应当显示哪个树
		$.ajax({
			url : url,
			dataType : "json",
			type : "GET",
			success : function(data) {
				//定义切换后的url 如果是用户和角色使用不同的异步展开url
				var asyncUrl = '';
				if (radioType !== "role") {
					asyncUrl = iframeContextPath + "/assignment/dept-node?access_token=" + window.parent.token;
				} /*else if (radioType == "role") {
					asyncUrl = iframeContextPath + "/queryRoleInfosByRoleClassId.json?iconType=1";
				}*/
				var setting = {
					async : {
            enable : true,
						type : "get",
						url : asyncUrl,
						autoParam : [ "id" ]
					},
					view : {
						dblClickExpand : false,
						showLine : true,
						selectedMulti : true
					},
					data : {
						simpleData : {
							enable : true,
							idKey : "id",
							pIdKey : "pId",
							rootPId : ""
						}
					},
					callback : {
						onClick : zTreeOnClick
					}
				};

				//定义当前的左边树的单击事件
				function zTreeOnClick(e, treeId, treeNode) {
					var treeNodeId = treeNode.id;
					var usersUrl = iframeContextPath + "/assignment/users";
					if ("role" == radioType) {
						usersUrl += "/role/" + treeNodeId;
					} else {
						usersUrl += "/dept/" + treeNodeId;
					}
					usersUrl += "?access_token=" + window.parent.token;
					$.ajax({
						url : usersUrl,
						dataType : "json",
						type : "GET",
						success : function(data) {
							renderMiddleTree(radioType, data);
						}
					});
				};

				$(function() {
					debugger
					var t = $("#tree");
					$.fn.zTree.init(t, setting, data.data);
					var t2 = $("#tree2");
					$.fn.zTree.init(t2, null, "");

				});
			}
		});
	}

}
//全部移除
function deleteAll() {
	var t3 = $('#tree3');
	if (!(t3[0].innerText.indexOf("流程发起人") != -1)) {
		t3 = $.fn.zTree.init(t3, t3_setting, null);
	} else {
		var zTree = $.fn.zTree.getZTreeObj("tree3");
		zTree.checkAllNodes(false);
	}
}

//默认选中
function initRadioType() {

	var hiddenValue = parent.document.getElementById("assigneeHidden").value;
	debugger
	if (hiddenValue) {
		if (hiddenValue.indexOf("(角色)") == 0) {
			$("input:radio[value='role']").attr('checked', 'true');

		} else if (hiddenValue.indexOf("(部门)") == 0) {
			$("input:radio[value='depart']").attr('checked', 'true');

		} else if (hiddenValue.indexOf("表达式:${initiator}") == 0) {
			//流程发起人
			$("input:radio[value='processSponsor']").attr('checked', 'true');

		} else if (hiddenValue.indexOf("表达式") == 0) {
			//表达式
			$("input:radio[value='expression']").attr('checked', 'true');
		} else if (hiddenValue.indexOf("上一节点处理人") == 0) {
			//上一节点处理人
			$("input:radio[value='dealPeople']").attr('checked', 'true');

		}
	}
	fun_change();

}
$(function() {
	initRadioType();
});
