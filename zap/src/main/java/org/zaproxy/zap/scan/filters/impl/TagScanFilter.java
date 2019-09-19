package org.zaproxy.zap.scan.filters.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.scan.filters.ScanFilter;

/**
 * @author KSASAN preetkaran20@gmail.com
 *
 */
public class TagScanFilter implements ScanFilter {

	private Set<String> tags = new LinkedHashSet<>();
	
	private boolean includeTags;

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	public boolean isIncludeTags() {
		return includeTags;
	}

	public void setIncludeTags(boolean includeTags) {
		this.includeTags = includeTags;
	}

	@Override
	public boolean isFiltered(StructuralNode node) {
		// TODO Auto-generated method stub
		return false;
	}

}
