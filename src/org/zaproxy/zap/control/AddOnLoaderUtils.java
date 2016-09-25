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
package org.zaproxy.zap.control;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.AbstractPlugin;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;

/**
 * A class with utility methods to help with add-on loading and (un)installation.
 *
 * @since 2.4.3
 */
final class AddOnLoaderUtils {

    private static final Logger LOGGER = Logger.getLogger(AddOnLoaderUtils.class);

    private AddOnLoaderUtils() {
    }

    /**
     * Loads, using the given {@code addOnClassLoader}, and creates an instance with the given {@code classname} of the
     * (expected) given {@code clazz}. The {@code type} is used in error log messages, to indicate the expected type being
     * loaded.
     *
     * @param <T> the type of the class that will be instantiated
     * @param addOnClassLoader the class loader of the add-on that contains the classes
     * @param classname the binary name of the class that will be loaded
     * @param clazz the type of the instance that will be created using the class loaded
     * @param type the expected type being loaded (for example, "extension", "ascanrule"...)
     * @return an instance of the given {@code clazz}, or {@code null} if an error occurred (for example, not being of the
     *         expected type)
     * @throws IllegalArgumentException if any of the parameters is {@code null}.
     */
    public static <T> T loadAndInstantiateClass(
            AddOnClassLoader addOnClassLoader,
            String classname,
            Class<T> clazz,
            String type) {
        validateNotNull(addOnClassLoader, "addOnClassLoader");
        validateNotNull(classname, "classname");
        validateNotNull(clazz, "clazz");
        validateNotNull(type, "type");

        return loadAndInstantiateClassImpl(addOnClassLoader, classname, clazz, type);
    }

    /**
     * Loads, using the given {@code addOnClassLoader}, and creates an instance with the given {@code classname} of the
     * (expected) given {@code clazz}. The {@code type} is used in error log messages, to indicate the expected type being
     * loaded.
     * <p>
     * <strong>Note:</strong> Internal method that does not validate that the parameters are not {@code null}.
     *
     * @param <T> the type of the class that will be instantiated
     * @param addOnClassLoader the class loader of the add-on that contains the classes, must not be {@code null}
     * @param classname the binary name of the class that will be loaded, must not be {@code null}
     * @param clazz the type of the instance that will be created using the class loaded, must not be {@code null}
     * @param type the expected type being loaded (for example, "extension", "ascanrule"...), must not be {@code null}
     * @return an instance of the given {@code clazz}, or {@code null} if an error occurred (for example, not being of the
     *         expected type)
     */
    private static <T> T loadAndInstantiateClassImpl(
            AddOnClassLoader addOnClassLoader,
            String classname,
            Class<T> clazz,
            String type) {
        Class<?> cls;
        try {
            cls = addOnClassLoader.loadClass(classname);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Declared \"" + type + "\" was not found: " + classname, e);
            return null;
        }

        if (Modifier.isAbstract(cls.getModifiers()) || Modifier.isInterface(cls.getModifiers())) {
            LOGGER.error("Declared \"" + type + "\" is abstract or an interface: " + classname);
            return null;
        }

        if (!clazz.isAssignableFrom(cls)) {
            LOGGER.error("Declared \"" + type + "\" is not of type \"" + clazz.getName() + "\": " + classname);
            return null;
        }

        try {
            @SuppressWarnings("unchecked")
            Constructor<T> c = (Constructor<T>) cls.getConstructor();
            T instance = c.newInstance();
            return instance;
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Loads, using the given {@code addOnClassLoader}, and creates instances with the given {@code classnames} of the
     * (expected) given {@code clazz}. The {@code type} is used in error log messages, to indicate the expected type being
     * loaded. Any classname that leads to an error (for example, not being of the expected type or if it was not found) it will
     * be ignored (after logging the error).
     *
     * @param <T> the type of the class that will be instantiated
     * @param addOnClassLoader the class loader of the add-on that contains the classes
     * @param classnames the binary names of the classes that will be loaded
     * @param clazz the type of the instance that will be created using the classes loaded
     * @param type the expected type being loaded (for example, "extension", "ascanrule"...)
     * @return an unmodifiable {@code List} with the instances of the given {@code clazz}
     * @throws IllegalArgumentException if any of the parameters is {@code null}.
     * @see #loadAndInstantiateClass(AddOnClassLoader, String, Class, String)
     */
    public static <T> List<T> loadDeclaredClasses(
            AddOnClassLoader addOnClassLoader,
            List<String> classnames,
            Class<T> clazz,
            String type) {
        validateNotNull(addOnClassLoader, "addOnClassLoader");
        validateNotNull(classnames, "classnames");
        validateNotNull(clazz, "clazz");
        validateNotNull(type, "type");

        if (classnames.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<T> instances = new ArrayList<>(classnames.size());
        for (String classname : classnames) {
            T instance = loadAndInstantiateClassImpl(addOnClassLoader, classname, clazz, type);
            if (instance != null) {
                instances.add(instance);
            }
        }
        instances.trimToSize();
        return Collections.unmodifiableList(instances);
    }

    /**
     * Gets the active scan rules of the given {@code addOn}. The active scan rules are first loaded, if they weren't already.
     *
     * @param addOn the add-on whose active scan rules will be returned
     * @param addOnClassLoader the {@code AddOnClassLoader} of the given {@code addOn}
     * @return an unmodifiable {@code List} with the active scan rules, never {@code null}
     * @throws IllegalArgumentException if any of the parameters is {@code null}.
     */
    public static List<AbstractPlugin> getActiveScanRules(AddOn addOn, AddOnClassLoader addOnClassLoader) {
        validateNotNull(addOn, "addOn");
        validateNotNull(addOnClassLoader, "addOnClassLoader");

        synchronized (addOn) {
            if (addOn.isLoadedAscanrulesSet()) {
                return addOn.getLoadedAscanrules();
            }

            List<AbstractPlugin> ascanrules = loadDeclaredClasses(
                    addOnClassLoader,
                    addOn.getAscanrules(),
                    AbstractPlugin.class,
                    "ascanrule");
            addOn.setLoadedAscanrules(ascanrules);
            addOn.setLoadedAscanrulesSet(true);
            return Collections.unmodifiableList(ascanrules);
        }
    }

    /**
     * Gets the passive scan rules of the given {@code addOn}. The passive scan rules are first loaded, if they weren't already.
     *
     * @param addOn the add-on whose passive scan rules will be returned
     * @param addOnClassLoader the {@code AddOnClassLoader} of the given {@code addOn}
     * @return an unmodifiable {@code List} with the passive scan rules, never {@code null}
     * @throws IllegalArgumentException if any of the parameters is {@code null}.
     */
    public static List<PluginPassiveScanner> getPassiveScanRules(AddOn addOn, AddOnClassLoader addOnClassLoader) {
        validateNotNull(addOn, "addOn");
        validateNotNull(addOnClassLoader, "addOnClassLoader");

        synchronized (addOn) {
            if (addOn.isLoadedPscanrulesSet()) {
                return addOn.getLoadedPscanrules();
            }

            List<PluginPassiveScanner> pscanrules = loadDeclaredClasses(
                    addOnClassLoader,
                    addOn.getPscanrules(),
                    PluginPassiveScanner.class,
                    "pscanrule");
            addOn.setLoadedPscanrules(pscanrules);
            addOn.setLoadedPscanrulesSet(true);
            return Collections.unmodifiableList(pscanrules);
        }
    }

    /**
     * Helper method that validates that the given {@code object} is not {@code null}, throwing an
     * {@code IllegalArgumentException} if it is.
     *
     * @param object the object that will be validated that it's not {@code null}
     * @param name the name used in the exception message
     * @throws IllegalArgumentException if the given {@code object} is {@code null}.
     */
    private static void validateNotNull(Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException("Parameter " + name + " must not be null.");
        }
    }

}
