/**
*	DXS页面公共组件函数库
*/

/**
 * ons配置选择
 */
var ons_cc_select = function (callback) {
	var requestFrame = top.window[window.name];//取得上层页面的window对象
	top.layer.open({
		type : 2,
		title : 'ONS配置选择',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : false, // 开启最大化最小化按钮
		shade : [ 0.3 ],
		area : [ "65%", "50%" ],
		shadeClose : false,
		content : "pages/common/ons_consumer_config_select.html",
		btn : ['确定', '取消'],
		yes : function (index, layero) {
			var iframe = layero.find('iframe')[0].contentWindow;
			var obj = iframe.choose();//调用打开页面内函数
			if (typeof callback == "function")
				callback(obj);//上层页面回调
			top.layer.close(index);
		}
		
	});
}

var amq_cc_select = function (callback) {
	var requestFrame = top.window[window.name];//取得上层页面的window对象
	top.layer.open({
		type : 2,
		title : 'AMQ配置选择',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : false, // 开启最大化最小化按钮
		shade : [ 0.3 ],
		area : [ "65%", "50%" ],
		shadeClose : false,
		content : "pages/common/amq_consumer_config_select.html",
		btn : ['确定', '取消'],
		yes : function (index, layero) {
			var iframe = layero.find('iframe')[0].contentWindow;
			var obj = iframe.choose();//调用打开页面内函数
			if (typeof callback == "function")
				callback(obj);//上层页面回调
			top.layer.close(index);
		}
		
	});
}

/**
 * 应用集成组件选择
 */
var ac_select = function (callback) {
	var requestFrame = top.window[window.name];//取得上层页面的window对象
	top.layer.open({
		type : 2,
		title : '集成组件选择',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : false, // 开启最大化最小化按钮
		shade : [ 0.3 ],
		area : [ "65%", "50%" ],
		shadeClose : false,
		content : "pages/common/aip_component_select.html",
		btn : ['确定', '取消'],
		yes : function (index, layero) {
			var iframe = layero.find('iframe')[0].contentWindow;
			var obj = iframe.choose();//调用打开页面内函数
			if (typeof callback == "function")
				callback(obj);//上层页面回调
			top.layer.close(index);
		}
		
	});
}

var app_select = function (callback) {
	var requestFrame = top.window[window.name];//取得上层页面的window对象
	top.layer.open({
		type : 2,
		title : '目标应用选择',
		closeBtn : 1, // 不显示关闭按钮
		shadeClose : false,
		shade : false,
		scrollbar: false,
		maxmin : false, // 开启最大化最小化按钮
		shade : [ 0.3 ],
		area : [ "65%", "60%" ],
		shadeClose : false,
		content : "pages/common/app_select.html",
		btn : ['确定', '取消'],
		yes : function (index, layero) {
			var iframe = layero.find('iframe')[0].contentWindow;
			var obj = iframe.choose();//调用打开页面内函数
			if (typeof callback == "function")
				callback(obj);//上层页面回调
			top.layer.close(index);
		}
	});
}
