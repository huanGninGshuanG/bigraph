$("#aipClusterGroupForm").validate({
	rules : {
		code:{
			required : true
		},
		name : {
			required : true
		},
		ip : {
			required : true
		},
		port : {
			required : true
		},
		contextPath : {
			required : true
		},
		accessProtocol : {
			required : true
		},
		orderNo:{
			required : true,
			digits : true
		}
	},
	messages : {
		code:{
			required : '集群编码必须填写'
		},
		name : {
			required : '通道名称必须填写'
		},
		ip : {
			required : "集群地址必须填写"
		},
		port : {
			required : "服务端口必须填写"
		},
		contextPath : {
			required : "服务上下文必须填写"
		},
		accessProtocol : {
			required : "访问协议必须选择"
		},
		orderNo:{
			required : '排序号码必须填写',
			digits : '排序号码必须是整数'
		}
	},
	onfocusout : function(element) {
		$(element).valid();
	}
});


var submitAipClusterGroupForm = function(formWinIndex,confirmIndex){
	
	var gridList = $("#nodesGrid").jqGrid("getRowData");
	var grid = [];
	$.each(gridList, function() {
		var obj = {
				host : this.host,
				port : this.port
		}
		grid.push(obj);
	});
	var requestData = GridOptions.serializeJson("aipClusterGroupForm");
	requestData.node = JSON.stringify(grid);
	
	$.ajax({
		url : '../../clustergroup/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : requestData,
		success : function(data) {
			if (data.success) {
				CommonUtils.notify("success","操作成功<br>","1500");
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


var initAipClusterGroupForm = function(){
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
 
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../clustergroup/detail.do',
			dataType : 'json',
			type : 'post',
			async: false,
			data : 'entityId=' + entityId,
			success : function(data) {
				if (data.success) {
					$("#aipClusterGroupCode").attr("readonly", true);
					FormUtils.fillFormByData("aipClusterGroupForm",data.responseData);
					var nodes = JSON.parse(data.responseData.node);
					$.each(nodes, function(i){
						console.log(this);
						var obj = {
								orderNo : i + 1,
								host : this.host,
								port : this.port
						};
						$("#nodesGrid").jqGrid("addRowData", i + 1, obj, "last");
					})
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("aipClusterGroupForm", {id:''});
	}
	
};

function initNodesGrid() {
	$("#nodesGrid").jqGrid({
		data:[],
		datatype : "local",
		scroll:1,//虚拟滚动条
		shrinkToFit: true, 
		rownumbers : false,
		autowidth : true,
		colNames : [ "序号","服务器地址","端口号","操作"],
		colModel : [
		    {name : 'orderNo', index : 'orderNo'},
		    {name : 'host', index : 'host', editable:true},
		    {name : 'port', index : 'port', editable:true},
		    {name : 'opera', index : 'opera'},
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
			host : "",
			port : "80"
	};
	$("#nodesGrid").jqGrid("addRowData", colDatas.length + 1, row, "last");
	$("#nodesGrid").jqGrid("editRow", colDatas.length + 1);
}

function grid_modify_detail(id, a){
	$("#nodesGrid").jqGrid("saveRow", id);
	var Regx = /^[0-9]*$/;
	var row = $("#nodesGrid").jqGrid("getRowData", id);
	var rowData = $("#nodesGrid").getRowData(id);
	if(!row.host){
		CommonUtils.notify("error", "请填写服务器地址", "4000");
		$("#nodesGrid").jqGrid("editRow", id);
	}
	if (!row.port) {
		CommonUtils.notify("error", "请填写端口", "4000");
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


