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

/*
 * Execution listeners
 */

var KisBpmMultiInstanceCtrl = [ '$scope', function($scope) {
	var html = '<div id="opacity" class="opacity"></div>';
	var value = null;
	var collectionValue = 'assigneeList';
	var variableValue = 'assignee';
	var usertaskassigneeValue = '${assignee}';
	var condition = angular.element(document.querySelector('#oryx-multiinstance_condition')).scope().property;
	var conditionHidden = angular.element(document.querySelector('#oryx-multiinstance_condition_hidden')).scope().property;
	var collection = angular.element(document.querySelector('#oryx-multiinstance_collection')).scope().property;
	var variable = angular.element(document.querySelector('#oryx-multiinstance_variable')).scope().property;
	var executionlisteners = angular.element(document.querySelector('#oryx-executionlisteners')).scope().property;
	if(!(undefined==angular.element(document.querySelector('#oryx-nodetactics')).scope())){
		var nodetactics = angular.element(document.querySelector('#oryx-nodetactics')).scope().property;
	}
	//操作配置
	var operations;
	if(jQuery('#oryx-operations').length > 0){
		operations = angular.element(document.querySelector('#oryx-operations')).scope().property;
		operations.value = {};
	}
	
	//任务分配
	var usertaskassignment;
	if(jQuery('#oryx-usertaskassignment').length > 0){
		usertaskassignment = angular.element(document.querySelector('#oryx-usertaskassignment')).scope().property;
		usertaskassignment.value = {};
	}
	executionlisteners.value = {};
	
	
    if ($scope.property.value == undefined && $scope.property.value == null)
    {
    	$scope.property.value = 'None';
    }
    
        
    $scope.multiInstanceChanged = function() {
    	debugger;
    	//改变策略状态，如果是会签的话不能选择节点策略
    	if ("None" != $scope.property.value && undefined != nodetactics) {
    		if (nodetactics != "" || nodetactics.nodetacticNumber != 0) {
    			nodetactics.value.autoCommitIfNullAssigneement = false;
    			nodetactics.value.autoCommitIfSameAssigneement = false;
    			nodetactics.value.nonStopCommitAfterRollBack = false;
    			nodetactics.value.dialogcontent0 = "";
    			nodetactics.value.nodetacticNumber = 0;
    		}
    		
    	}
    	
    	if ($scope.property.value == "Parallel") {
    		condition.value = '${voteHolder.isComplete(execution)}';
    		collection.value = collectionValue;
    		variable.value = variableValue;
    		//voteType:投票形式   1绝对票数 2百分比                            num：票数                   votingSystem:表决制 1无 2少数服从多数 3多数服从少数      
    		//endVote:表决人数达到多数后，是否结束会签 1是2否              integratedTicketing：一票制 1无 2一票同意 3 一票否决               
    		conditionHidden.value={};
    		conditionHidden.value.vote={};
    		conditionHidden.value.vote={
    			voteType : '2',
    			num : '100',
    			votingSystem : '2',
    			endVote : '2',
    			integratedTicketing : '1'
    			
    		};
    		executionlisteners.value.executionListeners = [{event : 'start',
	            implementation : '',
	            className : 'com.cesgroup.bpm.listener.CounterSignListener',
	            expression: '',
	            delegateExpression: ''}];
    		if(jQuery('#oryx-usertaskassignment').length > 0){
    			usertaskassignment.value.assignment = {assignee:usertaskassigneeValue,assigneeHidden:usertaskassigneeValue};
    		}
    	} else if($scope.property.value == "Sequential"){
    	      collection.value = collectionValue;
    	      variable.value = variableValue;
    	      
    	      executionlisteners.value.executionListeners = [{event : 'start',
    	             implementation : '',
    	             className : 'com.cesgroup.bpm.listener.CounterSignListener',
    	             expression: '',
    	             delegateExpression: ''}];
    	      if(jQuery('#oryx-usertaskassignment').length > 0){
    	       usertaskassignment.value.assignment = {assignee:usertaskassigneeValue,assigneeHidden:usertaskassigneeValue};
    	      }
    	      condition.value = value;
    	      
    	      conditionHidden.value = value;
    	      
    	      
    	      if(jQuery('#oryx-multiinstance_condition').length > 0){
    	       jQuery('#oryx-multiinstance_condition').children('.errorText').remove();
    	      }
    	}
    	else{
    		condition.value = value;
    		collection.value = value;
    		variable.value = value;
    		conditionHidden.value = value;
    		executionlisteners.value = value;
    		if(jQuery('#oryx-usertaskassignment').length > 0){
    			usertaskassignment.value = value;
    		}
    		if(jQuery('#oryx-multiinstance_condition').length > 0){
    			jQuery('#oryx-multiinstance_condition').children('.errorText').remove();
    		}
    	}
    	
    	if($scope.property.value == "Sequential"){
    		//完成条件（多实例），集合（多实例）元素变量（多实例）一开始置灰
    		//完成条件灰空值  集合灰 有值 元素多实例灰有值
    		jQuery('#oryx-multiinstance_condition,#oryx-multiinstance_collection,#oryx-multiinstance_variable,#oryx-multiinstance_condition_hidden').children('#opacity').remove();
    		jQuery('#oryx-multiinstance_condition,#oryx-multiinstance_collection,#oryx-multiinstance_variable,#oryx-multiinstance_condition_hidden').prepend(html);    		
    	}
    	else if($scope.property.value == "Parallel"){
    		jQuery('#oryx-multiinstance_condition,#oryx-multiinstance_collection,#oryx-multiinstance_variable,#oryx-multiinstance_condition_hidden').children('#opacity').remove();
    		//集合 有值 灰 元素多实例 灰 有值
    		jQuery('#oryx-multiinstance_collection,#oryx-multiinstance_variable').prepend(html);
    	}else{
    		jQuery('#oryx-multiinstance_condition,#oryx-multiinstance_collection,#oryx-multiinstance_variable,#oryx-multiinstance_condition_hidden').children('#opacity').remove();
    		//完成条件灰空 集合灰空 元素多实例灰空
    		jQuery('#oryx-multiinstance_condition,#oryx-multiinstance_collection,#oryx-multiinstance_variable,#oryx-multiinstance_condition_hidden').prepend(html);
    	}
    	
    	
    	$scope.updatePropertyInModel($scope.property);
    	$scope.updatePropertyInModel(condition);
    	$scope.updatePropertyInModel(collection);
    	$scope.updatePropertyInModel(conditionHidden);
    	$scope.updatePropertyInModel(variable);
    	$scope.updatePropertyInModel(executionlisteners);    	
    	$scope.updatePropertyInModel(nodetactics);  
    	if(undefined !=nodetactics){
    	    $scope.updatePropertyInModel(nodetactics);     
        }
    	if(jQuery('#oryx-operations').length > 0){
    		operations.value.operation= {dialogcontent0 :"submitTask"};
    		$scope.updatePropertyInModel(operations);
    	}
    	if(jQuery('#oryx-usertaskassignment').length > 0){
    		$scope.updatePropertyInModel(usertaskassignment);
    	}
    };
}];

var KisBpmMultiInstanceConditionCtrl=[ '$scope', function($scope) {
	if ($scope.property.value == undefined && $scope.property.value == null)
    {
    	$scope.property.value = 'None';
    }
        
    $scope.multiInstanceChanged = function() {
    	$scope.updatePropertyInModel($scope.property);
    };
}];


//调用元素
var KisBpmMultiInstanceCallCtrl=[ '$scope', '$http','$timeout', function($scope,$http,$timeout) {
	//var selectValue = $scope.property.value.name;
	
	//【结构】-【调用活动】-【调用元素】数据
	var url=location.href.substring(0,location.href.indexOf("widgets"));
	$http({ method: 'POST',
        ignoreErrors: true,
        headers: {'Accept': 'application/json',
                  'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
        transformRequest: function (obj) {
            var str = [];
            for (var p in obj) {
                str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
            }
            return str.join("&");
        },
        	url: url+"/ces-delegate-manage/process-modeler-load"
        })

        .success(function (data, status, headers, config) {
            $scope.property.valueHidden = data;
           /* $timeout(function(){
        		$scope.selected = selectValue;
        	},200);*/
        })
        .error(function (data, status, headers, config) {
            alert("error")
        });
	
	
	
	if ($scope.property.value == undefined && $scope.property.value == null)
    {
    	$scope.property.value = 'None';
    }
        
    $scope.multiInstanceChanged = function() {
    	$scope.property.value = $scope.property.valueHidden.code;
    	$scope.updatePropertyInModel($scope.property);
    };
    
}];