package bma.m.wsapp.task;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TaskManager {

	private static TaskManager instance;

	public static TaskManager getInstance() {
		return instance;
	}

	protected Map<String, TaskProvider> providers = new TreeMap<String, TaskProvider>();

	public TaskProvider getProvider(String type) {
		if (type == null || type.length() == 0)
			return null;
		return providers.get(type);
	}

	public void setProvider(String type, TaskProvider provider) {
		providers.put(type, provider);
	}

	public List<Task> listTasks(String type) {
		List<Task> r = new LinkedList<Task>();
		if (type == null) {
			for (TaskProvider p : providers.values()) {
				r.addAll(p.listTasks());
			}
		} else {
			TaskProvider p = getProvider(type);
			if (p != null) {
				r.addAll(p.listTasks());
			}
		}
		return r;
	}

	public Task createTask(String type, Map<String, String> properties) {
		TaskProvider p = getProvider(type);
		if (p != null) {
			return p.createTask(properties);
		}
		return null;
	}

	public Task getTask(String type, String id) {
		TaskProvider p = getProvider(type);
		if (p != null) {
			return p.getTask(id);
		}
		return null;
	}

	public boolean startTask(String type, String id) {
		TaskProvider p = getProvider(type);
		if (p != null) {
			return p.startTask(id);
		}
		return false;
	}

	public boolean pauseTask(String type, String id) {
		TaskProvider p = getProvider(type);
		if (p != null) {
			return p.pauseTask(id);
		}
		return false;
	}

	public boolean cancelTask(String type, String id) {
		TaskProvider p = getProvider(type);
		if (p != null) {
			return p.cancelTask(id);
		}
		return false;
	}
	
	public void taskDone(Task task) {
		String type = task.getType();
		TaskProvider p = getProvider(type);
		if (p != null) {
			p.taskDone(task);
		}
	}
}
