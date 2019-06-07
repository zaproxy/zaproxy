/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.search;

import org.parosproxy.paros.common.AbstractParam;

/**
 * Manages the search configurations saved in the configuration file.
 *
 * <p>It allows to change, programmatically, the following search options:
 *
 * <ul>
 *   <li>Maximum search results in GUI - allows to set the maximum number of results that should be
 *       shown in GUI results panel.
 * </ul>
 *
 * @see #getMaximumSearchResultsGUI()
 */
public class SearchParam extends AbstractParam {

    /** The base configuration key for all search configurations. */
    private static final String PARAM_BASE_KEY = "search";

    /**
     * The configuration key for the maximum results GUI option.
     *
     * @see #maximumSearchResultsGUI
     */
    private static final String PARAM_MAXIMUM_RESULTS_GUI = PARAM_BASE_KEY + ".maxResultsGUI";

    private static final int DEFAULT_MAXIMUM_RESULTS_GUI = 5000;

    /**
     * The number of maximum results that should be shown in the GUI.
     *
     * <p>Default is {@value #DEFAULT_MAXIMUM_RESULTS_GUI}.
     */
    private int maximumSearchResultsGUI = DEFAULT_MAXIMUM_RESULTS_GUI;

    /**
     * Parses the search options.
     *
     * <p>The following search options are parsed:
     *
     * <ul>
     *   <li>Maximum search results in GUI - allows to set the maximum number of results that should
     *       be shown in GUI results panel.
     * </ul>
     *
     * @see #getMaximumSearchResultsGUI()
     */
    @Override
    protected void parse() {
        maximumSearchResultsGUI = getInt(PARAM_MAXIMUM_RESULTS_GUI, DEFAULT_MAXIMUM_RESULTS_GUI);
    }

    /**
     * Sets whether the number of maximum results that should be shown in the results panel.
     *
     * @param maximumSearchResultsGUI the number of maximum results that should be shown in the
     *     results panel
     * @see #getMaximumSearchResultsGUI()
     */
    public void setMaximumSearchResultsGUI(int maximumSearchResultsGUI) {
        if (this.maximumSearchResultsGUI != maximumSearchResultsGUI) {
            this.maximumSearchResultsGUI = maximumSearchResultsGUI;

            getConfig().setProperty(PARAM_MAXIMUM_RESULTS_GUI, maximumSearchResultsGUI);
        }
    }

    /**
     * Returns the number of maximum results that should be shown in the results panel.
     *
     * @return the number of maximum results that should be shown in the results panel
     * @see #setMaximumSearchResultsGUI(int)
     */
    public int getMaximumSearchResultsGUI() {
        return maximumSearchResultsGUI;
    }
}
