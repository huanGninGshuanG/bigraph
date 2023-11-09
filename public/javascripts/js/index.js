// JavaScript Document
$(function(){
	var Lh=$(".leftmenu").height();
	var Mh=$(".midmenu").height();
	var Rh=$(".main").height();
	var divH=[Lh,Mh,Rh];
	divH.sort(function(a,b){
		return a-b;
	});
	$(".leftmenu").height(divH[divH.length-1]);
	$(".midmenu").height(divH[divH.length-1]);
	$(".main").height(divH[divH.length-1]);
	
	//菜单收缩
	$("#dir").click(function(){
		if($(this).hasClass("sidebar")){
			$(this).removeClass("sidebar");
			$("#leftmenu").removeClass("sidemenu");
			$(".ltitle span:first").hide().siblings().show();
			$(".leftnav li a").show();
		}else{
			$(this).addClass("sidebar");
			$("#leftmenu").addClass("sidemenu");
			$(".ltitle span:first").show().siblings().hide();
			
		}
	});
	
	$(".middir").click(function(){
		if($(this).hasClass("midon")){
			$(".midmenu").removeClass("middeply").children().show();
			$(this).removeClass("midon");
			
		}else{
			$(".midmenu").addClass("middeply").children().not("span").hide();
			$(this).addClass("midon");
		}
	});


	$(".dropdown-icon").click(function(){
		$(".dropdown-list").toggle();
	})
	$(".dropdown-list li").click(function(){
		var txt=$(this).text();
		$(".dropdown-input").text(txt);
		$(".dropdown-list").hide();

	})
})