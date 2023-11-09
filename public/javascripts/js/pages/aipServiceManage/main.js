var setting = {
	check : {
		enable : false
	},
	async : {
		enable : true,
		type : 'post',
		dataFilter : filter,
		url : '../../category/categoryTree.do'
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
		onRightClick : nodeRightClick,
		onAsyncSuccess : zTreeOnAsyncSuccess
	}
};


function filter(treeId, parentNode, childNodes) {
	if (!childNodes)
		return null;
	for ( var i = 0, l = childNodes.length; i < l; i++) {
		childNodes[i].name = childNodes[i].name.replace(/\.n/g, '.');
	}
	return childNodes;
}

function nodeClick(event, treeId, treeNode, clickFlag) {
	queryGrid(treeNode.id);
}

function nodeRightClick(event, treeId, treeNode) {
	if (treeNode) {
		var top = $(window).scrollTop();

		$.fn.zTree.getZTreeObj(treeId).selectNode(treeNode);// 获取选择的节点

		if (treeNode.getParentNode()) {
			var isParent = treeNode.isParent;
			if (isParent) {// 非叶子节点
				showRMenu("firstNode", event.clientX, event.clientY + top);// 处理位置，使用的是绝对位置
			} else {// 叶子节点
				showRMenu("secondNode", event.clientX, event.clientY + top);
			}
		} else {
			showRMenu("root", event.clientX, event.clientY + top);// 根节点
		}
	}
}

function zTreeOnAsyncSuccess(event, treeId, treeNode, msg) {
	var tree = $.fn.zTree.getZTreeObj(treeId);
	tree.expandAll(true);
	layer.closeAll('loading');
}

function showRMenu(type, x, y) {
	$("#rMenu ul").show();
	if (type == "root") {
		$("#m_add").show();
		$("#m_del").hide();
		$("#m_upd").hide();
	} else {
		$("#m_add").show();
		$("#m_del").show();
		$("#m_upd").show();
	}
	$("#rMenu").css({
		"top" : y + "px",
		"left" : x + "px",
		"visibility" : "visible"
	});

	$("body").bind("mousedown", onBodyMouseDown);

}

function onBodyMouseDown(event) {
	if (!(event.target.id == "rMenu" || $(event.target).parents("#rMenu").length > 0)) {
		$("#rMenu").css({
			"visibility" : "hidden"
		});
	}
}

function hideRMenu() {
	if (rMenu)
		$("#rMenu").css({
			"visibility" : "hidden"
		});
	$("body").unbind("mousedown", onBodyMouseDown);
}

function queryGrid(menuCode) {
	
	$("#aipCategoryId").val(menuCode);
	$("#aipWebServiceGrid").DataTable().fnSearch();
	
}
// 非节点form表单页面

// 打开layer
var openaipWebServiceLayer = function(update, entityId, updatebtn) {
	var DxsIntegrationAppIframeWin;
	var dxsServiceIsRefresh = false;
	// if (!update) {
	var zTree = $.fn.zTree.getZTreeObj("appTree");
	var nodes = zTree.getSelectedNodes();
	if (nodes && nodes.length != 1) {
		layer.tips('您未选择所属分类！', updatebtn, {
			tips : [ 2, '#18a689' ],
			time : 2000
		});
		return false;
	}
	var nodeId = nodes[0].id;
	var nodeName = nodes[0].name;
	var params = {
		"node" : nodeId,
		"nodeName" : nodeName,
		"mainName" : window.name
	};
	// }

	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}

	top.layer
			.open({
				type : 2,
				title : FORM_TITLE_PRE + '服务管理表单页',
				closeBtn : 1, // 不显示关闭按钮
				shadeClose : false,
				shade : false,
				scrollbar : false,
				maxmin : true, // 开启最大化最小化按钮
				area : [ '54%', '57%' ],
				shade : [ 0.3 ],
				content : 'pages/aipServiceManage/form.html?isUpdate='+ update + '&entityId=' + entityId + paramStr + '&v='+ version,
				btn : [ '保存', '取消' ],
				yes : function(index, layero) {
					DxsWebServiceIsRefresh = true;
					var iframeWin = layero.find('iframe')[0]; // 得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
					iframeWin.contentWindow.FormUtils.submitHandler(window.name, "submitAipIntegrationAppForm","aipIntegrationAppForm");
				},
				btn2 : function(index, layero) {
					// 按钮【按钮二】的回调
					DxsWebServiceIsRefresh = false;
				},
				cancel : function() {
					// 右上角关闭回调
					DxsWebServiceIsRefresh = false;
				},
				success : function(layero, index) {
					DxsIntegrationAppIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsQrtzGroupIframeWin.method()
				},
				end : function(index) {
					top.layer.closeAll('loading');
					if (DxsWebServiceIsRefresh)
						$("#aipWebServiceGrid").DataTable().draw();
				}
			});
}

function search() {
	$("#aipWebServiceGrid").DataTable().fnSearch();
}

function openCategoryNode(update) {
	var isRefresh = false;
	hideRMenu();

	var zTree = $.fn.zTree.getZTreeObj("appTree");
	var nodes = zTree.getSelectedNodes();
	var nodeId = nodes[0].id;

	var params = {
		"node" : nodeId
	};

	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}

	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '类别管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar : false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '50%', '55%' ],
		shade : [ 0.3 ],
		content : 'pages/aipCategory/form.html?isUpdate=' + update
				+ '&entityId=' + nodeId + paramStr + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index, layero) {
			isRefresh = true;
			var iframeWin = layero.find('iframe')[0]; // 得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(window.name,"submitAipCategoryForm", "aipCategoryForm");
					
		},
		cancel : function() {
			// 右上角关闭回调
			isRefresh = false;
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if (isRefresh) {
				zTree.reAsyncChildNodes(null, "refresh");// 刷新
			}
		}
	});
}

function removeCategoryNode() {
	hideRMenu();
	var zTree = $.fn.zTree.getZTreeObj("appTree");

	var nodes = zTree.getSelectedNodes();
	if (nodes && nodes.length > 0) {
		if (nodes[0].children && nodes[0].children.length > 0) {
			CommonUtils.notify("error", "根节点不允许删除<br>", "4000");
		} else {
			var ifram = layer.confirm('<small>您确定要执行此操作吗?</small>', {
				title : '<small>系统提示</small>',
				closeBtn : 0,
				icon : 0,
				btn : [ '确定', '取消' ]
			}, function(index) {
				top.layer.load(1, {
					shade : [ 0.2 ]
				// 透明度调整
				});
				$.ajax({
					url : '../../category/delete.do',
					dataType : 'json',
					type : 'post',
					data : {
						"ids" : nodes[0].id
					},
					success : function(data) {
						if (data.success) {
							CommonUtils.notify("success", "操作成功<br>", "1500");
							zTree.reAsyncChildNodes(null, "refresh");// 刷新
							top.layer.closeAll('loading');
							layer.close(ifram);
						} else {
							CommonUtils.notify("error", data.responseMessage,
									"4000");
							top.layer.closeAll('loading');
							layer.close(ifram);
						}
					}
				});
			});
		}
	}
}

//==============回调函数========================
function wsdlUrlRender(data, type, full) {
		return "<a target='_blank' href='"+data+"'>SOAP</a>";
}

function operateRender(data, type, full) {
	var buttons = [];
	buttons.push('<a onclick="dtOptions.lookup(\'' + data + '\', \'pages/aipServiceManage/form.html\',\'服务管理表单页\',54,57)" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openaipWebServiceLayer(true, \'' + data + '\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="deleteSelf(\'../../servicesManage/delete.do\', \'' + data + '\', \'aipWebServiceGrid\')" class="tb_a">删除</a>');
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
