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
package org.zaproxy.zap.extension.brk;

import org.zaproxy.zap.extension.httppanel.Message;

/**
 * @deprecated (2.15.0) See the break add-on in zap-extensions instead.
 */
@Deprecated(since = "2.15.0", forRemoval = true)
@SuppressWarnings("removal")
public interface BreakpointsUiManagerInterface {

    Class<? extends Message> getMessageClass();

    Class<? extends BreakpointMessageInterface> getBreakpointClass();

    String getType();

    void handleAddBreakpoint(Message aMessage);

    void handleEditBreakpoint(BreakpointMessageInterface breakpoint);

    void handleRemoveBreakpoint(BreakpointMessageInterface breakpoint);

    void reset();
}
