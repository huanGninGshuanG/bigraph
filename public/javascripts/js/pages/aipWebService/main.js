function filter(treeId, parentNode, childNodes) {
	if (!childNodes) return null;
	for (var i=0, l=childNodes.length; i<l; i++) {
		childNodes[i].name = childNodes[i].name.replace(/\.n/g, '.');
	}
	return childNodes;
}	
	
function onClick(event, treeId, treeNode, clickFlag) {
	$("#but").attr("status","1");
	status($("#but").attr("status"));
	var requestBodyxmp = $("<xmp></xmp>");
	requestBodyxmp.text(treeNode.requestBody);
	var responseBodyxmp = $("<xmp></xmp>");
	responseBodyxmp.text(treeNode.responseBody);
	$("#requestBody").empty().append(requestBodyxmp);
	$("#responseBody").empty().append(responseBodyxmp);
	$("#aipWebService").val(treeNode.locationUrl);
	$("#aiploginname").val(treeNode.loginname);
	$("#soapAction").val(treeNode.soapAction);
	$("#loginpassword").val(treeNode.loginpassword);
	$('#requestPage [href="tabs_panels.html#tab-4"]').tab('show');
	$("#result").html("");
	$("#log").html("");
}	

function zTreeOnAsyncSuccess(event, treeId, treeNode, msg) {
	var tree = $.fn.zTree.getZTreeObj(treeId);
	tree.expandAll(true);
	layer.closeAll('loading');
}
	
//function queryGrid(menuCode) {
//	$("#dxsWebServiceGrid").jqGrid("setGridParam",{datatype:'json',postData:{"dxsApp.id":menuCode}}).trigger("reloadGrid");
//}
	//打开layer
var openaipAppLayer = function(update,entityId,updatebtn){
	    var AipAppIframeWin;
	    var AipAppIsRefresh = false;
	    var data1=null;
	top.layer.open({
		type : 2,
		title :FORM_TITLE_PRE + ' Create a new soap test case.',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : true, // 开启最大化最小化按钮
		area : [ '40%', '45%' ],
		shade : [ 0.3 ],
		content : 'pages/aipWebService/form.html',
		btn : [ '确认', '取消' ],
		yes : function(index,layero) {
		var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
		if(iframeWin.contentWindow.$('#aipWebServiceForm' ).validate().form()){
			
		}else{
			return false;
		}
		var soap = iframeWin.contentWindow.save(index);
		$("#aipWebServiceUrl").val(soap.aipWebServiceUrl);
		$("#aipWebServiceUser").val(soap.aipWebServiceUser);
		$("#aipWebServicePw").val(soap.aipWebServicePw);
		//销毁页面全部树
		//$.fn.zTree.destroy();
		//遮罩
		top.layer.load(1, {
			shade : [ 0.2 ]
		});
		//发送请求
        $.ajax({
	    url : '../../webservice/loadDxsRpcServiceTree.do',
	    dataType : 'json',
		type : 'post',
		data : {
				'wsdlUrl':$("#aipWebServiceUrl").val(),
				'loginname':$("#aipWebServiceUser").val(),
				'password':$("#aipWebServicePw").val()
			},
	success : function(data) {
		if (data.success) {
			CommonUtils.notify("success","操作成功<br>","1500");
			var ztree=[];
			var jsonObj = {
					'id':0,
					'pId':null,
					'name':"[Soap-TestCase]",
					'open':true,
					'requestBody':""
					};
					ztree.push(jsonObj);
					     var soap = {
							    	'id':1,
							    	'pId':0,
								    'name':data.responseData[0].name,
								    'open':true,
								    'requestBody':""
							    };
					ztree.push(soap);
		               for(var j=0; j<data.responseData[0].sps.length;j++){
			                   var soap = {
					               'id':(j+2),
					               'pId':1,
						           'name':data.responseData[0].sps[j].portName,
							       'open':true,
							       'requestBody':""
					                       };
		                        	ztree.push(soap);
		                        	for(var i=0; i <data.responseData[0].sps[j].options.length; i++ ){
		                        		var soap = {
		                        				'id':(j+3+((i+1)*(j+1))),
		                        				'pId':(j+2),
		                        				'name':data.responseData[0].sps[j].options[i].name,
		                        				'requestBody':data.responseData[0].sps[j].options[i].requestBody,
		                        				'responseBody':data.responseData[0].sps[j].options[i].responseBody,
		                        				'locationUrl':data.responseData[0].sps[j].locationUrl,
		                        				'loginname':data.responseData[0].loginname,
		                        				'loginpassword':data.responseData[0].loginpassword,
		                        				'soapAction':data.responseData[0].sps[j].options[i].soapAction
		                        		         };
						                        ztree.push(soap);
		                        	      }
							          }
						//加载树
						$.fn.zTree.init($("#menuTree"), setting,ztree);
						top.layer.closeAll('loading');
						top.layer.close(index);
			  } else {
					CommonUtils.notify("error", data.responseMessage,"4000");
			     	top.layer.closeAll('loading');
			        return false;
			    }
		    }
	    });
	},
btn2: function(index, layero){
	//按钮【按钮二】的回调
	AipAppIsRefresh = false;	
},	
cancel: function(){ 
	//右上角关闭回调
	AipAppIsRefresh = false;
},	
success : function(layero, index) {
	AipAppIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：AipAppIframeWin.method()	
},
end : function(index) {
	top.layer.closeAll('loading');
}
	});
}
//点击事件加载数据onclick
var setting = {
		data: {
			simpleData: {
				enable: true,
				rootPId:0
			}
		},callback: {
			onClick: onClick
		}
};
//根据获得的参数获取数据
function execute(update,entityId,updatebtn){
	
	if ($("#aipWebService").val().length == 0) {
		layer.tips(' soap 执行地址为空！', updatebtn, {
			tips: [2, '#18a689'],
			time: 2000
		});
		return false;
	}
	if ($("#but").attr("status")=="1"){
		$("#but").attr("status","1");
		status($("#but").attr("status"));
	}
	search_sub();
		}
	function search_sub() {
		//遮罩
		layer.load(1, {
			shade : [ 0.2 ]
		});
		$.ajax({
				url : '../../webservice/invoker.do',
				dataType : 'json',
				type : 'post',
				data : 'wsdlUrl=' + $("#aipWebService").val()
						+ '&requestBody=' + $("#requestBody xmp").text()
						+ '&loginname=' + $("#aiploginname").val()
						+ '&password=' + $("#loginpassword").val()
						+ '&soapAction=' + $("#soapAction").val(),
				success : function(data) {
				if (data.success) {
						
						if(data.responseData.result == ""){
							$("#result").empty().append("<br><font color=red>无数据显示</font>");
						}else{
							var resultxmp = $("<xmp></xmp>");
							resultxmp.text(data.responseData.result);
							$("#result").empty().append(resultxmp);
						}
						var str = "<br>";
						str += "响应码：&nbsp;&nbsp;&nbsp;&nbsp;" + (data.responseData.resultCode == "200" ? "<font color=green>200</font>" : "<font color=red>" + data.responseData.resultCode + "</font>") + "<br>";
						str += "请求地址：" + data.responseData.realAddress + "<br>";
						str += "开始时间：" + getSmpFormatDateByLong(data.responseData.startTime,true) + "<br>";
						str += "结束时间：" + getSmpFormatDateByLong(data.responseData.endTime,true) + "<br>";
						str += "响应信息：" + (data.responseData.resultCode == 500 ? "<br>" : "") + data.responseData.message + "<br>";
						$("#log").empty().append(str);
					}
				layer.closeAll('loading');
				}
			});
			$('#responsePage [href="tabs_panels.html#tab-6"]').tab('show');
		};
//请求报文参数修改
var openaipAppLayer2 = function(update,entityId,updatebtn){
	status($("#but").attr("status"));
}
function status(value){
	if (value=="0"){
		$("#but").attr("status","1");
		$("#tubiao").removeClass("fa fa-edit").addClass("fa fa-check");
		$("#requestBodyTextArea").val($("#requestBody").html());
		$("#requestBody").hide();
		$("#requestBodyTextArea").show();
	}
	else{
		$("#but").attr("status","0");
		$("#tubiao").removeClass("fa fa-check").addClass("fa fa-edit");
		$("#requestBody").html($("#requestBodyTextArea").val());
		$("#requestBody").show();
		$("#requestBodyTextArea").hide();
	}
}
	