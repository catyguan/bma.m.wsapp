package bma.m.wsapp.task;

import java.util.Map;

public class TaskUtil {

	public static void put(Map<String, String> r, String name, String v) {
		if (v != null && v.length() > 0) {
			r.put(name, v);
		}
	}

	public static String get(Map<String, String> props, String name, String def) {
		String r = props.get(name);
		if (r != null)
			return r;
		return def;
	}

}
