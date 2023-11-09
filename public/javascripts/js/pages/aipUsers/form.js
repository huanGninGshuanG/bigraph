$.validator.addMethod("addm", function(value) {
	var Regx = /[\u4E00-\u9FA5]/i;
    
    if (!Regx.test(value)) {
        return true;
    }
        return false;
  }, '不可输入汉字！');
$.validator.addMethod("addm2", function(value) {
	if(value!=''&&value!=null&&value!='00'){
		return true;
	}
        return false;
  }, '请选择有效值');
$.validator.addMethod("addm3", function(value) {
	var Regx = /^1\d{10}$/gi;
    
    if (Regx.test(value)||value==''||value==null) {
        return true;
    }
        return false;
  }, '请输入有效电话！');
$("#aipUserForm").validate({

	rules : {
		code:{
			required : true,
			rangelength:[4,30],
			addm:true
		},
		name : {
			required : true,
			stringCheck:true
		},
		loginpwd:{
			required : true,
			addm:true
		},
		loginname:{
			required : true,
			rangelength:[4,30],
			addm:true
		},
		expireTime:{
			required : true
		},
		linkmail:{
			email:true
		},
		userTepy:{
			addm2 : true
		},
		linkphone:{
			addm3:true
		},
		orderNo:{
			required : true,
			digits : true,
			maxlength : 8
		}
	},
	messages : {
		code:{
			required : '用户编码必须填写',
			rangelength:'用户编码必须在4-30之间'
		},
		name : {
			required : '用户名称必须填写'
		},
		loginpwd:{
			required : '密码必须填写'
		},
		loginname:{
			required : '访问帐号必须填写',
			rangelength:'访问帐号必须在4-30之间'
		},
		expireTime:{
			required : '失效日期必须填写'
		},
		linkmail:{
			email:'邮箱格式不正确'
		},
		orderNo:{
			required : "请输入排序号",
			digits : "排序号码必须为整数",
			maxlength : "排序号码最大不能超过8位数"
		}
	},
	onfocusout : function(element) {
		$(element).valid();
	}
});

function get2Json(href){
	var paramArr = href.split('&');
	var res = {};
	for(var i = 0;i<paramArr.length;i++){
		var str = paramArr[i].split('=');
		res[str[0]]=str[1];
	}
	console.log(res);
	return res;
}

var submitAipUserForm = function(formWinIndex,confirmIndex,mainName){
	//var formWin = top.window.frames[mainName]; //得到main页面窗体 formWin.method();
	$.ajax({
		url : '../../user/savaOrUpdate',
		dataType : 'json',
		type : 'post',
		// data : $('#aipUserForm').serialize(),
		data : JSON.stringify(get2Json($('#aipUserForm').serialize())),
		contentType: "application/json;charset=utf-8",
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


var initaipUserForm = function(){
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	var parentNode = CommonUtils.getUrlParam("node");
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../user/getById/'+entityId,
			dataType : 'json',
			type : 'get',
			data : 'entityId=' + entityId,
			success : function(data) {
				if (data.success) {
					$("#aipUserCode").attr("readonly", true);
					FormUtils.fillFormByData("aipUserForm",data.responseData);
					if($("#aipUserUserType").val()=='RESPOND'){
						var str = "<i class='fa fa-info-circle'></i>"
						$("#ShowLoginpwd").append(str);
						$("#ShowLoginpwd").append($("#aipUserLoginpwdShow").val());
					}else{
						$("#aipUserLoginname").attr("readonly", true);
						$("#aipUserLoginpwd").attr("readonly", true);
					}
					$("#aipUserExpireTime").val(laydate.now(data.responseData.expireTime));
					if(data.responseData.headPic){
						var img = '<img class="img-circle m-t-xs img-responsive" src="' + data.responseData.headPic + '" alt="image" />';
						$("#imgPrev").empty().append(img);
					}
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("aipUserForm", {id:''});
		$("#aipUserExpireTime").val(laydate.now(4102329600000));
	}
}

var LoginpwdChange = function(){
	$("#aipUserLoginpwdShow").val($("#aipUserLoginpwd").val());
}