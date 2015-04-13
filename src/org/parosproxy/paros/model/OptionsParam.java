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
// ZAP: 2012/06/30 Added the instance variable databaseParam and the  method
// getDatabaseParam() and changed the method parse() to also load the database
// configurations.
// ZAP: 2012/12/31 Removed brute force options
// ZAP: 2013/01/16 Issue 453: Dynamic loading and unloading of add-ons
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2014-02-04 Added GlobalExcludeURL functionality:  Issue: TODO - insert list here.
// ZAP: 2014/03/23 Issue 1097: Move "Run applications" (invoke) extension to zap-extensions project
// ZAP: 2015/04/09 Generify getParamSet(Class) to avoid unnecessary casts

package org.parosproxy.paros.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.core.proxy.ProxyParam;
import org.parosproxy.paros.extension.option.DatabaseParam;
import org.parosproxy.paros.extension.option.OptionsParamCertificate;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.network.ConnectionParam;
// ZAP: new imports
import org.zaproxy.zap.extension.anticsrf.AntiCsrfParam;
import org.zaproxy.zap.extension.api.OptionsParamApi;
import org.zaproxy.zap.extension.autoupdate.OptionsParamCheckForUpdates;
import org.zaproxy.zap.extension.globalexcludeurl.GlobalExcludeURLParam;

import ch.csnc.extension.util.OptionsParamExperimentalSliSupport;



public class OptionsParam extends AbstractParam {
	
	private static final Logger logger = Logger.getLogger(OptionsParam.class);
	
//	private static final String ROOT = "Options";
	// ZAP: User directory now stored in the config file
	private static final String USER_DIR = "userDir";
	
	private ProxyParam proxyParam = new ProxyParam();
	private ConnectionParam connectionParam = new ConnectionParam();
	private OptionsParamView viewParam = new OptionsParamView();
	private OptionsParamCertificate certificateParam = new OptionsParamCertificate();
	// ZAP: Added many instance variables for new functionality.
	private OptionsParamCheckForUpdates checkForUpdatesParam = new OptionsParamCheckForUpdates();
	private AntiCsrfParam antiCsrfParam = new AntiCsrfParam();
	private OptionsParamApi apiParam = new OptionsParamApi();
	private GlobalExcludeURLParam globalExcludeURLParam = new GlobalExcludeURLParam();
	private OptionsParamExperimentalSliSupport experimentalFeatuesParam = new OptionsParamExperimentalSliSupport();
	
    /**
     * The database configurations.
     */
	// ZAP: Added the instance variable.
    private DatabaseParam databaseParam = new DatabaseParam();

	private Vector<AbstractParam> paramSetList = new Vector<>();
	private Map<Class<? extends AbstractParam>, AbstractParam> abstractParamsMap = new HashMap<>();
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
        abstractParamsMap.put(paramSet.getClass(), paramSet);
	    paramSet.load(getConfig());
    }
     
     public void removeParamSet(AbstractParam paramSet) {
         paramSetList.remove(paramSet);
         abstractParamsMap.remove(paramSet.getClass());
     }
    
    public <T extends AbstractParam> T getParamSet(Class<T> clazz) {
        if (clazz != null) {
            AbstractParam abstractParam = abstractParamsMap.get(clazz);
            if (abstractParam != null) {
                return clazz.cast(abstractParam);
            }
        }
        return null;
    }
    
    // ZAP: Removed the method getConfig().

    @Override
    protected void parse() {
		getConnectionParam().load(getConfig());
	    getProxyParam().load(getConfig());
		getCertificateParam().load(getConfig());
		getViewParam().load(getConfig());
		getCheckForUpdatesParam().load(getConfig());
		getAntiCsrfParam().load(getConfig());
		getApiParam().load(getConfig());
		getGlobalExcludeURLParam().load(getConfig());
		getExperimentalFeaturesParam().load(getConfig());
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
				logger.error(e1.getMessage(), e1);
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
     * @param currentDirectory The currentFolder to set.
     */
    public void setUserDirectory(File currentDirectory) {
        this.userDirectory = currentDirectory;
    	// ZAP: User directory now stored in the config file
        getConfig().setProperty(USER_DIR, currentDirectory.getAbsolutePath());
        try {
			getConfig().save();
		} catch (ConfigurationException e) {
			logger.error(e.getMessage(), e);
		}
    }

	public AntiCsrfParam getAntiCsrfParam() {
		return antiCsrfParam;
	}
	
	// ZAP: Added getter.
	public GlobalExcludeURLParam getGlobalExcludeURLParam() {
		return globalExcludeURLParam;
	}

	public OptionsParamApi getApiParam() {
		return apiParam;
	}
	
	public OptionsParamExperimentalSliSupport getExperimentalFeaturesParam() {
		return experimentalFeatuesParam;
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
