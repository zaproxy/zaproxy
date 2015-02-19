package org.zaproxy.zap.extension.script;

import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;

public class ScriptType {
	
	/**
	 * Script types with the CAPABILITY_APPEND can sensibly have requests appended to them. 
	 * Typically this will just apply to Zest scripts
	 */
	public static final String CAPABILITY_APPEND = "append";
	
	private String name;
	private String i18nKey;
	private ImageIcon icon;
	private boolean isEnableable;
	private Set<String> capabilitiesSet = new HashSet<String>();
	
	public ScriptType(String name, String i18nKey, ImageIcon icon, boolean isEnableable) {
		this(name, i18nKey, icon, isEnableable, null);
	}

	public ScriptType(String name, String i18nKey, ImageIcon icon, boolean isEnableable, String[] capabilities) {
		super();
		this.name = name;
		this.i18nKey = i18nKey;
		this.icon = icon;
		this.isEnableable = isEnableable;
		if (capabilities != null) {
			for (String capability : capabilities) {
				this.capabilitiesSet.add(capability);
			}
		}
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
	
	public void addCapability(String capability) {
		this.capabilitiesSet.add(capability);
	}

	/*
	 * Returns true if the script type has the specified capability
	 */
	public boolean hasCapability(String capability) {
		return this.capabilitiesSet.contains(capability);
	}
	
}
