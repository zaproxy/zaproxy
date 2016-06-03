/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.authentication;

/**
 * An interface that allows to manipulate the state (content, enabled and tool tip) of fields showing the logged in/out
 * indicators.
 * 
 * @since 2.5.0
 */
public interface AuthenticationIndicatorsPanel {

    /**
     * Gets the logged in indicator pattern.
     * 
     * @return the logged in indicator pattern
     */
    String getLoggedInIndicatorPattern();

    /**
     * Sets the logged in indicator pattern.
     * 
     * @param loggedInIndicatorPattern the new logged in indicator pattern
     */
    void setLoggedInIndicatorPattern(String loggedInIndicatorPattern);

    /**
     * Sets whether or not the field of the logged in indicator should be enabled.
     *
     * @param enabled {@code true} if the logged in indicator should be enabled, {@code false} otherwise
     */
    void setLoggedInIndicatorEnabled(boolean enabled);

    /**
     * Sets the tool tip of the logged in indicator.
     *
     * @param toolTip the tool tip of the logged in indicator, {@code null} to disable it
     */
    void setLoggedInIndicatorToolTip(String toolTip);

    /**
     * Gets the logged out indicator pattern.
     * 
     * @return the logged out indicator pattern
     */
    String getLoggedOutIndicatorPattern();

    /**
     * Sets the logged out indicator pattern.
     * 
     * @param loggedOutIndicatorPattern the new logged out indicator pattern
     */
    void setLoggedOutIndicatorPattern(String loggedOutIndicatorPattern);

    /**
     * Sets whether or not the field of the logged out indicator should be enabled.
     *
     * @param enabled {@code true} if the logged out indicator should be enabled, {@code false} otherwise
     */
    void setLoggedOutIndicatorEnabled(boolean enabled);

    /**
     * Sets the tool tip of the logged out indicator.
     *
     * @param toolTip the tool tip of the logged out indicator, {@code null} to disable it
     */
    void setLoggedOutIndicatorToolTip(String toolTip);

}
