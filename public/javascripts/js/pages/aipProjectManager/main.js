var setting = {
	check : {
		enable : false
	},
	async : {
		enable : true,
		type : 'post',
		dataFilter : filter,
		url : '../../project/loadAipProjectManagerTree.do'
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
		title : FORM_TITLE_PRE + '项目管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '40%', '40%' ],
		shade : [ 0.3 ],
		content : 'pages/aipProjectManager/form.html?isUpdate=' + update + '&entityId=' + entityId + paramStr + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index,layero) {
			typeIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(window.name,"submitaipProjectForm","aipProjectForm");
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
					url : '../../project/delete.do',
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
		title : FORM_TITLE_PRE + '项目管理表单页',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '40%', '40%' ],
		shade : [ 0.3 ],
		content : 'pages/aipProjectManager/form.html?isUpdate=' + update + '&entityId=' + nodeId + paramStr + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index,layero) {
			typeIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(window.name,"submitaipProjectForm","aipProjectForm");
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
	$("#AipProjectManagerId").val(typeCode);
	$("#aipUserGrid").DataTable().fnSearch();
}
//===============================================================用户维护============================
//打开layer
var openAipUserLayer = function(update,entityId,updatebtn){
	var AipUserIframeWin;
	var AipUserIsRefresh = false;
	
	var zTree = $.fn.zTree.getZTreeObj("appTree");
	var nodes = zTree.getSelectedNodes();
	
	if (nodes && nodes.length != 1) {
		layer.tips('您未选择应用节点！', updatebtn, {
			  tips: [2, '#18a689'],
			  time: 2000
		});
		return false;
	}
	var nodeId = nodes[0].id;
	var nodeName = nodes[0].name;
	var params = {"node":nodeId,
			"nodename" : nodeName};
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}
	if("ROOT"==nodeId)
	{
		layer.tips('根节点不可以添加用户！', updatebtn, {
			  tips: [2, '#18a689'],
			  time: 2000
	});
	}else{

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
		content : 'pages/aipProjectUser/form.html?isUpdate=' + update + '&entityId=' + entityId +paramStr + '&v=' + version,
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
			content : "pages/aipUsers/powerForm.html?id="+r.id+ '&v=' + version,
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
	buttons.push('<a onclick="dtOptions.lookup(\''+data+'\', \'pages/aipProjectUser/form.html\',\'项目管理表单页\',60,65)" class="tb_a">查看</a><i class="tb_i">|</i>');
//	buttons.push('<a onclick="openAipUserLayer(true,\''+data+'\')" class="tb_a">修改</a><i class="tb_i">|</i>');
	buttons.push('<a onclick="powerItemForm(this)" class="tb_a">分配权限</a>');
	return buttons.join(' ');
}