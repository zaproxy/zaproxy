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

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Source;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.spider.SpiderParam;

/**
 * The Class SpiderGitParser is used for parsing Git metadata from the .git/index file
 * This parser currently supports Git internal index file versions 2,3, and 4. It does not currently support version 1,
 * since this version is no longer supported.  Version 1 appears to have disappeared with Git version 0.05 in 2005.  
 * @author 70pointer
 *
 */
public class SpiderGitParser extends SpiderParser {
		
	/** The Spider parameters. */
	private SpiderParam params;
	
	/** a pattern to match the file name of the Git index file */
	private static final Pattern gitIndexFilenamePattern = Pattern.compile("/.git/index$");
			
	/** a pattern to match the content of the Git index file */
	private static final Pattern gitIndexContentPattern = Pattern.compile ("^DIRC");

	private Pattern GIT_FILE_PATTERN = Pattern.compile("/\\.git/index$");
	
	/**
	 * Instantiates a new spider Git Index parser.
	 * 
	 * @param params the params
	 */
	public SpiderGitParser(SpiderParam params) {
		super();
		this.params = params;
	}

	@SuppressWarnings("unused")
	@Override
	public boolean parseResource(HttpMessage message, Source source, int depth) {
		
		//parse the Git index file, based on publicly available (but incomplete) documentation of the file format, and some reverse-engineering.
		if (message == null || !params.isParseGit()) {
			return false;
		}
		log.debug("Parsing a Git resource...");
		
		// Get the response content
		byte [] data = message.getResponseBody().getBytes();
		String baseURL = message.getRequestHeader().getURI().toString();
				
		try {
			String fullpath= message.getRequestHeader().getURI().getPath();
			if (fullpath == null) fullpath = "";
			if ( log.isDebugEnabled()) log.debug("The full path is ["+ fullpath + "]");
			
			//make sure the file name is as expected
			Matcher gitIndexFilenameMatcher = gitIndexFilenamePattern.matcher(fullpath);
			if (!gitIndexFilenameMatcher.find()) {
				log.warn("This path cannot be handled by the Git parser: " + fullpath);
				return false;
			}
			
			//dealing with the Git Index file				
			
			Matcher gitIndexContentMatcher = gitIndexContentPattern.matcher(new String(data));
			if (!gitIndexContentMatcher.find()) {
				if (log.isDebugEnabled()) {
					log.debug("The file '"+ fullpath + "' could not be parsed as a Git Index file due to unexpected content");
				}
				return false;
			}
			//it looks like a duck, and quacks like a duck. 
			//although it could still be an animatronic duck
								
			ByteBuffer dataBuffer = ByteBuffer.wrap(data);
			
			byte [] dircArray = new byte [4];
			dataBuffer.get(dircArray, 0, 4);
			
			int indexFileVersion = dataBuffer.getInt();
			if ( log.isDebugEnabled() ) log.debug("The Git index file version is "+ indexFileVersion);
			
			int indexEntryCount = dataBuffer.getInt();
			if ( log.isDebugEnabled() ) log.debug(indexEntryCount + " entries were found in the Git index file ");

			if ( indexFileVersion != 2 && indexFileVersion != 3 && indexFileVersion != 4) {
				throw new Exception ("Only Git Index File versions 2, 3, and 4 are currently supported. Git Index File Version "+ indexFileVersion + " was found.");
			}
			
			//for version 4 (and upwards?), we need to know the previous entry name, so store it
			String previousIndexEntryName = "";
			for (int entryIndex = 0; entryIndex < indexEntryCount; entryIndex ++) {
				int entryBytesRead = 0;
				int indexEntryCtime1 = dataBuffer.getInt(); entryBytesRead+=4;
				if ( log.isDebugEnabled() ) log.debug ("Entry "+ entryIndex + " has indexEntryCtime1 "+ indexEntryCtime1);							
				int indexEntryCtime2 = dataBuffer.getInt();	entryBytesRead+=4;
				int indexEntryMtime1 = dataBuffer.getInt(); entryBytesRead+=4;
				int indexEntryMtime2 = dataBuffer.getInt(); entryBytesRead+=4;
				int indexEntryDev = dataBuffer.getInt(); entryBytesRead+=4;
				int indexEntryInode = dataBuffer.getInt(); entryBytesRead+=4;
				int indexEntryMode = dataBuffer.getInt(); entryBytesRead+=4;
				int indexEntryUid = dataBuffer.getInt(); entryBytesRead+=4;
				int indexEntryGid = dataBuffer.getInt(); entryBytesRead+=4;
				int indexEntrySize = dataBuffer.getInt(); entryBytesRead+=4;	
				if ( log.isDebugEnabled() ) log.debug("Entry "+ entryIndex + " has size "+ indexEntrySize);
				
				//size is unspecified for the entry id, but it seems to be 40 bytes SHA-1 string
				//stored as 20 bytes, network order
				byte [] indexEntryIdBuffer = new byte [20];
				dataBuffer.get(indexEntryIdBuffer, 0, 20);	entryBytesRead+=20;
				String indexEntryId = new String (indexEntryIdBuffer);
				
				short indexEntryFlags = dataBuffer.getShort(); entryBytesRead+=2;						
				if ( log.isDebugEnabled() ) log.debug ("Entry "+ entryIndex + " has flags " + indexEntryFlags);

				//mask off all but the least significant 12 bits of the index entry flags to get the length of the name in bytes 
				int indexEntryNameByteLength = indexEntryFlags & 4095;						
				if ( log.isDebugEnabled() ) log.debug ("Entry "+ entryIndex + " has a name of length " + indexEntryNameByteLength);

				//mask off all but the second most significant 12 bit of the index entry flags to get the extended flag for the entry 
				//int indexEntryExtendedFlag = indexEntryFlags & (int)16384;
				int indexEntryExtendedFlag = ((indexEntryFlags & (1<<14) )>>14);
				if ( log.isDebugEnabled() ) log.debug ("Entry "+ entryIndex + " has an extended flag of " + indexEntryExtendedFlag);

				//check that we parsed out the index entry extended flag correctly.
				//this is more of an assertion than anything. It's already saved my bacon once.
				if (indexEntryExtendedFlag != 0 && indexEntryExtendedFlag != 1 ) {
					throw new Exception ("Error parsing out the extended flag for index entry "+ entryIndex + ". We got "+ indexEntryExtendedFlag);
				}
				if ( indexFileVersion == 2 && indexEntryExtendedFlag != 0) {
					throw new Exception ("Index File Version 2 is supposed to have the extended flag set to 0. For index entry "+ entryIndex + ", it is set to "+ indexEntryExtendedFlag);
				}
				
				//specific to version 3 and above, if the extended flag is set for the entry.
				if (indexFileVersion > 2 && indexEntryExtendedFlag == 1) {
					if ( log.isDebugEnabled() ) log.debug ("For Index file version "+ indexFileVersion +", reading an extra 16 bits for Entry "+ entryIndex );
					short indexEntryExtendedFlags = dataBuffer.getShort(); entryBytesRead+=2;						
					if ( log.isDebugEnabled() ) log.debug ("Entry "+ entryIndex + " has (optional) extended flags " + indexEntryExtendedFlags);
					}
				
				String indexEntryName = null;
				if ( indexFileVersion > 3 ) {
					if ( log.isDebugEnabled() ) log.debug("Inflating the (deflated) entry name for index entry "+ entryIndex + " based on the previous entry name, since Index file version "+ indexFileVersion + " requires this");
												
					//get bytes until we find one with the msb NOT set. count the bytes.
					int n = 0, removeNfromPreviousName = 0;
					byte msbsetmask = (byte)(1<<7); 						// 1000 0000
					byte msbunsetmask = (byte) ((~ msbsetmask) & 0xFF );  	// 0111 1111
					while (++n > 0) {
						byte byteRead = dataBuffer.get(); entryBytesRead++;
						if (n==1)	//zero the msb of the first byte read
							removeNfromPreviousName = (removeNfromPreviousName << 8 ) | (0xFF & ( byteRead & msbunsetmask));
						else 		//set the msb of subsequent bytes read
							removeNfromPreviousName = (removeNfromPreviousName << 8 ) | (0xFF & ( byteRead | msbsetmask));
						if ( ( byteRead & msbsetmask) == 0 ) break;  //break if msb is NOT set in the byte
					}

					if (log.isDebugEnabled()) log.debug("We read "+ n + " bytes of variable length data from before the start of the entry name");
					if ( n > 4 ) 
						throw new Exception ("An entry name is never expected to be > 2^^32 bytes long. Some file corruption may have occurred, or a parsing error has occurred");
													
					//now read the (partial) name for the current entry
					int bytesToReadCurrentNameEntry = indexEntryNameByteLength- (previousIndexEntryName.length() - removeNfromPreviousName);
					byte [] indexEntryNameBuffer = new byte [bytesToReadCurrentNameEntry];
					dataBuffer.get(indexEntryNameBuffer, 0, bytesToReadCurrentNameEntry); entryBytesRead+=bytesToReadCurrentNameEntry;

					//build it up
					indexEntryName = previousIndexEntryName.substring(0, previousIndexEntryName.length() - removeNfromPreviousName) + new String (indexEntryNameBuffer);
				} else {
					//indexFileVersion <= 3 (waaaaay simpler logic, but the index file is larger in this version than for v4+)
					byte [] indexEntryNameBuffer = new byte [indexEntryNameByteLength];
					dataBuffer.get(indexEntryNameBuffer, 0, indexEntryNameByteLength); entryBytesRead+=indexEntryNameByteLength;
					indexEntryName = new String (indexEntryNameBuffer);
				}
				
				if ( log.isDebugEnabled() ) log.debug("Entry "+ entryIndex + " has name "+ indexEntryName);
				
				//and store off the index entry name, for the next iteration
				previousIndexEntryName=indexEntryName;
				//skip past the zero byte terminating the string (whose purpose seems completely pointless to me, but hey)
				byte indexEntryNul = dataBuffer.get(); entryBytesRead++;
				
				//the padding after the pathname does not exist for versions 4 or later. 
				if ( indexFileVersion < 4 ) {
					if ( log.isDebugEnabled() ) log.debug("Aligning to an 8 byte boundary after Entry "+ entryIndex + ", since Index file version "+ indexFileVersion + " mandates 64 bit alignment for index entries");
				
					int entryBytesToRead=((8-(entryBytesRead%8))%8); 
					if ( log.isDebugEnabled() ) {
						log.debug ("The number of bytes read for index entry "+ entryIndex + " thus far is: "+ entryBytesRead);
						log.debug ("So we must read "+ entryBytesToRead + " bytes to stay on a 64 bit boundary");
						}
					//read the 0-7 (NUL) bytes to keep reading index entries on an 8 byte boundary
					byte [] indexEntryPadBuffer = new byte [entryBytesToRead];
					dataBuffer.get(indexEntryPadBuffer, 0, entryBytesToRead); entryBytesRead+=entryBytesToRead;
					} 
				else {
					if ( log.isDebugEnabled() ) log.debug("Not aligning to an 8 byte boundary after Entry "+ entryIndex + ", since Index file version "+ indexFileVersion + " does not mandate 64 bit alignment for index entries");
					}
											
				//Git does not store entries for directories, but just files/symlinks/Git links, so no need to handle directories here, unlike with SVN, for instance.
				if ( indexEntryName != null && indexEntryName.length() > 0 ) {
					log.info("Found file/symbolic link/gitlink "+ indexEntryName + " in the Git entries file");
					processURL(message, depth, "../" + indexEntryName, baseURL);
				}
			}
			//all good, we're outta here.
			// We consider the message fully parsed, so it doesn't get parsed by 'fallback' parsers
			return true;				 
			
		} catch (Exception e) {
			log.warn("An error occurred trying to parse Git url '"+ baseURL + "': "+ e);
			// We consider the message fully parsed, so it doesn't get parsed by 'fallback' parsers
			return true;
		}
	}

	@Override
	public boolean canParseResource(HttpMessage message, String path, boolean wasAlreadyParsed) {
		// matches the file name of files that should be parsed with the GIT file parser 
		Matcher matcher = GIT_FILE_PATTERN.matcher(path);
		return matcher.find();
	}
}
