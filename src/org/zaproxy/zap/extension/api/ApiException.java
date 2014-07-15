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
package org.zaproxy.zap.extension.api;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.zaproxy.zap.utils.XMLStringUtil;

public class ApiException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public enum Type {BAD_FORMAT, BAD_TYPE, NO_IMPLEMENTOR, BAD_ACTION, BAD_VIEW, BAD_OTHER, INTERNAL_ERROR, MISSING_PARAMETER, 
		URL_NOT_FOUND, HREF_NOT_FOUND, SCAN_IN_PROGRESS, DISABLED, ALREADY_EXISTS, DOES_NOT_EXIST, ILLEGAL_PARAMETER, CONTEXT_NOT_FOUND,
		USER_NOT_FOUND, URL_NOT_IN_CONTEXT, BAD_API_KEY, SCRIPT_NOT_FOUND, BAD_SCRIPT_FORMAT, NO_ACCESS};
	
	private final String detail;

    private final Logger logger = Logger.getLogger(this.getClass());

	public ApiException(Type type) {
		super(type.name().toLowerCase());
		this.detail = null;
	}

    public ApiException(Type type, Throwable cause) {
        super(type.name().toLowerCase(), cause);
        this.detail = null;
    }

	public ApiException(Type type, String detail) {
		super(type.name().toLowerCase());
		this.detail = detail;
	}

    public ApiException(Type type, String detail, Throwable cause) {
        super(type.name().toLowerCase(), cause);
        this.detail = detail;
    }

	@Override
	public String toString () {
		if (detail != null) {
			return Constant.messages.getString("api.error." + super.getMessage()) +
				" (" + super.getMessage() + ") : " + detail;
		}
		return Constant.messages.getString("api.error." + super.getMessage()) +
			" (" + super.getMessage() + ")";
	}

	public String toString(API.Format format) {
		switch(format) {
		case HTML:
		case UI:
			return this.toString();
			
		case XML:
			try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
				Document doc = docBuilder.newDocument();
				Element rootElement = doc.createElement("Exception");
				doc.appendChild(rootElement);
				
				rootElement.setAttribute("type", "exception");
				rootElement.setAttribute("code", this.getMessage());
				if (detail != null) {
					rootElement.setAttribute("detail", XMLStringUtil.escapeControlChrs(this.detail));
				}
				
				rootElement.appendChild(doc.createTextNode(XMLStringUtil.escapeControlChrs(this.toString())));
				
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				
				StringWriter sw = new StringWriter();
				StreamResult result =  new StreamResult(sw);
				transformer.transform(source, result);
				
				return sw.toString();

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			break;

		case JSON:
			return this.toJSON().toString();
			
		default:
			break;
		}
		return null;
	}
	
	private JSONObject toJSON () {
		JSONObject ja = new JSONObject();
		ja.put("code", super.getMessage());
		ja.put("message", Constant.messages.getString("api.error." + super.getMessage()));
		if (detail != null) {
			ja.put("detail", detail);
		}
		return ja;
	}
	
}
