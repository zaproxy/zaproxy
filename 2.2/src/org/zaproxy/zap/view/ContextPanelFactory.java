package org.zaproxy.zap.view;

import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.model.Context;

public interface ContextPanelFactory {
	AbstractContextPropertiesPanel getContextPanel(Context ctx);
	
	void discardContexts();
}
