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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Specialized variant able to handles the filter parameters of OData URIs
 * 
 * Example of query:
 * http://services.odata.org/OData/OData.svc/Product?$filter=startswith(name,'Foo') and price lt 10
 *
 * Reference: 
 * http://www.odata.org/documentation/uri-conventions
 * http://msdn.microsoft.com/en-us/library/gg309461.aspx#BKMK_filter
 * 
 * TODO:
 * - Properly handle escaped vs. unescaped parameters
 * - Handle OData functions (startwith, substringof, ...)
 * 
 */
public class VariantODataFilterQuery implements Variant {
	
    private static Logger log = Logger.getLogger(VariantODataFilterQuery.class);

	// Extract the content of the $filter parameter
	private static final Pattern patternFilterParameters  = Pattern.compile("\\$filter\\=([\\w\\s\\(\\)\\', ]*)");

	// Extract the effective parameters from the $filter string
	private static final Pattern patternParameters = Pattern.compile("([\\w]+)\\s+(eq|ne|gt|ge|lt|le|and|or|not)\\s+([\\w\'\\/]+)");

	
	// Store the URI parts located before and after the filter expression 
	private String beforeFilterExpression = null ;
	private String afterFilterExpression  = null ;

	/**
	 * Storage for the operation parameters
	 */
	private Map<String,OperationParameter> mapParameters = null;
	
	@Override
	public void setMessage(HttpMessage msg) {
		URI uri = msg.getRequestHeader().getURI();
		parse(uri);
	}

	private void parse(URI uri) {
		try {
			String uriAsStr = uri.getURI();
				
			// Detection of a filter statement if any
			
			String filterExpression = "";
			
			Matcher matcher = patternFilterParameters.matcher(uriAsStr);
			if (matcher.find()) {
				filterExpression =  matcher.group(1); 
			
				
				int begin = uriAsStr.indexOf(filterExpression);
				int end   = begin + filterExpression.length();
				
				beforeFilterExpression = uriAsStr.substring(0,begin);
				afterFilterExpression  = uriAsStr.substring(end);
							
				// Now scan the expression in order to identify all parameters 
				mapParameters = new HashMap<>();
				
				Matcher matcherParameters = patternParameters.matcher(filterExpression);
				while (matcherParameters.find()){
					
					String nameOpAndValue = matcherParameters.group(0);
					String paramName  = matcherParameters.group(1);
					String operator   = matcherParameters.group(2);
					String paramValue = matcherParameters.group(3); 
					
					begin = filterExpression.indexOf(nameOpAndValue);
					end   = begin + nameOpAndValue.length();

					String before = filterExpression.substring(0,begin);
					String after  = filterExpression.substring(end);
					
					OperationParameter opParam = new OperationParameter(paramName, operator, paramValue, before, after);
					mapParameters.put(opParam.getParameterName(), opParam);
					
				} 
					
			
			}
			else {
				beforeFilterExpression = null;
				afterFilterExpression  = null;
				mapParameters = null;
			}
			
		
		} catch (URIException e) {
			log.error(e.getMessage() + uri, e);
		}
		
	}	

	@Override
	public Vector<NameValuePair> getParamList() {
		Vector<NameValuePair> out = new Vector<>();
		
		int i=1;
		for (OperationParameter opParam:mapParameters.values()){
			out.add(new NameValuePair(opParam.getParameterName(), opParam.getValue(),i++));
		}
		
		return out;
	}

	@Override
	public String setParameter(HttpMessage msg, NameValuePair originalPair, String param, String value) {
		// TODO: Implement correctly escaped / non-escaped params 

		if (mapParameters != null) {
		
			OperationParameter opParam = mapParameters.get(param);
			if (opParam != null) {
				String newfilter = opParam.getModifiedFilter(value);
				String modifiedUri = beforeFilterExpression + newfilter + afterFilterExpression;
			
				try {
					msg.getRequestHeader().setURI(new URI(modifiedUri));
				} catch (URIException e) {
					log.error("Exception with uri "+modifiedUri,e);
				} catch (NullPointerException e) {
					log.error("Exception with uri "+modifiedUri,e);
				}
				return newfilter;
			}
			
			
		}
		
		return null;
	}

	@Override
	public String setEscapedParameter(HttpMessage msg, NameValuePair originalPair, String param, String value) {
		// TODO: Implement correctly escaped / non-escaped params 
		return setParameter(msg,originalPair,param,value);
	}

	
	/**
	 * Store a parameter and related data
	 */
	static class OperationParameter {

		private String paramName;
		private String operator;
		private String originalValue;
		private String stringBeforeOperation;
		private String stringAfterOperation; 
		
		/**
		 * @param parameterName
		 * @param originalValue
		 * @param uriBeforeParameter
		 * @param uriAfterParamter
		 */
		public OperationParameter(String paramName, String operator, String originalValue, String stringBeforeOperation,String stringAfterOperation) {
			super();
			
			this.paramName = paramName;
			this.operator = operator;
			this.originalValue = originalValue;
			this.stringBeforeOperation = stringBeforeOperation;
			this.stringAfterOperation = stringAfterOperation;
		}

		/**
		 * @return
		 */
		public String getValue() {
			return this.originalValue;
		}

		public String getParameterName() {
			return this.paramName;
		}
		
		public String getQuery(String newValue) {
			return newValue;
		}
		
		
		public String getModifiedFilter(String newIdValue) {
			StringBuilder builder = new StringBuilder();
			builder.append(this.stringBeforeOperation)
			       .append(this.paramName)
			       .append(" ")
			       .append(this.operator)
			       .append(" ")
			       .append(newIdValue)
			       .append(this.stringAfterOperation);
			return builder.toString();
		}
		
	} 	
	
	
}
