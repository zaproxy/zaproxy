/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.view.panelsearch.items;

import java.awt.Component;
import java.lang.reflect.ParameterizedType;
import org.zaproxy.zap.view.panelsearch.Generic;
import org.zaproxy.zap.view.panelsearch.HighlightedComponent;
import org.zaproxy.zap.view.panelsearch.SearchQuery;

public abstract class AbstractComponentSearch<T> implements ComponentSearch, ComponentHighlighter {

    private Class<T> componentType;

    public AbstractComponentSearch() {
        ParameterizedType currentClassParamType = uncheckedCast(getClass().getGenericSuperclass());

        if (currentClassParamType.getActualTypeArguments()[0] instanceof ParameterizedType) {
            ParameterizedType paramTypeClassParamType =
                    uncheckedCast(currentClassParamType.getActualTypeArguments()[0]);
            this.componentType = uncheckedCast(paramTypeClassParamType.getRawType());
        } else {
            this.componentType = uncheckedCast(currentClassParamType.getActualTypeArguments()[0]);
        }
    }

    @Override
    public final boolean isResponsible(Object component) {
        return componentType.isInstance(component);
    }

    @Override
    public final boolean isSearchMatching(Object component, SearchQuery query) {
        if (isResponsible(component)) {
            return isSearchMatchingInternal(uncheckedCast(component), query);
        }
        return false;
    }

    @Override
    public final Object[] getComponents(Object component) {
        if (isResponsible(component)) {
            return getComponentsInternal(uncheckedCast(component));
        }
        return new Component[] {};
    }

    @Override
    public final HighlightedComponent highlight(Object component) {
        if (isResponsible(component)) {
            return highlightInternal(uncheckedCast(component));
        }
        return null;
    }

    @Override
    public final HighlightedComponent highlightAsParent(Object component) {
        if (isResponsible(component)) {
            return highlightAsParentInternal(uncheckedCast(component));
        }
        return null;
    }

    @Override
    public final boolean undoHighlight(HighlightedComponent highlightedComponent) {
        if (isResponsible(highlightedComponent.getComponent())) {
            undoHighlightInternal(
                    highlightedComponent, uncheckedCast(highlightedComponent.getComponent()));
            return true;
        }
        return false;
    }

    @Override
    public final boolean undoHighlightAsParent(HighlightedComponent highlightedComponent) {
        if (isResponsible(highlightedComponent.getComponent())) {
            undoHighlightAsParentInternal(
                    highlightedComponent, uncheckedCast(highlightedComponent.getComponent()));
            return true;
        }
        return false;
    }

    public static <T> T uncheckedCast(Object component) {
        return Generic.uncheckedCast(component);
    }

    protected boolean isSearchMatchingInternal(T component, SearchQuery query) {
        return false;
    }

    protected Object[] getComponentsInternal(T component) {
        return new Object[] {};
    }

    protected HighlightedComponent highlightInternal(T component) {
        return null;
    }

    protected HighlightedComponent highlightAsParentInternal(T component) {
        return null;
    }

    protected void undoHighlightInternal(HighlightedComponent highlightedComponent, T component) {}

    protected void undoHighlightAsParentInternal(
            HighlightedComponent highlightedComponent, T component) {}
}
