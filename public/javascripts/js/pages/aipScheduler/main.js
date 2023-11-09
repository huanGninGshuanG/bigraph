//var setting = {
//	check : {
//		enable : false
//	},
//	async : {
//		enable : true,
//		type : 'post',
//		dataFilter : filter,
//		url : '../../dxsSchedulerGroup//loadAppTree.do'
//	},
//	data : {
//		simpleData : {
//			enable : true,
//			idKey : 'id',
//			pIdKey : 'pId',
//			rootPId : 0
//		},
//		keep : {
//			leaf : false,
//			parent : false
//		}
//	},
//	callback : {
//		onClick : nodeClick,
//		onAsyncSuccess: zTreeOnAsyncSuccess
//	}
//};

var setting = {
		check : {
			enable : false
		},
		async : {
			enable : true,
			type : 'post',
			dataFilter : filter,
			url : '../../dxsSchedulerGroup//loadAppTree.do'
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
	for (var i=0, l=childNodes.length; i<l; i++) {
		childNodes[i].name = childNodes[i].name.replace(/\.n/g, '.');
	}
	return childNodes;
}

function nodeClick(event, treeId, treeNode, clickFlag) {
	queryGrid(treeNode.id);
//	alert(treeNode.name);
}

function queryGrid(menuCode) {
/* 
	GridOptions.clearCachePostData("DxsQrtzGroupGrid");
	$("#DxsQrtzGroupGrid").jqGrid("setGridParam",{datatype:'json',postData:{"dxsSchedulerGroup.id":menuCode,"name":''}}).trigger("reloadGrid");
*/
 
	$("#dxsAppId").val(menuCode);
	$("#DxsQrtzGroupGrid").DataTable().fnSearch();
//	$("#dxsAppId").val('');
	
}

function dateFormatRender(data, type, full) {
 
	return getSmpFormatDateByLong(full.createDate,true);
}


function operateRender(data, type, full) {
	var buttons = [];
	buttons.push('<a onclick="dtOptions.lookup(\'' + data + '\', \'pages/aipScheduler/form.html\',\'调度管理表单页\',50,35)" class="tb_a">查看</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openDxsQrtzGroupLayer(true, \'' + data + '\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="deleteSelf(\'../../dxsSchedulerTask/delete.do\', \'' + data + '\', \'DxsQrtzGroupGrid\')" class="tb_a">删除</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openDxsQrtzTriggersStepLayer(\'true\', \'' + data + '\'   )" class="tb_a">配置</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="openDxsQrtzLogLayer(\'true\', \'' + data + '\'   )" class="tb_a">日志</a>');
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


function zTreeOnAsyncSuccess(event, treeId, treeNode, msg) {
	var tree = $.fn.zTree.getZTreeObj(treeId);
	tree.expandAll(true);
	layer.closeAll('loading');
} 

function onRightClick(event, treeId, treeNode) {


//	if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
//		zTree.cancelSelectedNode();
//		showRMenu("root", event.clientX, event.clientY);
//	} else if (treeNode && !treeNode.noR) {
////		$("#dxsNodeId").val(treeNode.id);
//		zTree.selectNode(treeNode);
//		showRMenu("node", event.clientX, event.clientY);
//	}
	
	 if (treeNode) {
         var top = $(window).scrollTop();
     
         zTree.selectNode(treeNode);//获取选择的节点
         if (treeNode.getParentNode()) {
        	 $("#dxsnodeid").val(treeNode.id);
             var isParent = treeNode.isParent;
             if(isParent){//非叶子节点
                 showRMenu("firstNode", event.clientX, event.clientY+top);//处理位置，使用的是绝对位置
             }else{//叶子节点
                 showRMenu("secondNode", event.clientX, event.clientY+top);
             }
         } else {
             showRMenu("root", event.clientX, event.clientY+top);//根节点
         }
     }
}

function showRMenu(type, x, y) {
	$("#rMenu").show();
	if (type=="root") {
		$("#m_add").show();
		$("#m_upd").hide();
		$("#m_del").hide();
		$("#m_reset").show();
	} else {
		$("#m_add").show();
		$("#m_upd").show();
		$("#m_del").show();
		$("#m_reset").show();
	}
	rMenu.css({"top":y+"px", "left":x+"px", "visibility":"visible"});

	$("body").bind("mousedown", onBodyMouseDown);
}
function hideRMenu() {
	if (rMenu) rMenu.css({"visibility": "hidden"});
	$("body").unbind("mousedown", onBodyMouseDown);
}
function onBodyMouseDown(event){
	if (!(event.target.id == "rMenu" || $(event.target).parents("#rMenu").length>0)) {
		rMenu.css({"visibility" : "hidden"});
	}
}
function resetTree() {
	hideRMenu();
	$.fn.zTree.init($("#menuTree"), setting);
}


//打开layer
var openDxsQrtzGroupLayer = function(update,entityId,updatebtn){
	var DxsQrtzGroupIframeWin;
	var DxsQrtzGroupIsRefresh = false;

	
	var DxsNodeIframeWin;
	var DxsNodeIsRefresh = false;

	var zTree = $.fn.zTree.getZTreeObj("menuTree");
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
		title : FORM_TITLE_PRE + '调度管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '50%', '35%' ],
		shade : [ 0.3 ],
		content : 'pages/aipScheduler/form.html?isUpdate=' + update + '&entityId=' + entityId  + paramStr  + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index, layero) {
			DxsQrtzGroupIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(DxsQrtzGroupIframeWin,"submitDxsQrtzGroupForm","dxsQrtzGroupForm");
		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			DxsQrtzGroupIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			DxsQrtzGroupIsRefresh = false;
		},
		success : function(layero, index) {
			DxsQrtzGroupIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsQrtzGroupIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
//			
			if(DxsQrtzGroupIsRefresh){
//				$("#DxsQrtzGroupGrid").trigger("reloadGrid");
 
//				$("#dxpClusterName").val("");
//				GridOptions.clearCachePostData("DxsQrtzGroupGrid");
//				$("#DxsQrtzGroupGrid").jqGrid("setGridParam",{datatype:'json',postData:{"dxsSchedulerGroup.id":nodeId,"name":''}}).trigger("reloadGrid");
				
				$("#DxsQrtzGroupGrid").DataTable().draw();
			}
		}
	});
}


//打开UpLoadlayer
var openDxsQrtzGroupUploadLayer = function(update,entityId,updatebtn){
	var DxsQrtzGroupIframeWin;
	var DxsQrtzGroupIsRefresh = false;

	
	var DxsNodeIframeWin;
	var DxsNodeIsRefresh = false;

  
	
	
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '文件上传表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '50%' ],
		shade : [ 0.3 ],
		content : 'pages/aipScheduler/formupload.html?v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index, layero) {
			DxsQrtzGroupIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(DxsQrtzGroupIframeWin,"submitDxsQrtzGroupUploadForm","dxsQrtzGroupUploadForm");
		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			DxsQrtzGroupIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			DxsQrtzGroupIsRefresh = false;
		},
		success : function(layero, index) {
			DxsQrtzGroupIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsQrtzGroupIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
//			if(DxsQrtzGroupIsRefresh){
//				$("#DxsQrtzGroupGrid").trigger("reloadGrid");
//			}
		}
	});
}



var search = function() {
//	var name = $("#dxpClusterName").val();
//	GridOptions.clearCachePostData("DxsQrtzGroupGrid");
//	$("#DxsQrtzGroupGrid").jqGrid("setGridParam",{datatype:'json',postData:{"groupName":name}}).trigger("reloadGrid");
	
	
	$("#DxsQrtzGroupGrid").DataTable().fnSearch();
}

//打开layer
var openDxsQrtzTriggersStepLayer = function(update,entityId,updatebtn){
	var DxsQrtzGroupIframeWin;
	var DxsQrtzGroupIsRefresh = false;
	
	

	
	var paramStr = '';

 
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '调度管理配置表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '60%' ],
		shade : [ 0.3 ],
		content : 'pages/aipScheduler/formtrigerstep.html?isUpdate=' + update + '&entityId=' + entityId + paramStr  + '&v=' + version,
		btn : [   '关闭' ],
//		yes : function(index, layero) {
//			DxsQrtzGroupIsRefresh = true;
//			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
//			iframeWin.contentWindow.FormUtils.submitHandler(DxsQrtzGroupIframeWin,"submitDxsQrtzGroupForm","dxsQrtzGroupForm");
//		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			DxsQrtzGroupIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			DxsQrtzGroupIsRefresh = false;
		},
		success : function(layero, index) {
			DxsQrtzGroupIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsQrtzGroupIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(DxsQrtzGroupIsRefresh)
				$("#DxsQrtzGroupGrid").trigger("reloadGrid");
		}
	});
} ;


//var search = function() {
//	var name = $("#dxpClusterName").val();
//	$("#DxsQrtzGroupGrid").jqGrid("setGridParam",{datatype:'json',postData:{"groupName":name}}).trigger("reloadGrid");
//}

//打开layer
var openDxsQrtzLogLayer = function(update,entityId,updatebtn){
	var DxsQrtzGroupIframeWin;
	var DxsQrtzGroupIsRefresh = false;

 
	var paramStr = '';

	
	
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '执行日志',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '60%' ],
		shade : [ 0.3 ],
		content : 'pages/aipScheduler/formlog.html?isUpdate=' + update + '&entityId=' + entityId + paramStr  + '&v=' + version,
		btn : [   '关闭' ],
//		yes : function(index, layero) {
//			DxsQrtzGroupIsRefresh = true;
//			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
//			iframeWin.contentWindow.FormUtils.submitHandler(DxsQrtzGroupIframeWin,"submitDxsQrtzGroupForm","dxsQrtzGroupForm");
//		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			DxsQrtzGroupIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			DxsQrtzGroupIsRefresh = false;
		},
		success : function(layero, index) {
			DxsQrtzGroupIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsQrtzGroupIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(DxsQrtzGroupIsRefresh)
				$("#DxsQrtzGroupGrid").trigger("reloadGrid");
		}
	});
};


//挂起
execTrigger = function(execUrl,gridId,updatebtn){
	
	var ids = $("#" + gridId).jqGrid('getGridParam','selarrrow');
	
	if(ids == null || ids == ""){
		layer.tips('您未选中需要手工执行的记录，请选择！', updatebtn, {
		  tips: [2, '#18a689'],
		  time: 2000
		});
	}else{
 
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
				url : execUrl,
				dataType : 'json',
				type : 'post',
				data : {
					"ids":ids,
				},
				success : function(data) {
					if (data.success) {
						CommonUtils.notify("success", "手工执行任务将在1分后执行", 1500);
						$("#" + gridId).trigger("reloadGrid");
					} else {
						CommonUtils.notify("error", data.responseMessage, 4000);
					}
					layer.closeAll('loading');
					layer.close(confirmId);
				}
			});
		});
	}
};



//====================================================打开集群form表单页面======================================
var opendxsAppLayer = function(update, entityId, updatebtn) {
	hideRMenu();
 
	var DxsAppIframeWin;
	var DxsAppIsRefresh = false;
	if (!update) {
		var zTree = $.fn.zTree.getZTreeObj("menuTree");
		var nodes = zTree.getSelectedNodes();

		if (nodes && nodes.length != 1) {
			layer.tips('您未选择父集群节点！', updatebtn, {
				tips : [ 2, '#18a689' ],
				time : 2000
			});
			return false;
		}

		var nodeId = nodes[0].id;
		
		var nodeName=nodes[0].name;
		
//		alert(nodeId);
//		alert(nodeName);

		var params = {
			"nodeId" : nodeId,
			"nodeName":nodeName
		};
	}

	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}
	var entityId2 = '';
	if (update == "true") {
		entityId2 = $("#dxsnodeid").val();
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
		content : 'pages/aipScheduler/formmenu.html?isUpdate=' + update + '&entityId='
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
			if (DxsAppIsRefresh){}
//				$("#dxsAppGrid").trigger("reloadGrid");
//				zTree.reAsyncChildNodes(null, "refresh");//刷新
		}
	});
};
// ====================================================结束======================================

function reloadZtree(selectNode) {
	var tree = $.fn.zTree.getZTreeObj("menuTree");
	tree.reAsyncChildNodes(null, "refresh");
};


//删除集群节点
var deleteItemFromGPGrid4Single = function(deleteUrl, updatebtn,
		callback) {
 
 
 
		var id = $("#dxsnodeid").val();
		var confirmId = layer.confirm('<small>您确定要执行此操作吗?</small>', {
			title : '<small>系统提示</small>',
			closeBtn : 0,
			icon : 0,
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
							"id" : id,
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
 
};




uploadjar2 = function( updatebtn){
 
		
		var paramStr = '';	
 
		var params = {"entityId":'zd001',"aa":"111","bb":"222","cc":"333","dd":"444"}
	 
		if (params) {
			paramStr = CommonUtils.urlEncode(params);
		}
//		alert('upload success');
		top.layer.open({
			type : 2,
			title : FORM_TITLE_PRE + '文件上传',
			closeBtn : 1, // 不显示关闭按钮
			shadeClose : false,
			shade : false,
			maxmin : true, // 开启最大化最小化按钮
			//offset : '80px',
			area : [ '60%', '60%' ],
			shade : [ 0.3 ],
			content : "pages/aipBootstrapFileInput/main.html?v=" + version +paramStr ,
			btn : [  '关闭' ],
	 
//			yes : function(index, layero) {
//				DxsUserIsRefresh = true;
//				var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
//	 
//	 			
////				var defParameters =iframeWin.contentWindow.save(index);
////				
////				$("#dxsparam").val(defParameters);
//				
//	 
//		 
//			},
			
			
			btn2: function(index, layero){
			    //按钮【按钮二】的回调
				DxsUserIsRefresh = false;
			},
			cancel: function(){ 
			    //右上角关闭回调
				DxsUserIsRefresh = false;
			},
			success : function(layero, index) {
				DxsUserIsRefresh = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsRoleIframeWin.method()
			},
			end : function(index) {
				top.layer.closeAll('loading');
	 
			}
		});
	}
