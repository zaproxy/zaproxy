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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.swing.ImageIcon;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.parosproxy.paros.view.View;

public class JavascriptEngineWrapper extends DefaultEngineWrapper {

    /**
     * The icon of the engine.
     *
     * <p>Lazily initialised.
     *
     * @see #getIcon()
     */
    private static ImageIcon icon;

    /**
     * Constructs a {@code JavascriptEngineWrapper} with the given engine (to obtain a factory).
     *
     * @param engine an engine to obtain the corresponding {@code ScriptEngineFactory}.
     * @deprecated (2.8.0) Use {@link #JavascriptEngineWrapper(ScriptEngineFactory)} instead.
     */
    @Deprecated
    public JavascriptEngineWrapper(ScriptEngine engine) {
        super(engine);
    }

    /**
     * Constructs a {@code JavascriptEngineWrapper} with the given engine factory.
     *
     * @param factory the factory to create {@code ScriptEngine}s and obtain engine data (for
     *     example, engine name, language).
     * @since 2.8.0
     * @see #getEngine()
     */
    public JavascriptEngineWrapper(ScriptEngineFactory factory) {
        super(factory);
    }

    @Override
    public ImageIcon getIcon() {
        if (!View.isInitialised()) {
            return null;
        }

        if (icon == null) {
            createIcon();
        }
        return icon;
    }

    private static synchronized void createIcon() {
        if (icon == null) {
            icon =
                    new ImageIcon(
                            JavascriptEngineWrapper.class.getResource("/resource/icon/16/cup.png"));
        }
    }

    @Override
    public String getSyntaxStyle() {
        return SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
    }

    @Override
    public boolean isTextBased() {
        return true;
    }

    @Override
    public boolean isRawEngine() {
        return false;
    }
}
