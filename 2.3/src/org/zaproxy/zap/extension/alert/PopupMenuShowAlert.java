package org.zaproxy.zap.extension.alert;

import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.extension.history.ExtensionHistory;

public class PopupMenuShowAlert extends JMenuItem implements Comparable<PopupMenuShowAlert> {

	private static final long serialVersionUID = 1L;

	private ExtensionHistory extHist = null; 
	private Alert alert = null;

    private static final Logger log = Logger.getLogger(ExtensionPopupMenuItem.class);

	public PopupMenuShowAlert (String name, Alert alert) {
		super(name);
		this.alert = alert;
		this.initialize();
	}
	
	private void initialize() {

        this.addActionListener(new java.awt.event.ActionListener() { 

        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent e) {
        	    try {
        			if (extHist == null) {
        				extHist = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.NAME);
        			}
        			if (extHist != null) {
        				extHist.showAlertAddDialog(alert);
        			}
				} catch (Exception e2) {
					log.error(e2.getMessage(), e2);
				}
        	}
        });
	}

	public Alert getAlert() {
		return alert;
	}

	@Override
	public int compareTo(PopupMenuShowAlert o) {
		if (o == null) {
			return -1;
		}
		// Negate the alert comparison so higher risks shown first
		return - this.getAlert().compareTo(o.getAlert());
	}
}
