package bma.m.wsapp.task.base;

import java.util.Map;

import bma.m.wsapp.task.TaskManager;
import bma.m.wsapp.task.TaskUtil;

public abstract class ThreadTask extends AbstractTask {

	protected Thread thread;
	protected boolean doneOnFail = true;

	public boolean start() {
		if (thread != null) {
			if (thread.isAlive())
				return false;
			thread = null;
		}
		thread = new Thread(new Runnable() {

			public void run() {
				try {
					if (processTask()) {
						status = STATUS_FINISH;
						TaskManager.getInstance().taskDone(ThreadTask.this);
					} else {
						if (isDoneOnFail()) {
							TaskManager.getInstance().taskDone(ThreadTask.this);
						}
					}
				} catch (Exception e) {
					error(e.toString());
				}
			}
		});
		thread.start();
		status = STATUS_RUN;
		return true;
	}

	public boolean cancel() {
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
			return true;
		}
		return false;
	}

	public boolean isDoneOnFail() {
		return doneOnFail;
	}

	public void setDoneOnFail(boolean doneOnFail) {
		this.doneOnFail = doneOnFail;
	}

	@Override
	public void create(Map<String, String> props) {
		super.create(props);
		String v = TaskUtil.get(props, "doneOnFail", null);
		if (v != null) {
			doneOnFail = Boolean.valueOf(v);
		}
	}

	protected abstract boolean processTask() throws Exception;

}
