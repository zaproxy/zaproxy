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

package org.parosproxy.paros.common;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Vector;
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

    private String directory = "";
    private Vector<String> listClassName = new Vector<String>();
    private Logger logger = Logger.getLogger(DynamicLoader.class);
    
    public DynamicLoader(String directory, String packageName) {
    	this(directory, packageName, false);
    }
    
    public DynamicLoader(String directory, String packageName, boolean recurse) {
        super(new URL[0]);
        this.directory = directory;
        checkLocal(packageName, recurse);
        searchJars();
    }
    
    private void searchJars() {
        File dir = new File(directory);
        if (! dir.exists() || dir.isFile()) {
        	return;
        }
        File[] listFile = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String fileName) {
                if (fileName.endsWith(".jar")) {
                    return true;
                }
                return false;
            }
        });
        
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
            //System.out.println(jarFile);
            try {
                parseJarFile(new File(new URI(jarFile)));
            } catch (URISyntaxException e) {
            	logger.error(e.getMessage(), e);
            }
        } else {
            try {
                parseClassDir(new File(new URI(local.toString())), packageName, recurse);
            } catch (URISyntaxException e) {
            	logger.error(e.getMessage(), e);
            }
        }
    }
    
    
    public Vector<Object> getFilteredObject (Class classType) {
        String className = "";
        Class cls = null;
        Vector<Object> listClass = new Vector<Object>();
        for (int i=0; i<listClassName.size(); i++) {
            className = (String) listClassName.get(i);
            try {
                cls = loadClass(className);
                // abstract class or interface cannot be constructed.
                if (Modifier.isAbstract(cls.getModifiers()) || Modifier.isInterface(cls.getModifiers())) {
                    continue;
                }
                if (classType.isAssignableFrom(cls)) {
                    Object obj = cls.newInstance();
                    listClass.add(obj);
                }
            } catch (Throwable e) {
            	// Often not an error
            	logger.debug(e.getMessage(), e);
            }
        }
        
        return listClass;
    }

    private void parseClassDir(File file, String packageName, final boolean recurse) {
        File[] listFile = file.listFiles(new FilenameFilter() {
            public boolean accept(File file, String fileName) {
                if (fileName.endsWith(".class") || (file.isDirectory() && recurse & ! fileName.startsWith("."))) {
                    return true;
                }
                return false;
            }
        });
        
        for (int i=0; i<listFile.length; i++) {
            File entry = listFile[i];
            if (recurse && entry.isDirectory()) {
            	parseClassDir (entry, packageName, recurse);
            	continue;
            }
            if (!entry.isFile()) {
                continue;
            }
            String fileName = entry.toString();
            String match = packageName.replace('.', File.separatorChar);
            int pos = fileName.indexOf(match);
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
                entry = (ZipEntry) entries.nextElement();
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
                } catch (IOException e1) {
                }
            }
        }
        try {
            this.addURL(file.toURI().toURL());
        } catch (MalformedURLException e1) {
        	logger.error(e1.getMessage(), e1);
        }
    }
    
}
