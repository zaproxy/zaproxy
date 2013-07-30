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
// ZAP: 2012/03/03 Added getLevel(boolean incDefault) 
// ZAP: 2012/08/07 Renamed Level to AlertThreshold and added support for AttackStrength
// ZAP: 2012/08/31 Enabled control of AttackStrength
// ZAP: 2012/10/03 Issue 388: Added enabling support for technologies
// ZAP: 2013/01/19 Issue 460 Add support for a scan progress dialog
// ZAP: 2013/07/12 Issue 713: Add CWE and WASC numbers to issues

package org.parosproxy.paros.core.scanner;

import java.util.Date;

import org.apache.commons.configuration.Configuration;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.Tech;
import org.zaproxy.zap.model.TechSet;

/**
 * This interface must be implemented by a Plugin for running the checks.
 * The AbstractHostPlugin, AbstractAppPlugin, AbstractAppParamPlugin implement this interface
 * and is a good starting point for writing new plugins.
 * 
 */
public interface Plugin extends Runnable {
    
	public enum AlertThreshold {OFF, DEFAULT, LOW, MEDIUM, HIGH};
	public enum AttackStrength {DEFAULT, LOW, MEDIUM, HIGH, INSANE};
	
    /**
     * Unique Paros ID of this plugin.
     * @return
     */
    int getId();

    /**
     * Plugin name.  This is the human readable plugin name for display.
     * @return
     */
    String getName();

    /**
     * Code name is the plugin name used for dependency naming.  By default this is the 
     * class name (without the package prefix).
     * @return
     */
    String getCodeName();
    
    /**
     * Default description of this plugin.
     * @return
     */
    String getDescription();
    
    /**
     * returns the maximum risk alert that is thrown by the plugin
     * @return the maximum risk alert that is thrown by the plugin
     * @see Alert.RISK_HIGH
     * @see Alert.RISK_MEDIUM
     * @see Alert.RISK_LOW
     * @see Alert.RISK_INFO
     */
    int getRisk ();
    
    void init(HttpMessage msg, HostProcess parent);
    
    void scan();

    /**
     * Array of dependency of this plugin.  This plugin will start running until all
     * the dependency completed running.  The dependency is the class name.
     * @return null if there is no dependency.
     */
    String[] getDependency();

    /**
     * Enable/disable this plugin.
     * @param enabled
     */
    void setEnabled(boolean enabled);
    
    /**
     * Return if this plugin is enabled.
     * @return true = enabled.
     */
    boolean isEnabled();
    
    /**
     * The Category of this plugin.  See Category.
     * @return
     */
    int getCategory();
    
    /**
     * Default solution returned by this plugin.
     * @return
     */
    String getSolution();
    
    /**
     * Reference document provided by this plugin.
     * @return
     */
    String getReference();

    /**
     * Plugin must implement this to notify when completed.
     *
     */
    void notifyPluginCompleted(HostProcess parent);
    
    /**
     * Always true - if plugin is visible to the framework.
     * @return
     */
    boolean isVisible();

    void setConfig(Configuration config);
    
    Configuration getConfig();
    
    void createParamIfNotExist();
    
	// ZAP Added isDepreciated, getDelayInMs, setDelayInMs
	boolean isDepreciated();
	
	int getDelayInMs();
	
	void setDelayInMs(int delay);
	
	/**
	 * The alert threshold for this plugin, ie the level of certainty required to report an alert
	 * @param incDefault if the DEFAULT level should be returned as DEFAULT as opposed to the value of the default level
	 * @return The alert threshold currently set for this plugin
	 */
	AlertThreshold getAlertThreshold(boolean incDefault);
	
	/**
	 * The alert threshold for this plugin, ie the level of certainty required to report an alert.
	 * The DEFAULT level will not be returned, instead the value of the default level will be returned, if relevant.
	 * @return The alert threshold for this plugin
	 */
	AlertThreshold getAlertThreshold();
	
	/**
	 * Set the alert threshold for this plugin, ie the level of certainty required to report an alert
	 * @param level The alert threshold to set for this plugin
	 */
	void setAlertThreshold(AlertThreshold level);

	/**
	 * Set the default alert threshold for this plugin, ie the level of certainty required to report an alert
	 * @param level The alert threshold to set for this plugin
	 */
	void setDefaultAlertThreshold(AlertThreshold level);
	
	/**
	 * Returns an array of the AlertThresholds supported. It must include MEDIUM and may include LOW and HIGH
	 * OFF and DEFAULT are assumed and should not be returned.
	 * @return
	 */
	AlertThreshold[] getAlertThresholdsSupported();

	/**
	 * Returns the AttackStrength, which is an indication of the relative number of requests the plugin will make
	 * against a given target
	 * @param incDefault if the DEFAULT level should be returned as DEFAULT as opposed to the value of the default level
	 * @return The AttackStrength currently set for this plugin
	 */
	AttackStrength getAttackStrength(boolean incDefault);
	
	/**
	 * Returns the AttackStrength, which is an indication of the relative number of requests the plugin will make
	 * against a given target.
	 * The DEFAULT level will not be returned, instead the value of the default level will be returned, if relevant.
	 * @return The AttackStrength currently set for this plugin
	 */
	AttackStrength getAttackStrength();
	
	/**
	 * Set the attack strength for this plugin, ie the relative number of requests the plugin will make
	 * against a given target.
	 * @param level The alert threshold to set for this plugin
	 */
	void setAttackStrength (AttackStrength level);
	
	/**
	 * Set the default attack strength for this plugin, ie the relative number of attacks that will be performed
	 * @param strength The attack strength to set for this plugin
	 */
	void setDefaultAttackStrength(AttackStrength strength);
	
	/**
	 * Returns an array of the AttackStrengths supported. It must include MEDIUM and may include LOW, HIGH and INSANE
	 * DEFAULT is assumed and should not be returned.
	 * @return
	 */
	AttackStrength[] getAttackStrengthsSupported();
	
	/**
	 * Set the technology set this scanner should include in scope (if relevant)
	 * @param ts
	 */
	void setTechSet(TechSet ts);
	
	/**
	 * Returns true if the technology should be includes in the scope
	 * @param tech
	 * @return
	 */
	boolean inScope(Tech tech);
	
	void setTimeStarted();
	Date getTimeStarted();

	void setTimeFinished();
	Date getTimeFinished();
	
	/**
	 * Get the CWE Id: http://cwe.mitre.org/index.html
	 * @return
	 */
	int getCweId();
	
	/**
	 * Get the WASC Id: http://projects.webappsec.org/w/page/13246978/Threat%20Classification
	 * @return
	 */
	int getWascId();

}
