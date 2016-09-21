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
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.AbstractPlugin;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.AddOn.AddOnRunRequirements;
import org.zaproxy.zap.control.AddOn.ExtensionRunRequirements;
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

    private static final String ADDONS_RUNNABLE_BASE_KEY = "runnableAddOns";
    private static final String ADDONS_RUNNABLE_KEY = ADDONS_RUNNABLE_BASE_KEY + ".addon";
    private static final String ADDON_RUNNABLE_ID_KEY = "id";
    private static final String ADDON_RUNNABLE_VERSION_KEY = "version";
    private static final String ADDON_RUNNABLE_ALL_EXTENSIONS_KEY = "extensions.extension";

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
     * The runnable add-ons and its extensions.
     * <p>
     * The key is the add-on itself and the value its runnable extensions.
     */
    private Map<AddOn, List<String>> runnableAddOns;

    /**
     * The list of add-ons' IDs that have running issues (either the add-on itself or one of its extensions) since last run
     * because of changes in the dependencies.
     */
    private List<String> idsAddOnsWithRunningIssuesSinceLastRun;

    /*
     * Using sub-classloaders means we can unload and reload addons
     */
    private Map<String, AddOnClassLoader> addOnLoaders = new HashMap<>();

    public AddOnLoader(File[] dirs) {
        super(new URL[0], AddOnLoader.class.getClassLoader());
        
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
     * Returns a list with the IDs of add-ons that have running issues since last run, either Java version was changed, or
     * add-on dependencies are no longer met for the add-on or one of its extensions.
     *
     * @return a list with the add-ons that are not longer runnable
     * @since 2.4.0
     */
    public List<String> getIdsAddOnsWithRunningIssuesSinceLastRun() {
        return Collections.unmodifiableList(idsAddOnsWithRunningIssuesSinceLastRun);
    }

    private void loadAllAddOns() {
        runnableAddOns = new HashMap<>();
        idsAddOnsWithRunningIssuesSinceLastRun = new ArrayList<>();
        Map<AddOn, List<String>> oldRunnableAddOns = loadAddOnsRunState(aoc);
        List<AddOn> runAddons = new ArrayList<>();
        for (Iterator<AddOn> iterator = aoc.getAddOns().iterator(); iterator.hasNext();) {
            AddOn addOn = iterator.next();
            if (canLoadAddOn(addOn)) {
                AddOnRunRequirements reqs = calculateRunRequirements(addOn, aoc.getAddOns());
                if (reqs.isRunnable()) {
                    List<String> runnableExtensions;
                    if (addOn.hasExtensionsWithDeps()) {
                        runnableExtensions = getRunnableExtensionsWithDeps(reqs);
                        List<String> oldRunnableExtensions = oldRunnableAddOns.get(addOn);
                        if (oldRunnableExtensions != null && !oldRunnableExtensions.isEmpty()) {
                            oldRunnableExtensions.removeAll(runnableExtensions);
                            if (!oldRunnableExtensions.isEmpty()) {
                                idsAddOnsWithRunningIssuesSinceLastRun.add(addOn.getId());
                            }
                        }
                    } else {
                        runnableExtensions = Collections.emptyList();
                    }

                    runnableAddOns.put(addOn, runnableExtensions);
                    runAddons.add(addOn);
                } else if (oldRunnableAddOns.get(addOn) != null) {
                    idsAddOnsWithRunningIssuesSinceLastRun.add(addOn.getId());
                }
            } else {
                iterator.remove();
            }
        }

        saveAddOnsRunState(runnableAddOns);

        for (AddOn addOn : runAddons) {
            addOn.setInstallationStatus(AddOn.InstallationStatus.INSTALLED);
            createAndAddAddOnClassLoader(addOn);
        }
    }

    private static List<String> getRunnableExtensionsWithDeps(AddOnRunRequirements runRequirements) {
        List<String> runnableExtensions = new ArrayList<>();
        for (ExtensionRunRequirements extReqs : runRequirements.getExtensionRequirements()) {
            if (extReqs.isRunnable()) {
                runnableExtensions.add(extReqs.getClassname());
            }
        }
        return runnableExtensions;
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

    private static AddOnRunRequirements calculateRunRequirements(AddOn ao, Collection<AddOn> availableAddOns) {
        AddOnRunRequirements reqs = ao.calculateRunRequirements(availableAddOns);
        if (!reqs.isRunnable()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Can't run add-on " + ao.getName() + " because of missing requirements: "
                        + AddOnRunIssuesUtils.getRunningIssues(reqs));
            }
        }
        return reqs;
    }

    private AddOnClassLoader createAndAddAddOnClassLoader(AddOn ao) {
        try {
            AddOnClassLoader addOnClassLoader = addOnLoaders.get(ao.getId());
            if (addOnClassLoader != null) {
                return addOnClassLoader;
            }

            List<String> idsAddOnDependencies = ao.getIdsAddOnDependencies();
            if (idsAddOnDependencies.isEmpty()) {
                addOnClassLoader = new AddOnClassLoader(ao.getFile().toURI().toURL(), this, ao.getAddOnClassnames());
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

            addOnClassLoader = new AddOnClassLoader(ao.getFile().toURI().toURL(), this, dependencies, ao.getAddOnClassnames());
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

		AddOnRunRequirements reqs = calculateRunRequirements(ao, aoc.getInstalledAddOns());
		if (!reqs.isRunnable()) {
		    ao.setInstallationStatus(AddOn.InstallationStatus.NOT_INSTALLED);
		    return;
		}

		AddOnInstaller.install(createAndAddAddOnClassLoader(ao), ao);
		ao.setInstallationStatus(AddOn.InstallationStatus.INSTALLED);
		Control.getSingleton().getExtensionLoader().addOnInstalled(ao);

        if (runnableAddOns.get(ao) == null) {
            runnableAddOns.put(ao, getRunnableExtensionsWithDeps(reqs));
            saveAddOnsRunState(runnableAddOns);
        }

        checkAndLoadDependentExtensions();
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
                AddOnRunRequirements reqs = addOn.calculateRunRequirements(aoc.getInstalledAddOns());
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
     * Checks and loads all the extensions that have (now) all required dependencies fulfilled.
     * <p>
     * Should be called after an installation of an add-on.
     *
     * @see #addAddOnImpl(AddOn)
     * @since 2.4.0
     */
    private void checkAndLoadDependentExtensions() {
        boolean changed = false;
        for (Entry<String, AddOnClassLoader> entry : new HashMap<>(addOnLoaders).entrySet()) {
            AddOn runningAddOn = aoc.getAddOn(entry.getKey());
            for (String extClassName : runningAddOn.getExtensionsWithDeps()) {
                if (!runningAddOn.isExtensionLoaded(extClassName)) {
                    AddOn.AddOnRunRequirements reqs = runningAddOn.calculateExtensionRunRequirements(
                            extClassName,
                            aoc.getInstalledAddOns());
                    ExtensionRunRequirements extReqs = reqs.getExtensionRequirements().get(0);
                    if (extReqs.isRunnable()) {
                        List<AddOnClassLoader> dependencies = new ArrayList<>(extReqs.getDependencies().size());
                        for (AddOn addOnDep : extReqs.getDependencies()) {
                            dependencies.add(addOnLoaders.get(addOnDep.getId()));
                        }
                        AddOnClassLoader extAddOnClassLoader = new AddOnClassLoader(
                                entry.getValue(),
                                dependencies,
                                runningAddOn.getExtensionAddOnClassnames(extClassName));
                        Extension ext = loadAddOnExtension(runningAddOn, extReqs.getClassname(), extAddOnClassLoader);
                        AddOnInstaller.installAddOnExtension(runningAddOn, ext);
                        runnableAddOns.get(runningAddOn).add(extReqs.getClassname());
                        changed = true;
                    }
                }
            }
        }

        if (changed) {
            saveAddOnsRunState(runnableAddOns);
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
			if (runnableAddOns.remove(ao) != null) {
			    saveAddOnsRunState(runnableAddOns);
			}
			AddOnInstaller.uninstallAddOnFiles(ao, NULL_CALLBACK);
			removeAddOnClassLoader(ao);
			deleteAddOnFile(ao, upgrading);
			ao.setInstallationStatus(AddOn.InstallationStatus.UNINSTALLATION_FAILED);
			Control.getSingleton().getExtensionLoader().addOnUninstalled(ao, false);
			return false;
		}

		if (!this.aoc.includesAddOn(ao.getId())) {
			logger.warn("Trying to uninstall an add-on that is not installed: " + ao.getId());
			return false;
		}

		if (AddOn.InstallationStatus.NOT_INSTALLED == ao.getInstallationStatus()) {
			if (runnableAddOns.remove(ao) != null) {
			    saveAddOnsRunState(runnableAddOns);
			}
			
			deleteAddOnFile(ao, upgrading);
			
			return this.aoc.removeAddOn(ao);
		}

		unloadDependentExtensions(ao);
		softUninstallDependentAddOns(ao);

		boolean uninstalledWithoutErrors = AddOnInstaller.uninstall(ao, callback);

		if (uninstalledWithoutErrors && ! this.aoc.removeAddOn(ao)) {
			uninstalledWithoutErrors = false;
		}

		if (uninstalledWithoutErrors) {
			removeAddOnClassLoader(ao);
		}

		deleteAddOnFile(ao, upgrading);

		if (runnableAddOns.remove(ao) != null) {
		    saveAddOnsRunState(runnableAddOns);
		}
		
		ao.setInstallationStatus(uninstalledWithoutErrors
				? AddOn.InstallationStatus.AVAILABLE
				: AddOn.InstallationStatus.UNINSTALLATION_FAILED);

		Control.getSingleton().getExtensionLoader().addOnUninstalled(ao, uninstalledWithoutErrors);
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

    private void unloadDependentExtensions(AddOn ao) {
        boolean changed = true;
        for (Entry<String, AddOnClassLoader> entry : new HashMap<>(addOnLoaders).entrySet()) {
            AddOn runningAddOn = aoc.getAddOn(entry.getKey());
            for (Extension ext : runningAddOn.getLoadedExtensionsWithDeps()) {
                if (runningAddOn.dependsOn(ext, ao)) {
                    String classname = ext.getClass().getCanonicalName();
                    AddOnInstaller.uninstallAddOnExtension(runningAddOn, ext, NULL_CALLBACK);
                    try (AddOnClassLoader extensionClassLoader = (AddOnClassLoader) ext.getClass().getClassLoader()) {
                        ext = null;
                        entry.getValue().removeChildClassLoader(extensionClassLoader);
                        extensionClassLoader.clearDependencies();
                        ResourceBundle.clearCache(extensionClassLoader);
                    } catch (Exception e) {
                        logger.error("Failure while closing class loader of extension '" + classname + "':", e);
                    }
                    runnableAddOns.get(runningAddOn).remove(classname);
                    changed = true;
                }
            }
        }

        if (changed) {
            saveAddOnsRunState(runnableAddOns);
        }
    }

    private void softUninstallDependentAddOns(AddOn ao) {
        for (Entry<String, AddOnClassLoader> entry : new HashMap<>(addOnLoaders).entrySet()) {
            AddOn runningAddOn = aoc.getAddOn(entry.getKey());
            if (runningAddOn.dependsOn(ao)) {
                softUninstallDependentAddOns(runningAddOn);

                softUninstall(runningAddOn);
            }
        }
    }

    private void softUninstall(AddOn addOn) {
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
        Control.getSingleton()
                .getExtensionLoader()
                .addOnSoftUninstalled(addOn, status == AddOn.InstallationStatus.NOT_INSTALLED);
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
	 * <p>
	 * Extensions with unfulfilled dependencies are not be returned.
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
     * Extensions with unfulfilled dependencies are not be returned.
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
        AddOnClassLoader addOnClassLoader = this.addOnLoaders.get(addOn.getId());
        if (addOnClassLoader == null) {
            return Collections.emptyList();
        }

        List<Extension> extensions = new ArrayList<>();
        extensions.addAll(loadAddOnExtensions(addOn, addOn.getExtensions(), addOnClassLoader));

        if (addOn.hasExtensionsWithDeps()) {
            AddOn.AddOnRunRequirements reqs = addOn.calculateRunRequirements(aoc.getInstalledAddOns());
            for (ExtensionRunRequirements extReqs : reqs.getExtensionRequirements()) {
                if (extReqs.isRunnable()) {
                    List<AddOnClassLoader> dependencies = new ArrayList<>(extReqs.getDependencies().size());
                    for (AddOn addOnDep : extReqs.getDependencies()) {
                        dependencies.add(addOnLoaders.get(addOnDep.getId()));
                    }
                    AddOnClassLoader extAddOnClassLoader = new AddOnClassLoader(
                            addOnClassLoader,
                            dependencies,
                            addOn.getExtensionAddOnClassnames(extReqs.getClassname()));
                    Extension ext = loadAddOnExtension(addOn, extReqs.getClassname(), extAddOnClassLoader);
                    if (ext != null) {
                        extensions.add(ext);
                    }
                } else if (logger.isDebugEnabled()) {
                    logger.debug("Can't run extension '" + extReqs.getClassname() + "' of add-on '" + addOn.getName()
                            + "' because of missing requirements: " + AddOnRunIssuesUtils.getRunningIssues(extReqs));
                }
            }
        }
        return extensions;
    }

    private List<Extension> loadAddOnExtensions(AddOn addOn, List<String> extensions, AddOnClassLoader addOnClassLoader) {
        if (extensions == null || extensions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Extension> list = new ArrayList<>(extensions.size());
        for (String extName : extensions) {
            Extension ext = loadAddOnExtension(addOn, extName, addOnClassLoader);
            if (ext != null) {
                list.add(ext);
            }
        }
        return list;
    }

    private static Extension loadAddOnExtension(AddOn addOn, String classname, AddOnClassLoader addOnClassLoader) {
        Extension extension = AddOnLoaderUtils
                .loadAndInstantiateClass(addOnClassLoader, classname, Extension.class, "extension");
        if (extension != null) {
            addOn.addLoadedExtension(extension);
        }
        return extension;
    }

	/**
	 * Gets the active scan rules of all the loaded add-ons.
	 * <p>
	 * The discovery of active scan rules is done by resorting to {@code ZapAddOn.xml} file bundled in the add-ons.
	 *
	 * @return an unmodifiable {@code List} with all the active scan rules, never {@code null}
	 * @since 2.4.0
	 * @see AbstractPlugin
	 */
	public List<AbstractPlugin> getActiveScanRules () {
		ArrayList<AbstractPlugin> list = new ArrayList<>();
		for (AddOn addOn : getAddOnCollection().getAddOns()) {
			AddOnClassLoader addOnClassLoader = this.addOnLoaders.get(addOn.getId());
			if (addOnClassLoader != null) {
				list.addAll(AddOnLoaderUtils.getActiveScanRules(addOn, addOnClassLoader));
			}
        }
		list.trimToSize();
		return Collections.unmodifiableList(list);
	}

	/**
	 * Gets the passive scan rules of all the loaded add-ons.
	 * <p>
	 * The discovery of passive scan rules is done by resorting to {@code ZapAddOn.xml} file bundled in the add-ons.
	 *
	 * @return an unmodifiable {@code List} with all the passive scan rules, never {@code null}
	 * @since 2.4.0
	 * @see PluginPassiveScanner
	 */
	public List<PluginPassiveScanner> getPassiveScanRules() {
		ArrayList<PluginPassiveScanner> list = new ArrayList<>();
		for (AddOn addOn : getAddOnCollection().getAddOns()) {
			AddOnClassLoader addOnClassLoader = this.addOnLoaders.get(addOn.getId());
			if (addOnClassLoader != null) {
				list.addAll(AddOnLoaderUtils.getPassiveScanRules(addOn, addOnClassLoader));
			}
        }
		list.trimToSize();
		return Collections.unmodifiableList(list);
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
            	logger.debug(e.getMessage(), e);
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

    private static Map<AddOn, List<String>> loadAddOnsRunState(AddOnCollection addOnCollection) {
        List<HierarchicalConfiguration> savedAddOns = ((HierarchicalConfiguration) Model.getSingleton()
                .getOptionsParam()
                .getConfig()).configurationsAt(ADDONS_RUNNABLE_KEY);

        Map<AddOn, List<String>> runnableAddOns = new HashMap<>();
        for (HierarchicalConfiguration savedAddOn : savedAddOns) {
            AddOn addOn = addOnCollection.getAddOn(savedAddOn.getString(ADDON_RUNNABLE_ID_KEY, ""));
            if (addOn == null) {
                // No longer exists, skip it.
                continue;
            }
            int version = savedAddOn.getInt(ADDON_RUNNABLE_VERSION_KEY, -1);
            if (version == -1 || addOn.getFileVersion() != version) {
                // No version or not the same version, skip it.
                continue;
            }
            List<String> runnableExtensions = new ArrayList<>();
            List<String> currentExtensions = addOn.getExtensionsWithDeps();
            for (String savedExtension : savedAddOn.getStringArray(ADDON_RUNNABLE_ALL_EXTENSIONS_KEY)) {
                if (currentExtensions.contains(savedExtension)) {
                    runnableExtensions.add(savedExtension);
                }
            }
            runnableAddOns.put(addOn, runnableExtensions);
        }

        return runnableAddOns;
    }

    private static void saveAddOnsRunState(Map<AddOn, List<String>> runnableAddOns) {
        HierarchicalConfiguration config = (HierarchicalConfiguration) Model.getSingleton().getOptionsParam().getConfig();
        config.clearTree(ADDONS_RUNNABLE_BASE_KEY);

        int i = 0;
        for (Map.Entry<AddOn, List<String>> runnableAddOnEntry : runnableAddOns.entrySet()) {
            String elementBaseKey = ADDONS_RUNNABLE_KEY + "(" + i + ").";
            AddOn addOn = runnableAddOnEntry.getKey();

            config.setProperty(elementBaseKey + ADDON_RUNNABLE_ID_KEY, addOn.getId());
            config.setProperty(elementBaseKey + ADDON_RUNNABLE_VERSION_KEY, Integer.valueOf(addOn.getFileVersion()));

            String extensionBaseKey = elementBaseKey + ADDON_RUNNABLE_ALL_EXTENSIONS_KEY;
            for (String extension : runnableAddOnEntry.getValue()) {
                config.addProperty(extensionBaseKey, extension);
            }

            i++;
        }

        try {
            Model.getSingleton().getOptionsParam().getConfig().save();
        } catch (ConfigurationException e) {
            logger.error("Failed to save state of runnable add-ons:", e);
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
