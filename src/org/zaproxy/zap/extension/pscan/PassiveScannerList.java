package org.zaproxy.zap.extension.pscan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PassiveScannerList {

	private List<PassiveScanner> passiveScanners = new ArrayList<>();
	private Set<String> scannerNames = new HashSet<>();

	protected void add (PassiveScanner scanner) {
		if (scannerNames.contains(scanner.getName())) {
			// Prevent duplicates, log error?
			return;
		}
		passiveScanners.add(scanner);
		scannerNames.add(scanner.getName());
	}
	
	protected void remove (PassiveScanner scanner) {
		passiveScanners.remove(scanner);
		scannerNames.remove(scanner.getName());
	}

	protected List<PassiveScanner> list () {
		return this.passiveScanners;
	}
	
	protected PassiveScanner getDefn(String name) {
		for (PassiveScanner scanner : passiveScanners) {
			if (scanner.getName().equals(name)) {
				return scanner;
			}
		}
		return null;
	}

	public void save(PassiveScanner defn) {
		passiveScanners.remove(defn);
		passiveScanners.add(defn);
	}

}
