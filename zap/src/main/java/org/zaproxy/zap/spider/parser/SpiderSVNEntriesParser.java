/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.spider.parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import net.htmlparser.jericho.Source;
import org.apache.logging.log4j.LogManager;
import org.parosproxy.paros.network.HttpMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.zaproxy.zap.utils.XmlUtils;

/**
 * The Class SpiderSVNEntriesParser is used for parsing SVN metadata, including SVN "entries" and
 * "wc.db" files.
 *
 * @author 70pointer
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class SpiderSVNEntriesParser extends SpiderParser {
    /* this class was Cloned from SpiderRobotstxtParser, by Cosmin. Credit where credit is due. */

    /** a pattern to match for SQLite based file (in ".svn/wc.db") */
    private static final Pattern svnSQLiteFormatPattern = Pattern.compile("^SQLite format ");

    /** a pattern to match for XML based entries files */
    private static final Pattern svnXMLFormatPattern = Pattern.compile("<wc-entries");

    /** matches the entry *after* the line containing the file name */
    private static final Pattern svnTextFormatFileOrDirectoryPattern =
            Pattern.compile("^(file|dir)$"); // case sensitive

    /** matches the lines containing the repo location */
    private static final Pattern svnRepoLocationPattern =
            Pattern.compile("^(http://|https://)", Pattern.CASE_INSENSITIVE);

    /** used to parse the XML based .svn/entries file format */
    private static DocumentBuilder dBuilder;

    private Pattern SVN_ENTRIES_FILE_PATTERN = Pattern.compile("/\\.svn/entries$|/\\.svn/wc.db$");

    /** statically initialise the XML DocumentBuilder */
    static {
        try {
            dBuilder = XmlUtils.newXxeDisabledDocumentBuilderFactory().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LogManager.getLogger(SpiderSVNEntriesParser.class).error(e);
        }
    }

    /**
     * Instantiates a new spider SVN entries parser.
     *
     * @param params the params
     */
    public SpiderSVNEntriesParser(org.zaproxy.zap.spider.SpiderParam params) {
        super(params);
    }

    @Override
    public boolean parseResource(HttpMessage message, Source source, int depth) {
        if (message == null || !getSpiderParam().isParseSVNEntries()) {
            return false;
        }
        getLogger().debug("Parsing an SVN resource...");

        // Get the response content
        String content = message.getResponseBody().toString();

        // Get the context (base url)
        String baseURL = message.getRequestHeader().getURI().toString();

        // there are 2 major formats of ".svn/entries" file.
        // An XML version is used up to (and including) SVN working copy format 6
        // from SVN working copy format 7, a more space efficient text based version is used.
        // The ".svn/entries" file format disappeared in SVN working copy format 12, in favour of
        // a file called ".svn/wc.db" containing a sqlite database, so we parse this here as well.

        // which format are we parsing
        Matcher svnSQLiteFormatMatcher = svnSQLiteFormatPattern.matcher(content);
        Matcher svnXMLFormatMatcher = svnXMLFormatPattern.matcher(content);
        if (svnSQLiteFormatMatcher.find()) {
            // SQLite format is being used, ( >= SVN working copy format 12, or >= SVN 1.7)
            File tempSqliteFile;
            try {
                // get the binary data, and put it in a temp file we can use with the SQLite JDBC
                // driver
                // Note: File is not AutoCloseable, so cannot use a "try with resources" to manage
                // it
                tempSqliteFile = File.createTempFile("sqlite", null);
                tempSqliteFile.deleteOnExit();
                OutputStream fos = new FileOutputStream(tempSqliteFile);
                fos.write(message.getResponseBody().getBytes());
                fos.close();

                // now load the temporary SQLite file using JDBC, and query the file entries within.
                Class.forName("org.sqlite.JDBC");
                String sqliteConnectionUrl = "jdbc:sqlite:" + tempSqliteFile.getAbsolutePath();

                try (Connection conn = DriverManager.getConnection(sqliteConnectionUrl)) {
                    if (conn != null) {
                        Statement stmt = null;
                        ResultSet rsSVNWCFormat = null;
                        ResultSet rsNodes = null;
                        ResultSet rsRepo = null;
                        try {
                            stmt = conn.createStatement();
                            rsSVNWCFormat = stmt.executeQuery("pragma USER_VERSION");

                            // get the precise internal version of SVN in use
                            // this will inform how the Spider recurse should proceed in an
                            // efficient manner.
                            int svnFormat = 0;
                            while (rsSVNWCFormat.next()) {
                                getLogger().debug("Got a row from 'pragma USER_VERSION'");
                                svnFormat = rsSVNWCFormat.getInt(1);
                                break;
                            }
                            if (svnFormat < 29) {
                                throw new Exception(
                                        "The SVN Working Copy Format of the SQLite database should be >= 29. We found "
                                                + svnFormat);
                            }
                            if (svnFormat > 31) {
                                throw new Exception(
                                        "SVN Working Copy Format "
                                                + svnFormat
                                                + " is not supported at this time.  We support up to and including format 31 (~ SVN 1.8.5)");
                            }
                            getLogger()
                                    .debug(
                                            "Internal SVN Working Copy Format for {} is {}",
                                            tempSqliteFile,
                                            svnFormat);
                            getLogger()
                                    .debug(
                                            "Refer to http://svn.apache.org/repos/asf/subversion/trunk/subversion/libsvn_wc/wc.h for more details!");

                            // allow future changes to be easily handled
                            switch (svnFormat) {
                                case 29:
                                case 30:
                                case 31:
                                    rsNodes =
                                            stmt.executeQuery(
                                                    "select kind,local_relpath,'pristine/'||substr(checksum,7,2) || \"/\" || substr(checksum,7)|| \".svn-base\" from nodes order by wc_id");
                                    break;
                            }

                            if (rsNodes == null) {
                                getLogger()
                                        .error(
                                                "There was a problem parsing the resource. rsNodes should not be null.");
                                return false;
                            }
                            // now get the list of files stored in the SVN repo (or this folder of
                            // the repo, depending the SVN working copy format in use)
                            while (rsNodes.next()) {
                                getLogger()
                                        .debug(
                                                "Got a Node from the SVN wc.db file (format {})",
                                                svnFormat);
                                String kind = rsNodes.getString(1);
                                String filename = rsNodes.getString(2);
                                String svn_filename = rsNodes.getString(3);

                                if (filename != null && filename.length() > 0) {
                                    getLogger()
                                            .debug(
                                                    "Found a file/directory name in the (SQLite based) SVN wc.db file");

                                    processURL(
                                            message,
                                            depth,
                                            "../" + filename + (kind.equals("dir") ? "/" : ""),
                                            baseURL);

                                    // re-seed the spider for this directory.
                                    // this is not to do with the SVN version, but in case the SVN
                                    // root is not the WEB root..
                                    // in order to be sure we catch all the SVN repos, we recurse.
                                    if (kind.equals("dir")) {
                                        processURL(
                                                message,
                                                depth,
                                                "../" + filename + "/.svn/wc.db",
                                                baseURL);
                                    }
                                    // if we have an internal SVN filename for the file, process it.
                                    // this will probably result in source code disclosure at some
                                    // point.
                                    if (kind.equals("file")
                                            && svn_filename != null
                                            && svn_filename.length() > 0) {
                                        processURL(message, depth, svn_filename, baseURL);
                                    }
                                }
                            }

                            rsRepo = stmt.executeQuery("select root from REPOSITORY order by id");
                            // get additional information on where the SVN repository is located
                            while (rsRepo.next()) {
                                getLogger()
                                        .debug(
                                                "Got a potential Repository from the SVN wc.db file (format {})",
                                                svnFormat);
                                String repos_path = rsRepo.getString(1);
                                if (repos_path != null && repos_path.length() > 0) {
                                    // exclude local repositories here.. we cannot retrieve or
                                    // spider them
                                    Matcher repoMatcher =
                                            svnRepoLocationPattern.matcher(repos_path);
                                    if (repoMatcher.find()) {
                                        getLogger()
                                                .debug(
                                                        "Found an SVN repository location in the (SQLite based) SVN wc.db file");
                                        processURL(message, depth, repos_path + "/", baseURL);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            getLogger()
                                    .error(
                                            "Error executing SQL on temporary SVN SQLite database '{}': ",
                                            sqliteConnectionUrl,
                                            e);
                        } finally {
                            // the JDBC driver in use does not play well with "try with resource"
                            // construct. I tried!
                            if (rsRepo != null) rsRepo.close();
                            if (rsNodes != null) rsNodes.close();
                            if (rsSVNWCFormat != null) rsSVNWCFormat.close();
                            if (stmt != null) stmt.close();
                        }
                    } else
                        throw new SQLException(
                                "Could not open a JDBC connection to SQLite file "
                                        + tempSqliteFile.getAbsolutePath());
                } catch (Exception e) {
                    // the connection will have been closed already, since we're used a try with
                    // resources
                    getLogger()
                            .error(
                                    "Error parsing temporary SVN SQLite database {}",
                                    sqliteConnectionUrl);
                } finally {
                    // delete the temp file.
                    // this will be deleted when the VM is shut down anyway, but better to be safe
                    // than to run out of disk space.
                    tempSqliteFile.delete();
                }

            } catch (IOException | ClassNotFoundException e) {
                getLogger()
                        .error(
                                "An error occurred trying to set up to parse the SQLite based file: ",
                                e);
                // We consider the message fully parsed, so it doesn't get parsed by 'fallback'
                // parsers
                return true;
            }

        } else if (svnXMLFormatMatcher.find()) {
            // XML format is being used, ( < SVN working copy format 7).
            // The XML based file was replaced with the text based format with SVN 1.4, when format
            // 8 went live
            // Not all the working copy formats went live in SVN versions, so tracking the format
            // against the SVN version is tricky.

            Document doc;
            try {
                // work around the "no protocol" issue by wrapping the content in a
                // ByteArrayInputStream
                doc =
                        dBuilder.parse(
                                new InputSource(
                                        new ByteArrayInputStream(content.getBytes("utf-8"))));
            } catch (SAXException | IOException e) {
                getLogger()
                        .error(
                                "An error occurred trying to parse the XML based .svn/entries file: ",
                                e);
                // We consider the message fully parsed, so it doesn't get parsed by 'fallback'
                // parsers
                return true;
            }
            NodeList nodelist = doc.getElementsByTagName("entry");
            for (int i = 0; i < nodelist.getLength(); i++) {
                Node svnEntryNode = nodelist.item(i);
                String svnEntryName = ((Element) svnEntryNode).getAttribute("name");
                String svnEntryKind = ((Element) svnEntryNode).getAttribute("kind");
                String svnEntryUrl = ((Element) svnEntryNode).getAttribute("url");
                String svnEntryCopyFromUrl = ((Element) svnEntryNode).getAttribute("copyfrom-url");

                if (svnEntryName != null && svnEntryName.length() > 0) {
                    getLogger()
                            .debug(
                                    "Found a file/directory name in the (XML based) SVN < 1.4 entries file");
                    processURL(
                            message,
                            depth,
                            "../" + svnEntryName + (svnEntryKind.equals("dir") ? "/" : ""),
                            baseURL);
                    // get the internal SVN file, probably leading to source code disclosure
                    if (svnEntryKind.equals("file")) {
                        processURL(
                                message, depth, "text-base/" + svnEntryName + ".svn-base", baseURL);
                    }
                    // re-seed the spider for this directory.
                    if (svnEntryKind.equals("dir")) {
                        processURL(message, depth, "../" + svnEntryName + "/.svn/entries", baseURL);
                    }
                }

                // expected to be true for the first entry only (the directory housing other
                // entries)
                if (svnEntryName != null
                        && svnEntryName.length() == 0
                        && svnEntryKind.equals("dir")) {
                    // exclude local repositories here.. we cannot retrieve or spider them
                    Matcher repoMatcher = svnRepoLocationPattern.matcher(svnEntryUrl);
                    if (repoMatcher.find()) {
                        getLogger()
                                .debug(
                                        "Found an SVN repository location in the (XML based) SVN < 1.4 entries file");
                        processURL(message, depth, svnEntryUrl + "/", baseURL);
                    }
                }
                // this attribute seems to be set on various entries. Correspond to files, rather
                // than directories
                Matcher urlMatcher = svnRepoLocationPattern.matcher(svnEntryCopyFromUrl);
                if (urlMatcher.find()) {
                    getLogger().debug("Found an SVN URL in the (XML based) SVN < 1.4 entries file");
                    processURL(message, depth, svnEntryCopyFromUrl, baseURL);
                }
            }
        } else {
            // text based format us being used, so >= SVN 1.4, and < SVN 1.7.x
            // Parse each line in the ".svn/entries" file
            // we cannot use the StringTokenizer approach used by the robots.txt logic,
            // since this causes empty lines to be ignored, which causes problems...
            String previousline = null;
            String[] lines = content.split("\n");
            for (String line : lines) {
                // If the line is empty, skip it
                if (line.length() > 0) {

                    // getLogger().debug("Processing SVN entries line: " + line);

                    Matcher matcher = svnTextFormatFileOrDirectoryPattern.matcher(line);
                    if (matcher.find()) {
                        // filetype is "dir" or "file", as per the contents of the SVN file.
                        String filetype = matcher.group(0);
                        // the previous line actually contains the file/directory name.
                        if (previousline != null && previousline.length() > 0) {
                            getLogger()
                                    .debug(
                                            "Found a file/directory name in the (text based) SVN 1.4/1.5/1.6 SVN entries file");

                            processURL(
                                    message,
                                    depth,
                                    "../" + previousline + (filetype.equals("dir") ? "/" : ""),
                                    baseURL);
                            // get the internal SVN file, probably leading to source code disclosure
                            if (filetype.equals("file")) {
                                processURL(
                                        message,
                                        depth,
                                        "text-base/" + previousline + ".svn-base",
                                        baseURL);
                            }

                            // re-seed the spider for this directory.
                            if (filetype.equals("dir")) {
                                processURL(
                                        message,
                                        depth,
                                        "../" + previousline + "/.svn/entries",
                                        baseURL);
                            }
                        }
                    } else {
                        // not a "file" or "dir" line, but it may contain details of the SVN repo
                        // location
                        Matcher repoMatcher = svnRepoLocationPattern.matcher(line);
                        if (repoMatcher.find()) {
                            getLogger()
                                    .debug(
                                            "Found an SVN repository location in the (text based) 1.4/1.5/1.6 SVN entries file");

                            processURL(message, depth, line + "/", baseURL);
                        }
                    }
                }
                // last thing to do is to record the line as the previous line for the next
                // iteration.
                previousline = line;
            }
        }
        // We consider the message fully parsed, so it doesn't get parsed by 'fallback' parsers
        return true;
    }

    @Override
    public boolean canParseResource(HttpMessage message, String path, boolean wasAlreadyParsed) {
        // matches the file name of files that should be parsed with the SVN entries file parser
        Matcher matcher = SVN_ENTRIES_FILE_PATTERN.matcher(path);
        return matcher.find();
    }
}
