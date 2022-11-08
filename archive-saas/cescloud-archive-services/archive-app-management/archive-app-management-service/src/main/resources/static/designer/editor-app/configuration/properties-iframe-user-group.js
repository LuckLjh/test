var iframeContextPath = "../../../../workflow-version";

var $t1 = $("#tree");
var $t2 = $("#tree2");
var $t3 = $('#tree3');

var iframeUrl = window.location.href;
var token = window.parent.parent.token;
//获取当前的路径,并进行截取
var asyncUrl = "";
var t3_setting = {
    keep:{
        leaf:false,
        parent:false
    },
    check: {
        enable: false,
        chkStyle: "checkbox",
        chkboxType: { "Y": "p", "N": "s" }
    },
    view: {
        dblClickExpand: false,
        showLine: true,
        selectedMulti: true
    },
    edit: {
        enable: true,
        drag: {
            isCopy: false,
            isMove: true,
            prev:true,
            inner:false,
            next:true
        },
        showRemoveBtn: true,
        showRenameBtn: false
    },
    data: {
        simpleData: {
            enable:true,
            idKey: "id",
            pIdKey: "pId",
            rootPId: ""
        }
    },
    callback: {
        beforeClick: function(treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("tree3");
            if (treeNode.isParent) {
                zTree.expandNode(treeNode);
                return false;
            } else {
                return true;
            }
        }
    }
};
var t2_setting = {
    check: {
        enable: true,
        chkStyle: "radio",
        chkboxType: { "Y": "s", "N": "s" }
    },
    view: {
        dblClickExpand: false,
        showLine: true,
        selectedMulti: true
    },
    data: {
        simpleData: {
            enable:true,
            idKey: "id",
            pIdKey: "pId",
            rootPId: ""
        }
    },
    callback: {
        beforeClick: function(treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("tree");
            if (treeNode.isParent) {
                zTree.expandNode(treeNode);
                return false;
            } else {
                return true;
            }
        },
        onClick: function(event, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("tree2");
            zTree.checkNode(treeNode, !treeNode.checked, true);
         }
        //onClick: zTreeOnClick2,
    }
};
var setting = {
    async: {
        enable: true,
        url:iframeContextPath + "/assignment/dept-node?access_token=" + token,
        type: "get",
        autoParam:["id"]
    },
    view: {
        dblClickExpand: true,
        showLine: true,
        selectedMulti: true
    },
    data: {
        simpleData: {
            enable:true,
            idKey: "id",
            pIdKey: "pId",
            rootPId: ""
        }
    },
    callback: {
        beforeClick: function(treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("tree");
            if (treeNode.isParent) {
                zTree.expandNode(treeNode);
                return false;
            } else {
                return true;
            }
        },
        beforeClick:zTreeOnClick
    }
};
// 关闭对话框
function closed() {
    var iframe = parent.document.getElementById("iframeThird");
    iframe.parentNode.removeChild(iframe);
}

function initTree() {
    var radioType = "user";
    $.ajax({
        url: iframeContextPath + "/assignment/dept-root?access_token=" + token,
        dataType:"json",
        type:"GET",
        success:function(data){
            $.fn.zTree.init($t1, setting, data.data)
        }
    });
}
function initTree3() {
    var simpleNodes =[];
    var userId = getIframeParam("userId");
    if(userId){
        userId = userId.split(":");
        simpleNodes.push({name: userId[0], id: userId[1], pId: userId[1]});
    }
    $.fn.zTree.init($t3, t3_setting, simpleNodes);
}
$(function () {
   initTree();
   initTree3();
});

/**
 *
 * @param treeId
 * @param treeNode
 */
function zTreeOnClick(treeId, treeNode) {
    var treeNodeId = treeNode.id;
    var nodes = $.fn.zTree.getZTreeObj("tree3").getNodes();
    if(nodes.length>0){

        var str="";

        for(var node in nodes){
            str+=nodes[node].name+":"+nodes[node].loginName+",";
        }
        if(str!=""){
            str = str.substring(0,str.length-1);
        }
        //typeList = str;
    }
    var usersUrl = iframeContextPath + "/assignment/users/dept/" + treeNodeId + "?access_token=" + token;
    $.ajax({
        url: usersUrl,
        dataType:"json",
        type:"GET",
        success:function(data) {
        	data[0].chkDisabled = true;
            $.fn.zTree.init($t2, t2_setting, data);

        }
    });
};


function moveCheck(){
    var treeObj = $.fn.zTree.getZTreeObj("tree2");
    var nodes = treeObj.getCheckedNodes(true);
    var treeObj2 = $.fn.zTree.getZTreeObj("tree3");
    var tree3Node = [];
    for(var i=0;i<nodes.length;i++){
        if(nodes[i].id != 0){
            addObject(tree3Node,nodes[i]);
        }

    }
     $.fn.zTree.init($t3, t3_setting, tree3Node);
}
function addObject(ls,basemodel){
    var flag=false;
    for(var i=0;i<ls.length;i++){
        if(ls[i].loginName){
            if(ls[i].loginName==basemodel.loginName){
                flag=true;
                break;
            }
        }else{
            if(basemodel.id===ls[i].id){
                flag=true;
                break;
            }
        }
    }
    if(flag===false){
        ls.push(basemodel);
    }
    return ls;
}

//全部移除
function deleteAll(){
   var a = $($("tbody")[1]).find("tr:gt(0)").remove();
   if(a.context == undefined){
       var t3 = $('#tree3');
       t3 = $.fn.zTree.init(t3, t3_setting, null);
    }
}

function save(){
    var nodeList = $.fn.zTree.getZTreeObj("tree3");
    if(nodeList== null){
        alert("保存的节点不能为空");
    }else{
        var nodes = $.fn.zTree.getZTreeObj("tree3").getNodes(true);
        //var str=radioType+':';
        var str ='';
        var str2='';
        for(var i=0;i<nodes.length;i++){
            if(i==nodes.length-1){
                if(nodes[i].loginName==undefined){
                    str+=nodes[i].name;
                }else{
                    str+=nodes[i].name + window.parent.customRule.SPLIT_USER + nodes[i].id;
                }
                str2+=nodes[i].name;
            }else{
                if(nodes[i].loginName==undefined){
                    str+=nodes[i].name+',';
                }else{
                    str+=nodes[i].name + window.parent.customRule.SPLIT_USER + nodes[i].id+',';
                }
                str2+=nodes[i].name+',';
            }

        }
        // parent.document.getElementById("userField").value = str2;
       // parent.document.getElementById("candidateUsersHidden").value = str;
        var rowId = getIframeParam("rowId");
        //回调函数，返回选中的角色信息
        window.parent.customRule.fun_selectUserCallback(rowId, str, str2);
        closed();
    }

}

function cancel() {
	debugger
	var rowId = getIframeParam("rowId");
	window.parent.customRule.fun_cancelUserTypeCallback(rowId);
	closed();
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
