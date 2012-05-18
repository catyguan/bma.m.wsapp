package bma.m.wsapp.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;
import bma.m.wsapp.httpserver.HttpExchange;
import bma.m.wsapp.httpserver.HttpHandler;
import bma.m.wsapp.util.HttpExchangeUtil;

public class TaskManagerHttpHandler implements HttpHandler {

	public static final String URI = "/_wsa/task";
	public static final String TAG = "_wsa.task";

	public TaskManagerHttpHandler() {
		super();
	}

	public void handle(HttpExchange exchange) throws IOException {
		Map<String, Object> resp = new TreeMap<String, Object>();
		try {
			Map<String, String> params = HttpExchangeUtil
					.parseQueryString(exchange);
			String method = params.get("m");
			if (method == null || method.length() == 0) {
				method = "list";
			}
			if ("list".equals(method)) {
				String type = params.get("type");
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.w(TAG, "list('" + type + "')");
				}
				List<Task> list = TaskManager.getInstance().listTasks(type);
				List<Map<String, String>> rlist = new ArrayList<Map<String, String>>(
						list.size());
				for (Task task : list) {
					rlist.add(task.getProperties());
				}
				resp.put("status", "ok");
				resp.put("data", rlist);
			} else if ("get".equals(method)) {
				String type = params.get("type");
				String id = params.get("id");
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.w(TAG, "get('" + type + "," + id + "')");
				}
				Task task = TaskManager.getInstance().getTask(type, id);
				resp.put("status", "ok");
				if (task != null) {
					resp.put("data", task.getProperties());
				}
			} else if ("create".equals(method)) {
				String type = params.get("type");
				String prop = params.get("prop");
				Map<String, String> props;
				if (prop != null && prop.length() > 0) {
					JSONObject o = (JSONObject) new JSONTokener(prop)
							.nextValue();
					JSONArray nlist = o.names();
					props = new HashMap<String, String>(nlist.length());
					for (int i = 0; i < nlist.length(); i++) {
						String n = nlist.getString(i);
						Object v = o.get(n);
						if (v != null) {
							props.put(n, v.toString());
						}
					}
				} else {
					props = Collections.emptyMap();
				}
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.w(TAG, "create('" + type + "')");
				}
				Task task = TaskManager.getInstance().createTask(type, props);
				if (task != null) {
					resp.put("status", "ok");
					resp.put("data", task.getId());
				} else {
					resp.put("status", "error");
					resp.put("message", "create task '" + type + "' fail");
				}
			} else if ("start".equals(method)) {
				String type = params.get("type");
				String id = params.get("id");
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.w(TAG, "start('" + type + "," + id + "')");
				}
				boolean r = TaskManager.getInstance().startTask(type, id);
				resp.put("status", "ok");
				resp.put("data", Boolean.toString(r));
			} else if ("pause".equals(method)) {
				String type = params.get("type");
				String id = params.get("id");
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.w(TAG, "pause('" + type + "," + id + "')");
				}
				boolean r = TaskManager.getInstance().pauseTask(type, id);
				resp.put("status", "ok");
				resp.put("data", Boolean.toString(r));
			} else if ("cancel".equals(method)) {
				String type = params.get("type");
				String id = params.get("id");
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.w(TAG, "cancel('" + type + "," + id + "')");
				}
				boolean r = TaskManager.getInstance().cancelTask(type, id);
				resp.put("status", "ok");
				resp.put("data", Boolean.toString(r));
			} else {
				if (Log.isLoggable(TAG, Log.WARN)) {
					Log.w(TAG, "invalid method '" + method + "'");
				}
				resp.put("status", "error");
				resp.put("message", "invalid method '" + method + "'");
			}
		} catch (Exception e) {
			Log.w(TAG, "Internal Server error", e);
			resp.put("status", "error");
			resp.put("message", e.toString());
		}
		HttpExchangeUtil.replyJson(exchange, resp);
	}
}