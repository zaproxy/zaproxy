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
package org.zaproxy.zap.extension.brk.impl.http;

import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.brk.BreakpointManagementInterface;
import org.zaproxy.zap.extension.brk.ExtensionBreak;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.utils.Stats;

public class HttpBreakpointManagementDaemonImpl implements BreakpointManagementInterface {

    private boolean breakRequest;
    private boolean breakResponse;
    private boolean request;
    private HttpMessage msg;
    private boolean step;
    private boolean stepping;
    private boolean drop;

    @Override
    public boolean isBreakRequest() {
        return breakRequest;
    }

    @Override
    public boolean isBreakResponse() {
        return breakResponse;
    }

    @Override
    public boolean isBreakAll() {
        return (breakRequest && breakResponse);
    }

    @Override
    public void breakpointHit() {
        // Ignore
    }

    @Override
    public boolean isHoldMessage(Message aMessage) {
        if (step) {
            step = false;
            return false;
        }
        if (stepping) {
            return true;
        }
        if (drop) {
            return false;
        }
        if (aMessage instanceof HttpMessage) {
            HttpMessage msg = (HttpMessage) aMessage;
            if (msg.getResponseHeader().isEmpty()) {
                // Its a request
                if (this.isBreakRequest()) {
                    return true;
                }
            } else if (this.isBreakResponse()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isStepping() {
        return stepping;
    }

    @Override
    public boolean isToBeDropped() {
        if (drop) {
            drop = false;
            return true;
        }
        return false;
    }

    @Override
    public void setMessage(Message msg, boolean isRequest) {
        if (msg instanceof HttpMessage) {
            switch (Control.getSingleton().getMode()) {
                case safe:
                    throw new IllegalStateException("Not allowed in safe mode");
                case protect:
                    if (!msg.isInScope()) {
                        throw new IllegalStateException(
                                "Not allowed in protected mode for out of scope message");
                    }
                    break;
                case standard:
                    break;
                case attack:
                    break;
            }
            HttpMessage httpMsg = (HttpMessage) msg;
            if (this.msg == null) {
                this.msg = httpMsg;
                this.request = isRequest;
            } else {
                if (isRequest) {
                    this.msg.setRequestHeader(httpMsg.getRequestHeader());
                    this.msg.setRequestBody(httpMsg.getRequestBody());
                } else {
                    this.msg.setResponseHeader(httpMsg.getResponseHeader());
                    this.msg.setResponseBody(httpMsg.getResponseBody());
                }
            }
        } else {
            throw new IllegalArgumentException("Not an HttpMessage");
        }
    }

    @Override
    public boolean isRequest() {
        return this.request;
    }

    @Override
    public Message getMessage() {
        return this.msg;
    }

    @Override
    public void saveMessage(boolean isRequest) {
        // Ignore
    }

    @Override
    public void clearAndDisableRequest() {
        this.msg = null;
    }

    @Override
    public void clearAndDisableResponse() {
        this.msg = null;
    }

    @Override
    public void init() {}

    @Override
    public void reset() {
        // Ignore
    }

    @Override
    public void sessionModeChanged(Mode mode) {
        breakRequest = false;
        breakResponse = false;
        msg = null;
        step = false;
        stepping = false;
        drop = false;
    }

    @Override
    public void setBreakAllRequests(boolean brk) {
        this.breakRequest = brk;
    }

    @Override
    public void setBreakAllResponses(boolean brk) {
        this.breakResponse = brk;
    }

    @Override
    public void setBreakAll(boolean brk) {
        this.setBreakAllRequests(brk);
        this.setBreakAllResponses(brk);
    }

    @Override
    public void step() {
        this.step = true;
        this.stepping = true;
        Stats.incCounter(ExtensionBreak.BREAK_POINT_STEP_STATS);
    }

    @Override
    public void cont() {
        this.setBreakAllRequests(false);
        this.setBreakAllResponses(false);
        this.step = false;
        this.stepping = false;
    }

    @Override
    public void drop() {
        this.drop = true;
        Stats.incCounter(ExtensionBreak.BREAK_POINT_DROP_STATS);
    }

    @Override
    public void breakpointDisplayed() {
        // Ignore
    }
}
