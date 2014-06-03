package org.zaproxy.zap.view.panels;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.LayoutHelper;
import org.zaproxy.zap.view.ScanPanel;
import org.zaproxy.zap.view.ZapToggleButton;

/**
 * Under development...
 * 
 * @deprecated
 */
public abstract class AbstractScanToolbarStatusPanel extends AbstractContextSelectToolbarStatusPanel {

	private static final long serialVersionUID = -2351280081989616482L;

	/**
	 * Location provided to {@link #addToolBarElements(JToolBar, short, int)} to add items after the
	 * buttons.
	 */
	protected short TOOLBAR_LOCATION_AFTER_BUTTONS = 10;

	/**
	 * Location provided to {@link #addToolBarElements(JToolBar, short, int)} to add items after the
	 * progress bar.
	 */
	protected short TOOLBAR_LOCATION_AFTER_PROGRESS_BAR = 11;

	private JButton startScanButton;
	private JButton stopScanButton;
	private ZapToggleButton pauseScanButton;
	private JProgressBar progressBar;

	private Mode mode;

	public AbstractScanToolbarStatusPanel(String prefix, ImageIcon icon) {
		super(prefix, icon);

		mode = Control.getSingleton().getMode();
	}

	@Override
	protected void setupToolbarElements(JToolBar toolbar) {
		// We need to override this method completely to add more components and properly call the
		// addToolbarElements method with the new locations
		int x = 0;
		Insets insets = new Insets(0, 4, 0, 2);

		x = this.addToolBarElements(toolbar, TOOLBAR_LOCATION_START, x);

		toolbar.add(new JLabel(Constant.messages.getString(panelPrefix + ".toolbar.context.label")),
				LayoutHelper.getGBC(x++, 0, 1, 0, insets));
		toolbar.add(getContextSelectComboBox(), LayoutHelper.getGBC(x++, 0, 1, 0, insets));

		x = this.addToolBarElements(toolbar, TOOLBAR_LOCATION_AFTER_CONTEXTS_SELECT, x);

		toolbar.add(getStartScanButton(), LayoutHelper.getGBC(x++, 0, 1, 0, insets));
		toolbar.add(getPauseScanButton(), LayoutHelper.getGBC(x++, 0, 1, 0, insets));
		toolbar.add(getStopScanButton(), LayoutHelper.getGBC(x++, 0, 1, 0, insets));

		x = this.addToolBarElements(toolbar, TOOLBAR_LOCATION_AFTER_BUTTONS, x);

		toolbar.add(getProgressBar(), LayoutHelper.getGBC(x++, 0, 1, 0, insets));

		x = this.addToolBarElements(toolbar, TOOLBAR_LOCATION_AFTER_PROGRESS_BAR, x);

		toolbar.add(new JLabel(), LayoutHelper.getGBC(x++, 0, 1, 1.0)); // Spacer
		toolbar.add(getOptionsButton(), LayoutHelper.getGBC(x++, 0, 1, 0, insets));

		this.addToolBarElements(toolbar, TOOLBAR_LOCATION_END, x);
	}

	private JButton getStartScanButton() {
		if (startScanButton == null) {
			startScanButton = new JButton();
			startScanButton
					.setToolTipText(Constant.messages.getString(panelPrefix + ".toolbar.button.start"));
			startScanButton.setIcon(new ImageIcon(ScanPanel.class.getResource("/resource/icon/16/131.png")));
			startScanButton.setEnabled(false);
			startScanButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					startScan(getSelectedContext());
				}

			});

		}
		return startScanButton;
	}

	private JButton getStopScanButton() {
		if (stopScanButton == null) {
			stopScanButton = new JButton();
			stopScanButton.setToolTipText(Constant.messages.getString(panelPrefix + ".toolbar.button.stop"));
			stopScanButton.setIcon(new ImageIcon(ScanPanel.class.getResource("/resource/icon/16/142.png")));
			stopScanButton.setEnabled(false);
			stopScanButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stopScan(getSelectedContext());
				}
			});
		}
		return stopScanButton;
	}

	private JToggleButton getPauseScanButton() {
		if (pauseScanButton == null) {
			pauseScanButton = new ZapToggleButton();
			pauseScanButton
					.setToolTipText(Constant.messages.getString(panelPrefix + ".toolbar.button.pause"));
			pauseScanButton.setSelectedToolTipText(Constant.messages.getString(panelPrefix
					+ ".toolbar.button.unpause"));
			pauseScanButton.setIcon(new ImageIcon(ScanPanel.class.getResource("/resource/icon/16/141.png")));
			pauseScanButton.setEnabled(false);
			pauseScanButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (isScanPaused(getSelectedContext()))
						resumeScan(getSelectedContext());
					else
						pauseScan(getSelectedContext());
				}
			});
		}
		return pauseScanButton;
	}

	private JProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar(0, 100);
			progressBar.setValue(0);
			progressBar.setSize(new Dimension(80, 20));
			progressBar.setStringPainted(true);
			progressBar.setEnabled(false);
		}
		return progressBar;
	}

	@Override
	protected void contextSelected(Context context, boolean forceRefresh) {
		if (context == null) {
			resetScanButtonsAndProgressBarStates(false);
			return;
		}

		// If we are in 'Safe' mode, there's no need to disable anything as it was already disabled
		// when switching the mode
		if (Mode.safe.equals(this.mode)) {
			return;
		}

		// If context is not in scope and we are in 'Protect' mode, disable scanning.
		if (Mode.protect.equals(this.mode) && !context.isInScope()) {
			resetScanButtonsAndProgressBarStates(false);
			return;
		}

		if (forceRefresh || getSelectedContext() == null
				|| context.getIndex() != getSelectedContext().getIndex()) {
			if (isScanStarted(context)) {
				getStartScanButton().setEnabled(false);
				getStopScanButton().setEnabled(true);
				getPauseScanButton().setEnabled(true);
				getPauseScanButton().setSelected(isScanPaused(context));
				getProgressBar().setEnabled(true);
			} else {
				resetScanButtonsAndProgressBarStates(true);
			}

			getProgressBar().setValue(getScanProgress(context));
			getProgressBar().setMaximum(getScanMaximumProgress(context));
		}

		// Calling super takes care of updating the selectedContext and triggering a work panel view
		// switch
		super.contextSelected(context, forceRefresh);

	}

	private void resetScanButtonsAndProgressBarStates(boolean allowStartScan) {
		setScanButtonsAndProgressBarStates(false, false, allowStartScan);
		getProgressBar().setValue(0);
	}

	private void setScanButtonsAndProgressBarStates(boolean isStarted, boolean isPaused,
			boolean allowStartScan) {
		// TODO: Make sure this is called on the main thread
		if (isStarted) {
			getStartScanButton().setEnabled(false);
			getPauseScanButton().setEnabled(true);
			getPauseScanButton().setSelected(isPaused);
			getStopScanButton().setEnabled(true);
			getProgressBar().setEnabled(true);
		} else {
			getStartScanButton().setEnabled(allowStartScan);
			getStopScanButton().setEnabled(false);
			getPauseScanButton().setEnabled(false);
			getPauseScanButton().setSelected(false);
			getProgressBar().setEnabled(false);
		}
	}

	public void sessionModeChanged(Mode mode) {
		this.mode = mode;
		switch (mode) {
		case standard:
		case protect:
			// If the mode is standard or protect, make sure everything is set accordingly and
			// 'refresh' the UI if needed
			getContextSelectComboBox().setEnabled(true);
			if (getSelectedContext() != null) {
				this.contextSelected(getSelectedContext(), true);
			}
			break;
		case safe:
			// If the mode is 'safe', stop scans and disable controls
			// Stop all scans
			// stopAllScans();
			// And disable everything
			resetScanButtonsAndProgressBarStates(false);
			getContextSelectComboBox().setEnabled(false);
		}
	}

	protected abstract void startScan(Context context);

	protected abstract void pauseScan(Context context);

	protected abstract void resumeScan(Context context);

	protected abstract void stopScan(Context context);

	protected abstract boolean isScanStarted(Context context);

	protected abstract boolean isScanPaused(Context context);

	protected abstract int getScanProgress(Context context);

	protected abstract int getScanMaximumProgress(Context context);

}
