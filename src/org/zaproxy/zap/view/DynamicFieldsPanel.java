package org.zaproxy.zap.view;

import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.ZapTextField;

public class DynamicFieldsPanel extends JPanel {

	private static final String[] NO_FIELDS = new String[0];

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1129326656062554952L;

	/** The required fields. */
	private String[] requiredFields;

	/** The optional fields. */
	private String[] optionalFields;

	private Map<String, ZapTextField> textFields;

	public DynamicFieldsPanel(String[] requiredFields) {
		this(requiredFields, NO_FIELDS);
	}

	public DynamicFieldsPanel(String[] requiredFields, String[] optionalFields) {
		super(new GridBagLayout());

		setFields(requiredFields, optionalFields);
	}

	/**
	 * Sets the (required) fields that should be shown in the panel.
	 * <p>
	 * Any fields previously set are removed.
	 *
	 * @param requiredFields the required fields.
	 * @throws IllegalArgumentException if the given argument is {@code null}.
	 * @since TODO add version
	 * @see #setFields(String[], String[])
	 */
	public void setFields(String[] requiredFields) {
		setFields(requiredFields, NO_FIELDS);
	}

	/**
	 * Sets the required and optional fields that should be shown in the panel.
	 * <p>
	 * Any fields previously set are removed.
	 *
	 * @param requiredFields the required fields.
	 * @param optionalFields the optional fields.
	 * @throws IllegalArgumentException if the any of the arguments is {@code null}.
	 * @since TODO add version
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
			this.add(new JLabel("* " + fieldName + ": "), LayoutHelper.getGBC(0, fieldIndex, 1, 0.0d, 0.0d));

			ZapTextField tf = new ZapTextField();
			this.add(tf, LayoutHelper.getGBC(1, fieldIndex, 1, 1.0d, 0.0d));
			textFields.put(fieldName, tf);

			fieldIndex++;
		}

		for (String fieldName : optionalFields) {
			this.add(new JLabel(fieldName + ": "), LayoutHelper.getGBC(0, fieldIndex, 1, 0.0d, 0.0d));

			ZapTextField tf = new ZapTextField();
			this.add(tf, LayoutHelper.getGBC(1, fieldIndex, 1, 1.0d, 0.0d));
			textFields.put(fieldName, tf);

			fieldIndex++;
		}

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
				throw new IllegalStateException(Constant.messages.getString(
						"authentication.method.script.dialog.error.text.required", rf));
			}
	}
}
