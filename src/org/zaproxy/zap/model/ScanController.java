package org.zaproxy.zap.model;

import java.util.List;

import org.zaproxy.zap.users.User;

public interface ScanController {

	int startScan(String displayName, Target target, User user, Object[] contextSpecificObjects);

	List<GenericScanner2> getAllScans();

	List<GenericScanner2> getActiveScans();

	GenericScanner2 getScan(int id);

	void stopScan (int id);

	void stopAllScans ();

	void pauseScan (int id);

	void pauseAllScans ();

	void resumeScan(int id);

	void resumeAllScans();

	GenericScanner2 removeScan(int id);

	int removeAllScans();

	int removeFinishedScans();

	GenericScanner2 getLastScan();

}
