package org.zaproxy.zap.extension.httppanel.plugin.response.image;

import java.util.List;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.plugin.BasicPlugin;
import org.zaproxy.zap.extension.search.SearchMatch;

public class ResponseImageView extends BasicPlugin {

	public ResponseImageView(HttpPanel httpPanel, HttpMessage httpMessage) {
		super(httpPanel, httpMessage);

		initUi();
	}

	JPanel scrollPane;
	
	@Override
	protected void initUi() {
		// init elements used by BasicPlugin
		buttonShowView = new JButton("Image");
		panelOptions = new JPanel();
		panelMain = new JPanel();
		scrollPane = new JPanel();
		panelMain.add(scrollPane);

		// add Plugin to view.
		httpPanel.addHttpDataView(this);
	}
	
	@Override
	public String getName() {
		return "Image";
	}

	@Override
	protected boolean isRequest() {
		return false;
	}

	@Override
	public void load() {
			if (httpMessage == null || httpMessage.getResponseBody() == null) {
				return;
			}

			String contentType =httpMessage.getResponseHeader().getHeader("Content-type"); 
			if (contentType != null && ! contentType.toLowerCase().contains("image")) {
				return;
			}

			ImageIcon image = new ImageIcon( httpMessage.getResponseBody().getBytes());
			scrollPane.removeAll();
			JLabel label = new JLabel(image);
			scrollPane.add(label);
			
			// Update view to redraw the picture
			panelMain.invalidate();
	}

	@Override
	public void save() {
	}

	@Override
	protected void initModel() {
	}

	@Override
	protected void initPlugins() {
	}

	@Override
	public void searchHeader(Pattern p, List<SearchMatch> matches) {
	}

	@Override
	public void searchBody(Pattern p, List<SearchMatch> matches) {
	}

	@Override
	public void highlightHeader(SearchMatch sm) {
	}

	@Override
	public void highlightBody(SearchMatch sm) {
	}

}
