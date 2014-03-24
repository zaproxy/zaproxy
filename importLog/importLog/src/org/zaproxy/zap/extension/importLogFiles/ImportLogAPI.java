package org.zaproxy.zap.extension.importLogFiles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiView;

///This class extends the ImportLog functionality to the ZAP REST API 
public class ImportLogAPI extends ApiImplementor
{
	private static Logger log = Logger.getLogger(ImportLogAPI.class);

	//API method names
	private static final String PREFIX = "import";
	private static final String Import_Zap_Log_From_File = "ImportZAPLogFromFile";
	private static final String Import_ModSec_Log_From_File = "ImportModSecurityLogFromFile";
	private static final String Import_Zap_HttpRequestResponsePair = "ImportZAPHttpRequestResponsePair";
	private static final String Import_ModSec_AuditEvent = "ImportModSecurityAuditEvent";

	//API method parameters
	private static final String PARAM_FILE = "FilePath";
	private static final String PARAM_REQUEST = "HTTPRequest";
	private static final String PARAM_RESPONSE = "HTTPResponse";
	private static final String PARAM_AuditEventString = "AuditEventString";

	//Serverside directory locations
	private static String SERVERSIDE_FILEREPOSITORY = System.getenv("APPDATA") + "\\OWASP\\Zed Attack Proxy\\imported";
	private static String ADDEDFILESDICTIONARY = SERVERSIDE_FILEREPOSITORY + "\\AddedFiles";
	private static boolean ZapDirChecked = false;
	private static boolean ModSecDirChecked = false;
	private static boolean DirAddedFilesChecked = false;

	//Get the existing logging repository for REST retrieval if it exists, if not create it.
	private static String getLoggingStorageDirectory(String logType)
	{
		if(logType == ExtensionImportLogFiles.logType[0])
		{
			if(!ZapDirChecked)
			{
				File directory = new File(SERVERSIDE_FILEREPOSITORY + "\\ZAPLogs");
				if(!directory.isDirectory())
				{
					directory.mkdirs();
					ZapDirChecked = true;
					return directory.getAbsolutePath();
				}
				else
					return new String(SERVERSIDE_FILEREPOSITORY + "\\ZAPLogs");
			}
			else
				return new String(SERVERSIDE_FILEREPOSITORY + "\\ZAPLogs");
		}
		else
		{
			if(!ModSecDirChecked)
			{
				File directory = new File(SERVERSIDE_FILEREPOSITORY + "\\ModSecLogs");
				if(!directory.isDirectory())
				{
					directory.mkdirs();
					ModSecDirChecked = true;
					return directory.getAbsolutePath();
				}
				else
					return new String(SERVERSIDE_FILEREPOSITORY + "\\ModSecLogs");
			}
			else
				return new String(SERVERSIDE_FILEREPOSITORY + "\\ModSecLogs");
		}

	}

	private static String getAddedFilesDictionary() throws IOException
	{
		while(!DirAddedFilesChecked)
		{
			File hashes = new File(ADDEDFILESDICTIONARY);
			if(!hashes.isFile())
				hashes.createNewFile();
			return hashes.getAbsolutePath();
		}
		return ADDEDFILESDICTIONARY;
	}

	private static void appendAddedFilesHashes(File file) throws IOException
	{
		BufferedWriter wr = null;
		FileInputStream fs = null;
		try
		{
			fs = new FileInputStream(file);
			String md5 = DigestUtils.md5Hex(fs);

			wr = new BufferedWriter(new FileWriter(getAddedFilesDictionary()));
			wr.write(md5);
			wr.newLine();
		}
		finally
		{
			try {
				if (fs != null)
					fs.close();
				if (wr != null)
					wr.close();
			} catch (IOException ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}

	private static boolean FileAlreadyExists(File file)
	{
		boolean fileExists = false;
		FileInputStream fs = null;
		BufferedReader br = null;
		try {
			fs = new FileInputStream(file);
			String md5 = DigestUtils.md5Hex(fs);

			//TODO figure out what parts of the file to compare with MD5 as currently its giving different hashes as the metadata is different.
			//Probably have to hash the string[] lines of the file. Also might be worth adding an abstraction on the REST api so that the files are named by the hash.
			String sCurrentLine;
			br = new BufferedReader(new FileReader(getAddedFilesDictionary()));
			while ((sCurrentLine = br.readLine()) != null) {
				if (md5 == sCurrentLine)
					fileExists = true;
			}
		}

		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		finally
		{
			try {
				if (fs != null)
					fs.close();
				if (br != null)
					br.close();
			} catch (IOException ex) {
				log.error(ex.getMessage(), ex);
			}
		}
		return fileExists;
	}


	//Methods to show in the http API view
	public ImportLogAPI (ExtensionImportLogFiles extensionImportLogFiles) {
		this.addApiView(new ApiView(Import_Zap_Log_From_File, new String[] {PARAM_FILE}));
		this.addApiView(new ApiView(Import_ModSec_Log_From_File, new String[]{PARAM_FILE}));
		this.addApiView(new ApiView(Import_Zap_HttpRequestResponsePair, new String[]{PARAM_REQUEST,PARAM_RESPONSE}));
		this.addApiView(new ApiView(Import_ModSec_AuditEvent, new String[]{PARAM_AuditEventString}));
	}

	@Override
	public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
		ExtensionImportLogFiles importer = new ExtensionImportLogFiles();
		if(Import_Zap_Log_From_File.equals(name))
			return ProcessLogsFromFile(params.getString(PARAM_FILE),importer,ExtensionImportLogFiles.logType[0]);
		if(Import_ModSec_Log_From_File.equals(name))
			return ProcessLogsFromFile(params.getString(PARAM_FILE),importer,ExtensionImportLogFiles.logType[1]);
		if(Import_Zap_HttpRequestResponsePair.equals(name))
		{
			try 
			{
				List<HttpMessage> messages = importer.getHttpMessageFromPair(params.getString(PARAM_REQUEST), params.getString(PARAM_RESPONSE));
				return ProcessRequestResponsePair(messages,importer);
			} 
			catch (HttpMalformedHeaderException e) 
			{
				String errMessage = "Failed - " + e.getMessage();
				return new ApiResponseElement("Parsing logs files to ZAPs site tree", errMessage);
			}
		}
		if(Import_ModSec_AuditEvent.equals(name))
		{
			try (InputStream stream = IOUtils.toInputStream(params.getString(PARAM_AuditEventString)))
			{
				List<HttpMessage> messages = importer.ReadModSecAuditEvent(stream);
				return ProcessRequestResponsePair(messages,importer);
			}
			catch (Exception ex)
			{
				String errMessage = "Failed - " + ex.getMessage();
				return new ApiResponseElement("Parsing logs files to ZAPs site tree", errMessage);
			}
			//TODO add method for accessing gethistory and addtoTree methods
		}
		return new ApiResponseElement("Requested Method","Failed - Method Not Found");
	}

	private static ApiResponseElement ProcessLogsFromFile(String filePath, ExtensionImportLogFiles importer, String logType)
	{
		//Not appending the file with client state info as REST should produce a resource based on the request indefinitely.
		String sourceFilePath = filePath;
		String targetfileName = sourceFilePath.substring(sourceFilePath.lastIndexOf("\\")+1, sourceFilePath.lastIndexOf(".")) + ".txt";
		String absoluteTargetFilePath = getLoggingStorageDirectory(logType) + "\\" + targetfileName;
		File targetFile = new File(absoluteTargetFilePath);

		if(!targetFile.isFile() /*&& !FileAlreadyExists(new File(sourceFilePath))*/)
		{
			try
			{
				targetFile.createNewFile();
				//TODO investigate how to check for uniqueness of the file. Potentially hashing (md5) the string[] of the read file. Might be overkill?
				//appendAddedFilesHashes(targetFile);
			}
			catch (Exception ex)
			{
				return new ApiResponseElement("Parsing logs files to ZAPs site tree","Failed - Could not create file on server");
			}
		}
		else
		{
			return new ApiResponseElement("Parsing logs files to ZAPs site tree","Not processed - File already added");
		}

		BufferedWriter wr = null;
		BufferedReader br = null;
		try {
			String sCurrentLine;

			br = new BufferedReader(new FileReader(sourceFilePath));
			wr = new BufferedWriter(new FileWriter(targetFile));
			while ((sCurrentLine = br.readLine()) != null) {
				wr.write(sCurrentLine);
				wr.newLine();
			}

		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			try {
				if (wr != null)
					wr.close();
				if (br != null)
					br.close();
			} catch (IOException ex) {
				log.error(ex.getMessage(), ex);
			}
		}

		importer.ProcessInput(targetFile, logType);

		return new ApiResponseElement("Parsing log files to ZAPs site tree","Suceeded");
	}

	private static ApiResponseElement ProcessRequestResponsePair(List<HttpMessage> messages, ExtensionImportLogFiles importer)
	{
		try
		{
			importer.AddToTree(importer.getHistoryRefs(messages));
			return new ApiResponseElement("Parsing log files to ZAPs site tree","Suceeded");
		}
		catch (HttpMalformedHeaderException httpex)
		{
			String exceptionMessage = String.format("Parsing log files to ZAPs site tree","Failed - %s", httpex.getLocalizedMessage());
			return new ApiResponseElement(exceptionMessage);
		}
		catch (Exception e)
		{
			String exceptionMessage = String.format("Parsing log files to ZAPs site tree","Failed - %s", e.getLocalizedMessage());
			return new ApiResponseElement(exceptionMessage);
		}

	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

}
