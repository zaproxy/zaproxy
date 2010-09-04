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
package org.parosproxy.paros.core.scanner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;




/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

abstract public class AbstractPlugin implements Plugin, Comparable {
	
    /**
     * Default pattern used in pattern check for most plugins.
     */
    protected static final int PATTERN_PARAM = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE;
    /**
     * CRLF string.
     */
    protected static final String CRLF = "\r\n";
    
    private HostProcess parent = null;
    private HttpMessage msg = null;
//    private boolean enabled = false;
    private Log log = LogFactory.getLog(this.getClass());
    private Configuration config = null;
    
    public AbstractPlugin() {
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getId()
     */
    abstract public int getId();
    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getName()
     */
    abstract public String getName();
    
    public String getCodeName() {
        String result = getClass().getName();
        int pos = getClass().getName().lastIndexOf(".");
        if (pos > -1) {
            result = result.substring(pos+1);
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getDependency()
     */
    abstract public String[] getDependency();
    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getDescription()
     */
    abstract public String getDescription();
    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getCategory()
     */
    abstract public int getCategory();
    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getSolution()
     */
    abstract public String getSolution();
    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#getReference()
     */
    abstract public String getReference();
    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#init(com.proofsecure.paros.network.HttpMessage, com.proofsecure.paros.core.scanner.HostProcess)
     */
    public void init(HttpMessage msg, HostProcess parent) {
        this.msg = msg.cloneAll();
        this.parent = parent;
        init();
    }
    
    abstract public void init();

    /**
     * Obtain a new HttpMessage with the same request as the base.  The response is empty.
     * This is used by plugin to build/craft a new message to send/receive.  It does not affect
     * the base message. 
     * @return A new HttpMessage with cloned request.  Response is empty.
     */
    protected HttpMessage getNewMsg() {
        return msg.cloneRequest();
    }
    
    /**
     * Get the base reference HttpMessage for this check.  Both request and response is present.
     * It should not be modified during when the plugin runs.
     * @return The base HttpMessage with request/response.
     */
    protected HttpMessage getBaseMsg() {
        return msg;
    }
    
    /**
     * Send and receive a HttpMessage.  msg should have the request header/body set.
     * Fresh copy will always be retrieved via this method.
     * The request header content length will be modified by this method.
     * @param msg
     * @throws HttpException
     * @throws IOException
     */
    protected void sendAndReceive(HttpMessage msg) throws HttpException, IOException {
        // always get the fresh copy
        
        sendAndReceive(msg, true);
//        msg.getRequestHeader().setHeader(HttpHeader.IF_MODIFIED_SINCE, null);
//        msg.getRequestHeader().setHeader(HttpHeader.IF_NONE_MATCH, null);
//        msg.getRequestHeader().setContentLength(msg.getRequestBody().length());
//        parent.getHttpSender().sendAndReceive(msg);
    }
    
    /**
     * Send and receive a HttpMessage.  msg should have the request header/body set.
     * Fresh copy will always be retrieved via this method.
     * The request header content length will be modified by this method.
     * @param msg
     * @param isFollowRedirect follow redirect response
     * @throws HttpException
     * @throws IOException
     */
    protected void sendAndReceive(HttpMessage msg, boolean isFollowRedirect) throws HttpException, IOException {

      // always get the fresh copy
      msg.getRequestHeader().setHeader(HttpHeader.IF_MODIFIED_SINCE, null);
      msg.getRequestHeader().setHeader(HttpHeader.IF_NONE_MATCH, null);
      msg.getRequestHeader().setContentLength(msg.getRequestBody().length());

        parent.getHttpSender().sendAndReceive(msg, isFollowRedirect);
        return;

        // below code moved to httpsender for generic use.

//
//        if (!isFollowRedirect || !
//                (msg.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)
//                 || msg.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.PUT))
//           ) {
//            parent.getHttpSender().sendAndReceive(msg, isFollowRedirect);
//            return;
//        } else {
//            parent.getHttpSender().sendAndReceive(msg, false);
//        }
//
//        HttpMessage temp = msg.cloneAll();
//        // POST/PUT method cannot be redirected by library. Need to follow by code
//        
//		// loop 1 time only because httpclient can handle redirect itself after first GET.
//        for (int i=0; i<1
//				&& (HttpStatusCode.isRedirection(temp.getResponseHeader().getStatusCode())
//				&& temp.getResponseHeader().getStatusCode() != HttpStatusCode.NOT_MODIFIED); i++) {
//			String location = temp.getResponseHeader().getHeader(HttpHeader.LOCATION);
//			URI baseUri = temp.getRequestHeader().getURI();
//			URI newLocation = new URI(baseUri, location, false);
//			temp.getRequestHeader().setURI(newLocation);
//			
//			temp.getRequestHeader().setMethod(HttpRequestHeader.GET);
//			temp.getRequestHeader().setContentLength(0);
//			parent.getHttpSender().sendAndReceive(temp, true);
//		}
//		
//		msg.setResponseHeader(temp.getResponseHeader());
//		msg.setResponseBody(temp.getResponseBody());
    }
    
    public void run() {
        try {
            if (!isStop()) {
                scan();
            }
        } catch (Exception e) {
            getLog().warn(e.getMessage());
        }
        notifyPluginCompleted(getParent());
    }
    
    /**
     * The core scan method to be implmented by subclass.
     */
    abstract public void scan();

    /**
     * Generate an alert when a security issue (risk/info) is found.  Default name, description,
     * solution of this Plugin will be used.
     * @param risk
     * @param reliability
     * @param uri
     * @param param
     * @param otherInfo
     * @param msg
     */
	protected void bingo(int risk, int reliability, String uri, String param, String otherInfo, HttpMessage msg) {
	    bingo(risk, reliability, this.getName(), this.getDescription(), uri, param, otherInfo, this.getSolution(), msg);
	}

	/**
     * Generate an alert when a security issue (risk/info) is found.  Custome alert name,
     * description and solution will be used.
	 * 
	 * @param risk
	 * @param reliability
	 * @param name
	 * @param description
	 * @param uri
	 * @param param
	 * @param otherInfo
	 * @param solution
	 * @param msg
	 */
	protected void bingo(int risk, int reliability, String name, String description, String uri, String param, String otherInfo, String solution, HttpMessage msg) {
	    Alert alert = new Alert(this.getId(), risk, reliability, name);
	    if (uri == null || uri.equals("")) {
	        uri = msg.getRequestHeader().getURI().toString();
	    }
	    if (param == null) {
	        param = "";
	    }
	    alert.setDetail(description, uri, param, otherInfo, solution, this.getReference(), msg);
	    parent.alertFound(alert);
	}

	/**
	 * Check i
	 * @param msg
	 * @return
	 */
	protected boolean isFileExist(HttpMessage msg) {
	    return parent.getAnalyser().isFileExist(msg);	    
	}
	
	/**
	 * Check if this test should be stopped.  It should be checked periodically in Plugin
	 * (eg when in loops) so the HostProcess can stop this Plugin cleanly.
	 * @return
	 */
	protected boolean isStop() {
	    return parent.isStop();
	}
	
	
    /**
     * @return Returns if this test is enabled.
     */
    public boolean isEnabled() {
        return getProperty("enabled").equals("1");            

    }
    
    public boolean isVisible() {
        return true;
    }

    /**
     * Enable this test
     */
    public void setEnabled(boolean enabled) {
        if (enabled) {
            setProperty("enabled", "1");
        } else {
            setProperty("enabled", "0");
        }
    }

    /**
     * Compare if 2 plugin is the same.
     */
    public int compareTo(Object obj) {
        
        int result = -1;
        if (obj instanceof AbstractPlugin) {
            AbstractPlugin test = (AbstractPlugin) obj;
            if (getId() < test.getId()) {
                result = -1;
            } else if (getId() > test.getId()) {
                result = 1;
            } else {
                result = 0;
            }
        }
        return result;
    }
    
    public boolean equals(Object obj) {

        if (compareTo(obj) == 0) {
            return true;
        }
        
        return false;

    }


    /**
     * Check if the given pattern can be found in the header.
     * @param msg
     * @param header name.
     * @param pattern
     * @return true if the pattern can be found.
     */

	protected boolean matchHeaderPattern(HttpMessage msg, String header, Pattern pattern) {
		if (msg.getResponseHeader().isEmpty()) {
		    return false;
		}
		
	    String val = msg.getResponseHeader().getHeader(header);
		if (val == null) {
			return false;
		}
			
		Matcher matcher = pattern.matcher(val);
		return matcher.find();
	}
	
    /**
     * Check if the given pattern can be found in the msg body.  If the supplied
     * StringBuffer is not null, append the result to the StringBuffer.
     * @param msg
     * @param pattern
     * @param sb
     * @return true if the pattern can be found.
     */
	protected boolean matchBodyPattern(HttpMessage msg, Pattern pattern, StringBuffer sb) {
		Matcher matcher = pattern.matcher(msg.getResponseBody().toString());
		boolean result = matcher.find();
		if (result) {
			if (sb != null) {
				sb.append(matcher.group());
			}
		}
		return result;
	}
	
	/**
	 * Write a progress update message.  Currently this just display in System.out
	 * @param msg
	 */
	protected void writeProgress(String msg) {
	    //System.out.println(msg);
	}

	/**
	 * Get the parent HostProcess.
	 * @return
	 */
	protected  HostProcess getParent() {
	    return parent;
	}

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.scanner.Plugin#notifyPluginCompleted(com.proofsecure.paros.core.scanner.HostProcess)
     */
    abstract public void notifyPluginCompleted(HostProcess parent);


	
	/**
	 * Replace body by stripping of pattern string.  The URLencoded and URLdecoded
	 * pattern will also be stripped off.
	 * This is mainly used for stripping off a testing string in HTTP response
	 * for comparison against the original response.
	 * Reference: TestInjectionSQL
	 * @param body
	 * @param pattern
	 * @return
	 */
	protected String stripOff(String body, String pattern) {
	    String urlEncodePattern = getURLEncode(pattern);
	    String urlDecodePattern = getURLDecode(pattern);
	    String htmlEncodePattern1 = getHTMLEncode(pattern);
	    String htmlEncodePattern2 = getHTMLEncode(urlEncodePattern);
	    String htmlEncodePattern3 = getHTMLEncode(urlDecodePattern);
	    String result = body.replaceAll("\\Q" + pattern + "\\E", "").replaceAll("\\Q" + urlEncodePattern + "\\E", "").replaceAll("\\Q" + urlDecodePattern + "\\E", "");
	    result = result.replaceAll("\\Q" + htmlEncodePattern1 + "\\E", "").replaceAll("\\Q" + htmlEncodePattern2 + "\\E", "").replaceAll("\\Q" + htmlEncodePattern3 + "\\E", "");
	    return result;
	}
	
    public static String getURLEncode(String msg) {
        String result = "";
        try {
            result = URLEncoder.encode(msg, "UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
    
	public static String getURLDecode(String msg) {
	    String result = "";
        try {
            result = URLDecoder.decode(msg, "UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
	}
	
	public static String getHTMLEncode(String msg) {
	    String result = msg.replaceAll("<", "&#60;");
	    result = result.replaceAll(">", "&#62;");
	    return result;
	}
	
	protected Kb getKb() {
	    return getParent().getKb();
	}
	
	protected Log getLog() {
	    return log;
	}
	
	public String getProperty(String key) {	    
	    return config.getString("plugins." + "p" + getId() + "." + key);
	}

	public void setProperty(String key, String value) {	    
	    config.setProperty("plugins." + "p" + getId() + "." + key, value);
	}
		
	public void setConfig(Configuration config) {
	    this.config = config;
	}
	
	public Configuration getConfig() {
	    return config;
	}
	
	/**
	 * Check and create necessary parameter in config file if not already present.
	 *
	 */
	public void createParamIfNotExist() {
        if (getProperty("enabled") == null) {
            setProperty("enabled", "1");        
        }
	}
}
