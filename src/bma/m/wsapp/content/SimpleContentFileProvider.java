package bma.m.wsapp.content;

import java.io.File;

import bma.m.wsapp.httpserver.HttpExchange;

public class SimpleContentFileProvider implements ContentFileProvider {

	protected final static String TAG = "FileContent";

	private File root;

	public SimpleContentFileProvider(File root) {
		super();
		this.root = root;
	}

	public File getRoot() {
		return root;
	}

	public void setRoot(File root) {
		this.root = root;
	}

	public ContentFile getContent(String path, HttpExchange exchange) {
		if (path.startsWith("/"))
			path = path.substring(1);
		File p = new File(this.root, path);
		if (p.exists())
			return new SimpleContentFile(p);
		return null;
	}
}
