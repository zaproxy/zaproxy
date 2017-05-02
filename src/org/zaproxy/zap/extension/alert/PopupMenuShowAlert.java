package org.zaproxy.zap.extension.alert;

import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;

public class PopupMenuShowAlert extends JMenuItem implements Comparable<PopupMenuShowAlert> {

	private static final long serialVersionUID = 1L;

	private final ExtensionAlert extension;
	private final Alert alert;

    private static final Logger log = Logger.getLogger(ExtensionPopupMenuItem.class);

	public PopupMenuShowAlert (String name, ExtensionAlert extension, Alert alert) {
		super(name);
		this.alert = alert;
		this.extension = extension;

        this.addActionListener(new java.awt.event.ActionListener() { 

        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent e) {
        	    try {
        	        PopupMenuShowAlert.this.extension.showAlertEditDialog(PopupMenuShowAlert.this.alert);
				} catch (Exception e2) {
					log.error(e2.getMessage(), e2);
				}
        	}
        });
	}

	@Override
	public int compareTo(PopupMenuShowAlert o) {
		if (o == null) {
			return -1;
		}
		// Negate the alert comparison so higher risks shown first
		return - alert.compareTo(o.alert);
	}
}
