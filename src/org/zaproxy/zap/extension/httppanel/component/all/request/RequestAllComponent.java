/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.httppanel.component.all.request;

import java.awt.BorderLayout;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.component.HttpPanelComponentInterface;
import org.zaproxy.zap.extension.httppanel.component.HttpPanelComponentViewsManager;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelDefaultViewSelector;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestStringHttpPanelViewModel;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.extension.search.SearchableHttpPanelComponent;
import org.zaproxy.zap.model.HttpMessageLocation;
import org.zaproxy.zap.model.MessageLocation;
import org.zaproxy.zap.view.messagelocation.MessageLocationHighlight;
import org.zaproxy.zap.view.messagelocation.MessageLocationHighlighter;

public class RequestAllComponent implements HttpPanelComponentInterface, SearchableHttpPanelComponent, MessageLocationHighlighter {
	
	public static final String NAME = "RequestAll";

	private static final String BUTTON_TOOL_TIP = Constant.messages.getString("http.panel.component.all.tooltip");
	
	protected JToggleButton buttonShowView;
	
	protected JPanel panelOptions;
	protected JPanel panelMain;

	protected Message message;
	
	protected HttpPanelComponentViewsManager views;

	public RequestAllComponent() {
		this.message = null;
		
		views = new HttpPanelComponentViewsManager("all");
		
		initUi();
	}
	
	protected void initUi() {
		// Common
		buttonShowView = new JToggleButton(new ImageIcon(RequestAllComponent.class.getResource("/resource/icon/view_all.png")));
		buttonShowView.setToolTipText(BUTTON_TOOL_TIP);

		panelOptions = new JPanel();
		panelOptions.add(views.getSelectableViewsComponent());
		
		initViews();

		// All
		panelMain = new JPanel(new BorderLayout());
		panelMain.add(views.getViewsPanel());

		setSelected(false);
	}
	
	@Override
	public void setParentConfigurationKey(String configurationKey) {
		views.setConfigurationKey(configurationKey);
	}
	
	@Override
	public JToggleButton getButton() {
		return buttonShowView;
	}

	@Override
	public JPanel getOptionsPanel() {
		return panelOptions;
	}

    @Override
    public JPanel getMoreOptionsPanel() {
        return null;
    }

	@Override
	public JPanel getMainPanel() {
		return panelMain;
	}
	
	@Override
	public void setSelected(boolean selected) {
		buttonShowView.setSelected(selected);

		views.setSelected(selected);
	}

    @Override
    public boolean isEnabled(Message aMessage) {
        if (aMessage == null) {
            return true;
        }
        
        return (aMessage instanceof HttpMessage);
    }
	
	protected void initViews() {
		views.addView(new HttpRequestAllPanelTextView(new RequestStringHttpPanelViewModel()));
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getPosition() {
	    return 2;
	}
	
	@Override
	public void setMessage(Message aMessage) {
		this.message = aMessage;
		
		views.setMessage(message);
	}
	
	@Override
	public void save() {
		if (message == null) {
			return;
		}
		
		views.save();
	}
	
	@Override
	public void addView(HttpPanelView view, Object options, FileConfiguration fileConfiguration) {
		views.addView(view, fileConfiguration);
	}

	@Override
	public void removeView(String viewName, Object options) {
		views.removeView(viewName);
	}
	
	@Override
	public void clearView() {
		views.clearView();
	}

	@Override
	public void clearView(boolean enableViewSelect) {
		views.clearView();
		
		setEnableViewSelect(enableViewSelect);
	}
	
	@Override
	public void setEnableViewSelect(boolean enableViewSelect) {
		views.setEnableViewSelect(enableViewSelect);
	}

	@Override
	public void addDefaultViewSelector(HttpPanelDefaultViewSelector defaultViewSelector, Object options) {
		views.addDefaultViewSelector(defaultViewSelector);
	}

	@Override
	public void removeDefaultViewSelector(String defaultViewSelectorName, Object options) {
		views.removeDefaultViewSelector(defaultViewSelectorName);
	}

	@Override
	public void loadConfig(FileConfiguration fileConfiguration) {
		views.loadConfig(fileConfiguration);
	}

	@Override
	public void saveConfig(FileConfiguration fileConfiguration) {
		views.saveConfig(fileConfiguration);
	}

	@Override
	public void setEditable(boolean editable) {
		views.setEditable(editable);
	}

	@Override
	public void highlightHeader(SearchMatch sm) {
		views.highlight(sm);
	}

	@Override
	public void highlightBody(SearchMatch sm) {
		views.highlight(sm);
	}

	@Override
	public void searchHeader(Pattern p, List<SearchMatch> matches) {
		views.search(p, matches);
	}

	@Override
	public void searchBody(Pattern p, List<SearchMatch> matches) {
		views.search(p, matches);
	}

    @Override
    public boolean supports(MessageLocation location) {
        if (!(location instanceof HttpMessageLocation)) {
            return false;
        }

        HttpMessageLocation httpMessageLocation = (HttpMessageLocation) location;
        switch (httpMessageLocation.getLocation()) {
        case REQUEST_HEADER:
        case RESPONSE_HEADER:
        case REQUEST_BODY:
        case RESPONSE_BODY:
            return views.supports(httpMessageLocation);
        default:
            return false;
        }
    }

    @Override
    public boolean supports(Class<? extends MessageLocation> classLocation) {
        if (!(HttpMessageLocation.class.isAssignableFrom(classLocation))) {
            return true;
        }
        return false;
    }

    @Override
    public MessageLocationHighlight highlight(MessageLocation location) {
        if (!(location instanceof HttpMessageLocation)) {
            return null;
        }

        HttpMessageLocation httpMessageLocation = (HttpMessageLocation) location;
        switch (httpMessageLocation.getLocation()) {
        case REQUEST_HEADER:
        case RESPONSE_HEADER:
        case REQUEST_BODY:
        case RESPONSE_BODY:
            return views.highlight(httpMessageLocation);
        default:
            return null;
        }
    }

    @Override
    public MessageLocationHighlight highlight(MessageLocation location, MessageLocationHighlight highlight) {
        if (!(location instanceof HttpMessageLocation)) {
            return null;
        }

        HttpMessageLocation httpMessageLocation = (HttpMessageLocation) location;
        switch (httpMessageLocation.getLocation()) {
        case REQUEST_HEADER:
        case RESPONSE_HEADER:
        case REQUEST_BODY:
        case RESPONSE_BODY:
            return views.highlight(httpMessageLocation, highlight);
        default:
            return null;
        }
    }

    @Override
    public void removeHighlight(MessageLocation location, MessageLocationHighlight highlightReference) {
        if (!(location instanceof HttpMessageLocation)) {
            return;
        }

        HttpMessageLocation httpMessageLocation = (HttpMessageLocation) location;
        switch (httpMessageLocation.getLocation()) {
        case REQUEST_HEADER:
        case RESPONSE_HEADER:
        case REQUEST_BODY:
        case RESPONSE_BODY:
            views.removeHighlight(httpMessageLocation, highlightReference);
            break;
        default:
        }
    }

    @Override
    public HttpPanelView setSelectedView(String viewName) {
        return views.setSelectedView(viewName);
    }

}
