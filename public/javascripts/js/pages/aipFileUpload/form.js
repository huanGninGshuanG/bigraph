 
var submitDxsFileUploadForm = function(formWinIndex,confirmIndex){
 
 
	console.info($('#dxsUploadForm').serialize()+"&"+ $('#dxsUploadForm2').serialize());
	
	$.ajax({
		url : '../../dxsfileupload/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#dxsUploadForm').serialize()+"&"+ $('#dxsUploadForm2').serialize(),
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

 

var initdxsQrtzGroupForm = function(){
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	
 	
 
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../dxsfileupload/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			success : function(data) {
				
				if (data.success) {
					
  
					FormUtils.fillFormByData("dxsUploadForm",data.responseData);
					FormUtils.fillFormByData("dxsUploadForm2",data.responseData);
	 				
				}
				layer.closeAll('loading');
			}
		});
		
 
		
	}else{
		FormUtils.fillFormByData("dxsUploadForm", {id:''});
	}
};

 
 


 




