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
		onAsyncSuccess : zTreeOnAsyncSuccess,
		onRightClick : onRightClick
	}
};

function filter(treeId, parentNode, childNodes) {
	if (!childNodes) return null;
	for ( var i = 0, l = childNodes.length; i < l; i++) {
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
function onRightClick(event, treeId, treeNode) {
	 /*if (treeNode) {
        var top = $(window).scrollTop();
        
        $.fn.zTree.getZTreeObj(treeId).selectNode(treeNode);//获取选择的节点
        
        if (treeNode.getParentNode()) {
            var isParent = treeNode.isParent;
            $("#dxsNodeId").val(treeNode.id);
            if (isParent) {//非叶子节点
                showRMenu("firstNode", event.clientX, event.clientY + top);//处理位置，使用的是绝对位置
            } else {//叶子节点
                showRMenu("secondNode", event.clientX, event.clientY + top);
            }
        } else {
            showRMenu("root", event.clientX, event.clientY + top);//根节点
        }
    }*/
	var zTree = $.fn.zTree.getZTreeObj(treeId);
	if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
		zTree.cancelSelectedNode();
		showRMenu("root", event.clientX, event.clientY);
	} else if (treeNode && !treeNode.noR) {
		$("#dxsNodeId").val(treeNode.id);
		zTree.selectNode(treeNode);
		showRMenu("node", event.clientX, event.clientY);
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
//function resetTree() {
//	hideRMenu();
//	$.fn.zTree.init($("#menuTree"), setting);
//}

function queryGrid(menuCode) {
	$("#dxsAppId").val(menuCode);
	$("#dxsNodeGrid").DataTable().fnSearch();
}

// 打开form页面
var openDxsNodeLayer = function(update, entityId, updatebtn) {
	var DxsNodeIframeWin;
	var DxsNodeIsRefresh = false;

	var zTree = $.fn.zTree.getZTreeObj("appTree");
	var nodes = zTree.getSelectedNodes();

	if (nodes && nodes.length != 1) {
		layer.tips('您未选择所属集群节点！', updatebtn, {
			tips : [ 2, '#18a689' ],
			time : 2000
		});
		return false;
	}

	var nodeId = nodes[0].id;
	var nodeName = nodes[0].name;

	nodeName = encodeURI(encodeURI(nodeName));

	var params = {
		"node" : nodeId,
		"nodename" : nodeName
	};

	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '节点管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar : false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '50%' ],
		shade : [ 0.3 ],
		content : 'pages/aipNode/form.html?isUpdate=' + update + '&entityId='
				+ entityId + paramStr + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index, layero) {
			DxsNodeIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; // 得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(DxsNodeIframeWin,"submitdxsNodeForm", "dxsNodeForm");
		},
		btn2 : function(index, layero) {
			// 按钮【按钮二】的回调
			DxsNodeIsRefresh = false;
		},
		cancel : function() {
			// 右上角关闭回调
			DxsNodeIsRefresh = false;
		},
		success : function(layero, index) {
			DxsNodeIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsNodeIframeWin.method()
		},
		end : function() {
			top.layer.closeAll('loading');
			if (DxsNodeIsRefresh)
				$("#dxsNodeGrid").DataTable().fnSearch(false);
		}
	});
}
// =============左侧集群数的增删改==================
var openDxsAppLayer = function(update, entityId, updatebtn) {
	hideRMenu();
	var DxsAppIframeWin;
	var DxsAppIsRefresh = false;
	if (!update) {
		var zTree = $.fn.zTree.getZTreeObj("appTree");
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
	}

	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}
	var entityId2 = '';
	if (update) {
		var zTree = $.fn.zTree.getZTreeObj("appTree");
		entityId2 = zTree.getSelectedNodes()[0].id;
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
				+ entityId2 + paramStr + '&v=' + version,
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
			if (DxsAppIsRefresh)
				$("#dxsNodeGrid").DataTable().fnSearch(false);
		}
	});
}

// 删除集群节点
var deleteDxsApp = function(deleteUrl, updatebtn, callback) {
	var zTree = $.fn.zTree.getZTreeObj("appTree");
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
function reloadZtree(selectNode) {
	var tree = $.fn.zTree.getZTreeObj("appTree");
	tree.reAsyncChildNodes(null, "refresh");
}

var search = function() {
	$("#dxsNodeGrid").DataTable().fnSearch();
}

function osTypeRender(data, type, full) {
	if(data=="windows"){
		return " <a href='#windows'><i class='fa fa-windows'></i></a>";
	}else{
		return "<a href='#linux'><i class='fa fa-linux'></i></a>";
	}
}
function operateRender(data, type, full) {
	var buttons = [];
	buttons.push('<a onclick="dtOptions.lookup(\'' + data + '\', \'pages/aipNode/form.html\',\'节点管理表单页\',60,50)" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openDxsNodeLayer(true, \'' + data + '\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="deleteSelf(\'../../node/delete.do\', \'' + data + '\', \'dxsNodeGrid\')" class="tb_a">删除</a>');
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