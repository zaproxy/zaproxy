/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/11/09 Added recursive option and logging
// ZAP: 2011/11/22 Added parseJarFile(File file, String packageName) to take
// into account the package name when the local jar is loaded. Removed warnings
// (getFilteredObeject).
// ZAP: 2012/04/23 Changed the constructor DynamicLoader(String, String, boolean),
// the methods searchJars, checkLocal, getFilteredObject, parseClassDir and
// parseJarFile, added the classes JarFilenameFilter and ClassRecurseDirFileFilter,
// removed the instance variable "directory" and changed "listClassName" to use
// an ArrayList.

package org.parosproxy.paros.common;

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
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DynamicLoader extends URLClassLoader {

    // ZAP: Removed the instance variable "directory".

    private static final Logger logger = Logger.getLogger(DynamicLoader.class);

    // ZAP: Changed to use an ArrayList instead of Vector.
    private List<String> listClassName = new ArrayList<String>();
    
    public DynamicLoader(String directory, String packageName) {
    	this(directory, packageName, false);
    }
    
    public DynamicLoader(String directory, String packageName, boolean recurse) {
        super(new URL[0]);
        // ZAP: Removed the initialisation of the instance variable "directory".
        checkLocal(packageName, recurse);
        // ZAP: Changed to pass the parameter "directory" to the method searchJars.
        searchJars(directory);
    }
    
    // ZAP: Changed to receive the directory as parameter instead of using the
    // instance variable.
    private void searchJars(String directory) {
        File dir = new File(directory);
        if (! dir.exists() || dir.isFile()) {
        	return;
        }
        // ZAP: Changed to use the class JarFilenameFilter.
        File[] listFile = dir.listFiles(new JarFilenameFilter());
        
        for (int i=0; i<listFile.length; i++) {
            if (!listFile[i].isFile()) {
                continue;
            }
            
            parseJarFile(listFile[i]);
        }
        
    }

    /**
     * Check local jar (paros.jar) or related package if any target file is found.
     *
     */
    private void checkLocal(String packageName, boolean recurse) {
        if (packageName == null || packageName.equals("")) {
            return;
        }
        
        String folder = packageName.replace('.', '/');
        URL local = DynamicLoader.class.getClassLoader().getResource(folder);
        if (local == null) {
            return;
        }
        String jarFile = null;
        if (local.getProtocol().equals("jar")) {
            jarFile = local.toString().substring("jar:".length());
            int pos = jarFile.indexOf("!");
            jarFile = jarFile.substring(0, pos);
            
            // ZAP: Removed the commented statement.
            
            try {
                // ZAP: Changed to take into account the package name
                parseJarFile(new File(new URI(jarFile)), packageName);
            } catch (URISyntaxException e) {
            	logger.error(e.getMessage(), e);
            }
        } else {
            try {
                // ZAP: Changed to pass a FileFilter (ClassRecurseDirFileFilter)
                // and to pass the "packageName" with the dots already replaced.
                parseClassDir(new File(new URI(local.toString())),
                              packageName.replace('.', File.separatorChar),
                              new ClassRecurseDirFileFilter(recurse));
            } catch (URISyntaxException e) {
            	logger.error(e.getMessage(), e);
            }
        }
    }
    
    
    // ZAP: Removed warnings, changed to a generic method, removed unnecessary 
    // cast, changed to return an ArrayList and to use the method 
    // Constructor.newInstance().
    public <T> List<T> getFilteredObject (Class<T> classType) {
        String className = "";
        Class<?> cls = null;
        List<T> listClass = new ArrayList<T>();
        for (int i=0; i < listClassName.size(); i++) {
            className = listClassName.get(i);
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

    // ZAP: Changed to use only one FileFilter and the packageName is already
    // passed with the dots replaced.
    private void parseClassDir(File file, String packageName, FileFilter fileFilter) {
        File[] listFile = file.listFiles(fileFilter);
        
        for (int i=0; i<listFile.length; i++) {
            File entry = listFile[i];
            if (entry.isDirectory()) {
            	parseClassDir (entry, packageName, fileFilter);
            	continue;
            }
            String fileName = entry.toString();
            int pos = fileName.indexOf(packageName);
            if (pos > 0) {
                String className = fileName.substring(pos).replaceAll("\\.class$","").replace(File.separatorChar, '.');
                listClassName.add(className);
            }
        }
    }
    
    private void parseJarFile(File file) {
        JarFile jarFile = null;
        ZipEntry entry = null;
        String className = "";
        try {
            jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                // ZAP: Removed unnecessary cast.
                entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                    continue;
                }
                className = entry.toString().replaceAll("\\.class$","").replaceAll("/",".");
                listClassName.add(className);
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
        try {
            this.addURL(file.toURI().toURL());
        } catch (MalformedURLException e) {
        	logger.error(e.getMessage(), e);
        }
    }
    
    // ZAP: Added to take into account the package name
    private void parseJarFile(File file, String packageName) {
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
                    listClassName.add(className);
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
        try {
            this.addURL(file.toURI().toURL());
        } catch (MalformedURLException e) {
        	logger.error(e.getMessage(), e);
        }
    }
    
    // ZAP: Added
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
