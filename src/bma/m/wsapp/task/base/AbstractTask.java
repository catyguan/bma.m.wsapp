package bma.m.wsapp.task.base;

import java.util.Map;
import java.util.TreeMap;

import bma.m.wsapp.task.Const;
import bma.m.wsapp.task.Task;
import bma.m.wsapp.task.TaskUtil;

public abstract class AbstractTask implements Task {

	protected String id;
	protected String type;
	protected String status = Task.STATUS_PAUSE;
	protected String message;
	protected String title;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Map<String, String> getProperties() {
		Map<String, String> r = new TreeMap<String, String>();
		r.put(Const.TaskProperty.ID, id);
		r.put(Const.TaskProperty.TYPE, type);
		r.put(Const.TaskProperty.STATUS, status);
		TaskUtil.put(r, Const.TaskProperty.MESSAGE, message);
		TaskUtil.put(r, Const.TaskProperty.TITLE, title);
		return r;
	}

	public void create(Map<String, String> props) {
		title = TaskUtil.get(props, Const.TaskProperty.TITLE, null);
	}

	public void error(String msg) {
		this.status = Task.STATUS_ERROR;
		this.message = msg;
	}

}
