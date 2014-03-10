/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.bruteforce;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.ConversionException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

public class BruteForceParam extends AbstractParam {

	private static final Logger logger = Logger.getLogger(BruteForceParam.class);
	
	private static final String THREAD_PER_SCAN = "bruteforce.threadPerHost";
	private static final String DEFAULT_FILE = "bruteforce.defaultFile";
	private static final String RECURSIVE = "bruteforce.recursive";
	private static final String BROWSE_FILES = "bruteforce.browsefiles";
	private static final String FILE_EXTENSIONS = "bruteforce.fileextensions";
	
	public static final int DEFAULT_THREAD_PER_SCAN = 10;
	public static final int MAXIMUM_THREADS_PER_SCAN = 20;
	public static final boolean DEFAULT_RECURSIVE = true;
	public static final boolean DEFAULT_BROWSE_FILES = false;
	public static final String EMPTY_STRING = "";
		
	private int threadPerScan = DEFAULT_THREAD_PER_SCAN;
	private boolean recursive = DEFAULT_RECURSIVE;
	private ForcedBrowseFile defaultFile = null;
	private boolean browseFiles = DEFAULT_BROWSE_FILES;
	// can't be null
	private String fileExtensions = EMPTY_STRING;
	
    public BruteForceParam() {
    }

    @Override
    protected void parse() {
		try {
			this.threadPerScan = getConfig().getInt(THREAD_PER_SCAN, DEFAULT_THREAD_PER_SCAN);
			this.recursive = getConfig().getBoolean(RECURSIVE, DEFAULT_RECURSIVE);
			this.browseFiles = getConfig().getBoolean(BROWSE_FILES, DEFAULT_BROWSE_FILES);
			this.fileExtensions = getConfig().getString(FILE_EXTENSIONS, EMPTY_STRING);
		} catch (Exception e) {}
		
		try {
			String path = getConfig().getString(DEFAULT_FILE, "");
			if (!"".equals(path)) {
				this.defaultFile = new ForcedBrowseFile(new File(path));
			} else {
				this.defaultFile = null;
			}
		} catch (ConversionException e) {
			logger.error("Error while loading the forced browse default file: " + e.getMessage(), e);
			this.defaultFile = null;
		}
    }

    public int getThreadPerScan() {
        return threadPerScan;
    }
    
    public void setThreadPerScan(int threadPerHost) {
        this.threadPerScan = threadPerHost;
        getConfig().setProperty(THREAD_PER_SCAN, Integer.toString(this.threadPerScan));

    }

    public boolean getRecursive() {
        return recursive;
    }
    
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
        getConfig().setProperty(RECURSIVE, Boolean.toString(this.recursive));

    }

	protected ForcedBrowseFile getDefaultFile() {
		return defaultFile;
	}

	protected void setDefaultFile(ForcedBrowseFile defaultFile) {
		this.defaultFile = defaultFile;
		
		String absolutePath = "";
		if (defaultFile != null) {
			absolutePath = defaultFile.getFile().getAbsolutePath();
		}
		
		getConfig().setProperty(DEFAULT_FILE, absolutePath);
	}

	public boolean isBrowseFiles() {
		return browseFiles;
	}

	public void setBrowseFiles(boolean browseFiles) {
		this.browseFiles = browseFiles;
		getConfig().setProperty(BROWSE_FILES, browseFiles);
	}

	/**
	 * Define a comma-separated list of file extensions for 
	 * resources to be brute forced.
	 * 
	 * <p>
	 * 
	 * This method returns an empty string if extensions haven't
	 * been defined
	 * 
	 * @return comma-separated list of file extensions.
	 */
	public String getFileExtensions() {
		return fileExtensions;
	}

	/**
	 * Define a comma-separated list of file extensions for 
	 * resources to be brute forced
	 * 
	 * @param fileExtensions file extensions string
	 * @throws IllegalArgumentException if {@code fileExtensions} is
	 * {@code null}
	 */
	public void setFileExtensions(String fileExtensions) {
		if (fileExtensions == null) {			
			throw new IllegalArgumentException("fileExtensions is null");
		} 
		
		this.fileExtensions = fileExtensions;
		getConfig().setProperty(FILE_EXTENSIONS, fileExtensions);
	}

	/**
	 * Returns a list of file extensions to be force browsed
	 * 
	 * @return list of force browse file extensions, or an empty list
	 * in case no extensions have been defined.
	 */
	public List<String> getFileExtensionsList() {
	    if (fileExtensions.trim().equals(EMPTY_STRING)) {
	    	return Collections.emptyList();
	    }
	    
	    List<String> fileExtensionsList = new ArrayList<>();
	    for (String fileExtension: fileExtensions.replaceAll("\\s", 
	    		EMPTY_STRING).split(",")) {
	    	if (!fileExtension.equals(EMPTY_STRING)) {
	    		fileExtensionsList.add(fileExtension);
	    	}
	    }
	    
    	return fileExtensionsList;	    
	}
}
