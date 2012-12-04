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
    	this.platform = platform;
    	this.load(config);
    }

    private void load (ZapXmlConfiguration config) {
        try {
        	// See if theres a ZAP release defined
			String version = config.getString("core.version");
        	if (Platform.daily.equals(platform)) {
				version = config.getString("core.daily-version", version);
        	}
			if (version != null && version.length() > 0) {
				this.zapRelease = new ZapRelease(
								version, 
								new URL(config.getString("core." + platform.name() +".url")),
								config.getString("core." + platform.name() +".file"),
								config.getLong("core." + platform.name() +".size"),
								config.getString("core.relnotes"));
			}
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
        }
        
        try {
        	// And then load the addons
	        String[] addOnIds = config.getStringArray("addon");
        	for (String id: addOnIds) {
            	logger.debug("Found addon " + id);
        		this.addOns.add(
        				new AddOn(
        						id, 
        						config.getString("addon_" + id + ".name"), 
        						config.getInt("addon_" + id + ".version"),
        						AddOn.Status.valueOf(config.getString("addon_" + id + ".status")),
        						config.getString("addon_" + id + ".changes"), 
        						new URL(config.getString("addon_" + id + ".url")),
        						new File(downloadDir, config.getString("addon_" + id + ".file")),
        						config.getLong("addon_" + id + ".size")));
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

        List<AddOn> removeAddOns = new ArrayList<AddOn>();

        if (listFile != null) {
        	for (File addOnFile : listFile) {
        		if (AddOn.isAddOn(addOnFile)) {
	            	AddOn ao = new AddOn(addOnFile);
	            	for (AddOn addOn : addOns) {
	            		if (ao.isSameAddOn(addOn) && ao.isUpdateTo(addOn)) {
	            			// Remove them below so we're not changing a list we're iterating through
	            			removeAddOns.add(addOn);
	            		}
	            	}
	            	this.addOns.add(ao);
        		}
	        }
	    	for (AddOn remAddOn : removeAddOns) {
	    		this.addOns.remove(remAddOn);
	    	}
        }
    }
    
    public List <AddOn> getAddOns() {
    	return this.addOns;
    }

    /**
     * Returns a list of addons from the supplied collection that are newer than the equivalent ones in this collection
     * @param aoc the collection to compare with
     * @return a list of addons from the supplied collection that are newer than the equivalent ones in this collection
     */
    public List <AddOn> getUpdatedAddOns(AddOnCollection aoc) {
        List<AddOn> updatedAddOns = new ArrayList<AddOn>();

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
        List<AddOn> newAddOns = new ArrayList<AddOn>();

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
}
