 
var submitdxsNotifyForm = function(formWinIndex,confirmIndex){
 
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
		url : '../../notify/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#dxsNotifyForm').serialize()+"&content="+$("#dxscontent").val(),
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


var initdxsNotifyForm = function(){
	/*
	var node = CommonUtils.getUrlParam("node");
	var name = CommonUtils.getUrlParam("nodename");
	name = decodeURI(decodeURI(name));
 
 
 
 
	var dxsapp = $("#dxsAppId");
    var option = "<option value='"+node+"'>"+name+"</option>";
    dxsapp.append(option);	
    */
	

	var notifyid = CommonUtils.getUrlParam("notifyid");
	var readflag = CommonUtils.getUrlParam("readflag");
	var entityId = CommonUtils.getUrlParam("entityId");
//	alert(entityId);
	if (readflag=="false"){
	$.ajax({
		url : '../../notifystatus/update.do',
		dataType : 'json',
		type : 'post',
		data : 'entityId=' + entityId,
		success : function(data) {
			
			if (data.success) {
				
 
			}
 
		}
	});	
	}
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
 
	if(isUpdate == "true"){
		
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../notify/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + notifyid,
			success : function(data) {
				
				if (data.success) {
					
					FormUtils.fillFormByData("dxsNotifyForm",data.responseData);
					$("#dxscontent").val(data.responseData.content);
					
					$("#dxscreateDate").val(getSmpFormatDateByLong(data.responseData.createDate,true));
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("dxsNotifyForm", {id:''});
 
		
	}
}

var acceptForm = function(){
	//alert("show");
	
	var nodeId = $("#dxsAcceptId").val();
	
	var params = {"node":nodeId};
	
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}	
	
	
	top.layer.open({
		type : 2,
		title : ' ',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		maxmin : true, // 开启最大化最小化按钮
		//offset : '80px',
		area : [ '25%', '60%' ],
		shade : [ 0.3 ],
		content : "pages/dxsNotify/appForm.html?v=" + version+ paramStr ,
		btn : [ '确认', '取消' ],
		/*
		yes : function(index,layero) {
			DxsUserIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			top.layer.confirm('<small>您确定要执行此操作吗?</small>', {
				title : '<small>系统提示</small>',
				closeBtn : 0,
				icon:0,
				// shift: 1, //提示框载入动画
				// skin: 'layui-layer-molv', //样式类名
				btn : [ '确定', '取消' ]
			}, function(index) {
				top.layer.load(1, {
					shade : [ 0.2 ]
					// 透明度调整
				});
				iframeWin.contentWindow.save(index);
			});
		},
		*/
		yes : function(index, layero) {
			DxsNotifyIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
		//	$("#dxsappectid").val(iframeWin.contentWindow.save(index));
			
			var nodePower =iframeWin.contentWindow.save(index);
			var powerids = [];
			var powername=[];
			

			$.each(nodePower, function(i){
				powerids.push(this.id);
			});
			
			$.each(nodePower, function(i){
				if (!nodePower[i].isParent){
				powername.push(this.name);
				}
			});			
	 
			
			$("#dxsAcceptId").val(powerids);
			$("#dxsappectname").val(powername);
	 
		},
		
		
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			DxsUserIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			DxsUserIsRefresh = false;
		},
		success : function(layero, index) {
			DxsUserIsRefresh = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsRoleIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(DxsUserIsRefresh)
				$("#dxsUserGrid").trigger("reloadGrid");
		}
	});
}

