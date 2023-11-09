$("#aipQConfigForm").validate({
	rules:{
		code:{
			required:true
		},
		name:{
			required:true
		},
		consumerName:{
			required:true
		},
		componentName:{
			required:true
		},
		queue:{
			required:true
		},
		orderNo:{
			required:true
		}
	},
	messages:{
		code : {
			required : "请输入配置编码"
		},
		name:{
			required: "请输入配置名称"
		},
		consumerName:{
			required: "请选择ONS配置"
		},
		componentName:{
			required: "请选择集成组件"
		},
		queue:{
			required: "请输入订阅主题"
		},
		orderNo:{
			required: "请输入排序号码"
		}
	},
	onfocusout : function(element) {
		$(element).valid();
	},
	errorPlacement : function(error, element) {
		error.appendTo(element.closest("div.col-sm-8"));
	},
	highlight: function (e) {
        $(e).closest('.form-group').removeClass('has-success')
        $(e).closest("div.col-sm-8").addClass('has-error');  
    }, 
	success: function(label) {
		label.closest("div.col-sm-8").removeClass("has-error").addClass("has-success");
		//为了修复在使用input-group-btn时验证样式错位问题  special
		label.prevUntil(".input-group-btn").find("button").addClass("help-block").addClass("m-b-none");
	}
});

var initAipQConfigForm = function() {
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	
	if (isUpdate == 'true') {
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../amq_queue/detail.do',
			dataType : 'json',
			type : 'post',
			data : {'entityId' : entityId},
			success : function(data) {
				if (data.success) {
					FormUtils.fillFormByData("aipQConfigForm",data.responseData);
					$("#aipQConfigConsumerId").val(data.responseData.cid);
					$("#aipQConfigConsumerName").val(data.responseData.cName);
					$("#aipQConfigComponentId").val(data.responseData.aipComponentCode.split(";")[0]);
					$("#aipQConfigComponentName").val(data.responseData.aipComponentCode.split(";")[1]);
				}
				layer.closeAll('loading');
			}
		});
	} else {
		FormUtils.fillFormByData("aipQConfigForm", {id:''});
	}
}

var submitAipQConfigForm = function(formWinIndex, confirmIndex, mainName) {
	var postParam = GridOptions.serializeJson("aipQConfigForm");
	postParam.aipComponentCode = postParam.aipComponentCode + ";" + $("#aipQConfigComponentName").val();
	$.ajax({
		url : "../../amq_queue/saveOrUpdate.do",
		type : "post",
		dataType : "json",
		data : postParam,
		success : function (data) {
			if (data.success) {
				CommonUtils.notify("success", "操作成功<br>" ,"1500");
				
				top.layer.close(confirmIndex);
				top.layer.close(formWinIndex); // 再执行关闭
			} else {
				CommonUtils.notify("error", data.responseMessage, "4000");
				top.layer.closeAll('loading');
				top.layer.close(confirmIndex);
			}
		}
	});
}

var openCConfig = function() {
	var appId = $("#aipQConfigAppId").val();
	amq_cc_select(configCallback);
}

var openAComponent = function() {
	var appId = $("#aipQConfigAppId").val();
	ac_select(componentCallback);
}

var configCallback = function(obj) {
	$("#aipQConfigConsumerId").val(obj.id);
	$("#aipQConfigConsumerName").val(obj.name);
	$("#aipQConfigForm").validate().element($("#aipQConfigConsumerName"));//触发单个元素验证
}

var componentCallback = function(obj) {
	$("#aipQConfigComponentId").val(obj.code);
	$("#aipQConfigComponentName").val(obj.name);
	$("#aipQConfigForm").validate().element($("#aipQConfigComponentName"));//触发单个元素验证
}
