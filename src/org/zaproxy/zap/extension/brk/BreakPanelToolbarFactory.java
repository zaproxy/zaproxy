package org.zaproxy.zap.extension.brk;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.view.TabbedPanel;
import org.parosproxy.paros.view.View;

// Button notes
// BreakRequest button, if set all requests trapped
// BreakResponse button, ditto for responses
// If break point hit, Break tab gets focus and icon goes red
// Step button, only if break point hit, submits just this req/resp, breaks on next
// Continue button, only if break point hit, submits this req/resp and continues until next break point hit
// If BreakReq & Resp both selected Step and Continue buttons have same effect
// 

public class BreakPanelToolbarFactory {

	private LinkedList<JButton> btnContinueList = new LinkedList<>();
	private LinkedList<JButton> btnStepList = new LinkedList<>();
	private LinkedList<JButton> btnDropList = new LinkedList<>();
	private LinkedList<JButton> btnBrkPointList = new LinkedList<>();

	private LinkedList<JToggleButton> btnBreakRequestList = new LinkedList<>();
	private LinkedList<JToggleButton> btnBreakResponseList = new LinkedList<>();

	private boolean cont = false;
	private boolean step = false;
	private boolean stepping = false;
	private boolean toBeDropped = false;
	private boolean isBreakRequest = false;
	private boolean isBreakResponse = false;
	
	private BreakPanel breakPanel = null;
	
	private BreakpointsParam breakpointsParams;

	public BreakPanelToolbarFactory(BreakpointsParam breakpointsParams, BreakPanel breakPanel) {
		super();

		this.breakpointsParams = breakpointsParams;
		this.breakPanel = breakPanel;
	}

	private void setActiveIcon(boolean active) {
		if (breakPanel.getParent() instanceof TabbedPanel) {
			TabbedPanel parent = (TabbedPanel) breakPanel.getParent();
			if (active) {
				parent.setIconAt(
						parent.indexOfComponent(breakPanel),
						new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/101.png")));	// Red X
			} else {
				parent.setIconAt(
						parent.indexOfComponent(breakPanel), 
						new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/101grey.png")));	// Grey X
			}
		}
	}

	public void breakpointHit () {
		// This could have been via a break point, so force the serialisation
		resetRequestSerialization(true);

		// Set the active icon and reset the continue button
		this.setActiveIcon(true);
		setContinue(false);
	}

	public boolean isBreakRequest() {
		return isBreakRequest;
	}

	public boolean isBreakResponse() {
		return isBreakResponse;
	}
	
	public JButton getBtnStep() {
		JButton btnStep;

		btnStep = new JButton();
		btnStep.setIcon(new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/143.png")));
		btnStep.setToolTipText(Constant.messages.getString("brk.toolbar.button.step"));
		btnStep.addActionListener(new ActionListener() { 
			@Override
			public void actionPerformed(ActionEvent e) {    
				setStep(true);
			}
		});
		// Default to disabled
		btnStep.setEnabled(false);

		btnStepList.add(btnStep);
		return btnStep;
	}

	    
	public JButton getBtnContinue() {
		JButton btnContinue;

		btnContinue = new JButton();
		btnContinue.setIcon(new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/131.png")));
		btnContinue.setToolTipText(Constant.messages.getString("brk.toolbar.button.cont"));
		btnContinue.addActionListener(new ActionListener() { 
			@Override
			public void actionPerformed(ActionEvent e) {
				setContinue(true);
			}
		});
		// Default to disabled
		btnContinue.setEnabled(false);

		btnContinueList.add(btnContinue);	
		return btnContinue;
	}

	    
	public JButton getBtnDrop() {
		JButton btnDrop;

		btnDrop = new JButton();
		btnDrop.setIcon(new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/150.png")));
		btnDrop.setToolTipText(Constant.messages.getString("brk.toolbar.button.bin"));
		btnDrop.addActionListener(new ActionListener() { 
			@Override
			public void actionPerformed(ActionEvent e) {
				if (breakpointsParams.isConfirmDropMessage() && askForDropConfirmation() != JOptionPane.OK_OPTION) {
					return;
				}
				toBeDropped = true;
				setContinue(true);
			}
		});
		// Default to disabled
		btnDrop.setEnabled(false);

		btnDropList.add(btnDrop);

		return btnDrop;
	}
	
	private int askForDropConfirmation() {
		String title = Constant.messages.getString("brk.dialogue.confirmDropMessage.title");
		String message = Constant.messages.getString("brk.dialogue.confirmDropMessage.message");
		JCheckBox checkBox = new JCheckBox(Constant.messages.getString("brk.dialogue.confirmDropMessage.option.dontAskAgain"));
		String confirmButtonLabel = Constant.messages.getString("brk.dialogue.confirmDropMessage.button.confirm.label");
		String cancelButtonLabel = Constant.messages.getString("brk.dialogue.confirmDropMessage.button.cancel.label");
		
		int option = JOptionPane.showOptionDialog(View.getSingleton().getMainFrame(),
				new Object[]  {message, " ", checkBox}, title,
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, new String[] { confirmButtonLabel, cancelButtonLabel}, null);
		
		if (checkBox.isSelected()) {
			breakpointsParams.setConfirmDropMessage(false);
		}
		
		return option;
	}
	
	public JToggleButton getBtnBreakRequest() {
		JToggleButton btnBreakRequest;

		btnBreakRequest = new JToggleButton();
		isBreakRequest = false;

		btnBreakRequest.addActionListener(new ActionListener() { 
			@Override
			public void actionPerformed(ActionEvent e) {    
				// Toggle button
				toggleBreakRequest();
			}
		});

		btnBreakRequestList.add(btnBreakRequest);
		updateBreakRequestBtn();

		return btnBreakRequest;
	}

	 
	public JToggleButton getBtnBreakResponse() {
		JToggleButton btnBreakResponse;

		btnBreakResponse = new JToggleButton();
		isBreakResponse = false;

		btnBreakResponse.addActionListener(new ActionListener() { 
			@Override
			public void actionPerformed(ActionEvent e) {    
				// Toggle button
				toggleBreakResponse();
			}
		});

		btnBreakResponseList.add(btnBreakResponse);
		updateBreakResponseBtn();

		return btnBreakResponse;
	}


	public JButton getBtnBreakPoint() {
		JButton btnBreakPoint;

		btnBreakPoint = new JButton();
		btnBreakPoint.setIcon(new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/break_add.png")));
		btnBreakPoint.setToolTipText(Constant.messages.getString("brk.toolbar.button.brkpoint"));
		btnBreakPoint.addActionListener(new ActionListener() { 
			@Override
			public void actionPerformed(ActionEvent e) {
				breakPanel.showNewBreakPointDialog();
			}
		});

		btnBrkPointList.add(btnBreakPoint);

		return btnBreakPoint;
	}

	public boolean isStepping() {
		return stepping;
	}


	private void resetRequestSerialization (boolean forceSerialize) {
		if (Control.getSingleton() == null) {
			// Still in setup
			return;
		}
		// If forces or either break buttons are pressed force the proxy to submit requests and responses serially 
		if (forceSerialize || isBreakRequest() || isBreakResponse()) {
			Control.getSingleton().getProxy().setSerialize(true);
		} else {
			Control.getSingleton().getProxy().setSerialize(true);
		}
	}

	public void setBreakRequest(Boolean brk) {
		isBreakRequest = brk;
		resetRequestSerialization(false);

		updateBreakRequestBtn();
	}

	public void setBreakResponse(Boolean brk) {
		isBreakResponse = brk;
		resetRequestSerialization(false);

		updateBreakRequestBtn();
	}

	private void toggleBreakRequest() {
		isBreakRequest = !isBreakRequest();
		resetRequestSerialization(false);

		updateBreakRequestBtn();
	}

	private void toggleBreakResponse() {
		isBreakResponse = !isBreakResponse();
		resetRequestSerialization(false);

		updateBreakResponseBtn();
	}

	private void updateBreakRequestBtn() {
		if (isBreakRequest()) {
			for(JToggleButton btnBreakRequest: btnBreakRequestList) {
				btnBreakRequest.setIcon(new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/105r.png")));
				btnBreakRequest.setToolTipText(Constant.messages.getString("brk.toolbar.button.request.unset"));
				btnBreakRequest.setSelected(true);
			}
		} else {
			for(JToggleButton btnBreakRequest: btnBreakRequestList) {
				btnBreakRequest.setIcon(new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/105.png")));
				btnBreakRequest.setToolTipText(Constant.messages.getString("brk.toolbar.button.request.set"));
				btnBreakRequest.setSelected(false);
			}
		}
	}

	private void updateBreakResponseBtn() {
		if (isBreakResponse()) {
			for(JToggleButton btnBreakResponse: btnBreakResponseList) {
				btnBreakResponse.setIcon(new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/106r.png")));
				btnBreakResponse.setToolTipText(Constant.messages.getString("brk.toolbar.button.response.unset"));
				btnBreakResponse.setSelected(true);
			}
		} else {
			for(JToggleButton btnBreakResponse: btnBreakResponseList) {
				btnBreakResponse.setIcon(new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/106.png")));
				btnBreakResponse.setToolTipText(Constant.messages.getString("brk.toolbar.button.response.set"));
				btnBreakResponse.setSelected(false);
			}
		}
	}


	
	public boolean isHoldMessage() {
		if (step) {
			// Only works one time, until its pressed again
			stepping = true;
			step = false;
			return false;
		}
		if (cont) {
			// They've pressed the continue button, stop stepping
			stepping = false;
			resetRequestSerialization(false);
			return false;
		}
		return true;
	}

	public boolean isContinue() {
		return cont;
	}
	
	public void setBreakEnabled(boolean enabled) {
		if (!enabled) {
			this.isBreakRequest = false;
			this.isBreakResponse = false;
			this.setContinue(true);
		}
		for(JToggleButton btnBreakRequest: btnBreakRequestList) {
			btnBreakRequest.setIcon(new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/105.png")));
			btnBreakRequest.setToolTipText(Constant.messages.getString("brk.toolbar.button.request.set"));
			btnBreakRequest.setSelected(false);
			btnBreakRequest.setEnabled(enabled);
		}
		for(JToggleButton btnBreakResponse: btnBreakResponseList) {
			btnBreakResponse.setIcon(new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/106.png")));
			btnBreakResponse.setToolTipText(Constant.messages.getString("brk.toolbar.button.response.set"));
			btnBreakResponse.setSelected(false);
			btnBreakResponse.setEnabled(enabled);
		}
	}

	
	private void setContinue(boolean isContinue) {
		this.cont = isContinue;


		for(JButton btnStep: btnStepList) {
			btnStep.setEnabled( ! isContinue);
		}

		for(JButton btnContinue: btnContinueList) {
			btnContinue.setEnabled( ! isContinue);
		}

		for(JButton btnDrop: btnDropList) {
			btnDrop.setEnabled( ! isContinue);
		}

		if (isContinue) {
			this.setActiveIcon(false);
		}
	}

	private void setStep(boolean isStep) {
		step = isStep;

		for(JButton btnStep: btnStepList) {
			btnStep.setEnabled( ! isStep);
		}

		for(JButton btnContinue: btnContinueList) {
			btnContinue.setEnabled( ! isStep);
		}

		for(JButton btnDrop: btnDropList) {
			btnDrop.setEnabled( ! isStep);
		}

		if (isStep) {
			this.setActiveIcon(false);
		}
	}

	public boolean isToBeDropped() {
		boolean drop = toBeDropped;
		toBeDropped = false;
		return drop;
	}

	public void init() {
		cont = false;
		step = false;
		stepping = false;
		toBeDropped = false;
		isBreakRequest = false;
		isBreakResponse = false;
	}

	public void reset() {
		if (isBreakRequest()) {
			toggleBreakRequest();
		}

		if (isBreakResponse()) {
			toggleBreakResponse();
		}

		toBeDropped = true;
		setContinue(true);
	}

}
