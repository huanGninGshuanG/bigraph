$("#dxsServiceForm").validate({
	rules : {
		code : {
			required : true
		},
		name : {
			required : true
		},
		reqContentType : {
			required : true
		},
		orderNo : {
			required : true,
			digits : true
		}
	},
	messages : {
		code : {
			required : "请输入服务编码"
		},
		name : {
			required : "请输入服务名称"
		},
		reqContentType : {
			required : "请选择内容类型"
		},
		orderNo : {
			required : "请输入排序号码",
			digits : "排序号码必须是整数"
		}
	},
	onfocusout : function(element) {
		$(element).valid();
	}
});

function changeAuthType(){
	if (this.value == "BASIC")
		$("#AuGroup").show();
	else
		$("#AuGroup").hide();
}

function parseUrl(btn){
	var url = $("#url").val();
	if (CommonUtils.validateUrl(url)) {
		$.ajax({
			url : "../../service/parseUrl.do",
			type : "post",
			dataType : "json",
			data : {"url":url, "serviceType":$("#dxsServiceType").val()},
			success : function (response) {
				if (response.success) {
					var data = response.responseData;
					var path = $("#dxsServicePath").val();
					if (path.length <= 0 || path == data.path) {
						$("#host").val(data.host);
						$("#dxsServicePath").val(data.path);
						$("#dxsServiceWsdlAddress").val(data.content);
						var colDatas = $("#urlGrid").DataTable().rows().data();
						var row = {
								orderNo : colDatas.length + 1,
								protocol : data.protocol,
								url : data.host,
								port : data.port,
								hideUrl : url
						}
						$("#urlGrid").DataTable().row.add(row).draw();
						$("#urlGrid tbody tr .save-btn").click();
						$("#url").val("");
					} else {
						CommonUtils.notify('warning', "本次填写接口与上次填写接口不符");
					}
				} else {
					CommonUtils.notify('error', response.responseMessage);
				}
			}
		});
	} else {
		CommonUtils.notify('warning', "输入服务路径不匹配！");
	}
}

function changeServiceType(){
	if (this.value == "soap")
		$("#soapDesc").show();
	else
		$("#soapDesc").hide();
	
	//resizeForm(1);
}

function changeTargetAuthType(){
	if (this.value == "BASIC")
		$("#targetAuGroup").show();
	else
		$("#targetAuGroup").hide();
}

var submitDxsServiceFormHandler = function(){
	if ($("#dxsServiceForm").validate().form()) {
		top.layer.confirm('<small>您确定要执行此操作吗?</small>', {
			title : '<small>系统提示</small>',
			closeBtn : 0,
			icon:0,
			btn : [ '确定', '取消' ]
		}, function (index) {
			top.layer.load(1, {
				shade : [ 0.2 ]
				// 透明度调整
			});
			
			submitDxsServiceForm(index);
		});
	}
}

function closeSelf(){
	var index = top.layer.getFrameIndex(window.name); // 先得到当前iframe层的索引
	top.layer.close(index); // 再执行关闭
}

var submitDxsServiceForm = function(confirmIndex){
	var list = $("#urlGrid").DataTable().rows().data();
	var defList = [];
	$.each(list, function(){
		var obj = {
			protocol : this.protocol,
			ip : this.url,
			port : this.port
		}
		defList.push(obj);
	});
	
	var tree = $.fn.zTree.getZTreeObj("userTree");
	var nodes = tree.getCheckedNodes(true);
	var users = [];
	$.each(nodes, function(){
		if (this.level == 1) {
			var obj = {
					"id" : this.id,
					"name" : this.name
			};
			users.push(obj);
		}
	});
	
	$.ajax({
		url : "../../service/saveOrUpdate.do",
		type : "post",
		dataType : "json",
		data : {
			"id" : $("#dxsServiceId").val(),
			"methodType" : $("#dxsServiceMethodType").val(),
			"paramMod" : $("#dxsServiceParamMod").val(),
			"targetHost" : JSON.stringify(defList),
			"code" : $("#dxsServiceCode").val(),
			"name" : $("#dxsServiceName").val(),
			"path" : $("#dxsServicePath").val(),
			"encode" : $("#dxsServiceEncode").val(),
			"authType" : $("#dxsServiceAuthType").val(),
			"routeStrategyType" : $("#dxsServiceRouteStrategyType").val(),
			"reqContentType" : $("#dxsServiceReqContentType").val(),
			"targetloginname" : $("#dxsServiceLgName").val(),
			"targetloginpwd" : $("#dxsServiceLgPwd").val(),
			//"host" : $("#dxsServiceHost").val(),
			"orderNo" : $("#dxsServiceOrderNo").val(),
			"description" : $("#dxsServiceDesc").val(),
			"type" : $("#dxsServiceType").val(),
			"creator" : $("#dxsServiceCreator").val(),
			"dxsApp.id" : $("#dxsServiceAppId").val(),
			"dxsUser" : JSON.stringify(users),
			"targetAuthType" : $("#dxsServiceTargetAuthType").val(),
			"wsdlAddress" : $("#dxsServiceWsdlAddress").val()
		},
		success : function (response) {
			if (response.success) {
				CommonUtils.notify("success", "操作成功<br>", "1500");
				top.layer.close(confirmIndex);
				var minaName = CommonUtils.getUrlParam("mainName");
				var formWin = top.window.frames[minaName]; //得到main页面窗体 formWin.method();
//				formWin.GridOptions.reload('dxsServiceGrid');
				formWin.dtOptions.reload('dxsServiceGrid');
				closeSelf(); // 再执行关闭
			} else {
				CommonUtils.notify("error", response.responseMessage, "4000");
				top.layer.closeAll('loading');
				top.layer.close(confirmIndex);
			}
		}
	})
}

function initDxsServiceForm() {
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	var appId = CommonUtils.getUrlParam("node");
	var appName = CommonUtils.getUrlParam("nodeName");
	$("#dxsServiceAppId").val(appId);
	$("#dxsServiceAppName").val(appName);
	if (isUpdate == "true") {
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		});
		$.ajax({
			url : '../../service/detail.do',
			dataType : 'json',
			type : 'post',
			data : {'entityId' : entityId},
			success : function(data) {
				if (data.success) {
					FormUtils.fillFormByData("dxsServiceForm", data.responseData);
					$("#dxsServiceAppId").val(data.responseData.appId);
					$("#dxsServiceAppName").val(data.responseData.appName);
					initReqContentType(data.responseData.reqContentType);
					var targetHosts = [];
					if (data.responseData.targetHost)
					targetHosts = JSON.parse(data.responseData.targetHost);
					var objs = [];
					$.each(targetHosts, function(i){
						var obj = {
							orderNo : i + 1,
							protocol : this.protocol,
							url : this.ip,
							port : this.port
						};
						objs.push(obj);
					});
					if (objs.length > 0) {
						$("#urlGrid").DataTable().rows.add(objs).draw();
						$("#urlGrid tbody tr .save-btn").click();
					}
					var dxsUser = JSON.parse(data.responseData.dxsUser);
					var div = $("#alreadyUsers");
					$.each(dxsUser, function(){
						var li = $("<div></div>");
						li.addClass("feed-element");
						li.attr("id", this.id);
						li.append("<div class='feed-body'>"+this.name+"</div>");
						div.append(li);
					})
					$("#dxsServiceType").trigger("change");
					$("#dxsServiceTargetAuthType").trigger("change");
					$("#dxsServiceAuthType").trigger("change");
				}
				initUserTree($("#dxsServiceAppId").val());
			}
		});
	} else {
		FormUtils.fillFormByData("dxsServiceForm", {id:''});
		initReqContentType();
	}
}

var setting = {
	check : {
		enable : true
	},
	data : {
		simpleData : {
			enable : true,
			idKey : 'id',
			pIdKey : 'pId',
			rootPId : 0
		},
		keep : {
			leaf : false,
			parent : false
		}
	},
	callback : {
		onCheck: zTreeOnCheck
	}
};

function zTreeOnCheck(event, treeId, treeNode) {
	var treeObj = $.fn.zTree.getZTreeObj(treeId);
	var nodes = treeObj.getCheckedNodes(true);
	var div = $("#alreadyUsers").empty();
	if (nodes.length > 0) {
		$.each(nodes, function(){
			if (this.level == 1) {
				var li = $("<div></div>");
				li.addClass("feed-element");
				li.attr("id", this.id);
				li.append("<div class='feed-body'>"+this.name+"</div>");
				div.append(li);
			}
		});
	}
};

function initUserTree(appId){
	$.ajax({
		url : "../../user/queryUsersTree.do",
		dataType : "json",
		type : "post",
		data : {"type":"RESPOND", "appId":appId},
		success : function(data) {
			if (data.success) {
			   //加载树
	          var treeObj = $.fn.zTree.init($("#userTree"), setting, data.responseData);
	          var idsObj = $("#alreadyUsers > .feed-element");
	          $.each(idsObj, function(){
	        	  var node = treeObj.getNodesByParam("id", this.id, null);
	        	  if (node[0])
	        		  treeObj.checkNode(node[0], true, true, false);
	          })
	          treeObj.expandAll(true);
			}
			layer.closeAll('loading');
		}
	});
}

function initReqContentType(defaultValue) {
	$.post('../../TCodeValue/loadValueByType.do', {'type':'D_CONTENT_TYEP'},function(result){
		if (result.success) {
			$("#dxsServiceReqContentType").empty();
			var options = [];
			$.each(result.responseData, function(){
				options.push('<option value="'+this.value+'">'+this.value+'</option>');
			})
			$("#dxsServiceReqContentType").html(options.join(''));
			if (defaultValue) 
				$("#dxsServiceReqContentType").val(defaultValue);
		}
	}, 'json');
}


var initSetpForm = function() {
	$("#dxsServiceForm").steps({
		bodyTag : "fieldset",
		enableAllSteps: true,
		onStepChanging : function (event, currentIndex, newIndex) {
			if (currentIndex > newIndex) {
				return true;
			}
			var form = $(this);
			//resizeForm(currentIndex);
			if (currentIndex < newIndex) {
				$(".body:eq("+ newIndex+ ") label.error",form).remove();
				$(".body:eq("+ newIndex+ ") label.error",form).removeClass("error");
				form.validate().settings.ignore = ":disabled,:hidden";
				return form.valid();
			}
		},
		onStepChanged : function (event, currentIndex, priorIndex) {
			resizeForm(0);
		},
		onFinishing : function (event, currentIndex) {
			var form = $(this);
			form.validate().settings.ignore = ":disabled";
			return form.valid()
		},
		onFinished : function (event, currentIndex) {
			submitDxsServiceFormHandler();
		},
		onCanceled : function (event) {
			closeSelf();
		}
	}).validate({
		errorPlacement : function(error, element) {
			error.appendTo(element.is(":radio") || element.is(":checkbox") ? element.parent().parent()
					.parent() : element.parent())
			//error.appendTo(element.closest("div.col-sm-4"));
			//var currentSetId = element.closest("fieldset")[0].id;
			//var currentSetIndex = currentSetId.substring(currentSetId.length - 1);
			//resizeForm(currentSetIndex);
		},
		highlight: function (e) {
			$(e).parent().removeClass("has-success").addClass("has-error");
	    }, 
		success: function(label) {
			label.parent().removeClass("has-error").addClass("has-success");
			//为了修复在使用input-group-btn时验证样式错位问题  special
			label.prevUntil(".input-group-btn").find("button").addClass("help-block").addClass("m-b-none");
		},
		rules : {
			code : {
				required : true
			},
			name : {
				required : true
			}
		},
		messages : {
			code : {
				required : "请输入服务编码"
			},
			name : {
				required : "请输入服务名称"
			}
		}
	});
	resizeForm(0);
	$("#dxsServiceForm").removeClass("hide");
}

function resizeForm(currentIndex) {
	var h = $("#dxsServiceForm-p-"+currentIndex+" > .fieldcontent").height() + 80;
	if (currentIndex == 1) 
		h += 320;
	h = 450;//高度固定519px
	$(".wizard-big.wizard > .content").attr("style", "height:"+h+"px");
}

function add_detail(){
	var colDatas = $("#urlGrid").DataTable().rows().data();
	var row = {
		orderNo : colDatas.length + 1,
		protocol : "",
		url : "",
		port : ""
	};
	$("#urlGrid").DataTable().row.add(row).draw(false);
}

function del_detail(id){
	$("#urlGrid").DataTable().row('[id="'+id+'"]').remove().draw(false);
}

function operator(data, type, full) {
	modify = "<a href='#' title='确认' style='color:#000000;text-decoration: none' class='glyphicon glyphicon-check save-btn'></a>　　"; //这里的onclick就是调用了上面的javascript函数 Modify(id)
	del = "<a href='#' title='删除' style='color:#000000;text-decoration: none' class='glyphicon glyphicon-trash' onclick='del_detail(" + data + ", this)' ></a>";
	return modify + del;
}

function editInput(td, cellData, rowData, row, col) {
	var jqod = $(td);
	if (!jqod.hasClass('button')) {
		var txt = jqod.text();
		var put = $("<input type='text'>");
		put.val(txt);
		jqod.html(put);
	}
}
