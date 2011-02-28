package org.zaproxy.zap.extension.httppanel;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.HttpPanel;
import org.zaproxy.zap.extension.search.SearchMatch;

public class HttpDataModelReqHeader extends HttpDataModel {

	public HttpDataModelReqHeader(HttpPanel httpPanel,
			HttpPanelHexUi httpPanelHexUi, HttpPanelTableUi httpPanelTableUi,
			HttpPanelTextUi httpPanelTextUi) {
		super(httpPanel, httpPanelHexUi, httpPanelTableUi, httpPanelTextUi);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void hexDataFromView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hexDataToView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tableDataFromView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tableDataToView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void textDataFromView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void textDataToView() {
		httpPanelTextUi.setData(getHttpMessage().getRequestHeader().toString());
		
	}

	/* for UI search
	 * should use exactly same data as textDataToView(), or else it does not show correctly
	 */
	public void search(Pattern p, List<SearchMatch> matches) {
		Matcher m;
		
		m = p.matcher(getHttpMessage().getRequestHeader().toString());
		while (m.find()) {
			matches.add(
				new SearchMatch(SearchMatch.Locations.REQUEST_HEAD,
						m.start(), m.end()));
		}
	}

}
