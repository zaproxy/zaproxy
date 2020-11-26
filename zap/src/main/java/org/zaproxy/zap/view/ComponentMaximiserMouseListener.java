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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.view.View;

/**
 * A {@code MouseListener} that (un)maximises a component, when clicked twice, using a {@link
 * ComponentMaximiser}.
 *
 * @since 2.5.0
 * @see #triggerMaximisation(Component)
 */
public class ComponentMaximiserMouseListener extends MouseAdapter {

    private static final Logger LOGGER =
            LogManager.getLogger(ComponentMaximiserMouseListener.class);

    private static final String DOUBLE_CLICK_WARN_MESSAGE =
            Constant.messages.getString("tab.doubleClick.warning");

    /**
     * The view options to check (and update) if it's required a confirmation from the user to
     * maximise the component. Never {@code null}.
     *
     * @see #confirmMaximisation()
     */
    private final OptionsParamView viewOptions;

    /**
     * The delegate used to (un)maximise the component, might be {@code null}.
     *
     * @see #triggerMaximisation(Component)
     */
    private ComponentMaximiser componentMaximiser;

    /**
     * Constructs a {@code ComponentMaximiserMouseListener} with the given view options.
     *
     * <p>The view options to check (and update) if it's required a confirmation from the user to
     * maximise the component.
     *
     * @param viewOptions the view options
     * @throws IllegalArgumentException if the parameter {@code viewOptions} is {@code null}.
     * @see #setComponentMaximiser(ComponentMaximiser)
     */
    public ComponentMaximiserMouseListener(OptionsParamView viewOptions) {
        this(viewOptions, null);
    }

    /**
     * Constructs a {@code ComponentMaximiserMouseListener} with the given view options and given
     * component maximiser.
     *
     * <p>The view options to check (and update) if it's required a confirmation from the user to
     * maximise the component.
     *
     * @param viewOptions the view options
     * @param componentMaximiser the object responsible for maximising the component, might be
     *     {@code null}.
     * @throws IllegalArgumentException if the parameter {@code viewOptions} is {@code null}.
     */
    public ComponentMaximiserMouseListener(
            OptionsParamView viewOptions, ComponentMaximiser componentMaximiser) {
        if (viewOptions == null) {
            throw new IllegalArgumentException("Parameter viewOptions must not be null.");
        }
        this.viewOptions = viewOptions;
        setComponentMaximiser(componentMaximiser);
    }

    /**
     * Sets the {@code ComponentMaximiser} that will be used to maximise components. Might be {@code
     * null}, in which case there will be no maximisation when a component is clicked twice.
     *
     * @param componentMaximiser the {@code ComponentMaximiser} that will be used to maximise
     *     components
     * @see #getComponentMaximiser()
     * @see #triggerMaximisation(Component)
     */
    public void setComponentMaximiser(ComponentMaximiser componentMaximiser) {
        this.componentMaximiser = componentMaximiser;
    }

    /**
     * Gets the {@code ComponentMaximiser} that's used to maximise components.
     *
     * @return the {@code ComponentMaximiser} that's used to maximise the components, might be
     *     {@code null}
     * @see #setComponentMaximiser(ComponentMaximiser)
     */
    public ComponentMaximiser getComponentMaximiser() {
        return componentMaximiser;
    }

    /**
     * Calls {@link #triggerMaximisation(Component)} when clicked twice, with the source of the
     * event as parameter.
     */
    @Override
    public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            triggerMaximisation((Component) evt.getSource());
        }
    }

    /**
     * Convenience method that programmatically triggers the (un)maximisation logic.
     *
     * <p>If a component is already maximised it's unmaximised, otherwise it is maximised the given
     * {@code component}. This is the same logic that's executed when a component is clicked twice,
     * being the {@code component} the source of the mouse event.
     *
     * <p>The call to this method has no effect if there's no {@code ComponentMaximiser}.
     *
     * @param component the component that will be maximised, if none is maximised already
     * @throws IllegalArgumentException if the given {@code component} is {@code null} and there's
     *     no component maximised.
     * @see #setComponentMaximiser(ComponentMaximiser)
     */
    public void triggerMaximisation(Component component) {
        if (componentMaximiser == null) {
            return;
        }

        if (componentMaximiser.isComponentMaximised()) {
            componentMaximiser.unmaximiseComponent();
        } else if (confirmMaximisation()) {
            componentMaximiser.maximiseComponent(component);
        }
    }

    /**
     * Confirms, by asking the user, if the maximisation should be done.
     *
     * <p>After positive confirmation this method returns always {@code true}.
     *
     * @return {@code true} if the maximisation should be done, {@code false} otherwise.
     * @see #triggerMaximisation(Component)
     * @see OptionsParamView#getWarnOnTabDoubleClick()
     */
    private boolean confirmMaximisation() {
        if (!viewOptions.getWarnOnTabDoubleClick()) {
            return true;
        }

        if (View.getSingleton().showConfirmDialog(DOUBLE_CLICK_WARN_MESSAGE)
                != JOptionPane.OK_OPTION) {
            return false;
        }

        viewOptions.setWarnOnTabDoubleClick(false);
        try {
            viewOptions.getConfig().save();
        } catch (ConfigurationException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return true;
    }
}
