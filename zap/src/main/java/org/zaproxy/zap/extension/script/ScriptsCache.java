/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * A collection of cached scripts.
 *
 * @since 2.10.0
 * @see ExtensionScript#createScriptsCache(Configuration)
 */
public class ScriptsCache<T> {

    private final ExtensionScript extensionScript;
    private final Configuration<T> config;
    private final InterfaceProvider<T> interfaceProvider;
    private final Map<ScriptWrapper, CachedScript<T>> cache;

    private List<CachedScript<T>> cachedScripts;

    ScriptsCache(ExtensionScript extensionScript, Configuration<T> config) {
        this.extensionScript = extensionScript;
        this.config = config;
        this.interfaceProvider =
                config.getInterfaceProvider() == null
                        ? createDefaultInterfaceProvider()
                        : config.getInterfaceProvider();
        this.cache = Collections.synchronizedMap(new HashMap<>());
        this.cachedScripts = Collections.emptyList();
    }

    private InterfaceProvider<T> createDefaultInterfaceProvider() {
        return (scriptWrapper, targetInterface) -> {
            T script = extensionScript.getInterface(scriptWrapper, targetInterface);
            if (script == null) {
                extensionScript.handleFailedScriptInterface(
                        scriptWrapper,
                        config.getInterfaceErrorMessageProvider().getErrorMessage(scriptWrapper));
            }
            return script;
        };
    }

    /**
     * Refreshes the cache.
     *
     * <p>Any scripts that are now disabled are removed, scripts that were changed are recreated.
     *
     * <p>Should be called when the scripts can be safely refreshed, for example, if a script needs
     * to be initialised before usage the cache should not be refreshed while it's being used.
     */
    public void refresh() {
        synchronized (cache) {
            List<ScriptWrapper> latestScripts = extensionScript.getScripts(config.getScriptType());
            cache.keySet().retainAll(latestScripts);
            List<CachedScript<T>> latestCachedScripts = new ArrayList<>();
            latestScripts.forEach(
                    scriptWrapper -> refreshScriptImpl(scriptWrapper, latestCachedScripts));

            cachedScripts = Collections.unmodifiableList(latestCachedScripts);
        }
    }

    private void refreshScriptImpl(
            ScriptWrapper scriptWrapper, List<CachedScript<T>> latestCachedScripts) {
        CachedScript<T> cachedScript = cache.computeIfAbsent(scriptWrapper, CachedScript::new);
        if (!cachedScript.isEnabled()) {
            cachedScript.setScript(null);
            return;
        }

        if (!cachedScript.hasChanged()) {
            latestCachedScripts.add(cachedScript);
            return;
        }

        cachedScript.setScript(null);
        try {
            T script = interfaceProvider.getInterface(scriptWrapper, config.getTargetInterface());
            if (script != null) {
                cachedScript.setScript(script);
                latestCachedScripts.add(cachedScript);
            }
        } catch (Exception e) {
            extensionScript.handleScriptException(scriptWrapper, e);
        }
    }

    /**
     * Executes the given action on all cached scripts.
     *
     * <p>Exceptions thrown during the execution of the action are handled by the {@code
     * ExtensionScript}.
     *
     * @param action the action to be executed on each cached script.
     * @see ExtensionScript#handleScriptException(ScriptWrapper, Exception)
     */
    public void execute(ScriptAction<T> action) {
        cachedScripts.forEach(
                e -> {
                    try {
                        e.execute(
                                () -> {
                                    action.apply(e.getScript());
                                    return null;
                                });
                    } catch (Exception ex) {
                        extensionScript.handleScriptException(e.getScriptWrapper(), ex);
                    }
                });
    }

    /**
     * Convenience method that refreshes the cached scripts and executes the given action.
     *
     * @param action the action applied to each cached script.
     * @see #refresh()
     * @see #execute(ScriptAction)
     */
    public void refreshAndExecute(ScriptAction<T> action) {
        refresh();
        execute(action);
    }

    /**
     * Executes the given action on all cached scripts.
     *
     * <p>Includes the corresponding script wrapper of each script.
     *
     * <p>Exceptions thrown during the execution of the action are handled by the {@code
     * ExtensionScript}.
     *
     * @param action the action to be executed on each cached script.
     * @see ExtensionScript#handleScriptException(ScriptWrapper, Exception)
     */
    public void execute(ScriptWrapperAction<T> action) {
        cachedScripts.forEach(
                e -> {
                    ScriptWrapper sw = e.getScriptWrapper();
                    try {
                        ExtensionScript.recordScriptCalledStats(sw);
                        e.execute(
                                () -> {
                                    action.apply(sw, e.getScript());
                                    return null;
                                });
                    } catch (Exception ex) {
                        extensionScript.handleScriptException(sw, ex);
                    }
                });
    }

    /**
     * Convenience method that refreshes the cached scripts and executes the given action.
     *
     * @param action the action applied to each cached script.
     * @see #refresh()
     * @see #execute(ScriptWrapperAction)
     */
    public void refreshAndExecute(ScriptWrapperAction<T> action) {
        refresh();
        execute(action);
    }

    /**
     * Gets the cached scripts.
     *
     * @return an unmodifiable list containing the cached scripts.
     */
    public List<CachedScript<T>> getCachedScripts() {
        return cachedScripts;
    }

    /**
     * A cached script, the interface and the corresponding script wrapper.
     *
     * @param <T> the type of the interface.
     */
    public static class CachedScript<T> {

        private final ScriptWrapper scriptWrapper;
        private int currentModCount;
        private T script;

        CachedScript(ScriptWrapper scriptWrapper) {
            this.scriptWrapper = scriptWrapper;
            this.currentModCount = scriptWrapper.getModCount();
        }

        /**
         * Gets the script wrapper.
         *
         * @return the script wrapper, never {@code null}.
         */
        public ScriptWrapper getScriptWrapper() {
            return scriptWrapper;
        }

        /**
         * The script, through the interface.
         *
         * @return the script, never {@code null} for users of the collection.
         */
        public T getScript() {
            return script;
        }

        void setScript(T script) {
            this.script = script;
        }

        void execute(Callable<T> action) throws Exception {
            if (isSyncAccess()) {
                synchronized (this) {
                    action.call();
                }
            } else {
                action.call();
            }
        }

        private boolean isSyncAccess() {
            ScriptEngineWrapper engine = scriptWrapper.getEngine();
            return engine != null && engine.isSingleThreaded();
        }

        boolean hasChanged() {
            if (script == null) {
                return true;
            }

            int previousModCount = currentModCount;
            currentModCount = scriptWrapper.getModCount();
            return previousModCount != currentModCount;
        }

        boolean isEnabled() {
            return scriptWrapper.isEnabled();
        }
    }

    /**
     * The configuration of the {@link ScriptsCache}.
     *
     * @param <T> the target type of the scripts.
     */
    public static class Configuration<T> {

        private final String scriptType;
        private final Class<T> targetInterface;
        private final InterfaceProvider<T> interfaceProvider;
        private final InterfaceErrorMessageProvider interfaceErrorMessageProvider;

        private Configuration(
                String scriptType,
                Class<T> targetInterface,
                InterfaceProvider<T> interfaceProvider,
                InterfaceErrorMessageProvider interfaceErrorMessageProvider) {
            this.scriptType = scriptType;
            this.targetInterface = targetInterface;
            this.interfaceProvider = interfaceProvider;
            this.interfaceErrorMessageProvider = interfaceErrorMessageProvider;
        }

        public String getScriptType() {
            return scriptType;
        }

        public Class<T> getTargetInterface() {
            return targetInterface;
        }

        public InterfaceProvider<T> getInterfaceProvider() {
            return interfaceProvider;
        }

        public InterfaceErrorMessageProvider getInterfaceErrorMessageProvider() {
            return interfaceErrorMessageProvider;
        }

        /**
         * Returns a new configuration builder.
         *
         * @param <T1> the target type of the scripts.
         * @return the configuration builder.
         */
        public static <T1> Builder<T1> builder() {
            return new Builder<>();
        }

        /**
         * A builder of configurations.
         *
         * @see #build()
         */
        public static class Builder<T> {

            private String scriptType;
            private Class<T> targetInterface;
            private InterfaceProvider<T> interfaceProvider;
            private InterfaceErrorMessageProvider interfaceErrorMessageProvider;

            private Builder() {}

            /**
             * Sets the script type.
             *
             * @param scriptType the script type.
             * @return this, for chaining.
             */
            public Builder<T> setScriptType(String scriptType) {
                this.scriptType = scriptType;
                return this;
            }

            /**
             * Sets the target interface.
             *
             * @param targetInterface the target interface.
             * @return this, for chaining.
             */
            public Builder<T> setTargetInterface(Class<T> targetInterface) {
                this.targetInterface = targetInterface;
                return this;
            }

            /**
             * Sets the provider of interfaces.
             *
             * @param interfaceProvider the provider of interfaces.
             * @return this, for chaining.
             */
            public Builder<T> setInterfaceProvider(InterfaceProvider<T> interfaceProvider) {
                this.interfaceProvider = interfaceProvider;
                return this;
            }

            /**
             * Sets the provider of error messages.
             *
             * @param interfaceErrorMessageProvider the provider of error messages.
             * @return this, for chaining.
             */
            public Builder<T> setInterfaceErrorMessageProvider(
                    InterfaceErrorMessageProvider interfaceErrorMessageProvider) {
                this.interfaceErrorMessageProvider = interfaceErrorMessageProvider;
                return this;
            }

            /**
             * Builds the configuration from the specified data.
             *
             * @return the build configuration.
             * @throws IllegalStateException if the script type or the target interface is not set.
             *     Or, the interface error message provider is set at the same time as the interface
             *     provider. The error message provider is not used when using an interface
             *     provider.
             */
            public final Configuration<T> build() {
                if (scriptType == null || scriptType.isEmpty()) {
                    throw new IllegalStateException("The script type must be set.");
                }
                if (targetInterface == null) {
                    throw new IllegalStateException("The target interface must be set.");
                }

                if (interfaceProvider != null && interfaceErrorMessageProvider != null) {
                    throw new IllegalStateException(
                            "The interface error message provider must not be set if using an interface provider.");
                }

                return new Configuration<>(
                        scriptType,
                        targetInterface,
                        interfaceProvider,
                        interfaceErrorMessageProvider);
            }
        }
    }

    /**
     * An action applied on a script, through the interface.
     *
     * @param <T> the type of the interface.
     */
    public interface ScriptAction<T> {
        /**
         * Applies the action on the given script.
         *
         * @param script the script.
         * @throws Exception if an error occurred while applying the action to the script.
         */
        void apply(T script) throws Exception;
    }

    /**
     * An action applied on a script, through the interface.
     *
     * <p>For convenience the corresponding wrapper is also provided.
     *
     * @param <T> the type of the interface.
     */
    public interface ScriptWrapperAction<T> {
        /**
         * Applies the action on the given script.
         *
         * @param wrapper the corresponding script wrapper.
         * @param script the script.
         * @throws Exception if an error occurred while applying the action to the script.
         */
        void apply(ScriptWrapper wrapper, T script) throws Exception;
    }

    /**
     * A provider of interfaces from scripts.
     *
     * @param <T> the type of the interface.
     */
    public interface InterfaceProvider<T> {
        /**
         * Gets the given interface from the given script wrapper.
         *
         * @param scriptWrapper the script wrapper.
         * @param targetInterface the target interface.
         * @return the interface or {@code null} if the script does not implement it.
         * @throws Exception if an error occurred while creating the interface.
         */
        T getInterface(ScriptWrapper scriptWrapper, Class<T> targetInterface) throws Exception;
    }

    /**
     * A provider of error messages, that indicates that a script wrapper does not implement the
     * target interface.
     */
    public interface InterfaceErrorMessageProvider {
        /**
         * Gets the error message that indicates that the script wrapper does not implement the
         * interface.
         *
         * @param scriptWrapper the script wrapper that does not implement the interface.
         * @return the error message.
         */
        String getErrorMessage(ScriptWrapper scriptWrapper);
    }
}
