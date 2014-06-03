/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.scan;

/**
 * The base implementation that needs to be extended for a thread that is used to run a Scan.
 * <p/>
 * This abstract implementation handles common elements that correspond to all scans (scan progress,
 * scan maximum progress, running/paused state, start options), but should be extended to take into
 * consideration particular needs of the scan.
 * <p/>
 * An important characteristic of the scanner threads is that they are based on a set of start
 * options ({@link ScanStartOptions}) that should be used for fully specifying the configuration for
 * a particular scan. The scan options should be provided to the Scanner Thread before being started
 * via the {@link #setStartOptions(ScanStartOptions)} method. Examples of configuration required
 * might include: context and/or site node to scan, user to scan as, policy etc.
 * <p/>
 * The most important method that needs to be implemented is {@link #scan()}, but other common
 * methods should be implemented as well, calling the {@code super} implementation:
 * <ul>
 * <li>{@link #pauseScan()}</li>
 * <li>{@link #resumeScan()}</li>
 * <li>{@link #startScan()}</li>
 * <li>{@link #stopScan()}</li>
 * </ul>
 *
 * @param <StartOptions> the generic type
 * @see ScanStartOptions
 * @see ScannerThreadManager
 */
public abstract class BaseScannerThread<StartOptions extends ScanStartOptions> extends Thread {

	private StartOptions startOptions;
	private int progress = 0;
	private int maximumProgress = 100;
	private boolean paused;
	private boolean running;

	/**
	 * Gets the start options.
	 *
	 * @return the start options
	 */
	public StartOptions getStartOptions() {
		return startOptions;
	}

	/**
	 * Sets the start options.
	 *
	 * @param startOptions the new start options
	 */
	public void setStartOptions(StartOptions startOptions) {
		this.startOptions = startOptions;
	}

	/**
	 * Gets the scan's progress, on a scale from 0 to {@link #getScanMaximumProgress()}.
	 *
	 * @return the scan progress
	 */
	public int getScanProgress() {
		return progress;
	}

	/**
	 * Sets the scan's progress, on a scale from 0 to {@link #getScanMaximumProgress()}.
	 *
	 * @param progress the new scan progress
	 */
	public void setScanProgress(int progress) {
		this.progress = progress;
	}

	/**
	 * Sets the scan's maximum progress.
	 *
	 * @param maximumProgress the new scan maximum progress
	 */
	public void setScanMaximumProgress(int maximumProgress) {
		this.maximumProgress = maximumProgress;
	}

	/**
	 * Gets the scan's maximum progress.
	 *
	 * @return the scan maximum progress
	 */
	public int getScanMaximumProgress() {
		return maximumProgress;
	}

	/**
	 * Checks if is paused.
	 *
	 * @return true, if is paused
	 */
	public boolean isPaused() {
		return paused;
	}

	/**
	 * Checks if is running.
	 *
	 * @return true, if is running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Sets the paused.
	 *
	 * @param paused the new paused
	 */
	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	/**
	 * Sets the running.
	 *
	 * @param running the new running
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * Checks if the scanner thread has already run/been started. Useful as a Java Thread cannot be
	 * restarted.
	 *
	 * @return true, if successful
	 */
	public boolean hasRun() {
		return getState() != State.NEW;
	}

	@Override
	public void run() {
		this.startScan();
	}

	/**
	 * Method called when the scan is paused. The base implementation handles the update of the
	 * thread's paused state, but should be overridden if necessary.
	 * <p/>
	 * Note: Implementations must be careful to make sure the scan actually gets paused (either by
	 * overriding this method or by constantly checking the {@link #isPaused()} status in the
	 * {@link #scan()} method).
	 * <p/>
	 * Note: This method is not always run on the scanner thread.
	 */
	public void pauseScan() {
		setPaused(true);
	}

	/**
	 * Method called when the scan is resumed. The base implementation handles the update of the
	 * thread's paused state, but should be overridden if necessary.
	 * <p/>
	 * Note: Implementations must be careful to make sure the scan actually gets resumed (either by
	 * overriding this method or by constantly checking the {@link #isPaused()} status in the
	 * {@link #scan()} method).
	 * <p/>
	 * Note: This method is not always run on the scanner thread.
	 */
	public void resumeScan() {
		setPaused(false);
	}

	/**
	 * Method called when the scan is stopped before it finished. The base implementation handles
	 * the update of the thread's running state, but should be overridden if necessary.
	 * <p/>
	 * Note: Implementations must be careful to make sure the scan is stopped and the thread
	 * finishes (either by overriding this method or by constantly checking the {@link #isRunning()}
	 * status in the {@link #scan()} method).
	 * <p/>
	 * Note: This method is not always run on the scanner thread.
	 */
	public void stopScan() {
		setRunning(false);
	}

	/**
	 * Method called when the scan is started. The base implementation handles the update of the
	 * thread's running state and calls the {@link #scan()} method.
	 * <p/>
	 * Note: This method is being run on the scanner thread.
	 */
	public void startScan() {
		setRunning(true);
		this.scan();
	}

	/**
	 * Method called on the scanner thread in order to perform the work required by the scan.
	 */
	protected abstract void scan();
}
