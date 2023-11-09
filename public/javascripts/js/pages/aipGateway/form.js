$("#aipGatewayForm").validate({
	rules : {
		code : {
			required : true,
			minlength : 2
		},
		name : {
			required : true,
			stringCheck:true
		},
		loginname:{
			required : true
		},
		loginpwd:{
			required : true
		},
		mailbox:{
			required : true,
			email:true
		},
		port:{
			required : true
		},
		accessAdd:{
			required : true
		},
		SMTPserver:{
			required : true
		},
		orderNo : {
			required : true,
			digits : true,
			maxlength : 8
		}
	},
	messages : {
		code : {
			required : "请请输入网关编码",
			minlength : "网关编号至少由两个字母组成"
		},
		name : {
			required : "请输入网关名称"
		},
		loginname:{
			required : "请输入访问账号"
		},
		loginpwd:{
			required : "请输入访问密码"
		},
		mailbox:{
			required : "请输入访问邮箱",
			email:"访问邮箱格式不正确"
		},
		port:{
			required : "请输入网关端口"
		},
		accessAdd:{
			required : "请输入访问地址"
		},
		SMTPserver:{
			required : "请输入SMTP server"
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

var submitAipGatewayForm = function(formWinIndex,confirmIndex,mainName){
	//var formWin = top.window.frames[mainName]; //得到main页面窗体 formWin.method();
	$.ajax({
		url : '../../gateway/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#aipGatewayForm').serialize(),
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


var initaipGatewayForm = function(){
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	var parentNode = CommonUtils.getUrlParam("node");
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../gateway/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			success : function(data) {
				if (data.success) {
					$("#aipGatewayCode").attr("readonly", true);
					FormUtils.fillFormByData("aipGatewayForm",data.responseData);
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("aipGatewayForm", {id:''});
	}
}