package org.zaproxy.zap.view.widgets;

import java.awt.Component;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.model.Context;

/**
 * A ComboBox widget that displays the list of existing {@link Context Contexts}.
 * <p>
 * <strong>NOTE:</strong> Does not automatically refresh when the Contexts have changed. For this, make sure you
 * manually call {@link #reloadContexts(boolean)}.
 */
public class ContextSelectComboBox extends JComboBox<Context> {

	private static final long serialVersionUID = 6177823947839642740L;

	@SuppressWarnings("unchecked")
	public ContextSelectComboBox() {
		super();
		reloadContexts(false);
		this.setRenderer(new ContextComboBoxRenderer());
	}

	/**
	 * Reloads/refreshes the list of {@link Context Contexts} from the {@link Session}.
	 * 
	 * @param keepSelected whether the previously selected context is tried to be restored. If
	 *            {@code false}, defaults to no selection.
	 */
	public void reloadContexts(boolean keepSelected) {
		Context selected = null;
		if (keepSelected)
			selected = (Context) getSelectedItem();

		List<Context> contexts = Model.getSingleton().getSession().getContexts();
		Context[] contextsArray = contexts.toArray(new Context[contexts.size()]);
		ComboBoxModel<Context> model = new DefaultComboBoxModel<>(contextsArray);
		// No matter what, set selected item, so it either defaults to 'nothing selected' or
		// restores the previously selected item
		model.setSelectedItem(selected);

		this.setModel(model);
	}

	/**
	 * Returns the selected {@link Context}. Behaves the same as {@link #getSelectedItem()}, but
	 * performs a cast.
	 * 
	 * @see #getSelectedItem()
	 * @return the selected context, if any
	 */
	public Context getSelectedContext() {
		return (Context) getSelectedItem();
	}

	/**
	 * A renderer for properly displaying the name of Context in a ComboBox.
	 */
	private static class ContextComboBoxRenderer extends BasicComboBoxRenderer {

		private static final Border BORDER = new EmptyBorder(2, 8, 2, 8);
		private static final long serialVersionUID = 3272133514462699823L;

		@Override
		@SuppressWarnings("rawtypes")
		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value != null) {
				Context item = (Context) value;
				setText(item.getName());
				setBorder(BORDER);
			}
			return this;
		}
	}
}
