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

import java.util.Date;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.junit.BeforeClass;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.control.AddOn.Status;
import org.zaproxy.zap.model.Tech;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/**
 * Class with helper/utility methods to help testing classes involving {@code Plugin} class.
 *
 * @see Plugin
 */
public class PluginTestUtils {

    @BeforeClass
    public static void suppressLogging() {
        Logger.getRootLogger().addAppender(new NullAppender());
    }

    protected static AbstractPlugin createAbstractPluginWithConfig() {
        return createAbstractPluginWithConfig(0);
    }

    protected static AbstractPlugin createAbstractPluginWithConfig(int id) {
        AbstractPlugin plugin = createAbstractPlugin(id);
        plugin.setConfig(emptyConfig());
        return plugin;
    }

    protected static AbstractPlugin createAbstractPlugin() {
        return createAbstractPlugin(0);
    }

    protected static AbstractPlugin createAbstractPlugin(int id) {
        return new TestPlugin(id);
    }

    protected static AbstractPlugin createNonVisibleAbstractPlugin() {
        return new NonVisibleTestPlugin();
    }

    protected static AbstractPlugin createDeprecatedAbstractPlugin() {
        return new DeprecatedTestPlugin();
    }

    protected static Plugin createNonAbstractPlugin() {
        return new PluginImpl();
    }

    protected static ZapXmlConfiguration emptyConfig() {
        return new ZapXmlConfiguration();
    }

    private static class TestPlugin extends AbstractPlugin {

        private final int id;

        public TestPlugin() {
            this.id = 0;
        }

        public TestPlugin(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public int getCategory() {
            return 0;
        }

        @Override
        public String getSolution() {
            return null;
        }

        @Override
        public String getReference() {
            return null;
        }

        @Override
        public void scan() {
        }

        @Override
        public void notifyPluginCompleted(HostProcess parent) {
        }
    }

    private static class NonVisibleTestPlugin extends TestPlugin {

        public NonVisibleTestPlugin() {
        }

        @Override
        public boolean isVisible() {
            return false;
        }
    }

    private static class DeprecatedTestPlugin extends TestPlugin {

        public DeprecatedTestPlugin() {
        }

        @Override
        public boolean isDepreciated() {
            return true;
        }
    }

    private static class PluginImpl implements Plugin {

        @Override
        public void run() {
        }

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getCodeName() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public int getRisk() {
            return 0;
        }

        @Override
        public void init(HttpMessage msg, HostProcess parent) {
        }

        @Override
        public void scan() {
        }

        @Override
        public String[] getDependency() {
            return null;
        }

        @Override
        public void setEnabled(boolean enabled) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public int getCategory() {
            return 0;
        }

        @Override
        public String getSolution() {
            return null;
        }

        @Override
        public String getReference() {
            return null;
        }

        @Override
        public void notifyPluginCompleted(HostProcess parent) {
        }

        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public void setConfig(Configuration config) {

        }

        @Override
        public Configuration getConfig() {
            return null;
        }

        @Override
        public void saveTo(Configuration conf) {
        }

        @Override
        public void loadFrom(Configuration conf) {
        }

        @Override
        public void cloneInto(Plugin plugin) {
        }

        @Override
        public void createParamIfNotExist() {
        }

        @Override
        public boolean isDepreciated() {
            return false;
        }

        @Override
        public int getDelayInMs() {
            return 0;
        }

        @Override
        public void setDelayInMs(int delay) {
        }

        @Override
        public AlertThreshold getAlertThreshold(boolean incDefault) {
            return null;
        }

        @Override
        public AlertThreshold getAlertThreshold() {
            return null;
        }

        @Override
        public void setAlertThreshold(AlertThreshold level) {
        }

        @Override
        public void setDefaultAlertThreshold(AlertThreshold level) {
        }

        @Override
        public AlertThreshold[] getAlertThresholdsSupported() {
            return null;
        }

        @Override
        public AttackStrength getAttackStrength(boolean incDefault) {
            return null;
        }

        @Override
        public AttackStrength getAttackStrength() {
            return null;
        }

        @Override
        public void setAttackStrength(AttackStrength level) {
        }

        @Override
        public void setDefaultAttackStrength(AttackStrength strength) {
        }

        @Override
        public AttackStrength[] getAttackStrengthsSupported() {
            return null;
        }

        @Override
        public void setTechSet(TechSet ts) {
        }

        @Override
        public boolean inScope(Tech tech) {
            return false;
        }

        @Override
        public boolean targets(TechSet technologies) {
            return false;
        }

        @Override
        public void setTimeStarted() {
        }

        @Override
        public Date getTimeStarted() {
            return null;
        }

        @Override
        public void setTimeFinished() {
        }

        @Override
        public Date getTimeFinished() {
            return null;
        }

        @Override
        public int getCweId() {
            return 0;
        }

        @Override
        public int getWascId() {
            return 0;
        }

        @Override
        public Status getStatus() {
            return null;
        }
    }

}
