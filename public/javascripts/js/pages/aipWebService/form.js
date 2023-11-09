$.validator.setDefaults({
	highlight : function(e) {
		$(e).closest(".form-group1").removeClass("has-success").addClass(
				"has-error")
	},
	success : function(e) {
		e.closest(".form-group1").removeClass("has-error").addClass(
				"has-success")
	},
	errorPlacement : function(e, r) {
		var div = $("<div class='col-sm-12'></div>");
		e.appendTo(div);
		r.closest(".form-group1").append(div);

	} 
});
$("#aipWebServiceForm").validate({
 		rules : {
 			saopUrl : {
 				required : true
 			}
 		},
 		messages : {
 			saopUrl : {
 				required : "请输入soap请求地址",
 			}
 		},
 		onfocusout : function(element) {
 			$(element).valid();
 		}
});

//获取文本框里的值
function save(confirmIndex){
	var aipWebServiceUrl = $("#aipWebServiceUrl").val();
	var aipWebServiceUser = $("#aipWebServiceUser").val();
	var aipWebServicePw = $("#aipWebServicePw").val();	
//将获得的值放入对象中返回main页面		
	var soap = {
				'aipWebServiceUrl':	aipWebServiceUrl,
				'aipWebServiceUser':aipWebServiceUser,
				'aipWebServicePw':aipWebServicePw
	        	};
	   return soap;
   };