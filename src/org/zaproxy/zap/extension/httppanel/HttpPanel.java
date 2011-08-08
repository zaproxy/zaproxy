package org.zaproxy.zap.extension.httppanel;


import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.plugin.PluginInterface;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.extension.tab.Tab;
import org.zaproxy.zap.view.HttpPanelManager;

/**
*
* Panel to display HTTP request/response with header and body.
* 
* Future: to support different view.
* 
* This creates:
* +---------------------+
* | panelHeader         |
* +---------------------+
* | contentSplit        |
* | ------------------- |
* |                     |
* +---------------------+
* 
* 
*/
abstract public class HttpPanel extends AbstractPanel implements Tab, ActionListener {
	private static final long serialVersionUID = 1L;
	protected static final String VIEW_RAW = Constant.messages.getString("http.panel.rawView");	// ZAP: i18n
	protected static final String VIEW_TABULAR = Constant.messages.getString("http.panel.tabularView");	// ZAP: i18n
	protected static final String VIEW_IMAGE = Constant.messages.getString("http.panel.imageView");	// ZAP: i18n

	protected JPanel panelHeader;
	protected JPanel panelStandard;
	protected JPanel panelSpecial = null;
	protected JPanel panelContent;

	private Extension extension = null;
	protected boolean editable = false;
	protected HttpMessage httpMessage;
	protected List<HttpPanelView> views = new ArrayList<HttpPanelView>();
	
	private static Logger log = Logger.getLogger(ManualRequestEditorDialog.class);

	private JPanel panelOptions;
	private JPanel panelButtons;
	
	private PluginInterface currentPlugin;

	private Hashtable<JButton, String> buttonViewLink = new Hashtable<JButton, String>();
	private Hashtable<String, PluginInterface> viewLink = new Hashtable<String, PluginInterface>();
	
	/*** Constructors ***/

	public HttpPanel(boolean isEditable, HttpMessage httpMessage) {
		this.editable = isEditable;
		this.httpMessage = httpMessage;
		
		initialize();
		HttpPanelManager.getInstance().addPanel(this);
		initUi();
		initSpecial();
	}

	public HttpPanel(boolean isEditable, Extension extension, HttpMessage httpMessage) {
		this.editable = isEditable;
		this.httpMessage = httpMessage;
		this.extension = extension;
		
		initialize();
		HttpPanelManager.getInstance().addPanel(this);
		initUi();
		initSpecial();
	}

	abstract protected void initSpecial();
	
	private  void initialize() {
		this.setLayout(new BorderLayout());
		
//		if (Model.getSingleton().getOptionsParam().getViewParam().getAdvancedViewOption() > 0) {
			this.add(getPanelHeader(), BorderLayout.NORTH);
//		}
		this.add(getPanelContent(), BorderLayout.CENTER);
	}
	
	private void initUi() {
		panelOptions = new JPanel();
		panelButtons = new JPanel();

		panelOptions.setLayout(new CardLayout());
		
		panelStandard.add(panelButtons);
		panelStandard.add(panelOptions);

		initPlugins();
		
		switchView("Split");
		loadData();
	}

	
	/**
	 * This method initializes the content panel
	 */    
	protected JPanel getPanelContent() {
		if (panelContent == null) {
			panelContent = new JPanel();
			panelContent.setLayout(new CardLayout());
		}
		
		return panelContent;
	}
	

	/**
	 * This method initializes the header, aka toolbar
	 */    
	protected JPanel getPanelHeader() {
		if (panelHeader == null) {
			panelHeader = new JPanel();
			panelStandard = new JPanel();
			panelSpecial = new JPanel();
			
			panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.LINE_AXIS));
			panelStandard.setLayout(new BoxLayout(panelStandard, BoxLayout.LINE_AXIS));
			panelSpecial.setLayout(new BoxLayout(panelSpecial, BoxLayout.LINE_AXIS));
			
			panelHeader.add(panelStandard);
			panelHeader.add(panelSpecial);
		}

		return panelHeader;
	}
	
	/* Set new HttpMessage
	 * Update UI accordingly.
	 */
	public void setMessage(HttpMessage msg) {
		if (msg == null) {
			   return;
		}
		
		this.httpMessage = msg;
		updateContent();
	}
	
	public void setMessage(HttpMessage msg, boolean enableViewSelect) {
		setMessage(msg);
	}

	/* Get Special Panel
	 * Return panel where one can add functionality to this panel
	 */
	public JPanel getPanelSpecial() {
		return panelSpecial;
	}

	/* Get HttpMessage
	 * External code needs to modify or view saved HttpMessage
	 * save data first so it's current
	 */
	public HttpMessage getHttpMessage() {
//		saveData();
		return httpMessage;
	}


	// Obsolete?
	public void setExtension(Extension extension) {
		this.extension = extension;
	}

	public Extension getExtension() {
		return extension;
	}

	public boolean isEditable() {
		return editable;
	}

	abstract protected void initPlugins();
	
		
	public void addHttpDataView(PluginInterface plugin) {
		String name = plugin.getName();
		
		viewLink.put(name, plugin);
		
		panelButtons.add(plugin.getButton());
		plugin.getButton().addActionListener(this);
		
		panelOptions.add(plugin.getOptionsPanel(), name);
		panelContent.add(plugin.getMainPanel(), name);
		buttonViewLink.put(plugin.getButton(), name);
	}
	
	// Button listener
	@Override
	public void actionPerformed(ActionEvent e) {
		saveData();
	
		String name = buttonViewLink.get(e.getSource());

		currentPlugin.getButton().setBackground(Color.LIGHT_GRAY);
		switchView(name);
		
		loadData();
	}
	
	// New HttpMessage was set
	public void updateContent() {
		currentPlugin.setHttpMessage(httpMessage);
		loadData();
	}
	
	private void loadData() {
		if (getHttpMessage() == null) {
			return;
		}
		
		currentPlugin.load();
	}
	
	public void saveData() {
		if (getHttpMessage() == null) {
			return;
		}
		
		currentPlugin.save();
	}
	
	private void switchView(String name) {
		this.currentPlugin = viewLink.get(name);
		
		currentPlugin.getButton().setBackground(Color.gray);
		
		this.currentPlugin.setHttpMessage(httpMessage);
		
		CardLayout cl = (CardLayout)(getPanelContent().getLayout());
		cl.show(panelContent, name);

		cl = (CardLayout)(panelOptions.getLayout());
		cl.show(panelOptions, name);
	}
	
	
	/*** Search Functions - for SearchPanel and SearchResult 
	 * We'll only use the Text card for finding and displaying search results. 
	 * highlight* and *Search belong together.
	 ***/

	public void highlightHeader(SearchMatch sm) {
		switchView("Split");
		currentPlugin.highlightHeader(sm);
	}

	public void highlightBody(SearchMatch sm) {
		switchView("Split");
		currentPlugin.highlightBody(sm);
	}

	public void headerSearch(Pattern p, List<SearchMatch> matches) {
		viewLink.get("Split").searchHeader(p, matches);
	}

	public void bodySearch(Pattern p, List<SearchMatch> matches) {
		viewLink.get("Split").searchBody(p, matches);	}

	public void addHeaderPanel(JPanel aPanel) {
		panelStandard.add(aPanel);
	}

	public void clearView(boolean enableViewSelect) {
		currentPlugin.clearView(enableViewSelect);
	}
	
}
