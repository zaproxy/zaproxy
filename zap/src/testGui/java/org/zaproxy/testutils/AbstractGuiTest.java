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
package org.zaproxy.testutils;

import java.util.concurrent.Callable;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunnable;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.testing.AssertJSwingTestCaseTemplate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/** Base class for GUI tests. */
@ExtendWith(GuiCondition.class)
public abstract class AbstractGuiTest extends AssertJSwingTestCaseTemplate {

    @BeforeAll
    static void installEdtCheck() {
        FailOnThreadViolationRepaintManager.install();
    }

    @AfterAll
    static void uninstallEdtCheck() {
        FailOnThreadViolationRepaintManager.uninstall();
    }

    @BeforeEach
    void setup() {
        setUpRobot();
    }

    @AfterEach
    void clean() {
        cleanUp();
    }

    protected static <T> T executeInEdt(Callable<T> callable) {
        return GuiActionRunner.execute(callable);
    }

    protected static void executeInEdt(GuiActionRunnable task) {
        GuiActionRunner.execute(task);
    }
}
