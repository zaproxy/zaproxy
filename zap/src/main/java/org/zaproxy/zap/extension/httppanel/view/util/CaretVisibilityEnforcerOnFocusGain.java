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
package org.zaproxy.zap.extension.httppanel.view.util;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

public class CaretVisibilityEnforcerOnFocusGain implements PropertyChangeListener, FocusListener {

    private int caretBlinkRate;
    private JTextComponent textComponent;
    private boolean enforceCaretVisibility;

    public CaretVisibilityEnforcerOnFocusGain(JTextComponent textComponent) {
        this.textComponent = textComponent;

        enforceCaretVisibility = false;
    }

    public void setEnforceVisibilityOnFocusGain(boolean visible) {
        if (enforceCaretVisibility == visible) {
            return;
        }
        enforceCaretVisibility = visible;

        if (enforceCaretVisibility) {
            setupWithCaret(textComponent.getCaret());

            textComponent.addPropertyChangeListener("caret", this);
        } else {
            removeListenersWithCaret(textComponent.getCaret());

            textComponent.removePropertyChangeListener("caret", this);
        }
    }

    private void setupWithCaret(Caret caret) {
        if (caret != null) {
            textComponent.addFocusListener(this);
            textComponent.addPropertyChangeListener("editable", this);

            caretBlinkRate = caret.getBlinkRate();

            if (!textComponent.isEditable()) {
                caret.setBlinkRate(0);
            }
        } else {
            caretBlinkRate = 0;
        }
    }

    private void removeListenersWithCaret(Caret caret) {
        if (caret != null) {
            textComponent.removeFocusListener(this);
            textComponent.removePropertyChangeListener("editable", this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final String propertyName = evt.getPropertyName();
        if ("caret".equals(propertyName)) {
            removeListenersWithCaret((Caret) evt.getOldValue());
            setupWithCaret((Caret) evt.getNewValue());
        } else if ("editable".equals(propertyName)) {
            if (evt.getNewValue() == Boolean.TRUE) {
                textComponent.getCaret().setBlinkRate(caretBlinkRate);
            } else {
                textComponent.getCaret().setBlinkRate(0);
            }
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        textComponent.getCaret().setVisible(true);
    }

    @Override
    public void focusLost(FocusEvent e) {}
}
