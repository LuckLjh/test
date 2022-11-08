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
var configURL = window.parent.configURL=="cfg-resource"?"":window.parent.configURL;
var KisBpmConditionExpressionHiddenCtrl = [ '$scope', '$modal', function($scope, $modal) {

    // Config for the modal window
    var opts = {
		backdrop: false,
        keyboard: false,
        template:  'editor-app/configuration/properties/condition-expression-popup-hidden.html?version=' + Date.now(),
        scope: $scope
    };

    // Open the dialog
    $modal(opts);
}];

var KisBpmConditionExpressionPopupHiddenCtrl = [ '$scope', '$translate', '$http', function($scope, $translate, $http) {
	// Put json representing condition on scope
   /* if ($scope.property.value !== undefined && $scope.property.value !== null) {

        $scope.conditionExpression = {value: $scope.property.value};

    } else {
        $scope.conditionExpression = {value: ''};
    }*/

	var conditionsequenceflow = angular.element(document.querySelector('#oryx-conditionsequenceflow')).scope().property;

	//获取条件参数

	 if ($scope.property.value !== undefined && $scope.property.value !== null
			 && $scope.property.value.conditionExpression !== undefined
		     && $scope.property.value.conditionExpression !== null) {
		 if ($scope.property.value.conditionExpression.constructor == String)
	    	{
	    		$scope.conditionExpression = JSON.parse($scope.property.value.conditionExpression);
	    	}
	    	else
	    	{
	    		$scope.conditionExpression = angular.copy($scope.property.value.conditionExpression);
	    	    if($scope.property.value.conditionExpression.length > 0 && $scope.property.value.conditionExpression[0].activityId != undefined && $scope.property.value.conditionExpression[0].activityId !=null){
	    	    	$scope.activityId = $scope.property.value.conditionExpression[0].activityId;
	    	    }
	    	    if($scope.property.value.conditionExpression.length > 0 && $scope.property.value.conditionExpression[0].isVote != undefined && $scope.property.value.conditionExpression[0].isVote !=null){
	    	        $scope.isVote = $scope.property.value.conditionExpression[0].isVote;
	    	    }
	    	    if($scope.property.value.conditionExpression.length > 0 && $scope.property.value.conditionExpression[0].voteJoiner != undefined && $scope.property.value.conditionExpression[0].voteJoiner != null){
	    	        $scope.voteJoiner = $scope.property.value.conditionExpression[0].voteJoiner;
	    		}
	    	}

    } else {
    	//新增的时候，默认有一行
    	$scope.conditionExpression = [];
    	$scope.conditionExpression.push({
    		expression:''
    	});

    }


	if($scope.conditionExpression.length > 0){
		  if(!$scope.conditionExpression[0].expressions || $scope.conditionExpression[0].expressions.length == 0){
		      $scope.conditionExpression[0].expressions = [{left_parentheses : '(',
		                parameters  : '',
		                   condition   : '',
		                   para_value    : '',
		                   right_parentheses : ')',
		                   joiner   : '&&'}];
		      $scope.conditionExpression[0].voteJoiner='&&';
		  }
	}

	var bpmModelCode = $scope.editor.getModelMetaData().model.properties.process_id;
	$scope.init = function() {
		$http({
			method: 'GET',
	        url: "../workflow-version/condition/fields?bpmModelCode=" + bpmModelCode + "&access_token=" + token
		}).success(function (respData, status, headers, config) {
        	var data = respData.data || [];
        	var agreement = {
        	    'text': '是否同意',
        		'value': 'agreement'
        	};
            data.push(agreement);
        	$scope.ops = data;
        	$scope.property.value.ops = $scope.ops;
        	if($scope.conditionExpression.length > 0){
        		if($scope.conditionExpression[0].expression.indexOf('"') != -1){
                    for (var i = 0; i < $scope.conditionExpression[0].expressions.length; i++){
                       while ($scope.conditionExpression[0].expressions[i].para_value.indexOf('"') > -1 ){
                           $scope.conditionExpression[0].expressions[i].para_value = $scope.conditionExpression[0].expressions[i].para_value.replace('"','');
                       }
                    }
                }
                if($scope.conditionExpression[0].expression.indexOf("${voteHolder")!=-1 ||
                        $scope.conditionExpression[0].expression.indexOf("${!voteHolder")!=-1){
                  $scope.conditionExpression[0].voteJoiner=$scope.property.value.conditionExpression[0].voteJoiner;
                  $scope.conditionExpression[0].expressions ="";
                }
             }
        }).error(function (data, status, headers, config) {

        });

	}


	 // 指定排序的比较函数
	$scope.compare = function (property){
         return function(obj1,obj2){
             var value1 = obj1[property];
             var value2 = obj2[property];
             return value1 - value2;     // 升序
         }
    }

    $scope.save = function() {
    	if($scope.conditionExpression[0].expressions==""){
    		$scope.conditionExpression[0].expressions.length = 0;
        }
    	if ($scope.conditionExpression.length > 0){
    			$scope.property.value = {};
    			var expressionText = '';

				/*根据执行顺序增加括号
			   var conditionExpression = $scope.conditionExpression[0];
				 //1.根据业务字段拼写表达式
			   if (conditionExpression.expressions.length > 0){
				   var expressions = conditionExpression.expressions;
				   debugger
				   var sortObj = [];
				   for (var i=0; i<expressions.length; i++){
					   sortObj[i] = expressions[i];
				   }
				   //将表达式排序（升序）
				   sortObj = sortObj.sort($scope.compare("order"));
				   for (var i = 0; i < sortObj.length;  i++){
					   if(i == 0){//第一个元素
						   expressionText += "(" + sortObj[i].parameters + sortObj[i].condition + sortObj[i].para_value;
					   } else {
						   //如果为相同条件顺序的第一个元素，则在joiner和parameters之间加一个左括号
						   if (sortObj[i].order > sortObj[i-1].order) {
							   expressionText += sortObj[i].joiner + "(" + sortObj[i].parameters + sortObj[i].condition + sortObj[i].para_value;
						   }
						   //如果为相同条件顺序的中间元素，则直接拼接所有条件
						   if (sortObj[i].order == sortObj[i-1].order){
							   expressionText += sortObj[i].joiner + sortObj[i].parameters + sortObj[i].condition + sortObj[i].para_value;
						   }

					   }

					   //如果是所有元素的最后一个，或者是相同条件顺序的最后一个，则加上右括号
					   if (i == sortObj.length-1 || sortObj[i].order < sortObj[i+1].order){
						   expressionText += ")";
					   }
				   }


			   }

			   var html = '';
			 //2.如果需要满足会签条件则拼上voteHodler.getResult(excution,activityId)
			   if ( expressionText.indexOf("voteHodler") == -1){
				   var isVote = document.getElementsByName("isVote");
				   for (var i = 0; i < isVote.length; i++){
					   if (isVote[i].checked){
						  $scope.conditionExpression[0].isVote = isVote[i].value;
						  //如果选中满足会签条件，则判断是否输入节点id
			 			   if (isVote[i].value == 1) {
			 				   var voteJoiner = document.getElementById("voteJoiner").value;
			 				   var initValue = document.getElementById("activityId").value
			 				   if (voteJoiner == ''){
			 					   html += '需要配置会签结果的连接条件！';
			 				   }else{
			 					 if (initValue == '' || initValue == '请输入流程节点的id'){
	 			 					   html += '流程节点id不能为空！';
	 			 				   }else{
	 			 					  if (expressionText != '()'){
	 			 						expressionText += voteJoiner;
	 			 					  }
	 			 					  expressionText += 'voteHodler.getResults(excution,'+initValue+')';
	 			 					  $scope.conditionExpression[0].activityId = initValue;
	 			 					  $scope.conditionExpression[0].voteJoiner = voteJoiner;
	 			 				   }
			 				   }

			 			   }
					   }
				   }
			   }

			   if(html != ''){
	   			   jQuery('.errortextName2').html(html);
	   			   return;
	   		   }

			   //4.表达式拼${}
			   if(expressionText != '()'){
				   if(expressionText.indexOf('$') != 0){
						expressionText = '${' + expressionText + '}';
				   }
			   }else{
				   expressionText = '';
			   }

			   $scope.conditionExpression[0].expression = expressionText;*/

		    var conditionExpression = $scope.conditionExpression[0];
		    var expressions = conditionExpression.expressions;

		    for(var i = 0; i < expressions.length;i++){
		        var new_para_value  = expressions[i].para_value;
		           if(!isNaN(new_para_value) && new_para_value.indexOf('"') == -1){
		            expressions[i].para_value = "\""+new_para_value+"\"";
		       }
		    }

    		//校验所有条件框必输
		    var conditonHtml = '';
    		for(var i = 0; i < expressions.length;i++){
    			//1)表达式要么全输要么都不输
    			if(expressions[i].left_parentheses == '' && expressions[i].parameters == '' && expressions[i].condition == ''
    					&& expressions[i].para_value == '' && expressions[i].right_parentheses == '' && expressions[i].joiner == ''
    					|| expressions[i].left_parentheses != '' && expressions[i].parameters != '' && expressions[i].condition != ''
    						&& expressions[i].para_value != '' && expressions[i].right_parentheses != '' && expressions[i].joiner != ''){
    				if(expressions[i].$$hashKey){
    					delete expressions[i].$$hashKey;
    				}

    			}else{
    				conditonHtml += '第'+(i+1)+'行表达式有误！<br />';
    			}

    		}

		    if(conditonHtml != ''){
		    	jQuery('.errortextName2').html(conditonHtml);
		    	return;
		    }

    		//1.保存的时候拼写表达式

			   if (expressions.length > 0){
				 // var expressionText = '';
				   //修改拼写表达式的条件
				   for (var i = 0; i < expressions.length;i++){
				     //条件不包含，将!提到表达式前面
				     if (expressions[i].condition === "!contains") {
               expressionText += expressions[i].left_parentheses
                 + "!"
                 + expressions[i].parameters
                 + ".contains("
                 + expressions[i].para_value
                 + ")"
                 + expressions[i].right_parentheses
                 + expressions[i].joiner;
             } else if (expressions[i].condition === ".contains") {//包含条件自动加上( )
               expressionText += expressions[i].left_parentheses
                 + expressions[i].parameters
                 + expressions[i].condition
                 + "("
                 + expressions[i].para_value
                 + ")"
                 + expressions[i].right_parentheses
                 + expressions[i].joiner;
             } else {
               expressionText += expressions[i].left_parentheses
                 + expressions[i].parameters
                 + expressions[i].condition
                 + expressions[i].para_value
                 + expressions[i].right_parentheses
                 + expressions[i].joiner;
             }
				   }
				//   $scope.conditionExpression[0].expression = expressionText;
			   }

    		var html = '';
    		 //2.判断表达式的正确与否
    		if (expressions != ''){
    		//   var expressionText = $scope.conditionExpression[0].expression;
    		   //2.1.判断左括号数与右括号数是否相等
			   var leftCount = $scope.getCharCount(expressionText, '(');
			   var rightCount = $scope.getCharCount(expressionText, ')');
			   if(leftCount != rightCount){
				   html += '表达式语法有误！请核对！';
			   }

       		   //2.2.是否以&&或||结尾，如果是则截取
    		   var len = 0;
 			   if (expressionText.endWith('&&')){
 				  len = expressionText.lastIndexOf('&&');
 			   }
 			   if (expressionText.endWith('||')){
 				   len = expressionText.lastIndexOf('||');
 			   }
 			   if (len > 0){
 				   expressionText = expressionText.substr(0,len);
 			   }

 		   }

		   //3.如果需要满足会签条件则拼上voteHodler.getResult(excution,activityId)
    		var flag = '';
    	  var isAgree = document.getElementsByName("isAgree");
		  for(var i = 0; i < isAgree.length; i++){
			  if(isAgree[i].checked){
				flag = isAgree[i].value;
			  }
		  }

		   if ( expressionText.indexOf("voteHolder") == -1){
			   var isVote = document.getElementsByName("isVote");
			   for (var i = 0; i < isVote.length; i++){
				   if (isVote[i].checked){
					  $scope.conditionExpression[0].isVote = isVote[i].value;
					  //如果选中满足会签条件，则判断是否输入节点id
		 			   if (isVote[i].value == 1) {
		 				   var voteJoiner = document.getElementById("voteJoiner").value
		 				   var initValue = document.getElementById("activityId").value
		 				   if (voteJoiner == ''){
		 					   html += '需要配置会签结果的连接条件！';
		 				   }else{
		 					 if(flag == ''){
		 						 html += '需要配置是否是通过条件！'
		 					 }else{
		 						 debugger
			 					 if (initValue == '' || initValue == '请输入流程节点的id'){
	 			 					   html += '流程节点id不能为空！';
	 			 				   }else{
	 			 					 if (expressionText != ''){
		 			 					 expressionText += voteJoiner;
		 			 				}
	 			 					 if(flag == 0){
	 			 						 expressionText += '!';
	 			 					 }
	 			 					  expressionText += 'voteHolder.getResult(execution,"'+initValue+'")';
	 			 					  $scope.conditionExpression[0].activityId = initValue;
	 			 					  $scope.conditionExpression[0].voteJoiner = voteJoiner;
	 			 				   }
		 					 }

		 				   }

		 			   }
				   }
			   }
		   }


		  if(html != ''){
		   jQuery('.errortextName2').html(html);
		   return;
		  }

		   //3.表达式拼${}
		   if(expressionText != ''){
			   if(expressionText.indexOf('$') != 0){
					  expressionText = '${' + expressionText + '}';
				   }
		   }

		   	$scope.conditionExpression[0].expression = expressionText;
    		$scope.property.value.conditionExpression = $scope.conditionExpression;
    		//设置显示的表达式（原来的）
    		//$scope.property.value = $scope.conditionExpression[0].expression;

    	} else {
    		$scope.property.value = null;
    	}
    	conditionsequenceflow.value = $scope.conditionExpression[0].expression;

    	$scope.updatePropertyInModel(conditionsequenceflow);
        $scope.updatePropertyInModel($scope.property);
        $scope.close();
    };

    $scope.getCharCount = function (str, ch){
     	var num = 0 ;
     	var i;
     	for (i = 0; i < str.length; i++)
     	{
	     	var c = str.charAt(i);
	     	if (c == ch){
	     		num += 1 ;
	     	}
     	}
     	return num;
    }

    String.prototype.endWith = function (endStr){
    	  var len = this.length-endStr.length;
    	  return (len >= 0 && this.lastIndexOf(endStr) == len)
    }

    $scope.checked = function(flag) {
    	if(flag){
    		return true;
    	}
    	return false;
    }

    // Close button handler
    $scope.close = function() {
    	$scope.property.mode = 'read';
    	$scope.$hide();
    };

    $scope.addExpressionValue = function(index){
        $scope.conditionExpression[0].expressions.splice(index + 1, 0, { left_parentheses : '(',
                      parameters  : '',
                         condition   : '',
                         para_value    : '',
                         right_parentheses : ')',
                         joiner : '&&'
         });
     };

   $scope.leftLoad = function(){
	   debugger
	   $scope.left_parentheses = [];
	   var parentheses_value = '';
	   var len = $scope.conditionExpression[0].expressions.length;
		 if (len > 0){
			 for(var i=0; i < len; i++){
				 for(var j=0; j<=i; j++){
					 parentheses_value += '(';
				 }
				 $scope.left_parentheses.push(parentheses_value);
				 parentheses_value = '';
			 }
		 }
   }

   $scope.rightLoad = function(){
	   $scope.right_parentheses = [];
	   var parentheses_value = '';
	   var len = $scope.conditionExpression[0].expressions.length;
		 if (len > 0){
			 for(var i=0; i < len; i++){
				 for(var j=0; j<=i; j++){
					 parentheses_value += ')';
				 }
				 $scope.right_parentheses.push(parentheses_value);
				 parentheses_value = '';
			 }
		 }
   }

   $scope.removeExpressionValue = function(index){
      $scope.conditionExpression[0].expressions.splice(index, 1);
      //重新加载括号
      $scope.leftLoad();
      $scope.rightLoad();
   	 // $scope.expressionDetailsChanged();
   };

   $scope.expressionDetailsChanged = function(){
	   var conditionExpression = $scope.conditionExpression[0];
	   if (conditionExpression.expressions){
		   var expressionText = '';
		   var expressions = conditionExpression.expressions;
		   //修改拼写表达式的条件
		   for (var i = 0; i < expressions.length;i++){
			   expressionText += expressions[i].left_parentheses
			   					+ expressions[i].parameters
			   					+ expressions[i].condition
			   					+ expressions[i].para_value
			   					+ expressions[i].right_parentheses
			   					+ expressions[i].joiner;
		   }
		   $scope.conditionExpression[0].expression = expressionText;
	   }

   }

   $scope.clear = function(){
	   var expressions = $scope.conditionExpression[0].expressions;
	   //首行保留，内容清空
	   expressions[0].left_parentheses = '';
	   expressions[0].parameters = '';
	   expressions[0].condition = '';
	   expressions[0].para_value = '';
	   expressions[0].right_parentheses = '';
	   expressions[0].joiner = '';
	   $scope.conditionExpression[0].expressions.splice(1, expressions.length-1);

	   $scope.conditionExpression[0].isVote = '';
	   $scope.conditionExpression[0].activityId = '';
	   $scope.conditionExpression[0].voteJoiner = '';
	   $scope.conditionExpression[0].isAgree = '';
	   $scope.expressionDetailsChanged();
   }

   $scope.reset = function(){
	    $scope.conditionExpression[0].expressions = [{left_parentheses : '(',
	     parameters  : '',
	        condition   : '',
	        para_value    : '',
	        right_parentheses : ')',
	        joiner   : '&&'}];
	    $scope.conditionExpression[0].voteJoiner='&&';
	    $scope.conditionExpression[0].isVote = '';
	    $scope.conditionExpression[0].activityId = '';
	    $scope.conditionExpression[0].isAgree = '';
   }

}];
