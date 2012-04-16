	
package bma.m.wsapp.httpserver;

import java.io.IOException;

class StreamClosedException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StreamClosedException() {
		super();
	}

	public StreamClosedException(String detailMessage) {
		super(detailMessage);
	}

}
