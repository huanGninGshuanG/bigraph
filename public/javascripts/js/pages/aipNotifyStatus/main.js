//打开layer
var openDxsNotifyLayer = function(update,entityId,updatebtn,readflag){
//	 alert(entityId);
//	 alert(updatebtn);
	var DxsNotifyIframeWin;
	var DxsNotifyIsRefresh = false;
 	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '我的消息表单页',		
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '60%' ],
		shade : [ 0.3 ],
		content : 'pages/aipNotifyStatus/form.html?isUpdate=' + update + '&entityId=' + entityId  + '&notifyid=' + updatebtn  + '&readflag=' + readflag   + '&v=' + version,
		btn : [ '确认' ],
 
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			DxsNotifyIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			DxsNotifyIsRefresh = false;
		},
		success : function(layero, index) {
			DxsNotifyIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsNotifyIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			$("#DxsNotifyGrid").DataTable().fnSearch(false);
		}
	});
}

var deleteItemFromGPGridNotifyStatus = function(deleteUrl,gridId,updatebtn){
	var ids = $("#DxsNotifyGrid").DataTable().fnGetSelectedIds()
	if(ids == null || ids == ""){
		layer.tips('您未选中需要删除的记录，请选择！', updatebtn, {
		  tips: [2, '#18a689'],
		  time: 2000
		});
	}else{
		for ( var i = 0; i < ids.length; i++) {
			var id = ids[i];
			var model = $("#DxsNotifyGrid").DataTable().fnGetRowById(id);
			if(!model.readflag){
				layer.tips('未阅读的消息不能删除！', updatebtn, {
					  tips: [2, '#18a689'],
					  time: 2000
					});
				return;
			}			
		}
		var confirmId = layer.confirm('<small>您确定要执行此操作吗?</small>', {
			title : '<small>系统提示</small>',
			// skin: 'layui-layer-molv', //样式类名
			closeBtn : 0,
			icon:0,
			offset : '180px',
			// shift: 1, //提示框载入动画
			btn : [ '确定', '取消' ]
		// 按钮
		}, function() {
			layer.load(1, {
				shade : [ 0.2 ]
			// 透明度调整
			});
			
			$.ajax({
				url : deleteUrl,
				dataType : 'json',
				type : 'post',
				data : {
					"ids":ids,
				},
				success : function(data) {
					if (data.success) {
						CommonUtils.notify("success", "操作成功", 1500);
						$("#DxsNotifyGrid").DataTable().draw(false);
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

var search = function() {
	$("#DxsNotifyGrid").DataTable().fnSearch();
}
function readflagRender(data, type, full) {
	if(data){
		return "<small class='badge badge-primary'>已读</small>";
	}else{
		return "<small class='badge badge-danger'>未读</small>";
	}
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
function createDateRender(data, type, full) {
	return getSmpFormatDateByLong(data,false);
}
function operateRender(data, type, full) {
	var buttons = [];
	buttons.push('<a onclick="openDxsNotifyLayer(true,\''+full.id+'\',\''+data+'\',\''+full.readflag+'\')" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="deleteSelf(\'../../notifystatus/delete.do\', \'' + full.id + '\', \'DxsNotifyGrid\')" class="tb_a">删除</a>');
	return buttons.join(' ');
}
function deleteSelf(url, id, gridId) {
	
	var rowData = $("#DxsNotifyGrid").DataTable().fnGetRowById(id);
	console.info(rowData);
	if(!rowData.readflag){
		/*layer.tips('未阅读的消息不能删除！', this, {
			  tips: [2, '#18a689'],
			  time: 2000
		});*/
		CommonUtils.notify("error", "未阅读的消息不能删除！", "4000");
	}else{
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
}