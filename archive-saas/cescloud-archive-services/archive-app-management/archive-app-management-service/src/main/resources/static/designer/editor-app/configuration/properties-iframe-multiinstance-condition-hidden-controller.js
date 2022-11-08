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

var KisBpmMultiConditionCtrl = [ '$scope', '$modal','$timeout', function($scope, $modal, $timeout) {
	var url = 'editor-app/configuration/properties/iframe-multiinstance-condition.html?version=' + Date.now();
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
    /*$timeout(function(){
    	//var vote = angular.element(document.querySelector('#oryx-multiinstance_condition')).scope().property.value.vote;
    	if(vote != undefined){
    		if(vote.voteType != undefined) {
    			if(vote.voteType == 1) {
    				jQuery("#absoluteVote").attr("checked","true");
        		}else if(vote.voteType == 2){
        			jQuery("#percentVote").attr("checked","true");
        		}
    		}
    	}
    	
    },100);*/
}];




var KisBpmMultiinstancePopupCtrl = [ '$scope', '$modal','$timeout', function($scope, $modal,$timeout) {
	//解决ie9箭头穿透的问题
	jQuery('#main').addClass('opacity5');
	var html = '<div id="opacity-hidden" class="opacity-hidden"></div>';
	var multiinstanceCondition = angular.element(document.querySelector('#oryx-multiinstance_condition')).scope().property;
    // Put json representing assignment on scope
    if ($scope.property.value !== undefined && $scope.property.value !== null
        && $scope.property.value.vote !== undefined
        && $scope.property.value.vote.num !== null) 
    {
        $scope.vote = $scope.property.value.vote;
    } else {
        $scope.vote = {};
    }
	
    $scope.save = function() {
    	debugger
    	//nrOfInstances：实例总数<br>
		//nrOfActiveInstances：当前活动的，比如，还没完成的，实例数量。 对于顺序执行的多实例，值一直为1。<br>
		//nrOfCompletedInstances：已经完成实例的数目。<br>
    	
    	var num = $scope.vote.num;
    	if(num){
    		var reg = /^[0-9]*$/;
    		var result = reg.test(num);
    		if(result == false){
    			alert("票数请输入大于0的正整数")
    			return;
    		}
    		
    	}
    	
    	if($scope.vote.num >100 && $scope.vote.voteType == 2){
    		alert("百分比类型下，不能超过100");
    		return;
    	}
    	
    	
    	//multiinstanceCondition.value="aaaa";
    	//jQuery('#oryx-multiinstance_condition').prepend(html);
        $scope.property.value = {};
        //handleAssignmentInput($scope);
        //当没有任何一种投票形式（绝对票数，百分比）的时候默认的是百分比的投票形式，并且票数是100%
    	if($scope.vote.voteType!=1 && $scope.vote.voteType!=2){
    		$scope.vote.voteType=2;
    		$scope.vote.num=100;
    	}
    	
    	
        $scope.property.value.vote = $scope.vote;
        $scope.updatePropertyInModel(multiinstanceCondition);
        $scope.updatePropertyInModel($scope.property);
        $scope.close();
      //解决ie9箭头穿透的问题
    	jQuery('#main').removeClass('opacity5');
    };

    // Close button handler
    $scope.close = function() {
    	//handleAssignmentInput($scope);
    	$scope.property.mode = 'read';
    	$scope.$hide();
    	//解决ie9箭头穿透的问题
    	jQuery('#main').removeClass('opacity5');
    };
    
    var handleAssignmentInput = function($scope) {
    	if ($scope.vote.candidateUsers)
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