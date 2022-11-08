var iframeUrl = window.location.href;

//取消
function cancel() {
	var rowId = getIframeParam("rowId");
	window.parent.customRule.fun_cancelUserTypeCallback(rowId);
	closed();
}

//关闭对话框
function closed() {
    var iframe = parent.document.getElementById("iframeNode");
    iframe.parentNode.removeChild(iframe);
}

//保存
function save(){
	debugger
	var nodeId = $("#customNode").val();
	if(nodeId== null){
        alert("节点id不能为空");
	}else{
		 var rowId = getIframeParam("rowId");
		 var  value = nodeId + customRule.SPLIT_NODE + nodeId;
	        //回调函数，返回选中的角色信息
	        window.parent.customRule.fun_selectNodeCallback(rowId, value ,nodeId);
	        closed();
	}
	
}

function getIframeParam(name) {
    var reg = new RegExp("[^\?&]?" + encodeURI(name) + "=[^&]+");
    var arr = iframeUrl.match(reg);
    if (arr != null) {
        return decodeURI(arr[0].substring(arr[0].search("=") + 1));
    }
    return "";
}
$(function(){
	
});