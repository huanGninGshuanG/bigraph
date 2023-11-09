 
var submitAipNotifyForm = function(formWinIndex,confirmIndex){
 
//	if ($("#aipUserOrderNo").val()==""){
//		$("#aipUserOrderNo").val("0");
//	}else{
//	if (isNaN($("#aipUserOrderNo").val())){
//		CommonUtils.notify("error","排序号码是整型","4000");
//		
//		top.layer.closeAll('loading');
//		top.layer.close(confirmIndex);
//		return;
//	}
//	} 
//	
	if ($("#aipstatus2").val()=="STATUS_PUBLISH"){
		CommonUtils.notify("error","已发布的消息不能修改","4000");
		
		top.layer.closeAll('loading');
		top.layer.close(confirmIndex);
		return;
	}
	
	$.ajax({
		url : '../../notify/saveOrUpdate.do',
		dataType : 'json',
		type : 'post',
		data : $('#aipNotifyForm').serialize()+"&content="+$("#aipcontent").val(),
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
var initaipNotifyForm = function(){
	
 
 
 

    var isUpdate = CommonUtils.getUrlParam("isUpdate");
 
	if(isUpdate == "true"){
		var entityId = CommonUtils.getUrlParam("entityId");
		layer.load(1, {
			shade : [ 0.2 ]
		// 透明度调整
		});
		$.ajax({
			url : '../../notify/detail.do',
			dataType : 'json',
			type : 'post',
			data : 'entityId=' + entityId,
			success : function(data) {
				
				if (data.success) {
 
 
 
					FormUtils.fillFormByData("aipNotifyForm",data.responseData);
					$("#aipcontent").val(data.responseData.content);
					$("#aipstatus2").val(data.responseData.status);
				}
				layer.closeAll('loading');
			}
		});
	}else{
		FormUtils.fillFormByData("aipNotifyForm", {id:''});
	}
}

var acceptForm = function(){
	var nodeId = $("#aipAcceptId").val();
	
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
		area : [ '25%', '60%' ],
		shade : [ 0.3 ],
		content : "pages/aipNotify/appForm.html?v=" + version+ paramStr ,
		btn : [ '确认', '取消' ],
		yes : function(index, layero) {
			aipNotifyIsRefresh = true;
			var iframeWin = layero.find('iframe')[0]; //得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method();
			
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
			
			$("#aipAcceptId").val(powerids);
			$("#aipappectname").val(powername);
			if ($("#aipappectname").val()!=''){
	 
				$("#aipappectname").closest("div.form-group").removeClass("has-error");
				$("#aipappectname").closest("div.col-sm-8").find("span").filter(".help-block").hide();
			}
		},
		
		btn2: function(index, layero){
		    //按钮【按钮二】的回调
			aipUserIsRefresh = false;
		},
		cancel: function(){ 
		    //右上角关闭回调
			aipUserIsRefresh = false;
		},
		success : function(layero, index) {
			aipUserIsRefresh = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：aipRoleIframeWin.method()
		},
		end : function(index) {
			top.layer.closeAll('loading');
			if(aipUserIsRefresh)
				$("#aipUserGrid").trigger("reloadGrid");
		}
	});
};

$.validator.addMethod("addm", function(value) {
	var Regx = /[\u4E00-\u9FA5]/i;
    
    if (!Regx.test(value)) {
        return true;
    }
        return false;
  }, '不可输入汉字！');
$.validator.addMethod("addm2", function(value) {
	if(value>0){
		return true;
	}
        return false;
  }, '请正整数');
 

$("#aipNotifyForm").validate({
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
		code:{
			required : true,
			rangelength:[4,30],
			addm:true
		},
		name : {
			required : true
		},
		orderNo:{
			required : true,
			digits : true
		} 
	},
	messages : {
		code:{
			required : '消息编码必须填写',
			rangelength:'消息编码必须在4-30之间'
		},
		name : {
			required : '消息名称必须填写'
		},orderNo:{
			required : '排序号码必须填写',
			digits : '排序号码必须是整数'
		} 
	},
	onfocusout : function(element) {
		$(element).valid();
	}
});

$("#aipNotifyForm2").validate({
 
	rules : {
		content : {
			required : true
		} 
	},
	messages : {
		content : {
			required : '     消息内容必须填写'
		} 
	},
	onfocusout : function(element) {
		$(element).valid();
	}
});
