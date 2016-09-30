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
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.model.Model;
//import org.zaproxy.zap.extension.ruleconfig.RuleConfigParam;
import org.zaproxy.zap.utils.ClassLoaderUtil;
import org.zaproxy.zap.utils.I18N;

@RunWith(MockitoJUnitRunner.class)
public abstract class WithConfigsTest {

    private static final String INSTALL_PATH = "test/resources/install";
    private static final File HOME_DIR = new File("test/resources/home");

    @BeforeClass
    public static void beforeClass() {
    }

    public WithConfigsTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
/*
        // Useful if you need to get some info when debugging
        BasicConfigurator.configure();
        ConsoleAppender ca = new ConsoleAppender();
        ca.setWriter(new OutputStreamWriter(System.out));
        ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
        Logger.getRootLogger().addAppender(ca);
        Logger.getRootLogger().setLevel(Level.DEBUG);
/**/
        Constant.setZapInstall(INSTALL_PATH);
        HOME_DIR.mkdirs();
        Constant.setZapHome(HOME_DIR.getAbsolutePath());

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
        Mockito.when (control.getExtensionLoader()).thenReturn(extLoader);
        
    }
    
    @After
    public void shutDown() throws Exception {
        FileUtils.deleteDirectory(HOME_DIR);
    }
}