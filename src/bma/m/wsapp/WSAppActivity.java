package bma.m.wsapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

public class WSAppActivity extends Activity {

	public static String TAG = "WSApp";

	// The webview for our app
	protected WebView appView;
	protected WebViewClient webViewClient;

	protected LinearLayout root;
	protected boolean bound = false;
	protected WSAppServer server;
	protected boolean cancelLoadUrl = false;
	protected ProgressDialog spinnerDialog = null;

	// activityState
	private static int ACTIVITY_RUNNING = 1;
	private static int ACTIVITY_EXITING = 2;
	private int activityState = 0; // 0=starting, 1=running (after 1st resume),
									// 2=shutting down

	// The base of the initial URL for server url.
	// http://127.0.0.1:port/
	protected String baseUrl = null;

	// Flag indicates that a loadUrl timeout occurred
	protected int loadUrlTimeout = 0;

	// Default background color for activity
	// (this is not the color for the webview, which is set in HTML)
	private int backgroundColor = Color.BLACK;

	/*
	 * The variables below are used to cache some of the activity properties.
	 */
	// Draw a splash screen using an image located in the drawable resource
	// directory.
	// This is not the same as calling super.loadSplashscreen(url)
	protected int splashscreen = 0;

	// LoadUrl timeout value in msec (default of 20 sec)
	protected int loadUrlTimeoutValue = 20000;

	// Keep app running when pause is received. (default = true)
	// If true, then the JavaScript and native code continue to run in the
	// background
	// when another application (activity) is started.
	protected boolean keepRunning = true;

	// config
	protected String initPath;
	protected boolean fullescreen = false;

	public WSAppActivity() {
		super();
	}

	public String getInitPath() {
		return initPath;
	}

	public void setInitPath(String initPath) {
		this.initPath = initPath;
	}

	public boolean isFullescreen() {
		return fullescreen;
	}

	public void setFullescreen(boolean fullescreen) {
		this.fullescreen = fullescreen;
	}

	public final class EclairClient extends WebChromeClient {
		private long MAX_QUOTA = 2000000;

		Context ctx;

		public EclairClient(Context ctx) {
			super();
			this.ctx = ctx;
		}

		public void onExceededDatabaseQuota(String url,
				String databaseIdentifier, long currentQuota,
				long estimatedSize, long totalUsedQuota,
				WebStorage.QuotaUpdater quotaUpdater) {

			if (estimatedSize < MAX_QUOTA) {
				long newQuota = estimatedSize;
				quotaUpdater.updateQuota(newQuota);
			} else {
				// Set the quota to whatever it is and force an error
				// TODO: get docs on how to handle this properly
				quotaUpdater.updateQuota(currentQuota);
			}
		}

		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				JsResult result) {
			Log.d(TAG, message);
			// This shows the dialog box. This can be commented out for dev
			AlertDialog.Builder alertBldr = new AlertDialog.Builder(ctx);
			alertBldr.setMessage(message);
			alertBldr.setTitle("Alert");
			alertBldr.show();
			result.confirm();
			return true;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "WSAppActivity.onCreate()");
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		if (fullescreen) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}

		// This builds the view. We could probably get away with NOT having a
		// LinearLayout, but I like having a bucket!
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();

		LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT, 0.0F);

		LinearLayout.LayoutParams webviewParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT, 1.0F);

		root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setBackgroundColor(Color.BLACK);
		root.setLayoutParams(containerParams);

		appView = new WebView(this);
		appView.setLayoutParams(webviewParams);

		WebViewReflect.checkCompatibility();

		/*
		 * This changes the setWebChromeClient to log alerts to LogCat!
		 * Important for Javascript Debugging
		 */
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ECLAIR) {
			// TODO
			// appView.setWebChromeClient(new EclairClient(this));
		}

		appView.setInitialScale(100);
		appView.setVerticalScrollBarEnabled(false);

		WebSettings settings = appView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);

		Package pack = this.getClass().getPackage();
		String appPackage = pack.getName();

		WebViewReflect.setStorage(settings, true, "/data/data/" + appPackage
				+ "/app_database/");

		/* Bind the appView object to the gap class methods */
		// TODO
		// bindBrowser(appView);

		root.addView(appView);
		setContentView(root);

		// If url was passed in to intent, then init webview, which will load
		// the url
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			String path = bundle.getString("path");
			if (path != null) {
				this.initPath = path;
			}
		}
		// Setup the hardware volume controls to handle volume control
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

}