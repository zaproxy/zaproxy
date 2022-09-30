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

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreeNode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.extension.CommandLineListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.ExtensionFactory;
import org.zaproxy.zap.extension.script.ScriptsCache.Configuration;
import org.zaproxy.zap.utils.Stats;

public class ExtensionScript extends ExtensionAdaptor implements CommandLineListener {

    public static final int EXTENSION_ORDER = 60;
    public static final String NAME = "ExtensionScript";

    /** @deprecated (2.7.0) Use {@link #getScriptIcon()} instead. */
    @Deprecated public static final ImageIcon ICON = View.isInitialised() ? getScriptIcon() : null;

    /**
     * The {@code Charset} used to load/save the scripts from/to the file.
     *
     * <p>While the scripts can be loaded with any {@code Charset} (defaulting to this one) they are
     * always saved with this {@code Charset}.
     *
     * @since 2.7.0
     * @see #loadScript(ScriptWrapper)
     * @see #loadScript(ScriptWrapper, Charset)
     * @see #saveScript(ScriptWrapper)
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * The script icon.
     *
     * <p>Lazily initialised.
     *
     * @see #getScriptIcon()
     */
    private static ImageIcon scriptIcon;

    public static final String SCRIPTS_DIR = "scripts";
    public static final String TEMPLATES_DIR = SCRIPTS_DIR + File.separator + "templates";
    private static final String LANG_ENGINE_SEP = " : ";

    @Deprecated
    protected static final String SCRIPT_CONSOLE_HOME_PAGE =
            "https://github.com/zaproxy/zaproxy/wiki/ScriptConsole";

    protected static final String SCRIPT_NAME_ATT = "zap.script.name";

    public static final String TYPE_HTTP_SENDER = "httpsender";
    public static final String TYPE_PROXY = "proxy";
    public static final String TYPE_STANDALONE = "standalone";
    public static final String TYPE_TARGETED = "targeted";

    private ScriptEngineManager mgr = new ScriptEngineManager();
    private ScriptParam scriptParam = null;
    private OptionsScriptPanel optionsScriptPanel = null;

    private ScriptTreeModel treeModel = null;
    private List<ScriptEngineWrapper> engineWrappers = new ArrayList<>();
    private Map<String, ScriptType> typeMap = new HashMap<>();
    private ProxyListenerScript proxyListener = null;
    private HttpSenderScriptListener httpSenderScriptListener;

    private List<ScriptEventListener> listeners = new ArrayList<>();
    private MultipleWriters writers = new MultipleWriters();
    private ScriptUI scriptUI = null;

    private CommandLineArgument[] arguments = new CommandLineArgument[1];
    private static final int ARG_SCRIPT_IDX = 0;

    private static final Logger logger = LogManager.getLogger(ExtensionScript.class);

    /**
     * Flag that indicates if the scripts/templates should be loaded when a new script type is
     * registered.
     *
     * <p>This is to prevent loading scripts/templates of already installed scripts (ones that are
     * registered during ZAP initialisation) twice, while allowing to load the scripts/templates of
     * script types registered after initialisation (e.g. from installed add-ons).
     *
     * @since 2.4.0
     * @see #registerScriptType(ScriptType)
     * @see #optionsLoaded()
     */
    private boolean shouldLoadScriptsOnScriptTypeRegistration;

    /**
     * The directories added to the extension, to automatically add and remove its scripts.
     *
     * @see #addScriptsFromDir(File)
     * @see #removeScriptsFromDir(File)
     */
    private List<File> trackedDirs = Collections.synchronizedList(new ArrayList<>());

    /**
     * The script output listeners added to the extension.
     *
     * @see #addScriptOutputListener(ScriptOutputListener)
     * @see #getWriters(ScriptWrapper)
     * @see #removeScriptOutputListener(ScriptOutputListener)
     */
    private List<ScriptOutputListener> outputListeners = new CopyOnWriteArrayList<>();

    public ExtensionScript() {
        super(NAME);
        this.setOrder(EXTENSION_ORDER);

        ScriptEngine se = mgr.getEngineByName("ECMAScript");
        if (se != null) {
            this.registerScriptEngineWrapper(new JavascriptEngineWrapper(se.getFactory()));
        } else {
            logger.warn(
                    "No default JavaScript/ECMAScript engine found, some scripts might no longer work.");
        }
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("script.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        this.registerScriptType(
                new ScriptType(
                        TYPE_PROXY,
                        "script.type.proxy",
                        createIcon("/resource/icon/16/script-proxy.png"),
                        true));
        this.registerScriptType(
                new ScriptType(
                        TYPE_STANDALONE,
                        "script.type.standalone",
                        createIcon("/resource/icon/16/script-standalone.png"),
                        false,
                        new String[] {ScriptType.CAPABILITY_APPEND}));
        this.registerScriptType(
                new ScriptType(
                        TYPE_TARGETED,
                        "script.type.targeted",
                        createIcon("/resource/icon/16/script-targeted.png"),
                        false));
        this.registerScriptType(
                new ScriptType(
                        TYPE_HTTP_SENDER,
                        "script.type.httpsender",
                        createIcon("/resource/icon/16/script-httpsender.png"),
                        true));

        extensionHook.addSessionListener(new ClearScriptVarsOnSessionChange());

        extensionHook.addProxyListener(this.getProxyListener());
        extensionHook.addHttpSenderListener(getHttpSenderScriptListener());
        extensionHook.addOptionsParamSet(getScriptParam());

        extensionHook.addCommandLine(getCommandLineArguments());

        if (hasView()) {
            extensionHook.getHookView().addOptionPanel(getOptionsScriptPanel());
        } else {
            // No GUI so add stdout as a writer
            this.addWriter(new PrintWriter(System.out));
        }

        extensionHook.addApiImplementor(new ScriptAPI(this));
    }

    /**
     * Creates an {@code ImageIcon} with the given resource path, if in view mode.
     *
     * @param resourcePath the resource path of the icon, must not be {@code null}.
     * @return the icon, or {@code null} if not in view mode.
     */
    private ImageIcon createIcon(String resourcePath) {
        if (getView() == null) {
            return null;
        }
        return new ImageIcon(ExtensionScript.class.getResource(resourcePath));
    }

    private OptionsScriptPanel getOptionsScriptPanel() {
        if (optionsScriptPanel == null) {
            optionsScriptPanel = new OptionsScriptPanel(this);
        }
        return optionsScriptPanel;
    }

    private ProxyListenerScript getProxyListener() {
        if (this.proxyListener == null) {
            this.proxyListener = new ProxyListenerScript(this);
        }
        return this.proxyListener;
    }

    private HttpSenderScriptListener getHttpSenderScriptListener() {
        if (this.httpSenderScriptListener == null) {
            this.httpSenderScriptListener = new HttpSenderScriptListener(this);
        }
        return this.httpSenderScriptListener;
    }

    public List<String> getScriptingEngines() {
        List<String> engineNames = new ArrayList<>();
        List<ScriptEngineFactory> engines = mgr.getEngineFactories();
        for (ScriptEngineFactory engine : engines) {
            engineNames.add(engine.getLanguageName() + LANG_ENGINE_SEP + engine.getEngineName());
        }
        for (ScriptEngineWrapper sew : this.engineWrappers) {
            if (sew.isVisible() && !engines.contains(sew.getFactory())) {
                engineNames.add(sew.getLanguageName() + LANG_ENGINE_SEP + sew.getEngineName());
            }
        }

        Collections.sort(engineNames);
        return engineNames;
    }

    /**
     * Registers a new script engine wrapper.
     *
     * <p>The templates of the wrapped script engine are loaded, if any.
     *
     * <p>The engine is set to existing scripts targeting the given engine.
     *
     * @param wrapper the script engine wrapper that will be added, must not be {@code null}
     * @see #removeScriptEngineWrapper(ScriptEngineWrapper)
     * @see ScriptWrapper#setEngine(ScriptEngineWrapper)
     */
    public void registerScriptEngineWrapper(ScriptEngineWrapper wrapper) {
        logger.debug(
                "registerEngineWrapper {} : {}",
                wrapper.getLanguageName(),
                wrapper.getEngineName());
        this.engineWrappers.add(wrapper);

        setScriptEngineWrapper(getTreeModel().getScriptsNode(), wrapper, wrapper);
        setScriptEngineWrapper(getTreeModel().getTemplatesNode(), wrapper, wrapper);

        // Templates for this engine might not have been loaded
        this.loadTemplates(wrapper);

        synchronized (trackedDirs) {
            String engineName =
                    wrapper.getLanguageName() + LANG_ENGINE_SEP + wrapper.getEngineName();
            for (File dir : trackedDirs) {
                for (ScriptType type : this.getScriptTypes()) {
                    addScriptsFromDir(dir, type, engineName);
                }
            }
        }

        if (scriptUI != null) {
            try {
                scriptUI.engineAdded(wrapper);
            } catch (Exception e) {
                logger.error("An error occurred while notifying ScriptUI:", e);
            }
        }
    }

    /**
     * Sets the given {@code newEngineWrapper} to all children of {@code baseNode} that targets the
     * given {@code engineWrapper}.
     *
     * @param baseNode the node whose child nodes will have the engine set, must not be {@code null}
     * @param engineWrapper the script engine of the targeting scripts, must not be {@code null}
     * @param newEngineWrapper the script engine that will be set to the targeting scripts
     * @see ScriptWrapper#setEngine(ScriptEngineWrapper)
     */
    private void setScriptEngineWrapper(
            ScriptNode baseNode,
            ScriptEngineWrapper engineWrapper,
            ScriptEngineWrapper newEngineWrapper) {
        for (@SuppressWarnings("unchecked")
                Enumeration<TreeNode> e = baseNode.depthFirstEnumeration();
                e.hasMoreElements(); ) {
            ScriptNode node = (ScriptNode) e.nextElement();
            if (node.getUserObject() != null && (node.getUserObject() instanceof ScriptWrapper)) {
                ScriptWrapper scriptWrapper = (ScriptWrapper) node.getUserObject();
                if (hasSameScriptEngine(scriptWrapper, engineWrapper)) {
                    scriptWrapper.setEngine(newEngineWrapper);
                    if (newEngineWrapper == null) {
                        if (scriptWrapper.isEnabled()) {
                            setEnabled(scriptWrapper, false);
                            scriptWrapper.setPreviouslyEnabled(true);
                        }
                    } else if (scriptWrapper.isPreviouslyEnabled()) {
                        setEnabled(scriptWrapper, true);
                        scriptWrapper.setPreviouslyEnabled(false);
                    }
                }
            }
        }
    }

    /**
     * Tells whether or not the given {@code scriptWrapper} has the given {@code engineWrapper}.
     *
     * <p>If the given {@code scriptWrapper} has an engine set it's checked by reference (operator
     * {@code ==}), otherwise it's used the engine names.
     *
     * @param scriptWrapper the script wrapper that will be checked
     * @param engineWrapper the engine that will be checked against the engine of the script
     * @return {@code true} if the given script has the given engine, {@code false} otherwise.
     * @since 2.4.0
     * @see #isSameScriptEngine(String, String, String)
     */
    public static boolean hasSameScriptEngine(
            ScriptWrapper scriptWrapper, ScriptEngineWrapper engineWrapper) {
        if (scriptWrapper.getEngine() != null) {
            return scriptWrapper.getEngine() == engineWrapper;
        }

        return isSameScriptEngine(
                scriptWrapper.getEngineName(),
                engineWrapper.getEngineName(),
                engineWrapper.getLanguageName());
    }

    /**
     * Tells whether or not the given {@code name} matches the given {@code engineName} and {@code
     * engineLanguage}.
     *
     * @param name the name that will be checked against the given {@code engineName} and {@code
     *     engineLanguage}.
     * @param engineName the name of the script engine.
     * @param engineLanguage the language of the script engine.
     * @return {@code true} if the {@code name} matches the given engine's name and language, {@code
     *     false} otherwise.
     * @since 2.4.0
     * @see #hasSameScriptEngine(ScriptWrapper, ScriptEngineWrapper)
     */
    public static boolean isSameScriptEngine(
            String name, String engineName, String engineLanguage) {
        if (name == null) {
            return false;
        }

        // In the configs we just use the engine name, in the UI we use the language name as well
        if (name.indexOf(LANG_ENGINE_SEP) > 0) {
            if (name.equals(engineLanguage + LANG_ENGINE_SEP + engineName)) {
                return true;
            }
            return false;
        }

        return name.equals(engineName);
    }

    /**
     * Removes the given script engine wrapper.
     *
     * <p>The user's templates and scripts associated with the given engine are not removed but its
     * engine is set to {@code null}. Default templates are removed.
     *
     * <p>The call to this method has no effect if the given type is not registered.
     *
     * @param wrapper the script engine wrapper that will be removed, must not be {@code null}
     * @since 2.4.0
     * @see #registerScriptEngineWrapper(ScriptEngineWrapper)
     * @see ScriptWrapper#setEngine(ScriptEngineWrapper)
     */
    public void removeScriptEngineWrapper(ScriptEngineWrapper wrapper) {
        logger.debug(
                "Removing script engine: {} : {}",
                wrapper.getLanguageName(),
                wrapper.getEngineName());
        if (this.engineWrappers.remove(wrapper)) {
            if (scriptUI != null) {
                try {
                    scriptUI.engineRemoved(wrapper);
                } catch (Exception e) {
                    logger.error("An error occurred while notifying ScriptUI:", e);
                }
            }

            setScriptEngineWrapper(getTreeModel().getScriptsNode(), wrapper, null);
            processTemplatesOfRemovedEngine(getTreeModel().getTemplatesNode(), wrapper);
        }
    }

    private void processTemplatesOfRemovedEngine(
            ScriptNode baseNode, ScriptEngineWrapper engineWrapper) {
        @SuppressWarnings("unchecked")
        List<TreeNode> templateNodes = Collections.list(baseNode.depthFirstEnumeration());
        for (TreeNode tpNode : templateNodes) {
            ScriptNode node = (ScriptNode) tpNode;
            if (node.getUserObject() != null && (node.getUserObject() instanceof ScriptWrapper)) {
                ScriptWrapper scriptWrapper = (ScriptWrapper) node.getUserObject();
                if (hasSameScriptEngine(scriptWrapper, engineWrapper)) {
                    if (engineWrapper.isDefaultTemplate(scriptWrapper)) {
                        removeTemplate(scriptWrapper);
                    } else {
                        scriptWrapper.setEngine(null);
                        this.getTreeModel().nodeStructureChanged(scriptWrapper);
                    }
                }
            }
        }
    }

    public ScriptEngineWrapper getEngineWrapper(String name) {
        ScriptEngineWrapper sew = getEngineWrapperImpl(name);
        if (sew == null) {
            throw new InvalidParameterException("No such engine: " + name);
        }
        return sew;
    }

    /**
     * Gets the script engine with the given name.
     *
     * @param name the name of the script engine.
     * @return the engine, or {@code null} if not available.
     */
    private ScriptEngineWrapper getEngineWrapperImpl(String name) {
        for (ScriptEngineWrapper sew : this.engineWrappers) {
            if (isSameScriptEngine(name, sew.getEngineName(), sew.getLanguageName())) {
                return sew;
            }
        }

        // Not one we know of, create a default wrapper
        List<ScriptEngineFactory> engines = mgr.getEngineFactories();
        ScriptEngine engine = null;
        for (ScriptEngineFactory e : engines) {
            if (isSameScriptEngine(name, e.getEngineName(), e.getLanguageName())) {
                engine = e.getScriptEngine();
                break;
            }
        }
        if (engine != null) {
            DefaultEngineWrapper dew = new DefaultEngineWrapper(engine.getFactory());
            this.registerScriptEngineWrapper(dew);
            return dew;
        }
        return null;
    }

    public String getEngineNameForExtension(String ext) {
        ScriptEngine engine = mgr.getEngineByExtension(ext);
        if (engine != null) {
            return engine.getFactory().getLanguageName()
                    + LANG_ENGINE_SEP
                    + engine.getFactory().getEngineName();
        }
        for (ScriptEngineWrapper sew : this.engineWrappers) {
            if (sew.getExtensions() != null) {
                for (String extn : sew.getExtensions()) {
                    if (ext.equals(extn)) {
                        return sew.getLanguageName() + LANG_ENGINE_SEP + sew.getEngineName();
                    }
                }
            }
        }
        return null;
    }

    protected ScriptParam getScriptParam() {
        if (this.scriptParam == null) {
            this.scriptParam = new ScriptParam();
        }
        return this.scriptParam;
    }

    public ScriptTreeModel getTreeModel() {
        if (this.treeModel == null) {
            this.treeModel = new ScriptTreeModel();
        }
        return this.treeModel;
    }

    /**
     * Registers a new type of script.
     *
     * <p>The script is added to the tree of scripts and its scripts/templates loaded, if any.
     *
     * @param type the new type of script
     * @throws InvalidParameterException if a script type with same name is already registered
     * @see #removeScriptType(ScriptType)
     */
    public void registerScriptType(ScriptType type) {
        if (typeMap.containsKey(type.getName())) {
            throw new InvalidParameterException("ScriptType already registered: " + type.getName());
        }
        this.typeMap.put(type.getName(), type);
        this.getTreeModel().addType(type);

        if (shouldLoadScriptsOnScriptTypeRegistration) {
            addScripts(type);
            loadScriptTemplates(type);
        }

        synchronized (trackedDirs) {
            for (File dir : trackedDirs) {
                addScriptsFromDir(dir, type, null);
            }
        }
    }

    /**
     * Adds the (saved) scripts of the given script type.
     *
     * @param type the type of the script.
     * @see ScriptParam#getScripts()
     */
    private void addScripts(ScriptType type) {
        for (ScriptWrapper script : this.getScriptParam().getScripts()) {
            if (!type.getName().equals(script.getTypeName())) {
                continue;
            }

            try {
                loadScript(script);
                addScript(script, false, false);
            } catch (MalformedInputException e) {
                logger.warn(
                        "Failed to add script \"{}\", contains invalid character sequence (UTF-8).",
                        script.getName());
            } catch (InvalidParameterException | IOException e) {
                logger.error("Failed to add script: {}", script.getName(), e);
            }
        }
    }

    /**
     * Removes the given script type.
     *
     * <p>The templates and scripts associated with the given type are also removed, if any.
     *
     * <p>The call to this method has no effect if the given type is not registered.
     *
     * @param type the script type that will be removed
     * @since 2.4.0
     * @see #registerScriptType(ScriptType)
     * @deprecated (2.9.0) Use {@link #removeScriptType(ScriptType)} instead.
     */
    @Deprecated
    public void removeScripType(ScriptType type) {
        removeScriptType(type);
    }

    /**
     * Removes the given script type.
     *
     * <p>The templates and scripts associated with the given type are also removed, if any.
     *
     * <p>The call to this method has no effect if the given type is not registered.
     *
     * @param type the script type that will be removed
     * @since 2.9.0
     * @see #registerScriptType(ScriptType)
     */
    public void removeScriptType(ScriptType type) {
        ScriptType scriptType = typeMap.remove(type.getName());
        if (scriptType != null) {
            getTreeModel().removeType(scriptType);
        }
    }

    public ScriptType getScriptType(String name) {
        return this.typeMap.get(name);
    }

    public Collection<ScriptType> getScriptTypes() {
        return typeMap.values();
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("script.desc");
    }

    private void refreshScript(ScriptWrapper script) {
        for (ScriptEventListener listener : this.listeners) {
            try {
                listener.refreshScript(script);
            } catch (Exception e) {
                logScriptEventListenerException(listener, script, e);
            }
        }
    }

    private void reloadIfChangedOnDisk(ScriptWrapper script) {
        if (script.hasChangedOnDisk() && !script.isChanged()) {
            try {
                logger.debug(
                        "Reloading script as its been changed on disk {}",
                        script.getFile().getAbsolutePath());
                script.reloadScript();
            } catch (IOException e) {
                logger.error("Failed to reload script {}", script.getFile().getAbsolutePath(), e);
            }
        }
    }

    public ScriptWrapper getScript(String name) {
        ScriptWrapper script = getScriptImpl(name);
        if (script != null) {
            refreshScript(script);
            reloadIfChangedOnDisk(script);
        }
        return script;
    }

    /**
     * Gets the script with the given name.
     *
     * <p>Internal method that does not perform any actions on the returned script.
     *
     * @param name the name of the script.
     * @return the script, or {@code null} if it doesn't exist.
     * @see #getScript(String)
     */
    private ScriptWrapper getScriptImpl(String name) {
        return this.getTreeModel().getScript(name);
    }

    public ScriptNode addScript(ScriptWrapper script) {
        return this.addScript(script, true);
    }

    public ScriptNode addScript(ScriptWrapper script, boolean display) {
        return addScript(script, display, true);
    }

    private ScriptNode addScript(ScriptWrapper script, boolean display, boolean save) {
        if (script == null) {
            return null;
        }
        setEngine(script);
        ScriptNode node = this.getTreeModel().addScript(script);

        for (ScriptEventListener listener : this.listeners) {
            try {
                listener.scriptAdded(script, display);
            } catch (Exception e) {
                logScriptEventListenerException(listener, script, e);
            }
        }
        if (save && script.isLoadOnStart() && script.getFile() != null) {
            this.getScriptParam().addScript(script);
            this.getScriptParam().saveScripts();
        }
        return node;
    }

    private void logScriptEventListenerException(
            ScriptEventListener listener, ScriptWrapper script, Exception e) {
        String classname = listener.getClass().getCanonicalName();
        String scriptName = script.getName();
        logger.error(
                "Error while notifying '{}' with script '{}', cause: {}",
                classname,
                scriptName,
                e.getMessage(),
                e);
    }

    public void saveScript(ScriptWrapper script) throws IOException {
        refreshScript(script);
        script.saveScript();
        this.setChanged(script, false);
        // The removal is required for script that use wrappers, like Zest
        this.getScriptParam().removeScript(script);
        this.getScriptParam().addScript(script);
        this.getScriptParam().saveScripts();

        for (ScriptEventListener listener : this.listeners) {
            try {
                listener.scriptSaved(script);
            } catch (Exception e) {
                logScriptEventListenerException(listener, script, e);
            }
        }
    }

    public void removeScript(ScriptWrapper script) {
        script.setLoadOnStart(false);
        this.getScriptParam().removeScript(script);
        this.getScriptParam().saveScripts();
        this.getTreeModel().removeScript(script);
        for (ScriptEventListener listener : this.listeners) {
            try {
                listener.scriptRemoved(script);
            } catch (Exception e) {
                logScriptEventListenerException(listener, script, e);
            }
        }
    }

    public void removeTemplate(ScriptWrapper template) {
        this.getTreeModel().removeTemplate(template);
        for (ScriptEventListener listener : this.listeners) {
            try {
                listener.templateRemoved(template);
            } catch (Exception e) {
                logScriptEventListenerException(listener, template, e);
            }
        }
    }

    public ScriptNode addTemplate(ScriptWrapper template) {
        return this.addTemplate(template, true);
    }

    public ScriptNode addTemplate(ScriptWrapper template, boolean display) {
        if (template == null) {
            return null;
        }
        ScriptNode node = this.getTreeModel().addTemplate(template);

        for (ScriptEventListener listener : this.listeners) {
            try {
                listener.templateAdded(template, display);
            } catch (Exception e) {
                logScriptEventListenerException(listener, template, e);
            }
        }
        return node;
    }

    @Override
    public void postInit() {
        ScriptEngineWrapper ecmaScriptEngineWrapper = null;
        final List<String[]> scriptsNotAdded = new ArrayList<>(1);
        for (ScriptWrapper script : this.getScriptParam().getScripts()) {
            // Change scripts using Rhino (Java 7) script engine to Nashorn (Java 8+).
            if (script.getEngine() == null && isRhinoScriptEngine(script.getEngineName())) {
                if (ecmaScriptEngineWrapper == null) {
                    ecmaScriptEngineWrapper = getEcmaScriptEngineWrapper();
                }
                if (ecmaScriptEngineWrapper != null) {
                    logger.info(
                            "Changing [{}] (ECMAScript) script engine from [{}] to [{}].",
                            script.getName(),
                            script.getEngineName(),
                            ecmaScriptEngineWrapper.getEngineName());
                    script.setEngine(ecmaScriptEngineWrapper);
                }
            }

            try {
                this.loadScript(script);
                if (script.getType() != null) {
                    this.addScript(script, false, false);
                } else {
                    logger.warn(
                            "Failed to add script \"{}\", provided script type \"{}\" not found, available: {}",
                            script.getName(),
                            script.getTypeName(),
                            getScriptTypesNames());
                    scriptsNotAdded.add(
                            new String[] {
                                script.getName(),
                                script.getEngineName(),
                                Constant.messages.getString(
                                        "script.info.scriptsNotAdded.error.missingType",
                                        script.getTypeName())
                            });
                }

            } catch (MalformedInputException e) {
                logger.warn(
                        "Failed to add script \"{}\", contains invalid character sequence (UTF-8).",
                        script.getName());
                scriptsNotAdded.add(
                        new String[] {
                            script.getName(),
                            script.getEngineName(),
                            Constant.messages.getString(
                                    "script.info.scriptsNotAdded.error.invalidChars")
                        });
            } catch (InvalidParameterException | IOException e) {
                logger.error(e.getMessage(), e);
                scriptsNotAdded.add(
                        new String[] {
                            script.getName(),
                            script.getEngineName(),
                            Constant.messages.getString("script.info.scriptsNotAdded.error.other")
                        });
            }
        }

        informScriptsNotAdded(scriptsNotAdded);

        this.loadTemplates();

        for (File dir : this.getScriptParam().getScriptDirs()) {
            // Load the scripts from subdirectories of each directory configured
            int numAdded = addScriptsFromDir(dir);
            logger.debug("Added {} scripts from dir: {}", numAdded, dir.getAbsolutePath());
        }
        shouldLoadScriptsOnScriptTypeRegistration = true;

        Path defaultScriptsDir = Paths.get(Constant.getZapHome(), SCRIPTS_DIR, SCRIPTS_DIR);
        for (ScriptType scriptType : typeMap.values()) {
            Path scriptTypeDir = defaultScriptsDir.resolve(scriptType.getName());
            if (Files.notExists(scriptTypeDir)) {
                try {
                    Files.createDirectories(scriptTypeDir);
                } catch (IOException e) {
                    logger.warn(
                            "Failed to create directory for script type: {}",
                            scriptType.getName(),
                            e);
                }
            }
        }
    }

    private static boolean isRhinoScriptEngine(String engineName) {
        return "Mozilla Rhino".equals(engineName) || "Rhino".equals(engineName);
    }

    private ScriptEngineWrapper getEcmaScriptEngineWrapper() {
        for (ScriptEngineWrapper sew : this.engineWrappers) {
            if ("ECMAScript".equals(sew.getLanguageName())) {
                return sew;
            }
        }
        return null;
    }

    private List<String> getScriptTypesNames() {
        return getScriptTypes().stream()
                .collect(ArrayList::new, (c, e) -> c.add(e.getName()), ArrayList::addAll);
    }

    private void informScriptsNotAdded(final List<String[]> scriptsNotAdded) {
        if (!hasView() || scriptsNotAdded.isEmpty()) {
            return;
        }

        final List<Object> optionPaneContents = new ArrayList<>(2);
        optionPaneContents.add(Constant.messages.getString("script.info.scriptsNotAdded.message"));

        JXTable table =
                new JXTable(
                        new AbstractTableModel() {

                            private static final long serialVersionUID = -457689656746030560L;

                            @Override
                            public String getColumnName(int column) {
                                if (column == 0) {
                                    return Constant.messages.getString(
                                            "script.info.scriptsNotAdded.table.column.scriptName");
                                } else if (column == 1) {
                                    return Constant.messages.getString(
                                            "script.info.scriptsNotAdded.table.column.scriptEngine");
                                }
                                return Constant.messages.getString(
                                        "script.info.scriptsNotAdded.table.column.errorCause");
                            }

                            @Override
                            public Object getValueAt(int rowIndex, int columnIndex) {
                                return scriptsNotAdded.get(rowIndex)[columnIndex];
                            }

                            @Override
                            public int getRowCount() {
                                return scriptsNotAdded.size();
                            }

                            @Override
                            public int getColumnCount() {
                                return 3;
                            }
                        });

        table.setColumnControlVisible(true);
        table.setVisibleRowCount(Math.min(scriptsNotAdded.size() + 1, 5));
        table.packAll();
        optionPaneContents.add(new JScrollPane(table));

        EventQueue.invokeLater(
                new Runnable() {

                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(
                                getView().getMainFrame(),
                                optionPaneContents.toArray(),
                                Constant.PROGRAM_NAME,
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                });
    }

    /**
     * Adds the scripts from the given directory and starts tracking it.
     *
     * <p>The directory will be tracked to add or remove its scripts when a new script engine/type
     * is added or removed.
     *
     * <p>The scripts are expected to be under directories of the corresponding script type, for
     * example:
     *
     * <pre>{@code
     * (dir specified)
     * ├── active
     * │   ├── gof_lite.js
     * │   ├── TestInsecureHTTPVerbs.py
     * │   └── User defined attacks.js
     * ├── extender
     * │   └── HTTP Message Logger.js
     * ├── httpfuzzerprocessor
     * │   ├── add_msgs_sites_tree.js
     * │   ├── http_status_code_filter.py
     * │   └── showDifferences.js
     * ├── httpsender
     * │   ├── add_header_request.py
     * │   └── Capture and Replace Anti CSRF Token.js
     * └── variant
     *     └── JsonStrings.js
     * }</pre>
     *
     * where {@code active}, {@code extender}, {@code httpfuzzerprocessor}, {@code httpsender}, and
     * {@code variant} are the script types.
     *
     * @param dir the directory from where to add the scripts.
     * @return the number of scripts added.
     * @since 2.4.1
     * @see #removeScriptsFromDir(File)
     */
    public int addScriptsFromDir(File dir) {
        logger.debug("Adding scripts from dir: {}", dir.getAbsolutePath());
        trackedDirs.add(dir);
        int addedScripts = 0;
        for (ScriptType type : this.getScriptTypes()) {
            addedScripts += addScriptsFromDir(dir, type, null);
        }
        return addedScripts;
    }

    /**
     * Adds the scripts from the given directory of the given script type and, optionally, for the
     * engine with the given name.
     *
     * @param dir the directory from where to add the scripts.
     * @param type the script type, must not be {@code null}.
     * @param targetEngineName the engine that the scripts must be of, {@code null} for all engines.
     * @return the number of scripts added.
     */
    private int addScriptsFromDir(File dir, ScriptType type, String targetEngineName) {
        int addedScripts = 0;
        File typeDir = new File(dir, type.getName());
        if (typeDir.exists()) {
            for (File f : typeDir.listFiles()) {
                String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1);
                String engineName = this.getEngineNameForExtension(ext);

                if (engineName != null
                        && (targetEngineName == null || engineName.equals(targetEngineName))) {
                    try {
                        if (f.canWrite()) {
                            String scriptName = this.getUniqueScriptName(f.getName(), ext);
                            logger.debug("Loading script {}", scriptName);
                            ScriptWrapper sw =
                                    new ScriptWrapper(
                                            scriptName,
                                            "",
                                            this.getEngineWrapper(engineName),
                                            type,
                                            false,
                                            f);
                            this.loadScript(sw);
                            this.addScript(sw, false);
                        } else {
                            // Cant write so add as a template
                            String scriptName = this.getUniqueTemplateName(f.getName(), ext);
                            logger.debug("Loading script {}", scriptName);
                            ScriptWrapper sw =
                                    new ScriptWrapper(
                                            scriptName,
                                            "",
                                            this.getEngineWrapper(engineName),
                                            type,
                                            false,
                                            f);
                            this.loadScript(sw);
                            this.addTemplate(sw, false);
                        }
                        addedScripts++;
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                } else {
                    logger.debug("Ignoring {}", f.getName());
                }
            }
        }
        return addedScripts;
    }

    /**
     * Removes the scripts added from the given directory and stops tracking it.
     *
     * <p>The number of scripts removed might be different than the number of scripts initially
     * added, as a script engine or type might have been added or removed in the meantime.
     *
     * @param dir the directory previously added.
     * @return the number of scripts removed.
     * @since 2.4.1
     * @see #addScriptsFromDir(File)
     */
    public int removeScriptsFromDir(File dir) {
        logger.debug("Removing scripts from dir: {}", dir.getAbsolutePath());
        trackedDirs.remove(dir);
        int removedScripts = 0;

        for (ScriptType type : this.getScriptTypes()) {

            File locDir = new File(dir, type.getName());
            if (locDir.exists()) {
                // Loop through all of the known scripts and templates
                // removing any from this directory
                for (ScriptWrapper sw : this.getScripts(type)) {
                    if (isSavedInDir(sw, locDir)) {
                        this.removeScript(sw);
                        removedScripts++;
                    }
                }
                for (ScriptWrapper sw : this.getTemplates(type)) {
                    if (isSavedInDir(sw, locDir)) {
                        this.removeTemplate(sw);
                        removedScripts++;
                    }
                }
            }
        }
        return removedScripts;
    }

    /**
     * Tells whether or not the given script is saved in the given directory.
     *
     * @param scriptWrapper the script to check.
     * @param directory the directory where to check.
     * @return {@code true} if the script is saved in the given directory, {@code false} otherwise.
     */
    private static boolean isSavedInDir(ScriptWrapper scriptWrapper, File directory) {
        File file = scriptWrapper.getFile();
        if (file == null) {
            return false;
        }
        return file.getParentFile().equals(directory);
    }

    /**
     * Gets the numbers of scripts for the given directory for the currently registered script
     * engines and types.
     *
     * @param dir the directory to check.
     * @return the number of scripts.
     * @since 2.4.1
     */
    public int getScriptCount(File dir) {
        int scripts = 0;

        for (ScriptType type : this.getScriptTypes()) {
            File locDir = new File(dir, type.getName());
            if (locDir.exists()) {
                for (File f : locDir.listFiles()) {
                    String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1);
                    String engineName = this.getEngineNameForExtension(ext);
                    if (engineName != null) {
                        scripts++;
                    }
                }
            }
        }
        return scripts;
    }

    /*
     * Returns a unique name for the given script name
     */
    private String getUniqueScriptName(String name, String ext) {
        if (this.getScriptImpl(name) == null) {
            // Its unique
            return name;
        }
        // Its not unique, add a suitable index...
        String stub = name.substring(0, name.length() - ext.length() - 1);
        int index = 1;
        do {
            index++;
            name = stub + "(" + index + ")." + ext;
        } while (this.getScriptImpl(name) != null);

        return name;
    }

    /*
     * Returns a unique name for the given template name
     */
    private String getUniqueTemplateName(String name, String ext) {
        if (this.getTreeModel().getTemplate(name) == null) {
            // Its unique
            return name;
        }
        // Its not unique, add a suitable index...
        String stub = name.substring(0, name.length() - ext.length() - 1);
        int index = 1;
        do {
            index++;
            name = stub + "(" + index + ")." + ext;
        } while (this.getTreeModel().getTemplate(name) != null);

        return name;
    }

    private void loadTemplates() {
        this.loadTemplates(null);
    }

    private void loadTemplates(ScriptEngineWrapper engine) {
        for (ScriptType type : this.getScriptTypes()) {
            loadScriptTemplates(type, engine);
        }
    }

    /**
     * Loads script templates of the given {@code type}, for all script engines.
     *
     * @param type the script type whose templates will be loaded
     * @since 2.4.0
     * @see #loadScriptTemplates(ScriptType, ScriptEngineWrapper)
     */
    private void loadScriptTemplates(ScriptType type) {
        loadScriptTemplates(type, null);
    }

    /**
     * Loads script templates of the given {@code type} for the given {@code engine}.
     *
     * @param type the script type whose templates will be loaded
     * @param engine the script engine whose templates will be loaded for the given {@code script}
     * @since 2.4.0
     * @see #loadScriptTemplates(ScriptType)
     */
    private void loadScriptTemplates(ScriptType type, ScriptEngineWrapper engine) {
        File locDir =
                new File(
                        Constant.getZapHome()
                                + File.separator
                                + TEMPLATES_DIR
                                + File.separator
                                + type.getName());
        File stdDir =
                new File(
                        Constant.getZapInstall()
                                + File.separator
                                + TEMPLATES_DIR
                                + File.separator
                                + type.getName());

        // Load local files first, as these override any one included in the release
        if (locDir.exists()) {
            for (File f : locDir.listFiles()) {
                loadTemplate(f, type, engine, false);
            }
        }
        if (stdDir.exists()) {
            for (File f : stdDir.listFiles()) {
                // Dont log errors on duplicates - 'local' templates should take precedence
                loadTemplate(f, type, engine, true);
            }
        }
    }

    private void loadTemplate(
            File f, ScriptType type, ScriptEngineWrapper engine, boolean ignoreDuplicates) {
        if (f.getName().indexOf(".") > 0) {
            if (this.getTreeModel().getTemplate(f.getName()) == null) {
                String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1);
                String engineName = this.getEngineNameForExtension(ext);
                if (engineName != null
                        && (engine == null || engine.getExtensions().contains(ext))) {
                    try {
                        ScriptWrapper template =
                                new ScriptWrapper(
                                        f.getName(),
                                        "",
                                        this.getEngineWrapper(engineName),
                                        type,
                                        false,
                                        f);
                        this.loadScript(template);
                        this.addTemplate(template);
                    } catch (InvalidParameterException e) {
                        if (!ignoreDuplicates) {
                            logger.error(e.getMessage(), e);
                        }
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Loads the script from the file, using {@link #DEFAULT_CHARSET}.
     *
     * <p>If the file contains invalid byte sequences (for {@code DEFAULT_CHARSET}) it will be
     * loaded again using the {@link Charset#defaultCharset() (JVM) default charset}, to load
     * scripts saved with older ZAP versions (which relied on default charset).
     *
     * @param script the ScriptWrapper to be loaded (read script from file).
     * @return the {@code ScriptWrapper} with the actual script read from the file.
     * @throws IOException if an error occurred while reading the script from the file.
     * @throws IllegalArgumentException if the {@code script} is {@code null}.
     * @see #loadScript(ScriptWrapper, Charset)
     */
    public ScriptWrapper loadScript(ScriptWrapper script) throws IOException {
        try {
            return loadScript(script, DEFAULT_CHARSET);
        } catch (MalformedInputException e) {
            if (Charset.defaultCharset() == DEFAULT_CHARSET) {
                // No point trying the (JVM) default charset if it's the same...
                throw e;
            }

            logger.debug(
                    "Failed to load script [{}] using [{}], falling back to [{}].",
                    script.getName(),
                    DEFAULT_CHARSET,
                    Charset.defaultCharset(),
                    e);
            return loadScript(script, Charset.defaultCharset());
        }
    }

    /**
     * Loads the script from the file, using the given charset.
     *
     * @param script the ScriptWrapper to be loaded (read script from file).
     * @param charset the charset to use when reading the script from the file.
     * @return the {@code ScriptWrapper} with the actual script read from the file.
     * @throws IOException if an error occurred while reading the script from the file.
     * @throws IllegalArgumentException if the {@code script} or the {@code charset} is {@code
     *     null}.
     * @since 2.7.0
     * @see #loadScript(ScriptWrapper)
     */
    public ScriptWrapper loadScript(ScriptWrapper script, Charset charset) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("Parameter script must not be null.");
        }
        if (charset == null) {
            throw new IllegalArgumentException("Parameter charset must not be null.");
        }
        script.loadScript(charset);

        if (script.getType() == null) {
            // This happens when scripts are loaded from the configs as the types
            // may well not have been registered at that stage
            script.setType(this.getScriptType(script.getTypeName()));
        }
        setEngine(script);
        return script;
    }

    /**
     * Sets the engine script to the given script, if not already set.
     *
     * <p>Scripts loaded from the configuration file might not have the engine set when used.
     *
     * <p>Does nothing if the engine script is not available.
     *
     * @param script the script to set the engine.
     */
    private void setEngine(ScriptWrapper script) {
        if (script.getEngine() != null) {
            return;
        }
        ScriptEngineWrapper sew = getEngineWrapperImpl(script.getEngineName());
        if (sew == null) {
            return;
        }
        script.setEngine(sew);
    }

    public List<ScriptWrapper> getScripts(String type) {
        return this.getScripts(this.getScriptType(type));
    }

    public List<ScriptWrapper> getScripts(ScriptType type) {
        List<ScriptWrapper> scripts = new ArrayList<>();
        if (type == null) {
            return scripts;
        }
        for (ScriptNode node : this.getTreeModel().getNodes(type.getName())) {
            ScriptWrapper script = (ScriptWrapper) node.getUserObject();
            refreshScript(script);
            scripts.add((ScriptWrapper) node.getUserObject());
        }
        return scripts;
    }

    public List<ScriptWrapper> getTemplates(ScriptType type) {
        List<ScriptWrapper> scripts = new ArrayList<>();
        if (type == null) {
            return scripts;
        }
        for (ScriptWrapper script : this.getTreeModel().getTemplates(type)) {
            scripts.add(script);
        }
        return scripts;
    }

    /*
     * This extension supports any number of writers to be registered which all get written to for
     * ever script. It also supports script specific writers.
     */
    private Writer getWriters(ScriptWrapper script) {
        Writer delegatee = this.writers;
        Writer writer = script.getWriter();
        if (writer != null) {
            // Use the script specific writer in addition to the std one
            MultipleWriters scriptWriters = new MultipleWriters();
            scriptWriters.addWriter(writer);
            scriptWriters.addWriter(writers);
            delegatee = scriptWriters;
        }
        return new ScriptWriter(script, delegatee, outputListeners);
    }

    /**
     * Invokes the given {@code script}, synchronously, handling any {@code Exception} thrown during
     * the invocation.
     *
     * <p>The context class loader of caller thread is replaced with the class loader {@code
     * AddOnLoader} to allow the script to access classes of add-ons. If this behaviour is not
     * desired call the method {@code invokeScriptWithOutAddOnLoader} instead.
     *
     * @param script the script that will be invoked
     * @return an {@code Invocable} for the {@code script}, or {@code null} if none.
     * @throws ScriptException if the engine of the given {@code script} was not found.
     * @see #invokeScriptWithOutAddOnLoader(ScriptWrapper)
     * @see Invocable
     */
    public Invocable invokeScript(ScriptWrapper script) throws ScriptException {
        logger.debug("invokeScript {}", script.getName());
        preInvokeScript(script);

        ClassLoader previousContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ExtensionFactory.getAddOnLoader());
        try {
            return invokeScriptImpl(script);
        } finally {
            Thread.currentThread().setContextClassLoader(previousContextClassLoader);
        }
    }

    /**
     * Notifies the {@code ScriptEventListener}s that the given {@code script} should be refreshed,
     * resets the error and output states of the given {@code script} and notifies {@code
     * ScriptEventListener}s of the pre-invocation.
     *
     * <p>If the script does not have an engine it will be set one (if found).
     *
     * @param script the script that will be invoked after the call to this method
     * @throws ScriptException if the engine of the given {@code script} was not found.
     * @see ScriptEventListener#refreshScript(ScriptWrapper)
     * @see ScriptEventListener#preInvoke(ScriptWrapper)
     */
    private void preInvokeScript(ScriptWrapper script) throws ScriptException {
        setEngine(script);

        if (script.getEngine() == null) {
            throw new ScriptException("Failed to find script engine: " + script.getEngineName());
        }

        refreshScript(script);
        script.setLastErrorDetails("");
        script.setLastException(null);
        script.setLastOutput("");

        for (ScriptEventListener listener : this.listeners) {
            try {
                listener.preInvoke(script);
            } catch (Exception e) {
                logScriptEventListenerException(listener, script, e);
            }
        }
    }

    /**
     * Invokes the given {@code script}, handling any {@code Exception} thrown during the
     * invocation.
     *
     * <p>Script's (or default) {@code Writer} is set to the {@code ScriptContext} of the {@code
     * ScriptEngine} before the invocation.
     *
     * @param script the script that will be invoked
     * @return an {@code Invocable} for the {@code script}, or {@code null} if none.
     * @see #getWriters(ScriptWrapper)
     * @see Invocable
     */
    private Invocable invokeScriptImpl(ScriptWrapper script) {
        ScriptEngine se = script.getEngine().getEngine();
        Writer writer = getWriters(script);
        se.getContext().setWriter(writer);

        String scriptName = script.getName();
        se.getContext().setAttribute(ScriptEngine.FILENAME, scriptName, ScriptContext.ENGINE_SCOPE);
        // Set the script name as a context attribute - this is used for script level variables
        se.getContext().setAttribute(SCRIPT_NAME_ATT, scriptName, ScriptContext.ENGINE_SCOPE);

        se.put("control", Control.getSingleton());
        se.put("model", getModel());

        if (hasView()) {
            se.put("view", getView());
        }

        reloadIfChangedOnDisk(script);
        recordScriptCalledStats(script);

        try {
            se.eval(script.getContents());
        } catch (Exception e) {
            handleScriptException(script, writer, e);
        } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
            if (e.getCause() instanceof Exception) {
                handleScriptException(script, writer, (Exception) e.getCause());
            } else {
                handleUnspecifiedScriptError(script, writer, e.getMessage());
            }
        }

        if (se instanceof Invocable) {
            return (Invocable) se;
        }
        return null;
    }

    /**
     * Invokes the given {@code script}, synchronously, handling any {@code Exception} thrown during
     * the invocation.
     *
     * @param script the script that will be invoked/evaluated
     * @return an {@code Invocable} for the {@code script}, or {@code null} if none.
     * @throws ScriptException if the engine of the given {@code script} was not found.
     * @see #invokeScript(ScriptWrapper)
     * @see Invocable
     */
    public Invocable invokeScriptWithOutAddOnLoader(ScriptWrapper script) throws ScriptException {
        logger.debug("invokeScriptWithOutAddOnLoader {}", script.getName());
        preInvokeScript(script);

        return invokeScriptImpl(script);
    }

    /**
     * Handles exceptions thrown by scripts.
     *
     * <p>The given {@code exception} (if of type {@code ScriptException} the cause will be used
     * instead) will be written to the writer(s) associated with the given {@code script}, moreover
     * the script will be disabled and flagged that has an error.
     *
     * @param script the script that resulted in an exception, must not be {@code null}
     * @param exception the exception thrown, must not be {@code null}
     * @since 2.5.0
     * @see #setEnabled(ScriptWrapper, boolean)
     * @see #setError(ScriptWrapper, Exception)
     * @see #handleFailedScriptInterface(ScriptWrapper, String)
     * @see #handleScriptError(ScriptWrapper, String)
     * @see ScriptException
     */
    public void handleScriptException(ScriptWrapper script, Exception exception) {
        handleScriptException(script, getWriters(script), exception);
    }

    /**
     * Handles exceptions thrown by scripts.
     *
     * <p>The given {@code exception} (if of type {@code ScriptException} the cause will be used
     * instead) will be written to the given {@code writer} and the given {@code script} will be
     * disabled and flagged that has an error.
     *
     * @param script the script that resulted in an exception, must not be {@code null}
     * @param writer the writer associated with the script, must not be {@code null}
     * @param exception the exception thrown , must not be {@code null}
     * @see #setError(ScriptWrapper, Exception)
     * @see #setEnabled(ScriptWrapper, boolean)
     * @see ScriptException
     */
    private void handleScriptException(ScriptWrapper script, Writer writer, Exception exception) {
        recordScriptFailedStats(script);
        Exception cause = exception;
        if (cause instanceof ScriptException && cause.getCause() instanceof Exception) {
            // Dereference one level
            cause = (Exception) cause.getCause();
        }
        try {
            writer.append(cause.toString());
        } catch (IOException ignore) {
            logger.error(cause.getMessage(), cause);
        }
        this.setError(script, cause);
        this.setEnabled(script, false);
    }

    /**
     * Handles errors caused by scripts.
     *
     * <p>The given {@code error} will be written to the writer(s) associated with the given {@code
     * script}, moreover the script will be disabled and flagged that has an error.
     *
     * @param script the script that caused the error, must not be {@code null}.
     * @param error the error caused by the script, must not be {@code null}.
     * @since 2.9.0
     * @see #setEnabled(ScriptWrapper, boolean)
     * @see #setError(ScriptWrapper, String)
     * @see #handleScriptException(ScriptWrapper, Exception)
     */
    public void handleScriptError(ScriptWrapper script, String error) {
        recordScriptFailedStats(script);
        try {
            getWriters(script).append(error);
        } catch (IOException ignore) {
            // Nothing to do, callers should log the issue.
        }
        this.setError(script, error);
        this.setEnabled(script, false);
    }

    /**
     * Invokes the given {@code script}, synchronously, as a {@link TargetedScript}, handling any
     * {@code Exception} thrown during the invocation.
     *
     * <p>The context class loader of caller thread is replaced with the class loader {@code
     * AddOnLoader} to allow the script to access classes of add-ons.
     *
     * @param script the script to invoke.
     * @param msg the HTTP message to process.
     * @since 2.2.0
     * @see #getInterface(ScriptWrapper, Class)
     */
    public void invokeTargetedScript(ScriptWrapper script, HttpMessage msg) {
        validateScriptType(script, TYPE_TARGETED);

        Writer writer = getWriters(script);
        try {
            // Dont need to check if enabled as it can only be invoked manually
            TargetedScript s = this.getInterface(script, TargetedScript.class);

            if (s != null) {
                recordScriptCalledStats(script);
                s.invokeWith(msg);

            } else {
                handleUnspecifiedScriptError(
                        script,
                        writer,
                        Constant.messages.getString("script.interface.targeted.error"));
            }

        } catch (Exception e) {
            handleScriptException(script, writer, e);
        }
    }

    /**
     * Validates that the given {@code script} is of the given {@code scriptType}, throwing an
     * {@code IllegalArgumentException} if not.
     *
     * @param script the script that will be checked, must not be {@code null}
     * @param scriptType the expected type of the script, must not be {@code null}
     * @throws IllegalArgumentException if the given {@code script} is not the given {@code
     *     scriptType}.
     * @see ScriptWrapper#getTypeName()
     */
    private static void validateScriptType(ScriptWrapper script, String scriptType)
            throws IllegalArgumentException {
        if (!scriptType.equals(script.getTypeName())) {
            throw new IllegalArgumentException(
                    "Script "
                            + script.getName()
                            + " is not a '"
                            + scriptType
                            + "' script: "
                            + script.getTypeName());
        }
    }

    /**
     * Handles a failed attempt to convert a script into an interface.
     *
     * <p>The given {@code errorMessage} will be written to the writer(s) associated with the given
     * {@code script}, moreover it will be disabled and flagged that has an error.
     *
     * @param script the script that resulted in an exception, must not be {@code null}
     * @param errorMessage the message that will be written to the writer(s)
     * @since 2.5.0
     * @see #setEnabled(ScriptWrapper, boolean)
     * @see #setError(ScriptWrapper, Exception)
     * @see #handleScriptException(ScriptWrapper, Exception)
     */
    public void handleFailedScriptInterface(ScriptWrapper script, String errorMessage) {
        handleUnspecifiedScriptError(script, getWriters(script), errorMessage);
    }

    /**
     * Handles an unspecified error that occurred while calling or invoking a script.
     *
     * <p>The given {@code errorMessage} will be written to the given {@code writer} and the given
     * {@code script} will be disabled and flagged that has an error.
     *
     * @param script the script that failed to be called/invoked, must not be {@code null}
     * @param writer the writer associated with the script, must not be {@code null}
     * @param errorMessage the message that will be written to the given {@code writer}
     * @see #setError(ScriptWrapper, String)
     * @see #setEnabled(ScriptWrapper, boolean)
     */
    private void handleUnspecifiedScriptError(
            ScriptWrapper script, Writer writer, String errorMessage) {
        recordScriptFailedStats(script);
        try {
            writer.append(errorMessage);
        } catch (IOException e) {
            logger.debug("Failed to append script error message because of an exception:", e);
            logger.warn("Failed to append error message: {}", errorMessage);
        }
        this.setError(script, errorMessage);
        this.setEnabled(script, false);
    }

    /**
     * Invokes the given {@code script}, synchronously, as a {@link ProxyScript}, handling any
     * {@code Exception} thrown during the invocation.
     *
     * <p>The context class loader of caller thread is replaced with the class loader {@code
     * AddOnLoader} to allow the script to access classes of add-ons.
     *
     * @param script the script to invoke.
     * @param msg the HTTP message being proxied.
     * @param request {@code true} if processing the request, {@code false} otherwise.
     * @return {@code true} if the request should be forward to the server, {@code false} otherwise.
     * @since 2.2.0
     * @see #getInterface(ScriptWrapper, Class)
     */
    public boolean invokeProxyScript(ScriptWrapper script, HttpMessage msg, boolean request) {
        validateScriptType(script, TYPE_PROXY);

        Writer writer = getWriters(script);
        try {
            // Dont need to check if enabled as it can only be invoked manually
            ProxyScript s = this.getInterface(script, ProxyScript.class);

            if (s != null) {
                recordScriptCalledStats(script);
                if (request) {
                    return s.proxyRequest(msg);
                } else {
                    return s.proxyResponse(msg);
                }

            } else {
                handleUnspecifiedScriptError(
                        script,
                        writer,
                        Constant.messages.getString("script.interface.proxy.error"));
            }

        } catch (Exception e) {
            handleScriptException(script, writer, e);
        }
        // Return true so that the request is submitted - if we returned false all proxying would
        // fail on script errors
        return true;
    }

    /**
     * Invokes the given {@code script}, synchronously, as a {@link HttpSenderScript}, handling any
     * {@code Exception} thrown during the invocation.
     *
     * <p>The context class loader of caller thread is replaced with the class loader {@code
     * AddOnLoader} to allow the script to access classes of add-ons.
     *
     * @param script the script to invoke.
     * @param msg the HTTP message being sent/received.
     * @param initiator the initiator of the request.
     * @param sender the sender of the given {@code HttpMessage}.
     * @param request {@code true} if processing the request, {@code false} otherwise.
     * @since 2.4.1
     * @see #getInterface(ScriptWrapper, Class)
     */
    public void invokeSenderScript(
            ScriptWrapper script,
            HttpMessage msg,
            int initiator,
            HttpSender sender,
            boolean request) {
        validateScriptType(script, TYPE_HTTP_SENDER);

        Writer writer = getWriters(script);
        try {
            HttpSenderScript senderScript = this.getInterface(script, HttpSenderScript.class);

            if (senderScript != null) {
                recordScriptCalledStats(script);
                if (request) {
                    senderScript.sendingRequest(msg, initiator, new HttpSenderScriptHelper(sender));
                } else {
                    senderScript.responseReceived(
                            msg, initiator, new HttpSenderScriptHelper(sender));
                }
            } else {
                handleUnspecifiedScriptError(
                        script,
                        writer,
                        Constant.messages.getString("script.interface.httpsender.error"));
            }
        } catch (Exception e) {
            handleScriptException(script, writer, e);
        }
    }

    public void setChanged(ScriptWrapper script, boolean changed) {
        script.setChanged(changed);
        ScriptNode node = this.getTreeModel().getNodeForScript(script);
        if (node != null) {
            if (node.getNodeName().equals(script.getName())) {
                // The name is the same
                this.getTreeModel().nodeStructureChanged(script);
            } else {
                // The name has changed
                node.setNodeName(script.getName());
                this.getTreeModel().nodeStructureChanged(node.getParent());
            }
        }

        notifyScriptChanged(script);
    }

    /**
     * Notifies the {@code ScriptEventListener}s that the given {@code script} was changed.
     *
     * @param script the script that was changed, must not be {@code null}
     * @see #listeners
     * @see ScriptEventListener#scriptChanged(ScriptWrapper)
     */
    private void notifyScriptChanged(ScriptWrapper script) {
        for (ScriptEventListener listener : this.listeners) {
            try {
                listener.scriptChanged(script);
            } catch (Exception e) {
                logScriptEventListenerException(listener, script, e);
            }
        }
    }

    public void setEnabled(ScriptWrapper script, boolean enabled) {
        if (!script.getType().isEnableable()) {
            return;
        }

        if (enabled && script.getEngine() == null) {
            return;
        }

        script.setEnabled(enabled);
        this.getTreeModel().nodeStructureChanged(script);

        notifyScriptChanged(script);
    }

    public void setError(ScriptWrapper script, String details) {
        script.setError(true);
        script.setLastErrorDetails(details);
        script.setLastOutput(details);

        this.getTreeModel().nodeStructureChanged(script);

        for (ScriptEventListener listener : this.listeners) {
            try {
                listener.scriptError(script);
            } catch (Exception e) {
                logScriptEventListenerException(listener, script, e);
            }
        }
    }

    public void setError(ScriptWrapper script, Exception e) {
        script.setLastException(e);
        String message = e.getMessage();
        setError(script, message != null ? message : ExceptionUtils.getRootCauseMessage(e));
    }

    public void addListener(ScriptEventListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(ScriptEventListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Adds the given writer.
     *
     * <p>It will be written to each time a script writes some output.
     *
     * @param writer the writer to add.
     * @see #removeWriter(Writer)
     * @see #addScriptOutputListener(ScriptOutputListener)
     */
    public void addWriter(Writer writer) {
        this.writers.addWriter(writer);
    }

    /**
     * Removes the given writer.
     *
     * @param writer the writer to remove.
     * @see #addWriter(Writer)
     */
    public void removeWriter(Writer writer) {
        this.writers.removeWriter(writer);
    }

    /**
     * Adds the given script output listener.
     *
     * @param listener the listener to add.
     * @since 2.8.0
     * @throws NullPointerException if the given listener is {@code null}.
     * @see #removeScriptOutputListener(ScriptOutputListener)
     */
    public void addScriptOutputListener(ScriptOutputListener listener) {
        outputListeners.add(
                Objects.requireNonNull(listener, "The parameter listener must not be null."));
    }

    /**
     * Removes the given script output listener.
     *
     * @param listener the listener to remove.
     * @since 2.8.0
     * @throws NullPointerException if the given listener is {@code null}.
     * @see #addScriptOutputListener(ScriptOutputListener)
     */
    public void removeScriptOutputListener(ScriptOutputListener listener) {
        outputListeners.remove(
                Objects.requireNonNull(listener, "The parameter listener must not be null."));
    }

    public ScriptUI getScriptUI() {
        return scriptUI;
    }

    public void setScriptUI(ScriptUI scriptUI) {
        if (this.scriptUI != null) {
            throw new InvalidParameterException(
                    "A script UI has already been set - only one is supported");
        }
        this.scriptUI = scriptUI;
    }

    public void removeScriptUI() {
        this.scriptUI = null;
    }

    /**
     * Creates a scripts cache.
     *
     * @param <T> the target interface.
     * @param config the cache configuration
     * @return the scripts cache.
     * @since 2.10.0
     */
    public <T> ScriptsCache<T> createScriptsCache(Configuration<T> config) {
        return new ScriptsCache<>(this, config);
    }

    /**
     * Gets the interface {@code class1} from the given {@code script}. Might return {@code null} if
     * the {@code script} does not implement the interface.
     *
     * <p>First tries to get the interface directly from the {@code script} by calling the method
     * {@code ScriptWrapper.getInterface(Class)}, if it returns {@code null} the interface will be
     * extracted from the script after invoking it, using the method {@code
     * Invocable.getInterface(Class)}.
     *
     * <p>The context class loader of caller thread is replaced with the class loader {@code
     * AddOnLoader} to allow the script to access classes of add-ons. If this behaviour is not
     * desired call the method {@code getInterfaceWithOutAddOnLoader(} instead.
     *
     * @param script the script that will be invoked
     * @param class1 the interface that will be obtained from the script
     * @return the interface implemented by the script, or {@code null} if the {@code script} does
     *     not implement the interface.
     * @throws ScriptException if the engine of the given {@code script} was not found.
     * @throws IOException if an error occurred while obtaining the interface directly from the
     *     script ( {@code ScriptWrapper.getInterface(Class)})
     * @see #getInterfaceWithOutAddOnLoader(ScriptWrapper, Class)
     * @see ScriptWrapper#getInterface(Class)
     * @see Invocable#getInterface(Class)
     */
    public <T> T getInterface(ScriptWrapper script, Class<T> class1)
            throws ScriptException, IOException {

        ClassLoader previousContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ExtensionFactory.getAddOnLoader());
        try {
            T iface = script.getInterface(class1);

            if (iface != null) {
                // the script wrapper has overridden the usual scripting mechanism
                return iface;
            }
        } finally {
            Thread.currentThread().setContextClassLoader(previousContextClassLoader);
        }

        if (script.isRunnableStandalone()) {
            return null;
        }

        Invocable invocable = invokeScript(script);
        if (invocable != null) {
            return invocable.getInterface(class1);
        }
        return null;
    }

    /**
     * Gets the interface {@code clazz} from the given {@code script}. Might return {@code null} if
     * the {@code script} does not implement the interface.
     *
     * <p>First tries to get the interface directly from the {@code script} by calling the method
     * {@code ScriptWrapper.getInterface(Class)}, if it returns {@code null} the interface will be
     * extracted from the script after invoking it, using the method {@code
     * Invocable.getInterface(Class)}.
     *
     * @param script the script that will be invoked
     * @param clazz the interface that will be obtained from the script
     * @return the interface implemented by the script, or {@code null} if the {@code script} does
     *     not implement the interface.
     * @throws ScriptException if the engine of the given {@code script} was not found.
     * @throws IOException if an error occurred while obtaining the interface directly from the
     *     script ( {@code ScriptWrapper.getInterface(Class)})
     * @see #getInterface(ScriptWrapper, Class)
     * @see ScriptWrapper#getInterface(Class)
     * @see Invocable#getInterface(Class)
     */
    public <T> T getInterfaceWithOutAddOnLoader(ScriptWrapper script, Class<T> clazz)
            throws ScriptException, IOException {
        T iface = script.getInterface(clazz);
        if (iface != null) {
            // the script wrapper has overridden the usual scripting mechanism
            return iface;
        }
        return invokeScriptWithOutAddOnLoader(script).getInterface(clazz);
    }

    @Override
    public List<String> getUnsavedResources() {
        // Report all of the unsaved scripts
        List<String> list = new ArrayList<>();
        for (ScriptType type : this.getScriptTypes()) {
            for (ScriptWrapper script : this.getScripts(type)) {
                if (script.isChanged()) {
                    list.add(Constant.messages.getString("script.resource", script.getName()));
                }
            }
        }
        return list;
    }

    private void openCmdLineFile(File f) throws IOException, ScriptException {
        if (!f.exists()) {
            CommandLine.info(
                    Constant.messages.getString("script.cmdline.nofile", f.getAbsolutePath()));
            return;
        }
        if (!f.canRead()) {
            CommandLine.info(
                    Constant.messages.getString("script.cmdline.noread", f.getAbsolutePath()));
            return;
        }
        int dotIndex = f.getName().lastIndexOf(".");
        if (dotIndex <= 0) {
            CommandLine.info(
                    Constant.messages.getString("script.cmdline.noext", f.getAbsolutePath()));
            return;
        }
        String ext = f.getName().substring(dotIndex + 1);
        String engineName = this.getEngineNameForExtension(ext);
        if (engineName == null) {
            CommandLine.info(Constant.messages.getString("script.cmdline.noengine", ext));
            return;
        }
        ScriptWrapper sw =
                new ScriptWrapper(
                        f.getName(), "", engineName, this.getScriptType(TYPE_STANDALONE), true, f);

        this.loadScript(sw);
        this.addScript(sw);
        if (!hasView()) {
            // Only invoke if run from the command line
            // if the GUI is present then its up to the user to invoke it
            this.invokeScript(sw);
        }
    }

    @Override
    public void execute(CommandLineArgument[] args) {
        if (args[ARG_SCRIPT_IDX].isEnabled()) {
            for (String script : args[ARG_SCRIPT_IDX].getArguments()) {
                try {
                    openCmdLineFile(new File(script));
                } catch (Exception e) {
                    CommandLine.error(e.getMessage(), e);
                }
            }
        }
    }

    private CommandLineArgument[] getCommandLineArguments() {

        arguments[ARG_SCRIPT_IDX] =
                new CommandLineArgument(
                        "-script",
                        1,
                        null,
                        "",
                        "-script <script>         "
                                + Constant.messages.getString("script.cmdline.help"));
        return arguments;
    }

    @Override
    public boolean handleFile(File file) {
        int dotIndex = file.getName().lastIndexOf(".");
        if (dotIndex <= 0) {
            // No extension, cant work out which engine
            return false;
        }
        String ext = file.getName().substring(dotIndex + 1);
        String engineName = this.getEngineNameForExtension(ext);
        if (engineName == null) {
            // No engine for this extension, we cant handle this
            return false;
        }
        try {
            openCmdLineFile(file);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    @Override
    public List<String> getHandledExtensions() {
        // The list of all of the script extensions that can be handled from the command line
        List<String> exts = new ArrayList<>();
        for (ScriptEngineWrapper sew : this.engineWrappers) {
            exts.addAll(sew.getExtensions());
        }

        return exts;
    }

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }

    /**
     * Gets the script icon.
     *
     * <p>Should be called/used only when in view mode.
     *
     * @return the script icon, never {@code null}.
     * @since 2.7.0
     */
    public static ImageIcon getScriptIcon() {
        if (scriptIcon == null) {
            scriptIcon =
                    new ImageIcon(ExtensionScript.class.getResource("/resource/icon/16/059.png"));
        }
        return scriptIcon;
    }

    private static class ClearScriptVarsOnSessionChange implements SessionChangedListener {

        @Override
        public void sessionChanged(Session session) {}

        @Override
        public void sessionAboutToChange(Session session) {
            ScriptVars.clear();
        }

        @Override
        public void sessionScopeChanged(Session session) {}

        @Override
        public void sessionModeChanged(Mode mode) {}
    }

    /** A {@code Writer} that notifies {@link ScriptOutputListener}s when writing. */
    private static class ScriptWriter extends Writer {

        private final ScriptWrapper script;
        private final Writer delegatee;
        private final List<ScriptOutputListener> outputListeners;

        public ScriptWriter(
                ScriptWrapper script,
                Writer delegatee,
                List<ScriptOutputListener> outputListeners) {
            this.script = Objects.requireNonNull(script);
            this.delegatee = Objects.requireNonNull(delegatee);
            this.outputListeners = Objects.requireNonNull(outputListeners);
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            delegatee.write(cbuf, off, len);
            if (!outputListeners.isEmpty()) {
                String output = new String(cbuf, off, len);
                outputListeners.forEach(e -> e.output(script, output));
            }
        }

        @Override
        public void flush() throws IOException {
            delegatee.flush();
        }

        @Override
        public void close() throws IOException {
            delegatee.close();
        }
    }

    public static void recordScriptCalledStats(ScriptWrapper sw) {
        if (sw != null) {
            Stats.incCounter("stats.script.call." + sw.getEngineName() + "." + sw.getTypeName());
        }
    }

    public static void recordScriptFailedStats(ScriptWrapper sw) {
        if (sw != null) {
            Stats.incCounter("stats.script.error." + sw.getEngineName() + "." + sw.getTypeName());
        }
    }
}
