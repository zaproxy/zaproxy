package org.zaproxy.zap.extension.httppanel.plugin.request.all;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.plugin.PluginInterface;
import org.zaproxy.zap.extension.httppanel.view.hex.HttpPanelHexView;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextView;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextArea.MessageType;
import org.zaproxy.zap.extension.search.SearchMatch;

public class RequestAllView implements PluginInterface, ActionListener {

	private JButton buttonShowView;
	private JComboBox comboxSelectView;
	
	private JPanel panelOptions;
	private JPanel panelMain;
	
	private HttpPanel httpPanel;
	private HttpMessage httpMessage;
	
	private HttpPanelTextView textView;
	private HttpPanelHexView hexView;
//	private HttpPanelTabularView httpPanelTableView;
	
	private RequestAllModelText modelText;
	private RequestAllModelText modelHex;
	
	private HttpPanelView currentView;
	private Hashtable<String, HttpPanelView> views = new Hashtable<String, HttpPanelView>();
	
    private static Log log = LogFactory.getLog(ManualRequestEditorDialog.class);
	
	public RequestAllView(HttpPanel httpPanel, HttpMessage httpMessage) {
		this.httpPanel = httpPanel;
		this.httpMessage = httpMessage;
		initModel();
		initUi();
		switchView(textView.getName());
	}
	
	private void initModel() {
		modelText = new RequestAllModelText(httpMessage);
		modelHex = new RequestAllModelText(httpMessage);
	}
	
	private void initUi() {
		// Common
		buttonShowView = new JButton(Constant.messages.getString("request.panel.button.all"));

		// Main Panel
		panelMain = new JPanel();
		panelMain.setLayout(new CardLayout());
		
		// Plugins
		textView = new HttpPanelTextView(modelText, MessageType.Full, httpPanel.isEditable());
//		httpPanelTableView = new HttpPanelTabularView();
		hexView = new HttpPanelHexView(modelText, MessageType.Full, httpPanel.isEditable());
		
		views.put(textView.getName(), textView);
//		views.put(httpPanelTableView.getName(), httpPanelTableView);
		views.put(hexView.getName(), hexView);
		
		panelMain.add(textView.getPane(), textView.getName());
//		panelMain.add(httpPanelTableView.getPane(), httpPanelTableView.getName());
		panelMain.add(hexView.getPane(), hexView.getName());
		
		// Combobox
		comboxSelectView = new JComboBox();
		comboxSelectView.addItem(textView.getName());
//		comboxSelectView.addItem(httpPanelTableView.getName());
		comboxSelectView.addItem(hexView.getName());
		comboxSelectView.addActionListener(this);
		
		panelOptions = new JPanel();
		panelOptions.add(comboxSelectView);
		
		httpPanel.addHttpDataView(this);
	}
	
	private void switchView(String name) {
		this.currentView = views.get(name);		
        CardLayout card = (CardLayout) panelMain.getLayout();
        card.show(panelMain, name);	
	}

	@Override
	public String getName() {
		return "All";
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
		
		currentView.load();		
	}

	@Override
	public void save() {
		if (httpMessage == null) {
			return;
		}
		
		currentView.save();
	}

	@Override
	public void setHttpMessage(HttpMessage httpMessage) {
		this.httpMessage = httpMessage;

		modelText.setHttpMessage(httpMessage);

		
		this.textView.setHttpMessage(httpMessage);
	}

	@Override
	public void clearView(boolean enableViewSelect) {
		// TODO Auto-generated method stub
		
	}

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
	
	
	//// Not implemented
	
	@Override
	public void searchHeader(Pattern p, List<SearchMatch> matches) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void searchBody(Pattern p, List<SearchMatch> matches) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void highlightHeader(SearchMatch sm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void highlightBody(SearchMatch sm) {
		// TODO Auto-generated method stub
		
	}

}
