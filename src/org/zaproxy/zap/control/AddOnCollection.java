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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

public class AddOnCollection {
	
	public enum Platform {daily, windows, linux, mac}
	
    private static final Logger logger = Logger.getLogger(AddOnCollection.class);
    private ZapRelease zapRelease = null;
	private List <AddOn> addOns = new ArrayList<>();
	private File downloadDir = new File(Constant.FOLDER_LOCAL_PLUGIN);
	private Platform platform;

    public AddOnCollection(ZapXmlConfiguration config, Platform platform) {
    	this(config, platform, true);
    }

    public AddOnCollection(ZapXmlConfiguration config, Platform platform, boolean allowAddOnsWithDependencyIssues) {
        this.platform = platform;
        this.load(config);

        if (!allowAddOnsWithDependencyIssues) {
            List<AddOn> checkedAddOns = new ArrayList<>(addOns);
            List<AddOn> runnableAddOns = new ArrayList<>(addOns.size());
            while (!checkedAddOns.isEmpty()) {
                AddOn addOn = checkedAddOns.remove(0);
                // Shouldn't happen but make sure to not show add-ons that wouldn't run, or one of its extensions
                // because of dependency issues or
                AddOn.AddOnRunRequirements requirements = addOn.calculateRunRequirements(addOns);
                if (requirements.hasDependencyIssue()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Ignoring add-on  " + addOn.getName() + " because of dependency issue: "
                                + AddOnRunIssuesUtils.getDependencyIssue(requirements));
                    }
                    if (AddOn.AddOnRunRequirements.DependencyIssue.CYCLIC == requirements.getDependencyIssue()) {
                        @SuppressWarnings("unchecked")
                        Set<AddOn> cyclicChain = (Set<AddOn>) requirements.getDependencyIssueDetails().get(0);
                        checkedAddOns.removeAll(cyclicChain);
                    }
                } else if (requirements.hasExtensionsWithRunningIssues()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Ignoring add-on  " + addOn.getName() + " because of dependency issue in an extension: "
                                + AddOnRunIssuesUtils.getDependencyIssue(requirements));
                    }
                } else {
                    runnableAddOns.add(addOn);
                }
            }
            addOns = runnableAddOns;
        }
    }

    private void load (ZapXmlConfiguration config) {
        config.setExpressionEngine(new XPathExpressionEngine());
        try {
        	// See if theres a ZAP release defined
			String version = config.getString("core/version");
        	if (Platform.daily.equals(platform)) {
        		// Daily releases take precedence even if running on Kali as they will have been manually installed
				version = config.getString("core/daily-version", version);
        	} else if (Constant.isKali()) {
				version = config.getString("core/kali-version", version);
        	}
			if (version != null && version.length() > 0) {
				String relUrlStr = config.getString("core/relnotes-url", null);
				URL relUrl = null;
				if (relUrlStr != null ) {
					relUrl = new URL(relUrlStr);
				}
				
				this.zapRelease = new ZapRelease(
								version, 
								new URL(config.getString("core/" + platform.name() +"/url")),
								config.getString("core/" + platform.name() +"/file"),
								config.getLong("core/" + platform.name() +"/size"),
								config.getString("core/relnotes"),
								relUrl,
								config.getString("core/" + platform.name() +"/hash"));
			}
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
        }
        
        try {
        	// And then load the addons
	        String[] addOnIds = config.getStringArray("addon");
        	for (String id: addOnIds) {
            	logger.debug("Found addon " + id);
            	
        		AddOn ao;
        		try {
        		    ao = new AddOn(id, downloadDir, config.configurationAt("addon_" + id));
        		    ao.setInstallationStatus(AddOn.InstallationStatus.AVAILABLE);
        		} catch (Exception e) {
        		    logger.warn("Failed to create add-on for " + id, e);
        		    continue;
        		}
        		if (ao.canLoadInCurrentVersion()) {
        			// Ignore ones that dont apply to this version
        			this.addOns.add(ao);
        		} else {
        			logger.debug("Ignoring addon " + ao.getName() + " cant load in this version");
        		}
        	}
        	
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
        }
    	
    }


	public AddOnCollection (File[] dirs) {
        if (dirs != null) {
        	for (File dir : dirs) {
                try {
					this.addDirectory(dir);
				} catch (Exception e) {
		    		logger.error(e.getMessage(), e);
				}
        	}
        }
	}

	private void addDirectory (File dir) throws Exception {
    	if (dir == null) {
    		logger.error("Null directory supplied");
    		return;
    	}
    	if (! dir.exists()) {
    		logger.error("No such directory: " + dir.getAbsolutePath());
    	}
    	if (! dir.isDirectory()) {
    		logger.error("Not a directory: " + dir.getAbsolutePath());
    	}
    	// Load the addons
        File[] listFile = dir.listFiles();

        if (listFile != null) {
        	for (File addOnFile : listFile) {
        		if (AddOn.isAddOn(addOnFile)) {
	            	AddOn ao = createAddOn(addOnFile);
                    if (ao == null) {
                        continue;
                    }

	            	boolean add = true;
	            	for (AddOn addOn : addOns) {
	            		if (ao.isSameAddOn(addOn)) {
		            		if (ao.isUpdateTo(addOn)) {
                                if (ao.canLoadInCurrentVersion()) {
    		            			// Replace in situ so we're not changing a list we're iterating through
    		                    	logger.debug("Addon " + addOn.getId() + " version " + addOn.getFileVersion() + 
    		                    			" superceeded by " + ao.getFileVersion());
    		                    	addOns.remove(addOn);
                                } else {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Ignoring newer addon " + ao.getId() + " version " + ao.getFileVersion()
                                                + " because of ZAP version constraints; Not before=" + ao.getNotBeforeVersion()
                                                + " Not from=" + ao.getNotFromVersion() + " Current Version="
                                                + Constant.PROGRAM_VERSION);
                                    }
                                    add = false;
                                }
		            		} else {
		            			// Same or older version, dont include
		                    	logger.debug("Addon " + ao.getId() + " version " + ao.getFileVersion() + 
		                    			" not latest.");
		            			add = false;
		            		}
	                    	break;
	            		}
	            	}
	            	if (add) {
	            		logger.debug("Found addon " + ao.getId() + " version " + ao.getFileVersion());
	            		this.addOns.add(ao);
	            	}
        		}
	        }
        }
    }

    private static AddOn createAddOn(File addOnFile) {
        try {
            return new AddOn(addOnFile);
        } catch (Exception e) {
            logger.warn("Failed to create add-on for: " + addOnFile.toString(), e);
        }
        return null;
    }
    
    /**
     * Gets all add-ons of this add-on collection.
     *
     * @return a {@code List} with all add-ons of the collection
     * @see #getInstalledAddOns()
     */
    public List <AddOn> getAddOns() {
    	return this.addOns;
    }

    /**
     * Gets all installed add-ons of this add-on collection, that is, the add-ons whose installation status is {@code INSTALLED}.
     *
     * @return a {@code List} with all installed add-ons of the collection
     * @see #getAddOns()
     * @see AddOn.InstallationStatus#INSTALLED
     */
    public List<AddOn> getInstalledAddOns() {
        List<AddOn> installedAddOns = new ArrayList<>(addOns.size());
        for (AddOn addOn : addOns) {
            if (AddOn.InstallationStatus.INSTALLED == addOn.getInstallationStatus()) {
                installedAddOns.add(addOn);
            }
        }
        return installedAddOns;
    }
    
    public AddOn getAddOn(String id) {
    	for (AddOn addOn : addOns) {
    		if (addOn.getId().equals(id)) {
    			return addOn;
    		}
    	}
    	return null;
    }

    /**
     * Returns a list of addons from the supplied collection that are newer than the equivalent ones in this collection
     * @param aoc the collection to compare with
     * @return a list of addons from the supplied collection that are newer than the equivalent ones in this collection
     */
    public List <AddOn> getUpdatedAddOns(AddOnCollection aoc) {
        List<AddOn> updatedAddOns = new ArrayList<>();

    	for (AddOn ao : aoc.getAddOns()) {
        	for (AddOn addOn : addOns) {
        		try {
					if (ao.isSameAddOn(addOn) && ao.isUpdateTo(addOn)) {
						// Its an update to one in this collection
						updatedAddOns.add(ao);
					}
				} catch (Exception e) {
		    		logger.error(e.getMessage(), e);
				}
        	}
        }
    	return updatedAddOns;
    }
    
    /**
     * Returns a list of addons from the supplied collection that are newer than the equivalent ones in this collection
     * @param aoc the collection to compare with
     * @return a list of addons from the supplied collection that are newer than the equivalent ones in this collection
     */
    public List <AddOn> getNewAddOns(AddOnCollection aoc) {
        List<AddOn> newAddOns = new ArrayList<>();

    	for (AddOn ao : aoc.getAddOns()) {
    		boolean isNew = true;
        	for (AddOn addOn : addOns) {
        		try {
					if (ao.isSameAddOn(addOn)) {
						isNew = false;
						break;
					}
				} catch (Exception e) {
		    		logger.error(e.getMessage(), e);
				}
        	}
    		if (isNew) {
				newAddOns.add(ao);
    		}
        }
    	return newAddOns;
    }
    
	public ZapRelease getZapRelease() {
		return zapRelease;
	}
	
	public boolean includesAddOn(String id) {
		boolean inc = false;
    	for (AddOn addOn : addOns) {
    		if (addOn.getId().equals(id)) {
    			return true;
    		}
    	}
		return inc;
	}
	
	public boolean addAddOn(AddOn ao) {
		if (this.includesAddOn(ao.getId())) {
			return false;
		}
		this.addOns.add(ao);
		return true;
	}

	public boolean removeAddOn(AddOn ao) {
    	for (AddOn addOn : addOns) {
    		if (addOn.getId().equals(ao.getId())) {
    			addOns.remove(addOn);
    			return true;
    		}
    	}
    	return false;
	}
}
