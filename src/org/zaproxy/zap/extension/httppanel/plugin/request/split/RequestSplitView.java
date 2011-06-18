package org.zaproxy.zap.extension.httppanel.plugin.request.split;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.plugin.PluginInterface;
import org.zaproxy.zap.extension.httppanel.view.hex.HttpPanelHexView;
import org.zaproxy.zap.extension.httppanel.view.table.HttpPanelTabularView;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextArea;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextModelInterface;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextView;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextArea.MessageType;
import org.zaproxy.zap.extension.search.SearchMatch;

public class RequestSplitView implements PluginInterface, ActionListener  {
	private HttpPanelTextView viewBodyText;
	private HttpPanelHexView viewBodyHex;
	private HttpPanelTabularView viewBodyTable;
	private HttpPanelTextView viewHeaderText;
	
	protected JPanel panelHeader;
	protected JPanel panelBody;
	
	protected JButton buttonShowView;
	protected JComboBox comboxSelectView;

	protected JSplitPane splitMain;
	protected JPanel panelOptions;
	protected JPanel panelMain;

	protected HttpPanel httpPanel;
	protected HttpMessage httpMessage;

	protected HttpPanelView currentView;
	protected Hashtable<String, HttpPanelView> views = new Hashtable<String, HttpPanelView>();
	
	protected static Log log = LogFactory.getLog(ManualRequestEditorDialog.class);

	protected HttpPanelTextModelInterface modelTextHeader;
	protected HttpPanelTextModelInterface modelTextBody;
	protected HttpPanelTextModelInterface modelHexBody;
	
	public RequestSplitView(HttpPanel httpPanel, HttpMessage httpMessage) {
		this.httpPanel = httpPanel;
		this.httpMessage = httpMessage;
		initModel();
		initUi();
		switchView(viewBodyText.getName());
	}
	
	protected void initModel() {
		modelTextHeader = new RequestSplitModelHeaderText(httpMessage);
		modelTextBody = new RequestSplitModelBodyText(httpMessage);
		modelHexBody = new RequestSplitModelBodyText(httpMessage);
	}
	

	protected void initUi() {
		// Common
		buttonShowView = new JButton("Split");
		panelOptions = new JPanel();
		comboxSelectView = new JComboBox();		

		// Main
		panelHeader = new JPanel();
		panelBody = new JPanel();
		panelBody.setLayout(new CardLayout());

		// Header
		viewHeaderText = new HttpPanelTextView(modelTextHeader, MessageType.Header, httpPanel.isEditable());
		panelHeader.setLayout(new BorderLayout());
		panelHeader.add(viewHeaderText.getPane(), BorderLayout.CENTER);

		// Body
		splitMain = new JSplitPane();
		splitMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
		panelHeader.setMinimumSize(new Dimension(100, 100));
		splitMain.setTopComponent(panelHeader);
		splitMain.setBottomComponent(panelBody);
		
		// Plugins
		viewBodyText = new HttpPanelTextView(modelTextBody, MessageType.Body, httpPanel.isEditable());
		viewBodyTable = new HttpPanelTabularView();
		viewBodyHex = new HttpPanelHexView(modelTextBody, MessageType.Body, httpPanel.isEditable());
		
		views.put(viewBodyText.getName(), viewBodyText);
		views.put(viewBodyTable.getName(), viewBodyTable);
		views.put(viewBodyHex.getName(), viewBodyHex);
		
		panelBody.add(viewBodyText.getPane(), viewBodyText.getName());
		panelBody.add(viewBodyTable.getPane(), viewBodyTable.getName());
		panelBody.add(viewBodyHex.getPane(), viewBodyHex.getName());
		
		// Combobox
		comboxSelectView.addItem(viewBodyText.getName());
		comboxSelectView.addItem(viewBodyTable.getName());
		comboxSelectView.addItem(viewBodyHex.getName());
		comboxSelectView.addActionListener(this);
		panelOptions.add(comboxSelectView);

		// All
		panelMain = new JPanel(new BorderLayout());
		panelMain.add(splitMain, BorderLayout.CENTER);

		httpPanel.addHttpDataView(this);
	}

	@Override
	public String getName() {
		return "Split";
	}

	@Override
	public JButton getButton() {
		return buttonShowView;
	}

	@Override
	public JPanel getOptionsPanel() {
		return panelOptions;
	}

	@Override
	public JPanel getMainPanel() {
		return panelMain;
	}

	@Override
	public void load() {
		if (httpMessage == null) {
			return;
		}

		// Update UI
		viewHeaderText.load();
		currentView.load();
	}

	@Override
	public void save() {
		if (httpMessage == null) {
			return;
		}
		
		viewHeaderText.save();
		currentView.save();
	}
	
	protected void switchView(String name) {
		this.currentView = views.get(name);		
        CardLayout card = (CardLayout) panelBody.getLayout();
        card.show(panelBody, name);	
	}

	@Override
	public void setHttpMessage(HttpMessage httpMessage) {
		this.httpMessage = httpMessage;

		modelTextHeader.setHttpMessage(httpMessage);

		modelTextBody.setHttpMessage(httpMessage);
///		modelTableBody.setHttpMessage(httpMessage);
		modelHexBody.setHttpMessage(httpMessage);
		
		// This is not nice, but needed for fuzzing
		// ExtensionAntiCSRF gets HttpMessage from HttpPanelTextView...
		viewBodyText.setHttpMessage(httpMessage);
		viewHeaderText.setHttpMessage(httpMessage);
	}

	@Override
	public void clearView(boolean enableViewSelect) {
		// TODO Auto-generated method stub
	}

	@Override
	public void searchHeader(Pattern p, List<SearchMatch> matches) {
		Matcher m;
		
		m = p.matcher(modelTextHeader.getData());
		while (m.find()) {
			matches.add(
					new SearchMatch(SearchMatch.Location.REQUEST_HEAD,	m.start(), m.end()));
		}
	}

	@Override
	public void searchBody(Pattern p, List<SearchMatch> matches) {
		Matcher m;

		m = p.matcher(modelTextBody.getData());
		while (m.find()) {
			matches.add(
					new SearchMatch(SearchMatch.Location.REQUEST_HEAD,	m.start(), m.end()));
		}
	}

	@Override
	public void highlightHeader(SearchMatch sm) {
		viewHeaderText.highlight(sm);
	}

	@Override
	public void highlightBody(SearchMatch sm) {
		viewBodyText.highlight(sm);
	}

	
	// Combobox action listener
	@Override
	public void actionPerformed(ActionEvent arg0) {
        String item = (String) comboxSelectView.getSelectedItem();
        if (item == null || item.equals(currentView.getName())) {
                return;
        }
        
        save();
        switchView(item);
        load();
	}

}
