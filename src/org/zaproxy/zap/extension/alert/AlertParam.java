/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.alert;

import org.apache.commons.configuration.ConversionException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

public class AlertParam extends AbstractParam {

    private static final Logger LOGGER = Logger.getLogger(AlertParam.class);

    /**
     * The base configuration key for all alert configurations.
     */
    private static final String PARAM_BASE_KEY = "alert";

    private static final String PARAM_MERGE_RELATED_ISSUES = PARAM_BASE_KEY + ".mergeissues";

    /**
     * The configuration key for the maximum number of instances to include in a report.
     * 
     * @see #maximumInstances
     */
    private static final String PARAM_MAXIMUM_INSTANCES = PARAM_BASE_KEY + ".maxInstances";

    private static final String PARAM_OVERRIDES_FILENAME = PARAM_BASE_KEY + ".overridesFilename";

    private static final int DEFAULT_MAXIMUM_INSTANCES = 20;

    /**
     * The number of maximum instances of each vulnerability included in a report.
     * <p>
     * Default is {@value #DEFAULT_MAXIMUM_INSTANCES}.
     * </p>
     */
    private int maximumInstances = DEFAULT_MAXIMUM_INSTANCES;
    
    private boolean mergeRelatedIssues = true;
    
    private String overridesFilename;

    /**
     * Parses the alert options.
     * 
     * @see #getMaximumInstances()
     */
    @Override
    protected void parse() {
        try {
            maximumInstances = getConfig().getInt(PARAM_MAXIMUM_INSTANCES, DEFAULT_MAXIMUM_INSTANCES);
        } catch (ConversionException e) {
            LOGGER.error("Failed to load the \"Maximum instances\" configuration: " + e.getMessage(), e);
        }
        try {
        	mergeRelatedIssues = getConfig().getBoolean(PARAM_MERGE_RELATED_ISSUES, true);
        } catch (ConversionException e) {
            LOGGER.error("Failed to load the \"old format\" configuration: " + e.getMessage(), e);
        }
        try {
            overridesFilename = getConfig().getString(PARAM_OVERRIDES_FILENAME, "");
        } catch (ConversionException e) {
            LOGGER.error("Failed to load the \"Overrides filename\" configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Sets the maximum instances of an alert to include in a report.
     */
    public void setMaximumInstances(int maximumInstances) {
        if (this.maximumInstances != maximumInstances) {
            this.maximumInstances = maximumInstances;

            getConfig().setProperty(PARAM_MAXIMUM_INSTANCES, Integer.valueOf(maximumInstances));
        }
    }

    /**
     * Returns the maximum instances of an alert to include in a report.
     */
    public int getMaximumInstances() {
        return maximumInstances;
    }

	public boolean isMergeRelatedIssues() {
		return mergeRelatedIssues;
	}

	public void setMergeRelatedIssues(boolean mergeRelatedIssues) {
		if (this.mergeRelatedIssues != mergeRelatedIssues) {
			this.mergeRelatedIssues = mergeRelatedIssues;
            getConfig().setProperty(PARAM_MERGE_RELATED_ISSUES, Boolean.valueOf(mergeRelatedIssues));
		}
	}

    public String getOverridesFilename() {
        return overridesFilename;
    }

    public void setOverridesFilename(String overridesFilename) {
        this.overridesFilename = overridesFilename;
        getConfig().setProperty(PARAM_OVERRIDES_FILENAME, overridesFilename);
    }

}
