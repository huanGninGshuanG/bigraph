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

function nodeRightClick(event, treeId, treeNode) {
	 if (treeNode) {
        var top = $(window).scrollTop();
        
        $.fn.zTree.getZTreeObj(treeId).selectNode(treeNode);//获取选择的节点
        
        if (treeNode.getParentNode()) {
            var isParent = treeNode.isParent;
            if (isParent) {//非叶子节点
                showRMenu("firstNode", event.clientX, event.clientY+top);//处理位置，使用的是绝对位置
            } else {//叶子节点
                showRMenu("secondNode", event.clientX, event.clientY+top);
            }
        } else {
            showRMenu("root", event.clientX, event.clientY+top);//根节点
        }
    }
}

function zTreeOnAsyncSuccess(event, treeId, treeNode, msg) {
	var tree = $.fn.zTree.getZTreeObj(treeId);
//	var top = {'id':'0','name':'组件分类'};
//	tree.addNodes(null, top)
	tree.expandAll(true);
	layer.closeAll('loading');
}

function showRMenu(type, x, y) {
	$("#rMenu ul").show();
	if (type=="root") {
		$("#m_add").show();
		$("#m_del").hide();
		$("#m_upd").hide();
	} else {
		$("#m_add").show();
		$("#m_del").show();
		$("#m_upd").show();
	}
	$("#rMenu").css({"top":y+"px", "left":x+"px", "visibility":"visible"});

	$("body").bind("mousedown", onBodyMouseDown);
		
}

function onBodyMouseDown(event){
	if (!(event.target.id == "rMenu" || $(event.target).parents("#rMenu").length>0)) {
		$("#rMenu").css({"visibility" : "hidden"});
	}
}

function hideRMenu() {
	if (rMenu) $("#rMenu").css({"visibility": "hidden"});
	$("body").unbind("mousedown", onBodyMouseDown);
}

function queryGrid(menuCode) {
	$("#aipCategoryId").val(menuCode);
	$("#aipComponentKettleGrid").DataTable().fnSearch();
}

//打开layer
var openAipComponentKettleLayer = function(update,entityId,updatebtn){
	var dxsServiceIsRefresh = false;
	if (!update) {
		var zTree = $.fn.zTree.getZTreeObj("appTree");
		var nodes = zTree.getSelectedNodes();
		
		if (nodes && nodes.length != 1) {
			layer.tips('您未选择服务分类！', updatebtn, {
				tips: [2, '#18a689'],
				time: 2000
			});
			return false;
		}
		
		var nodeId = nodes[0].id;
		
		if (nodeId == -'1') {
			layer.tips('顶级分类不能添加组件！', updatebtn, {
				tips: [2, '#18a689'],
				time: 2000
			});
			return false;
		}
		
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
		title : FORM_TITLE_PRE + '服务池表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '82%' ],
		shade : [ 0.3 ],
		content : 'pages/aipComponentKettle/form.html?isUpdate=' + update + '&entityId=' + entityId + paramStr + '&v=' + version,
		cancel: function(){ 
		    //右上角关闭回调
			dxsServiceIsRefresh = false;
		},
		success : function(layero, index) {
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(dxsServiceIsRefresh)
				$("#aipComponentKettleGrid").DataTable().fnSearch(false);
		}
	});
}

function search() {
	$("#aipComponentKettleGrid").DataTable().fnSearch();
}

function showHttpAddress(id) {
	var table = $("#aipComponentKettleGrid").DataTable();
	var row = table.row($(':checkbox[value="'+id+'"]').closest('tr')[0]).data();
	var JSON = "JSON : " + row.http_json;
	var XML = "XML : " + row.http_xml;
	layer.msg(JSON + "<br>" + XML, {
	    time: 20000, //20s后自动关闭
	    area : [ '40%' ],
	    btn: ['知道了']
	});
}

function openCategoryNode(update) {
	var isRefresh = false;
	hideRMenu();
	
	var zTree = $.fn.zTree.getZTreeObj("appTree");
	var nodes = zTree.getSelectedNodes();
	var nodeId = nodes[0].id;
	
	var params = {"node":nodeId};
	
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
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '50%', '55%' ],
		shade : [ 0.3 ],
		content : 'pages/aipCategory/form.html?isUpdate=' + update + '&entityId=' + nodeId  + paramStr + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index,layero) {
			isRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(window.name,"submitAipCategoryForm","aipCategoryForm");
		},
		cancel: function(){ 
		    //右上角关闭回调
			isRefresh=false;
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(isRefresh){
				zTree.reAsyncChildNodes(null, "refresh");//刷新
			}
		}
	});
}

function removeCategoryNode() {
	hideRMenu();
	var zTree = $.fn.zTree.getZTreeObj("appTree");
	
	var nodes = zTree.getSelectedNodes();
	if (nodes && nodes.length>0) {
		if (nodes[0].children && nodes[0].children.length > 0) {
			CommonUtils.notify("error","根节点不允许删除<br>","4000");
		} else {
			var ifram = layer.confirm('<small>您确定要执行此操作吗?</small>', {
				title : '<small>系统提示</small>',
				closeBtn : 0,
				icon:0,
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
					data : {"ids" : nodes[0].id},
					success : function(data) {
						if (data.success) {
							CommonUtils.notify("success", "操作成功<br>", "1500");
							zTree.reAsyncChildNodes(null, "refresh");//刷新
							top.layer.closeAll('loading');
							layer.close(ifram);
						} else {
							CommonUtils.notify("error", data.responseMessage, "4000");
							top.layer.closeAll('loading');
							layer.close(ifram);
						}
					}
				});
			});
		} 
	}
}

function componentTypeRender(data, type, full) {
	switch(data){
	case "TRANS":return "转换";
	case "JOB":return "作业";
	default: return "转换";
	}
}
	
function authTypeRender(data, type, full) {
	var str;
	if (data == "NONE")
		str = "匿名";
	if (data == "BASIC")
		str = "BASIC";
	return str;
}

function executeTypeRender(data, type, full) {
	switch(data){
	case "SYNC":return "同步";
	case "ASYNC":return "异步";
	default: return "同步";
	}
}

function wsdlUrlRender(data, type, full) {
	if (full.complex)
		return "<a target='_blank' href='"+data+"'>SOAP</a>";
	else
		return "<a target='_blank' href='"+data+"'>SOAP</a>　<a href='javascript:void(0)' onclick=showHttpAddress('"+full.id+"')>HTTP</a>";
}

function operateRender(data, type, full) {
	var buttons = [];
	//buttons.push('<a onclick="dtOptions.lookup(\'' + data + '\', \'pages/dxsAIPComponentKettle/form.html\')" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openAipComponentKettleLayer(true, \'' + data + '\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="deleteSelf(\'../../aipcomponent/delete.do\', \'' + data + '\', \'aipComponentKettleGrid\')" class="tb_a">删除</a>');
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

