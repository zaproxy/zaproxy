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
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method.
// ZAP: 2012/06/07 Added targetParam options
// ZAP: 2012/07/30 Issue 43: Added support for Scope

package org.parosproxy.paros.core.scanner;

import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.core.scanner.Plugin.Level;

public class ScannerParam extends AbstractParam {

	private static final String HOST_PER_SCAN = "scanner.hostPerScan";
	private static final String THREAD_PER_HOST = "scanner.threadPerHost";
	// ZAP: Added support for delayInMs
	private static final String DELAY_IN_MS = "scanner.delayInMs";
	private static final String HANDLE_ANTI_CSRF_TOKENS = "scanner.antiCSFR";
	private static final String LEVEL = "scanner.level";
	/* Work in progress
	private static final String TARGET_PARAMS_URL = "scanner.params.url";
	private static final String TARGET_PARAMS_FORM = "scanner.params.form";
	private static final String TARGET_PARAMS_COOKIE = "scanner.params.cookie";
	*/
		
	private int hostPerScan = 2;
	private int threadPerHost = 1;
	private int delayInMs = 0;
	private boolean handleAntiCSRFTokens = false;
	private Plugin.Level level = Level.MEDIUM; 
	/* Work in progress
	private boolean targetParamsUrl = true;
	private boolean targetParamsForm = true;
	private boolean targetParamsCookie = true;
	*/

    /**
     * @param rootElementName
     */
    public ScannerParam() {
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.common.FileXML#parse()
     */
    @Override
    protected void parse(){
        
		try {
			this.threadPerHost = getConfig().getInt(THREAD_PER_HOST, 1);
		} catch (Exception e) {}
		try {
			this.hostPerScan = getConfig().getInt(HOST_PER_SCAN, 2);
		} catch (Exception e) {}
		try {
			this.delayInMs = getConfig().getInt(DELAY_IN_MS, 0);
		} catch (Exception e) {}
		try {
			this.handleAntiCSRFTokens = getConfig().getBoolean(HANDLE_ANTI_CSRF_TOKENS, false);
		} catch (Exception e) {}
		try {
			this.level = Level.valueOf(getConfig().getString(LEVEL, Level.MEDIUM.name()));
		} catch (Exception e) {}
		/* Work in progress
		try {
			this.targetParamsUrl = getConfig().getBoolean(TARGET_PARAMS_URL, true);
		} catch (Exception e) {}
		try {
			this.targetParamsForm = getConfig().getBoolean(TARGET_PARAMS_FORM, true);
		} catch (Exception e) {}
		try {
			this.targetParamsCookie = getConfig().getBoolean(TARGET_PARAMS_COOKIE, true);
		} catch (Exception e) {}
		*/

    }

    public int getThreadPerHost() {
        return threadPerHost;
    }
    
    public void setThreadPerHost(int threadPerHost) {
        this.threadPerHost = threadPerHost;
        getConfig().setProperty(THREAD_PER_HOST, Integer.toString(this.threadPerHost));

    }

    /**
     * @return Returns the thread.
     */
    public int getHostPerScan() {
        return hostPerScan;
    }
    /**
     * @param thread The thread to set.
     */
    public void setHostPerScan(int hostPerScan) {
        this.hostPerScan = hostPerScan;
		getConfig().setProperty(HOST_PER_SCAN, Integer.toString(this.hostPerScan));

    }

	public void setDelayInMs(int delayInMs) {
		this.delayInMs = delayInMs;
		getConfig().setProperty(DELAY_IN_MS, Integer.toString(this.delayInMs));
	}

	public int getDelayInMs() {
		return delayInMs;
	}

	public boolean getHandleAntiCSRFTokens() {
		return handleAntiCSRFTokens;
	}

	public void setHandleAntiCSRFTokens(boolean handleAntiCSRFTokens) {
		this.handleAntiCSRFTokens = handleAntiCSRFTokens;
		getConfig().setProperty(HANDLE_ANTI_CSRF_TOKENS, handleAntiCSRFTokens);
	}
	
    public Plugin.Level getLevel() {
		return level;
	}

    public void setLevel(Plugin.Level level) {
		this.level = level;
		getConfig().setProperty(LEVEL, level.name());
	}

    public void setLevel(String level) {
		this.setLevel(Level.valueOf(level));
	}

	/* Work in progress
	public boolean isTargetParamsUrl() {
		return targetParamsUrl;
	}

	public void setTargetParamsUrl(boolean targetParamsUrl) {
		this.targetParamsUrl = targetParamsUrl;
		getConfig().setProperty(TARGET_PARAMS_URL, this.targetParamsUrl);
	}

	public boolean isTargetParamsForm() {
		return targetParamsForm;
	}

	public void setTargetParamsForm(boolean targetParamsForm) {
		this.targetParamsForm = targetParamsForm;
		getConfig().setProperty(TARGET_PARAMS_FORM, this.targetParamsForm);
	}

	public boolean isTargetParamsCookie() {
		return targetParamsCookie;
	}

	public void setTargetParamsCookie(boolean targetParamsCookie) {
		this.targetParamsCookie = targetParamsCookie;
		getConfig().setProperty(TARGET_PARAMS_COOKIE, this.targetParamsCookie);
	}
	*/

}
