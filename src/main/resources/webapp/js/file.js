$(document).ready(function() {
	$('#upload').fileupload({
		dataType: 'json',
		url: "/file",
		formData: [
		           {
		        	   name: 'dir',
		        	   value: _DIR
		           }
		],
        add: function (e, data) {
        	$.each(data.files, function (index, file) {
        		data.context = $('<div>')
        			.appendTo("#uploadSec")
        			.html(file.name)
        			.addClass('uploadRow')
        			.click(function () {
        				$(this).html("Uploading...");
        				$(this).addClass("uploading");
        				$(this).unbind("click");
        				data.submit();
        			});
        	});
        },
		done: function (e, data) {
			$.each(data.files, function (index, file) {
				data.context.html('Finished');
				data.context.addClass("finishUpload");
				$(this).removeClass("uploading");
				data.context.delay(500).slideUp(function() {
					$(this).remove();
				});
			});
		}
	});
});