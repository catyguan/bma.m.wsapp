package bma.m.wsapp.task;

import java.util.List;
import java.util.Map;

public interface TaskProvider {

	public List<Task> listTasks();

	public Task createTask(Map<String, String> properties);

	public Task getTask(String id);

	public boolean startTask(String id);

	public boolean pauseTask(String id);

	public boolean cancelTask(String id);

	public void taskDone(Task task);
	
}
