/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
package org.parosproxy.paros.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Dimension;
import java.awt.Frame;
import org.junit.jupiter.api.Test;
import org.zaproxy.testutils.AbstractZapGuiTest;

/** GUI tests for {@link SessionDialog}. */
class SessionDialogGuiTest extends AbstractZapGuiTest {

    @Test
    void shouldCreateDialogWithExpectedSize() {
        SessionDialog dialog = executeInEdt(() -> new SessionDialog());
        try {
            assertEquals(new Dimension(650, 550), dialog.getSize());
        } finally {
            executeInEdt(dialog::dispose);
        }
    }

    @Test
    void shouldSetTitleWhenCreatedWithParentAndTitle() {
        SessionDialog dialog =
                executeInEdt(() -> new SessionDialog((Frame) null, false, "Session", "Session"));
        try {
            assertEquals("Session", dialog.getTitle());
        } finally {
            executeInEdt(dialog::dispose);
        }
    }
}
