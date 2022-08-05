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
// ZAP: 2013/09/08 Issue 691: Handle old plugins
// ZAP: 2014/02/12 Issue 1030: Load and save scan policies
// ZAP: 2014/02/21 Issue 1043: Custom active scan dialog
// ZAP: 2014/11/19 Issue 1412: Init scan rule status (quality) from add-on
// ZAP: 2015/07/26 Issue 1618: Target Technology Not Honored
// ZAP: 2017/07/12 and 2017/09/21 JavaDoc tweaks.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2021/05/14 Remove empty statement.
package org.parosproxy.paros.core.scanner;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.Configuration;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.extension.alert.ExampleAlertProvider;
import org.zaproxy.zap.model.Tech;
import org.zaproxy.zap.model.TechSet;

/**
 * This interface must be implemented by a Plugin for running the checks. The {@link
 * AbstractHostPlugin}, {@link AbstractAppPlugin}, {@link AbstractAppParamPlugin} implement this
 * interface and is a good starting point for writing new plugins.
 */
public interface Plugin extends Runnable, ExampleAlertProvider {

    public enum AlertThreshold {
        /**
         * Indicates that the scanner is disabled. A scanner is not used when set to {@code OFF}.
         */
        OFF,
        /**
         * Indicates that the configured default value will be used (scanners do not need to check
         * this value).
         */
        DEFAULT,
        LOW,
        MEDIUM,
        HIGH
    }

    public enum AttackStrength {
        /**
         * Indicates that the configured default value will be used (scanners do not need to check
         * this value).
         */
        DEFAULT,
        LOW,
        MEDIUM,
        HIGH,
        INSANE
    }

    /**
     * Unique Paros ID of this plugin.
     *
     * @return the ID
     */
    int getId();

    /**
     * Plugin name. This is the human readable plugin name for display.
     *
     * @return the internationalised name
     */
    String getName();

    /**
     * Code name is the plugin name used for dependency naming. By default this is the class name
     * (without the package prefix).
     *
     * @return the internal name
     */
    String getCodeName();

    /**
     * Default description of this plugin.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Gets the highest risk level of the alerts raised by the plugin.
     *
     * @return the highest risk level of the alerts raised by the plugin.
     * @see Alert#RISK_HIGH
     * @see Alert#RISK_MEDIUM
     * @see Alert#RISK_LOW
     * @see Alert#RISK_INFO
     * @since 2.0.0
     */
    int getRisk();

    /**
     * Initialises the plugin with the given message and host process.
     *
     * @param msg the message to be scanned.
     * @param parent the parent host process.
     */
    void init(HttpMessage msg, HostProcess parent);

    /**
     * Scans the target server using the message previously set during initialisation.
     *
     * @see #init(HttpMessage, HostProcess)
     */
    void scan();

    /**
     * The {@link #getCodeName() names} of dependencies of the plugin.
     *
     * <p>The plugin will not run if the dependencies are not fulfilled nor run.
     *
     * @return an array with the names of the dependencies, or {@code null}/empty if none.
     */
    String[] getDependency();

    /**
     * Sets whether or not the scanner is enabled.
     *
     * @param enabled {@code true} if the scanner should be enabled, {@code false} otherwise
     */
    void setEnabled(boolean enabled);

    /**
     * Tells whether or not the scanner is enabled.
     *
     * @return {@code true} if the scanner is enabled, {@code false} otherwise
     */
    boolean isEnabled();

    /**
     * Gets the category of this scanner.
     *
     * @return the category of the scanner
     * @see Category
     */
    int getCategory();

    /**
     * Default solution returned by this plugin.
     *
     * @return the solution
     */
    String getSolution();

    /**
     * Reference document provided by this plugin.
     *
     * @return the references
     */
    String getReference();

    /**
     * Gets the tags attached to the alerts raised by this plugin.
     *
     * @return the alert tags
     * @since 2.11.0
     */
    default Map<String, String> getAlertTags() {
        return Collections.emptyMap();
    }

    /**
     * Plugin must implement this to notify when completed.
     *
     * @param parent the parent {@code HostProcess}
     */
    void notifyPluginCompleted(HostProcess parent);

    /**
     * Tells whether or not the scanner can be selected and should be shown..
     *
     * @return {@code true} if the scanner is visible, {@code false} otherwise
     */
    boolean isVisible();

    void setConfig(Configuration config);

    Configuration getConfig();

    void saveTo(Configuration conf);

    void loadFrom(Configuration conf);

    void cloneInto(Plugin plugin);

    void createParamIfNotExist();

    // ZAP Added isDepreciated, getDelayInMs, setDelayInMs
    boolean isDepreciated();

    int getDelayInMs();

    void setDelayInMs(int delay);

    /**
     * The alert threshold for this plugin, i.e. the level of certainty required to report an alert
     *
     * @param incDefault if the DEFAULT level should be returned as DEFAULT as opposed to the value
     *     of the default level
     * @return The alert threshold currently set for this plugin
     */
    AlertThreshold getAlertThreshold(boolean incDefault);

    /**
     * The alert threshold for this plugin, i.e. the level of certainty required to report an alert.
     * The DEFAULT level will not be returned, instead the value of the default level will be
     * returned, if relevant.
     *
     * @return The alert threshold for this plugin
     */
    AlertThreshold getAlertThreshold();

    /**
     * Set the alert threshold for this plugin, i.e. the level of certainty required to report an
     * alert
     *
     * @param level The alert threshold to set for this plugin
     */
    void setAlertThreshold(AlertThreshold level);

    /**
     * Set the default alert threshold for this plugin, i.e. the level of certainty required to
     * report an alert
     *
     * @param level The alert threshold to set for this plugin
     */
    void setDefaultAlertThreshold(AlertThreshold level);

    /**
     * Returns an array of the AlertThresholds supported. It must include MEDIUM and may include LOW
     * and HIGH OFF and DEFAULT are assumed and should not be returned.
     *
     * @return an array containing the attack thresholds supported
     */
    AlertThreshold[] getAlertThresholdsSupported();

    /**
     * Returns the AttackStrength, which is an indication of the relative number of requests the
     * plugin will make against a given target
     *
     * @param incDefault if the DEFAULT level should be returned as DEFAULT as opposed to the value
     *     of the default level
     * @return The AttackStrength currently set for this plugin
     */
    AttackStrength getAttackStrength(boolean incDefault);

    /**
     * Returns the AttackStrength, which is an indication of the relative number of requests the
     * plugin will make against a given target. The DEFAULT level will not be returned, instead the
     * value of the default level will be returned, if relevant.
     *
     * @return The AttackStrength currently set for this plugin
     */
    AttackStrength getAttackStrength();

    /**
     * Set the attack strength for this plugin, i.e. the relative number of requests the plugin will
     * make against a given target.
     *
     * @param level The alert threshold to set for this plugin
     */
    void setAttackStrength(AttackStrength level);

    /**
     * Set the default attack strength for this plugin, i.e. the relative number of attacks that
     * will be performed
     *
     * @param strength The attack strength to set for this plugin
     */
    void setDefaultAttackStrength(AttackStrength strength);

    /**
     * Returns an array of the AttackStrengths supported. It must include MEDIUM and may include
     * LOW, HIGH and INSANE. DEFAULT is assumed and should not be returned.
     *
     * @return an array containing the attack strengths supported
     */
    AttackStrength[] getAttackStrengthsSupported();

    /**
     * Sets the technologies enabled for the scan.
     *
     * <p>Called before {@link #init(HttpMessage, HostProcess) initialising the plugin}.
     *
     * @param ts the technologies enabled for the scan
     * @throws IllegalArgumentException (since 2.6.0) if the given parameter is {@code null}.
     * @since 2.0.0
     * @see #targets(TechSet)
     */
    void setTechSet(TechSet ts);

    /**
     * Tells whether or not the given technology is enabled for the scan.
     *
     * <p>Helper method to check if a technology is enabled before performing a test/scan.
     *
     * @param tech the technology that will be checked
     * @return {@code true} if the technology is enabled for the scan, {@code false} otherwise
     * @since 2.0.0
     * @see #targets(TechSet)
     */
    boolean inScope(Tech tech);

    /**
     * Tells whether or not the scanner targets the given {@code technologies} to be run. If the
     * scanner does not target a specific technology is should return, always, {@code true} so the
     * scanner is run independently of the technologies enabled.
     *
     * <p>Scanners that target multiple technologies must check which technologies are enabled
     * before performing the actual scans.
     *
     * @param technologies the technologies that are enabled for the scan, never {@code null}
     * @return {@code true} if the scanner is targeting the given technologies (or none at all),
     *     {@code false} otherwise
     * @since 2.4.1
     * @see #setTechSet(TechSet)
     * @see #inScope(Tech)
     */
    boolean targets(TechSet technologies);

    void setTimeStarted();

    Date getTimeStarted();

    void setTimeFinished();

    Date getTimeFinished();

    /**
     * Gets the CWE ID of the issue(s) raised by the scanner.
     *
     * @return the CWE ID, -1 if unknown.
     * @since 2.2.0
     * @see <a href="https://cwe.mitre.org/index.html">CWE - Common Weakness Enumeration</a>
     */
    int getCweId();

    /**
     * Gets the WASC ID of the issue(s) raised by the scanner.
     *
     * @return the WASC ID, -1 if unknown.
     * @since 2.2.0
     * @see <a href="http://projects.webappsec.org/w/page/13246978/Threat%20Classification">The WASC
     *     Threat Classification</a>
     */
    int getWascId();

    /**
     * Gets the status of the plugin (as given by the corresponding add-on).
     *
     * <p>The status is automatically set by core code during initialisation.
     *
     * @return the status of the plugin.
     * @since 2.4.0
     */
    AddOn.Status getStatus();

    /**
     * Gets a list of example alerts that the plugin can raise.
     *
     * @since 2.10.0
     */
    @Override
    default List<Alert> getExampleAlerts() {
        return null;
    }

    /**
     * Gets the name of the scan rule, falling back to the simple name of the class as last resort.
     *
     * @return a name representing the scan rule.
     * @since 2.12.0
     */
    String getDisplayName();
}
