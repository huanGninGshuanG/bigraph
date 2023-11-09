var setting = {
	check : {
		enable : false
	},
	async : {
		enable : true,
		type : 'post',
		dataFilter : filter,
		url : '../../app/loadAppTree.do'
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
		onAsyncSuccess: zTreeOnAsyncSuccess,
		onRightClick : onRightClick
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
	//因有级联查询，故虚拟节点不进行查询操作
	if (treeNode.level != 0)
		queryGrid(treeNode.id);
}

function zTreeOnAsyncSuccess(event, treeId, treeNode, msg) {
	var tree = $.fn.zTree.getZTreeObj(treeId);
	tree.expandAll(true);
	layer.closeAll('loading');
}

function onRightClick(event, treeId, treeNode) {
	var zTree = $.fn.zTree.getZTreeObj(treeId);
	var top = $(window).scrollTop();
	if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
		zTree.cancelSelectedNode();
		showRMenu("root", event.clientX, event.clientY + top);
	} else if (treeNode && !treeNode.noR) {
		$("#dxsNodeId").val(treeNode.id);
		zTree.selectNode(treeNode);
		showRMenu("node", event.clientX, event.clientY + top);
	}
}

function showRMenu(type, x, y) {
	$("#rMenu ul").show();
	if (type=="root") {
		$("#m_add").show();
		$("#m_upd").hide();
		$("#m_del").hide();
	} else {
		$("#m_add").show();
		$("#m_upd").show();
		$("#m_del").show();
	}
	$("#rMenu").css({"top":y+"px", "left":x+"px", "visibility":"visible"});

	$("body").bind("mousedown", onBodyMouseDown);
}

function hideRMenu() {
	$("#rMenu").css({"visibility": "hidden"});
	$("body").unbind("mousedown", onBodyMouseDown);
}

function onBodyMouseDown(event){
	if (!(event.target.id == "rMenu" || $(event.target).parents("#rMenu").length>0)) {
		$("#rMenu").css({"visibility" : "hidden"});
	}
}

function queryGrid(menuCode) {
	$("#dxsAppId").val(menuCode);
	$("#dxsServiceGrid").DataTable().fnSearch();
}

//打开layer
var openDxsServiceLayer = function(update,entityId,updatebtn){
	var dxsServiceIsRefresh = false;
	if (!update) {
		var zTree = $.fn.zTree.getZTreeObj("serviceTree");
		var nodes = zTree.getSelectedNodes();
		
		if (nodes && nodes.length != 1) {
			layer.tips('您未选择所属集群！', updatebtn, {
				tips: [2, '#18a689'],
				time: 2000
			});
			return false;
		}
		
		var nodeId = nodes[0].id;
		var nodeName = nodes[0].name;
		
		var params = {"node":nodeId, "nodeName":nodeName, "mainName":window.name};
	}
	
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	} else {
		paramStr = CommonUtils.urlEncode({"mainName":window.name});
	}
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '服务注册表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '50%', '80%' ],
		shade : [ 0.3 ],
		content : 'pages/aipService/form.html?isUpdate=' + update + '&entityId=' + entityId + paramStr + '&v=' + version,
		cancel: function(){ 
		    //右上角关闭回调
			dxsServiceIsRefresh = false;
		},
		success : function(layero, index) {
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(dxsServiceIsRefresh)
				$("#dxsServiceGrid").DataTable().fnSearch(false);
		}
	});
}

var search = function() {
	$("#dxsServiceGrid").DataTable().fnSearch();
}

function authTypeRender(data, type, full) {
	var str;
	switch(data){
		case "NONE" : str = "匿名"; break;
		case "BASIC" : str =  "Basic Auth"; break;
		default : break;
	}
	return str;
}

function strategyTypeRender(data, type, full){
	var str;
	switch (data) {
		case "polling": str = "轮询 "; break;
		case "polling_weight": str = "权重 "; break;
		case "hash": str = "哈希"; break;
		case "random": str = "随机"; break;
		default: break;
	}
	return str;
}

function hostRender(data, type, full) {
	data = data || "";
	return '<span title="' + data + '">' + data + '</span>';
}

function operateRender(data, type, full) {
	var buttons = [];
//	buttons.push('<a onclick="dtOptions.lookup(\'' + data + '\', \'pages/dxsService/form.html\')" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openDxsServiceLayer(true, \'' + data + '\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="deleteSelf(\'../../service/delete.do\', \'' + data + '\', \'dxsServiceGrid\')" class="tb_a">删除</a>');
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

//=============左侧集群数的增删改==================
var openDxsAppLayer = function(update, entityId, updatebtn) {
	hideRMenu();
	var DxsAppIframeWin;
	var DxsAppIsRefresh = false;
	var zTree = $.fn.zTree.getZTreeObj("serviceTree");
	if (!update) {
		var nodes = zTree.getSelectedNodes();
		if (nodes && nodes.length != 1) {
			layer.tips('您未选择父集群节点！', updatebtn, {
				tips : [ 2, '#18a689' ],
				time : 2000
			});
			return false;
		}

		var nodeId = nodes[0].id;

		var params = {
			"node" : nodeId
		};
	} else {
		entityId = zTree.getSelectedNodes()[0].id;
	}

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
		area : [ '60%', '50%' ],
		shade : [ 0.3 ],
		content : 'pages/aipApp/form.html?isUpdate=' + update + '&entityId='
				+ entityId + paramStr + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index, layero) {
			DxsAppIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; // 得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(window.name,
					"submitDxsAppForm", "dxsAppForm");
		},
		btn2 : function(index, layero) {
			// 按钮【按钮二】的回调
			DxsAppIsRefresh = false;
		},
		cancel : function() {
			// 右上角关闭回调
			DxsAppIsRefresh = false;
		},
		success : function(layero, index) {
			DxsAppIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsAppIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if (DxsAppIsRefresh) {
			}
		}
	});
}

//删除集群节点
var deleteDxsApp = function(deleteUrl, updatebtn, callback) {
	var zTree = $.fn.zTree.getZTreeObj("serviceTree");
	var ids = zTree.getSelectedNodes()[0].id;
	var confirmId = layer.confirm('<small>您确定要执行此操作吗?</small>', {
		title : '<small>系统提示</small>',
		closeBtn : 0,
		icon : 0,
		offset : '180px',
		btn : [ '确定', '取消' ]
	}, function() {
		layer.load(1, {
			shade : [ 0.2 ]// 透明度调整
		});

		$.ajax({
			url : "../../app/delete.do",
			dataType : 'json',
			type : 'post',
			data : {
				"ids" : ids,
			},
			success : function(data) {
				if (data.success) {
					CommonUtils.notify("success", "操作成功", 1500);
					if (typeof callback == "function")
						new callback();
				} else {
					CommonUtils.notify("error",
							data.responseMessage, 4000);
				}
				layer.closeAll('loading');
				layer.close(confirmId);
			}
		});
	});
}

// 刷新ztree
function reloadZtree() {
	var tree = $.fn.zTree.getZTreeObj("serviceTree");
	tree.reAsyncChildNodes(null, "refresh");
}