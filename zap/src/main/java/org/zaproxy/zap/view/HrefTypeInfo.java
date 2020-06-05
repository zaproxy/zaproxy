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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.manualrequest.ExtensionManualRequestEditor;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.view.View;

/**
 * The representation of an {@code HistoryReference}'s type, has the type's name (to be displayed)
 * and value (used for comparison).
 *
 * @since TODO add version
 * @see #getFromType(int)
 * @see HistoryReference
 * @see HistoryReference#TYPE_PROXIED
 * @see HistoryReference#TYPE_ZAP_USER
 */
public class HrefTypeInfo implements Comparable<HrefTypeInfo> {

    private static final Map<Integer, HrefTypeInfo> values;

    static {
        Map<Integer, HrefTypeInfo> temp = new HashMap<>();

        HrefTypeInfo hrefTypeInfo =
                new HrefTypeInfo(
                        HistoryReference.TYPE_PROXIED,
                        Constant.messages.getString("view.href.type.name.proxy"));
        temp.put(hrefTypeInfo.getType(), hrefTypeInfo);

        hrefTypeInfo =
                new HrefTypeInfo(
                        HistoryReference.TYPE_ZAP_USER,
                        Constant.messages.getString("view.href.type.name.manual"));
        temp.put(hrefTypeInfo.getType(), hrefTypeInfo);

        values = Collections.unmodifiableMap(temp);
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
                return new ImageIcon(
                        HrefTypeInfo.class.getResource("/resource/icon/16/doublearrow.png"));
            case HistoryReference.TYPE_ZAP_USER:
                return ExtensionManualRequestEditor.getIcon();
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
}
