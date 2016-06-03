/*
 * Created on May 18, 2004
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/05/31 Added option to dynamically change the display
// ZAP: 2012/03/15 Changed so the change display option stays visually selected.
// ZAP: 2012/04/26 Removed the method setStatus(String) and instance variable
// "txtStatus".
// ZAP: 2013/07/23 Issue 738: Options to hide tabs
// ZAP: 2015/01/29 Issue 1489: Version number in window title
// ZAP: 2016/04/04 Do not require a restart to show/hide the tool bar
// ZAP: 2016/04/06 Fix layouts' issues

package org.parosproxy.paros.view;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.MainToolbarPanel;
import org.zaproxy.zap.view.ZapToggleButton;

public class MainFrame extends AbstractFrame {

	private static final Logger LOGGER = Logger.getLogger(MainFrame.class);

	private static final String TABS_VIEW_TOOL_TIP = Constant.messages.getString("view.toolbar.messagePanelsPosition.tabs");
	private static final String DISABLED_TABS_VIEW_TOOL_TIP = Constant.messages.getString("view.toolbar.messagePanelsPosition.tabs.disabled");
	private static final String ABOVE_VIEW_TOOL_TIP = Constant.messages.getString("view.toolbar.messagePanelsPosition.above");
	private static final String DISABLED_ABOVE_VIEW_TOOL_TIP = Constant.messages.getString("view.toolbar.messagePanelsPosition.above.disabled");
	private static final String SIDE_BY_SIDE_VIEW_TOOL_TIP = Constant.messages.getString("view.toolbar.messagePanelsPosition.sideBySide");
	private static final String DISABLED_SIDE_BY_SIDE_VIEW_TOOL_TIP = Constant.messages.getString("view.toolbar.messagePanelsPosition.sideBySide.disabled");

	private static final long serialVersionUID = -1430550461546083192L;

	private final OptionsParam options;
	private JPanel paneContent = null;
	// ZAP: Removed instance variable (JLabel txtStatus). The status label that
	// was in the footer panel is no longer used.
	private final WorkbenchPanel paneStandard;
	private org.parosproxy.paros.view.MainMenuBar mainMenuBar = null;
	private JPanel paneDisplay = null;

	private MainToolbarPanel mainToolbarPanel = null;
	private MainFooterPanel mainFooterPanel = null;

	/**
	 * The {@code ZapToggleButton} that sets whether or not the tabs should show the panels' names.
	 * <p>
	 * Lazily initialised.
	 * 
	 * @see #getShowTabIconNamesButton()
	 */
	private ZapToggleButton showTabIconNamesButton;

	/**
	 * The {@code JButton} that shows all tabs.
	 * <p>
	 * Lazily initialised.
	 * 
	 * @see #getShowAllTabsButton()
	 */
	private JButton showAllTabsButton;

	/**
	 * The {@code JButton} that hides all tabs (if hideable and not pinned).
	 * <p>
	 * Lazily initialised.
	 * 
	 * @see #getHideAllTabsButton()
	 */
	private JButton hideAllTabsButton;

	/**
	 * The current workbench layout, never {@code null}.
	 * 
	 * @see #setWorkbenchLayout(WorkbenchPanel.Layout)
	 */
	private WorkbenchPanel.Layout workbenchLayout;

	/**
	 * The {@code JToggleButton} that sets the layout {@link WorkbenchPanel.Layout#EXPAND_SELECT}.
	 * <p>
	 * Lazily initialised.
	 * 
	 * @see #getExpandSelectLayoutButton()
	 */
	private JToggleButton expandSelectLayoutButton;

	/**
	 * The {@code JToggleButton} that sets the layout {@link WorkbenchPanel.Layout#EXPAND_STATUS}.
	 * <p>
	 * Lazily initialised.
	 * 
	 * @see #getExpandStatusLayoutButton()
	 */
	private JToggleButton expandStatusLayoutButton;

	/**
	 * The {@code JToggleButton} that sets the layout {@link WorkbenchPanel.Layout#FULL}.
	 * <p>
	 * Lazily initialised.
	 * 
	 * @see #getFullLayoutButton()
	 */
	private JToggleButton fullLayoutButton;

	/**
	 * The current position of the response panel, never {@code null}.
	 * 
	 * @see #setResponsePanelPosition(WorkbenchPanel.ResponsePanelPosition)
	 */
	private WorkbenchPanel.ResponsePanelPosition responsePanelPosition;

	/**
	 * The {@code ZapToggleButton} that sets the response panel position
	 * {@link WorkbenchPanel.ResponsePanelPosition#TABS_SIDE_BY_SIDE}.
	 * <p>
	 * Lazily initialised.
	 * 
	 * @see #getTabsResponsePanelPositionButton()
	 */
	private ZapToggleButton tabsResponsePanelPositionButton;

	/**
	 * The {@code ZapToggleButton} that sets the response panel position
	 * {@link WorkbenchPanel.ResponsePanelPosition#PANEL_ABOVE}.
	 * <p>
	 * Lazily initialised.
	 * 
	 * @see #getAboveResponsePanelPositionButton()
	 */
	private ZapToggleButton aboveResponsePanelPositionButton;

	/**
	 * The {@code ZapToggleButton} that sets the response panel position
	 * {@link WorkbenchPanel.ResponsePanelPosition#PANELS_SIDE_BY_SIDE}.
	 * <p>
	 * Lazily initialised.
	 * 
	 * @see #getPanelsResponsePanelPositionButton()
	 */
	private ZapToggleButton panelsResponsePanelPositionButton;

	/**
	 * @deprecated (2.5.0) Use {@link #MainFrame(OptionsParam, HttpPanelRequest, HttpPanelResponse)} instead.
	 */
	@Deprecated
	@SuppressWarnings("javadoc")
	public MainFrame(int displayOption) {
		this(Model.getSingleton().getOptionsParam(),
			 View.getSingleton().getRequestPanel(),
			 View.getSingleton().getResponsePanel());

		changeDisplayOption(displayOption);
	}

	/**
	 * Constructs a {@code MainFrame} with the given options and request and response panels.
	 * 
	 * @param options the options
	 * @param requestPanel the main request panel
	 * @param responsePanel the main response panel
	 * @throws IllegalArgumentException if any of the parameters is {@code null}.
	 * @since 2.5.0
	 */
	public MainFrame(OptionsParam options, AbstractPanel requestPanel, AbstractPanel responsePanel) {
		super();

		if (options == null) {
			throw new IllegalArgumentException("Parameter options must not be null");
		}
		if (requestPanel == null) {
			throw new IllegalArgumentException("Parameter requestPanel must not be null");
		}
		if (responsePanel == null) {
			throw new IllegalArgumentException("Parameter responsePanel must not be null");
		}

		this.options = options;
		paneStandard = new WorkbenchPanel(options.getViewParam(), requestPanel, responsePanel);
		paneStandard.setLayout(new CardLayout());
		paneStandard.setName("paneStandard");

		initialize();

		applyViewOptions();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setJMenuBar(getMainMenuBar());
		this.setContentPane(getPaneContent());
    	this.setPreferredSize(new Dimension(1000, 800));
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				getMainMenuBar().getMenuFileControl().exit();
			}
		});

		this.setVisible(false);
	}

	/**
	 * This method initializes paneContent
	 *
	 * @return JPanel
	 */
	private JPanel getPaneContent() {
		if (paneContent == null) {

			paneContent = new JPanel();
			paneContent.setLayout(new BoxLayout(getPaneContent(), BoxLayout.Y_AXIS));
			paneContent.setEnabled(true);

			paneContent.add(getMainToolbarPanel(), null);

			paneContent.add(getPaneDisplay(), null);
			paneContent.add(getMainFooterPanel(), null);

		}
		return paneContent;
	}

	/**
	 * Gets the {@code WorkbenchPanel}.
	 *
	 * @return the workbench panel
	 * @since 2.2.0
	 */
	public WorkbenchPanel getWorkbench() {
		return paneStandard;
	}

	/**
	 * This method initializes mainMenuBar
	 *
	 * @return org.parosproxy.paros.view.MenuDisplay
	 */
	public org.parosproxy.paros.view.MainMenuBar getMainMenuBar() {
		if (mainMenuBar == null) {
			mainMenuBar = new org.parosproxy.paros.view.MainMenuBar();
		}
		return mainMenuBar;
	}

	// ZAP: Removed the method getTxtStatus()
	
	// ZAP: Removed the method setStatus(String). The status label
	// ("txtStatus") that was in the footer panel is no longer used.
	
	/**
	 * This method initializes paneDisplay
	 *
	 * @return JPanel
	 */
	public JPanel getPaneDisplay() {
		if (paneDisplay == null) {
			paneDisplay = new JPanel();
			paneDisplay.setLayout(new CardLayout());
			paneDisplay.setName("paneDisplay");
			paneDisplay.add(getWorkbench(), getWorkbench().getName());
		}
		return paneDisplay;
	}

	// ZAP: Added main toolbar panel
	public MainToolbarPanel getMainToolbarPanel() {
		if (mainToolbarPanel == null) {
			mainToolbarPanel = new MainToolbarPanel();

			mainToolbarPanel.addButton(getShowAllTabsButton());
			mainToolbarPanel.addButton(getHideAllTabsButton());
			mainToolbarPanel.addButton(getShowTabIconNamesButton());
			mainToolbarPanel.addSeparator();

			ButtonGroup layoutsButtonGroup = new ButtonGroup();
			mainToolbarPanel.addButton(getExpandSelectLayoutButton());
			layoutsButtonGroup.add(getExpandSelectLayoutButton());
			mainToolbarPanel.addButton(getExpandStatusLayoutButton());
			layoutsButtonGroup.add(getExpandStatusLayoutButton());
			mainToolbarPanel.addButton(getFullLayoutButton());
			layoutsButtonGroup.add(getFullLayoutButton());
			mainToolbarPanel.addSeparator();

			ButtonGroup responsePanelPositionsButtonGroup = new ButtonGroup();
			mainToolbarPanel.addButton(getTabsResponsePanelPositionButton());
			responsePanelPositionsButtonGroup.add(getTabsResponsePanelPositionButton());
			mainToolbarPanel.addButton(getPanelsResponsePanelPositionButton());
			responsePanelPositionsButtonGroup.add(getPanelsResponsePanelPositionButton());
			mainToolbarPanel.addButton(getAboveResponsePanelPositionButton());
			responsePanelPositionsButtonGroup.add(getAboveResponsePanelPositionButton());
			mainToolbarPanel.addSeparator();
		}
		return mainToolbarPanel;
	}

	private JButton getShowAllTabsButton() {
		if (showAllTabsButton == null) {
			showAllTabsButton = new JButton();
			showAllTabsButton.setIcon(new ImageIcon(WorkbenchPanel.class.getResource("/resource/icon/fugue/ui-tab-show.png")));
			showAllTabsButton.setToolTipText(Constant.messages.getString("menu.view.tabs.show"));
			DisplayUtils.scaleIcon(showAllTabsButton);

			showAllTabsButton.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					View.getSingleton().showAllTabs();
				}
			});
		}
		return showAllTabsButton;
	}

	private JButton getHideAllTabsButton() {
		if (hideAllTabsButton == null) {
			hideAllTabsButton = new JButton();
			hideAllTabsButton.setIcon(new ImageIcon(WorkbenchPanel.class.getResource("/resource/icon/fugue/ui-tab-hide.png")));
			hideAllTabsButton.setToolTipText(Constant.messages.getString("menu.view.tabs.hide"));
			DisplayUtils.scaleIcon(hideAllTabsButton);

			hideAllTabsButton.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					View.getSingleton().hideAllTabs();
				}
			});
		}
		return hideAllTabsButton;
	}

	private JToggleButton getShowTabIconNamesButton() {
		if (showTabIconNamesButton == null) {
			showTabIconNamesButton = new ZapToggleButton();
			showTabIconNamesButton.setIcon(new ImageIcon(WorkbenchPanel.class.getResource("/resource/icon/ui_tab_icon.png")));
			showTabIconNamesButton.setToolTipText(Constant.messages.getString("view.toolbar.showNames"));
			showTabIconNamesButton
					.setSelectedIcon(new ImageIcon(WorkbenchPanel.class.getResource("/resource/icon/ui_tab_text.png")));
			showTabIconNamesButton.setSelectedToolTipText(Constant.messages.getString("view.toolbar.showIcons"));
			showTabIconNamesButton.setSelected(Model.getSingleton().getOptionsParam().getViewParam().getShowTabNames());
			DisplayUtils.scaleIcon(showTabIconNamesButton);

			showTabIconNamesButton.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(ActionEvent evt) {
					boolean showTabNames = getShowTabIconNamesButton().isSelected();
					setShowTabNames(showTabNames);
					Model.getSingleton().getOptionsParam().getViewParam().setShowTabNames(showTabNames);
					try {
						Model.getSingleton().getOptionsParam().getViewParam().getConfig().save();
					} catch (ConfigurationException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
			});
		}
		return showTabIconNamesButton;
	}

	private JToggleButton getExpandSelectLayoutButton() {
		if (expandSelectLayoutButton == null) {
			expandSelectLayoutButton = new JToggleButton(
					new ChangeWorkbenchLayoutAction(
							WorkbenchPanel.class.getResource("/resource/icon/expand_sites.png"),
							WorkbenchPanel.Layout.EXPAND_SELECT));
			expandSelectLayoutButton.setToolTipText(Constant.messages.getString("view.toolbar.expandSites"));
		}
		return expandSelectLayoutButton;
	}

	private JToggleButton getExpandStatusLayoutButton() {
		if (expandStatusLayoutButton == null) {
			expandStatusLayoutButton = new JToggleButton(
					new ChangeWorkbenchLayoutAction(
							WorkbenchPanel.class.getResource("/resource/icon/expand_info.png"),
							WorkbenchPanel.Layout.EXPAND_STATUS));
			expandStatusLayoutButton.setToolTipText(Constant.messages.getString("view.toolbar.expandInfo"));
		}
		return expandStatusLayoutButton;
	}

	private JToggleButton getFullLayoutButton() {
		if (fullLayoutButton == null) {
			fullLayoutButton = new JToggleButton(
					new ChangeWorkbenchLayoutAction(
							WorkbenchPanel.class.getResource("/resource/icon/expand_full.png"),
							WorkbenchPanel.Layout.FULL));
			fullLayoutButton.setToolTipText(Constant.messages.getString("view.toolbar.expandFull"));
		}
		return fullLayoutButton;
	}

	private ZapToggleButton getTabsResponsePanelPositionButton() {
		if (tabsResponsePanelPositionButton == null) {
			tabsResponsePanelPositionButton = new ZapToggleButton(
					new SetResponsePanelPositionAction(
							WorkbenchPanel.class.getResource("/resource/icon/layout_tabbed.png"),
							WorkbenchPanel.ResponsePanelPosition.TABS_SIDE_BY_SIDE));
			tabsResponsePanelPositionButton.setToolTipText(TABS_VIEW_TOOL_TIP);
			tabsResponsePanelPositionButton.setDisabledToolTipText(DISABLED_TABS_VIEW_TOOL_TIP);
		}
		return tabsResponsePanelPositionButton;
	}

	private ZapToggleButton getPanelsResponsePanelPositionButton() {
		if (panelsResponsePanelPositionButton == null) {
			panelsResponsePanelPositionButton = new ZapToggleButton(
					new SetResponsePanelPositionAction(
							WorkbenchPanel.class.getResource("/resource/icon/layout_horizontal_split.png"),
							WorkbenchPanel.ResponsePanelPosition.PANELS_SIDE_BY_SIDE));
			panelsResponsePanelPositionButton.setToolTipText(SIDE_BY_SIDE_VIEW_TOOL_TIP);
			panelsResponsePanelPositionButton.setDisabledToolTipText(DISABLED_SIDE_BY_SIDE_VIEW_TOOL_TIP);
		}
		return panelsResponsePanelPositionButton;
	}

	private ZapToggleButton getAboveResponsePanelPositionButton() {
		if (aboveResponsePanelPositionButton == null) {
			aboveResponsePanelPositionButton = new ZapToggleButton(
					new SetResponsePanelPositionAction(
							WorkbenchPanel.class.getResource("/resource/icon/layout_vertical_split.png"),
							WorkbenchPanel.ResponsePanelPosition.PANEL_ABOVE));
			aboveResponsePanelPositionButton.setToolTipText(ABOVE_VIEW_TOOL_TIP);
			aboveResponsePanelPositionButton.setDisabledToolTipText(DISABLED_ABOVE_VIEW_TOOL_TIP);
		}
		return aboveResponsePanelPositionButton;
	}

	// ZAP: Added footer toolbar panel
	public MainFooterPanel getMainFooterPanel() {
		if (mainFooterPanel == null) {
			mainFooterPanel = new MainFooterPanel();
		}
		return mainFooterPanel;
	}

	/**
	 * @deprecated (2.5.0) Use {@link #setWorkbenchLayout(WorkbenchPanel.Layout)} instead.
	 */
	@Deprecated
	@SuppressWarnings("javadoc")
	public void changeDisplayOption(int displayOption) {
		setWorkbenchLayout(WorkbenchPanel.Layout.getLayout(displayOption));
	}

	/**
	 * Applies the view options to the main frame components.
	 * <p>
	 * It controls the visibility of the main tool bar, the layout and response panel position of the workbench panel and if the
	 * tabs should display the panels' names.
	 * 
	 * @since 2.5.0
	 * @see #setMainToolbarVisible(boolean)
	 * @see #setWorkbenchLayout(WorkbenchPanel.Layout)
	 * @see #setResponsePanelPosition(WorkbenchPanel.ResponsePanelPosition)
	 * @see #setShowTabNames(boolean)
	 * @see org.parosproxy.paros.extension.option.OptionsParamView
	 */
	public void applyViewOptions() {
		setMainToolbarVisible(options.getViewParam().isShowMainToolbar());

		setWorkbenchLayout(WorkbenchPanel.Layout.getLayout(options.getViewParam().getDisplayOption()));

		WorkbenchPanel.ResponsePanelPosition position = WorkbenchPanel.ResponsePanelPosition.TABS_SIDE_BY_SIDE;
		try {
			position = WorkbenchPanel.ResponsePanelPosition.valueOf(options.getViewParam().getResponsePanelPosition());
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Failed to restore the position of response panel: ", e);
		}
		setResponsePanelPosition(position);

		setShowTabNames(options.getViewParam().getShowTabNames());
	}

	/**
	 * Sets the layout of the workbench panel.
	 * <p>
	 * If the layout is already set no further action is taken, otherwise updates the main tool bar buttons, the workbench panel
	 * and the configurations file.
	 *
	 * @param layout the new layout of the workbench panel
	 * @throws IllegalArgumentException if the given parameter is {@code null}.
	 * @since 2.5.0
	 * @see #getWorkbenchLayout()
	 * @see #setResponsePanelPosition(WorkbenchPanel.ResponsePanelPosition)
	 */
	public void setWorkbenchLayout(WorkbenchPanel.Layout layout) {
		if (layout == null) {
			throw new IllegalArgumentException("Parameter layout must not be null.");
		}

		if (workbenchLayout == layout) {
			return;
		}

		workbenchLayout = layout;

		switch (workbenchLayout) {
		case EXPAND_STATUS:
			getExpandStatusLayoutButton().setSelected(true);
			setResponsePanelPositionButtonsEnabled(true);
			break;
		case FULL:
			getFullLayoutButton().setSelected(true);
			setResponsePanelPositionButtonsEnabled(false);
			break;
		case EXPAND_SELECT:
		default:
			getExpandSelectLayoutButton().setSelected(true);
			setResponsePanelPositionButtonsEnabled(true);
			break;
		}

		getWorkbench().setWorkbenchLayout(workbenchLayout);
		options.getViewParam().setDisplayOption(workbenchLayout.getId());
	}

	/**
	 * Sets whether or not the buttons that control the response panel position should be enabled.
	 *
	 * @param enabled {@code true} if the buttons should be enabled, {@code false} otherwise.
	 */
	private void setResponsePanelPositionButtonsEnabled(boolean enabled) {
		tabsResponsePanelPositionButton.setEnabled(enabled);
		panelsResponsePanelPositionButton.setEnabled(enabled);
		aboveResponsePanelPositionButton.setEnabled(enabled);
	}

	/**
	 * Gets the workbench layout.
	 *
	 * @return the workbench layout, never {@code null}.
	 * @since 2.5.0
	 * @see #setWorkbenchLayout(WorkbenchPanel.Layout)
	 */
	public WorkbenchPanel.Layout getWorkbenchLayout() {
		return workbenchLayout;
	}

	/**
	 * Sets the position of the response panel. Should be considered a hint, not all workbench layouts might use this setting.
	 * <p>
	 * If the position is already set no further action is taken, otherwise updates the main tool bar buttons, the workbench
	 * panel and the configurations file.
	 *
	 * @param position the new position of the response panel
	 * @throws IllegalArgumentException if the given parameter is {@code null}.
	 * @since 2.5.0
	 * @see #getResponsePanelPosition()
	 * @see #setWorkbenchLayout(org.parosproxy.paros.view.WorkbenchPanel.Layout)
	 */
	public void setResponsePanelPosition(WorkbenchPanel.ResponsePanelPosition position) {
		if (position == null) {
			throw new IllegalArgumentException("Parameter position must not be null.");
		}

		if (responsePanelPosition == position) {
			return;
		}

		responsePanelPosition = position;

		switch (position) {
		case PANEL_ABOVE:
			aboveResponsePanelPositionButton.setSelected(true);
			break;
		case PANELS_SIDE_BY_SIDE:
			panelsResponsePanelPositionButton.setSelected(true);
			break;
		case TABS_SIDE_BY_SIDE:
		default:
			tabsResponsePanelPositionButton.setSelected(true);
		}

		getWorkbench().setResponsePanelPosition(responsePanelPosition);
		options.getViewParam().setResponsePanelPosition(responsePanelPosition.toString());
	}

	/**
	 * Gets the response panel position.
	 *
	 * @return the response panel position, never {@code null}.
	 * @since 2.5.0
	 * @see #setResponsePanelPosition(WorkbenchPanel.ResponsePanelPosition)
	 */
	public WorkbenchPanel.ResponsePanelPosition getResponsePanelPosition() {
		return responsePanelPosition;
	}

	/**
	 * Sets whether or not the tabs should display the name of the panels.
	 * 
	 * @param showTabNames {@code true} if the names should be shown, {@code false} otherwise.
	 * @since 2.5.0
	 */
	public void setShowTabNames(boolean showTabNames) {
		getWorkbench().toggleTabNames(showTabNames);
		getShowTabIconNamesButton().setSelected(showTabNames);
	}

	/**
	 * Sets the title of the main window.
	 * <p>
	 * The actual title set is the given {@code title} followed by the program name and version.
	 * 
	 * @see Constant#PROGRAM_NAME
	 * @see Constant#PROGRAM_VERSION
	 */
	@Override
	public void setTitle(String title) {
		StringBuilder strBuilder = new StringBuilder();
		if (title != null && !title.isEmpty()) {
			strBuilder.append(title);
			strBuilder.append(" - ");
		}
		strBuilder.append(Constant.PROGRAM_NAME).append(' ').append(Constant.PROGRAM_VERSION);
		super.setTitle(strBuilder.toString());
	}

	/**
	 * Sets whether or not the main tool bar should be visible.
	 *
	 * @param visible {@code true} if the main tool bar should be visible, {@code false} otherwise.
	 * @since 2.5.0
	 */
	public void setMainToolbarVisible(boolean visible) {
		getMainToolbarPanel().setVisible(visible);
	}

	/**
	 * An {@code Action} that changes the layout of the workbench panels.
	 * 
	 * @see MainFrame#setWorkbenchLayout(WorkbenchPanel.Layout)
	 */
	private class ChangeWorkbenchLayoutAction extends AbstractAction {

		private static final long serialVersionUID = 8323387638733162321L;

		private final WorkbenchPanel.Layout layout;

		public ChangeWorkbenchLayoutAction(URL iconURL, WorkbenchPanel.Layout layout) {
			super("", DisplayUtils.getScaledIcon(new ImageIcon(iconURL)));

			this.layout = layout;
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			setWorkbenchLayout(layout);
		}
	}

	/**
	 * An {@code Action} that changes the position of the response panel.
	 * 
	 * @see MainFrame#setResponsePanelPosition(org.parosproxy.paros.view.WorkbenchPanel.ResponsePanelPosition)
	 */
	private class SetResponsePanelPositionAction extends AbstractAction {

		private static final long serialVersionUID = 756133292459364854L;

		private final WorkbenchPanel.ResponsePanelPosition position;

		public SetResponsePanelPositionAction(URL iconLocation, WorkbenchPanel.ResponsePanelPosition position) {
			super("", new ImageIcon(iconLocation));

			this.position = position;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setResponsePanelPosition(position);
		}
	}

}
