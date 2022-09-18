/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.zaproxy.zap.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Based on code from http://twit88.com/blog/2007/10/04/java-dynamic-loading-of-class-and-jar-file/

/**
 * @deprecated (2.8.0) The use of this class is discouraged, it expects a {@code URLClassLoader} as
 *     system class loader, which is not always the case (e.g. Java 9+).
 */
@Deprecated
public class ClassLoaderUtil {

    // Log object
    private static Logger log = LogManager.getLogger(ClassLoaderUtil.class);

    // Parameters
    private static final Class<?>[] parameters = new Class<?>[] {URL.class};

    /**
     * Add file to CLASSPATH
     *
     * @param s File name
     * @throws IOException IOException
     */
    public static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }

    /**
     * Add file to CLASSPATH
     *
     * @param f File object
     * @throws IOException IOException
     */
    public static void addFile(File f) throws IOException {
        addURL(f.toURI().toURL());
    }

    /**
     * Add URL to CLASSPATH
     *
     * @param u URL
     * @throws IOException IOException
     */
    public static void addURL(URL u) throws IOException {
        if (!(ClassLoader.getSystemClassLoader() instanceof URLClassLoader)) {
            return;
        }

        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL[] urls = sysLoader.getURLs();
        for (int i = 0; i < urls.length; i++) {
            if (StringUtils.equalsIgnoreCase(urls[i].toString(), u.toString())) {
                log.debug("URL {} is already in the CLASSPATH", u);
                return;
            }
        }
        Class<URLClassLoader> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysLoader, new Object[] {u});
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }
}
