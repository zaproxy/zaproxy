package org.zaproxy.zap.extension.alert;

import java.awt.Component;

import javax.swing.JTree;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.extension.history.ExtensionHistory;

public class PopupMenuShowAlert extends ExtensionPopupMenuItem implements Comparable<PopupMenuShowAlert> {

	private static final long serialVersionUID = 1L;
	private String parentName;

	private ExtensionHistory extHist = null; 
	private Alert alert = null;

    private static final Logger log = Logger.getLogger(ExtensionPopupMenuItem.class);

	public PopupMenuShowAlert (String name, Alert alert, String parentName) {
		super(name);
		this.alert = alert;
		this.parentName = parentName;
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
    public boolean isEnableForComponent(Component invoker) {
        if (invoker.getName() != null && invoker.getName().equals("ListLog")) {
        	return true;
        } else if (invoker instanceof JTree && invoker.getName().equals("treeSite")) {
        	return true;
        }

        return false;
    }
    
    @Override
    public String getParentMenuName() {
    	return this.parentName;
    }
    
    @Override
    public boolean isSubMenu() {
    	return true;
    }

	@Override
	public int compareTo(PopupMenuShowAlert o) {
		if (o == null) {
			return -1;
		}
		// Negate the alert comparison so higher risks shown first
		return - this.getAlert().compareTo(o.getAlert());
	}

    @Override
    public boolean isSafe() {
    	return true;
    }
}
