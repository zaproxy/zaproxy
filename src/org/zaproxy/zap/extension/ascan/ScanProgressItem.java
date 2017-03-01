/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 ZAP development team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.ascan;

import java.util.Date;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.Plugin;

/**
 * Class for Visual Plugin Progress management
 */
public class ScanProgressItem {
    
    // Inner constants for status management
    public static final int STATUS_PENDING   = 0x01;
    public static final int STATUS_RUNNING   = 0x02;
    public static final int STATUS_COMPLETED = 0x03;

    private HostProcess hProcess;
    private Plugin plugin;
    private int status;

    /**
     *
     * @param plugin
     * @param status
     */
    public ScanProgressItem(HostProcess hProcess, Plugin plugin, int status) {
        this.hProcess = hProcess;
        this.plugin = plugin;
        this.status = status;
    }

    /**
     *
     * @return
     */
    public String getNameLabel() {
        return plugin.getName();
    }

    /**
     *
     * @return
     */
    public String getAttackStrenghtLabel() {
        return Constant.messages.getString("ascan.policy.level." + plugin.getAttackStrength().name().toLowerCase());
    }

    /**
     *
     * @return
     */
    public String getStatusLabel() {
        switch (status) {
            case STATUS_COMPLETED:
                return Constant.messages.getString("ascan.progress.label.completed");

            case STATUS_RUNNING:
                return Constant.messages.getString("ascan.progress.label.running");

            case STATUS_PENDING:
                return Constant.messages.getString("ascan.progress.label.pending");
        }

        return "";
    }

    public long getElapsedTime() {
        if ((status == STATUS_PENDING) || (plugin.getTimeStarted() == null)) {
            return -1;
        }

        Date end = (plugin.getTimeFinished() == null) ? new Date() : plugin.getTimeFinished();
        return (end.getTime() - plugin.getTimeStarted().getTime());
    }

    /**
     * Get back the percentage of completion.
     * 
     * @return the percentage value from 0 to 100
     */
    public int getProgressPercentage()  {
        // Implemented using node counts...
        if (isRunning()) {
            int progress = (hProcess.getTestCurrentCount(plugin) * 100) / hProcess.getTestTotalCount();
            // Make sure not return 100 (or more) if still running...
            // That might happen if more nodes are being scanned that the ones enumerated at the beginning.
            return progress >= 100 ? 99 : progress;
        } else if (isCompleted()) {
            return 100;        
            
        } else {
            return 0;
        }
    }
    
    /**
     * 
     * @return 
     */
    public boolean isRunning() {
        return (status == STATUS_RUNNING);
    }

    /**
     * 
     * @return 
     */
    public boolean isCompleted() {
        return (status == STATUS_COMPLETED);
    }
    
    /**
     * Tells whether or not the plugin was skipped.
     * 
     * @return {@code true} if the plugin was skipped, {@code false} otherwise.
     * @since 2.4.0
     * @see #getSkippedReason()
     */
    public boolean isSkipped() {
        return hProcess.isSkipped(plugin);
    }

    /**
     * Gets the reason why the plugin was skipped.
     *
     * @return the reason why the plugin was skipped, might be {@code null} if there's no reason
     * @since TODO add version
     * @see #isSkipped()
     */
    public String getSkippedReason() {
        return hProcess.getSkippedReason(plugin);
    }

    public void skip() {
        if (isRunning()) {
            hProcess.pluginSkipped(plugin, Constant.messages.getString("ascan.progress.label.skipped.reason.user"));
        }
    }
    
    /**
     * 
     * @return 
     */
    protected Plugin getPlugin() {
        return plugin;
    }

	public int getReqCount() {
		return hProcess.getPluginRequestCount(plugin.getId());
	}

	@Override
	public String toString() {
		return Integer.toString(getProgressPercentage());
	}
}
