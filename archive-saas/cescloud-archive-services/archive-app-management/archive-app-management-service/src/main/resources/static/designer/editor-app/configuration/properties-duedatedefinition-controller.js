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
 * Condition expression
 */

var KisBpmDuedatedefinitionCtrl = [ '$scope', '$modal', function($scope, $modal) {
    // Config for the modal window
    var opts = {
		backdrop: false,
        keyboard: false,
        template:  'editor-app/configuration/properties/duedatedefinition-popup.html?version=' + Date.now(),
        scope: $scope
    };

    // Open the dialog
    $modal(opts);
}];

var KisBpmDuedatedefinitionPopupCtrl = [ '$scope', '$translate', '$http','$timeout', function($scope, $translate, $http,$timeout) {
	
	//解决ie9箭头穿透的问题
	jQuery('#main').addClass('opacity5');
	// Put json representing condition on scope
    if ($scope.property.value !== undefined && $scope.property.value !== null) {

        $scope.duedatedefinition = {value: $scope.property.value};
        
    } else {
        $scope.duedatedefinition = {value: ''};
    }
    debugger
    $scope.yearInputBlurred = function() {
    	//去掉00 000等手动输入的
    	if(jQuery("#timerYear").val() == 0){
    		jQuery("#timerYear").val(0);
    	}
    	
    	
    	
    	if(jQuery("#timerYear").val().length>1){
    		var str = jQuery("#timerYear").val();
    		var s = str.charAt(0);
    		if(s == 0){
    			jQuery("#timerYear").val(str.charAt(1))
    		}
    	}
    	
    	
    	
    };
    
    $scope.inputBlurred = function() {
    	//去掉00 000等手动输入的
    	if(jQuery("#timerDay").val() == 0){
			jQuery("#timerDay").val(0);
		}
    	//最大只能是365 超过的按照365
    	if(jQuery("#timerDay").val() > 365){
			jQuery("#timerDay").val(365);
		}
    	var str = jQuery("#timerDay").val();
		var charLength = str.length;
		if(charLength == 2){
			var s = str.charAt(0);
    		if(s == 0){
    			jQuery("#timerDay").val(str.charAt(1));
    		}
		}
		if(charLength == 3){
			var s = str.charAt(0);
			//如果是3位的情况下  判断如果第一位是0的情况下
    		if(s == 0){
    			if(str.charAt(1) == 0){
    				jQuery("#timerDay").val(str.charAt(2));
    			}else{
    				jQuery("#timerDay").val(str.substring(1));
    			}
    		}
		}
    };
    
    $scope.enterPressed = function(keyEvent) {
    	if (keyEvent && keyEvent.which === 13) {
    		keyEvent.preventDefault();
	        $scope.inputBlurred(); // we want to do the same as if the user would blur the input field
    	}
    };

	$timeout(function(){
		for(var i=0;i<=24;i++){
			jQuery("#timerHour").append("<option value="+i+">"+i+"</option>");
		};
		for(var i=0;i<=60;i++){
			jQuery("#timerMinute,#timerSecond").append("<option value="+i+">"+i+"</option>");
		};
		var value = $scope.duedatedefinition.value;
		if(value != ""){
			jQuery("#timerYear").val(value.substring(1,value.indexOf("M")));
			jQuery("#timerDay").val(value.substring(value.indexOf("M")+1,value.indexOf("D")));
			jQuery("#timerHour").val(value.substring(value.indexOf("T")+1,value.indexOf("H")));
			jQuery("#timerMinute").val(value.substring(value.indexOf("H")+1,value.lastIndexOf("M")));
			jQuery("#timerSecond").val(value.substring(value.lastIndexOf("M")+1,value.indexOf("S")));
		}
		
		
	},100);
    
    
    
	
    $scope.save = function() {
    	$scope.duedatedefinition.value = "";
    	if(jQuery("#timerYear").val() != ""){
    		$scope.duedatedefinition.value += "P"+jQuery("#timerYear").val()+"M";
    	}
    	if(jQuery("#timerDay").val() != ""){
    		$scope.duedatedefinition.value += jQuery("#timerDay").val() + "D";
    	}
    	if(jQuery("#timerHour").val() != ""){
    		$scope.duedatedefinition.value += "T"+jQuery("#timerHour").val() + "H";
    	}
    	if(jQuery("#timerMinute").val() != ""){
    		$scope.duedatedefinition.value += jQuery("#timerMinute").val() + "M";
    	}
    	if(jQuery("#timerSecond").val() != ""){
    		$scope.duedatedefinition.value += jQuery("#timerSecond").val() + "S";
    	}
    	if('P0Y0DT0H0M0S'==$scope.duedatedefinition.value){
    		$scope.duedatedefinition.value="";
    	}
        $scope.property.value = $scope.duedatedefinition.value;
        $scope.updatePropertyInModel($scope.property);
        $scope.close();
      //解决ie9箭头穿透的问题
    	jQuery('#main').removeClass('opacity5');
    };

    // Close button handler
    $scope.close = function() {
    	$scope.property.mode = 'read';
    	$scope.$hide();
    	//解决ie9箭头穿透的问题
    	jQuery('#main').removeClass('opacity5');
    };
}];