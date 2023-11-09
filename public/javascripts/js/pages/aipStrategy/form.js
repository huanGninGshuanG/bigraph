/*
 $("#dxsStrategyForm").validate({
 rules : {
 code : {
 required : true,
 minlength : 2
 },
 name : {
 required : true,
 stringCheck:true
 },

 orderNo : {
 required : true,
 digits : true,
 maxlength : 8
 }
 },
 messages : {
 code : {
 required : "请输入资源编码",
 minlength : "资源帮忙至少由两个字母组成"
 },
 name : {
 required : "请输入资源名称"
 },

 orderNo : {
 required : "请输入排序号",
 digits : "排序号码必须为整数",
 maxlength : "排序号码最大不能超过8位数"
 }
 },
 onfocusout : function(element) {
 $(element).valid();
 }
 });
 */

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


var submitDxsStrategysForm = function(formWinIndex, confirmIndex) {
	document.getElementById("id-input-file-3").value = "";
	
	
	var categoryId = $("#dxsStrategyCategoryIdForFrom").val();
	if(categoryId != null && categoryId != ""){
		$.ajax({
			url : '../../strategy/savaOrUpdate',
			dataType : 'json',
			type : 'post',
			data :  ($('#dxsStrategyForm').serialize())
					+ "&strategyType=" + $("#dxsStrategyFiletype").val() + "&aipCategory.id=" + $("#dxsStrategyCategoryIdForFrom").val(),
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
	}else{

		$.ajax({
			url : '../../strategy/savaOrUpdate',
			dataType : 'json',
			type : 'post',
			contentType: "application/json;charset=utf-8",
			data : JSON.stringify(get2Json($('#dxsStrategyForm').serialize()
				+ "&strategyType=" + $("#dxsStrategyFiletype").val())),
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
}

var initdxsStrategyForm = function() {

	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	var entityId = CommonUtils.getUrlParam("entityId");
	var categoryId = CommonUtils.getUrlParam("aipCategoryId");
	var categoryName = CommonUtils.getUrlParam("aipCategoryName");
	$("#dxsStrategyCategoryIdForFrom").val(categoryId);
	$("#dxsStrategyCategoryNameForFrom").val(categoryName);
	$('#dxsStrategyCategoryNameForFrom').attr('readonly', true);
	// $("#dxsStrategyCode").attr("readonly", true);
	// $('#dxsStrategyName').attr('readonly', true);
	$('#dxsStrategyCreateName').attr('readonly', true);
	$('#dxsStrategyCreateTime').attr('readonly', true);
	$('#dxsStrategyUpdateName').attr('readonly', true);
	$('#dxsStrategyUpdateTime').attr('readonly', true);
	$("#span1").html(
			"<i class='fa fa-info-circle'></i>请选择<font color=red>"
					+ $("#dxsStrategyFiletype").val() + "</font>文件类型进行上传");
	if (isUpdate == "true") {
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../strategy/getById/'+entityId,
			dataType : 'json',
			type : 'get',
			data : 'entityId=' + entityId,
			success : function(data) {
				if (data.success) {
					FormUtils.fillFormByData("dxsStrategyForm",
							data.responseData);
					$("#dxsStrategyFileContent").val(
							data.responseData.fileContent);
					$("#dxsStrategyFiletype").val(data.responseData.fileType);

					$("#dxsStrategyCreateName").val(data.responseData.creator);
					$("#dxsStrategyUpdateName").val(
							data.responseData.modifiedBy);
					$("#dxsStrategyCreateTime").val(
							getSmpFormatDateByLong(
									data.responseData.createDate, true));
					$("#dxsStrategyUpdateTime").val(
							getSmpFormatDateByLong(
									data.responseData.modifyDate, true));

					// $("#dxsStrategyCode").attr("readonly", true);
					// initParentMenuNodes(parentNode, data.responseData);
					$("#span1").html(
							"<i class='fa fa-info-circle'></i>请选择<font color=red>"
									+ $("#dxsStrategyFiletype").val()
									+ "</font>文件类型进行上传");
				}
				layer.closeAll('loading');
			}
		});
	}
}

$("#dxsStrategyFiletype").change(
		function() {
			$("#span1").html(
					"<i class='fa fa-info-circle'></i>请选择<font color=red>"
							+ $("#dxsStrategyFiletype").val()
							+ "</font>文件类型进行上传");
		});

function upload(input) {
	debugger
	var filetype = "." + $("#dxsStrategyFiletype").val();
	// 支持chrome IE10
	if (window.FileReader) {
		var file = input.files[0];
		filename = file.name.split(".")[0];
		if (input.value.indexOf(filetype) == -1) {
			CommonUtils.notify("error", "格式不对,请检查<br>", "1500");
			var file1 = document.getElementById("id-input-file-3");
			file1.value = "";
			return;

		}
		var reader = new FileReader();
		reader.onload = function() {
			var StrategyFileName = input.value.substring(input.value.lastIndexOf('\\') + 1);
			//TODO 如果是更新操作，需要校验传递的文件格式是否和现有格式一致，如不一致，不允许修改
			
			if(CommonUtils.getUrlParam("isUpdate") == "true"){
				if($('#dxsStrategyName').val() != StrategyFileName){
					CommonUtils.notify("error", "资源文件不匹配,请检查<br>", "1500");
					var file1 = document.getElementById("id-input-file-3");
					file1.value = "";
					return;
				}
			}
			$("#dxsStrategyFileContent").val(this.result);
			$('#dxsStrategyName').val(StrategyFileName);
			if (StrategyFileName != null && StrategyFileName != "" && StrategyFileName != undefined) {
				$('#dxsStrategyCode').val(StrategyFileName.split(".")[0]);
			}
		}
		reader.readAsText(file);
	}
	// 支持IE 7 8 9 10
	else if (typeof window.ActiveXObject != 'undefined') {
		if (input.value.indexOf(filetype) == -1) {
			CommonUtils.notify("error", "格式不对,请检查<br>", "1500");
			var file1 = document.getElementById("id-input-file-3");
			file1.value = "";
			return;

		}
		var xmlDoc;
		xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
		xmlDoc.async = false;
		xmlDoc.load(input.value);
		var StrategyFileName = input.value.substring(input.value.lastIndexOf('\\') + 1);
		//TODO 如果是更新操作，需要校验传递的文件格式是否和现有格式一致，如不一致，不允许修改
		if(CommonUtils.getUrlParam("isUpdate") == "true"){
			if($('#dxsStrategyName').val() != StrategyFileName){
				CommonUtils.notify("error", "资源文件不匹配,请检查<br>", "1500");
				var file1 = document.getElementById("id-input-file-3");
				file1.value = "";
				return;
			}
		}
		$("#dxsStrategyFileContent").val(xmlDoc.xml);
		$('#dxsStrategyName').val(StrategyFileName);
		if (StrategyFileName != null && StrategyFileName != "" && StrategyFileName != undefined) {
			$('#dxsStrategyCode').val(StrategyFileName.split(".")[0]);
		}
	}
	// 支持FF
	else if (document.implementation && document.implementation.createDocument) {
		if (input.value.indexOf(filetype) == -1) {
			CommonUtils.notify("error", "格式不对,请检查<br>", "1500");
			var file1 = document.getElementById("id-input-file-3");
			file1.value = "";
			return;

		}
		var xmlDoc;
		xmlDoc = document.implementation.createDocument("", "", null);
		xmlDoc.async = false;
		xmlDoc.load(input.value);
		
		var StrategyFileName = input.value.substring(input.value.lastIndexOf('\\') + 1);
		//TODO 如果是更新操作，需要校验传递的文件格式是否和现有格式一致，如不一致，不允许修改
		if(CommonUtils.getUrlParam("isUpdate") == "true"){
			if($('#dxsStrategyName').val() != StrategyFileName){
				CommonUtils.notify("error", "资源文件不匹配,请检查<br>", "1500");
				var file1 = document.getElementById("id-input-file-3");
				file1.value = "";
				return;
			}
		}
		$("#dxsStrategyFileContent").val(xmlDoc.xml);
		$('#dxsStrategyName').val(StrategyFileName);
		if (StrategyFileName != null && StrategyFileName != "" && StrategyFileName != undefined) {
			$('#dxsStrategyCode').val(StrategyFileName.split(".")[0]);
		}
	} else {
		CommonUtils.notify("error", "未知异常，请刷新浏览器重试<br>", "1500");
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
	if (value > 0) {
		return true;
	}
	return false;
}, '请正整数');

$("#dxsStrategyForm").validate({

	rules : {
		code : {
			required : true,
			rangelength : [ 4, 40 ],
			addm : true
		},
		name : {
			required : true
		},
		orderNo : {
			required : true,
			digits : true
		}
	},
	messages : {
		code : {
			required : '资源编码必须填写',
			rangelength : '资源编码必须在4-40之间'
		},
		name : {
			required : '资源名称必须通过导入文件填写'
		},
		orderNo : {
			required : '排序号码必须填写',
			digits : '排序号码必须是整数'
		}
	},
	onfocusout : function(element) {
		$(element).valid();
	}
});

