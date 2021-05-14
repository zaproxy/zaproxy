/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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

import java.util.regex.Pattern;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.brk.AbstractBreakPointMessage;
import org.zaproxy.zap.extension.httppanel.Message;

public class HttpBreakpointMessage extends AbstractBreakPointMessage {

    public enum Location {
        url,
        request_header,
        request_body,
        response_header,
        response_body
    }

    public enum Match {
        contains,
        regex
    }

    private static final Logger logger = LogManager.getLogger(HttpBreakpointMessage.class);

    private static final String TYPE = "HTTP";

    private String string;
    private Pattern pattern;
    private Location location;
    private Match match;
    private boolean inverse;
    private boolean ignoreCase;

    public HttpBreakpointMessage(
            String string, Location location, Match match, boolean inverse, boolean ignoreCase) {
        super();
        this.string = string;
        this.location = location;
        this.match = match;
        this.inverse = inverse;
        this.ignoreCase = ignoreCase;

        compilePattern();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public String getString() {
        return string;
    }

    public void setString(String str) {
        this.string = str;
        compilePattern();
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
        compilePattern();
    }

    public boolean isInverse() {
        return inverse;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        compilePattern();
    }

    @Override
    public boolean match(Message aMessage, boolean isRequest, boolean onlyIfInScope) {
        if (aMessage instanceof HttpMessage) {
            HttpMessage message = (HttpMessage) aMessage;

            try {
                String uri = message.getRequestHeader().getURI().toString();

                if (onlyIfInScope) {
                    if (!Model.getSingleton().getSession().isInScope(uri)) {
                        return false;
                    }
                }

                String src;
                switch (location) {
                    default:
                    case url:
                        src = uri;
                        break;
                    case request_header:
                        if (!isRequest) {
                            return false;
                        }
                        src = message.getRequestHeader().toString();
                        break;
                    case request_body:
                        if (!isRequest) {
                            return false;
                        }
                        src = message.getRequestBody().toString();
                        break;
                    case response_header:
                        if (isRequest) {
                            return false;
                        }
                        src = message.getResponseHeader().toString();
                        break;
                    case response_body:
                        if (isRequest) {
                            return false;
                        }
                        src = message.getResponseBody().toString();
                        break;
                }

                boolean res;
                if (Match.contains.equals(this.match)) {
                    if (ignoreCase) {
                        res = src.toLowerCase().contains(string.toLowerCase());
                    } else {
                        res = src.contains(string);
                    }

                } else {
                    res = pattern.matcher(src).find();
                }

                if (inverse) {
                    return !res;
                } else {
                    return res;
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return false;
    }

    private void compilePattern() {
        try {
            if (ignoreCase) {
                pattern = Pattern.compile(string, Pattern.CASE_INSENSITIVE);
            } else {
                pattern = Pattern.compile(string);
            }
        } catch (Exception e) {
            // This wont be a problem if its a 'contains' match
            logger.debug("Potentially invalid regex", e);
        }
    }

    @Override
    public String getDisplayMessage() {
        return Constant.messages.getString("brk.brkpoint.location." + location.name())
                + ": "
                + Constant.messages.getString("brk.brkpoint.match." + match.name())
                + ": "
                + (ignoreCase ? Constant.messages.getString("brk.brkpoint.ignorecase.label") : "")
                + (inverse ? Constant.messages.getString("brk.brkpoint.inverse.label") : "")
                + string;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof HttpBreakpointMessage)) {
            return false;
        }
        HttpBreakpointMessage hbm = (HttpBreakpointMessage) obj;
        return this.getString().equals(hbm.getString())
                && this.getLocation().equals(hbm.getLocation())
                && this.getMatch().equals(hbm.getMatch())
                && this.isIgnoreCase() == hbm.isIgnoreCase()
                && this.isInverse() == hbm.isInverse();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(349, 631)
                . // two 'randomly' chosen prime numbers
                append(string)
                .append(location)
                .append(match)
                .append(ignoreCase)
                .append(inverse)
                .toHashCode();
    }
}
