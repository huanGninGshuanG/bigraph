var channelFlag = false;
var connectionFlag = false;
var ElementId=1;
var zNodes =[{id:0, name:"xsd", open:true}];
//var OutElementId=1;
//var zNodesOut =[{id:0, name:"xsd", open:true}];
function changeAuthType(){
	if (this.value == "BASIC")
		$("#AuGroup").show();
	else
		$("#AuGroup").hide();
}
function changeExecuteType(){
	if (this.value == "SYNC") {
		$("#DataNodeDiv").show();
		$("#aipACKDataNode").rules("add", {
			required : true,
			messages : {
				required : "请选择输出节点"
			}
		});
		//resourceCascadeDatanode('');
		//异步执行时设置servlet运行模式为不支持
		$("#aipACKServletSupport").val("NO");
	} else {
		$("#DataNodeDiv").hide();
		$(this).closest("div.form-group").removeClass("has-error");
		$("#aipACKDataNode").rules("remove");
	}
}

var submitAIPComponentKettleFormHandler = function(){
	if ($("#aipComponentKettleForm").validate().form()) {
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
			
			submitAIPComponentKettleForm(index);
		});
	}
}

function closeSelf(){
	var index = top.layer.getFrameIndex(window.name); // 先得到当前iframe层的索引
	top.layer.close(index); // 再执行关闭
}

var submitAIPComponentKettleForm = function(confirmIndex){
	var inputTree = $.fn.zTree.getZTreeObj("inputTree");
	var inputList = inputTree.getNodes()[0];
//	var outputTree = $.fn.zTree.getZTreeObj("outputTree");
//	var outputList = outputTree.getNodes()[0];
	var outputParamList = $("#outputParamGrid").jqGrid("getRowData");
	
	var outputList = [];
	$.each(outputParamList, function(){
		var obj = {
			id : this.orderNo,
			name : this.output_name,
			MaxOccurs : this.output_MaxOccurs,
			MinOccurs : this.output_MinOccurs,
			required : this.output_required == "是" ? true : false,
			variableType : this.output_variableType
		}
		outputList.push(obj);
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
	
	var requestData = GridOptions.serializeJson("aipComponentKettleForm");
	requestData.inputParams = JSON.stringify(inputList);
	requestData.outputParams = JSON.stringify(outputList);
	requestData.aipUsers = JSON.stringify(users);
	//嵌套json格式赋值
	requestData["dxsConnection.id"] = $("#aipACKDxsConnection").val();
	requestData["dxsChannel.id"] = $("#aipACKChannel").val();

	
	$.ajax({
		url : "../../aipcomponent/saveOrUpdate.do",
		type : "post",
		dataType : "json",
		data : requestData,
		success : function (response) {
			if (response.success) {
				CommonUtils.notify("success", "操作成功<br>", "1500");
				top.layer.close(confirmIndex);
				var minaName = CommonUtils.getUrlParam("mainName");
				var formWin = top.window.frames[minaName]; //得到main页面窗体 formWin.method();
				//formWin.GridOptions.reload('aipComponentKettleGrid');
				formWin.dtOptions.reload('aipComponentKettleGrid');
				closeSelf(); // 再执行关闭
			} else {
				CommonUtils.notify("error", response.responseMessage, "4000");
				top.layer.closeAll('loading');
				top.layer.close(confirmIndex);
			}
		}
	})
}
function initConnection(appId, defaultValue) {
	$("#aipACKDxsConnection").change(connectionCascadeResource);
	
	$.ajax({
		url : "../../connection/list.do",
		dataType : 'json',
		type : 'post',
		async : true,
		data : {"dxsApp.id":appId},
		success : function(data) {
			channelFlag = true;
			if (data.success) {
				$.each(data.responseData.datas, function(){
					var option = "<option value='"+this.id+"'>"+this.name+"</option>";
					$("#aipACKDxsConnection").append(option);
				})
				$("#aipACKDxsConnection").val(defaultValue);
				$("#aipACKDxsConnection").trigger("change");
			}
			closeLayer();
		}
	});
}

function initChannel(appId, defaultValue) {
	$.ajax({
		url : "../../channel/list.do",
		dataType : 'json',
		type : 'post',
//		async : true,
		data : {"dxsApp.id":appId},
		success : function(data) {
			connectionFlag = true;
			if (data.success) {
				$.each(data.responseData.datas, function(){
					var option = "<option value='"+this.id+"'>"+this.name+"</option>";
					$("#aipACKChannel").append(option);
				})
					$("#aipACKChannel").val(defaultValue);
			}
			closeLayer();
		}
	});
}

function initDxsAIPComponentKettleForm() {
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	var categoryId = CommonUtils.getUrlParam("node");
	var categoryName = CommonUtils.getUrlParam("nodeName");
	$("#aipCKCategoryId").val(categoryId);
	$("#aipCKCategoryName").val(categoryName);
	$("#resourceName").change(resourceCascadeDatanode);
	$('#aipACKCreateName').attr('readonly',true);
	$('#aipACKCreateTime').attr('readonly',true);
	$('#aipACKUpdateName').attr('readonly',true);
	$('#aipACKUpdateTime').attr('readonly',true);
	
	layer.load(1, {
		shade : [ 0.2 ]
	});
	if (isUpdate == "true") {
		var entityId = CommonUtils.getUrlParam("entityId");
		$.ajax({
			url : '../../aipcomponent/detail.do',
			dataType : 'json',
			type : 'post',
			data : {'entityId' : entityId},
			success : function(data) {
				if (data.success) {
					FormUtils.fillFormByData("aipComponentKettleForm", data.responseData);
					$("#aipCKCategoryId").val(data.responseData.categoryId);
					$("#aipCKCategoryName").val(data.responseData.categoryName);
					$("#aipACKResouceCode").val(data.responseData.resoucename);
					$("#aipACKTargetServerAppName").val(data.responseData.targetServerApp ? data.responseData.targetServerApp.split(";")[1] : "");
					
					$("#aipACKCreateName").val(data.responseData.creator);
					$("#aipACKUpdateName").val(data.responseData.modifiedBy);
					$("#aipACKCreateTime").val(getSmpFormatDateByLong(data.responseData.createDate, true));
					$("#aipACKUpdateTime").val(getSmpFormatDateByLong(data.responseData.modifyDate, true));
					//initConnection(data.responseData.appId, data.responseData.connectionId);
					//initChannel(data.responseData.appId, data.responseData.channelId);
					initResource();
					resourceCascadeDatanode(data.responseData.dataNode, data.responseData.connectionId);
					if (data.responseData.inputParams) {
						var inputParams = JSON.parse(data.responseData.inputParams);
						$.fn.zTree.init($("#inputTree"), setting2, inputParams);
					}
					/*if (data.responseData.outputParams) {
						var outputParams = JSON.parse(data.responseData.outputParams);
						$.fn.zTree.init($("#outputTree"), setting3, outputParams);
					}*/
					var outputParams = [];
					if (data.responseData.outputParams) {
						outputParams = JSON.parse(data.responseData.outputParams);
						$.each(outputParams, function(i){
							var obj = {
									//orderNo : i + 1,
									orderNo : this.id,
									output_name : this.name,
									output_MaxOccurs : this.MaxOccurs,
									output_MinOccurs : this.MinOccurs,
									output_required : this.required == true ? "是":"否",
									output_variableType : this.variableType
							};
							$("#outputParamGrid").jqGrid("addRowData", i + 1, obj, "last");
						});
					}
					var dxsUser = JSON.parse(data.responseData.aipUsers);
					var div = $("#alreadyUsers");
					$.each(dxsUser, function(){
						var li = $("<div></div>");
						li.addClass("feed-element");
						li.attr("id", this.id);
						li.append("<div class='feed-body'>"+this.name+"</div>");
						div.append(li);
					})
					$("#aipACKExecuteType").trigger("change");
					$("#aipCKAuthType").trigger("change");
					$("#aipACKServletSupport").val(data.responseData.servletSupport);
				}
				
				initUserTree();
			}
		});
	} else {
		FormUtils.fillFormByData("aipComponentKettleForm", {id:''});
		initResource();
		//initConnection(appId);
		//initChannel(appId);
		$.fn.zTree.init($("#inputTree"), setting2, zNodes);
//		$.fn.zTree.init($("#outputTree"), setting3, zNodesOut);
	}
	layer.closeAll('loading');
}

function initResource(){
	layer.load(1, {
		shade : [ 0.2 ]
		// 透明度调整
	});
	$.ajax({
		url : '../../resource/findResource.do',
		dataType : 'json',
		type : 'post',
		async: true,
		success: function(data){
			if(data.success) {
				var resourceArray = [];
				$.each(data.responseData, function(){
					var returndata = {
						"code" : this.code,
						"name" : this.name
					}
					resourceArray.push(returndata);
				})
				var resourceBsSuggest = $("#resourceName").bsSuggest({
					listStyle: {
				        "padding-top":0, "max-width": "800px",
				        "overflow": "auto", "width": "auto",
				        "transition": "0.3s", "-webkit-transition": "0.3s", "-moz-transition": "0.3s", "-o-transition": "0.3s",
				        "height": "100px"
				    },
					data : {
						"value" :resourceArray,
						"defaults" : "未配置资源名称"
					}
				}).on('onSetSelectValue', function (e, keyword) {
					//点击事件 触发数据节点加载
					//$("#resourceName").trigger("change");
					resourceCascadeDatanode('');
			    });
			}
			layer.closeAll('loading');
		}
	})
}

//根据资源名称code查出数据节点
function resourceCascadeDatanode(defaultValue, connectionId){
	var resourceName = $("#resourceName").val();
	var executeType = $("#aipACKExecuteType").val();
	if (resourceName && executeType == 'SYNC' && typeof defaultValue === 'string') {
		top.layer.load(1, {
			shade : [ 0.2 ]
			// 透明度调整
		});
		if (!connectionId)
			connectionId = $("#aipACKDxsConnection").val();
		$.ajax({
			url:"../../aipcomponent/findDataNode.do",
			dataType : 'json',
			type : 'post',
			async : false,
			data : {
				"resourceCode" : resourceName,
				"connectionId" : connectionId
			},
			success:function(data){
				if (data.success) {
					var select = $("#aipACKDataNode").empty();
					$.each(data.responseData, function(){
						var option = $("<option></option>");
						option.attr("value", this);
						option.text(this);
						select.append(option);
					})
					if (defaultValue)
						select.val(defaultValue);
				}
				top.layer.closeAll("loading");
			}
		})
	}
}

function closeLayer(){
	if(connectionFlag && channelFlag){
		layer.closeAll('loading');
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
		url : "../../user/queryAllUserTree.do",
		dataType : "json",
		type : "post",
		data : {"type":"RESPOND"},
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
		}
	});
}

var initSetpForm = function() {
	$("#aipComponentKettleForm").steps({
		bodyTag : "fieldset",
		enableAllSteps: true,
		onStepChanging : function (event, currentIndex, newIndex) {
			if (currentIndex > newIndex) {
				return true;
			}
			var form = $(this);
			var AuthTypeValue = $("#aipCKAuthType").val();
			
			if(currentIndex == 1 && AuthTypeValue=="BASIC"){
					if (validateUser())
						return true;
					else
						return false;
				} else if (currentIndex == 2 ){
					var flag = validInputParams();
					var flag2 = validInputTree();
					if (!flag || !flag2)
						return false;
			} else {
				if (currentIndex < newIndex) {
					$(".body:eq("+ newIndex+ ") label.error",form).remove();
					$(".body:eq("+ newIndex+ ") label.error",form).removeClass("error");
					form.validate().settings.ignore = ":disabled,:hidden";
					return form.valid();
				}
			}
			
			if (currentIndex < newIndex) {
				$(".body:eq("+ newIndex+ ") label.error",form).remove();
				$(".body:eq("+ newIndex+ ") label.error",form).removeClass("error");
				form.validate().settings.ignore = ":disabled,:hidden";
				return form.valid();
			}
			//resizeForm(currentIndex);
		},
		onStepChanged : function (event, currentIndex, priorIndex) {
			
			resizeForm(0);
		},
		onFinishing : function (event, currentIndex) {
			var form = $(this);
			form.validate().settings.ignore = ":disabled";
			return form.valid() && /*validInputParams() &&*/ validOutputParams() && validInputTree();
		},
		onFinished : function (event, currentIndex) {
			submitAIPComponentKettleFormHandler();
		},
		onCanceled : function (event) {
			closeSelf();
		}
	}).validate({
		errorPlacement : function(error, element) {
//				error.appendTo(element.is(":radio") || element.is(":checkbox") ? element.parent().parent()
//						.parent() : element.parent())
			error.appendTo(element.closest('td'));
			var currentSetId = element.closest("fieldset")[0].id;
			var currentSetIndex = currentSetId.substring(currentSetId.length - 1);
			//resizeForm(currentSetIndex);
		},
		highlight: function (e) {
	        $(e).closest('td').removeClass('has-success')
	        $(e).closest('td').addClass('has-error');  
	    }, 
		success: function(label) {
			label.closest('td').removeClass("has-error").addClass("has-success");
			//为了修复在使用input-group-btn时验证样式错位问题  special
			label.prevUntil(".input-group-btn").find("button").addClass("help-block").addClass("m-b-none");
		},
		rules : {
			code : {
				required : true
			},
			name : {
				required : true
			},
			dxsConnectionId : {
				required : true
			},
			dxsChannelId : {
				required : true
			},
			orderNo : {
				required : true,
				digits : true
			},
			ackTragetServiceAppName : {
				required : true,
			}
		},
		messages : {
			code : {
				required : "请输入服务编码"
			},
			name : {
				required : "请输入服务名称"
			},
			dxsConnectionId : {
				required : "请选择连接对象"
			},
			dxsChannelId : {
				required : "请选择所属通道"
			},
			orderNo : {
				required : "请输入排序号码",
				digits : "请输入排序号码"
			},
			ackTragetServiceAppName : {
				required : "请选择目标应用",
			}
		}
	});
	$("#aipComponentKettleForm").removeClass("hide");
	resizeForm(0);
}
function validInputTree() {
	var f = true;
	var inputTree = $.fn.zTree.getZTreeObj("inputTree");
	var node = inputTree.getNodes()[0];
	var inputnodes = inputTree.getNodesByParam("elementType", "complexContent", node);
	$.each(inputnodes, function(i){
		if(!(inputnodes[i].children)){
			if(!(inputnodes[i].arr)){
				f = false;
				return false;
			}
		}
	});
	if(!f){
		CommonUtils.notify("error", "输入参数中，xsd结构未建全，请补全复杂类型中的简单结构元素！", "4000");
	}
	return f;
}
function validInputParams() {
	var list = $("#inputParamGrid").jqGrid("getRowData");
	var colDataIDs = $("#inputParamGrid").jqGrid("getDataIDs");
	
	var flag = true;
	if(colDataIDs.length > 0){
		var orderNo;
		$.each(list, function(i){
			if (this.input_name.indexOf("editable inline-edit-cell form-control")==-1){
				
			}else{
				flag = false;
				orderNo = this.orderNo;
				return false;
			}
		});
		if(!flag){
			CommonUtils.notify("error","第"+orderNo +"行信息未填写","4000");			
		}
	}
	return flag;
}

function validOutputParams() {
	var list = $("#outputParamGrid").jqGrid("getRowData");
	var colDataIDs = $("#outputParamGrid").jqGrid("getDataIDs");
	
	var flag = true;
	if(colDataIDs.length > 0){
		var orderNo;
		$.each(list, function(i){
			if ((this.output_name.indexOf("editable inline-edit-cell form-control")==-1)
					&&(this.output_MaxOccurs.indexOf("editable inline-edit-cell form-control")==-1)
					&&(this.output_MinOccurs.indexOf("editable inline-edit-cell form-control")==-1)){
				
			}else{
				flag = false;
				orderNo = this.orderNo;
				return false;
			}
		});
		if(!flag){
			CommonUtils.notify("error","第"+orderNo +"行信息未填写","4000");			
		}
	}
	return flag;
}

//jQuery.validator.addMethod("combin", function(value, element) {
//	var chrnum = /^[a-zA-Z][0-9a-zA-Z]{7}$/;
//	return this.optional(element) || (chrnum.test(value));
//	}, "数字字母组合，以字母开始，且八位");

function validateUser(){
	var treeObj = $.fn.zTree.getZTreeObj("userTree");
	var nodes = treeObj.getCheckedNodes(true);
	if(nodes.length<2){
		CommonUtils.notify("error", "请选择用户", "4000");
		return false;
	}
	return true;
}
function resizeForm(currentIndex) {
	console.log();
	var h = $("#aipComponentKettleForm-p-"+currentIndex+" > .fieldcontent").height() + 80;
	if (currentIndex == 1) 
		h += 320;
	h = 450;//高度固定519px
	$(".wizard-big.wizard > .content").attr("style", "height:"+h+"px");
}
function checkNameNode(node,name,id){
	//在节点下查询名称是否重复
	var f = false;
	if(node.arr){
		$.each(node.arr, function(){
			if(this.id!=id){
				if(this.name==name){
					f=true;
					return false;
				}
			}
		});
	}
	/*if(!f){
		if(node.children){
			$.each(node.children, function(){
				f = checkNameNode(this,name,id);
				if(f){
					return false;
				}
			});
		}
	}*/
	return f;
}
function CheckName(name,id){
	var f = false;
	var inputTree = $.fn.zTree.getZTreeObj("inputTree");
	var node = inputTree.getNodes()[0];
	var inputnodes = inputTree.getNodesByParam("name", name, null);//查询所有元素名称相同的树节点
//	var elmentNodes = inputTree.getNodesByParam("elementType", "complexContent", node);//查询所有复杂结构的树节点
	var list = $("#outputParamGrid").jqGrid("getRowData");
	if(inputnodes.length>0){//校验树上的名称
		f=true;
	}else{
		var selectedNode = inputTree.getSelectedNodes()[0];//获取当前节点
		f = checkNameNode(selectedNode, name, id);//校验当前节点右侧简单元素的名称
		if(!f){//校验输出参数的名称
			$.each(list, function(i){
				if(this.orderNo!=id){
					if(this.output_name==name){
						f=true;
						return false;
					}
				}
			});
		}
	}
	return f;
}

function input_modify_detail(id, a){
	$("#inputParamGrid").jqGrid("saveRow", id);
	var f =true;
	var Regx = /^[0-9]*$/;
	var row = $("#inputParamGrid").jqGrid("getRowData", id);
	var rowData = $("#inputParamGrid").getRowData(id);
	if(row.input_MaxOccurs == ""){
		CommonUtils.notify("error", "请填写最多次", "4000");
		$("#inputParamGrid").jqGrid("editRow", id);
		f = false;
	}else if(row.input_MaxOccurs!='unbounded' && !Regx.test(row.input_MaxOccurs)){
		CommonUtils.notify("error", "最多次请输入数字或unbounded", "4000");
		$("#inputParamGrid").jqGrid("editRow", id);
		f = false;
	}
	if(row.input_MinOccurs == ""){
		CommonUtils.notify("error", "请填写最少次", "4000");
		$("#inputParamGrid").jqGrid("editRow", id);
		f = false;
	}else if(!Regx.test(row.input_MinOccurs)){
		CommonUtils.notify("error", "最少次请输入数字", "4000");
		$("#inputParamGrid").jqGrid("editRow", id);
		f = false;
	}
	if(row.input_name == ""){
		CommonUtils.notify("error", "请填写参名称", "4000");
		$("#inputParamGrid").jqGrid("editRow", id);
		f = false;
	}else if(CheckName(row.input_name,id)){
		CommonUtils.notify("error", "有重复的参数名称，请重新填写参数名称", "4000");
		$("#inputParamGrid").jqGrid("editRow", id);
		f = false;
	}
	if(row.input_value == "" && row.input_type == "常量"){
		CommonUtils.notify("error", "请填写参数值", "4000");
		$("#inputParamGrid").jqGrid("editRow", id);
		f = false;
	}else if(row.input_type == "变量"){
		$("#inputParamGrid").setCell(id, 'input_value', '${'+row.input_name+'}');
		row.input_value = row.input_name
	}
	if(f){
		var rows = {'id':row.orderNo,
		            'name':row.input_name,
		            'value':row.input_value,
		            'MaxOccurs':row.input_MaxOccurs,
		            'MinOccurs':row.input_MinOccurs,
		            'type':row.input_type == "常量" ? "CONSTANT" : "VARIATE",
		            'required':row.input_required == "是" ? true : false,
		            'variableType':row.input_variableType
		            };
		//向树上保存数据
		var zTree = $.fn.zTree.getZTreeObj("inputTree");
		var nodes = zTree.getSelectedNodes()[0];
		if(nodes.arr){
			$.each(nodes.arr, function(i){
				if(this.id==rows.id){
					//修改节点数组
					this.id=i+1;
					this.name = rows.name;
					this.value = rows.value;
					this.MaxOccurs = rows.MaxOccurs;
					this.MinOccurs = rows.MinOccurs;
					this.type = rows.type ;
					this.required = rows.required ;
					this.variableType = rows.variableType;
				}
			});
			if(nodes.arr.length<rows.id){
				nodes.arr.push(rows);
			}
		}else{
			nodes.arr = [];
			nodes.arr.push(rows);
		}
		zTree.updateNode(nodes);
	}
}

function output_modify_detail(id, a){
	$("#outputParamGrid").jqGrid("saveRow", id);
	var Regx = /^[0-9]*$/;
	var row = $("#outputParamGrid").jqGrid("getRowData", id);
	var rowData = $("#outputParamGrid").getRowData(id);
	if(row.output_MaxOccurs == ""){
		CommonUtils.notify("error", "请填写最多次", "4000");
		$("#outputParamGrid").jqGrid("editRow", id);
	}else if(row.output_MaxOccurs!='unbounded' && !Regx.test(row.output_MaxOccurs)){
		CommonUtils.notify("error", "最多次请输入数字或unbounded", "4000");
		$("#outputParamGrid").jqGrid("editRow", id);
	}
	if(row.output_MinOccurs == ""){
		CommonUtils.notify("error", "请填写最少次", "4000");
		$("#outputParamGrid").jqGrid("editRow", id);
	}else if(!Regx.test(row.output_MinOccurs)){
		CommonUtils.notify("error", "最少次请输入数字", "4000");
		$("#outputParamGrid").jqGrid("editRow", id);
	}
	if(row.output_name == ""){
		CommonUtils.notify("error", "请填写参名称", "4000");
		$("#outputParamGrid").jqGrid("editRow", id);
	}else if(CheckName(row.output_name,id)){
		CommonUtils.notify("error", "有重复的参数名称，请重新填写参数名称", "4000");
		$("#outputParamGrid").jqGrid("editRow", id);
	}
}

function add_inputparamdetail(updatebtn){
	var zTree = $.fn.zTree.getZTreeObj("inputTree");
	var nodes = zTree.getSelectedNodes();
	
	if (nodes && nodes.length != 1) {
		layer.tips('您未选择元素节点！', updatebtn, {
			  tips: [2, '#18a689'],
			  time: 2000
		});
		return false;
	}
	var colDatas = $("#inputParamGrid").jqGrid("getRowData");
	var f = false;
	if(colDatas){
		$.each(colDatas, function(i){
			if ((this.input_name.indexOf("editable inline-edit-cell form-control")==-1)
					&&(this.input_value.indexOf("editable inline-edit-cell form-control")==-1)
					&&(this.input_MaxOccurs.indexOf("editable inline-edit-cell form-control")==-1)
					&&(this.input_MinOccurs.indexOf("editable inline-edit-cell form-control")==-1)){
			}else{
				f = true;
				return false;
			}
		});
	}
	if(f){
		layer.tips('请先保存未保存的元素！', updatebtn, {
			  tips: [2, '#18a689'],
			  time: 2000
		});
		return false;
	}
	var row = {
		orderNo : colDatas.length + 1,
		name : "",
		value : "",
		MaxOccurs:"1",
		MinOccurs:"1",
		type : "",
		required : "",
		variableType:""
	};
	$("#inputParamGrid").jqGrid("addRowData", colDatas.length + 1, row, "last");
	$("#inputParamGrid").jqGrid("editRow", colDatas.length + 1);

}
function add_outputparamdetail(){
	var colDatas = $("#outputParamGrid").jqGrid("getRowData");
	
	var row = {
			orderNo : colDatas.length + 1,
			name : "",
			MaxOccurs:"",
			MinOccurs:"",
			required : "",
			variableType : ""
	};
	$("#outputParamGrid").jqGrid("addRowData", colDatas.length + 1, row, "last");
	$("#outputParamGrid").jqGrid("editRow", colDatas.length + 1);
	
}

function input_del_detail(id){
	var colDatas = $("#inputParamGrid").jqGrid("getRowData");
	var zTree = $.fn.zTree.getZTreeObj("inputTree");
	var nodes = zTree.getSelectedNodes()[0];
	if(colDatas){
		nodes.arr = [];
		if(colDatas.length==1){
			$("#inputParamGrid").jqGrid("clearGridData");
		}else{
			$("#inputParamGrid").jqGrid("delRowData", id);
			var ResidualData = $("#inputParamGrid").jqGrid("getRowData");
			$("#inputParamGrid").jqGrid("clearGridData");
			$.each(ResidualData, function(i){
				var rows = {'id':i+1,
			            'name':this.input_name,
			            'value':this.input_value,
			            'MaxOccurs':this.input_MaxOccurs,
			    		'MinOccurs':this.input_MinOccurs,
			            'type':this.input_type == "常量" ? "CONSTANT" : "VARIATE",
			            'required':this.input_required == "是" ? true : false,
			            'variableType':this.input_variableType
			            };
				if ((this.input_name.indexOf("editable inline-edit-cell form-control")==-1)
						&&(this.input_value.indexOf("editable inline-edit-cell form-control")==-1)
						&&(this.input_MaxOccurs.indexOf("editable inline-edit-cell form-control")==-1)
						&&(this.input_MinOccurs.indexOf("editable inline-edit-cell form-control")==-1)){
					nodes.arr.push(rows);
				}
				//nodes.arr.push(rows);
			});
			if(nodes.arr){
				$.each(nodes.arr, function(i){
					var obj = {
							orderNo : this.id,
							input_name : this.name,
							input_value : this.value,
							input_MaxOccurs:this.MaxOccurs,
							input_MinOccurs:this.MinOccurs,
							input_type : this.type == "CONSTANT" ? "常量" : "变量",
							input_required : this.required == true ? "是" : "否",
							input_variableType : this.variableType
					};
					$("#inputParamGrid").jqGrid("addRowData", this.id, obj, "last");
				});
			}
		}
	}
}
function output_del_detail(id){
	var colDatas = $("#outputParamGrid").jqGrid("getRowData");
	if(colDatas){
		if(colDatas.length==1){
			$("#outputParamGrid").jqGrid("clearGridData");
		}else{
			$("#outputParamGrid").jqGrid("delRowData", id);
		}
	}
}
//=============================配置xsd=======================================
var setting2 = {
		view: {
			dblClickExpand: false
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
		callback: {
			onClick : nodeClick,
			onAsyncSuccess : zTreeOnAsyncSuccess,
			onRightClick: OnRightClick
		}
	};
	
	function nodeClick(event, treeId, treeNode, clickFlag) {
		//清空列表
		jQuery("#inputParamGrid").jqGrid("clearGridData");
		if(treeNode.arr){
			$.each(treeNode.arr, function(i){
				if(this.type == 'VARIATE'){
					this.value = '${'+this.name+'}';
				}
				var obj = {
						orderNo : i+1,
						input_name : this.name,
						input_value : this.value,
						input_MaxOccurs:this.MaxOccurs,
						input_MinOccurs:this.MinOccurs,
						input_type : this.type == "CONSTANT" ? "常量" : "变量",
						input_required : this.required == true ? "是" : "否",
						input_variableType : this.variableType
				};
				$("#inputParamGrid").jqGrid("addRowData", i + 1, obj, "last");
			});
		}
	}

	function zTreeOnAsyncSuccess(event, treeId, treeNode, msg) {
		var tree = $.fn.zTree.getZTreeObj(treeId);
		tree.expandAll(true);
		layer.closeAll('loading');
	}
	
	function OnRightClick(event, treeId, treeNode) {
		if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
			showRMenu("root", event.clientX, event.clientY-100);
		} else if (treeNode && !treeNode.noR) {
			var zTree = $.fn.zTree.getZTreeObj(treeId);
			zTree.selectNode(treeNode);
			//是否是根节点
			if(treeNode.id=='0'){
				showRMenu("treeRoot", event.clientX-50, event.clientY-100);
			}else{
				showRMenu("node", event.clientX-50, event.clientY-100);
			}
		}
	}

	function showRMenu(type, x, y) {
		$("#rMenu ul").show();
		if(type=="treeRoot"){
			$("#m_add").show();
			$("#m_upd").hide();
			$("#m_del").hide();
			$("#m_reset").show();
		}else if (type=="root") {
			$("#m_add").hide();
			$("#m_upd").hide();
			$("#m_del").hide();
			$("#m_reset").hide();
		} else {
			$("#m_add").show();
			$("#m_upd").show();
			$("#m_del").show();
			$("#m_reset").show();
		}
		rMenu.css({"top":y+"px", "left":x+"px", "visibility":"visible"});

		$("body").bind("mousedown", onBodyMouseDown);
	}
	function hideRMenu() {
		if (rMenu) rMenu.css({"visibility": "hidden"});
		$("body").unbind("mousedown", onBodyMouseDown);
	}
	function onBodyMouseDown(event){
		if (!(event.target.id == "rMenu" || $(event.target).parents("#rMenu").length>0)) {
			rMenu.css({"visibility" : "hidden"});
		}
	}
	//取出树上所有节点的名称
	function ztreeName(node,id){
		var names = [];
		if(node.id!=id){
			names.push(node.name);
		}
		if(node.arr){
			$.each(node.arr, function(){
				if(this.id!=id){
					names.push(this.name);
				}
			});
		}
		if(node.children){
			$.each(node.children, function(){
				var s = ztreeName(this,id);
				Array.prototype.push.apply(names,s);
			});
		}
		return names;
	}
	// ====================================================打开元素form表单页面======================================
	var opendxsElementLayer = function(update, updatebtn) {
		hideRMenu();
		var paramStr = '';
		var ElementId2 = ElementId;
		var zTree = $.fn.zTree.getZTreeObj("inputTree");
		var node = zTree.getNodes()[0];
		var nodes = zTree.getSelectedNodes();
//		var elmentNodes = zTree.getNodesByParam("elementType", "complexContent", node);//查询所有复杂结构的树节点
		var lists = $("#outputParamGrid").jqGrid("getRowData");
		var names = [];
		names = ztreeName(node,nodes[0].id);
		$.each(lists, function(i){
			names.push(this.output_name);
		});
		
		var arrnodes = {'id' : nodes[0].id, 
						'name':nodes[0].name, 
						'elementType':nodes[0].elementType,
						'maxOccurs':nodes[0].maxOccurs,
						'minOccurs':nodes[0].minOccurs,
						'required':nodes[0].required
						};
		var params = {
			"arr" : JSON.stringify(arrnodes)
		};
		if (params) {
			paramStr = CommonUtils.urlEncode(params);
		}
		top.layer.open({
			type : 2,
			title : FORM_TITLE_PRE + '元素管理表单页',
			closeBtn : 1, // 不显示关闭按钮
			shadeClose : false,
			shade : false,
			scrollbar : false,
			maxmin : true, // 开启最大化最小化按钮
			area : [ '40%', '50%' ],
			shade : [ 0.3 ],
			content : 'pages/aipComponentKettle/ElementForm.html?isUpdate=' + update +  paramStr + '&v=' + version,
			btn : [ '保存', '取消' ],
			yes : function(index, layero) {
				var iframeWin = layero.find('iframe')[0].contentWindow; // 得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
				var ElementNode =iframeWin.ElementCallback(index,ElementId2,names);
				if (zTree.getSelectedNodes()[0]) {
//					ElementNode.checked = zTree.getSelectedNodes()[0].checked;
					if(update){
						var nodes = zTree.getSelectedNodes()[0];
						nodes.id = ElementNode.id;
						nodes.name = ElementNode.name;
						nodes.elementType = ElementNode.elementType;
						nodes.maxOccurs = ElementNode.maxOccurs;
						nodes.minOccurs = ElementNode.minOccurs;
						nodes.required = ElementNode.required;
						zTree.updateNode(nodes);
					}else
						zTree.addNodes(zTree.getSelectedNodes()[0], ElementNode);
				} else {
					zTree.addNodes(null, ElementNode);
				}
			},
			end : function(index) {
				top.layer.closeAll('loading');
			}
		});
		if(!update)
			ElementId++;
	}
	// ====================================================结束======================================
	
	function removeTreeNode() {
		hideRMenu();
		var zTree = $.fn.zTree.getZTreeObj("inputTree");
		var nodes = zTree.getSelectedNodes();
		if (nodes && nodes.length>0) {
			if (nodes[0].children && nodes[0].children.length > 0) {
				var msg = "要删除的节点是父节点，如果删除将连同子节点一起删掉。\n\n请确认！";
				var ifram = layer.confirm('<small>'+msg+'</small>', {
					title : '<small>系统提示</small>',
					closeBtn : 0,
					icon:0,
					btn : [ '确定', '取消' ]
				},function(index) {
					ElementId--;
					zTree.removeNode(nodes[0]);
					layer.close(ifram);
				});
			} else {
				var ifram = layer.confirm('<small>您确定要执行此操作吗?</small>', {
					title : '<small>系统提示</small>',
					closeBtn : 0,
					icon:0,
					btn : [ '确定', '取消' ]
				},function(index) {
					ElementId--;
					zTree.removeNode(nodes[0]);
					layer.close(ifram);
				});
			}
		}
	}
	function resetTree(treeNode) {
		hideRMenu();
		var treeObj = $.fn.zTree.getZTreeObj("inputTree");
		treeObj.refresh();
	}
