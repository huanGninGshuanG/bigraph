 
var submitdxsNodeForm = function(formWinIndex,confirmIndex){
 
	if ($("#dxsUserOrderNo").val()==""){
		$("#dxsUserOrderNo").val("0");
	}else{
	if (isNaN($("#dxsUserOrderNo").val())){
		CommonUtils.notify("error","排序号码是整型","4000");
		
		top.layer.closeAll('loading');
		top.layer.close(confirmIndex);
		return;
	}
	} 
	
	$.ajax({
		url : '../../node/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#dxsNodeForm').serialize(),
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


var initdxsNodeForm = function(){
	
	var node = CommonUtils.getUrlParam("node");
	var name = CommonUtils.getUrlParam("nodename");
	name = decodeURI(decodeURI(name));
 
	var dxsapp = $("#dxsAppId");
    var option = "<option value='"+node+"'>"+name+"</option>";
    dxsapp.append(option);	
    
    
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
 
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../node/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			success : function(data) {
				
				if (data.success) {
					dxsapp.empty();
				    var option = "<option value='"+data.responseData.appId+"'>"+data.responseData.depname+"</option>";
				    dxsapp.append(option);	
 
					$("#dxscode").attr("readonly", true);
					FormUtils.fillFormByData("dxsNodeForm",data.responseData);
	 				
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("dxsNodeForm", {id:''});
 
		
	}
};


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

$("#dxsNodeForm").validate({

	rules : {
		code:{
			required : true,
			rangelength:[4,30],
			addm:true
		},
		name : {
			required : true
		},
		orderNo:{
			required : true,
			digits : true
		},
		ip : {
			required : true
		},
		servicePort : {
			required : true
		}
	},
	messages : {
		code:{
			required : '资源编码必须填写',
			rangelength:'资源编码必须在4-30之间'
		},
		name : {
			required : '资源名称必须填写'
		},orderNo:{
			required : '排序号码必须填写',
			digits : '排序号码必须是整数'
		},
		ip : {
			required : '服务地址必须填写'
		},
		servicePort : {
			required : '服务端口必须填写'
		}
	},
	onfocusout : function(element) {
		$(element).valid();
	}
});




