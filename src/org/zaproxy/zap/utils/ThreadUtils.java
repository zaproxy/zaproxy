package org.zaproxy.zap.utils;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

public class ThreadUtils {

	/**
	 * Utility method used to run, synchronously, a Runnable on the main thread. Behaves exactly
	 * like {@link EventQueue#invokeAndWait(Runnable)}, but can be called from the main thread as
	 * well.
	 * 
	 * @param runnable
	 * @throws InterruptedException
	 * @throws InvocationTargetException
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
