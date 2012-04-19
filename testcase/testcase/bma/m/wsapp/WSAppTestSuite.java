package testcase.bma.m.wsapp;

import junit.framework.TestSuite;

public class WSAppTestSuite extends TestSuite {

	public WSAppTestSuite() {
		super();
		addTestSuite(HttpServerTest.class);
	}
}
