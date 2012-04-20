package bma.m.wsapp.httpserver;

import java.util.HashMap;
import java.util.Map;

public class MimeTypes {
	private static Map<String, String> mimeTypes;

	static {
		mimeTypes = new HashMap<String, String>();
		mimeTypes.put("htm", "text/html");
		mimeTypes.put("html", "text/html");
		mimeTypes.put("xml", "text/xml");
		mimeTypes.put("txt", "text/plain");

		mimeTypes.put("css", "text/css");

		mimeTypes.put("jpg", "image/jpeg");
		mimeTypes.put("jpeg", "image/jpeg");
		mimeTypes.put("gif", "image/gif");
		mimeTypes.put("png", "image/png");
		mimeTypes.put("tif", "image/tiff");
		mimeTypes.put("tiff", "image/tiff");
		mimeTypes.put("ico", "image/x-icon");

		mimeTypes.put("js", "application/x-javascript");
		mimeTypes.put("json", "application/x-json");
		mimeTypes.put("swf", "application/x-shockwave-flash");

		mimeTypes.put("mid", "audio/midi");
		mimeTypes.put("midi", "audio/midi");
		mimeTypes.put("kar", "audio/midi");

		mimeTypes.put("mp3", "audio/mpeg");
		mimeTypes.put("3gpp", "audio/3gpp");
		mimeTypes.put("3gp", "audio/3gpp");
		mimeTypes.put("mp4", "audio/mp4");
		mimeTypes.put("mpeg", "audio/mpeg");
		mimeTypes.put("mpg", "audio/mpeg");
		mimeTypes.put("mov", "audio/quicktime");
		mimeTypes.put("flv", "audio/x-flv");
		mimeTypes.put("m4v", "audio/x-m4v");
	}

	public static String contentType(String fileName) {
		String ext = "";
		int idx = fileName.lastIndexOf(".");
		if (idx >= 0) {
			ext = fileName.substring(idx + 1);
		}

		if (mimeTypes.containsKey(ext))
			return mimeTypes.get(ext);
		else
			return "application/octet-stream";
	}
}
