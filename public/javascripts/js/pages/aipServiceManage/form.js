$("#aipIntegrationAppForm").validate({
	rules : {
		"aipAIPComponentKettle.name":{
			required : true
		},
		categoryName:{
			required:true
		},
		authType:{
			required:true
		},
		code:{
			required:true
		},
		name:{
			required:true
		},
		orderNo:{
			required : true,
			digits : true,
			maxlength : 8
		}
	},
	messages : {
		"aipAIPComponentKettle.name":{
			required : "请选择组件"
		},
		response:{
			categoryName:'请选择访问方式'
		},
		response:{
			authType:'请选择访问方式'
		},
		code:{
			required:'服务编码不能为空'
		},
		name:{
			required:'服务名称不能为空'
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
var submitAipIntegrationAppForm = function(formWinIndex, confirmIndex, mainName) {
	$.ajax({
		url : '../../servicesManage/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#aipIntegrationAppForm').serialize() + "&wsdl="+ $("#aipWebServiceswsdl").val(),
		success : function(data) {
			if (data.success) {
				CommonUtils.notify("success", "操作成功<br>", "1500");
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

var initaipIntegrationAppForm = function() {

	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	
	var node = CommonUtils.getUrlParam("node");
	var categoryName = CommonUtils.getUrlParam("nodeName");
	$("#aipAIPCKCategoryId").val(node);
	$("#aipAIPCKCategoryName").val(categoryName);
	
	if (isUpdate == "true") {
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../servicesManage/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			success : function(data) {

				if (data.success) {
					// $("#dxsIntegrationAppCode").attr("readonly", true);
					FormUtils.fillFormByData("aipIntegrationAppForm",data.responseData);
					$("#aipWebServiceswsdl").val(data.responseData.wsdl);
					$("#aipAIPComponentKettlename").val(data.responseData.kettlename);
					$("#aipAIPComponentKettleid").val(data.responseData.kettleid);
					$("#aipAIPCKCategoryId").val(node);
				}
				layer.closeAll('loading');
			}
		});
	} else {
		FormUtils.fillFormByData("aipIntegrationAppForm", {
			id : ''
		});
		$("#aipAIPCKCategoryId").val(node);

	}

}

// 获取树节点id查询对应的组件
var openAipComponentKettleLayer = function(update, entityId, updatebtn) {
	var DxsQrtzTrigersIframeWin;
	var DxsQrtzTrigersIsRefresh = false;

	var node = $("#aipAIPCKCategoryId").val();
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '组件分类',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar : false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '54%', '57%' ],
		shade : [ 0.3 ],
		content : 'pages/aipServiceManage/queryForm.html?node=' + node,
		btn : [ '保存', '取消' ],
		yes : function(index, layero) {
			DxsUserIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; // 得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			var kettleobj = iframeWin.contentWindow.save(index);
			if (kettleobj.result == "1") {
				$("#aipAIPComponentKettleid").val(kettleobj.kettleid);
				$("#aipAIPComponentKettlename").val(kettleobj.kettlename);
			}
		},

		btn2 : function(index, layero) {
			// 按钮【按钮二】的回调
			DxsQrtzTrigersIsRefresh = false;
		},
		cancel : function() {
			// 右上角关闭回调
			DxsQrtzTrigersIsRefresh = false;
		},
		success : function(layero, index) {
			DxsQrtzTrigersIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsQrtzTrigersIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if (DxsQrtzTrigersIsRefresh)
				$("#aipKettleGrid").trigger("reloadGrid");
		}           
	});
};


