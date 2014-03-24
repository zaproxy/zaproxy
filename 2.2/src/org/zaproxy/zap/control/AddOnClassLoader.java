/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 ZAP development team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.control;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A {@code URLClassLoader} that search for classes and resources (first) in a given add-on file ({@code URL}). If a class or
 * resource is not found, in the add-on file, the loading is delegated to the parent class loader.
 * 
 * @see URLClassLoader
 */
public class AddOnClassLoader extends URLClassLoader {

    private final ParentClassLoader parent;

    /**
     * Constructs a new {@code AddOnClassLoader}.
     * 
     * @param addOnFileUrl the URL to the add-on file that will be (first) used to load classes and resources
     * @param parent the parent class loader for delegation
     * @throws IllegalArgumentException if the {@code addOnFileUrl} or {@code parent} is {@code null}.
     */
    public AddOnClassLoader(URL addOnFileUrl, ClassLoader parent) {
        super(new URL[] { addOnFileUrl }, null);

        if (addOnFileUrl == null) {
            throw new IllegalArgumentException("Parameter addOnFileUrl must not be null.");
        }

        if (parent == null) {
            throw new IllegalArgumentException("Parameter parent must not be null.");
        }

        this.parent = new ParentClassLoader(parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException ignore) {
        }
        return parent.loadClass(name, false);
    }

    @Override
    public URL findResource(String name) {
        URL url = super.findResource(name);
        if (url == null) {
            url = parent.getResource(name);
        }
        return url;
    }

    /**
     * Helper class that exposes the protected method {@code ClassLoader#loadClass(String, boolean)} to the class
     * {@code AddOnClassLoader} (called in the method {@code AddOnClassLoader#findClass(String)}).
     * <p>
     * The class {@code AddOnClassLoader} needs access to the mentioned method because the class {@code AddOnLoader} makes use
     * of the class {@code AddOnClassLoader} (in the method {@code AddOnLoader#loadClass(String)}), so it's called directly the
     * method {@code ClassLoader#loadClass(String, boolean)} to prevent an infinite loop while loading the classes.
     * 
     * @see AddOnLoader#loadClass(String)
     * @see AddOnClassLoader#findClass(String)
     * @see ClassLoader#loadClass(String, boolean)
     */
    private static class ParentClassLoader extends ClassLoader {

        public ParentClassLoader(ClassLoader classLoader) {
            super(classLoader);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            return super.loadClass(name, resolve);
        }
    }
}
