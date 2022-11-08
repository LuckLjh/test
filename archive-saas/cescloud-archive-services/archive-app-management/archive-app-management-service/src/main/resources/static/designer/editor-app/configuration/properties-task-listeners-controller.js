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
 * Task listeners
 */
var KisBpmTaskListenersCtrl = [ '$scope', '$modal', '$timeout', '$translate', function($scope, $modal, $timeout, $translate) {

    // Config for the modal window
    var opts = {
		backdrop: false,
        keyboard: false,
        template:  'editor-app/configuration/properties/task-listeners-popup.html?version=' + Date.now(),
        scope: $scope
    };

    // Open the dialog
    $modal(opts);
}];

var KisBpmTaskListenersPopupCtrl = [ '$scope', '$q', '$translate', '$timeout', '$http',function($scope, $q, $translate, $timeout,$http) {

	//解决ie9箭头点透事件
	jQuery('#main').addClass('opacity5');
    // Put json representing form properties on scope
    if ($scope.property.value !== undefined && $scope.property.value !== null
        && $scope.property.value.taskListeners !== undefined
        && $scope.property.value.taskListeners !== null) {
        
        if ($scope.property.value.taskListeners.constructor == String)
        {
            $scope.taskListeners = JSON.parse($scope.property.value.taskListeners);
        }
        else
        {
            // Note that we clone the json object rather then setting it directly,
            // this to cope with the fact that the user can click the cancel button and no changes should have happened
            $scope.taskListeners = angular.copy($scope.property.value.taskListeners);
        }
        
        for (var i = 0; i < $scope.taskListeners.length; i++)
        {
            var taskListener = $scope.taskListeners[i];
            if (taskListener.className !== undefined && taskListener.className !== '')
            {
                taskListener.implementation = taskListener.className;
            }
            else if (taskListener.expression !== undefined && taskListener.expression !== '')
            {
                taskListener.implementation = taskListener.expression;
            }
            else if (taskListener.delegateExpression !== undefined && taskListener.delegateExpression !== '')
            {
                taskListener.implementation = taskListener.delegateExpression;
            }
        }
    } else {
        $scope.taskListeners = [];
    }

    // Array to contain selected properties (yes - we only can select one, but ng-grid isn't smart enough)
    $scope.selectedListeners = [];
    $scope.selectedFields = [];
    $scope.translationsRetrieved = false;
    
    $scope.labels = {};
    
    var eventPromise = $translate('PROPERTY.TASKLISTENERS.EVENT');
    var implementationPromise = $translate('PROPERTY.TASKLISTENERS.FIELDS.IMPLEMENTATION');
    var namePromise = $translate('PROPERTY.TASKLISTENERS.FIELDS.NAME');
    
    $q.all([eventPromise, implementationPromise, namePromise]).then(function(results) { 
        $scope.labels.eventLabel = results[0];
        $scope.labels.implementationLabel = results[1];
        $scope.labels.nameLabel = results[2];
        $scope.translationsRetrieved = true;

        // Config for grid
        $scope.gridOptions = {
            data: 'taskListeners',
            enableRowReordering: true,
            headerRowHeight: 28,
            multiSelect: false,
            keepLastSelected : false,
            selectedItems: $scope.selectedListeners,
            afterSelectionChange: function (rowItem, event) {
                $scope.selectedFields.length = 0;
                if ($scope.selectedListeners.length > 0)
                {
                    var fields = $scope.selectedListeners[0].fields;
                    if (fields !== undefined && fields !== null)
                    {
                        for (var i = 0; i < fields.length; i++)
                        {
                            var field = fields[i];
                            if (field.stringValue !== undefined && field.stringValue !== '')
                            {
                                field.implementation = field.stringValue;
                            }
                            else if (field.expression !== undefined && field.expression !== '')
                            {
                                field.implementation = field.expression;
                            }
                            else if (field.string !== undefined && field.string !== '')
                            {
                                field.implementation = field.string;
                            }
                        }
                    }
                }
            },
            columnDefs: [{ field: 'event', displayName: $scope.labels.eventLabel },
                { field: 'implementation', displayName: '实现'}]
        };
        
        // Config for field grid
        $scope.gridFieldOptions = {
            data: 'selectedListeners[0].fields',
            enableRowReordering: true,
            headerRowHeight: 28,
            multiSelect: false,
            keepLastSelected : false,
            selectedItems: $scope.selectedFields,
            columnDefs: [{ field: 'name', displayName: $scope.labels.name },
                { field: 'implementation', displayName: $scope.labels.implementationLabel}]
        };
    });
    
    $scope.displayData = function(){
    	//jQuery(".selectBox .fa").on("click",function(event){
    		jQuery(".selectUl").toggle();//ul弹窗展开
    	
			if (event.stopPropagation) {   
	        	// 针对 Mozilla 和 Opera   
	        	event.stopPropagation();   
	        }else if (window.event) {   
	        	// 针对 IE   
	        	window.event.cancelBubble = true;   
	        }  
		//});
		
		//
    	jQuery(".selectUl li").click(function(event){
			event=event||window.event; 
		
			jQuery(this).addClass("actived_li").siblings().removeClass("actived_li");//点击当前的添加。actived_li这个类；其他的移除这个类名
			var oliName = jQuery(this).attr("oliName");//定义一个name属性，获取点击的元素属性赋值到当前，方便动态化传值
			var oliId = jQuery(this).attr("oliId");//定义一个id属性，获取点击的元素属性赋值到当前，方便数据交互传值
			jQuery(this).parent().prev().children().children("input").val(oliId); //把当前点击的name赋值到显示的input的val里面
			$scope.selectedListeners[0].className = oliId;
			if ($scope.selectedListeners[0].className !== '')
	        {
	            $scope.selectedListeners[0].implementation = $scope.selectedListeners[0].className;
	        }
	        else if ($scope.selectedListeners[0].expression !== '')
	        {
	            $scope.selectedListeners[0].implementation = $scope.selectedListeners[0].expression;
	        }
	        else if ($scope.selectedListeners[0].delegateExpression !== '')
	        {
	            $scope.selectedListeners[0].implementation = $scope.selectedListeners[0].delegateExpression;
	        }
	        else
	        {
	            $scope.selectedListeners[0].implementation = '';
	        }
			 jQuery("#classField").trigger("keyup")
		});
		//点击任意地方隐藏下拉
		jQuery(document).click(function(event){
			event=event||window.event; 
			//$(".inputCase .fa").removeClass("fa-caret-up").addClass("fa-caret-down")//当点隐藏ul弹窗时候，把小图标恢复原状
			jQuery(".selectUl").hide();//当点击空白处，隐藏ul弹窗
		});
    };
    
    $scope.listenerDetailsChanged = function() {
        if ($scope.selectedListeners[0].className !== '')
        {
            $scope.selectedListeners[0].implementation = $scope.selectedListeners[0].className;
        }
        else if ($scope.selectedListeners[0].expression !== '')
        {
            $scope.selectedListeners[0].implementation = $scope.selectedListeners[0].expression;
        }
        else if ($scope.selectedListeners[0].delegateExpression !== '')
        {
            $scope.selectedListeners[0].implementation = $scope.selectedListeners[0].delegateExpression;
        }
        else
        {
            $scope.selectedListeners[0].implementation = '';
        }
    };
    
    $scope.keyup = function() {
    	var html = '';       
        
        for(var i=0;i<$scope.property.value.taskListeners.length;i++){     	
        	var n= 0;
        	if($scope.property.value.taskListeners[i].className !="" ) n++;
        	if($scope.property.value.taskListeners[i].delegateExpression !="" ) n++;
        	if($scope.property.value.taskListeners[i].expression !="" ) n++;
        	if(n<1){
        		html += '第'+(i+1)+'个事件：类，表达式，代理表达式至少有一个必须填写！<br />'
        	}
        }
        
        jQuery('.errortextName2').html(html);
    };

    // Click handler for add button
    $scope.addNewListener = function() {
        $scope.taskListeners.push({ event : 'create',
            implementation : '',
            className : '',
            expression: '',
            delegateExpression: ''});
    };

    // Click handler for remove button
    $scope.removeListener = function() {
        if ($scope.selectedListeners.length > 0) {
            var index = $scope.taskListeners.indexOf($scope.selectedListeners[0]);
            $scope.gridOptions.selectItem(index, false);
            $scope.taskListeners.splice(index, 1);

            $scope.selectedListeners.length = 0;
            if (index < $scope.taskListeners.length) {
                $scope.gridOptions.selectItem(index + 1, true);
            } else if ($scope.taskListeners.length > 0) {
                $scope.gridOptions.selectItem(index - 1, true);
            }
            
            var html = '';
            for(var i=0;i<$scope.property.value.taskListeners.length;i++){     	
            	var n= 0;
            	if($scope.property.value.taskListeners[i].className !="" ) n++;
            	if($scope.property.value.taskListeners[i].delegateExpression !="" ) n++;
            	if($scope.property.value.taskListeners[i].expression !="" ) n++;
            	if(n<1){
            		html += '第'+(i+1)+'个事件：类，表达式，代理表达式至少有一个必须填写！<br />'
            	}
            }
            
            jQuery('.errortextName2').html(html);
        }
    };

    // Click handler for up button
    $scope.moveListenerUp = function() {
        if ($scope.selectedListeners.length > 0) {
            var index = $scope.taskListeners.indexOf($scope.selectedListeners[0]);
            if (index != 0) { // If it's the first, no moving up of course
                // Reason for funny way of swapping, see https://github.com/angular-ui/ng-grid/issues/272
                var temp = $scope.taskListeners[index];
                $scope.taskListeners.splice(index, 1);
                $timeout(function(){
                    $scope.taskListeners.splice(index + -1, 0, temp);
                }, 100);

            }
        }
    };

    // Click handler for down button
    $scope.moveListenerDown = function() {
        if ($scope.selectedListeners.length > 0) {
            var index = $scope.taskListeners.indexOf($scope.selectedListeners[0]);
            if (index != $scope.taskListeners.length - 1) { // If it's the last element, no moving down of course
                // Reason for funny way of swapping, see https://github.com/angular-ui/ng-grid/issues/272
                var temp = $scope.taskListeners[index];
                $scope.taskListeners.splice(index, 1);
                $timeout(function(){
                    $scope.taskListeners.splice(index + 1, 0, temp);
                }, 100);

            }
        }
    };
    
    $scope.fieldDetailsChanged = function() {
        if ($scope.selectedFields[0].stringValue != '')
        {
            $scope.selectedFields[0].implementation = $scope.selectedFields[0].stringValue;
        }
        else if ($scope.selectedFields[0].expression != '')
        {
            $scope.selectedFields[0].implementation = $scope.selectedFields[0].expression;
        }
        else if ($scope.selectedFields[0].string != '')
        {
            $scope.selectedFields[0].implementation = $scope.selectedFields[0].string;
        }
        else
        {
            $scope.selectedFields[0].implementation = '';
        }
    };

    // Click handler for add button
    $scope.addNewField = function() {
        if ($scope.selectedListeners.length > 0)
        {
            if ($scope.selectedListeners[0].fields == undefined)
            {
                $scope.selectedListeners[0].fields = [];
            }
            $scope.selectedListeners[0].fields.push({ name : 'fieldName',
                implementation : '',
                stringValue : '',
                expression: '',
                string: ''});
        }
    };

    // Click handler for remove button
    $scope.removeField = function() {
        if ($scope.selectedFields.length > 0) {
            var index = $scope.selectedListeners[0].fields.indexOf($scope.selectedFields[0]);
            $scope.gridFieldOptions.selectItem(index, false);
            $scope.selectedListeners[0].fields.splice(index, 1);

            $scope.selectedFields.length = 0;
            if (index < $scope.selectedListeners[0].fields.length) {
                $scope.gridFieldOptions.selectItem(index + 1, true);
            } else if ($scope.selectedListeners[0].fields.length > 0) {
                $scope.gridFieldOptions.selectItem(index - 1, true);
            }
        }
    };

    // Click handler for up button
    $scope.moveFieldUp = function() {
        if ($scope.selectedFields.length > 0) {
            var index = $scope.selectedListeners[0].fields.indexOf($scope.selectedFields[0]);
            if (index != 0) { // If it's the first, no moving up of course
                // Reason for funny way of swapping, see https://github.com/angular-ui/ng-grid/issues/272
                var temp = $scope.selectedListeners[0].fields[index];
                $scope.selectedListeners[0].fields.splice(index, 1);
                $timeout(function(){
                    $scope.selectedListeners[0].fields.splice(index + -1, 0, temp);
                }, 100);

            }
        }
    };

    // Click handler for down button
    $scope.moveFieldDown = function() {
        if ($scope.selectedFields.length > 0) {
            var index = $scope.selectedListeners[0].fields.indexOf($scope.selectedFields[0]);
            if (index != $scope.selectedListeners[0].fields.length - 1) { // If it's the last element, no moving down of course
                // Reason for funny way of swapping, see https://github.com/angular-ui/ng-grid/issues/272
                var temp = $scope.selectedListeners[0].fields[index];
                $scope.selectedListeners[0].fields.splice(index, 1);
                $timeout(function(){
                    $scope.selectedListeners[0].fields.splice(index + 1, 0, temp);
                }, 100);

            }
        }
    };

    // Click handler for save button
    $scope.save = function() {

        if ($scope.taskListeners.length > 0) {
            $scope.property.value = {};
            $scope.property.value.taskListeners = $scope.taskListeners;
            
            var html = '';
            for(var i=0;i<$scope.property.value.taskListeners.length;i++){     	
            	var n= 0;
            	if($scope.property.value.taskListeners[i].className !="" ) n++;
            	if($scope.property.value.taskListeners[i].delegateExpression !="" ) n++;
            	if($scope.property.value.taskListeners[i].expression !="" ) n++;
            	if(n<1){
            		html += '第'+(i+1)+'个事件：类，表达式，代理表达式至少有一个必须填写！<br />'
            	}
            }
            
            if(html != ''){
            	jQuery('.errortextName2').html(html);
            	return;
            }
        } else {
            $scope.property.value = null;
        }

        $scope.updatePropertyInModel($scope.property);
        $scope.close();
      //解决ie9箭头点透事件
    	jQuery('#main').removeClass('opacity5');
    };

    $scope.cancel = function() {
        $scope.close();
      //解决ie9箭头点透事件
    	jQuery('#main').removeClass('opacity5');
    };

    // Close button handler
    $scope.close = function() {
        $scope.property.mode = 'read';
        $scope.$hide();
      //解决ie9箭头点透事件
    	jQuery('#main').removeClass('opacity5');
    };

    $scope.init = function() {
    	var bpmModelCode = $scope.editor.getModelMetaData().model.properties.process_id;
    	var url = "../workflow-version/listener/tasks?bpmModelCode=" + bpmModelCode + "&access_token=" + token;
    	$http.get(url)
        .success(function (data, header, config, status) {
        	$scope.listener = data.data;
        })
        .error(function (data, header, config, status) {
            console.log(data);
        });
    };
    
    $scope.init();
}];