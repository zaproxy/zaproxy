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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
abstract public class FileXML {

	protected Document doc = null;
	protected DocumentBuilder docBuilder = null;
	protected DocumentBuilderFactory docBuilderFactory = null;
	
	public FileXML(String rootElementName) {

		docBuilderFactory = DocumentBuilderFactory.newInstance();
		String rootString = "<" + rootElementName + "></" + rootElementName + ">";
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			
			doc = docBuilder.parse(new InputSource(new StringReader(rootString)));

		} catch (Exception e) {
			e.printStackTrace();
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
	 * @param parent
	 * @param childTagName
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
			element = (Element) getElement(base, tag);
			result = getText(element);
		} catch (Exception e) {
		}
		return result;
	    
	}
	
	protected String getValue(String tag) {
		Element element = doc.getDocumentElement();
		return getValue(element, tag);		
	}

	protected List getValues(String tag) {
		NodeList nodeList = (NodeList) doc.getElementsByTagName(tag);
		ArrayList resultList = new ArrayList();
		Element element = null;
		for (int i = 0; i < nodeList.getLength(); i++) {
			element = (Element) nodeList.item(i);
			resultList.add(getText(element));
		}
		return resultList;
	}
	
	abstract protected void parse() throws Exception;

	public void readAndParseFile(String fileName) throws SAXException, IOException, Exception {
		readFile(fileName);
		parse();
	}
	
	protected void readFile(String fileName) throws SAXException, IOException {

		// xml document processing
		DocumentBuilderFactory	factory 	= null;
		DocumentBuilder 		builder	= null;
		
		try {
			factory = DocumentBuilderFactory.newInstance();
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
           
        } catch (TransformerConfigurationException tce) {
           // Error generated by the parser
           System.out.println ("\n** Transformer Factory error");
           System.out.println("   " + tce.getMessage() );

           // Use the contained exception, if any
           Throwable x = tce;
           if (tce.getException() != null)
               x = tce.getException();
           x.printStackTrace();
      
        } catch (TransformerException te) {
           // Error generated by the parser
           System.out.println ("\n** Transformation error");
           System.out.println("   " + te.getMessage() );

           // Use the contained exception, if any
           Throwable x = te;
           if (te.getException() != null)
               x = te.getException();
           x.printStackTrace();
           

        } catch (IOException ioe) {
           // I/O error
           ioe.printStackTrace();
        } finally {
            if (outFile != null) {
                try {
                    outFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
			nodeList = (NodeList) doc.getElementsByTagName(path[i]);
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
