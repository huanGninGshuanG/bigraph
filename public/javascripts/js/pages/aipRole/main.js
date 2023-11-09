//打开layer
var openAipRoleLayer = function(update,entityId,updatebtn){
	var AipRoleIframeWin;
	var AipRoleIsRefresh = false;
	
/*	var params = {"node":nodeId};
	
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}*/
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '角色管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '55%' ],
		shade : [ 0.3 ],
		content : 'pages/aipRole/form.html?isUpdate=' + update + '&entityId=' + entityId + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index,layero) {
			AipRoleIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(window.name,"submitAipRoleForm","aipRoleForm");
		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			AipRoleIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			AipRoleIsRefresh = false;
		},
		success : function(layero, index) {
			AipRoleIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：AipRoleIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(AipRoleIsRefresh)
				$("#aipRoleGrid").DataTable().fnSearch(false);
		}
	});
}
var powerItemForm = function(element){
	var r =$("#aipRoleGrid").DataTable().row($(element).closest("tr")).data();
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '角色管理分配权限页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		maxmin : true, // 开启最大化最小化按钮
		//offset : '80px',
		area : [ '25%', '60%' ],
		shade : [ 0.3 ],
		content : "pages/aipRole/powerForm.html?id="+r.id+ '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index,layero) {
			AipRoleIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			top.layer.confirm('<small>您确定要执行此操作吗?</small>', {
				title : '<small>系统提示</small>',
				closeBtn : 0,
				icon:0,
				// shift: 1, //提示框载入动画
				// skin: 'layui-layer-molv', //样式类名
				btn : [ '确定', '取消' ]
			}, function(index) {
				top.layer.load(1, {
					shade : [ 0.2 ]
					// 透明度调整
				});
				iframeWin.contentWindow.save(index);
			});
			
		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			AipRoleIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			AipRoleIsRefresh = false;
		},
		success : function(layero, index) {
			AipRoleIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：AipRoleIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(AipRoleIsRefresh)
				$("#aipRoleGrid").DataTable().draw(false);
		}
	});
}
var search = function() {
	$("#aipRoleGrid").DataTable().fnSearch();
}
function operateRender(data, type, full) {
	var buttons = [];
	buttons.push('<a onclick="dtOptions.lookup(\''+data+'\', \'pages/aipRole/form.html\',\'角色管理表单页\',60,50)" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openAipRoleLayer(true,\''+data+'\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="deleteSelf(\'../../role/delete.do\', \'' + data + '\', \'aipRoleGrid\')" class="tb_a">删除</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="powerItemForm(this)" class="tb_a">分配权限</a>');
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