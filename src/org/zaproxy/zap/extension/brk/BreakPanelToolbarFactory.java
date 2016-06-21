package org.zaproxy.zap.extension.brk;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.view.TabbedPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.TabbedPanel2;
import org.zaproxy.zap.view.ZapToggleButton;

// Button notes
// BreakRequest button, if set all requests trapped
// BreakResponse button, ditto for responses
// If break point hit, Break tab gets focus and icon goes red
// Step button, only if break point hit, submits just this req/resp, breaks on next
// Continue button, only if break point hit, submits this req/resp and continues until next break point hit
// If BreakReq & Resp both selected Step and Continue buttons have same effect
// 

public class BreakPanelToolbarFactory {

	private ContinueButtonAction continueButtonAction;
	private StepButtonAction stepButtonAction;
	private DropButtonAction dropButtonAction;
	private AddBreakpointButtonAction addBreakpointButtonAction;

	private BreakRequestsButtonAction breakRequestsButtonAction;
	private BreakResponsesButtonAction breakResponsesButtonAction;
	private BreakAllButtonAction breakAllButtonAction;

	private boolean cont = false;
	private boolean step = false;
	private boolean stepping = false;
	private boolean toBeDropped = false;
	private boolean isBreakRequest = false;
	private boolean isBreakResponse = false;
	private boolean isBreakAll = false;
	
	private BreakPanel breakPanel = null;
	
	private BreakpointsParam breakpointsParams;
	private int mode = 0;

	public BreakPanelToolbarFactory(BreakpointsParam breakpointsParams, BreakPanel breakPanel) {
		super();

		continueButtonAction = new ContinueButtonAction();
		stepButtonAction = new StepButtonAction();
		dropButtonAction = new DropButtonAction();
		addBreakpointButtonAction = new AddBreakpointButtonAction();

		breakRequestsButtonAction = new BreakRequestsButtonAction();
		breakResponsesButtonAction = new BreakResponsesButtonAction();
		breakAllButtonAction = new BreakAllButtonAction();

		this.breakpointsParams = breakpointsParams;
		this.breakPanel = breakPanel;
	}

	private void setActiveIcon(boolean active) {
		if (active) {
			// Have to do this before the getParent() call
			breakPanel.setTabFocus();
		}
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
			if (parent instanceof TabbedPanel2) {
				// If possible lock the tab while it is active so it cant be closed
				((TabbedPanel2)parent).setTabLocked(breakPanel, !active);
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
		return isBreakRequest || isBreakAll;
	}

	public boolean isBreakResponse() {
		return isBreakResponse || isBreakAll;
	}
	
	public boolean isBreakAll() {
		return isBreakAll;
	}
	
	public JButton getBtnStep() {
		return new JButton(stepButtonAction);
	}

	    
	public JButton getBtnContinue() {
		return new JButton(continueButtonAction);
	}

	    
	public JButton getBtnDrop() {
		return new JButton(dropButtonAction);
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
		ZapToggleButton btnBreakRequest;

		btnBreakRequest = new ZapToggleButton(breakRequestsButtonAction);
		btnBreakRequest.setSelectedIcon(
				new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/105r.png")));
		btnBreakRequest.setSelectedToolTipText(Constant.messages.getString("brk.toolbar.button.request.unset"));

		return btnBreakRequest;
	}

	 
	public JToggleButton getBtnBreakResponse() {
		ZapToggleButton btnBreakResponse;

		btnBreakResponse = new ZapToggleButton(breakResponsesButtonAction);
		btnBreakResponse.setSelectedIcon(
				new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/106r.png")));
		btnBreakResponse.setSelectedToolTipText(Constant.messages.getString("brk.toolbar.button.response.unset"));

		return btnBreakResponse;
	}

	public JToggleButton getBtnBreakAll() {
		ZapToggleButton btnBreakAll;

		btnBreakAll = new ZapToggleButton(breakAllButtonAction);
		btnBreakAll.setSelectedIcon(
				new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/151.png")));
		btnBreakAll.setSelectedToolTipText(Constant.messages.getString("brk.toolbar.button.all.unset"));

		return btnBreakAll;
	}

	public JButton getBtnBreakPoint() {
		return new JButton(addBreakpointButtonAction);
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
		if (forceSerialize || isBreakRequest() || isBreakResponse() || isBreakAll) {
			Control.getSingleton().getProxy().setSerialize(true);
		} else {
			Control.getSingleton().getProxy().setSerialize(false);
		}
	}

	public void setBreakRequest(boolean brk) {
		isBreakRequest = brk;
		resetRequestSerialization(false);

		breakRequestsButtonAction.setSelected(isBreakRequest);
	}

	public void setBreakResponse(boolean brk) {
		isBreakResponse = brk;
		resetRequestSerialization(false);

		breakResponsesButtonAction.setSelected(isBreakResponse);
	}

	public void setBreakAll(boolean brk) {
		isBreakAll = brk;
		if (!brk) {
			stepping = false;
		}

		resetRequestSerialization(false);

		breakAllButtonAction.setSelected(isBreakAll);
	}

	private void toggleBreakRequest() {
		setBreakRequest(!isBreakRequest);
	}

	private void toggleBreakResponse() {
		setBreakResponse(!isBreakResponse);
	}

	private void toggleBreakAll() {
		setBreakAll(!isBreakAll);
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
			this.isBreakAll = false;
			this.setContinue(true);
		}
		breakRequestsButtonAction.setSelected(false);
		breakRequestsButtonAction.setEnabled(enabled);
		
		breakResponsesButtonAction.setSelected(false);
		breakResponsesButtonAction.setEnabled(enabled);
		
		breakAllButtonAction.setSelected(false);
		breakAllButtonAction.setEnabled(enabled);
	}

	
	protected void setContinue(boolean isContinue) {
		this.cont = isContinue;

		stepButtonAction.setEnabled( ! isContinue);

		continueButtonAction.setEnabled( ! isContinue);

		dropButtonAction.setEnabled( ! isContinue);

		if (isContinue) {
			this.setActiveIcon(false);
		}
	}

	protected void setStep(boolean isStep) {
		step = isStep;

		stepButtonAction.setEnabled( ! isStep);

		continueButtonAction.setEnabled( ! isStep);

		dropButtonAction.setEnabled( ! isStep);

		if (isStep) {
			this.setActiveIcon(false);
		}
	}
	
	protected void drop() {
        if (breakpointsParams.isConfirmDropMessage() && askForDropConfirmation() != JOptionPane.OK_OPTION) {
            return;
        }
        toBeDropped = true;
        setContinue(true);
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
		isBreakAll = false;
	}

	public void reset() {
		if (isBreakRequest()) {
			toggleBreakRequest();
		}

		if (isBreakResponse()) {
			toggleBreakResponse();
		}

		if (isBreakAll()) {
			toggleBreakAll();
		}

		toBeDropped = true;
		setContinue(true);
	}
	
	/**
	 * Sets the current button mode.
	 * <p>
	 * If the mode is already set no change is done, otherwise it does the following:
	 * <ul>
	 * <li>When changing from {@link BreakpointsParam#BUTTON_MODE_SIMPLE BUTTON_MODE_SIMPLE} to
	 * {@link BreakpointsParam#BUTTON_MODE_DUAL BUTTON_MODE_DUAL} set "break on request" and "on response" enabled and
	 * "break on all" disabled, if "break on all" is enabled;</li>
	 * <li>When changing from {@code BUTTON_MODE_DUAL} to {@code BUTTON_MODE_SIMPLE} set "break on all" enabled and "break on
	 * request" and "on response" disabled, if at least one of "break on request" and "on response" is enabled;</li>
	 * <li>If none of the "break on ..." states is enabled there's no changes in its states.</li>
	 * </ul>
	 * The enabled state of previous mode is disabled to prevent interferences between the modes.
	 *
	 * @param mode the mode to be set
	 * @see #isBreakAll()
	 * @see #isBreakRequest()
	 * @see #isBreakResponse()
	 */
	public void setButtonMode (int mode) {
		if (this.mode == mode) {
			return;
		}
		if (this.mode == BreakpointsParam.BUTTON_MODE_SIMPLE) {
			if (isBreakAll) {
				setBreakAll(false);
				setBreakRequest(true);
				setBreakResponse(true);
			}
		} else if (isBreakRequest || isBreakResponse) {
			setBreakRequest(false);
			setBreakResponse(false);
			setBreakAll(true);
		}
		this.mode = mode;
	}

    private class ContinueButtonAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public ContinueButtonAction() {
            super(null, new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/131.png")));
            putValue(Action.SHORT_DESCRIPTION, Constant.messages.getString("brk.toolbar.button.cont"));

            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setContinue(true);
           	setBreakAll(false);
           	setBreakRequest(false);
           	setBreakResponse(false);
        }
    }

    private class StepButtonAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public StepButtonAction() {
            super(null, new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/143.png")));
            putValue(Action.SHORT_DESCRIPTION, Constant.messages.getString("brk.toolbar.button.step"));

            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (mode == BreakpointsParam.BUTTON_MODE_SIMPLE && ! isBreakAll) {
            	// In simple mode 'step' if the breakAll button is disabled then it acts like 'continue'
            	// so that its hopefully obvious to users when break is on or not
                setContinue(true);
            } else {
                setStep(true);
            }
        }
    }

    private class DropButtonAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public DropButtonAction() {
            super(null, new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/150.png")));
            putValue(Action.SHORT_DESCRIPTION, Constant.messages.getString("brk.toolbar.button.bin"));

            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            drop();
        }
    }

    private class AddBreakpointButtonAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public AddBreakpointButtonAction() {
            super(null, new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/break_add.png")));
            putValue(Action.SHORT_DESCRIPTION, Constant.messages.getString("brk.toolbar.button.brkpoint"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            breakPanel.showNewBreakPointDialog();
        }
    }

    private class BreakRequestsButtonAction extends SelectableAbstractAction {

        private static final long serialVersionUID = 1L;

        public BreakRequestsButtonAction() {
            super(null, new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/105.png")));
            putValue(Action.SHORT_DESCRIPTION, Constant.messages.getString("brk.toolbar.button.request.set"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleBreakRequest();
        }
    }

    private class BreakResponsesButtonAction extends SelectableAbstractAction {

        private static final long serialVersionUID = 1L;

        public BreakResponsesButtonAction() {
            super(null, new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/106.png")));
            putValue(Action.SHORT_DESCRIPTION, Constant.messages.getString("brk.toolbar.button.response.set"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleBreakResponse();
        }
    }

    private class BreakAllButtonAction extends SelectableAbstractAction {

        private static final long serialVersionUID = 1L;

        public BreakAllButtonAction() {
            super(null, new ImageIcon(BreakPanelToolbarFactory.class.getResource("/resource/icon/16/152.png")));
            putValue(Action.SHORT_DESCRIPTION, Constant.messages.getString("brk.toolbar.button.all.set"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleBreakAll();
        }
    }

    /**
     * An {@code AbstractAction} which allows to be selected.
     * 
     * @see AbstractAction
     * @see #setSelected(boolean)
     */
    private static abstract class SelectableAbstractAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        /**
         * Creates a {@code SelectableAbstractAction} with the specified {@code name} and {@code icon}.
         * 
         * @param name the name for the action or {@code null} for no name
         * @param icon the icon for the action or {@code null} for no icon
         */
        public SelectableAbstractAction(String name, Icon icon) {
            super(name, icon);
        }

        /**
         * Sets whether the action is selected or not.
         * 
         * @param selected {@code true} if the action should be selected, {@code false} otherwise
         */
        public void setSelected(boolean selected) {
            putValue(Action.SELECTED_KEY, Boolean.valueOf(selected));
        }
    }

}
