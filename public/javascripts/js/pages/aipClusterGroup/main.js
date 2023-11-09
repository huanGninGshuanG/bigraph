function queryGrid(menuCode) {
	$("#aipClusterGroupGrid").DataTable().fnSearch();
}

// 打开layer
var openAipClusterGroupLayer = function(update, entityId, updatebtn) {
	var AipChannelIframeWin;
	var AipChannelIsRefresh = false;

	var params = {};
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}

	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '集群管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar : false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '60%' ],
		shade : [ 0.3 ],
		content : 'pages/aipClusterGroup/form.html?isUpdate=' + update + '&entityId=' + entityId + paramStr + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index, layero) {
			AipChannelIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; // 得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			//iframeWin.contentWindow.$('#nodesGrid .glyphicon-check').click();
			iframeWin.contentWindow.FormUtils.submitHandler(AipChannelIframeWin, "submitAipClusterGroupForm", "aipClusterGroupForm");
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
				$("#aipClusterGroupGrid").DataTable().draw(false);
		}
	});
}

var search = function() {
	$("#aipClusterGroupGrid").DataTable().fnSearch();
}

function operateRender(data, type, full) {
	var buttons = [];
	buttons.push('<a onclick="dtOptions.lookup(\'' + data + '\', \'pages/aipClusterGroup/form.html\',\'集群管理表单页\',60,60)" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openAipClusterGroupLayer(true, \'' + data + '\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="deleteSelf(\'../../clustergroup/delete.do\', \'' + data + '\', \'aipClusterGroupGrid\')" class="tb_a">删除</a>');
	return buttons.join(' ');
}

function invalidRender(data, type, full) {
	if (data) 
		return "是";
	else
		return "否";
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

function executeEffectting(updatebtn) {
	var ids = $("#aipClusterGroupGrid").DataTable().fnGetSelectedIds();
	
	if(ids == null || ids == ""){
		layer.tips('您未选中需要使其生效的记录，请选择！', updatebtn, {
		  tips: [2, '#18a689'],
		  time: 2000
		});
	} else if (ids.length > 1) {
		layer.tips('生效只能选择单条数据进行操作！', updatebtn, {
			  tips: [2, '#18a689'],
			  time: 2000
			});
	} else {
		var confirmId = layer.confirm('<small>您确定要执行此操作吗?</small>', {
			title : '<small>系统提示</small>',
			closeBtn : 0,
			icon:0,
			offset : '180px',
			btn : [ '确定', '取消' ]
		}, function() {
			layer.load(1, {
				shade : [ 0.2 ]
			});
			
			$.ajax({
				url : '../../clustergroup/effect.do',
				dataType : 'json',
				type : 'post',
				data : {
					"id":ids[0],
				},
				success : function(data) {
					if (data.success) {
						CommonUtils.notify("success", "操作成功", 1500);
						$("#aipClusterGroupGrid").DataTable().fnSearch(false);
					} else {
						CommonUtils.notify("error", data.responseMessage, 4000);
					}
					layer.closeAll('loading');
					layer.close(confirmId);
				}
			});
		});
	}
}
