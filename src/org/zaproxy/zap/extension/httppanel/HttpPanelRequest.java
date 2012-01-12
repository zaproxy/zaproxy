package org.zaproxy.zap.extension.httppanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.extension.httppanel.plugin.request.all.RequestAllView;
import org.zaproxy.zap.extension.httppanel.plugin.request.split.RequestSplitView;

/*
 *
 */

public class HttpPanelRequest extends HttpPanel {
	private static final long serialVersionUID = 1L;
	private JComboBox comboChangeMethod;
	
	public HttpPanelRequest(boolean isEditable, HttpMessage httpMessage) {
		super(isEditable, httpMessage);
	}

	public HttpPanelRequest(boolean isEditable, Extension extension, HttpMessage httpMessage) {
		super(isEditable, extension, httpMessage);
	}
	
	public HttpPanelRequest(boolean isEditable, Extension extension, HttpMessage httpMessage, OptionsParamView.ViewType viewType) {
		super(isEditable, extension, httpMessage, viewType);
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