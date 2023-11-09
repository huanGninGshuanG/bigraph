
$.fn.dataTable.ext.errMode = function(s,h,m){}//取消datatable加载错误弹出提示

$.fn.dataTable.Api.register( 'fnSearch()', function(reset) {
	var that = this;
	that.draw(reset);
});

$.fn.dataTable.Api.register( 'fnGetRows()', function() {
	var table = this;
	var rows = [];
	$(table.rows().nodes()).each(function(){
		rows.push(table.row(this).data());
	})
	return rows;
})

//extends datatables to delay typing search
$.fn.dataTable.Api.register( 'fnSetFilteringDelay()', function(oSettings, iDelay) {
	var _that = this;

	if (iDelay === undefined) {
		iDelay = 250;
	}

	this.each(function(i) {
		$.fn.dataTableExt.iApiIndex = i;
		// var $this = this,
		var oTimerId = null, sPreviousSearch = null, anControl = $('input', _that.fnSettings().aanFeatures.f);

		anControl.unbind('keyup').bind(
				'keyup',
				function() {
					// var $$this = $this;

					if ((anControl.val().length == 0 || anControl.val().length >= 2)
							&& (sPreviousSearch === null || sPreviousSearch != anControl.val())) {
						window.clearTimeout(oTimerId);
						sPreviousSearch = anControl.val();
						oTimerId = window.setTimeout(function() {
							$.fn.dataTableExt.iApiIndex = i;
							_that.fnFilter(anControl.val());
						}, iDelay);
					}
				});

		return this;
	});
	return this;
});

$.fn.dataTable.Api.register( 'fnGetSelected()', function() {
	var table = this;
	return table.rows(':has(:checkbox:checked)').data();
});

$.fn.dataTable.Api.register( 'fnGetRowById()', function(id) {
	var table = this;
	return table.row('[id='+id+']').data();
});

$.fn.dataTable.Api.register( 'fnGetSelectedRows()', function() {
	var table = this;
	var rows = [];
	$('input.selector', table.fnGetNodes()).each(function(){
		if (this.checked) {
			var tr = $(this).closest('tr')[0];
			rows.push(tr);
		}
	});
	return rows;
});

$.fn.dataTable.Api.register( 'fnGetSelectedIds()', function() {
	var table = this;
	var ids = [];
	$('input.selector', table.rows().nodes()).each(function(){
		if (this.checked) {
			ids.push(this.value);
		}
	});
	return ids;
});

$.fn.vTable = function() {
	if ($.fn.DataTable.isDataTable( this )) {
		return $(this).DataTable();
	}
	
	function parseFuction(funcname) {
		var func = window [funcname];
		if (typeof func != 'function')
			console.error (funcname + ' 不是一个函数');
		return func;
	}
	
	function transform(obj){
	    var arr = [];
	    for(var item in obj){
	        arr.push(obj[item]);
	    }
	    return arr;
	}
	
	function serializeForm(formId) {
		var serializeObj = {};
		var form;
		if (formId instanceof jQuery) {
			form = formId;
		} else {
			form = $(formId);
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
	
	/*
	 * 将字符串中的变量 (%n) 替换为对象中的实际属性值 str - 源字符串 (例："<span>这是第 %1 条记录</span>")
	 * attrs - 对象属性，逗号分隔（例："id,name"，%1、％2 将替换为 data.id, data.name) data - 数据对象
	 */
	function replaceToDeath(str, attrs, data) {
		var props = attrs.split(/\s?,\s?/);
		if (!props || props.length < 1)
			return;
		for ( var i = 0, prop; prop = props[i]; i++) {
			var obj = data, val = null;
			var atts = prop.split('.');
			for ( var j = 0, p; p = atts[j]; j++) {
				if (obj[p])
					obj = obj[p];
				val = obj;
			}
			str = str.replace(new RegExp('%' + (i + 1), 'g'), val);
		}
		return str;
	}
	
	var jqnode = $(this);
	var cols = [];
	var colDefs = [];
	// 是否显示分页
	var paginate = 'false' != jqnode.attr('data-paginate');
	var pagingsize = parseInt(jqnode.attr('data-pagesize'));
	var remote = 'false' != jqnode.attr('data-remote');
	var loaded = 'auto' == jqnode.attr('data-load');
	var rowId = jqnode.attr('data-rowId') ? jqnode.attr('data-rowId') : 'id';
	var selectname = 'table' + (this.id ? this.id : this.offsetLeft + '_' + this.offsetTop);
	
	jqnode.find('th[data-column]').each(function(index, node) {
		var col = $(this);
		var sortable = ($(node).attr('data-sorable') == 'true');
		var visible = !(col.attr('data-visible') == 'false');
		
		var colomu = {
				defaultContent : '',
				visible : visible,
				data : col.attr('data-column'),
				className : col.attr('data-className')
		};
		var coldef = {
				'targets' : [ index ],
				'visible' : visible,
				'data' : col.attr('data-column'),
				'className' : col.attr('data-className')
		};
		
		var tdWidth = col.attr('data-width');
		if (tdWidth)
			coldef['width'] = tdWidth;
		
		if (col.attr('data-ref') == 'handle') { //操作列， 表头内容会复制到每一行的对应列中
			var template = col.html();
			colomu['orderable'] = sortable;
			colomu['orderData'] = col.attr('data-sort');
			colomu['render'] = function (data, type, full) {
				return replaceToDeath(template, col.attr('data-column'), full);
			};
			coldef['orderable'] = sortable;
			coldef['orderData'] = col.attr('data-sort');
			coldef['render'] = function (data, type, full) {
				return replaceToDeath(template, col.attr('data-column'), full);
			};
			
			col.html(col.attr('data-name'));
		} else if (col.attr('data-ref') == 'renderer') {
			colomu['orderable'] = sortable;
			colomu['orderData'] = col.attr('data-sort');
			colomu['render'] = function (data, type, full) {
				var func = parseFuction(col.attr('data-renderer'));
				return func(data, type, full);
			}
			coldef['orderable'] = sortable;
			coldef['orderData'] = col.attr('data-sort');
			coldef['render'] = function (data, type, full) {
				var func = parseFuction(col.attr('data-renderer'));
				return func(data, type, full);
			}
		} else {
			colomu['orderable'] = sortable;
			colomu['orderData'] = col.attr('data-sort');
			coldef['orderable'] = sortable;
			coldef['orderData'] = col.attr('data-sort');
		}
		colDefs.push(coldef);
		cols.push(colomu);
	});
	
	var form = $(jqnode.attr('data-search'));
	var pageinfo = "<'row'<'span6'i><'span6'p>>";
	var onload = jqnode.attr('data-onload');
	if (!paginate)
		pageinfo = "";
	
	var options = {
		'order' : [],
		'autoWidth' : false,
		'scrollCollapse' : true,
		'responsive' : true,
		'paging' : paginate,
		'lengthChange' : false,
		'retrieve' : true,
		'searching' : false,
		'processing' : true,
		'serverSide' : remote,
		'pageLength' : pagingsize,
		'pagingType' : 'full_numbers',
		'rowId' : rowId,
		'rowCallback' : function(row, data, index) {
			var rcallback = jqnode.data('rowCallback');
			if (typeof rcallback === 'function') {
				rcallback(row, data, index);
			}
		},
		//'columns' : cols,
		'columnDefs' : colDefs,
		'initComplete' : function (settings, json) {
			var loadfunc = onload ? parseFuction(onload) : null;
			if (typeof loaded == 'function')
				loadfunc(settings, json);
			loaded = true;
		},
		'ajax' : function(data, fnCallback, settings) {
			if (!loaded) {
				fnCallback({
					data:[],
					recordsTotal : 0,
					recordsFiltered : 0,
				});
				return;
			}
			//添加自定义参数到请求参数中
			data = $.extend(true, data, serializeForm(jqnode.attr('data-search')));
			$.ajax({
				url : jqnode.attr('data-url'),
				type : 'POST',
				data : data,
				success : function (result) {
					result = JSON.stringify(result);
					var obj = $.parseJSON(result);
					if (obj.data)
						fnCallback(obj);
					else {
						var json = {
								draw : obj.responseData.draw,
								recordsTotal : obj.responseData.totalRecordNums,
								recordsFiltered : obj.responseData.totalRecordNums,
								data : obj.responseData.datas
						};
						fnCallback(json);
					}
					var funcname = jqnode.attr('data-callback');
					if (funcname) {
						var func = parseFuction(funcname);
						if (typeof func == 'function')
							func(obj);
					}
				},
				error : function(result) {
					console.log(result);
					fnCallback({
						data : []
					});
				}
			});
		}
	};
	
	var oTable = jqnode.DataTable(options);
	oTable.fnSetFilteringDelay();
	form.on('submit', function() {
		oTable.fnSearch();
		return false;
	});
	return oTable;
}

/**
 * $('#example').DataTable().row(':has(:checkbox:checked)').data();
 * 
 * 
 * 
 * 
 */
