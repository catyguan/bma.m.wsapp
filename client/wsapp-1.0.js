(function($) {
	var WSApp = {
		DEBUG : true,
		_SESSION : {},
		_LISTEN : {},
		_LISTEN_ID : 0,
		
		debug : function(tag,msg) {
			if(window.WSApp) {
				window.WSApp.log(0,tag,msg);
			} else if(console && console.log) {
				console.log(tag+": "+msg);
			}
		},
		info : function(tag,msg) {
			if(window.WSApp) {
				window.WSApp.log(1,tag,msg);
			} else if(console && console.log) {
				console.log(tag+": "+msg);
			}
		},
		warn : function(tag,msg) {
			if(window.WSApp) {
				window.WSApp.log(2,tag,msg);
			} else if(console && console.log) {
				console.log(tag+": "+msg);
			}
		},
		error : function(tag,msg) {
			if(window.WSApp) {
				window.WSApp.log(3,tag,msg);
			} else if(console && console.log) {
				console.log(tag+": "+msg);
			}
		},
		put : function(key,value,timeout) {
			if(window.WSApp) {
				value = JSON.stringify(value);
				window.WSApp.setSession(key,value,timeout);
			} else {
				var tm = timeout>0?new Date().getTime()+timeout:0;
				window.$$._SESSION[key] = {"value":value,"timeout":tm};
			}
		},
		get : function(key,def) {
			if(window.WSApp) {
				var v = window.WSApp.getSession(key);
				if(v!=null) {
					v = JSON.parse(v);
				}
				return v?v:def;
			} else {
				var v = window.$$._SESSION[key];
				if(v) {
					if(v.timeout>0) {
						if(new Date().getTime()>=v.timeout) {
							delete window.$$._SESSION[key];
							return def;
						}
					}
					return v.value;
				}
				return def;
			}
		},
		remove : function(key) {
			if(window.WSApp) {
				window.WSApp.removeSession(key);
			} else {
				delete 	window.$$._SESSION[key];
			}
		},
		listen : function(id,fun,keep) {
			var v = {"f":fun,"o":!keep,"i":++window.$$._LISTEN_ID};
			var a = window.$$._LISTEN[id];
			if(a==null) {
				a = [v];
				window.$$._LISTEN[id] = a;
			} else {
				a[a.length] = v;
			}
			return v.i;
		},
		unlisten : function(id,gid) {
			var a = window.$$._LISTEN[id];
			if(typeof gid == "boolean") {
				if(gid) {
					delete window.$$._LISTEN[id];
				}
			} else {
				if(a!=null) {
					for(var i=0;i<a.length;i++) {
						if(a[i].i==gid) {
							a.splice(i,1);
						}
					}
				}
			}
		},
		fire : function(id,ctx) {
			var a = window.$$._LISTEN[id];
			if(a!=null) {
				for(var i=0;i<a.length;i++) {
					try {
						var v = a[i];
						if(v.f)a[i].f(ctx);
						if(v.o) {
							a.splice(i,1);
							i--;
						}
					} catch(err) {
						window.$$.debug("fire("+id+","+ctx+") fail:"+err);
					}
				}
				if(a.length==0) {
					delete window.$$._LISTEN[id];
				}
			}
		},
		
	};
	window.$$ = WSApp;
})(jQuery);