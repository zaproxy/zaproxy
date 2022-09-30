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
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2012/05/02 Added the method createSingleton and changed the method
// getSingleton to use it.
// ZAP: 2012/06/11 Changed the method copySessionDb to call the method
// Database.close(boolean, boolean).
// ZAP: 2012/08/08 Check if file exist.
// ZAP: 2012/10/02 Issue 385: Added support for Contexts
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/04/16 Issue 638: Persist and snapshot sessions instead of saving them
// ZAP: 2013/08/27 Issue 772: Restructuring of Saving/Loading Context Data
// ZAP: 2013/11/16 Issue 881: Fail immediately if zapdb.script file is not found
// ZAP: 2013/12/03 Issue 933: Automatically determine install dir
// ZAP: 2014/01/17 Issue 987: Allow arbitrary config file values to be set via the command line
// ZAP: 2014/07/15 Issue 1265: Context import and export
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative
// implementations
// ZAP: 2015/04/02 Issue 321: Support multiple databases
// ZAP: 2016/02/10 Issue 1958: Allow to disable database (HSQLDB) log
// ZAP: 2016/03/22 Allow to remove ContextDataFactory
// ZAP: 2016/03/23 Issue 2331: Custom Context Panels not show in existing contexts after
// installation of add-on
// ZAP: 2016/06/10 Do not clean up the database if the current session does not require it
// ZAP: 2016/07/05 Issue 2218: Persisted Sessions don't save unconfigured Default Context
// ZAP: 2017/06/07 Allow to persist the session properties (e.g. name, description).
// ZAP: 2018/03/27 Validate that context and configurations for ContextDataFactory are not null.
// ZAP: 2018/07/19 Fallback to bundled zapdb.script file.
// ZAP: 2018/08/15 Deprecated addSessionListener
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/09/15 Added the VariantFactory
// ZAP: 2020/10/14 Allow to set a singleton Model for tests.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.paros.ParosDatabase;
import org.xml.sax.SAXException;
import org.zaproxy.zap.control.ControlOverrides;
import org.zaproxy.zap.db.sql.DbSQL;
import org.zaproxy.zap.extension.ascan.VariantFactory;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ContextDataFactory;

public class Model {

    private static Model model = null;

    private static final String DBNAME_TEMPLATE = Constant.DBNAME_TEMPLATE;
    // private static final String DBNAME_UNTITLED = Constant.DBNAME_UNTITLED;
    private String DBNAME_UNTITLED = Constant.getInstance().DBNAME_UNTITLED;
    private static int DBNAME_COPY = 1;

    private Session session = null;
    private OptionsParam optionsParam = null;
    private Database db = null;
    private String currentDBNameUntitled = "";
    // ZAP: Added logger
    private Logger logger = LogManager.getLogger(Model.class);
    private List<ContextDataFactory> contextDataFactories = new ArrayList<>();
    private VariantFactory variantFactory = new VariantFactory();

    private boolean postInitialisation;

    public Model() {
        // make sure the variable here will not refer back to model itself.
        // DO it in init or respective getter.

        session = new Session(this);
        optionsParam = new OptionsParam();
    }

    /** @return Returns the optionsParam. */
    public OptionsParam getOptionsParam() {
        if (optionsParam == null) {
            optionsParam = new OptionsParam();
        }
        return optionsParam;
    }

    /** @param param The optionsParam to set. */
    public void setOptionsParam(OptionsParam param) {
        optionsParam = param;
    }

    /** @return Returns the session. */
    public Session getSession() {
        if (session == null) {
            session = new Session(this);
        }
        return session;
    }

    /**
     * This method should typically only be called from the Control class
     *
     * @return Returns the session.
     */
    public Session newSession() {
        session = new Session(this);
        // Always start with one context
        session.saveContext(
                session.getNewContext(Constant.messages.getString("context.default.name")));
        return session;
    }

    /** This method should typically only be called from the Control class */
    public void openSession(String fileName)
            throws SQLException, SAXException, IOException, Exception {
        getSession().open(fileName);
    }

    public void openSession(String fileName, final SessionListener callback) {
        getSession().open(fileName, callback);
    }

    /** This method should typically only be called from the Control class */
    public void openSession(final File file, final SessionListener callback) {
        getSession().open(file, callback);
    }

    /** This method should typically only be called from the Control class */
    public void saveSession(final String fileName, final SessionListener callback) {
        getSession().save(fileName, callback);
    }

    /** This method should typically only be called from the Control class */
    public void saveSession(String fileName) throws Exception {
        getSession().save(fileName);
    }

    /**
     * Persists the properties (e.g. name, description) of the current session.
     *
     * <p>Should be called only by "core" classes.
     *
     * @throws Exception if an error occurred while persisting the properties.
     * @since 2.7.0
     */
    public void persistSessionProperties() throws Exception {
        getSession().persistProperties();
    }

    /** This method should typically only be called from the Control class */
    public void snapshotSession(final String fileName, final SessionListener callback) {
        getSession().snapshot(fileName, callback);
    }

    /** This method should typically only be called from the Control class */
    public void discardSession() {
        getSession().discard();
    }

    /** This method should typically only be called from the Control class */
    public void closeSession() {
        getSession().close();
    }

    public void init(ControlOverrides overrides) throws SAXException, IOException, Exception {
        getOptionsParam().load(Constant.getInstance().FILE_CONFIG, overrides);

        if (overrides.isExperimentalDb()) {
            logger.info("Using experimental database :/");
            db = DbSQL.getSingleton().initDatabase();
        } else {
            ParosDatabase parosDb = new ParosDatabase();
            parosDb.setDatabaseParam(getOptionsParam().getDatabaseParam());
            db = parosDb;
        }

        createAndOpenUntitledDb();

        HistoryReference.setTableHistory(getDb().getTableHistory());
        HistoryReference.setTableTag(getDb().getTableTag());
        HistoryReference.setTableAlert(getDb().getTableAlert());
    }

    public static Model getSingleton() {
        if (model == null) {
            // ZAP: Changed to use the method createSingleton().
            createSingleton();
        }
        return model;
    }

    // ZAP: Added method.
    private static synchronized void createSingleton() {
        if (model == null) {
            model = new Model();
        }
    }

    /**
     * Sets the given {@code Model} as the singleton.
     *
     * <p><strong>Note:</strong> Not part of the public API.
     *
     * @param testModel the {@code Model} to test with.
     */
    public static void setSingletonForTesting(Model testModel) {
        model = testModel;
        model.contextDataFactories = new ArrayList<>();
    }

    /** @return Returns the db. */
    public Database getDb() {
        return db;
    }

    // TODO disable for non file based sessions
    public void moveSessionDb(String destFile) throws Exception {

        // always use copySession because moving file does not work in Debian,
        // and for Windows renaming file across different drives does not work.

        copySessionDb(currentDBNameUntitled, destFile);

        // getDb().close();
        //
        // boolean result = false;
        // File fileIn1 = new File(currentDBNameUntitled + ".data");
        // File fileIn2 = new File(currentDBNameUntitled + ".script");
        // File fileIn3 = new File(currentDBNameUntitled + ".properties");
        // File fileIn4 = new File(currentDBNameUntitled + ".backup");
        //
        // File fileOut1 = new File(destFile + ".data");
        // File fileOut2 = new File(destFile + ".script");
        // File fileOut3 = new File(destFile + ".properties");
        // File fileOut4 = new File(destFile + ".backup");
        //
        // if (fileOut1.exists()) fileOut1.delete();
        // if (fileOut2.exists()) fileOut2.delete();
        // if (fileOut3.exists()) fileOut3.delete();
        // if (fileOut4.exists()) fileOut4.delete();
        //
        // result = fileIn1.renameTo(fileOut1);
        // result = fileIn2.renameTo(fileOut2);
        // result = fileIn3.renameTo(fileOut3);
        // if (fileIn4.exists()) {
        // result = fileIn4.renameTo(fileOut4);
        // }
        //
        // getDb().open(destFile);

    }

    // TODO disable for non file based sessions
    protected void copySessionDb(String currentFile, String destFile) throws Exception {

        // ZAP: Changed to call the method close(boolean, boolean).
        getDb().close(false, false);

        // copy session related files to the path specified
        FileCopier copier = new FileCopier();

        // ZAP: Check if files exist.
        File fileIn1 = new File(currentFile + ".data");
        if (fileIn1.exists()) {
            File fileOut1 = new File(destFile + ".data");
            copier.copy(fileIn1, fileOut1);
        }

        File fileIn2 = new File(currentFile + ".script");
        if (fileIn2.exists()) {
            File fileOut2 = new File(destFile + ".script");
            copier.copy(fileIn2, fileOut2);
        }

        File fileIn3 = new File(currentFile + ".properties");
        if (fileIn3.exists()) {
            File fileOut3 = new File(destFile + ".properties");
            copier.copy(fileIn3, fileOut3);
        }

        File fileIn4 = new File(currentFile + ".backup");
        if (fileIn4.exists()) {
            File fileOut4 = new File(destFile + ".backup");
            copier.copy(fileIn4, fileOut4);
        }

        // ZAP: Handle the "lobs" file.
        File lobsFile = new File(currentFile + ".lobs");
        if (lobsFile.exists()) {
            File newLobsFile = new File(destFile + ".lobs");
            copier.copy(lobsFile, newLobsFile);
        }

        getDb().open(destFile);
    }

    // TODO disable for non file based sessions
    protected void snapshotSessionDb(String currentFile, String destFile) throws Exception {
        logger.debug("snapshotSessionDb {} -> {}", currentFile, destFile);

        // ZAP: Changed to call the method close(boolean, boolean).
        getDb().close(false, false);

        // copy session related files to the path specified
        FileCopier copier = new FileCopier();

        // ZAP: Check if files exist.
        File fileIn1 = new File(currentFile + ".data");
        if (fileIn1.exists()) {
            File fileOut1 = new File(destFile + ".data");
            copier.copy(fileIn1, fileOut1);
        }

        File fileIn2 = new File(currentFile + ".script");
        if (fileIn2.exists()) {
            File fileOut2 = new File(destFile + ".script");
            copier.copy(fileIn2, fileOut2);
        }

        File fileIn3 = new File(currentFile + ".properties");
        if (fileIn3.exists()) {
            File fileOut3 = new File(destFile + ".properties");
            copier.copy(fileIn3, fileOut3);
        }

        File fileIn4 = new File(currentFile + ".backup");
        if (fileIn4.exists()) {
            File fileOut4 = new File(destFile + ".backup");
            copier.copy(fileIn4, fileOut4);
        }

        // ZAP: Handle the "lobs" file.
        File lobsFile = new File(currentFile + ".lobs");
        if (lobsFile.exists()) {
            File newLobsFile = new File(destFile + ".lobs");
            copier.copy(lobsFile, newLobsFile);
        }

        if (currentFile.length() == 0) {
            logger.debug("snapshotSessionDb using {} -> {}", currentDBNameUntitled, destFile);
            currentFile = currentDBNameUntitled;
        }

        getDb().open(currentFile);
    }

    /** This method should typically only be called from the Control class */
    // TODO disable for non file based sessions
    public void createAndOpenUntitledDb() throws ClassNotFoundException, Exception {

        getDb().close(false, session.isCleanUpRequired());

        // delete all untitled session db in "session" directory
        File dir = new File(getSession().getSessionFolder());
        File[] listFile =
                dir.listFiles(
                        new FilenameFilter() {
                            @Override
                            public boolean accept(File dir1, String fileName) {
                                if (fileName.startsWith("untitled")) {
                                    return true;
                                }
                                return false;
                            }
                        });
        for (int i = 0; i < listFile.length; i++) {
            if (!listFile[i].delete()) {
                // ZAP: Log failure to delete file
                logger.error("Failed to delete file {}", listFile[i].getAbsolutePath());
            }
        }

        // ZAP: Check if files exist.
        // copy and create new template db
        currentDBNameUntitled = DBNAME_UNTITLED + DBNAME_COPY;
        FileCopier copier = new FileCopier();
        File fileIn = new File(Constant.getZapInstall(), DBNAME_TEMPLATE + ".data");
        if (fileIn.exists()) {
            File fileOut = new File(currentDBNameUntitled + ".data");
            if (fileOut.exists() && !fileOut.delete()) {
                // ZAP: Log failure to delete file
                logger.error("Failed to delete file {}", fileOut.getAbsolutePath());
            }

            copier.copy(fileIn, fileOut);
        }

        fileIn = new File(Constant.getZapInstall(), DBNAME_TEMPLATE + ".properties");
        if (fileIn.exists()) {
            File fileOut = new File(currentDBNameUntitled + ".properties");
            if (fileOut.exists() && !fileOut.delete()) {
                // ZAP: Log failure to delete file
                logger.error("Failed to delete file {}", fileOut.getAbsolutePath());
            }

            copier.copy(fileIn, fileOut);
        }

        fileIn = new File(Constant.getZapInstall(), DBNAME_TEMPLATE + ".script");
        if (fileIn.exists()) {
            File fileOut = new File(currentDBNameUntitled + ".script");
            if (fileOut.exists() && !fileOut.delete()) {
                // ZAP: Log failure to delete file
                logger.error("Failed to delete file {}", fileOut.getAbsolutePath());
            }

            copier.copy(fileIn, fileOut);
        } else {
            String fallbackResource = "/org/zaproxy/zap/resources/zapdb.script";
            try (InputStream is = Model.class.getResourceAsStream(fallbackResource)) {
                if (is == null) {
                    throw new FileNotFoundException(
                            "Bundled resource not found: " + fallbackResource);
                }
                Files.copy(is, Paths.get(currentDBNameUntitled + ".script"));
            }
        }

        fileIn = new File(currentDBNameUntitled + ".backup");
        if (fileIn.exists()) {
            if (!fileIn.delete()) {
                // ZAP: Log failure to delete file
                logger.error("Failed to delete file {}", fileIn.getAbsolutePath());
            }
        }

        // ZAP: Handle the "lobs" file.
        fileIn = new File(currentDBNameUntitled + ".lobs");
        if (fileIn.exists()) {
            if (!fileIn.delete()) {
                logger.error("Failed to delete file {}", fileIn.getAbsolutePath());
            }
        }

        getDb().open(currentDBNameUntitled);
        DBNAME_COPY++;
    }

    @Deprecated
    public void addSessionListener(SessionListener listener) {}

    /**
     * Adds the given context data factory to the model.
     *
     * @param contextDataFactory the context data factory that will be added.
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     * @see #removeContextDataFactory(ContextDataFactory)
     */
    public void addContextDataFactory(ContextDataFactory contextDataFactory) {
        if (contextDataFactory == null) {
            throw new IllegalArgumentException("Parameter contextDataFactory must not be null.");
        }
        this.contextDataFactories.add(contextDataFactory);

        if (postInitialisation) {
            for (Context context : getSession().getContexts()) {
                contextDataFactory.loadContextData(getSession(), context);
            }
        }
    }

    /**
     * Removes the given context data factory from the model.
     *
     * @param contextDataFactory the context data factory that will be removed.
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     * @since 2.5.0
     * @see #addContextDataFactory(ContextDataFactory)
     */
    public void removeContextDataFactory(ContextDataFactory contextDataFactory) {
        if (contextDataFactory == null) {
            throw new IllegalArgumentException("Parameter contextDataFactory must not be null.");
        }
        contextDataFactories.remove(contextDataFactory);
    }

    /**
     * Loads the given context, by calling all the {@link ContextDataFactory}ies.
     *
     * @param ctx the context to load.
     * @throws IllegalArgumentException (since 2.8.0) if the given context is {@code null}.
     * @see ContextDataFactory#loadContextData(Session, Context)
     * @since 2.0.0
     */
    public void loadContext(Context ctx) {
        validateContextNotNull(ctx);

        for (ContextDataFactory cdf : this.contextDataFactories) {
            cdf.loadContextData(getSession(), ctx);
        }
    }

    /**
     * Validates that the given context is not {@code null}, throwing an {@code
     * IllegalArgumentException} if it is.
     *
     * @param context the context to be validated.
     * @throws IllegalArgumentException if the context is {@code null}.
     */
    private static void validateContextNotNull(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("The context must not be null.");
        }
    }

    /**
     * Saves the given context, by calling all the {@link ContextDataFactory}ies.
     *
     * @param ctx the context to save.
     * @throws IllegalArgumentException (since 2.8.0) if the given context is {@code null}.
     * @since 2.0.0
     * @see ContextDataFactory#persistContextData(Session, Context)
     */
    public void saveContext(Context ctx) {
        validateContextNotNull(ctx);

        for (ContextDataFactory cdf : this.contextDataFactories) {
            cdf.persistContextData(getSession(), ctx);
        }
    }

    /**
     * Import a context from the given configuration
     *
     * @param ctx the context to import the context data to.
     * @param config the {@code Configuration} containing the context data.
     * @throws ConfigurationException if an error occurred while reading the context data from the
     *     {@code Configuration}.
     * @throws IllegalArgumentException (since 2.8.0) if the given context or configuration is
     *     {@code null}.
     * @since 2.4.0
     */
    public void importContext(Context ctx, Configuration config) throws ConfigurationException {
        validateContextNotNull(ctx);
        validateConfigNotNull(config);

        for (ContextDataFactory cdf : this.contextDataFactories) {
            cdf.importContextData(ctx, config);
        }
    }

    /**
     * Validates that the given configuration is not {@code null}, throwing an {@code
     * IllegalArgumentException} if it is.
     *
     * @param config the config to be validated.
     * @throws IllegalArgumentException if the config is {@code null}.
     */
    private static void validateConfigNotNull(Configuration config) {
        if (config == null) {
            throw new IllegalArgumentException("The configuration must not be null.");
        }
    }

    /**
     * Export a context into the given configuration
     *
     * @param ctx the context to export.
     * @param config the {@code Configuration} where to export the context data.
     * @throws IllegalArgumentException (since 2.8.0) if the given context is {@code null}.
     * @since 2.4.0
     */
    public void exportContext(Context ctx, Configuration config) {
        validateContextNotNull(ctx);
        validateConfigNotNull(config);

        for (ContextDataFactory cdf : this.contextDataFactories) {
            cdf.exportContextData(ctx, config);
        }
    }

    /**
     * Notifies the model that the initialisation has been done.
     *
     * <p><strong>Note:</strong> Should be called only by "core" code after the initialisation.
     *
     * @since 2.5.0
     */
    public void postInit() {
        postInitialisation = true;
    }

    /**
     * Returns the VariantFactory
     *
     * @return the VariantFactory
     * @since 2.10.0
     */
    public VariantFactory getVariantFactory() {
        return this.variantFactory;
    }
}
