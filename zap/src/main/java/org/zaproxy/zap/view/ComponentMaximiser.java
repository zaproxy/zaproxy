/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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

import java.awt.Component;
import java.awt.Container;

/**
 * A utility class to maximise a (non-direct) child {@code Component} of a {@code Container}.
 *
 * <p>Adds the component being maximised directly to the container, making it occupy the whole
 * container. The previous state of the container is preserved internally by this class, any changes
 * done to the container should be done only after unmaximising the component.
 *
 * @since 2.5.0
 * @see #maximiseComponent(Component)
 * @see ComponentMaximiserMouseListener
 */
public class ComponentMaximiser {

    /** The container that has the components that can be maximised. Never {@code null}. */
    private final Container container;

    /**
     * The child component of the container which holds the (sub-)components that can be maximised,
     * {@code null} when no component is maximised.
     */
    private Component containerChild;

    /**
     * The parent component of the maximised component, {@code null} when no component is maximised.
     */
    private Container parentMaximisedComponent;

    /**
     * The (currently) maximised component, {@code null} if none.
     *
     * <p>A maximised component occupies the whole {@code container}, that is, is the only child
     * component.
     *
     * @see #container
     * @see #maximiseComponent(Component)
     * @see #unmaximiseComponent()
     */
    private Component maximisedComponent;

    /**
     * Constructs a {@code ComponentMaximiser} with the given container.
     *
     * @param container the container that will be used to maximise components
     * @throws IllegalArgumentException if the given {@code container} is {@code null}.
     */
    public ComponentMaximiser(Container container) {
        if (container == null) {
            throw new IllegalArgumentException("Parameter container must not be null.");
        }
        this.container = container;
    }

    /**
     * Maximises the given component, to occupy the whole container.
     *
     * <p>The maximisation is done by adding the component directly to the container. If another
     * component is already maximised it does nothing.
     *
     * @param component the component to maximise
     * @throws IllegalArgumentException if the given {@code component} is {@code null}.
     * @see #unmaximiseComponent()
     * @see #isComponentMaximised()
     */
    public void maximiseComponent(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("Parameter component must not be null.");
        }

        if (maximisedComponent != null) {
            return;
        }

        maximisedComponent = component;
        parentMaximisedComponent = component.getParent();
        containerChild = container.getComponent(0);

        parentMaximisedComponent.remove(component);
        container.remove(containerChild);
        container.add(component);

        container.validate();
    }

    /**
     * Unmaximises the current maximised component.
     *
     * <p>It does nothing if no component is maximised.
     *
     * @see #maximiseComponent(Component)
     */
    public void unmaximiseComponent() {
        if (maximisedComponent == null) {
            return;
        }

        container.remove(maximisedComponent);
        container.add(containerChild);
        parentMaximisedComponent.add(maximisedComponent);
        container.validate();

        containerChild = null;
        parentMaximisedComponent = null;
        maximisedComponent = null;
    }

    /**
     * Tells whether or not there is a maximised component.
     *
     * @return {@code true} if there is a maximised component, {@code false} otherwise.
     * @see #getMaximisedComponent()
     * @see #maximiseComponent(Component)
     */
    public boolean isComponentMaximised() {
        return maximisedComponent != null;
    }

    /**
     * Gets the component that is maximised.
     *
     * @return the component maximised, or {@code null} if none
     * @see #isComponentMaximised()
     */
    public Component getMaximisedComponent() {
        return maximisedComponent;
    }
}
