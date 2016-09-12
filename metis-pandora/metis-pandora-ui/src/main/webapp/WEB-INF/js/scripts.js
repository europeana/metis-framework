$(document).ready(function() {
	$('.mapping-widget-expanded').hide();
	$('.dropdown').hide();
	
	$('.mapping-widget-values td').click(function () {
		$('.mapping-widget-values td').removeClass('selected');
		$(this).addClass('selected');
	});

	$('.values-expand').click(function() {
		$('.mapping-widget-expanded').slideToggle('1000');
		$('.mapping-widget-collapsed').hide();
	});
	
	$('.values-collapse').click(function() {
		$('.mapping-widget-expanded').hide();
		$('.mapping-widget-collapsed').show();
	});
	
	$('.pagination li').click(function() {
		$('.pagination li').removeClass('is-current');
		
		$(this).addClass('is-current');
	});

	$('.item-suspicious').click(function () {
		$('#test-item').removeClass('erroneous');
		$('#test-item').addClass('suspicious');
		
		$('.test-item-colored').removeClass('normal-colored');
		$('.test-item-colored').removeClass('erroneous-colored');
		$('.test-item-colored').addClass('suspicious-colored');
		
		$('.test-item-colored-dark').removeClass('normal-colored-dark');
		$('.test-item-colored-dark').removeClass('erroneous-colored-dark');
		$('.test-item-colored-dark').addClass('suspicious-colored-dark');
		
		$('.dropdown').hide();
	});
	
	$('.item-erroneous').click(function () {
		$('#test-item').removeClass('suspicious');
		$('#test-item').addClass('erroneous');
		
		$('.test-item-colored').removeClass('normal-colored');
		$('.test-item-colored').removeClass('suspicious-colored');
		$('.test-item-colored').addClass('erroneous-colored');
		
		$('.test-item-colored-dark').removeClass('normal-colored-dark');
		$('.test-item-colored-dark').removeClass('suspicious-colored-dark');
		$('.test-item-colored-dark').addClass('erroneous-colored-dark');
		
		$('.dropdown').hide();
	});
	
	$('.item-normal').click(function () {
		$('#test-item').removeClass('suspicious');
		$('#test-item').removeClass('erroneous');
		
		$('.test-item-colored').removeClass('suspicious-colored');
		$('.test-item-colored').removeClass('erroneous-colored');
		$('.test-item-colored').addClass('normal-colored');
		
		$('.test-item-colored-dark').removeClass('suspicious-colored-dark');
		$('.test-item-colored-dark').removeClass('erroneous-colored-dark');
		$('.test-item-colored-dark').addClass('normal-colored-dark');
		
		$('.dropdown').hide();
	});
	
	$(window).click(function() {
		$('[id^=object_furtheractions]').hide();
	});
	
	$('.dropdown-trigger').click(function (event) {
	    event.stopPropagation();
	});
});

function dropDownMenu(id) {
	if ($(id).is(':hidden')) {
		$('[id^=object_furtheractions]').hide();
		$(id).show();			
	} else {
		$('[id^=object_furtheractions]').hide();
//		$(id).hide();
	}
}
