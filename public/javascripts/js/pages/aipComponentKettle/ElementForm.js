$.validator.addMethod("addm", function(value) {
	var Regx = /^[0-9]*$/;
	if(value=='unbounded' || Regx.test(value)){
		return true;
	}
        return false;
  }, '请输入数字或unbounded');
$("#dxsElementForm").validate({
	rules : {
		name : {
			required : true
		},
		maxOccurs:{
			required :true,
			addm : true
		},
		minOccurs:{
			required : true,
			digits : true
		}
		
	},
	messages : {
		name : {
			required : "请输入元素名称"
		},
		maxOccurs:{
			required : "请输入最多出现次数"
		},
		minOccurs:{
			required : "请输入最少出现次数",
			digits : "请输入数字"
		}
	},
	onfocusout : function(element) {
		$(element).valid();
	}
});
function formCheckName(names,name){
	var f = false;
	$.each(names, function(i){
		if(this == name){
			f = true;
			return ;
		}
	});
	
	return f;
}
var ElementCallback = function(formWinIndex,ids,names){
	//校验重名
	if(formCheckName(names,$("#dxsElementName").val())){
		CommonUtils.notify("error", "有重复的参数名称，请重新填写参数名称", "4000");
		$("#dxsElementName").val("");
	}
	//拿到数据放到ztree的结构中
	if($('#dxsElementForm').validate().form()){
/*		if($("#dxsElementType").val()=='VARIATE'){
			$("#dxsElementValue").val('${'+$("#dxsElementValue").val()+'}');
		}*/
		var ElementNode;
		ElementNode = {	id:ids,
						name:$("#dxsElementName").val(),
						elementType:$("#dxsElementElementType").val(),
						maxOccurs:$("#dxsElementMaxOccurs").val(),
						minOccurs:$("#dxsElementMinOccurs").val(),
						required:$("#dxsElementRequired").val()
						/*value:$("#dxsElementValue").val(),
						type:$("#dxsElementType").val(),
						variableType:$("#dxsElementVariableType").val()*/
					  };
		top.layer.close(formWinIndex);
		return ElementNode;
	}
}


var initdxsElementForm = function(){
	
	var isUpdate = CommonUtils.getUrlParam("isUpdate");
	var arr = JSON.parse(CommonUtils.getUrlParam("arr"));
	if(isUpdate == "true"){
		//修改时把值放在指定位置
		FormUtils.fillFormByData("dxsElementForm", arr);
		/*if($("#dxsElementType").val()=='VARIATE'){
			var tsrc = $("#dxsElementValue").val();
			var src = tsrc.substr(2,tsrc.length-3);
			$("#dxsElementValue").val(src);
		}*/
	}else{
		FormUtils.fillFormByData("dxsElementForm", {id:''});
	}
}