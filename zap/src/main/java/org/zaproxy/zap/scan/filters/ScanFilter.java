package org.zaproxy.zap.scan.filters;

import org.zaproxy.zap.model.StructuralNode;

/**
 * @author KSASAN preetkaran20@gmail.com
 *
 */
public interface ScanFilter {

	boolean isFiltered(StructuralNode node);
	
}
