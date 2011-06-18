package org.zaproxy.zap.extension.httppanel.plugin;

import java.awt.Component;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.search.SearchMatch;

public interface PluginInterface {
	// Name of the plugin for internal reference.
	public String getName();
	
	// Plugin has to provide the button which is displayed in the HttpPanel to select this view
	public JButton getButton();
	
	// Plugin needs to provide a anel with main content which is displayed in HttpPanel
	public JPanel getMainPanel();
	
	// Plugin can provide an additional panel which is displayed in the HttpPanel header when this view is selected
	public JPanel getOptionsPanel();
	
	// Set a new HttpMessage for this Plugin
	// For example, the user selects a new message in the history tab. 
	// The plugin should update it's models accordingly.
	public void setHttpMessage(HttpMessage httpMessage);
	
	// The plugin is requested to load data from HttpMessage into the current UI.
	// For example, the user selects a new message in the history tab. 
	public void load();
	
	// The plugin is requested to save data from the UI into the current HttpMessage.
	// For example, the user selects a new message in the history tab. Or in break mode, want to send the modified message. 
	public void save();
	
	
	// Optional
	public void clearView(boolean enableViewSelect);
	
	public void searchHeader(Pattern p, List<SearchMatch> matches);
	public void searchBody(Pattern p, List<SearchMatch> matches);
	
	public void highlightHeader(SearchMatch sm);
	public void highlightBody(SearchMatch sm);
}
