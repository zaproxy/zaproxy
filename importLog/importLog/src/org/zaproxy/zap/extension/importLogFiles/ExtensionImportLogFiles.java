package org.zaproxy.zap.extension.importLogFiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.jwall.web.audit.AuditEvent;
import org.jwall.web.audit.io.ModSecurity2AuditReader;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.network.HttpRequestBody;
import org.zaproxy.zap.network.HttpResponseBody;

public class ExtensionImportLogFiles extends ExtensionAdaptor
{
	private JMenuItem menuExample = null;
	private ResourceBundle messages = null;

	private static Logger log = Logger.getLogger(ExtensionImportLogFiles.class);

	public ExtensionImportLogFiles() {
		super();
		initialize();
	}

	public ExtensionImportLogFiles(String name) {
		super(name);
	}

	private void initialize() {
		this.setName("ExtensionImportLogFiles");
		// Load extension specific language files - these are held in the extension jar
		messages = ResourceBundle.getBundle(
				this.getClass().getPackage().getName() + ".Messages", Constant.getLocale());
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);
		ImportLogAPI test = new ImportLogAPI(null);
		API.getInstance().registerApiImplementor(test);
		if (getView() != null) {
			// Register our top menu item, as long as we're not running as a daemon
			// Use one of the other methods to add to a different menu list
			extensionHook.getHookMenu().addAnalyseMenuItem(getImportOption());
		}

	}

	/**
	 * Logging options for the import
	 */
	public static final String[] logType = { "ZAP Logs", "ModSecurity2 Logs" };

	private JMenuItem getImportOption() {
		if (menuExample == null) {
			menuExample = new JMenuItem();
			menuExample.setText(getMessageString("ext.ImportLogFiles.analyze.import"));

			menuExample.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {

					//View Stuff
					View view = View.getSingleton();
					JFrame main = view.getMainFrame();

					//File Chooser
					JFileChooser fc = new JFileChooser();
					fc.setAcceptAllFileFilterUsed(false);
					FileFilter filter = new FileNameExtensionFilter("TEXT File", "txt");
					fc.addChoosableFileFilter(filter);

					String logChoice = (String) JOptionPane.showInputDialog(main, "Log Type", "Select Log Type", JOptionPane.QUESTION_MESSAGE, null, logType, logType[0]);

					if(!logChoice.isEmpty())
					{
						int openChoice = fc.showOpenDialog(main);
						if (openChoice == JFileChooser.APPROVE_OPTION)
						{
							File newFile = fc.getSelectedFile();
							ProcessInput(newFile,logChoice);
						}
					}

				}
			});
		}
		return menuExample;
	}

	public List<HttpMessage> ReadModSecAuditEvent(InputStream stream)
	{
		ModSecurity2AuditReader reader = null;
		try 
		{
			reader = new ModSecurity2AuditReader(stream);
			return ReadModSecLogs(reader);
		} 
		catch (Exception e) {

			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * For reading logs that are exported from the ModSecurity application
	 * @param newFile java.io.File object referring to the ModSecurity text log file
	 * @return List of HttpMessages containing Request Header and Body and Response Header and Body
	 * @throws IOException
	 */
	public List<HttpMessage> ReadModSecLogsFromFile(File newFile)
	{
		ModSecurity2AuditReader reader = null;
		try 
		{
			reader = new ModSecurity2AuditReader(newFile);
			return ReadModSecLogs(reader);
		} 
		catch (Exception e) {

			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	private synchronized List<HttpMessage> ReadModSecLogs(ModSecurity2AuditReader reader) throws IOException
	{
		List<HttpMessage> messages = new ArrayList<HttpMessage>();
		

		while(reader.bytesRead() < reader.bytesAvailable())
		{
			try
			{
				AuditEvent a = reader.readNext();
				if(a != null)
					//Mod Security logs don't provide http response bodies to load in.
					messages.add(new HttpMessage(new HttpRequestHeader(a.getRequestHeader()),new HttpRequestBody(a.getRequestBody()),new HttpResponseHeader(a.getResponseHeader()), new HttpResponseBody()));
				else
					break;
			}
			catch(Exception e)
			{
				//View.getSingleton().showWarningDialog("Cannot import this log as it does not match the ModSecurity2 data format");
				log.error(e.getMessage(), e);
			}
		}

		reader.close();

		if(messages.size() == 0)
			return null;
		else
			return messages;

	}
	
	/**
	 * Updates the UI view with the newly added HttpMessages
	 * This method needs to be public as it can be called internally and by the API
	 * @param historyList List of History References returned from adding HttpMessages to the ZAP database
	 */
	public void AddToTree(List<HistoryReference> historyList)
	{
		SiteMap currentTree = Model.getSingleton().getSession().getSiteTree();

		for(HistoryReference historyref: historyList)
		{
			currentTree.addPath(historyref);
		}

		currentTree.reload();
		///Need to refresh history tabs for details and alerts refresh
	}
	
	/**
	 * Switch method called by the entry point for the log imports to choose path to take based on the log type selected by the user 
	 * @param newFile java.IO.File representation of the logfile, called from both the UI and from the API
	 * @param logChoice type of logfile being imported 
	 */
	public void ProcessInput(File newFile, String logChoice)
	{
		//ZAP log choice
		if(logChoice != null && logChoice == logType[0])
		{
			List<String> parsedText = ReadFile(newFile);
			try 
			{	
				List<HttpMessage> messages = getHttpMessages(parsedText);
				List<HistoryReference> history = getHistoryRefs(messages);
				AddToTree(history);
			} 
			catch (HttpMalformedHeaderException e) 
			{
				log.error(e.getMessage(), e);
			}
		}

		//ModSecurity2 Choice
		else if(logChoice != null && logChoice == logType[1])
		{
			try 
			{
				List<HttpMessage> messages = ReadModSecLogsFromFile(newFile);
				List<HistoryReference> history = getHistoryRefs(messages);
				SiteMap currentTree = Model.getSingleton().getSession().getSiteTree();
				for(HistoryReference historyref: history)
				{
					currentTree.addPath(historyref);
				}
				currentTree.reload();
				///Need to refresh history tabs for details.
			} 
			catch (IOException e) 
			{
				log.error(e.getMessage(), e);
			} 
			catch (Exception e) 
			{
				log.error(e.getMessage(), e);
			}
		}
	}

	List<String> ReadFile(File file)
	{
		return ReadFileFromPath(Paths.get(file.getPath()));
	}
	
	List<String> ReadFileFromPath(Path filePath)
	{
		List<String> parsed = new ArrayList<String>();
		Charset charset = Charset.forName("US-ASCII");
		try (BufferedReader reader = Files.newBufferedReader(filePath, charset)) 
		{
			Scanner sc = new Scanner(reader);
			sc.useDelimiter(Pattern.compile("====\\s[0-9]*\\s=========="));
			while(sc.hasNext())
			{
				parsed.add(sc.next());
			}
			sc.close();
			reader.close();
			return parsed;
		} 
		catch (IOException x) 
		{
			log.error(x.getMessage(), x);
		}
		return null;
	}

	/**
	 * Called exclusively by the REST API to get the HttpMessage ZAP object representation of the request response pair.
	 * @param request HttpRequest string
	 * @param response HttpRespones string
	 * @return List of the HttpMessage objects
	 * @throws HttpMalformedHeaderException
	 */
	public List<HttpMessage> getHttpMessageFromPair(String request,String response) throws HttpMalformedHeaderException
	{
		List<String> reqResp = new ArrayList<String>(2);
		reqResp.add(request);
		reqResp.add(response);
		return getHttpMessages(reqResp);
	}
	
	private List<HttpMessage> getHttpMessages(List<String> parsedrequestandresponse) throws HttpMalformedHeaderException
	{
		//http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html
		Pattern requestP = Pattern.compile("^OPTIONS|^GET|^HEAD|^POST|^PUT|^DELETE|^TRACE|^CONNECT");

		//http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html
		//Not sure whether to use the allchars-then-allwhitespacechars or just the DOTALL to get the httprequestbody?
		Pattern responseP = Pattern.compile("(\\S*\\s*)?(HTTP/[0-9].[0-9]\\s[0-9]{3}.*)", Pattern.DOTALL | Pattern.MULTILINE);
		//Pattern responseP = Pattern.compile("(.*)?(HTTP/[0-9].[0-9]\\s[0-9]{3}.*)", Pattern.DOTALL | Pattern.MULTILINE);

		//Add capture group as we want to just match the html, not the rest of the payload
		Pattern responseBodyP = Pattern.compile("\\S*?(<html>.*</html>)",Pattern.DOTALL | Pattern.MULTILINE);

		HttpRequestHeader tempRequestHeader = null;
		HttpRequestBody tempRequestBody = new HttpRequestBody();
		HttpResponseHeader tempResponseHeader = null;
		HttpResponseBody tempResponseBody = new HttpResponseBody();

		//Initialise list at total parsed message count for performance.
		List<HttpMessage> messages = new ArrayList<HttpMessage>(parsedrequestandresponse.size());

		for(String block: parsedrequestandresponse)
		{
			//HTTP request and response header pairs have a 2 line break between them as per RFC 2616 http://tools.ietf.org/html/rfc2616
			String[] httpComponents = block.split("\r\n\r\n");
			for(String component: httpComponents)
			{
				//Remove leading and trailing whitespace
				component = component.trim();

				Matcher requestM = requestP.matcher(component);
				if(requestM.find())
				{
					tempRequestHeader = new HttpRequestHeader(component);
					//tempRequestHeader = component;
				}

				//Strange way of splitting it up but usually if the httpRequestBody is present, i.e. on a Post request there's a token in the body usually
				//So I'm using the group matching in the regex to split that up. We'll need either a blank HttpRequestBody or the actual one further down the line.
				Matcher responseM = responseP.matcher(component);
				if(responseM.find())
				{
					if(!responseM.group(1).trim().isEmpty())
						tempRequestBody = new HttpRequestBody(responseM.group(1).trim());

					tempResponseHeader = new HttpResponseHeader(responseM.group(2).trim());	
					//tempResponseHeader = component;
				}
				Matcher responseBodyM = responseBodyP.matcher(component);
				if(responseBodyM.find())
				{
					tempResponseBody = new HttpResponseBody(responseBodyM.group(1));
				}
			}

			messages.add(new HttpMessage(tempRequestHeader,tempRequestBody,tempResponseHeader,tempResponseBody));
		}
		return messages;
	}

	public List<HistoryReference> getHistoryRefs(List<HttpMessage> messages) throws HttpMalformedHeaderException
	{
		//Initialise list at total parsed message count for performance.
		List<HistoryReference> historyRefs = new ArrayList<HistoryReference>(messages.size());
		Session currentSession = Model.getSingleton().getSession();

		for(HttpMessage message: messages)
		{
			try 
			{
				historyRefs.add(new HistoryReference(currentSession, 1, message));
			} 
			catch (SQLException e) 
			{
				log.error(e.getMessage(), e);
			}
			catch (HttpMalformedHeaderException e)
			{
				log.error(e.getMessage(), e);
			}
			catch (NullPointerException n) 
			{
				log.error(n.getMessage(), n);
			}
		}
		return historyRefs;
	}


	public String getMessageString (String key) {
		return messages.getString(key);
	}
	@Override
	public String getAuthor() {
		return "Joseph Kirwin";
	}

	@Override
	public String getDescription() {
		return messages.getString("ext.ImportLogFiles.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL("http://code.google.com/p/zaproxy/wiki/MozillaMentorship_ImportingModSecurityLogs");
		} catch (MalformedURLException e) {
			return null;
		}
	}
}