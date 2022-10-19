/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.AbstractPlugin;
import org.parosproxy.paros.extension.Extension;
import org.zaproxy.zap.Version;
import org.zaproxy.zap.control.BaseZapAddOnXmlData.AddOnDep;
import org.zaproxy.zap.control.BaseZapAddOnXmlData.Dependencies;
import org.zaproxy.zap.control.BaseZapAddOnXmlData.ExtensionWithDeps;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;

public class AddOn {

    /**
     * The name of the manifest file, contained in the add-ons.
     *
     * <p>The manifest is expected to be in the root of the ZIP file.
     *
     * @since 2.6.0
     */
    public static final String MANIFEST_FILE_NAME = "ZapAddOn.xml";

    public enum Status {
        unknown,
        example,
        alpha,
        beta,
        weekly,
        release
    }

    private static ZapRelease v2_4 = new ZapRelease("2.4.0");

    /**
     * The installation status of the add-on.
     *
     * @since 2.4.0
     */
    public enum InstallationStatus {

        /**
         * The add-on is available for installation, for example, an add-on in the marketplace (even
         * if it requires previous actions, in this case, download the file).
         */
        AVAILABLE,

        /**
         * The add-on was not (yet) installed. For example, the add-on is available in the 'plugin'
         * directory but it's missing a dependency or requires a greater Java version. It's also in
         * this status while a dependency is being updated.
         */
        NOT_INSTALLED,

        /** The add-on is installed. */
        INSTALLED,

        /** The add-on is being downloaded. */
        DOWNLOADING,

        /**
         * The uninstallation of the add-on failed. For example, when the add-on is not dynamically
         * installable or when an {@code Exception} is thrown during the uninstallation.
         */
        UNINSTALLATION_FAILED,

        /**
         * The soft uninstallation of the add-on failed. It's in this status when the uninstallation
         * failed for an update of a dependency.
         */
        SOFT_UNINSTALLATION_FAILED
    }

    /**
     * The result of checking if a file is a valid add-on.
     *
     * @since 2.8.0
     * @see AddOn#isValidAddOn(Path)
     */
    public static class ValidationResult {

        /** The validity of the add-on. */
        public enum Validity {
            /**
             * The add-on is valid.
             *
             * <p>The result contains the {@link ValidationResult#getManifest() manifest} of the
             * add-on.
             */
            VALID,
            /** The file path is not valid. */
            INVALID_PATH,
            /** The file does not have the expected extension {@value #FILE_EXTENSION}. */
            INVALID_FILE_NAME,
            /** The file is not a regular file or is not readable. */
            FILE_NOT_READABLE,
            /**
             * There was an error reading the ZIP file.
             *
             * <p>The result contains the {@link ValidationResult#getException() exception}.
             */
            UNREADABLE_ZIP_FILE,
            /**
             * There was an error reading the file.
             *
             * <p>The result contains the {@link ValidationResult#getException() exception}.
             */
            IO_ERROR_FILE,
            /** The ZIP file does not have the add-on manifest, {@value #MANIFEST_FILE_NAME}. */
            MISSING_MANIFEST,
            /**
             * The manifest is not valid.
             *
             * <p>The result contains the {@link ValidationResult#getException() exception}.
             */
            INVALID_MANIFEST,
            /** The add-on declared a missing/invalid library. */
            INVALID_LIB,
        }

        private final Validity validity;
        private final Exception exception;
        private final ZapAddOnXmlFile manifest;

        private ValidationResult(Validity validity) {
            this(validity, null);
        }

        private ValidationResult(ZapAddOnXmlFile manifest) {
            this(Validity.VALID, null, manifest);
        }

        private ValidationResult(Validity validity, Exception exception) {
            this(validity, exception, null);
        }

        private ValidationResult(Validity validity, Exception exception, ZapAddOnXmlFile manifest) {
            this.validity = validity;
            this.exception = exception;
            this.manifest = manifest;
        }

        /**
         * Gets the validity of the add-on.
         *
         * @return the validity of the add-on.
         */
        public Validity getValidity() {
            return validity;
        }

        /**
         * Gets the exception that occurred while validating the file.
         *
         * @return the exception, or {@code null} if none.
         */
        public Exception getException() {
            return exception;
        }

        /**
         * Gets the manifest of the add-on.
         *
         * @return the manifest of the add-on, if valid, {@code null} otherwise.
         */
        public ZapAddOnXmlFile getManifest() {
            return manifest;
        }
    }

    /**
     * The file extension of ZAP add-ons.
     *
     * @since 2.6.0
     */
    public static final String FILE_EXTENSION = ".zap";

    private String id;
    private String name;
    private String description = "";
    private String author = "";
    /**
     * The version declared in the manifest file.
     *
     * <p>Never {@code null}.
     */
    private Version version;
    /**
     * The (semantic) version declared in the manifest file, to be replaced by {@link #version}.
     *
     * <p>Might be {@code null}.
     */
    private Version semVer;

    private Status status;
    private String changes = "";
    private File file = null;
    private URL url = null;
    private URL info = null;
    private URL repo;
    private long size = 0;
    private boolean hasZapAddOnEntry = false;

    /**
     * Flag that indicates if the manifest was read (or attempted to). Allows to prevent reading the
     * manifest a second time when the add-on file is corrupt.
     */
    private boolean manifestRead;

    private String notBeforeVersion = null;
    private String notFromVersion = null;
    private String hash = null;
    private String releaseDate;

    /**
     * The installation status of the add-on.
     *
     * <p>Default is {@code NOT_INSTALLED}.
     *
     * @see InstallationStatus#NOT_INSTALLED
     */
    private InstallationStatus installationStatus = InstallationStatus.NOT_INSTALLED;

    private List<String> extensions = Collections.emptyList();
    private List<ExtensionWithDeps> extensionsWithDeps = Collections.emptyList();

    /**
     * The extensions of the add-on that were loaded.
     *
     * <p>This instance variable is lazy initialised.
     *
     * @see #addLoadedExtension(Extension)
     * @see #removeLoadedExtension(Extension)
     */
    private List<Extension> loadedExtensions;

    private List<String> ascanrules = Collections.emptyList();
    private List<AbstractPlugin> loadedAscanrules = Collections.emptyList();
    private boolean loadedAscanRulesSet;
    private List<String> pscanrules = Collections.emptyList();
    private List<PluginPassiveScanner> loadedPscanrules = Collections.emptyList();
    private boolean loadedPscanRulesSet;
    private List<String> files = Collections.emptyList();
    private List<Lib> libs = Collections.emptyList();

    private AddOnClassnames addOnClassnames = AddOnClassnames.ALL_ALLOWED;

    private ClassLoader classLoader;

    private Dependencies dependencies;

    /**
     * The data of the bundle declared in the manifest.
     *
     * <p>Never {@code null}.
     */
    private BundleData bundleData = BundleData.EMPTY_BUNDLE;

    /**
     * The resource bundle from the declaration in the manifest.
     *
     * <p>Might be {@code null}, if not declared.
     */
    private ResourceBundle resourceBundle;

    /**
     * The data for the HelpSet, declared in the manifest.
     *
     * <p>Never {@code null}.
     */
    private HelpSetData helpSetData = HelpSetData.EMPTY_HELP_SET;

    /**
     * Flag that indicates if the add-on is mandatory, add-ons needed to start ZAP and that can't be
     * uninstalled.
     */
    private boolean mandatory;

    private static final Logger logger = LogManager.getLogger(AddOn.class);

    /**
     * Tells whether or not the given file name matches the name of a ZAP add-on.
     *
     * <p>The file name must have as file extension {@link #FILE_EXTENSION}.
     *
     * @param fileName the name of the file to check
     * @return {@code true} if the given file name is the name of an add-on, {@code false}
     *     otherwise.
     * @since 2.6.0
     */
    public static boolean isAddOnFileName(String fileName) {
        if (fileName == null) {
            return false;
        }
        return fileName.toLowerCase(Locale.ROOT).endsWith(FILE_EXTENSION);
    }

    /**
     * Tells whether or not the given file is a ZAP add-on.
     *
     * <p>An add-on is a ZIP file that contains, at least, a {@value #MANIFEST_FILE_NAME} file.
     *
     * @param file the file to be checked
     * @return {@code true} if the given file is an add-on, {@code false} otherwise.
     * @since 2.6.0
     * @see #isAddOnFileName(String)
     * @see #isValidAddOn(Path)
     */
    public static boolean isAddOn(Path file) {
        return isValidAddOn(file).getValidity() == ValidationResult.Validity.VALID;
    }

    /**
     * Tells whether or not the given file is a ZAP add-on.
     *
     * @param file the file to be checked.
     * @return the result of the validation.
     * @since 2.8.0
     * @see #isAddOn(Path)
     */
    public static ValidationResult isValidAddOn(Path file) {
        if (file == null || file.getNameCount() == 0) {
            return new ValidationResult(ValidationResult.Validity.INVALID_PATH);
        }

        if (!isAddOnFileName(file.getFileName().toString())) {
            return new ValidationResult(ValidationResult.Validity.INVALID_FILE_NAME);
        }

        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            return new ValidationResult(ValidationResult.Validity.FILE_NOT_READABLE);
        }

        try (ZipFile zip = new ZipFile(file.toFile())) {
            ZipEntry manifestEntry = zip.getEntry(MANIFEST_FILE_NAME);
            if (manifestEntry == null) {
                return new ValidationResult(ValidationResult.Validity.MISSING_MANIFEST);
            }

            try (InputStream zis = zip.getInputStream(manifestEntry)) {
                ZapAddOnXmlFile manifest;
                try {
                    manifest = new ZapAddOnXmlFile(zis);
                } catch (IOException e) {
                    return new ValidationResult(ValidationResult.Validity.INVALID_MANIFEST, e);
                }

                if (hasInvalidLibs(file, manifest, zip)) {
                    return new ValidationResult(ValidationResult.Validity.INVALID_LIB);
                }
                return new ValidationResult(manifest);
            }
        } catch (ZipException e) {
            return new ValidationResult(ValidationResult.Validity.UNREADABLE_ZIP_FILE, e);
        } catch (Exception e) {
            return new ValidationResult(ValidationResult.Validity.IO_ERROR_FILE, e);
        }
    }

    private static boolean hasInvalidLibs(Path file, ZapAddOnXmlFile manifest, ZipFile zip) {
        return manifest.getLibs().stream()
                .anyMatch(
                        e -> {
                            ZipEntry libEntry = zip.getEntry(e);
                            if (libEntry == null) {
                                logger.warn("The add-on {} does not have the lib: {}", file, e);
                                return true;
                            }
                            if (libEntry.isDirectory()) {
                                logger.warn("The add-on {} does not have a file lib: {}", file, e);
                                return true;
                            }
                            return false;
                        });
    }

    /**
     * Convenience method that attempts to create an {@code AddOn} from the given file.
     *
     * <p>A warning is logged if the add-on is invalid and an error if an error occurred while
     * creating it.
     *
     * @param file the file of the add-on.
     * @return a container with the {@code AddOn}, or empty if not valid or an error occurred while
     *     creating it.
     * @since 2.8.0
     */
    public static Optional<AddOn> createAddOn(Path file) {
        try {
            return Optional.of(new AddOn(file));
        } catch (AddOn.InvalidAddOnException e) {
            String logMessage = "Invalid add-on: " + file.toString() + ".";
            if (logger.isDebugEnabled() || Constant.isDevMode()) {
                logger.warn(logMessage, e);
            } else {
                logger.warn("{} {}", logMessage, e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Failed to create an add-on from: {}", file, e);
        }
        return Optional.empty();
    }

    /**
     * Constructs an {@code AddOn} from the given {@code file}.
     *
     * <p>The {@value #MANIFEST_FILE_NAME} ZIP file entry is read after validating that the add-on
     * is a valid add-on.
     *
     * <p>The installation status of the add-on is 'not installed'.
     *
     * @param file the file of the add-on
     * @throws InvalidAddOnException (since 2.8.0) if the given {@code file} does not exist, does
     *     not have a valid add-on file name, or an error occurred while reading the add-on manifest
     *     ({@value #MANIFEST_FILE_NAME}).
     * @throws IOException if an error occurred while reading/validating the file.
     * @see #isAddOn(Path)
     */
    public AddOn(Path file) throws IOException {
        ValidationResult result = isValidAddOn(file);
        if (result.getValidity() != ValidationResult.Validity.VALID) {
            throw new InvalidAddOnException(result);
        }
        this.id = extractAddOnId(file.getFileName().toString());
        this.file = file.toFile();
        this.size = Files.size(file);
        readZapAddOnXmlFile(result.getManifest());
    }

    private static String extractAddOnId(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf('.')).split("-")[0];
    }

    private void loadManifestFile() throws IOException {
        manifestRead = true;
        if (file.exists()) {
            // Might not exist in the tests
            try (ZipFile zip = new ZipFile(file)) {
                ZipEntry zapAddOnEntry = zip.getEntry(MANIFEST_FILE_NAME);
                if (zapAddOnEntry == null) {
                    throw new IOException(
                            "Add-on does not have the " + MANIFEST_FILE_NAME + " file.");
                }

                try (InputStream zis = zip.getInputStream(zapAddOnEntry)) {
                    ZapAddOnXmlFile zapAddOnXml = new ZapAddOnXmlFile(zis);
                    readZapAddOnXmlFile(zapAddOnXml);
                }
            }
        }
    }

    private void readZapAddOnXmlFile(ZapAddOnXmlFile zapAddOnXml) {
        this.name = zapAddOnXml.getName();
        this.version = zapAddOnXml.getVersion();
        this.semVer = zapAddOnXml.getSemVer();
        this.status = AddOn.Status.valueOf(zapAddOnXml.getStatus());
        this.description = zapAddOnXml.getDescription();
        this.changes = zapAddOnXml.getChanges();
        this.author = zapAddOnXml.getAuthor();
        this.notBeforeVersion = zapAddOnXml.getNotBeforeVersion();
        this.notFromVersion = zapAddOnXml.getNotFromVersion();
        this.dependencies = zapAddOnXml.getDependencies();
        this.info = createUrl(zapAddOnXml.getUrl());
        this.repo = createUrl(zapAddOnXml.getRepo());

        this.ascanrules = zapAddOnXml.getAscanrules();
        this.extensions = zapAddOnXml.getExtensions();
        this.extensionsWithDeps = zapAddOnXml.getExtensionsWithDeps();
        this.files = zapAddOnXml.getFiles();
        this.libs = createLibs(zapAddOnXml.getLibs());
        this.pscanrules = zapAddOnXml.getPscanrules();

        this.addOnClassnames = zapAddOnXml.getAddOnClassnames();

        String bundleBaseName = zapAddOnXml.getBundleBaseName();
        if (!bundleBaseName.isEmpty()) {
            bundleData = new BundleData(bundleBaseName, zapAddOnXml.getBundlePrefix());
        }

        String helpSetBaseName = zapAddOnXml.getHelpSetBaseName();
        if (!helpSetBaseName.isEmpty()) {
            this.helpSetData =
                    new HelpSetData(helpSetBaseName, zapAddOnXml.getHelpSetLocaleToken());
        }

        hasZapAddOnEntry = true;
    }

    /**
     * Constructs an {@code AddOn} from an add-on entry of {@code ZapVersions.xml} file. The
     * installation status of the add-on is 'not installed'.
     *
     * <p>The {@value #MANIFEST_FILE_NAME} ZIP file entry is read, if the add-on file exists
     * locally.
     *
     * @param id the id of the add-on
     * @param baseDir the base directory where the add-on is located
     * @param xmlData the source of add-on entry of {@code ZapVersions.xml} file
     * @throws MalformedURLException if the {@code URL} of the add-on is malformed
     * @throws IOException if an error occurs while reading the XML data
     */
    public AddOn(String id, File baseDir, SubnodeConfiguration xmlData)
            throws MalformedURLException, IOException {
        this.id = id;
        ZapVersionsAddOnEntry addOnData = new ZapVersionsAddOnEntry(xmlData);
        this.name = addOnData.getName();
        this.description = addOnData.getDescription();
        this.author = addOnData.getAuthor();
        this.dependencies = addOnData.getDependencies();
        this.extensionsWithDeps = addOnData.getExtensionsWithDeps();
        this.version = addOnData.getVersion();
        this.semVer = addOnData.getSemVer();
        this.status = AddOn.Status.valueOf(addOnData.getStatus());
        this.changes = addOnData.getChanges();
        this.url = new URL(addOnData.getUrl());
        this.file = new File(baseDir, addOnData.getFile());
        this.size = addOnData.getSize();
        this.notBeforeVersion = addOnData.getNotBeforeVersion();
        this.notFromVersion = addOnData.getNotFromVersion();
        this.info = createUrl(addOnData.getInfo());
        this.repo = createUrl(addOnData.getRepo());
        this.releaseDate = addOnData.getDate();
        this.hash = addOnData.getHash();

        loadManifestFile();
    }

    private URL createUrl(String url) {
        if (url != null && !url.isEmpty()) {
            try {
                return new URL(url);
            } catch (Exception e) {
                logger.warn("Invalid URL for add-on \"{}\": {}", id, url, e);
            }
        }
        return null;
    }

    private static List<Lib> createLibs(List<String> libPaths) {
        if (libPaths.isEmpty()) {
            return Collections.emptyList();
        }
        return libPaths.stream().map(Lib::new).collect(Collectors.toList());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the file version of the add-on.
     *
     * @return the file version.
     * @deprecated (2.7.0) Use {@link #getVersion()} instead.
     */
    @Deprecated
    public int getFileVersion() {
        return getVersion().getMajorVersion();
    }

    /**
     * Gets the semantic version of this add-on.
     *
     * <p>Since 2.7.0, for add-ons that use just an integer as the version it's appended ".0.0", for
     * example, for version {@literal 14} it returns the version {@literal 14.0.0}.
     *
     * @return the semantic version of the add-on, since 2.7.0, never {@code null}.
     * @since 2.4.0
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Gets the semantic version declared in the manifest file.
     *
     * <p>To be replaced by {@link #getVersion()}.
     *
     * @return the semantic version declared in the manifest file, might be {@code null}.
     */
    Version getSemVer() {
        return semVer;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }

    /**
     * Gets the normalised file name of the add-on.
     *
     * <p>Should be used when copying the file from an "unknown" source (e.g. manually installed).
     *
     * @return the normalised file name.
     * @since 2.6.0
     * @see #getFile()
     */
    public String getNormalisedFileName() {
        return getId() + "-" + getVersion() + FILE_EXTENSION;
    }

    /**
     * Gets the file of the add-on.
     *
     * @return the file of the add-on.
     * @see #getNormalisedFileName()
     */
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets the class loader of the add-on.
     *
     * @return the class loader of the add-on, {@code null} if the add-on is not installed.
     * @since 2.8.0
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Sets the class loader of the add-on.
     *
     * <p><strong>Note:</strong> This method should be called only by bootstrap classes.
     *
     * @param classLoader the class loader of the add-on, might be {@code null}.
     * @since 2.8.0
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Sets the installation status of the add-on.
     *
     * @param installationStatus the new installation status
     * @throws IllegalArgumentException if the given {@code installationStatus} is {@code null}.
     * @since 2.4.0
     */
    public void setInstallationStatus(InstallationStatus installationStatus) {
        if (installationStatus == null) {
            throw new IllegalArgumentException("Parameter installationStatus must not be null.");
        }

        this.installationStatus = installationStatus;
    }

    /**
     * Gets installations status of the add-on.
     *
     * @return the installation status, never {@code null}
     * @since 2.4.0
     */
    public InstallationStatus getInstallationStatus() {
        return installationStatus;
    }

    public boolean hasZapAddOnEntry() {
        if (!hasZapAddOnEntry) {
            if (!manifestRead) {
                // Worth trying, as it depends which constructor has been used
                try {
                    this.loadManifestFile();
                } catch (IOException e) {
                    logger.debug(
                            "Failed to read the {} file of {}:", AddOn.MANIFEST_FILE_NAME, id, e);
                }
            }
        }
        return hasZapAddOnEntry;
    }

    /**
     * Gets the classnames that can be loaded for the add-on.
     *
     * @return the classnames that can be loaded
     * @since 2.4.3
     */
    public AddOnClassnames getAddOnClassnames() {
        return addOnClassnames;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    /**
     * Returns the classnames of {@code Extension}sthat have dependencies on add-ons.
     *
     * @return the classnames of the extensions with dependencies on add-ons.
     * @see #hasExtensionsWithDeps()
     */
    public List<String> getExtensionsWithDeps() {
        if (extensionsWithDeps.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> extensionClassnames = new ArrayList<>(extensionsWithDeps.size());
        for (ExtensionWithDeps extension : extensionsWithDeps) {
            extensionClassnames.add(extension.getClassname());
        }
        return extensionClassnames;
    }

    /**
     * Returns the classnames that can be loaded for the given {@code Extension} (with
     * dependencies).
     *
     * @param classname the classname of the extension
     * @return the classnames that can be loaded
     * @since 2.4.3
     * @see #hasExtensionsWithDeps()
     */
    public AddOnClassnames getExtensionAddOnClassnames(String classname) {
        if (extensionsWithDeps.isEmpty() || classname == null || classname.isEmpty()) {
            return AddOnClassnames.ALL_ALLOWED;
        }

        for (ExtensionWithDeps extension : extensionsWithDeps) {
            if (classname.equals(extension.getClassname())) {
                return extension.getAddOnClassnames();
            }
        }
        return AddOnClassnames.ALL_ALLOWED;
    }

    /**
     * Tells whether or not this add-on has at least one extension with dependencies.
     *
     * @return {@code true} if the add-on has at leas one extension with dependencies, {@code false}
     *     otherwise
     * @see #getExtensionsWithDeps()
     */
    public boolean hasExtensionsWithDeps() {
        return !extensionsWithDeps.isEmpty();
    }

    /**
     * Gets the extensions of this add-on that have dependencies and were loaded.
     *
     * @return an unmodifiable {@code List} with the extensions of this add-on that have
     *     dependencies and were loaded
     * @since 2.4.0
     */
    public List<Extension> getLoadedExtensionsWithDeps() {
        List<String> classnames = getExtensionsWithDeps();
        ArrayList<Extension> loadedExtensions = new ArrayList<>(extensionsWithDeps.size());
        for (Extension extension : getLoadedExtensions()) {
            if (classnames.contains(extension.getClass().getCanonicalName())) {
                loadedExtensions.add(extension);
            }
        }
        loadedExtensions.trimToSize();
        return loadedExtensions;
    }

    /**
     * Gets the extensions of this add-on that were loaded.
     *
     * @return an unmodifiable {@code List} with the extensions of this add-on that were loaded
     * @since 2.4.0
     */
    public List<Extension> getLoadedExtensions() {
        if (loadedExtensions == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(loadedExtensions);
    }

    /**
     * Adds the given {@code extension} to the list of loaded extensions of this add-on.
     *
     * <p>This add-on is set to the given {@code extension}.
     *
     * @param extension the extension of this add-on that was loaded
     * @throws IllegalArgumentException if extension is {@code null}
     * @since 2.4.0
     * @see #removeLoadedExtension(Extension)
     * @see Extension#setAddOn(AddOn)
     */
    public void addLoadedExtension(Extension extension) {
        if (extension == null) {
            throw new IllegalArgumentException("Parameter extension must not be null.");
        }

        if (loadedExtensions == null) {
            loadedExtensions = new ArrayList<>(1);
        }

        if (!loadedExtensions.contains(extension)) {
            loadedExtensions.add(extension);
            extension.setAddOn(this);
        }
    }

    /**
     * Removes the given {@code extension} from the list of loaded extensions of this add-on.
     *
     * <p>The add-on of the given {@code extension} is set to {@code null}.
     *
     * <p>The call to this method has no effect if the given {@code extension} does not belong to
     * this add-on.
     *
     * @param extension the loaded extension of this add-on that should be removed
     * @throws IllegalArgumentException if extension is {@code null}
     * @since 2.4.0
     * @see #addLoadedExtension(Extension)
     * @see Extension#setAddOn(AddOn)
     */
    public void removeLoadedExtension(Extension extension) {
        if (extension == null) {
            throw new IllegalArgumentException("Parameter extension must not be null.");
        }

        if (loadedExtensions != null && loadedExtensions.contains(extension)) {
            loadedExtensions.remove(extension);
            extension.setAddOn(null);
        }
    }

    public List<String> getAscanrules() {
        return ascanrules;
    }

    /**
     * Gets the active scan rules of this add-on that were loaded.
     *
     * @return an unmodifiable {@code List} with the active scan rules of this add-on that were
     *     loaded, never {@code null}
     * @since 2.4.3
     * @see #setLoadedAscanrules(List)
     */
    public List<AbstractPlugin> getLoadedAscanrules() {
        return loadedAscanrules;
    }

    /**
     * Sets the loaded active scan rules of the add-on, allowing to set the status of the active
     * scan rules appropriately and to keep track of the active scan rules loaded so that they can
     * be removed during uninstallation.
     *
     * <p><strong>Note:</strong> Helper method to be used (only) by/during (un)installation process
     * and loading of the add-on. Should be called when installing/loading the add-on, by setting
     * the loaded active scan rules, and when uninstalling by setting an empty list. The method
     * {@code setLoadedAscanrulesSet(boolean)} should also be called.
     *
     * @param ascanrules the active scan rules loaded, might be empty if none were actually loaded
     * @throws IllegalArgumentException if {@code ascanrules} is {@code null}.
     * @since 2.4.3
     * @see #setLoadedAscanrulesSet(boolean)
     * @see AbstractPlugin#setStatus(Status)
     */
    void setLoadedAscanrules(List<AbstractPlugin> ascanrules) {
        if (ascanrules == null) {
            throw new IllegalArgumentException("Parameter ascanrules must not be null.");
        }

        if (ascanrules.isEmpty()) {
            loadedAscanrules = Collections.emptyList();
            return;
        }

        for (AbstractPlugin ascanrule : ascanrules) {
            ascanrule.setStatus(getStatus());
        }
        loadedAscanrules = Collections.unmodifiableList(new ArrayList<>(ascanrules));
    }

    /**
     * Tells whether or not the loaded active scan rules of the add-on, if any, were already set to
     * the add-on.
     *
     * <p><strong>Note:</strong> Helper method to be used (only) by/during (un)installation process
     * and loading of the add-on.
     *
     * @return {@code true} if the loaded active scan rules were already set, {@code false}
     *     otherwise
     * @since 2.4.3
     * @see #setLoadedAscanrules(List)
     * @see #setLoadedAscanrulesSet(boolean)
     */
    boolean isLoadedAscanrulesSet() {
        return loadedAscanRulesSet;
    }

    /**
     * Sets whether or not the loaded active scan rules, if any, where already set to the add-on.
     *
     * <p><strong>Note:</strong> Helper method to be used (only) by/during (un)installation process
     * and loading of the add-on. The method should be called, with {@code true} during
     * installation/loading and {@code false} during uninstallation, after calling the method {@code
     * setLoadedAscanrules(List)}.
     *
     * @param ascanrulesSet {@code true} if the loaded active scan rules were already set, {@code
     *     false} otherwise
     * @since 2.4.3
     * @see #setLoadedAscanrules(List)
     */
    void setLoadedAscanrulesSet(boolean ascanrulesSet) {
        loadedAscanRulesSet = ascanrulesSet;
    }

    public List<String> getPscanrules() {
        return pscanrules;
    }

    /**
     * Gets the passive scan rules of this add-on that were loaded.
     *
     * @return an unmodifiable {@code List} with the passive scan rules of this add-on that were
     *     loaded, never {@code null}
     * @since 2.4.3
     * @see #setLoadedPscanrules(List)
     */
    public List<PluginPassiveScanner> getLoadedPscanrules() {
        return loadedPscanrules;
    }

    /**
     * Sets the loaded passive scan rules of the add-on, allowing to set the status of the passive
     * scan rules appropriately and keep track of the passive scan rules loaded so that they can be
     * removed during uninstallation.
     *
     * <p><strong>Note:</strong> Helper method to be used (only) by/during (un)installation process
     * and loading of the add-on. Should be called when installing/loading the add-on, by setting
     * the loaded passive scan rules, and when uninstalling by setting an empty list. The method
     * {@code setLoadedPscanrulesSet(boolean)} should also be called.
     *
     * @param pscanrules the passive scan rules loaded, might be empty if none were actually loaded
     * @throws IllegalArgumentException if {@code pscanrules} is {@code null}.
     * @since 2.4.3
     * @see #setLoadedPscanrulesSet(boolean)
     * @see PluginPassiveScanner#setStatus(Status)
     */
    void setLoadedPscanrules(List<PluginPassiveScanner> pscanrules) {
        if (pscanrules == null) {
            throw new IllegalArgumentException("Parameter pscanrules must not be null.");
        }

        if (pscanrules.isEmpty()) {
            loadedPscanrules = Collections.emptyList();
            return;
        }

        for (PluginPassiveScanner pscanrule : pscanrules) {
            pscanrule.setStatus(getStatus());
        }
        loadedPscanrules = Collections.unmodifiableList(new ArrayList<>(pscanrules));
    }

    /**
     * Tells whether or not the loaded passive scan rules of the add-on, if any, were already set to
     * the add-on.
     *
     * <p><strong>Note:</strong> Helper method to be used (only) by/during (un)installation process
     * and loading of the add-on.
     *
     * @return {@code true} if the loaded passive scan rules were already set, {@code false}
     *     otherwise
     * @since 2.4.3
     * @see #setLoadedPscanrules(List)
     * @see #setLoadedPscanrulesSet(boolean)
     */
    boolean isLoadedPscanrulesSet() {
        return loadedPscanRulesSet;
    }

    /**
     * Sets whether or not the loaded passive scan rules, if any, where already set to the add-on.
     *
     * <p><strong>Note:</strong> Helper method to be used (only) by/during (un)installation process
     * and loading of the add-on. The method should be called, with {@code true} during
     * installation/loading and {@code false} during uninstallation, after calling the method {@code
     * setLoadedPscanrules(List)}.
     *
     * @param pscanrulesSet {@code true} if the loaded passive scan rules were already set, {@code
     *     false} otherwise
     * @since 2.4.3
     * @see #setLoadedPscanrules(List)
     */
    void setLoadedPscanrulesSet(boolean pscanrulesSet) {
        loadedPscanRulesSet = pscanrulesSet;
    }

    public List<String> getFiles() {
        return files;
    }

    /**
     * Gets the bundled libraries of the add-on.
     *
     * @return the bundled libraries, never {@code null}.
     */
    List<Lib> getLibs() {
        return libs;
    }

    private boolean hasMissingLibs() {
        return libs.stream().anyMatch(lib -> lib.getFileSystemUrl() == null);
    }

    public boolean isSameAddOn(AddOn addOn) {
        return this.getId().equals(addOn.getId());
    }

    public boolean isUpdateTo(AddOn addOn) throws IllegalArgumentException {
        if (!this.isSameAddOn(addOn)) {
            throw new IllegalArgumentException(
                    "Different addons: " + this.getId() + " != " + addOn.getId());
        }
        int result = this.getVersion().compareTo(addOn.getVersion());
        if (result != 0) {
            return result > 0;
        }

        result = this.getStatus().compareTo(addOn.getStatus());
        if (result != 0) {
            return result > 0;
        }

        if (getFile() == null) {
            return false;
        }
        if (addOn.getFile() == null) {
            return true;
        }
        return getFile().lastModified() > addOn.getFile().lastModified();
    }

    /**
     * Tells whether or not this add-on can be loaded in the currently running ZAP version, as given
     * by {@code Constant.PROGRAM_VERSION}.
     *
     * @return {@code true} if the add-on can be loaded in the currently running ZAP version, {@code
     *     false} otherwise
     * @see #canLoadInVersion(String)
     * @see Constant#PROGRAM_VERSION
     */
    public boolean canLoadInCurrentVersion() {
        return canLoadInVersion(Constant.PROGRAM_VERSION);
    }

    /**
     * Tells whether or not this add-on can be run in the currently running Java version.
     *
     * <p>This is a convenience method that calls {@code canRunInJavaVersion(String)} with the
     * running Java version (as given by {@code SystemUtils.JAVA_VERSION}) as parameter.
     *
     * @return {@code true} if the add-on can be run in the currently running Java version, {@code
     *     false} otherwise
     * @since 2.4.0
     * @see #canRunInJavaVersion(String)
     * @see SystemUtils#JAVA_VERSION
     */
    public boolean canRunInCurrentJavaVersion() {
        return canRunInJavaVersion(SystemUtils.JAVA_VERSION);
    }

    /**
     * Tells whether or not this add-on can be run in the given {@code javaVersion}.
     *
     * <p>If the given {@code javaVersion} is {@code null} and this add-on depends on a specific
     * java version the method returns {@code false}.
     *
     * @param javaVersion the java version that will be checked
     * @return {@code true} if the add-on can be loaded in the given {@code javaVersion}, {@code
     *     false} otherwise
     * @since 2.4.0
     */
    public boolean canRunInJavaVersion(String javaVersion) {
        if (dependencies == null) {
            return true;
        }

        String requiredVersion = dependencies.getJavaVersion();
        if (requiredVersion == null) {
            return true;
        }

        if (javaVersion == null) {
            return false;
        }

        return getJavaVersion(javaVersion) >= getJavaVersion(requiredVersion);
    }

    /**
     * Calculates the requirements to run this add-on, in the current ZAP and Java versions and with
     * the given {@code availableAddOns}.
     *
     * <p>If the add-on depends on other add-ons, those add-ons are also checked if are also
     * runnable.
     *
     * <p><strong>Note:</strong> All the given {@code availableAddOns} are expected to be loadable
     * in the currently running ZAP version, that is, the method {@code
     * AddOn.canLoadInCurrentVersion()}, returns {@code true}.
     *
     * @param availableAddOns the other add-ons available
     * @return a requirements to run the add-on, and if not runnable the reason why it's not.
     * @since 2.4.0
     * @see #canLoadInCurrentVersion()
     * @see #canRunInCurrentJavaVersion()
     * @see AddOnRunRequirements
     */
    public AddOnRunRequirements calculateRunRequirements(Collection<AddOn> availableAddOns) {
        return calculateRunRequirements(availableAddOns, true);
    }

    /**
     * Calculates the requirements to install/run this add-on, same as {@link
     * #calculateRunRequirements(Collection)} but does not require the libraries of the add-ons
     * (this or dependencies) to exist in the file system.
     *
     * @param availableAddOns the other add-ons available.
     * @return the requirements to install/run the add-on, and if not runnable the reason why it's
     *     not.
     * @since 2.9.0
     * @see #calculateRunRequirements(Collection)
     * @see AddOnRunRequirements
     */
    public AddOnRunRequirements calculateInstallRequirements(Collection<AddOn> availableAddOns) {
        return calculateRunRequirements(availableAddOns, false);
    }

    private AddOnRunRequirements calculateRunRequirements(
            Collection<AddOn> availableAddOns, boolean checkLibs) {
        AddOnRunRequirements requirements = new AddOnRunRequirements(this);
        calculateRunRequirementsImpl(availableAddOns, requirements, null, this, checkLibs);
        if (requirements.isRunnable()) {
            checkExtensionsWithDeps(availableAddOns, requirements, this, checkLibs);
        }
        return requirements;
    }

    private static void calculateRunRequirementsImpl(
            Collection<AddOn> availableAddOns,
            BaseRunRequirements requirements,
            AddOn parent,
            AddOn addOn,
            boolean checkLibs) {
        if (checkLibs && addOn.hasMissingLibs()) {
            requirements.setMissingLibsIssue(addOn);
            return;
        }

        AddOn installedVersion = getAddOn(availableAddOns, addOn.getId());
        if (installedVersion != null && !addOn.equals(installedVersion)) {
            requirements.setIssue(
                    BaseRunRequirements.DependencyIssue.OLDER_VERSION, installedVersion);
            logger.debug(
                    "Add-on {} not runnable, old version still installed: {}",
                    addOn,
                    installedVersion);
            return;
        }

        if (!requirements.addDependency(parent, addOn)) {
            logger.warn("Cyclic dependency detected with: {}", requirements.getDependencies());
            requirements.setIssue(
                    BaseRunRequirements.DependencyIssue.CYCLIC, requirements.getDependencies());
            return;
        }

        if (addOn.dependencies == null) {
            return;
        }

        if (!addOn.canRunInCurrentJavaVersion()) {
            requirements.setMinimumJavaVersionIssue(addOn, addOn.dependencies.getJavaVersion());
        }

        for (AddOnDep dependency : addOn.dependencies.getAddOns()) {
            String addOnId = dependency.getId();
            if (addOnId != null) {
                AddOn addOnDep = getAddOn(availableAddOns, addOnId);
                if (addOnDep == null) {
                    requirements.setIssue(BaseRunRequirements.DependencyIssue.MISSING, addOnId);
                    return;
                }

                if (!dependency.getVersion().isEmpty()) {
                    if (!versionMatches(addOnDep, dependency)) {
                        requirements.setIssue(
                                BaseRunRequirements.DependencyIssue.VERSION,
                                addOnDep,
                                dependency.getVersion());
                        return;
                    }
                }

                calculateRunRequirementsImpl(
                        availableAddOns, requirements, addOn, addOnDep, checkLibs);
                if (requirements.hasDependencyIssue()) {
                    return;
                }
            }
        }
    }

    private static void checkExtensionsWithDeps(
            Collection<AddOn> availableAddOns,
            AddOnRunRequirements requirements,
            AddOn addOn,
            boolean checkLibs) {
        if (addOn.extensionsWithDeps.isEmpty()) {
            return;
        }

        for (ExtensionWithDeps extension : addOn.extensionsWithDeps) {
            calculateExtensionRunRequirements(
                    extension, availableAddOns, requirements, addOn, checkLibs);
        }
    }

    private static void calculateExtensionRunRequirements(
            ExtensionWithDeps extension,
            Collection<AddOn> availableAddOns,
            AddOnRunRequirements requirements,
            AddOn addOn,
            boolean checkLibs) {
        ExtensionRunRequirements extensionRequirements =
                new ExtensionRunRequirements(addOn, extension.getClassname());
        requirements.addExtensionRequirements(extensionRequirements);
        for (AddOnDep dependency : extension.getDependencies()) {
            String addOnId = dependency.getId();
            if (addOnId == null) {
                continue;
            }

            AddOn addOnDep = getAddOn(availableAddOns, addOnId);
            if (addOnDep == null) {
                if (addOn.hasOnlyOneExtensionWithDependencies()) {
                    requirements.setIssue(BaseRunRequirements.DependencyIssue.MISSING, addOnId);
                    return;
                }
                extensionRequirements.setIssue(
                        BaseRunRequirements.DependencyIssue.MISSING, addOnId);
                continue;
            }

            if (!dependency.getVersion().isEmpty()) {
                if (!versionMatches(addOnDep, dependency)) {
                    if (addOn.hasOnlyOneExtensionWithDependencies()) {
                        requirements.setIssue(
                                BaseRunRequirements.DependencyIssue.VERSION,
                                addOnDep,
                                dependency.getVersion());
                        return;
                    }
                    extensionRequirements.setIssue(
                            BaseRunRequirements.DependencyIssue.VERSION,
                            addOnDep,
                            dependency.getVersion());
                    continue;
                }
            }

            calculateRunRequirementsImpl(
                    availableAddOns, extensionRequirements, addOn, addOnDep, checkLibs);
        }
    }

    private boolean hasOnlyOneExtensionWithDependencies() {
        if (extensionsWithDeps.size() != 1) {
            return false;
        }
        if (extensions.isEmpty()
                && files.isEmpty()
                && pscanrules.isEmpty()
                && ascanrules.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Calculates the requirements to run the given {@code extension}, in the current ZAP and Java
     * versions and with the given {@code availableAddOns}.
     *
     * <p>If the extension depends on other add-ons, those add-ons are checked if are also runnable.
     *
     * <p><strong>Note:</strong> All the given {@code availableAddOns} are expected to be loadable
     * in the currently running ZAP version, that is, the method {@code
     * AddOn.canLoadInCurrentVersion()}, returns {@code true}.
     *
     * @param extension the extension that will be checked
     * @param availableAddOns the add-ons available
     * @return the requirements to run the extension, and if not runnable the reason why it's not.
     * @since 2.4.0
     * @see AddOnRunRequirements#getExtensionRequirements()
     */
    public AddOnRunRequirements calculateExtensionRunRequirements(
            Extension extension, Collection<AddOn> availableAddOns) {
        return calculateExtensionRunRequirementsImpl(
                extension.getClass().getCanonicalName(), availableAddOns, true);
    }

    /**
     * Calculates the requirements to install/run the given {@code extension}, same as {@link
     * #calculateExtensionRunRequirements(Extension, Collection)} but does not require the libraries
     * of the add-ons (this or dependencies) to exist in the file system.
     *
     * @param extension the extension that will be checked.
     * @param availableAddOns the add-ons available.
     * @return the requirements to install/run the extension, and if not runnable the reason why
     *     it's not.
     * @since 2.9.0
     * @see AddOnRunRequirements#getExtensionRequirements()
     */
    public AddOnRunRequirements calculateExtensionInstallRequirements(
            Extension extension, Collection<AddOn> availableAddOns) {
        return calculateExtensionRunRequirementsImpl(
                extension.getClass().getCanonicalName(), availableAddOns, false);
    }

    /**
     * Calculates the requirements to run the extension with the given {@code classname}, in the
     * current ZAP and Java versions and with the given {@code availableAddOns}.
     *
     * <p>If the extension depends on other add-ons, those add-ons are checked if are also runnable.
     *
     * <p><strong>Note:</strong> All the given {@code availableAddOns} are expected to be loadable
     * in the currently running ZAP version, that is, the method {@code
     * AddOn.canLoadInCurrentVersion()}, returns {@code true}.
     *
     * @param classname the classname of extension that will be checked
     * @param availableAddOns the add-ons available
     * @return the requirements to run the extension, and if not runnable the reason why it's not.
     * @since 2.4.0
     * @see AddOnRunRequirements#getExtensionRequirements()
     */
    public AddOnRunRequirements calculateExtensionRunRequirements(
            String classname, Collection<AddOn> availableAddOns) {
        return calculateExtensionRunRequirementsImpl(classname, availableAddOns, true);
    }

    /**
     * Calculates the requirements to install/run the given {@code extension}, same as {@link
     * #calculateExtensionRunRequirements(String, Collection)} but does not require the libraries of
     * the add-ons (this or dependencies) to exist in the file system.
     *
     * @param classname the classname of extension that will be checked.
     * @param availableAddOns the add-ons available.
     * @return the requirements to install/run the extension, and if not runnable the reason why
     *     it's not.
     * @since 2.9.0
     * @see AddOnRunRequirements#getExtensionRequirements()
     */
    public AddOnRunRequirements calculateExtensionInstallRequirements(
            String classname, Collection<AddOn> availableAddOns) {
        return calculateExtensionRunRequirementsImpl(classname, availableAddOns, false);
    }

    private AddOnRunRequirements calculateExtensionRunRequirementsImpl(
            String classname, Collection<AddOn> availableAddOns, boolean checkLibs) {
        AddOnRunRequirements requirements = new AddOnRunRequirements(this);
        for (ExtensionWithDeps extensionWithDeps : extensionsWithDeps) {
            if (extensionWithDeps.getClassname().equals(classname)) {
                calculateExtensionRunRequirements(
                        extensionWithDeps, availableAddOns, requirements, this, checkLibs);
                break;
            }
        }
        return requirements;
    }

    /**
     * Tells whether or not the given {@code extension} has a (direct) dependency on the given
     * {@code addOn} (including version).
     *
     * @param extension the extension that will be checked
     * @param addOn the add-on that will be checked in the dependencies on the extension
     * @return {@code true} if the extension depends on the given add-on, {@code false} otherwise.
     * @since 2.4.0
     */
    public boolean dependsOn(Extension extension, AddOn addOn) {
        String classname = extension.getClass().getCanonicalName();

        for (ExtensionWithDeps extensionWithDeps : extensionsWithDeps) {
            if (extensionWithDeps.getClassname().equals(classname)) {
                return dependsOn(extensionWithDeps.getDependencies(), addOn);
            }
        }
        return false;
    }

    private static boolean dependsOn(List<AddOnDep> dependencies, AddOn addOn) {
        for (AddOnDep dependency : dependencies) {
            if (dependency.getId().equals(addOn.id)) {
                if (!dependency.getVersion().isEmpty()) {
                    return versionMatches(addOn, dependency);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Tells whether or not the given add-on version matches the one required by the dependency.
     *
     * <p>This methods is required to also check the {@code semVer} of the add-on, once removed it
     * can match the version directly.
     *
     * @param addOn the add-on to check
     * @param dependency the dependency
     * @return {@code true} if the version matches, {@code false} otherwise.
     */
    private static boolean versionMatches(AddOn addOn, AddOnDep dependency) {
        if (addOn.version.matches(dependency.getVersion())) {
            return true;
        }

        if (addOn.semVer != null && addOn.semVer.matches(dependency.getVersion())) {
            return true;
        }

        return false;
    }

    /**
     * Tells whether or not the extension with the given {@code classname} is loaded.
     *
     * @param classname the classname of the extension
     * @return {@code true} if the extension is loaded, {@code false} otherwise
     * @since 2.4.0
     */
    public boolean isExtensionLoaded(String classname) {
        for (Extension extension : getLoadedExtensions()) {
            if (classname.equals(extension.getClass().getCanonicalName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the minimum Java version required to run this add-on or an empty {@code String} if
     * there's no minimum version.
     *
     * @return the minimum Java version required to run this add-on or an empty {@code String} if no
     *     minimum version
     * @since 2.4.0
     */
    public String getMinimumJavaVersion() {
        if (dependencies == null) {
            return "";
        }
        return dependencies.getJavaVersion();
    }

    /**
     * Gets the add-on with the given {@code id} from the given collection of {@code addOns}.
     *
     * @param addOns the collection of add-ons where the search will be made
     * @param id the id of the add-on to search for
     * @return the {@code AddOn} with the given id, or {@code null} if not found
     */
    private static AddOn getAddOn(Collection<AddOn> addOns, String id) {
        for (AddOn addOn : addOns) {
            if (addOn.getId().equals(id)) {
                return addOn;
            }
        }
        return null;
    }

    /**
     * Tells whether or not this add-on can be loaded in the given {@code zapVersion}.
     *
     * @param zapVersion the ZAP version that will be checked
     * @return {@code true} if the add-on can be loaded in the given {@code zapVersion}, {@code
     *     false} otherwise
     */
    public boolean canLoadInVersion(String zapVersion) {
        // Require add-ons to declare the version they implement
        if (this.notBeforeVersion == null || this.notBeforeVersion.isEmpty()) {
            return false;
        }

        ZapReleaseComparitor zrc = new ZapReleaseComparitor();
        ZapRelease zr = new ZapRelease(zapVersion);
        ZapRelease notBeforeRelease = new ZapRelease(this.notBeforeVersion);
        if (zrc.compare(zr, notBeforeRelease) < 0) {
            return false;
        }

        if (zrc.compare(notBeforeRelease, v2_4) < 0) {
            // Don't load any add-ons that imply they are prior to 2.4.0 - they probably wont work
            return false;
        }
        if (this.notFromVersion != null && this.notFromVersion.length() > 0) {
            ZapRelease notFromRelease = new ZapRelease(this.notFromVersion);
            return (zrc.compare(zr, notFromRelease) < 0);
        }
        return true;
    }

    public void setNotBeforeVersion(String notBeforeVersion) {
        this.notBeforeVersion = notBeforeVersion;
    }

    public void setNotFromVersion(String notFromVersion) {
        this.notFromVersion = notFromVersion;
    }

    public String getNotBeforeVersion() {
        return notBeforeVersion;
    }

    public String getNotFromVersion() {
        return notFromVersion;
    }

    public URL getInfo() {
        return info;
    }

    public void setInfo(URL info) {
        this.info = info;
    }

    /**
     * Gets the URL to the browsable repo.
     *
     * @return the URL to the repo, might be {@code null}.
     * @since 2.9.0
     */
    public URL getRepo() {
        return repo;
    }

    public String getHash() {
        return hash;
    }

    /**
     * Gets the date when the add-on was released.
     *
     * <p>The date has the format {@code YYYY-MM-DD}.
     *
     * <p><strong>Note:</strong> The date is only available for add-ons created from the
     * marketplace.
     *
     * @return the release date, or {@code null} if not available.
     * @since 2.10.0
     */
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * Gets the data of the bundle declared in the manifest.
     *
     * @return the bundle data, never {@code null}.
     * @since 2.8.0
     * @see #getResourceBundle()
     */
    public BundleData getBundleData() {
        return bundleData;
    }

    /**
     * Gets the resource bundle of the add-on.
     *
     * @return the resource bundle, or {@code null} if none.
     * @since 2.8.0
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * Sets the resource bundle of the add-on.
     *
     * <p><strong>Note:</strong> This method should be called only by bootstrap classes.
     *
     * @param resourceBundle the resource bundle of the add-on, might be {@code null}.
     * @since 2.8.0
     * @see #getBundleData()
     */
    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * Gets the data for the HelpSet, declared in the manifest.
     *
     * @return the HelpSet data, never {@code null}.
     * @since 2.8.0
     */
    public HelpSetData getHelpSetData() {
        return helpSetData;
    }

    /**
     * Sets if this add-on is mandatory.
     *
     * <p><strong>Note:</strong> This method should be called only by bootstrap classes.
     *
     * @param mandatory {@code true} if the add-on is mandatory, {@code false} otherwise.
     * @since 2.12.0
     * @see #isMandatory()
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * Tells whether or not this add-on is mandatory.
     *
     * @return {@code true} if the add-on is mandatory, {@code false} otherwise.
     * @since 2.12.0
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * Returns the IDs of the add-ons dependencies, an empty collection if none.
     *
     * @return the IDs of the dependencies.
     * @since 2.4.0
     */
    public List<String> getIdsAddOnDependencies() {
        if (dependencies == null) {
            return Collections.emptyList();
        }

        List<String> ids = new ArrayList<>(dependencies.getAddOns().size());
        for (AddOnDep dep : dependencies.getAddOns()) {
            ids.add(dep.getId());
        }
        return ids;
    }

    /**
     * Tells whether or not this add-on has a (direct) dependency on the given {@code addOn}
     * (including version).
     *
     * @param addOn the add-on that will be checked
     * @return {@code true} if it depends on the given add-on, {@code false} otherwise.
     * @since 2.4.0
     */
    public boolean dependsOn(AddOn addOn) {
        if (dependencies == null || dependencies.getAddOns().isEmpty()) {
            return false;
        }

        return dependsOn(dependencies.getAddOns(), addOn);
    }

    /**
     * Tells whether or not this add-on has a (direct) dependency on any of the given {@code addOns}
     * (including version).
     *
     * @param addOns the add-ons that will be checked
     * @return {@code true} if it depends on any of the given add-ons, {@code false} otherwise.
     * @since 2.4.0
     */
    public boolean dependsOn(Collection<AddOn> addOns) {
        if (dependencies == null || dependencies.getAddOns().isEmpty()) {
            return false;
        }

        for (AddOn addOn : addOns) {
            if (dependsOn(addOn)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("[id=").append(id);
        strBuilder.append(", version=").append(version);
        strBuilder.append(']');

        return strBuilder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + version.hashCode();
        return result;
    }

    /** Two add-ons are considered equal if both add-ons have the same ID and version. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AddOn other = (AddOn) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return version.equals(other.version);
    }

    /**
     * A library (JAR) bundled in the add-on.
     *
     * <p>Bundled libraries are copied and loaded from the file system, which allows to properly
     * maintain/access all JAR's data (e.g. module info, manifest, services).
     */
    static class Lib {
        private final String jarPath;
        private final String name;
        private URL fileSystemUrl;

        Lib(String path) {
            jarPath = path;
            name = extractName(path);
        }

        String getJarPath() {
            return jarPath;
        }

        String getName() {
            return name;
        }

        URL getFileSystemUrl() {
            return fileSystemUrl;
        }

        void setFileSystemUrl(URL fileSystemUrl) {
            this.fileSystemUrl = fileSystemUrl;
        }

        private static String extractName(String path) {
            int idx = path.lastIndexOf('/');
            if (idx == -1) {
                return path;
            }
            return path.substring(idx + 1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jarPath);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            return Objects.equals(jarPath, ((Lib) obj).jarPath);
        }
    }

    public abstract static class BaseRunRequirements {

        /**
         * The reason why an add-on can not be run because of a dependency.
         *
         * <p>More details of the issue can be obtained with the method {@code
         * RunRequirements.getDependencyIssueDetails()}. The exact contents are mentioned in each
         * {@code DependencyIssue} constant.
         *
         * @see AddOnRunRequirements#getDependencyIssueDetails()
         */
        public enum DependencyIssue {

            /**
             * A cyclic dependency was detected.
             *
             * <p>Issue details contain all the add-ons in the cyclic chain.
             */
            CYCLIC,

            /**
             * Older version of the add-on is still installed.
             *
             * <p>Issue details contain the old version.
             */
            OLDER_VERSION,

            /**
             * A dependency was not found.
             *
             * <p>Issue details contain the id of the add-on.
             */
            MISSING,

            /**
             * The dependency found has a older version than the version required.
             *
             * <p>Issue details contain the instance of the {@code AddOn} and the required version.
             *
             * @deprecated (2.7.0) No longer in use. It should be used just {@link #VERSION}.
             */
            @Deprecated
            PACKAGE_VERSION_NOT_BEFORE,

            /**
             * The dependency found has a newer version than the version required.
             *
             * <p>Issue details contain the instance of the {@code AddOn} and the required version.
             *
             * @deprecated (2.7.0) No longer in use. It should be used just {@link #VERSION}.
             */
            @Deprecated
            PACKAGE_VERSION_NOT_FROM,

            /**
             * The dependency found has a different version.
             *
             * <p>Issue details contain the instance of the {@code AddOn} and the required version.
             */
            VERSION
        }

        private final AddOn addOn;
        private final DirectedGraph<AddOn, DefaultEdge> dependencyTree;
        private Set<AddOn> dependencies;

        private DependencyIssue depIssue;
        private List<Object> issueDetails;

        private String minimumJavaVersion;
        private AddOn addOnMinimumJavaVersion;
        private AddOn addOnMissingLibs;

        private boolean runnable;

        private BaseRunRequirements(AddOn addOn) {
            this.addOn = addOn;
            dependencyTree = new DefaultDirectedGraph<>(DefaultEdge.class);
            dependencyTree.addVertex(addOn);
            runnable = true;
            issueDetails = Collections.emptyList();
        }

        /**
         * Gets the add-on that was tested to check if it can be run.
         *
         * @return the tested add-on
         */
        public AddOn getAddOn() {
            return addOn;
        }

        /**
         * Tells whether or not this add-on has a dependency issue.
         *
         * @return {@code true} if the add-on has a dependency issue, {@code false} otherwise
         * @see #getDependencyIssue()
         * @see #getDependencyIssueDetails()
         * @see DependencyIssue
         */
        public boolean hasDependencyIssue() {
            return (depIssue != null);
        }

        /**
         * Gets the dependency issue, if any.
         *
         * @return the {@code DependencyIssue} or {@code null}, if none
         * @see #hasDependencyIssue()
         * @see #getDependencyIssueDetails()
         * @see DependencyIssue
         */
        public DependencyIssue getDependencyIssue() {
            return depIssue;
        }

        /**
         * Gets the details of the dependency issue, if any.
         *
         * @return a list containing the details of the issue or an empty list if none
         * @see #hasDependencyIssue()
         * @see #getDependencyIssue()
         * @see DependencyIssue
         */
        public List<Object> getDependencyIssueDetails() {
            return issueDetails;
        }

        /**
         * Tells whether or not this add-on can be run.
         *
         * @return {@code true} if the add-on can be run, {@code false} otherwise
         */
        public boolean isRunnable() {
            return runnable;
        }

        protected void setRunnable(boolean runnable) {
            this.runnable = runnable;
        }

        /**
         * Gets the (found) dependencies of the add-on, including transitive dependencies.
         *
         * @return a set containing the dependencies of the add-on
         * @see AddOn#getIdsAddOnDependencies()
         */
        public Set<AddOn> getDependencies() {
            if (dependencies == null) {
                dependencies = new HashSet<>();
                for (TopologicalOrderIterator<AddOn, DefaultEdge> it =
                                new TopologicalOrderIterator<>(dependencyTree);
                        it.hasNext(); ) {
                    dependencies.add(it.next());
                }
                dependencies.remove(addOn);
            }
            return Collections.unmodifiableSet(dependencies);
        }

        protected void setIssue(DependencyIssue issue, Object... details) {
            runnable = false;
            this.depIssue = issue;
            if (details != null) {
                issueDetails = Arrays.asList(details);
            } else {
                issueDetails = Collections.emptyList();
            }
        }

        protected boolean addDependency(AddOn parent, AddOn addOn) {
            if (parent == null) {
                return true;
            }

            dependencyTree.addVertex(parent);
            dependencyTree.addVertex(addOn);

            dependencyTree.addEdge(parent, addOn);

            CycleDetector<AddOn, DefaultEdge> cycleDetector = new CycleDetector<>(dependencyTree);
            boolean cycle = cycleDetector.detectCycles();
            if (cycle) {
                dependencies = cycleDetector.findCycles();

                return false;
            }
            return true;
        }

        /**
         * Tells whether or not this add-on requires a newer Java version to run.
         *
         * <p>The requirement might be imposed by a dependency or the add-on itself. To check which
         * one use the methods {@code getAddOn()} and {@code getAddOnMinimumJavaVersion()}.
         *
         * @return {@code true} if the add-on requires a newer Java version, {@code false}
         *     otherwise.
         * @see #getAddOn()
         * @see #getAddOnMinimumJavaVersion()
         * @see #getMinimumJavaVersion()
         */
        public boolean isNewerJavaVersionRequired() {
            return (minimumJavaVersion != null);
        }

        /**
         * Gets the minimum Java version required to run the add-on.
         *
         * @return the Java version, or {@code null} if no minimum.
         * @see #isNewerJavaVersionRequired()
         * @see #getAddOn()
         * @see #getAddOnMinimumJavaVersion()
         */
        public String getMinimumJavaVersion() {
            return minimumJavaVersion;
        }

        /**
         * Gets the add-on that requires the minimum Java version.
         *
         * @return the add-on, or {@code null} if no minimum.
         * @see #isNewerJavaVersionRequired()
         * @see #getMinimumJavaVersion()
         * @see #getAddOn()
         */
        public AddOn getAddOnMinimumJavaVersion() {
            return addOnMinimumJavaVersion;
        }

        protected void setMinimumJavaVersionIssue(AddOn srcAddOn, String requiredVersion) {
            setRunnable(false);

            if (minimumJavaVersion == null) {
                setMinimumJavaVersion(srcAddOn, requiredVersion);
            } else if (getJavaVersion(requiredVersion) > getJavaVersion(minimumJavaVersion)) {
                setMinimumJavaVersion(srcAddOn, requiredVersion);
            }
        }

        private void setMinimumJavaVersion(AddOn srcAddOn, String requiredVersion) {
            addOnMinimumJavaVersion = srcAddOn;
            minimumJavaVersion = requiredVersion;
        }

        /**
         * Tells whether or not this add-on has missing libraries (that is, not present in the file
         * system).
         *
         * <p>The issue might be caused by a dependency or the add-on itself. To check which one use
         * the methods {@link #getAddOn()} and {@link #getAddOnMissingLibs()}.
         *
         * @return {@code true} if the add-on has missing libraries, {@code false} otherwise.
         * @since 2.9.0
         */
        public boolean hasMissingLibs() {
            return addOnMissingLibs != null;
        }

        /**
         * Gets the add-on that has missing libraries.
         *
         * @return the add-on, or {@code null} if none.
         * @see #hasMissingLibs()
         * @see #getAddOn()
         * @since 2.9.0
         */
        public AddOn getAddOnMissingLibs() {
            return addOnMissingLibs;
        }

        protected void setMissingLibsIssue(AddOn srcAddOn) {
            setRunnable(false);
            addOnMissingLibs = srcAddOn;
        }
    }

    /**
     * The requirements to run an {@code AddOn}.
     *
     * <p>It can be used to check if an add-on can or not be run, which requirements it has (for
     * example, minimum Java version or dependency add-ons) and which issues prevent it from being
     * run, if any.
     *
     * @since 2.4.0
     */
    public static class AddOnRunRequirements extends BaseRunRequirements {

        private List<ExtensionRunRequirements> addExtensionsRequirements;

        private AddOnRunRequirements(AddOn addOn) {
            super(addOn);
        }

        /**
         * Gets the run requirements of the extensions that have dependencies.
         *
         * @return a {@code List} containing the requirements of each extension that have
         *     dependencies
         * @see #hasExtensionsWithRunningIssues()
         */
        public List<ExtensionRunRequirements> getExtensionRequirements() {
            if (addExtensionsRequirements == null) {
                addExtensionsRequirements = Collections.emptyList();
            }
            return addExtensionsRequirements;
        }

        /**
         * Tells whether or not there's at least one extension with running issues.
         *
         * @return {@code true} if at least one extension has running issues, {@code false}
         *     otherwise.
         * @see #getExtensionRequirements()
         */
        public boolean hasExtensionsWithRunningIssues() {
            for (ExtensionRunRequirements reqs : getExtensionRequirements()) {
                if (!reqs.isRunnable()) {
                    return true;
                }
            }
            return false;
        }

        protected void addExtensionRequirements(ExtensionRunRequirements extension) {
            if (addExtensionsRequirements == null) {
                addExtensionsRequirements = new ArrayList<>(5);
            }
            addExtensionsRequirements.add(extension);
        }
    }

    /**
     * The requirements to run an {@code extension} (with add-on dependencies).
     *
     * <p>It can be used to check if an extension can or not be run, which requirements it has (for
     * example, dependency add-ons) and which issues prevent it from being run, if any.
     *
     * @since 2.4.0
     */
    public static class ExtensionRunRequirements extends BaseRunRequirements {

        private final String classname;

        private ExtensionRunRequirements(AddOn addOn, String classname) {
            super(addOn);
            this.classname = classname;
        }

        /**
         * Gets the classname of the extension.
         *
         * @return the classname of the extension
         */
        public String getClassname() {
            return classname;
        }
    }

    /**
     * The data of the bundle declared in the manifest of an add-on.
     *
     * <p>Used to load a {@link ResourceBundle}.
     *
     * @since 2.8.0
     */
    public static final class BundleData {

        private static final BundleData EMPTY_BUNDLE = new BundleData();

        private final String baseName;
        private final String prefix;

        private BundleData() {
            this("", "");
        }

        private BundleData(String baseName, String prefix) {
            this.baseName = baseName;
            this.prefix = prefix;
        }

        /**
         * Tells whether or not the the bundle data is empty.
         *
         * <p>An empty {@code BundleData} does not contain any information to load a {@link
         * ResourceBundle}.
         *
         * @return {@code true} if empty, {@code false} otherwise.
         */
        public boolean isEmpty() {
            return this == EMPTY_BUNDLE;
        }

        /**
         * Gets the base name of the bundle.
         *
         * @return the base name, or empty if not defined.
         */
        public String getBaseName() {
            return baseName;
        }

        /**
         * Gets the prefix of the bundle, to add into a {@link
         * org.zaproxy.zap.utils.I18N#addMessageBundle(String, ResourceBundle) I18N}.
         *
         * @return the prefix, or empty if not defined.
         */
        public String getPrefix() {
            return prefix;
        }
    }

    /**
     * The data to load a {@link javax.help.HelpSet HelpSet}, declared in the manifest of an add-on.
     *
     * <p>Used to dynamically add/remove the help of the add-on.
     *
     * @since 2.8.0
     */
    public static final class HelpSetData {

        private static final HelpSetData EMPTY_HELP_SET = new HelpSetData();

        private final String baseName;
        private final String localeToken;

        private HelpSetData() {
            this("", "");
        }

        private HelpSetData(String baseName, String localeToken) {
            this.baseName = baseName;
            this.localeToken = localeToken;
        }

        /**
         * Tells whether or not the the HelpSet data is empty.
         *
         * <p>An empty {@code HelpSetData} does not contain any information to load the help.
         *
         * @return {@code true} if empty, {@code false} otherwise.
         */
        public boolean isEmpty() {
            return this == EMPTY_HELP_SET;
        }

        /**
         * Gets the base name of the HelpSet file.
         *
         * @return the base name, or empty if not defined.
         */
        public String getBaseName() {
            return baseName;
        }

        /**
         * Gets the locale token.
         *
         * @return the locale token, or empty if not defined.
         */
        public String getLocaleToken() {
            return localeToken;
        }
    }

    /**
     * An {@link IOException} that indicates that a file is not a valid add-on.
     *
     * @since 2.8.0
     * @see #getValidationResult()
     */
    @SuppressWarnings("serial")
    public static class InvalidAddOnException extends IOException {

        private static final long serialVersionUID = 1L;

        private final ValidationResult validationResult;

        private InvalidAddOnException(ValidationResult validationResult) {
            super(getRootCauseMessage(validationResult), validationResult.getException());
            this.validationResult = validationResult;
        }

        /**
         * Gets the result of validating the add-on.
         *
         * @return the result, never {@code null}.
         */
        public ValidationResult getValidationResult() {
            return validationResult;
        }
    }

    private static String getRootCauseMessage(ValidationResult validationResult) {
        Exception exception = validationResult.getException();
        if (exception == null) {
            return validationResult.getValidity().toString();
        }
        Throwable root = ExceptionUtils.getRootCause(exception);
        return root == null ? exception.getLocalizedMessage() : root.getLocalizedMessage();
    }

    private static int getJavaVersion(String javaVersion) {
        return toVersionInt(toJavaVersionIntArray(javaVersion, 2));
    }

    // NOTE: Following 2 methods copied from org.apache.commons.lang.SystemUtils version 2.6 because
    // of constrained visibility
    private static int[] toJavaVersionIntArray(String version, int limit) {
        if (version == null) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }
        String[] strings = StringUtils.split(version, "._- ");
        int[] ints = new int[Math.min(limit, strings.length)];
        int j = 0;
        for (int i = 0; i < strings.length && j < limit; i++) {
            String s = strings[i];
            if (s.length() > 0) {
                try {
                    ints[j] = Integer.parseInt(s);
                    j++;
                } catch (Exception e) {
                }
            }
        }
        if (ints.length > j) {
            int[] newInts = new int[j];
            System.arraycopy(ints, 0, newInts, 0, j);
            ints = newInts;
        }
        return ints;
    }

    private static int toVersionInt(int[] javaVersions) {
        if (javaVersions == null) {
            return 0;
        }
        int intVersion = 0;
        int len = javaVersions.length;
        if (len >= 1) {
            intVersion = javaVersions[0] * 100;
        }
        if (len >= 2) {
            intVersion += javaVersions[1] * 10;
        }
        if (len >= 3) {
            intVersion += javaVersions[2];
        }
        return intVersion;
    }
}
