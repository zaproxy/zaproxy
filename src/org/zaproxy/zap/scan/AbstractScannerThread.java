package org.zaproxy.zap.scan;

import javax.swing.ListModel;

import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.GenericScanner;
import org.zaproxy.zap.users.User;

/**
 * Under development...
 * 
 * @deprecated
 */
public abstract class AbstractScannerThread<StartOptions extends AbstractScannerStartOptions> extends Thread
		implements GenericScanner {

	protected StartOptions startOptions;
	protected int progress = 0;
	protected int maximumProgress = 100;
	private boolean paused;
	private boolean running;

	public StartOptions getStartOptions() {
		return startOptions;
	}

	public void setStartOptions(StartOptions startOptions) {
		this.startOptions = startOptions;
	}

	@Override
	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getMaximumProgress() {
		return maximumProgress;
	}

	public void setMaximumProgress(int maximumProgress) {
		this.maximumProgress = maximumProgress;
	}

	@Override
	public int getMaximum() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isPaused() {
		return paused;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public boolean isStopped() {
		return !running;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public String getSite() {
		// Not needed by all the scanner threads, so needs to be implemented when needed
		return null;
	}

	@Override
	public SiteNode getStartNode() {
		// Not needed by all the scanner threads, so needs to be implemented when needed
		return null;
	}

	@Override
	public void setStartNode(SiteNode startNode) {
		// Not needed by all the scanner threads, so needs to be implemented when needed
	}

	@Override
	public void setJustScanInScope(boolean scanInScope) {
		// Not needed by all the scanner threads, so needs to be implemented when needed

	}

	@Override
	public boolean getJustScanInScope() {
		// Not needed by all the scanner threads, so needs to be implemented when needed
		return false;
	}

	@Override
	public ListModel<?> getList() {
		// Not needed by all the scanner threads, so needs to be implemented when needed
		return null;
	}

	@Override
	public void setScanChildren(boolean scanChildren) {
		// Not needed by all the scanner threads, so needs to be implemented when needed
	}

	@Override
	public void setScanContext(Context context) {
		// Not needed by all the scanner threads, so needs to be implemented when needed

	}

	@Override
	public void setScanAsUser(User user) {
		// Not needed by all the scanner threads, so needs to be implemented when needed
	}

	@Override
	public void run() {
		super.run();
		this.startScan();
	}

	public abstract void startScan();

}
