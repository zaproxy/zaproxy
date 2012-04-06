package org.zaproxy.zap.utils;

import java.io.File;
import java.io.FilenameFilter;

public class FilenameExtensionFilter implements FilenameFilter {

	String ext;
	boolean ignoreCase = false;
	
	public FilenameExtensionFilter (String ext, boolean ignoreCase) {
		this.ext = ext;
		this.ignoreCase = ignoreCase;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		if (ignoreCase) {
			return name.toLowerCase().endsWith(ext.toLowerCase());
		}
		return name.endsWith(ext);
	}

}
