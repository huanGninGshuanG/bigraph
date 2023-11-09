var settingTreeForStrategyMain = {
	check : {
		enable : false
	},
	async : {
		enable : true,
		type : 'post',
		dataFilter : filterStrategy,
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
		onClick : nodeClickStrategy,
		onRightClick : nodeRightClickStrategy,
		onAsyncSuccess: zTreeOnAsyncSuccessStrategy
	}
};
function filterStrategy(treeId, parentNode, childNodes) {
	if (!childNodes) return null;
	for (var i=0, l=childNodes.length; i<l; i++) {
		childNodes[i].name = childNodes[i].name.replace(/\.n/g, '.');
	}
	return childNodes;
}
function nodeClickStrategy(event, treeId, treeNode, clickFlag) {
	$("#aipStrategyCategoryId").val(treeNode.id);
	$("#aipStrategyCategoryName").val(treeNode.name);
	$("#aipDesourceGrid").DataTable().fnSearch();
}
function nodeRightClickStrategy(event, treeId, treeNode) {

}
function zTreeOnAsyncSuccessStrategy(event, treeId, treeNode, msg) {
	var tree = $.fn.zTree.getZTreeObj(treeId);
	tree.expandAll(true);
	layer.closeAll('loading');
}

// 打开layer
var openAipChannelLayer = function(update, entityId, updatebtn) {
	var AipChannelIframeWin;
	var AipChannelIsRefresh = false;
	var params = {};
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}
	
	//TODO 需要在此处判断是否选择了分类
	var aipCategoryId = $("#aipStrategyCategoryId").val();
	var aipCategoryName = $("#aipStrategyCategoryName").val();
	var arrStep=[{formid:'dxsStrategyForm',li:"li1",tab:"tab-1"},{formid:'dxsStrategyForm2',li:"li2",tab:"tab-2"}]; 
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '策略表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar : false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '55%' ],
		shade : [ 0.3 ],
		// content : 'pages/aipStrategy/form.html?aipCategoryName=' + aipCategoryName + '&aipCategoryId=' + aipCategoryId + '&isUpdate=' + update + '&entityId=' + entityId + paramStr + '&v=' + version,

		// content : '/StrategyForm?aipCategoryName='+ aipCategoryName + '&aipCategoryId=' + aipCategoryId + '&isUpdate=' + update + '&entityId=' + entityId + paramStr + '&v=' + version,

		content : '/assets/html/strategy_form.scala.html?aipCategoryName='+ aipCategoryName + '&aipCategoryId=' + aipCategoryId + '&isUpdate=' + update + '&entityId=' + entityId + paramStr + '&v=' + version,


		btn : [ '保存', '取消' ],
		yes : function(index, layero) {
			AipChannelIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; // 得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
//			iframeWin.contentWindow.FormUtils.submitHandler(AipChannelIframeWin, "submitDxsStrategysForm", "dxsStrategyForm");
			iframeWin.contentWindow.FormUtils.submitHandlerMul(AipChannelIframeWin,"submitDxsStrategysForm",arrStep);
		},
		btn2 : function(index, layero) {
			// 按钮【按钮二】的回调
			AipChannelIsRefresh = false;
		},
		cancel : function() {
			// 右上角关闭回调
			AipChannelIsRefresh = false;
		},
		success : function(layero, index) {
			AipChannelIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：AipChannelIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if (AipChannelIsRefresh)
				$("#aipDesourceGrid").DataTable().draw(false);
		}
	});
}

var search = function() {
	
	if($("#isSelectCategory").val() == "yes"){
		var categoryId = $("#aipStrategyCategoryId").val();
		if(categoryId == null || categoryId == ""){
			CommonUtils.notify("error", "您已锁定分类,请先选择分类！<br>", "2000");
			return;
		}
	}
	$("#aipDesourceGrid").DataTable().fnSearch();
}
function StrategyReset(){
	$('#searchForm')[0].reset();
	$("#aipStrategyCategoryId").val(null);
	$("#aipStrategyCategoryName").val(null);
	$.fn.zTree.getZTreeObj("StrategyCategoryTree").reAsyncChildNodes(null, "refresh");
}
function operateRender(data, type, full) {
	var buttons = [];
	buttons.push('<a onclick="dtOptions.lookup(\'' + data + '\', \'assets/html/strategy_form.scala.html\',\'策略管理表单页\',60,35)" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openAipChannelLayer(true, \'' + data + '\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="deleteSelf(\'../../strategy/delByIds\', \'' + data + '\', \'aipDesourceGrid\')" class="tb_a">删除</a>');
	return buttons.join(' ');
}

var downloadfile = function(id ){
	$("#id").val(id);
	document.form1.submit();
}

function asyncCluster(StrategyCode) {
	openCluster(StrategyCode, asyncFile);
}

function asyncFile(StrategyCode, clusterCode) {
	$.ajax({
		url : '../../Strategy/async.do',
		dataType : 'json',
		type : 'post',
		async : false,
		data : { 'componentCode' : StrategyCode, "clusterCode" : clusterCode},
		success : function(result) {
			if (result.success)
				CommonUtils.notify("success", "操作成功", 1500);
			else
				CommonUtils.notify("error", result.responseMessage, 4000);
		}
	})
}

var openCluster = function(StrategyCode, callback) {
	var requestFrame = top.window[window.name];//取得上层页面的window对象
	top.layer.open({
		type : 2,
		title : '集成组件选择',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : false, // 开启最大化最小化按钮
		shade : [ 0.3 ],
		area : [ "65%", "50%" ],
		shadeClose : false,
		content : "pages/common/cluster_group_select.html",
		btn : ['确定', '取消'],
		yes : function (index, layero) {
			var iframe = layero.find('iframe')[0].contentWindow;
			var obj = iframe.choose();//调用打开页面内函数
			if (typeof callback == "function")
				callback(StrategyCode, obj.code);//上层页面回调
			top.layer.close(index);
		}
		
	});
}


function deleteSelf(url, id, gridId) {
	dtOptions.delete4Self(function(index) {
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
