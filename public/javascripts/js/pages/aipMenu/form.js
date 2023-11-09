$("#aipMenuForm").validate({
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
			required : "请请输入菜单编码",
			minlength : "菜单编号至少由两个字母组成"
		},
		name : {
			required : "请输入菜单名称"
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

var submitAipMenuForm = function(formWinIndex, confirmIndex, mainName){
	debugger
	$.ajax({
		url : '../../menu/savaOrUpdate',
		dataType : 'json',
		type : 'post',
		// data : $('#aipMenuForm').serialize(),
		data : JSON.stringify(get2Json($('#aipMenuForm').serialize())),
		contentType: "application/json;charset=utf-8",
		success : function(data) {
			if (data.success) {
				
				CommonUtils.notify("success","操作成功<br>","1500");
				
				var formWin = top.window.frames[mainName]; //得到main页面窗体 formWin.method();
				formWin.reloadZtree();
				
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

var initAipMenuForm = function(){
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	var parentNode = CommonUtils.getUrlParam("node");
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../menu/getById/'+entityId,
			dataType : 'json',
			type : 'get',
			data : {"entityId" : entityId},
			success : function(data) {
				if (data.success) {
					FormUtils.fillFormByData("aipMenuForm",data.responseData);
					initParentMenuNodes(data.responseData.parentId, data.responseData);
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("aipMenuForm", {id:''});
		initParentMenuNodes(parentNode);
	}
}

function initParentMenuNodes(parentNode, responseData){
	$.ajax({
		url : "../../menu/getById/"+parentNode,
		async : false,
		type : "get",
		data : {"id":parentNode},
		dataType : "json",
		success : function (data) {
			if (data.success) {
				var parentMenu = $("#aipMenuParentId");
/*				$.each(data.responseData, function(){
					var option = "<option value='"+this.id+"'>"+this.name+"</option>";
					parentMenu.append(option);
				})*/
				var option = "<option value='"+data.responseData.id+"'>"+data.responseData.name+"</option>";
				parentMenu.append(option);
				if (responseData)
					parentMenu.val(responseData.parentId);
				else
					parentMenu.val(parentNode);
			}
		}
	})
}

