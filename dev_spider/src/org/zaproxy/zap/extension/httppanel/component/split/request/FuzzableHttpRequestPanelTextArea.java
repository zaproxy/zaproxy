package org.zaproxy.zap.extension.httppanel.component.split.request;

import org.zaproxy.zap.extension.fuzz.FuzzableComponent;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextArea;

public abstract class FuzzableHttpRequestPanelTextArea extends HttpPanelTextArea implements FuzzableComponent {
	
	private static final long serialVersionUID = 4129376491067755149L;

	@Override
	public boolean canFuzz() {
		//Currently do not allow to fuzz if the text area is editable, because the HttpMessage used is not updated with the changes.
		if (isEditable()) {
			return false;
		}
		
		final String selectedText = getSelectedText();
		if (selectedText == null || selectedText.isEmpty()) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public String getFuzzTarget() {
		return getSelectedText();
	}

}
