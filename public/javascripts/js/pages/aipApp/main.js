var setting = {
	check : {
		enable : false
	},
	async : {
		enable : true,
		type : 'post',
		dataFilter : filter,
		url : '../../app//loadAppTree.do'
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

function queryGrid(appCode) {
	GridOptions.clearCachePostData("dxsAppGrid");
	var name = $("#dxpAppName").val();
	var postData = {
		"name" : name,
		"parent.id" : appCode
	};
	$("#dxsAppGrid").jqGrid("setGridParam",{datatype:'json',postData:postData}).trigger("reloadGrid");
}

//打开layer
var opendxsAppLayer = function(update,entityId,updatebtn){
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
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '集群管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '50%' ],
		shade : [ 0.3 ],
		content : 'pages/aipApp/form.html?isUpdate=' + update + '&entityId=' + entityId + paramStr + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index,layero) {
			DxsAppIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(window.name,"submitDxsAppForm","dxsAppForm");
		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			DxsAppIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			DxsAppIsRefresh = false;
		},
		success : function(layero, index) {
			DxsAppIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsAppIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(DxsAppIsRefresh)
				$("#dxsAppGrid").trigger("reloadGrid");
		}
	});
}

var search = function() {
	var name = $("#dxpAppName").val();
	$("#dxsAppGrid").jqGrid("setGridParam",{postData:{"name":name}}).trigger("reloadGrid");
}

function reloadZtree(selectNode){
	
	var tree = $.fn.zTree.getZTreeObj("appTree");
	tree.reAsyncChildNodes(null, "refresh");
}