/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.ascan;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.Plugin.AttackStrength;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

public class ScanPolicy {

    private String name;
    private PluginFactory pluginFactory = new PluginFactory();
    private AlertThreshold defaultThreshold;
    private AttackStrength defaultStrength;
    private ZapXmlConfiguration conf;

    public ScanPolicy() {
        conf = new ZapXmlConfiguration();
        name = conf.getString("policy", "");
        pluginFactory.loadAllPlugin(conf);
        setDefaultThreshold(AlertThreshold.MEDIUM);
        setDefaultStrength(AttackStrength.MEDIUM);
    }

    public ScanPolicy(ZapXmlConfiguration conf) throws ConfigurationException {
        this.conf = conf;
        name = conf.getString("policy", "");
        pluginFactory.loadAllPlugin(conf);

        setDefaultThreshold(
                AlertThreshold.valueOf(
                        conf.getString("scanner.level", AlertThreshold.MEDIUM.name())));
        setDefaultStrength(
                AttackStrength.valueOf(
                        conf.getString("scanner.strength", AttackStrength.MEDIUM.name())));
    }

    public ScanPolicy(FileConfiguration conf) throws ConfigurationException {
        pluginFactory.loadAllPlugin(conf);
        this.conf = new ZapXmlConfiguration();
        name = "";
        setDefaultThreshold(AlertThreshold.MEDIUM);
        setDefaultStrength(AttackStrength.MEDIUM);
    }

    public ScanPolicy clonePolicy() throws ConfigurationException {
        return new ScanPolicy((ZapXmlConfiguration) this.conf.clone());
    }

    public void cloneInto(ScanPolicy policy) {
        policy.pluginFactory.loadFrom(this.pluginFactory);
        policy.defaultStrength = this.getDefaultStrength();
        policy.defaultThreshold = this.getDefaultThreshold();
    }

    public String getName() {
        return name;
    }

    public PluginFactory getPluginFactory() {
        return pluginFactory;
    }

    public AlertThreshold getDefaultThreshold() {
        return defaultThreshold;
    }

    public AttackStrength getDefaultStrength() {
        return defaultStrength;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefaultThreshold(AlertThreshold defaultThreshold) {
        this.defaultThreshold = defaultThreshold;
        for (Plugin plugin : pluginFactory.getAllPlugin()) {
            plugin.setDefaultAlertThreshold(defaultThreshold);
        }
    }

    public void setDefaultStrength(AttackStrength defaultStrength) {
        this.defaultStrength = defaultStrength;
        for (Plugin plugin : pluginFactory.getAllPlugin()) {
            plugin.setDefaultAttackStrength(defaultStrength);
        }
    }

    public void save() throws ConfigurationException {
        this.conf.save();
    }
}
