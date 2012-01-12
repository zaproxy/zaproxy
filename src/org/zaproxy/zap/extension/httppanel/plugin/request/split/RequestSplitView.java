package org.zaproxy.zap.extension.httppanel.plugin.request.split;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.plugin.BasicPlugin;
import org.zaproxy.zap.extension.httppanel.view.hex.HttpPanelHexView;
import org.zaproxy.zap.extension.httppanel.view.posttable.RequestPostTableView;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextModelInterface;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextView;
import org.zaproxy.zap.extension.search.SearchMatch;

public class RequestSplitView extends BasicPlugin  {
	
	private static final String PREF_DIVIDER_LOCATION = "divider.location";
	private static final String DIVIDER_HORIZONTAL = "horizontal";
	
	protected JPanel panelHeader;
	protected JSplitPane splitMain;

	protected static final Logger log = Logger.getLogger(RequestSplitView.class);

	private final Preferences preferences;
	private final String prefnzPrefix = this.getClass().getSimpleName()+".";
	
	
	// Plugins
	protected HttpPanelTextModelInterface modelTextHeader;
	protected HttpPanelTextModelInterface modelTextBody;
	protected HttpPanelTextModelInterface modelHexBody;
	protected HttpPanelTextModelInterface modelTableBody;
	
	protected HttpPanelTextView viewBodyText;
	protected HttpPanelHexView viewBodyHex;
	protected RequestPostTableView viewBodyTable;
	protected HttpPanelTextView viewHeaderText;
	
	public RequestSplitView(HttpPanel httpPanel, HttpMessage httpMessage) {
		super(httpPanel, httpMessage);
		this.preferences = Preferences.userNodeForPackage(getClass());
		initUi();
		switchView(null);
	}
	
	protected void initModel() {
		modelTextHeader = new RequestSplitModelHeaderText(httpMessage);
		modelTextBody = new RequestSplitModelBodyText(httpMessage);
		modelHexBody = new RequestSplitModelBodyText(httpMessage);
		modelTableBody = new RequestSplitModelBodyText(httpMessage);
	}

	protected void initUi() {
		// Common
		buttonShowView = new JButton(Constant.messages.getString("request.panel.button.split"));
		panelOptions = new JPanel();
		comboxSelectView = new JComboBox();		

		// Main
		panelHeader = new JPanel();
		panelMainSwitchable = new JPanel();
		panelMainSwitchable.setLayout(new CardLayout());

		// Header
		viewHeaderText = createHttpPanelTextView();
		panelHeader.setLayout(new BorderLayout());
		panelHeader.add(viewHeaderText.getPane(), BorderLayout.CENTER);

		// Body
		splitMain = new JSplitPane();
		splitMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
		panelHeader.setMinimumSize(new Dimension(100, 100));
		splitMain.setDividerLocation(restoreDividerLocation(DIVIDER_HORIZONTAL, 100));
		splitMain.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new DividerResizedListener(DIVIDER_HORIZONTAL));
		
		splitMain.setTopComponent(panelHeader);
		splitMain.setBottomComponent(panelMainSwitchable);
		
		// Plugins
		initPlugins();

		// All
		panelMain = new JPanel(new BorderLayout());
		panelMain.add(splitMain, BorderLayout.CENTER);

		httpPanel.addHttpDataView(this);
	}
	
	protected HttpPanelTextView createHttpPanelTextView() {
		return new HttpRequestHeaderPanelTextView(modelTextHeader, httpPanel.isEditable());
	}
	
	protected void initPlugins() {
		viewBodyText = new HttpRequestBodyPanelTextView(modelTextBody, httpPanel.isEditable());
		viewBodyTable = new RequestPostTableView(modelTextBody, httpPanel.isEditable());
		viewBodyHex = new HttpPanelHexView(modelTextBody, httpPanel.isEditable());
		
		views.put(viewBodyText.getName(), viewBodyText);
		views.put(viewBodyTable.getName(), viewBodyTable);
		views.put(viewBodyHex.getName(), viewBodyHex);
		
		panelMainSwitchable.add(viewBodyText.getPane(), viewBodyText.getName());
		panelMainSwitchable.add(viewBodyTable.getPane(), viewBodyTable.getName());
		panelMainSwitchable.add(viewBodyHex.getPane(), viewBodyHex.getName());
		
		// Combobox
		comboxSelectView.addItem(viewBodyText.getName());
		comboxSelectView.addItem(viewBodyTable.getName());
		comboxSelectView.addItem(viewBodyHex.getName());
		comboxSelectView.addActionListener(this);
		panelOptions.add(comboxSelectView);
	}

	@Override
	public String getName() {
		return "Split";
	}

	@Override
	public void setHttpMessage(HttpMessage httpMessage) {
		this.httpMessage = httpMessage;

		modelTextHeader.setHttpMessage(httpMessage);

		modelTextBody.setHttpMessage(httpMessage);
		modelHexBody.setHttpMessage(httpMessage);
		modelTableBody.setHttpMessage(httpMessage);
		
		// This is not nice, but needed for fuzzing
		// ExtensionAntiCSRF gets HttpMessage from HttpPanelTextView...
		viewBodyText.setHttpMessage(httpMessage);
		viewHeaderText.setHttpMessage(httpMessage);
	}
	
    @Override
    public void load() {
            if (httpMessage == null) {
                    return;
            }

            // Update UI
            viewHeaderText.load();
            
            if (currentView == null) {
            	return;
            }
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

	
	/**
	 * @param prefix
	 * @param location
	 */
	private final void saveDividerLocation(String prefix, int location) {
		if (location > 0) {
			if (log.isDebugEnabled()) log.debug("Saving preference " + prefnzPrefix+prefix + "." + PREF_DIVIDER_LOCATION + "=" + location);
			this.preferences.put(prefnzPrefix+prefix + "." + PREF_DIVIDER_LOCATION, Integer.toString(location));
			// immediate flushing
			try {
				this.preferences.flush();
			} catch (final BackingStoreException e) {
				log.error("Error while saving the preferences", e);
			}
		}
	}
	
	/**
	 * @param prefix
	 * @param fallback
	 * @return the size of the frame OR fallback value, if there wasn't any preference.
	 */
	private final int restoreDividerLocation(String prefix, int fallback) {
		int result = fallback;
		final String sizestr = preferences.get(prefnzPrefix+prefix + "." + PREF_DIVIDER_LOCATION, null);
		if (sizestr != null) {
			int location = 0;
			try {
				location = Integer.parseInt(sizestr.trim());
			} catch (final Exception e) {
				// ignoring, cause is prevented by default values;
			}
			if (location > 0 ) {
				result = location;
				if (log.isDebugEnabled()) log.debug("Restoring preference " + prefnzPrefix+prefix + "." + PREF_DIVIDER_LOCATION + "=" + location);
			}
		}
		return result;
	}
	
	/*
	 * ========================================================================
	 */
	
	private final class DividerResizedListener implements PropertyChangeListener {

		private final String prefix;
		
		public DividerResizedListener(String prefix) {
			super();
			assert prefix != null;
			this.prefix = prefix;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			JSplitPane component = (JSplitPane) evt.getSource();
			if (component != null) {
				if (log.isDebugEnabled()) log.debug(prefnzPrefix+prefix + "." + "location" + "=" + component.getDividerLocation());
				saveDividerLocation(prefix, component.getDividerLocation());
			}
		}
		
	}

	@Override
	protected boolean isRequest() {
		return true;
	}

}
