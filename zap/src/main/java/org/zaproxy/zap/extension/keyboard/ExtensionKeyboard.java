/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.keyboard;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.view.MainMenuBar;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.DesktopUtils;
import org.zaproxy.zap.view.ZapAction;
import org.zaproxy.zap.view.ZapMenuItem;

public class ExtensionKeyboard extends ExtensionAdaptor {

    private static final Logger LOGGER = LogManager.getLogger(ExtensionKeyboard.class);

    public static final String NAME = "ExtensionKeyboard";

    private OptionsKeyboardShortcutPanel optionsKeyboardPanel = null;
    private KeyboardParam keyboardParam = null;
    private ReferenceMap map = new ReferenceMap();
    private KeyboardAPI api = null;

    /**
     * The identifiers of the menus with duplicated default accelerators.
     *
     * <p>Lazily initialised.
     *
     * @see #getDefaultAccelerator(ZapMenuItem)
     */
    private List<String> menusDupDefaultAccelerator;

    private final Map<String, Consumer<KeyStroke>> acceleratorChangeListeners = new HashMap<>();

    public ExtensionKeyboard() {
        super(NAME);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("keyboard.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        if (getView() != null) {
            // Usually options are loaded in daemon mode, but really no point for keyboard
            // shortcuts;)
            extensionHook.addOptionsParamSet(getKeyboardParam());
            extensionHook.getHookView().addOptionPanel(getOptionsKeyboardPanel());

            // Ditto the API
            api = new KeyboardAPI(this);
            extensionHook.addApiImplementor(api);
        }
    }

    protected KeyboardParam getKeyboardParam() {
        if (keyboardParam == null) {
            keyboardParam = new KeyboardParam();
        }
        return keyboardParam;
    }

    @Override
    public void postInit() {
        if (hasView()) {
            LOGGER.info("Initializing keyboard shortcuts");
            processMainMenuBarMenus(this::initAllMenuItems);
        }
    }

    private void processMainMenuBarMenus(Consumer<JMenu> action) {
        MainMenuBar mainMenuBar = getView().getMainFrame().getMainMenuBar();
        for (int i = 0; i < mainMenuBar.getMenuCount(); i++) {
            JMenu menu = mainMenuBar.getMenu(i);
            if (menu != null) {
                action.accept(menu);
            }
        }
    }

    public void registerMenuItem(ZapMenuItem zme) {
        String identifier = zme.getIdentifier();
        if (identifier != null) {
            validateDefaultAccelerator(identifier, zme.getDefaultAccelerator());
            if (isIdentifierWithDuplicatedAccelerator(identifier)
                    && zme.getDefaultAccelerator() != null
                    && zme.getDefaultAccelerator().equals(zme.getAccelerator())) {
                zme.setAccelerator(null);
            }
            setConfiguredAccelerator(zme);
            this.map.put(identifier, new KeyboardMapping(zme));
        } else {
            LOGGER.warn("ZapMenuItem \"{}\" has a null identifier.", zme.getName());
        }
    }

    /**
     * Registers an action and binds it to the given component's {@code InputMap}/{@code ActionMap}.
     *
     * @param action the action to register.
     * @param targetComponent the component to bind the action to.
     * @param condition the {@code InputMap} condition (e.g. {@link
     *     JComponent#WHEN_ANCESTOR_OF_FOCUSED_COMPONENT}).
     * @since 2.18.0
     */
    public void registerAction(ZapAction action, JComponent targetComponent, int condition) {
        registerAction(
                action,
                targetComponent,
                condition,
                Constant.messages.getString("keyboard.scope.component"));
    }

    /**
     * Registers an action and binds it to the given component's {@code InputMap}/{@code ActionMap}.
     *
     * @param action the action to register.
     * @param targetComponent the component to bind the action to.
     * @param condition the {@code InputMap} condition (e.g. {@link
     *     JComponent#WHEN_ANCESTOR_OF_FOCUSED_COMPONENT}).
     * @param scope the scope shown in the keyboard options (e.g. panel name).
     * @since 2.18.0
     */
    public void registerAction(
            ZapAction action, JComponent targetComponent, int condition, String scope) {
        String identifier = action.getIdentifier();
        validateDefaultAccelerator(identifier, action.getDefaultAccelerator());
        setConfiguredAccelerator(action);
        KeyboardMapping mapping = new KeyboardMapping(action, targetComponent, condition, scope);
        this.map.put(identifier, mapping);
        bindAction(mapping);
    }

    /**
     * Registers an action and binds it globally (to the workbench with {@link
     * JComponent#WHEN_IN_FOCUSED_WINDOW}).
     *
     * @param action the action to register.
     * @since 2.18.0
     */
    public void registerGlobalAction(ZapAction action) {
        registerAction(
                action,
                View.getSingleton().getWorkbench(),
                JComponent.WHEN_IN_FOCUSED_WINDOW,
                Constant.messages.getString("keyboard.scope.global"));
    }

    /**
     * Registers a listener notified when the accelerator of the given shortcut changes.
     *
     * @param identifier the shortcut identifier.
     * @param listener the listener to notify.
     * @since 2.18.0
     */
    public void registerAcceleratorChangeListener(String identifier, Consumer<KeyStroke> listener) {
        acceleratorChangeListeners.put(identifier, listener);
    }

    /**
     * Validates that the given menu item does not have a duplicated default accelerator.
     *
     * <p>Duplicated default accelerators are ignored when configuring the menus.
     *
     * @param zme the menu item to validate.
     * @see #menusDupDefaultAccelerator
     */
    private void validateDefaultAccelerator(String identifier, KeyStroke defaultAccelerator) {
        if (defaultAccelerator == null) {
            return;
        }

        if (isIdentifierWithDuplicatedAccelerator(identifier)) {
            return;
        }

        for (Object obj : map.values()) {
            KeyboardMapping km = (KeyboardMapping) obj;
            if (isIdentifierWithDuplicatedAccelerator(km.getIdentifier())) {
                continue;
            }

            if (hasSameDefaultAccelerator(km, defaultAccelerator)
                    && !identifier.equals(km.getIdentifier())) {
                String msg =
                        String.format(
                                "Shortcuts %s and %s use the same default accelerator: %s",
                                identifier, km.getIdentifier(), defaultAccelerator);
                LOGGER.log(Constant.isDevMode() ? Level.ERROR : Level.WARN, msg);
                if (menusDupDefaultAccelerator == null) {
                    menusDupDefaultAccelerator = new ArrayList<>();
                }
                menusDupDefaultAccelerator.add(identifier);
            }
        }
    }

    /**
     * Tells whether or not the given keyboard mapping has the given default accelerator.
     *
     * @param km the keyboard mapping to check.
     * @param ks the accelerator.
     * @return {@code true} if the keyboard mapping has the given default accelerator, {@code false}
     *     otherwise.
     */
    private static boolean hasSameDefaultAccelerator(KeyboardMapping km, KeyStroke ks) {
        KeyStroke kmKs = km.getDefaultKeyStroke();
        if (kmKs == null) {
            return false;
        }
        return kmKs.getKeyCode() == ks.getKeyCode() && kmKs.getModifiers() == ks.getModifiers();
    }

    /**
     * Tells whether or not the given identifier is of a menu with duplicated default accelerator.
     *
     * @param identifier the menu identifier to check.
     * @return {@code true} if it's a menu with duplicated default accelerator, {@code false}
     *     otherwise.
     * @see #menusDupDefaultAccelerator
     */
    private boolean isIdentifierWithDuplicatedAccelerator(String identifier) {
        return menusDupDefaultAccelerator != null
                && menusDupDefaultAccelerator.contains(identifier);
    }

    private void initAllMenuItems(JMenu menu) {
        for (Component c : menu.getMenuComponents()) {
            if (c instanceof ZapMenuItem) {
                this.registerMenuItem((ZapMenuItem) c);

            } else if (c instanceof JMenu) {
                initAllMenuItems((JMenu) c);

            } else if (c instanceof JMenuItem) {
                JMenuItem menuItem = (JMenuItem) c;
                LOGGER.debug("Unable to set accelerators on menu {}", menuItem.getText());
            }
        }
    }

    private void setConfiguredAccelerator(ZapMenuItem menuItem) {
        setConfiguredAccelerator(menuItem.getIdentifier(), menuItem::setAccelerator);
    }

    private void setConfiguredAccelerator(ZapAction action) {
        setConfiguredAccelerator(action.getIdentifier(), action::setAccelerator);
    }

    private void setConfiguredAccelerator(String identifier, Consumer<KeyStroke> setter) {
        KeyStroke ks = this.getKeyboardParam().getShortcut(identifier);
        if (ks == null) {
            return;
        }

        if (ks.getKeyCode() == 0) {
            // Used to indicate no accelerator should be used
            LOGGER.debug("Cleaning shortcut {} accelerator", identifier);
            ks = null;
        } else {
            LOGGER.debug("Setting shortcut {} accelerator to {}", identifier, ks);
        }
        setter.accept(ks);
    }

    private void bindAction(KeyboardMapping mapping) {
        rebindAction(mapping, null, mapping.getKeyStroke());
    }

    private boolean rebindAction(
            KeyboardMapping mapping, KeyStroke oldKeyStroke, KeyStroke newKeyStroke) {
        ZapAction action = mapping.getZapAction();
        JComponent target = mapping.getTargetComponent();
        int condition = mapping.getInputMapCondition();
        String identifier = action.getIdentifier();

        ActionMap actionMap = target.getActionMap();
        actionMap.put(identifier, action);

        InputMap inputMap = target.getInputMap(condition);
        if (newKeyStroke != null && newKeyStroke.getKeyCode() != 0) {
            Object existing = inputMap.get(newKeyStroke);
            if (existing != null && !identifier.equals(existing)) {
                LOGGER.warn(
                        "KeyStroke {} already bound to {} on component {}; not rebinding {}",
                        newKeyStroke,
                        existing,
                        target.getName(),
                        identifier);
                return false;
            }
        }
        if (oldKeyStroke != null && oldKeyStroke.getKeyCode() != 0) {
            inputMap.remove(oldKeyStroke);
        }
        if (newKeyStroke != null && newKeyStroke.getKeyCode() != 0) {
            inputMap.put(newKeyStroke, identifier);
        }
        return true;
    }

    public List<KeyboardShortcut> getShortcuts() {
        return this.getShortcuts(false);
    }

    public List<KeyboardShortcut> getShortcuts(boolean reset) {
        if (hasView()) {
            List<KeyboardShortcut> kss = new ArrayList<>();
            processMainMenuBarMenus(menu -> addAllMenuItems(kss, menu, reset));
            addAllActions(kss, reset);
            return kss;
        }
        return null;
    }

    private void addAllActions(List<KeyboardShortcut> kss, boolean reset) {
        List<KeyboardMapping> actions = new ArrayList<>();
        for (Object obj : map.values()) {
            KeyboardMapping km = (KeyboardMapping) obj;
            if (km.isAction()) {
                actions.add(km);
            }
        }
        actions.sort(
                Comparator.comparing(
                        KeyboardMapping::getName, Comparator.nullsLast(String::compareTo)));
        for (KeyboardMapping km : actions) {
            kss.add(actionToShortcut(km, reset));
        }
    }

    private void addAllMenuItems(List<KeyboardShortcut> kss, JMenu menu, boolean reset) {
        for (Component c : menu.getMenuComponents()) {
            if (c instanceof ZapMenuItem) {
                kss.add(menuToShortcut((ZapMenuItem) c, reset));

            } else if (c instanceof JMenu) {
                addAllMenuItems(kss, (JMenu) c, reset);

            } else if (c instanceof JMenuItem) {
                JMenuItem menuItem = (JMenuItem) c;
                LOGGER.debug("Unable to set accelerators on menu {}", menuItem.getText());
            }
        }
    }

    private KeyboardShortcut menuToShortcut(ZapMenuItem menuItem, boolean reset) {
        if (reset) {
            return new KeyboardShortcut(
                    menuItem.getIdentifier(),
                    menuItem.getText(),
                    getDefaultAccelerator(menuItem),
                    Constant.messages.getString("keyboard.scope.menu"));
        }

        setConfiguredAccelerator(menuItem);
        return new KeyboardShortcut(
                menuItem.getIdentifier(),
                menuItem.getText(),
                menuItem.getAccelerator(),
                Constant.messages.getString("keyboard.scope.menu"));
    }

    private KeyboardShortcut actionToShortcut(KeyboardMapping mapping, boolean reset) {
        ZapAction action = mapping.getZapAction();
        if (reset) {
            return new KeyboardShortcut(
                    action.getIdentifier(),
                    (String) action.getValue(Action.NAME),
                    getDefaultAccelerator(action),
                    mapping.getScope());
        }

        setConfiguredAccelerator(action);
        return new KeyboardShortcut(
                action.getIdentifier(),
                (String) action.getValue(Action.NAME),
                action.getAccelerator(),
                mapping.getScope());
    }

    /**
     * Gets the default accelerator of the given menu, taken into account duplicated default
     * accelerators.
     *
     * @param menuItem the menu item to return the default accelerator
     * @return the KeyStroke or {@code null} if duplicated or does not have a default.
     */
    private KeyStroke getDefaultAccelerator(ZapMenuItem menuItem) {
        return getDefaultAccelerator(menuItem.getIdentifier(), menuItem.getDefaultAccelerator());
    }

    private KeyStroke getDefaultAccelerator(ZapAction action) {
        return getDefaultAccelerator(action.getIdentifier(), action.getDefaultAccelerator());
    }

    private KeyStroke getDefaultAccelerator(String identifier, KeyStroke defaultAccelerator) {
        if (isIdentifierWithDuplicatedAccelerator(identifier)) {
            return null;
        }
        return defaultAccelerator;
    }

    public KeyStroke getShortcut(String identifier) {
        KeyboardMapping mapping = (KeyboardMapping) this.map.get(identifier);
        if (mapping == null) {
            return null;
        }
        return mapping.getKeyStroke();
    }

    public void setShortcut(String identifier, KeyStroke ks) {
        KeyboardMapping mapping = (KeyboardMapping) this.map.get(identifier);
        if (mapping == null) {
            LOGGER.error("No mapping found for keyboard shortcut: {}", identifier);
            return;
        }
        KeyStroke oldKeyStroke = mapping.getKeyStroke();
        if (mapping.isAction() && !rebindAction(mapping, oldKeyStroke, ks)) {
            return;
        }
        mapping.setKeyStroke(ks);
        this.getKeyboardParam().setShortcut(identifier, ks);
        Consumer<KeyStroke> listener = acceleratorChangeListeners.get(identifier);
        if (listener != null) {
            listener.accept(ks);
        }
    }

    private OptionsKeyboardShortcutPanel getOptionsKeyboardPanel() {
        if (optionsKeyboardPanel == null) {
            optionsKeyboardPanel = new OptionsKeyboardShortcutPanel(this);
        }
        return optionsKeyboardPanel;
    }

    public void displayCheatsheetSortedByAction() {
        try {
            DesktopUtils.openUrlInBrowser(api.getCheatSheetActionURI());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void displayCheatsheetSortedByKey() {
        try {
            DesktopUtils.openUrlInBrowser(api.getCheatSheetKeyURI());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("keyboard.desc");
    }

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }
}
