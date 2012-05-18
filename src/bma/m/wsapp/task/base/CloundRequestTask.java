package bma.m.wsapp.task.base;

import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import bma.m.wsapp.task.TaskUtil;

public class CloundRequestTask extends ThreadTask {

	private String url;
	private String result;

	@Override
	public Map<String, String> getProperties() {
		Map<String, String> r = super.getProperties();
		TaskUtil.put(r, "url", url);
		TaskUtil.put(r, "result", result);
		return r;
	}

	public void create(Map<String, String> props) {
		super.create(props);
		url = TaskUtil.get(props, "url", null);
	}

	public boolean pause() {
		return false;
	}

	@Override
	protected boolean processTask() throws Exception {
		if (url == null || url.length() == 0) {
			error("url is empty");
			return false;
		}
		HttpGet httpGet = new HttpGet(url);
		HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
		int code = httpResponse.getStatusLine().getStatusCode();
		if (code == 200) {
			result = EntityUtils.toString(httpResponse.getEntity());
			return true;
		} else {
			error("status code " + code);
			return false;
		}
	}

}
