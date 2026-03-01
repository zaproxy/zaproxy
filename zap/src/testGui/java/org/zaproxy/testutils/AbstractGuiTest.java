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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.junit.jupiter.api.io.TempDir;
import org.parosproxy.paros.Constant;

/** Base class for GUI tests. */
@ExtendWith(GuiCondition.class)
public abstract class AbstractGuiTest extends AssertJSwingTestCaseTemplate {

    /**
     * A temporary directory where ZAP home/installation dirs are created.
     *
     * <p>Can be used for other temporary files/dirs.
     */
    @TempDir protected static Path tempDir;

    private static String zapInstallDir;
    private static String zapHomeDir;

    @BeforeAll
    static void beforeClass() throws Exception {
        zapInstallDir =
                Files.createDirectories(tempDir.resolve("install")).toAbsolutePath().toString();
        zapHomeDir = Files.createDirectories(tempDir.resolve("home")).toAbsolutePath().toString();

        InputStream resourceStream =
                AbstractGuiTest.class.getResourceAsStream("/log4j2-test.properties");
        if (resourceStream == null) {
            Path fallback = Paths.get("src", "test", "resources", "log4j2-test.properties");
            resourceStream = Files.newInputStream(fallback);
        }
        try (InputStream in = resourceStream) {
            Files.copy(in, Paths.get(zapHomeDir, "log4j2.properties"));
        }
    }

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
        Constant.setZapInstall(zapInstallDir);
        Constant.setZapHome(zapHomeDir);
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
