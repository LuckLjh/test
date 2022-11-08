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
var KisBpmNodeTacticsCtrl = [ '$scope', '$modal', function($scope, $modal) {
	var url = 'editor-app/configuration/properties/nodetactics-popup.html?version=' + Date.now();
	var html = '<div id="opacity" class="opacity"></div>';
    // Config for the modal window
    var opts = {
		backdrop: false,
        keyboard: false,
        template:  url,
        scope: $scope
    };
    
    // Open the dialog
    $modal(opts);
    
    // Close button handler
    $scope.close = function() {
    	$scope.property.mode = 'read';
    	$scope.$hide();
    	//解决ie9箭头穿透的问题
    	jQuery('#main').removeClass('opacity5');
    };
}];

var KisBpmDialogPropertyPopupCtrl = ['$scope', function($scope) {

	// Put json representing dialog on scope

    if ($scope.property.value !== undefined && $scope.property.value !== null) 
    {
        $scope.nodetactics = $scope.property.value;
    	if($scope.property.value.dialogcontent0 !== undefined && $scope.property.value.dialogcontent0 !== null){
    		var list = new String($scope.property.value.dialogcontent0).split(",");
    	}
        
        
    } else {
        $scope.nodetactics = {};
    }
    
    $scope.save = function() {
    	$scope.property.value = $scope.nodetactics;
    	
    	var nodetacticNumber = 0;
    	var str = "";
    	jQuery("[name=nodetactics_chk]:checked").each(function(){
    		str += "," + jQuery(this).val();
    		nodetacticNumber ++;
    	})
    	if (str != "") {
    		str = str.substring(1);
    	}
    	$scope.nodetactics.nodetacticNumber = nodetacticNumber;
    	$scope.nodetactics.dialogcontent0 = str;
        $scope.updatePropertyInModel($scope.property);
        $scope.close();
    };
    
    $scope.cancel = function() {
    	$scope.nodetactics.autoCommitIfNullAssigneement = false;
    	$scope.nodetactics.autoCommitIfSameAssigneement = false;
    	$scope.nodetactics.nonStopCommitAfterRollBack = false;
    	var len = list? list.length: 0
    	for (var i = 0; i < len; i ++) {
    		var flag = list[i];
    		if ("autoCommitIfNullAssigneement" == flag) {
    			$scope.nodetactics.autoCommitIfNullAssigneement = true;
    		} 
    		if ("autoCommitIfSameAssigneement" == flag) {
    			$scope.nodetactics.autoCommitIfSameAssigneement = true;
    		} 
    		if ("nonStopCommitAfterRollBack" == flag) {
    			$scope.nodetactics.nonStopCommitAfterRollBack = true;
    		} 
    	}
        $scope.close();
    };
    
    $scope.checked = function(flag) {
    	if(flag){
    		return flag;
    	}
    	return false;
    }
    
    //根据是否有选择会签类型来决定多选框是否可用
    $scope.checkIsDisableNodeTactics = function() {
    	return "None" !== 
    		angular.element(document.querySelector('#oryx-multiinstance_type')).scope().property.value;
    }

    $scope.close = function() {
        $scope.property.mode = 'read';
        $scope.$hide();
    };
}];


