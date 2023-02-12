/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.zaproxy.zap.extension.search;

import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.TableExportButton;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.ZapToggleButton;

@SuppressWarnings("serial")
public class SearchPanel extends AbstractPanel implements SearchListenner {

    private static final long serialVersionUID = 1L;

    /**
     * The name of the search results HTTP messages container.
     *
     * @see org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer
     */
    public static final String HTTP_MESSAGE_CONTAINER_NAME = "SearchHttpMessageContainer";

    private ExtensionSearch extension;

    private javax.swing.JPanel panelCommand = null;
    private javax.swing.JToolBar panelToolbar = null;
    private JScrollPane jScrollPane = null;

    private ZapToggleButton scopeButton = null;
    private ZapTextField regEx = null;
    private JButton btnSearch = null;
    private JComboBox<SearchOption> searchType = null;
    private JButton btnNext = null;
    private JButton btnPrev = null;
    private JCheckBox chkInverse = null;
    private JLabel numberOfMatchesLabel;
    private JLabel searchStatusLabel;
    private JButton optionsButton;
    private TableExportButton<SearchResultsTable> exportButton = null;

    private MessageFormat numberOfMatchesFormat;

    private SearchResultsTable resultsTable;
    private SearchResultsTableModel resultsModel;

    private final ViewDelegate view;

    public SearchPanel(ViewDelegate view) {
        super();
        this.view = view;
        initialize();
    }

    public ExtensionSearch getExtension() {
        return extension;
    }

    public void setExtension(ExtensionSearch extension) {
        this.extension = extension;
    }

    /** This method initializes this */
    private void initialize() {
        resultsModel = new SearchResultsTableModel();
        resultsTable = new SearchResultsTable(resultsModel);
        resultsTable.setName(HTTP_MESSAGE_CONTAINER_NAME);

        resultsTable
                .getSelectionModel()
                .addListSelectionListener(
                        new ListSelectionListener() {

                            @Override
                            public void valueChanged(ListSelectionEvent evt) {
                                if (!evt.getValueIsAdjusting()) {
                                    SearchResult searchResult =
                                            resultsTable.getSelectedSearchResult();
                                    if (searchResult == null) {
                                        return;
                                    }

                                    displayMessage(resultsTable.getSelectedSearchResult());

                                    // Get the focus back so that the arrow keys work
                                    resultsTable.requestFocusInWindow();
                                }
                            }
                        });

        this.setLayout(new CardLayout());
        // this.setSize(474, 251);
        this.setName(Constant.messages.getString("search.panel.title"));
        this.setIcon(
                new ImageIcon(
                        SearchPanel.class.getResource(
                                "/resource/icon/16/049.png"))); // 'magnifying glass' icon
        this.add(getPanelCommand(), getPanelCommand().getName());
        this.setShowByDefault(true);
    }

    /**
     * This method initializes panelCommand
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getPanelCommand() {
        if (panelCommand == null) {

            panelCommand = new javax.swing.JPanel();
            panelCommand.setLayout(new java.awt.GridBagLayout());
            panelCommand.setName("Search Panel");

            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.weightx = 1.0D;
            gridBagConstraints1.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;

            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.gridy = 1;
            gridBagConstraints2.weightx = 1.0;
            gridBagConstraints2.weighty = 1.0;
            gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 0);
            gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;

            panelCommand.add(this.getPanelToolbar(), gridBagConstraints1);
            panelCommand.add(getJScrollPane(), gridBagConstraints2);
        }
        return panelCommand;
    }

    private GridBagConstraints newGBC(int gridx) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = gridx;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        return gridBagConstraints;
    }

    private javax.swing.JToolBar getPanelToolbar() {
        if (panelToolbar == null) {

            panelToolbar = new javax.swing.JToolBar();
            panelToolbar.setLayout(new java.awt.GridBagLayout());
            panelToolbar.setEnabled(true);
            panelToolbar.setFloatable(false);
            panelToolbar.setRollover(true);
            panelToolbar.setPreferredSize(new java.awt.Dimension(800, 30));
            panelToolbar.setName("Search Toolbar");

            GridBagConstraints gridBagConstraintsX = new GridBagConstraints();
            gridBagConstraintsX.gridx = 20;
            gridBagConstraintsX.gridy = 0;
            gridBagConstraintsX.weightx = 1.0;
            gridBagConstraintsX.weighty = 1.0;
            gridBagConstraintsX.insets = new java.awt.Insets(0, 0, 0, 0);
            gridBagConstraintsX.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraintsX.fill = java.awt.GridBagConstraints.HORIZONTAL;

            GridBagConstraints optionsGridBag = new GridBagConstraints();
            optionsGridBag.gridx = gridBagConstraintsX.gridx + 1;
            optionsGridBag.gridy = 0;
            optionsGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
            optionsGridBag.anchor = java.awt.GridBagConstraints.EAST;

            JLabel t1 = new JLabel();
            JLabel inverseTooltip =
                    new JLabel(Constant.messages.getString("search.toolbar.label.inverse"));
            inverseTooltip.setToolTipText(
                    Constant.messages.getString("search.toolbar.tooltip.inverse"));

            panelToolbar.add(getScopeButton(), newGBC(0));
            panelToolbar.add(getRegExField(), newGBC(1));
            panelToolbar.add(getSearchType(), newGBC(2));
            panelToolbar.add(inverseTooltip, newGBC(3));
            panelToolbar.add(getChkInverse(), newGBC(4));
            panelToolbar.add(getBtnSearch(), newGBC(5));
            panelToolbar.add(getBtnNext(), newGBC(6));
            panelToolbar.add(getBtnPrev(), newGBC(7));
            panelToolbar.add(new JToolBar.Separator(), newGBC(8));
            panelToolbar.add(getNumberOfMatchesLabel(), newGBC(9));
            panelToolbar.add(new JToolBar.Separator(), newGBC(10));
            panelToolbar.add(getSearchStatusLabel(), newGBC(11));
            panelToolbar.add(new JToolBar.Separator(), newGBC(12));
            panelToolbar.add(t1, gridBagConstraintsX);
            panelToolbar.add(getExportButton(), newGBC(13));
            panelToolbar.add(getOptionsButton(), optionsGridBag);
        }
        return panelToolbar;
    }

    private JToggleButton getScopeButton() {
        if (scopeButton == null) {
            scopeButton = new ZapToggleButton();
            scopeButton.setIcon(
                    new ImageIcon(
                            SearchPanel.class.getResource("/resource/icon/fugue/target-grey.png")));
            scopeButton.setToolTipText(
                    Constant.messages.getString("search.toolbar.tooltip.scope.unselected"));
            scopeButton.setSelectedIcon(
                    new ImageIcon(
                            SearchPanel.class.getResource("/resource/icon/fugue/target.png")));
            scopeButton.setSelectedToolTipText(
                    Constant.messages.getString("search.toolbar.tooltip.scope.selected"));
            DisplayUtils.scaleIcon(scopeButton);

            scopeButton.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            extension.setSearchJustInScope(scopeButton.isSelected());
                        }
                    });
        }
        return scopeButton;
    }

    private JCheckBox getChkInverse() {
        if (chkInverse == null) {
            chkInverse = new JCheckBox();
            chkInverse.setToolTipText(
                    Constant.messages.getString("search.toolbar.tooltip.inverse"));
        }
        return chkInverse;
    }

    private JButton getBtnSearch() {
        if (btnSearch == null) {
            btnSearch = new JButton();
            btnSearch.setText(Constant.messages.getString("search.toolbar.label.search"));
            btnSearch.setIcon(
                    new ImageIcon(
                            SearchPanel.class.getResource(
                                    "/resource/icon/16/049.png"))); // 'magnifying glass' icon
            btnSearch.setToolTipText(Constant.messages.getString("search.toolbar.tooltip.search"));
            DisplayUtils.scaleIcon(btnSearch);

            btnSearch.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            doSearch();
                        }
                    });
        }
        return btnSearch;
    }

    private JButton getBtnNext() {
        if (btnNext == null) {
            btnNext = new JButton();
            btnNext.setText(Constant.messages.getString("search.toolbar.label.next"));
            btnNext.setIcon(
                    new ImageIcon(
                            SearchPanel.class.getResource(
                                    "/resource/icon/16/107.png"))); // 'arrow down' icon
            btnNext.setToolTipText(Constant.messages.getString("search.toolbar.tooltip.next"));
            DisplayUtils.scaleIcon(btnNext);

            btnNext.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            highlightNextResult();
                        }
                    });
        }
        return btnNext;
    }

    private JButton getBtnPrev() {
        if (btnPrev == null) {
            btnPrev = new JButton();
            btnPrev.setText(Constant.messages.getString("search.toolbar.label.previous"));
            btnPrev.setIcon(
                    new ImageIcon(
                            SearchPanel.class.getResource(
                                    "/resource/icon/16/108.png"))); // 'arrow up' icon
            btnPrev.setToolTipText(Constant.messages.getString("search.toolbar.tooltip.previous"));
            DisplayUtils.scaleIcon(btnPrev);

            btnPrev.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            highlightPrevResult();
                        }
                    });
        }
        return btnPrev;
    }

    protected ZapTextField getRegExField() {
        if (regEx == null) {
            regEx = new ZapTextField();
            regEx.setHorizontalAlignment(ZapTextField.LEFT);
            regEx.setAlignmentX(0.0F);
            regEx.setPreferredSize(DisplayUtils.getScaledDimension(250, 25));
            regEx.setText("");
            regEx.setToolTipText(Constant.messages.getString("search.toolbar.tooltip.regex"));
            regEx.setMinimumSize(DisplayUtils.getScaledDimension(250, 25));

            regEx.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            doSearch();
                        }
                    });
        }
        return regEx;
    }

    private JLabel getNumberOfMatchesLabel() {
        if (numberOfMatchesLabel == null) {
            numberOfMatchesLabel = new JLabel();
            numberOfMatchesFormat =
                    new MessageFormat(
                            Constant.messages.getString("search.toolbar.label.number.of.matches"));
            setNumberOfMatches(0);
        }
        return numberOfMatchesLabel;
    }

    private void setNumberOfMatches(int numberOfMatches) {
        numberOfMatchesLabel.setText(numberOfMatchesFormat.format(new Object[] {numberOfMatches}));
    }

    private TableExportButton<SearchResultsTable> getExportButton() {
        if (exportButton == null) {
            exportButton = new TableExportButton<>(resultsTable);
        }
        return exportButton;
    }

    private JButton getOptionsButton() {
        if (optionsButton == null) {
            optionsButton = new JButton();
            optionsButton.setToolTipText(
                    Constant.messages.getString("search.toolbar.button.options"));
            optionsButton.setIcon(
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    SearchPanel.class.getResource("/resource/icon/16/041.png"))));
            optionsButton.addActionListener(
                    new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Control.getSingleton()
                                    .getMenuToolsControl()
                                    .options(
                                            Constant.messages.getString(
                                                    "search.optionspanel.name"));
                        }
                    });
        }
        return optionsButton;
    }

    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setHorizontalScrollBarPolicy(
                    javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jScrollPane.setViewportView(resultsTable);
        }
        return jScrollPane;
    }

    public void resetSearchResults() {
        setNumberOfMatches(0);
        resultsModel.clear();
        getSearchStatusLabel().setText("");
        getBtnSearch().setEnabled(true);
    }

    @Override
    public void addSearchResult(final SearchResult str) {
        if (EventQueue.isDispatchThread()) {
            resultsModel.addSearchResult(str);

            setNumberOfMatches(resultsModel.getRowCount());
            if (resultsModel.getRowCount() == 1) {
                highlightFirstResult();
            }
        } else {
            EventQueue.invokeLater(
                    new Runnable() {
                        @Override
                        public void run() {
                            addSearchResult(str);
                        }
                    });
        }
    }

    private void doSearch() {
        Pattern pattern;
        try {
            pattern = Pattern.compile(regEx.getText());
        } catch (IllegalArgumentException e) {
            regEx.requestFocusInWindow();
            View.getSingleton()
                    .showWarningDialog(
                            Constant.messages.getString("search.toolbar.error.invalid.regex"));
            return;
        }

        if (pattern.matcher("").find()) {
            int option =
                    JOptionPane.showOptionDialog(
                            View.getSingleton().getMainFrame(),
                            Constant.messages.getString(
                                    "search.toolbar.warn.regex.match.empty.string.text"),
                            Constant.messages.getString(
                                    "search.toolbar.warn.regex.match.empty.string.title"),
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[] {
                                Constant.messages.getString(
                                        "search.toolbar.warn.regex.match.empty.string.button.search"),
                                Constant.messages.getString(
                                        "search.toolbar.warn.regex.match.empty.string.button.cancel")
                            },
                            null);
            if (option != JOptionPane.OK_OPTION) {
                regEx.requestFocusInWindow();
                return;
            }
        }

        SearchOption option = (SearchOption) getSearchType().getSelectedItem();

        ExtensionSearch.Type type = option.getType();
        String customSearcherName = null;

        if (ExtensionSearch.Type.Custom == type) {
            customSearcherName = option.getName();
        }

        setNumberOfMatches(0);
        extension.search(regEx.getText(), type, customSearcherName, false, chkInverse.isSelected());

        // Select first result
        if (resultsTable.getModel().getRowCount() > 0) {
            resultsTable.getSelectionModel().setSelectionInterval(0, 0);
            resultsTable.requestFocus();
        }
    }

    protected void setSearchType(ExtensionSearch.Type type) {
        if (ExtensionSearch.Type.Custom == type) {
            // Not supported
            return;
        }

        for (int i = 0; i < getSearchType().getItemCount(); i++) {
            SearchOption option = getSearchType().getItemAt(i);
            if (option.getType() == type) {
                getSearchType().setSelectedIndex(i);
                break;
            }
        }
    }

    private void displayMessage(SearchResult sr) {
        HttpMessage msg = sr.getMessage();
        view.displayMessage(msg);

        if (msg == null) {
            return;
        }

        highlightFirstResult(sr);
    }

    private void highlightMatch(SearchMatch sm) {
        if (sm == null) {
            return;
        }

        switch (sm.getLocation()) {
            case REQUEST_HEAD:
                view.getRequestPanel().highlightHeader(sm);
                view.getRequestPanel().setTabFocus();
                view.getRequestPanel().requestFocus();
                break;
            case REQUEST_BODY:
                view.getRequestPanel().highlightBody(sm);
                view.getRequestPanel().setTabFocus();
                view.getRequestPanel().requestFocus();
                break;
            case RESPONSE_HEAD:
                view.getResponsePanel().highlightHeader(sm);
                view.getResponsePanel().setTabFocus();
                view.getResponsePanel().requestFocus();
                break;
            case RESPONSE_BODY:
                view.getResponsePanel().highlightBody(sm);
                view.getResponsePanel().setTabFocus();
                view.getResponsePanel().requestFocus();
                break;
        }
    }

    private void highlightFirstResult(SearchResult sr) {
        highlightMatch(sr.getFirstMatch(view.getRequestPanel(), view.getResponsePanel()));
    }

    protected void highlightFirstResult() {
        if (resultsTable.getModel().getRowCount() > 0) {
            resultsTable.getSelectionModel().setSelectionInterval(0, 0);
            resultsTable.scrollRowToVisible(0);
            resultsTable.requestFocus();
        }
    }

    protected void highlightNextResult() {
        if (resultsTable.getSelectedSearchResult() == null) {
            this.highlightFirstResult();
            return;
        }

        SearchResult sr = resultsTable.getSelectedSearchResult();
        SearchMatch sm = sr.getNextMatch();

        if (sm != null) {
            highlightMatch(sm);
        } else {
            // Next record
            if (resultsTable.getSelectedRow() < resultsTable.getModel().getRowCount() - 1) {
                resultsTable
                        .getSelectionModel()
                        .setSelectionInterval(
                                resultsTable.getSelectedRow() + 1,
                                resultsTable.getSelectedRow() + 1);
                resultsTable.scrollRowToVisible(resultsTable.getSelectedRow());
            } else {
                this.highlightFirstResult();
            }
        }
    }

    private void highlightLastResult(SearchResult sr) {
        highlightMatch(sr.getLastMatch(view.getRequestPanel(), view.getResponsePanel()));
    }

    protected void highlightPrevResult() {
        if (resultsTable.getSelectedSearchResult() == null) {
            this.highlightFirstResult();
            return;
        }

        SearchResult sr = resultsTable.getSelectedSearchResult();
        SearchMatch sm = sr.getPrevMatch();

        if (sm != null) {
            highlightMatch(sm);
        } else {
            // Previous record
            if (resultsTable.getSelectedRow() > 0) {
                resultsTable
                        .getSelectionModel()
                        .setSelectionInterval(
                                resultsTable.getSelectedRow() - 1,
                                resultsTable.getSelectedRow() - 1);
            } else {
                resultsTable
                        .getSelectionModel()
                        .setSelectionInterval(
                                resultsTable.getModel().getRowCount() - 1,
                                resultsTable.getRowCount() - 1);
            }
            resultsTable.scrollRowToVisible(resultsTable.getSelectedRow());
            highlightLastResult(resultsTable.getSelectedSearchResult());
        }
    }

    private JComboBox<SearchOption> getSearchType() {
        if (searchType == null) {
            searchType = new JComboBox<>();
            searchType.addItem(
                    new SearchOption(
                            Constant.messages.getString("search.toolbar.label.type.all"),
                            ExtensionSearch.Type.All));
            searchType.addItem(
                    new SearchOption(
                            Constant.messages.getString("search.toolbar.label.type.url"),
                            ExtensionSearch.Type.URL));
            searchType.addItem(
                    new SearchOption(
                            Constant.messages.getString("search.toolbar.label.type.request"),
                            ExtensionSearch.Type.Request));
            searchType.addItem(
                    new SearchOption(
                            Constant.messages.getString("search.toolbar.label.type.response"),
                            ExtensionSearch.Type.Response));
            searchType.addItem(
                    new SearchOption(
                            Constant.messages.getString("search.toolbar.label.type.header"),
                            ExtensionSearch.Type.Header));
        }
        return searchType;
    }

    protected void addCustomSearcher(String name) {
        getSearchType().addItem(new SearchOption(name, ExtensionSearch.Type.Custom));
    }

    protected void removeCustomSearcher(String name) {
        for (int i = 0; i < getSearchType().getItemCount(); i++) {
            SearchOption option = getSearchType().getItemAt(i);
            if (option.getType() == ExtensionSearch.Type.Custom && name.equals(option.getName())) {
                getSearchType().removeItemAt(i);
                break;
            }
        }
    }

    public void searchFocus() {
        this.setTabFocus();
        getRegExField().requestFocus();
    }

    @Override
    public void searchStarted() {
        EventQueue.invokeLater(
                () -> {
                    getSearchStatusLabel()
                            .setText(
                                    Constant.messages.getString(
                                            "search.toolbar.label.status.searching"));
                    getBtnSearch().setEnabled(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                });
    }

    @Override
    public void searchComplete() {
        EventQueue.invokeLater(
                () -> {
                    getSearchStatusLabel()
                            .setText(
                                    Constant.messages.getString(
                                            "search.toolbar.label.status.complete"));
                    getBtnSearch().setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                });
    }

    private JLabel getSearchStatusLabel() {
        if (searchStatusLabel == null) {
            searchStatusLabel = new JLabel("");
        }
        return searchStatusLabel;
    }

    private static class SearchOption {

        private final String name;
        private final ExtensionSearch.Type type;

        public SearchOption(String name, ExtensionSearch.Type type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public ExtensionSearch.Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
