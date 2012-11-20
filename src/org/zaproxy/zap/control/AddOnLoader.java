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
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;

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

    private static final Logger logger = Logger.getLogger(AddOnLoader.class);
    private List<AddOn> addOns = new ArrayList<AddOn>();
    private List<File> jars = new ArrayList<File>();

    public AddOnLoader(File[] dirs) {
        super(new URL[0]);
        
        if (dirs != null) {
        	for (File dir : dirs) {
                try {
					this.addDirectory(dir);
				} catch (Exception e) {
		    		logger.error(e.getMessage(), e);
				}
        	}
        }
        
    	for (AddOn addOn : addOns) {
            try {
				this.addURL(addOn.getFile().toURI().toURL());
			} catch (MalformedURLException e) {
	    		logger.error(e.getMessage(), e);
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
        File[] listFile = dir.listFiles(new ZapFilenameFilter());

        List<AddOn> removeAddOns = new ArrayList<AddOn>();

        if (listFile != null) {
        	for (File addOnFile : listFile) {
            	AddOn ao = new AddOn(addOnFile);
            	for (AddOn addOn : addOns) {
            		if (ao.isSameAddOn(addOn) && ao.isUpdateTo(addOn)) {
            			// Remove them below so we're not changing a list we're iterating through
            			removeAddOns.add(addOn);
            		}
            	}
            	this.addOns.add(ao);
	        }
	    	for (AddOn remAddOn : removeAddOns) {
	    		this.addOns.remove(remAddOn);
	    	}
        }

        // Load the jar files
        File[] listJars = dir.listFiles(new JarFilenameFilter());
        if (listJars != null) {
        	for (File jar : listJars) {
        		this.jars.add(jar);
        	}
        }
    }
    
    private <T> List<String> getClassNames (String packageName, Class<T> classType) {
    	List<String> listClassName = new ArrayList<>();
    	
    	listClassName.addAll(this.getLocalClassNames(packageName));
    	for (AddOn addOn : addOns) {
        	listClassName.addAll(this.getJarClassNames(addOn.getFile(), packageName));
    	}
    	for (File jar : jars) {
    		listClassName.addAll(this.getJarClassNames(jar, packageName));
    	}
    	return listClassName;
    }
    
	public <T> List<T> getImplementors (String packageName, Class<T> classType) {
        Class<?> cls = null;
        List<T> listClass = new ArrayList<>();
        List<String> classNames = this.getClassNames(packageName, classType);
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
    
    private static final class ZapFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String fileName) {
        	return AddOn.isAddOn(dir);
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
