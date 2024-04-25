/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.script;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

public class ScriptParam extends AbstractParam {

    private static final String SCRIPTS_BASE_KEY = "script";

    private static final String PARAM_DEFAULT_SCRIPT = SCRIPTS_BASE_KEY + ".defaultScript";
    private static final String PARAM_DEFAULT_DIR = SCRIPTS_BASE_KEY + ".defaultDir";

    private static final String ALL_SCRIPTS_KEY = SCRIPTS_BASE_KEY + ".scripts";

    private static final String SCRIPT_NAME_KEY = "name";
    private static final String SCRIPT_DESC_KEY = "description";
    private static final String SCRIPT_ENGINE_KEY = "engine";
    private static final String SCRIPT_TYPE_KEY = "type";
    private static final String SCRIPT_FILE_KEY = "file";
    private static final String SCRIPT_ENABLED_KEY = "enabled";
    private static final String SCRIPT_DIRS = SCRIPTS_BASE_KEY + ".dirs";
    private static final String SCRIPT_CONFIRM_REMOVE_DIR = SCRIPTS_BASE_KEY + ".confRemdir";
    private static final String SCRIPT_ENABLE_SCRIPTS_FROM_DIRS =
            SCRIPTS_BASE_KEY + ".enableScriptsFromDirs";

    private static final Logger LOGGER = LogManager.getLogger(ScriptParam.class);

    private String defaultScript = null;
    private String defaultDir = null;
    private Set<ScriptWrapper> scripts;
    private List<File> scriptDirs;
    private boolean confirmRemoveDir = true;
    private boolean enableScriptsFromDirs;

    public ScriptParam() {}

    @Override
    protected void parse() {
        migrateOldKeys();
        defaultScript = getString(PARAM_DEFAULT_SCRIPT, "");
        defaultDir = getString(PARAM_DEFAULT_DIR, "");

        try {
            List<HierarchicalConfiguration> fields =
                    ((HierarchicalConfiguration) getConfig()).configurationsAt(ALL_SCRIPTS_KEY);
            this.scripts = new HashSet<>(fields.size());
            List<String> tempListNames = new ArrayList<>(fields.size());
            for (HierarchicalConfiguration sub : fields) {
                String name = sub.getString(SCRIPT_NAME_KEY, "");
                try {
                    if (!"".equals(name) && !tempListNames.contains(name)) {
                        tempListNames.add(name);

                        File file = new File(sub.getString(SCRIPT_FILE_KEY));
                        if (!file.exists()) {
                            LOGGER.error("Script '{}' does not exist", file.getAbsolutePath());
                            continue;
                        }

                        ScriptWrapper script =
                                new ScriptWrapper(
                                        sub.getString(SCRIPT_NAME_KEY),
                                        sub.getString(SCRIPT_DESC_KEY),
                                        sub.getString(SCRIPT_ENGINE_KEY),
                                        sub.getString(SCRIPT_TYPE_KEY),
                                        sub.getBoolean(SCRIPT_ENABLED_KEY),
                                        file);

                        script.setLoadOnStart(true); // Because it was saved ;)

                        scripts.add(script);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error while loading the script: {}", name, e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while loading the scripts: {}", e.getMessage(), e);
        }

        try {
            this.scriptDirs = new ArrayList<>();
            for (Object dirName : getConfig().getList(SCRIPT_DIRS)) {
                File f = new File((String) dirName);
                if (!f.exists() || !f.isDirectory()) {
                    LOGGER.error("Not a valid script directory: {}", dirName);
                } else {
                    scriptDirs.add(f);
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error while loading the script dirs: {}", e.getMessage(), e);
        }
        confirmRemoveDir = getBoolean(SCRIPT_CONFIRM_REMOVE_DIR, true);
        enableScriptsFromDirs = getBoolean(SCRIPT_ENABLE_SCRIPTS_FROM_DIRS, false);
    }

    private void migrateOldKeys() {
        String[] oldKeys = {"dirs", "confRemdir"};
        String[] params = {SCRIPT_DIRS, SCRIPT_CONFIRM_REMOVE_DIR};
        for (int i = 0; i < oldKeys.length; i++) {
            if (getConfig().containsKey(oldKeys[i])) {
                getConfig().setProperty(params[i], getConfig().getProperty(oldKeys[i]));
                getConfig().clearProperty(oldKeys[i]);
            }
        }
    }

    public void addScript(ScriptWrapper script) {
        this.scripts.add(script);
    }

    public void removeScript(ScriptWrapper script) {
        this.scripts.remove(script);
    }

    public void saveScripts() {
        ((HierarchicalConfiguration) getConfig()).clearTree(ALL_SCRIPTS_KEY);

        int i = 0;
        for (ScriptWrapper script : scripts) {
            if (script.isLoadOnStart()) {
                String elementBaseKey = ALL_SCRIPTS_KEY + "(" + i + ").";
                getConfig().setProperty(elementBaseKey + SCRIPT_NAME_KEY, script.getName());
                getConfig().setProperty(elementBaseKey + SCRIPT_DESC_KEY, script.getDescription());
                getConfig().setProperty(elementBaseKey + SCRIPT_ENGINE_KEY, script.getEngineName());
                getConfig().setProperty(elementBaseKey + SCRIPT_TYPE_KEY, script.getTypeName());
                getConfig().setProperty(elementBaseKey + SCRIPT_ENABLED_KEY, script.isEnabled());
                getConfig()
                        .setProperty(
                                elementBaseKey + SCRIPT_FILE_KEY,
                                script.getFile().getAbsolutePath());
                i++;
            }
        }
    }

    public Set<ScriptWrapper> getScripts() {
        return scripts;
    }

    /**
     * Saves the properties of the provided script to the configuration file. Currently, only the
     * `enabled` property of the script is saved.
     */
    void saveScriptProperties(ScriptWrapper script) {
        List<HierarchicalConfiguration> fields =
                ((HierarchicalConfiguration) getConfig()).configurationsAt(ALL_SCRIPTS_KEY);
        fields.stream()
                .filter(config -> script.getName().equals(config.getString(SCRIPT_NAME_KEY)))
                .findAny()
                .ifPresent(config -> config.setProperty(SCRIPT_ENABLED_KEY, script.isEnabled()));
    }

    public void addScriptDir(File dir) {
        this.scriptDirs.add(dir);
        saveScriptDirs();
    }

    public void removeScriptDir(File dir) {
        this.scriptDirs.remove(dir);
        saveScriptDirs();
    }

    private void saveScriptDirs() {
        getConfig().setProperty(SCRIPT_DIRS, this.scriptDirs);
    }

    public List<File> getScriptDirs() {
        return scriptDirs;
    }

    public void setScriptDirs(List<File> scriptDirs) {
        this.scriptDirs = scriptDirs;
        saveScriptDirs();
    }

    public String getDefaultScript() {
        return defaultScript;
    }

    public void setDefaultScript(String defaultScript) {
        this.defaultScript = defaultScript;
        getConfig().setProperty(PARAM_DEFAULT_SCRIPT, this.defaultScript);
    }

    public String getDefaultDir() {
        return defaultDir;
    }

    public void setDefaultDir(String defaultDir) {
        this.defaultDir = defaultDir;
        getConfig().setProperty(PARAM_DEFAULT_DIR, this.defaultDir);
    }

    public void setConfirmRemoveDir(boolean confirmRemoveDir) {
        this.confirmRemoveDir = confirmRemoveDir;
        getConfig().setProperty(SCRIPT_CONFIRM_REMOVE_DIR, this.confirmRemoveDir);
    }

    public boolean isConfirmRemoveDir() {
        return confirmRemoveDir;
    }

    public void setEnableScriptsFromDirs(boolean enableScriptsFromDirs) {
        this.enableScriptsFromDirs = enableScriptsFromDirs;
        getConfig().setProperty(SCRIPT_ENABLE_SCRIPTS_FROM_DIRS, this.enableScriptsFromDirs);
    }

    public boolean isEnableScriptsFromDirs() {
        return enableScriptsFromDirs;
    }
}
