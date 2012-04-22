package bma.m.wsapp.plugin;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import android.util.Log;

public class WSAppPlugin {
	
	private final static String TAG = "jsWSApp";
	
	private static class SessionItem {
		public Object value;
		public long timeout;
	}
	private Map<String, SessionItem> session = Collections.synchronizedMap(new TreeMap<String, WSAppPlugin.SessionItem>());

	public void log(int type,String tag,String msg) {
		switch(type) {
		case 1:
			Log.i(tag, msg);
			break;
		case 2:
			Log.w(tag, msg);
			break;
		case 3:
			Log.e(tag, msg);
			break;
		default:
			Log.d(tag, msg);
			break;
		}
	}
	
	public void setSession(String key,String value,int timeout) {
		if(Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, key+"="+value+";"+timeout);
		}
		SessionItem item = session.get(key);
		if(item==null) {
			item = new SessionItem();
			session.put(key, item);
		}
		item.value = value;
		item.timeout = timeout>0?System.currentTimeMillis()+timeout:timeout;
	}
	
	public Object getSession(String key) {
		SessionItem item = session.get(key);
		if(item==null) {
			if(Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG, key+" not exists");
			}
			return null;
		}
		if(item.timeout>0) {
			if(System.currentTimeMillis()>=item.timeout) {
				session.remove(key);
				if(Log.isLoggable(TAG, Log.DEBUG)) {
					Log.d(TAG, key+" timeout");
				}
				return null;
			}
		}
		if(Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, key+" => "+item.value);
		}
		return item.value;
	}
	
	public void removeSession(String key) {
		if(Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, key+" delete");
		}
		session.remove(key);
	}
}
