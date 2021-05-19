/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.utils;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.parosproxy.paros.Constant;

/**
 * {@code ZapTextComponentUndoManager} manages a list of {@code UndoableEdit}s, providing a way to
 * undo or redo the appropriate edits with undo and redo actions accessible through {@code
 * KeyStroke}s created with {@code Constant.ACCELERATOR_UNDO} and {@code Constant.ACCELERATOR_REDO},
 * respectively.
 *
 * <p>The default is to maintain a window of 100 undoable edits. When the limit is reached older
 * undoable edits start to be discarded when new ones are saved. The limit can be changed with the
 * method {@code setLimit(int)}.
 *
 * <p>There are three policies that affect if, and when, the undoable edits are saved:
 *
 * <ul>
 *   <li>{@code UndoManagerPolicy.DEFAULT}
 *   <li>{@code UndoManagerPolicy.ALWAYS_ENABLED}
 *   <li>{@code UndoManagerPolicy.ALWAYS_DISABLED}
 * </ul>
 *
 * The policy can be changed with the method {@code setUndoManagerPolicy}.
 *
 * <p>The {@code ZapTextComponentUndoManager} listens to changes to the {@code Document} of the
 * {@code JTextComponent} used with the {@code ZapTextComponentUndoManager}, so there is no need to
 * do anything if the document is changed.
 *
 * @since 1.4.1
 * @see #setLimit(int)
 * @see #setUndoManagerPolicy(UndoManagerPolicy)
 * @see UndoManagerPolicy
 * @see UndoManager
 */
public class ZapTextComponentUndoManager extends UndoManager implements PropertyChangeListener {

    /**
     * There are three policies that affect if, and when, the undoable edits are saved:
     *
     * <ul>
     *   <li>{@code UndoManagerPolicy.DEFAULT}
     *   <li>{@code UndoManagerPolicy.ALWAYS_ENABLED}
     *   <li>{@code UndoManagerPolicy.ALWAYS_DISABLED}
     * </ul>
     *
     * @see #DEFAULT
     * @see #ALWAYS_ENABLED
     * @see #ALWAYS_DISABLED
     */
    public enum UndoManagerPolicy {
        /**
         * The undoable edits are saved exactly when the {@code JTextComponent} is enabled and
         * editable.
         *
         * <p>The {@code ZapTextComponentUndoManager} listens to the changes in the properties
         * "enabled" and "editable", so that can change accordingly to save or not the undoable
         * edits.
         */
        DEFAULT,

        /**
         * The undoable edits are always saved, even if the {@code JTextComponent} is not editable
         * and/or enabled.
         */
        ALWAYS_ENABLED,

        /** The undoable edits are not saved. */
        ALWAYS_DISABLED
    }

    private static final long serialVersionUID = -5728632360771625298L;

    private final JTextComponent textComponent;

    private final UndoAction undoAction;
    private final RedoAction redoAction;

    private boolean enabled;

    private UndoManagerPolicy policy;

    /**
     * Creates a new {@code ZapTextComponentUndoManager} with a {@code DEFAULT} policy.
     *
     * @param textComponent the {@code JTextComponent} that will have undoable edits.
     * @throws NullPointerException if textComponent is {@code null}.
     * @see UndoManagerPolicy#DEFAULT
     */
    public ZapTextComponentUndoManager(JTextComponent textComponent) {
        super();

        if (textComponent == null) {
            throw new NullPointerException("The textComponent must not be null.");
        }

        this.textComponent = textComponent;

        this.undoAction = new UndoAction(this);
        this.redoAction = new RedoAction(this);

        this.enabled = false;
        this.policy = null;

        setUndoManagerPolicy(UndoManagerPolicy.DEFAULT);
    }

    /**
     * Sets the new policy.
     *
     * @param policy the new policy
     * @throws NullPointerException if policy is {@code null}
     * @see UndoManagerPolicy
     */
    public final void setUndoManagerPolicy(UndoManagerPolicy policy) throws NullPointerException {
        if (policy == null) {
            throw new NullPointerException("The policy must not be null.");
        }

        if (this.policy == policy) {
            return;
        }

        final UndoManagerPolicy oldPolicy = this.policy;
        this.policy = policy;

        if (oldPolicy == UndoManagerPolicy.DEFAULT) {
            this.textComponent.removePropertyChangeListener("editable", this);
            this.textComponent.removePropertyChangeListener("enabled", this);
        }

        if (this.policy == UndoManagerPolicy.DEFAULT) {
            this.textComponent.addPropertyChangeListener("editable", this);
            this.textComponent.addPropertyChangeListener("enabled", this);
        }

        handleUndoManagerPolicy();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final String propertyName = evt.getPropertyName();

        if ("document".equals(propertyName)) {
            if (this.enabled) {
                ((Document) evt.getOldValue()).removeUndoableEditListener(this);
                ((Document) evt.getNewValue()).addUndoableEditListener(this);

                if (policy == UndoManagerPolicy.DEFAULT) {
                    handleUndoManagerDefaultPolicy();
                }
            }
        } else if (policy == UndoManagerPolicy.DEFAULT
                && ("editable".equals(propertyName) || "enabled".equals(propertyName))) {
            handleUndoManagerDefaultPolicy();
        }
    }

    private void setEnabled(boolean enabled) {
        if (enabled != this.enabled) {
            this.enabled = enabled;

            if (enabled) {
                this.textComponent.addPropertyChangeListener("document", this);

                this.textComponent.getDocument().addUndoableEditListener(this);

                this.textComponent.getActionMap().put(UndoAction.ACTION_NAME, undoAction);
                this.textComponent.getActionMap().put(RedoAction.ACTION_NAME, redoAction);

                this.textComponent.getInputMap().put(UndoAction.KEY_STROKE, UndoAction.ACTION_NAME);
                this.textComponent.getInputMap().put(RedoAction.KEY_STROKE, RedoAction.ACTION_NAME);
            } else {
                this.textComponent.removePropertyChangeListener("document", this);

                this.textComponent.getDocument().removeUndoableEditListener(this);

                this.textComponent.getActionMap().remove(UndoAction.ACTION_NAME);
                this.textComponent.getActionMap().remove(RedoAction.ACTION_NAME);

                this.textComponent.getInputMap().remove(UndoAction.KEY_STROKE);
                this.textComponent.getInputMap().remove(RedoAction.KEY_STROKE);
            }
        }
    }

    private void handleUndoManagerPolicy() {
        switch (policy) {
            case ALWAYS_DISABLED:
                this.setEnabled(false);
                break;
            case ALWAYS_ENABLED:
                this.setEnabled(true);
                break;
            case DEFAULT:
            default:
                handleUndoManagerDefaultPolicy();
        }
    }

    private void handleUndoManagerDefaultPolicy() {
        this.setEnabled(this.textComponent.isEditable() && this.textComponent.isEnabled());
    }

    private static final class UndoAction extends AbstractAction {

        private static final long serialVersionUID = 6681683056944213164L;

        public static final String ACTION_NAME = "Undo";
        public static final KeyStroke KEY_STROKE =
                KeyStroke.getKeyStroke(Constant.ACCELERATOR_UNDO);

        private UndoManager undoManager;

        public UndoAction(UndoManager undoManager) {
            super(ACTION_NAME);

            this.undoManager = undoManager;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            try {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            } catch (CannotUndoException e) {
            }
        }
    }

    private static final class RedoAction extends AbstractAction {

        private static final long serialVersionUID = -7098526742716575130L;

        public static final String ACTION_NAME = "Redo";
        public static final KeyStroke KEY_STROKE =
                KeyStroke.getKeyStroke(Constant.ACCELERATOR_REDO);

        private UndoManager undoManager;

        public RedoAction(UndoManager undoManager) {
            super(ACTION_NAME);

            this.undoManager = undoManager;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            try {
                if (undoManager.canRedo()) {
                    undoManager.redo();
                }
            } catch (CannotRedoException e) {
            }
        }
    }
}
