var initAipCategoryForm = function() {
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	layer.load(1, {
		shade : [ 0.2 ]
		// 透明度调整
	});
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		$.ajax({
			url : '../../category/detail.do',
			dataType : 'json',
			type : 'post',
			data : {'entityId':entityId},
			success : function(data) {
				if (data.success) {
					FormUtils.fillFormByData("aipCategoryForm", data.responseData);
					$("#aipCategoryParentCode").val(data.responseData.pid);
					try {
						if (data.responseData.reverseProxy) {
							var nodes = JSON.parse(data.responseData.reverseProxy);
							$.each(nodes, function(i){
								var obj = {
										orderNo : i + 1,
										ip : this.host,
										port : this.port
								};
								$("#nodesGrid").jqGrid("addRowData", i + 1, obj, "last");
							})
						}
					} catch(e){}
				}
				layer.closeAll('loading');
			}
		});
	} else {
		FormUtils.fillFormByData("aipCategoryForm", {id:''});
		$.ajax({
			url : '../../clustergroup/getEffectiveOne.do',
			dataType : 'json',
			type : 'post',
			success : function(data) {
				if (data.success) {
					$("#aipCategoryPublishAddr").val(data.responseData.autoUrl);
					var nodes = JSON.parse(data.responseData.node);
					$.each(nodes, function(i){
						var obj = {
								orderNo : i + 1,
								ip : this.host,
								port : this.port
						};
						$("#nodesGrid").jqGrid("addRowData", i + 1, obj, "last");
					})
				}
				layer.closeAll('loading');
			}
		})
		$("#aipCategoryParentCode").val(CommonUtils.getUrlParam("node"));
	}
}

var submitAipCategoryForm = function(formWinIndex,confirmIndex) {
	
	var gridList = $("#nodesGrid").jqGrid("getRowData");
	var grid = [];
	$.each(gridList, function() {
		var obj = {
			host : this.ip,
			port : this.port
		}
		grid.push(obj);
	});
	
	var body = FormUtils.serializeJson("aipCategoryForm");
	
	body.parentCode = $("#aipCategoryParentCode").val();
	body.reverseProxy = JSON.stringify(grid);
	
	$.ajax({
		url : '../../category/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data :  body,
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

var initNodesGrid = function () {
	$("#nodesGrid").jqGrid({
		data:[],
		datatype : "local",
		scroll:1,//虚拟滚动条
		shrinkToFit: true, 
		rownumbers : false,
		autowidth : true,
		colNames : [ "序号","ip","端口","操作"],
		colModel : [
		    {name : 'orderNo', index : 'orderNo', width:30},
		    {name : 'ip', index : 'ip', editable:true},
		    {name : 'port', index : 'port', editable:true},
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
			port : 80
	};
	$("#nodesGrid").jqGrid("addRowData", colDatas.length + 1, row, "last");
	$("#nodesGrid").jqGrid("editRow", colDatas.length + 1);
}

function grid_modify_detail(id, a){
	$("#nodesGrid").jqGrid("saveRow", id);
	var row = $("#nodesGrid").jqGrid("getRowData", id);
	var rowData = $("#nodesGrid").getRowData(id);
	if(!row.ip){
		CommonUtils.notify("error", "请填写ip", "4000");
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