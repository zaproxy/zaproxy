package org.zaproxy.zap.model;

import java.util.List;

import org.zaproxy.zap.users.User;

public interface ScanController<T extends GenericScanner2> {

	int startScan(String displayName, Target target, User user, Object[] contextSpecificObjects);

	List<T> getAllScans();

	List<T> getActiveScans();

	T getScan(int id);

	void stopScan (int id);

	void stopAllScans ();

	void pauseScan (int id);

	void pauseAllScans ();

	void resumeScan(int id);

	void resumeAllScans();

	T removeScan(int id);

	int removeAllScans();

	int removeFinishedScans();

	T getLastScan();

}
