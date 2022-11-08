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
 * Assignment
 */

var assigneeFieldVal = '${assignee}';

var KisBpmAssignmentCtrl = [ '$scope', '$modal','$timeout', function($scope, $modal, $timeout) {
	var url = 'editor-app/configuration/properties/assignment-popup.html?version=' + Date.now();
	var type = angular.element(document.querySelector('#oryx-multiinstance_type')).scope().property;
    // Config for the modal window
    var opts = {
		backdrop: false,
        keyboard: false,
        template:  url,
        scope: $scope
    };

    // Open the dialog
    $modal(opts);
    $timeout(function(){
    	var assigneeFieldProperty = angular.element(document.querySelector('#assigneeField')).scope().property;
    	var assigneeFieldId = jQuery('#assigneeField');
    	if(type.value == 'Parallel' || type.value == 'Sequential'){
    		
    		//jQuery('#firstGutter').hide();
    		assigneeFieldId.val(assigneeFieldVal);
    		//assigneeFieldProperty.value = assigneeFieldVal;
    	}else{
    		if(assigneeFieldProperty.value.assignment==undefined){
    		}else{
    			if(assigneeFieldProperty.value.assignment.assignee=="${assignee}"){
        			assigneeFieldId.val('');
            		//assigneeFieldProperty.value = ''; 
        		}
    		}
    		
    		//jQuery('#firstGutter').show();
    		
    	}
    },100);
}];




var KisBpmAssignmentPopupCtrl = [ '$scope', '$modal','$timeout', function($scope, $modal,$timeout) {
	//解决ie9箭头穿透的问题
	jQuery('#main').addClass('opacity5');
    // Put json representing assignment on scope
    if ($scope.property.value !== undefined && $scope.property.value !== null
        && $scope.property.value.assignment !== undefined
        && $scope.property.value.assignment !== null) 
    {
        $scope.assignment = $scope.property.value.assignment;
    } else {
        $scope.assignment = {};
    }

    /*if ($scope.assignment.candidateUsers == undefined || $scope.assignment.candidateUsers.length == 0)
    {
    	//$scope.assignment.candidateUserss = [{value: ''}];
    	
    	$scope.assignment.candidateUsers = '';
    }
    
    // Click handler for + button after enum value
    var userValueIndex = 1;
    $scope.addcandidateUsersValue = function(index) {
        $scope.assignment.candidateUsers.splice(index + 1, 0, {value: 'value ' + userValueIndex++});
    };

    // Click handler for - button after enum value
    $scope.removecandidateUsersValue = function(index) {
        $scope.assignment.candidateUsers.splice(index, 1);
    };
    
    if ($scope.assignment.candidateGroups == undefined || $scope.assignment.candidateGroups.length == 0)
    {
    	//$scope.assignment.candidateGroupss = [{value: ''}];
    	
    	$scope.assignment.candidateGroups = '';
    }
    
    var groupValueIndex = 1;
    $scope.addcandidateGroupsValue = function(index) {
        $scope.assignment.candidateGroups.splice(index + 1, 0, {value: 'value ' + groupValueIndex++});
    };

    // Click handler for - button after enum value
    $scope.removecandidateGroupsValue = function(index) {
        $scope.assignment.candidateGroups.splice(index, 1);
    };*/
    
    
    //办理人
    $scope.assigneeClick = function() {
    	/*var opts2 = {
	        template:  'editor-app/configuration/properties/iframe-assignee.html',
	        scope: $scope
	    };*/
    	
    	var assigneeFieldValue = document.getElementById('assigneeField').value;
    	
    	var userFieldValue = document.getElementById('userField').value;
    	var groupFieldValue = document.getElementById('groupField').value;
    	
    	if( assigneeFieldValue == assigneeFieldVal){
    		alert("不可选");
    		return;
    	}
    	
    	if(userFieldValue != '' || groupFieldValue != ''){
    		alert("不可选（请先删除“侯选人”和“侯选组”，再配置）");
    		return;
    	}
    	
    	var src = "editor-app/configuration/properties/iframe-assignee.html";
    	var iframe = document.createElement('iframe');  
	    iframe.id = 'iframeTest'; 
	    iframe.scrolling = "no";
	    iframe.src = src; 
		document.body.appendChild(iframe);
    };
    
    
    //候选人
    $scope.candidateUsersClick = function() {
    	var assigneeValue = document.getElementById('assigneeField').value;
    	if( assigneeValue != ''&&assigneeValue!='${assignee}'){
    		alert("不可选（请先删除“办理人”，再配置）");
    		return;
    	}
    	
    	//多实例值
    	var type = angular.element(document.querySelector('#oryx-multiinstance_type')).scope().property;    	
    	jQuery('#assignmentType').val(type.value);
    	
    	var src = "editor-app/configuration/properties/iframe-candidateUser.html";
    	var iframe = document.createElement('iframe');  
	    iframe.id = 'iframeTest'; 
	    iframe.scrolling = "no";
	    iframe.src = src; 
		document.body.appendChild(iframe);
    };
    
    //候选人组
    $scope.candidateGroupsClick = function() {
    	var assigneeValue = document.getElementById('assigneeField').value;
    	if( assigneeValue != ''&&assigneeValue!='${assignee}'){
    		alert("不可选（请先删除“办理人”，再配置）");
    		return;
    	}
    	
    	var src = "editor-app/configuration/properties/iframe-candidateGroup.html";
    	var iframe = document.createElement('iframe');  
	    iframe.id = 'iframeTest'; 
	    iframe.scrolling = "no";
	    iframe.src = src; 
		document.body.appendChild(iframe);
    };
    
    
    
    $scope.save = function() {
        $scope.property.value = {};
        //handleAssignmentInput($scope);
        //办理人
        if(jQuery('#assigneeField').val() != undefined){
	        if(jQuery('#assigneeField').val() !=''){
	        	if(jQuery('#assigneeField').val().split(":")[0]=="表达式"){
	        		$scope.assignment.assignee = jQuery('#assigneeField').val();
	        	}else{
	        		$scope.assignment.assignee = jQuery('#assigneeHidden').val();
	        	}
	        	
	        	$scope.assignment.assigneeHidden = jQuery('#assigneeField').val();
	        }else{
	        	$scope.assignment.assignee = '';
	        	$scope.assignment.assigneeHidden = '';
	        }
        }
        
        //候选人
        if(jQuery('#userField').val() != undefined){
        	if(jQuery('#userField').val() !=''){
        		/*if(jQuery('#userField').val().split(":")[0]=="表达式"){
        		    $scope.assignment.candidateUsers = jQuery('#userField').val();
        	    }else{
        		    $scope.assignment.candidateUsers = jQuery('#candidateUsersHidden').val();
        	    }*/
    		    $scope.assignment.candidateUsers = jQuery('#candidateUsersHidden').val();
            	
            	$scope.assignment.candidateUsersHidden = jQuery('#userField').val();
            }else{
            	$scope.assignment.candidateUsers = '';
            	$scope.assignment.candidateUsersHidden = '';
            }
        }
        
        
         
        //候选人组
        if(jQuery('#groupField').val() != undefined){
	        if(jQuery('#groupField').val() !=''){
	        	if(jQuery('#groupField').val().split(":")[0]=="表达式"){
	        		$scope.assignment.candidateGroups = jQuery('#groupField').val();
	        	}else{
	        		$scope.assignment.candidateGroups = jQuery('#candidateGroupsHidden').val();
	        	}
	        	
	        	$scope.assignment.candidateGroupsHidden = jQuery('#groupField').val();
	        }else{
	        	$scope.assignment.candidateGroups = '';
	        	$scope.assignment.candidateGroupsHidden = '';
	        }
        }
        
        $scope.property.value.assignment = $scope.assignment;
        
        $scope.updatePropertyInModel($scope.property);
        $scope.close();
      //解决ie9箭头穿透的问题
    	jQuery('#main').removeClass('opacity5');
    };

    // Close button handler
    $scope.close = function() {
    	handleAssignmentInput($scope);
    	$scope.property.mode = 'read';
    	$scope.$hide();
    	//解决ie9箭头穿透的问题
    	jQuery('#main').removeClass('opacity5');
    };
    
    var handleAssignmentInput = function($scope) {
    	if ($scope.assignment.candidateUsers)
    	{
	    	var emptyUsers = true;
	    	var toRemoveIndexes = [];
	        for (var i = 0; i < $scope.assignment.candidateUsers.length; i++)
	        {
	        	if ($scope.assignment.candidateUsers[i].value != '')
	        	{
	        		emptyUsers = false;
	        	}
	        	else
	        	{
	        		toRemoveIndexes[toRemoveIndexes.length] = i;
	        	}
	        }
	        
	        for (var i = 0; i < toRemoveIndexes.length; i++)
	        {
	        	$scope.assignment.candidateUsers.splice(toRemoveIndexes[i], 1);
	        }
	        
	        if (emptyUsers)
	        {
	        	$scope.assignment.candidateUsers = undefined;
	        }
    	}
        
    	if ($scope.assignment.candidateGroups)
    	{
	        var emptyGroups = true;
	        var toRemoveIndexes = [];
	        for (var i = 0; i < $scope.assignment.candidateGroups.length; i++)
	        {
	        	if ($scope.assignment.candidateGroups[i].value != '')
	        	{
	        		emptyGroups = false;
	        	}
	        	else
	        	{
	        		toRemoveIndexes[toRemoveIndexes.length] = i;
	        	}
	        }
	        
	        for (var i = 0; i < toRemoveIndexes.length; i++)
	        {
	        	$scope.assignment.candidateGroups.splice(toRemoveIndexes[i], 1);
	        }
	        
	        if (emptyGroups)
	        {
	        	$scope.assignment.candidateGroups = undefined;
	        }
    	}
    };
    
    
}];


function cleanPara(obj){
	var thisInput = jQuery(obj).parent('.form-group').children('input');
	if(thisInput.val() == assigneeFieldVal){
		return;
	}
	thisInput.val(undefined);
}


var assignmentTootip = function(obj){
	var value = jQuery(obj).parent('.form-group').children('input:first').val();
	var object = jQuery(obj).parent('.form-group').children('.assignmentTootip');
	if(value != ''){
		object.html(value).show();
	}	
};

var assignmentTootipleave = function(obj){
	var object = jQuery(obj).parent('.form-group').children('.assignmentTootip');
	object.hide();
}