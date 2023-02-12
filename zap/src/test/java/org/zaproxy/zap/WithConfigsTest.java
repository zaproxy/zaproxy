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
package org.zaproxy.zap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.testutils.TestUtils;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

@ExtendWith(MockitoExtension.class)
public abstract class WithConfigsTest extends TestUtils {

    /**
     * A temporary directory where ZAP home/installation dirs are created.
     *
     * <p>Can be used for other temporary files/dirs.
     */
    @TempDir protected static Path tempDir;

    /** The mocked {@code Model}. */
    protected Model model;

    /** The mocked {@code ExtensionLoader}. */
    protected ExtensionLoader extensionLoader;

    private static String zapInstallDir;
    private static String zapHomeDir;

    @BeforeAll
    static void beforeClass() throws Exception {
        zapInstallDir =
                Files.createDirectories(tempDir.resolve("install")).toAbsolutePath().toString();
        zapHomeDir = Files.createDirectories(tempDir.resolve("home")).toAbsolutePath().toString();

        try (InputStream in =
                WithConfigsTest.class.getResourceAsStream("/log4j2-test.properties")) {
            Files.copy(in, Paths.get(zapHomeDir, "log4j2.properties"));
        }
    }

    /**
     * Sets up ZAP, by initialising the home/installation dirs and core classes (for example, {@link
     * Constant}, {@link Control}, {@link Model}).
     *
     * @throws Exception if an error occurred while setting up the dirs or core classes.
     */
    @BeforeEach
    void setUpZap() throws Exception {
        Constant.setZapInstall(zapInstallDir);
        Constant.setZapHome(zapHomeDir);

        model = mock(Model.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        Model.setSingletonForTesting(model);

        extensionLoader =
                mock(ExtensionLoader.class, withSettings().strictness(Strictness.LENIENT));

        // Init all the things
        Constant.getInstance();
        setUpConstantMessages();
        Control.initSingletonForTesting(Model.getSingleton(), extensionLoader);
        Model.getSingleton().getOptionsParam().load(new ZapXmlConfiguration());
    }

    @AfterEach
    void cleanUp() {
        Constant.messages = null;
    }

    public static void setUpConstantMessages() {
        I18N i18n = Mockito.mock(I18N.class, withSettings().strictness(Strictness.LENIENT));
        given(i18n.getString(anyString())).willReturn("");
        given(i18n.getString(anyString(), any())).willReturn("");
        given(i18n.getLocal()).willReturn(Locale.getDefault());
        Constant.messages = i18n;
    }
}
