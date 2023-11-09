$("#aipRoleForm").validate({
	rules : {
		code : {
			required : true,
			minlength : 2
		},
		name : {
			required : true,
			stringCheck:true
		},
		orderNo : {
			required : true,
			digits : true,
			maxlength : 8
		}
	},
	messages : {
		code : {
			required : "请请输入角色编码",
			minlength : "角色编号至少由两个字母组成"
		},
		name : {
			required : "请输入角色名称"
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

var submitAipRoleForm = function(formWinIndex,confirmIndex,mainName){
	//var formWin = top.window.frames[mainName]; //得到main页面窗体 formWin.method();
	$.ajax({
		url : '../../role/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#aipRoleForm').serialize(),
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


var initaipRoleForm = function(){
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	var parentNode = CommonUtils.getUrlParam("node");
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../role/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			success : function(data) {
				if (data.success) {
					$("#aipRoleCode").attr("readonly", true);
					FormUtils.fillFormByData("aipRoleForm",data.responseData);
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("aipRoleForm", {id:''});
	}
}