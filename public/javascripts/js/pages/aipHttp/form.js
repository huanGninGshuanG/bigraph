$("#aipHttpForm").validate({
	rules : {
		code : {
			required : true,
			minlength : 2
		},
		name : {
			required : true,
			stringCheck:true
		},
		ip : {
			required : true
			//stringCheck:true
		},
		clusterPort : {
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
			required : "请输入URL路径",
			minlength : "集群编号至少由两个字母组成"
		},
		name : {
			required : "请输入传输编码"
		},
		ip : {
			required : "请输入访问账号"
		},
		clusterPort : {
			required : "请输入访问密码",
			digits : "服务端口必须为整数",
			maxlength : "服务端口最大不能超过6位数"
		},
		orderNo : {
			required : "请输入排序号",
			digits : "排序号码必须为整数",
			maxlength : "排序号码最大不能超过8位数"
		},
	},
	onfocusout : function(element) {
		$(element).valid();
	}
});

var submitAipHttpForm  = function(formWinIndex,confirmIndex){
	var defParameterStr,defParameters = "";

	var list = $("#dxpClusterGrid").jqGrid("getRowData");
	var defList = new Array();
	
	$.each(list, function(i){
		var obj = new Object();
		obj.orderNo = i;
		obj.ip = this.ip;
		obj.port = this.port;
		defList[i] = obj;
		var str = obj.ip + "11fe86210d3441579708da3bb26c1b68" + obj.port + ";";
		defParameters += str;
	});
	defParameterStr = JSON.stringify(defList);
	defParameters.substring(0, defParameters - 1);
	var sub=defParameterStr + "dxpSeparator" + defParameters;
	$.ajax({
		url : '../../cluster/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#aipHttpForm').serialize()+"&subServerIps="+sub,
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

var initAipHttpForm = function(){
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../cluster/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			success : function(data) {
				if (data.success) {
					FormUtils.fillFormByData("aipHttpForm",data.responseData);
					if (data.responseData.subServerIps){
					var defParameterStr = data.responseData.subServerIps.split("dxpSeparator")[0];
					if(defParameterStr != null && defParameterStr != "" && defParameterStr != "null"){
						defParameterStr = $.parseJSON(defParameterStr);
						$.each(defParameterStr, function(i){
							this.orderNo = i + 1;
							$("#dxpClusterGrid").jqGrid("addRowData", i + 1, this, "last");
						});
					}
				  }
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("aipHttpForm", {id:''});
	}
}