/**
 * Created by wxl on 2018/1/24.
 * @author wxl
 */

var iframeContextPath = "../../../../workflow-version";
//获取已经被选中的节点的名称
var typeList = parent.parent.document.getElementById("candidateGroupsHidden").value;
//获取当前的路径,并进行截取
var asyncUrl = "";
//前两个树的setting
/*var setting = {
    async: {
        enable: false,
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
        }
        ,
        beforeClick: zTreeOnClick
    }
};*/
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
        /*beforeClick: function (treeId, treeNode) {
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
        }*/
    }
};
var setting3 = {
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
    },
    callback: {
        /*beforeClick: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("tree3");
            if (treeNode.isParent) {
                zTree.expandNode(treeNode);
                return false;
            } else {
                return true;
            }
        }*/
    }
};


var $t1 = $("#tree");
var $t2 = $("#tree2");
var $t3 = $('#tree3');
$(function () {
    initTree3();
    initTree();
});

function initTree3() {
    var simpleNodes =[];
    var roleId = getIframeParam("roleId");
    if(roleId && roleId !="选择更多"){
        roleId = roleId.replace("(角色)","");
        roleId = roleId.split(",");
        $.each(roleId,function (index,elem) {
            elem = elem.split(":");
            simpleNodes.push({name: elem[0], id: elem[1], pId: elem[1]});
        })

    }
    $.fn.zTree.init($t3, setting3, simpleNodes);
}

function initTree() {
    $.ajax({
        url: iframeContextPath + "/assignment/role-root?access_token=" + window.parent.parent.token,
        dataType: "json",
        type: "GET",
        success: function (data) {
            $.fn.zTree.init($t2, t2_setting, data.data);
        }
    });
}

//定义当前的左边树的单击事件
/*function zTreeOnClick(treeId, treeNode) {
    var radioType = "role";
    var treeNodeId = treeNode.id;
    $.ajax({
        url: (configURL!=""?("/" + configURL):"")+"/appmanage/workflow-version!getTreeByParamId.json",
        data: {
            "treeNodeId": treeNodeId,
            "radioType": radioType,
            "typeList": "",
            "systemTree": "tree"
        },
        dataType: "json",
        type: "GET",
        success: function (data) {
            var t2 = $("#tree2");
            var treeObj = $.fn.zTree.getZTreeObj("tree3");
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

            t2 = $.fn.zTree.init(t2, t2_setting, data);


        }
    });

};*/

function moveCheck() {
    var treeObj = $.fn.zTree.getZTreeObj("tree2");
    var nodes = treeObj.getCheckedNodes(true);
    var treeObj2 = $.fn.zTree.getZTreeObj("tree3");
    if (treeObj2 != null) {
        var tree3Node = treeObj2.getNodes();

        for (var i = 0; i < nodes.length; i++) {
            if (nodes[i].id != 0 && !nodes[i].isParent) {
                addObject(tree3Node, nodes[i]);
            }

        }
    }
    $.fn.zTree.init($t3, setting3, tree3Node);
}

//全部移除
function deleteAll(){
	  var t3 = $('#tree3');
	  if(!(t3[0].innerText.indexOf("流程发起人") != -1)){
	     t3 = $.fn.zTree.init(t3, setting3, null);
	  }else{
	     var zTree = $.fn.zTree.getZTreeObj("tree3");
	     zTree.checkAllNodes(false);
	  }
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

// 关闭对话框
function closed() {
    var iframe = parent.document.getElementById("iframeThird");
    iframe.parentNode.removeChild(iframe);
}


function save() {
    var nodeList = $.fn.zTree.getZTreeObj("tree3");
    if (nodeList == null) {
        alert("保存的节点不能为空");
    } else {
        var nodes = $.fn.zTree.getZTreeObj("tree3").getNodes(true);
        //var str=radioType+':';
        var str = '';
        var str2 = '';
        if (nodes.length > 0) {
            str += "(角色)";
            str2 += "(角色)";
        }

        for (var i = 0; i < nodes.length; i++) {
            if (i == nodes.length - 1) {
                str += nodes[i].name + ":" + nodes[i].id;
                str2 += nodes[i].name;
            } else {
                str += nodes[i].name + ":" + nodes[i].id + ',';
                str2 += nodes[i].name + ',';
            }

        }


        // //数据包括名称
        // parent.document.getElementById("groupField").value = str2;
        // //数据包括名称和id
        // parent.document.getElementById("candidateGroupsHidden").value = str;
        var rowId = getIframeParam("rowId");
        //回调函数，返回选中的角色信息
        window.parent.customRule.fun_selectRoleCallback(rowId, str, str2);
        closed();
    }


}

/**
 * 根据参数名称获取iframe地址上携带的参数
 * @param name 参数名称
 * @returns {string} 参数的内容
 */
function getIframeParam(name) {
    var reg = new RegExp("[^\?&]?" + encodeURI(name) + "=[^&]+");
    var arr = window.location.href.match(reg);
    if (arr != null) {
        return decodeURI(arr[0].substring(arr[0].search("=") + 1));
    }
    return "";
}