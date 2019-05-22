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
// ZAP: 2012/04/23 Removed unnecessary casts.
// ZAP: 2012/05/02 Changed to set the initial capacity of a List.
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2015/08/19 Deprecated; Issue 1804: Disable processing of XML external entities by default
package org.parosproxy.paros.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.zaproxy.zap.utils.XmlUtils;

/**
 * @deprecated (2.4.2) Use {@link org.zaproxy.zap.utils.ZapXmlConfiguration} instead.
 */
@Deprecated
public abstract class FileXML {

	private static final Logger logger = Logger.getLogger(FileXML.class);

	protected Document doc = null;
	protected DocumentBuilder docBuilder = null;
	protected DocumentBuilderFactory docBuilderFactory = null;
	
	public FileXML(String rootElementName) {

		String rootString = "<" + rootElementName + "></" + rootElementName + ">";
		try {
			docBuilderFactory = XmlUtils.newXxeDisabledDocumentBuilderFactory();
			docBuilder = docBuilderFactory.newDocumentBuilder();
			
			doc = docBuilder.parse(new InputSource(new StringReader(rootString)));

		} catch (Exception e) {
		    logger.error(e.getMessage(), e);
		}
		
	}
	
	public Document getDocument() {
		return doc;
	}
	
	/*
	 * Get a single element (first element) under a base element matching a tag
	 */
	protected Element getElement(Element base, String childTag) {
	    Element[] elements = getElements(base, childTag);
	    if (elements == null) {
	        return null;
	    } else {
	        return elements[0];
		}
	}
	
	protected Element getElement(String tag) {
		Element parent = doc.getDocumentElement();
		return getElement(parent, tag);
	}
	
	protected Element getElement(String[] path) {
		
	    Element[] elements = getElements(path);
	    if (elements==null) {
	        return null;
	    } else {
	        return elements[0];
	    }
	    
	}

	/**
	 * Get all elements under a base element matching a tag name
	 * @param base
	 * @param childTag
	 * @return
	 */
	protected Element[] getElements(Element base, String childTag) {
		NodeList nodeList = base.getElementsByTagName(childTag);
		if (nodeList.getLength() == 0) {
			return null;
		}
		Element[] elements = new Element[nodeList.getLength()];
		for (int i=0; i<nodeList.getLength(); i++) {
		    elements[i] = (Element) nodeList.item(i);
		}
		return elements;
	}
	
	protected Element[] getElements(String tagName) {
		Element parent = doc.getDocumentElement();
		return getElements(parent, tagName);
	}
	
	protected Element[] getElements(String[] path) {
		NodeList nodeList = null;
		Element element = doc.getDocumentElement();
		for (int i=0; i<path.length-1; i++) {
			nodeList = element.getElementsByTagName(path[i]);
			if (nodeList.getLength() > 0) {
				element = (Element) nodeList.item(i);
			} else {
				return null;
			}
		}
		nodeList = element.getElementsByTagName(path[path.length-1]);
		if (nodeList.getLength() == 0) {
			return null;
		}
		Element[] elements = new Element[nodeList.getLength()];
		for (int i=0; i<nodeList.getLength(); i++) {
		    elements[i] = (Element) nodeList.item(0);
		}
		return elements;	
	    
	}
			
	/**
	Get the text in text node from the element.
	@param	element	Element to get text from
	@return	Text in the text node under the element
	*/
	private String getText(Element element) {
		try {
			for (int i = 0; i < element.getChildNodes().getLength(); i++) {
				Node node = element.getChildNodes().item(i);
				if (node.getNodeType() == Node.TEXT_NODE) {
					return node.getNodeValue();
				}
			}
		} catch (Exception e) {
		}
		return "";

	}
	
	/**
	 * Get the value of the tag under a base element
	 * @param base
	 * @param tag
	 * @return
	 */
	protected String getValue(Element base, String tag) {

	    Element element = null;
		String result = "";
		try {
			// ZAP: Removed unnecessary cast.
			element = getElement(base, tag);
			result = getText(element);
		} catch (Exception e) {
		}
		return result;
	    
	}
	
	protected String getValue(String tag) {
		Element element = doc.getDocumentElement();
		return getValue(element, tag);		
	}

	protected List<String> getValues(String tag) {
		// ZAP: Removed unnecessary cast.
		NodeList nodeList = doc.getElementsByTagName(tag);
		// ZAP: Added variable "length".
		final int length = nodeList.getLength();
		// ZAP: Changed to set the initial capacity.
		ArrayList<String> resultList = new ArrayList<>(length);
		Element element = null;
		// ZAP: Changed to use the variable "length".
		for (int i = 0; i < length; i++) {
			element = (Element) nodeList.item(i);
			resultList.add(getText(element));
		}
		return resultList;
	}
	
	protected abstract void parse() throws Exception;

	public void readAndParseFile(String fileName) throws SAXException, IOException, Exception {
		readFile(fileName);
		parse();
	}
	
	protected void readFile(String fileName) throws SAXException, IOException {

		// xml document processing
		DocumentBuilderFactory	factory 	= null;
		DocumentBuilder 		builder	= null;
		
		try {
			factory = XmlUtils.newXxeDisabledDocumentBuilderFactory();
			factory.setValidating(false);
			builder	= factory.newDocumentBuilder();
			//builder.setErrorHandler(new ErrorHandler() {
			//});
		} catch (ParserConfigurationException e) {
		}
		
		doc = builder.parse(fileName);
	}
	
	public void saveFile(String fileName) {
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setNamespaceAware(true);
        //factory.setValidating(true);
		File file = null;
		FileOutputStream outFile = null;
		
        try {
            file = new File(fileName); 
            outFile = new FileOutputStream(file);
            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(outFile);
            //StreamResult result = new StreamResult(System.out);
            
            transformer.transform(source, result);
           
        } catch (TransformerException | IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (outFile != null) {
                try {
                    outFile.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

    }
	
	public void setDocument(Document doc) throws Exception {
		this.doc = doc;
		parse();
	}
	
	protected void setValue(String tagName, String value) {
		Element element = null;
		try {
			// set only the first tag
			element = getElement(tagName);
			if (element == null) {
				// if not found, add to root element
				element = doc.createElement(tagName);
				doc.getDocumentElement().appendChild(element);
			}
			for (int i=0; i<element.getChildNodes().getLength(); i++) {
				Node node = element.getChildNodes().item(i);
				if (node.getNodeType() == Node.TEXT_NODE) {
					node.setNodeValue(value);
					return;
				}
			}
			Node newNode = doc.createTextNode(value);
			element.appendChild(newNode);
		} catch (Exception e) {
		}
		
	}
	
	protected void setValue(String[] path, String value) {
		Element element = doc.getDocumentElement();
		NodeList nodeList = null;
		Element newElement = null;
		Node newNode = null;
		
		for (int i=0; i<path.length; i++) {
			// ZAP: Removed unnecessary cast.
			nodeList = doc.getElementsByTagName(path[i]);
			if (nodeList.getLength() == 0) {
				// create element if not found
				newElement = doc.createElement(path[i]);
				element.appendChild(newElement);
				element = newElement;
			} else {
				// point to new element
				element = (Element) nodeList.item(0);
			}
		}

		// element located
		try {
			// search for text node and set value
			for (int i = 0; i < element.getChildNodes().getLength(); i++) {
				Node node = element.getChildNodes().item(i);
				if (node.getNodeType() == Node.TEXT_NODE) {
					node.setNodeValue(value);
					return;
				}
			}
			// if not found, create text node
			newNode = doc.createTextNode(value);
			element.appendChild(newNode);
		} catch (Exception e) {
		}
		
	}
	
	protected void removeElement(Element base, String tag) {
	    Element[] elements = getElements(base, tag);
	    if (elements== null) return;
	    for (int i=0; i<elements.length; i++) {
	        try {
	            base.removeChild(elements[i]);
	        } catch (Exception e) {}
	    }
	}

	protected void removeElement(String tag) {
		Element base = doc.getDocumentElement();
		removeElement(base, tag);
	}

	/**
	 * 
	 * @param base
	 * @param tag
	 * @param value
	 * @return added element
	 */
	protected Element addElement(Element base, String tag, String value) {
	    Element element = doc.createElement(tag);
	    base.appendChild(element);
	    for (int i=0; i<element.getChildNodes().getLength(); i++) {
	        Node node = element.getChildNodes().item(i);
	        if (node.getNodeType() == Node.TEXT_NODE) {
	            node.setNodeValue(value);
	            return element;
	        }
	    }
	    Node newNode = doc.createTextNode(value);
	    element.appendChild(newNode);
	    return element;
	}
	
	protected Element addElement(Element base, String tag) {
	    Element element = doc.createElement(tag);
		base.appendChild(element);
	    return element;
	}
	
	protected Element addElement(String tag) {
	    Element element = doc.createElement(tag);
		doc.getDocumentElement().appendChild(element);
	    return element;
	}
}
