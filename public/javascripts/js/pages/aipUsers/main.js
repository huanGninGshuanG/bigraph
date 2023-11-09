//打开layer
var openAipUserLayer = function(update,entityId,updatebtn){
	var AipUserIframeWin;
	var AipUserIsRefresh = false;
	
	/*var params = {"node":nodeId};
	
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}*/
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '用户管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '55%' ],
		shade : [ 0.3 ],
		// content : 'pages/aipUsers/form.html?isUpdate=' + update + '&entityId=' + entityId + '&v=' + version,
		content : '/assets/html/user_form.scala.html?isUpdate=' + update + '&entityId=' + entityId + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index,layero) {
			AipUserIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(window.name,"submitAipUserForm","aipUserForm");
		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			AipUserIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			AipUserIsRefresh = false;
		},
		success : function(layero, index) {
			AipUserIsRefresh = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：AipUserIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(AipUserIsRefresh)
				$("#aipUserGrid").DataTable().fnSearch(false);
		}
	});
}

var powerItemForm = function(element){
	var AipUserIframeWin;
	var AipUserIsRefresh = false;
	var r =$("#aipUserGrid").DataTable().row($(element).closest("tr")).data();
	if(r.userType=="RESPOND"){
		layer.tips('通讯用户不能分配权限！', element, {
			  tips: [2, '#18a689'],
			  time: 2000
		});
	} else {
		top.layer.open({
			type : 2,
			title : FORM_TITLE_PRE + '用户管理分配权限页面',
			closeBtn : 1, // 不显示关闭按钮
			shadeClose : false,
			shade : false,
			maxmin : true, // 开启最大化最小化按钮
			//offset : '80px',
			area : [ '25%', '60%' ],
			shade : [ 0.3 ],
			// content : "pages/aipUsers/powerForm.html?id="+r.id+ '&v=' + version,
			content : "/assets/html/user_powerForm.scala.html?id="+r.id+ '&v=' + version,

			btn : [ '保存', '取消' ],
			yes : function(index,layero) {
				AipUserIsRefresh = true;
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
				AipUserIsRefresh = false;
			},
			cancel: function(){ 
			    //右上角关闭回调
				AipUserIsRefresh = false;
			},
			success : function(layero, index) {
				AipUserIsRefresh = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：AipRoleIframeWin.method()
			},
			end : function(index) {
				top.layer.closeAll('loading');
				if(AipUserIsRefresh)
					$("#aipUserGrid").DataTable().draw(false);
			}
		});
	}
}

var search = function() {
	$("#aipUserGrid").DataTable().fnSearch();
}
function invalidRender(data, type, full) {
	if(data){
		return "<small class='badge badge-danger'>禁用</small>";
	}else{
		return "<small class='badge badge-primary'>启用</small>";
	}
}
function userTypeRender(data, type, full) {
	if(data=='SYSTEM'){
		return "系统用户";
	}else{
		return "通讯用户";
	}
}
function expireTimeRender(data, type, full) {
	return getSmpFormatDateByLong(data,false);
}
function operateRender(data, type, full) {
	var buttons = [];
	//dtOptions.lookup--形参：data，url，name，width，height
	// buttons.push('<a onclick="dtOptions.lookup(\''+data+'\', \'pages/aipUsers/form.html\',\'用户管理表单页\',60,70)" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="dtOptions.lookup(\''+data+'\', \'assets/html/user_form.scala.html\',\'用户管理表单页\',60,70)" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openAipUserLayer(true,\''+data+'\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="powerItemForm(this)" class="tb_a">分配权限</a>');
	return buttons.join(' ');
}
