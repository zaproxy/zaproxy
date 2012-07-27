/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 * ZAP: Based on work by Yasser Ganjisaffar <lastname at gmail dot com> 
 * from project http://code.google.com/p/crawler4j/
 */

package org.zaproxy.zap.spider;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * The URLCanonicalizer is used for the process of converting an URL into a canonical (normalized)
 * form. See {@link http://en.wikipedia.org/wiki/URL_normalization} for a reference. <br/>
 * <br/>
 * 
 * Note: some parts of the code are adapted from: http://stackoverflow.com/a/4057470/405418
 * 
 * 
 */
public class URLCanonicalizer {

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(URLCanonicalizer.class);

	/**
	 * Gets the canonical url.
	 * 
	 * @param url the url
	 * @return the canonical url
	 */
	public static String getCanonicalURL(String url) {
		return getCanonicalURL(url, null);
	}

	/**
	 * Gets the canonical url, starting from a relative or absolute url found in a given context
	 * (baseURL).
	 * 
	 * @param url the url string defining the reference
	 * @param baseURL the context in which this url was found
	 * @return the canonical url
	 */
	public static String getCanonicalURL(String url, String baseURL) {

		try {
			/* Build the absolute URL, from the url and the baseURL */
			String resolvedURL = URLResolver.resolveUrl(baseURL == null ? "" : baseURL, url);
			log.info("Resolved URL: " + resolvedURL);
			URI canonicalURI = new URI(resolvedURL);

			/* Some checking. */
			if (canonicalURI.getScheme() == null)
				throw new MalformedURLException("Protocol could not be reliably evaluated from uri: " + canonicalURI
						+ " and base url: " + baseURL);
			if (canonicalURI.getHost() == null)
				throw new MalformedURLException("Host could not be reliably evaluated from: " + canonicalURI);

			/* Normalize: no empty segments (i.e., "//"), no segments equal to ".", and no segments
			 * equal to ".." that are preceded by a segment not equal to "..". */
			String path = canonicalURI.normalize().getPath();

			/* Convert '//' -> '/' */
			int idx = path.indexOf("//");
			while (idx >= 0) {
				path = path.replace("//", "/");
				idx = path.indexOf("//");
			}

			/* Drop starting '/../' */
			while (path.startsWith("/../")) {
				path = path.substring(3);
			}

			/* Trim */
			path = path.trim();

			/* Process parameters and sort them. */
			final SortedMap<String, String> params = createParameterMap(canonicalURI.getQuery());
			final String queryString;

			if (params != null && params.size() > 0) {
				String canonicalParams = canonicalize(params);
				queryString = (canonicalParams.isEmpty() ? "" : "?" + canonicalParams);
			} else {
				queryString = "";
			}

			/* Add starting slash if needed */
			if (path.length() == 0) {
				path = "/" + path;
			}

			/* Drop default port: example.com:80 -> example.com */
			int port = canonicalURI.getPort();
			if (port == 80) {
				port = -1;
			}

			/* Lowercasing protocol and host */
			String protocol = canonicalURI.getScheme().toLowerCase();
			String host = canonicalURI.getHost().toLowerCase();
			String pathAndQueryString = normalizePath(path) + queryString;

			URL result = new URL(protocol, host, port, pathAndQueryString);
			return result.toExternalForm();

		} catch (MalformedURLException ex) {
			log.error("Error while Processing URL in the spidering process: " + ex.getMessage());
			return null;
		} catch (URISyntaxException ex) {
			log.error("Error while Processing URI in the spidering process: " + ex.getMessage());
			return null;
		}
	}

	/**
	 * Takes a query string, separates the constituent name-value pairs, and stores them in a
	 * SortedMap ordered by lexicographical order.
	 * 
	 * @param queryString the query string
	 * @return Null if there is no query string.
	 */
	private static SortedMap<String, String> createParameterMap(final String queryString) {
		if (queryString == null || queryString.isEmpty()) {
			return null;
		}

		final String[] pairs = queryString.split("&");
		final Map<String, String> params = new HashMap<String, String>(pairs.length);

		for (final String pair : pairs) {
			if (pair.length() == 0) {
				continue;
			}

			String[] tokens = pair.split("=", 2);
			switch (tokens.length) {
			case 1:
				if (pair.charAt(0) == '=') {
					params.put("", tokens[0]);
				} else {
					params.put(tokens[0], "");
				}
				break;
			case 2:
				params.put(tokens[0], tokens[1]);
				break;
			}
		}
		return new TreeMap<String, String>(params);
	}

	/**
	 * Canonicalize the query string.
	 * 
	 * @param sortedParamMap Parameter name-value pairs in lexicographical order.
	 * @return Canonical form of query string.
	 */
	private static String canonicalize(final SortedMap<String, String> sortedParamMap) {
		if (sortedParamMap == null || sortedParamMap.isEmpty()) {
			return "";
		}

		final StringBuffer sb = new StringBuffer(100);
		for (Map.Entry<String, String> pair : sortedParamMap.entrySet()) {
			final String key = pair.getKey().toLowerCase();
			// Ignore irrelevant parameters
			if (key.equals("jsessionid") || key.equals("phpsessid") || key.equals("aspsessionid")
					|| key.startsWith("utm_")) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append('&');
			}
			sb.append(percentEncodeRfc3986(pair.getKey()));
			if (!pair.getValue().isEmpty()) {
				sb.append('=');
				sb.append(percentEncodeRfc3986(pair.getValue()));
			}
		}
		return sb.toString();
	}

	/**
	 * Percent-encode values according the RFC 3986. The built-in Java URLEncoder does not encode
	 * according to the RFC, so we make the extra replacements.
	 * 
	 * @param string Decoded string.
	 * @return Encoded string per RFC 3986.
	 */
	private static String percentEncodeRfc3986(String string) {
		try {
			string = string.replace("+", "%2B");
			string = URLDecoder.decode(string, "UTF-8");
			string = URLEncoder.encode(string, "UTF-8");
			return string.replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
		} catch (Exception e) {
			return string;
		}
	}

	/**
	 * Normalize path.
	 * 
	 * @param path the path
	 * @return the string
	 */
	private static String normalizePath(final String path) {
		return path.replace("%7E", "~").replace(" ", "%20");
	}

	/**
	 * The main method. NOTE: TEST Code...
	 * 
	 * @param args the arguments
	 * @throws URISyntaxException the uRI syntax exception
	 */
	public static void main(String args[]) throws URISyntaxException {
		// TODO: Test code - to delete
		BasicConfigurator.configure();
		String url = "java.sun.com/a/b/../j2se/1.3/./docs/guide/index.html";
		URI uri = new URI(url);
		System.out.println("URI: " + uri.normalize().toString());
		System.out.println("URL Resolver: " + URLResolver.resolveUrl("", url));
		System.out.println("URL Canonicalizer: " + URLCanonicalizer.getCanonicalURL(url));
	}
}
