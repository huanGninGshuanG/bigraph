//打开layer
/*var opendxsComponentLogItemLayer = function(update,entityId,updatebtn,readflag){
 var DxsNotifyIframeWin;
 var DxsNotifyIsRefresh = false;

 top.layer.open({
 type : 2,
 title : FORM_TITLE_PRE + '执行日志列表页面',		
 closeBtn : 1, // 不显示关闭按钮
 shadeClose : false,
 shade : false,
 scrollbar: false,
 maxmin : true, // 开启最大化最小化按钮
 area : [ '60%', '80%' ],
 shade : [ 0.3 ],
 content : 'pages/dxsComponentLog/main_item.html?isUpdate=' + update + '&entityId=' + entityId  + '&notifyid=' + updatebtn  + '&readflag=' + readflag   + '&v=' + version,
 });
 }*/
function createDateRender(data, type, full) {

	return getSmpFormatDateByLong(data, true);
}

function executeResultRender(data, type, full) {
/*	switch (data) {
	case "失败":
		return "<a href='#' style='color:red;'  >" + full.finalResult + "</a>";
	case "成功":
		return "<a href='#' style='color:green;' >" + full.finalResult+ "</a>";
	default:
		return "---";
	}*/
	switch (data) {
		case "failed":
			return "<a href='#' style='color:red;'  >" + "失败" + "</a>";
		case "successed":
			return "<a href='#' style='color:green;' >" + "成功"+ "</a>";
		default:
			return "---";
	}

}

var search = function() {
	$("#AipComponentLogGrid").DataTable().fnSearch();
}

function onExecute(cellvalue) {
	switch (cellvalue) {
	case "SYNC":executeResultRender
		return "同步";
	case "ASYNC":
		return "异步";
	default:
		return cellvalue;
	}
}

function onAuthType(cellvalue) {
	switch (cellvalue) {
	case "NONE":
		return "匿名";
	case "BASIC":
		return "Basic";
	default:
		return "匿名";
	}
}

function aipSucceed(data,id) {
	debugger
	switch (data){
	case "失败":
		//return "<a href='#' style='color:red;'  >" + data + "</a>";
		return '<a href="javascript:void(0)" style="color:red;" onclick="openLogDetailLayer(true, \''+id+ '\')">' + data + '</a>';
	case "成功":
		return '<a href="javascript:void(0)" style="color:green;" onclick="openLogDetailLayer(true, \''+id+ '\')">' + data + '</a>';
	default:
		return "---";
	}

/*	switch (data){
		case "failed":
			//return "<a href='#' style='color:red;'  >" + data + "</a>";
			return '<a href="javascript:void(0)" style="color:red;" onclick="openLogDetailLayer(true, \''+id+ '\')">' + "失败" + '</a>';
		case "successed":
			return '<a href="javascript:void(0)" style="color:green;" onclick="openLogDetailLayer(true, \''+id+ '\')">' + "成功" + '</a>';
		default:
			return "---";
	}*/

}

var openLogDetailLayer = function(update, entityId, updatebtn, readflag) {
	top.layer.open({
		type : 2,
//		title : FORM_TITLE_PRE + '日志详情页面',
		title : FORM_TITLE_PRE + '详情页面',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar : false,
//		maxmin : true, // 开启最大化最小化按钮
//		area : [ '60%', '60%' ],
		area : [ '100%', '100%' ],
		shade : [ 0.3 ],
		// content : 'pages/aipComponentLogs/form.html?isUpdate=' + update+ '&entityId=' + entityId + '&notifyid=' + updatebtn	+ '&readflag=' + readflag + '&v=' + version,
		content : 'assets/html/log_form.scala.html?isUpdate=' + update+ '&entityId=' + entityId + '&notifyid=' + updatebtn	+ '&readflag=' + readflag + '&v=' + version,
	});
}

//毫秒转化成时分秒
function timeStamp(second_time) {
	var time = parseInt(second_time) + "秒";  
	if( parseInt(second_time )> 60){  
	  
	    var second = parseInt(second_time) % 60;  
	    var min = parseInt(second_time / 60);  
	    time = min + "分" + second + "秒";  
	      
	    if( min > 60 ){  
	        min = parseInt(second_time / 60) % 60;  
	        var hour = parseInt( parseInt(second_time / 60) /60 );  
	        time = hour + "小时" + min + "分" + second + "秒";  
	  
	        if( hour > 24 ){  
	            hour = parseInt( parseInt(second_time / 60) /60 ) % 24;  
	            var day = parseInt( parseInt( parseInt(second_time / 60) /60 ) / 24 );  
	            time = day + "天" + hour + "小时" + min + "分" + second + "秒";  
	        }  
	    }  
	      
	  
	}  
	  
	return time;
}