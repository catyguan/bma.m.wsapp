(function($) {
	var TaskManager = {
		serviceUrl : '/_wsa/task',
		list : function(type) {
			return $.get(serviceUrl,{'m':'list','type':type});
		},
		create : function(type,prop) {
			return $.get(serviceUrl,{'m':'create','type':type,'prop':JSON.stringify(prop)});
		},
		get : function(type,id) {
			return $.get(serviceUrl,{'m':'get','type':type,'id':id});
		},
		start : function(type,id) {
			return $.get(serviceUrl,{'m':'start','type':type,'id':id});
		},
		pause : function(type,id) {
			return $.get(serviceUrl,{'m':'pause','type':type,'id':id});
		},
		cancel : function(type,id) {
			return $.get(serviceUrl,{'m':'cancel','type':type,'id':id});
		},
		
	};
	$.TaskManager = TaskManager;
})(jQuery);