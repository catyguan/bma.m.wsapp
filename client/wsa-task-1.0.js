(function($) {
	var TaskManager = {
		serviceUrl : '/_wsa/task',
		_call : function(q) {
			return $.ajax(TaskManager.serviceUrl,
					{
						data : q,
						dataType : 'json',
						timeout : 1000,
					});
		},	
		list : function(type) {
			return TaskManager._call({'m':'list','type':type});
		},
		create : function(type,prop) {
			return TaskManager._call({'m':'create','type':type,'prop':JSON.stringify(prop)});
		},
		get : function(type,id) {
			return TaskManager._call({'m':'get','type':type,'id':id});
		},
		start : function(type,id) {
			return TaskManager._call({'m':'start','type':type,'id':id});
		},
		pause : function(type,id) {
			return TaskManager._call({'m':'pause','type':type,'id':id});
		},
		cancel : function(type,id) {
			return TaskManager._call({'m':'cancel','type':type,'id':id});
		},
		
	};
	$.TaskManager = TaskManager;
})(jQuery);