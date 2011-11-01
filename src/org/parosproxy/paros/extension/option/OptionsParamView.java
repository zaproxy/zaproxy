/*
*
* Paros and its related class files.
* 
* Paros is an HTTP/HTTPS proxy for assessing web application security.
* Copyright (C) 2003-2004 Chinotec Technologies Company
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the Clarified Artistic License
* as published by the Free Software Foundation.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* Clarified Artistic License for more details.
* 
* You should have received a copy of the Clarified Artistic License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
// ZAP: 2011/06/02 Warn the first time the user double clicks on a tab

package org.parosproxy.paros.extension.option;

import java.util.Locale;

import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.model.Model;

// ZAP: Added support for selecting the locale

public class OptionsParamView extends AbstractParam {

	private static final String PROCESS_IMAGES = "view.processImages";
	public static final String LOCALE = "view.locale";
	public static final String LOCALES = "view.locales";
	public static final String DISPLAY_OPTION = "view.displayOption";
	public static final String EDITORVIEW_OPTION = "view.editorView";
	public static final String BRK_PANEL_VIEW_OPTION = "view.brkPanelView";
	public static final String SHOW_MAIN_TOOLBAR_OPTION = "view.showMainToolbar";
	public static final String DEFAULT_LOCALE = "en_GB";
	public static final String ADVANCEDUI_OPTION = "view.advancedview";
	public static final String WMUIHANDLING_OPTION = "view.uiWmHandling";
	public static final String ASKONEXIT_OPTION = "view.askOnExit";
	public static final String WARN_ON_TAB_DOUBLE_CLICK_OPTION = "view.warnOnTabDoubleClick";
	public static final String REVEAL_OPTION = "view.reveal";

	public static final String DEFAULT_VIEW_REQ_MANUAL = "view.defaultViewReqManual";
	public static final String DEFAULT_VIEW_REQ_PROXY = "view.defaultViewReqProxy";
	public static final String DEFAULT_VIEW_REQ_HISTORY = "view.defaultViewReqHistory";
	public static final String DEFAULT_VIEW_RES_MANUAL = "view.defaultViewResManual";
	public static final String DEFAULT_VIEW_RES_PROXY = "view.defaultViewResProxy";
	public static final String DEFAULT_VIEW_RES_HISTORY = "view.defaultViewResHistory";
	
	public static enum ViewType {
		req_manual,
		req_proxy,
		req_history,
		res_manual,
		res_proxy,
		res_history
	};
	
	private int advancedViewEnabled = 0;
	private int editorViewOption;
	private int processImages = 0;
	private int showMainToolbar = 1;
	private String configLocale = "";
	private String locale = "";
	private int displayOption = 0;
	private int brkPanelViewOption = 0;
	private int askOnExitEnabled = 1;
	private int wmUiHandlingEnabled = 0;
	private boolean warnOnTabDoubleClick = false;
	private boolean reveal = false;
	
	private String defaultReqViewManual = "";
	private String defaultReqViewProxy = "";
	private String defaultReqViewHistory = "";
	private String defaultResViewManual = "";
	private String defaultResViewProxy = "";
	private String defaultResViewHistory = "";

	
    /**
     * @param rootElementName
     */
    public OptionsParamView() {
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.common.FileXML#parse()
     */
    @Override
	protected void parse() {
	    // use temp variable to check.  Exception will be flagged if any error.
	    processImages = getConfig().getInt(PROCESS_IMAGES, 0);
	    configLocale = getConfig().getString(LOCALE);	// No default
	    locale = getConfig().getString(LOCALE, DEFAULT_LOCALE);
	    displayOption = getConfig().getInt(DISPLAY_OPTION, 0);
	    editorViewOption = getConfig().getInt(EDITORVIEW_OPTION, 2);
	    brkPanelViewOption = getConfig().getInt(BRK_PANEL_VIEW_OPTION, 0);
	    showMainToolbar = getConfig().getInt(SHOW_MAIN_TOOLBAR_OPTION, 1);
	    advancedViewEnabled = getConfig().getInt(ADVANCEDUI_OPTION, 0);
	    wmUiHandlingEnabled = getConfig().getInt(WMUIHANDLING_OPTION, 0);
	    askOnExitEnabled = getConfig().getInt(ASKONEXIT_OPTION, 1);
	    warnOnTabDoubleClick = getConfig().getBoolean(WARN_ON_TAB_DOUBLE_CLICK_OPTION, true);
	    reveal = getConfig().getBoolean(REVEAL_OPTION, true);
	    
	    defaultReqViewManual = getConfig().getString(DEFAULT_VIEW_REQ_MANUAL, "");
	    defaultReqViewProxy = getConfig().getString(DEFAULT_VIEW_REQ_PROXY, "");
	    defaultReqViewHistory = getConfig().getString(DEFAULT_VIEW_REQ_HISTORY, "");
	    defaultResViewManual = getConfig().getString(DEFAULT_VIEW_RES_MANUAL, "");
	    defaultResViewProxy = getConfig().getString(DEFAULT_VIEW_RES_PROXY, "");
	    defaultResViewHistory = getConfig().getString(DEFAULT_VIEW_RES_HISTORY, "");
    }

	/**
	 * @return Returns the skipImage.
	 */
	public int getProcessImages() {
		return processImages;
	}
	
	/**
	 * @param processImages 0 = not to process.  Other = process images
	 * 
	 */
	public void setProcessImages(int processImages) {
		this.processImages = processImages;
		getConfig().setProperty(PROCESS_IMAGES, Integer.toString(processImages));
	}
	
	public boolean isProcessImages() {
		return !(processImages == 0);
	}
	
	public int getShowMainToolbar() {
		return showMainToolbar;
	}
	
	public void setShowMainToolbar(int showMainToolbar) {
		this.showMainToolbar = showMainToolbar;
		getConfig().setProperty(SHOW_MAIN_TOOLBAR_OPTION, Integer.toString(showMainToolbar));
	}

	
	/**
	 * @return the locale, which should be used. 
	 *         It will return a default value, if nothing was configured yet. 
	 *         Never null
	 * @see {@link #getConfigLocale()}
	 */
	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		if (locale != null) {
			this.locale = locale;
			getConfig().setProperty(LOCALE, locale);
		}
	}
	
	public void setLocale(Locale locale) {
		if (locale != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(locale.getLanguage());
			if (locale.getCountry().length() > 0) sb.append("_").append(locale.getCountry());
			if (locale.getVariant().length() > 0) sb.append("_").append(locale.getVariant());
			this.locale = sb.toString();
			getConfig().setProperty(LOCALE, locale);
		}
	}

	/**
	 * @return The really configured locale, can be null
	 * @see {@link #getLocale()}
	 */
	public String getConfigLocale() {
		return configLocale;
	}

	public int getEditorViewOption() {
		return editorViewOption;
	}
	
	public void setEditorViewOption(int idx) {
		editorViewOption = idx;
		getConfig().setProperty(EDITORVIEW_OPTION, Integer.toString(editorViewOption));
	}
	
	public int getBrkPanelViewOption() {
		return brkPanelViewOption;
	}
	
	public void setBrkPanelViewOption(int brkPanelViewIdx) {
		brkPanelViewOption = brkPanelViewIdx;
		getConfig().setProperty(BRK_PANEL_VIEW_OPTION, Integer.toString(brkPanelViewOption));
	}
	
	public int getDisplayOption() {
		return displayOption;
	}

	public void setDisplayOption(int displayOption) {
		this.displayOption = displayOption;
		getConfig().setProperty(DISPLAY_OPTION, Integer.toString(displayOption));
	}
	
	public int getAdvancedViewOption() {
		return advancedViewEnabled;
	}
	
	public void setAdvancedViewOption(int isEnabled) {
		advancedViewEnabled = isEnabled;
		getConfig().setProperty(ADVANCEDUI_OPTION, Integer.toString(isEnabled));
	}

	public void setAskOnExitOption(int isEnabled) {
		askOnExitEnabled = isEnabled;
		getConfig().setProperty(ASKONEXIT_OPTION, Integer.toString(isEnabled));
	}

	public int getAskOnExitOption() {
		return askOnExitEnabled;
	}

	public void setWmUiHandlingOption(int isEnabled) {
		wmUiHandlingEnabled = isEnabled;
		getConfig().setProperty(WMUIHANDLING_OPTION, Integer.toString(isEnabled));
	}
	
	public int getWmUiHandlingOption() {
		return wmUiHandlingEnabled;
	}

	public boolean getWarnOnTabDoubleClick() {
		return warnOnTabDoubleClick;
	}

	public void setWarnOnTabDoubleClick(boolean warnOnTabDoubleClick) {
		this.warnOnTabDoubleClick = warnOnTabDoubleClick;
		getConfig().setProperty(WARN_ON_TAB_DOUBLE_CLICK_OPTION, warnOnTabDoubleClick);
	}

	public boolean getReveal() {
		return reveal;
	}
	
	public void setReveal(boolean reveal) {
		this.reveal = reveal;
		getConfig().setProperty(REVEAL_OPTION, reveal);
	}
	
	public void setDefaultView(OptionsParamView.ViewType viewType, String view) {
		if (viewType == null) {
			return;
		}
		
		if (viewType == ViewType.req_history) {
			defaultReqViewHistory = view;
			getConfig().setProperty(DEFAULT_VIEW_REQ_HISTORY, view);
		} else if (viewType == ViewType.req_proxy) {
			defaultReqViewProxy = view;
			getConfig().setProperty(DEFAULT_VIEW_REQ_PROXY, view);
		} else if (viewType == ViewType.req_manual) {
			defaultReqViewManual = view;
			getConfig().setProperty(DEFAULT_VIEW_REQ_MANUAL, view);
		} else if (viewType == ViewType.res_history) {
			defaultResViewHistory = view;
			getConfig().setProperty(DEFAULT_VIEW_RES_HISTORY, view);
		} else if (viewType == ViewType.res_proxy) {
			defaultResViewProxy = view;
			getConfig().setProperty(DEFAULT_VIEW_RES_PROXY, view);
		} else if (viewType == ViewType.res_manual) {
			defaultResViewManual = view;
			getConfig().setProperty(DEFAULT_VIEW_RES_MANUAL, view);
		}
	}
	
	public String getDefaultView(OptionsParamView.ViewType viewType) {
		if (viewType == ViewType.req_history) {
			return defaultReqViewHistory;
		} else if (viewType == ViewType.req_proxy) {
			return defaultReqViewProxy;
		} else if (viewType == ViewType.req_manual) {
			return defaultReqViewManual;
		} else if (viewType == ViewType.res_history) {
			return defaultResViewHistory;
		} else if (viewType == ViewType.res_proxy) {
			return defaultResViewProxy;
		} else if (viewType == ViewType.res_manual) {
			return defaultResViewManual;
		}
			
		return "Split";
	}
	
}