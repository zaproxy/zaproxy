package org.zaproxy.zap.extension.httppanel;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.extension.httppanel.plugin.PluginInterface;
import org.zaproxy.zap.extension.httppanel.plugin.request.all.RequestAllView;
import org.zaproxy.zap.extension.httppanel.plugin.request.split.RequestSplitView;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.extension.httppanel.HttpPanel;

/*
 *
 */

public class HttpPanelRequest extends HttpPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JComboBox comboChangeMethod;
	
	public HttpPanelRequest(boolean isEditable, HttpMessage httpMessage) {
		super(isEditable, httpMessage);
	}

	public HttpPanelRequest(boolean isEditable, Extension extension, HttpMessage httpMessage) {
		super(isEditable, extension, httpMessage);
	}


	protected void initPlugins() {
		// TODO: elsewhere
		new RequestSplitView(this, httpMessage);
		new RequestAllView(this, httpMessage);
	}

	protected void initSpecial() {
		if (isEditable()) {
			initComboChangeMethod();
		}
	}


	//	private JComboBox getComboChangeMethod() {
	private void initComboChangeMethod() {
		comboChangeMethod = new JComboBox();
		comboChangeMethod.setEditable(false);
		comboChangeMethod.addItem(Constant.messages.getString("manReq.pullDown.method"));
		comboChangeMethod.addItem(HttpRequestHeader.CONNECT);
		comboChangeMethod.addItem(HttpRequestHeader.DELETE);
		comboChangeMethod.addItem(HttpRequestHeader.GET);
		comboChangeMethod.addItem(HttpRequestHeader.HEAD);
		comboChangeMethod.addItem(HttpRequestHeader.OPTIONS);
		comboChangeMethod.addItem(HttpRequestHeader.POST);
		comboChangeMethod.addItem(HttpRequestHeader.PUT);
		comboChangeMethod.addItem(HttpRequestHeader.TRACE);
		comboChangeMethod = comboChangeMethod;

		comboChangeMethod.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (comboChangeMethod.getSelectedIndex() > 0) {
					saveData();
					getHttpMessage().mutateHttpMethod((String) comboChangeMethod.getSelectedItem());
					comboChangeMethod.setSelectedIndex(0);
					updateContent();
				}
			}});

		panelSpecial.add(comboChangeMethod);
	}

}