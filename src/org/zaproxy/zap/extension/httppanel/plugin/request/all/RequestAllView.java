package org.zaproxy.zap.extension.httppanel.plugin.request.all;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.plugin.BasicPlugin;
import org.zaproxy.zap.extension.httppanel.view.hex.HttpPanelHexView;
import org.zaproxy.zap.extension.httppanel.view.paramtable.RequestAllTableModel;
import org.zaproxy.zap.extension.httppanel.view.paramtable.RequestAllTableView;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextModelInterface;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextView;
import org.zaproxy.zap.extension.search.SearchMatch;

public class RequestAllView extends BasicPlugin {
	
	// Plugins
	protected HttpPanelTextView textView;
	protected HttpPanelHexView hexView;
	protected RequestAllTableView tableView;
	
	// Models
	protected HttpPanelTextModelInterface modelText;
	protected HttpPanelTextModelInterface modelHex;
	protected RequestAllTableModel modelTable;
	

	public RequestAllView(HttpPanel httpPanel, HttpMessage httpMessage) {
		super(httpPanel, httpMessage);
		initUi();
		switchView(null);
	}
	
	protected void initUi() {
		// Common
		buttonShowView = new JButton(Constant.messages.getString("request.panel.button.all"));

		// Main Panel
		panelMain = new JPanel();
		panelMainSwitchable = new JPanel();
		panelMainSwitchable.setLayout(new CardLayout());
		
		initPlugins();

		panelOptions = new JPanel();
		panelOptions.add(comboxSelectView);

		// All
		panelMain = new JPanel(new BorderLayout());
		panelMain.add(panelMainSwitchable);
		
		httpPanel.addHttpDataView(this);
	}
	
	
	protected void initModel() {
		modelText = new RequestAllModelText(httpMessage);
		modelHex = new RequestAllModelText(httpMessage);
		modelTable = new RequestAllTableModel(httpMessage, httpPanel.isEditable());
	}
	
	protected void initPlugins() {
		// Plugins - View
		textView = new HttpRequestAllPanelTextView(modelText, httpPanel.isEditable());
		//textView.setDocument(new HighlightedDocument(SyntaxType.HTTPREQ_2PHASE));
		//textView.setStyleKey("text/httphtml");
		tableView = new RequestAllTableView(modelTable, httpPanel.isEditable());
		hexView = new HttpPanelHexView(modelText, httpPanel.isEditable());
		
		views.put(textView.getName(), textView);
		views.put(tableView.getName(), tableView);
		views.put(hexView.getName(), hexView);
		
		panelMainSwitchable.add(textView.getPane(), textView.getName());
		panelMainSwitchable.add(tableView.getPane(), tableView.getName());
		panelMainSwitchable.add(hexView.getPane(), hexView.getName());

		// Combobox
		comboxSelectView = new JComboBox();
		comboxSelectView.addItem(textView.getName());
		comboxSelectView.addItem(tableView.getName());
		comboxSelectView.addItem(hexView.getName());
		comboxSelectView.addActionListener(this);
	}

	public String getName() {
		return "All";
	}

	public void setHttpMessage(HttpMessage httpMessage) {
		this.httpMessage = httpMessage;
		
		modelText.setHttpMessage(httpMessage);
		modelTable.setHttpMessage(httpMessage);
		
		// This is not nice, but needed for fuzzing
		// ExtensionAntiCSRF gets HttpMessage from HttpPanelTextView...
		textView.setHttpMessage(httpMessage);
		//tableView.set
	}
	
	@Override
	public void load() {
		if (httpMessage == null) {
			return;
		}
		currentView.load();
	}

	@Override
	public void save() {
		if (httpMessage == null) {
			return;
		}
		// currentView.getModel().save();
		currentView.save();
	}

	@Override
	public void searchHeader(Pattern p, List<SearchMatch> matches) {
//		Matcher m;
//		
//		m = p.matcher(modelText.getData());
//		while (m.find()) {
//			matches.add(
//					new SearchMatch(SearchMatch.Location.REQUEST_HEAD,	m.start(), m.end()));
//		}
	}

	@Override
	public void searchBody(Pattern p, List<SearchMatch> matches) {
//		Matcher m;
//
//		m = p.matcher(modelText.getData());
//		while (m.find()) {
//			matches.add(
//					new SearchMatch(SearchMatch.Location.REQUEST_HEAD,	m.start(), m.end()));
//		}
	}

	@Override
	public void highlightHeader(SearchMatch sm) {
		textView.highlight(sm);
	}

	@Override
	public void highlightBody(SearchMatch sm) {
		textView.highlight(sm);
	}

	@Override
	protected boolean isRequest() {
		return true;
	}

}
