package org.zaproxy.zap.model;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

public class VulnerabilitiesLoader {

	private static final Logger logger = Logger.getLogger(VulnerabilitiesLoader.class);
	
	private String xmlDirectory;
	private String vulnerabilitiesBase;
	
	public VulnerabilitiesLoader(String xmlDiretory, String vulnerabilitiesBase) {
		this.vulnerabilitiesBase = vulnerabilitiesBase;
		this.xmlDirectory = xmlDiretory;
	}

	public VulnerabilitiesI18NMap load() {
		
		VulnerabilitiesI18NMap vulnerabilitiesI18NMap = new VulnerabilitiesI18NMap();
		
		String[] fileNames = getListOfVulnerabilitiesFiles();
		
		for (String fileName : fileNames) {
			List<Vulnerability> list = LoadVulnerabilitiesFile(fileName);	
			
			if (list == null) {
				list = Collections.unmodifiableList(Collections.<Vulnerability>emptyList());
			}
			
			String localeName = null;
			
			if (hasLocalePostfix(fileName)) {
				localeName = getLocaleName(fileName);	
			}
			
			vulnerabilitiesI18NMap.putVulnerabilitiesList(localeName, list);
			
			logger.debug("loading vulnerabilities from " + fileName + " for locale " + localeName + ".");
		}
		
		return vulnerabilitiesI18NMap;

	}

	private boolean hasLocalePostfix(String fileName) {
		return fileName.length() > vulnerabilitiesBase.length() + 4;
	}
	
	private List<Vulnerability> LoadVulnerabilitiesFile(String fileName) {

		XMLConfiguration config;
        try {
        	File f = new File(xmlDirectory, fileName);
        	config = new XMLConfiguration();
        	config.setDelimiterParsingDisabled(true);
        	config.load(f);
        } catch (ConfigurationException e) {
        	logger.error(e.getMessage(), e);
        	return null;
        }
        
        String[] test;
        try {
        	test = config.getStringArray("vuln_items");
        } catch (ConversionException e) {
        	logger.error(e.getMessage(), e);
        	return null;
        }
    	final int numberOfVulns = test.length;
    	
    	List<Vulnerability> tempVulns = new ArrayList<>(numberOfVulns);
    	
    	String name;
    	List<String> references;
    	
    	for (String item : test) {
    		name = "vuln_item_" + item;
    		try {
    			references = new ArrayList<>(Arrays.asList(config.getStringArray(name + ".reference")));
    		} catch (ConversionException e) {
    			logger.error(e.getMessage(), e);
    			references = new ArrayList<>(0);
    		}
    			
    		Vulnerability v = 
    			new Vulnerability(
    					item,
    					config.getString(name + ".alert"),
    					config.getString(name + ".desc"),
    					config.getString(name + ".solution"),
    					references);
    		tempVulns.add(v);
    	}
    	
    	return tempVulns;
	}

	private String getLocaleName(String filename) {
		return filename.substring(vulnerabilitiesBase.length()+ 1, vulnerabilitiesBase.length() + 6);
	}

	private String[] getListOfVulnerabilitiesFiles() {
		File file = new File(xmlDirectory);
		String[] files = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File dir, String name) {
			if (name.indexOf(".xml") == -1) {
				return false;
			}
			
			return name.indexOf(vulnerabilitiesBase) >= 0;
		  }
		});
		return files;
	}
	

}
