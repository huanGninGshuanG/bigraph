$("#aipDIPComponentForm").validate({

		rules : {
			code:{
				required : true,
			},
			name : {
				required : true
			},
			orderNo:{
				required : true,
				digits : true
			},
			resoucename:{
				required : true 
			}  
		},
		messages : {
			code:{
				required : '组件编码必须填写',
			},
			name : {
				required : '组件名称必须填写'
			},orderNo:{
				required : '排序号码必须填写',
				digits : '排序号码必须是整数'
			},
			resoucename : {
				required : '资源名称必须选择'
			} 
		},
		onfocusout : function(element) {
			$(element).valid();
		}
	});

var submitAipDataComponentForm = function(formWinIndex,confirmIndex,mainName){
	var formWin = top.window.frames[mainName]; //得到main页面窗体 formWin.method();
	
	var list = $("#aipGrid").jqGrid("getRowData");
	var defList = new Array();
	var str="";
	var defParameterStr,defParameters = "";
    var judge=0
	$.each(list, function(i){
		var obj = new Object();
		obj.orderNo=this.orderNo;
		obj.param = this.param;
		obj.value = this.value;
		defList[i] = obj;
		var str = obj.param + "11fe86210d3441579708da3bb26c1b68" + obj.value + ";";
		defParameters += str;			
		if (obj.param.indexOf("editable inline-edit-cell form-control")==-1){
			
		}else{				
//			alert("第"+obj.orderNo +"行信息未确认"); 
			CommonUtils.notify("error","第"+obj.orderNo +"行信息未确认","4000");			
			top.layer.closeAll('loading');
			top.layer.close(confirmIndex);
			judge=1;
			return false;
		} 
	});

    if (judge==1){
    	return;
    }
	defParameterStr = JSON.stringify(defList);
	defParameters.substring(0, defParameters - 1);
	var sub=defParameterStr + "aipSeparator" + defParameters;
	
	$.ajax({
		url : '../../dipcomponent/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#aipDIPComponentForm').serialize()+"&defaultParams="+sub,
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


var initaipDIPComponentForm = function(){
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	var parentNode = CommonUtils.getUrlParam("node");
	
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../dipcomponent/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			
			success : function(data) {
				$("#aipDataComponent").val(parentNode);
				 nodeview($("#aipexecuteType").val());
				bindobject('edit',data.responseData.channelid);
				bindResource('aipedit',data.responseData.cenameid);
				FormUtils.fillFormByData("aipDIPComponentForm",data.responseData);
				
				if (data.responseData.defaultParams){
				var defParameterStr = data.responseData.defaultParams.split("aipSeparator")[0];
				if(defParameterStr != null && defParameterStr != "" && defParameterStr != "null"){
					defParameterStr = $.parseJSON(defParameterStr);
					$.each(defParameterStr, function(i){
						this.orderNo = i + 1;
						$("#aipGrid").jqGrid("addRowData", i + 1, this, "last");
					});
				}
			  }
				layer.closeAll('loading');
			}
		});
	}else{
		nodeview($("#aipexecuteType").val());
		bindobject();
		bindResource();
		FormUtils.fillFormByData("aipDIPComponentForm", {id:''});
		$("#aipDataComponent").val(parentNode);
	}
}

function bindobject(status,channelid){
    var run1='0';
    var run2='0';
	var channel = $("#aipChannelid");
    $("#aipChannelid").empty();
    $("#aipChannelid").val('');
	$.ajax({
		url : '../../channel/list.do',
		dataType : 'json',
		type : 'post',
		//data :"",
		success : function(data) {
			if (data.success) {				
				for(var i=0; i<data.responseData.datas.length;i++){					
					var option = "<option value='"+data.responseData.datas[i].id+"'>"+data.responseData.datas[i].name+"</option>";					
					channel.append(option);
				}
				if (status=='edit'){
					$("#aipChannelid").val(channelid);
				}
				if (run1=='1'){
					layer.closeAll('loading');
				}else{
					run2='1'
				}
			} 
		}
	});	
};

function bindResource(status,cenameid){
	 var run1='0';
	 var run2='0';
  var resoucename = $("#aipresoucename");
    $("#aipresoucename").empty();
    $("#aipresoucename").val('');
	$.ajax({
		url : '../../resource/list.do',
		dataType : 'json',
		type : 'post',
		success : function(data) {
			if (data.success) {				
				for(var i=0; i<data.responseData.datas.length;i++){					
					var option = "<option value='"+data.responseData.datas[i].id+"'>"+data.responseData.datas[i].name+"</option>";					
					resoucename.append(option);
				}
				if(status=='aipedit'){
					 $("#aipresoucename").val(cenameid);
				}
				if (run1=='1'){
					layer.closeAll('loading');
				}else{
					run2='1'
				}
			} 			
		}
	});	
} 

$("#aipexecuteType").change(function(){
	
	nodeview($("#aipexecuteType").val());
	if ($("#aipexecuteType").val()=="SYNC") {
		validatedelall();
		//validateadd($("#aipdataNode"),"请输入数据节点" );
	}else{
		validatedelall();
	}
});

function validatedelall(){	
//	validatedel($("#aipdataNode")); 
};
function nodeview(executeType){
	if (executeType=='SYNC'){
	    $("#divnode").show();
	}else{
	    $("#divnode").hide();
	}
	
};


function add_detail1(){
	  var colDatas = $("#aipGrid").jqGrid("getRowData");
	  var row = {
	   orderNo : colDatas.length + 1,
	   param : "",
	   value : ""
	   };
	   $("#aipGrid").jqGrid("addRowData", colDatas.length + 1, row, "last");
	   $("#aipGrid").jqGrid("editRow", colDatas.length + 1);
					
	};

	function modify_detail1(id, b){
		$("#aipGrid").jqGrid("saveRow", id);
		var model = jQuery('#aipGrid').jqGrid('getRowData', id);
//	 	alert(model.param);
		if (model.param==""){
			layer.tips('参数名为空！', b, {
				  tips: [2, '#18a689'],
				  time: 2000
			});
			$("#aipGrid").jqGrid("editRow", model.orderNo);
			return ;
		}
		
		if (model.value==""){
			layer.tips('参数值为空！', b, {
				  tips: [2, '#18a689'],
				  time: 2000
			});
			$("#aipGrid").jqGrid("editRow", model.orderNo);
			return ;
		}	
		
	};

	function del_detail1(id){
		$("#aipGrid").jqGrid("delRowData", id);
	};