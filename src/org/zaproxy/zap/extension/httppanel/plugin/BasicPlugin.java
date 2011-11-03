package org.zaproxy.zap.extension.httppanel.plugin;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelView;
import org.zaproxy.zap.extension.search.SearchMatch;

public abstract class BasicPlugin implements PluginInterface, ActionListener {
	protected JButton buttonShowView;
	protected JComboBox comboxSelectView;
	
	protected JPanel panelOptions;
	protected JPanel panelMainSwitchable;
	protected JPanel panelMain;
	
	protected HttpPanel httpPanel;
	protected HttpMessage httpMessage;

	protected HttpPanelView currentView;
	protected Hashtable<String, HttpPanelView> views = new Hashtable<String, HttpPanelView>();
	
	protected static Logger log = Logger.getLogger(ManualRequestEditorDialog.class);
    
	public BasicPlugin(HttpPanel httpPanel, HttpMessage httpMessage) {
		this.httpPanel = httpPanel;
		this.httpMessage = httpMessage;
		initModel();
	}
	
	protected abstract void initModel();
	protected abstract void initPlugins();
	protected abstract void initUi();
		
	protected void switchView(String name) {
		if (name == null) {
			String configView = Model.getSingleton().getOptionsParam().getViewParam().getPluginView(this.getName(), isRequest());
			System.out.println("Configview: " + configView);
			if (configView == null) {
				// Fallback
				this.currentView = views.get("Text");
				name = "Text";
			} else {
				// From config
				this.currentView = views.get(configView);
				name = configView;
				comboxSelectView.setSelectedItem(configView);
			}
		} else {
			// Last selected
			this.currentView = views.get(name);
			Model.getSingleton().getOptionsParam().getViewParam().setPluginView(this.getName(), isRequest(), name);
		}
		
		if (this.currentView == null) {
			log.error("Could not find plugin view");
			return;
		}
		
        CardLayout card = (CardLayout) panelMainSwitchable.getLayout();
        card.show(panelMainSwitchable, name);
	}

	abstract public String getName();

	abstract protected boolean isRequest();
	
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

	abstract public void load();
	abstract public void save();

	@Override
	public void clearView(boolean enableViewSelect) {
		// TODO Auto-generated method stub
		
	}

	@Override
	// Combobox event
	public void actionPerformed(ActionEvent arg0) {
        String item = (String) comboxSelectView.getSelectedItem();
        if (item == null || item.equals(currentView.getName())) {
                return;
        }
        
        save();
        switchView(item);
        load();
	}
	
	abstract public void searchHeader(Pattern p, List<SearchMatch> matches);
	abstract public void searchBody(Pattern p, List<SearchMatch> matches);
	abstract public void highlightHeader(SearchMatch sm);
	abstract public void highlightBody(SearchMatch sm);

	abstract public void setHttpMessage(HttpMessage httpMessage);
}
