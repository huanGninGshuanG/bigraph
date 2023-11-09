/**
 * 加载选择框控件 参数： data-url: 数据源 url（返回标准AjaxResult {response: [{*},{*}]}）
 * data-name: option 名称对应数据源属性 data-value: option 值对应数据源属性 data-autoselect:
 * 是否默认选中第一项 data-parent: 是否存在父子级联 data-parent-name: 如果存在父子级联，指定父控件值在子控件查询中的参数名称
 */
$(function() {
	function loadSelect(node, url, params) {
		var jqSelect = $(node);
		jqSelect.data('loaded', true);
		jqSelect.val('');
		jqSelect.html('');
		var autoselect = jqSelect.attr('data-autoselect');
		var name = jqSelect.attr('data-name');
		var value = jqSelect.attr('data-value');
		$.ajax({
			url : url,
			data : params,
			type : 'POST',
			dataType : 'JSON',
			success : function(result) {
				if (result.responseCode != 200 || !result.responseData)
					return;
				var data = result.responseData;
				var html = [];
				html.push('<option value=""></option>');
				var firstval = '';
				for ( var i = 0, d; d = data[i]; i++) {
					if (i == 0)
						firstval = d[value];
					html.push('<option value="' + d[value] + '">' + d[name] + '</option>');
				}
				var selectValue = jqSelect.data('select') ? jqSelect.data('select') : jqSelect.val();
				jqSelect.html(html.join(''));
				if (!selectValue && 'true' == autoselect) {
					jqSelect.val(firstval);
					jqSelect.change();
				} else if (selectValue) {
					jqSelect.val(selectValue);
					jqSelect.change();
				}
				jqSelect.data('model', data);
			},
			error : function() {
				console.err('error loading data for select: ', node.name);
			}
		});
	}
	$('select[data-url]').each(function() {
		var that = this;
		var url = $(this).attr('data-url');
		if (url == null || url == '')
			return;
		if ($(that).data('loaded'))
			return;
		var parent = $(this).attr('data-parent');
		var parentName = $(this).attr('data-parent-name');
		var params = {};
		if (parent && $(parent).length > 0) {
			var pselect = $(parent);
			pselect.off();
			pselect.change(function() {
				if (pselect.val() != '') {
					params[parentName] = pselect.val();
					loadSelect(that, url, params);
				}
			});
			if (pselect.val() === null || pselect.val() == '') {
				console.log(this.name, 'child was loaded before parent ;)');
				return;
			}
		}
		loadSelect(this, url);
	});
});