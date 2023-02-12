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
import java.util.Map;
import org.apache.commons.configuration.Configuration;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOn.Status;
import org.zaproxy.zap.model.Tech;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/**
 * Class with helper/utility methods to help testing classes involving {@code Plugin} class.
 *
 * @see Plugin
 */
class PluginTestUtils {

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

    protected static AbstractPlugin createDefaultPlugin() {
        return new TestPlugin(123456789, Alert.RISK_HIGH, 123, 456);
    }

    protected static AbstractPlugin createAbstractPlugin(int id) {
        return new TestPlugin(id);
    }

    protected static AbstractPlugin createAbstractPlugin(int id, int risk) {
        return new TestPlugin(id, risk);
    }

    protected static AbstractPlugin createAbstractPlugin(int id, int risk, AddOn.Status status) {
        AbstractPlugin plugin = createAbstractPlugin(id, risk);
        plugin.setStatus(status);
        return plugin;
    }

    protected static AbstractPlugin createAbstractPlugin(int id, String codeName) {
        return new TestPlugin(id, codeName);
    }

    protected static AbstractPlugin createAbstractPlugin(
            int id, String codeName, String... dependencies) {
        return new TestPlugin(id, codeName, dependencies);
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

        private static final int NOT_DEFINED = -1;

        private int id;
        private int risk;
        private String name;
        private String description;
        private String solution;
        private String reference;
        private int cweId;
        private int wascId;
        private String codeName;
        private String[] dependencies;

        TestPlugin() {
            this(0);
        }

        TestPlugin(int id) {
            this(id, NOT_DEFINED);
        }

        TestPlugin(int id, int risk) {
            this.id = id;
            this.risk = risk;
            this.name = "Plugin Name";
            this.description = "Plugin Description";
            this.solution = "Plugin Solution";
            this.reference = "Plugin Reference";
        }

        TestPlugin(int id, int risk, int cweId, int wascId) {
            this(id, risk);
            this.cweId = cweId;
            this.wascId = wascId;
        }

        TestPlugin(int id, String codeName) {
            this(id, codeName, (String[]) null);
        }

        TestPlugin(int id, String codeName, String... dependencies) {
            this(id);
            this.codeName = codeName;
            this.dependencies = dependencies;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getCodeName() {
            if (codeName != null) {
                return codeName;
            }
            return super.getCodeName();
        }

        @Override
        public String[] getDependency() {
            if (dependencies != null) {
                return dependencies;
            }
            return super.getDependency();
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public int getCategory() {
            return 0;
        }

        @Override
        public String getSolution() {
            return solution;
        }

        @Override
        public String getReference() {
            return reference;
        }

        @Override
        public int getCweId() {
            return cweId;
        }

        @Override
        public int getWascId() {
            return wascId;
        }

        @Override
        public void scan() {}

        @Override
        public void notifyPluginCompleted(HostProcess parent) {}

        @Override
        public int getRisk() {
            if (risk == NOT_DEFINED) {
                return super.getRisk();
            }
            return risk;
        }

        @Override
        public void cloneInto(Plugin plugin) {
            super.cloneInto(plugin);
            if (plugin instanceof TestPlugin) {
                ((TestPlugin) plugin).id = id;
                ((TestPlugin) plugin).risk = risk;
                ((TestPlugin) plugin).codeName = codeName;
                ((TestPlugin) plugin).dependencies = dependencies;
            }
        }

        @Override
        public String toString() {
            return getCodeName();
        }
    }

    private static class NonVisibleTestPlugin extends TestPlugin {

        NonVisibleTestPlugin() {}

        @Override
        public boolean isVisible() {
            return false;
        }
    }

    private static class DeprecatedTestPlugin extends TestPlugin {

        DeprecatedTestPlugin() {}

        @Override
        public boolean isDepreciated() {
            return true;
        }
    }

    private static class PluginImpl implements Plugin {

        @Override
        public void run() {}

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
        public void init(HttpMessage msg, HostProcess parent) {}

        @Override
        public void scan() {}

        @Override
        public String[] getDependency() {
            return null;
        }

        @Override
        public void setEnabled(boolean enabled) {}

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
        public void notifyPluginCompleted(HostProcess parent) {}

        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public void setConfig(Configuration config) {}

        @Override
        public Configuration getConfig() {
            return null;
        }

        @Override
        public void saveTo(Configuration conf) {}

        public @Override void loadFrom(Configuration conf) {}

        @Override
        public void cloneInto(Plugin plugin) {}

        @Override
        public void createParamIfNotExist() {}

        @Override
        public boolean isDepreciated() {
            return false;
        }

        @Override
        public int getDelayInMs() {
            return 0;
        }

        @Override
        public void setDelayInMs(int delay) {}

        @Override
        public AlertThreshold getAlertThreshold(boolean incDefault) {
            return null;
        }

        @Override
        public AlertThreshold getAlertThreshold() {
            return null;
        }

        @Override
        public void setAlertThreshold(AlertThreshold level) {}

        @Override
        public void setDefaultAlertThreshold(AlertThreshold level) {}

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
        public void setAttackStrength(AttackStrength level) {}

        @Override
        public void setDefaultAttackStrength(AttackStrength strength) {}

        @Override
        public AttackStrength[] getAttackStrengthsSupported() {
            return null;
        }

        @Override
        public void setTechSet(TechSet ts) {}

        @Override
        public boolean inScope(Tech tech) {
            return false;
        }

        @Override
        public boolean targets(TechSet technologies) {
            return false;
        }

        @Override
        public void setTimeStarted() {}

        @Override
        public Date getTimeStarted() {
            return null;
        }

        @Override
        public void setTimeFinished() {}

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

        @Override
        public Map<String, String> getAlertTags() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }
    }
}
