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
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2014/01/22 Issue 996: Ensure all dialogs close when the escape key is pressed
// ZAP: 2014/10/31 Issue 1176: Changed owner to Window as part of spider advanced dialog changes
// ZAP: 2014/11/06 Set ZAP icons
// ZAP: 2015/02/10 Issue 1528: Support user defined font size
// ZAP: 2015/09/07 Move icon loading to a utility class

package org.parosproxy.paros.extension;


import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.DisplayUtils;


/**
 * Abstract base class for all dialog box.
 */
public abstract class AbstractDialog extends JDialog {
 
	private static final long serialVersionUID = -3951504408180103696L;

	protected AbstractDialog thisDialog = null;
    
    /**
	 * Constructs an {@code AbstractDialog} with no owner and not modal.
	 * 
	 * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
	 */
	public AbstractDialog() throws HeadlessException {
		super();
		initialize();
	}

	/**
	 * Constructs an {@code AbstractDialog} with the given owner and whether or not it's modal.
	 * 
	 * @param owner the {@code Frame} from which the dialog is displayed
	 * @param modal {@code true} if the dialogue should be modal, {@code false} otherwise
	 * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
	 */
	public AbstractDialog(Frame owner, boolean modal) {
		super(owner, modal);
		initialize();
	}

	/**
	 * Constructs an {@code AbstractDialog} with the given owner and whether or not it's modal.
	 * 
	 * @param owner the {@code Window} from which the dialog is displayed or {@code null} if this dialog has no owner
	 * @param modal {@code true} if the dialogue should be modal, {@code false} otherwise
	 * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
	 */
	public AbstractDialog(Window owner, boolean modal) {
		super(owner, Dialog.ModalityType.APPLICATION_MODAL);
		this.setModal(modal);
		initialize();
	}


	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setVisible(false);
		this.setIconImages(DisplayUtils.getZapIconImages());
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(300,200);
	    }
		this.setTitle(Constant.PROGRAM_NAME);
		
        //  Handle escape key to close the dialog    
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        AbstractAction escapeAction = new AbstractAction() {
			private static final long serialVersionUID = 3516424501887406165L;
			@Override
            public void actionPerformed(ActionEvent e) {
				dispatchEvent(new WindowEvent(AbstractDialog.this, WindowEvent.WINDOW_CLOSING));
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE",escapeAction);
	}

	/**
	 * Centres this dialog on the main fame.
	 * This is needed, because when using multiple monitors.
	 * Additionally, it will shrink the size of the dialog to fit the screen.
	 */
	public void centreDialog() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		// shrink dialog to fit screen if necessary
		frameSize.height = Math.min(frameSize.height,screenSize.height);
		frameSize.width  = Math.min(frameSize.width, screenSize.width);
		// centres the dialog on main frame 
		final Rectangle mainrect = View.getSingleton().getMainFrame().getBounds();
		int x = mainrect.x + (mainrect.width - frameSize.width) / 2;
		int y = mainrect.y + (mainrect.height - frameSize.height) / 2;
		// finally set the new location
	    this.setLocation(x, y);
	}
	
	@Override
	public void setVisible(boolean show) {
	    if (show) {
		    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
		    	centreDialog();
		    }
	    }
	    super.setVisible(show);
	}
}
