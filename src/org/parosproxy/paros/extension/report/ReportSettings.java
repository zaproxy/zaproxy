package org.parosproxy.paros.extension.report;

public class ReportSettings {

	private boolean includeHTML = false;
	
	public ReportSettings(boolean html) {
		includeHTML = html;
	}
	
	public void setIncludeHTML(boolean includeHTML) {
		this.includeHTML = includeHTML;
	}
	
	public boolean getIncludeHTML() {
		return includeHTML;
	}
}
