$("#aipCConfigForm").validate({
	rules:{
		code:{
			required:true
		},
		consumerId:{
			required:true
		},
		name:{
			required:true
		},
		accessKey:{
			required:true
		},
		secretKey:{
			required:true
		},
		threadNums:{
			required:true,
			digits:true
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
		consumerId:{
			required: "请输入接收者ID"
		},
		consumerId:{
			required: "请输入接收者ID"
		},
		accessKey:{
			required: "请输入接入码"
		},
		secretKey:{
			required: "请输入接入密钥"
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
	
	if (isUpdate == 'true') {
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../ons_consumer/detail.do',
			dataType : 'json',
			type : 'post',
			data : {'entityId' : entityId},
			success : function(data) {
				if (data.success) {
					FormUtils.fillFormByData("aipCConfigForm", data.responseData);
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
		url : "../../ons_consumer/saveOrUpdate.do",
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