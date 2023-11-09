$.validator.addMethod("addm", function(value) {
	var Regx = /[\u4E00-\u9FA5]/i;
    
    if (!Regx.test(value)) {
        return true;
    }
        return false;
  }, '不可输入汉字！');
$.validator.addMethod("addm1", function(value) {

	var isPhone = /^([0-9]{3,4}-)?[0-9]{7,8}$/;
    var isMob=/^((\+?86)|(\(\+86\)))?(13[012356789][0-9]{8}|15[012356789][0-9]{8}|18[02356789][0-9]{8}|147[0-9]{8}|1349[0-9]{7})$/;
	
    if (isPhone.test(value)||isMob.test(value)||value==''||value==null) {
        return true;
    }
        return false;
  }, '请输入有效电话！');
$.validator.addMethod("addm2", function(value) {
	var check ="((http|ftp|https)://)(([a-zA-Z0-9\._-]+\.[a-zA-Z]{2,6})|([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\&%_\./-~-]*)?";
	var re = new RegExp(check);
    if (re.test(value)||value==''||value==null) {
        return true;
    }
        return false;
  }, '请输入有效url！');

$("#aipIntegrationAppForm").validate({
	rules : {
		appCode : {
			required : true,
			minlength : 2,
			addm:true
		},
		appFullName : {
			required : true
		},
		appName : {
			required : true
		},
		URL : {
			addm2:true
		},
		linkpohone:{
			addm1:true
		},
		linkmail:{
			email:true
		},
		orderNo : {
			required : true,
			digits : true,
			maxlength : 8
		}
	},
	messages : {
		appCode : {
			required : "请请输入应用编码",
			minlength : "应用编号至少由两个字母组成"
		},
		appFullName : {
			required : "请输入应用全称"
		},
		appName:{
			required : "请输入应用简称"
		},
		linkmail:{
			email:"联系邮箱格式不正确"
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

var submitAipIntegrationAppForm = function(formWinIndex,confirmIndex){
	$.ajax({
		url : '../../integrationapp/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#aipIntegrationAppForm').serialize(),
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

var initaipIntegrationAppForm = function(){
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]// 透明度调整
		});
		$.ajax({
			url : '../../integrationapp/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			success : function(data) {
				if (data.success) {
					$("#aipIntegrationAppCode").attr("readonly", true);
					FormUtils.fillFormByData("aipIntegrationAppForm",data.responseData);
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("aipIntegrationAppForm", {id:''});
	}
}