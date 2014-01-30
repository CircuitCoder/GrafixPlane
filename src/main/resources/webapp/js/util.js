function init() {
	$(':input.empty_input').each(function(index) {
		$(this).val($(this).attr("empty_value"));
		$(this).focusin(function() {
			if($(this).hasClass("empty_input")) {
				$(this).removeClass("empty_input");
				$(this).val("");
			}
		})
		$(this).focusout(function() {
			if($(this).val()=="") {
				$(this).addClass("empty_input");
				$(this).val($(this).attr("empty_value"));
			}
		})
	})
}

function checkEmpty() {
	var result=true;
	$(':input.non_empty').each(function() {
		if(this.value==""||$(this).hasClass("empty_input")) {
			result=false;
			$(this).css("transition-property","background-color,box-shadow,border-color,color")
			$(this).css("border-color","#C66");
			$(this).css("color","#FAA");
			$(this).css("background-color","#FEE");
			$(this).focusin(function() {
				$(this).css("border-color","");
				$(this).css("color", "");
				$(this).css("background-color","");
				$(this).css("transition-property","background-color,box-shadow,border-color")
			})
		}
	})
	return result;
}

$(document).ready(init);