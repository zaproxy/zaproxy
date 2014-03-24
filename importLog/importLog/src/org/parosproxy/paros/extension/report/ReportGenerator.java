/*
Paros and its related class files.
Paros is an HTTP/HTTPS proxy for assessing web application security.
Copyright (C) 2003-2004 Chinotec Technologies Company

This program is free software; you can redistribute it and/or
modify it under the terms of the Clarified Artistic License
as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
Clarified Artistic License for more details.

You should have received a copy of the Clarified Artistic License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
// ZAP: 2011/11/04 Correct entityEncode
// ZAP: 2011/12/19 Escape invalid XML characters
// ZAP: 2012/08/07 synchronize calls on staticDateFormat
// ZAP: 2013/01/23 Clean up of exception handling/logging.

package org.parosproxy.paros.extension.report;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.zaproxy.zap.utils.XMLStringUtil;

public class ReportGenerator {

	private static final Logger logger = Logger.getLogger(ReportGenerator.class);

//    private static Pattern patternWindows = Pattern.compile("window", Pattern.CASE_INSENSITIVE);
//	private static Pattern patternLinux = Pattern.compile("linux", Pattern.CASE_INSENSITIVE);

	private static final SimpleDateFormat staticDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
	
	public static File stringToHtml (String inxml, String infilexsl, String outfilename) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	Document doc = null;

        //factory.setNamespaceAware(true);
        //factory.setValidating(true);
 		File stylesheet 		= null;
 		File outfile			= null;
 		StringReader inReader	= new StringReader(inxml);
 		
        try {
            stylesheet = new File(infilexsl);
            outfile	= new File(outfilename);
 
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new InputSource(inReader));
            
            // Use a Transformer for output
            TransformerFactory tFactory =
                TransformerFactory.newInstance();
            StreamSource stylesource = new StreamSource(stylesheet);
            Transformer transformer = tFactory.newTransformer(stylesource);
 
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(outfile);
            transformer.transform(source, result);

        } catch (TransformerException | SAXException | ParserConfigurationException | IOException e) {
            logger.error(e.getMessage(), e);
        } finally {

        }
                
		return outfile;

    }

    
    public static File fileToHtml (String infilexml, String infilexsl, String outfilename) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	Document doc = null;

        //factory.setNamespaceAware(true);
        //factory.setValidating(true);
 		File stylesheet 	= null;
 		File datafile		= null;
 		File outfile		= null;
 		
        try {
            stylesheet = new File(infilexsl);
            datafile   = new File(infilexml);
            outfile	= new File(outfilename);
 
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(datafile);
 
            // Use a Transformer for output
            TransformerFactory tFactory =
                TransformerFactory.newInstance();
            StreamSource stylesource = new StreamSource(stylesheet);
            Transformer transformer = tFactory.newTransformer(stylesource);
 
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(outfile);
            transformer.transform(source, result);

        } catch (TransformerException | SAXException | ParserConfigurationException | IOException e) {
            logger.error(e.getMessage(), e);
        } finally {

        }
                
		return outfile;

    }
    
	/**
	Encode entity for HTML or XML output.
	*/
	public static String entityEncode(String text) {
		String result = text;
		
		if (result == null) {
			return result;
		}

		// The escapeXml function doesnt cope with some 'special' chrs
		
		return StringEscapeUtils.escapeXml(XMLStringUtil.escapeControlChrs(result));
	}
	
	/**
	Get today's date string.
	*/
	public static String getCurrentDateTimeString() {
		Date dateTime = new Date(System.currentTimeMillis());
		return getDateTimeString(dateTime);
		
	}

	public static String getDateTimeString(Date dateTime) {
		// ZAP: fix unsafe call to DateFormats
		synchronized (staticDateFormat) {
		    return staticDateFormat.format(dateTime);
		}
	}
	
}
