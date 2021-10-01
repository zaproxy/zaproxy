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
package org.zaproxy.zap.extension.brk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control.Mode;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.utils.Stats;

public class BreakpointMessageHandler2 {

    private static final Logger LOGGER = LogManager.getLogger(BreakpointMessageHandler2.class);

    protected static final Object SEMAPHORE = new Object();

    protected final BreakpointManagementInterface breakMgmt;

    protected List<BreakpointMessageInterface> enabledBreakpoints;

    protected List<BreakpointMessageInterface> enabledIgnoreRules;

    private List<String> enabledKeyBreakpoints = new ArrayList<>();

    public List<String> getEnabledKeyBreakpoints() {
        return enabledKeyBreakpoints;
    }

    public void setEnabledKeyBreakpoints(List<String> enabledKeyBreakpoints) {
        this.enabledKeyBreakpoints = enabledKeyBreakpoints;
    }

    public BreakpointMessageHandler2(BreakpointManagementInterface aBreakPanel) {
        this.breakMgmt = aBreakPanel;
    }

    public void setEnabledBreakpoints(List<BreakpointMessageInterface> breakpoints) {
        this.enabledBreakpoints = breakpoints;
    }

    public void setEnabledIgnoreRules(List<BreakpointMessageInterface> IgnoreRules) {
        this.enabledIgnoreRules = IgnoreRules;
    }

    /**
     * Do not call if in {@link Mode#safe}.
     *
     * @param aMessage
     * @param onlyIfInScope
     * @return False if message should be dropped.
     */
    public boolean handleMessageReceivedFromClient(Message aMessage, boolean onlyIfInScope) {
        if (!isBreakpoint(aMessage, true, onlyIfInScope)) {
            return true;
        }

        // Do this outside of the semaphore loop so that the 'continue' button can apply to all
        // queued breakpoints
        // but be reset when the next breakpoint is hit
        breakMgmt.breakpointHit();
        Stats.incCounter(ExtensionBreak.BREAK_POINT_HIT_STATS);
        BreakEventPublisher.getPublisher().publishHitEvent(aMessage);

        synchronized (SEMAPHORE) {
            if (breakMgmt.isHoldMessage(aMessage)) {
                BreakEventPublisher.getPublisher().publishActiveEvent(aMessage);
                setBreakDisplay(aMessage, true);
                waitUntilContinue(aMessage, true);
                BreakEventPublisher.getPublisher().publishInactiveEvent(aMessage);
            }
        }
        breakMgmt.clearAndDisableRequest();
        return !breakMgmt.isToBeDropped();
    }

    /**
     * Do not call if in {@link Mode#safe}.
     *
     * @param aMessage
     * @param onlyIfInScope
     * @return False if message should be dropped.
     */
    public boolean handleMessageReceivedFromServer(Message aMessage, boolean onlyIfInScope) {
        if (!isBreakpoint(aMessage, false, onlyIfInScope)) {
            return true;
        }

        // Do this outside of the semaphore loop so that the 'continue' button can apply to all
        // queued breakpoints
        // but be reset when the next breakpoint is hit
        breakMgmt.breakpointHit();
        Stats.incCounter(ExtensionBreak.BREAK_POINT_HIT_STATS);
        BreakEventPublisher.getPublisher().publishHitEvent(aMessage);

        synchronized (SEMAPHORE) {
            if (breakMgmt.isHoldMessage(aMessage)) {
                BreakEventPublisher.getPublisher().publishActiveEvent(aMessage);
                setBreakDisplay(aMessage, false);
                waitUntilContinue(aMessage, false);
                BreakEventPublisher.getPublisher().publishInactiveEvent(aMessage);
            }
        }
        breakMgmt.clearAndDisableResponse();
        return !breakMgmt.isToBeDropped();
    }

    private void setBreakDisplay(final Message msg, boolean isRequest) {
        breakMgmt.setMessage(msg, isRequest);
        breakMgmt.breakpointDisplayed();
    }

    private void waitUntilContinue(Message aMessage, final boolean isRequest) {
        // Note that multiple requests and responses can get built up, so pressing continue only
        // releases the current break, not all of them.
        while (breakMgmt.isHoldMessage(aMessage)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
        breakMgmt.saveMessage(isRequest);
    }

    /**
     * You have to handle {@link Mode#safe} outside.
     *
     * @param aMessage
     * @param isRequest
     * @param onlyIfInScope
     * @return True if a breakpoint for given message exists.
     */
    public boolean isBreakpoint(Message aMessage, boolean isRequest, boolean onlyIfInScope) {
        if (aMessage.isForceIntercept()) {
            // The browser told us to do it Your Honour
            return true;
        }

        if (isSkipOnIgnoreRules(aMessage, isRequest, onlyIfInScope)) {
            return false;
        }

        if (onlyIfInScope && !aMessage.isInScope()) {
            return false;
        }

        if (isBreakOnAllRequests(aMessage, isRequest)) {
            // Break on all requests
            return true;
        } else if (isBreakOnAllResponses(aMessage, isRequest)) {
            // Break on all responses
            return true;
        } else if (isBreakOnStepping(aMessage, isRequest)) {
            // Stopping through all requests and responses
            return true;
        }

        return isBreakOnEnabledBreakpoint(aMessage, isRequest, onlyIfInScope);
    }

    protected boolean isBreakOnAllRequests(Message aMessage, boolean isRequest) {
        return isRequest && breakMgmt.isBreakRequest();
    }

    protected boolean isBreakOnAllResponses(Message aMessage, boolean isRequest) {
        return !isRequest && breakMgmt.isBreakResponse();
    }

    protected boolean isBreakOnStepping(Message aMessage, boolean isRequest) {
        return breakMgmt.isStepping();
    }

    protected boolean isBreakOnEnabledBreakpoint(
            Message aMessage, boolean isRequest, boolean onlyIfInScope) {
        if (enabledBreakpoints.isEmpty()) {
            // No breakpoints
            return false;
        }

        // match against the breakpoints
        synchronized (enabledBreakpoints) {
            Iterator<BreakpointMessageInterface> it = enabledBreakpoints.iterator();

            while (it.hasNext()) {
                BreakpointMessageInterface breakpoint = it.next();

                if (breakpoint.match(aMessage, isRequest, onlyIfInScope)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean isSkipOnIgnoreRules(
            Message aMessage, boolean isRequest, boolean onlyIfInScope) {
        if (enabledIgnoreRules == null || enabledIgnoreRules.isEmpty()) {
            // No Ignoring rules
            return false;
        }

        // match against the ignoring rule
        synchronized (enabledIgnoreRules) {
            Iterator<BreakpointMessageInterface> it = enabledIgnoreRules.iterator();

            while (it.hasNext()) {
                BreakpointMessageInterface ignoreRule = it.next();

                if (ignoreRule.isEnabled()
                        && ignoreRule.match(aMessage, isRequest, onlyIfInScope)) {
                    return true;
                }
            }
        }

        return false;
    }
}
