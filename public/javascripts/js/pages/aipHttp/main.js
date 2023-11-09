function add_detail1(){
  var colDatas = $("#aipHttpGridTo").jqGrid("getRowData");
  var row = {
   orderNo : colDatas.length + 1,
   param : "",
   value : ""
   };
   $("#aipHttpGridTo").jqGrid("addRowData", colDatas.length + 1, row, "last");
   $("#aipHttpGridTo").jqGrid("editRow", colDatas.length + 1);
				
};

function modify_detail1(id, b){
	$("#aipHttpGridTo").jqGrid("saveRow", id);
	var model = jQuery('#aipHttpGridTo').jqGrid('getRowData', id);
// 	alert(model.param);
	if (model.param==""){
		layer.tips('参数名为空！', b, {
			  tips: [2, '#18a689'],
			  time: 2000
		});
		$("#aipHttpGridTo").jqGrid("editRow", model.orderNo);
		return ;
	}
	
	if (model.value==""){
		layer.tips('参数值为空！', b, {
			  tips: [2, '#18a689'],
			  time: 2000
		});
		$("#aipHttpGridTo").jqGrid("editRow", model.orderNo);
		return ;
	}	
	
};

function del_detail1(id){
	$("#aipHttpGridTo").jqGrid("delRowData", id);
};

function search(obj){
	var list = $("#aipHttpGridTo").jqGrid("getRowData");
	var str="";
	
	$.each(list, function(i){
		var obj = new Object();
 
		obj.orderNo=this.orderNo;
		obj.param = this.param;
		obj.value = this.value;
		
		
 
		str =str+ "&"+obj.param + "=" + obj.value ;
 
	});
	alert(str);
};

//根据获得的参数获取数据
function execute(update,entityId,updatebtn){
		if ($("#dxphttpurl").val().length == 0) {
			layer.tips('URL地址为空！', updatebtn, {
				  tips: [2, '#18a689'],
				  time: 2000
			});
			return false;
		}

		var list = $("#aipHttpGridTo").jqGrid("getRowData");
		var str="";
        var judge=0
		$.each(list, function(i){
			var obj = new Object();
	 
			obj.orderNo=this.orderNo;
			obj.param = this.param;
			obj.value = this.value;
			
			if (obj.param.indexOf("editable inline-edit-cell form-control")==-1){ 
			}else{				
				layer.tips("第"+obj.orderNo +"行信息未确认", updatebtn, {
					  tips: [2, '#18a689'],
					  time: 2000
				});
				judge=1;
				return false;
			}
			str =str+ "&"+obj.param + "=" + obj.value ;
		});
        if (judge==1){
        	return false;
        }
		search_sub(str);
}
function search_sub(str) {
	
	var dataresult='';
	var datarrayresult='';
	//遮罩
	layer.load(1, {
		shade : [ 0.2 ]
	});
	$.ajax({
			url : '../../http/invoker.do',
			dataType : 'json',
			type : 'post',
			data : 'targeturl=' + $("#dxphttpurl").val() 
					+ '&loginname=' + $("#dxpusername").val()
					+ '&password=' + $("#dxpuserpwd").val()
					+ '&requestmethod=' + $("#aipMethod").val()
					+ '&contenttype=' + $("#aipContentType").val()
					+ '&codeformat=' + $("#aipCodeFormat").val()
					+ str,
			success : function(data) {
				if (data.success) {
					if(data.responseData.result == ""){
							$("#result").empty().append("<br><font color=red>无数据显示</font>");
						}else{
							$("#result").empty();
							var xmp = $("<xmp></xmp>");
							dataresult=data.responseData.result;							
							var bool = dataresult.indexOf("\\r\\n");
								if(bool>0){
									var result_array=dataresult.split("\\r\\n");
									for (i=0;i<result_array.length;i++){
										datarrayresult=datarrayresult+result_array[i] + "<br>";
									}
									$("#result").append(datarrayresult);
								}else{ 
								  xmp.html(dataresult);
								  $("#result").append(xmp);
								}
							}
							var str = "<br>";
							str += "响应码：&nbsp;&nbsp;&nbsp;&nbsp;" + (data.responseData.resultCode == "200" ? "<font color=green>200</font>" : "<font color=red>" + data.responseData.resultCode + "</font>") + "<br>";
							str += "响应信息：" + data.responseMessage + "<br>";
							str += "请求地址：" + data.responseData.realAddress + "<br>";
							str += "开始时间：" + getSmpFormatDateByLong(data.responseData.startTime,true) + "<br>";
							str += "结束时间：" + getSmpFormatDateByLong(data.responseData.endTime,true) + "<br>";
							$("#log").empty().append(str);
						}else{
							$("#result").empty().append("<br><font color=red>未解析成功</font>");
						}
							layer.closeAll('loading');
					}
				});
			$('#responsePage [href="tabs_panels.html#tab-6"]').tab('show');
		};