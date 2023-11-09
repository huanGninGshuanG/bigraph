$.validator.addMethod("addm", function(value) {
	var Regx = /[\u4E00-\u9FA5]/i;
    
    if (!Regx.test(value)) {
        return true;
    }
        return false;
  }, '不可输入汉字！');

$("#aipDataDictionarytypeForm").validate({

	rules : {
		code:{
			required : true,
			rangelength:[2,30],
			addm:true
		},
		name : {
			required : true,
			stringCheck:true
		},
		orderNo:{
			required : true,
			digits : true,
			maxlength : 8
		}
	},
	messages : {
		code:{
			required : '字典类型编码不能为空',
			rangelength:'字典类型编码必须在2-30之间'
		},
		name:{
			required : '字典类型名称不能为空',
		},
		orderNo:{
			required : "排序号不能为空",
			digits : "排序号码必须为整数",
			maxlength : "排序号码最大不能超过8位数"
		}
	},
	onfocusout : function(element) {
		$(element).valid();
	}
});

var submitAipDataDictionaryForm = function(formWinIndex,confirmIndex,mainName){

	$.ajax({
		url : '../../TCodeType/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#aipDataDictionarytypeForm').serialize(),//编码表单元素集为字符串以便提交
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


var initaipDataDictionarytypeForm = function(){
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	var parentNode = CommonUtils.getUrlParam("node");
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../TCodeType/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			success : function(data) {
				if (data.success) {
					$("#aipdxsDataDictionaryId").val(parentNode);
					$("#aipDataDictionaryCode").attr("readonly", true);
					FormUtils.fillFormByData("aipDataDictionarytypeForm",data.responseData);
					$("#dxsDataDictionaryExpireTime").val(laydate.now(data.responseData.expireTime));
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("AipDataDictionaryForm", {id:''});//填充表单域
	}
}

