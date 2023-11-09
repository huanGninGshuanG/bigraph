//validate初始化
$.validator.setDefaults({
	highlight : function(e) {
		$(e).parent().removeClass("has-success").addClass(
				"has-error")	
	},
	success : function(e) {
		e.parent().removeClass("has-error").addClass(
				"has-success")
	},
	errorElement : "span",
	errorPlacement : function(e, r) {
		e.appendTo(r.is(":radio") || r.is(":checkbox") ? r.parent().parent()
				.parent() : r.parent())
	},
	errorClass : "help-block m-b-none",
	validClass : "help-block m-b-none"
});
//字符验证 
$.validator.addMethod("stringCheck", function(value, element) { 
   
	return this.optional(element) || /^[\u0391-\uFFE5\w]+$/.test(value); 
}, "只能包括中文字、英文字母、数字和下划线");

//电话号码
$.validator.addMethod("isMobile", function(value, element) { 
  var length = value.length; 
  var mobile = /^(((13[0-9]{1})|(15[0-9]{1}))+\d{8})$/; 
  return this.optional(element) || (length == 11 && mobile.test(value)); 
}, "请正确填写您的手机号码"); 


var FormUtils = {
	findProp : function(obj, prop){
	    prop = prop.split('.');
	    for(var i=0; i<prop.length; i++)
	    {
	    	obj = obj[prop[i]];
	    	if (!obj) break;
	    }
	    return obj;
	},
	fillFormByData : function(formId,data){
		if (!data) return;
		var form = document.getElementById(formId);
		if ($(form).is('form')){
			for ( var i = 0, e; e = form.elements[i]; i++) {
				if (e.name == null || $.trim(e.name) == '') {
				} else {
					var value = FormUtils.findProp (data, e.name);
					if (value != null) e.value = value;
				}
			}
		}
	},
	serializeJson : function(formId) {
		var serializeObj = {};
		var form = $("#"+formId);
		var array = form.serializeArray();
		$(array).each(function(){
			if (serializeObj[this.name]) {
				if ($.isArray(serializeObj[this.name])) {
					serializeObj[this.name].push(this.value);  
				} else {
					serializeObj[this.name] = [serializeObj[this.name],this.value];
				}
			} else {
				serializeObj[this.name] = this.value;   
			}
		})
		return serializeObj;
	},
	submitHandler: function(iframeWin,callback,formId,title){
		
		if($('#' + formId).validate().form()){
			top.layer.confirm('<small>' + (title == null || title == "" ?  '您确定要执行此操作吗?' : tile)  + '</small>', {
				title : '<small>系统提示</small>',
				closeBtn : 0,
				icon:0,
				// shift: 1, //提示框载入动画
				// skin: 'layui-layer-molv', //样式类名
				btn : [ '确定', '取消' ]
			// 按钮
			}, function(index) {
				top.layer.load(1, {
					shade : [ 0.2 ]
					// 透明度调整
				});
				var func = eval(callback);
				new func(top.layer.getFrameIndex(window.name), index, iframeWin);
			});
		}
	},
	submitHandlerMul: function(iframeWin,callback,formIdList,title){
//		alert(formIdList[0]);
		var judge=1;
		for (i in formIdList) {
			var formId = formIdList[i].formid;
//			alert(formId);
			var li=formIdList[i].li;
			$("li[id]").removeClass("active");
			$('#' + li).addClass("active");
			var tab=formIdList[i].tab;			
			$("div[id^='tab']").removeClass("active");
			$('#' + tab).addClass("active");
 
			if($('#' + formId).validate().form()){
				
			}else{
				judge=0;
				break;
			}
			
		}
		
 
		if (judge==1){ 
			var li=formIdList[0].li;
			$("li[id]").removeClass("active");
			$('#' + li).addClass("active");
			var tab=formIdList[0].tab;			
			$("div[id^='tab']").removeClass("active");
			$('#' + tab).addClass("active");
			
			top.layer.confirm('<small>' + (title == null || title == "" ?  '您确定要执行此操作吗?' : tile)  + '</small>', {
				title : '<small>系统提示</small>',
				closeBtn : 0,
				icon:0,
				// shift: 1, //提示框载入动画
				// skin: 'layui-layer-molv', //样式类名
				btn : [ '确定', '取消' ]
			// 按钮
			}, function(index) {
				top.layer.load(1, {
					shade : [ 0.2 ]
					// 透明度调整
				});
				var func = eval(callback);
				new func(top.layer.getFrameIndex(window.name), index, iframeWin);
			});
		}
 
	}
}
