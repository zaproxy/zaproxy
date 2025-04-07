/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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

/** This is an internal class rather than part of a supported API and may be changed at any time. */
public class RPCParameter implements Comparable<RPCParameter> {
    private String name;
    private String value;
    private int beginOffset;
    private int endOffset;
    private boolean toQuote;

    public RPCParameter() {}

    public RPCParameter(
            String name, String value, int beginOffset, int endOffset, boolean toQuote) {
        super();
        this.name = name;
        this.value = value;
        this.beginOffset = beginOffset;
        this.endOffset = endOffset;
        this.toQuote = toQuote;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getBeginOffset() {
        return beginOffset;
    }

    public void setBeginOffset(int beginOffset) {
        this.beginOffset = beginOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    public boolean isToQuote() {
        return toQuote;
    }

    public void setToQuote(boolean toQuote) {
        this.toQuote = toQuote;
    }

    @Override
    public int compareTo(RPCParameter t) {
        return this.beginOffset - t.beginOffset;
    }
}
