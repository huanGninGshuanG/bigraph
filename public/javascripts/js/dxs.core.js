var FORM_TITLE_PRE = '<i class="fa fa-list-ul"></i>&nbsp;&nbsp;';
var version = Math.random();
var itemResponseData;

//全局ajax session超时监控
$(function(){  
    $.ajaxSetup({  
        contentType: "application/x-www-form-urlencoded;charset=utf-8",
        cache: false,
        complete: function(XHR, TS){
            var resText = XHR.responseText;
            if (jQuery.isFunction(XHR.getResponseHeader)) {
	            var sessionstatus = XHR.getResponseHeader("sessionstatus");
	            var loginPath = XHR.getResponseHeader("loginPath");
	            if (911 == XHR.status && "timeout" == sessionstatus) {
	            	top.layer.alert('您的会话已经过期，请重新登陆后继续操作！', {
	            		  closeBtn: 0
	            		}, function(){
	            			if (top.location != self.location){
		                        top.location = loginPath;
		                    }
            		});
//	                if(window.confirm('session过期', '您的会话已经过期，请重新登陆后继续操作！')) {
//	                	if (top.location != self.location){
//	                        top.location = loginPath;
//	                    }
//	                }
	                return;
	            } else if (912 == XHR.status) {
	            	alert("您没有权限进行此项操作！");
	            }
            }
        }
    });
});

var getWindowSize = function() {
	return [ "Height", "Width" ].map(function(a) {
		return window["inner" + a] || document.compatMode === "CSS1Compat"
				&& document.documentElement["client" + a]
				|| document.body["client" + a]
	})
};

// 扩展Date的format方法
Date.prototype.format = function(format) {
	var o = {
		"M+" : this.getMonth() + 1,
		"d+" : this.getDate(),
		"h+" : this.getHours(),
		"m+" : this.getMinutes(),
		"s+" : this.getSeconds(),
		"q+" : Math.floor((this.getMonth() + 3) / 3),
		"S" : this.getMilliseconds()
	}
	if (/(y+)/.test(format)) {
		format = format.replace(RegExp.$1, (this.getFullYear() + "")
				.substr(4 - RegExp.$1.length));
	}
	for ( var k in o) {
		if (new RegExp("(" + k + ")").test(format)) {
			format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k]
					: ("00" + o[k]).substr(("" + o[k]).length));
		}
	}
	return format;
}

/**
 * 转换日期对象为日期字符串
 * 
 * @param date
 *            日期对象
 * @param isFull
 *            是否为完整的日期数据, 为true时, 格式如"2000-03-05 01:05:04" 为false时, 格式如
 *            "2000-03-05"
 * @return 符合要求的日期字符串
 */
function getSmpFormatDate(date, isFull) {
	var pattern = "";
	if (isFull == true || isFull == undefined) {
		pattern = "yyyy-MM-dd hh:mm:ss";
	} else {
		pattern = "yyyy-MM-dd";
	}
	return getFormatDate(date, pattern);
}
/**
 * 转换当前日期对象为日期字符串
 * 
 * @param date
 *            日期对象
 * @param isFull
 *            是否为完整的日期数据, 为true时, 格式如"2000-03-05 01:05:04" 为false时, 格式如
 *            "2000-03-05"
 * @return 符合要求的日期字符串
 */
function getSmpFormatNowDate(isFull) {
	return getSmpFormatDate(new Date(), isFull);
}
/**
 * 转换long值为日期字符串
 * 
 * @param l
 *            long值
 * @param isFull
 *            是否为完整的日期数据, 为true时, 格式如"2000-03-05 01:05:04" 为false时, 格式如
 *            "2000-03-05"
 * @return 符合要求的日期字符串
 */
function getSmpFormatDateByLong(l, isFull) {
	return getSmpFormatDate(new Date(l), isFull);
}
/**
 * 转换long值为日期字符串
 * 
 * @param l
 *            long值
 * @param pattern
 *            格式字符串,例如：yyyy-MM-dd hh:mm:ss
 * @return 符合要求的日期字符串
 */
function getFormatDateByLong(l, pattern) {
	return getFormatDate(new Date(l), pattern);
}
/**
 * 转换日期对象为日期字符串
 * 
 * @param l
 *            long值
 * @param pattern
 *            格式字符串,例如：yyyy-MM-dd hh:mm:ss
 * @return 符合要求的日期字符串
 */
function getFormatDate(date, pattern) {
	if (date == undefined) {
		date = new Date();
	}
	if (pattern == undefined) {
		pattern = "yyyy-MM-dd hh:mm:ss";
	}
	return date.format(pattern);
}

var formSubmit = function(formId) {
	$("#" + formId).submit();
}

var CommonUtils = {
	getUrlParam : function(name) {
		var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); // 构造一个含有目标参数的正则表达式对象
		var r = window.location.search.substr(1).match(reg); // 匹配目标参数
		if (r != null)
			return decodeURIComponent(r[2]);
		return null; // 返回参数值
	},
	validateUrl : function(url) {
		if (!url) 
			return false;
		/*var strRegex = '^((https|http|ftp|rtsp|mms)?://)' 
			+ '?(([0-9a-z_!~*\'().&=+$%-]+: )?[0-9a-z_!~*\'().&=+$%-]+@)?' //ftp的user@ 
			+ '(([0-9]{1,3}.){3}[0-9]{1,3}' // IP形式的URL- 199.194.52.184 
			+ '|' // 允许IP和DOMAIN（域名） 
			+ '([0-9a-z_!~*\'()-]+.)*' // 域名- www. 
			+ '([0-9a-z][0-9a-z-]{0,61})?[0-9a-z].' // 二级域名 
			+ '[a-z]{2,6})' // first level domain- .com or .museum 
			+ '(:[0-9]{1,4})?' // 端口- :80 
			+ '((/?)|' // a slash isn't required if there is no file name 
			+ '(/[0-9a-z_!~*\'().;?:@&=+$,%#-]+)+/?)$';*/
		var check = "((http|ftp|https)://)(([a-zA-Z0-9\._-]+\.[a-zA-Z]{2,6})|([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\&%_\./-~-]*)?";
		var re = new RegExp(check);
		return re.test(url);
	},
	/**
	 * 对象转url参数()
	 * @example {name:'tom','class':{className:'class1'},classMates:[{name:'lily'}]}
	 * @param param
	 * @param key
	 * @param encode 
	 * @returns {String} url参数字符串
	 */
	urlEncode : function(param, key, encode) {
		if (!param) return '';
		var paramStr = "";
		var t = typeof (param);
		if (t == "string" || t == "number" || t == "boolean") {
			paramStr += '&' + key + '=' + ((encode==null||encode) ? encodeURIComponent(param) : param);
		} else {
			for (var i in param) {
				var k = key == null ? i : key + (param instanceof Array ? '[' + i + ']' : '.' + i);
				paramStr += CommonUtils.urlEncode(param[i], k, encode);
			}
		}
		return paramStr;
	},
	/**
	 * 通知方法 
	 * @param type 提示类型 {success, info, warning, error}
	 * @param msg 提示信息
	 * @param parent 通知在本页面还是顶级页面
	 */
	notify : function (type, msg, timeout) {
		top.toastr.options = {
				  "closeButton": false,
				  "debug": false,
				  "progressBar": false,
				  "positionClass": "toast-top-center",
				  "onclick": null,
				  "showDuration": "400",
				  "hideDuration": "1000",
				  "timeOut": timeout,
				  "extendedTimeOut": "1000",
				  "showEasing": "swing",
				  "hideEasing": "linear",
				  "showMethod": "fadeIn",
				  "hideMethod": "fadeOut"
		}
		top.toastr[type](msg);
	},
	/**
	 * 针对一个具体的URL，判断其是否能正常被响应
	 */
	openUrl	: function(url){
		 $.ajax({
			  url: url,
			  type: 'GET',
			  complete: function(response) {
			   if(response.status == 200) {
			   	   return true;
			   } else {
				   return false;
			   }
			  }
		});
	},
	/**
	 * 去空格
	 * @param str 需处理字符串
	 * @param is_global 是否去掉全部空格
	 */
	trim : function(str, is_global) {
		var result;
	    result = str.replace(/(^\s+)|(\s+$)/g,"");
	    if(is_global)
	    {
	        result = result.replace(/\s/g,"");
	     }
	    return result;
	},
	flowSize : function(size) {
		if (!isNaN(size)) {
			var t = Number(size);
			
			//如果字节数少于1024，则直接以B为单位，否则先除于1024，后3位因太少无意义  
			if (t < 1024) {
				return t + "B";
			} else {
				t = (t / 1024).toFixed(2);
			}
			//如果原字节数除于1024之后，少于1024，则可以直接以KB作为单位  
		    //因为还没有到达要使用另一个单位的时候  
		    //接下去以此类推  
			if (t < 1024) {
				return t + "KB";
			} else {
				t = (t / 1024).toFixed(2);
			}
			if (t < 1024) {  
		        //因为如果以MB为单位的话，要保留最后1位小数，  
		        //因此，把此数乘以100之后再取余  
		        t = t * 100;  
		        return (t / 100) + "."  
		                + (t % 100) + "MB";  
		    } else {  
		        //否则如果要以GB为单位的，先除于1024再作同样的处理  
		        t = t * 100 / 1024;  
		        return (t / 100) + "."  
		                + (t % 100) + "GB";  
		    }
		}
	}
}

var GridOptions = {
	
	clearCachePostData : function(gridId) {
		var obj = $("#" + gridId).jqGrid("getGridParam", "postData");
		$.each(obj, function(k, v){
			delete obj[k];
		});
	},
	updateItemFromGPGrid : function(gridId,updatebtn,openFunc){
		
		var s = $("#" + gridId).jqGrid('getGridParam','selarrrow');
		
		if(s == null || s == ""){
			layer.tips('您未选中需要修改的记录，请选择！', updatebtn, {
			  tips: [2, '#18a689'],
			  time: 2000
			});
		}else if(s.length != 1){
			
			layer.tips('您选中的记录数大于1，请选择一条记录进行修改！', updatebtn, {
				  tips: [2, '#18a689'],
				  time: 2000
			});
			
		} else{
			var func = eval(openFunc);
			new func(true,s[0]);
		}
	},
	deleteItemFromGPGrid : function(deleteUrl,gridId,updatebtn){
		
		var ids = $("#" + gridId).jqGrid('getGridParam','selarrrow');
		
		if(ids == null || ids == ""){
			layer.tips('您未选中需要删除的记录，请选择！', updatebtn, {
			  tips: [2, '#18a689'],
			  time: 2000
			});
		}else{
			var confirmId = layer.confirm('<small>您确定要执行此操作吗?</small>', {
				title : '<small>系统提示</small>',
				// skin: 'layui-layer-molv', //样式类名
				closeBtn : 0,
				icon:0,
				offset : '180px',
				// shift: 1, //提示框载入动画
				btn : [ '确定', '取消' ]
			// 按钮
			}, function() {
				layer.load(1, {
					shade : [ 0.2 ]
				// 透明度调整
				});
				
				$.ajax({
					url : deleteUrl,
					dataType : 'json',
					type : 'post',
					data : {
						"ids":ids,
					},
					success : function(data) {
						if (data.success) {
							CommonUtils.notify("success", "操作成功", 1500);
							$("#" + gridId).trigger("reloadGrid");
						} else {
							CommonUtils.notify("error", data.responseMessage, 4000);
						}
						layer.closeAll('loading');
						layer.close(confirmId);
					}
				});
			});
		}
	},
	deleteItemFromGPGridJudge : function(deleteUrl,gridId,updatebtn){
		
		var ids = $("#" + gridId).jqGrid('getGridParam','selarrrow');
		
		if(ids == null || ids == ""){
			layer.tips('您未选中需要删除的记录，请选择！', updatebtn, {
			  tips: [2, '#18a689'],
			  time: 2000
			});
		}else{
			for ( var i = 0; i < ids.length; i++) {
				var id = ids[i];
				var model = jQuery("#" + gridId).jqGrid('getRowData', id);
				//alert(model.status);
				if(model.status=="2"){
					layer.tips('已经发布的消息不能删除！', updatebtn, {
						  tips: [2, '#18a689'],
						  time: 2000
						});
					return;
				}
				
			}
			var confirmId = layer.confirm('<small>您确定要执行此操作吗?</small>', {
				title : '<small>系统提示</small>',
				// skin: 'layui-layer-molv', //样式类名
				closeBtn : 0,
				icon:0,
				offset : '180px',
				// shift: 1, //提示框载入动画
				btn : [ '确定', '取消' ]
			// 按钮
			}, function() {
				layer.load(1, {
					shade : [ 0.2 ]
				// 透明度调整
				});
				
				$.ajax({
					url : deleteUrl,
					dataType : 'json',
					type : 'post',
					data : {
						"ids":ids,
					},
					success : function(data) {
						if (data.success) {
							CommonUtils.notify("success", "操作成功", 1500);
							$("#" + gridId).trigger("reloadGrid");
						} else {
							CommonUtils.notify("error", data.responseMessage, 4000);
						}
						layer.closeAll('loading');
						layer.close(confirmId);
					}
				});
			});
		}
	},	
	deleteItemFromGPGrid4Single : function(deleteUrl,gridId,updatebtn, callback){
		
		var ids = $("#" + gridId).jqGrid('getGridParam','selarrrow');
		
		if(ids == null || ids == ""){
			layer.tips('您未选中需要删除的记录，请选择！', updatebtn, {
			  tips: [2, '#18a689'],
			  time: 2000
			});
		} else if (ids.length > 1) {
			layer.tips('删除时只能选择单条数据进行操作！', updatebtn, {
				  tips: [2, '#18a689'],
				  time: 2000
				});
		} else {
			var confirmId = layer.confirm('<small>您确定要执行此操作吗?</small>', {
				title : '<small>系统提示</small>',
				closeBtn : 0,
				icon:0,
				offset : '180px',
				btn : [ '确定', '取消' ]
			}, function() {
				layer.load(1, {
					shade : [ 0.2 ]
				});
				
				$.ajax({
					url : deleteUrl,
					dataType : 'json',
					type : 'post',
					data : {
						"ids":ids[0],
					},
					success : function(data) {
						if (data.success) {
							CommonUtils.notify("success", "操作成功", 1500);
							if (typeof callback == "function")
								new callback();
							$("#" + gridId).trigger("reloadGrid");
						} else {
							CommonUtils.notify("error", data.responseMessage, 4000);
						}
						layer.closeAll('loading');
						layer.close(confirmId);
					}
				});
			});
		}
	},
	loadItemFromGPGrid : function(entityId, queryURL, itemDetailUrl,gridId,formId) {

		if (entityId != undefined && entityId != "undefined") {
			layer.load(1, {
				shade : [ 0.2 ]
			// 透明度调整
			});
			$.ajax({
						url : queryURL,
						dataType : 'json',
						type : 'post',
						data : 'entityId=' + entityId,
						success : function(data) {

							if (data.success) {
								itemResponseData = data.responseData;
								GridOptions.openItemFromGPGrid(true, gridId,itemDetailUrl,formId);
							} else {

								parent.layer.msg('操作失败：<br>'
										+ data.responseMessage, {
									icon : 2,
									offset : '180px'
								});
							}
							layer.closeAll('loading');
						}
					});
		}
	},
	getItemResponseData : function() {
		return itemResponseData;
	},
	openItemFromGPGrid : function(isLoad, jpGridId, itemDetailUrl, formId) {

		var iframeWin;
		var isRefresh = false;
		// iframe窗
		layer.open({
			type : 2,
			title : ' ',
			closeBtn : 1, // 不显示关闭按钮
			shadeClose : false,
			shade : false,
			maxmin : true, // 开启最大化最小化按钮
			offset : '80px',
			area : [ '800px', '640px' ],
			shade : [ 0.3 ],
			content : itemDetailUrl + '?isLoad=' + isLoad + '&v='
					+ Math.random(),
			btn : [ '保存', '取消' ],
			yes : function(layero, index) {
				isRefresh = true;
				iframeWin.FormUtils.submit(formId);
			},
			btn2: function(index, layero){
			    //按钮【按钮二】的回调
				isRefresh = false;
			},
			cancel: function(){ 
			    //右上角关闭回调
				isRefresh = false;
			},
			success : function(layero, index) {
				iframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：iframeWin.method()
			},
			end : function(index) {
				layer.closeAll('loading');
				if(isRefresh)
					$("#" + jpGridId).trigger("reloadGrid");
			}
		});
	},
	reload : function(gridId){
		$('#' + gridId).trigger('reloadGrid');
	},
	gridSearch : function(jpGridId, formId) {
//		var form = $("#"+formId);
//		var array = form.serializeArray();
//		console.log(array);
		var postData = GridOptions.serializeJson(formId);
		$("#"+jpGridId).jqGrid("setGridParam",{postData:postData}).trigger("reloadGrid");
	},
	serializeJson : function(formId) {
		var serializeObj = {};
		var form;
		if (formId instanceof jQuery) {
			form = formId;
		} else {
			form = $("#"+formId);
		}
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
	}
};

var dtOptions = {
		deleteItemFromGPGrid : function(deleteUrl, gridId, updatebtn) {
			var ids = $("#" + gridId).DataTable().fnGetSelectedIds();
			if(ids == null || ids == ""){
				layer.tips('您未选中需要删除的记录，请选择！', updatebtn, {
				  tips: [2, '#18a689'],
				  time: 2000
				});
			}else{
				var confirmId = layer.confirm('<small>您确定要执行此操作吗?</small>', {
					title : '<small>系统提示</small>',
					closeBtn : 0,
					icon:0,
					offset : '180px',
					btn : [ '确定', '取消' ]
				}, function() {
					layer.load(1, {
						shade : [ 0.2 ]
					});
					
					$.ajax({
						url : deleteUrl,
						dataType : 'json',
						type : 'post',
						data : JSON.stringify({
							"ids":ids,
						}),
						contentType: "application/json;charset=utf-8",
						success : function(data) {
							if (data.success) {
								CommonUtils.notify("success", "操作成功", 1500);
								$("#" + gridId).DataTable().fnSearch(false);
							} else {
								CommonUtils.notify("error", data.responseMessage, 4000);
							}
							layer.closeAll('loading');
							layer.close(confirmId);
						}
					});
				});
			}
		},
		deleteItemFromGPGrid4Single : function(deleteUrl, gridId, updatebtn, callback){
			var ids = $("#" + gridId).DataTable().fnGetSelectedIds();
			
			if(ids == null || ids == ""){
				layer.tips('您未选中需要删除的记录，请选择！', updatebtn, {
				  tips: [2, '#18a689'],
				  time: 2000
				});
			} else if (ids.length > 1) {
				layer.tips('删除时只能选择单条数据进行操作！', updatebtn, {
					  tips: [2, '#18a689'],
					  time: 2000
					});
			} else {
				var confirmId = layer.confirm('<small>您确定要执行此操作吗?</small>', {
					title : '<small>系统提示</small>',
					closeBtn : 0,
					icon:0,
					offset : '180px',
					btn : [ '确定', '取消' ]
				}, function() {
					layer.load(1, {
						shade : [ 0.2 ]
					});
					
					$.ajax({
						url : deleteUrl,
						dataType : 'json',
						type : 'post',
						data : {
							"ids":ids[0],
						},
						success : function(data) {
							if (data.success) {
								CommonUtils.notify("success", "操作成功", 1500);
								if (typeof callback == "function")
									new callback();
								$("#" + gridId).DataTable().fnSearch(false);
							} else {
								CommonUtils.notify("error", data.responseMessage, 4000);
							}
							layer.closeAll('loading');
							layer.close(confirmId);
						}
					});
				});
			}
		},
		reload : function (gridId) {
			$('#' + gridId).DataTable().draw(false);
		},
		delete4Self : function (success) {
			var confirmId = layer.confirm('<small>您确定要执行此操作吗?</small>', {
				title : '<small>系统提示</small>',
				closeBtn : 0,
				icon:0,
				offset : '180px',
				btn : [ '确定', '取消' ]
			}, function(index){
				success(index);
			});
		},
		lookup : function(entityId, itemDetailUrl,lookupName,w,h) {
			top.layer.open({
				type : 2,
				title : FORM_TITLE_PRE + lookupName,
				closeBtn : 1, // 不显示关闭按钮
				shadeClose : false,
				shade : false,
				scrollbar: false,
				maxmin : true, // 开启最大化最小化按钮
				area : [ ''+w+'%', ''+h+'%' ],
				shade : [ 0.3 ],
				content : itemDetailUrl + "?isUpdate=true&lookup=true&entityId=" + entityId + '&v=' + version,
				btn : [ '关闭' ],
				yes : function(index,layero) {
					top.layer.close(index);
				},
				cancel: function(){ 
				    //右上角关闭回调
					DxsAppIsRefresh = false;
				},
				success : function(layero, index) {
					DxsAppIframeWin = window[layero.find('iframe')[0]['name']]; // 得到iframe页的窗口对象，执行iframe页的方法：DxsAppIframeWin.method()
				},
				end : function(index) {
					top.layer.closeAll('loading');
				}
			});
		}
}

