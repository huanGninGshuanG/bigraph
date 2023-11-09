 

function queryGrid() {
 
	$("#DxsGrid").DataTable().fnSearch();
}

function dateFormatRender(data, type, full) {
 
	return getSmpFormatDateByLong(full.createDate,true);
}

function  uploadtypeRender(data, type, full) {
	 if (data=="local"){
		 return "本地"
	 }else if (data=="ftp"){
		 return "FTP"
	 }else if (data=="stream"){
		 return "文件流"
	 }
 
}


function  judgeRender(data, type, full) {
	 if (data=="1"){
		 return "有效"
	 }else{
		 return "无效"
	 }

}

function operateRender(data, type, full) {
	var buttons = [];
	buttons.push('<a onclick="dtOptions.lookup(\'' + data + '\', \'pages/aipFileUpload/form.html\',\'文件上传管理表单页\',60,50)" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openDxsFileUploadLayer(true, \'' + data + '\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="deleteSelf(\'../../dxsfileupload/delete.do\', \'' + data + '\', \'DxsGrid\')" class="tb_a">删除</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openDxsQrtzFileLayer(\'true\', \'' + data + '\'   )" class="tb_a">明细</a>');
	return buttons.join(' ');
}

function operateRender2(data, type, full) {
	var buttons = [];
 
	buttons.push('<a onclick="deleteSelf(\'../../dxsfileuploadManger/delete.do\', \'' + data + '\', \'DxsQrtzGroupGrid\')" class="tb_a">删除</a>');
	return buttons.join(' ');
}

function deleteSelf(url, id, gridId) {
	dtOptions.delete4Self(function(index){
		layer.load(1, {
			shade : [ 0.2 ]
		});
		var ids = [];
		ids.push(id);
		$.ajax({
			url : url,
			dataType : 'json',
			type : 'post',
			data : {
				"ids":ids,
			},
			success : function(data) {
				if (data.success) {
					CommonUtils.notify("success", "操作成功", 1500);
					dtOptions.reload(gridId);
				} else {
					CommonUtils.notify("error", data.responseMessage, 4000);
				}
				layer.closeAll('loading');
				layer.close(index);
			}
		});
	});
}


var openDxsQrtzFileLayer = function(update,entityId,updatebtn){
	var DxsQrtzGroupIframeWin;
	var DxsQrtzGroupIsRefresh = false;

 
	var paramStr = '';

	
	
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '文件明细',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '60%' ],
		shade : [ 0.3 ],
		content : 'pages/aipFileUpload/formfile.html?isUpdate=' + update + '&entityId=' + entityId + paramStr  + '&v=' + version,
		btn : [   '关闭' ],
//		yes : function(index, layero) {
//			DxsQrtzGroupIsRefresh = true;
//			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
//			iframeWin.contentWindow.FormUtils.submitHandler(DxsQrtzGroupIframeWin,"submitDxsQrtzGroupForm","dxsQrtzGroupForm");
//		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			DxsQrtzGroupIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			DxsQrtzGroupIsRefresh = false;
		},
		success : function(layero, index) {
			DxsQrtzGroupIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsQrtzGroupIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(DxsQrtzGroupIsRefresh)
				$("#DxsQrtzGroupGrid").trigger("reloadGrid");
		}
	});
};

//打开layer
var openDxsFileUploadLayer = function(update,entityId,updatebtn){
	var DxsQrtzGroupIframeWin;
	var DxsQrtzGroupIsRefresh = false;

	
	var DxsNodeIframeWin;
	var DxsNodeIsRefresh = false;

 	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '文件上传管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '50%' ],
		shade : [ 0.3 ],
		content : 'pages/aipFileUpload/form.html?isUpdate=' + update + '&entityId=' + entityId  + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index, layero) {
			DxsQrtzGroupIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(DxsQrtzGroupIframeWin,"submitDxsFileUploadForm","dxsUploadForm");
		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			DxsQrtzGroupIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			DxsQrtzGroupIsRefresh = false;
		},
		success : function(layero, index) {
			DxsQrtzGroupIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsQrtzGroupIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
 		
			if(DxsQrtzGroupIsRefresh){
 			
				$("#DxsGrid").DataTable().draw();
			}
		}
	});
}

var initdxsQrtzLogFormNew = function(){
	var entityId = CommonUtils.getUrlParam("entityId");
	$("#dxsHiddenFileUploadId").val(entityId);
//	$("#dxsbeginTime").val(GetDateStr(-1));
//	$("#dxsendTime").val(GetDateStr(0));
	$("#DxsQrtzGroupGrid").DataTable().fnSearch();
		
}	; 



var search = function() {
//	var name = $("#dxpClusterName").val();
//	GridOptions.clearCachePostData("DxsQrtzGroupGrid");
//	$("#DxsQrtzGroupGrid").jqGrid("setGridParam",{datatype:'json',postData:{"groupName":name}}).trigger("reloadGrid");
	
	
	$("#DxsGrid").DataTable().fnSearch();
}

 
 