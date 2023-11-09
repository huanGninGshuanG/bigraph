$("#dxsAppForm").validate({
	rules : {
		code : {
			required : true,
			minlength : 2
		},
		name : {
			required : true,
			stringCheck:true
		},
		accessUrl : {
			required : true
		},
		accessProtocol : {
			required : true
		},
		serviceName : {
			required : true
		},
		accessPort : {
			required : true,
			digits : true,
			maxlength : 6
		},
		orderNo : {
			required : true,
			digits : true,
			maxlength : 8
		}
	},
	messages : {
		code : {
			required : "请输入集群编码",
			minlength : "集群编号至少由两个字母组成"
		},
		name : {
			required : "请输入集群名称"
		},
		accessUrl : {
			required : "请输入集群地址",
		},
		accessProtocol : {
			required : "请输入访问协议",
		},
		serviceName : {
			required : "请输入服务名称",
		},
		accessPort : {
			required : "请输入集群端口",
			digits : "集群端口必须为整数",
			maxlength : "集群端口最大不能超过6位数"
		},
		orderNo : {
			required : "请输入排序号",
			digits : "排序号码必须为整数",
			maxlength : "排序号码最大不能超过8位数"
		}
	},
	onfocusout : function(element) {
		$(element).valid();
	}
});

var submitDxsAppForm = function(formWinIndex,confirmIndex, mainName){
	
	var gridList = $("#nodesGrid").jqGrid("getRowData");
	var grid = [];
	$.each(gridList, function() {
		var obj = {
				ip : this.ip,
				servicePort : this.servicePort
		}
		grid.push(obj);
	});
	
	var body = FormUtils.serializeJson("dxsAppForm");
	
	body.nodes = JSON.stringify(grid);
	
	$.ajax({
		url : '../../app/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : body,
		success : function(data) {
			if (data.success) {
				CommonUtils.notify("success","操作成功<br>","1500");
				
				var formWin = top.window.frames[mainName]; //得到main页面窗体 formWin.method();
				formWin.reloadZtree();
				
				top.layer.close(confirmIndex);
				top.layer.close(formWinIndex); // 再执行关闭
			} else {
				CommonUtils.notify("error",data.responseMessage,"4000");
				top.layer.closeAll('loading');
				top.layer.close(confirmIndex);
			}
		}
	});
}


var initdxsAppForm = function(){
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	var parentNode = CommonUtils.getUrlParam("node");
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../app/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			success : function(data) {
				if (data.success) {
					FormUtils.fillFormByData("dxsAppForm",data.responseData);
					initParentAppNodes(data.responseData.parentId, data.responseData);
					var nodes = JSON.parse(data.responseData.nodes);
					$.each(nodes, function(i){
						var obj = {
								orderNo : i + 1,
								ip : this.ip,
								servicePort : this.servicePort
						};
						$("#nodesGrid").jqGrid("addRowData", i + 1, obj, "last");
					})
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("dxsAppForm", {id:''});
		initParentAppNodes(parentNode);
		$.ajax({
			url : '../../clustergroup/getEffectiveOne.do',
			dataType : 'json',
			type : 'post',
			success : function(data) {
				if (data.success) {
					$("#dxsAppAccessProtocol").val(data.responseData.accessProtocol);
					$("#dxsAppAccessUrl").val(data.responseData.ip);
					$("#dxsAppAccessPort").val(data.responseData.port);
					$("#dxsAppServiceName").val(data.responseData.contextPath);
					try {
						var nodes = JSON.parse(data.responseData.node);
						$.each(nodes, function(i){
							var obj = {
									orderNo : i + 1,
									ip : this.host,
									servicePort : this.port
							};
							$("#nodesGrid").jqGrid("addRowData", i + 1, obj, "last");
						})
					} catch(e) {}
				}
				layer.closeAll('loading');
			}
		})
	}
}

function initParentAppNodes(parentNode, responseData){
	$.ajax({
		url : "../../app/sameLevelNodes.do",
		async : false,
		type : "post",
		data : {"id":parentNode},
		dataType : "json",
		success : function (data) {
			if (data.success) {
				var parentApp = $("#dxsAppParentId");
				$.each(data.responseData, function(){
					var option = "<option value='"+this.id+"'>"+this.name+"</option>";
					parentApp.append(option);
				})
				if (responseData)
					parentApp.val(responseData.parentId);
				else
					parentApp.val(parentNode)
			}
		}
	})
}

var initNodesGrid = function () {
	$("#nodesGrid").jqGrid({
		data:[],
		datatype : "local",
		scroll:1,//虚拟滚动条
		shrinkToFit: true, 
		rownumbers : false,
		autowidth : true,
		colNames : [ "序号","IP","端口","操作"],
		colModel : [
		    {name : 'orderNo', index : 'orderNo', width:30},
		    {name : 'ip', index : 'ip', editable:true},
		    {name : 'servicePort', index : 'servicePort', editable:true},
		    {name : 'opera', index : 'opera', width: 20},
		],
		viewrecords : true,
		hidegrid : false,
		pager : "#pager_list_nodesGrid",
		caption : '<a href="javascript:void(0)" title="新增记录" style="color:#000000;text-decoration: none" class="glyphicon glyphicon-plus" onclick="add_node(this)"></a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="#" title="刷新记录" style="color:#000000;text-decoration: none" class="glyphicon glyphicon-refresh"></a>',
		onSelectRow : function(id) {
			if (id)
				$("#nodesGrid").jqGrid("editRow", id);
		},
		gridComplete : function() {
			var ids = $('#nodesGrid').jqGrid('getDataIDs');
			for ( var i = 0; i < ids.length; i++) {
				var id = ids[i];
				$("#" + id).attr("style","cursor:pointer");
				modify = "<a href='javascript:void(0)' title='确认' style='color:#000000;text-decoration: none' class='glyphicon glyphicon-check' onclick='grid_modify_detail(" + id + ", this)'></a>　　"; //这里的onclick就是调用了上面的javascript函数 Modify(id)
				del = "<a href='javascript:void(0)' title='删除' style='color:#000000;text-decoration: none' class='glyphicon glyphicon-trash' onclick='grid_del_detail(" + id + ")' ></a>";
				$("#nodesGrid").jqGrid("setRowData",ids[i], {opera : modify + del});
			}
		}
	});
}

function add_node(target) {
	var colDatas = $("#nodesGrid").jqGrid("getRowData");
	var row = {
			orderNo : colDatas.length + 1,
			ip : "",
			servicePort : 80,
			
	};
	$("#nodesGrid").jqGrid("addRowData", colDatas.length + 1, row, "last");
	$("#nodesGrid").jqGrid("editRow", colDatas.length + 1);
}

function grid_modify_detail(id, a){
	$("#nodesGrid").jqGrid("saveRow", id);
	var row = $("#nodesGrid").jqGrid("getRowData", id);
	var rowData = $("#nodesGrid").getRowData(id);
	if(!row.ip){
		CommonUtils.notify("error", "请填写url", "4000");
		$("#nodesGrid").jqGrid("editRow", id);
	}
}

function grid_del_detail(id){
	var colDatas = $("#nodesGrid").jqGrid("getRowData");
	if(colDatas){
		if(colDatas.length==1){
			$("#nodesGrid").jqGrid("clearGridData");
		}else{
			$("#nodesGrid").jqGrid("delRowData", id);
		}
	}
}