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
package org.zaproxy.zap.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.view.View;

/**
 * The representation of an {@code HistoryReference}'s type, has the type's name (to be displayed)
 * and value (used for comparison).
 *
 * @since 2.10.0
 * @see #getFromType(int)
 * @see HistoryReference
 * @see HistoryReference#TYPE_PROXIED
 * @see HistoryReference#TYPE_ZAP_USER
 */
public class HrefTypeInfo implements Comparable<HrefTypeInfo> {

    private static final Logger LOGGER = LogManager.getLogger(HrefTypeInfo.class);

    static final Map<Integer, HrefTypeInfo> values;

    static {
        values = new HashMap<>();

        addTypeImpl(
                new HrefTypeInfo(
                        HistoryReference.TYPE_PROXIED,
                        Constant.messages.getString("view.href.type.name.proxy")));

        addTypeImpl(
                new HrefTypeInfo(
                        HistoryReference.TYPE_PROXY_CONNECT,
                        Constant.messages.getString("view.href.type.name.proxy")));

        addTypeImpl(
                new HrefTypeInfo(
                        HistoryReference.TYPE_AUTHENTICATION,
                        Constant.messages.getString("view.href.type.name.auth")));
    }

    /**
     * Represents a non {@code HistoryReference}'s type. It has an empty name and value {@literal
     * -1}.
     *
     * <p>Should be used when there's no known type.
     */
    public static final HrefTypeInfo NO_TYPE = new HrefTypeInfo(-1, "");

    /**
     * Represents an undefined {@code HistoryReference}'s type. It has the name as {@literal
     * undefined} and value {@literal -2}.
     */
    public static final HrefTypeInfo UNDEFINED_TYPE =
            new HrefTypeInfo(-2, Constant.messages.getString("view.href.type.name.undefined"));

    private final int source;
    private final String name;
    private Icon icon;

    public HrefTypeInfo(final int source, final String name) {
        super();
        this.source = source;
        this.name = name;
        this.icon = createIcon(source);
    }

    public HrefTypeInfo(final int source, final String name, Icon icon) {
        super();
        this.source = source;
        this.name = name;
        this.icon = icon;
    }

    public int getType() {
        return source;
    }

    public Icon getIcon() {
        return icon;
    }

    private static Icon createIcon(int source) {
        if (!View.isInitialised()) {
            return null;
        }
        switch (source) {
            case HistoryReference.TYPE_PROXIED:
            case HistoryReference.TYPE_PROXY_CONNECT:
                return new ImageIcon(
                        HrefTypeInfo.class.getResource("/resource/icon/16/doublearrow.png"));
            case HistoryReference.TYPE_AUTHENTICATION:
                return new ImageIcon(
                        HrefTypeInfo.class.getResource(
                                "/resource/icon/16/181.png")); // Padlock icon
            default:
                return null;
        }
    }

    @Override
    public int hashCode() {
        return 31 + source;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        HrefTypeInfo other = (HrefTypeInfo) object;
        if (source != other.source) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(HrefTypeInfo other) {
        return name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns an {@code HrefTypeInfo} for the given {@code type}. If the type is {@literal -1} the
     * returned {@code HrefTypeInfo} will be {@code NO_TYPE}.
     *
     * <p>If the given {@code type} is not {@literal -1} but it's not one of the types defined an
     * {@code UNDEFINED_TYPE} will be returned.
     *
     * @param type the type of the href
     * @return the {@code HrefTypeInfo} for the given {@code type}.
     * @see #NO_TYPE
     * @see #UNDEFINED_TYPE
     */
    public static HrefTypeInfo getFromType(final int type) {
        if (type == -1) {
            return NO_TYPE;
        }

        HrefTypeInfo hrefTypeInfo = values.get(type);
        if (hrefTypeInfo == null) {
            return UNDEFINED_TYPE;
        }
        return hrefTypeInfo;
    }

    /**
     * Adds the given type info.
     *
     * <p>The type info is not added if a type info with the same type was already added.
     *
     * <p><strong>Note:</strong> Use of the {@link
     * org.parosproxy.paros.extension.ExtensionHook#addHrefType(HrefTypeInfo)} method is preferred.
     *
     * @param typeInfo the type info to add, must not be {@code null}.
     * @throws NullPointerException if the given type info is {@code null}.
     * @since 2.12.0
     */
    public static void addType(HrefTypeInfo typeInfo) {
        Objects.requireNonNull(typeInfo);

        int type = typeInfo.getType();

        if (type == NO_TYPE.source || type == UNDEFINED_TYPE.source) {
            LOGGER.warn("Attempting to override logic type: {}", type);
            return;
        }

        if (values.containsKey(type)) {
            LOGGER.warn("Attempting to add an existing type: {}", type);
            return;
        }

        addTypeImpl(typeInfo);
    }

    private static void addTypeImpl(HrefTypeInfo typeInfo) {
        values.put(typeInfo.getType(), typeInfo);
    }

    /**
     * Removes the given type info.
     *
     * @param typeInfo the type info to remove, must not be {@code null}.
     * @since 2.12.0
     * @throws NullPointerException if the given type info is {@code null}.
     */
    public static void removeType(HrefTypeInfo typeInfo) {
        Objects.requireNonNull(typeInfo);

        values.remove(typeInfo.getType());
    }
}
