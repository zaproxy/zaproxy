package org.zaproxy.zap;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;

public class ZapHttpConnectionManager extends SimpleHttpConnectionManager {

	/**
	 * Use custom HttpConnection class to allow for socket exposure.
	 */
	public HttpConnection getConnectionWithTimeout(
			HostConfiguration hostConfiguration, long timeout) {

		if (httpConnection == null) {
			httpConnection = new ZapHttpConnection(hostConfiguration);
			httpConnection.setHttpConnectionManager(this);
			httpConnection.getParams().setDefaults(this.getParams());
		}
		
		return super.getConnectionWithTimeout(hostConfiguration, timeout);
	}
}
