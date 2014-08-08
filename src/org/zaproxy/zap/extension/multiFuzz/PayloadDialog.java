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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.multiFuzz;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;

public class PayloadDialog<G extends FuzzGap<?, ?, P>, P extends Payload, F extends PayloadFactory<P>>
		extends AbstractDialog {
	private F factory;
	private ExtensionFuzz res;
	private FuzzerListener<PayloadDialog<G, P, F>, G> listen;
	private G target;
	private JTable payloads;
	private PayloadTableModel<P> payloadModel;

	private JPanel background;

	private JButton deletePayloadButton;

	private JButton addSinglePayloadButton;
	private JTextArea payloadText;

	private JButton addFuzzerFileButton;
	private ComboMenuBar categoryField;

	private JButton addRegExPayloadButton;
	private JTextArea regExText;
	private JFormattedTextField regExLimit;

	private JButton addScriptButton;
	private JComboBox<String> payloadScripts;

	private JButton cancelButton;
	private JButton doneButton;

	public PayloadDialog(G g, F factory, ExtensionFuzz ext) {
		super(View.getSingleton().getMainFrame(), true);
		this.target = g;
		this.factory = factory;
		this.res = ext;
		initialize();
		getDoneButton().setEnabled(target.getPayloads().size() > 0);
		if (getCancelButton().getAction() instanceof PayloadDialog.CancelAction) {
			((CancelAction) getCancelButton().getAction())
					.setPayloads((ArrayList<P>) target.getPayloads().clone());
		}
	}

	protected void initialize() {
		this.setTitle(Constant.messages.getString("fuzz.title"));
		this.setContentPane(getJPanel());
		this.setSize(800, 400);
	}

	private JPanel getJPanel() {
		if (background == null) {
			background = new JPanel();
			background.setLayout(new GridBagLayout());
			int currentRow = 0;
			Font headLine = new Font(Font.SERIF, Font.BOLD, 20);
			JLabel headL = new JLabel(
					Constant.messages.getString("fuzz.payloadDia.title")
							+ target.orig());
			headL.setFont(headLine);
			GridBagConstraints h = Util.getGBC(0, currentRow, 4, 1.0, 0.0,
					java.awt.GridBagConstraints.HORIZONTAL);
			h.anchor = java.awt.GridBagConstraints.PAGE_START;
			background.add(headL, h);
			currentRow++;
			background.add(new JScrollPane(getPayloadField()), Util.getGBC(0,
					currentRow, 4, 1.0, 1.0, java.awt.GridBagConstraints.BOTH));
			currentRow++;
			background.add(getDeletePayloadButton(),
					Util.getGBC(3, currentRow, 1, 0.25));
			currentRow++;
			JLabel addSinglePayload = new JLabel(
					Constant.messages.getString("fuzz.add.singlePayload"));
			background
					.add(addSinglePayload, Util.getGBC(0, currentRow, 4, 1.0));
			currentRow++;
			JLabel singlePayloadContent = new JLabel(
					Constant.messages.getString("fuzz.payloadDia.content"));
			background.add(singlePayloadContent,
					Util.getGBC(0, currentRow, 1, 0.25));
			background.add(getPayloadText(), Util.getGBC(1, currentRow, 2, 0.5,
					0.0, java.awt.GridBagConstraints.HORIZONTAL));
			background.add(getAddSinglePayloadButton(),
					Util.getGBC(3, currentRow, 1, 0.25));
			currentRow++;

			JLabel addRegExPayload = new JLabel(
					Constant.messages.getString("fuzz.add.regExPayload"));
			background.add(addRegExPayload, Util.getGBC(0, currentRow, 1, 1.0));
			currentRow++;
			JLabel RegExContent = new JLabel("Content:");
			background.add(RegExContent, Util.getGBC(0, currentRow, 1, 0.25));
			background.add(getRegExText(), Util.getGBC(1, currentRow, 1, 0.25,
					0.0, java.awt.GridBagConstraints.HORIZONTAL));
			background.add(getRegExLimitField(), Util.getGBC(2, currentRow, 1,
					0.25, 0.0, java.awt.GridBagConstraints.HORIZONTAL));
			background.add(getAddRegExPayloadButton(),
					Util.getGBC(3, currentRow, 1, 0.25));
			currentRow++;

			JLabel addFuzzerFile = new JLabel(
					Constant.messages.getString("fuzz.add.fuzzerFile"));
			background.add(addFuzzerFile, Util.getGBC(0, currentRow, 1, 1.0));
			currentRow++;
			JLabel FileContent = new JLabel(
					Constant.messages.getString("fuzz.payloadDia.files"));
			background.add(FileContent, Util.getGBC(0, currentRow, 1, 0.25));
			background.add(getCategoryField(), Util.getGBC(1, currentRow, 2,
					0.5, 0.0, java.awt.GridBagConstraints.HORIZONTAL));
			background.add(getAddFuzzerFileButton(),
					Util.getGBC(3, currentRow, 1, 0.25));
			currentRow++;

			JLabel addFuzzScript = new JLabel(
					Constant.messages.getString("fuzz.add.fuzzScript"));
			background.add(addFuzzScript, Util.getGBC(0, currentRow, 1, 0.2));
			background.add(getPayloadScripts(), Util.getGBC(1, currentRow, 2,
					0.6, 0.0, java.awt.GridBagConstraints.HORIZONTAL));
			background.add(getAddFuzzScriptButton(),
					Util.getGBC(3, currentRow, 3, 0.2));
			currentRow++;

			background.add(getDoneButton(), Util.getGBC(0, currentRow, 1, 0.5));
			background.add(getCancelButton(),
					Util.getGBC(3, currentRow, 1, 0.5));
		}
		return background;
	}

	private JTable getPayloadField() {
		if (payloads == null) {
			payloadModel = new PayloadTableModel();
			payloadModel.addEntries(target.getPayloads());
			payloads = new JTable(payloadModel);
		}
		return payloads;
	}

	public JFormattedTextField getRegExLimitField() {
		if (regExLimit == null) {
			regExLimit = new JFormattedTextField(
					NumberFormat.getNumberInstance());
			regExLimit.setValue(new Integer(1000));
			regExLimit.setColumns(2);
			regExLimit.setMinimumSize(new Dimension(40, 28));
		}
		return regExLimit;
	}

	private JTextArea getPayloadText() {
		if (payloadText == null) {
			payloadText = new JTextArea(
					Constant.messages.getString("fuzz.label.singlePay"));
		}
		return payloadText;
	}

	private JTextArea getRegExText() {
		if (regExText == null) {
			regExText = new JTextArea(
					Constant.messages.getString("fuzz.label.regEx"));
		}
		return regExText;
	}

	private ComboMenuBar getCategoryField() {
		if (categoryField == null) {
			JMenu menu = ComboMenuBar.createMenu(res.getDefaultCategory());
			// Add File based fuzzers (fuzzdb)
			for (String category : res.getFileFuzzerCategories()) {
				ArrayList<String> entries = new ArrayList<>(
						Arrays.asList(category.split(" / ")));
				JMenu parent = menu;
				while (entries.size() > 0) {
					boolean exists = false;
					for (int i = 0; i < parent.getItemCount(); i++) {
						if (parent.getItem(i).getText().equals(entries.get(0))) {
							parent = (JMenu) parent.getItem(i);
							exists = true;
							break;
						}
					}
					if (!exists) {
						JMenu i = new JMenu(entries.get(0));
						MenuScroll.setScrollerFor(i, 10, 125, 0, 0);
						parent.add(i);
						parent = i;
					}
					entries.remove(0);
				}
				for (String fuzzer : res.getFileFuzzerNames(category)) {
					parent.add(new JMenuItem(fuzzer));
				}
			}

			// Add jbrofuzz fuzzers
			for (String category : res.getJBroFuzzCategories()) {
				ArrayList<String> entries = new ArrayList<>(
						Arrays.asList(category.split(" / ")));
				JMenu parent = menu;
				while (entries.size() > 0) {
					boolean exists = false;
					for (int i = 0; i < parent.getItemCount(); i++) {
						if (parent.getItem(i).getText().equals(entries.get(0))) {
							parent = (JMenu) parent.getItem(i);
							exists = true;
							break;
						}
					}
					if (!exists) {
						JMenu i = new JMenu(entries.get(0));
						parent.add(i);
						parent = i;
					}
					entries.remove(0);
				}
				for (String fuzzer : res.getJBroFuzzFuzzerNames(category)) {
					parent.add(new JMenuItem(fuzzer));
				}
			}

			// Custom category
			JMenu cat = new JMenu(
					Constant.messages.getString("fuzz.category.custom"));
			for (String fuzzer : res.getCustomFileList()) {
				cat.add(new JMenuItem(fuzzer));
			}
			menu.add(cat);
			// MenuScroll.setScrollerFor(menu, 10, 125, 0, 0);
			categoryField = new ComboMenuBar(menu);
		}
		return categoryField;
	}

	private JComboBox<String> getPayloadScripts() {
		if (payloadScripts == null) {
			DefaultComboBoxModel<String> payloadModel = new DefaultComboBoxModel<>();
			ExtensionScript extension = (ExtensionScript) Control
					.getSingleton().getExtensionLoader()
					.getExtension(ExtensionScript.NAME);
			if (extension != null) {
				List<ScriptWrapper> scripts = extension
						.getScripts(ExtensionFuzz.SCRIPT_TYPE_PAYLOAD);
				for (ScriptWrapper script : scripts) {
					if (script.isEnabled()) {
						payloadModel.addElement(script.getName());
					}
				}
			}
			payloadScripts = new JComboBox<>(payloadModel);
		}
		return payloadScripts;
	}

	public void addListener(FuzzerListener<PayloadDialog<G, P, F>, G> l) {
		this.listen = l;
	}

	protected boolean isCustomCategory() {
		return Constant.messages.getString("fuzz.category.custom").equals(
				getCategoryField().getSelectedCategory());
	}

	protected boolean isJBroFuzzCategory() {
		return getCategoryField().getSelectedCategory().startsWith(
				ExtensionFuzz.JBROFUZZ_CATEGORY_PREFIX);
	}

	protected DeletePayloadAction getDeletePayloadAction() {
		return new DeletePayloadAction();
	}

	protected AddSinglePayloadAction getAddSinglePayloadAction() {
		return new AddSinglePayloadAction();
	}

	protected Action getAddRegExPayloadAction() {
		return new AddRegExAction();
	}

	protected AddFuzzerFileAction getAddFuzzerFileAction() {
		return new AddFuzzerFileAction();
	}

	protected AddScriptAction getAddScriptAction() {
		return new AddScriptAction();
	}

	protected DoneAction getDoneAction() {
		return new DoneAction();
	}

	protected CancelAction getCancelAction() {
		return new CancelAction();
	}

	protected JButton getDeletePayloadButton() {
		if (deletePayloadButton == null) {
			deletePayloadButton = new JButton();
			deletePayloadButton.setAction(getDeletePayloadAction());
		}
		return deletePayloadButton;
	}

	protected JButton getAddFuzzScriptButton() {
		if (addScriptButton == null) {
			addScriptButton = new JButton();
			addScriptButton.setAction(getAddScriptAction());
		}
		return addScriptButton;
	}

	protected JButton getAddFuzzerFileButton() {
		if (addFuzzerFileButton == null) {
			addFuzzerFileButton = new JButton();
			addFuzzerFileButton.setAction(getAddFuzzerFileAction());
		}
		return addFuzzerFileButton;
	}

	protected JButton getAddRegExPayloadButton() {
		if (addRegExPayloadButton == null) {
			addRegExPayloadButton = new JButton();
			addRegExPayloadButton.setAction(getAddRegExPayloadAction());
		}
		return addRegExPayloadButton;
	}

	protected JButton getAddSinglePayloadButton() {
		if (addSinglePayloadButton == null) {
			addSinglePayloadButton = new JButton();
			addSinglePayloadButton.setAction(getAddSinglePayloadAction());
		}
		return addSinglePayloadButton;
	}

	protected JButton getDoneButton() {
		if (doneButton == null) {
			doneButton = new JButton();
			doneButton.setAction(getDoneAction());
		}
		return doneButton;
	}

	protected JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setAction(getCancelAction());
		}
		return cancelButton;
	}

	protected class DeletePayloadAction extends AbstractAction {
		public DeletePayloadAction() {
			super(Constant.messages.getString("fuzz.add.deletePayload"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int[] sel = getPayloadField().getSelectedRows();
			for (int i = 0; i < sel.length; i++) {
				payloadModel.deleteEntry(sel[i] - i);
				target.getPayloads().remove(sel[i] - i);
			}
			if (getPayloadField().getRowCount() == 0) {
				getDoneButton().setEnabled(false);
			}
		}
	}

	protected class AddSinglePayloadAction extends AbstractAction {
		public AddSinglePayloadAction() {
			super(Constant.messages.getString("fuzz.add.singlePayload"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String input = getPayloadText().getText();
			if (input.startsWith(FileFuzzer.TYPE_SIG_BEG)) {
				int i = input.indexOf(FileFuzzer.TYPE_SIG_END);
				if (i > 0) {
					Payload.Type type;
					switch (input.substring(8, i)) {
					case "REGEX":
						type = Payload.Type.REGEX;
						break;
					case "FILE":
						type = Payload.Type.FILE;
						break;
					case "SCRIPT":
						type = Payload.Type.SCRIPT;
						break;
					default:
						type = Payload.Type.STRING;
						break;
					}
					String data = input.substring(i + 2);
					P pay = factory.createPayload(type, data);
					if (pay != null) {
						target.getPayloads().add(pay);
						payloadModel.addEntry(pay);
						getDoneButton().setEnabled(true);
					}
				}
			} else {
				P newP = factory.createPayload(input);
				if (newP != null) {
					target.getPayloads().add(newP);
					payloadModel.addEntry(newP);
					getDoneButton().setEnabled(true);
				}
			}
		}
	}

	protected class AddRegExAction extends AbstractAction {
		public AddRegExAction() {
			super(Constant.messages.getString("fuzz.add.regExPayload"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String input = getRegExText().getText();
			P newP = factory.createPayload(Payload.Type.REGEX, input,
					((Number) getRegExLimitField().getValue()).intValue() - 1);
			if (newP != null) {
				target.getPayloads().add(newP);
				payloadModel.addEntry(newP);
				getDoneButton().setEnabled(true);
			}
		}
	}

	protected class AddFuzzerFileAction extends AbstractAction {
		public AddFuzzerFileAction() {
			super(Constant.messages.getString("fuzz.add.fuzzerFile"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String choice = getCategoryField().getSelectedItem();
			String cat = getCategoryField().getSelectedCategory();
			if ((res.getFileFuzzerCategories().contains(cat) && res
					.getFileFuzzerNames(cat).contains(choice))
					|| (res.getJBroFuzzCategories().contains(cat) && res
							.getJBroFuzzFuzzerNames(cat).contains(choice))) {
				P pay = factory.createPayload(Payload.Type.FILE, cat + " --> "
						+ choice);
				target.getPayloads().add(pay);
				payloadModel.addEntry(pay);
				getDoneButton().setEnabled(true);
			}
		}
	}

	protected class AddScriptAction extends AbstractAction {
		public AddScriptAction() {
			super(Constant.messages.getString("fuzz.add.fuzzScript"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (payloadScripts.getSelectedItem() != null) {
				String name = (String) payloadScripts.getSelectedItem();
				P newP = factory.createPayload(Payload.Type.SCRIPT, name);
				if (newP != null) {
					target.getPayloads().add(newP);
					payloadModel.addEntry(newP);
					getDoneButton().setEnabled(true);
				}
			}
		}
	}

	protected class DoneAction extends AbstractAction {

		private static final long serialVersionUID = -6716179197963523133L;

		public DoneAction() {
			super(Constant.messages.getString("fuzz.button.add.done"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			listen.notifyFuzzerComplete(target);
			setVisible(false);
		}
	}

	protected class CancelAction extends AbstractAction {

		private static final long serialVersionUID = -6716179197963523133L;
		private ArrayList<P> payloads;

		public CancelAction() {
			super(Constant.messages.getString("fuzz.button.cancel"));
			payloads = new ArrayList<>();
		}

		public void setPayloads(ArrayList<P> p) {
			payloads = p;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			target.setPayloads(payloads);
			listen.notifyFuzzerComplete(target);
			setVisible(false);
		}
	}

	private static class ComboMenuBar extends JMenuBar {

		JMenu menu;
		String cat;
		Dimension preferredSize;

		public ComboMenuBar(JMenu menu) {
			this.menu = menu;
			MenuItemListener listener = new MenuItemListener();
			setListener(menu, listener);
			add(menu);
			this.setMinimumSize(new Dimension(50, 28));
		}

		class MenuItemListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				JMenuItem item = (JMenuItem) e.getSource();
				menu.setText(item.getText());
				JPopupMenu popUp = ((JPopupMenu) item.getParent());
				cat = "";
				while (popUp.getInvoker() instanceof JMenu
						&& popUp.getInvoker().getParent() instanceof JPopupMenu) {
					cat = ((JMenu) popUp.getInvoker()).getText() + " / " + cat;
					popUp = (JPopupMenu) popUp.getInvoker().getParent();
				}
				cat = cat.substring(0, cat.length() - 3);
				menu.requestFocus();
			}
		}

		private void setListener(JMenuItem item, ActionListener listener) {
			if (item instanceof JMenu) {
				JMenu menu = (JMenu) item;
				int n = menu.getItemCount();
				for (int i = 0; i < n; i++) {
					setListener(menu.getItem(i), listener);
				}
			} else if (item != null) { // null means separator
				item.addActionListener(listener);
			}
		}

		public String getSelectedCategory() {
			return cat;
		}

		public String getSelectedItem() {
			return menu.getText();
		}

		@Override
		public void setPreferredSize(Dimension size) {
			preferredSize = size;
		}

		@Override
		public Dimension getPreferredSize() {
			if (preferredSize == null) {
				Dimension menuD = getItemSize(menu);
				Insets margin = menu.getMargin();
				Dimension retD = new Dimension(menuD.width, margin.top
						+ margin.bottom + menuD.height);
				menu.setPreferredSize(retD);
				preferredSize = retD;
			}
			return preferredSize;
		}

		private Dimension getItemSize(JMenu menu) {
			Dimension d = new Dimension(0, 0);
			int n = menu.getItemCount();
			for (int i = 0; i < n; i++) {
				Dimension itemD;
				JMenuItem item = menu.getItem(i);
				if (item instanceof JMenu) {
					itemD = getItemSize((JMenu) item);
				} else if (item != null) {
					itemD = item.getPreferredSize();
				} else {
					itemD = new Dimension(0, 0); // separator
				}
				d.width = Math.max(d.width, itemD.width);
				d.height = Math.max(d.height, itemD.height);
			}
			return d;
		}

		private static class ComboMenu extends JMenu {

			public ComboMenu(String label) {
				super(label);
				setBorder(new EtchedBorder());
				setHorizontalTextPosition(JButton.LEFT);
				setFocusPainted(true);
			}
		}

		public static JMenu createMenu(String label) {
			return new ComboMenu(label);
		}

	}

	private static class PayloadTableModel<P extends Payload> extends
			AbstractTableModel {
		private static String[] columnNames = {
				Constant.messages.getString("fuzz.table.payload"),
				Constant.messages.getString("fuzz.table.length"),
				Constant.messages.getString("fuzz.table.recursive") };
		private ArrayList<P> payloads = new ArrayList<>();

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return payloads.size();
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 0) {
				return payloads.get(row).toString();
			} else if (col == 1) {
				return payloads.get(row).getLength();
			} else if (col == 2) {
				return payloads.get(row).getRecursive();
			} else {
				return "";
			}
		}

		@Override
		public Class<?> getColumnClass(int c) {
			switch (c) {
			case 1:
				return Integer.class;
			case 2:
				return Boolean.class;
			default:
				return String.class;
			}
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			if (col != 0) {
				return true;
			}
			return false;
		}

		public void addEntries(ArrayList<P> pays) {
			payloads.addAll(pays);
			fireTableRowsInserted(payloads.size() - pays.size(),
					payloads.size() - 1);
		}

		public void addEntry(P pay) {
			payloads.add(pay);
			fireTableRowsInserted(payloads.size() - 1, payloads.size() - 1);
		}

		public void deleteEntry(int i) {
			payloads.remove(i);
			fireTableRowsDeleted(i, i);
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (col == 1 && value instanceof Integer) {
				payloads.get(row).setLength((int) value);
				fireTableCellUpdated(row, col);
			} else if (col == 2 && value instanceof Boolean) {
				payloads.get(row).setRecursive((boolean) value);
				fireTableCellUpdated(row, col);
			}
		}
	}
}
