package bma.m.wsapp.httpserver;

/**
 * Parameters that users will not likely need to set but are useful for
 * debugging
 */

class ServerConfig {

	static int clockTick = 10000;
	static long readTimeout = 20;
	static long writeTimeout = 60;
	static long idleInterval = 300;
	static long selCacheTimeout = 120;
	static int maxIdleConnections = 200;
	static long drainAmount = 64 * 1024;
	static boolean debug = false;

	static long getReadTimeout() {
		return readTimeout;
	}

	static long getSelCacheTimeout() {
		return selCacheTimeout;
	}

	static boolean debugEnabled() {
		return debug;
	}

	static long getIdleInterval() {
		return idleInterval;
	}

	static int getClockTick() {
		return clockTick;
	}

	static int getMaxIdleConnections() {
		return maxIdleConnections;
	}

	static long getWriteTimeout() {
		return writeTimeout;
	}

	static long getDrainAmount() {
		return drainAmount;
	}

	public static boolean isDebug() {
		return debug;
	}

	public static void setDebug(boolean debug) {
		ServerConfig.debug = debug;
	}

	public static void setClockTick(int clockTick) {
		ServerConfig.clockTick = clockTick;
	}

	public static void setReadTimeout(long readTimeout) {
		ServerConfig.readTimeout = readTimeout;
	}

	public static void setWriteTimeout(long writeTimeout) {
		ServerConfig.writeTimeout = writeTimeout;
	}

	public static void setIdleInterval(long idleInterval) {
		ServerConfig.idleInterval = idleInterval;
	}

	public static void setSelCacheTimeout(long selCacheTimeout) {
		ServerConfig.selCacheTimeout = selCacheTimeout;
	}

	public static void setMaxIdleConnections(int maxIdleConnections) {
		ServerConfig.maxIdleConnections = maxIdleConnections;
	}

	public static void setDrainAmount(long drainAmount) {
		ServerConfig.drainAmount = drainAmount;
	}

}
