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
var KisBpmDialogCtrl = [ '$scope', '$modal', function($scope, $modal) {

    // Config for the modal window
    var opts = {
        template:  'editor-app/configuration/properties/iframe-notice1.html?version=' + Date.now(),
        scope: $scope
    };

    // Open the dialog
    $modal(opts);
}];

var KisBpmDialogPopupCtrl = [ '$scope', function($scope) {
    // Put json representing dialog on scope
    if ($scope.property.value !== undefined && $scope.property.value !== null
        && $scope.property.value.dialog !== undefined
        && $scope.property.value.dialog !== null) 
    {
        $scope.dialog = $scope.property.value.dialog;
    } else {
        $scope.dialog = {};
    }


    $scope.save = function() {
        $scope.property.value = {};
        
        $scope.dialog.dialogcontent1 = document.getElementById('noticeUserId').value;
        $scope.property.value.dialog = $scope.dialog;
        $scope.updatePropertyInModel($scope.property);
        $scope.close();
    };

    // Close button handler
    $scope.close = function() {
    	$scope.property.mode = 'read';
    	$scope.$hide();
    };
    
    $scope.noticeClick=function(){
    	var src = "editor-app/configuration/properties/iframe-notice-choosepeople.html";
    	var iframe = document.createElement('iframe');  
	    iframe.id = 'iframeTest'; 
	    iframe.scrolling = "no";
	    iframe.src = src; 
		document.body.appendChild(iframe);
    }
    
   
}];