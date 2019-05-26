package org.zaproxy.zap.utils;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

public class ThreadUtils {

	/**
	 * Utility method used to run, synchronously, a Runnable on the main thread. Behaves exactly
	 * like {@link EventQueue#invokeAndWait(Runnable)}, but can be called from the main thread as
	 * well.
	 * 
	 * @param runnable the {@code Runnable} to be run in the EDT
	 * @throws InterruptedException if the current thread was interrupted while waiting for the EDT
	 * @throws InvocationTargetException if an exception occurred while running the {@code Runnable}
	 */
	public static void invokeAndWait(Runnable runnable) throws InvocationTargetException,
			InterruptedException {
		if (EventQueue.isDispatchThread()) {
			runnable.run();
		} else {
			EventQueue.invokeAndWait(runnable);
		}
	}
}
