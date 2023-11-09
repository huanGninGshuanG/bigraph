//打开layer
var openAipQueueConfiglayer = function(update,entityId,updatebtn){
	var params;
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + 'AMQ点对点管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '50%' ],
		shade : [ 0.3 ],
		content : 'pages/aipAmqQueueConfig/form.html?isUpdate=' + update + '&entityId=' + entityId + paramStr + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index,layero) {
			isRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(window.name,"submitAipQConfigForm","aipQConfigForm");
		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			isRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			isRefresh = false;
		},
		success : function(layero, index) {
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(isRefresh)
				$("#aipQueueConfigGrid").DataTable().fnSearch(false);
		}
	});
}

var search = function() {
	$("#aipQueueConfigGrid").DataTable().fnSearch();
}

function changeStatus(target, rowData) {
	layer.load(1, {
		shade : [ 0.2 ]
	});
	var flag = false;
	if (rowData.status != 'ON') {
		flag = true;
	}
	$.ajax({
		url : "../../amq_queue/startOrStopListener.do",
		type : "post",
		data : {
			"id":rowData.id,
			"status":flag
		},
		dataType : "json",
		success : function(data) {
			if (data.success) {
				CommonUtils.notify('success', '操作成功！', 2000);
				dtOptions.reload('aipQueueConfigGrid');
			} else {
				CommonUtils.notify('error', data.responseMessage, 4000);
			}
			layer.closeAll("loading");
		}
	});
}

var cache = layer.cache||{}, skin = function(type){
	  return (cache.skin ? (' ' + cache.skin + ' ' + cache.skin + '-'+type) : '');
	}; 

var select_prompt = function(target, option) {
	var s =$("#aipQueueConfigGrid").DataTable().row($(target).closest("tr")).data();
	changeStatus(target, s);
}
function aipComponentCodeRender(data, type, full) {
	return data.split(';')[0];
}
function targetNodeRender(data, type, full) {
	return data;
}
function isAliveRender(data, type, full) {
	if (data == 'ON') {
		return "<small class='badge badge-primary'>启动中</small>";
	} else {
		return "<small class='badge badge-danger'>停止</small>";
	}
}
function operateRender(data, type, full) {
	var buttons = [];
	buttons.push('<a onclick="dtOptions.lookup(\''+data+'\', \'pages/aipAmqQueueConfig/form.html\',\'AMQ点对点管理表单页\',60,50)" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openAipQueueConfiglayer(true,\''+data+'\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="deleteSelf(\'../../amq_queue/delete.do\', \'' + data + '\', \'aipQueueConfigGrid\')" class="tb_a">删除</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="select_prompt(this)" class="tb_a">执行</a>');
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