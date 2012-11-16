package org.zaproxy.zap.extension.pscan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.zaproxy.zap.extension.pscan.scanner.RegexAutoTagScanner;

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
	
	protected List<PassiveScanner> list () {
		return this.passiveScanners;
	}
	
	public void setAutoTagScanners(List<RegexAutoTagScanner> autoTagScanners) {
		List<PassiveScanner> tempScanners = new ArrayList<>(passiveScanners.size() + autoTagScanners.size());
        
        for (Iterator<PassiveScanner> it = passiveScanners.iterator(); it.hasNext();) {
            PassiveScanner scanner = it.next();
            if (!(scanner instanceof RegexAutoTagScanner)) {
                tempScanners.add(scanner);
            } else {
                this.scannerNames.remove(scanner.getName());
            }
        }
        
        for (Iterator<PassiveScanner> it = passiveScanners.iterator(); it.hasNext();) {
            PassiveScanner scanner = it.next();
            if (scannerNames.contains(scanner.getName())) {
                // Prevent duplicates, log error?
                break;
            }
            tempScanners.add(scanner);
            scannerNames.add(scanner.getName());
        }
        
        this.passiveScanners = tempScanners;
    }

}
