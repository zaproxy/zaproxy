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
package org.zaproxy.zap.view.panels;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session.OnContextsChangedListener;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.LayoutHelper;
import org.zaproxy.zap.view.ScanPanel;
import org.zaproxy.zap.view.widgets.ContextSelectComboBox;

/**
 * A base implementation for a status panel with a control toolbar that shows information/actions
 * based on a selected {@link Context}.
 */
public abstract class AbstractContextSelectToolbarStatusPanel extends AbstractPanel implements
		OnContextsChangedListener {

	private static final long serialVersionUID = 7164298579345445108L;
	private static final Logger log = Logger.getLogger(AbstractContextSelectToolbarStatusPanel.class);

	/**
	 * Location provided to {@link #addToolBarElements(JToolBar, short, int)} to add items at the
	 * beginning of the toolbar.
	 */
	protected static final short TOOLBAR_LOCATION_START = 0;
	/**
	 * Location provided to {@link #addToolBarElements(JToolBar, short, int)} to add items right
	 * after the context selection box.
	 */
	protected static final short TOOLBAR_LOCATION_AFTER_CONTEXTS_SELECT = 1;
	/**
	 * Location provided to {@link #addToolBarElements(JToolBar, short, int)} to add items at the
	 * end of the toolbar
	 */
	protected static final short TOOLBAR_LOCATION_END = 99;

	protected String panelPrefix;

	private JToolBar panelToolbar = null;
	private JButton optionsButton = null;
	private ContextSelectComboBox contextSelectBox;

	public AbstractContextSelectToolbarStatusPanel(String prefix, ImageIcon icon) {
		super();
		this.panelPrefix = prefix;
		initialize(icon);
	}

	private void initialize(ImageIcon icon) {
		if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
			this.setSize(474, 251);
		}
		this.setName(Constant.messages.getString(panelPrefix + ".panel.title"));
		this.setIcon(icon);

		// Add the two components
		this.setLayout(new GridBagLayout());
		this.add(getPanelToolbar(), LayoutHelper.getGBC(0, 0, 1, 1.0d, 0.0d, GridBagConstraints.HORIZONTAL));
		this.add(getWorkPanel(), LayoutHelper.getGBC(0, 1, 1, 1.0d, 1.0d, GridBagConstraints.BOTH));

		// Register to be notified when the list of contexts changes
		Model.getSingleton().getSession().addOnContextsChangedListener(this);
	}

	private JToolBar getPanelToolbar() {
		if (panelToolbar == null) {

			panelToolbar = new javax.swing.JToolBar();
			panelToolbar.setLayout(new GridBagLayout());
			panelToolbar.setEnabled(true);
			panelToolbar.setFloatable(false);
			panelToolbar.setRollover(true);
			panelToolbar.setPreferredSize(new java.awt.Dimension(800, 30));
			panelToolbar.setName(panelPrefix + ".toolbar");

			setupToolbarElements(panelToolbar);
		}
		return panelToolbar;
	}

	/**
	 * Method used to setup the toolbar elements. Should not usually be overriden. Instead, use the
	 * {@link #addToolBarElements(JToolBar, short, int)} method to add elements at various points.
	 * @param toolbar the tool bar of the status panel
	 */
	protected void setupToolbarElements(JToolBar toolbar) {
		int x = 0;
		Insets insets = new Insets(0, 4, 0, 2);

		x = this.addToolBarElements(toolbar, TOOLBAR_LOCATION_START, x);

		toolbar.add(new JLabel(Constant.messages.getString(panelPrefix + ".toolbar.context.label")),
				LayoutHelper.getGBC(x++, 0, 1, 0, insets));
		toolbar.add(getContextSelectComboBox(), LayoutHelper.getGBC(x++, 0, 1, 0, insets));

		x = this.addToolBarElements(toolbar, TOOLBAR_LOCATION_AFTER_CONTEXTS_SELECT, x);

		toolbar.add(new JLabel(), LayoutHelper.getGBC(x++, 0, 1, 1.0)); // Spacer
		if (hasOptions()) {
			toolbar.add(getOptionsButton(), LayoutHelper.getGBC(x++, 0, 1, 0, insets));
		}

		this.addToolBarElements(toolbar, TOOLBAR_LOCATION_END, x);
	}

	/**
	 * Gets the options button.
	 *
	 * @return the options button
	 */
	protected JButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JButton();
			optionsButton
					.setToolTipText(Constant.messages.getString(panelPrefix + ".toolbar.button.options"));
			optionsButton.setIcon(new ImageIcon(ScanPanel.class.getResource("/resource/icon/16/041.png")));
			optionsButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Control.getSingleton().getMenuToolsControl()
							.options(Constant.messages.getString(panelPrefix + ".options.title"));
				}
			});
		}
		return optionsButton;
	}

	/**
	 * Gets the Context select combo box.
	 *
	 * @return the context select combo box
	 */
	protected ContextSelectComboBox getContextSelectComboBox() {
		if (contextSelectBox == null) {
			contextSelectBox = new ContextSelectComboBox();
			contextSelectBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					contextSelected((Context) contextSelectBox.getSelectedItem());
				}
			});
		}
		return contextSelectBox;
	}

	/**
	 * Adds elements to the tool bar. The method is called while initializing the StatusPanel, at
	 * some specific points ({@code TOOLBAR_LOCATION_*} constants):
	 * <ul>
	 * <li>{@link #TOOLBAR_LOCATION_START}</li>
	 * <li>{@link #TOOLBAR_LOCATION_AFTER_CONTEXTS_SELECT}</li>
	 * <li>{@link #TOOLBAR_LOCATION_END}</li>
	 * <li>other {@code TOOLBAR_LOCATION_*} constants defined in extending classes (if case)</li>
	 * </ul>
	 * <p>
	 * Should be overridden by all subclasses that want to add new elements to the ScanPanel's tool
	 * bar.
	 * <p>
	 * The tool bar uses a {@link GridBagLayout}, so elements have to be added with a
	 * {@link GridBagConstraints}. For this, the {@link LayoutHelper#getGBC(int, int, int, double)}
	 * methods can be used. The {@code gridX} parameter specifies the cell (as used in
	 * {@link GridBagConstraints#gridx}) of the current row where the elements can be added.
	 * <p>
	 * The method must return the new coordinates of the current cell, after the elements have been
	 * added.
	 * 
	 * @param toolBar the tool bar
	 * @param location the current location where elements will be added
	 * @param gridX the x coordinates of the current cell in the {@code GridBagLayout}
	 * @return the new coordinates of the current cell, after the elements have been added.
	 * @see LayoutHelper
	 * @see GridBagConstraints
	 * @see GridBagLayout
	 */
	protected int addToolBarElements(JToolBar toolBar, short location, int gridX) {
		return gridX;
	}

	/**
	 * Method called whenever a new context is selected.
	 * @param context the context that was selected
	 */
	protected void contextSelected(Context context) {
		log.debug("Selected new context: " + context);
		switchViewForContext(context);
	}

	/**
	 * Gets the selected context.
	 *
	 * @return the selected context
	 */
	public Context getSelectedContext() {
		return contextSelectBox.getSelectedContext();
	}

	@Override
	public void contextAdded(Context context) {
		log.debug("Context added...");
		contextSelectBox.reloadContexts(true);
	}

	@Override
	public void contextDeleted(Context context) {
		log.debug("Context deleted...");
		contextSelectBox.reloadContexts(false);
		contextSelectBox.setSelectedIndex(-1);
	}

	@Override
	public void contextsChanged() {
		log.debug("Contexts changed...");
		contextSelectBox.reloadContexts(false);
		contextSelectBox.setSelectedIndex(-1);
	}

	/**
	 * Called during initialization to check whether an Options button should be added to the
	 * toolbar or not. Implementations can override this method to remove the default Options
	 * button.
	 * @return {@code true} if the tool bar should show an Options button, {@code false} otherwise
	 */
	protected boolean hasOptions() {
		return true;
	}

	/**
	 * Called in order to build the main panel displayed below the toolbar.
	 * @return the main panel
	 */
	protected abstract Component getWorkPanel();

	/**
	 * Called in order to switch the data displayed on the main panel below the toolbar for a new
	 * context.
	 * <p>
	 * <strong>NOTE:</strong> Should not recreate a new work panel, but change the existing one (obtained through the
	 * first call to {@link #getWorkPanel()}) to show the new data (e.g. change the DataModel for a
	 * table).
	 * 
	 * @param context the context for which to display the panel
	 */
	protected abstract void switchViewForContext(Context context);
}
