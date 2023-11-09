function queryGrid(menuCode) {
	$("#aipNotifyGrid").DataTable().fnSearch();
}

//打开layer
var openAipNotifyLayer = function(update,entityId,updatebtn){
	var aipNotifyIframeWin;
	var aipNotifyIsRefresh = false;

	var	paramStr = CommonUtils.urlEncode();
	
    var arrStep=[{formid:'aipNotifyForm',li:"li1",tab:"tab-1"},{formid:'aipNotifyForm2',li:"li2",tab:"tab-2"}]; 
 
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '消息管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '65%' ],
		shade : [ 0.3 ],
		content : 'pages/aipNotify/form.html?isUpdate=' + update + '&entityId=' + entityId + paramStr  + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index, layero) {
			aipNotifyIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandlerMul(aipNotifyIframeWin,"submitAipNotifyForm",arrStep);
		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			aipNotifyIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			aipNotifyIsRefresh = false;
		},
		success : function(layero, index) {
			aipNotifyIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：AipNotifyIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(aipNotifyIsRefresh)
				$("#aipNotifyGrid").DataTable().fnSearch(false);
		}
	});
}

var search = function() {
	$("#aipNotifyGrid").DataTable().fnSearch();
}

function createDateRender(data, type, full){
	return getSmpFormatDateByLong(data,false);
}

function notifyTypeRender(data, type, full){
	if (data=="NOTIFY_CONFERENCE"){
		return "会议通告";
	}else if (data=="NOTIFY_REWARD_PUNISH"){
		return "奖罚通告";
	}else{
		return "活动通告";
	}
}

function statusRender(data, type, full){
	if (data=="STATUS_DRAFT"){
		return "草稿";
	} else{
		return "发布";
	}
}

function operateRender(data, type, full) {
	var buttons = [];
	buttons.push('<a onclick="dtOptions.lookup(\'' + data + '\', \'pages/aipNotify/form.html\',\'消息管理表单页\',60,40)" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openAipNotifyLayer(true, \'' + data + '\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="deleteSelf(\'../../notify/delete.do\', \'' + data + '\', \'aipNotifyGrid\')" class="tb_a">删除</a>');
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