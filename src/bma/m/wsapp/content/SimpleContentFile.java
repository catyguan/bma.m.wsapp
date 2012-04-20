package bma.m.wsapp.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import bma.m.wsapp.httpserver.MimeTypes;
import bma.m.wsapp.util.IOUtil;

public class SimpleContentFile implements ContentFile {

	private File file;
	private int cacheTime;

	public SimpleContentFile(File file) {
		super();
		this.file = file;
	}

	public void setCacheTime(int cacheTime) {
		this.cacheTime = cacheTime;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getContentType() {
		return MimeTypes.contentType(this.file.getName());
	}

	public boolean exists() {
		return this.file.exists();
	}

	public boolean isDirectory() {
		return this.file.isDirectory();
	}

	public Date lastModified() {
		return new Date(this.file.lastModified());
	}

	public long getCacheTime() {
		return cacheTime;
	}

	public int getContentLength() {
		return (int) this.file.length();
	}

	public void writeTo(OutputStream out) throws IOException {		
		InputStream in = null;
		try {
			in = new FileInputStream(this.file);			
			IOUtil.copy(in, out);
		} finally {
			IOUtil.close(in);
		}
	}

}
