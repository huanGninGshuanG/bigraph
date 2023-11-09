$.validator.addMethod("addm", function(value) {
	var Regx = /[\u4E00-\u9FA5]/i;
    
    if (!Regx.test(value)) {
        return true;
    }
        return false;
  }, '不可输入汉字！');

$("#aipDataDictionaryForm").validate({

	rules : {
		code:{
			required : true,
			rangelength:[2,30],
			addm:true
		},
		value : {
			required : true
		},
		typeName : {
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
			required : '字典编码不能为空',
			rangelength:'字典编码必须在2-30之间'
		},
		value:{
			required : '字典值不能为空',
		},
		typeName:{
			required : '所属类型不能为空',
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
	//var formWin = top.window.frames[mainName]; //得到main页面窗体 formWin.method();
	$.ajax({
		url : '../../TCodeValue/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#aipDataDictionaryForm').serialize(),
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


var initaipDataDictionaryForm = function(){
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	var parentNode = CommonUtils.getUrlParam("node");
	var name = CommonUtils.getUrlParam("nodename");
	name = decodeURI(decodeURI(name));
	$("#aipDataDictionarytypeCodeId").val(parentNode);
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../TCodeValue/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			success : function(data) {
				if (data.success) {
 
					$("#aipDataDictionaryName").val(name);
					$("#aipDataDictionaryCode").attr("readonly", true);
					$("#aipDataDictionaryName").attr("readonly", true);
					FormUtils.fillFormByData("aipDataDictionaryForm",data.responseData);
					$("#dxsDataDictionaryExpireTime").val(laydate.now(data.responseData.expireTime));
				}
				layer.closeAll('loading');
			}
		});
	}else{
		$("#aipDataDictionaryName").val(name);
		FormUtils.fillFormByData("aipDataDictionaryForm", {id:''});
	}
}