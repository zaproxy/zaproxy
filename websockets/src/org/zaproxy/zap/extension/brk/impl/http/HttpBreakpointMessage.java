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
 */
package org.zaproxy.zap.extension.brk.impl.http;

import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.brk.AbstractBreakPointMessage;
import org.zaproxy.zap.extension.httppanel.Message;

public class HttpBreakpointMessage extends AbstractBreakPointMessage {

    private static final String TYPE = "HTTP";
    
	private String url;
	private Pattern pattern;

	public HttpBreakpointMessage(String url) {
		this.url = url;

		compilePattern();
	}

	@Override
	public String getType() {
	    return TYPE;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		
		compilePattern();
	}

	@Override
	public boolean match(Message aMessage) {
	    if (aMessage instanceof HttpMessage) {
	        HttpMessage messge = (HttpMessage)aMessage;
	        URI uri;
            try {
                uri = (URI) messge.getRequestHeader().getURI().clone();
                uri.setQuery(null);
                return pattern.matcher(uri.getURI()).find();
            } catch (CloneNotSupportedException e1) {
                e1.printStackTrace();
            } catch (URIException e) {
                e.printStackTrace();
            }
	    }
	    
		return false;
	}

	private void compilePattern() {
		String str = url;

		str = str.replaceAll("\\.", "\\\\.");
		str = str.replaceAll("\\*", ".*?").replaceAll("(;+$)|(^;+)", "");
		str = "(" + str.replaceAll(";+", "|") + ")$";

		pattern = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
	}

    @Override
    public String getDisplayMessage() {
        return url;
    }
    
}
