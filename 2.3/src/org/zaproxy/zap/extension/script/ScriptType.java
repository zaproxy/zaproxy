package org.zaproxy.zap.extension.script;

import javax.swing.ImageIcon;

public class ScriptType {
	private String name;
	private String i18nKey;
	private ImageIcon icon;
	private boolean isEnableable;
	
	public ScriptType(String name, String i18nKey, ImageIcon icon, boolean isEnableable) {
		super();
		this.name = name;
		this.i18nKey = i18nKey;
		this.icon = icon;
		this.isEnableable = isEnableable;
	}

	public String getName() {
		return name;
	}

	public String getI18nKey() {
		return i18nKey;
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public boolean isEnableable() {
		return isEnableable;
	}
	
	
}
