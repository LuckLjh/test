/*
 * Activiti Modeler component part of the Activiti project
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
'use strict';

var KISBPM = KISBPM || {};
KISBPM.TOOLBAR = {
  ACTIONS: {

    saveModel: function (services) {
      //首先获取编辑器里面的数据
      var json = services.$scope.editor.getJSON();
      //如果节点配置了人员的情况下 是可以通过的，如果有节点是没有配置为false
      var userFlag = true;
      //默认的情况下 没有配置开始节点，如果有配置开始节点，设置为true
      var startFlag = false;
      //默认的情况下 没有配置结束节点，如果有配置结束节点，设置为true
      var endFlag = false;
      //当是人工节点的时候，去判断是不是会签类型，如果是的话，那么判断候选人和候选人组有没有值，没有值的话就直接alert不能保存


      var ArrayId = [];
      var nodeMaps = {};
      for (var i = 0; i < json.childShapes.size(); i++) {
        var childshape = json.childShapes[i];
        var props = childshape.properties;
        nodeMaps[childshape.resourceId] = childshape;
        //overrideid id不能为空
        if (props.overrideid == undefined) {
          //continue;
        } else {
          //stencil
          if (props.overrideid != "") {
            ArrayId.push(props.overrideid);
          }
        }
      }


      var res = [];
      ArrayId.sort();
      for (var i = 0; i < ArrayId.length;) {
        var count = 0;
        for (var j = i; j < ArrayId.length; j++) {
          if (ArrayId[i] == ArrayId[j]) {
            count++;
          }
        }
        res.push([ArrayId[i], count]);
        i += count;

      }
      //res 二维数维中保存了 值和值的重复数
      var html = '';
      for (var i = 0; i < res.length; i++) {
        if (res[i][1] > 1) {
          html += '<p>ID：' + res[i][0] + '，重复次数：' + (res[i][1] - 1) + '</p>';
        }
      }
      if (html != '') {
        var repeatmodal = '<div class="modal" style="display:block;">'
          + '<div class="modal-dialog"><div class="modal-content">'
          + '<div class="modal-header"><h2>提示</h2></div>'
          + '<div class="modal-body"><h2>ID不能重复！</h2>' + html + '</div>'
          + '<div class="modal-footer"><button class="btn btn-primary" onclick="repeatmodalRemove(this)">确定</button></div>'
          + '</div></div>'
          + '</div>';
        jQuery('body').append(repeatmodal);
        return;
      }

      //判断同时设置默认流与流条件
      for (var i = 0; i < json.childShapes.size(); i++) {
        var childshape = json.childShapes[i].properties;
        if (childshape.defaultflow != undefined && childshape.defaultflow != "") {
          if (childshape.defaultflow == "true" || childshape.defaultflow == true) {
            if (childshape.conditionsequenceflow != "") {
              alert("设置默认流时流条件必须为空！");
              return;
            }
          }
        }
      }

      //验证子流程节点为调用活动时，需要设置调用元素callactivitycalledelement
      for (var i = 0; i < json.childShapes.size(); i++) {
        var childshape = json.childShapes[i].properties;
        if (childshape.callactivitycalledelement != undefined) {
          if (childshape.callactivitycalledelement == "" || childshape.callactivitycalledelement == null) {
            alert("调用活动必须设置调用元素！");
            return;
          }
        }
      }

      for (var i = 0; i < json.childShapes.size(); i++) {
        var childshape = json.childShapes[i].properties;

        //完成条件的值不能为空 multiinstance_condition
        if (childshape.multiinstance_type != undefined && childshape.multiinstance_type == "Parallel") {
          if (childshape.multiinstance_condition == null) {
            alert("完成条件(多实例)的值不能为空！");
            return;
          }
        }


        if (childshape.usertaskassignment == undefined) {
          //continue;
        } else {
          //判断有没有配人 两种情况：1是没有配置过人 第一个if就可以判断  2 如果是曾经配置过人，然后再删除掉，其实是保留了assignee对象，所以使用第二个if去判断
          if (childshape.usertaskassignment == '') {
            userFlag = false;
          }
          if (childshape.usertaskassignment.assignment != undefined) {
            if (childshape.usertaskassignment.assignment.assignee == '' && childshape.usertaskassignment.assignment.candidateGroups == '' && childshape.usertaskassignment.assignment.candidateUsers == '') {
              userFlag = false;
            }
          }

        }
      }

      //判断是否有开始节点
      //记录节点的个数
      var startNodeNum = 0;
      var startNode;
      for (var i = 0; i < json.childShapes.size(); i++) {
        if (json.childShapes[i].stencil.id == 'StartNoneEvent') {
          startNode = json.childShapes[i];
          if (startFlag) {
            startNodeNum++;
            continue;
          }
          startFlag = true;
          startNodeNum++;
        }
      }

      if (startNodeNum > 1) {
        alert("只允许有一个开始事件！");
        return;
      }

      //判断是否有结束节点
      for (var i = 0; i < json.childShapes.size(); i++) {
        if (json.childShapes[i].stencil.id == 'EndNoneEvent') {
          if (endFlag) {
            continue;
          }
          endFlag = true;
        }
      }

      //当是人工节点的时候，去判断是不是会签类型，如果是的话，那么判断候选人和候选人组有没有值，没有值的话就直接alert不能保存
      var userGroupValue = true;
      for (var i = 0; i < json.childShapes.size(); i++) {
        if (json.childShapes[i].stencil.id == 'UserTask') {
          if (json.childShapes[i].properties.multiinstance_type == "Parallel" || json.childShapes[i].properties.multiinstance_type == "Sequential") {
            if ((json.childShapes[i].properties.usertaskassignment.assignment.candidateGroupsHidden == undefined || json.childShapes[i].properties.usertaskassignment.assignment.candidateGroupsHidden == "") && (json.childShapes[i].properties.usertaskassignment.assignment.candidateUsersHidden == undefined || json.childShapes[i].properties.usertaskassignment.assignment.candidateUsersHidden == "")) {
              userGroupValue = false;
              break;
            }
          }
        }
      }

      if (userFlag == false) {
        alert("人工节点必须配置人员!")
        return;
      }
      if (startFlag == false) {
        alert("必须配置开始节点");
        return;
      }
      if (endFlag == false) {
        alert("必须配置结束节点");
        return;
      }
      if (userGroupValue == false) {
        alert("会签节点必须配置候选人或者候选人组");
        return;
      }

      //验证流程事件是否完整连接
      for (var i = 0; i < json.childShapes.size(); i++) {
        var childshape = json.childShapes[i];
        if (childshape.stencil.id != 'EndNoneEvent') {
          if (childshape.outgoing == undefined || childshape.outgoing.length == 0) {
            alert("流程事件未完整链接！");
            return;
          }
        }
      }

      if (startNode.outgoing.length > 1) {
        alert("开始节点不支持分支！");
        return;
      }

      var firstFlowId = startNode.outgoing[0].resourceId;
      var firstNodeId = nodeMaps[firstFlowId].outgoing[0].resourceId;
      var firstNode = nodeMaps[firstNodeId];
      if (firstNode.stencil.id != 'UserTask') {
        alert("开始节点后的第一个节点必须是【人工任务】节点");
        return;
      }
      if (firstNode.properties.usertaskassignment && firstNode.properties.usertaskassignment.assignment
        && firstNode.properties.usertaskassignment.assignment.assignee != '表达式:${initiator}') {
        alert("开始节点后的第一个节点的任务分配必须指定：【办理人】为【流程发起人】");
        return;
      }

      //保存当前版本
      var $scope = services.$scope;
      var modelMetaData = $scope.editor.getModelMetaData();

      var json = services.$scope.editor.getJSON();
      json = JSON.stringify(json);

      var json = $scope.editor.getJSON();
      json = JSON.stringify(json);


      var params = {
        xmlJson: json,
        name: modelMetaData.name,
        version: modelMetaData.version,
        description: modelMetaData.description
      };

      // Update
      services.$http({
        method: 'POST',
        data: params,
        ignoreErrors: true,
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        transformRequest: function (obj) {
          var str = [];
          for (var p in obj) {
            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
          }
          return str.join("&");
        },
        url: KISBPM.URL.putModel(modelMetaData.modelId, modelMetaData.bpmModelId)
      })

        .success(function (data, status, headers, config) {
          if (data.success) {
            $scope.editor.handleEvents({
              type: ORYX.CONFIG.EVENT_SAVED
            });
            if (typeof window.opener.reloadVersionGrid == "function") {
              window.opener.reloadVersionGrid();
            }
            if (typeof window.parent.reloadVersionGrid == "function") {
              window.parent.reloadVersionGrid();
            }

            if (data.data && data.data.id != modelId) {
              window.location.href = "modeler.html?modelId=" + data.data.id + "&access_token=" + token;
              jQuery('body').append('<div id="saveSuccess" class="saveAnimation">保存成功</div>');
              setTimeout(function () {
                jQuery("#saveSuccess").remove();
              }, 1000);
            } else {
              jQuery('body').append('<div id="saveSuccess" class="saveAnimation">保存成功</div>');
              setTimeout(function () {
                jQuery("#saveSuccess").remove();
              }, 1000);
            }
          } else {
            jQuery('body').append('<div id="saveFail" class="saveAnimation">' + data.info + '</div>');
            setTimeout(function () {
              jQuery("#saveFail").remove();
            }, 2000);
          }

        })
        .error(function (data, status, headers, config) {
          $scope.error = {};
          console.log('Something went wrong when updating the process model:' + JSON.stringify(data));
//                $scope.status.loading = false;
        });

    },

    saveNewModel: function (services) {
      //保存并新增
      var modal = services.$modal({
        backdrop: false,
        keyboard: false,
        template: 'editor-app/popups/save-model.html?__t=' + Date.now(),
        scope: services.$scope
      });
    },

    exportXmlForm: function (services) {
      debugger;
      var json = angular.copy(services.$scope.editor.getJSON());
      json = JSON.stringify(json);
      /*var params = {
          jsonXml: json
            };*/

      var exportForm = document.createElement("form");
      //exportForm.innerHTML = "<form name='exportForm' action='123.jsp' method='post' target='_blank'><input type='hidden' name='jsonXml' value='123'/></form>";
      exportForm.id = "exportForm";
      exportForm.name = "exportForm";
      exportForm.action = KISBPM.URL.exportModelXml();
      exportForm.target = "_blank";
      exportForm.method = "post";
      //exportForm.acceptCharset = document.charset;

      var jsonInput = document.createElement("input");
      jsonInput.type = "hidden";
      jsonInput.name = "xmlJson";
      jsonInput.value = json;
      exportForm.appendChild(jsonInput);

      /*var modelIdInput = document.createElement("input");
      modelIdInput.type = "hidden";
      modelIdInput.name = "modelId";
      modelIdInput.value = EDITOR.UTIL.getParameterByName('modelId');
      exportForm.appendChild(modelIdInput);

      var tokenInput = document.createElement("input");
      tokenInput.type = "hidden";
      tokenInput.name = "access_token";
      tokenInput.value = EDITOR.UTIL.getParameterByName('access_token');
      exportForm.appendChild(tokenInput);*/
      console.log("document.charset=" + document.charset);
      //document.charset = "utf-8";
      document.body.appendChild(exportForm);

      exportForm.submit();

      return;
    },

    importXmlForm: function (services) {
      var modal = services.$modal({
        backdrop: false,
        keyboard: false,
        template: 'editor-app/popups/importModelXml.html?__t=' + Date.now(),
        scope: services.$scope
      });

    },

    undo: function (services) {

      // Get the last commands
      var lastCommands = services.$scope.undoStack.pop();

      if (lastCommands) {
        // Add the commands to the redo stack
        services.$scope.redoStack.push(lastCommands);

        // Force refresh of selection, might be that the undo command
        // impacts properties in the selected item
        if (services.$rootScope && services.$rootScope.forceSelectionRefresh) {
          services.$rootScope.forceSelectionRefresh = true;
        }

        // Rollback every command
        for (var i = lastCommands.length - 1; i >= 0; --i) {
          lastCommands[i].rollback();
        }

        // Update and refresh the canvas
        services.$scope.editor.handleEvents({
          type: ORYX.CONFIG.EVENT_UNDO_ROLLBACK,
          commands: lastCommands
        });

        // Update
        services.$scope.editor.getCanvas().update();
        services.$scope.editor.updateSelection();
      }

      var toggleUndo = false;
      if (services.$scope.undoStack.length == 0) {
        toggleUndo = true;
      }

      var toggleRedo = false;
      if (services.$scope.redoStack.length > 0) {
        toggleRedo = true;
      }

      if (toggleUndo || toggleRedo) {
        for (var i = 0; i < services.$scope.items.length; i++) {
          var item = services.$scope.items[i];
          if (toggleUndo && item.action === 'KISBPM.TOOLBAR.ACTIONS.undo') {
            services.$scope.safeApply(function () {
              item.enabled = false;
            });
          } else if (toggleRedo && item.action === 'KISBPM.TOOLBAR.ACTIONS.redo') {
            services.$scope.safeApply(function () {
              item.enabled = true;
            });
          }
        }
      }
    },

    redo: function (services) {

      // Get the last commands from the redo stack
      var lastCommands = services.$scope.redoStack.pop();

      if (lastCommands) {
        // Add this commands to the undo stack
        services.$scope.undoStack.push(lastCommands);

        // Force refresh of selection, might be that the redo command
        // impacts properties in the selected item
        if (services.$rootScope && services.$rootScope.forceSelectionRefresh) {
          services.$rootScope.forceSelectionRefresh = true;
        }

        // Execute those commands
        lastCommands.each(function (command) {
          command.execute();
        });

        // Update and refresh the canvas
        services.$scope.editor.handleEvents({
          type: ORYX.CONFIG.EVENT_UNDO_EXECUTE,
          commands: lastCommands
        });

        // Update
        services.$scope.editor.getCanvas().update();
        services.$scope.editor.updateSelection();
      }

      var toggleUndo = false;
      if (services.$scope.undoStack.length > 0) {
        toggleUndo = true;
      }

      var toggleRedo = false;
      if (services.$scope.redoStack.length == 0) {
        toggleRedo = true;
      }

      if (toggleUndo || toggleRedo) {
        for (var i = 0; i < services.$scope.items.length; i++) {
          var item = services.$scope.items[i];
          if (toggleUndo && item.action === 'KISBPM.TOOLBAR.ACTIONS.undo') {
            services.$scope.safeApply(function () {
              item.enabled = true;
            });
          } else if (toggleRedo && item.action === 'KISBPM.TOOLBAR.ACTIONS.redo') {
            services.$scope.safeApply(function () {
              item.enabled = false;
            });
          }
        }
      }
    },

    cut: function (services) {
      KISBPM.TOOLBAR.ACTIONS._getOryxEditPlugin(services.$scope).editCut();
      for (var i = 0; i < services.$scope.items.length; i++) {
        var item = services.$scope.items[i];
        if (item.action === 'KISBPM.TOOLBAR.ACTIONS.paste') {
          services.$scope.safeApply(function () {
            item.enabled = true;
          });
        }
      }
    },

    copy: function (services) {
      KISBPM.TOOLBAR.ACTIONS._getOryxEditPlugin(services.$scope).editCopy();
      for (var i = 0; i < services.$scope.items.length; i++) {
        var item = services.$scope.items[i];
        if (item.action === 'KISBPM.TOOLBAR.ACTIONS.paste') {
          services.$scope.safeApply(function () {
            item.enabled = true;
          });
        }
      }
    },

    paste: function (services) {
      KISBPM.TOOLBAR.ACTIONS._getOryxEditPlugin(services.$scope).editPaste();
    },

    deleteItem: function (services) {
      KISBPM.TOOLBAR.ACTIONS._getOryxEditPlugin(services.$scope).editDelete();
    },

    addBendPoint: function (services) {

      var dockerPlugin = KISBPM.TOOLBAR.ACTIONS._getOryxDockerPlugin(services.$scope);

      var enableAdd = !dockerPlugin.enabledAdd();
      dockerPlugin.setEnableAdd(enableAdd);
      if (enableAdd) {
        dockerPlugin.setEnableRemove(false);
        document.body.style.cursor = 'pointer';
      } else {
        document.body.style.cursor = 'default';
      }
    },

    removeBendPoint: function (services) {

      var dockerPlugin = KISBPM.TOOLBAR.ACTIONS._getOryxDockerPlugin(services.$scope);

      var enableRemove = !dockerPlugin.enabledRemove();
      dockerPlugin.setEnableRemove(enableRemove);
      if (enableRemove) {
        dockerPlugin.setEnableAdd(false);
        document.body.style.cursor = 'pointer';
      } else {
        document.body.style.cursor = 'default';
      }
    },

    /**
     * Helper method: fetches the Oryx Edit plugin from the provided scope,
     * if not on the scope, it is created and put on the scope for further use.
     *
     * It's important to reuse the same EditPlugin while the same scope is active,
     * as the clipboard is stored for the whole lifetime of the scope.
     */
    _getOryxEditPlugin: function ($scope) {
      if ($scope.oryxEditPlugin === undefined || $scope.oryxEditPlugin === null) {
        $scope.oryxEditPlugin = new ORYX.Plugins.Edit($scope.editor);
      }
      return $scope.oryxEditPlugin;
    },

    zoomIn: function (services) {
      KISBPM.TOOLBAR.ACTIONS._getOryxViewPlugin(services.$scope).zoom([1.0 + ORYX.CONFIG.ZOOM_OFFSET]);
    },

    zoomOut: function (services) {
      KISBPM.TOOLBAR.ACTIONS._getOryxViewPlugin(services.$scope).zoom([1.0 - ORYX.CONFIG.ZOOM_OFFSET]);
    },

    zoomActual: function (services) {
      KISBPM.TOOLBAR.ACTIONS._getOryxViewPlugin(services.$scope).setAFixZoomLevel(1);
    },

    zoomFit: function (services) {
      KISBPM.TOOLBAR.ACTIONS._getOryxViewPlugin(services.$scope).zoomFitToModel();
    },

    alignVertical: function (services) {
      KISBPM.TOOLBAR.ACTIONS._getOryxArrangmentPlugin(services.$scope).alignShapes([ORYX.CONFIG.EDITOR_ALIGN_MIDDLE]);
    },

    alignHorizontal: function (services) {
      KISBPM.TOOLBAR.ACTIONS._getOryxArrangmentPlugin(services.$scope).alignShapes([ORYX.CONFIG.EDITOR_ALIGN_CENTER]);
    },

    sameSize: function (services) {
      KISBPM.TOOLBAR.ACTIONS._getOryxArrangmentPlugin(services.$scope).alignShapes([ORYX.CONFIG.EDITOR_ALIGN_SIZE]);
    },

    closeEditor: function (services) {
      //window.location.href = "./index.html";
      //关闭当前窗口，并刷新modelerGrid
      window.opener.refreshModelerGrid();
      window.close();
    },

    /**
     * Helper method: fetches the Oryx View plugin from the provided scope,
     * if not on the scope, it is created and put on the scope for further use.
     */
    _getOryxViewPlugin: function ($scope) {
      if ($scope.oryxViewPlugin === undefined || $scope.oryxViewPlugin === null) {
        $scope.oryxViewPlugin = new ORYX.Plugins.View($scope.editor);
      }
      return $scope.oryxViewPlugin;
    },

    _getOryxArrangmentPlugin: function ($scope) {
      if ($scope.oryxArrangmentPlugin === undefined || $scope.oryxArrangmentPlugin === null) {
        $scope.oryxArrangmentPlugin = new ORYX.Plugins.Arrangement($scope.editor);
      }
      return $scope.oryxArrangmentPlugin;
    },

    _getOryxDockerPlugin: function ($scope) {
      if ($scope.oryxDockerPlugin === undefined || $scope.oryxDockerPlugin === null) {
        $scope.oryxDockerPlugin = new ORYX.Plugins.AddDocker($scope.editor);
      }
      return $scope.oryxDockerPlugin;
    }
  }
};

/** Custom controller for the save dialog */
var SaveModelCtrl = ['$rootScope', '$scope', '$http', '$route', '$location',
  function ($rootScope, $scope, $http, $route, $location) {

    var modelMetaData = $scope.editor.getModelMetaData();

    var description = '';
    if (modelMetaData.description) {
      description = modelMetaData.description;
    }

    var saveDialog = {
      'name': modelMetaData.name,
      'version': modelMetaData.revision,
      'description': description
    };

    $scope.saveDialog = saveDialog;

    var json = $scope.editor.getJSON();
    json = JSON.stringify(json);

    var params = {
      modeltype: modelMetaData.model.modelType,
      json_xml: json,
      name: 'model'
    };

    $scope.status = {
      loading: false
    };

    $scope.close = function () {
      $scope.$hide();
    };

    $scope.saveAndClose = function () {
      $scope.save(function () {
        //window.opener.refreshGrid();
        //刷新配置平台工作流版本列表
        //    		window.opener.that.reloadVersionGrid();
        //    		window.opener.that.refreshCurrentNode();

        setTimeout("window.close();", 500);

        //window.close();
        /*window.location.href = "./index.html";*/
      });
    };
    $scope.saveNew = function () {
      $scope.save(function (data) {
        console.log(data);
        //    		$scope.$hide();
        //window.close();
        /*window.location.href = "./index.html";*/
      });
    };

    $scope.save = function (successCallback) {

      if (!$scope.saveDialog.name || $scope.saveDialog.name.length == 0) {
        return;
      }

      // Indicator spinner image
      $scope.status = {
        loading: true
      };

      modelMetaData.name = $scope.saveDialog.name;
      modelMetaData.revision = $scope.saveDialog.version;
      modelMetaData.description = $scope.saveDialog.description;

      //校验版本号


      var json = $scope.editor.getJSON();
      json = JSON.stringify(json);

      var selection = $scope.editor.getSelection();
      $scope.editor.setSelection([]);

      // Get the serialized svg image source
      var svgClone = $scope.editor.getCanvas().getSVGRepresentation(true);
      $scope.editor.setSelection(selection);
      if ($scope.editor.getCanvas().properties["oryx-showstripableelements"] === false) {
        var stripOutArray = jQuery(svgClone).find(".stripable-element");
        for (var i = stripOutArray.length - 1; i >= 0; i--) {
          stripOutArray[i].remove();
        }
      }

      // Remove all forced stripable elements
      var stripOutArray = jQuery(svgClone).find(".stripable-element-force");
      for (var i = stripOutArray.length - 1; i >= 0; i--) {
        stripOutArray[i].remove();
      }

      // Parse dom to string
      var svgDOM = DataManager.serialize(svgClone);

      var params = {
        json_xml: json,
        svg_xml: svgDOM,
        name: $scope.saveDialog.name,
        version: $scope.saveDialog.version,
        description: $scope.saveDialog.description
      };

      $http.get(KISBPM.URL.checkUnique(modelMetaData.workflowId, modelMetaData.revision))
        .success(function (data) {
          if (data.success) {
            // 校验不重复
            $http({
              method: 'POST',
              data: params,
              ignoreErrors: true,
              headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
              },
              transformRequest: function (obj) {
                var str = [];
                for (var p in obj) {
                  str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
                }
                return str.join("&");
              },
              url: KISBPM.URL.putNewModel(modelMetaData.modelId, modelMetaData.bpmModelId)
            })

              .success(function (data, status, headers, config) {
                $scope.editor.handleEvents({
                  type: ORYX.CONFIG.EVENT_SAVED
                });
                $scope.modelData.name = $scope.saveDialog.name;
                $scope.modelData.version = $scope.saveDialog.version;
                $scope.modelData.lastUpdated = data.lastUpdated;

                $scope.status.loading = false;
                $scope.$hide();

                // Fire event to all who is listening
                var saveEvent = {
                  type: KISBPM.eventBus.EVENT_TYPE_MODEL_SAVED,
                  model: params,
                  modelId: modelMetaData.modelId,
                  eventType: 'update-model'
                };
                KISBPM.eventBus.dispatch(KISBPM.eventBus.EVENT_TYPE_MODEL_SAVED, saveEvent);

                // Reset state
                $scope.error = undefined;
                $scope.status.loading = false;

                // Execute any callback
                if (typeof window.opener.reloadVersionGrid == "function") {
                  window.opener.reloadVersionGrid();
                  window.opener.refreshCurrentNode();
                } else {
                  window.opener.CfgWorkflowDefine.reloadVersionGrid();
                  window.opener.CfgWorkflowDefine.reloadCurrentNode();
                }
                if (successCallback) {
                  successCallback(data);
                }
                window.location.href = "modeler.html?modelId=" + data.workflowEngineId;
              })
              .error(function (data, status, headers, config) {
                $scope.error = {};
                console.log('Something went wrong when updating the process model:' + JSON.stringify(data));
                $scope.status.loading = false;
              });
          } else {
            // 校验重复
            console.log('Something went wrong when updating the process model:' + JSON.stringify(data));
            $scope.status.loading = false;
            $scope.saveDialog.versionExist = "版本号已存在";
          }
        }).error(function (data) {
        $scope.error = {};
        console.log('Something went wrong when updating the process model:' + JSON.stringify(data));
        $scope.status.loading = false;
      });

    };

  }];
activitiModeler.controller('importModelXmlCtrl', ['$scope', '$http', '$timeout', '$compile', 'Upload',
  function ($scope, $http, $timeout, $compile, Upload) {
    var modelMetaData = $scope.editor.getModelMetaData();

    var description = '';
    if (modelMetaData.description) {
      description = modelMetaData.description;
    }
    var filename = '';
    if (modelMetaData.filename) {
      filename = modelMetaData.filename;
    }

    var saveDialog = {
      'description': description,
      'filename': filename
    };

    $scope.saveDialog = saveDialog;

    var json = $scope.editor.getJSON();
    json = JSON.stringify(json);

    var params = {
      modeltype: modelMetaData.model.modelType,
      json_xml: json,
      name: 'model'
    };

    $scope.status = {
      loading: false
    };

    $scope.close = function () {
      $scope.$hide();
      //解决ie9下箭头穿透的问题
      jQuery('#main').removeClass('opacity5');
    };

    $scope.deployAndClose = function () {
      $scope.save(function () {
        //window.opener.refreshGrid();
        //更新modelerVersionDialog
        window.opener.refreshModelerGrid();

        //	setTimeout("window.close();",500);
        window.close();
        window.location.href = "./index.html";
        //解决ie9下箭头穿透的问题
        jQuery('#main').removeClass('opacity5');
      });
    };

    $scope.saveAndClose = function () {
      var file = $scope.document.fileInput;
      $scope.uploadFile(file);
      //解决ie9下箭头穿透的问题
      jQuery('#main').removeClass('opacity5');
    };

    $scope.filepathchange = function () {
      document.getElementById('filepath').value = $scope.document.fileInput.name;
    }

    $scope.modifyAndClose = function () {
      $scope.uploadFile();
      //解决ie9下箭头穿透的问题
      jQuery('#main').removeClass('opacity5');
    };

    $scope.uploadFile = function (file) {
      //var url=location.href.substring(0,location.href.indexOf("widgets"));
      //"../../../appmanage/workflow-version!saveModel.json?modelId=" + modelId;
      //寻找第一个&符号
      /*var index1 = location.href.indexOf("&");
      var bpmConfBaseId = "";
      if(index1!=-1){
        bpmConfBaseId = location.href.substring(location.href.indexOf("modelId")+"modelId".length+1, index1);
//
      }else{
        bpmConfBaseId = location.href.substring(location.href.indexOf("modelId")+"modelId".length+1);
      }*/
      var fileName = file.name;

      if (fileName.substring(fileName.lastIndexOf(".") + 1) == "xml") {
        $scope.fileInfo = file;
        Upload.upload({
          //服务端接收
          url: "../workflow-version/model/import?modelId=" + modelId + "&access_token=" + token,
          method: "post",
          //上传的文件
          file: file
        }).success(function (data, status) {
          debugger
          //var ctx = data.data.ctx;
          //var fileName = data.data.fileName;
          var url = "../designer/modeler.html?modelId=" + data.data.id + "&access_token=" + token;
          window.location.href = url;
        }).error(function (data, status) {
          debugger
          console.log("Error ... " + status);
          console.log(data);
        });
      } else {
        alert("只能导入模型编辑器对应的完整xml文件!");
        return;
      }

    }
  }]);