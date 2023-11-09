function queryGrid(menuCode) {
	$("#aipChannelGrid").DataTable().fnSearch();
}

// 打开layer
var openAipChannelLayer = function(update, entityId, updatebtn) {
	var AipChannelIframeWin;
	var AipChannelIsRefresh = false;

	var params = {};
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}

	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '通道管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar : false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '60%' ],
		shade : [ 0.3 ],
		content : 'pages/aipChannel/form.html?isUpdate=' + update + '&entityId=' + entityId + paramStr + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index, layero) {
			AipChannelIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; // 得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(AipChannelIframeWin, "submitAipChannelForm", "aipChannelForm");
		},
		btn2 : function(index, layero) {
			// 按钮【按钮二】的回调
			AipChannelIsRefresh = false;
		},
		cancel : function() {
			// 右上角关闭回调
			AipChannelIsRefresh = false;
		},
		success : function(layero, index) {
			AipChannelIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：AipChannelIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if (AipChannelIsRefresh)
				$("#aipChannelGrid").DataTable().draw(false);
		}
	});
}

var search = function() {
	$("#aipChannelGrid").DataTable().fnSearch();

}

function channelStatusRender(data, type, full) {
	if (data == "STATUS_RUNNING") {
		return "运行中";
	} else {
		return "已停止";
	}
}

function channelTypeRender(data, type, full) {
	if (data == "CHANNELTYPE_USERDEFINE") {
		return "自定义";
	} else if (data == "CHANNELTYPE_NEWCACHEDTHREADPOOL") {
		return "缓冲通道";
	} else if (data == "CHANNELTYPE_NEWSINGLETHREADEXECUTOR") {
		return "队列通道";
	} else {
		return "固定通道";
	}
}

function exceptionStrategyRender(data, type, full) {
	if (data == "STRATEGY_ABORTPOLICY") {
		return "直接拒绝";
	} else if (data == "STRATEGY_DISCARDPOLICY") {
		return "直接丢弃";
	} else {
		return "优先执行";
	}
}

function operateRender(data, type, full) {
	var buttons = [];
	buttons.push('<a onclick="dtOptions.lookup(\'' + data + '\', \'pages/aipChannel/form.html\',\'通道管理表单页\',60,60)" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openAipChannelLayer(true, \'' + data + '\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="deleteSelf(\'../../channel/delete.do\', \'' + data + '\', \'aipChannelGrid\')" class="tb_a">删除</a>');
	return buttons.join(' ');
}
function deleteSelf(url, id, gridId) {
	dtOptions.delete4Self(function(index) {
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
				"ids" : ids,
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
