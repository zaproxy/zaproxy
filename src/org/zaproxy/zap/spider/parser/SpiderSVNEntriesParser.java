/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.htmlparser.jericho.Source;

import org.parosproxy.paros.network.HttpMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.zaproxy.zap.spider.SpiderParam;

/**
 * The Class SpiderSVNEntriesParser is used for parsing SVN metadata, inclusing SVN "entries" and "wc.db" files.
 * @author 70pointer
 *
 */
public class SpiderSVNEntriesParser extends SpiderParser {
	/* this class was Cloned from SpiderRobotstxtParser, by Cosmin. Credit where credit is due. */
	
	/** a pattern to match for SQLite based file (in ".svn/wc.db") */
	private static final Pattern svnSQLiteFormatPattern = Pattern.compile ("^SQLite format ");
	
	/** a pattern to match for XML based entries files */
	private static final Pattern svnXMLFormatPattern = Pattern.compile("<wc-entries");

	/** matches the entry *after* the line containing the file name */
	private static final Pattern svnTextFormatFileOrDirectoryPattern = Pattern.compile("^(file|dir)$"); //case sensitive
	
	/** matches the lines containing the repo location  */
	private static final Pattern svnRepoLocationPattern = Pattern.compile("^(http://|https://)", Pattern.CASE_INSENSITIVE);
	
		
	/** The Spider parameters. */
	private SpiderParam params;
	
	/** used to parse the XML based .svn/entries file format */ 
	private static DocumentBuilderFactory dbFactory;
	
	/** used to parse the XML based .svn/entries file format */ 
	private static DocumentBuilder dBuilder;
	
	/** statically initialise the XML DocumentBuilderFactory and DocumentBuilder */
	static {
		dbFactory = DocumentBuilderFactory.newInstance();
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			log.error(e);
		}
	}

	/**
	 * Instantiates a new spider SVN entries parser.
	 * 
	 * @param params the params
	 */
	public SpiderSVNEntriesParser(SpiderParam params) {
		super();
		this.params = params;
	}

	@Override
	public void parseResource(HttpMessage message, Source source, int depth) {
		if (message == null || !params.isParseSVNEntries()) {
			return;
		}
		// Get the response content
		String content = message.getResponseBody().toString();

		// Get the context (base url)
		String baseURL = message.getRequestHeader().getURI().toString();
				
		//there are 2 major formats of ".svn/entries" file. 
		//An XML version is used up to (and including) SVN 1.2
		//from SVN 1.3, a more space efficient text based version is used.
		//The ".svn/entries" file format disappeared in SVN 1.6.something, in favour of 
		//a file called ".svn/wc.db" continaing a sqlite database, so we parse this here as well.
		
		//which format are we parsing
		Matcher svnSQLiteFormatMatcher = svnSQLiteFormatPattern.matcher(content);
		Matcher svnXMLFormatMatcher = svnXMLFormatPattern.matcher(content);
		if (svnSQLiteFormatMatcher.find()) {
			//SQLite format is being used, ( >= SVN 1.6)			
			File tempSqliteFile;
			try {
				//get the binary data, and put it in a temp file we can use with the SQLite JDBC driver
				//Note: File is not AutoClosable, so cannot use a "try with resources" to manage it
				tempSqliteFile = File.createTempFile("sqlite", null);
				tempSqliteFile.deleteOnExit();
				OutputStream fos = new FileOutputStream (tempSqliteFile);
				fos.write(message.getResponseBody().getBytes());
				fos.close();
				
				if ( log.isDebugEnabled() ) {
					org.sqlite.JDBC jdbcDriver = new org.sqlite.JDBC();
					log.debug ("Created a temporary SQLite database file '"+ tempSqliteFile+ "'");				
					log.debug("SQLite JDBC Driver is version " + jdbcDriver.getMajorVersion() + "." + jdbcDriver.getMinorVersion());
					}

				//now load the temporary SQLite file using JDBC, and query the file entries within.
				Class.forName("org.sqlite.JDBC"); 
				String sqliteConnectionUrl = "jdbc:sqlite:" + tempSqliteFile.getAbsolutePath();
				
				try (Connection conn = DriverManager.getConnection(sqliteConnectionUrl)) {
					if (conn != null) {
						try (	Statement stmt = conn.createStatement();
								ResultSet rsSVNVersion = stmt.executeQuery("pragma USER_VERSION");
								ResultSet rs = stmt.executeQuery("select kind,repos_path from nodes order by wc_id");
								ResultSet rsRepo = stmt.executeQuery("select root from REPOSITORY order by id");
								) {
							//get the precise internal version of SVN in use   
							//this will inform how the Spider recurse should proceed in an efficient manner.
							int svnFormat = 0;
							while (rsSVNVersion.next()) {
								svnFormat = rsSVNVersion.getInt(1);
								break;
							}
							if ( log.isDebugEnabled() ) {
								log.debug("Internal SVN version for "+ tempSqliteFile + " is "+ svnFormat);
								log.debug("Refer to http://svn.apache.org/repos/asf/subversion/trunk/subversion/libsvn_wc/wc.h for more details!");
							}
							
							//now get the list of files stored in the SVN repo (or this folder of the repo, depending the SVN version) 
							while (rs.next()) {
								String kind = rs.getString(1);
								String repos_path = rs.getString(2);
		
								if ( repos_path != null && repos_path.length() > 0 ) {
									log.debug("Found a file/directory name in the (SQLite based) SVN >= 1.6 wc.db file");
		
									processURL(message, depth, "../" + repos_path + (kind.equals("dir")?"/":""), baseURL);
			
									//re-seed the spider for this directory.
									//depending on the precise SVN version in use, there will either be just one "wc.db" file 
									//in the repository root directory, or there will be a "wc.db" file
									//in every directory associated with the repository, in which case, we need to recurse.
									if ( kind.equals("dir") && svnFormat < 19) {
										processURL(message, depth, "../" + repos_path + "/.svn/wc.db", baseURL);
									}
								}
							}
							
							//get additional information on where the SVN repository is located
							while (rsRepo.next()) {
								String repos_path = rs.getString(1);						
								if ( repos_path != null && repos_path.length() > 0 ) {
									//exclude local repositories here.. we cannot retrieve or spider them
									Matcher repoMatcher = svnRepoLocationPattern.matcher(repos_path);
									if ( repoMatcher.find() ) {
										log.debug("Found an SVN repository location in the (SQLite based) SVN >= 1.6 wc.db file");
										processURL(message, depth, repos_path + "/", baseURL);	
									}
								}
							}							
						}
						catch (Exception e) {
							log.error ("Error executing SQL on temporary SVN SQLite database "+ sqliteConnectionUrl);
						}
					}
				else 
					throw new SQLException ("Could not open a JDBC connection to SQLite file "+ tempSqliteFile.getAbsolutePath());
				} 
				catch (Exception e) {
					//the connection will have been closed already, since we're used a try with resources
					log.error ("Error parsing temporary SVN SQLite database "+ sqliteConnectionUrl);					
				}
				finally {
					//delete the temp file.
					//this will be deleted when the VM is shut down anyway, but better to be safe than to run out of disk space.				
					tempSqliteFile.delete();
				}

			} catch (IOException | ClassNotFoundException e) {
				log.error("An error occurred trying to set up to parse the SQLite based file: "+ e);
				return;
			}
			
		} else if (svnXMLFormatMatcher.find()) {
			//XML format is being used, ( < SVN 1.3)
			Document doc;
			try {
				//work around the "no protocol" issue by wrapping the content in a ByteArrayInputStream
				doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(content.getBytes("utf-8"))));
			} catch (SAXException | IOException e) {
				log.error("An error occurred trying to parse the XML based .svn/entries file: "+ e);
				return;
			}
			NodeList nodelist = doc.getElementsByTagName("entry");
			for ( int i=0; i< nodelist.getLength(); i++) {
				Node svnEntryNode = nodelist.item(i);				
				String svnEntryName = ((Element)svnEntryNode).getAttribute("name");
				String svnEntryKind = ((Element)svnEntryNode).getAttribute("kind");
				String svnEntryUrl = ((Element)svnEntryNode).getAttribute("url");
				
				if ( svnEntryName != null && svnEntryName.length() > 0 ) {
					log.debug("Found a file/directory name in the (XML based) SVN < 1.3 entries file");
				
					processURL(message, depth, "../" + svnEntryName + (svnEntryKind.equals("dir")?"/":""), baseURL);
					
					//re-seed the spider for this directory. 
					if ( svnEntryKind.equals("dir") ) {
						processURL(message, depth, "../" + svnEntryName + "/.svn/entries", baseURL);
					}
				} else if ( svnEntryName != null && svnEntryName.length() == 0 && svnEntryKind.equals("dir") ) {
					//exclude local repositories here.. we cannot retrieve or spider them
					Matcher repoMatcher = svnRepoLocationPattern.matcher(svnEntryUrl);
					if ( repoMatcher.find() ) {
						log.debug("Found an SVN repository location in the (XML based) SVN < 1.3 entries file");
						
						processURL(message, depth, svnEntryUrl + "/", baseURL);
					}
				}
			}
		}
		else	{			
			//text based format us being used, so >= SVN 1.3 (but not later than SVN 1.6.something)
			//Parse each line in the ".svn/entries" file
			//we cannot use the StringTokenizer approach used by the robots.txt logic, 
			//since this causes empty lines to be ignored, which causes problems...
			String previousline = null;	
			String [] lines = content.split("\n"); 
			for (String line : lines ) {
				// If the line is empty, skip it
				if (line.length() > 0) {
					
					//log.debug("Processing SVN entries line: " + line);
					
					Matcher matcher = svnTextFormatFileOrDirectoryPattern.matcher(line);
					if (matcher.find()) {
						//filetype is "dir" or "file", as per the contents of the SVN file.
						String filetype  = matcher.group(0);
						//the previous line actually contains the file/directory name.
						if ( previousline != null && previousline.length() > 0 ) {
							log.debug("Found a file/directory name in the (text based) SVN 1.3/1.4/1.5/1.6 SVN entries file");
						
							processURL(message, depth, "../" + previousline + (filetype.equals("dir")?"/":""), baseURL);
							
							//re-seed the spider for this directory. 
							if ( filetype.equals("dir") ) {
								processURL(message, depth, "../" + previousline + "/.svn/entries", baseURL);
							}
						}
					} else {
						//not a "file" or "dir" line, but it may contain details of the SVN repo location
						Matcher repoMatcher = svnRepoLocationPattern.matcher(line);
						if (repoMatcher.find()) {
							log.debug("Found an SVN repository location in the (text based) SVN 1.3/1.4/1.5/1.6 SVN entries file");
							
							processURL(message, depth, line + "/", baseURL);
						}
						
					} 
				} 
				//last thing to do is to record the line as the previous line for the next iteration.
				previousline = line;
			}
		}
		return;
	}
}
