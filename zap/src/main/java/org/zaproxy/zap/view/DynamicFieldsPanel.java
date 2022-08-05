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
package org.zaproxy.zap.view;

import java.awt.GridBagLayout;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.ZapTextField;

@SuppressWarnings("serial")
public class DynamicFieldsPanel extends JPanel {

    private static final String[] NO_FIELDS = new String[0];

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1129326656062554952L;

    /** The required fields. */
    private String[] requiredFields;

    /** The optional fields. */
    private String[] optionalFields;

    private Map<String, ZapTextField> textFields;

    /**
     * Constructs a {@code DynamicFieldsPanel} with no fields.
     *
     * @since 2.7.0
     * @see #setFields(String[])
     */
    public DynamicFieldsPanel() {
        this(NO_FIELDS, NO_FIELDS);
    }

    public DynamicFieldsPanel(String[] requiredFields) {
        this(requiredFields, NO_FIELDS);
    }

    public DynamicFieldsPanel(String[] requiredFields, String[] optionalFields) {
        super(new GridBagLayout());

        setFields(requiredFields, optionalFields);
    }

    /**
     * Sets the (required) fields that should be shown in the panel.
     *
     * <p>Any fields previously set are removed.
     *
     * @param requiredFields the required fields.
     * @throws IllegalArgumentException if the given argument is {@code null}.
     * @since 2.7.0
     * @see #setFields(String[], String[])
     */
    public void setFields(String[] requiredFields) {
        setFields(requiredFields, NO_FIELDS);
    }

    /**
     * Sets the required and optional fields that should be shown in the panel.
     *
     * <p>Any fields previously set are removed.
     *
     * @param requiredFields the required fields.
     * @param optionalFields the optional fields.
     * @throws IllegalArgumentException if the any of the arguments is {@code null}.
     * @since 2.7.0
     * @see #setFields(String[])
     */
    public void setFields(String[] requiredFields, String[] optionalFields) {
        if (requiredFields == null) {
            throw new IllegalArgumentException("Parameter requiredFields must not be null.");
        }

        if (optionalFields == null) {
            throw new IllegalArgumentException("Parameter optionalFields must not be null.");
        }

        this.requiredFields = requiredFields;
        this.optionalFields = optionalFields;

        this.textFields = new HashMap<>(requiredFields.length + optionalFields.length);

        removeAll();

        int fieldIndex = 0;
        for (String fieldName : requiredFields) {
            addRequiredField(fieldName, fieldIndex);
            fieldIndex++;
        }

        for (String fieldName : optionalFields) {
            addField(fieldName, fieldIndex);
            fieldIndex++;
        }
        add(Box.createVerticalGlue(), LayoutHelper.getGBC(0, fieldIndex, 2, 0.0d, 1.0d));

        validate();
    }

    private void addRequiredField(String fieldName, int fieldIndex) {
        addFieldImpl("* " + fieldName, fieldName, fieldIndex);
    }

    private void addField(String fieldName, int fieldIndex) {
        addFieldImpl(fieldName, fieldName, fieldIndex);
    }

    private void addFieldImpl(String labelText, String fieldName, int fieldIndex) {
        JLabel label = new JLabel(labelText + ": ");
        this.add(label, LayoutHelper.getGBC(0, fieldIndex, 1, 0.0d, 0.0d));

        ZapTextField tf = new ZapTextField();
        label.setLabelFor(tf);
        this.add(tf, LayoutHelper.getGBC(1, fieldIndex, 1, 1.0d, 0.0d));
        textFields.put(fieldName, tf);
    }

    /**
     * Clears all the fields, leaving an empty panel.
     *
     * @since 2.7.0
     * @see #setFields(String[])
     */
    public void clearFields() {
        this.requiredFields = NO_FIELDS;
        this.optionalFields = NO_FIELDS;

        this.textFields = Collections.emptyMap();

        removeAll();
        validate();
    }

    /**
     * Gets a mapping of the field names to the configured field values.
     *
     * @return the field values
     */
    public Map<String, String> getFieldValues() {
        Map<String, String> values = new HashMap<>(requiredFields.length + optionalFields.length);
        for (Entry<String, ZapTextField> f : textFields.entrySet())
            values.put(f.getKey(), f.getValue().getText());
        return values;
    }

    /**
     * Bind a mapping of field names/values to the fields in this panel. All the fields whose names
     * have a value provided in the map get set to that value, the others get cleared.
     *
     * @param fieldValues the field values
     */
    public void bindFieldValues(Map<String, String> fieldValues) {
        for (Entry<String, ZapTextField> f : textFields.entrySet()) {
            ZapTextField field = f.getValue();
            field.setText(fieldValues.get(f.getKey()));
            field.discardAllEdits();
        }
    }

    /**
     * Validate the fields of the panel, checking that all the required fields has been filled. If
     * any of the fields are not in the proper state, an IllegalStateException is thrown, containing
     * a message describing the problem.
     *
     * @throws IllegalStateException the illegal state exception
     */
    public void validateFields() throws IllegalStateException {
        for (String rf : requiredFields)
            if (textFields.get(rf).getText().trim().isEmpty()) {
                textFields.get(rf).requestFocusInWindow();
                throw new IllegalStateException(
                        Constant.messages.getString(
                                "authentication.method.script.dialog.error.text.required", rf));
            }
    }
}
