/*
 * Created on May 26, 2004
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2012/01/12 Changed the method createRequestMethod to always use CRLF
// ZAP: 2012/03/15 Changed to use the class StringBuilder instead of StringBuffer
// ZAP: 2012/05/04 Changed to use the class ZapGetMethod instead of org.apache.commons.httpclient.methods.GetMethod
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/06/17 Issue 687: Change HTTP response header parser to be less strict
// ZAP: 2013/07/10 Issue 721: Non POST and PUT requests receive a 504 when server expects a request body
// ZAP: 2016/05/16 Throw exception if failed to set the request URI
package org.parosproxy.paros.network;

import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.zaproxy.zap.ZapGetMethod;
import org.zaproxy.zap.network.ZapDeleteMethod;
import org.zaproxy.zap.network.ZapHeadMethod;
import org.zaproxy.zap.network.ZapOptionsMethod;
import org.zaproxy.zap.network.ZapPostMethod;
import org.zaproxy.zap.network.ZapPutMethod;
import org.zaproxy.zap.network.ZapTraceMethod;

public class HttpMethodHelper {

	private static final Logger logger = Logger.getLogger(HttpMethodHelper.class);

	private static final String OPTIONS	= "OPTIONS";
	private static final String GET		= "GET";
	private static final String HEAD	= "HEAD";
	private static final String POST	= "POST";
	private static final String PUT		= "PUT";
	private static final String DELETE	= "DELETE";
	private static final String TRACE	= "TRACE";
	// TODO: What's up with the CONNECT method?
	//private static final String CONNECT	= "CONNECT";
	
	
	private static final String CRLF	=	"\r\n";
	private static final String LF		=	"\n";
	private static final Pattern patternCRLF	= Pattern.compile("\\r\\n", Pattern.MULTILINE);
	private static final Pattern patternLF		= Pattern.compile("\\n", Pattern.MULTILINE);
	
	private String mUserAgent = "";
	
	public void setUserAgent(String userAgent) {
		mUserAgent = userAgent;
	}

	// Not used - all abstract using Generic method but GET cannot be used.
	public HttpMethod createRequestMethodNew(HttpRequestHeader header, HttpBody body) throws URIException {
		HttpMethod httpMethod = null;
		
		String method = header.getMethod();
		URI uri	= header.getURI();
		String version = header.getVersion();
		
		httpMethod = new GenericMethod(method);

		httpMethod.setURI(uri);
		HttpMethodParams httpParams = httpMethod.getParams();
		// default to use HTTP 1.0
		httpParams.setVersion(HttpVersion.HTTP_1_0);
		if (version.equalsIgnoreCase(HttpHeader.HTTP11)) {
			httpParams.setVersion(HttpVersion.HTTP_1_1);
		}
		
		// set various headers
		int pos = 0;
		// ZAP: FindBugs fix - always initialise pattern
		Pattern pattern = patternCRLF;
		String delimiter = CRLF;
		
		String msg = header.getHeadersAsString();
		if ((pos = msg.indexOf(CRLF)) < 0) {
			if ((pos = msg.indexOf(LF)) < 0) {
				delimiter = LF;
				pattern = patternLF;
			}
		} else {
			delimiter = CRLF;
			pattern = patternCRLF;
		}
	        
		String[] split = pattern.split(msg);
		String token = null;
		String name = null;
		String value = null;
		//String host = null;
		
		for (int i=0; i<split.length; i++) {
			token = split[i];
			if (token.equals("")) {
				continue;
			}
			
			if ((pos = token.indexOf(":")) < 0) {
				return null;
			}
			name  = token.substring(0, pos).trim();
			value = token.substring(pos +1).trim();			
			httpMethod.addRequestHeader(name, value);

		}

		// set body if post method or put method
		if (body != null && body.length() > 0) {
			EntityEnclosingMethod generic = (EntityEnclosingMethod) httpMethod;
//			generic.setRequestEntity(new StringRequestEntity(body.toString()));
            generic.setRequestEntity(new ByteArrayRequestEntity(body.getBytes()));

        }

		httpMethod.setFollowRedirects(false);
		return httpMethod;

	}

	//  This is the currently in use method.
	// may be replaced by the New method - however the New method is not yet fully tested so this is stil used.
	public HttpMethod createRequestMethod(HttpRequestHeader header, HttpBody body) throws URIException {
		HttpMethod httpMethod = null;
		
		String method = header.getMethod();
		URI uri	= header.getURI();
		String version = header.getVersion();
		
		if (method == null || method.trim().length() < 3) {
			throw new URIException("Invalid HTTP method: " + method);
		}
		
		if (method.equalsIgnoreCase(GET)) {
			//httpMethod = new GetMethod();
			// ZAP: avoid discarding HTTP status code 101 that is used for WebSocket upgrade 
			httpMethod = new ZapGetMethod();
		} else if (method.equalsIgnoreCase(POST)) {
			httpMethod = new ZapPostMethod();
		} else if (method.equalsIgnoreCase(DELETE)) {
			httpMethod = new ZapDeleteMethod();
		} else if (method.equalsIgnoreCase(PUT)) {
			httpMethod = new ZapPutMethod();
		} else if (method.equalsIgnoreCase(HEAD)) {
			httpMethod = new ZapHeadMethod();
		} else if (method.equalsIgnoreCase(OPTIONS)) {
			httpMethod = new ZapOptionsMethod();
		} else if (method.equalsIgnoreCase(TRACE)) {
			httpMethod = new ZapTraceMethod(uri.toString());
		} else {
			httpMethod = new GenericMethod(method);
		}

		try {
			httpMethod.setURI(uri);
		} catch (Exception e1) {
			throw new URIException("Failed to set URI: " + e1.getMessage());
		}
		
		HttpMethodParams httpParams = httpMethod.getParams();
		// default to use HTTP 1.0
		httpParams.setVersion(HttpVersion.HTTP_1_0);
		if (version.equalsIgnoreCase(HttpHeader.HTTP11)) {
			httpParams.setVersion(HttpVersion.HTTP_1_1);
		}
		
		// set various headers
		int pos = 0;
		// ZAP: changed to always use CRLF, like the HttpHeader
		Pattern pattern = patternCRLF;
		String delimiter = header.getLineDelimiter();
		
		// ZAP: Shouldn't happen as the HttpHeader always uses CRLF
		if (delimiter.equals(LF)) {
			delimiter = LF;
			pattern = patternLF;
		} 
		
		String msg = header.getHeadersAsString();
		
		String[] split = pattern.split(msg);
		String token = null;
		String name = null;
		String value = null;
		
		for (int i=0; i<split.length; i++) {
			token = split[i];
			if (token.equals("")) {
				continue;
			}
			
			if ((pos = token.indexOf(":")) < 0) {
				return null;
			}
			name  = token.substring(0, pos).trim();
			value = token.substring(pos +1).trim();			
			httpMethod.addRequestHeader(name, value);

		}

		// set body if post method or put method
		if (body != null && body.length() > 0 &&  (httpMethod instanceof EntityEnclosingMethod)) {
			EntityEnclosingMethod post = (EntityEnclosingMethod) httpMethod;
//			post.setRequestEntity(new StringRequestEntity(body.toString()));
            post.setRequestEntity(new ByteArrayRequestEntity(body.getBytes()));

		}

		httpMethod.setFollowRedirects(false);
		return httpMethod;

	}
	
	
	/*
	 * Build a HttpMethod (eg GET, POST) from raw string.  All headers will be set accordingly as in
	 * the raw string.
	 * @param request	raw request string with header and body.
	 * @param isSecure true if the connection is SSL.
	 * @return an unexecuted HttpMethod
	 */
	/* This is the original code using a String as request.  Now obsolete
	 * 
	private HttpMethod createRequestMethod(String request, boolean isSecure) throws HttpException, URIException {
		HttpMethod httpMethod = null;
		Pattern pattern = null;
		String delimiter = CRLF;
		
		int pos = 0;
		
		if (request == null || request.equals("")) {
			throw new HttpException ("Null or empty request");
		}
		
		if ((pos = request.indexOf(CRLF)) < 0) {
			if ((pos = request.indexOf(LF)) < 0) {
				throw new HttpException ("Invalid HTTP request with missing CR/LF");
			} else {
				delimiter = LF;
				pattern = patternLF;
			}
		} else {
			delimiter = CRLF;
			pattern = patternCRLF;
		}
	        
		String[] split = pattern.split(request);
		String startLine = split[0];
		httpMethod = createMethodFromStartLine(startLine);
		
		String token = null;
		String name = null;
		String value = null;
		String host = null;
		
		for (int i=1; i<split.length; i++) {
			token = split[i];
			if (token.equals("")) {
				continue;
			}
			
			if ((pos = token.indexOf(":")) < 0) {
				return null;
			}
			name  = token.substring(0, pos).trim();
			value = token.substring(pos +1).trim();

			if (name.equalsIgnoreCase(HEADER_HOST)) {
				host = value;
			}
			
			httpMethod.addRequestHeader(name, value);

		}

		URI uri = httpMethod.getURI();			
		boolean isUriChanged = false;
		if (uri.getScheme() == null || uri.getScheme().equals("")) {
			uri = new URI(HTTP + "://" + host + uri.toString(), true);
			isUriChanged = true;
		}

		if (isSecure && uri.getScheme().equalsIgnoreCase(HTTP)) {
			uri = new URI(uri.toString().replaceFirst(HTTP, HTTPS), true);
			isUriChanged = true;
		}
		
		if (isUriChanged) {
			httpMethod.setURI(uri);
		}
		
		httpMethod.setFollowRedirects(false);
		return httpMethod;
	}
	
	private HttpMethod createMethodFromStartLine(String startLine) throws URIException {
		HttpMethod httpMethod = null;
		
		Matcher matcher = patternRequestLine.matcher(startLine);
		if (!matcher.find()) {
			throw new URIException("Missing startLine in HTTP request");
		}
		
		String method = matcher.group(1);
		String uri	= matcher.group(2);
		String version = matcher.group(3);
		
		if (method.equalsIgnoreCase(GET)) {
			httpMethod = new GetMethod();
		} else if (method.equalsIgnoreCase(POST)) {
			httpMethod = new PostMethod();
		} else if (method.equalsIgnoreCase(DELETE)) {
			httpMethod = new DeleteMethod();
		} else if (method.equalsIgnoreCase(PUT)) {
			httpMethod = new PutMethod();
		} else if (method.equalsIgnoreCase(HEAD)) {
			httpMethod = new HeadMethod();
		} else if (method.equalsIgnoreCase(OPTIONS)) {
			httpMethod = new OptionsMethod();
		} else if (method.equalsIgnoreCase(TRACE)) {
			httpMethod = new TraceMethod(uri);
		} else {
			// httpMethod = GenericMethod();
		}

		httpMethod.setURI(new URI(uri, true));
		HttpMethodParams httpParams = httpMethod.getParams();
		// default to use HTTP 1.0
		httpParams.setVersion(HttpVersion.HTTP_1_0);
		if (version.equalsIgnoreCase(HTTP11)) {
			httpParams.setVersion(HttpVersion.HTTP_1_1);
		}

		httpMethod.setParams(httpParams);
		
		return httpMethod;
	}
	*/
	
	public static void updateHttpRequestHeaderSent(HttpRequestHeader req, HttpMethod httpMethodSent) {
		// Not used yet, no need to update request.
		if (!httpMethodSent.hasBeenUsed()) {
		    return;
		}
		
		StringBuilder sb = new StringBuilder(200);
		String name = null;
		String value = null;
		
		// add status line
		sb.append(req.getPrimeHeader()).append(CRLF);

		Header[] header = httpMethodSent.getRequestHeaders();
		for (int i=0; i<header.length; i++) {
			name = header[i].getName();
			value = header[i].getValue();
			sb.append(name).append(": ").append(value).append(CRLF);
		}
		
		sb.append(CRLF);
	    try {
            req.setMessage(sb.toString());
        } catch (HttpMalformedHeaderException e) {
            logger.error(e.getMessage(), e);
        }
	}
	
	private static String getHttpResponseHeaderAsString(HttpMethod httpMethod) {
		StringBuilder sb = new StringBuilder(200);
		String name = null;
		String value = null;

		// add status line
		sb.append(httpMethod.getStatusLine().toString()).append(CRLF);

		Header[] header = httpMethod.getResponseHeaders();
		for (int i=0; i<header.length; i++) {
			name = header[i].getName();
			value = header[i].getValue();
			sb.append(name).append(": ").append(value).append(CRLF);
		}
		
		sb.append(CRLF);
		return sb.toString();
	}

	
	public static HttpResponseHeader getHttpResponseHeader(HttpMethod httpMethod) throws HttpMalformedHeaderException {
		return new HttpResponseHeader(getHttpResponseHeaderAsString(httpMethod));
	}

	/*
	public static String getHttpRequestHeaderAsString(HttpMethod httpMethod) {
		StringBuilder sb = new StringBuilder(200);
		String name = null;
		String value = null;

		// add status line
		try {
			sb.append(httpMethod.getName()).append(' ').append(httpMethod.getURI().toString()).append(' ').append(httpMethod.getParams().getVersion()).append(CRLF);
		} catch (URIException e) {
			
		}
		
		Header[] header = httpMethod.getRequestHeaders();
		for (int i=0; i<header.length; i++) {
			name = header[i].getName();
			value = header[i].getValue();
			sb.append(name).append(": ").append(value).append(CRLF);
		}
		
		sb.append(CRLF);
		return sb.toString();
	}
	*/

	
	
}
