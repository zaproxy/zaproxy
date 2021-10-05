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

import java.util.HashSet;
import java.util.Set;
import javax.swing.ImageIcon;

/**
 * A type of script supported by ZAP, which allows to customise and extend ZAP's functionalities
 * through its scripts.
 *
 * @since 2.2.0
 * @see ScriptWrapper
 */
public class ScriptType {

    /**
     * Script types with this capability can sensibly have requests appended to them. Typically this
     * will just apply to Zest scripts.
     *
     * @since 2.4.0
     */
    public static final String CAPABILITY_APPEND = "append";

    private final String name;
    private final String i18nKey;
    private final ImageIcon icon;
    private final boolean isEnableable;

    /** Flag that indicates if the scripts of this script type should be enabled by default. */
    private final boolean enabledByDefault;

    private final Set<String> capabilitiesSet = new HashSet<>();

    /**
     * Constructs a {@code ScriptType} with the given data.
     *
     * @param name the (internal) name of the script type.
     * @param i18nKey the resource key for the internationalised name.
     * @param icon the icon of the script type.
     * @param isEnableable {@code true} if the scripts of the script type can be enabled, {@code
     *     false} otherwise.
     * @see #ScriptType(String, String, ImageIcon, boolean, boolean)
     */
    public ScriptType(String name, String i18nKey, ImageIcon icon, boolean isEnableable) {
        this(name, i18nKey, icon, isEnableable, false);
    }

    /**
     * Constructs a {@code ScriptType} with the given data.
     *
     * @param name the (internal) name of the script type.
     * @param i18nKey the resource key for the internationalised name.
     * @param icon the icon of the script type.
     * @param enableable {@code true} if the scripts of the script type can be enabled, {@code
     *     false} otherwise.
     * @param enabledByDefault {@code true} if the scripts of the script type should be enabled by
     *     default, {@code false} otherwise. Ignored if {@code enableable} is false.
     * @since 2.6.0
     */
    public ScriptType(
            String name,
            String i18nKey,
            ImageIcon icon,
            boolean enableable,
            boolean enabledByDefault) {
        this(name, i18nKey, icon, enableable, enabledByDefault, null);
    }

    /**
     * Constructs a {@code ScriptType} with the given data.
     *
     * @param name the (internal) name of the script type.
     * @param i18nKey the resource key for the internationalised name.
     * @param icon the icon of the script type.
     * @param isEnableable {@code true} if the scripts of the script type can be enabled, {@code
     *     false} otherwise.
     * @param capabilities the capabilities of the scripts of the script type.
     * @since 2.4.0
     * @see #ScriptType(String, String, ImageIcon, boolean, boolean, String[])
     */
    public ScriptType(
            String name,
            String i18nKey,
            ImageIcon icon,
            boolean isEnableable,
            String[] capabilities) {
        this(name, i18nKey, icon, isEnableable, false, capabilities);
    }

    /**
     * Constructs a {@code ScriptType} with the given data.
     *
     * @param name the (internal) name of the script type.
     * @param i18nKey the resource key for the internationalised name.
     * @param icon the icon of the script type.
     * @param enableable {@code true} if the scripts of the script type can be enabled, {@code
     *     false} otherwise.
     * @param enabledByDefault {@code true} if the scripts should be enabled by default, {@code
     *     false} otherwise. Ignored if {@code enableable} is false.
     * @param capabilities the capabilities of the scripts of the script type.
     * @since 2.6.0
     */
    public ScriptType(
            String name,
            String i18nKey,
            ImageIcon icon,
            boolean enableable,
            boolean enabledByDefault,
            String[] capabilities) {
        super();
        this.name = name;
        this.i18nKey = i18nKey;
        this.icon = icon;
        this.isEnableable = enableable;
        this.enabledByDefault = isEnableable ? enabledByDefault : false;
        if (capabilities != null) {
            for (String capability : capabilities) {
                this.capabilitiesSet.add(capability);
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    /**
     * Tells whether or not the scripts of this script type can be enabled/disabled.
     *
     * @return {@code true} if the scripts can be enabled/disabled, {@code false} otherwise.
     * @see #isEnabledByDefault()
     */
    public boolean isEnableable() {
        return isEnableable;
    }

    /**
     * Tells whether or not the scripts of this script type should be enabled by default.
     *
     * <p>Only meaningful if the script type is {@link #isEnableable() enableable}.
     *
     * @return {@code true} if the scripts should be enabled by default, {@code false} otherwise.
     * @since 2.6.0
     */
    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    /**
     * Adds the given capability to this script type.
     *
     * @param capability the new capability of the script type.
     * @since 2.4.0
     * @see #hasCapability(String)
     */
    public void addCapability(String capability) {
        this.capabilitiesSet.add(capability);
    }

    /**
     * Tells whether or not this script type has the given capability.
     *
     * @param capability the capability to check.
     * @return {@code true} if the script type has the capability, {@code false} otherwise.
     * @since 2.4.0
     * @see #ScriptType(String, String, ImageIcon, boolean, String[])
     * @see #addCapability(String)
     */
    public boolean hasCapability(String capability) {
        return this.capabilitiesSet.contains(capability);
    }
}
