/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.extension.multiFuzz;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.owasp.jbrofuzz.core.Fuzzer;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.Message;

public abstract class FuzzDialog<M extends Message, L extends FuzzLocation<M>, P extends Payload, G extends FuzzGap<M, L, P>>
		extends AbstractDialog {

	private static final long serialVersionUID = 3855005636913607013L;
	private static final Logger logger = Logger.getLogger(FuzzDialog.class);
	protected ExtensionFuzz res;
	protected M fuzzableMessage;

	private JPanel background;
	private JSplitPane splitPane;
	private JLabel info;
	private JPanel searchBar;

	private JButton cancelButton = null;
	private JButton startButton = null;

	private JButton addComponentButton;
	private JTextArea searchField;

	private JCheckBox scriptEnabled;
	protected TargetModel targetModel;
	protected JTable targetTable;
	private ArrayList<FuzzerListener<?, ArrayList<G>>> listeners = new ArrayList<FuzzerListener<?, ArrayList<G>>>();

	public abstract FileFuzzer<P> convertToFileFuzzer(Fuzzer jBroFuzzer);

	public abstract FuzzProcessFactory getFuzzProcessFactory();

	protected abstract PayloadFactory<P> getPayloadFactory();

	public M getMessage() {
		return this.fuzzableMessage;
	}

	protected abstract int addCustomComponents(JPanel panel, int currentRow);

	protected abstract FuzzComponent<M, L, G> getMessageContent();

	/**
	 * 
	 * @param extension
	 * @param fuzzTarget
	 * @throws HeadlessException
	 */
	public FuzzDialog(ExtensionFuzz ext, L loc, M msg) throws HeadlessException {
		super(View.getSingleton().getMainFrame(), true);
		this.setTitle(Constant.messages.getString("fuzz.title"));
		this.res = ext;
		fuzzableMessage = msg;
		initialize();
	}

	/**
	 * This method initializes the Dialog and its components
	 */
	protected void initialize() {
		this.setContentPane(getJPanel());
		this.setSize(800, 400);
	}

	/**
	 * This method initializes the main JPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (background == null) {
			background = new JPanel();
			background.setLayout(new GridBagLayout());
			int currentRow = 0;
			Font headLine = new Font(Font.SERIF, Font.BOLD, 20);
			JLabel headL = new JLabel(Constant.messages.getString("fuzz.title"));
			headL.setFont(headLine);
			GridBagConstraints h = getGBC(0, currentRow, 2, 1.0, 0.0,
					java.awt.GridBagConstraints.HORIZONTAL);
			h.anchor = java.awt.GridBagConstraints.PAGE_START;
			background.add(headL, h);
			currentRow++;
			GridBagConstraints i = getGBC(0, currentRow, 2, 1.0, 0.0,
					java.awt.GridBagConstraints.HORIZONTAL);
			i.anchor = java.awt.GridBagConstraints.PAGE_START;
			background.add(getInfo(), i);
			currentRow++;
			GridBagConstraints b = getGBC(0, currentRow, 2, 1.0, 1.0,
					java.awt.GridBagConstraints.BOTH);
			b.anchor = java.awt.GridBagConstraints.CENTER;
			background.add(getJTabbed(), b);
		}
		return background;
	}

	private JSplitPane getJTabbed() {
		if (splitPane == null) {

			Font headLine = new Font(Font.SERIF, Font.BOLD, 16);
			splitPane = new JSplitPane();

			JPanel left = new JPanel();
			left.setLayout(new GridBagLayout());
			int currentRow = 0;
			left.add(
					getMessageContent().messageView(),
					getGBC(0, currentRow, 2, 1.0, 1.0,
							java.awt.GridBagConstraints.BOTH));
			currentRow++;
			left.add(
					getSearchBar(),
					getGBC(0, currentRow, 2, 1.0, 0.0,
							java.awt.GridBagConstraints.HORIZONTAL));

			JPanel rbg = new JPanel();

			JTabbedPane tabbed = new JTabbedPane();
			JPanel targetDisplay = new JPanel();
			JPanel general = new JPanel();

			targetDisplay.setLayout(new GridBagLayout());
			currentRow = 0;

			JLabel targetHead = new JLabel(
					Constant.messages.getString("fuzz.targetHead"));
			targetHead.setFont(headLine);
			targetDisplay.add(targetHead, getGBC(0, currentRow, 6, 1));
			currentRow++;

			targetDisplay.add(
					new JScrollPane(getTargetField()),
					getGBC(0, currentRow, 3, 1.0, 1.0,
							java.awt.GridBagConstraints.BOTH));
			currentRow++;

			targetDisplay.add(getAddComponentButton(),
					getGBC(1, currentRow, 1, 0.0));

			general.setLayout(new GridBagLayout());
			currentRow = 0;
			JLabel messageOpts = new JLabel(
					Constant.messages.getString("fuzz.message.options"));

			messageOpts.setFont(headLine);
			general.add(messageOpts, getGBC(0, currentRow, 6, 1));
			currentRow++;
			general.add(
					new JLabel(Constant.messages
							.getString("fuzz.label.scriptEnabled")),
					getGBC(0, currentRow, 2, 0.125));
			general.add(getScriptEnabled(), getGBC(2, currentRow, 4, 0.125));
			currentRow++;
			currentRow = addCustomComponents(general, currentRow);

			tabbed.addTab(Constant.messages.getString("fuzz.tab.targets"),
					targetDisplay);
			tabbed.addTab(Constant.messages.getString("fuzz.tab.general"),
					general);

			rbg.setLayout(new GridBagLayout());
			rbg.add(tabbed,
					getGBC(0, 0, 3, 1.0, 1.0, java.awt.GridBagConstraints.BOTH));
			rbg.add(getStartButton(), getGBC(0, 1, 1, 0.0));
			rbg.add(getCancelButton(), getGBC(2, 1, 1, 0.0));

			Dimension minimumSize = new Dimension(50, 50);
			left.setMinimumSize(minimumSize);
			rbg.setMinimumSize(minimumSize);
			splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			splitPane.setLeftComponent(left);
			splitPane.setRightComponent(rbg);
			splitPane.setDividerLocation(400);
		}
		return splitPane;
	}

	protected JTable getTargetField() {
		if (targetTable == null) {
			targetModel = new TargetModel();
			targetTable = new JTable(targetModel);
			targetTable
					.setDefaultRenderer(Color.class, new ColorRenderer(true));
		}
		return targetTable;
	}

	protected JLabel getInfo() {
		if (info == null) {
			info = new JLabel(Constant.messages.getString("fuzz.info.gen"));
		}
		return info;
	}

	// Left Panel
	private JPanel getSearchBar() {
		if (searchBar == null) {
			this.searchBar = new JPanel();
			searchBar.setLayout(new GridBagLayout());
			searchField = new JTextArea(
					Constant.messages.getString("fuzz.label.search"));
			searchField.setEditable(true);
			searchBar
					.add(searchField,
							getGBC(0, 0, 4, 0.8, 1.0,
									java.awt.GridBagConstraints.BOTH));
			JButton search = new JButton();
			search.setAction(getSearchAction());
			searchBar.add(search, getGBC(5, 0, 1, 0.2));
		}
		return searchBar;
	}

	// Right Panel
	private Color getColor(int n) {
		float hue = (float) (n % 5) / 5;
		float sat = (float) Math.ceil((float) n / 5) / 2;
		float bright = (float) Math.ceil((float) n / 5);
		return Color.getHSBColor(hue, sat, bright);
	}

	protected GridBagConstraints getGBC(int x, int y, int width, double weightx) {
		return getGBC(x, y, width, weightx, 0.0,
				java.awt.GridBagConstraints.NONE);
	}

	protected GridBagConstraints getGBC(int x, int y, int width,
			double weightx, double weighty, int fill) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.insets = new java.awt.Insets(1, 5, 1, 5);
		gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gbc.fill = fill;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridwidth = width;
		return gbc;
	}

	public void addFuzzerListener(FuzzerListener<?, ArrayList<G>> listener) {
		listeners.add(listener);
	}

	public void removeFuzzerListener(FuzzerListener<?, ArrayList<G>> listener) {
		listeners.remove(listener);
	}

	public boolean getScripting() {
		return scriptEnabled.isSelected();
	}

	protected boolean isCustomCategory(String cat) {
		return Constant.messages.getString("fuzz.category.custom").equals(cat);
	}

	protected boolean isJBroFuzzCategory(String cat) {
		return cat.startsWith(ExtensionFuzz.JBROFUZZ_CATEGORY_PREFIX);
	}

	protected JButton getAddComponentButton() {
		if (addComponentButton == null) {
			addComponentButton = new JButton();
			addComponentButton.setAction(getAddFuzzAction());
			getAddComponentButton().setText(
					Constant.messages.getString("fuzz.button.add.add"));
			addComponentButton.setEnabled(true);
		}
		return addComponentButton;
	}

	protected JButton getStartButton() {
		if (startButton == null) {
			startButton = new JButton();
			startButton.setAction(getStartFuzzAction());
		}
		return startButton;
	}

	protected JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setAction(getCancelFuzzAction());
		}
		return cancelButton;
	}

	private JCheckBox getScriptEnabled() {
		if (scriptEnabled == null) {
			scriptEnabled = new JCheckBox();
			scriptEnabled.setSelected(true);
		}
		return scriptEnabled;
	}

	protected Action getSearchAction() {
		return new SearchAction();
	}

	protected AddFuzzAction getAddFuzzAction() {
		return new AddFuzzAction();
	}

	protected StartFuzzAction getStartFuzzAction() {
		return new StartFuzzAction();
	}

	protected CancelFuzzAction getCancelFuzzAction() {
		return new CancelFuzzAction();
	}

	protected class StartFuzzAction extends AbstractAction {

		private static final long serialVersionUID = -961522394390805325L;

		public StartFuzzAction() {
			super(Constant.messages.getString("fuzz.button.start"));
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			for (FuzzerListener<?, ArrayList<G>> f : listeners) {
				f.notifyFuzzerComplete(targetModel.getEntries());
			}
		}
	}

	protected class CancelFuzzAction extends AbstractAction {

		private static final long serialVersionUID = -6716179197963523133L;

		public CancelFuzzAction() {
			super(Constant.messages.getString("fuzz.button.cancel"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	}

	protected class AddFuzzAction extends AbstractAction {

		private static final long serialVersionUID = -961522394390805325L;

		public AddFuzzAction() {
			super(Constant.messages.getString("fuzz.button.add.add"));
			setEnabled(true);
		}

		protected boolean isValidLocation(L l) {
			boolean valid = true;
			for (G g : targetModel.getEntries()) {
				valid &= !l.overlap(g.getLocation());
			}
			return valid;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			getInfo().setText(Constant.messages.getString("fuzz.info.gen"));
			getMessageContent().highlight(targetModel.getEntries());
		}
	}

	private class SearchAction extends AbstractAction {
		public SearchAction() {
			super(Constant.messages.getString("fuzz.search"));
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			getMessageContent().search(searchField.getText());
		}
	}

	public class TargetModel extends AbstractTableModel {
		private String[] columnNames = {
				Constant.messages.getString("fuzz.table.origHead"),
				Constant.messages.getString("fuzz.table.color") };
		private ArrayList<G> targets = new ArrayList<G>();

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return targets.size();
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 0) {
				return targets.get(row).orig();
			} else if (col == 1) {
				return getColor(row + 1);
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void addEntries(ArrayList<G> t) {
			targets.addAll(t);
			fireTableRowsInserted(targets.size() - t.size(), targets.size() - 1);
		}

		public void addEntry(G t) {
			targets.add(t);
			fireTableRowsInserted(targets.size() - 1, targets.size() - 1);
		}

		public ArrayList<G> getEntries() {
			return targets;
		}

		public void removeEntry(G t) {
			int idx = targets.indexOf(t);
            if (idx != -1) {
                targets.remove(idx);
                fireTableRowsDeleted(idx, idx);
            } 
		}
	}

	public class ColorRenderer extends JLabel implements TableCellRenderer {
		Border unselectedBorder = null;
		Border selectedBorder = null;
		boolean isBordered = true;

		public ColorRenderer(boolean isBordered) {
			this.isBordered = isBordered;
			setOpaque(true); // MUST do this for background to show up.
		}
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object color, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Color newColor = (Color) color;
			setBackground(newColor);
			if (isBordered) {
				if (isSelected) {
					if (selectedBorder == null) {
						selectedBorder = BorderFactory.createMatteBorder(2, 5,
								2, 5, table.getSelectionBackground());
					}
					setBorder(selectedBorder);
				} else {
					if (unselectedBorder == null) {
						unselectedBorder = BorderFactory.createMatteBorder(2,
								5, 2, 5, table.getBackground());
					}
					setBorder(unselectedBorder);
				}
			}

			setToolTipText("RGB value: " + newColor.getRed() + ", "
					+ newColor.getGreen() + ", " + newColor.getBlue());
			return this;
		}
	}
}