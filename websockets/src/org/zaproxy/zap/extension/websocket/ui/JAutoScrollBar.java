package org.zaproxy.zap.extension.websocket.ui;

import java.awt.Adjustable;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;

/**
 * Proved to be the best way for scrolling down, if scroll bar was at bottom
 * last, before new content was added.
 * 
 * With a JList I have used the following code first: <code>
 * SwingUtilities.invokeLater(new Runnable() {
 *      public void run() {
 *          messagesLog.ensureIndexIsVisible(model.getSize() - 1);
 *      }
 * });</code>
 * 
 * But this {@link Scrollbar} with its {@link AdjustmentListener}
 * worked well. No scrolling artifacts, nor does it loose the scrolling.
 */
public class JAutoScrollBar extends JScrollBar {
	private static final long serialVersionUID = 4619824764464679424L;

	/**
	 * Previous value of the scroll pane indicating if we have to autoscroll.
	 */
	private int previousMaximum = 0;

	public JAutoScrollBar() {
		super();
		init();
	}

	/**
	 * Install adjustment listener.
	 */
	private void init() {
		addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				Adjustable source = (Adjustable) e.getSource();
				
				if (source.getValue() + source.getVisibleAmount() == previousMaximum
						&& source.getMaximum() > previousMaximum) {
					// scrollbar is at previous position,
					// that was also the former maximum value
					
					// now content was added => scroll down
					source.setValue(source.getMaximum());
				}
				
				previousMaximum = source.getMaximum();
			}
		});
	}
}