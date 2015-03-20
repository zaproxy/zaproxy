package org.zaproxy.zap.view;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.Model;

public class TabbedPanelTab extends JPanel {

	private static final Icon CLOSE_TAB_GREY_ICON = new ImageIcon(
			TabbedPanel2.class.getResource("/resource/icon/fugue/cross-small-grey.png"));
	private static final Icon CLOSE_TAB_RED_ICON = new ImageIcon(
			TabbedPanel2.class.getResource("/resource/icon/fugue/cross-small-red.png"));
	private static final Icon PIN_TAB_GREY_ICON = new ImageIcon(
			TabbedPanel2.class.getResource("/resource/icon/fugue/pin-small-grey.png"));
	private static final Icon PIN_TAB_LIGHT_GREY_ICON = new ImageIcon(
			TabbedPanel2.class.getResource("/resource/icon/fugue/pin-small-ltgrey.png"));
	private static final Icon PIN_TAB_RED_ICON = new ImageIcon(
			TabbedPanel2.class.getResource("/resource/icon/fugue/pin-small-red.png"));

	private static final long serialVersionUID = 1L;

	private JButton btnPin = new JButton();
	private JButton btnClose = new JButton();
	private AbstractPanel ap = null;

	public TabbedPanelTab(final TabbedPanel2 parent, String title, Icon icon, final Component c, 
			boolean hideable, boolean isPinned) {
		super(new FlowLayout(FlowLayout.CENTER, 0, 0));

		this.setOpaque(false);

        // change the title variable if 'Options - Display - show tab names' selected
		if (!Model.getSingleton().getOptionsParam().getViewParam().getShowTabNames()) {
            title = "";
        }
		if (c.getName() == null) {
			c.setName(title);
		}

		// Add a JLabel with title and the left-side tab icon
		JLabel lblTitle = new JLabel(title);
		lblTitle.setIcon(icon);

		this.add(lblTitle);
		
		if (hideable) {
			// The buttons only make sense if the tab can be hidden
			if (c instanceof AbstractPanel) {
				ap = (AbstractPanel) c;
				ap.setPinned(isPinned);
				btnPin.setOpaque(false);
	
				// Configure icon and rollover icon for button
				btnPin.setRolloverEnabled(true);
				btnPin.setToolTipText(Constant.messages.getString("all.button.pin"));
				btnPin.setContentAreaFilled(false);
				
				if (ap.isPinned()) {
					btnPin.setIcon(PIN_TAB_GREY_ICON);
					btnPin.setRolloverIcon(PIN_TAB_RED_ICON);
				} else {
					btnPin.setIcon(PIN_TAB_LIGHT_GREY_ICON);
					btnPin.setRolloverIcon(PIN_TAB_RED_ICON);
				}
				// Set a border only on the left side so the button doesn't make the tab too big
				btnPin.setBorder(new EmptyBorder(0, 6, 0, 0));
				// This is needed to Macs for some reason
				btnPin.setBorderPainted(false);
	
				// Make sure the button can't get focus, otherwise it looks funny
				btnPin.setFocusable(false);
				
				// All buttons start off hidden and disabled - they are enabled when the tab is selected
				btnPin.setEnabled(false);
				btnPin.setVisible(false);
	
				// Add the listener that removes the tab
				ActionListener pinListener = new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// Toggle the state
						setPinned(!ap.isPinned());
						parent.saveTabState(ap);
					}
				};
				btnPin.addActionListener(pinListener);
	
				this.add(btnPin);
			}

			// Create a JButton for the close tab button
			btnClose.setOpaque(false);
	
			// Configure icon and rollover icon for button
			btnClose.setRolloverIcon(CLOSE_TAB_RED_ICON);
			btnClose.setRolloverEnabled(true);
			btnClose.setContentAreaFilled(false);
			btnClose.setToolTipText(Constant.messages.getString("all.button.close"));
			btnClose.setIcon(CLOSE_TAB_GREY_ICON);
			// Set a border only on the left side so the button doesn't make the tab too big
			btnClose.setBorder(new EmptyBorder(0, 6, 0, 0));
			// This is needed to Macs for some reason
			btnClose.setBorderPainted(false);
	
			// Make sure the button can't get focus, otherwise it looks funny
			btnClose.setFocusable(false);
			
			// All close buttons start off hidden and disabled - they are enabled when the tab is selected
			btnClose.setEnabled(false);
			btnClose.setVisible(false);

			// Add the listener that removes the tab
			ActionListener closeListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// The component parameter must be declared "final" so that it
					// can be
					// referenced in the anonymous listener class like this.
					parent.setVisible(c, false);
				}
			};
			btnClose.addActionListener(closeListener);

			// Only include the close button is the tab is hideable
			this.add(btnClose);
		}
	}

	public void setPinned (boolean pinned) {
		if (ap == null) {
			return;
		}
		ap.setPinned(pinned);
		if (ap.isPinned()) {
			btnPin.setIcon(PIN_TAB_GREY_ICON);
			btnPin.setRolloverIcon(PIN_TAB_RED_ICON);
			btnPin.setToolTipText(Constant.messages.getString("all.button.unpin"));
		} else {
			btnPin.setIcon(PIN_TAB_LIGHT_GREY_ICON);
			btnPin.setRolloverIcon(PIN_TAB_RED_ICON);
			btnPin.setToolTipText(Constant.messages.getString("all.button.pin"));
		}
		btnClose.setEnabled(!ap.isPinned());
		btnClose.setVisible(!ap.isPinned());
	}
	
	/*
	 * Temporarily lock/unlock the tab, eg if its active and mustnt be closed.
	 * Locked (AbstractPanel) tabs will not have the pin/close tab buttons displayed 
	 */
	public void setLocked(boolean locked) {
		if (ap == null) {
			return;
		}
		ap.setLocked(locked);
		btnPin.setVisible(! locked);
		if (!ap.isPinned()) {
			// Wont be visible if its pinned
			btnClose.setVisible(! locked);
		}
	}

	@Override
	public void setEnabled (boolean enabled) {
		if (ap != null && ap.isLocked()) {
			return;
		}
		btnPin.setEnabled(enabled);
		btnPin.setVisible(enabled);
		if (ap == null || ! ap.isPinned()) {
			btnClose.setEnabled(enabled);
			btnClose.setVisible(enabled);
		}
	}

	protected AbstractPanel getAbstractPanel() {
		return ap;
	}
	
}
