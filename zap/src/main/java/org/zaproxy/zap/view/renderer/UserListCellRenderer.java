package org.zaproxy.zap.view.renderer;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.users.User;

/**
 * A renderer for properly displaying the name of an User in a ComboBox. If the user is disabled,
 * the list item is disabled and marked as such.
 */
public class UserListCellRenderer extends BasicComboBoxRenderer {
	private static final long serialVersionUID = 3654541772447187317L;
	private static final String DISABLED_STRING = " ("
			+ Constant.messages.getString("generic.value.disabled") + ')';

	private static final Border BORDER = new EmptyBorder(2, 3, 3, 3);

	@Override
	@SuppressWarnings("rawtypes")
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if (value != null) {
			User user = (User) value;
			if (!user.isEnabled())
				setText(user.getName() + DISABLED_STRING);
			else
				setText(user.getName());

			setEnabled(user.isEnabled());
		}
		setBorder(BORDER);

		return this;
	}
}