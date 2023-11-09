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
 

$("#aipChannelForm").validate({

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
			required : '通道编码必须填写',
			rangelength:'通道编码必须在4-30之间'
		},
		name : {
			required : '通道名称必须填写'
		},orderNo:{
			required : '排序号码必须填写',
			digits : '排序号码必须是整数'
		}
	},
	onfocusout : function(element) {
		$(element).valid();
	}
});


var judgeisNaN = function(obj,str,confirmIndex){
	if (obj.val()==""){
		obj.val("0");
		return true;
	}else{
		if (isNaN(obj.val())){
			CommonUtils.notify("error",str,"4000");
			
			top.layer.closeAll('loading');
			top.layer.close(confirmIndex);
			return false;
		}else{
			return true;
		}
	}
	
}


var submitAipChannelForm = function(formWinIndex,confirmIndex){
  
 
	
	
	
	$.ajax({
		url : '../../channel/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#aipChannelForm').serialize(),
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


var initdxsChannelForm = function(){
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
 
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../channel/detail.do',
			dataType : 'json',
			type : 'post',
			async: false,
			data : 'entityId=' + entityId,
			success : function(data) {
				
				if (data.success) {
	 
 
					$("#dxscode").attr("readonly", true);
					FormUtils.fillFormByData("aipChannelForm",data.responseData);
					chaneltypechange($("#dxschannelType").val());
 					
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("aipChannelForm", {id:''});
		chaneltypechange($("#dxschannelType").val());
		
	}
	
	var channeltype=$("#dxschannelType").val();
	if (channeltype=='CHANNELTYPE_NEWFIXEDTHREADPOOL') {
		validatedelall();
		validateadd($("#dxscorepoolSize"),"请输入工作线程数","工作线程数必须为整数","工作线程数必须为大于0的正整数");
	}else if (channeltype=='CHANNELTYPE_USERDEFINE'){
		validatedelall();
		validateadd($("#dxscorepoolSize"),"请输入工作线程数","工作线程数必须为整数","工作线程数必须为大于0的正整数");
		validateadd($("#dxskeepalivetime"),"请输入线程闲置值","线程闲置值必须为整数","线程闲置值必须为大于0的正整数");
		validateadd($("#dxsbufferQueueSize"),"请输入缓冲队列数","缓冲队列数必须为整数","缓冲队列数必须为大于0的正整数");
		validateadd($("#dxsmaxinumpoolSize"),"请输入最大并发数","最大并发数必须为整数","最大并发数必须为大于0的正整数");
	}else{
		validatedelall();
	}
	
};

$("#dxschannelType").change(function(){
	$("#dxscorepoolSize").val(0);
	$("#dxsmaxinumpoolSize").val(0);
	$("#dxskeepalivetime").val(0);
	$("#dxsbufferQueueSize").val(0);
	chaneltypechange($("#dxschannelType").val());
});

function chaneltypechange(channeltype){
	if (channeltype=='CHANNELTYPE_NEWFIXEDTHREADPOOL'  ){
	    $("#div1").show();
	    $("#div_disk").hide();
	    $("#div2").hide();
//	    $("#dxsdataNode").show();
	}else if (channeltype=='CHANNELTYPE_USERDEFINE'){
	    $("#div1").show();
	    $("#div_disk").show();
	    $("#div2").show();
//	    $("#dxsdataNode").hide();
	}else {
	    $("#div1").hide();
	    $("#div_disk").hide();
	    $("#div2").hide();
	}
	
	if (channeltype=='CHANNELTYPE_NEWFIXEDTHREADPOOL') {
		validatedelall();
		validateadd($("#dxscorepoolSize"),"请输入工作线程数","工作线程数必须为整数","工作线程数必须为大于0的正整数");
	}else if (channeltype=='CHANNELTYPE_USERDEFINE'){
		validatedelall();
		validateadd($("#dxscorepoolSize"),"请输入工作线程数","工作线程数必须为整数","工作线程数必须为大于0的正整数");
		validateadd($("#dxskeepalivetime"),"请输入线程闲置值","线程闲置值必须为整数","线程闲置值必须为大于0的正整数");
		validateadd($("#dxsbufferQueueSize"),"请输入缓冲队列数","缓冲队列数必须为整数","缓冲队列数必须为大于0的正整数");
		validateadd($("#dxsmaxinumpoolSize"),"请输入最大并发数","最大并发数必须为整数","最大并发数必须为大于0的正整数");
	}else{
		validatedelall();
	}
 
 

	
};
function validateadd(div,s1,s2,s3){	
	div.rules("add", {
		required : true,
		digits : true,
		addm2: true,
		messages : {
			required : s1,
			digits : s2,
			addm2:s3
		}
	});
	 
};

function validatedel(div){	
	div.closest("div.form-group").removeClass("has-error");
	div.closest("div.col-sm-8").find("span").hide();
	div.rules("remove");	 
};
function validatedelall(){	
	validatedel($("#dxscorepoolSize")); 
	validatedel($("#dxsmaxinumpoolSize")); 
	validatedel($("#dxskeepalivetime")); 
	validatedel($("#dxsbufferQueueSize")); 
};

//$("#dxscorepoolSize").rules("add", {
//required : true,
//digits : true,
//addm2: true,
//messages : {
//	required : "请输入工作线程数",
//	digits : "工作线程数必须为整数",
//	addm2:"工作线程数必须为大于0的正整数"
//}
//});
//} else {
//
//$("#dxscorepoolSize").closest("div.form-group").removeClass("has-error");
//$("#dxscorepoolSize").closest("div.col-sm-8").find("span").hide();
//$("#dxscorepoolSize").rules("remove");
 


