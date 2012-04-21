package bma.m.wsapp.content.asset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import bma.m.wsapp.content.ContentFile;
import bma.m.wsapp.httpserver.MimeTypes;
import bma.m.wsapp.util.IOUtil;

public class AssetContentFile implements ContentFile {

	private String fileName;
	private byte[] data;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public String getContentType() {
		return MimeTypes.contentType(this.fileName);
	}

	public boolean exists() {
		return true;
	}

	public Date lastModified() {
		return null;
	}

	public long getCacheTime() {
		return 0;
	}

	public int getContentLength() {
		if (this.data==null)
			return -1;
		return this.data.length;
	}

	public void writeTo(OutputStream out) throws IOException {
		InputStream in = null;
		try {
			in = new ByteArrayInputStream(this.data);
			IOUtil.copy(in, out);
		} finally {
			IOUtil.close(in);
		}
	}

}
