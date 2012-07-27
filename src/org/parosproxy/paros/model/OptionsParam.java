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
// ZAP: 2011/10/29 Support for parameters
// ZAP: 2011/11/02 Added brute force options
// ZAP: 2011/11/15 Removed the getConfig method and the config field, now it's
// used the method of the base class.
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2012/04/25 Added type argument to generic type.
// ZAP: 2012/05/03 Changed the type of one variable in the method getParamSet.
// ZAP: 2012/06/29 Added OptionsWebSocketParam.
// ZAP: 2012/06/30 Added the instance variable databaseParam and the  method
// getDatabaseParam() and changed the method parse() to also load the database
// configurations.

package org.parosproxy.paros.model;

import java.io.File;
import java.util.Vector;

import org.apache.commons.configuration.ConfigurationException;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.core.proxy.ProxyParam;
import org.parosproxy.paros.extension.option.DatabaseParam;
import org.parosproxy.paros.extension.option.OptionsParamCertificate;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.network.ConnectionParam;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfParam;
import org.zaproxy.zap.extension.api.OptionsParamApi;
import org.zaproxy.zap.extension.bruteforce.BruteForceParam;
import org.zaproxy.zap.extension.invoke.InvokeParam;
import org.zaproxy.zap.extension.option.OptionsParamCheckForUpdates;
import org.zaproxy.zap.extension.session.SessionParam;
import org.zaproxy.zap.extension.websocket.ui.OptionsWebSocketParam;

import ch.csnc.extension.util.OptionsParamExperimentalSliSupport;



/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OptionsParam extends AbstractParam {
	
//	private static final String ROOT = "Options";
	// ZAP: User directory now stored in the config file
	private static final String USER_DIR = "userDir";
	
	private ProxyParam proxyParam = new ProxyParam();
	private ConnectionParam connectionParam = new ConnectionParam();
	private OptionsParamView viewParam = new OptionsParamView();
	private OptionsParamCertificate certificateParam = new OptionsParamCertificate();
	// ZAP: Added OptionsParamCheckForUpdates, InvokeParam
	private OptionsParamCheckForUpdates checkForUpdatesParam = new OptionsParamCheckForUpdates();
	private InvokeParam invokeParam = new InvokeParam();
	// ZAP: Added OptionsWebSocketParam
	private OptionsWebSocketParam websocketParam = new OptionsWebSocketParam();
	private AntiCsrfParam antiCsrfParam = new AntiCsrfParam();
	private OptionsParamApi apiParam = new OptionsParamApi();
	private BruteForceParam bruteForceParam = new BruteForceParam();
	private OptionsParamExperimentalSliSupport experimentalFeatuesParam = new OptionsParamExperimentalSliSupport();
	private SessionParam sessionParam = new SessionParam();
    /**
     * The database configurations.
     */
	// ZAP: Added the instance variable.
    private DatabaseParam databaseParam = new DatabaseParam();

	private Vector<AbstractParam> paramSetList = new Vector<AbstractParam>();
	private boolean gui = true;
	private File userDirectory = null;
	
	public OptionsParam() {	
	}
	
    /**
     * @return Returns the connectionParam.
     */
    public ConnectionParam getConnectionParam() {
        return connectionParam;
    }
    
	/**
	 * @return Returns the proxyParam.
	 */
	public ProxyParam getProxyParam() {
		return proxyParam;
	}
	
	/**
	 * @param proxyParam The proxyParam to set.
	 */
	public void setProxyParam(ProxyParam proxyParam) {
		this.proxyParam = proxyParam;
	}

    /**
     * @param connectionParam The connectionParam to set.
     */
    public void setConnectionParam(ConnectionParam connectionParam) {
        this.connectionParam = connectionParam;
    }
    
    /**
     * @param viewParam The viewParam to set.
     */
    public void setViewParam(OptionsParamView viewParam) {
        this.viewParam = viewParam;
    }
    
    /**
     * @return Returns the viewParam.
     */
    public OptionsParamView getViewParam() {
        return viewParam;
    }

    /**
     * @return Returns the viewParam.
     */
    public OptionsParamCheckForUpdates getCheckForUpdatesParam() {
        return checkForUpdatesParam;
    }

    /**
     * @param certificateParam The certificateParam to set.
     */
    public void setCertificateParam(OptionsParamCertificate certificateParam) {
        this.certificateParam = certificateParam;
    }
    
    /**
     * @return Returns the certificateParam.
     */
    public OptionsParamCertificate getCertificateParam() {
        return certificateParam;
    }
    
     public void addParamSet(AbstractParam paramSet) {
        paramSetList.add(paramSet);
	    paramSet.load(getConfig());
    }
    
    // ZAP: Added type argument.
    public AbstractParam getParamSet(Class<? extends AbstractParam> className) {
       
        AbstractParam result = null;
        for (int i=0; i<paramSetList.size(); i++) {
            // ZAP: Changed the type to AbstractParam and renamed to
            // abstractParam. 
            AbstractParam abstractParam = paramSetList.get(i);
            if (abstractParam.getClass().equals(className)) {
                try {
                    // ZAP: Removed the cast.
                    result = abstractParam;
                    break;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    
    // ZAP: Removed the method getConfig().

    /* (non-Javadoc)
     * @see org.parosproxy.paros.common.AbstractParam#parse()
     */
    @Override
    protected void parse() {
		getConnectionParam().load(getConfig());
	    getProxyParam().load(getConfig());
		getCertificateParam().load(getConfig());
		getViewParam().load(getConfig());
		getCheckForUpdatesParam().load(getConfig());
		getInvokeParam().load(getConfig());
		getAntiCsrfParam().load(getConfig());
		getApiParam().load(getConfig());
		getBruteForceParam().load(getConfig());
		getExperimentalFeaturesParam().load(getConfig());
		getSessionParam().load(getConfig());
        // ZAP: Added the statement.
        getWebSocketParam().load(getConfig());
		// ZAP: Added the statement.
        getDatabaseParam().load(getConfig());
		
		String userDir = null;
		try {
			userDir = getConfig().getString(USER_DIR);
			if (userDir != null) {
				this.userDirectory = new File(userDir);
			}
		} catch (Exception e) {
			// In a previous release the userdir was set as a file
			try {
				File file = (File) getConfig().getProperty(USER_DIR);
				if (file != null && file.isDirectory()) {
					this.userDirectory = file;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
//		for (int i=0; i<paramSetList.size(); i++) {
//		    AbstractParam param = (AbstractParam) paramSetList.get(i);
//		    param.load(getConfig());
//		}
    }
    
    public boolean isGUI() {
        return gui;
    }
    
    public void setGUI(boolean gui) {
        this.gui = gui;
    }

    /**
     * @return Returns the currentFolder.
     */
    public File getUserDirectory() {
        return userDirectory;
    }

    /**
     * @param currentFolder The currentFolder to set.
     */
    public void setUserDirectory(File currentDirectory) {
        this.userDirectory = currentDirectory;
    	// ZAP: User directory now stored in the config file
        getConfig().setProperty(USER_DIR, currentDirectory.getAbsolutePath());
        try {
			getConfig().save();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
    }

	public InvokeParam getInvokeParam() {
		return invokeParam;
	}

	public OptionsWebSocketParam getWebSocketParam() {
		return websocketParam;
	}

	public AntiCsrfParam getAntiCsrfParam() {
		return antiCsrfParam;
	}

	public OptionsParamApi getApiParam() {
		return apiParam;
	}
	
	public OptionsParamExperimentalSliSupport getExperimentalFeaturesParam() {
		return experimentalFeatuesParam;
	}

	public SessionParam getSessionParam() {
		return sessionParam;
	}
	
	public BruteForceParam getBruteForceParam() {
		return bruteForceParam;
	}

	public void setBruteForceParam(BruteForceParam bruteForceParam) {
		this.bruteForceParam = bruteForceParam;
	}

    /**
     * Gets the database configurations.
     *
     * @return the database configurations
     */
	// ZAP: Added the method.
    public DatabaseParam getDatabaseParam() {
        return databaseParam;
    }
	
}
