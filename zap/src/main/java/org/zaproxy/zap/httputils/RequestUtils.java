package org.zaproxy.zap.httputils;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpRequestHeader;

/**
 * @deprecated No longer used/needed. It will be removed in a future release.
 */
@Deprecated
public class RequestUtils {

	/*
	 * Change the HTTP Method in header to method.
	 * 
	 */

	public static HttpRequestHeader changeMethod(String method, String header, String body) throws URIException, HttpMalformedHeaderException {
		HttpRequestHeader hrh = new HttpRequestHeader(header);
		URI uri = hrh.getURI();
		String prevMethod = hrh.getMethod();
		if (prevMethod.equalsIgnoreCase(method)) {
			return hrh;
		}
		if (prevMethod.equals(HttpRequestHeader.POST)) {
			// Was POST, move all params onto the URL
			if (body != null && body.length() > 0) {
				StringBuilder sb = new StringBuilder();
				if (uri.getQuery() != null) {
					sb.append(uri.getQuery());
				}

				String [] params = body.split("&");
				for (String param : params) {
					if (sb.length() > 0) {
						sb.append('&');
					}
					String[] nv = param.split("=");
					if (nv.length == 1) {
						// This effectively strips out the equals if theres no value 
						sb.append(nv[0]);
					} else {
						sb.append(param);
					}
				}
				uri.setQuery(sb.toString());

			}
			hrh.setURI(uri);
			// Clear the body
			body = "";

		} else if (method.equals(HttpRequestHeader.POST)) {
			// To be a port, move all URL query params into the body
			String query = uri.getQuery();
			if (query != null) {
				StringBuilder sb = new StringBuilder();
				String [] params = query.split("&");
				for (String param : params) {
					if (sb.length() > 0) {
						sb.append('&');
					}
					sb.append(param);
					String[] nv = param.split("=");
					if (nv.length == 1) {
						// Cope with URL params with no values e.g. http://www.example.com/test?key
						sb.append('=');
					}
				}
				// fixed: dead store to variable body by commenting the following line
				// body = sb.toString();
				uri.setQuery(null);
				hrh.setURI(uri);
			}
		}
		hrh.setMethod(method);
		
		return hrh;
	}
}
