 


var openDxsQrtzTrigersLayer = function(update,entityId,updatebtn){
	
	 
	var DxsQrtzTrigersIframeWin;
	var DxsQrtzTrigersIsRefresh = false;	
	var paramStr = '';	
	var taskId=$("#dxsHiddenentityId").val();
	var params = {"taskId":taskId}
 
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '执行计划',
		closeBtn : 1,  //不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '60%' ],
		shade : [ 0.3 ],
		content : 'pages/aipScheduler/formtriger.html?isUpdate=' + update + '&entityId=' + entityId + paramStr  + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index, layero) {
			DxsQrtzTrigersIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			iframeWin.contentWindow.FormUtils.submitHandler(DxsQrtzTrigersIframeWin,"submitDxsQrtzTrigersForm","dxsQrtzTrigersForm");
		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			DxsQrtzTrigersIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			DxsQrtzTrigersIsRefresh = false;
		},
		success : function(layero, index) {
			DxsQrtzTrigersIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsQrtzTrigersIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(DxsQrtzTrigersIsRefresh)
//				$("#DxsGrid").trigger("reloadGrid");
				$("#DxsGrid").DataTable().fnSearch();
		}
	});
};

var lookuptriger = function(update,entityId,updatebtn){
	
	 
	var DxsQrtzTrigersIframeWin;
	var DxsQrtzTrigersIsRefresh = false;	
	var paramStr = '';	
	var taskId=$("#dxsHiddenentityId").val();
	var params = {"taskId":taskId}
 
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '执行计划',
		closeBtn : 1,  //不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '60%' ],
		shade : [ 0.3 ],
		content : 'pages/aipScheduler/formtriger.html?isUpdate=' + update + '&entityId=' + entityId + paramStr  + '&v=' + version,
		btn : [  '关闭' ],
//		yes : function(index, layero) {
//			DxsQrtzTrigersIsRefresh = true;
//			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
//			iframeWin.contentWindow.FormUtils.submitHandler(DxsQrtzTrigersIframeWin,"submitDxsQrtzTrigersForm","dxsQrtzTrigersForm");
//		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			DxsQrtzTrigersIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			DxsQrtzTrigersIsRefresh = false;
		},
		success : function(layero, index) {
			DxsQrtzTrigersIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsQrtzTrigersIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(DxsQrtzTrigersIsRefresh)
//				$("#DxsGrid").trigger("reloadGrid");
				$("#DxsGrid").DataTable().fnSearch();
		}
	});
};

var submitDxsQrtzTrigersForm = function(formWinIndex,confirmIndex){
 	$.ajax({
		url : '../../dxsSchedulerTriggers/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#dxsQrtzTrigersForm').serialize(),
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

var initdxsQrtzGroupForm = function(){
	
	var taskId = CommonUtils.getUrlParam("taskId");
	 
	$("#dxsTaskId").val(taskId); 
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
 
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../dxsSchedulerTriggers/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			success : function(data) {
				
				if (data.success) {
	 
 
 
		 
					FormUtils.fillFormByData("dxsQrtzTrigersForm",data.responseData);
					$("#dxsTaskId").val(data.responseData.taskId);
 
 
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("dxsQrtzTrigersForm", {id:''});
 
	}
};





var openDxsQrtzStepLayer = function(update,entityId,updatebtn){
	
	 
	var DxsQrtzStepIframeWin;
	var DxsQrtzStepIsRefresh = false;	
	var paramStr = '';	
	var taskId=$("#dxsHiddenentityId").val();
	var params = {"taskId":taskId}
 
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}
	
    var arrStep=[{formid:'dxsQrtzStepForm',li:"li1",tab:"tab-1"},{formid:'dxsQrtzStepForm2',li:"li2",tab:"tab-2"}]; 
//	alert(arrStep[0]);
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '执行步骤',
		closeBtn : 1,  //不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '60%' ],
		shade : [ 0.3 ],
		content : 'pages/aipScheduler/formstep.html?isUpdate=' + update + '&entityId=' + entityId + paramStr  + '&v=' + version,
		btn : [ '保存', '取消' ],
		yes : function(index, layero) {
			DxsQrtzStepIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
//			iframeWin.contentWindow.FormUtils.submitHandler(DxsQrtzStepIframeWin,"submitDxsQrtzStepForm","dxsQrtzStepForm");
			iframeWin.contentWindow.FormUtils.submitHandlerMul(DxsQrtzStepIframeWin,"submitDxsQrtzStepForm",arrStep);
		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			DxsQrtzStepIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			DxsQrtzStepIsRefresh = false;
		},
		success : function(layero, index) {
			DxsQrtzStepIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsQrtzStepIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(DxsQrtzStepIsRefresh)
//				$("#DxsStepGrid").trigger("reloadGrid");
				$("#DxsStepGrid").DataTable().fnSearch();
		}
	});
};

var lookupsetp = function(update,entityId,updatebtn){
	
	 
	var DxsQrtzStepIframeWin;
	var DxsQrtzStepIsRefresh = false;	
	var paramStr = '';	
	var taskId=$("#dxsHiddenentityId").val();
	var params = {"taskId":taskId}
 
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}
	
    var arrStep=[{formid:'dxsQrtzStepForm',li:"li1",tab:"tab-1"},{formid:'dxsQrtzStepForm2',li:"li2",tab:"tab-2"}]; 
//	alert(arrStep[0]);
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '执行步骤',
		closeBtn : 1,  //不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '60%', '60%' ],
		shade : [ 0.3 ],
		content : 'pages/aipScheduler/formstep.html?isUpdate=' + update + '&entityId=' + entityId + paramStr  + '&v=' + version,
		btn : [ '关闭' ],
//		yes : function(index, layero) {
//			DxsQrtzStepIsRefresh = true;
//			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
////			iframeWin.contentWindow.FormUtils.submitHandler(DxsQrtzStepIframeWin,"submitDxsQrtzStepForm","dxsQrtzStepForm");
//			iframeWin.contentWindow.FormUtils.submitHandlerMul(DxsQrtzStepIframeWin,"submitDxsQrtzStepForm",arrStep);
//		},
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			DxsQrtzStepIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			DxsQrtzStepIsRefresh = false;
		},
		success : function(layero, index) {
			DxsQrtzStepIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsQrtzStepIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(DxsQrtzStepIsRefresh)
				$("#DxsStepGrid").DataTable().fnSearch();
		}
	});
};


var initdxsQrtzStepForm = function(){
	
	var taskId = CommonUtils.getUrlParam("taskId");
	 
	$("#dxsTaskId").val(taskId); 

	var param='';
	var paramhttp='';
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	
//	$("#li4").hide();
//	$("#tab-4").hide();
 
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../dxsSchedulerStep/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			success : function(data) {
				
				if (data.success) { 
		 
					FormUtils.fillFormByData("dxsQrtzStepForm",data.responseData);
					$("#dxsfireNum").val(data.responseData.fireNum);
					$("#dxsfireMin").val(data.responseData.fireMin);
					$("#dxsfireFlag").val(data.responseData.fireFlag);
					$("#dxsstepRemark").val(data.responseData.stepRemark);
					$("#dxsorderNo").val(data.responseData.orderNo);
					
					$("#dxsAIPComponentKettleid").val(data.responseData.kettleid);
					$("#dxsAIPComponentKettlename").val(data.responseData.kettlename);
					
					if (data.responseData.stepType=="0"){
						$("#dxsparam").val('');
						param=data.responseData.param;
		 
 			
						var paramdefault_array=param.split(";");
								
						$("#dxshttpname").val(paramdefault_array[0].split("=")[1]);
						$("#dxshttppassword").val(paramdefault_array[1].split("=")[1]);

						
						if (paramdefault_array.length>2){

							for (i=2;i<paramdefault_array.length;i++){
								paramhttp=paramhttp+paramdefault_array[i]+';';
							}
							
							paramhttp=paramhttp.substring(0, paramhttp.length - 1);
						}

						$("#dxsparam").val(paramhttp);
						
					}					
					
					if (data.responseData.stepType=="2"){
						$("#div_kettle").show();
					}else{
						$("#div_kettle").hide();
					}
					
					if (data.responseData.stepType=="3"){
						$("#dxsparam").val('');
						param=data.responseData.param;
		 
 			
						var paramdefault_array=param.split(";");
								
						$("#dxshttpname").val(paramdefault_array[0].split("=")[1]);
						$("#dxshttppassword").val(paramdefault_array[1].split("=")[1]);
						$("#dxshttprequestmothed").val(paramdefault_array[2].split("=")[1]);
						$("#dxshttptxttype").val(paramdefault_array[3].split("=")[1]);
						$("#dxshttpcodeformat").val(paramdefault_array[4].split("=")[1]);
						
						if (paramdefault_array.length>5){

							for (i=5;i<paramdefault_array.length;i++){
								paramhttp=paramhttp+paramdefault_array[i]+';';
							}
							
							paramhttp=paramhttp.substring(0, paramhttp.length - 1);
						}

						$("#dxshttpparam").val(paramhttp);
						
					}
					
					if (data.responseData.stepType=="4"){
						$("#dxsuserdefineparam").val('');
						param=data.responseData.param;
		 
 			
						var paramdefault_array=param.split(";");
								
						$("#dxsuserDefinedClass").val(paramdefault_array[0].split("=")[1]);
						$("#dxsuserDefinedMothed").val(paramdefault_array[1].split("=")[1]);
						$("#dxsregexvalue").val(paramdefault_array[2].split("=")[1]);
						$("#dxsregexvalueuserdefine").val(paramdefault_array[3].split("=")[1]);

						
						
						
						if (paramdefault_array.length>4){

							for (i=4;i<paramdefault_array.length;i++){
								paramhttp=paramhttp+paramdefault_array[i]+';';
							}
							
							paramhttp=paramhttp.substring(0, paramhttp.length - 1);
						}

						$("#dxsuserdefineparam").val(paramhttp);
						
//						$("#li4").show();
//						$("#tab-4").show();
						
					}					
					
					bindtype();
 
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("dxsQrtzStepForm", {id:''});
		bindtype();
 
	}
	

	
};

 
var submitDxsQrtzStepForm = function(formWinIndex,confirmIndex){
 
//	if (!judgeisNaN($("#dxsfireNum"),"重试次数必须是整型",confirmIndex))
//		return;
//	
//	if (!judgeisNaN($("#dxsfireMin"),"间隔分钟必须是整型",confirmIndex))
//		return;	
	var kettleid='';
	var userdefineregex='';
	
 
	if ($("#dxsAIPComponentKettleid").val()==''||$("#dxsAIPComponentKettleid").val()==null){
		kettleid='';
	}else{
		kettleid="&dxsAIPComponentKettle.id="+$("#dxsAIPComponentKettleid").val();
	}

	if ($("#dxsstepType").val()=="0"){
		  var param='loginname='+$("#dxshttpname").val()+';password='+$("#dxshttppassword").val()  ;		  
		  if ($("#dxsparam").val()!='' && $("#dxsparam").val()!=null ){
			  param+=";"+$("#dxsparam").val();
		  }
		  $("#dxsparam").val(param);
		}	
	if ($("#dxsstepType").val()=="3"){
	  var param='loginname='+$("#dxshttpname").val()+';password='+$("#dxshttppassword").val()+';' ;
	  param+='requestmethod='+$("#dxshttprequestmothed").val()+';';
	  param+='contenttype='+$("#dxshttptxttype").val()+';';
	  param+='codeformat='+$("#dxshttpcodeformat").val();
	  
	  if ($("#dxshttpparam").val()!='' && $("#dxshttpparam").val()!=null ){
		  param+=";"+$("#dxshttpparam").val();
	  }
	  $("#dxsparam").val(param);
	}
	if ($("#dxsstepType").val()=="4"){
		
		if ($("#dxsregexvalue").val()=="userdefine"){
			userdefineregex= $("#dxsregexvalueuserdefine").val();
		} else{
			userdefineregex= $("#dxsregexvalue").val();
		}
		
		  var param='UserDefinePath='+$("#dxsuserDefinedClass").val()+';UserDefineMethod='+$("#dxsuserDefinedMothed").val()  ;	
		  param+=";UserDefinePathValue="+$("#dxsregexvalue").val()+";UserDefinePathRegex="+userdefineregex ;
		  if ($("#dxsuserdefineparam").val()!='' && $("#dxsuserdefineparam").val()!=null ){
			  param+=";"+$("#dxsuserdefineparam").val();
		  }
		  $("#dxsparam").val(param);
		}	
	
	$.ajax({
		url : '../../dxsSchedulerStep/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#dxsQrtzStepForm').serialize()+"&"+ $('#dxsQrtzStepForm2').serialize()+"&stepRemark="+$("#dxsstepRemark").val()+kettleid ,
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
};


function GetDateStr(AddDayCount) {
	var dd = new Date();
	dd.setDate(dd.getDate()+AddDayCount);//获取AddDayCount天后的日期
	var y = dd.getFullYear();
	var m = dd.getMonth()+1;//获取当前月份的日期
	var d = dd.getDate();
	
    if (m >= 1 && m <= 9) {
        m = "0" + m;
    }
    if (d >= 0 && d <= 9) {
        d = "0" + d;
    }
	return y+"-"+m+"-"+d;
	} ;
	

var initdxsQrtzLogForm = function(){
	var entityId = CommonUtils.getUrlParam("entityId");
	$("#dxsHiddenLogentityId").val(entityId);
	$("#dxsbeginTime").val(GetDateStr(-1));
	$("#dxsendTime").val(GetDateStr(0));
		
	var execplain = $("#dxsexecplain");
    $("#dxsexecplain").empty();
 	
 
 	
		var isUpdate = CommonUtils.getUrlParam("isUpdate");
 
		if(isUpdate == "true"){
			
			layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
			
			$.ajax({
			url : '../../dxsqrtztriggersexp/list.do',

			dataType : 'json',
			type : 'post',
			data : 'qrtzTriggers.qrtzGroup.id=' + entityId,
			success : function(data) {
				
				if (data.success) { 
		 

					
					
					console.info(data.responseData);
					 
					var option = "<option value=''>全部</option>";
					
					execplain.append(option);
					
					for(var i=0; i<data.responseData.datas.length;i++){
						
						
						var option = "<option value='"+data.responseData.datas[i].id+"'>"+data.responseData.datas[i].expCronDesc+"</option>";
						
						execplain.append(option);
					}
					
					 queryGrid(entityId);
				}
				layer.closeAll('loading');
			}
			
		});
			
		}
		
}	;


var initdxsQrtzLogFormNew = function(){
	var entityId = CommonUtils.getUrlParam("entityId");
	$("#dxsHiddenLogentityId").val(entityId);
	$("#dxsbeginTime").val(GetDateStr(-1));
	$("#dxsendTime").val(GetDateStr(0));
	$("#DxsQrtzGroupGrid").DataTable().fnSearch();
		
}	;


function queryGrid(entityId) {
	GridOptions.clearCachePostData("DxsQrtzGroupGrid");
	var taskDesc='';
    if ($("#dxsexecplain").val()==''){
    	
    }else{
    	taskDesc=$("#dxsexecplain").find("option:selected").text()
    }
    
	var taskExec='';
    if ($("#dxsexecresult").val()==''){
    	
    }else{
    	taskExec=$("#dxsexecresult").find("option:selected").text()
    }
	
	var postData = {
		"triggerGroup" : entityId,
		"taskDesc" : taskDesc,
		"taskExec" : taskExec,
		"startTime":$("#dxsbeginTime").val(),
		"finishTime":$("#dxsendTime").val()
	};
	$("#DxsQrtzGroupGrid").jqGrid("setGridParam",{datatype:'json',postData:postData}).trigger("reloadGrid");
};

var searchlog = function(updatebtn) {
	queryGrid($("#dxsHiddenLogentityId").val());
};

var searchlognew = function() {
	$("#DxsQrtzGroupGrid").DataTable().fnSearch();

};

var resetlog = function() {
 
	$("#dxsbeginTime").val(GetDateStr(-1));
	$("#dxsendTime").val(GetDateStr(0));
	$("#dxsexecresult").val("");
	$("#DxsQrtzGroupGrid").DataTable().clear();
	
};

function dateFormatRender(data, type, full) {
	 
	return getSmpFormatDateByLong(data,true);
}
 
 
updateItemFromGPGridTriger =function(gridId,updatebtn,openFunc){
	
	var s = $("#" + gridId).jqGrid('getGridParam','selarrrow');
	
	if(s == null || s == ""){
		layer.tips('您未选中需要修改的记录，请选择！', updatebtn, {
		  tips: [2, '#18a689'],
		  time: 2000
		});
	}else if(s.length != 1){
		
		layer.tips('您选中的记录数大于1，请选择一条记录进行修改！', updatebtn, {
			  tips: [2, '#18a689'],
			  time: 2000
		});
		
	} else{
		
//      alert(111);
//      alert(s);
      var model = jQuery("#" + gridId).jqGrid('getRowData', s);

		if(model.expCronDesc=="手动立即执行"){
			layer.tips('手动立即执行不能编辑！', updatebtn, {
				  tips: [2, '#18a689'],
				  time: 2000
				});
			return;
		}			
 
		
		var func = eval(openFunc);
		new func(true,s[0]);
	}
};

deleteItemFromTriger = function(deleteUrl,gridId,updatebtn){
	
	var ids = $("#" + gridId).jqGrid('getGridParam','selarrrow');
	
	if(ids == null || ids == ""){
		layer.tips('您未选中需要删除的记录，请选择！', updatebtn, {
		  tips: [2, '#18a689'],
		  time: 2000
		});
	}else{
		for ( var i = 0; i < ids.length; i++) {
			var id = ids[i];
			var model = jQuery("#" + gridId).jqGrid('getRowData', id);
			//alert(model.expCronDesc);
			if(model.expCronDesc=="手动立即执行"){
				layer.tips('手动立即执行不能删除！', updatebtn, {
					  tips: [2, '#18a689'],
					  time: 2000
					});
				return;
			}
			
		}
		var confirmId = layer.confirm('<small>您确定要执行此操作吗?</small>', {
			title : '<small>系统提示</small>',
			// skin: 'layui-layer-molv', //样式类名
			closeBtn : 0,
			icon:0,
			offset : '180px',
			// shift: 1, //提示框载入动画
			btn : [ '确定', '取消' ]
		// 按钮
		}, function() {
			layer.load(1, {
				shade : [ 0.2 ]
			// 透明度调整
			});
			
			$.ajax({
				url : deleteUrl,
				dataType : 'json',
				type : 'post',
				data : {
					"ids":ids,
				},
				success : function(data) {
					if (data.success) {
						CommonUtils.notify("success", "操作成功", 1500);
						$("#" + gridId).trigger("reloadGrid");
					} else {
						CommonUtils.notify("error", data.responseMessage, 4000);
					}
					layer.closeAll('loading');
					layer.close(confirmId);
				}
			});
		});
	}
};

//激活
resumeTrigger = function(resumeUrl,gridId,updatebtn){
	
//	var ids = $("#" + gridId).jqGrid('getGridParam','selarrrow');
	
	var ids = $("#" + gridId).DataTable().fnGetSelectedIds();
	
	if(ids == null || ids == ""){
		layer.tips('您未选中需要激活的记录，请选择！', updatebtn, {
		  tips: [2, '#18a689'],
		  time: 2000
		});
	}else{
 
		var confirmId = layer.confirm('<small>您确定要执行此操作吗?</small>', {
			title : '<small>系统提示</small>',
			// skin: 'layui-layer-molv', //样式类名
			closeBtn : 0,
			icon:0,
			offset : '180px',
			// shift: 1, //提示框载入动画
			btn : [ '确定', '取消' ]
		// 按钮
		}, function() {
			layer.load(1, {
				shade : [ 0.2 ]
			// 透明度调整
			});
			
			$.ajax({
				url : resumeUrl,
				dataType : 'json',
				type : 'post',
				data : {
					"ids":ids,
				},
				success : function(data) {
					if (data.success) {
						CommonUtils.notify("success", "操作成功", 1500);
//						$("#" + gridId).trigger("reloadGrid");
						$("#" + gridId).DataTable().fnSearch();
					} else {
						CommonUtils.notify("error", data.responseMessage, 4000);
					}
					layer.closeAll('loading');
					layer.close(confirmId);
				}
			});
		});
	}
};

//挂起
pauseTrigger = function(resumeUrl,gridId,updatebtn){
	
//	var ids = $("#" + gridId).jqGrid('getGridParam','selarrrow');
	
	var ids = $("#" + gridId).DataTable().fnGetSelectedIds();
	
	if(ids == null || ids == ""){
		layer.tips('您未选中需要激活的记录，请选择！', updatebtn, {
		  tips: [2, '#18a689'],
		  time: 2000
		});
	}else{
 
		var confirmId = layer.confirm('<small>您确定要执行此操作吗?</small>', {
			title : '<small>系统提示</small>',
			// skin: 'layui-layer-molv', //样式类名
			closeBtn : 0,
			icon:0,
			offset : '180px',
			// shift: 1, //提示框载入动画
			btn : [ '确定', '取消' ]
		// 按钮
		}, function() {
			layer.load(1, {
				shade : [ 0.2 ]
			// 透明度调整
			});
			
			$.ajax({
				url : resumeUrl,
				dataType : 'json',
				type : 'post',
				data : {
					"ids":ids,
				},
				success : function(data) {
					if (data.success) {
						CommonUtils.notify("success", "操作成功", 1500);
//						$("#" + gridId).trigger("reloadGrid");
						$("#" + gridId).DataTable().fnSearch();
					} else {
						CommonUtils.notify("error", data.responseMessage, 4000);
					}
					layer.closeAll('loading');
					layer.close(confirmId);
				}
			});
		});
	}
};

//立即执行
immediateTrigger = function(resumeUrl,gridId,updatebtn){
	
	
// 	var ids = $("#" + gridId).jqGrid('getGridParam','selarrrow');
	
	var ids = $("#" + gridId).DataTable().fnGetSelectedIds();	
	var id=$("#dxsHiddenentityId").val();
	if(ids == null || ids == ""){
		layer.tips('您未选中需要激活的记录，请选择！', updatebtn, {
		  tips: [2, '#18a689'],
		  time: 2000
		});
	}else{
 
		var confirmId = layer.confirm('<small>您确定要执行此操作吗?</small>', {
			title : '<small>系统提示</small>',
			// skin: 'layui-layer-molv', //样式类名
			closeBtn : 0,
			icon:0,
			offset : '180px',
			// shift: 1, //提示框载入动画
			btn : [ '确定', '取消' ]
		// 按钮
		}, function() {
			layer.load(1, {
				shade : [ 0.2 ]
			// 透明度调整
			});
 		
			$.ajax({
				url : resumeUrl,
				dataType : 'json',
				type : 'post',
				data : {
					"ids":ids,
					"id":id,
				},
				success : function(data) {
					if (data.success) {
						CommonUtils.notify("success", "操作成功", 1500);
//						$("#" + gridId).trigger("reloadGrid");
						$("#" + gridId).DataTable().fnSearch();
					} else {
						CommonUtils.notify("error", data.responseMessage, 4000);
					}
					layer.closeAll('loading');
					layer.close(confirmId);
				}
			});
		});
	}
};

//jar包上传
uploadjar = function(resumeUrl,gridId,updatebtn){
	var ids = $("#" + gridId).jqGrid('getGridParam','selarrrow');
	
	if(ids == null || ids == ""){
		layer.tips('您未选中需要上传的步骤，请选择！', updatebtn, {
		  tips: [2, '#18a689'],
		  time: 2000
		});
		return;
	}else{
		if (ids.length>1){
			layer.tips('只能选择单条数据！', updatebtn, {
				  tips: [2, '#18a689'],
				  time: 2000
				});
				return;	
		}
		var id = ids[0];
		var model = jQuery("#" + gridId).jqGrid('getRowData', id);
//		alert(model.stepType);
		if(model.stepType!="自定义"){
			layer.tips('上传文件必须选择自定义步骤！', updatebtn, {
				  tips: [2, '#18a689'],
				  time: 2000
				});
			return;
		}
		
		var paramStr = '';	
		var taskId=model.id;
		var params = {"taskId":taskId}
	 
		if (params) {
			paramStr = CommonUtils.urlEncode(params);
		}
//		alert('upload success');
		top.layer.open({
			type : 2,
			title : FORM_TITLE_PRE + '文件上传',
			closeBtn : 1, // 不显示关闭按钮
			shadeClose : false,
			shade : false,
			maxmin : true, // 开启最大化最小化按钮
			//offset : '80px',
			area : [ '60%', '69%' ],
			shade : [ 0.3 ],
			content : "pages/dxsBootstrapFileInput/main.html?v=" + version +paramStr ,
//			btn : [  '取消' ],
	 
//			yes : function(index, layero) {
//				DxsUserIsRefresh = true;
//				var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
//	 
//	 			
////				var defParameters =iframeWin.contentWindow.save(index);
////				
////				$("#dxsparam").val(defParameters);
//				
//	 
//		 
//			},
			
			
//			btn2: function(index, layero){
//			    //按钮【按钮二】的回调
//				DxsUserIsRefresh = false;
//			},
			cancel: function(){ 
			    //右上角关闭回调
				DxsUserIsRefresh = false;
			},
			success : function(layero, index) {
				DxsUserIsRefresh = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsRoleIframeWin.method()
			},
			end : function(index) {
				top.layer.closeAll('loading');
	 
			}
		});
	}
}


//立即执行
downjar = function(resumeUrl,gridId,updatebtn){
	
	var ids = $("#" + gridId).jqGrid('getGridParam','selarrrow');
	
	if(ids == null || ids == ""){
		layer.tips('您未选中需要上传的步骤，请选择！', updatebtn, {
		  tips: [2, '#18a689'],
		  time: 2000
		});
		return;
	}else{
		if (ids.length>1){
			layer.tips('只能选择单条数据！', updatebtn, {
				  tips: [2, '#18a689'],
				  time: 2000
				});
				return;	
		}
		var id = ids[0];
		var model = jQuery("#" + gridId).jqGrid('getRowData', id);
//		alert(model.stepType);
		if(model.stepType!="自定义"){
			layer.tips('上传文件必须选择自定义步骤！', updatebtn, {
				  tips: [2, '#18a689'],
				  time: 2000
				});
			return;
		}
 
		var id=model.id; 
		var confirmId = layer.confirm('<small>您确定要执行此操作吗?</small>', {
			title : '<small>系统提示</small>',
			// skin: 'layui-layer-molv', //样式类名
			closeBtn : 0,
			icon:0,
			offset : '180px',
			// shift: 1, //提示框载入动画
			btn : [ '确定', '取消' ]
		// 按钮
		}, function() {
			layer.load(1, {
				shade : [ 0.2 ]
			// 透明度调整
			});
 		
			$.ajax({
				url : resumeUrl,
				dataType : 'json',
				type : 'post',
				data : {
					"entityId":id,
				},
				success : function(data) {
					if (data.success) {
						CommonUtils.notify("success", "操作成功", 1500);
						$("#" + gridId).trigger("reloadGrid");
					} else {
						CommonUtils.notify("error", data.responseMessage, 4000);
					}
					layer.closeAll('loading');
					layer.close(confirmId);
				}
			});
		});
	}
};


var paramForm = function(){
	//alert("show");
	
	var param = $("#dxsparam").val();
	
	var params = {"param":param};
	
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}	
	
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '步骤参数',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		maxmin : true, // 开启最大化最小化按钮
		//offset : '80px',
		area : [ '65%', '60%' ],
		shade : [ 0.3 ],
		content : "pages/aipScheduler/fromparam.html?v=" + version+ paramStr ,
		btn : [ '确认', '取消' ],
 
		yes : function(index, layero) {
			DxsUserIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
 
 			
			var defParameters =iframeWin.contentWindow.save(index);
			
			$("#dxsparam").val(defParameters);
			
 
	 
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
 
		}
	});
};

var httpparamForm = function(){
//    var param='loginname='+$("#dxshttpname").val()+';password='+$("#dxshttppassword").val()+';' ;
//    param+='requestmethod='+$("#dxshttprequestmothed").val()+';';
//    param+='contenttype='+$("#dxshttptxttype").val()+';';
//    param+='codeformat='+$("#dxshttpcodeformat").val()+';';
//    $("#dxshttpparam").val(param) ;
	
	var param = $("#dxshttpparam").val();
	
	var params = {"param":param};
	
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}	
	
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '步骤参数',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		maxmin : true, // 开启最大化最小化按钮
		//offset : '80px',
		area : [ '65%', '60%' ],
		shade : [ 0.3 ],
		content : "pages/aipScheduler/fromparam.html?v=" + version+ paramStr ,
		btn : [ '确认', '取消' ],
 
		yes : function(index, layero) {
			DxsUserIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
 
 			
			var defParameters =iframeWin.contentWindow.save(index);
			
			$("#dxshttpparam").val(defParameters);
			
 
	 
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
 
		}
	});	
    
};

var userdefineparamForm = function(){
//  var param='loginname='+$("#dxshttpname").val()+';password='+$("#dxshttppassword").val()+';' ;
//  param+='requestmethod='+$("#dxshttprequestmothed").val()+';';
//  param+='contenttype='+$("#dxshttptxttype").val()+';';
//  param+='codeformat='+$("#dxshttpcodeformat").val()+';';
//  $("#dxshttpparam").val(param) ;
	
	var param = $("#dxsuserdefineparam").val();
	
	var params = {"param":param};
	
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}	
	
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '步骤参数',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		maxmin : true, // 开启最大化最小化按钮
		//offset : '80px',
		area : [ '65%', '60%' ],
		shade : [ 0.3 ],
		content : "pages/aipScheduler/fromparam.html?v=" + version+ paramStr ,
		btn : [ '确认', '取消' ],

		yes : function(index, layero) {
			DxsUserIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();

			
			var defParameters =iframeWin.contentWindow.save(index);
			
			$("#dxsuserdefineparam").val(defParameters);
			

	 
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

		}
	});	
  
};


var filenameForm = function(){
//  var param='loginname='+$("#dxshttpname").val()+';password='+$("#dxshttppassword").val()+';' ;
//  param+='requestmethod='+$("#dxshttprequestmothed").val()+';';
//  param+='contenttype='+$("#dxshttptxttype").val()+';';
//  param+='codeformat='+$("#dxshttpcodeformat").val()+';';
//  $("#dxshttpparam").val(param) ;
	
	var param = $("#dxsfilename").val();
	
	var params = {"param":param,"id":$("#dxsUserId").val()};
	
	var paramStr = '';
	if (params) {
		paramStr = CommonUtils.urlEncode(params);
	}	
	
	
	top.layer.open({
		type : 2,
		title : FORM_TITLE_PRE + '文件名称',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		maxmin : true, // 开启最大化最小化按钮
		//offset : '80px',
		area : [ '65%', '60%' ],
		shade : [ 0.3 ],
		content : "pages/aipScheduler/formfile.html?v=" + version+ paramStr ,
		btn : [ '确认' ],

		yes : function(index, layero) {
			DxsUserIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();

			
			var defParameters =iframeWin.contentWindow.save(index);
			
			$("#dxsfilename").val(defParameters);
			

	 
		},
		
		
//		btn2: function(index, layero){
//		    //按钮【按钮二】的回调
//			DxsUserIsRefresh = false;
//		},
		cancel: function(){ 
		    //右上角关闭回调
			DxsUserIsRefresh = false;
		},
		success : function(layero, index) {
			DxsUserIsRefresh = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsRoleIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');

		}
	});	
  
};

function add_detail1(){
	  var colDatas = $("#dxsparamGrid").jqGrid("getRowData");
	  var row = {
	   orderNo : colDatas.length + 1,
	   param : "",
	   value : ""
	   };
	   $("#dxsparamGrid").jqGrid("addRowData", colDatas.length + 1, row, "last");
	   $("#dxsparamGrid").jqGrid("editRow", colDatas.length + 1);
					
	};

	function modify_detail1(id, b){
		$("#dxsparamGrid").jqGrid("saveRow", id);
		var model = jQuery('#dxsparamGrid').jqGrid('getRowData', id);
//	 	alert(model.param);
		if (model.param==""){
			layer.tips('参数名为空！', b, {
				  tips: [2, '#18a689'],
				  time: 2000
			});
			$("#dxsparamGrid").jqGrid("editRow", model.orderNo);
			return ;
		}
		
		if (model.value==""){
			layer.tips('参数值为空！', b, {
				  tips: [2, '#18a689'],
				  time: 2000
			});
			$("#dxsparamGrid").jqGrid("editRow", model.orderNo);
			return ;
		}	
		
	};

	function del_detail1(id){
		$("#dxsparamGrid").jqGrid("delRowData", id);
	};


	
	var cornForm = function(){
 
		
		
		top.layer.open({
			type : 2,
			title : FORM_TITLE_PRE + '执行公式',
			closeBtn : 1, // 不显示关闭按钮
			shadeClose : false,
			shade : false,
			maxmin : true, // 开启最大化最小化按钮
			//offset : '80px',
			area : [ '60%', '60%' ],
			shade : [ 0.3 ],
			content : "pages/aipScheduler/formcorn.html?v=" + version ,
			btn : [ '确认', '取消' ],
	 
			yes : function(index, layero) {
				DxsUserIsRefresh = true;
				var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
//	 
//	 			
				var cronobj =iframeWin.contentWindow.save(index);
				 
				$("#dxsexpCron").val(cronobj.cron);
				$("#dxsexpCronDesc").val(cronobj.crondesc);
				$("#dxsonceFlag").val(cronobj.onceFlag);
	 
		 
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
	 
			}
		});
	};

	
	
	
	var initdxsQrtzCornForm = function(){
 
		$("#dxsexeconceTime").val(GetDateStr(0)+" 00:00:00");
		
		hidediv();
//		bindtime($("#dxshourmin"),$("#dxshm"),$("#lblhm"),$("#div_33"));
//		bindtime($("#dxshourmin_space"),$("#dxshm_space"),$("#lblhm_space"),$("#div_43"));
			
	}	;
	
	function hidediv(){
		$("div[order='1']").hide();
		$("div[order='2']").hide();
		$("div[order='3']").hide();
		$("div[order='4']").hide();
		$("div[order='5']").hide();
	};
	
	
	$("#dxsexectype").change(function(){
//	   alert($("#dxsexectype").val());
		if ($("#dxsexectype").val()=="0"){
			hidediv();
		}else if ($("#dxsexectype").val()=="1"){
			hidediv();
			$("div[order='1']").show();
		}else if ($("#dxsexectype").val()=="2"){
			hidediv();
			$("div[order='2']").show();
		}else if ($("#dxsexectype").val()=="3"){
			hidediv();
			$("div[order='3']").show();
			bindtime($("#dxshourmin"),$("#dxshm"),$("#lblhm"),$("#div_33"));
		}else if ($("#dxsexectype").val()=="4"){
			hidediv();
			$("div[order='4']").show();
			bindtime($("#dxshourmin_space"),$("#dxshm_space"),$("#lblhm_space"),$("#div_43"));
		}else if ($("#dxsexectype").val()=="5"){
			hidediv();
			$("div[order='5']").show();
		}
	});
	
	
	function bindtime(select1,select2,label,div){
 
		if (select1.val()=="hour"){
			select2.empty();
			for (var i=1;i<24;i++){
			  var option = "<option value='"+i+"'>"+i+"</option>";
			  select2.append(option);	
			}
			label.text("小时");
			div.hide();
		}else if (select1.val()=="min"){
			select2.empty();
			for (var i=1;i<60;i++){
			  var option = "<option value='"+i+"'>"+i+"</option>";
			  select2.append(option);	
			}
			label.text("分钟");
			div.show();
		}else if (select1.val()=="sec"){
			select2.empty();
			for (var i=1;i<60;i++){
			  var option = "<option value='"+i+"'>"+i+"</option>";
			  select2.append(option);	
			}
			label.text("秒");
			div.show();
		}
 
		
	}
	
	$("#dxshourmin").change(function(){
		bindtime($("#dxshourmin"),$("#dxshm"),$("#lblhm"),$("#div_33"));
	});
	
	$("#dxshourmin_space").change(function(){
		bindtime($("#dxshourmin_space"),$("#dxshm_space"),$("#lblhm_space"),$("#div_43"));
	});
	
	var judgeisNaN = function(obj,str,confirmIndex){ 
		if (obj.val()==""){
 
			return true;
		}else{
			if (isNaN(obj.val())){
				CommonUtils.notify("error",str,"4000");
				
				top.layer.closeAll('loading');
				top.layer.close(confirmIndex);
				return false;
			}else{
				return true;
			}
		}	
	}	;

	

	var aIPComponentKettleForm = function(){
 
		
		var param = $("#dxsparam").val();
		
		var params = {"param":param};
		
		var paramStr = '';
		if (params) {
			paramStr = CommonUtils.urlEncode(params);
		}	
		
		
		top.layer.open({
			type : 2,
			title : FORM_TITLE_PRE + '服务组件',
			closeBtn : 1, // 不显示关闭按钮
			shadeClose : false,
			shade : false,
			maxmin : true, // 开启最大化最小化按钮
			//offset : '80px',
			area : [ '60%', '70%' ],
			shade : [ 0.3 ],
			content : "pages/aipScheduler/formAIPComponentKettle.html?v=" + version+ paramStr ,
			btn : [ '确认', '取消' ],
	 
			yes : function(index, layero) {
				DxsUserIsRefresh = true;
				var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();	 
	 			
				var kettleobj =iframeWin.contentWindow.save(index);
				
				if (kettleobj.result=="1"){
					$("#dxsAIPComponentKettleid").val(kettleobj.kettleid);
					$("#dxsAIPComponentKettlename").val(kettleobj.kettlename);
				}
				
//				$("#dxsparam").val(defParameters);
				
	 
		 
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
	 
			}
		});
	};
	
	
	$("#dxsstepType").change(function(){
 
		$("form[id='dxsQrtzStepForm'] input[type='text']").val('');
		$("form[id='dxsQrtzStepForm'] input[type='password']").val('');
		$("#dxsAIPComponentKettleid").val('');
//		alert($("#dxshttprequestmothed").eq(0).val());		
		$("#dxshttprequestmothed option").eq(0).attr("selected",true);
		$("#dxshttptxttype option").eq(0).attr("selected",true);
		$("#dxshttpcodeformat option").eq(0).attr("selected",true);		
		$("#dxsregexvalue option").eq(0).attr("selected",true);		
		
//		$("#li4").hide();
//		$("#tab-4").hide();
		
		bindtype(); 
		
		
	});
	
	function bindtype(){

		$("#div_url").hide();
		$("#div_ip").hide();
		$("#div_port").hide(); 	
		$("#div_kettle").hide();
		$("#div_param").hide();
		
		$("#div_username").hide();
		$("#div_password").hide();
		$("#div_requestmothed").hide();
		$("#div_txttype").hide();
		$("#div_codeformat").hide();
		$("#div_httpparam").hide();
		
		$("#div_userdefinepath").hide();
		$("#div_userdefinemethod").hide();
		
		$("#div_regexselect").hide();
		$("#div_regexinput").hide();
		
		$("#div_userdefineparam").hide();
		$("#div_filename").hide();
 		
		if ($("#dxsstepType").val()=="0" ){
			$("#div_url").show();
			$("#div_ip").show();
			$("#div_port").show();
			$("#div_param").show();
			$("#div_username").show();
			$("#div_password").show();			
		}else if ($("#dxsstepType").val()=="1"){
			$("#div_url").show();
			$("#div_ip").show();
			$("#div_port").show();
			$("#div_param").show();
		}else if ($("#dxsstepType").val()=="2"){
			$("#div_kettle").show();
			$("#div_param").show();
		}else if ($("#dxsstepType").val()=="3"){
			$("#div_url").show();
			$("#div_username").show();
			$("#div_password").show();
			$("#div_requestmothed").show();
			$("#div_txttype").show();
			$("#div_codeformat").show();
			$("#div_httpparam").show();
		}else if ($("#dxsstepType").val()=="4"){
 
			$("#div_userdefinepath").show();
			$("#div_userdefinemethod").show();
			$("#div_userdefineparam").show();
//			$("#div_regexselect").show();
//			$("#div_filename").show();
			
			if ($("#dxsregexvalue").val()=="userdefine"){
 
				$("#div_regexinput").show();
 
			}else{
				$("#div_regexinput").hide();
 
			}
			
//			$("#li4").show();
//			$("#tab-4").show();
			
		} 
 		
	}
	
	
	$("#dxsregexvalue").change(function(){
		if ($("#dxsregexvalue").val()=="userdefine"){
			$("#div_regexinput").show();
			$("#dxsregexvalueuserdefine").val('');
		}else{
			$("#div_regexinput").hide();
		}
 
	});
	
	$("#dxsQrtzStepForm").validate({

		highlight : function(e) {
			$(e).closest(".form-group").removeClass("has-success").addClass(
					"has-error")
			},
		success : function(e) {
			e.closest(".form-group").removeClass("has-error").addClass(
					"has-success")
			},
		errorPlacement : function(e, r) {

			if (r.next().is("span")){
				e.appendTo(r.parent().parent());
				
			}else{
				e.appendTo(r.is(":radio") || r.is(":checkbox") ? r.parent().parent()
						.parent() : r.parent())
			}

		} ,
		
		rules : {
			addre:{
				required : true
			},
			ip : {
				required : true
			},
			port : {
				required : true
			},
			aIPComponentKettlename : {
				required : true
			},
			userDefinedClass : {
				required : true
			},
			userDefinedMothed : {
				required : true
			},
			regexvalueuserdefine : {
				required : true
			}
		},
		messages : {
			addre:{
				required : '服务/URL必须填写' 
			},
			ip : {
				required : 'ip必须填写'
			},
			port : {
				required : '端口必须填写'
			},
			aIPComponentKettlename : {
				required : '服务组件必须填写'
			},
			userDefinedClass : {
				required : '自定义路径必须填写'
			},
			userDefinedMothed : {
				required : '自定义方法必须填写'
			},
			regexvalueuserdefine : {
				required : '正则表达式必须填写'
			}
		},
		onfocusout : function(element) {
			$(element).valid();
		}
	});
	
	$("#dxsQrtzStepForm2").validate({

		rules : {
			fireNum:{
				required : true,
				digits : true
			},
			fireMin : {
				required : true,
				digits : true
			},
			orderNo : {
				required : true,
				digits : true
			}
		},
		messages : {
			fireNum:{
				required : '重试次数必须填写',
				digits : '重试次数必须是整数'
			},
			fireMin : {
				required : '间隔分钟必须填写',
				digits : '间隔分钟必须是整数'
			},
			orderNo : {
				required : '序号必须填写',
				digits : '序号必须是整数'
			}
		},
		onfocusout : function(element) {
			$(element).valid();
		}
	});
	
	$("#dxsQrtzTrigersForm").validate({
		highlight : function(e) {
		$(e).closest(".form-group").removeClass("has-success").addClass(
				"has-error")
		},
	success : function(e) {
		e.closest(".form-group").removeClass("has-error").addClass(
				"has-success")
		},
	errorPlacement : function(e, r) {

		if (r.next().is("span")){
			e.appendTo(r.parent().parent());
			
		}else{
			e.appendTo(r.is(":radio") || r.is(":checkbox") ? r.parent().parent()
					.parent() : r.parent())
		}

	} ,
		rules : {
			expCron:{
				required : true 
			},
			expCronDesc : {
				required : true 
			}
		},
		messages : {
			expCron:{
				required : '执行公式必须填写' 
			},
			expCronDesc : {
				required : '执行描述必须填写' 
			}
		},
		onfocusout : function(element) {
			$(element).valid();
		}
	});	
 