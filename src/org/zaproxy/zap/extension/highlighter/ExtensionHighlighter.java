package org.zaproxy.zap.extension.highlighter;

import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.params.ParamsAPI;
import org.zaproxy.zap.view.SiteMapListener;

/*
 * Implements the Extension Interface for HighlighterManager and HighlighterPanel
 */
public class ExtensionHighlighter extends ExtensionAdaptor
implements SessionChangedListener, /*ProxyListener, */ SiteMapListener {

	public static final String NAME = "ExtensionHighlighter";
	private HighlighterPanel highlighterPanel;
		
	public ExtensionHighlighter() {
		this.setName(NAME);
		this.setOrder(69);
		//API.getInstance().registerApiImplementor(new ParamsAPI(this));
	}
	
	@Override
	public void sessionChanged(Session session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nodeSelected(SiteNode node) {
		// TODO Auto-generated method stub
		
	}

	public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        extensionHook.addSessionListener(this);
        extensionHook.addSiteMapListner(this);
        
        if (getView() != null) {
            @SuppressWarnings("unused")
            ExtensionHookView pv = extensionHook.getHookView();
            extensionHook.getHookView().addStatusPanel(getHighlighterPanel());

//            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuParamSearch());
 //           extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAddAntiCSRF());
 //           extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuRemoveAntiCSRF());
 //           extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAddSession());
 //           extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuRemoveSession());
//
            ExtensionHelp.enableHelpKey(getHighlighterPanel(), "ui.tabs.hilighter");
        }
    }
	
    protected HighlighterPanel getHighlighterPanel() {
        if (highlighterPanel == null) {
        	highlighterPanel = new HighlighterPanel(this);
        }
        return highlighterPanel;
}
    
	/*
    private PopupMenuParamSearch getPopupMenuParamSearch() {
            if (popupMenuSearch == null) {
                    popupMenuSearch = new PopupMenuParamSearch();
                    popupMenuSearch.setExtension(this);
            }
            return popupMenuSearch;
    }
    
    private PopupMenuAddAntiCSRF getPopupMenuAddAntiCSRF() {
            if (popupMenuAddAntiCsrf == null) {
                    popupMenuAddAntiCsrf = new PopupMenuAddAntiCSRF();
                    popupMenuAddAntiCsrf.setExtension(this);
            }
            return popupMenuAddAntiCsrf;
    }

    private PopupMenuRemoveAntiCSRF getPopupMenuRemoveAntiCSRF() {
            if (popupMenuRemoveAntiCsrf == null) {
                    popupMenuRemoveAntiCsrf = new PopupMenuRemoveAntiCSRF();
                    popupMenuRemoveAntiCsrf.setExtension(this);
            }
            return popupMenuRemoveAntiCsrf;
    }

    private PopupMenuAddSession getPopupMenuAddSession() {
            if (popupMenuAddSession == null) {
                    popupMenuAddSession = new PopupMenuAddSession();
                    popupMenuAddSession.setExtension(this);
            }
            return popupMenuAddSession;
    }

    private PopupMenuRemoveSession getPopupMenuRemoveSession() {
            if (popupMenuRemoveSession == null) {
                    popupMenuRemoveSession = new PopupMenuRemoveSession();
                    popupMenuRemoveSession.setExtension(this);
            }
            return popupMenuRemoveSession;
    }

    protected ParamsPanel getParamsPanel() {
            if (paramsPanel == null) {
                    paramsPanel = new ParamsPanel(this);
            }
            return paramsPanel;
    }
    */
	
	@Override
	public void sessionAboutToChange(Session session) {
	}
}
