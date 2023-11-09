 //打开layer
var openAipIntegrationAppLayer = function(update,entityId,updatebtn){
	var AipIntegrationAppIframeWin;
	var AipIntegrationAppIsRefresh = false;

	var paramStr = '';
	
	paramStr = CommonUtils.urlEncode();
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '应用管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '55%' ],
		shade : [ 0.3 ],
		content : 'pages/aipIntegrationApp/form.html?isUpdate=' + update + '&entityId=' + entityId + paramStr  + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index, layero) {
			AipIntegrationAppIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(AipIntegrationAppIframeWin,"submitAipIntegrationAppForm","aipIntegrationAppForm");
		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			AipIntegrationAppIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			AipIntegrationAppIsRefresh = false;
		},
		success : function(layero, index) {
			AipIntegrationAppIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsQrtzGroupIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(AipIntegrationAppIsRefresh)
				$("#aipIntegrationAppGrid").DataTable().fnSearch(!update);
		}
	});
}

var search = function() {
	$("#aipIntegrationAppGrid").DataTable().fnSearch();
}

function operateRender(data, type, full) {
	var buttons = [];
	buttons.push('<a onclick="dtOptions.lookup(\'' + data + '\', \'pages/aipIntegrationApp/form.html\',\'应用管理表单页\',60,55)" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openAipIntegrationAppLayer(true, \'' + data + '\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="deleteSelf(\'../../integrationapp/delete.do\', \'' + data + '\', \'aipIntegrationAppGrid\')" class="tb_a">删除</a>');
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