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
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.pscan.ExtensionPassiveScan;

/**
 * This class is heavily based on the original Paros class org.parosproxy.paros.common.DynamicLoader
 * However its been restructured and enhanced to support multiple directories or versioned ZAP addons.
 * The constructor takes an array of directories. 
 * All of the generic jars in the directories are loaded.
 * Only the latest ZAP addons are loaded, so if the following addons are found:
 *  zap-ext-test-alpha-1.zap
 *  zap-ext-test-beta-2.zap
 *  zap-ext-test-alpha-3.zap
 * then only the latest one (zap-ext-test-alpha-3.zap) will be loaded - this is entirely based on the
 * version number.
 * The status (alpha/beta/release) is for informational purposes only. 
 */
public class AddOnLoader extends URLClassLoader {

	private static final String ADDONS_BLOCK_LIST = "addons.block";
	
    private static final Logger logger = Logger.getLogger(AddOnLoader.class);
    private AddOnCollection aoc = null;
    private List<File> jars = new ArrayList<File>();
    /**
     * Addons can be included in the ZAP release, in which case the user might not have permissions to delete the files.
     * To support the removal of such addons we just maintain a 'block list' in the configs which is a comma separated
     * list of the addon ids that the user has uninstalled
     */
    private List<String> blockList = new ArrayList<String>();
    /*
     * Using sub-classloaders means we can unload and reload addons
     */
    private Map<String, URLClassLoader> addOnLoaders = new HashMap<String, URLClassLoader>();

    public AddOnLoader(File[] dirs) {
        super(new URL[0]);
        
        this.loadBlockList();

        this.aoc = new AddOnCollection(dirs);
        
        for (AddOn ao : this.aoc.getAddOns()) {
        	// Add, unless its on the block list
        	if ( ! this.blockList.contains(ao.getId())) {
        		this.addAddOnFile(ao);
        	}
        }

        if (dirs != null) {
        	for (File dir : dirs) {
                try {
					this.addDirectory(dir);
				} catch (Exception e) {
		    		logger.error(e.getMessage(), e);
				}
        	}
        }
        
    	for (File jar : jars) {
            try {
				this.addURL(jar.toURI().toURL());
			} catch (MalformedURLException e) {
	    		logger.error(e.getMessage(), e);
			}
    	}
    }
    
    private void addAddOnFile(AddOn ao) {
    	try {
			this.addOnLoaders.put(ao.getId(), new URLClassLoader(new URL[]{ao.getFile().toURI().toURL()}));
		} catch (MalformedURLException e) {
    		logger.error(e.getMessage(), e);
		}
	}

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
			return loadClass(name, false);
		} catch (ClassNotFoundException e) {
			// Continue for now
		}
        for (URLClassLoader loader : addOnLoaders.values()) {
            try {
    			return loader.loadClass(name);
    		} catch (ClassNotFoundException e) {
    			// Continue for now
    		}
        }
        throw new ClassNotFoundException(name);
    }


    
    public AddOnCollection getAddOnCollection() {
    	return this.aoc;
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

        // Load the jar files
        File[] listJars = dir.listFiles(new JarFilenameFilter());
        if (listJars != null) {
        	for (File jar : listJars) {
        		this.jars.add(jar);
        	}
        }
    }
    
    public void addAddon(AddOn ao) {
    	if (! ao.canLoad()) {
    		throw new IllegalArgumentException("Cant load add-on " + ao.getName() + 
    				" Not before=" + ao.getNotBeforeVersion() + " Not from=" + ao.getNotFromVersion() + 
    				" Version=" + Constant.PROGRAM_VERSION);
    	}
    	if (this.aoc.addAddOn(ao)) {
            try {
            	this.addURL(ao.getFile().toURI().toURL());
            	
            	if (this.blockList.contains(ao.getId())) {
            		// Explicitly being added back, so remove from the block list
            		this.blockList.remove(ao.getId());
            		this.saveBlockList();
            	}

        		if (ao.hasZapAddOnEntry()) {
        			// Can dynamically install
	
	            	// Extensions
					List<Extension> listExts = ExtensionFactory.loadAddOnExtensions(
							Control.getSingleton().getExtensionLoader(), 
							Model.getSingleton().getOptionsParam().getConfig(), ao);
					
				   	for (Extension ext : listExts) {
				   		if (ext.isEnabled()) {
				   			logger.debug("Starting extension " + ext.getName());
				   			Control.getSingleton().getExtensionLoader().startLifeCycle(ext);
				   		}
				   	}
	
	            	// Ascan rules
	    			List<String> ascanNames = ao.getAscanrules();
	    			if (ascanNames != null) {
	    				for (String name : ascanNames) {
	    					logger.debug("Install ascanrule: " + name);
	    					if (!PluginFactory.addPlugin(name)) {
	    						logger.error("Failed to install ascanrule: " + name);
	    					}
	    				}
	    			}
	
	    			// Pscan rules
	    			List<String> pscanNames = ao.getPscanrules();
	    			ExtensionPassiveScan extPscan = (ExtensionPassiveScan) Control.getSingleton().getExtensionLoader().getExtension(ExtensionPassiveScan.NAME);
	
	    			if (pscanNames != null && extPscan != null) {
	    				for (String name : pscanNames) {
	    					logger.debug("Install pscanrule: " + name);
	    					if (!extPscan.addPassiveScanner(name)) {
	    						logger.error("Failed to install pscanrule: " + name);
	    					}
	    				}
	    			}

	    			// Pscan rules
	    			List<String> fileNames = ao.getFiles();

	    			if (fileNames != null && fileNames != null) {
	    				for (String name : fileNames) {
							File outfile = null;
	    					logger.debug("Install file: " + name);
	    					try {
								outfile = new File(Constant.getZapHome(), name);
								InputStream in = ExtensionFactory.getAddOnLoader().getResourceAsStream(name);
								if ( ! outfile.getParentFile().exists() && !outfile.getParentFile().mkdirs()) {
		    						logger.error("Failed to create directories for: " + outfile.getAbsolutePath());
									continue;
								}
								OutputStream out = new FileOutputStream(outfile);
								byte[] buffer = new byte[1024];
								int bytesRead;
								while ((bytesRead = in.read(buffer)) != -1) {
									out.write(buffer, 0, bytesRead);
								}
								in.close();
								out.close();
							} catch (Exception e) {
	    						logger.error("Failed to install file " + outfile.getAbsolutePath(), e);
							}
	    				}
	    				Control.getSingleton().getExtensionLoader().addonFilesAdded();
	    			}

        		}

			} catch (MalformedURLException e) {
	    		logger.error(e.getMessage(), e);
			}
    	}
    }
    

	public boolean removeAddOn(AddOn ao) {
		boolean result = true;
		
		if (! ao.hasZapAddOnEntry()) {
			// Cant dynamically uninstall
			result = false;
		} else {
			List<String> extNames = ao.getExtensions();
			if (extNames != null) {
				for (String name : extNames) {
					Extension ext = Control.getSingleton().getExtensionLoader().getExtensionByClassName(name);
					if (ext != null && ext.isEnabled()) {
						if (ext.canUnload()) {
							logger.debug("Unloading ext: " + ext.getName());
							ext.unload();
							// TODO should remove from extension loader here to prevent duplicate name warning
							// Control.getSingleton().getExtensionLoader().
						} else {
							logger.debug("Cant dynamically unload ext: " + name);
							result = false;
						}
					}
				}
			}

			// Ascan rules
			List<String> ascanNames = ao.getAscanrules();
			logger.debug("Uninstall ascanrules: " + ascanNames);
			if (ascanNames != null) {
				for (String name : ascanNames) {
					logger.debug("Uninstall ascanrule: " + name);
					if (!PluginFactory.removePlugin(name)) {
						logger.error("Failed to uninstall ascanrule: " + name);
						result = false;
					}
				}
			}

			// Pscan rules
			List<String> pscanNames = ao.getPscanrules();
			ExtensionPassiveScan extPscan = (ExtensionPassiveScan) Control.getSingleton().getExtensionLoader().getExtension(ExtensionPassiveScan.NAME);
			logger.debug("Uninstall pscanrules: " + pscanNames);
			if (pscanNames != null && extPscan != null) {
				for (String name : pscanNames) {
					logger.debug("Uninstall pscanrule: " + name);
					if (!extPscan.removePassiveScanner(name)) {
						logger.error("Failed to uninstall pscanrule: " + name);
						result = false;
					}
				}
			}
			
			// Files
			List<String> fileNames = ao.getFiles();
			if (fileNames != null && fileNames != null) {
				File file = null;
				for (String name : fileNames) {
					File parent = null;
					logger.debug("Uninstall file: " + name);
					try {
						file = new File(Constant.getZapHome(), name);
						parent = file.getParentFile();
						if (!file.delete()) {
    						logger.error("Failed to delete: " + file.getAbsolutePath());
    						result = false;
						}
						if (parent.list().length == 0) {
    						logger.debug("Deleting: " + parent.getAbsolutePath());
							if (!parent.delete()) {
								// Ignore - check for <= 2 as on *nix '.' and '..' are returned
	    						logger.debug("Failed to delete: " + parent.getAbsolutePath());
							}
						}
					} catch (Exception e) {
						logger.error("Failed to uninstall file " + file.getAbsolutePath(), e);
					}
				}
				// Delete any empty dirs up to the ZAP root dir
				while (file != null && ! file.exists()) {
					file = file.getParentFile();
				}
				String root = new File(Constant.getZapHome()).getAbsolutePath();
				while (file != null && file.exists()) {
					if (file.getAbsolutePath().startsWith(root) && file.getAbsolutePath().length() > root.length()) {
						deleteEmptyDirs(file);
						file = file.getParentFile();
					} else {
						// Gone above the ZAP home dir
						break;
					}
				}
				Control.getSingleton().getExtensionLoader().addonFilesAdded();
			}
		}

		if (! this.aoc.removeAddOn(ao)) {
			result = false;
		}
		this.addOnLoaders.remove(ao.getId());
		
		if (ao.getFile() != null && ao.getFile().exists()) {
			if (!ao.getFile().delete()) {
				logger.debug("Cant delete " + ao.getFile().getAbsolutePath());
        		this.blockList.add(ao.getId());
        		this.saveBlockList();
			}
		}
		
		return result;
	}
	
	private void loadBlockList() {
        String blockStr = Model.getSingleton().getOptionsParam().getConfig().getString(ADDONS_BLOCK_LIST, null);
        if (blockStr != null && blockStr.length() > 0) {
        	for (String str : blockStr.split(",")) {
        		this.blockList.add(str);
        	}
        }
	}
	
	private void saveBlockList() {
		StringBuilder sb = new StringBuilder();

		for (String id: this.blockList) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(id);
		}

        Model.getSingleton().getOptionsParam().getConfig().setProperty(ADDONS_BLOCK_LIST, sb.toString());
        try {
			Model.getSingleton().getOptionsParam().getConfig().save();
		} catch (ConfigurationException e) {
			logger.error("Failed to save block list: " + sb.toString(), e);
		}
	}
	
	private void deleteEmptyDirs(File dir) {
		logger.debug("Deleting dir " + dir.getAbsolutePath());
		for (File d : dir.listFiles()) {
			if (d.isDirectory()) {
				deleteEmptyDirs(d);
			}
		}
		if (! dir.delete()) {
			logger.debug("Failed to delete: " + dir.getAbsolutePath());
		}
	}


    private <T> List<String> getClassNames (String packageName, Class<T> classType) {
    	List<String> listClassName = new ArrayList<>();
    	
    	listClassName.addAll(this.getLocalClassNames(packageName));
    	for (AddOn addOn : this.aoc.getAddOns()) {
        	listClassName.addAll(this.getJarClassNames(addOn.getFile(), packageName));
    	}
    	for (File jar : jars) {
    		listClassName.addAll(this.getJarClassNames(jar, packageName));
    	}
    	return listClassName;
    }
    
	public <T> List<T> getImplementors (String packageName, Class<T> classType) {
		return this.getImplementors(null, packageName, classType);
    }

	public <T> List<T> getImplementors (AddOn ao, String packageName, Class<T> classType) {
        Class<?> cls = null;
        List<T> listClass = new ArrayList<>();
        
        List<String> classNames;
        if (ao != null) {
        	classNames = this.getJarClassNames(ao.getFile(), packageName);
        } else {
        	classNames = this.getClassNames(packageName, classType);
        }
        for (String className : classNames) {
            try {
                cls = loadClass(className);
                // abstract class or interface cannot be constructed.
                if (Modifier.isAbstract(cls.getModifiers()) || Modifier.isInterface(cls.getModifiers())) {
                    continue;
                }
                if (classType.isAssignableFrom(cls)) {
                    @SuppressWarnings("unchecked")
                    Constructor<T> c = (Constructor<T>) cls.getConstructor();
                    listClass.add(c.newInstance());

                }
            } catch (Throwable e) {
            	// Often not an error
            	logger.debug(e.getMessage(), e);
            }
        }
        
        return listClass;
	}

    /**
     * Check local jar (paros.jar) or related package if any target file is found.
     *
     */
    private <T> List<String> getLocalClassNames (String packageName) {
    
        if (packageName == null || packageName.equals("")) {
            return new ArrayList<String>();
        }
        
        String folder = packageName.replace('.', '/');
        URL local = AddOnLoader.class.getClassLoader().getResource(folder);
        if (local == null) {
            return new ArrayList<String>();
        }
        String jarFile = null;
        if (local.getProtocol().equals("jar")) {
            jarFile = local.toString().substring("jar:".length());
            int pos = jarFile.indexOf("!");
            jarFile = jarFile.substring(0, pos);
            
            try {
                // ZAP: Changed to take into account the package name
                return getJarClassNames(new File(new URI(jarFile)), packageName);
            } catch (URISyntaxException e) {
            	logger.error(e.getMessage(), e);
            }
        } else {
            try {
                // ZAP: Changed to pass a FileFilter (ClassRecurseDirFileFilter)
                // and to pass the "packageName" with the dots already replaced.
                return parseClassDir(new File(new URI(local.toString())),
                              packageName.replace('.', File.separatorChar),
                              new ClassRecurseDirFileFilter(true));
            } catch (URISyntaxException e) {
            	logger.error(e.getMessage(), e);
            }
        }
        return new ArrayList<String>();
    }

    // ZAP: Changed to use only one FileFilter and the packageName is already
    // passed with the dots replaced.
    private List<String> parseClassDir(File file, String packageName, FileFilter fileFilter) {
    	List<String> classNames = new ArrayList<String> ();
        File[] listFile = file.listFiles(fileFilter);
        
        for (int i=0; i<listFile.length; i++) {
            File entry = listFile[i];
            if (entry.isDirectory()) {
            	classNames.addAll(parseClassDir (entry, packageName, fileFilter));
            	continue;
            }
            String fileName = entry.toString();
            int pos = fileName.indexOf(packageName);
            if (pos > 0) {
                String className = fileName.substring(pos).replaceAll("\\.class$","").replace(File.separatorChar, '.');
                classNames.add(className);
            }
        }
        return classNames;
    }
    
    // ZAP: Added to take into account the package name
    private List<String> getJarClassNames(File file, String packageName) {
    	List<String> classNames = new ArrayList<String> ();
        JarFile jarFile = null;
        ZipEntry entry = null;
        String className = "";
        try {
            jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                    continue;
                }
                className = entry.toString().replaceAll("\\.class$","").replaceAll("/",".");
                if (className.indexOf(packageName) >= 0) {
                    classNames.add(className);
                }
            }
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    // ZAP: Log the exception.
                    if (logger.isDebugEnabled()) {
                        logger.debug(e.getMessage(), e);
                    }
                }
            }
        }
        return classNames;
    }
    
    private static final class JarFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String fileName) {
            if (fileName.endsWith(".jar")) {
                return true;
            }
            return false;
        }
    }
    
    // ZAP: Added
    private static final class ClassRecurseDirFileFilter implements FileFilter {
        
        private boolean recurse;
        
        public ClassRecurseDirFileFilter(boolean recurse) {
            this.recurse = recurse;
        }
        
        @Override
        public boolean accept(File file) {
            if (recurse && file.isDirectory() && ! file.getName().startsWith(".")) {
                return true;
            } else if (file.isFile() && file.getName().endsWith(".class")) {
                return true;
            }
            
            return false;
        }
    }
}
