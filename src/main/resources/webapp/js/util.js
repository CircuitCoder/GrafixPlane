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

$(document).ready(init);