/*
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
// ZAP: 2013/02/12 New class 
package org.parosproxy.paros.core.scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;
/**
 * Specialized variant able to handles OData URIs for the resource ID part
 * 
 * Example of query having a single unnamed id:
 * http://services.odata.org/OData/OData.svc/Category(1)/Products?$top=2&$orderby=name
 *  
 * Example of query having a composite (named) id:
 * http://services.odata.org/OData/OData.svc/DisplayItem(key1=2L,key2='B0EB1CA')
 *  
 * Reference: http://www.odata.org/documentation/uri-conventions
 * 
 */
public class VariantODataIdQuery implements Variant {

    private static Logger log = Logger.getLogger(VariantODataIdQuery.class);
    
    /** In order to identify the unnamed id we add this prefix to the resource name **/ 
    public static final String RESOURCE_ID_PREFIX = "__ID__";
    
    /** 
     * It's optional to have a resource parameter
     * Set it to null of there is no such parameter in the URI
     */
    private ResourceParameter resourceParameter = null ;

	// Extract the ID of a resource including the surrounding quote
	// First group is the resource_name
	// Second group is the ID (quote will be taken as part of the value)
	private static final Pattern patternResourceIdentifierUnquoted  = Pattern.compile("\\/(\\w*)\\(([\\w\\']*)\\)");
	
	// Detect a section containing a composite IDs
	private static final Pattern patternResourceMultipleIdentifier  = Pattern.compile("\\/\\w*\\((.*)\\)");

	// Extract the detail of the multiples IDs
	private static final Pattern patternResourceMultipleIdentifierDetail = Pattern.compile("(\\w*)=([\\w\\']*)");

	// Not very clean, should be improved.
	// Save part of the URI before and after the section containing the composite IDs
	private String beforeMultipleIDs = null;
	private String afterMultipleIDs  = null;
	
	// Store the composite IDs if any
	private List<NameValuePair> listParams =null;
	
	
	@Override
	public void setMessage(HttpMessage msg) {
		URI uri = msg.getRequestHeader().getURI();
		parse(uri);
	}

	private void parse(URI uri) {
		try {
			resourceParameter = null;
			
			beforeMultipleIDs = null;
			afterMultipleIDs  = null;
			listParams        = null;

			String uriAsStr = uri.getURI();
				
			// Detection of the resource and resource id (if any)
			
			String resourceName = "";
			String resourceID   ;
			
			// check for single ID (unnamed)
			Matcher matcher = patternResourceIdentifierUnquoted.matcher(uriAsStr);
			if (matcher.find()) {
				resourceName =  matcher.group(1); 
				resourceID   =  matcher.group(2);
			
				String subString = resourceName + "(" + resourceID + ")";
				int begin = uriAsStr.indexOf(subString);
				int end   = begin + subString.length();
				
				String beforeSubstring = uriAsStr.substring(0,begin);
				String afterSubstring  = uriAsStr.substring(end);
				
				resourceParameter = new ResourceParameter(resourceName, resourceID, beforeSubstring, afterSubstring );
											
			} else {
				
				matcher = patternResourceMultipleIdentifier.matcher(uriAsStr);
				if (matcher.find()) {
					// We've found a composite identifier. i.e: /Resource(field1=a,field2=3)
					
					String multipleIdentifierSection =   matcher.group(1); 
					
					int begin = uriAsStr.indexOf(multipleIdentifierSection);
					int end   = begin + multipleIdentifierSection.length();

					beforeMultipleIDs = uriAsStr.substring(0,begin);
					afterMultipleIDs  = uriAsStr.substring(end);

					listParams = new ArrayList<>();

					matcher = patternResourceMultipleIdentifierDetail.matcher(multipleIdentifierSection);
					int i = 1;
					while (matcher.find()) {
						
						String paramName       = matcher.group(1);
						String value           = matcher.group(2);
					
						NameValuePair vp = new NameValuePair(paramName,value,i++);
						listParams.add(vp);
					}
							
				} 
			}
			
		
		} catch (URIException e) {
			log.error(e.getMessage() + uri, e);
		}
		
	}	

	

	@Override
	public Vector<NameValuePair> getParamList() {
		Vector<NameValuePair> params = new Vector<>();
		
		if (resourceParameter != null) {
			params.add(new NameValuePair(resourceParameter.getParameterName(), resourceParameter.getValue(), 1));
		}
		
		if (listParams != null) {
			for (NameValuePair nv: listParams) {
				params.add(nv);
			}
		}
		
		
		return params;
	}

	@Override
	public String setParameter(HttpMessage msg, NameValuePair originalPair,	String param, String value) {
		// TODO: Implement correctly escaped vs. non-escaped params 

		// Check if the parameter is a resource parameter
		
		if (resourceParameter != null && resourceParameter.getParameterName().equals(param)) {
			
			String query = resourceParameter.getQuery(value);
			String uriAsStr = resourceParameter.getModifiedURI(value);
			
			try {
				msg.getRequestHeader().setURI(new URI(uriAsStr));
			} catch (URIException e) {
				throw new RuntimeException("Error with uri "+uriAsStr,e);
			} catch (NullPointerException e) {
				throw new RuntimeException("Error with uri "+uriAsStr,e);
			}
			
			return query;
			
		} else if (listParams != null) {
			// Check for composite ID
			
			StringBuilder sb      = new StringBuilder();
			StringBuilder sbQuery = new StringBuilder();

		    sb.append(beforeMultipleIDs);
		    
		    boolean firstPass = true;
			for (NameValuePair nv: listParams) {
				if (firstPass) {
					firstPass=false;
				} else {
					sbQuery.append(",");
				}
				sbQuery.append(nv.getName()).append("=");
				
				if (nv.getName().equals(param)) {
					sbQuery.append(value);
				} else {
					sbQuery.append(nv.getValue());
				}
			}
			
			sb.append(sbQuery);
			sb.append(afterMultipleIDs);
			
			String uriAsStr = sb.toString();
			String query    = sbQuery.toString();
			
			try {
				msg.getRequestHeader().setURI(new URI(uriAsStr));
			} catch (URIException e) {
				throw new RuntimeException("Error with uri "+uriAsStr,e);
			} catch (NullPointerException e) {
				throw new RuntimeException("Error with uri "+uriAsStr,e);
			}
			
			return query;
	
		}

		return "";
	}

	@Override
	public String setEscapedParameter(HttpMessage msg,	NameValuePair originalPair, String param, String value) {
		// TODO: Implement correctly escaped vs. non-escaped params 
		return setParameter(msg,originalPair,param,value);
	}

	/**
	 * Store the ID of a resource and related data
	 */
	static class ResourceParameter {

		private String parameterName;
		private String resourceName;
		private String originalValue;
		private String uriBeforeParameter;
		private String uriAfterParamter; 
		
		/**
		 * @param parameterName
		 * @param originalValue
		 * @param uriBeforeParameter
		 * @param uriAfterParamter
		 */
		public ResourceParameter(String resourceName, String originalValue, String uriBeforeParameter,	String uriAfterParamter) {
			super();
			
			this.resourceName = resourceName;
			this.parameterName = RESOURCE_ID_PREFIX + resourceName ;
			this.originalValue = originalValue;
			this.uriBeforeParameter = uriBeforeParameter;
			this.uriAfterParamter = uriAfterParamter;
		}

		/**
		 * @return
		 */
		public String getValue() {
			return this.originalValue;
		}

		public String getParameterName() {
			return this.parameterName;
		}
		
		public String getQuery(String newIdValue) {
			return newIdValue;
		}
		
		public String getModifiedURI(String newIdValue) {
			StringBuilder builder = new StringBuilder();
			builder.append(this.uriBeforeParameter)
			       .append(this.resourceName)
			       .append("(")
			       .append(getQuery(newIdValue))
			       .append(")")
			       .append(this.uriAfterParamter);
			return builder.toString();
		}
		
	}
	
}
