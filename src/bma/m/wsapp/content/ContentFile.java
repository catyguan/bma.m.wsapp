package bma.m.wsapp.content;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public interface ContentFile {

	public String getContentType();

	public boolean exists();

	public boolean isDirectory();

	public Date lastModified();

	public long getCacheTime();

	public int getContentLength();

	public void writeTo(OutputStream out) throws IOException;

}
