package org.zaproxy.zap.model;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.zaproxy.zap.utils.LocaleUtils;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/**
 * Helper class that loads {@code Vulnerability} from a XML file for a given {@code Locale}.
 *
 * @see Vulnerability
 */
public class VulnerabilitiesLoader {

	private static final Logger logger = Logger.getLogger(VulnerabilitiesLoader.class);
	
	private final Path directory;
	private final String fileName;
	private final String fileExtension;
	
	/**
	 * Constructs a {@code VulnerabilitiesLoader} that loads the resource XML files from the given {@code directory} with the
	 * given {@code fileName} and {@code fileExtension}.
	 *
	 * @param directory the directory where the XML files are located
	 * @param fileName the name of the XML files that contains the vulnerabilities
	 * @param fileExtension the extension (with dot) of the XML files that contains the vulnerabilities
	 * @throws IllegalArgumentException if {@code directory} is {@code null} or if {@code fileName} and {@code fileExtension}
	 *			 are {@code null} or empty
	 */
	public VulnerabilitiesLoader(Path directory, String fileName, String fileExtension) {
		Validate.notNull(directory, "Parameter directory must not be null.");
		Validate.notEmpty(fileName, "Parameter fileName must not be null nor empty.");
		Validate.notEmpty(fileExtension, "Parameter fileExtension must not be null nor empty.");

		this.directory = directory;
		this.fileName = fileName;
		this.fileExtension = fileExtension;
	}

	/**
	 * Returns an unmodifiable {@code List} of {@code Vulnerability}s for the given {@code locale}.
	 * <p>
	 * If there's no perfect match for the given {@code locale} the default will be returned, if available. The list will be
	 * empty if an error occurs.
	 *
	 * @param locale the locale that will {@code Vulnerability}s will be loaded
	 * @return an unmodifiable {@code List} of {@code Vulnerability}s for the given {@code locale}
	 */
	public List<Vulnerability> load(Locale locale) {
		List<String> filenames = getListOfVulnerabilitiesFiles();

		String extension = fileExtension;
		if (extension.startsWith(".")) {
			extension = extension.substring(1);
		}
		List<Vulnerability> vulnerabilities = LocaleUtils.findResource(fileName, extension, locale, candidateFilename -> {
			if (filenames.contains(candidateFilename)) {
				if (logger.isDebugEnabled()) {
					logger.debug("loading vulnerabilities from " + candidateFilename + " for locale " + locale + ".");
				}

				List<Vulnerability> list = loadVulnerabilitiesFile(directory.resolve(candidateFilename));
				if (list == null) {
					return Collections.emptyList();
				}
				return Collections.unmodifiableList(list);
			}
			return null;
		});

		if (vulnerabilities == null) {
			return Collections.emptyList();
		}
		return vulnerabilities;
	}
	
	List<Vulnerability> loadVulnerabilitiesFile(Path file) {

		ZapXmlConfiguration config;
        try {
        	config = new ZapXmlConfiguration(file.toFile());
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

	/**
	 * Returns a {@code List} of resources files with {@code fileName} and {@code fileExtension} contained in the
	 * {@code directory}.
	 *
	 * @return the list of resources files contained in the {@code directory}
	 * @see LocaleUtils#createResourceFilesPattern(String, String)
	 */
	List<String> getListOfVulnerabilitiesFiles() {
		final Pattern filePattern = LocaleUtils.createResourceFilesPattern(fileName, fileExtension);
		final List<String> fileNames = new ArrayList<>();
		try {
			Files.walkFileTree(directory, Collections.<FileVisitOption> emptySet(), 1, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					String fileName = file.getFileName().toString();
					if (filePattern.matcher(fileName).matches()) {
						fileNames.add(fileName);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			logger.error("An error occurred while walking directory: " + directory, e);
		}
		return fileNames;
	}

}
