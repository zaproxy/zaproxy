/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2015 The ZAP Development Team
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/**
 * Unit test for {@link PluginFactory}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Constant.class)
public class PluginFactoryUnitTest extends PluginTestUtils {

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockConstantClass();
    }

    private void mockConstantClass() {
        PluginFactory pf = PowerMockito.mock(PluginFactory.class);
        Whitebox.setInternalState(pf, "loadedPlugins", null);

        Constant constant = PowerMockito.mock(Constant.class);
        Whitebox.setInternalState(constant, "instance", constant);
        Whitebox.setInternalState(constant, "zapInstall", ".");

        I18N i18n = PowerMockito.mock(I18N.class);
        given(i18n.getString(anyString())).willReturn("");
        given(i18n.getString(anyString(), anyObject())).willReturn("");
        Whitebox.setInternalState(constant, "messages", i18n);

        Model.getSingleton().getOptionsParam().load(new ZapXmlConfiguration());
    }

    @Test
    public void shouldNotHaveUndefinedPluginLoaded() {
        // Given
        AbstractPlugin undefinedPlugin = null;
        // When
        boolean loaded = PluginFactory.isPluginLoaded(undefinedPlugin);
        // Then
        assertThat(loaded, is(equalTo(false)));
    }

    @Test
    public void shouldAddLoadedPlugin() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        // When
        PluginFactory.loadedPlugin(plugin);
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(true)));
    }

    @Test
    public void shouldRemoveLoadedPlugin() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        PluginFactory.loadedPlugin(plugin);
        // When
        PluginFactory.unloadedPlugin(plugin);
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(false)));
    }

    @Test
    public void shouldHaveNoEffectRemovingANotYetLoadedPlugin() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        // When
        PluginFactory.unloadedPlugin(plugin);
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(false)));
    }

    @Test
    public void shouldHaveOnePluginByDefault() {
        // Given
        PluginFactory pluginFactory = new PluginFactory();
        // When
        pluginFactory.loadAllPlugin(emptyConfig());
        // Then
        assertThat(pluginFactory.getAllPlugin(), hasSize(equalTo(1)));
    }

    @Test
    public void shouldNotLoadNonVisiblePlugins() {
        // Given
        AbstractPlugin plugin = createNonVisibleAbstractPlugin();
        PluginFactory.loadedPlugin(plugin);
        PluginFactory pluginFactory = new PluginFactory();
        // When
        pluginFactory.loadAllPlugin(emptyConfig());
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(true)));
        assertThat(pluginFactory.getAllPlugin(), hasSize(equalTo(1)));
        assertThat(pluginFactory.getAllPlugin().get(0), is(not(equalTo((Plugin) plugin))));
    }

    @Test
    public void shouldNotLoadDeprecatedPlugins() {
        // Given
        AbstractPlugin plugin = createDeprecatedAbstractPlugin();
        PluginFactory.loadedPlugin(plugin);
        PluginFactory pluginFactory = new PluginFactory();
        // When
        pluginFactory.loadAllPlugin(emptyConfig());
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(true)));
        assertThat(pluginFactory.getAllPlugin(), hasSize(equalTo(1)));
        assertThat(pluginFactory.getAllPlugin().get(0), is(not(equalTo((Plugin) plugin))));
    }

    @Test
    public void shouldLoadVisibleNonDeprecatedPlugin() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        PluginFactory.loadedPlugin(plugin);
        PluginFactory pluginFactory = new PluginFactory();
        // When
        pluginFactory.loadAllPlugin(emptyConfig());
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(true)));
        assertThat(pluginFactory.getAllPlugin(), hasSize(equalTo(2)));
        assertThat(pluginFactory.getAllPlugin().get(0), is(not(equalTo((Plugin) plugin))));
        assertThat(pluginFactory.getAllPlugin().get(1), is(equalTo((Plugin) plugin)));
    }

    @Test
    public void shouldHaveNoEffectLoadingAPluginMoreThanOnce() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        PluginFactory pluginFactory = new PluginFactory();
        // When
        PluginFactory.loadedPlugin(plugin);
        PluginFactory.loadedPlugin(plugin);
        pluginFactory.loadAllPlugin(emptyConfig());
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(true)));
        assertThat(pluginFactory.getAllPlugin(), hasSize(equalTo(2)));
        assertThat(pluginFactory.getAllPlugin().get(0), is(not(equalTo((Plugin) plugin))));
        assertThat(pluginFactory.getAllPlugin().get(1), is(equalTo((Plugin) plugin)));
    }

    @Test
    public void shouldHaveDifferentPluginInstancesPerPluginFactory() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        PluginFactory.loadedPlugin(plugin);
        PluginFactory pluginFactory = new PluginFactory();
        PluginFactory otherPluginFactory = new PluginFactory();
        // When
        pluginFactory.loadAllPlugin(emptyConfig());
        otherPluginFactory.loadAllPlugin(emptyConfig());
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(true)));
        assertThat(pluginFactory.getAllPlugin(), hasSize(equalTo(2)));
        assertThat(otherPluginFactory.getAllPlugin(), hasSize(equalTo(2)));
        assertThat(pluginFactory.getAllPlugin().get(0), is(not(equalTo((Plugin) plugin))));
        assertThat(otherPluginFactory.getAllPlugin().get(0), is(not(equalTo((Plugin) plugin))));
        assertThat(pluginFactory.getAllPlugin().get(1), is(equalTo((Plugin) plugin)));
        assertThat(otherPluginFactory.getAllPlugin().get(1), is(equalTo((Plugin) plugin)));
        assertThat(pluginFactory.getAllPlugin().get(1), is(not(sameInstance(otherPluginFactory.getAllPlugin().get(1)))));
    }
}
