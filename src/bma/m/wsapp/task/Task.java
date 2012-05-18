package bma.m.wsapp.task;

import java.util.Map;

public interface Task {

	public static final String STATUS_PAUSE = "pause";
	public static final String STATUS_RUN = "run";
	public static final String STATUS_FINISH = "finish";
	public static final String STATUS_ERROR = "error";

	public String getId();
	
	public String getType();

	public String getStatus();

	public Map<String, String> getProperties();

	public boolean start();

	public boolean pause();

	public boolean cancel();
}
