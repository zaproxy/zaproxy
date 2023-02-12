/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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

import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.swing.ImageIcon;

public abstract class ScriptEngineWrapper {

    private final ScriptEngineFactory factory;

    /**
     * Constructs a {@code ScriptEngineWrapper} with the given engine (to obtain a factory).
     *
     * @param engine an engine to obtain the corresponding {@code ScriptEngineFactory}.
     * @deprecated (2.8.0) Use {@link #ScriptEngineWrapper(ScriptEngineFactory)} instead.
     */
    @Deprecated
    public ScriptEngineWrapper(ScriptEngine engine) {
        this(engine.getFactory());
    }

    /**
     * Constructs a {@code ScriptEngineWrapper} with the given engine factory.
     *
     * @param factory the factory to create {@code ScriptEngine}s and obtain engine data (for
     *     example, engine name, language).
     * @since 2.8.0
     * @see #getEngine()
     */
    public ScriptEngineWrapper(ScriptEngineFactory factory) {
        this.factory = factory;
    }

    public String getLanguageName() {
        return factory.getLanguageName();
    }

    public String getEngineName() {
        return factory.getEngineName();
    }

    public ScriptEngine getEngine() {
        return factory.getScriptEngine();
    }

    ScriptEngineFactory getFactory() {
        return factory;
    }

    public abstract boolean isTextBased();

    public abstract String getTemplate(String type);

    public abstract String getSyntaxStyle();

    public abstract ImageIcon getIcon();

    public List<String> getExtensions() {
        return factory.getExtensions();
    }

    /**
     * Tells whether or not this engine is single threaded.
     *
     * <p>Engines that are single threaded have their cached scripts executed by a single thread at
     * a time.
     *
     * @return {@code true} if the engine is single threaded, {@code false} otherwise.
     * @since 2.12.0
     * @see ScriptsCache
     */
    public boolean isSingleThreaded() {
        return false;
    }

    /**
     * Tells whether or not this engine should be visible to the user.
     *
     * <p>Engines that are not visible are not shown in the GUI nor listed through the API.
     *
     * <p>Engines are visible by default.
     *
     * @return {@code true} if the engine should be visible to the user, {@code false} otherwise.
     * @since 2.8.0
     */
    public boolean isVisible() {
        return true;
    }

    public abstract boolean isRawEngine();

    /**
     * Returns true if this engine supports script types without defined templates.
     *
     * @return {@code true} if the engine doesn't require templates, {@code false} otherwise.
     */
    public abstract boolean isSupportsMissingTemplates();

    /**
     * Tells whether or not the given {@code script} is a default template of this engine.
     *
     * <p>Default templates are automatically removed when the script engine is removed.
     *
     * @param script the script template that should be checked
     * @return {@code true} if the template script is default of this engine, {@code false}
     *     otherwise
     */
    public abstract boolean isDefaultTemplate(ScriptWrapper script);
}
