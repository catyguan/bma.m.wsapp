package bma.m.wsapp.task.base;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import bma.m.wsapp.task.Task;
import bma.m.wsapp.task.TaskProvider;

public abstract class RuntimeTaskProvider implements TaskProvider {

	protected long removeDelayTime = 10 * 1000;

	protected List<Task> tasks = Collections
			.synchronizedList(new LinkedList<Task>());

	public long getRemoveDelayTime() {
		return removeDelayTime;
	}

	public void setRemoveDelayTime(long removeDelayTime) {
		this.removeDelayTime = removeDelayTime;
	}

	public List<Task> listTasks() {
		return tasks;
	}

	public Task getTask(String id) {
		for (Task t : tasks) {
			String v = t.getId();
			if (v != null && v.equals(id))
				return t;
		}
		return null;
	}

	public boolean startTask(String id) {
		Task t = getTask(id);
		if (t != null)
			return t.start();
		return false;
	}

	public boolean pauseTask(String id) {
		Task t = getTask(id);
		if (t != null)
			return t.pause();
		return false;
	}

	public boolean cancelTask(String id) {
		Task t = getTask(id);
		if (t != null)
			return t.cancel();
		return false;
	}

	public void taskDone(final Task task) {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				tasks.remove(task);
			}
		}, this.removeDelayTime);
	}

}
