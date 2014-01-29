package org.zaproxy.zap.view;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.parosproxy.paros.Constant;

public class ZapMenuItem extends JMenuItem {
	
	private static final long serialVersionUID = 1L;
	
	private String identifier;
	private KeyStroke defaultAccelerator = null;

	/**
	 * Constructor - use this in preference to the other constructor as it supports mnemonics 
	 * @param i18nKey
	 * @param defaultAccelerator
	 */
	public ZapMenuItem(String i18nKey, KeyStroke defaultAccelerator) {
		this(i18nKey, Constant.messages.getString(i18nKey), defaultAccelerator);
		// This will handle missing i18n keys ok
		this.setMnemonic(Constant.messages.getChar(i18nKey + ".mnemonic"));
	}

	/**
	 * Use the ZapMenuItem(String i18nKey, KeyStroke defaultAccelerator) constructor in preference to this one,
	 * however this is useful if you cannot easily access the original i18nkey
	 * @param identifier
	 * @param text
	 * @param defaultAccelerator
	 */
	public ZapMenuItem(String identifier, String text, KeyStroke defaultAccelerator) {
		super(text);
		this.identifier = identifier;
		this.defaultAccelerator = defaultAccelerator;
		
		if (defaultAccelerator != null) {
			// Note that this can be overriden by the Keyboard extension 
			this.setAccelerator(defaultAccelerator);
		}
	}

	public ZapMenuItem(String i18nKey) {
		this(i18nKey, null);
	}
	
	public String getIdenfifier() {
		return this.identifier;
	}
	
	public void resetAccelerator() {
		this.setAccelerator(this.defaultAccelerator);
	}

	public KeyStroke getDefaultAccelerator() {
		return defaultAccelerator;
	}
	
}
