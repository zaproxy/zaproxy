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
package org.zaproxy.zap.extension.ruleconfig;

import java.util.List;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;

public class ExtensionRuleConfig extends ExtensionAdaptor {

    public static final String NAME = "ExtensionRuleConfig";

    private OptionsRuleConfigPanel optionsRuleConfigPanel;
    private RuleConfigParam ruleConfigParam;

    public ExtensionRuleConfig() {
        super(NAME);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("ruleconfig.name");
    }

    public void addRuleconfig(RuleConfig rc) {
        this.getRuleConfigParam().addRuleConfig(rc);
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        extensionHook.addOptionsParamSet(getRuleConfigParam());
        if (extensionHook.getView() != null) {
            extensionHook.getHookView().addOptionPanel(getOptionsRuleConfigPanel());
        }

        extensionHook.addApiImplementor(new RuleConfigAPI(this));
    }

    public RuleConfigParam getRuleConfigParam() {
        if (ruleConfigParam == null) {
            ruleConfigParam = new RuleConfigParam();
        }
        return ruleConfigParam;
    }

    public List<RuleConfig> getAllRuleConfigs() {
        return this.getRuleConfigParam().getAllRuleConfigs();
    }

    public RuleConfig getRuleConfig(String key) {
        RuleConfig rc = this.getRuleConfigParam().getRuleConfig(key);
        if (rc != null) {
            return rc.clone();
        }
        return null;
    }

    public void setRuleConfigValue(String key, String value) {
        this.getRuleConfigParam().setRuleConfigValue(key, value);
    }

    public void resetRuleConfigValue(String key) {
        this.getRuleConfigParam().resetRuleConfigValue(key);
    }

    public void resetAllRuleConfigValues() {
        this.getRuleConfigParam().resetAllRuleConfigValues();
    }

    private OptionsRuleConfigPanel getOptionsRuleConfigPanel() {
        if (optionsRuleConfigPanel == null) {
            optionsRuleConfigPanel = new OptionsRuleConfigPanel(this);
        }
        return optionsRuleConfigPanel;
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("ruleconfig.desc");
    }

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }
}
