package org.zaproxy.zap.extension.httppanel;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.history.ManualRequestEditorDialog;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.HttpPanel;
import org.zaproxy.zap.extension.httppanel.model.HttpDataModel;
import org.zaproxy.zap.extension.httppanel.model.HttpDataModelResAll;
import org.zaproxy.zap.extension.httppanel.model.HttpDataModelResContent;
import org.zaproxy.zap.extension.httppanel.model.HttpDataModelResCookies;
import org.zaproxy.zap.extension.httppanel.model.HttpDataModelResHeader;
import org.zaproxy.zap.extension.httppanel.model.HttpDataModelResSplit;
import org.zaproxy.zap.extension.search.SearchMatch;


public class HttpPanelResponse extends HttpPanel implements ActionListener  {
	private static final long serialVersionUID = 1L;

    private JComboBox jComboView;
    
    private JButton btnSplit;
	private JButton btnAll;
	private JButton btnHeader;
	private JButton btnContent;
	private JButton btnCookies;

	private HttpPanelTextUi httpPanelTextUi;
	private HttpPanelTableUi httpPanelTableUi;
	private HttpPanelHexUi httpPanelHexUi;
	private HttpPanelSplitUi httpPanelSplitUi;
	
	private HttpDataModelResAll httpDataModelAll;
	private HttpDataModelResHeader httpDataModelHeader;
	private HttpDataModelResContent httpDataModelContent;
	private HttpDataModelResCookies httpDataModelCookies;
	private HttpDataModelResSplit httpDataModelSplit;
	
	private View currentView = View.ALL;
	private HttpDataModel currentHttpDataModel;
	
	// ZAP: Added logger
    private static Log log = LogFactory.getLog(ManualRequestEditorDialog.class);

	private enum View {
		ALL,
		HEADER,
		CONTENT,
		COOKIES,
		SPLIT
	}
	
	private enum ViewMode {
		TEXT,
		TABLE,
		HEX
	}
    
	public HttpPanelResponse() {
		super();
		init();
	}
	
	public HttpPanelResponse(boolean isEditable, HttpMessage httpMessage) {
		super(isEditable, httpMessage);
		init();
	}
	
	public HttpPanelResponse(boolean isEditable, Extension extension, HttpMessage httpMessage) {
		super(isEditable, extension, httpMessage);
		init();
	}
	
	private void init() {
		initHeader();
		initContent();
		
		changeCard();
		changeView(View.ALL);
		
		if (isEditable()) {
		}
	}

	private void initHeader() {
		btnSplit = new JButton("Split");
		btnAll = new JButton("All");
		btnHeader = new JButton("Header");
		btnContent = new JButton("Content");
		btnCookies = new JButton("Cookies");
		
		btnSplit.addActionListener(this);
		btnAll.addActionListener(this);
		btnHeader.addActionListener(this);
		btnContent.addActionListener(this);
		btnCookies.addActionListener(this);
		
		getPanelHeader().add(btnSplit);
		getPanelHeader().add(btnAll);
		getPanelHeader().add(btnHeader);
		getPanelHeader().add(btnContent);
		getPanelHeader().add(btnCookies);
		
		jComboView = new JComboBox();
		jComboView.setSelectedIndex(-1);
		jComboView.addItem("Text");
		jComboView.addItem("Table");
		jComboView.addItem("Hex");
		jComboView.addActionListener(this);
		
		getPanelHeader().add(jComboView);
	}
	
	private void initContent() {
		httpPanelTextUi = new HttpPanelTextUi();
		httpPanelTableUi = new HttpPanelTableUi();
		httpPanelHexUi = new HttpPanelHexUi();
		httpPanelSplitUi = new HttpPanelSplitUi(isEditable(), httpMessage);
		
		getPanelContent().setLayout(new CardLayout());
		
		getPanelContent().add(httpPanelTextUi, "text");
		getPanelContent().add(httpPanelTableUi, "table");
		getPanelContent().add(httpPanelHexUi, "hex");
		getPanelContent().add(httpPanelSplitUi, "split");
		
		httpDataModelAll = new HttpDataModelResAll(this, httpPanelHexUi, httpPanelTableUi, httpPanelTextUi);
		httpDataModelHeader = new HttpDataModelResHeader(this, httpPanelHexUi, httpPanelTableUi, httpPanelTextUi);
		httpDataModelContent = new HttpDataModelResContent(this, httpPanelHexUi, httpPanelTableUi, httpPanelTextUi);
		httpDataModelCookies = new HttpDataModelResCookies(this, httpPanelHexUi, httpPanelTableUi, httpPanelTextUi);
		httpDataModelSplit = new HttpDataModelResSplit(this, httpPanelSplitUi);
	}
	
	/* Update current content because of changed httpMessage
	 * 
	 */
	@Override
	public void updateContent() {
		loadData();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		saveData();
		
		if (e.getSource() == btnSplit) {
			// Split is special
			CardLayout cl = (CardLayout)(getPanelContent().getLayout());
			cl.show(getPanelContent(), "split");
			currentView = View.SPLIT;
		} else {
			if (currentView == View.SPLIT) {
				changeCard();
			}
			
			if (e.getSource() == btnContent) {
				changeView(View.CONTENT);
			} else if (e.getSource() == btnAll) {
				changeView(View.ALL);
			} else if (e.getSource() == btnHeader) {
				changeView(View.HEADER);
			} else if (e.getSource() == btnCookies) {
				changeView(View.COOKIES);
			} else if (e.getSource() == jComboView) {
				changeCard();
			}
		}
		
		loadData();
	}
	
	private void changeCard() {
		ViewMode view = getView();
	    CardLayout cl = (CardLayout)(getPanelContent().getLayout());
	    
		if (view.equals(ViewMode.TEXT)) {
			cl.show(getPanelContent(), "text");
		} else if (view.equals(ViewMode.TABLE)) {
			cl.show(getPanelContent(), "table");
		} else if (view.equals(ViewMode.HEX)) {
			cl.show(getPanelContent(), "hex");
		}
	}
	
	private void loadData() {
		ViewMode view = getView();
		
		if (currentView.equals(View.SPLIT)) {
			// Split is special
			httpDataModelSplit.loadData();
		} else if (view.equals(ViewMode.TEXT)) {
			currentHttpDataModel.textDataToView();
		} else if (view.equals(ViewMode.TABLE)) {
			currentHttpDataModel.tableDataToView();
		} else if (view.equals(ViewMode.HEX)) {
			currentHttpDataModel.hexDataToView();
		}
	}
	
	public void saveData() {
		
	}
	
	public void clearView(boolean enableViewSelect) {
		
	}	

	private void changeView(View view) {
		this.currentView = view;

		if (view.equals(View.ALL)) {
			currentHttpDataModel = httpDataModelAll;
		} else if (view.equals(View.CONTENT)) {
			currentHttpDataModel = httpDataModelContent;
		} else if (view.equals(View.HEADER)) {
			currentHttpDataModel = httpDataModelHeader;
		} else if (view.equals(View.COOKIES)) {
			currentHttpDataModel = httpDataModelCookies;
		}

	}

	private ViewMode getView() {
		String item = (String) jComboView.getSelectedItem();

		if (item.equals("Text")) {
			return ViewMode.TEXT;
		} else if (item.equals("Table")) {
			return ViewMode.TABLE;
		} else if (item.equals("Hex")) {
			return ViewMode.HEX;
		}
		
		// Default
		return ViewMode.TEXT;
	}

	/*** Search Functions - for SearchPanel and SearchResult 
	 * We'll only use the Text card for finding and displaying search results. 
	 * highlight* and *Search belong together.
	 ***/
	
	public void highlightHeader(SearchMatch sm) {
		changeView(View.HEADER);
		httpPanelTextUi.highlight(sm);
		
	}

	public void highlightBody(SearchMatch sm) {
		changeView(View.CONTENT);
		httpPanelTextUi.highlight(sm);
	}

	@Override
	public void bodySearch(Pattern p, List<SearchMatch> matches) {
		httpDataModelHeader.search(p, matches);		
	}

	@Override
	public void headerSearch(Pattern p, List<SearchMatch> matches) {
		httpDataModelContent.search(p, matches);		
	}
	
}