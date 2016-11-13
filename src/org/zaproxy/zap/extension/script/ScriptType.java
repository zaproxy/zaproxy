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
	
	private final String name;
	private final String i18nKey;
	private final ImageIcon icon;
	private final boolean isEnableable;

	/**
	 * Flag that indicates if the scripts of this script type should be enabled by default.
	 */
	private final boolean enabledByDefault;

	private final Set<String> capabilitiesSet = new HashSet<String>();
	
	/**
	 * Constructs a {@code ScriptType} with the given data.
	 *
	 * @param name the (internal) name of the script type.
	 * @param i18nKey the resource key for the internationalised name.
	 * @param icon the icon of the script type.
	 * @param isEnableable {@code true} if the scripts of the script type can be enabled, {@code false} otherwise.
	 * @see #ScriptType(String, String, ImageIcon, boolean, boolean)
	 */
	public ScriptType(String name, String i18nKey, ImageIcon icon, boolean isEnableable) {
		this(name, i18nKey, icon, isEnableable, false);
	}

	/**
	 * Constructs a {@code ScriptType} with the given data.
	 *
	 * @param name the (internal) name of the script type.
	 * @param i18nKey the resource key for the internationalised name.
	 * @param icon the icon of the script type.
	 * @param enableable {@code true} if the scripts of the script type can be enabled, {@code false} otherwise.
	 * @param enabledByDefault {@code true} if the scripts of the script type should be enabled by default, {@code false}
	 *			otherwise. Ignored if {@code enableable} is false.
	 * @since TODO add version
	 */
	public ScriptType(String name, String i18nKey, ImageIcon icon, boolean enableable, boolean enabledByDefault) {
		this(name, i18nKey, icon, enableable, enabledByDefault, null);
	}

	/**
	 * Constructs a {@code ScriptType} with the given data.
	 *
	 * @param name the (internal) name of the script type.
	 * @param i18nKey the resource key for the internationalised name.
	 * @param icon the icon of the script type.
	 * @param isEnableable {@code true} if the scripts of the script type can be enabled, {@code false} otherwise.
	 * @param capabilities the capabilities of the scripts of the script type.
	 * @see #ScriptType(String, String, ImageIcon, boolean, boolean, String[])
	 */
	public ScriptType(String name, String i18nKey, ImageIcon icon, boolean isEnableable, String[] capabilities) {
		this(name, i18nKey, icon, isEnableable, false, capabilities);
	}

	/**
	 * Constructs a {@code ScriptType} with the given data.
	 *
	 * @param name the (internal) name of the script type.
	 * @param i18nKey the resource key for the internationalised name.
	 * @param icon the icon of the script type.
	 * @param enableable {@code true} if the scripts of the script type can be enabled, {@code false} otherwise.
	 * @param enabledByDefault {@code true} if the scripts should be enabled by default, {@code false} otherwise. Ignored if
	 *			{@code enableable} is false.
	 * @param capabilities the capabilities of the scripts of the script type.
	 * @since TODO add version
	 */
	public ScriptType(String name, String i18nKey, ImageIcon icon, boolean enableable, boolean enabledByDefault,
			String[] capabilities) {
		super();
		this.name = name;
		this.i18nKey = i18nKey;
		this.icon = icon;
		this.isEnableable = enableable;
		this.enabledByDefault = isEnableable ? enabledByDefault : false;
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

	/**
	 * Tells whether or not the scripts of this script type can be enabled/disabled.
	 *
	 * @return {@code true} if the scripts can be enabled/disabled, {@code false} otherwise.
	 * @see #isEnabledByDefault()
	 */
	public boolean isEnableable() {
		return isEnableable;
	}

	/**
	 * Tells whether or not the scripts of this script type should be enabled by default.
	 * <p>
	 * Only meaningful if the script type is {@link #isEnableable() enableable}.
	 * 
	 * @return {@code true} if the scripts should be enabled by default, {@code false} otherwise.
	 * @since TODO add version
	 */
	public boolean isEnabledByDefault() {
		return enabledByDefault;
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
