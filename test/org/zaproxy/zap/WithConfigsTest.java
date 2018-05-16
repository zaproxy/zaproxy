/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP development team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.testutils.TestUtils;
import org.zaproxy.zap.utils.ClassLoaderUtil;
import org.zaproxy.zap.utils.I18N;

@RunWith(MockitoJUnitRunner.class)
public abstract class WithConfigsTest extends TestUtils {

    /**
     * A temporary directory where ZAP home/installation dirs are created.
     * <p>
     * Can be used for other temporary files/dirs.
     */
    @ClassRule
    public static TemporaryFolder tempDir = new TemporaryFolder();
    private static String zapInstallDir;
    private static String zapHomeDir;

    @BeforeClass
    public static void beforeClass() throws Exception {
        File installDir = tempDir.newFolder("install");
        Path langDir = Files.createDirectory(installDir.toPath().resolve("lang"));
        Files.createFile(langDir.resolve("Messages.properties"));
        Path xmlDir = Files.createDirectory(installDir.toPath().resolve("xml"));
        Files.createFile(xmlDir.resolve("log4j.properties"));
        Path configXmlPath = Files.createFile(xmlDir.resolve("config.xml"));
        Files.write(configXmlPath, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><config></config>".getBytes(StandardCharsets.UTF_8));

        zapInstallDir = installDir.getAbsolutePath();
        zapHomeDir = tempDir.newFolder("home").getAbsolutePath();
    }

    /**
     * Sets up ZAP, by initialising the home/installation dirs and core classes (for example, {@link Constant}, {@link Control},
     * {@link Model}).
     *
     * @throws Exception if an error occurred while setting up the dirs or core classes.
     */
    @Before
    public void setUpZap() throws Exception {
        Constant.setZapInstall(zapInstallDir);
        Constant.setZapHome(zapHomeDir);

        File langDir = new File(Constant.getZapInstall(), "lang");
        ClassLoaderUtil.addFile(langDir.getAbsolutePath());
        
        ExtensionLoader extLoader = Mockito.mock(ExtensionLoader.class);
        Control control = Mockito.mock(Control.class);
        Mockito.when (control.getExtensionLoader()).thenReturn(extLoader);

        // Init all the things
        Constant.getInstance();
        I18N i18n = Mockito.mock(I18N.class);
        given(i18n.getString(anyString())).willReturn("");
        given(i18n.getString(anyString(), anyObject())).willReturn("");
        given(i18n.getLocal()).willReturn(Locale.getDefault());
        Constant.messages = i18n;
        Control.initSingletonForTesting(Model.getSingleton());
    }
}