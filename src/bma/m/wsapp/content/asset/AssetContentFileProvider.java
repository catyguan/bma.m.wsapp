package bma.m.wsapp.content.asset;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import bma.m.wsapp.content.ContentFile;
import bma.m.wsapp.content.ContentFileProvider;
import bma.m.wsapp.httpserver.HttpExchange;
import bma.m.wsapp.util.IOUtil;
import bma.m.wsapp.util.VPath;

public class AssetContentFileProvider implements ContentFileProvider {

	protected final static String TAG = "AssetContent";

	private AssetManager manager;
	private VPath root;
	
	public AssetContentFileProvider(AssetManager manager) {
		super();
		this.manager = manager;
		root = VPath.root();
	}

	public AssetContentFileProvider(AssetManager manager, String root) {
		super();
		this.manager = manager;
		this.root = VPath.create(root);
	}
	
	public AssetContentFileProvider(Context ctx, String root) {
		super();
		this.manager = ctx.getAssets();
		this.root = VPath.create(root);
	}

	public AssetManager getManager() {
		return manager;
	}

	public void setManager(AssetManager manager) {
		this.manager = manager;
	}

	public String getRoot() {
		return root.toString();
	}

	public void setRoot(String root) {
		this.root = VPath.create(root);
	}

	public ContentFile getContent(String path, HttpExchange exchange) {
		VPath p = this.root.add(path);
		ContentFile file = query(p.toString(false));
		if (file != null) {
			if (file.exists()) {
				return file;
			}
		}
		return null;
	}

	protected ContentFile query(String fileName) {
		InputStream in = null;
		try {
			in = this.manager.open(fileName);
			if(Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG,"Asset File ("+fileName +")");
			}
			
			byte[] data = IOUtil.readStreamToBytes(in);
			
			AssetContentFile file = new AssetContentFile();
			file.setFileName(fileName);
			file.setData(data);
			return file;
		} catch (IOException e) {
			Log.d(TAG, "query assert:"+e.getMessage());
		} finally {
			IOUtil.close(in);
		}
		
		if(Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG,"not Asset File ("+fileName +")");
		}
		
		return null;
	}
}
