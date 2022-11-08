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
 * dialog
 */
var KisBpmoperationDialogCtrl = [ '$scope', '$modal','$timeout', function($scope, $modal,$timeout) {

    // Config for the modal window
    var opts = {
		backdrop: false,
        keyboard: false,
        template:  'editor-app/configuration/properties/iframe-operation.html?version=' + Date.now(),
        scope: $scope
    };

    // Open the dialog
    $modal(opts);
    
    $timeout(function(){
    	//并串行
    	var multiinstanceType = angular.element(document.querySelector('#oryx-multiinstance_type')).scope().property;
    	var usertaskassignment = angular.element(document.querySelector('#oryx-usertaskassignment')).scope().property;
    	var multiinstanceTypeValue;
    	var usertaskassignmentValue;
    	if(multiinstanceType != undefined){
    		multiinstanceTypeValue = multiinstanceType.value;
    	}
    	if(usertaskassignment != undefined){
    		usertaskassignmentValue = usertaskassignment.value;
    	}
    	
    	//非会签状态下  任务分配选择候选人或候选人组配置了值后  释放任务可配
    	//releaseTaskBox
    	//rollbackBox 回退 非会签状态下显示
    	//completeTaskBox //非会签状态下显示
    	if(multiinstanceTypeValue == "None"){
    		jQuery("#rollbackBox,#completeTaskBox").show();
    		jQuery("#counterBox").hide();
    		if( usertaskassignmentValue != null && usertaskassignmentValue != "" ){
    			if( usertaskassignmentValue.assignment.candidateUsers != "" || usertaskassignmentValue.assignment.candidateGroups != "" ){
	    			jQuery("#releaseTaskBox").show();
	    		}
    		}
    	}

    	//counterBox 加签 减签 会签状态下
    	if(multiinstanceTypeValue == "Parallel" || multiinstanceTypeValue == "Sequential"){
    		jQuery("#counterBox").show();
    		jQuery("#rollbackBox,#completeTaskBox,#releaseTaskBox").hide();
    	}
    	
    	
    },150);
}];

var KisBpmoperationDialogPopupCtrl = [ '$scope', function($scope) {
	
    // Put json representing dialog on scope
    if ($scope.property.value !== undefined && $scope.property.value !== null
        && $scope.property.value.operation !== undefined
        && $scope.property.value.operation !== null) 
    {
        $scope.operation = $scope.property.value.operation;
    } else {
        $scope.operation = {};
    }


    $scope.save = function() {
    	$scope.property.value = {};

    	/*if(document.getElementById('chk1').checked == true){
    	$scope.operation.dialogcontent1 = document.getElementById('chk1').value;
    	}*/
    	var operations=document.getElementsByName("chk"); 
    	var str='';
    	var flag=true;
    	for(var i=0;i<operations.length;i++){
	    	if(operations[i].checked){
	    		//如果是回退到指定步骤的话  判断是否内容为空 空的话 flag=false 然后不能保存 并且提示输入框不能为空
	    		if(operations[i].value=="rollbackActivity"){
	    			var inputValue = document.getElementById("rollbackactivityValue").value;
	    			if(inputValue==''||inputValue=='请输入流程节点的id，并以冒号分隔'){
	    				flag=false;
	    				alert("文本框不能为空");
	    			}else{
	    				str += document.getElementById('chk'+(i+1)).value+"["+inputValue+"]"+","; 
			    		
	    			}
	    		}else{
	    			str += document.getElementById('chk'+(i+1)).value+","; 
		    		
	    		}
	    		
	    		
	    	}
    	} 
    	if(flag){
    		if(str !=''){
    			str = str.substring(0,str.length-1);
    			$scope.operation.dialogcontent0 = str;
    			$scope.property.value.operation = $scope.operation;
    			var strings = str.split(",");
    			for (var i = 0; i < strings.length; i++) {
    				if ("submitTask" === strings[i]) {
    					delete $scope.property.value.operation.submitTaskWithPerson;
    			        delete $scope.property.value.operation.submitTaskWithPresetPerson;
    			    } else if ("submitTaskWithPresetPerson" === strings[i]) {
    			        delete $scope.property.value.operation.submitTaskWithPerson;
    			        delete $scope.property.value.operation.submitTask;
    			    } else {
    			        delete $scope.property.value.operation.submitTaskWithPresetPerson;
    			        delete $scope.property.value.operation.submitTask;
    			    }
    		    }
    		}else {
    			$scope.property.value="";
    		}
    		
        	

        	$scope.updatePropertyInModel($scope.property);
        	$scope.close(); 
    	}
    	

    };

    // Close button handler
    $scope.close = function() {
    	$scope.property.mode = 'read';
    	$scope.$hide();
    };
    
    
    
    
   
}];