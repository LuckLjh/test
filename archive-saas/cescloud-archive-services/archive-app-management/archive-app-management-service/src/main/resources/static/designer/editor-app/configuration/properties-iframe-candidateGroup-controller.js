var iframeContextPath = "../../../../workflow-version";
//获取已经被选中的节点的名称
var typeList = parent.document.getElementById("candidateGroupsHidden").value;
var typeMap = {
		"depart" : "部门",
		"role" : "角色",
		"expression" : "表达式"
};

//获取radio的类型
var radioType = $('input:radio:checked').val();
if (radioType == "expression") {
    typeList = "";
}
var valueType;
if (typeList != "") {
    valueType = typeList.substring(1, typeList.indexOf(")"));
}

var t3 = $("#tree3");
var data = [];
var t3_setting = {
    check: {
        enable: false,
        chkStyle: "checkbox",
        chkboxType: {"Y": "p", "N": "s"}
    },
    view: {
        dblClickExpand: false,
        showLine: true,
        selectedMulti: true
    },
    edit: {
        enable: true,
        showRemoveBtn: true,
        showRenameBtn: false
    },
    data: {
        simpleData: {
            enable: true,
            idKey: "id",
            pIdKey: "pId",
            rootPId: ""
        }
    }
};


if (typeList != '') {
	if (typeList.indexOf("表达式:${customRuleService") > -1) {
		$.fn.zTree.init($("#tree3"), t3_setting, null);
	} else {
		typeList = typeList.replace(/\(角色\)|\(部门\)/g, "");
		var typeList2 = typeList.split(',');
		var typeList3;
		var totalSum = [];
		var selectedNodes = [];
		for (var i = 0; i < typeList2.length; i++) {
			typeList3 = typeList2[i].split(':');
			selectedNodes.push({name: typeList3[0], id: typeList3[1], pId: 0});
		}
		/*if (selectedNodes[0].name.split(":")[0] == '表达式') {
			$.fn.zTree.init($("#tree3"), t3_setting, null);
		} else {
			$.fn.zTree.init($("#tree3"), t3_setting, selectedNodes);
		}*/
		$.fn.zTree.init($("#tree3"), t3_setting, selectedNodes);
	}
}


//获取当前的路径,并进行截取
//根据当前的radio类型去判断应当显示哪个树
$.ajax({
    url: iframeContextPath + "/assignment/dept-root?access_token=" + window.parent.token,
    dataType: "json",
    type: "GET",
    success: function (data) {
        var zTree;
        var setting = {
            async: {
                enable: true,
				type : "get",
				url : iframeContextPath + "/assignment/dept-node?access_token=" + window.parent.token,
                autoParam: ["id"]
            },
            view: {
                dblClickExpand: true,
                showLine: true,
                selectedMulti: true
            },
            data: {
                simpleData: {
                    enable: true,
                    idKey: "id",
                    pIdKey: "pId",
                    rootPId: ""
                }
            },
            callback: {
                beforeClick: function (treeId, treeNode) {
                    var zTree = $.fn.zTree.getZTreeObj("tree");
                    if (treeNode.isParent) {
                        zTree.expandNode(treeNode);
                        return false;
                    } else {
                        return true;
                    }
                },
                beforeClick: zTreeOnClick
            }
        };
        $(function () {
            var t = $("#tree");
            t = $.fn.zTree.init(t, setting, data.data);
        });


        //定义当前的tree树的单击事件，然后根据返回值去刷新tree1的值
        function zTreeOnClick(treeId, treeNode) {
            var treeNodeId = treeNode.id;
            $.ajax({
                url: iframeContextPath + "/assignment/dept-node?access_token=" + window.parent.token,
                data: {"id": treeNodeId},
                dataType: "json",
                type: "GET",
                success: function (data) {
                    var t2_setting = {
                        check: {
                            enable: true,
                            chkStyle: "checkbox",
                            chkboxType: {"Y": "s", "N": "s"}
                        },
                        view: {
                            dblClickExpand: false,
                            showLine: true,
                            selectedMulti: true
                        },
                        data: {
                            simpleData: {
                                enable: true,
                                idKey: "id",
                                pIdKey: "pId",
                                rootPId: ""
                            }
                        },
                        callback: {
                            beforeClick: function (treeId, treeNode) {
                                var zTree = $.fn.zTree.getZTreeObj("tree");
                                if (treeNode.isParent) {
                                    zTree.expandNode(treeNode);
                                    return false;
                                } else {
                                    return true;
                                }
                            },
                            onClick: function(event, treeId, treeNode){
                                var zTree = $.fn.zTree.getZTreeObj("tree2");
                                zTree.checkNode(treeNode, !treeNode.checked, true); 
                            }
                        }
                    };

                    var t2 = $("#tree2");
                    var treeObj = $.fn.zTree.getZTreeObj("tree3");
                    if (treeObj != null) {
                        var nodes = treeObj.getNodes();
                        if (nodes.length > 0) {
                            for (var i = 0; i < data.length; i++) {
                                for (var j = 0; j < nodes.length; j++) {
                                    if (nodes[j].id == data[i].id) {
                                        data[i].checked = true;
                                        continue;
                                    }
                                }
                            }
                        }
                    }


                    t2 = $.fn.zTree.init(t2, t2_setting, data);

                    function zTreeOnClick2(event, treeId, treeNode) {

                    }
                }
            });
        };


    }
});


function closed() {
    var iframe = parent.document.getElementById("iframeTest");
    iframe.parentNode.removeChild(iframe);
}

function save() {
    var radioType = $('input:radio:checked').val();
    var nodeList = $.fn.zTree.getZTreeObj("tree3");
    var closeFlag = false;
    if (radioType == 'expression') {
        closeFlag = customRule.save(function (value, showValue) {
            //parent.document.getElementById("groupField").value = "表达式:" + value;
            //数据包括名称
            parent.document.getElementById("groupField").value =  showValue;
            //数据包括名称和id
            parent.document.getElementById("candidateGroupsHidden").value = "表达式:" + value;
        });
        // var expre = document.getElementById("expressionId").value;
        // if (expre) {
        //     parent.document.getElementById("groupField").value = "表达式:" + expre;
        // }
    } else {
        if (nodeList == null) {
            alert("保存的节点不能为空");
        } else {
            var nodes = $.fn.zTree.getZTreeObj("tree3").getNodes(true);

            //var str=radioType+':';
            var preStr = "";
            var str = '';
            var str2 = '';
            if (nodes.length > 0) {
                if (radioType) {
                    if (radioType == "depart") {
                    	preStr = "(部门)";
                    } else if (radioType == "role") {
                    	preStr = "(角色)";
                    }
                }
            }
            var hideValueArr = [];
            var showValueArr = [];
            for (var i = 0; i < nodes.length; i++) {
            	hideValueArr.push(preStr + nodes[i].name + ":" + nodes[i].id);
            	showValueArr.push(nodes[i].name);
            }
            //数据包括名称
            parent.document.getElementById("groupField").value = preStr + showValueArr.join(",");
            //数据包括名称和id
            parent.document.getElementById("candidateGroupsHidden").value = hideValueArr.join(",");
        }
    }
    if(!closeFlag){
        var iframe = parent.document.getElementById("iframeTest");
        iframe.parentNode.removeChild(iframe);

    }

}

function moveCheck() {
    var treeObj = $.fn.zTree.getZTreeObj("tree2");
    var nodes = treeObj.getCheckedNodes(true);
    var treeObj2 = $.fn.zTree.getZTreeObj("tree3");
    if (treeObj2 != null) {
        var tree3Node = treeObj2.getNodes();

        for (var i = 0; i < nodes.length; i++) {
            if (!nodes[i].isParent) {
                addObject(tree3Node, {id: nodes[i].id, name: nodes[i].name, pId: 0 });
            }

        }
    }

    var t3 = $('#tree3');
    t3 = $.fn.zTree.init(t3, t3_setting, tree3Node);
}

function addObject(ls, basemodel) {
    var flag = false;
    for (var i = 0; i < ls.length; i++) {
        if (ls[i].id) {
            if (ls[i].id == basemodel.id) {
                flag = true;
                break;
            }
        } else {
            if (basemodel.id === ls[i].id) {
                flag = true;
                break;
            }
        }
    }
    if (flag === false) {
        ls.push(basemodel);
    }
    return ls;
}

function moveAll() {
}

//单选事件改变时触发事件 刷新当前页面的tree
$("[name=user]").change(fun_change);
function fun_change() {
    //获取radio的类型

    var radioType = $('input:radio:checked').val();
    
    defaultSelectedTree(radioType);

    if (radioType == "expression") {
    	$('.modal-body').removeClass('middle-right');
        $('.modal-body').addClass('onlyrow1');
        var v = parent.document.getElementById("groupField").value;
        $('#expressionBox').css({
            display: "block"
        });
        if( $("#gridExpression").hasClass("ctrl-init-grid")){
            $("#gridExpression").grid("destroy");
        }
        //自定义规则html
        var html = '<div id="gridExpressionToolbar"></div> <div id="gridExpression"></div>';
        $('#expressionBox').html(html);
        customRule.fun_initTable("gridExpression","candidateGroupsHidden",function () {
            customRule.fun_initToolBar("gridExpressionToolbar");
            //回调函数，处理显示内容
            if (customRule.tableData.length > 0) {
                //处理选择角色的回选
                $.each(customRule.tableData, function (index, rowData) {
                    var selectUser = rowData["selectUser"];
                    var selectUsers = selectUser.split(",")
                    var text = "";
                    $.each(selectUsers, function (i, role) {
                        text += "," + role.split(":")[0];
                    });
                    customRule.fun_selectRoleCallback(index + 1, selectUser, text.substr(1, text.length));
                });
                //处理选择对象类型的指定用户
                $.each(customRule.tableData, function (index, rowData) {
                    var $grid = customRule.$grid;
                    var rowId = index + 1;
                    var userType = rowData["userType"];
                    if (userType.indexOf(customRule.SPLIT_USER) != -1) {
                        //选中组件
                        var $com = $grid.grid("getCellComponent", rowId, "userType");
                        $com.combobox("setValue", "assign");
                        //显示内容
                        var userTypes = userType.split(customRule.SPLIT_USER);
                        var text = userTypes[0];
                        var $userType = $("tr[id='" + (index + 1) + "'] [name='userType']");
                        customRule.fun_selectUserCallback(index + 1, userType, text);
                    }
                    if (userType.indexOf(customRule.SPLIT_DEPT) != -1) {
                        //选中组件
                        var $com = $grid.grid("getCellComponent", rowId, "userType");
                        $com.combobox("setValue", "selectOrg");
                        //显示内容
                        var userTypes = userType.split(customRule.SPLIT_DEPT);
                        var text = userTypes[0];
                        var $userType = $("tr[id='" + (index + 1) + "'] [name='userType']");
                        customRule.fun_selectOrgCallback(index + 1, userType, text);
                    }
                    if (userType.indexOf(customRule.SPLIT_NODE) != -1) {
                        //选中组件
                         var $com = $grid.grid("getCellComponent", rowId, "userType");
                         $com.combobox("setValue", "node");
                         //显示内容
                         var userTypes = userType.split(customRule.SPLIT_NODE);
                         var text = userTypes[0];
                         var $userType = $("tr[id='" + (index + 1) + "'] [name='userType']");
                         customRule.fun_selectNodeCallback(index + 1, userType, text);
                     }
                });

            }
        });
        //$('#expressionBox').html("123");
        // var html = '<input type="text" id="expressionId">'
        //     + '<div class="expressContnt clearfix">'
        //     + '<p><span class="iconfont icon-bangzhux">？</span><span class="expresstext">这里可以使用uel表达式，UEL是java EE6规范的一部分，UEL（Unified Expression Language）即统一表达式语言，activiti支持两个UEL表达式：UEL-value和UEL-method。<br/>1.使用uel-value,设置处理人表达式${assignee},然后在启动时动态在流程变量中设置assignee的值，processEngine.getRuntimeService().setVariable(executionId, "${assignee}",处理人);同时此处可以使用对象，${user.userId}，然后在流程变量中放入user对象即可。<br/>2.使用uel-method，设置节点的执行人为${method.getLoginNameByUserId(userId)}，其中method方法是我们注入到spring中的一个类，userId是我 们设置的全局变量，将method方法注入到activiti的SpringProcessEngineConfiguration的bean中，最后启动一个流程设置全局变量userId作为启动参数。</span></p>'
        //     + '</div>';
        // var v = parent.document.getElementById("groupField").value;
        // if (v.startsWith("表达式:")) {
        //     v = v.substring(4)
        //     html = '<input type="text" id="expressionId" value="' + v + '">'
        //         + '<div class="expressContnt clearfix">'
        //         + '<p><span class="iconfont icon-bangzhux">？</span><span class="expresstext">这里可以使用uel表达式，UEL是java EE6规范的一部分，UEL（Unified Expression Language）即统一表达式语言，activiti支持两个UEL表达式：UEL-value和UEL-method。<br/>1.使用uel-value,设置处理人表达式${assignee},然后在启动时动态在流程变量中设置assignee的值，processEngine.getRuntimeService().setVariable(executionId, "${assignee}",处理人);同时此处可以使用对象，${user.userId}，然后在流程变量中放入user对象即可。<br/>2.使用uel-method，设置节点的执行人为${method.getLoginNameByUserId(userId)}，其中method方法是我们注入到spring中的一个类，userId是我 们设置的全局变量，将method方法注入到activiti的SpringProcessEngineConfiguration的bean中，最后启动一个流程设置全局变量userId作为启动参数。</span></p>'
        //         + '</div>';
        // }
        // $('#expressionBox').html(html);
    } else if ("depart" == radioType) {
    	$('.modal-body').removeClass('onlyrow1');
    	$('.modal-body').removeClass('middle-right');

		var url = iframeContextPath + "/assignment/dept-root?access_token=" + window.parent.token;
        // $('#expressionBox').html('');
        $('#expressionBox').css({
            display: "none"
        });

        //根据当前的radio类型去判断应当显示哪个树
        $.ajax({
            url: url,
            dataType: "json",
            type: "GET",
            success: function (data) {
                var zTree;
                var asyncUrl = iframeContextPath + "/assignment/dept-node?access_token=" + window.parent.token;
                var setting = {
                    async: {
                        enable: true,
                        type: "get",
                        url: asyncUrl,
                        autoParam: ["id"]
                    },
                    view: {
                        dblClickExpand: false,
                        showLine: true,
                        selectedMulti: true
                    },
                    data: {
                        simpleData: {
                            enable: true,
                            idKey: "id",
                            pIdKey: "pId",
                            rootPId: ""
                        }
                    },
                    callback: {
                        beforeClick: function (treeId, treeNode) {
                            var zTree = $.fn.zTree.getZTreeObj("tree");
                            if (treeNode.isParent) {
                                zTree.expandNode(treeNode);
                                return false;
                            } else {
                                return true;
                            }
                        },
                        beforeClick: zTreeOnClick
                    }
                };
                $(function () {
                    var t = $("#tree");
                    t = $.fn.zTree.init(t, setting, data.data);
                    var t = $("#tree2");
                    t = $.fn.zTree.init(t, setting, null);
                });

                //定义当前的左边树的单击事件
                function zTreeOnClick(treeId, treeNode) {
                    var treeNodeId = treeNode.id;
                    //分成角色和部门两类  角色下 只有角色分类点击了 发送请求
                    $.ajax({
                        url: iframeContextPath + "/assignment/dept-node?access_token=" + window.parent.token,
                        data: { "id": treeNodeId },
                        dataType: "json",
                        type: "GET",
                        success: function (data) {
                            var rootData = [{id: treeNodeId, pId: null, name: "部门名称", isParent: true, open: true}];
                            for (var i = 0, len = data.length; i < len; i++) {
                            	data[i].isParent = false;
                            	rootData.push(data[i]);
                            }
                            renderMiddleTree(radioType, rootData);
                        }
                    });

                };
            }
        });

    } else if ("role" == radioType) {
    	$('.modal-body').removeClass('onlyrow1');
    	$('.modal-body').addClass('middle-right');
    	
    	var url = iframeContextPath + "/assignment/role-root?access_token=" + window.parent.token;
        // $('#expressionBox').html('');
        $('#expressionBox').css({
            display: "none"
        });
        
        //根据当前的radio类型去判断应当显示哪个树
        $.ajax({
            url: url,
            dataType: "json",
            type: "GET",
            success: function (data) {
            	renderMiddleTree(radioType, data.data);
            }
        });
    }
}
// 默认选中
function checkSelectedValue(type, data) {
	if (typeMap[type] != valueType) {
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
	if (typeMap[type] != valueType || type == "expression") {
		$.fn.zTree.init($("#tree3"), t3_setting, null);
	} else {
		$.fn.zTree.init($("#tree3"), t3_setting, selectedNodes);
	}
}

// 渲染中间的树
function renderMiddleTree(type, data) {
	var setting = {
        check: {
            enable: true,
            chkStyle: "checkbox",
            chkboxType: {"Y": "s", "N": "s"}
        },
        view: {
            dblClickExpand: false,
            showLine: true,
            selectedMulti: true
        },
        data: {
            simpleData: {
                enable: true,
                idKey: "id",
                pIdKey: "pId",
                rootPId: ""
            }
        },
        callback: {
            /*onClick: function(event, treeId, treeNode) {
                var zTree = $.fn.zTree.getZTreeObj("tree2");
                zTree.checkNode(treeNode, !treeNode.checked, true); 
            }*/
        }
    };
	checkSelectedValue(type, data);

    $.fn.zTree.init($("#tree2"), setting, data);
}

//默认选中
function initRadioType() {
    var hiddenValue = parent.document.getElementById("candidateGroupsHidden").value;
    if (hiddenValue) {
        if (hiddenValue.indexOf("(角色)") == 0) {
            $("input:radio[value='role']").attr('checked', 'true');

        } else if (hiddenValue.indexOf("(部门)") == 0) {
            $("input:radio[value='depart']").attr('checked', 'true');

        } else if (hiddenValue.indexOf("表达式") == 0) {
            //表达式
            $("input:radio[value='expression']").attr('checked', 'true');
        }
    }
    fun_change();
}


//全部移除
function deleteAll() {
	 var a = $($("tbody")[1]).find("tr:gt(0)").remove();
     if(a.context == undefined){
        var t3 = $('#tree3');
        if(!((t3[0].innerText.indexOf("流程发起人") != -1) ||(t3[0].innerText.indexOf("上一节点处理人") != -1) )){
            t3 = $.fn.zTree.init(t3, t3_setting, null);
        } else {
            var zTree = $.fn.zTree.getZTreeObj("tree3");
            zTree.checkAllNodes(false);
        }
     }
}

// /**
//  * --------------------
//  *  自定义规则
//  *
//  *  @Author wxl
//  *
//  * --------------------
//  */
// //自定义规则对象
// var customRule = customRule || {};
// //表格工具条属性对象
// customRule.tableToolBarData = [
//     {
//         "id": "add",
//         "label": "新增",
//         "disabled": "false",
//         "onClick": "customRule.fun_addTableRow()",
//         "type": "button",
//         "cls": "greenbtn",
//         "icon": "icon-plus3"
//     },
//     {
//         "id": "delete",
//         "label": "删除",
//         "disabled": "false",
//         "onClick": "customRule.fun_deleteTableRow()()",
//         "type": "button",
//         "cls": "deleteBtn",
//         "icon": "icon-bin"
//     }
// ];
// //用户合并最终表格数据的对象
// var initTableData = {}
// //初始化表格对象
// var tableData = [];
// //初始化数据
// customRule.initTableData = function () {
//     var tableDataStr = parent.document.getElementById("candidateGroupsHidden").value;
//     if (tableDataStr && tableDataStr.indexOf("表达式") != -1) {
//         tableDataStr = tableDataStr.replace("表达式:", "");
//         var re = /(\$|,\$)/g;
//         tableDataStr = tableDataStr.replace(re, "-");
//         var re2 = /(-{customRuleService.[\w]{1,}\([\w|']+,)/g;
//         tableDataStr = tableDataStr.replace(re2, "-");
//         var myDatas = tableDataStr.split("-");
//         var myDataJson = [];
//         $.each(myDatas, function (index, myData) {
//             if (myData) {
//                 myData = myData.replace(")}", "");
//                 myData = myData.replace(/'/g, "");
//                 myData = JSON.parse(myData);
//                 myDataJson.push(myData);
//             }
//         });
//         $.each(myDataJson, function (index, myJson) {
//             initTableData[index + 1] = myJson;
//             tableData.push(myJson)
//         })
//     }
//
// };
// //初始化表格
// customRule.initTable = function (callback) {
//     tableData=[];
//     initTableData={};
//     customRule.initTableData();
//     $("#gridExpressionToolbar").toolbar({
//         data: customRule.tableToolBarData
//     });
//     var gridColModel = [
//         {
//             label: "对象类型",
//             name: "userType",
//             formatter: "combobox",
//             sortable: false,
//             formatoptions: {//combobox初始化需要的属性、事件写在formatoptions内，和表单下的combobox一致\
//                 'emptyText': '请选择',
//                 'required': true,
//                 "onSelect": "customRule.userTypeOnSelect",
//                 'data': [{
//                     'value': 'initiator',
//                     'text': '流程发起人'
//                 }, {
//                     'value': 'latest',
//                     'text': '上一节点办理人'
//                 }, {
//                     'value': 'assign',
//                     'text': '指派办理人'
//                 }]
//             }
//         },
//         {
//             label: "用户来自",
//             name: "userOrigin",
//             formatter: "combobox",
//             sortable: false,
//             formatoptions: {//combobox初始化需要的属性、事件写在formatoptions内，和表单下的combobox一致
//                 'emptyText': '请选择',
//                 'required': true,
//                 'data': [{
//                     'value': 'department',
//                     'text': '部门'
//                 }, {
//                     'value': 'unit',
//                     'text': '单位'
//                 }, {
//                     'value': 'bloc',
//                     'text': '集团'
//                 }]
//             }
//         },
//         {
//             label: "是否判断专业线",
//             name: "checkMajorLine",
//             formatter: "checkbox",
//             sortable: false,
//             formatoptions: {value: '1:0'}
//         },
//         {
//             label: "选择对象",
//             name: "selectUser",
//             formatter: "toolbar",
//             sortable: false,
//             formatoptions: {
//                 data: [
//                     {
//                         "id": "add",
//                         "label": "选择",
//                         "disabled": "false",
//                         "onClick": "customRule.fun_selectUser()",
//                         "type": "button",
//                         "cls": "greenbtn",
//                         "icon": "icon-plus3"
//                     }
//                 ]
//             }
//
//         }
//     ];
//     var $grid = $("#gridExpression"),
//         _setting = {
//             width: "auto",
//             height: "auto",
//             colModel: gridColModel,
//             datatype: "local",
//             multiselect: true,
//             data: tableData
//
//         };
//     //  id选择器.组件名({key: value, key: value })
//     $grid.grid(_setting);
//     callback && callback();
// };
// /**
//  * 选择用户对象弹出框
//  */
// customRule.fun_selectUser = function () {
//     // customRule.fun_selectRoleCallback()
//     var rowId = $(event.target).parents("tr").attr("id");
//     var roleId = "";
//     if (initTableData) {
//         if (initTableData[rowId]) {
//             roleId = initTableData[rowId]["selectUser"];
//         }
//     }
//     var src = "iframe-role.html?rowId=" + rowId + "&roleId=" + roleId;
//     var iframe = document.createElement('iframe');
//     iframe.id = 'iframeThird';
//     iframe.scrolling = "no";
//     iframe.src = src;
//     document.body.appendChild(iframe);
// }
// //增加一行
// customRule.fun_addTableRow = function () {
//     var $grid = $("#gridExpression");
//     var totalRecords = $grid.grid("option", "records"),
//         lastsel = "tem_" + (totalRecords + 1),
//         dataAdded = {userType: "initiator", userOrigin: "department", checkMajorLine: true, selectUser: ""};
//     initTableData[lastsel] = dataAdded;
//     $grid.grid("addRowData", lastsel, dataAdded, "last");
// }
// //删除表格中行
// customRule.fun_deleteTableRow = function () {
//     var $grid = $("#gridExpression");
//     var rowIds = $grid.grid("option", "selarrrow");
//     if (rowIds.length == 0) {
//         return;
//     }
//     $.each(rowIds, function (i, id) {
//         $grid.grid("delRowData", id);
//         delete initTableData[id]
//         customRule.fun_deleteTableRow();
//     })
//
// }
// customRule.fun_selectRoleCallback = function (rowId, value, text) {
//     var $showNode = $('tr[id="' + rowId + '"] td[aria-describedby="gridExpression_selectUser"]');
//     var showDiv = $showNode.children(".showSelectUser");
//     var html = text + "<input type='hidden' class='selectUser-value' value='" + value + "' />";
//     if (showDiv.length != 0) {
//         showDiv.html(html);
//     } else {
//         $showNode.append("<div class='showSelectUser'>" + html + "</div>");
//     }
// }
// /**
//  * 以initTableData 作为第三方变量，来存储提交值：因为直接获取table中的值，无法获取自定义的值，所以
//  * 解决方法是搞一个第三方变量专门存储要提交的值
//  * @param callback
//  */
// customRule.save = function (callback) {
//     var $grid = $("#gridExpression");
//     var results = [];
//     $.each(initTableData, function (rowId, initRowData) {
//         console.info(initTableData);
//         console.info(rowId);
//         var realRowData = $grid.grid("getRowData", rowId);
//         var $selectRoleValue = $("#gridExpression tr[id='" + rowId + "'] input[type='hidden'].selectUser-value");
//         if ($selectRoleValue.length == 1) {
//             realRowData["selectUser"] = $selectRoleValue.val();
//         }
//         var $userTypeValue = $("#gridExpression tr[id='" + rowId + "'] input[type='hidden'].userType-value");
//         if ($userTypeValue.length == 1) {
//             realRowData["userType"] = $userTypeValue.val();
//         }
//         results.push(realRowData);
//
//     });
//     tableData = results;
//     //最后提交的结果
//     var value = "";
//     var showValue = "";
//     $.each(results, function (index, result) {
//         var resultTypeValue = result["userType"];
//         // 'value' : 'initiator', value' : 'latest', 'value' : 'assign',
//         var resultStr = JSON.stringify(result);
//         if (resultTypeValue == "initiator") {
//             //流程发起人
//             value += ",${customRuleService.candidate(initiator,'" + resultStr + "')}"
//         } else if (resultTypeValue == "latest") {
//             //上一节点办理人
//             value += ",${customRuleService.candidate(initiator,'" + resultStr + "')}"
//         } else if (resultTypeValue && resultTypeValue.indexOf(":") != -1) {
//             //指定办理人
//             value += ",${customRuleService.candidate('123','" + resultStr + "')}"
//         }
//         showValue += customRule.fun_showValue(result);
//     });
//
//     console.info(results)
//     callback && callback(value.substr(1, value.length), showValue);
//     // //选择的角色
//     // var $roleValue = $("#gridExpression input[type='hidden'].selectUser-value");
//     // $.each($roleValue,function (i,elem) {
//     //    var roleValue = $(elem).val();
//     //    rowDatas[i]["selectUser"] = roleValue;
//     // })
//     // //选择指定办理人
//     // var $userValue = $("#gridExpression input[type='hidden'].userType-value");
//     // $.each($userValue,function (i,elem) {
//     //     var roleValue = $(elem).val();
//     //     rowDatas[i]["userType"] = roleValue;
//     // })
//
//
// };
//
// /**
//  * 选择对象类型的弹出框
//  * @param event
//  * @param ui
//  */
// customRule.userTypeOnSelect = function (event, ui) {
//
//     if (ui.item.value == "assign") {
//         customRule.initTableData();
//         var rowId = $(event.target).parents("tr").attr("id");
//         var userId = "";
//         if (initTableData) {
//             if (initTableData[rowId]) {
//                 userId = initTableData[rowId]["userType"];
//                 if (userId.indexOf(":") == -1) {
//                     userId = "";
//                 }
//             }
//         }
//         var src = "iframe-user.html?rowId=" + rowId + "&userId=" + userId;
//         var iframe = document.createElement('iframe');
//         iframe.id = 'iframeThird';
//         iframe.scrolling = "no";
//         iframe.src = src;
//         document.body.appendChild(iframe);
//     } else {
//         //清空之前选择的内容
//         var $tr = $(event.target).parents("tr");
//         var $showUserType = $tr.find("div.showUserType");
//         if ($showUserType.length != 0) {
//             $showUserType.html("");
//         }
//     }
// }
//
// customRule.fun_selectUserCallback = function (rowId, value, text) {
//     var $showNode = $('tr[id="' + rowId + '"] td[aria-describedby="gridExpression_userType"]');
//     var showDiv = $showNode.children(".showUserType");
//     var html = text + "<input type='hidden' class='userType-value' value='" + value + "' />";
//     if (showDiv.length != 0) {
//         showDiv.html(html);
//     } else {
//         $showNode.append("<div class='showUserType'>" + html + "</div>");
//     }
// }
// //显示内容整理
// customRule.fun_showValue = function (data) {
//     //userType: "latest", userOrigin: "department", checkMajorLine: "1",
//     // selectUser: "(角色)部门经理:0444a6dbb5d2434f9e0fdff5d088904d"}
//     var userType = data["userType"];
//     var userTypeShow = "选择对象:"
//     if (userType == "latest") {
//         userTypeShow += "上一节点办理人"
//     } else if (userType == "initiator") {
//         userTypeShow += "流程发起人";
//     } else if (userType.indexOf(":") != -1) {
//         userTypeShow += "指定节点办理人-" + userType.split(":")[0]
//     }
//
//     var userOrigin = data["userOrigin"];
//     var userOriginShow = "用户来自:";
//     if (userOrigin == "department") {
//         userOriginShow += "部门";
//     } else if (userOrigin == "unit") {
//         userOriginShow += "单位";
//     } else if (userOrigin == "bloc") {
//         userOriginShow += "集团";
//     }
//
//     var checkMajorLine = data["checkMajorLine"];
//     var checkMajorLineShow = "是否判断专业线:"
//     if (checkMajorLine == "1") {
//         checkMajorLineShow += "是"
//     } else {
//         checkMajorLineShow += "否";
//     }
//
//     var selectUser = data["selectUser"];
//     var selectUserShow = "选择对象:";
//     if (selectUser && selectUser != "选择更多") {
//         var selectUsers = selectUser.split(",");
//         $.each(selectUsers, function (index, elem) {
//             selectUserShow += elem.split(":")[0];
//         })
//     }
//
//     return userTypeShow + "," + userOriginShow + "," + checkMajorLineShow + "," + selectUserShow;
//
// };

$(function () {

    initRadioType();
});


