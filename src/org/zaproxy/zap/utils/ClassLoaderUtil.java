package org.zaproxy.zap.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

// Based on code from http://twit88.com/blog/2007/10/04/java-dynamic-loading-of-class-and-jar-file/

public class ClassLoaderUtil {

    // Log object
    private static Logger log = Logger.getLogger(ClassLoaderUtil.class);

    // Parameters
	private static final Class<?>[] parameters = new Class<?>[]{URL.class};

    /**
     * Add file to CLASSPATH
     * @param s File name
     * @throws IOException  IOException
     */
    public static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }

    /**
     * Add file to CLASSPATH
     * @param f  File object
     * @throws IOException IOException
     */
    public static void addFile(File f) throws IOException {
        addURL(f.toURI().toURL());
    }

    /**
     * Add URL to CLASSPATH
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
                if (log.isDebugEnabled()) {
                    log.debug("URL " + u + " is already in the CLASSPATH");
                }
                return;
            }
        }
        Class<URLClassLoader> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysLoader, new Object[]{u});
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }

}
