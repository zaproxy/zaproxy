/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A {@code URLClassLoader} that search for classes and resources (first) in a given add-on file
 * ({@code URL}). If a class or resource is not found, in the add-on file, the loading is delegated
 * to the parent class loader. If not found in the parent class loader it will be searched in the
 * dependencies (add-ons), if any.
 *
 * @see URLClassLoader
 */
public class AddOnClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private final ParentClassLoader parent;
    private final List<AddOnClassLoader> childClassLoaders;
    private List<AddOnClassLoader> dependencies;
    private AddOnClassnames addOnClassnames;
    private ClassLoadingLockProvider classLoadingLockProvider;

    /**
     * Constructs a new {@code AddOnClassLoader} without dependencies on other add-ons.
     *
     * @param addOnFileUrl the URL to the add-on file that will be (first) used to load classes and
     *     resources
     * @param parent the parent class loader for delegation
     * @throws IllegalArgumentException if the {@code addOnFileUrl} or {@code parent} is {@code
     *     null}.
     */
    public AddOnClassLoader(URL addOnFileUrl, ClassLoader parent) {
        this(
                addOnFileUrl,
                parent,
                Collections.<AddOnClassLoader>emptyList(),
                AddOnClassnames.ALL_ALLOWED);
    }

    /**
     * Constructs a new {@code AddOnClassLoader} without dependencies on other add-ons. Possibly
     * restricting or allowing the loading of the given {@code addOnClassnames}, directly from this
     * {@code AddOnClassLoader}.
     *
     * @param addOnFileUrl the URL to the add-on file that will be (first) used to load classes and
     *     resources
     * @param parent the parent class loader for delegation
     * @param addOnClassnames the classnames that can be loaded
     * @throws IllegalArgumentException if the {@code addOnFileUrl} or {@code parent} is {@code
     *     null}.
     * @since 2.4.3
     */
    public AddOnClassLoader(URL addOnFileUrl, ClassLoader parent, AddOnClassnames addOnClassnames) {
        this(addOnFileUrl, parent, Collections.<AddOnClassLoader>emptyList(), addOnClassnames);
    }

    /**
     * Constructs a new {@code AddOnClassLoader} without dependencies on other add-ons. Possibly
     * restricting or allowing the loading of the given {@code addOnClassnames}, directly from this
     * {@code AddOnClassLoader}.
     *
     * @param addOnFileUrl the URL to the add-on file that will be (first) used to load classes and
     *     resources
     * @param parent the parent class loader for delegation and sharing of class loading lock(s).
     * @param addOnClassnames the classnames that can be loaded
     * @throws IllegalArgumentException if the {@code addOnFileUrl} or {@code parent} is {@code
     *     null}.
     * @since 2.7.0
     */
    public AddOnClassLoader(URL addOnFileUrl, AddOnLoader parent, AddOnClassnames addOnClassnames) {
        this(
                addOnFileUrl,
                (ClassLoader) parent,
                Collections.<AddOnClassLoader>emptyList(),
                addOnClassnames);
        this.classLoadingLockProvider = parent::getClassLoadingLock;
    }

    /**
     * Constructs a new {@code AddOnClassLoader} with the given {@code dependencies} which are used
     * to find classes and resources when not found in the add-on or in {@code parent} ClassLoader.
     *
     * @param addOnFileUrl the URL to the add-on file that will be (first) used to load classes and
     *     resources
     * @param parent the parent class loader for delegation
     * @param dependencies the {@code AddOnClassLoader}s of the dependencies of the add-on
     * @throws IllegalArgumentException if the {@code addOnFileUrl}, {@code parent} or {@code
     *     dependencies} is {@code null}.
     * @since 2.4.0
     */
    public AddOnClassLoader(
            URL addOnFileUrl, ClassLoader parent, List<AddOnClassLoader> dependencies) {
        this(addOnFileUrl, parent, dependencies, AddOnClassnames.ALL_ALLOWED);
    }

    /**
     * Constructs a new {@code AddOnClassLoader} with the given {@code dependencies} which are used
     * to find classes and resources when not found in the add-on or in {@code parent} ClassLoader.
     * Possibly restricting or allowing the loading of the given {@code addOnClassnames}, directly
     * from this {@code AddOnClassLoader}.
     *
     * @param addOnFileUrl the URL to the add-on file that will be (first) used to load classes and
     *     resources
     * @param parent the parent class loader for delegation
     * @param dependencies the {@code AddOnClassLoader}s of the dependencies of the add-on
     * @param addOnClassnames the classnames that can be loaded
     * @throws IllegalArgumentException if the {@code addOnFileUrl}, {@code parent} or {@code
     *     dependencies} is {@code null}.
     * @since 2.4.3
     */
    public AddOnClassLoader(
            URL addOnFileUrl,
            ClassLoader parent,
            List<AddOnClassLoader> dependencies,
            AddOnClassnames addOnClassnames) {
        super(new URL[] {addOnFileUrl}, null);

        if (addOnFileUrl == null) {
            throw new IllegalArgumentException("Parameter addOnFileUrl must not be null.");
        }

        if (parent == null) {
            throw new IllegalArgumentException("Parameter parent must not be null.");
        }

        if (dependencies == null) {
            throw new IllegalArgumentException("Parameter dependencies must not be null.");
        }

        this.parent = new ParentClassLoader(parent);
        this.dependencies =
                dependencies.isEmpty()
                        ? Collections.<AddOnClassLoader>emptyList()
                        : new ArrayList<>(dependencies);
        this.childClassLoaders = new ArrayList<>(2);
        this.addOnClassnames = addOnClassnames;
    }

    /**
     * Constructs a new {@code AddOnClassLoader} with the given {@code dependencies} which are used
     * to find classes and resources when not found in the add-on or in {@code parent} ClassLoader.
     * Possibly restricting or allowing the loading of the given {@code addOnClassnames}, directly
     * from this {@code AddOnClassLoader}.
     *
     * @param addOnFileUrl the URL to the add-on file that will be (first) used to load classes and
     *     resources
     * @param parent the parent class loader for delegation and sharing of class loading lock(s).
     * @param dependencies the {@code AddOnClassLoader}s of the dependencies of the add-on
     * @param addOnClassnames the classnames that can be loaded
     * @throws IllegalArgumentException if the {@code addOnFileUrl}, {@code parent} or {@code
     *     dependencies} is {@code null}.
     * @since 2.7.0
     */
    public AddOnClassLoader(
            URL addOnFileUrl,
            AddOnLoader parent,
            List<AddOnClassLoader> dependencies,
            AddOnClassnames addOnClassnames) {
        this(addOnFileUrl, (ClassLoader) parent, dependencies, addOnClassnames);
        this.classLoadingLockProvider = parent::getClassLoadingLock;
    }

    /**
     * Constructs a new {@code AddOnClassLoader} with the given {@code dependencies} which are used
     * to find classes and resources when not found in the add-on or in {@code parent} {@code
     * AddOnClassLoader}.
     *
     * @param parent the parent class loader for delegation
     * @param dependencies the {@code AddOnClassLoader}s of the dependencies of the add-on
     * @throws NullPointerException if {@code parent} is {@code null}.
     * @throws IllegalArgumentException if the {@code addOnFileUrl} or {@code dependencies} is
     *     {@code null}.
     * @since 2.4.0
     */
    public AddOnClassLoader(AddOnClassLoader parent, List<AddOnClassLoader> dependencies) {
        super(parent.getURLs(), null);

        if (dependencies == null) {
            throw new IllegalArgumentException("Parameter dependencies must not be null.");
        }

        parent.childClassLoaders.add(this);
        this.parent = new ParentClassLoader(parent);
        this.dependencies =
                dependencies.isEmpty()
                        ? Collections.<AddOnClassLoader>emptyList()
                        : new ArrayList<>(dependencies);
        this.childClassLoaders = Collections.emptyList();
        this.addOnClassnames = AddOnClassnames.ALL_ALLOWED;
        this.classLoadingLockProvider = parent::getClassLoadingLock;
    }

    /**
     * Constructs a new {@code AddOnClassLoader} with the given {@code dependencies} which are used
     * to find classes and resources when not found in the add-on or in {@code parent} {@code
     * AddOnClassLoader}. Possibly restricting or allowing the loading of the given {@code
     * classnames}, directly from this {@code AddOnClassLoader}.
     *
     * @param parent the parent class loader for delegation
     * @param dependencies the {@code AddOnClassLoader}s of the dependencies of the add-on
     * @param addOnClassnames the classnames that can be loaded
     * @throws NullPointerException if {@code parent} is {@code null}.
     * @throws IllegalArgumentException if the {@code addOnFileUrl}, {@code dependencies} or {@code
     *     packages} is {@code null}.
     * @since 2.4.3
     */
    public AddOnClassLoader(
            AddOnClassLoader parent,
            List<AddOnClassLoader> dependencies,
            AddOnClassnames addOnClassnames) {
        super(parent.getURLs(), null);

        if (dependencies == null) {
            throw new IllegalArgumentException("Parameter dependencies must not be null.");
        }

        if (addOnClassnames == null) {
            throw new IllegalArgumentException("Parameter addOnClassnames must not be null.");
        }

        parent.childClassLoaders.add(this);
        this.parent = new ParentClassLoader(parent);
        this.dependencies =
                dependencies.isEmpty()
                        ? Collections.<AddOnClassLoader>emptyList()
                        : new ArrayList<>(dependencies);
        this.childClassLoaders = Collections.emptyList();
        this.addOnClassnames = addOnClassnames;
        this.classLoadingLockProvider = parent::getClassLoadingLock;
    }

    /**
     * Adds the given URLs to the class loader, to search for classes and resources.
     *
     * @param urls the URLs to add to the class loader.
     * @throws NullPointerException if the given list is {@code null}.
     */
    void addUrls(List<URL> urls) {
        Objects.requireNonNull(urls);
        urls.forEach(this::addURL);
    }

    /**
     * Removes the given child class loader from the list of child class loaders.
     *
     * @param child the child class loader that will be removed
     * @since 2.4.0
     */
    public void removeChildClassLoader(AddOnClassLoader child) {
        childClassLoaders.remove(child);
    }

    /**
     * Gets the child class loaders.
     *
     * @return an unmodifiable list with the child class loaders.
     */
    List<AddOnClassLoader> getChildClassLoaders() {
        return Collections.unmodifiableList(childClassLoaders);
    }

    @Override
    public void close() throws IOException {
        for (AddOnClassLoader childClassLoader : childClassLoaders) {
            childClassLoader.close();
        }
        super.close();
    }

    @Override
    protected Object getClassLoadingLock(String className) {
        if (classLoadingLockProvider != null) {
            return classLoadingLockProvider.getLock(className);
        }
        return super.getClassLoadingLock(className);
    }

    /**
     * Clears the dependencies. Should be called when this class loader is no longer needed.
     *
     * @since 2.4.0
     */
    public void clearDependencies() {
        this.dependencies = Collections.emptyList();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (addOnClassnames.isAllowed(name)) {
            try {
                return findClassInAddOn(name);
            } catch (ClassNotFoundException ignore) {
            }
        }

        try {
            return parent.loadClass(name, false);
        } catch (ClassNotFoundException ignore) {
        }

        Class<?> clazz = findClassInDependencies(name);
        if (clazz == null) {
            throw new ClassNotFoundException();
        }
        return clazz;
    }

    private Class<?> findClassInAddOn(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    private Class<?> findClassInDependencies(String name) {
        if (dependencies.isEmpty()) {
            return null;
        }

        for (AddOnClassLoader addOnClassLoader : dependencies) {
            try {
                return addOnClassLoader.loadClass(name, false);
            } catch (ClassNotFoundException ignore) {
            }
        }
        return null;
    }

    @Override
    public URL findResource(String name) {
        URL url = findResourceInAddOn(name);
        if (url == null) {
            url = parent.getResource(name);
        }
        if (url == null) {
            url = findResourceInDependencies(name);
        }
        return url;
    }

    /**
     * Finds the resource with the specified name (only) in the add-on file.
     *
     * @param name the name of the resource
     * @return a {@code URL} for the resource, or {@code null} if the resource could not be found,
     *     or if the loader is closed.
     * @since 2.4.0
     * @see #findResource(String)
     */
    public URL findResourceInAddOn(String name) {
        return super.findResource(name);
    }

    private URL findResourceInDependencies(String name) {
        if (dependencies.isEmpty()) {
            return null;
        }

        for (AddOnClassLoader addOnClassLoader : dependencies) {
            URL url = addOnClassLoader.findResourceInAddOn(name);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    /**
     * Helper class that exposes the protected method {@code ClassLoader#loadClass(String, boolean)}
     * to the class {@code AddOnClassLoader} (called in the method {@code
     * AddOnClassLoader#findClass(String)}).
     *
     * <p>The class {@code AddOnClassLoader} needs access to the mentioned method because the class
     * {@code AddOnLoader} makes use of the class {@code AddOnClassLoader} (in the method {@code
     * AddOnLoader#loadClass(String)}), so it's called directly the method {@code
     * ClassLoader#loadClass(String, boolean)} to prevent an infinite loop while loading the
     * classes.
     *
     * @see AddOnLoader#loadClass(String)
     * @see AddOnClassLoader#findClass(String)
     * @see ClassLoader#loadClass(String, boolean)
     */
    private class ParentClassLoader extends ClassLoader {

        public ParentClassLoader(ClassLoader classLoader) {
            super(classLoader);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            return super.loadClass(name, resolve);
        }

        @Override
        protected Object getClassLoadingLock(String className) {
            if (classLoadingLockProvider != null) {
                return classLoadingLockProvider.getLock(className);
            }
            return super.getClassLoadingLock(className);
        }
    }

    @FunctionalInterface
    private static interface ClassLoadingLockProvider {

        Object getLock(String className);
    }
}
