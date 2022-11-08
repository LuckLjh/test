var iframeContextPath = "../../../../workflow-version";

var $orgTree = $("#orgTree");

var iframeUrl = window.location.href;
var token = window.parent.parent.token;
//获取当前的路径,并进行截取
var asyncUrl = "";
var setting = {
    async: {
        enable: true,
        url: iframeContextPath + "/assignment/dept-node?access_token=" + token,
        type: "get",
        autoParam:["id"]
    },
    view: {
        dblClickExpand: true,
        showLine: true,
        selectedMulti: false
    },
    data: {
        simpleData: {
            enable:true,
            idKey: "id",
            pIdKey: "pId",
            rootPId: ""
        }
    }
};

function initTree() {
    var radioType = "user";
    $.ajax({
        url: iframeContextPath + "/assignment/dept-root?access_token=" + token,
        dataType:"json",
        type:"GET",
        success:function(data){
            $.fn.zTree.init($orgTree, setting, data.data)
        }
    });
}

$(function () {
	initTree();
});

function save() {
    var selectedNodes = $.fn.zTree.getZTreeObj("orgTree").getSelectedNodes();
    if (selectedNodes == null || selectedNodes.length <= 0) {
    	alert("请选择一个组织节点！");
    	return;
    }

    var orgId = selectedNodes[0].id;
    if(orgId == 0){
    	alert("请选择一个组织节点！");
    	return;
    }
    var orgName = selectedNodes[0].name;

    //准备参数
    var rowId = getIframeParam("rowId");
    var text = orgName;
    var value = orgName + window.parent.customRule.SPLIT_DEPT + orgId;
    //回调函数，返回选中的角色信息
    window.parent.customRule.fun_selectOrgCallback(rowId, value, text);
    closed();
}

function cancel() {
	var rowId = getIframeParam("rowId");
	window.parent.customRule.fun_cancelUserTypeCallback(rowId);
	closed();
}

//关闭对话框
function closed() {
    var iframe = parent.document.getElementById("iframeThird");
    iframe.parentNode.removeChild(iframe);
}

/**
 * 根据参数名称获取iframe地址上携带的参数
 * @param name 参数名称
 * @returns {string} 参数的内容
 */
function getIframeParam(name) {
    var reg = new RegExp("[^\?&]?" + encodeURI(name) + "=[^&]+");
    var arr = iframeUrl.match(reg);
    if (arr != null) {
        return decodeURI(arr[0].substring(arr[0].search("=") + 1));
    }
    return "";
}
