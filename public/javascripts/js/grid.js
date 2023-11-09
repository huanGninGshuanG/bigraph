/**
 * 将表格转换为 dataTables 控件
 */
$(document).ready(function() {
	$('table[data-view=dataTables]').each(function(index, node)
	{
		if (!$.fn.DataTable.isDataTable (this))
		{//防止重复初始化 dataTables
			$(this).wrap ('<div class="row"><div class="col-sm-12"></div></div>');
			$(this).vTable();
		}
	});
});