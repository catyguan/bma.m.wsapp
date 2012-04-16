package bma.m.wsapp.httpserver;

/**
 * A Http error
 */
class HttpError extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public HttpError(String msg) {
		super(msg);
	}
}
