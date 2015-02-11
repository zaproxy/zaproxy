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

import java.awt.EventQueue;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.AbstractPlugin;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.AddOn.RunRequirements;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;

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

	public static final String ADDONS_BLOCK_LIST = "addons.block";

    private static final String ADDONS_RUNNABLE_LIST = "addons.runnable";

    /**
     * A "null" object, for use when no callback is given during the uninstallation process.
     */
    private static final AddOnUninstallationProgressCallback NULL_CALLBACK = new NullUninstallationProgressCallBack();
	
    private static final Logger logger = Logger.getLogger(AddOnLoader.class);
    private AddOnCollection aoc = null;
    private List<File> jars = new ArrayList<>();
    /**
     * Addons can be included in the ZAP release, in which case the user might not have permissions to delete the files.
     * To support the removal of such addons we just maintain a 'block list' in the configs which is a comma separated
     * list of the addon ids that the user has uninstalled
     */
    private List<String> blockList = new ArrayList<>();

    /**
     * The list of add-ons' ID that are runnable.
     */
    private List<String> idsRunnableAddOns;

    /**
     * The list of add-ons that are no longer runnable since the last run.
     */
    private List<String> addOnsNoLongerRunnableSinceLastRun;

    /*
     * Using sub-classloaders means we can unload and reload addons
     */
    private Map<String, AddOnClassLoader> addOnLoaders = new HashMap<>();

    public AddOnLoader(File[] dirs) {
        super(new URL[0]);
        
        this.loadBlockList();

        this.aoc = new AddOnCollection(dirs);
        loadAllAddOns();

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
    	
    	// Install any files that are not already present
		for (Entry<String, AddOnClassLoader> entry : addOnLoaders.entrySet()) {
			AddOnInstaller.installMissingAddOnFiles(entry.getValue(), getAddOnCollection().getAddOn(entry.getKey()));
		}

    }

    /**
     * Returns a list with the IDs of add-ons that are no longer runnable since last run, either Java version was changed or
     * dependencies of an add-on are no longer met.
     *
     * @return a list with the add-ons that are not longer runnable
     * @since 2.4.0
     */
    public List<String> getIdsAddOnsNoLongerRunnableSinceLastRun() {
        return Collections.unmodifiableList(addOnsNoLongerRunnableSinceLastRun);
    }

    private void loadAllAddOns() {
        idsRunnableAddOns  = new ArrayList<>();
        addOnsNoLongerRunnableSinceLastRun = new ArrayList<>();
        List<String> idsOldAddOns = loadList(ADDONS_RUNNABLE_LIST);
        List<AddOn> runnableAddons = new ArrayList<>();
        for (Iterator<AddOn> iterator = aoc.getAddOns().iterator(); iterator.hasNext();) {
            AddOn addOn = iterator.next();
            if (canLoadAddOn(addOn)) {
                if (canRunAddOn(addOn, aoc.getAddOns())) {
                    runnableAddons.add(addOn);
                    idsRunnableAddOns.add(addOn.getId());
                } else if (idsOldAddOns.contains(addOn.getId())) {
                    addOnsNoLongerRunnableSinceLastRun.add(addOn.getId());
                }
            } else {
                iterator.remove();
            }
        }

        saveList(ADDONS_RUNNABLE_LIST, idsRunnableAddOns);

        for (AddOn addOn : runnableAddons) {
            addOn.setInstallationStatus(AddOn.InstallationStatus.INSTALLED);
            createAndAddAddOnClassLoader(addOn);
        }
    }

    private boolean canLoadAddOn(AddOn ao) {
        if (blockList.contains(ao.getId())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Can't load add-on " + ao.getName()
                        + " it's on the block list (add-on uninstalled but the file couldn't be removed).");
            }
            return false;
        }

        if (!ao.canLoadInCurrentVersion()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Can't load add-on " + ao.getName() + " because of ZAP version constraints; Not before="
                        + ao.getNotBeforeVersion() + " Not from=" + ao.getNotFromVersion() + " Current Version="
                        + Constant.PROGRAM_VERSION);
            }
            return false;
        }
        return true;
    }

    private static boolean canRunAddOn(AddOn ao, Collection<AddOn> availableAddOns) {
        RunRequirements reqs = ao.calculateRunRequirements(availableAddOns);
        if (!reqs.isRunnable()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Can't run add-on " + ao.getName() + " because of missing requirements: "
                        + AddOnRunIssuesUtils.getRunningIssues(reqs));
            }
            return false;
        }
        return true;
    }

    private AddOnClassLoader createAndAddAddOnClassLoader(AddOn ao) {
        try {
            AddOnClassLoader addOnClassLoader = addOnLoaders.get(ao.getId());
            if (addOnClassLoader != null) {
                return addOnClassLoader;
            }

            List<String> idsAddOnDependencies = ao.getIdsAddOnDependencies();
            if (idsAddOnDependencies.isEmpty()) {
                addOnClassLoader = new AddOnClassLoader(ao.getFile().toURI().toURL(), this);
                this.addOnLoaders.put(ao.getId(), addOnClassLoader);
                return addOnClassLoader;
            }

            List<AddOnClassLoader> dependencies = new ArrayList<>(idsAddOnDependencies.size());
            for (String addOnId : idsAddOnDependencies) {
                addOnClassLoader = addOnLoaders.get(addOnId);
                if (addOnClassLoader == null) {
                    addOnClassLoader = createAndAddAddOnClassLoader(aoc.getAddOn(addOnId));
                }
                dependencies.add(addOnClassLoader);
            }

            addOnClassLoader = new AddOnClassLoader(ao.getFile().toURI().toURL(), this, dependencies);
            this.addOnLoaders.put(ao.getId(), addOnClassLoader);
            return addOnClassLoader;
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Failed to convert URL for AddOnClassLoader " + ao.getFile().toURI(), e);
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
			return loadClass(name, false);
		} catch (ClassNotFoundException e) {
			// Continue for now
		}
        for (AddOnClassLoader loader : addOnLoaders.values()) {
            try {
    			return loader.loadClass(name);
    		} catch (ClassNotFoundException e) {
    			// Continue for now
    		}
        }
        throw new ClassNotFoundException(name);
    }
    
    @Override
    public URL getResource(String name) {
		URL url = super.getResource(name);
		if (url != null) {
			return url;
		}
		for (AddOnClassLoader loader : addOnLoaders.values()) {
			url = loader.findResourceInAddOn(name);
			if (url != null) {
				return url;
			}
		}
		return url;
    }

    public AddOnCollection getAddOnCollection() {
    	return this.aoc;
    }
    
    private void addDirectory (File dir) {
    	if (dir == null) {
    		logger.error("Null directory supplied");
    		return;
    	}
    	if (! dir.exists()) {
    		logger.error("No such directory: " + dir.getAbsolutePath());
    		return;
    	}
    	if (! dir.isDirectory()) {
    		logger.error("Not a directory: " + dir.getAbsolutePath());
    		return;
    	}

        // Load the jar files
        File[] listJars = dir.listFiles(new JarFilenameFilter());
        if (listJars != null) {
        	for (File jar : listJars) {
        		this.jars.add(jar);
        	}
        }
    }
    
    public synchronized void addAddon(AddOn ao) {
    	if (! ao.canLoadInCurrentVersion()) {
    		throw new IllegalArgumentException("Cant load add-on " + ao.getName() + 
    				" Not before=" + ao.getNotBeforeVersion() + " Not from=" + ao.getNotFromVersion() + 
    				" Version=" + Constant.PROGRAM_VERSION);
    	}
    	if (!this.aoc.addAddOn(ao)) {
    	    return;
    	}

		addAddOnImpl(ao);
	}

	private void addAddOnImpl(AddOn ao) {
		if (AddOn.InstallationStatus.INSTALLED == ao.getInstallationStatus()) {
			return;
		}

    	if (this.blockList.contains(ao.getId())) {
    		// Explicitly being added back, so remove from the block list
    		this.blockList.remove(ao.getId());
    		this.saveBlockList();
    	}

		if (!isDynamicallyInstallable(ao)) {
			return;
		}

		if (!canRunAddOn(ao, aoc.getInstalledAddOns())) {
		    ao.setInstallationStatus(AddOn.InstallationStatus.NOT_INSTALLED);
		    return;
		}

		AddOnInstaller.install(createAndAddAddOnClassLoader(ao), ao);
		ao.setInstallationStatus(AddOn.InstallationStatus.INSTALLED);

        if (!idsRunnableAddOns.contains(ao.getId())) {
            idsRunnableAddOns.add(ao.getId());
            saveList(ADDONS_RUNNABLE_LIST, idsRunnableAddOns);
        }

		checkAndInstallAddOnsNotInstalled();

        if (View.isInitialised()) {
            EventQueue.invokeLater(new Runnable() {
                
                @Override
                public void run() {
                    View.getSingleton().refreshTabViewMenus();
                }
            });
        }
	}

    /**
     * Checks and installs all the add-ons whose installation status is {@code NOT_INSTALLED} that have (now) all required
     * dependencies fulfilled.
     * <p>
     * Should be called after an installation of an add-on.
     *
     * @see #addAddOnImpl(AddOn)
     * @see AddOn.InstallationStatus#NOT_INSTALLED
     * @since 2.4.0
     */
    private void checkAndInstallAddOnsNotInstalled() {
        List<AddOn> runnableAddOns = new ArrayList<>();
        for (AddOn addOn : aoc.getAddOns()) {
            if (AddOn.InstallationStatus.NOT_INSTALLED == addOn.getInstallationStatus() && addOnLoaders.get(addOn.getId()) == null) {
                RunRequirements reqs = addOn.calculateRunRequirements(aoc.getInstalledAddOns());
                if (reqs.isRunnable()) {
                    runnableAddOns.add(addOn);
                }
            }
        }

        for (AddOn addOn : runnableAddOns) {
            addAddOnImpl(addOn);
        }
    }

    /**
     * Tells whether or not the given {@code addOn} is dynamically installable.
     * <p>
     * It checks if the given {@code addOn} is dynamically installable by calling the method {@code AddOn#hasZapAddOnEntry()}.
     * 
     * @param addOn the add-on that will be checked
     * @return {@code true} if the given add-on is dynamically installable, {@code false} otherwise.
     * @see AddOn#hasZapAddOnEntry()
     * @since 2.3.0
     */
    private static boolean isDynamicallyInstallable(AddOn addOn) {
        return addOn.hasZapAddOnEntry();
    }

	public synchronized boolean removeAddOn(AddOn ao, boolean upgrading, AddOnUninstallationProgressCallback progressCallback) {
		AddOnUninstallationProgressCallback callback = (progressCallback == null) ? NULL_CALLBACK : progressCallback;

		callback.uninstallingAddOn(ao, upgrading);
		boolean removed = removeAddOnImpl(ao, upgrading, callback);
		callback.addOnUninstalled(removed);

		return removed;
	}

	private boolean removeAddOnImpl(AddOn ao, boolean upgrading, AddOnUninstallationProgressCallback callback) {
		if (!isDynamicallyInstallable(ao)) {
			return false;
		}

		if (AddOn.InstallationStatus.SOFT_UNINSTALLATION_FAILED == ao.getInstallationStatus()) {
			if (idsRunnableAddOns.remove(ao.getId())) {
				saveList(ADDONS_RUNNABLE_LIST, idsRunnableAddOns);
			}
			AddOnInstaller.uninstallAddOnFiles(ao, NULL_CALLBACK);
			removeAddOnClassLoader(ao);
			deleteAddOnFile(ao, upgrading);
			ao.setInstallationStatus(AddOn.InstallationStatus.UNINSTALLATION_FAILED);
			return false;
		}

		if (!this.aoc.includesAddOn(ao.getId())) {
			logger.warn("Trying to uninstall an add-on that is not installed: " + ao.getId());
			return false;
		}

		if (AddOn.InstallationStatus.NOT_INSTALLED == ao.getInstallationStatus()) {
			if (idsRunnableAddOns.remove(ao.getId())) {
				saveList(ADDONS_RUNNABLE_LIST, idsRunnableAddOns);
			}
			
			deleteAddOnFile(ao, upgrading);
			
			return this.aoc.removeAddOn(ao);
		}

		softUninstallDependentAddOns(ao, callback);

		boolean uninstalledWithoutErrors = AddOnInstaller.uninstall(ao, callback);

		if (uninstalledWithoutErrors && ! this.aoc.removeAddOn(ao)) {
			uninstalledWithoutErrors = false;
		}

		if (uninstalledWithoutErrors) {
			removeAddOnClassLoader(ao);
		}

		deleteAddOnFile(ao, upgrading);

		if (idsRunnableAddOns.remove(ao.getId())) {
			saveList(ADDONS_RUNNABLE_LIST, idsRunnableAddOns);
		}
		
		ao.setInstallationStatus(uninstalledWithoutErrors
				? AddOn.InstallationStatus.AVAILABLE
				: AddOn.InstallationStatus.UNINSTALLATION_FAILED);

		return uninstalledWithoutErrors;
	}
	
	private void deleteAddOnFile(AddOn addOn, boolean upgrading) {
		if (addOn.getFile() != null && addOn.getFile().exists()) {
			if (!addOn.getFile().delete() && !upgrading) {
				logger.debug("Cant delete " + addOn.getFile().getAbsolutePath());
				this.blockList.add(addOn.getId());
				this.saveBlockList();
			}
		}
	}

    private void removeAddOnClassLoader(AddOn addOn) {
        if (this.addOnLoaders.containsKey(addOn.getId())) {
            try (AddOnClassLoader addOnClassLoader = this.addOnLoaders.remove(addOn.getId())) {
                if (!addOn.getIdsAddOnDependencies().isEmpty()) {
                    addOnClassLoader.clearDependencies();
                }
                ResourceBundle.clearCache(addOnClassLoader);
            } catch (Exception e) {
                logger.error("Failure while closing class loader of " + addOn.getId() + " add-on:", e);
            }
        }
    }

    private void softUninstallDependentAddOns(AddOn ao, AddOnUninstallationProgressCallback callback) {
        for (Entry<String, AddOnClassLoader> entry : new HashMap<>(addOnLoaders).entrySet()) {
            AddOn runningAddOn = aoc.getAddOn(entry.getKey());
            if (runningAddOn.dependsOn(ao)) {
                softUninstallDependentAddOns(runningAddOn, callback);

                softUninstall(runningAddOn, callback);
            }
        }
    }

    private void softUninstall(AddOn addOn, AddOnUninstallationProgressCallback callback) {
        if (AddOn.InstallationStatus.INSTALLED != addOn.getInstallationStatus()) {
            return;
        }

        AddOn.InstallationStatus status;
        if (isDynamicallyInstallable(addOn) && AddOnInstaller.softUninstall(addOn, NULL_CALLBACK)) {
            removeAddOnClassLoader(addOn);
            status = AddOn.InstallationStatus.NOT_INSTALLED;
        } else {
            status = AddOn.InstallationStatus.SOFT_UNINSTALLATION_FAILED;
        }

        addOn.setInstallationStatus(status);
    }

	private void loadBlockList() {
	    blockList = loadList(ADDONS_BLOCK_LIST);
	}
	
	private void saveBlockList() {
		saveList(ADDONS_BLOCK_LIST, this.blockList);
	}

    private <T> List<ClassNameWrapper> getClassNames (String packageName, Class<T> classType) {
    	List<ClassNameWrapper> listClassName = new ArrayList<>();
    	
    	listClassName.addAll(this.getLocalClassNames(packageName));
    	for (String addOnId : this.addOnLoaders.keySet()) {
        	listClassName.addAll(this.getJarClassNames(aoc.getAddOn(addOnId), packageName));
    	}
    	for (File jar : jars) {
    		listClassName.addAll(this.getJarClassNames(this.getClass().getClassLoader(), jar, packageName));
    	}
    	return listClassName;
    }

	/**
	 * Returns all the {@code Extension}s of all the installed add-ons.
	 * <p>
	 * The discovery of {@code Extension}s is done by resorting to the {@code ZapAddOn.xml} file bundled in the add-ons.
	 *
	 * @return a list containing all {@code Extension}s of all installed add-ons
	 * @since 2.4.0
	 * @see Extension
	 * @see #getExtensions(AddOn)
	 */
	public List<Extension> getExtensions () {
		List<Extension> list = new ArrayList<Extension>();
		for (AddOn addOn : getAddOnCollection().getAddOns()) {
			list.addAll(getExtensions(addOn));
        }
		
		return list;
	}

    /**
     * Returns all {@code Extension}s of the given {@code addOn}.
     * <p>
     * The discovery of {@code Extension}s is done by resorting to {@code ZapAddOn.xml} file bundled in the add-on.
     * <p>
     * <strong>Note:</strong> If the add-on is not installed the method returns an empty list.
     *
     * @param addOn the add-on whose extensions will be returned
     * @return a list containing the {@code Extension}s of the given {@code addOn}
     * @since 2.4.0
     * @see Extension
     * @see #getExtensions()
     */
    public List<Extension> getExtensions(AddOn addOn) {
        List<String> extensions = addOn.getExtensions();
        if (extensions == null || extensions.isEmpty()) {
            return Collections.emptyList();
        }
        AddOnClassLoader addOnClassLoader = this.addOnLoaders.get(addOn.getId());
        if (addOnClassLoader == null) {
            return Collections.emptyList();
        }

        List<Extension> list = new ArrayList<>(extensions.size());
        for (String extName : extensions) {
            Class<?> cls;
            try {
                cls = addOnClassLoader.loadClass(extName);
            } catch (ClassNotFoundException e) {
                logger.error("Declared extension was not found: " + extName, e);
                continue;
            }

            if (Modifier.isAbstract(cls.getModifiers()) || Modifier.isInterface(cls.getModifiers())) {
                logger.error("Declared \"extension\" is abstract or an interface: " + extName);
                continue;
            }

            if (!Extension.class.isAssignableFrom(cls)) {
                logger.error("Declared \"extension\" is not of type Extension: " + extName);
                continue;
            }

            try {
                @SuppressWarnings("unchecked")
                Constructor<Extension> c = (Constructor<Extension>) cls.getConstructor();
                list.add(c.newInstance());
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        }

        addOn.setLoadedExtensions(list);
        return list;
    }

	@SuppressWarnings("unchecked")
	public List<AbstractPlugin> getActiveScanRules () {
		List<AbstractPlugin> list = new ArrayList<AbstractPlugin>();
		for (AddOn addOn : getAddOnCollection().getAddOns()) {
			if (addOn.getAscanrules() != null) {
				for (String extName : addOn.getAscanrules()) {
					try {
						Class<?> cls = this.addOnLoaders.get(addOn.getId()).loadClass(extName);
	                    Constructor<?> c = (Constructor<?>) cls.getConstructor();
	                    AbstractPlugin plugin = ((Constructor<AbstractPlugin>)c).newInstance();
	                    plugin.setStatus(addOn.getStatus());
	                    list.add(plugin);
					} catch (Exception e) {
		            	logger.debug(e.getMessage());
					}
				}
			}
        }
		
		return list;
	}


	@SuppressWarnings("unchecked")
	public List<PluginPassiveScanner> getPassiveScanRules() {
		List<PluginPassiveScanner> list = new ArrayList<PluginPassiveScanner>();
		for (AddOn addOn : getAddOnCollection().getAddOns()) {
			if (addOn.getPscanrules() != null) {
				for (String extName : addOn.getPscanrules()) {
					try {
						Class<?> cls = this.addOnLoaders.get(addOn.getId()).loadClass(extName);
	                    Constructor<?> c = (Constructor<?>) cls.getConstructor();
						PluginPassiveScanner plugin = ((Constructor<PluginPassiveScanner>)c).newInstance();
	                    plugin.setStatus(addOn.getStatus());
	                    list.add(plugin);
					} catch (Exception e) {
		            	logger.debug(e.getMessage());
					}
				}
			}
        }
		
		return list;
	}

	public <T> List<T> getImplementors (String packageName, Class<T> classType) {
		return this.getImplementors(null, packageName, classType);
    }

	public <T> List<T> getImplementors (AddOn ao, String packageName, Class<T> classType) {
        Class<?> cls = null;
        List<T> listClass = new ArrayList<>();
        
        List<ClassNameWrapper> classNames;
        if (ao != null) {
        	classNames = this.getJarClassNames(ao, packageName);
        } else {
        	classNames = this.getClassNames(packageName, classType);
        }
        for (ClassNameWrapper classWrapper : classNames) {
            try {
                cls = classWrapper.getCl().loadClass(classWrapper.getClassName());
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
            	logger.debug(e.getMessage());
            }
        }
        return listClass;
	}

    /**
     * Check local jar (paros.jar) or related package if any target file is found.
     *
     */
    private List<ClassNameWrapper> getLocalClassNames (String packageName) {
    
        if (packageName == null || packageName.equals("")) {
            return Collections.emptyList();
        }
        
        String folder = packageName.replace('.', '/');
        URL local = AddOnLoader.class.getClassLoader().getResource(folder);
        if (local == null) {
            return Collections.emptyList();
        }
        String jarFile = null;
        if (local.getProtocol().equals("jar")) {
            jarFile = local.toString().substring("jar:".length());
            int pos = jarFile.indexOf("!");
            jarFile = jarFile.substring(0, pos);
            
            try {
                // ZAP: Changed to take into account the package name
                return getJarClassNames(this.getClass().getClassLoader(), new File(new URI(jarFile)), packageName);
            } catch (URISyntaxException e) {
            	logger.error(e.getMessage(), e);
            }
        } else {
            try {
                // ZAP: Changed to pass a FileFilter (ClassRecurseDirFileFilter)
                // and to pass the "packageName" with the dots already replaced.
                return parseClassDir(this.getClass().getClassLoader(), new File(new URI(local.toString())),
                              packageName.replace('.', File.separatorChar),
                              new ClassRecurseDirFileFilter(true));
            } catch (URISyntaxException e) {
            	logger.error(e.getMessage(), e);
            }
        }
        return Collections.emptyList();
    }

    // ZAP: Changed to use only one FileFilter and the packageName is already
    // passed with the dots replaced.
    private List<ClassNameWrapper> parseClassDir(ClassLoader cl, File file, String packageName, FileFilter fileFilter) {
    	List<ClassNameWrapper> classNames = new ArrayList<> ();
        File[] listFile = file.listFiles(fileFilter);
        
        for (int i=0; i<listFile.length; i++) {
            File entry = listFile[i];
            if (entry.isDirectory()) {
            	classNames.addAll(parseClassDir (cl, entry, packageName, fileFilter));
            	continue;
            }
            String fileName = entry.toString();
            int pos = fileName.indexOf(packageName);
            if (pos > 0) {
                String className = fileName.substring(pos).replaceAll("\\.class$","").replace(File.separatorChar, '.');
                classNames.add(new ClassNameWrapper(cl, className));
            }
        }
        return classNames;
    }
    
    // ZAP: Added to take into account the package name
    private List<ClassNameWrapper> getJarClassNames(ClassLoader cl, File file, String packageName) {
    	List<ClassNameWrapper> classNames = new ArrayList<> ();
        ZipEntry entry = null;
        String className = "";
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                    continue;
                }
                className = entry.toString().replaceAll("\\.class$","").replaceAll("/",".");
                if (className.indexOf(packageName) >= 0) {
                    classNames.add(new ClassNameWrapper(cl, className));
                }
            }
        } catch (Exception e) {
        	logger.error("Failed to open file: " + file.getAbsolutePath(), e);
        }
        return classNames;
    }

    private List<ClassNameWrapper> getJarClassNames(AddOn ao, String packageName) {
    	List<ClassNameWrapper> classNames = new ArrayList<> ();
        ZipEntry entry = null;
        String className = "";
        try (JarFile jarFile = new JarFile(ao.getFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                    continue;
                }
                className = entry.toString().replaceAll("\\.class$","").replaceAll("/",".");
                if (className.indexOf(packageName) >= 0) {
                    classNames.add(new ClassNameWrapper(this.addOnLoaders.get(ao.getId()), className));
                }
            }
        } catch (Exception e) {
        	logger.error("Failed to open file: " + ao.getFile().getAbsolutePath(), e);
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
    
    private class ClassNameWrapper {
    	private ClassLoader cl;
    	private String className;
		public ClassNameWrapper(ClassLoader cl, String className) {
			super();
			this.cl = cl;
			this.className = className;
		}
		public ClassLoader getCl() {
			return cl;
		}
		public String getClassName() {
			return className;
		}
    	
    }

    private static List<String> loadList(String key) {
        List<String> data = new ArrayList<>();
        String blockStr = Model.getSingleton().getOptionsParam().getConfig().getString(key, null);
        if (blockStr != null && blockStr.length() > 0) {
            for (String str : blockStr.split(",")) {
                data.add(str);
            }
        }
        return data;
    }

    private static void saveList(String key, List<String> list) {
        StringBuilder sb = new StringBuilder();

        for (String id: list) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(id);
        }

        Model.getSingleton().getOptionsParam().getConfig().setProperty(key, sb.toString());
        try {
            Model.getSingleton().getOptionsParam().getConfig().save();
        } catch (ConfigurationException e) {
            logger.error("Failed to save list [" + key + "]: " + sb.toString(), e);
        }
    }

    /**
     * An {@code UninstallationProgressCallback} that does nothing. A "{@code null}" object, for use when no callback is given
     * during the uninstallation process.
     */
    private static class NullUninstallationProgressCallBack implements AddOnUninstallationProgressCallback {

        @Override
        public void uninstallingAddOn(AddOn addOn, boolean updating) {
        }

        @Override
        public void activeScanRulesWillBeRemoved(int numberOfRules) {
        }

        @Override
        public void activeScanRuleRemoved(String name) {
        }

        @Override
        public void passiveScanRulesWillBeRemoved(int numberOfRules) {
        }

        @Override
        public void passiveScanRuleRemoved(String name) {
        }

        @Override
        public void filesWillBeRemoved(int numberOfFiles) {
        }

        @Override
        public void fileRemoved() {
        }

        @Override
        public void extensionsWillBeRemoved(int numberOfExtensions) {
        }

        @Override
        public void extensionRemoved(String name) {
        }

        @Override
        public void addOnUninstalled(boolean uninstalled) {
        }
    }
}
