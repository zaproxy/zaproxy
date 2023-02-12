/*
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
// ZAP: 2011/04/16 i18n
// ZAP: 2011/11/20 Handle dialogs with no children
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/04/25 Added @SuppressWarnings annotation in the method
//      getTreeNodeFromPanelName(String).
// ZAP: 2012/10/02 Issue 385: Added support for Contexts
// ZAP: 2013/08/05 Added accessor to shown panels
// ZAP: 2013/08/21 Added support for detecting when AbstractParamPanels are being shown/hidden in a
// AbstractParamDialog
// ZAP: 2013/11/28 Issue 923: Allow a footer to be set.
// ZAP: 2014/02/21 Issue 1043: Custom active scan dialog
// ZAP: 2014/12/10 Issue 1427: Standardize on [Cancel] [OK] button order
// ZAP: 2016/11/17 Issue 2701 Added support for additional buttons to support Factory Reset
// ZAP: 2018/01/08 Allow to expand the node of a param panel.
// ZAP: 2018/04/12 Allow to check if a param panel is selected.
// ZAP: 2019/03/19 Log the exception when validating/saving the AbstractParamPanels.
// ZAP: 2019/04/04 Log NullPointerException as error when validating/saving the AbstractParamPanels.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/08/25 Move NullPointerException log to AbstractParamContainerPanel.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2022/02/08 Set the dialogue as parent of the warning messages.
package org.parosproxy.paros.view;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.view.LayoutHelper;

public class AbstractParamDialog extends AbstractDialog {

    private static final long serialVersionUID = -5223178126156052670L;
    private static final Logger LOGGER = LogManager.getLogger(AbstractParamDialog.class);

    private int exitResult = JOptionPane.CANCEL_OPTION;

    private JPanel jContentPane = null;
    private JButton btnOK = null;
    private JButton btnCancel = null;
    private AbstractParamContainerPanel jSplitPane;
    // private JSplitPane jSplitPane = null;
    private JLabel footer = null;
    private String rootName = null;

    /**
     * Constructs an {@code AbstractParamDialog} with no parent and not modal.
     *
     * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
     */
    public AbstractParamDialog() {
        super();
        initialize();
    }

    /**
     * Constructs an {@code AbstractParamDialog} with the given parent, title and root node's name
     * and whether or not it's modal.
     *
     * @param parent the {@code Window} from which the dialog is displayed or {@code null} if this
     *     dialog has no parent
     * @param modal {@code true} if the dialogue should be modal, {@code false} otherwise
     * @param title the title of the dialogue
     * @param rootName the name of the root node
     * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
     */
    public AbstractParamDialog(Window parent, boolean modal, String title, String rootName) {
        super(parent, modal);
        this.rootName = rootName;
        initialize();
        this.setTitle(title);
    }

    /** This method initializes this */
    private void initialize() {
        // enables the options dialog to be in front, but an modal dialog
        // stays on top of the main application window, but doesn't block childs
        // Examples of childs: help window and client certificate viewer
        this.setModalityType(ModalityType.DOCUMENT_MODAL);

        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
            this.setSize(500, 375);
        }

        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane *
     *
     * @return javax.swing.JPanel *
     */
    private javax.swing.JPanel getJContentPane() {
        if (jContentPane == null) {

            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.add(getJSplitPane(), LayoutHelper.getGBC(0, 0, 1, 1.0, 1.0));

            // Create the footer
            JPanel footerPane = new JPanel();
            footerPane.setLayout(new GridBagLayout());
            footer = new JLabel();

            int x = 0;
            JButton[] extraButtons = this.getExtraButtons();
            if (extraButtons != null) {
                for (JButton button : extraButtons) {
                    footerPane.add(
                            button,
                            LayoutHelper.getGBC(
                                    x++,
                                    0,
                                    1,
                                    0.0,
                                    0.0,
                                    GridBagConstraints.NONE,
                                    GridBagConstraints.WEST,
                                    new Insets(2, 2, 2, 2)));
                }
            }

            footerPane.add(footer, LayoutHelper.getGBC(x++, 0, 1, 1.0, new Insets(2, 2, 2, 2)));
            footerPane.add(
                    getBtnCancel(),
                    LayoutHelper.getGBC(
                            x++,
                            0,
                            1,
                            0.0,
                            0.0,
                            GridBagConstraints.NONE,
                            GridBagConstraints.EAST,
                            new Insets(2, 2, 2, 2)));
            footerPane.add(
                    getBtnOK(),
                    LayoutHelper.getGBC(
                            x++,
                            0,
                            1,
                            0.0,
                            0.0,
                            GridBagConstraints.NONE,
                            GridBagConstraints.EAST,
                            new Insets(2, 2, 2, 2)));

            jContentPane.add(footerPane, LayoutHelper.getGBC(0, 1, 1, 1.0, 0.0));
        }

        return jContentPane;
    }

    /**
     * Sets the text to be shown in the footer of the dialogue (along with the OK and Cancel
     * buttons).
     *
     * @param text the text to be shown in the footer, might be {@code null} in which case no text
     *     is shown.
     */
    public void setFooter(String text) {
        footer.setText(text);
    }

    public JButton[] getExtraButtons() {
        return null;
    }

    /**
     * This method initializes btnOK
     *
     * @return javax.swing.JButton
     */
    private JButton getBtnOK() {
        if (btnOK == null) {
            btnOK = new JButton();
            btnOK.setName("btnOK");
            btnOK.setText(Constant.messages.getString("all.button.ok"));
            btnOK.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {

                            try {
                                validateParam();
                                saveParam();
                                exitResult = JOptionPane.OK_OPTION;

                                AbstractParamDialog.this.setVisible(false);

                            } catch (Exception ex) {
                                LOGGER.debug("Failed to validate or save the panels: ", ex);
                                View.getSingleton()
                                        .showWarningDialog(
                                                AbstractParamDialog.this,
                                                Constant.messages.getString(
                                                        "options.dialog.save.error",
                                                        ex.getMessage()));
                            }
                        }
                    });
        }
        return btnOK;
    }

    /**
     * This method initializes btnCancel
     *
     * @return javax.swing.JButton
     */
    protected JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton();
            btnCancel.setName("btnCancel");
            btnCancel.setText(Constant.messages.getString("all.button.cancel"));
            btnCancel.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {

                            exitResult = JOptionPane.CANCEL_OPTION;
                            AbstractParamDialog.this.setVisible(false);
                        }
                    });
        }
        return btnCancel;
    }

    /**
     * This method initializes jSplitPane
     *
     * @return javax.swing.JSplitPane
     */
    private AbstractParamContainerPanel getJSplitPane() {
        if (jSplitPane == null) {
            jSplitPane = new AbstractParamContainerPanel();
            jSplitPane.setVisible(true);
            if (this.rootName != null) {
                jSplitPane.getRootNode().setUserObject(rootName);
            }
        }

        return jSplitPane;
    }

    /**
     * Adds the given panel with the given name positioned under the given parents (or root node if
     * none given).
     *
     * <p>If not sorted the panel is appended to existing panels.
     *
     * @param parentParams the name of the parent nodes of the panel, might be {@code null}.
     * @param name the name of the panel, must not be {@code null}.
     * @param panel the panel, must not be {@code null}.
     * @param sort {@code true} if the panel should be added in alphabetic order, {@code false}
     *     otherwise
     */
    public void addParamPanel(
            String[] parentParams, String name, AbstractParamPanel panel, boolean sort) {
        this.getJSplitPane().addParamPanel(parentParams, name, panel, sort);
    }

    /**
     * Adds the given panel, with its {@link Component#getName() own name}, positioned under the
     * given parents (or root node if none given).
     *
     * <p>If not sorted the panel is appended to existing panels.
     *
     * @param parentParams the name of the parent nodes of the panel, might be {@code null}.
     * @param panel the panel, must not be {@code null}.
     * @param sort {@code true} if the panel should be added in alphabetic order, {@code false}
     *     otherwise
     */
    public void addParamPanel(String[] parentParams, AbstractParamPanel panel, boolean sort) {
        addParamPanel(parentParams, panel.getName(), panel, sort);
    }

    /**
     * Removes the given panel.
     *
     * @param panel the panel that will be removed
     */
    public void removeParamPanel(AbstractParamPanel panel) {
        this.getJSplitPane().removeParamPanel(panel);
    }

    // ZAP: Made public so that other classes can specify which panel is displayed
    public void showParamPanel(String parent, String child) {
        this.getJSplitPane().showParamPanel(parent, child);
    }

    /**
     * Shows the panel with the given name.
     *
     * <p>Nothing happens if there's no panel with the given name (or the given name is empty or
     * {@code null}).
     *
     * <p>The previously shown panel (if any) is notified that it will be hidden.
     *
     * @param name the name of the panel to be shown
     */
    public void showParamPanel(String name) {
        this.getJSplitPane().showParamPanel(name);
    }

    /**
     * Shows the panel with the given name.
     *
     * <p>The previously shown panel (if any) is notified that it will be hidden.
     *
     * @param panel the panel that will be notified that is now shown, must not be {@code null}.
     * @param name the name of the panel that will be shown, must not be {@code null}.
     */
    public void showParamPanel(AbstractParamPanel panel, String name) {
        this.getJSplitPane().showParamPanel(panel, name);
    }

    /**
     * Initialises all panels with the given object.
     *
     * @param obj the object that contains the data to be shown in the panels and save them
     * @see #validateParam()
     * @see #saveParam()
     */
    public void initParam(Object obj) {
        this.getJSplitPane().initParam(obj);
    }

    /**
     * Validates all panels, throwing an exception if there's any validation error.
     *
     * <p>The message of the exception can be shown in GUI components (for example, an error
     * dialogue) callers can expect an internationalised message.
     *
     * @throws Exception if there's any validation error.
     * @see #initParam(Object)
     * @see #saveParam()
     */
    public void validateParam() throws Exception {
        this.getJSplitPane().validateParam();
    }

    /**
     * Saves the data of all panels, throwing an exception if there's any error.
     *
     * <p>The message of the exception can be shown in GUI components (for example, an error
     * dialogue) callers can expect an internationalised message.
     *
     * @throws Exception if there's any error while saving the data.
     * @see #initParam(Object)
     * @see #validateParam()
     */
    public void saveParam() throws Exception {
        this.getJSplitPane().saveParam();
    }

    /**
     * Expands the root node.
     *
     * @see #expandParamPanelNode(String)
     */
    protected void expandRoot() {
        this.getJSplitPane().expandRoot();
    }

    /**
     * Expands the node of the param panel with the given name.
     *
     * @param panelName the name of the panel whose node should be expanded, should not be {@code
     *     null}.
     * @since 2.8.0
     * @see #expandRoot()
     */
    protected void expandParamPanelNode(String panelName) {
        getJSplitPane().expandParamPanelNode(panelName);
    }

    /**
     * Tells whether or not the given param panel is selected.
     *
     * @param panelName the name of the panel to check if it is selected, should not be {@code
     *     null}.
     * @return {@code true} if the panel is selected, {@code false} otherwise.
     * @since 2.8.0
     * @see #isParamPanelOrChildSelected(String)
     */
    protected boolean isParamPanelSelected(String panelName) {
        return getJSplitPane().isParamPanelSelected(panelName);
    }

    /**
     * Tells whether or not the given param panel, or one of its child panels, is selected.
     *
     * @param panelName the name of the panel to check, should not be {@code null}.
     * @return {@code true} if the panel or one of its child panels is selected, {@code false}
     *     otherwise.
     * @since 2.8.0
     * @see #isParamPanelSelected(String)
     */
    protected boolean isParamPanelOrChildSelected(String panelName) {
        return getJSplitPane().isParamPanelOrChildSelected(panelName);
    }

    public int showDialog(boolean showRoot) {
        return showDialog(showRoot, null);
    }

    // ZAP: Added option to specify panel - note this only supports one level at the moment
    // ZAP: show the last selected panel
    public int showDialog(boolean showRoot, String panel) {
        this.getJSplitPane().showDialog(showRoot, panel);
        this.setVisible(true);
        return this.exitResult;
    }

    // ZAP: Added accessor to the panels
    /**
     * Gets the panels shown on this dialog.
     *
     * @return the panels
     */
    protected Collection<AbstractParamPanel> getPanels() {
        return this.getJSplitPane().getPanels();
    }

    // Useful method for debugging panel issues ;)
    public void printTree() {
        this.getJSplitPane().printTree();
    }

    public void renamePanel(AbstractParamPanel panel, String newPanelName) {
        this.getJSplitPane().renamePanel(panel, newPanelName);
    }
} //  @jve:decl-index=0:visual-constraint="73,11"
