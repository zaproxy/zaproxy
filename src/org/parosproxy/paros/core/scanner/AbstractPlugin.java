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
// ZAP: 2011/08/30 Support for scanner levels
// ZAP: 2012/01/02 Separate param and attack
// ZAP: 2012/03/03 Added getLevel(boolean incDefault)
// ZAP: 2102/03/15 Changed the type of the parameter "sb" of the method matchBodyPattern to 
// StringBuilder.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/08/07 Renamed Level to AlertThreshold and added support for AttackStrength


package org.parosproxy.paros.core.scanner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.encoder.Encoder;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;

abstract public class AbstractPlugin implements Plugin, Comparable<Object> {
	
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
    private Logger log = Logger.getLogger(this.getClass());
    private Configuration config = null;
    
    // ZAP Added delayInMs
    private int delayInMs;
    private ExtensionAntiCSRF extAntiCSRF = null;
	private Encoder encoder = new Encoder();
	private AlertThreshold defaultAttackThreshold = AlertThreshold.MEDIUM;
	private static final AlertThreshold[] alertThresholdsSupported = new AlertThreshold[] { AlertThreshold.MEDIUM };
	private AttackStrength defaultAttackStrength = AttackStrength.MEDIUM;
	private static final AttackStrength[] attackStrengthsSupported = new AttackStrength[] { AttackStrength.MEDIUM };
    
    public AbstractPlugin() {
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Plugin#getId()
     */
    @Override
    abstract public int getId();
    
    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Plugin#getName()
     */
    @Override
    abstract public String getName();
    
    @Override
    public String getCodeName() {
        String result = getClass().getName();
        int pos = getClass().getName().lastIndexOf(".");
        if (pos > -1) {
            result = result.substring(pos+1);
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Plugin#getDependency()
     */
    @Override
    abstract public String[] getDependency();
    
    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Plugin#getDescription()
     */
    @Override
    abstract public String getDescription();
    
    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Plugin#getCategory()
     */
    @Override
    abstract public int getCategory();
    
    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Plugin#getSolution()
     */
    @Override
    abstract public String getSolution();
    
    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Plugin#getReference()
     */
    @Override
    abstract public String getReference();
    
    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.Plugin#init(org.parosproxy.paros.network.HttpMessage, org.parosproxy.paros.core.scanner.HostProcess)
     */
    @Override
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
        sendAndReceive(msg, true);
    }
    
    protected void sendAndReceive(HttpMessage msg, boolean isFollowRedirect) throws HttpException, IOException {
        sendAndReceive(msg, isFollowRedirect, true);
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
    protected void sendAndReceive(HttpMessage msg, boolean isFollowRedirect, boolean handleAntiCSRF) throws HttpException, IOException {
    	
    	if (parent.handleAntiCsrfTokens() && handleAntiCSRF) {
	    	if (extAntiCSRF == null) {
	    		extAntiCSRF = (ExtensionAntiCSRF) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAntiCSRF.NAME);
	    	}
			List<AntiCsrfToken> tokens = extAntiCSRF.getTokens(msg);
			AntiCsrfToken antiCsrfToken = null;
			if (tokens.size() > 0) {
				antiCsrfToken = tokens.get(0);
			}
			
			if (antiCsrfToken != null) {
				regenerateAntiCsrfToken(msg, antiCsrfToken);
			}
        }

		// always get the fresh copy
		msg.getRequestHeader().setHeader(HttpHeader.IF_MODIFIED_SINCE, null);
		msg.getRequestHeader().setHeader(HttpHeader.IF_NONE_MATCH, null);
		msg.getRequestHeader().setContentLength(msg.getRequestBody().length());

		if (this.getDelayInMs() > 0) {
			try {
				Thread.sleep(this.getDelayInMs());
			} catch (InterruptedException e) {
				// Ignore
			}
		}

        parent.getHttpSender().sendAndReceive(msg, isFollowRedirect);
        // ZAP: Notify parent
        parent.notifyNewMessage(msg);
        return;

    }
    
    private void regenerateAntiCsrfToken(HttpMessage msg, AntiCsrfToken antiCsrfToken) {
    	if (antiCsrfToken == null) {
    		return;
    	}
		String tokenValue = null;
		try {
			HttpMessage tokenMsg = antiCsrfToken.getMsg().cloneAll();

			// Ensure we dont loop
	        sendAndReceive(tokenMsg, true, false);

			tokenValue = extAntiCSRF.getTokenValue(tokenMsg, antiCsrfToken.getName());

	    } catch (Exception e) {
	        log.error(e.getMessage(), e);
		}
	    if (tokenValue != null) {
	    	// Replace token value - only supported in the body right now
	    	log.debug("regenerateAntiCsrfToken replacing " + antiCsrfToken.getValue() + " with " + encoder.getURLEncode(tokenValue));
	    	String replaced = msg.getRequestBody().toString();
	    	replaced = replaced.replace(encoder.getURLEncode(antiCsrfToken.getValue()), encoder.getURLEncode(tokenValue));
	    	msg.setRequestBody(replaced);
	    	extAntiCSRF.registerAntiCsrfToken(new AntiCsrfToken(msg, antiCsrfToken.getName(), tokenValue));
	    }
    }

    @Override
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
    @Override
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
	protected void bingo(int risk, int reliability, String uri, String param, String attack, String otherInfo, HttpMessage msg) {
	    bingo(risk, reliability, this.getName(), this.getDescription(), uri, param, attack, otherInfo, this.getSolution(), msg);
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
	protected void bingo(int risk, int reliability, String name, String description, String uri, 
			String param, String attack, String otherInfo, String solution, HttpMessage msg) {
		log.debug("New alert pluginid=" + + this.getId() + " " + name + " uri=" + uri);
	    Alert alert = new Alert(this.getId(), risk, reliability, name);
	    if (uri == null || uri.equals("")) {
	        uri = msg.getRequestHeader().getURI().toString();
	    }
	    if (param == null) {
	        param = "";
	    }
	    alert.setDetail(description, uri, param, attack, otherInfo, solution, this.getReference(), msg);
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
    @Override
    public boolean isEnabled() {
        return getProperty("enabled").equals("1");            

    }
    
    @Override
    public boolean isVisible() {
        return true;
    }

    /**
     * Enable this test
     */
    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            setProperty("enabled", "1");
        } else {
            setProperty("enabled", "0");
        }
    }
    
	@Override
	public AlertThreshold getAlertThreshold() {
		return this.getAlertThreshold(false);
	}
	
	@Override
	public AlertThreshold getAlertThreshold(boolean incDefault) {
		AlertThreshold level = null;
		try {
			level = AlertThreshold.valueOf(getProperty("level"));
			//log.debug("getAlertThreshold from configs: " + level.name());
		} catch (Exception e) {
			// Ignore
		}
		if (level == null) {
			if (this.isEnabled()) {
				if (incDefault) {
					level = AlertThreshold.DEFAULT;
				} else {
					level = defaultAttackThreshold;
				}
				//log.debug("getAlertThreshold default: " + level.name());
			} else {
				level = AlertThreshold.OFF;
				//log.debug("getAlertThreshold not enabled: " + level.name());
			}
		} else if (level.equals(AlertThreshold.DEFAULT)) {
			if (incDefault) {
				level = AlertThreshold.DEFAULT;
			} else {
				level = defaultAttackThreshold;
			}
			//log.debug("getAlertThreshold default: " + level.name());
		}
		return level;
	}
	
	@Override
	public void setAlertThreshold(AlertThreshold level) {
		setProperty("level", level.name());
	}

	@Override
	public void setDefaultAlertThreshold(AlertThreshold level) {
		this.defaultAttackThreshold = level;
	}
	
	/**
	 * Override this if you plugin supports other levels.
	 */
	@Override
	public AlertThreshold[] getAlertThresholdsSupported() {
		return alertThresholdsSupported;
	}

	@Override
	public AttackStrength getAttackStrength(boolean incDefault) {
		AttackStrength level = null;
		try {
			level = AttackStrength.valueOf(getProperty("strength"));
			//log.debug("getAttackStrength from configs: " + level.name());
		} catch (Exception e) {
			// Ignore
		}
		if (level == null) {
			if (incDefault) {
				level = AttackStrength.DEFAULT;
			} else {
				level = this.defaultAttackStrength;
			}
			//log.debug("getAttackStrength default: " + level.name());
		} else if (level.equals(AlertThreshold.DEFAULT)) {
			if (incDefault) {
				level = AttackStrength.DEFAULT;
			} else {
				level = this.defaultAttackStrength;
			}
			//log.debug("getAttackStrength default: " + level.name());
		}
		return level;
	
	}

	@Override
	public AttackStrength getAttackStrength() {
		return this.getAttackStrength(false);
	}
	
	@Override
	public void setAttackStrength (AttackStrength level) {
		setProperty("strength", level.name());
	}
	
	/**
	 * Override this if you plugin supports other levels.
	 */
	@Override
	public AttackStrength[] getAttackStrengthsSupported() {
		return attackStrengthsSupported;
	}


    /**
     * Compare if 2 plugin is the same.
     */
    @Override
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
    
    @Override
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
     * StringBuilder is not null, append the result to the StringBuilder.
     * @param msg
     * @param pattern
     * @param sb
     * @return true if the pattern can be found.
     */
	protected boolean matchBodyPattern(HttpMessage msg, Pattern pattern, StringBuilder sb) { // ZAP: Changed the type of the parameter "sb" to StringBuilder.
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
     * @see org.parosproxy.paros.core.scanner.Plugin#notifyPluginCompleted(org.parosproxy.paros.core.scanner.HostProcess)
     */
    @Override
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
	
	protected Logger getLog() {
	    return log;
	}
	
	public String getProperty(String key) {	    
	    return config.getString("plugins." + "p" + getId() + "." + key);
	}

	public void setProperty(String key, String value) {	    
	    config.setProperty("plugins." + "p" + getId() + "." + key, value);
	}
		
	@Override
	public void setConfig(Configuration config) {
	    this.config = config;
	}
	
	@Override
	public Configuration getConfig() {
	    return config;
	}
	
	/**
	 * Check and create necessary parameter in config file if not already present.
	 *
	 */
	@Override
	public void createParamIfNotExist() {
        if (getProperty("enabled") == null) {
            setProperty("enabled", "1");        
        }
	}
	
	// ZAP Added isDepreciated
	@Override
	public boolean isDepreciated() {
		return false;
	}

	@Override
	public int getDelayInMs() {
		return delayInMs;
	}

	@Override
	public void setDelayInMs(int delayInMs) {
		this.delayInMs = delayInMs;
	}
	
}
