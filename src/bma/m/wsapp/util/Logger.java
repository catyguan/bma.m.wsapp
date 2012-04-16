package bma.m.wsapp.util;

/////////////////////////////////////
/////////////////////////////////////
//          NOT USED!!!           //
/////////////////////////////////////
/////////////////////////////////////

import android.util.Log;

/**
 * 
 * @author Joan Puig Sanz
 * 
 */
public class Logger {

	private String tag;

	public Logger(String tag) {
		super();
	}

	public static Logger getLogger(String tag) {
		return new Logger(tag);
	}

	public static Logger getLogger(Class cls) {
		return getLogger(cls.getName());
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

}
