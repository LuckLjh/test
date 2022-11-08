/**
 * Created by wxl on 2018/2/1.
 * @author wxl
 */
/**
 * 自定义以规则类
 */
var customRule = customRule || {};
// 指定用户
customRule.SPLIT_USER = "__user__";
// 指定部门
customRule.SPLIT_DEPT = "__org__";
// 指定节点
customRule.SPLIT_NODE = "__node__";
// 自定义用户字段
customRule.SPLIT_USER_COL = "__user_col__";
// 自定义部门字段
customRule.SPLIT_DEPT_COL = "__org_col__";

//表格工具条属性对象
customRule.tableToolBarData = [{
    "id": "add",
    "label": "新增",
    "disabled": "false",
    "onClick": "customRule.fun_addTableRow()",
    "type": "button",
    "cls": "greenbtn",
    "icon": "icon-plus3"
}, {
    "id": "delete",
    "label": "删除",
    "disabled": "false",
    "onClick": "customRule.fun_deleteTableRow()",
    "type": "button",
    "cls": "deleteBtn",
    "icon": "icon-bin"
}];
customRule.tableInitTableData = {};
customRule.tableData = [];
//用户来自数据（部门或单位）
customRule.tableUserOrigin=[];
/**
 * 初始化表格数据
 * @param value
 */
customRule.fun_initTableData = function (valueId) {
    var tableDataStr = parent.document.getElementById(valueId).value;
    if (tableDataStr && tableDataStr.indexOf("表达式:${customRuleService") != -1 /*&& (tableDataStr.indexOf("roleId")!=-1 || tableDataStr.indexOf("execution"))*/) {
        tableDataStr = tableDataStr.replace(/表达式:/g, "");
        var re = /(\$|,\$)/g;
        tableDataStr = tableDataStr.replace(re, "-");
        var re2 = /(-{customRuleService.[\w]{1,}\([\w,"']+,)|(-{customRuleService.[\w]{1,}\([\w]+,[\w"']+,)/g;
        tableDataStr = tableDataStr.replace(re2, "-");
        var myDatas = tableDataStr.split("-");
        var myDataJson = [];
        $.each(myDatas, function (index, myData) {
            if (myData) {
                myData = myData.replace(")}", "");
                myData = myData.replace(/'/g, "\"");
                myData = myData.substring(1, myData.length -1)
                myData = JSON.parse(myData);
                //用于恢复选择的隐藏域
                var userType = myData.userType;
                if (userType != null && userType != undefined && userType.indexOf(customRule.SPLIT_USER) != -1) {
                	userType = 'assign'
                }
                if (userType != null && userType != undefined && userType.indexOf(customRule.SPLIT_DEPT) != -1) {
                	userType = 'selectOrg'
                }
                if(userType != null && userType != undefined && userType.indexOf(customRule.SPLIT_NODE) != -1){
                    userType = 'node'
                }
                myData.hiddenUserType = userType;
                myDataJson.push(myData);
            }
        });
        $.each(myDataJson, function (index, myJson) {
            customRule.tableInitTableData[index + 1] = myJson;
            customRule.tableData.push(myJson)
        })
    }

};

/**
 * 初始化表格
 * 1.清空与表格相关的数据
 * 2.
 * @param callback
 */
customRule.fun_initTable = function (tableId,valueId,callback) {
    //清空表格
    customRule.tableInitTableData = {};
    customRule.tableData = [];
    //初始化数据 candidateUsersHidden
    customRule.fun_initTableData(valueId);
    //租户的组织结构
    var tenantOrgs = customRule.fun_getTenantOrg();
    customRule.tableUserOrigin = tenantOrgs;
    var gridColModel = [
        {
            label: "对象类型",
            name: "userType",
            formatter: "combobox",
            sortable: false,
            formatoptions: {//combobox初始化需要的属性、事件写在formatoptions内，和表单下的combobox一致\
                'required': true,
                "onSelect": "customRule.fun_userTypeOnSelect",
                'data': customRule.fun_initUserTypeData()
            }
        },
        {
        	label: "组织关系",
            name: "userOrigin",
            formatter: "combobox",
            sortable: false,
            formatoptions: {//combobox初始化需要的属性、事件写在formatoptions内，和表单下的combobox一致
             'required': true,
             'onSelect': "customRule.fun_userOriginOnSelect",
             'data':  [
            	 {
                     'value': "currentDept",
                     'text': '当前层级',
                     "selected": true
                 }, {
                     'value': "verticalSuperior",
                     'text': "上级层级"
                 },{
                     'value': "currentDeptIncludeSub",
                     'text': '当前层级及其下属部门'
                 },{
                     'value': "upper_current_below",
                     'text': '单位及下属部门'
                 }
                 /*, {
                     'value': "horizontalSuperior",
                     'text': '上级层级(横向)'
                 }*/] //暂时屏蔽掉，用户系统暂无此功能
        	}
        },
        {
            label: "成员/管理员",
            name: "memberOrManager",
            formatter: "combobox",
            hidden: false,
            sortable: false,
            formatoptions: {
                'required': true,
                'onSelect': "customRule.fun_memberOrManagerOnSelect",
                'data': [{
                	'value':'member',
                	'text':'成员'
                }, {
                	'value':'manager',
                	'text':'管理员'
               	}]
            }
        },
        {
            label: "是否判断专业线",
            name: "checkMajorLine",
            hidden: true,
            //formatter: "checkbox",
            formatter: "combobox",
            sortable: false,
           // formatoptions: {value: '1:0'}
            formatoptions: {
                'required': true,
                'data': [{
                    'value': 1,
                    'text': '是',

                }, {
                    'value': 0,
                    'text': '否'
                }]
            }
        },
        {
            label: "选择对象",
            name: "selectUser",
            formatter: "toolbar",
            sortable: false,
            formatoptions: {
                'componentCls': 'grid-selectUser',
                data: [
                    {
                        "id": "select_id",
                        "label": "选择",
                        "disabled": "false",
                        "onClick": "customRule.fun_selectUser",
                        "type": "button",
                        "cls": "greenbtn",
                        "icon": "icon-plus3"
                    }
                ]
            }
        },
        {
        	label: "与/或",
            name: "andOr",
            //formatter: "checkbox",
            formatter: "combobox",
            sortable: false,
           // formatoptions: {value: '1:0'}
            formatoptions: {
            	'componentCls': 'grid-andOr',
                'required': true,
                'data': [{
                    'value': '&&',
                    'text': '与',

                }, {
                    'value': '||',
                    'text': '或'
                }]
            }
        },
        {
        	name: "hiddenUserType",
        	hidden: true
        }
    ];
    var $grid = $("#" + tableId),
        _setting = {
            width: "auto",
            height: "auto",
            colModel: gridColModel,
            datatype: "local",
            multiselect: true,
            data: customRule.tableData,
            onComplete : function (e, data) {
            	var rowIds = $grid.grid("getDataIDs");
            	var len = rowIds.length;
            	if (!len) {
            		return ;
            	}
            	for (var i = 0; i < len; i++) {
            		var rowId = rowIds[i];
            		var rowData = $grid.grid("getRowData", rowId);
            		if ("currentDeptIncludeSub" == rowData.userOrigin || "upper_current_below" == rowData.userOrigin) {
            			var mmJq = $grid.grid("getCellComponent", rowId, "memberOrManager");
            			if (mmJq.length > 1) mmJq = $(mmJq.get(0));
            			mmJq.combobox('option', 'disabled', true);
            		}
            		if ("manager" === rowData.memberOrManager) {
            			var jq = $grid.grid("getCellComponent", rowId, "selectUser");
            			if (jq.length > 1) jq = $(jq.get(0));
            			jq.toolbar('option', 'disabled', true);
            		    var utJq = $('tr[id="' + rowId + '"] td[aria-describedby="gridExpression_selectUser"]');
            		    utJq.find(".showSelectUser").remove();
            		}
            	}
            }

        };
    //  id选择器.组件名({key: value, key: value })
    $grid.grid(_setting);
    //把表格对象赋值给自定义对象
    customRule.$grid = $grid;
    callback && callback();
};
/**
 * 初始化工具条
 * @param toolbarId domId
 * @param callback 回调函数
 */
customRule.fun_initToolBar = function (toolbarId, callback) {
    $("#" + toolbarId).toolbar({
        data: customRule.tableToolBarData
    });
    callback && callback();
};

customRule.fun_initUserTypeData = function () {
	var result = [{
        'value': 'initiator',
        'text': '流程发起人'
    }, {
        'value': 'latest',
        'text': '上一节点办理人'
    }, /*{
        'value': 'node',
        'text': '指定节点办理人'
    },*/ {
    	'value': 'assign',
        'text': '指定办理人'
    }, {
    	'value': 'selectOrg',
    	'text': '指定组织节点'
    }];

	var url = iframeContextPath + "/assignment/dept-columns/" + window.parent.processCode + "?access_token=" + window.parent.token;
	$.ajax({
        type : "get",
        url : url,
        async : false,
        dataType: "json",
        success : function(data){
        	if (0 !== data.code) {
        		return ;
        	}
        	var deptData = data.data;
        	if (null == deptData) {
        		return ;
        	}
        	for (i in deptData) {
        		var o = {
        				  'value'	: deptData[i].text + customRule.SPLIT_DEPT_COL + deptData[i].value,
        				  'text'	: deptData[i].text
        				}
        		result.push(o);
        	}

        }
    });
	return result;
}

/**
 * 添加一行
 */
customRule.fun_addTableRow = function () {
    var $grid = customRule.$grid;
    //var userOriginId = customRule.tableUserOrigin[0].id;
    var totalRecords = $grid.grid("option", "records"),
        lastsel = "tem_" + (totalRecords + 1),
        dataAdded = {userType: "initiator", userOrigin: "currentSuperior",memberOrManager:'member', checkMajorLine: 0,
    	    andOr:"||",selectUser: "", hiddenUserType: "initiator"};
    customRule.tableInitTableData[lastsel] = dataAdded;
    $grid.grid("addRowData", lastsel, dataAdded, "last");
};

/**
 * 删除一行
 */
customRule.fun_deleteTableRow = function () {
    var $grid = customRule.$grid;
    var rowIds = $grid.grid("option", "selarrrow");
    if (rowIds.length == 0) {
        return;
    }
    $.each(rowIds, function (i, id) {
        $grid.grid("delRowData", id);
        delete customRule.tableInitTableData[id]
        customRule.fun_deleteTableRow();
    })

};

/**
 * 对象类型选择事件
 * 1.只有当类型为指定办理人的时候才会触发
 * 2.弹出选择具体用户的对话框
 */
customRule.fun_userTypeOnSelect = function (event, ui) {
	var rowId = $(event.target).parents("tr").attr("id");
	//选择用户
    if (ui.item.value == "assign") {
        //customRule.fun_initTableData();
        var userId = "";
        if (customRule.tableInitTableData) {
            if (customRule.tableInitTableData[rowId]) {
                userId = customRule.tableInitTableData[rowId]["userType"];
                if (userId.indexOf(":") == -1) {
                    userId = "";
                }
            }
        }
        var src = "iframe-user.html?rowId=" + rowId + "&userId=" + userId;
        var iframe = document.createElement('iframe');
        iframe.id = 'iframeThird';
        iframe.scrolling = "no";
        iframe.src = src;
        document.body.appendChild(iframe);
    //选择组织
    } else if (ui.item.value == "selectOrg") {
        var userId = "";
        if (customRule.tableInitTableData) {
            if (customRule.tableInitTableData[rowId]) {
                userId = customRule.tableInitTableData[rowId]["userType"];
                if (userId.indexOf(":") == -1) {
                    userId = "";
                }
            }
        }
        var src = "iframe-org.html?rowId=" + rowId + "&userId=" + userId;
        var iframe = document.createElement('iframe');
        iframe.id = 'iframeThird';
        iframe.scrolling = "no";
        iframe.src = src;
        document.body.appendChild(iframe);
    } else if (ui.item.value == "node") {
        var userId = "";
        if (customRule.tableInitTableData) {
            if (customRule.tableInitTableData[rowId]) {
                userId = customRule.tableInitTableData[rowId]["userType"];
                if (userId.indexOf(":") == -1) {
                    userId = "";
                }
            }
        }
      var src = "iframe-custom-node.html?rowId=" + rowId;
      var iframe = document.createElement('iframe');
      iframe.id = 'iframeNode';
      iframe.scrolling = "yes";
      iframe.src = src;
      document.body.appendChild(iframe);
  } else {
        //清空之前选择的内容
        var $tr = $(event.target).parents("tr");
        var $showUserType = $tr.find("div.showUserType");
        if ($showUserType.length != 0) {
            $showUserType.html("");
        }
        customRule.$grid.grid('setCell', rowId, 'hiddenUserType' , ui.item.value);
    }

    if (ui.item.value != 'assign' && ui.item.value != 'initiator' && ui.item.value != 'latest' && ui.item.value != 'node'&& ui.item.value != 'selectOrg' ) {
    	//置灰无用选项
    	//customRule.$grid.grid("getCellComponent", rowId, 'userOrigin').combobox('setValue', '');
    	//customRule.$grid.grid("getCellComponent", rowId, 'userOrigin').combobox('option', 'readonly', true);
    } else {
    	var value = customRule.$grid.grid("getCellComponent", rowId, 'userOrigin').combobox('getValue');
    	if (value == '' || value == undefined || value == null) {
    		customRule.$grid.grid("getCellComponent", rowId, 'userOrigin').combobox('setValue', currentSuperior);
    	}
    	customRule.$grid.grid("getCellComponent", rowId, 'userOrigin').combobox('option', 'readonly', false);
    }
}

/**
 * 所在/被管辖选择事件
 */
customRule.fun_userOriginOnSelect = function (event, ui) {
	debugger;
	var rowId = event.data.rowId;
	var mmJq = customRule.$grid.grid("getCellComponent", rowId, "memberOrManager");
	if (mmJq.length > 1) {
		mmJq = $(mmJq.get(0));
	}
	if ("currentDeptIncludeSub" === ui.value || "upper_current_below" === ui.value) {
		mmJq.combobox("setValue", "member");
		mmJq.combobox('option', 'disabled', true);
		var suJq = customRule.$grid.grid("getCellComponent", rowId, "selectUser");
		suJq.toolbar('option', 'disabled', false);
	} else {
		mmJq.combobox('option', 'disabled', false);
	}
}

/**
 * 所在/被管辖选择事件
 */
customRule.fun_memberOrManagerOnSelect = function (event, ui) {
	var rowId = event.data.rowId;
	var jq = customRule.$grid.grid("getCellComponent", rowId, "selectUser");
	if (jq.length > 1) {
		jq = $(jq.get(0));
	}
	if ("manager" === ui.value) {
		jq.toolbar('option', 'disabled', true);
	    var utJq = $('tr[id="' + rowId + '"] td[aria-describedby="gridExpression_selectUser"]');
	    utJq.find(".showSelectUser").remove();
	} else {
		jq.toolbar('option', 'disabled', false);
	}
}

/**
 * 选择完用户类型之后回调
 * @param rowId 行ID
 * @param value 隐藏的内容
 * @param text 返回的要展示的结果
 */
customRule.fun_selectUserCallback = function (rowId, value, text) {
    var $showNode = $('tr[id="' + rowId + '"] td[aria-describedby="gridExpression_userType"]');
    var showDiv = $showNode.children(".showUserType");
    var html = "<span class='cuc-tooltip' data-attr='"+text+"'>"+text+"</span><input type='hidden' class='userType-value' value='" + value + "' />";
    if (showDiv.length != 0) {
        showDiv.html(html);
    } else {
        $showNode.append("<div class='showUserType'>" + html + "</div>");
    }
    customRule.$grid.grid('setCell', rowId, 'hiddenUserType', 'assign');
    $(".cuc-tooltip").tooltip({
    	  "content": function(){
    	      var element = $( this );
    	      var val = $(this).children(".showUserType").children(".cuc-tooltip").data("attr");
    	      return  val
    	  }
    })
}

/**
 * 选择完组织之后回调
 * @param rowId 行ID
 * @param value 隐藏的内容
 * @param text 返回的要展示的结果
 */
customRule.fun_selectOrgCallback = function (rowId, value, text) {
    var $showNode = $('tr[id="' + rowId + '"] td[aria-describedby="gridExpression_userType"]');
    var showDiv = $showNode.children(".showUserType");
    var html = "<span class='cus-tooltip' data-attr='"+text+"'>"+text+"</span><input type='hidden' class='userType-value' value='" + value + "' />";
    if (showDiv.length != 0) {
        showDiv.html(html);
    } else {
        $showNode.append("<div class='showUserType'>" + html + "</div>");
    }
    customRule.$grid.grid('setCell', rowId, 'hiddenUserType', 'selectOrg');
    $(".cus-tooltip").tooltip({
    	  "content": function() {
    	     var element = $( this );
    	     var val = $(this).children(".showUserType").children(".cus-tooltip").data("attr");
    	     return  val
    	  }
    })
}

/**
 * 选择用户角色之后的回调函数
 * @param rowId 行ID
 * @param value 隐藏的内容
 * @param text 返回的要展示的结果
 */
customRule.fun_selectRoleCallback = function (rowId, value, text) {
    var $showNode = $('tr[id="' + rowId + '"] td[aria-describedby="gridExpression_selectUser"]');
    var showDiv = $showNode.children(".showSelectUser");
    var html =  "<span class='cui-tooltip' data-attr='"+text+"'>"+text+"</span><input type='hidden' class='selectUser-value' value='" + value + "' />";
    if (showDiv.length != 0) {
        showDiv.html(html);
    } else {
        $showNode.append("<div class='showSelectUser'>" + html + "</div>");
    }
    $(".cui-tooltip").tooltip({
    	  "content": function() {
    	      var element = $( this );
    	      var val = $(this).children(".showSelectUser").children(".cui-tooltip").data("attr");
    	      return  val
    	  }
    })
}

/**
 * 选择指定节点之后的回调函数
 * @param rowId 行id
 * @param nodeId 选择的节点id
 */
customRule.fun_selectNodeCallback = function(rowId, value ,text){
 var $showNode = $('tr[id="' + rowId + '"] td[aria-describedby="gridExpression_userType"]');
 var showDiv = $showNode.children(".showUserType");
    var html = text+"<input type='hidden' class='userType-value' value='" + value + "'/>";;
    if (showDiv.length != 0) {
        showDiv.html(html);
    } else {
        $showNode.append("<div class='showUserType'>" + html + "</div>");
    }
    customRule.$grid.grid('setCell', rowId, 'hiddenUserType', 'node');
}
/**
 * 取消选择框时的回调函数
 * 用于还原UserType选择框的选项
 */
customRule.fun_cancelUserTypeCallback = function (rowId) {
	var $grid = customRule.$grid;
	var hiddenUserType = $grid.grid('getCell', rowId, 'hiddenUserType');
	$grid.grid("getCellComponent", rowId, "userType")
		.combobox('setValue', hiddenUserType);
	if (hiddenUserType != 'assign' && hiddenUserType != 'initiator' && hiddenUserType != 'latest' && hiddenUserType != 'node'&& hiddenUserType != 'selectOrg' ) {
    	//置灰无用选项
		//$grid.grid("getCellComponent", rowId, 'userOrigin').combobox('setValue', '');
    	//$grid.grid("getCellComponent", rowId, 'userOrigin').combobox('option', 'readonly', true);
    } else {
    	$grid.grid("getCellComponent", rowId, 'userOrigin').combobox('option', 'readonly', false);
    }
}

/**
 * 选择用户对象弹出框
 */
customRule.fun_selectUser = function (e,ui) {
	debugger;
    var rowId = $(e.target).parents("tr").attr("id");
	var $grid = customRule.$grid;
	var memberOrManager = $grid.grid('getCell', rowId, 'memberOrManager');
	if ("manager" == memberOrManager) {
		return;
	}
    var roleId = "";
    if (customRule.tableInitTableData) {
        if (customRule.tableInitTableData[rowId]) {
            roleId = customRule.tableInitTableData[rowId]["selectUser"];
        }
    }
    var src = "iframe-role.html?rowId=" + rowId + "&roleId=" + roleId;
    var iframe = document.createElement('iframe');
    iframe.id = 'iframeThird';
    iframe.scrolling = "no";
    iframe.src = src;
    document.body.appendChild(iframe);
}
/**
 * 以customRule.tableInitTableData 作为第三方变量，来存储提交值：因为直接获取table中的值，无法获取自定义的值，所以
 * 解决方法是搞一个第三方变量专门存储要提交的值
 * @param callback
 */
customRule.save = function (callback) {
    var userTypeFlag = false;
    var $grid = customRule.$grid;
    var results = [];
    $.each(customRule.tableInitTableData, function (rowId, initRowData) {
        var realRowData = $grid.grid("getRowData", rowId);
        var $selectRoleValue = $("#gridExpression tr[id='" + rowId + "'] input[type='hidden'].selectUser-value");

        if ($selectRoleValue.length == 1) {
            realRowData["selectUser"] = $selectRoleValue.val();
        }
        var $userTypeValue = $("#gridExpression tr[id='" + rowId + "'] input[type='hidden'].userType-value");
        if(realRowData['userType']=='assign' && $userTypeValue.length == 0){
            alert("请选择指派办理人");
            userTypeFlag = true;
        }
        if ($userTypeValue.length == 1) {
            realRowData["userType"] = $userTypeValue.val();
        }
        if ("manager" == realRowData.memberOrManager) {
        	realRowData.selectUser = "";
        }
        results.push(realRowData);

    });
    //如果没有选中提示为选择指定班里人
    if(userTypeFlag) return true;
    customRule.tableData = results;
    //最后提交的结果
    var value = "";
    var showValue = "";
    showValue += "任务分配：自定义规则"+results.length+"条";
    $.each(results, function (index, result) {
        var resultTypeValue = result["userType"];
        // 'value' : 'initiator', value' : 'latest', 'value' : 'assign',
        var resultStr = JSON.stringify(result);
        var firstIsBlank = (index ==0?"":"表达式:")
        if (resultTypeValue == "initiator") {
            //流程发起人
            value += ","+firstIsBlank+"${customRuleService.candidateByUserId(initiator,'" + resultStr + "')}"
        } else if (resultTypeValue == "latest") {
            //上一节点办理人
            value += ","+firstIsBlank+"${customRuleService.candidateByUserId(workflow_defaultVariable_preTaskAssignee,'" + resultStr + "')}"
        } else if (resultTypeValue && resultTypeValue.indexOf(customRule.SPLIT_NODE) != -1){
            //指定节点办理人
            var nodeId = resultTypeValue.split(customRule.SPLIT_NODE)[1];
            value += ","+firstIsBlank+"${customRuleService.candidateByNodeIdAndUserId(execution,\""+nodeId+"\",'" + resultStr + "')}"
        } else if (resultTypeValue && resultTypeValue.indexOf(customRule.SPLIT_USER) != -1) {
            //指定办理人
            var userId = resultTypeValue.split(customRule.SPLIT_USER)[1];
            value += ","+firstIsBlank+"${customRuleService.candidateByUserId(\""+userId+"\",'" + resultStr + "')}"
        } else if (resultTypeValue && resultTypeValue.indexOf(customRule.SPLIT_DEPT) != -1) {
            //指定组织节点
            var orgId = resultTypeValue.split(customRule.SPLIT_DEPT)[1];
            value += ","+firstIsBlank+"${customRuleService.candidateByOrgId(\""+orgId+"\",'" + resultStr + "')}"
        } else if (resultTypeValue && resultTypeValue.indexOf(customRule.SPLIT_DEPT_COL) != -1) {
            //配置平台导入的数据
            var orgId = resultTypeValue.split(customRule.SPLIT_DEPT_COL)[1];
            value += ","+firstIsBlank+"${customRuleService.candidateByOrgId("+orgId+",'" + resultStr + "')}"
        } else if (resultTypeValue && resultTypeValue.indexOf(customRule.SPLIT_USER_COL) != -1) {
            //配置平台导入的数据
            var userId = resultTypeValue.split(customRule.SPLIT_USER_COL)[1];
            value += ","+firstIsBlank+"${customRuleService.candidateByUserId("+userId+",'" + resultStr + "')}"
        }
        //showValue += customRule.fun_showValue(result);
    });

    console.info(results)
    callback && callback(value.substr(1, value.length), showValue);

};
//显示内容整理
customRule.fun_showValue = function (data) {

    //userType: "latest", userOrigin: "department", checkMajorLine: "1",
    // selectUser: "(角色)部门经理:0444a6dbb5d2434f9e0fdff5d088904d"}
    var userType = data["userType"];
    var userTypeShow = "选择对象:"
    if (userType == "latest") {
        userTypeShow += "上一节点办理人"
    } else if (userType == "initiator") {
        userTypeShow += "流程发起人";
    } else if (userType.indexOf(customRule.SPLIT_USER) != -1) {
        userTypeShow += "指定办理人-" + userType.split(customRule.SPLIT_USER)[0]
    } else if (userType.indexOf(customRule.SPLIT_NODE) != -1) {
        userTypeShow += "指定节点办理人-" + userType.split(customRule.SPLIT_NODE)[0]
    } else if (userType.indexOf(customRule.SPLIT_DEPT) != -1) {
        userTypeShow += "指定组织节点-" + userType.split(customRule.SPLIT_DEPT)[0]
    }

    var memberOrManager = data["memberOrManager"];
    var memberOrManagerShow = " 成员/管理员:";
    if (memberOrManager == "member") {
    	memberOrManagerShow += "成员";
    } else if (memberOrManager == "manager") {
    	memberOrManagerShow += "管理员";
    }


    var userOrigin = data["userOrigin"];
    var userOriginShow = "组织关系:";
    $.each(customRule.tableUserOrigin,function (index,data) {
        if(data && data.id == userOrigin){
            userOriginShow+=data["name"];
        }
    })

    var checkMajorLine = data["checkMajorLine"];
    var checkMajorLineShow = "是否判断专业线:"
    if (checkMajorLine == 1) {
        checkMajorLineShow += "是";
    } else {
        checkMajorLineShow += "否";
    }

    var selectUser = data["selectUser"];
    var selectUserShow = "选择对象:";
    if (selectUser && selectUser != "选择更多") {
        var selectUsers = selectUser.split(",");
        $.each(selectUsers, function (index, elem) {
            selectUserShow += elem.split(":")[0];
        })
    }else{
        selectUserShow+="暂无";
    }

    return userTypeShow + "," + userOriginShow + "," + memberOrManagerShow
    	+ "," + checkMajorLineShow + "," + selectUserShow;

};

//获取当前租户的组织结构
customRule.fun_getTenantOrg = function () {
    var results = [];
    var url = iframeContextPath + "/assignment/tenant/org-levels?access_token=" + window.parent.token;
    $.ajax({
        type : "get",
        url : url,
        async : false,
        dataType: "json",
        success : function(data){
            results = data;
        }
    });

    return results;
}
