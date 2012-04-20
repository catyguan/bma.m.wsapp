package bma.m.wsapp.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import android.content.res.AssetFileDescriptor;
import bma.m.wsapp.content.ContentFile;
import bma.m.wsapp.httpserver.MimeTypes;
import bma.m.wsapp.util.IOUtil;

public class AssetContentFile implements ContentFile {

	private String fileName;
	private AssetFileDescriptor fd;
	private boolean directory;
	private boolean exists = true;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isExists() {
		return exists;
	}

	public void setExists(boolean exists) {
		this.exists = exists;
	}

	public AssetFileDescriptor getFd() {
		return fd;
	}

	public void setFd(AssetFileDescriptor fh) {
		this.fd = fh;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public String getContentType() {
		return MimeTypes.contentType(this.fileName);
	}

	public boolean exists() {
		return exists;
	}

	public boolean isDirectory() {
		return directory;
	}

	public Date lastModified() {
		return null;
	}

	public long getCacheTime() {
		return 0;
	}

	public int getContentLength() {
		if (this.directory)
			return -1;
		if (this.fd == null)
			return -1;
		return (int) this.fd.getDeclaredLength();
	}

	public void writeTo(OutputStream out) throws IOException {
		if (this.fd == null) {
			throw new NullPointerException(fileName + " AssetFileDescriptor");
		}
		InputStream in = null;
		try {
			in = this.fd.createInputStream();
			IOUtil.copy(in, out);
		} finally {
			IOUtil.close(in);
		}
	}

}
