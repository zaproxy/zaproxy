/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 ZAP development team
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
package org.zaproxy.zap.control;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;

public class AddOn  {
	public enum Status {unknown, example, alpha, beta, weekly, release}
	private String id;
	private String name;
	private String description = "";
	private String author = "";
	private int version;
	private Status status;
	private String changes = "";
	private File file = null;
	private URL url = null;
	private URL info = null;
	private long size = 0;
	private boolean hasZapAddOnEntry = false;

	/**
	 * Flag that indicates if the manifest was read (or attempted to). Allows to prevent reading the manifest a second time when
	 * the add-on file is corrupt.
	 */
	private boolean manifestRead;

	private String notBeforeVersion = null;
	private String notFromVersion = null;
	private String hash = null;
	
	private List<String> extensions = null;
	private List<String> ascanrules = null;
	private List<String> pscanrules = null;
	private List<String> files = null;
	
	private static final Logger logger = Logger.getLogger(AddOn.class);
	
	public static boolean isAddOn(String fileName) {
		if (! fileName.toLowerCase().endsWith(".zap")) {
			return false;
		}
		if (fileName.substring(0, fileName.indexOf(".")).split("-").length < 3) {
			return false;
		}
		String[] strArray = fileName.substring(0, fileName.indexOf(".")).split("-");
		try {
			Status.valueOf(strArray[1]);
			Integer.parseInt(strArray[2]);
		} catch (Exception e) {
			return false;
		}

		return true;
		
	}
	public static boolean isAddOn(File f) {
		if (! f.exists()) {
			return false;
		}
		return isAddOn(f.getName());
	}

	public AddOn(String fileName) throws Exception {
		// Format is <name>-<status>-<version>.zap
		if (! isAddOn(fileName)) {
			throw new Exception("Invalid ZAP add-on file " + fileName);
		}
		String[] strArray = fileName.substring(0, fileName.indexOf(".")).split("-");
		this.id = strArray[0];
		this.name = this.id;	// Will be overriden if theres a ZapAddOn.xml file
		this.status = Status.valueOf(strArray[1]);
		this.version = Integer.parseInt(strArray[2]);
	}

	public AddOn(File file) throws Exception {
		this(file.getName());
		if (! isAddOn(file)) {
			throw new Exception("Invalid ZAP add-on file " + file.getAbsolutePath());
		}
		this.file = file;
		loadManifestFile();
	}
	
	private void loadManifestFile() throws IOException {
		manifestRead = true;
		if (file.exists()) {
			// Might not exist in the tests
			try (ZipFile zip = new ZipFile(file)) {
				ZipEntry zapAddOnEntry = zip.getEntry("ZapAddOn.xml");
				if (zapAddOnEntry != null) {
					try (InputStream zis = zip.getInputStream(zapAddOnEntry)) {
						ZapAddOnXmlFile zapAddOnXml = new ZapAddOnXmlFile(zis);

						this.name = zapAddOnXml.getName();
						this.description = zapAddOnXml.getDescription();
						this.changes = zapAddOnXml.getChanges();
						this.author = zapAddOnXml.getAuthor();
						this.notBeforeVersion = zapAddOnXml.getNotBeforeVersion();
						this.notFromVersion = zapAddOnXml.getNotFromVersion();

						this.ascanrules = zapAddOnXml.getAscanrules();
						this.extensions = zapAddOnXml.getExtensions();
						this.files = zapAddOnXml.getFiles();
						this.pscanrules = zapAddOnXml.getPscanrules();

						hasZapAddOnEntry = true;
					}

				}
			}
		}
		
	}
	
	public AddOn(String id, String name, String description, String author, int version, Status status, 
			String changes, URL url, File file, long size, String notBeforeVersion, String notFromVersion,
			URL info, String hash) throws IOException {
		this.id = id;
		this.name = name;
		this.description = description;
		this.author = author;
		this.version = version;
		this.status = status;
		this.changes = changes;
		this.url = url;
		this.file = file;
		this.size = size;
		this.notBeforeVersion = notBeforeVersion;
		this.notFromVersion = notFromVersion;
		this.info = info;
		this.hash = hash;
		
		loadManifestFile();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getChanges() {
		return changes;
	}

	public void setChanges(String changes) {
		this.changes = changes;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public boolean hasZapAddOnEntry() {
		if (! hasZapAddOnEntry) {
			if (!manifestRead) {
				// Worth trying, as it depends which constructor has been used
				try {
					this.loadManifestFile();
				} catch (IOException e) {
					if (logger.isDebugEnabled()) {
						logger.debug("Failed to read the ZapAddOn.xml file of " + id + ":", e);
					}
				}
			}
		}
		return hasZapAddOnEntry;
	}
	
	public List<String> getExtensions() {
		return extensions;
	}
	
	public List<String> getAscanrules() {
		return ascanrules;
	}
	
	public List<String> getPscanrules() {
		return pscanrules;
	}
	
	public List<String> getFiles() {
		return files;
	}
	
	public boolean isSameAddOn(AddOn addOn) {
		return this.getId().equals(addOn.getId());
	}

	public boolean isUpdateTo(AddOn addOn) throws IllegalArgumentException {
		if (! this.isSameAddOn(addOn)) {
			throw new IllegalArgumentException("Different addons: " + this.getId() + " != " + addOn.getId());
		}
		if (this.getVersion() > addOn.getVersion()) {
			return true;
		}
		return this.getStatus().ordinal() > addOn.getStatus().ordinal();
	}
	
	public boolean canLoad() {
		return this.canLoadInVersion(Constant.PROGRAM_VERSION);
	}
	
	public boolean canLoadInVersion(String version) {
		ZapReleaseComparitor zrc = new ZapReleaseComparitor();
		ZapRelease zr = new ZapRelease(version);
		if (this.notBeforeVersion != null && this.notBeforeVersion.length() > 0) {
			ZapRelease notBeforeRelease = new ZapRelease(this.notBeforeVersion);
			if (zrc.compare(zr, notBeforeRelease) < 0) {
				return false;
			}
		}
		if (this.notFromVersion != null && this.notFromVersion.length() > 0) {
			ZapRelease notFromRelease = new ZapRelease(this.notFromVersion);
			return zrc.compare(zr, notFromRelease) < 0;
		}
		return true;
	}
	
	public void setNotBeforeVersion(String notBeforeVersion) {
		this.notBeforeVersion = notBeforeVersion;
	}
	
	public void setNotFromVersion(String notFromVersion) {
		this.notFromVersion = notFromVersion;
	}
	
	public String getNotBeforeVersion() {
		return notBeforeVersion;
	}
	
	public String getNotFromVersion() {
		return notFromVersion;
	}

	public URL getInfo() {
		return info;
	}
	
	public void setInfo(URL info) {
		this.info = info;
	}
	
	public String getHash() {
		return hash;
	}
	
}
