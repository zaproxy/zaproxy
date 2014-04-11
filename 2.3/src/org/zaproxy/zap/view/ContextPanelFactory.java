package org.zaproxy.zap.view;

import org.zaproxy.zap.model.Context;

public interface ContextPanelFactory {
	AbstractContextPropertiesPanel getContextPanel(Context ctx);
	
	void discardContexts();
}
