/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.users;

public class AuthenticationState {

    private long lastSuccessfulAuthTime;

    private long lastPollTime;

    private Boolean lastPollResult;

    private int requestsSincePoll = 0;

    private int lastAuthRequestHistoryId;

    public String lastAuthFailure;

    public long getLastSuccessfulAuthTime() {
        return lastSuccessfulAuthTime;
    }

    public void setLastSuccessfulAuthTime(long lastSuccessfulAuthTime) {
        this.lastSuccessfulAuthTime = lastSuccessfulAuthTime;
    }

    public long getLastPollTime() {
        return lastPollTime;
    }

    public void setLastPollTime(long lastPollTime) {
        this.lastPollTime = lastPollTime;
    }

    /**
     * Gets the last poll result - true means that the user is authenticated, false is
     * unauthenticated and null if no poll request has been made.
     *
     * @return the last poll result
     */
    public Boolean getLastPollResult() {
        return lastPollResult;
    }

    /**
     * Sets the last poll result - this can be used by script or add-ons to change the known logged
     * in state e.g. if they have more accurate information
     *
     * @param lastPollResult
     */
    public void setLastPollResult(Boolean lastPollResult) {
        this.lastPollResult = lastPollResult;
    }

    public int getRequestsSincePoll() {
        return requestsSincePoll;
    }

    public void setRequestsSincePoll(int requestsSincePoll) {
        this.requestsSincePoll = requestsSincePoll;
    }

    public void incRequestsSincePoll() {
        this.requestsSincePoll += 1;
    }

    public int getLastAuthRequestHistoryId() {
        return lastAuthRequestHistoryId;
    }

    public void setLastAuthRequestHistoryId(int lastAuthRequestHistoryId) {
        this.lastAuthRequestHistoryId = lastAuthRequestHistoryId;
    }

    public String getLastAuthFailure() {
        return lastAuthFailure;
    }

    public void setLastAuthFailure(String lastAuthFailure) {
        this.lastAuthFailure = lastAuthFailure;
    }
}
