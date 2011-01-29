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

package org.parosproxy.paros.extension.option;

import org.parosproxy.paros.common.AbstractParam;

public class OptionsParamView extends AbstractParam {

	private static final String PROCESS_IMAGES = "view.processImages";
	// ZAP: Added support fr selecting the locale
	public static final String LOCALE = "view.locale";
	public static final String LOCALES = "view.locales";
	public static final String DISPLAY_OPTION = "view.displayOption";
	public static final String EDITORVIEW_OPTION = "view.editorView";
	public static final String BRK_PANEL_VIEW_OPTION = "view.brkPanelView";
	public static final String SHOW_MAIN_TOOLBAR_OPTION = "view.showMainToolbar";
	public static final String DEFAULT_LOCALE = "en_GB";
	//private static final String[] DEFAULT_LOCALES = {"en_GB", "de_DE", "es_ES", "pt_BR", "pl_PL"};

	private int editorViewOption;
	private int processImages = 0;
	private int showMainToolbar = 1;
	private String configLocale = "";
	private String locale = "";
	private int displayOption = 0;
	private int brkPanelViewOption = 0;
	
    /**
     * @param rootElementName
     */
    public OptionsParamView() {
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.common.FileXML#parse()
     */
    protected void parse() {
	    // use temp variable to check.  Exception will be flagged if any error.
	    processImages = getConfig().getInt(PROCESS_IMAGES, 0);
	    configLocale = getConfig().getString(LOCALE);	// No default
	    locale = getConfig().getString(LOCALE, DEFAULT_LOCALE);
	    displayOption = getConfig().getInt(DISPLAY_OPTION, 0);
	    editorViewOption = getConfig().getInt(EDITORVIEW_OPTION, 2);
	    brkPanelViewOption = getConfig().getInt(BRK_PANEL_VIEW_OPTION, 0);
	    showMainToolbar = getConfig().getInt(SHOW_MAIN_TOOLBAR_OPTION, 1);
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

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
		getConfig().setProperty(LOCALE, locale);
	}

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
	
}
