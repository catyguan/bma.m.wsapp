package bma.m.wsapp.util;


import android.util.Log;

public class Logger {

	private String tag;

	public Logger(String tag) {
		super();
		this.tag = tag;
	}

	public static Logger getLogger(String tag) {
		return new Logger(tag);
	}

	public static Logger getLogger(Class cls) {
		return getLogger(cls.getSimpleName());
	}

	public String getTag() {
		return tag;
	}

	public void debug(String msg) {
		Log.d(tag, msg);
	}

	public void debug(String msg, Throwable t) {
		Log.d(tag, msg, t);
	}

	public void error(String msg) {
		Log.e(tag, msg);
	}

	public void error(String msg, Throwable t) {
		Log.e(tag, msg, t);
	}

	public void warn(String msg) {
		Log.w(tag, msg);
	}

	public void warn(String msg, Throwable t) {
		Log.w(tag, msg, t);
	}

	public void info(String msg) {
		Log.i(tag, msg);
	}

	public void info(String msg, Throwable t) {
		Log.i(tag, msg, t);
	}

	public boolean isLoggable(int level) {
		return Log.isLoggable(tag, level);
	}

}
