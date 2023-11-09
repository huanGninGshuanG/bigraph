$.validator.addMethod('mqConnectionValid', function(value, ele) {
	if (value) {
		var url = '((tcp)://)(([a-zA-Z0-9\._-]+\.[a-zA-Z]{2,6})|([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\&%_\./-~-]*)?';
		var re = new RegExp(url);
		return re.test(value);
	} else {
		return true;
	}
}, "请输入tcp://xxx形式的连接url");

$("#aipCConfigForm").validate({
	rules:{
		code:{
			required:true
		},
		clientID:{
			required:true
		},
		name:{
			required:true
		},
		prefetch:{
			required:true
		},
		backOffMultiplier:{
			required:true,
			digits:true
		},
		maxReconsumeTimes:{
			required:true,
			digits:true
		},
		suspendTimeMillis:{
			required:true,
			digits:true
		},
		threadNums:{
			required:true,
			digits:true
		},
		connectionUrl:{
			mqConnectionValid:true
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
		clientID:{
			required: "请输入客户端ID"
		},
		prefetch:{
			required: "请输入轮换条数"
		},
		backOffMultiplier:{
			required: "请输入重试递增倍数",
			digits:"重试递增倍数必须是整数"
		},
		maxReconsumeTimes:{
			required: "请输入重试等待时间",
			digits:"重试等待时间必须是整数"
		},
		suspendTimeMillis:{
			required: "请输入最大重试次数",
			digits:"最大重试次数必须是整数"
		},
		threadNums:{
			required: "请输入消费线程数",
			digits: "消费线程数必须是整数"
		},
		orderNo:{
			required: "请输入排序号码"
		}
	},
	onfocusout : function(element) {
		$(element).valid();
	}
});

var initAipCConfigForm = function() {
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	var appId = CommonUtils.getUrlParam("node");
	var appName = CommonUtils.getUrlParam("nodeName");
	$("#aipCConfigAppId").val(appId);
	$("#aipCConfigAppName").val(appName);
	
	if (isUpdate == 'true') {
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../amq_consumer/detail.do',
			dataType : 'json',
			type : 'post',
			data : {'entityId' : entityId},
			success : function(data) {
				if (data.success) {
					FormUtils.fillFormByData("aipCConfigForm",data.responseData);
					$("#aipCConfigAppId").val(data.responseData.dxsAppId);
					$("#aipCConfigAppId").val(data.responseData.dxsAppName);
				}
				layer.closeAll('loading');
			}
		});
	} else {
		FormUtils.fillFormByData("aipCConfigForm", {id:''});
	}
}

var submitAipCConfigForm = function(formIndex, confirmIndex, mainName) {
	$.ajax({
		url : "../../amq_consumer/saveOrUpdate.do",
		type : "post",
		dataType : "json",
		data : GridOptions.serializeJson("aipCConfigForm"),
		success : function (data) {
			if (data.success) {
				CommonUtils.notify("success", "操作成功<br>" ,"1500");
				
				top.layer.close(confirmIndex);
				top.layer.close(formIndex); // 再执行关闭
			} else {
				CommonUtils.notify("error", data.responseMessage, "4000");
				top.layer.closeAll('loading');
				top.layer.close(confirmIndex);
			}
		}
	});
}