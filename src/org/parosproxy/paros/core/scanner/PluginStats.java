/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2017 The ZAP Development Team
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
package org.parosproxy.paros.core.scanner;

/**
 * The stats of a {@link Plugin}, when the {@code Plugin} was started, how many messages were sent, number of alerts raised, and
 * its scan progress.
 * 
 * @since TODO add version
 */
public class PluginStats {

    private long startTime;
    private int messageCount;
    private int alertCount;
    private int progress;
    private boolean skipped;
    private String skippedReason;

    /**
     * Constructs a {@code PluginStats}.
     * 
     * @see #start()
     */
    PluginStats() {
    }

    /**
     * Tells whether or not the plugin was skipped.
     *
     * @return {@code true} if the plugin was skipped, {@code false} otherwise
     */
    public boolean isSkipped() {
        return skipped;
    }

    /**
     * Skips the plugin.
     *
     * @see #isSkipped()
     * @see #setSkippedReason(String)
     */
    void skip() {
        this.skipped = true;
    }

    /**
     * Sets the reason why the plugin was skipped.
     *
     * @param reason the reason why the plugin was skipped, might be {@code null}
     * @see #getSkippedReason()
     * @see #isSkipped()
     */
    void setSkippedReason(String reason) {
        this.skippedReason = reason;
    }

    /**
     * Gets the reason why the plugin was skipped.
     *
     * @return the reason why the plugin was skipped, might be {@code null}
     * @see #setSkippedReason(String)
     * @see #isSkipped()
     */
    public String getSkippedReason() {
        return skippedReason;
    }

    /**
     * Starts the plugin stats.
     */
    void start() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Gets the time when the plugin was started, in milliseconds.
     *
     * @return time when the plugin was started
     * @see System#currentTimeMillis()
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Gets the count of messages sent by the plugin.
     *
     * @return the count of messages sent
     */
    public int getMessageCount() {
        return messageCount;
    }

    /**
     * Increments the count of messages sent by the plugin.
     * <p>
     * Should be called when the plugin notifies that a message was sent.
     */
    void incMessageCount() {
        messageCount++;
    }

    /**
     * Gets the count of alerts raised by the plugin.
     *
     * @return the count of alerts raised.
     */
    public int getAlertCount() {
        return alertCount;
    }

    /**
     * Increments the count of alerts raised by the plugin.
     * <p>
     * Should be called when the plugin notifies that an alert was found.
     */
    void incAlertCount() {
        alertCount++;
    }

    /**
     * Gets the scan progress of the plugin.
     *
     * @return the scan progress
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Increments the scan progress of the plugin.
     * <p>
     * Should be called after scanning a message.
     */
    void incProgress() {
        this.progress++;
    }

    /**
     * Sets the scan progress of the plugin.
     *
     * @param progress the progress to set
     */
    void setProgress(int progress) {
        this.progress = progress;
    }
}