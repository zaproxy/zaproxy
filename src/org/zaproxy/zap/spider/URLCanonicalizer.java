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
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.log4j.Logger;
import org.zaproxy.zap.spider.SpiderParam.HandleParametersOption;

/**
 * The URLCanonicalizer is used for the process of converting an URL into a canonical (normalized) form. See
 * <a href="http://en.wikipedia.org/wiki/URL_normalization">URL Normalization</a> for a reference.
 * <p>
 * Note: some parts of the code are adapted from: <a
 * href="http://stackoverflow.com/a/4057470/405418">stackoverflow</a>
 * 
 * Added support for OData URLs
 */
public final class URLCanonicalizer {

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(URLCanonicalizer.class);

	private static final String HTTP_SCHEME = "http";
	private static final int HTTP_DEFAULT_PORT = 80;

	private static final String HTTPS_SCHEME = "https";
	private static final int HTTPS_DEFAULT_PORT = 443;

	/** The Constant IRRELEVANT_PARAMETERS defining the parameter names which are ignored in the URL. */
	private static final Set<String> IRRELEVANT_PARAMETERS = new HashSet<>(3);
	static {
		IRRELEVANT_PARAMETERS.add("jsessionid");
		IRRELEVANT_PARAMETERS.add("phpsessid");
		IRRELEVANT_PARAMETERS.add("aspsessionid");
	}
	
	/** 
	 *	OData support
	 *	Extract the ID of a resource including the surrounding quote
	 *  First group is the resource_name
	 *  Second group is the ID (quote will be taken as part of the value)
	 */
	private static final Pattern patternResourceIdentifierUnquoted  = Pattern.compile("/([\\w%]*)\\(([\\w']*)\\)");

	/** 
	 * OData support
	 * Detect a section containing a composite IDs 
	 */
	private static final Pattern patternResourceMultipleIdentifier  = Pattern.compile("/[\\w%]*\\((.*)\\)");

	/** 
	 * OData support
	 * Extract the detail of the multiples IDs
	 */
	private static final Pattern patternResourceMultipleIdentifierDetail = Pattern.compile("([\\w%]*)=([\\w']*)");

	
	/**
	 * Private constructor to avoid initialization of object.
	 */
	private URLCanonicalizer(){}

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
	 * Gets the canonical url, starting from a relative or absolute url found in a given context (baseURL).
	 * 
	 * @param url the url string defining the reference
	 * @param baseURL the context in which this url was found
	 * @return the canonical url
	 */
	public static String getCanonicalURL(String url, String baseURL) {

		try {
			/* Build the absolute URL, from the url and the baseURL */
			String resolvedURL = URLResolver.resolveUrl(baseURL == null ? "" : baseURL, url);
			log.debug("Resolved URL: " + resolvedURL);
			URI canonicalURI;
			try {
				canonicalURI = new URI(resolvedURL);
			} catch (Exception e) {
				canonicalURI = new URI(URIUtil.encodeQuery(resolvedURL));
			}

			/* Some checking. */
			if (canonicalURI.getScheme() == null) {
				throw new MalformedURLException("Protocol could not be reliably evaluated from uri: " + canonicalURI
						+ " and base url: " + baseURL);
			}

			if (canonicalURI.getRawAuthority() == null) {
				log.debug("Ignoring URI with no authority (host[\":\"port]): " + canonicalURI);
				return null;
			}

			if (canonicalURI.getHost() == null) {
				throw new MalformedURLException("Host could not be reliably evaluated from: " + canonicalURI);
			}

			/*
			 * Normalize: no empty segments (i.e., "//"), no segments equal to ".", and no segments equal to
			 * ".." that are preceded by a segment not equal to "..".
			 */
			String path = canonicalURI.normalize().getRawPath();

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
			final SortedSet<QueryParameter> params = createSortedParameters(canonicalURI.getRawQuery());
			final String queryString;
			String canonicalParams = canonicalize(params);
			queryString = (canonicalParams.isEmpty() ? "" : "?" + canonicalParams);

			/* Add starting slash if needed */
			if (path.length() == 0) {
				path = "/" + path;
			}

			/* Drop default port: example.com:80 -> example.com */
			int port = canonicalURI.getPort();
			if (isDefaultPort(canonicalURI.getScheme(), port)) {
				port = -1;
			}

			/* Lowercasing protocol and host */
			String protocol = canonicalURI.getScheme().toLowerCase();
			String host = canonicalURI.getHost().toLowerCase();
			String pathAndQueryString = normalizePath(path) + queryString;

			URL result = new URL(protocol, host, port, pathAndQueryString);
			return result.toExternalForm();

		} catch (Exception ex) {
			log.warn("Error while Processing URL in the spidering process (on base " + baseURL + "): "
					+ ex.getMessage());
			return null;
		}
	}

	/**
	 * Tells whether or not the given port is the default for the given scheme.
	 * <p>
	 * <strong>Note:</strong> Only HTTP and HTTPS schemes are taken into account.
	 *
	 * @param scheme the scheme
	 * @param port the port
	 * @return {@code true} if given the port is the default port for the given scheme, {@code false} otherwise.
	 */
	private static boolean isDefaultPort(String scheme, int port) {
		return HTTP_SCHEME.equalsIgnoreCase(scheme) && port == HTTP_DEFAULT_PORT
				|| HTTPS_SCHEME.equalsIgnoreCase(scheme) && port == HTTPS_DEFAULT_PORT;
	}

	/**
	 * Builds a String representation of the URI with cleaned parameters, that can be used when checking if an
	 * URI was already visited. The URI provided as a parameter should be already cleaned and canonicalized,
	 * so it should be build with a result from {@link #getCanonicalURL(String)}.
	 * 
	 * <p>
	 * When building the URI representation, the same format should be used for all the cases, as it may
	 * affect the number of times the pages are visited and reported if the option HandleParametersOption is
	 * changed while the spider is running.
	 * </p>
	 * 
	 * @param uri the uri
	 * @param handleParameters the handle parameters option
	 * @param handleODataParametersVisited Should we handle specific OData parameters
	 * @return the string representation of the URI
	 * @throws URIException the URI exception
	 */
	public static String buildCleanedParametersURIRepresentation(org.apache.commons.httpclient.URI uri,
			SpiderParam.HandleParametersOption handleParameters, boolean handleODataParametersVisited) throws URIException {
		// If the option is set to use all the information, just use the default string representation
		if (handleParameters.equals(HandleParametersOption.USE_ALL)) {
			return uri.toString();
		}

		// If the option is set to ignore parameters completely, ignore the query completely
		if (handleParameters.equals(HandleParametersOption.IGNORE_COMPLETELY)) {
			return createBaseUriWithCleanedPath(uri, handleParameters, handleODataParametersVisited);
		}

		// If the option is set to ignore the value, we get the parameters and we only add their name to the
		// query
		if (handleParameters.equals(HandleParametersOption.IGNORE_VALUE)) {
			StringBuilder retVal = new StringBuilder(
					createBaseUriWithCleanedPath(uri, handleParameters, handleODataParametersVisited));
			
			String cleanedQuery = getCleanedQuery(uri.getEscapedQuery());
			
			// Add the parameters' names to the uri representation. 
			if(cleanedQuery.length()>0) {
				retVal.append('?').append(cleanedQuery);
			}

			return retVal.toString();
		}

		// Should not be reached
		return uri.toString();
	}

    private static String createBaseUriWithCleanedPath(
            org.apache.commons.httpclient.URI uri,
            HandleParametersOption handleParameters,
            boolean handleODataParametersVisited) throws URIException {
        StringBuilder uriBuilder = new StringBuilder(createBaseUri(uri));

        uriBuilder.append(getCleanedPath(uri.getEscapedPath(), handleParameters, handleODataParametersVisited));

        return uriBuilder.toString();
    }

    private static String createBaseUri(org.apache.commons.httpclient.URI uri) throws URIException {
        StringBuilder baseUriBuilder = new StringBuilder();
        baseUriBuilder.append(uri.getScheme()).append("://").append(uri.getHost());
        if (uri.getPort() != -1) {
            baseUriBuilder.append(':').append(uri.getPort());
        }
        return baseUriBuilder.toString();
    }

    private static String getCleanedPath(
            String escapedPath,
            HandleParametersOption handleParameters,
            boolean handleODataParametersVisited) {
        if (escapedPath == null) {
            return "";
        }

        String cleanedPath;
        if (handleODataParametersVisited) {
            cleanedPath = cleanODataPath(escapedPath, handleParameters);
        } else {
            cleanedPath = escapedPath;
        }

        return cleanedPath;
    }

    private static String getCleanedQuery(String escapedQuery) {
        // Get the parameters' names
        SortedSet<QueryParameter> params = createSortedParameters(escapedQuery);
        Set<String> parameterNames = new HashSet<>();
        StringBuilder cleanedQueryBuilder = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            for (QueryParameter parameter : params) {
                String name = parameter.getName();
                if (parameterNames.contains(name)) {
                    continue;
                }
                parameterNames.add(name);
                // Ignore irrelevant parameters
                if (IRRELEVANT_PARAMETERS.contains(name) || name.startsWith("utm_")) {
                    continue;
                }
                if (cleanedQueryBuilder.length() > 0) {
                    cleanedQueryBuilder.append('&');
                }
                cleanedQueryBuilder.append(name);
            }
        }

        return cleanedQueryBuilder.toString();
    }

	/**
	 * Clean the path in the case of an OData Uri containing a resource identifier (simple or multiple)
	 * 
	 * @param path The path to clean
	 * @param handleParameters tThe cleaning mode
	 * @return A cleaned path
	 */
	private static String cleanODataPath(String path, HandleParametersOption handleParameters) {
		String cleanedPath = path;
		
		if (HandleParametersOption.USE_ALL.equals(handleParameters) ) {
			cleanedPath = path;
		} else {

			// check for single ID (unnamed)
			Matcher matcher = patternResourceIdentifierUnquoted.matcher(path);
			if (matcher.find()) {
				String resourceName =  matcher.group(1); 
				String resourceID   =  matcher.group(2);
			
				String subString = resourceName + "(" + resourceID + ")";
				int begin = path.indexOf(subString);
				int end   = begin + subString.length();
				
				String beforeSubstring = path.substring(0,begin);
				String afterSubstring  = path.substring(end);
				
	
				if (HandleParametersOption.IGNORE_COMPLETELY.equals(handleParameters) ||
				    HandleParametersOption.IGNORE_VALUE.equals(handleParameters)	     ) {
					
					StringBuilder sb = new StringBuilder(beforeSubstring);
					sb.append(resourceName).append("()").append(afterSubstring);
					cleanedPath = sb.toString();
				} 
							
			} else {
				
				matcher = patternResourceMultipleIdentifier.matcher(path);
				if (matcher.find()) {
					// We've found a composite identifier. i.e: /Resource(field1=a,field2=3)
					
					String multipleIdentifierSection =   matcher.group(1); 
					
					int begin = path.indexOf(multipleIdentifierSection);
					int end   = begin + multipleIdentifierSection.length();
	
					String beforeSubstring = path.substring(0,begin);
					String afterSubstring  = path.substring(end);

					if (HandleParametersOption.IGNORE_COMPLETELY.equals(handleParameters) ) {
							cleanedPath = beforeSubstring + afterSubstring;
					} else {
						StringBuilder sb = new StringBuilder(beforeSubstring);
						
						matcher = patternResourceMultipleIdentifierDetail.matcher(multipleIdentifierSection);
						int i = 1;
						while (matcher.find()) {
							
							if (i >  1) {
								sb.append(',');
							}
							String paramName       = matcher.group(1);
							sb.append(paramName);
							i++;
						}
					
						sb.append(afterSubstring);
						cleanedPath = sb.toString();
					}
							
				} 
			}		
		}
		
		return cleanedPath;
	}

	/**
	 * Creates a sorted set with all the parameters from the given {@code query}, ordered lexicographically by name and value.
	 * 
	 * @param queryString the query string
	 * @return a sorted set with all parameters, or {@code null} if the query string is {@code null} or empty.
	 */
	private static SortedSet<QueryParameter> createSortedParameters(final String queryString) {
		if (queryString == null || queryString.isEmpty()) {
			return null;
		}

		final String[] pairs = queryString.split("&");
		final SortedSet<QueryParameter> params = new TreeSet<>();

		for (final String pair : pairs) {
			if (pair.length() == 0) {
				continue;
			}

			String[] tokens = pair.split("=", 2);
			switch (tokens.length) {
			case 1:
				if (pair.charAt(0) == '=') {
					params.add(new QueryParameter("", tokens[0]));
				} else {
					params.add(new QueryParameter(tokens[0], ""));
				}
				break;
			case 2:
				params.add(new QueryParameter(tokens[0], tokens[1]));
				break;
			}
		}
		return params;
	}

	/**
	 * Canonicalize the query string.
	 * 
	 * @param sortedParameters Parameter name-value pairs in lexicographical order.
	 * @return Canonical form of query string.
	 */
	private static String canonicalize(final SortedSet<QueryParameter> sortedParameters) {
		if (sortedParameters == null || sortedParameters.isEmpty()) {
			return "";
		}

		final StringBuilder sb = new StringBuilder(100);
		for (QueryParameter parameter : sortedParameters) {
			final String name = parameter.getName().toLowerCase();
			// Ignore irrelevant parameters
			if (IRRELEVANT_PARAMETERS.contains(name) || name.startsWith("utm_")) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append('&');
			}
			sb.append(parameter.getName());
			if (!parameter.getValue().isEmpty()) {
				sb.append('=');
				sb.append(parameter.getValue());
			}
		}
		return sb.toString();
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
	 * A query parameter, with non-{@code null} name and value.
	 * <p>
	 * The query parameters are ordered by name and value.
	 */
	private static class QueryParameter implements Comparable<QueryParameter> {

		private final String name;
		private final String value;

		public QueryParameter(String name, String value) {
			if (name == null) {
				throw new IllegalArgumentException("Parameter name must not be null.");
			}
			if (value == null) {
				throw new IllegalArgumentException("Parameter value must not be null.");
			}
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		@Override
		public int compareTo(QueryParameter other) {
			if (other == null) {
				return 1;
			}
			int result = name.compareTo(other.name);
			if (result != 0) {
				return result;
			}
			return value.compareTo(other.value);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + name.hashCode();
			result = prime * result + value.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			QueryParameter other = (QueryParameter) obj;
			if (!name.equals(other.name)) {
				return false;
			}
			if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}
	}
}
