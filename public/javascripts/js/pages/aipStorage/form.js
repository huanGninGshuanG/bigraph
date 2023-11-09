$("#aipstorageType").change(function(){
	if ($("#aipstorageType").val()=="STORAGETYPE_DATABASE"){
		$("#div_db").show();
		$("#div_disk").hide();
		}else{
 		$("#div_db").hide();
		$("#div_disk").show();
	}
	
	if ($("#aipstorageType").val()=="STORAGETYPE_DATABASE") {
		validatedelall();
		validateadd($("#aipdrivername"),"请输入驱动名称" );
	}else{
		validatedelall();
		validateadd($("#aipstorageDirectory"),"请输入存储目录" );
	}
	   
	});
var submitAipStorageForm = function(formWinIndex,confirmIndex){
	if ($("#aipstorageType").val()=="STORAGETYPE_DATABASE"){
 
		$("#aipstorageDirectory").val("");
		$("#aiposType").val("OSTYPE_WINDOWS");
		
	}else{

		$("#aipdbType").val("");
		$("#aipdrivername").val("");
	}
	
 
	if ($("#aipservicePort").val()==""){
		$("#aipservicePort").val("0");
	}else{
	if (isNaN($("#aipservicePort").val())){
		CommonUtils.notify("error","端口必须是整型","4000");
		
		top.layer.closeAll('loading');
		top.layer.close(confirmIndex);
		return;
	}
	}
 
	if ($("#aipUserOrderNo").val()==""){
		$("#aipUserOrderNo").val("0");
	}else{
	if (isNaN($("#aipUserOrderNo").val())){
		CommonUtils.notify("error","排序号码是整型","4000");
		
		top.layer.closeAll('loading');
		top.layer.close(confirmIndex);
		return;
	}
	}
	$.ajax({
		url : '../../storage/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#aipStorageForm').serialize(),
		success : function(data) {
			if (data.success) {
				CommonUtils.notify("success","操作成功<br>","1500");
				top.layer.close(confirmIndex);
				// 再执行关闭
				top.layer.close(formWinIndex); 
			} else {
				CommonUtils.notify("error",data.responseMessage,"4000");
				top.layer.closeAll('loading');
				top.layer.close(confirmIndex);
			}
		}
	});
}

var initaipStorageForm = function(){
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		// 透明度调整
		layer.load(1, {
			shade : [ 0.2 ]
		});
	$.ajax({
		url : '../../storage/detail.do',
		dataType : 'json',
		type : 'post',
		data : 'entityId=' + entityId,
		success : function(data) {
			if (data.success) {
				$("#aipcode").attr("readonly", true);
				FormUtils.fillFormByData("aipStorageForm",data.responseData);
				if ($("#aipstorageType").val()=="STORAGETYPE_DATABASE"){
					$("#div_db").show();
					$("#div_disk").hide();
				}else{
					$("#div_db").hide();
					$("#div_disk").show();
				}					
			}
			layer.closeAll('loading');
		}
	});
	}else{
		FormUtils.fillFormByData("aipStorageForm", {id:''});
		if ($("#aipstorageType").val()=="STORAGETYPE_DATABASE"){
			$("#div_db").show();
			$("#div_disk").hide();
		}else{
 
			$("#div_db").hide();
			$("#div_disk").show();
		}
	}
	
	if ($("#aipstorageType").val()=="STORAGETYPE_DATABASE") {
		validatedelall();
		validateadd($("#aipdrivername"),"请输入驱动名称" );
 
	}else{
		validatedelall();
		validateadd($("#aipstorageDirectory"),"请输入存储目录" );
 
	}
}

$.validator.addMethod("addm", function(value) {
	var Regx = /[\u4E00-\u9FA5]/i;
    
    if (!Regx.test(value)) {
        return true;
    }
        return false;
  }, '不可输入汉字！');
$.validator.addMethod("addm2", function(value) {
	if(value>0){
		return true;
	}
        return false;
  }, '请正整数');

$("#aipStorageForm").validate({

	rules : {
		code:{
			required : true,
			rangelength:[4,30],
			addm:true
		},
		name : {
			required : true
		},orderNo:{
			required : true,
			digits : true
		}
	},
	messages : {
		code:{
			required : '存储编码必须填写',
			rangelength:'存储编码必须在4-30之间'
		},
		name : {
			required : '存储名称必须填写'
		},orderNo:{
			required : '排序号码必须填写',
			digits : '排序号码必须是整数'
		}
	},
	onfocusout : function(element) {
		$(element).valid();
	}
});


function validateadd(div,s1,s2,s3){	
	div.rules("add", {
		required : true,
		messages : {
			required : s1 
		}
	});
};

function validatedel(div){	
	div.closest("div.form-group").removeClass("has-error");
	div.closest("div.col-sm-8").find("span").hide();
	div.rules("remove");	 
};

function validatedelall(){	
	validatedel($("#aipdrivername")); 
	validatedel($("#aipstorageDirectory")); 
 
};
