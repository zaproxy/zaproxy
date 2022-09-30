/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.ViewLocale;

public final class LocaleUtils {

    private static final Logger logger = LogManager.getLogger(LocaleUtils.class);

    private static final String MESSAGES_BASE_FILENAME = Constant.MESSAGES_PREFIX + "_";

    private static final String DEFAULT_LOCALE = "en_GB";

    private LocaleUtils() {}

    /**
     * Regular expression to match a language of a {@code Locale}.
     *
     * @see #COUNTRY_LOCALE_REGEX
     * @since 2.4.0
     */
    public static final String LANGUAGE_LOCALE_REGEX = "[a-zA-Z]{2,8}";

    /**
     * Regular expression to match a country/region of a {@code Locale}.
     *
     * @see #LANGUAGE_LOCALE_REGEX
     * @since 2.4.0
     */
    public static final String COUNTRY_LOCALE_REGEX = "[a-zA-Z]{2}|[0-9]{3}";

    /**
     * Finds and returns a localised resource, using the given {@code function} to validate it.
     *
     * <p>The function is called for each resource until a non-{@code null} resource is returned or
     * all resources are consumed. The resource name is built using the parameters {@code fileName},
     * {@code fileExtension}, and the given {@code locale}. The candidates and fallback locale are
     * obtained from a {@link ZapResourceBundleControl}.
     *
     * <p>For example, with the following parameters:
     *
     * <ul>
     *   <li>{@code fileName} - {@code org.zaproxy.zap.file}
     *   <li>{@code fileExtension} - {@code ext}
     *   <li>{@code locale} - {@code es_ES}
     * </ul>
     *
     * and "en_GB" as fallback locale returned by the control, it would produce the following
     * resource names:
     *
     * <pre>{@code
     * org/zaproxy/zap/file_es_ES.ext
     * org/zaproxy/zap/file_es.ext
     * org/zaproxy/zap/file.ext
     * org/zaproxy/zap/file_en_GB.ext
     * org/zaproxy/zap/file_en.ext
     * }</pre>
     *
     * <p>The {@code function} could be, for example, the instance method {@code
     * ClassLoader::getResource}.
     *
     * @param <R> the type of the result.
     * @param fileName the base file name.
     * @param fileExtension the file extension.
     * @param locale the target locale.
     * @param function the function that validates and returns the resource.
     * @return the resource, or {@code null} if none found.
     * @since 2.8.0
     */
    public static <R> R findResource(
            String fileName, String fileExtension, Locale locale, Function<String, R> function) {
        return findResource(fileName, fileExtension, "", locale, function);
    }

    /**
     * Finds and returns a localised resource, using the given {@code function} to validate it.
     *
     * <p>The function is called for each resource until a non-{@code null} resource is returned or
     * all resources are consumed. The resource name is built using the parameters {@code fileName},
     * {@code fileExtension}, and the given {@code locale}. The candidates and fallback locale are
     * obtained from the given resource bundle control.
     *
     * <p>For example, with the following parameters:
     *
     * <ul>
     *   <li>{@code control} - {@code new ZapResourceBundleControl()}
     *   <li>{@code fileName} - {@code org.zaproxy.zap.file}
     *   <li>{@code fileExtension} - {@code ext}
     *   <li>{@code locale} - {@code es_ES}
     * </ul>
     *
     * and "en_GB" as fallback locale returned by the {@code control}, it would produce the
     * following resource names:
     *
     * <pre>{@code
     * org/zaproxy/zap/file_es_ES.ext
     * org/zaproxy/zap/file_es.ext
     * org/zaproxy/zap/file.ext
     * org/zaproxy/zap/file_en_GB.ext
     * org/zaproxy/zap/file_en.ext
     * }</pre>
     *
     * <p>The {@code function} could be, for example, the instance method {@code
     * ClassLoader::getResource}.
     *
     * @param <R> the type of the result.
     * @param control the resource bundle control to obtain the candidate and fallback locales and
     *     build the resource names.
     * @param fileName the base file name.
     * @param fileExtension the file extension.
     * @param locale the target locale.
     * @param function the function that validates and returns the resource.
     * @return the resource, or {@code null} if none found.
     * @since 2.8.0
     */
    public static <R> R findResource(
            ResourceBundle.Control control,
            String fileName,
            String fileExtension,
            Locale locale,
            Function<String, R> function) {
        return findResource(control, fileName, fileExtension, "", locale, function);
    }

    /**
     * Finds and returns a localised resource, using the given {@code function} to validate it.
     *
     * <p>The function is called for each resource until a non-{@code null} resource is returned or
     * all resources are consumed. The resource name is built using the parameters {@code fileName},
     * {@code fileExtension}, and the given {@code locale}. The {@code localeToken}, if non-{@code
     * null} nor empty, is replaced in the {@code fileName} with the current candidate locale. The
     * candidates and fallback locale are obtained from a {@link ZapResourceBundleControl}.
     *
     * <p>For example, with the following parameters:
     *
     * <ul>
     *   <li>{@code fileName} - {@code org.zaproxy.zap.dir%LC%.file}
     *   <li>{@code fileExtension} - {@code ext}
     *   <li>{@code localeToken} - {@code %LC%}
     *   <li>{@code locale} - {@code es_ES}
     * </ul>
     *
     * and "en_GB" as fallback locale returned by the control, it would produce the following
     * resource names:
     *
     * <pre>{@code
     * org/zaproxy/zap/dir_es_ES/file_es_ES.ext
     * org/zaproxy/zap/dir_es/file_es.ext
     * org/zaproxy/zap/dir/file.ext
     * org/zaproxy/zap/dir_en_GB/file_en_GB.ext
     * org/zaproxy/zap/dir_en/file_en.ext
     * }</pre>
     *
     * <p>The {@code function} could be, for example, the instance method {@code
     * ClassLoader::getResource}.
     *
     * @param <R> the type of the result.
     * @param fileName the base file name.
     * @param fileExtension the file extension.
     * @param localeToken the token that represents the locale, to be replaced in the {@code
     *     fileName}. Might be {@code null}.
     * @param locale the target locale.
     * @param function the function that validates and returns the resource.
     * @return the resource, or {@code null} if none found.
     * @since 2.8.0
     */
    public static <R> R findResource(
            String fileName,
            String fileExtension,
            String localeToken,
            Locale locale,
            Function<String, R> function) {
        return findResource(
                new ZapResourceBundleControl(),
                fileName,
                fileExtension,
                localeToken,
                locale,
                function);
    }

    /**
     * Finds and returns a localised resource, using the given {@code function} to validate it.
     *
     * <p>The function is called for each resource until a non-{@code null} resource is returned or
     * all resources are consumed. The resource name is built using the parameters {@code fileName},
     * {@code fileExtension}, and the given {@code locale}. The {@code localeToken}, if non-{@code
     * null} nor empty, is replaced in the {@code fileName} with the current candidate locale. The
     * candidates and fallback locale are obtained from the given resource bundle control.
     *
     * <p>For example, with the following parameters:
     *
     * <ul>
     *   <li>{@code control} - {@code new ZapResourceBundleControl()}
     *   <li>{@code fileName} - {@code org.zaproxy.zap.dir%LC%.file}
     *   <li>{@code fileExtension} - {@code ext}
     *   <li>{@code localeToken} - {@code %LC%}
     *   <li>{@code locale} - {@code es_ES}
     * </ul>
     *
     * and "en_GB" as fallback locale returned by the {@code control}, it would produce the
     * following resource names:
     *
     * <pre>{@code
     * org/zaproxy/zap/dir_es_ES/file_es_ES.ext
     * org/zaproxy/zap/dir_es/file_es.ext
     * org/zaproxy/zap/dir/file.ext
     * org/zaproxy/zap/dir_en_GB/file_en_GB.ext
     * org/zaproxy/zap/dir_en/file_en.ext
     * }</pre>
     *
     * <p>The {@code function} could be, for example, the instance method {@code
     * ClassLoader::getResource}.
     *
     * @param <R> the type of the result.
     * @param control the resource bundle control to obtain the candidate and fallback locales and
     *     build the resource names.
     * @param fileName the base file name.
     * @param fileExtension the file extension.
     * @param localeToken the token that represents the locale, to be replaced in the {@code
     *     fileName}. Might be {@code null}.
     * @param locale the target locale.
     * @param function the function that validates and returns the resource.
     * @return the resource, or {@code null} if none found.
     * @since 2.8.0
     */
    public static <R> R findResource(
            ResourceBundle.Control control,
            String fileName,
            String fileExtension,
            String localeToken,
            Locale locale,
            Function<String, R> function) {
        Set<Locale> candidateLocales = new LinkedHashSet<>();
        candidateLocales.addAll(control.getCandidateLocales("", locale));
        Locale fallbackLocale = control.getFallbackLocale("", locale);
        if (fallbackLocale != null) {
            candidateLocales.addAll(control.getCandidateLocales("", fallbackLocale));
        }
        for (Locale candidateLocale : candidateLocales) {
            String strLocale = control.toBundleName("", candidateLocale);
            String prefix = fileName;
            if (localeToken != null && !localeToken.isEmpty()) {
                prefix =
                        prefix.replaceAll(
                                Pattern.quote(localeToken), Matcher.quoteReplacement(strLocale));
            }
            R result = function.apply(control.toResourceName(prefix + strLocale, fileExtension));
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Convenience method that calls the method {@code #createResourceFilePattern(String, String)},
     * with parameters {@code Constant.MESSAGES_PREFIX} and {@code Constant.MESSAGES_EXTENSION},
     * respectively.
     *
     * @return a {@code Pattern} that matches the Messages.properties files of different {@code
     *     Locale}s
     * @see #createResourceFilesPattern(String, String)
     * @see Constant#MESSAGES_PREFIX
     * @see Constant#MESSAGES_EXTENSION
     * @since 2.4.0
     */
    public static Pattern createMessagesPropertiesFilePattern() {
        return createResourceFilesPattern(Constant.MESSAGES_PREFIX, Constant.MESSAGES_EXTENSION);
    }

    /**
     * Returns a regular expression to match source and translated resource filenames with the given
     * {@code fileName} and {@code fileExtension}.
     *
     * <p>For example, with {@code fileName} as "Messages" and {@code fileExtension} as
     * ".properties" the returned pattern would match:
     *
     * <ul>
     *   <li>Messages.properties
     *   <li>Messages_en.properties
     *   <li>Messages_en_GB.properties
     * </ul>
     *
     * @param fileName the name of the resource files
     * @param fileExtension the extension of the resource files
     * @return the regular expression to match resource filenames
     * @throws IllegalArgumentException if the given {@code fileName} or {@code fileExtension} is
     *     {@code null}.
     * @see #createResourceFilesPattern(String, String)
     * @see #LANGUAGE_LOCALE_REGEX
     * @see #COUNTRY_LOCALE_REGEX
     * @since 2.4.0
     */
    public static String createResourceFilesRegex(String fileName, String fileExtension) {
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName must not be null.");
        }
        if (fileExtension == null) {
            throw new IllegalArgumentException("Parameter fileExtension must not be null.");
        }

        StringBuilder strBuilder =
                new StringBuilder(
                        fileName.length()
                                + LANGUAGE_LOCALE_REGEX.length()
                                + COUNTRY_LOCALE_REGEX.length()
                                + fileExtension.length()
                                + 13);
        strBuilder.append(Pattern.quote(fileName));
        strBuilder.append("(?:_").append(LANGUAGE_LOCALE_REGEX);
        strBuilder.append("(?:_").append(COUNTRY_LOCALE_REGEX).append(")?").append(")?");
        strBuilder.append(Pattern.quote(fileExtension));
        strBuilder.append('$');
        return strBuilder.toString();
    }

    /**
     * Returns a {@code Pattern} to match source and translated resource filenames with the given
     * {@code fileName} and {@code fileExtension}.
     *
     * <p>For example, with {@code fileName} as "Messages" and {@code fileExtension} as
     * ".properties" the returned pattern would match:
     *
     * <ul>
     *   <li>Messages.properties
     *   <li>Messages_en.properties
     *   <li>Messages_en_GB.properties
     * </ul>
     *
     * <p>The pattern is case-sensitive.
     *
     * @param fileName the name of the resource files
     * @param fileExtension the extension of the resource files
     * @return the {@code Pattern} to match resource filenames
     * @throws IllegalArgumentException if the given {@code fileName} or {@code fileExtension} is
     *     {@code null}.
     * @see #createResourceFilesRegex(String, String)
     * @see #LANGUAGE_LOCALE_REGEX
     * @see #COUNTRY_LOCALE_REGEX
     * @since 2.4.0
     */
    public static Pattern createResourceFilesPattern(String fileName, String fileExtension) {
        return Pattern.compile(createResourceFilesRegex(fileName, fileExtension));
    }

    /**
     * Returns a list of languages and countries of the {@code Locale}s (as {@code String}, for
     * example "en_GB"), of default language and available translations.
     *
     * <p>The list is sorted by language/country codes with default locale, always, at first
     * position.
     *
     * @return The list of available translations, ZAP provides
     */
    public static List<String> getAvailableLocales() {
        List<String> locales = readAvailableLocales();
        Collections.sort(locales);

        // Always put English at the top
        locales.add(0, DEFAULT_LOCALE);

        return locales;
    }

    private static List<String> readAvailableLocales() {
        File dir = new File(Constant.getZapInstall(), Constant.LANG_DIR);
        if (!dir.exists()) {
            logger.debug(
                    "Skipping read of available locales, the directory does not exist: {}",
                    dir.getAbsolutePath());
            return new ArrayList<>(0);
        }

        FilenameFilter filter = new MessagesPropertiesFilenameFilter();
        String[] files = dir.list(filter);

        if (files == null || files.length == 0) {
            logger.warn("No Messages files in directory {}", dir.getAbsolutePath());
            return new ArrayList<>(0);
        }

        List<String> locales = new ArrayList<>(files.length);

        final int baseFilenameLength = MESSAGES_BASE_FILENAME.length();
        for (String file : Arrays.asList(files)) {
            if (file.startsWith(MESSAGES_BASE_FILENAME)) {
                locales.add(file.substring(baseFilenameLength, file.indexOf(".")));
            }
        }
        return locales;
    }

    /**
     * Convenience method that creates a {@code ViewLocale} with the given {@code locale} and a
     * display name created by calling {@code getLocalDisplayName(String)}, with the {@code locale}
     * as argument.
     *
     * @param locale the locale that will used to create the {@code ViewLocale}
     * @return the {@code ViewLocale} for the given locale
     * @since 2.4.0
     * @see #getLocalDisplayName(String)
     */
    public static ViewLocale getViewLocale(String locale) {
        return new ViewLocale(locale, getLocalDisplayName(locale));
    }

    /**
     * Returns a list of {@code ViewLocale}s, sorted by display name, of the default language and
     * available translations.
     *
     * @return the {@code ViewLocale}s of the default language and available translations.
     * @see ViewLocale
     * @since 2.4.0
     */
    public static List<ViewLocale> getAvailableViewLocales() {
        List<String> locales = readAvailableLocales();

        List<ViewLocale> localesUI = new ArrayList<>();
        if (!locales.isEmpty()) {
            for (String locale : locales) {
                localesUI.add(new ViewLocale(locale, getLocalDisplayName(locale)));
            }

            Collections.sort(
                    localesUI,
                    new Comparator<ViewLocale>() {

                        @Override
                        public int compare(ViewLocale o1, ViewLocale o2) {
                            return o1.toString().compareTo(o2.toString());
                        }
                    });
        }

        // Always put English at the top
        localesUI.add(0, new ViewLocale(DEFAULT_LOCALE, getLocalDisplayName(DEFAULT_LOCALE)));

        return localesUI;
    }

    /**
     * Gets the name of the language of and for the given locale.
     *
     * @param locale the locale whose language name will be returned
     * @return the name of the language
     */
    public static String getLocalDisplayName(String locale) {
        String desc = "" + locale;
        if (locale != null) {
            String[] langArray = locale.split("_");
            Locale loc = null;
            if (langArray.length == 1) {
                loc = new Locale(langArray[0]);
            } else if (langArray.length == 2) {
                loc = new Locale(langArray[0], langArray[1]);
            } else if (langArray.length == 3) {
                loc = new Locale(langArray[0], langArray[1], langArray[2]);
            }
            if (loc != null) {
                desc = loc.getDisplayLanguage(loc);
            }
        }
        return desc;
    }

    private static final class MessagesPropertiesFilenameFilter implements FilenameFilter {
        private final Pattern messagesPropertiesPattern = createMessagesPropertiesFilePattern();

        @Override
        public boolean accept(File dir, String name) {
            return messagesPropertiesPattern.matcher(name).matches();
        }
    }
}
