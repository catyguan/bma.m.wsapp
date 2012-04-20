package bma.m.wsapp.resource;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;
import bma.m.wsapp.content.ContentFile;
import bma.m.wsapp.content.ContentFileProvider;
import bma.m.wsapp.httpserver.HttpExchange;
import bma.m.wsapp.util.VPath;

public class AssetContentFileProvider implements ContentFileProvider {

	protected final static String TAG = "AssertContent";

	private AssetManager manager;
	private VPath root;
	private Map<String, AssetContentFile> cache = Collections
			.synchronizedMap(new HashMap<String, AssetContentFile>());

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
		ContentFile file = query(p.toString());
		if (file != null) {
			if (file.exists()) {
				return file;
			}
		}
		return null;
	}

	protected ContentFile query(String fileName) {
		AssetContentFile file = cache.get(fileName);
		if (file != null) {
			return file;
		}
		try {
			String[] list = this.manager.list(fileName);
			if (list != null && list.length > 0) {
				if (Log.isLoggable(TAG, Log.DEBUG)) {
					Log.d(TAG, "Asset Directory (" + fileName + ")");
				}
				file = new AssetContentFile();
				file.setFileName(fileName);
				file.setDirectory(true);
				cache.put(fileName, file);
				return file;
			}
			AssetFileDescriptor fd = this.manager.openFd(fileName);
			if (fd != null && fd.getDeclaredLength() > 0) {
				if(Log.isLoggable(TAG, Log.DEBUG)) {
					Log.d(TAG,"Asset File ("+fileName +")");
				}
				file = new AssetContentFile();
				file.setFileName(fileName);
				file.setFd(fd);
				cache.put(fileName, file);
				return file;
			}
		} catch (IOException e) {
			Log.d(TAG, "query assert", e);
		}
		
		if(Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG,"not Asset File ("+fileName +")");
		}

		file = new AssetContentFile();
		file.setFileName(fileName);
		file.setExists(false);
		cache.put(fileName, file);
		return null;
	}
}
