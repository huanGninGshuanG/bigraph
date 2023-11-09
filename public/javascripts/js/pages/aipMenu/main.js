var setting = {
	check : {
		enable : false
	},
	async : {
		enable : true,
		type : 'get',
		dataFilter : filter,
		url : '../../menu/menuTree'
	},
	data : {
		simpleData : {
			enable : true,
			idKey : 'id',
			pIdKey : 'pId',
			rootPId : 0
		},
		keep : {
			leaf : false,
			parent : false
		}
	},
	callback : {
		onClick : nodeClick,
		onAsyncSuccess: zTreeOnAsyncSuccess
	}
};

function filter(treeId, parentNode, childNodes) {
	if (!childNodes) return null;
	for (var i=0, l=childNodes.length; i<l; i++) {
		childNodes[i].name = childNodes[i].name.replace(/\.n/g, '.');
	}
	return childNodes;
}

function nodeClick(event, treeId, treeNode, clickFlag) {
	queryGrid(treeNode.id);
}

function zTreeOnAsyncSuccess(event, treeId, treeNode, msg) {
	var tree = $.fn.zTree.getZTreeObj(treeId);
	tree.expandAll(true);
	layer.closeAll('loading');
}

function queryGrid(menuCode) {
	$("#parentId").val(menuCode);
	$("#aipMenuGrid").DataTable().fnSearch();
}


//打开layer
var openAipMenuLayer = function(update,entityId,updatebtn){
	var aipMenuIsRefresh = false;
	if (!update) {
		var zTree = $.fn.zTree.getZTreeObj("menuTree");
		var nodes = zTree.getSelectedNodes();
		
		if (nodes && nodes.length != 1) {
			layer.tips('您未选择需要添加菜单的父节点！', updatebtn, {
				tips: [2, '#18a689'],
				time: 2000
			});
			return false;
		}
		
		var nodeId = nodes[0].id;
		
		var params = {"node":nodeId};
	}
	
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '菜单管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '50%' ],
		shade : [ 0.3 ],
		// content : 'pages/aipMenu/form.html?isUpdate=' + update + '&entityId=' + entityId + paramStr + '&v=' + version,
		content : '/assets/html/menu_form.scala.html?isUpdate=' + update + '&entityId=' + entityId + paramStr + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index, layero) {
			aipMenuIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(window.name,"submitAipMenuForm","aipMenuForm");
		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			aipMenuIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			aipMenuIsRefresh = false;
		},
		success : function(layero, index) {
			//DxsMenuIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsMenuIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(aipMenuIsRefresh)
				$("#aipMenuGrid").DataTable().fnSearch(false);
		}
	});
}

var search = function() {
	$("#aipMenuGrid").DataTable().fnSearch();
}

function reloadZtree(){
	var tree = $.fn.zTree.getZTreeObj("menuTree");
	tree.reAsyncChildNodes(null, "refresh");
}

function operateRender(data, type, full) {
	var buttons = [];
	buttons.push('<a onclick="dtOptions.lookup(\''+data+'\', \'assets/html/menu_form.scala.html\',\'菜单管理表单页\',60,50)" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openAipMenuLayer(true,\''+data+'\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="deleteSelf(\'../../menu/delByIds\', \'' + data + '\', \'aipMenuGrid\')" class="tb_a">删除</a>');
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
			data : JSON.stringify({
				"ids":ids,
			}),
			contentType: "application/json;charset=utf-8",
			success : function(data) {
				if (data.success) {
					CommonUtils.notify("success", "操作成功", 1500);
					reloadZtree();
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