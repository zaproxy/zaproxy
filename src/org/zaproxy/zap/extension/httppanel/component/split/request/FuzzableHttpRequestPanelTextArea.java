package org.zaproxy.zap.extension.httppanel.component.split.request;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.fuzz.FuzzableComponent;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextArea;
import org.zaproxy.zap.extension.httppanel.view.util.CaretVisibilityEnforcerOnFocusGain;

public abstract class FuzzableHttpRequestPanelTextArea extends HttpPanelTextArea implements FuzzableComponent {
	
	private static final long serialVersionUID = 4129376491067755149L;

	private CaretVisibilityEnforcerOnFocusGain caretVisiblityEnforcer;
	
	public FuzzableHttpRequestPanelTextArea() {
		super();
		
		caretVisiblityEnforcer = new CaretVisibilityEnforcerOnFocusGain(this);
	}

	@Override
	public void setMessage(Message aMessage) {
		super.setMessage(aMessage);
		
		caretVisiblityEnforcer.setEnforceVisibilityOnFocusGain(aMessage != null);
	}
	
    @Override
    public Class<? extends Message> getMessageClass() {
        return HttpMessage.class;
    }

	@Override
	public boolean canFuzz() {
		if (getMessage() == null) {
			return false;
		}
		
		//Currently do not allow to fuzz if the text area is editable, because the HttpMessage used is not updated with the changes.
		return !isEditable();
	}
	
	@Override
	public String getFuzzTarget() {
		final String selectedText = getSelectedText();
		if (selectedText != null) {
			return selectedText;
		}
		return "";
	}

}
