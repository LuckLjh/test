var iframeContextPath = "../../../../workflow-version";

//获取多实例类型值
var type = parent.document.getElementById("assignmentType").value;
if (type == 'Parallel' || type == "Sequential") {
  $('#processSponsor,#dealPeople').parents('span').hide();
}

var t3 = $("#tree3");
//获取已经被选中的节点的名称
var typeList = parent.document.getElementById("candidateUsersHidden").value;
//console.log(typeList);
//获取当前的路径,并进行截取
var t3_setting = {
  keep: {
    leaf: false,
    parent: false
  },
  check: {
    enable: false,
    chkStyle: "checkbox",
    chkboxType: {
      "Y": "p",
      "N": "s"
    }
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
      prev: true,
      inner: false,
      next: true
    },
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
    beforeClick: function (treeId, treeNode) {
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
if (typeList != '') {
  var typeList2 = typeList.split(',');
  var totalSum = [];
  var simpleNodes = [];
  var typeList3;
  for (var i = 0; i < typeList2.length; i++) {
    //totalSum.push(typeList2[i]);

    //alert(totalSum);
    /*var simpleNodes =[
                      { name:typeList2[i],
                      id:i,
                      pId:i
                    }
                  ];*/
    typeList3 = typeList2[i].split(':');
    simpleNodes.push({
      name: typeList3[0],
      id: typeList3[1],
      loginName: typeList3[1]
    });
  }
  //alert(simpleNodes);
  var data = simpleNodes;

}
if (data == null) {
  t3 = $.fn.zTree.init(t3, t3_setting, null);
} else {
  if (data[0].name == '流程发起人' || data[0].name == '上一节点处理人'
    || data[0].name.split(":")[0] == '表达式') {
    t3 = $.fn.zTree.init(t3, t3_setting, null);
  } else {
    t3 = $.fn.zTree.init(t3, t3_setting, data);
  }
}

var treeObj3 = $.fn.zTree.getZTreeObj("tree3");

//获取radio的类型
var radioType = $('input:radio:checked').val();

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
        type: "get",
        url: iframeContextPath + "/assignment/dept-node?access_token=" + window.parent.token,
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
      var nodes = treeObj3.getNodes();
      if (nodes.length > 0) {

        var str = "";

        for (var node in nodes) {
          str += nodes[node].name + ":"
            + nodes[node].loginName + ",";
        }
        if (str != "") {
          str = str.substring(0, str.length - 1);
        }
        typeList = str;
      }
      var usersUrl = iframeContextPath + "/assignment/users";
      if ("role" == radioType) {
        usersUrl += "/role/" + treeNodeId;
      } else {
        usersUrl += "/dept/" + treeNodeId;
      }
      usersUrl += "?access_token=" + window.parent.token;
      $.ajax({
        url: usersUrl,
        dataType: "json",
        type: "GET",
        success: function (data) {
          var t2_setting = {
            check: {
              enable: true,
              chkStyle: "checkbox",
              chkboxType: {
                "Y": "s",
                "N": "s"
              }
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
              onClick: zTreeOnClick2,
            }
          };

          var t2 = $("#tree2");
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
    // var expre =  document.getElementById("expressionId").value;
    // if(expre){
    //  parent.document.getElementById("userField").value = "表达式:"+expre;
    // }

    closeFlag = customRule
      .save(function (value, showValue) {
        //parent.document.getElementById("groupField").value = "表达式:" + value;
        //数据包括名称
        parent.document.getElementById("userField").value = showValue;
        //数据包括名称和id
        parent.document.getElementById("candidateUsersHidden").value = "表达式:"
          + value;
      });
  } else if (radioType == 'customExpression') {
    /*var expre =  document.getElementById("expressionId").value;
    if(expre){
     parent.document.getElementById("userField").value = "表达式:"+expre;
     parent.document.getElementById("candidateUsersHidden").value = "表达式:"+expre;
    }*/
    var expreText = $("#expressionId").combobox("getText");
    var expre = $("#expressionId").combobox("getValue");
    if (expre) {
      parent.document.getElementById("userField").value = "表达式:"
        + expreText;
      parent.document.getElementById("candidateUsersHidden").value = "表达式:"
        + expre;
    }
  } else {
    if (nodeList == null) {
      alert("保存的节点不能为空");
    } else {
      var nodes = $.fn.zTree.getZTreeObj("tree3").getNodes(true);
      //var str=radioType+':';
      var str = '';
      var str2 = '';
      for (var i = 0; i < nodes.length; i++) {
        if (i == nodes.length - 1) {
          if (nodes[i].loginName == undefined) {
            str += nodes[i].name;
          } else {
            str += nodes[i].name + ":" + nodes[i].id;
          }
          str2 += nodes[i].name;
        } else {
          if (nodes[i].loginName == undefined) {
            str += nodes[i].name + ',';
          } else {
            str += nodes[i].name + ":" + nodes[i].id + ',';
          }
          str2 += nodes[i].name + ',';
        }

      }
      if (radioType == 'processSponsor') {
        parent.document.getElementById("userField").value = '流程发起人';
        parent.document.getElementById("candidateUsersHidden").value = '表达式:${initiator}';
      } else if (radioType == 'dealPeople') {
        parent.document.getElementById("userField").value = '上一节点处理人';
        parent.document.getElementById("candidateUsersHidden").value = '上一节点处理人';
      } else {
        if (str != '') {
          parent.document.getElementById("userField").value = str2;
          parent.document.getElementById("candidateUsersHidden").value = str;
        } else {
          parent.document.getElementById("userField").value = "";
          parent.document.getElementById("candidateUsersHidden").value = "";
        }
      }
    }
  }
  if (!closeFlag) {
    var iframe = parent.document.getElementById("iframeTest");
    iframe.parentNode.removeChild(iframe);

  }

}

function moveCheck() {
  var treeObj = $.fn.zTree.getZTreeObj("tree2");
  var nodes = treeObj.getCheckedNodes(true);
  var treeObj2 = $.fn.zTree.getZTreeObj("tree3");
  var tree3Node = treeObj2.getNodes();

  for (var i = 0; i < nodes.length; i++) {
    if (!nodes[i].isParent) {
      addObject(tree3Node, nodes[i]);
    }

  }
  var t3 = $("#tree3");
  t3 = $.fn.zTree.init(t3, t3_setting, tree3Node);
}

function addObject(ls, basemodel) {
  var flag = false;
  for (var i = 0; i < ls.length; i++) {
    if (ls[i].id) {
      if (basemodel.id === ls[i].id) {
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
  //获取已经选中的用户的名字
  var typeList = parent.document.getElementById("candidateUsersHidden").value;
  //获取radio的类型
  var radioType = $('input:radio:checked').val();

  //获取当前的路径
  if (radioType == "processSponsor" || radioType == "dealPeople") {
    //$('.modal-body').append('<div class="readonly"></div>');
    $('.modal-body').addClass('onlyrow1');
    $('#expressionBox').html('');
    var t2_setting = {
      check: {
        enable: true,
        chkStyle: "checkbox",
        chkboxType: {
          "Y": "p",
          "N": "s"
        }
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
        onClick: function (event, treeId, treeNode) {
          var zTree = $.fn.zTree.getZTreeObj("tree3");
          zTree.checkNode(treeNode, !treeNode.checked, true);
        }

      }
    };

    var hiddenValue = parent.document
      .getElementById("candidateUsersHidden").value;
    var checkFlag = false;
    var t3 = $("#tree3");
    if (radioType == "processSponsor") {
      if (hiddenValue && hiddenValue.indexOf("表达式:${initiator}") == 0) {
        checkFlag = true;
      }
      var data = [{
        "id": "8b797d01ad254557bb201e20e7aec57a",
        "name": "流程发起人",
        "checked": checkFlag
      }];

    } else {
      if (hiddenValue && hiddenValue == "上一节点处理人") {
        checkFlag = true;
      }
      var data = [{
        "id": "8b797d01ad254557bb201e20e7aec57b",
        "name": "上一节点处理人",
        "checked": checkFlag
      }];
    }

    t3 = $.fn.zTree.init(t3, t2_setting, data);

  } else if (radioType == "expression") {
    $('.modal-body').addClass('onlyrow1');
    var t3 = $('#tree3');
    t3 = $.fn.zTree.init(t3, t2_setting, null);
    //显示内容
    $('#expressionBox').css({
      display: "block"
    });
    //销毁已经存在的表格
    if ($("#gridExpression").hasClass("ctrl-init-grid")) {
      $("#gridExpression").grid("destroy");
    }
    //自定义规则html
    var html = '<div id="gridExpressionToolbar"></div> <div id="gridExpression"></div>';
    $('#expressionBox').html(html);

    //初始化表格
    customRule.fun_initTable("gridExpression", "candidateUsersHidden",
      function () {
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
            customRule.fun_selectRoleCallback(index + 1,
              selectUser, text.substr(1, text.length));
          });
          //处理选择对象类型的指定用户
          $.each(customRule.tableData, function (index, rowData) {
            var $grid = customRule.$grid;
            var rowId = index + 1;
            var userType = rowData["userType"];
            if (userType.indexOf(customRule.SPLIT_USER) != -1) {
              //选中组件
              var $com = $grid.grid("getCellComponent",
                rowId, "userType");
              $com.combobox("setValue", "assign");
              //显示内容
              var userTypes = userType.split(customRule.SPLIT_USER);
              var text = userTypes[0];
              var $userType = $("tr[id='" + (index + 1)
                + "'] [name='userType']");
              customRule.fun_selectUserCallback(index + 1,
                userType, text);
            }
            if (userType.indexOf(customRule.SPLIT_DEPT) != -1) {
              //选中组件
              var $com = $grid.grid("getCellComponent",
                rowId, "userType");
              $com.combobox("setValue", "selectOrg");
              //显示内容
              var userTypes = userType.split(customRule.SPLIT_DEPT);
              var text = userTypes[0];
              var $userType = $("tr[id='" + (index + 1)
                + "'] [name='userType']");
              customRule.fun_selectOrgCallback(index + 1,
                userType, text);
            }
            if (userType.indexOf(customRule.SPLIT_NODE) != -1) {
              //选中组件
              var $com = $grid.grid("getCellComponent",
                rowId, "userType");
              $com.combobox("setValue", "node");
              //显示内容
              var userTypes = userType.split(customRule.SPLIT_NODE);
              var text = userTypes[0];
              var $userType = $("tr[id='" + (index + 1)
                + "'] [name='userType']");
              customRule.fun_selectNodeCallback(index + 1,
                userType, text);
            }

          });

        }
      });
    // var html = '<input type="text" id="expressionId">'
    //  		+ '<div class="expressContnt clearfix">'
    //  			+ '<p><span class="iconfont icon-bangzhux">？</span><span class="expresstext">这里可以使用uel表达式，UEL是java EE6规范的一部分，UEL（Unified Expression Language）即统一表达式语言，activiti支持两个UEL表达式：UEL-value和UEL-method。<br/>1.使用uel-value,设置处理人表达式${assignee},然后在启动时动态在流程变量中设置assignee的值，processEngine.getRuntimeService().setVariable(executionId, "${assignee}",处理人);同时此处可以使用对象，${user.userId}，然后在流程变量中放入user对象即可。<br/>2.使用uel-method，设置节点的执行人为${method.getLoginNameByUserId(userId)}，其中method方法是我们注入到spring中的一个类，userId是我 们设置的全局变量，将method方法注入到activiti的SpringProcessEngineConfiguration的bean中，最后启动一个流程设置全局变量userId作为启动参数。</span></p>'
    //  		+ '</div>';
    // var v = parent.document.getElementById("userField").value;
    // 	if(v.startsWith("表达式:")){
    // 		v = v.substring(4)
    // 		html = '<input type="text" id="expressionId" value="' + v + '">'
    //    		+ '<div class="expressContnt clearfix">'
    //  			+ '<p><span class="iconfont icon-bangzhux">？</span><span class="expresstext">这里可以使用uel表达式，UEL是java EE6规范的一部分，UEL（Unified Expression Language）即统一表达式语言，activiti支持两个UEL表达式：UEL-value和UEL-method。<br/>1.使用uel-value,设置处理人表达式${assignee},然后在启动时动态在流程变量中设置assignee的值，processEngine.getRuntimeService().setVariable(executionId, "${assignee}",处理人);同时此处可以使用对象，${user.userId}，然后在流程变量中放入user对象即可。<br/>2.使用uel-method，设置节点的执行人为${method.getLoginNameByUserId(userId)}，其中method方法是我们注入到spring中的一个类，userId是我 们设置的全局变量，将method方法注入到activiti的SpringProcessEngineConfiguration的bean中，最后启动一个流程设置全局变量userId作为启动参数。</span></p>'
    //  		+ '</div>';
    // 	}
    // $('#expressionBox').html(html);
  } else if (radioType == 'customExpression') {
    debugger
    $('.modal-body').addClass('onlyrow1');
    var t3 = $('#tree3');
    t3 = $.fn.zTree.init(t3, t2_setting, null);
    //显示内容
    $('#expressionBox').css({
      display: "block"
    });
    /*var html = '<input type="text" id="expressionId">'
            + '<div class="expressContnt clearfix">'
              + '<p><span class="iconfont icon-bangzhux">？</span><span class="expresstext">这里可以使用uel表达式，UEL是java EE6规范的一部分，UEL（Unified Expression Language）即统一表达式语言，activiti支持两个UEL表达式：UEL-value和UEL-method。<br/>1.使用uel-value,设置处理人表达式${assignee},然后在启动时动态在流程变量中设置assignee的值，processEngine.getRuntimeService().setVariable(executionId, "${assignee}",处理人);同时此处可以使用对象，${user.userId}，然后在流程变量中放入user对象即可。<br/>2.使用uel-method，设置节点的执行人为${method.getLoginNameByUserId(userId)}，其中method方法是我们注入到spring中的一个类，userId是我 们设置的全局变量，将method方法注入到activiti的SpringProcessEngineConfiguration的bean中，最后启动一个流程设置全局变量userId作为启动参数。</span></p>'
            + '</div>';
      var v = parent.document.getElementById("userField").value;
          if(v.startsWith("表达式:")){
            v = v.substring(4)
            html = '<input type="text" id="expressionId" value="' + v + '">'
                + '<div class="expressContnt clearfix">'
              + '<p><span class="iconfont icon-bangzhux">？</span><span class="expresstext">这里可以使用uel表达式，UEL是java EE6规范的一部分，UEL（Unified Expression Language）即统一表达式语言，activiti支持两个UEL表达式：UEL-value和UEL-method。<br/>1.使用uel-value,设置处理人表达式${assignee},然后在启动时动态在流程变量中设置assignee的值，processEngine.getRuntimeService().setVariable(executionId, "${assignee}",处理人);同时此处可以使用对象，${user.userId}，然后在流程变量中放入user对象即可。<br/>2.使用uel-method，设置节点的执行人为${method.getLoginNameByUserId(userId)}，其中method方法是我们注入到spring中的一个类，userId是我 们设置的全局变量，将method方法注入到activiti的SpringProcessEngineConfiguration的bean中，最后启动一个流程设置全局变量userId作为启动参数。</span></p>'
            + '</div>';
          }*/
    var exprData;
    $.ajax({
      url: iframeContextPath + "/assignment/custom-formulas/" + window.parent.processCode + "?access_token=" + window.parent.token,
      dataType: "json",
      type: "GET",
      async: false,
      success: function (data) {
        exprData = data;
      }
    });

    var v = parent.document.getElementById("candidateUsersHidden").value;
    html = '<input id="expressionId" name="exprCombobox">';
    $('#expressionBox').html(html);
    $('#expressionId').combobox({
      valueField: 'value',
      textField: 'text',
      width: 320,
      placeholder: '请选择',
      data: exprData
    });
    if (v.startsWith("表达式:") && (v != "表达式:${initiator}")) {
      v = v.substring(4);
      $('#expressionId').combobox("setValue", v);
    }
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
      url: url,
      dataType: "json",
      type: "GET",
      success: function (data) {
        var zTree;
        //定义切换后的url 如果是用户和角色使用不同的异步展开url
        var asyncUrl = '';
        if (radioType !== "role") {
          asyncUrl = iframeContextPath + "/assignment/dept-node?access_token=" + window.parent.token;
        } /*else if (radioType == "role") {
					asyncUrl = iframeContextPath + "/queryRoleInfosByRoleClassId.json?iconType=1";
				}*/
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
          var t2 = $("#tree2");
          t2 = $.fn.zTree.init(t2, setting, null);
          var t3 = $("#tree3");

          var treeObj2 = $.fn.zTree.getZTreeObj("tree3");
          var tree3Node = treeObj2.getNodes();

          if (tree3Node[0] == undefined) {
            t3 = $.fn.zTree.init(t3, t3_setting, null);
          } else {
            if (tree3Node[0].name == '流程发起人'
              || tree3Node[0].name == '上一节点处理人'
              || tree3Node[0].name.split(":")[0] == '表达式') {
              t3 = $.fn.zTree.init(t3, t3_setting, null);
            } else {
              t3 = $.fn.zTree.init(t3, t3_setting, tree3Node);
            }
          }

        });

        //定义当前的左边树的单击事件

        function zTreeOnClick(treeId, treeNode) {
          var treeObj3 = $.fn.zTree.getZTreeObj("tree3");
          var tree3Node = treeObj3.getNodes();
          var nodes = treeObj3.getNodes();
          if (nodes.length > 0) {
            var str = "";
            for (var node in nodes) {
              str += nodes[node].name + ":"
                + nodes[node].loginName + ",";
            }
            if (str != "") {
              str = str.substring(0, str.length - 1);
            }
            typeList = str;
          } else {
            typeList = null;
          }
          var treeNodeId = treeNode.id;
          var usersUrl = iframeContextPath + "/assignment/users";
          if ("role" == radioType) {
            usersUrl += "/role/" + treeNodeId;
          } else {
            usersUrl += "/dept/" + treeNodeId;
          }
          usersUrl += "?access_token=" + window.parent.token;
          $.ajax({
            url: usersUrl,
            dataType: "json",
            type: "GET",
            success: function (data) {
              var t2_setting = {
                check: {
                  enable: true,
                  chkStyle: "checkbox",
                  chkboxType: {
                    "Y": "s",
                    "N": "s"
                  }
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
                  onClick: zTreeOnClick2
                }
              };
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

              function zTreeOnClick2(event, treeId, treeNode) {
                var zTree = $.fn.zTree.getZTreeObj("tree2");
                zTree.checkNode(treeNode, !treeNode.checked, true);
              }
            }
          });
        };
      }
    });
  }
}

//全部移除
function deleteAll() {
  debugger
  var a = $($("tbody")[1]).find("tr:gt(0)").remove();
  if (a.context == undefined) {
    var t3 = $('#tree3');
    if (!((t3[0].innerText.indexOf("流程发起人") != -1) || (t3[0].innerText
      .indexOf("上一节点处理人") != -1))) {
      t3 = $.fn.zTree.init(t3, t3_setting, null);
    } else {
      var zTree = $.fn.zTree.getZTreeObj("tree3");
      zTree.checkAllNodes(false);
    }
  }
}

//默认选中
function initRadioType() {
  debugger
  var hiddenValue = parent.document.getElementById("candidateUsersHidden").value;
  if (hiddenValue) {
    if (hiddenValue.indexOf("(角色)") == 0) {
      $("input:radio[value='role']").attr('checked', 'true');

    } else if (hiddenValue.indexOf("(部门)") == 0) {
      $("input:radio[value='depart']").attr('checked', 'true');

    } else if (hiddenValue.indexOf("表达式:${initiator}") == 0) {
      //流程发起人
      $("input:radio[value='processSponsor']").attr('checked', 'true');

    } else if (hiddenValue.indexOf("表达式") == 0) {
      var field = parent.document.getElementById("userField").value;
      if (field) {
        if (field.indexOf("任务分配") == 0) {
          //表达式
          $("input:radio[value='expression']")
            .attr('checked', 'true');
        } else {
          $("input:radio[value='customExpression']").attr('checked',
            'true');
        }
      }
    } else if (hiddenValue.indexOf("上一节点处理人") == 0) {
      //上一节点处理人
      $("input:radio[value='dealPeople']").attr('checked', 'true');

    }
  }
  fun_change();

}

$(function () {
  initRadioType();
});