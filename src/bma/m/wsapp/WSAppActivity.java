package bma.m.wsapp;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
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
	protected String serverUrl;
	// The base of the initial URL for server url.
	// http://127.0.0.1:port/
	protected String baseUrl = null;

	// config
	protected String initPath;
	protected boolean fullescreen = false;

	public WSAppActivity() {
		super();
	}

	public String getBaseUrl() {
		return this.baseUrl;
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

	public WebView getView() {
		return this.appView;
	}

	protected void bindServerUrl(String surl) {
		this.serverUrl = surl;
		if (surl != null) {
			int idx = surl.lastIndexOf('/');
			if (idx != -1) {
				this.baseUrl = surl.substring(0, idx + 1);
			} else {
				this.baseUrl = surl;
			}
		} else {
			this.baseUrl = null;
		}
	}

	public final class AppWebChromeClient extends WebChromeClient {
		private long MAX_QUOTA = 2000000;

		public AppWebChromeClient() {
			super();
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
			AlertDialog.Builder alertBldr = new AlertDialog.Builder(
					WSAppActivity.this);
			alertBldr.setMessage(message);
			alertBldr.setTitle("Alert");
			alertBldr.show();
			result.confirm();
			return true;
		}

	}
	
	public class AppWebViewClient extends WebViewClient {
		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			Log.e(TAG, errorCode+":"+description+":"+failingUrl);
			// super.onReceivedError(view, errorCode, description, failingUrl);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
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
		// Display display = getWindowManager().getDefaultDisplay();
		// int width = display.getWidth();
		// int height = display.getHeight();

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
			appView.setWebChromeClient(new AppWebChromeClient());
		}
		appView.setWebViewClient(new AppWebViewClient());

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
		buildWebView(appView);

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
			String surl = bundle.getString("serverUrl");
			if (surl != null && surl.length() > 0) {
				bindServerUrl(surl);
			}
		}
		// Setup the hardware volume controls to handle volume control
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.endActivity();
	}

	protected void endActivity() {
		if (this.appView != null) {
			Log.d(TAG, "unloadWebView");
			unloadWebView(this.appView);
			this.appView.loadUrl("about:blank");
		}
		if (this.server != null) {
			Log.d(TAG, "stop server");
			this.server.stop(0);
			this.server = null;
		}
	}

	protected void buildWebView(WebView appView) {

	}

	protected void unloadWebView(WebView appView) {

	}

	protected WSAppServer newServer() throws IOException {
		WSAppServer r = new WSAppServer();
		buildServer(r);
		return r;
	}

	protected void buildServer(WSAppServer s) {

	}

	protected void start() throws IOException {
		if (this.serverUrl == null) {
			if (this.server == null) {
				this.server = newServer();
			}
			bindServerUrl(this.server.createServer());
		}
		StringBuffer buf = new StringBuffer(1024);
		buf.append(this.serverUrl);
		if (this.initPath != null && this.initPath.length() > 0) {
			buf.append("?go=").append(this.initPath);
		}
		Log.d(TAG, "start(" + buf + ")");
		appView.loadUrl(buf.toString());
	}

	public void loadUrl(String url) {
		appView.loadUrl(url);
	}

}