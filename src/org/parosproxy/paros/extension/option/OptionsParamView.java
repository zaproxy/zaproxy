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
// ZAP: 2012/03/15 Removed the options of the http panels.
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/07/23 Issue 738: Options to hide tabs
// ZAP: 2013/12/13 Added support for optional names in tabs.

package org.parosproxy.paros.extension.option;

import java.util.Locale;

import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.control.Control.Mode;

// ZAP: Added support for selecting the locale

public class OptionsParamView extends AbstractParam {
	
	public static final String BASE_VIEW_KEY = "view";

	private static final String SHOW_TEXT_ICONS = "view.showTextIcons";
	private static final String PROCESS_IMAGES = "view.processImages";
	public static final String LOCALE = "view.locale";
	public static final String LOCALES = "view.locales";
	public static final String DISPLAY_OPTION = "view.displayOption";
	public static final String BRK_PANEL_VIEW_OPTION = "view.brkPanelView";
	public static final String SHOW_MAIN_TOOLBAR_OPTION = "view.showMainToolbar";
	public static final String DEFAULT_LOCALE = "en_GB";
	public static final String ADVANCEDUI_OPTION = "view.advancedview";
	public static final String WMUIHANDLING_OPTION = "view.uiWmHandling";
	public static final String ASKONEXIT_OPTION = "view.askOnExit";
	public static final String WARN_ON_TAB_DOUBLE_CLICK_OPTION = "view.warnOnTabDoubleClick";
	public static final String REVEAL_OPTION = "view.reveal";
	public static final String MODE_OPTION = "view.mode";
	public static final String TAB_OPTION = "view.tab";

	private int advancedViewEnabled = 0;
	private int processImages = 0;
	private int showMainToolbar = 1;
	private String configLocale = "";
	private String locale = "";
	private int displayOption = 0;
	private int brkPanelViewOption = 0;
	private int askOnExitEnabled = 1;
  private int showTextIcons = 1;
	private int wmUiHandlingEnabled = 0;
	private boolean warnOnTabDoubleClick = false;
	private boolean reveal = false;
	private String mode = Mode.standard.name();
	
    public OptionsParamView() {
    }

    @Override
	protected void parse() {
	    // use temp variable to check.  Exception will be flagged if any error.
      showTextIcons = getConfig().getInt(SHOW_TEXT_ICONS, 1);
	    processImages = getConfig().getInt(PROCESS_IMAGES, 0);
	    configLocale = getConfig().getString(LOCALE);	// No default
	    locale = getConfig().getString(LOCALE, DEFAULT_LOCALE);
	    displayOption = getConfig().getInt(DISPLAY_OPTION, 0);
	    brkPanelViewOption = getConfig().getInt(BRK_PANEL_VIEW_OPTION, 0);
	    showMainToolbar = getConfig().getInt(SHOW_MAIN_TOOLBAR_OPTION, 1);
	    advancedViewEnabled = getConfig().getInt(ADVANCEDUI_OPTION, 0);
	    wmUiHandlingEnabled = getConfig().getInt(WMUIHANDLING_OPTION, 0);
	    askOnExitEnabled = getConfig().getInt(ASKONEXIT_OPTION, 1);
	    warnOnTabDoubleClick = getConfig().getBoolean(WARN_ON_TAB_DOUBLE_CLICK_OPTION, true);
	    reveal = getConfig().getBoolean(REVEAL_OPTION, false);
	    mode = getConfig().getString(MODE_OPTION, Mode.standard.name());
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
	 * @see #getConfigLocale()
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
	 * @see #getLocale()
	 */
	public String getConfigLocale() {
		return configLocale;
	}

	public int getTextIcons() {
		return showTextIcons;
	}
	
	public void setShowTextIcons(int brkPanelViewIdx) {
		showTextIcons = brkPanelViewIdx;
		getConfig().setProperty(SHOW_TEXT_ICONS, Integer.toString(showTextIcons));
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

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
		getConfig().setProperty(MODE_OPTION, mode);
	}
	
}
