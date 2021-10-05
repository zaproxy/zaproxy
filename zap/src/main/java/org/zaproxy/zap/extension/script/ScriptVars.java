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
package org.zaproxy.zap.extension.script;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.script.ScriptContext;

/**
 * Manages global and script variables.
 *
 * <p>The global variables are meant to be accessible by all scripts and the script variables are
 * meant to be accessible by just a given script (identified by its name).
 *
 * <p><strong>Note:</strong> While it's possible to a script to access the variables of another
 * script that usage is discouraged.
 *
 * <p>The keys and (string) values of the variables have restrictions on its character length:
 *
 * <ul>
 *   <li>Key: {@value #MAX_KEY_SIZE};
 *   <li>Value: {@value #MAX_VALUE_SIZE};
 * </ul>
 *
 * <p>There's a maximum number of global/script variables:
 *
 * <ul>
 *   <li>Global: {@value #MAX_GLOBAL_VARS};
 *   <li>Script: {@value #MAX_SCRIPT_VARS};
 * </ul>
 *
 * @since 2.4.0
 */
public final class ScriptVars {

    // Relaxed accessibility for tests.
    static final int MAX_KEY_SIZE = 30;
    static final int MAX_VALUE_SIZE = 1024 * 1024;
    static final int MAX_SCRIPT_VARS = 20;
    static final int MAX_GLOBAL_VARS = 50;

    private static Map<String, String> globalVars = Collections.synchronizedMap(new HashMap<>());
    private static Map<String, Object> globalCustomVars =
            Collections.synchronizedMap(new HashMap<>());
    private static Map<String, Map<String, String>> scriptVars =
            Collections.synchronizedMap(new HashMap<>());
    private static Map<String, Map<String, Object>> scriptCustomVars =
            Collections.synchronizedMap(new HashMap<>());

    private ScriptVars() {}

    /**
     * Sets or removes a global variable.
     *
     * <p>The variable is removed when the {@code value} is {@code null}.
     *
     * @param key the key of the variable.
     * @param value the value of the variable.
     * @throws IllegalArgumentException if one of the following conditions is met:
     *     <ul>
     *       <li>The {@code key} is {@code null} or its length is higher than the maximum allowed
     *           ({@value #MAX_KEY_SIZE});
     *       <li>The {@code value}'s length is higher than the maximum allowed ({@value
     *           #MAX_VALUE_SIZE});
     *       <li>The number of global variables is higher than the maximum allowed ({@value
     *           #MAX_GLOBAL_VARS});
     *     </ul>
     */
    public static void setGlobalVar(String key, String value) {
        if (value != null) {
            validateValueLength(value);
        }
        setVar(globalVars, key, value);
    }

    private static <T> void setVar(Map<String, T> vars, String key, T value) {
        validateKey(key);

        if (value == null) {
            vars.remove(key);
        } else {
            if (vars.size() > MAX_GLOBAL_VARS) {
                throw new IllegalArgumentException(
                        "Maximum number of global variables reached: " + MAX_GLOBAL_VARS);
            }
            vars.put(key, value);
        }
    }

    /**
     * Validates the given key.
     *
     * @param key the key to validate.
     * @throws IllegalArgumentException if the {@code key} is {@code null} or its length is higher
     *     than the maximum allowed ({@value #MAX_KEY_SIZE}).
     */
    private static void validateKey(String key) {
        if (key == null || key.length() > MAX_KEY_SIZE) {
            throw new IllegalArgumentException(
                    "Invalid key - must be non null and have a length less than " + MAX_KEY_SIZE);
        }
    }

    /**
     * Validates the length of the given value.
     *
     * @param value the value to validate, must not be {@code null}.
     * @throws IllegalArgumentException if the length is higher than the maximum allowed ({@value
     *     #MAX_VALUE_SIZE}).
     */
    private static void validateValueLength(String value) {
        if (value.length() > MAX_VALUE_SIZE) {
            throw new IllegalArgumentException(
                    "Invalid value - must have a length less than " + MAX_VALUE_SIZE);
        }
    }

    /**
     * Gets a global variable.
     *
     * @param key the key of the variable.
     * @return the value of the variable, might be {@code null} if no value was previously set.
     */
    public static String getGlobalVar(String key) {
        return globalVars.get(key);
    }

    /**
     * Gets an unmodifiable map, variable key to value, containing the global variables.
     *
     * <p>Iterations should be done in a synchronised block using the returned map.
     *
     * @return an unmodifiable map containing the global variables, never {@code null}.
     * @since 2.8.0
     */
    public static Map<String, String> getGlobalVars() {
        return Collections.unmodifiableMap(globalVars);
    }

    /**
     * Sets or removes a global custom variable.
     *
     * <p>The variable is removed when the {@code value} is {@code null}.
     *
     * @param key the key of the variable.
     * @param value the value of the variable.
     * @throws IllegalArgumentException if one of the following conditions is met:
     *     <ul>
     *       <li>The {@code key} is {@code null} or its length is higher than the maximum allowed
     *           ({@value #MAX_KEY_SIZE});
     *       <li>The number of global variables is higher than the maximum allowed ({@value
     *           #MAX_GLOBAL_VARS});
     *     </ul>
     *
     * @since 2.9.0
     */
    public static void setGlobalCustomVar(String key, Object value) {
        setVar(globalCustomVars, key, value);
    }

    /**
     * Gets a global custom variable.
     *
     * @param key the key of the variable.
     * @return the value of the variable, might be {@code null} if no value was previously set.
     * @since 2.9.0
     */
    public static Object getGlobalCustomVar(String key) {
        return globalCustomVars.get(key);
    }

    /**
     * Gets an unmodifiable map, variable key to value, containing the global custom variables.
     *
     * <p>Iterations should be done in a synchronised block using the returned map.
     *
     * @return an unmodifiable map containing the global variables, never {@code null}.
     * @since 2.9.0
     */
    public static Map<String, Object> getGlobalCustomVars() {
        return Collections.unmodifiableMap(globalCustomVars);
    }

    /**
     * Sets or removes a script variable.
     *
     * <p>The variable is removed when the {@code value} is {@code null}.
     *
     * @param context the context of the script.
     * @param key the key of the variable.
     * @param value the value of the variable.
     * @throws IllegalArgumentException if one of the following conditions is met:
     *     <ul>
     *       <li>The {@code context} is {@code null} or it does not contain the name of the script;
     *       <li>The {@code key} is {@code null} or its length is higher than the maximum allowed
     *           ({@value #MAX_KEY_SIZE});
     *       <li>The {@code value}'s length is higher than the maximum allowed ({@value
     *           #MAX_VALUE_SIZE});
     *       <li>The number of script variables is higher than the maximum allowed ({@value
     *           #MAX_SCRIPT_VARS});
     *     </ul>
     */
    public static void setScriptVar(ScriptContext context, String key, String value) {
        setScriptVarImpl(getScriptName(context), key, value);
    }

    /**
     * Gets the script name from the given context.
     *
     * @param context the context of the script.
     * @return the name of the script, never {@code null}.
     * @throws IllegalArgumentException if the {@code context} is {@code null} or it does not
     *     contain the name of the script.
     */
    private static String getScriptName(ScriptContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Invalid context - must be non null");
        }
        Object scriptName = context.getAttribute(ExtensionScript.SCRIPT_NAME_ATT);
        if (scriptName == null) {
            throw new IllegalArgumentException("Failed to find script name in the script context.");
        }
        if (!(scriptName instanceof String)) {
            throw new IllegalArgumentException(
                    "The script name is not a String: " + scriptName.getClass().getCanonicalName());
        }
        return (String) scriptName;
    }

    /**
     * Sets or removes a script variable.
     *
     * <p>The variable is removed when the {@code value} is {@code null}.
     *
     * @param scriptName the name of the script.
     * @param key the key of the variable.
     * @param value the value of the variable.
     * @throws IllegalArgumentException if one of the following conditions is met:
     *     <ul>
     *       <li>The {@code scriptName} is {@code null};
     *       <li>The {@code key} is {@code null} or its length is higher than the maximum allowed
     *           ({@value #MAX_KEY_SIZE});
     *       <li>The {@code value}'s length is higher than the maximum allowed ({@value
     *           #MAX_VALUE_SIZE});
     *       <li>The number of script variables is higher than the maximum allowed ({@value
     *           #MAX_SCRIPT_VARS});
     *     </ul>
     *
     * @since 2.8.0
     */
    public static void setScriptVar(String scriptName, String key, String value) {
        validateScriptName(scriptName);
        setScriptVarImpl(scriptName, key, value);
    }

    /**
     * Internal method that sets a variable without validating the script name.
     *
     * @param scriptName the name of the script.
     * @param key the key of the variable.
     * @param value the value of the variable.
     */
    private static void setScriptVarImpl(String scriptName, String key, String value) {
        if (value != null) {
            validateValueLength(value);
        }
        setVar(scriptVars, scriptName, key, value);
    }

    private static <T> void setVar(
            Map<String, Map<String, T>> vars, String scriptName, String key, T value) {
        validateKey(key);

        Map<String, T> scVars =
                vars.computeIfAbsent(scriptName, k -> Collections.synchronizedMap(new HashMap<>()));

        if (value == null) {
            scVars.remove(key);
        } else {
            if (scVars.size() > MAX_SCRIPT_VARS) {
                throw new IllegalArgumentException(
                        "Maximum number of script variables reached: " + MAX_SCRIPT_VARS);
            }
            scVars.put(key, value);
        }
    }

    /**
     * Validates the given script name.
     *
     * @param scriptName the name to validate.
     * @throws IllegalArgumentException if the name is {@code null}.
     */
    private static void validateScriptName(String scriptName) {
        if (scriptName == null) {
            throw new IllegalArgumentException("The script name must not be null.");
        }
    }

    /**
     * Gets a script variable.
     *
     * @param context the context of the script.
     * @param key the key of the variable.
     * @return the value of the variable, might be {@code null} if no value was previously set.
     * @throws IllegalArgumentException if the {@code context} is {@code null} or it does not
     *     contain the name of the script.
     */
    public static String getScriptVar(ScriptContext context, String key) {
        return getScriptVarImpl(getScriptName(context), key);
    }

    /**
     * Gets a variable from the script with the given name.
     *
     * @param scriptName the name of the script.
     * @param key the key of the variable.
     * @return the value of the variable, might be {@code null} if no value was previously set.
     * @throws IllegalArgumentException if the {@code scriptName} is {@code null}.
     * @since 2.8.0
     */
    public static String getScriptVar(String scriptName, String key) {
        validateScriptName(scriptName);
        return getScriptVarImpl(scriptName, key);
    }

    /**
     * Internal method that gets a variable without validating the script name.
     *
     * @param scriptName the name of the script.
     * @param key the key of the variable.
     * @return the value of the variable, might be {@code null} if no value was previously set.
     */
    private static String getScriptVarImpl(String scriptName, String key) {
        return getVar(scriptVars, scriptName, key);
    }

    private static <T> T getVar(Map<String, Map<String, T>> vars, String scriptName, String key) {
        Map<String, T> scVars = vars.get(scriptName);
        if (scVars == null) {
            // No vars have been associated with this script
            return null;
        }

        return scVars.get(key);
    }

    /**
     * Gets an unmodifiable map, variable key to value, containing the variables of the script with
     * the given name.
     *
     * <p>Iterations should be done in a synchronised block using the returned map.
     *
     * @param scriptName the name of the script.
     * @return an unmodifiable map containing the script variables, never {@code null}.
     * @since 2.8.0
     */
    public static Map<String, String> getScriptVars(String scriptName) {
        return Collections.unmodifiableMap(
                scriptVars.getOrDefault(scriptName, Collections.emptyMap()));
    }

    /**
     * Sets or removes a script custom variable.
     *
     * <p>The variable is removed when the {@code value} is {@code null}.
     *
     * @param context the context of the script.
     * @param key the key of the variable.
     * @param value the value of the variable.
     * @throws IllegalArgumentException if one of the following conditions is met:
     *     <ul>
     *       <li>The {@code context} is {@code null} or it does not contain the name of the script;
     *       <li>The {@code key} is {@code null} or its length is higher than the maximum allowed
     *           ({@value #MAX_KEY_SIZE});
     *       <li>The number of script variables is higher than the maximum allowed ({@value
     *           #MAX_SCRIPT_VARS});
     *     </ul>
     *
     * @since 2.9.0
     */
    public static void setScriptCustomVar(ScriptContext context, String key, Object value) {
        setScriptCustomVarImpl(getScriptName(context), key, value);
    }

    /**
     * Sets or removes a script custom variable.
     *
     * <p>The variable is removed when the {@code value} is {@code null}.
     *
     * @param scriptName the name of the script.
     * @param key the key of the variable.
     * @param value the value of the variable.
     * @throws IllegalArgumentException if one of the following conditions is met:
     *     <ul>
     *       <li>The {@code scriptName} is {@code null};
     *       <li>The {@code key} is {@code null} or its length is higher than the maximum allowed
     *           ({@value #MAX_KEY_SIZE});
     *       <li>The number of script variables is higher than the maximum allowed ({@value
     *           #MAX_SCRIPT_VARS});
     *     </ul>
     *
     * @since 2.9.0
     */
    public static void setScriptCustomVar(String scriptName, String key, Object value) {
        validateScriptName(scriptName);
        setScriptCustomVarImpl(scriptName, key, value);
    }

    /**
     * Internal method that sets a custom variable without validating the script name.
     *
     * @param scriptName the name of the script.
     * @param key the key of the variable.
     * @param value the value of the variable.
     */
    private static void setScriptCustomVarImpl(String scriptName, String key, Object value) {
        setVar(scriptCustomVars, scriptName, key, value);
    }

    /**
     * Gets a script custom variable.
     *
     * @param context the context of the script.
     * @param key the key of the variable.
     * @return the value of the variable, might be {@code null} if no value was previously set.
     * @throws IllegalArgumentException if the {@code context} is {@code null} or it does not
     *     contain the name of the script.
     * @since 2.9.0
     */
    public static Object getScriptCustomVar(ScriptContext context, String key) {
        return getScriptCustomVarImpl(getScriptName(context), key);
    }

    /**
     * Gets a custom variable from the script with the given name.
     *
     * @param scriptName the name of the script.
     * @param key the key of the variable.
     * @return the value of the variable, might be {@code null} if no value was previously set.
     * @throws IllegalArgumentException if the {@code scriptName} is {@code null}.
     * @since 2.9.0
     */
    public static Object getScriptCustomVar(String scriptName, String key) {
        validateScriptName(scriptName);
        return getScriptCustomVarImpl(scriptName, key);
    }

    /**
     * Internal method that gets a custom variable without validating the script name.
     *
     * @param scriptName the name of the script.
     * @param key the key of the variable.
     * @return the value of the variable, might be {@code null} if no value was previously set.
     */
    private static Object getScriptCustomVarImpl(String scriptName, String key) {
        return getVar(scriptCustomVars, scriptName, key);
    }

    /**
     * Gets an unmodifiable map, variable key to value, containing the custom variables of the
     * script with the given name.
     *
     * <p>Iterations should be done in a synchronised block using the returned map.
     *
     * @param scriptName the name of the script.
     * @return an unmodifiable map containing the script variables, never {@code null}.
     * @since 2.9.0
     */
    public static Map<String, Object> getScriptCustomVars(String scriptName) {
        return Collections.unmodifiableMap(
                scriptCustomVars.getOrDefault(scriptName, Collections.emptyMap()));
    }

    /**
     * Clears the global variables.
     *
     * @since 2.8.0
     * @see #clear()
     * @see #clearScriptVars(String)
     */
    public static void clearGlobalVars() {
        globalVars.clear();
        globalCustomVars.clear();
    }

    /**
     * Clears the variables of the script with the given name.
     *
     * @param scriptName the name of the script.
     * @since 2.8.0
     * @see #clear()
     * @see #clearGlobalVars()
     */
    public static void clearScriptVars(String scriptName) {
        scriptVars.remove(scriptName);
        scriptCustomVars.remove(scriptName);
    }

    /**
     * Clears all variables, global and script.
     *
     * @since 2.8.0
     * @see #clearGlobalVars()
     * @see #clearScriptVars(String)
     */
    public static void clear() {
        clearGlobalVars();
        scriptVars.clear();
        scriptCustomVars.clear();
    }
}
