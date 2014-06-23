/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.multiFuzz.impl.http;

import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.zaproxy.zap.utils.Pair;

/**
 * Added via {@link JTable#setDefaultRenderer(Class, TableCellRenderer)} as
 * <code>JTable#setDefaultRenderer(Pair.class, new IconTableCellRenderer());</code>
 */
public class IconTableCellRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = 4181897788209983012L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		@SuppressWarnings("unchecked")
		Pair<String, ImageIcon> data = (Pair<String, ImageIcon>) value;

		this.setOpaque(true);

		if (isSelected) {
			this.setBackground(table.getSelectionBackground());
			this.setForeground(table.getSelectionForeground());
		} else {
			this.setForeground(table.getForeground());

			if (row % 2 == 0) {
				this.setBackground(UIManager
						.getColor("Table.alternateRowColor"));
			} else {
				this.setBackground(Color.WHITE);
			}
		}

		this.setText(data.first);
		this.setIcon(data.second);

		return this;
	}

}
