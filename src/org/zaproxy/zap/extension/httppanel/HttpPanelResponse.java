package org.zaproxy.zap.extension.httppanel;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.plugin.PluginInterface;
import org.zaproxy.zap.extension.httppanel.plugin.request.all.RequestAllView;
import org.zaproxy.zap.extension.httppanel.plugin.request.split.RequestSplitView;
import org.zaproxy.zap.extension.httppanel.plugin.response.split.ResponseSplitView;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.extension.httppanel.HttpPanel;

public class HttpPanelResponse extends HttpPanel implements ActionListener  {
	private static final long serialVersionUID = 1L;
    
	
	public HttpPanelResponse(boolean isEditable, HttpMessage httpMessage) {
		super(isEditable, httpMessage);
	}
	
	public HttpPanelResponse(boolean isEditable, Extension extension, HttpMessage httpMessage) {
		super(isEditable, extension, httpMessage);
	}

	protected void initPlugins() {
		// TODO: elsewhere
		new ResponseSplitView(this, httpMessage);
	}

	protected void initSpecial() {
		
	}
	
}