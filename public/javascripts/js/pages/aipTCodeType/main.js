var setting = {
	check : {
		enable : false
	},
	async : {
		enable : true,
		type : 'post',
		dataFilter : filter,
		url : '../../TCodeType/loadTCodeTypeTree.do'
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
		onRightClick : OnRightClick, 
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
//=======================================================树的基础维护======================================================
function OnRightClick(event, treeId, treeNode) {
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

function showRMenu(type, x, y) {
	$("#rMenu ul").show();
	if (type=="root") {
		$("#m_add").show();
		$("#m_del").hide();
		$("#m_upd").hide();
	} else {
		$("#m_add").hide();
		$("#m_del").show();
		$("#m_upd").show();
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

function resetTree() {
	hideRMenu();
	$.fn.zTree.init($("#appTree"), setting);
}

function addTreeNode(update,entityId,updatebtn) {
	hideRMenu();
	var DxstypeIframeWin;//定义一个窗口对象
	var typeIsRefresh = false;//控制是否需要刷新
	
	var zTree = $.fn.zTree.getZTreeObj("appTree");
	var nodes = zTree.getSelectedNodes();//获取当前节点
	
	var nodeId = nodes[0].id;//获取当前节点id
	
	var params = {"node":nodeId};
	
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '字典类型管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '40%', '40%' ],
		shade : [ 0.3 ],
		content : 'pages/aipTCodeType/typeform.html?isUpdate=' + update + '&entityId=' + entityId + paramStr + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index,layero) {
			typeIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(window.name,"submitAipDataDictionaryForm","aipDataDictionarytypeForm");
		},                                                                                             
		cancel: function(){ 
		    //右上角关闭回调
			typeIsRefresh=false;
		},
		success : function(layero, index) {
			DxstypeIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsUserIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(typeIsRefresh){
				zTree.reAsyncChildNodes(null, "refresh");//刷新
			}
		}
	});
}
function removeTreeNode() {
	hideRMenu();
	var zTree = $.fn.zTree.getZTreeObj("appTree");
	var nodes = zTree.getSelectedNodes();
	if (nodes && nodes.length>0) {
		if (nodes[0].children && nodes[0].children.length > 0) {
			CommonUtils.notify("error","根节点不允许删除<br>","4000");
		} else {
			//zTree.removeNode(nodes[0]);
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
					url : '../../TCodeType/delete.do',
					dataType : 'json',
					type : 'post',
					data : "id=" + nodes[0].id,
					success : function(data) {
						if (data.success) {
							CommonUtils.notify("success","操作成功<br>","1500");
							zTree.reAsyncChildNodes(null, "refresh");
							top.layer.closeAll('loading');
							layer.close(ifram);
						} else {
							CommonUtils.notify("error",data.responseMessage,"4000");
							top.layer.closeAll('loading');
							layer.close(ifram);
						}
					}
				});
			});
		} 
	}
}
function updateTreeNode(update,entityId,updatebtn) {
	hideRMenu();
	var DxstypeIframeWin;
	var typeIsRefresh = false;
	
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
		title : FORM_TITLE_PRE + '字典类型管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '40%', '40%' ],
		shade : [ 0.3 ],
		content : 'pages/aipTCodeType/typeform.html?isUpdate=' + update + '&entityId=' + nodeId + paramStr + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index,layero) {
			typeIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(window.name,"submitAipDataDictionaryForm","aipDataDictionarytypeForm");
		},                                                                                      
		cancel: function(){ 
		    //右上角关闭回调
			typeIsRefresh=false;
		},
		success : function(layero, index) {
			DxstypeIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsUserIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			zTree.reAsyncChildNodes(null, "refresh");//刷新
		}
	});
}
//===================================================================树的基础维护=====================
function zTreeOnAsyncSuccess(event, treeId, treeNode, msg) {
	var tree = $.fn.zTree.getZTreeObj(treeId);
	tree.expandAll(true);
	layer.closeAll('loading');
}

function queryGrid(typeCode) {
	$("#typeCodeId").val(typeCode);
	$("#aipDataDictionaryGrid").DataTable().fnSearch();
}

//打开layer
var openAipDataDictionaryLayer = function(update,entityId,updatebtn){
	var DxsDataDictionaryIframeWin;
	var DxsDataDictionaryIsRefresh = false;
	
	var zTree = $.fn.zTree.getZTreeObj("appTree");
	var nodes = zTree.getSelectedNodes();

	if (nodes && nodes.length != 1) {
		layer.tips('您未选择字典类型！', updatebtn, {
			  tips: [2, '#18a689'],
			  time: 2000
		});
		return false;
	}
	
	var nodeId = nodes[0].id;
	var nodeName = nodes[0].name;
	
	var params = {"node":nodeId,"nodename" : nodeName};
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}
	if("ROOT"==nodeId)
		{
			layer.tips('根节点不可以添加字典！', updatebtn, {
				  tips: [2, '#18a689'],
				  time: 2000
		});
		}else
		{15310801182
			top.layer.open({
				type : 2,
				title : FORM_TITLE_PRE + '数据字典表单页',
				closeBtn : 1, // 不显示关闭按钮
				shadeClose : false,
				shade : false,
				scrollbar: false,
				maxmin : true, // 开启最大化最小化按钮
				area : [ '60%', '45%' ],
				shade : [ 0.3 ],
				content : 'pages/aipTCodeType/form.html?isUpdate=' + update + '&entityId=' + entityId + paramStr + '&v=' + version,
				btn : [ '保存', '取消' ],
				yes : function(index,layero) { 
					DxsDataDictionaryIsRefresh = true;
					var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
					iframeWin.contentWindow.FormUtils.submitHandler(window.name,"submitAipDataDictionaryForm","aipDataDictionaryForm");
				},
				btn2: function(index, layero){
				    //按钮【按钮二】的回调
					DxsDataDictionaryIsRefresh = false;
				},
				cancel: function(){ 
				    //右上角关闭回调
					DxsDataDictionaryIsRefresh = false;
				},
				success : function(layero, index) {
					DxsDataDictionaryIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsUserIframeWin.method()
				},
				end : function(index) {
					top.layer.closeAll('loading');
					if(DxsDataDictionaryIsRefresh)
						//$("#aipDataDictionaryGrid").trigger("reloadGrid");
					$("#aipDataDictionaryGrid").DataTable().draw();
				}
			});
		}
}
function search() {
	$("#dxsDataDictionaryValue").DataTable().fnSearch();
}

function operateRender(data, type, full) {
var buttons = [];
buttons.push('<a onclick="dtOptions.lookup(\'' + data + '\', \'pages/aipTCodeType/form.html\',\'数据字典表单页\',60,45)" class="tb_a">查看</a><i class="tb_i">|</i>');
buttons.push('<a onclick="openAipDataDictionaryLayer(true, \'' + data + '\')" class="tb_a">修改</a><i class="tb_i">|</i>');
buttons.push('<a onclick="deleteSelf(\'../../TCodeValue/delete.do\', \'' + data + '\', \'aipDataDictionaryGrid\')" class="tb_a">删除</a>');
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
