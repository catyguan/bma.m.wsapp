package bma.m.wsapp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.webkit.WebSettings;

public class WebViewReflect {

	private static Method webSettings_setDatabaseEnabled;
	private static Method webSettings_setDatabasePath;
	static {
		checkCompatibility();
	}

	public static void checkCompatibility() {
		try {
			webSettings_setDatabaseEnabled = WebSettings.class.getMethod(
					"setDatabaseEnabled", new Class[] { boolean.class });
			webSettings_setDatabasePath = WebSettings.class.getMethod(
					"setDatabasePath", new Class[] { String.class });
			/* success, this is a newer device */
		} catch (NoSuchMethodException nsme) {
			/* failure, must be older device */
		}
	}

	public static void setStorage(WebSettings setting, boolean enable,
			String path) {
		if (webSettings_setDatabaseEnabled != null) {
			/* feature is supported */
			try {
				webSettings_setDatabaseEnabled.invoke(setting, true);
				webSettings_setDatabasePath.invoke(setting, path);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// setting.setDatabaseEnabled(enable);
			// setting.setDatabasePath(path);
		} else {
			/* feature not supported, do something else */
			// System.out.println("dump not supported");
		}
	}
}
