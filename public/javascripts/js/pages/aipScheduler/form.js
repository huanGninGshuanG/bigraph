 
var submitDxsQrtzGroupForm = function(formWinIndex,confirmIndex){
 
 
	
	$.ajax({
		url : '../../dxsSchedulerTask/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#dxsQrtzGroupForm').serialize(),
		success : function(data) {
			if (data.success) {
				
				CommonUtils.notify("success","操作成功<br>","1500");
				
//				var formWin = top.window.frames[mainName]; //得到main页面窗体 formWin.method();
//				formWin.reloadZtree();				
				
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

var submitDxsQrtzGroupUploadForm = function(formWinIndex,confirmIndex){
 
 	alert(111);
 
	document.dxsQrtzScheduleUploadForm.submit();
//	$.ajax({
//		url : '../../dxsSchedulerGroup/upload.do',
//		dataType : 'json',
//		type : 'post',
//		data : $('#dxsQrtzGroupUploadForm').serialize(),
//		success : function(data) {
//			if (data.success) {
//				
//				CommonUtils.notify("success","操作成功<br>","1500");
//				
// 		
//				
//				top.layer.close(confirmIndex);
//				top.layer.close(formWinIndex); // 再执行关闭
//			} else {
//				CommonUtils.notify("error",data.responseMessage,"4000");
//				top.layer.closeAll('loading');
//				top.layer.close(confirmIndex);
//			}
//		}
//	});
}


var initdxsQrtzGroupForm = function(){
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	
	var node = CommonUtils.getUrlParam("node");
	var name = CommonUtils.getUrlParam("nodename");
	name = decodeURI(decodeURI(name));
	
	var dxsapp = $("#dxsAppId");
    var option = "<option value='"+node+"'>"+name+"</option>";
    dxsapp.append(option);		
 
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../dxsSchedulerTask/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			success : function(data) {
				
				if (data.success) {
					
					dxsapp.empty();
				    var option = "<option value='"+data.responseData.groupId+"'>"+data.responseData.groupName2+"</option>";
				    dxsapp.append(option);	 
					FormUtils.fillFormByData("dxsQrtzGroupForm",data.responseData);
	 				
				}
				layer.closeAll('loading');
			}
		});
		
//		$.ajax({
//			url : '../../dxsqrtztriggersexp/list.do',
//			dataType : 'json',
//			type : 'post',
//			data : 'qrtzTriggers.qrtzGroup.id=' + entityId,
//			success : function(data) {
//				
//				if (data.success) {
// 
//					FormUtils.fillFormByData("dxsQrtzGroupForm",data.responseData);
//	 				
//				}
//				layer.closeAll('loading');
//			}
//		});		
		
//		$.ajax({
//		url : '../../dxsqrtzstep/list.do',
//		dataType : 'json',
//		type : 'post',
//		data : 'qrtzGroup.id=' + entityId,
//		success : function(data) {
//			
//			if (data.success) {
//
//				FormUtils.fillFormByData("dxsQrtzGroupForm",data.responseData);
// 				
//			}
//			layer.closeAll('loading');
//		}
//	});	
		
		
	}else{
		FormUtils.fillFormByData("dxsQrtzGroupForm", {id:''});
	}
};

$("#dxsQrtzGroupForm").validate({

		rules : {
			code:{
				required : true 
			},
			groupName : {
				required : true
			}
		},
		messages : {
			code:{
				required : "请输入任务编码", 
			},
			groupName : {
				required : "请输入任务名称",
			}
		},
		onfocusout : function(element) {
			$(element).valid();
		}
});


var initdxsSchedulerGroupForm = function(){
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	
	var node = CommonUtils.getUrlParam("nodeId");
	var name = CommonUtils.getUrlParam("nodeName");
	name = decodeURI(decodeURI(name));
	
 
 
	

	var dxsapp = $("#dxsAppParentId");
	dxsapp.empty();
	dxsapp.val();
    var option = "<option value='"+node+"'>"+name+"</option>";
    dxsapp.append(option);	
    
	 
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
	 
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../dxsSchedulerGroup/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			success : function(data) {
				if (data.success) {
					FormUtils.fillFormByData("dxsAppForm",data.responseData);
					
					$("dxsbelongProject").val(data.responseData.appId);
					var dxsapp = $("#dxsAppParentId");
					dxsapp.empty();
					dxsapp.val();
				    var option = "<option value='"+data.responseData.parentId+"'>"+data.responseData.parentName+"</option>";
				    dxsapp.append(option);	
	 
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("dxsAppForm", {id:''});
 
	}
	

};


var submitDxsAppForm = function(formWinIndex,confirmIndex, mainName){
	$.ajax({
		url : '../../dxsSchedulerGroup/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#dxsAppForm').serialize(),
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
};





