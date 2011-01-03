package org.zaproxy.zap.extension.pscan;

import java.util.ArrayList;
import java.util.List;

public class PassiveScannerList {

	private List<PassiveScanner> passiveScanners = new ArrayList<PassiveScanner>();

	protected void add (PassiveScanner scanner) {
		passiveScanners.add(scanner);
	}
	
	protected void remove (PassiveScanner scanner) {
		passiveScanners.remove(scanner);
	}

	protected List<PassiveScanner> list () {
		return this.passiveScanners;
	}
	
	protected PassiveScanner getDefn(int index) {
		return this.passiveScanners.get(index);
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
