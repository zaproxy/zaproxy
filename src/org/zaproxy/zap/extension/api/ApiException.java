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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.zaproxy.zap.utils.XMLStringUtil;

public class ApiException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public enum Type {
		/**
		 * Indicates that the response's format requested is not valid.
		 * 
		 * @see API.Format
		 */
		BAD_FORMAT,
		/**
		 * Indicates that the requested type is not valid.
		 * 
		 * @see API.RequestType
		 */
		BAD_TYPE, NO_IMPLEMENTOR, BAD_ACTION, BAD_VIEW, BAD_OTHER, INTERNAL_ERROR, MISSING_PARAMETER, 
		URL_NOT_FOUND, HREF_NOT_FOUND, SCAN_IN_PROGRESS, DISABLED, ALREADY_EXISTS, DOES_NOT_EXIST,
		/**
		 * Indicates that the value of a parameter is illegal/invalid (for example, it's not of expected type (boolean,
		 * integer)).
		 * <p>
		 * The name of the parameter should be in the {@code detail} of the exception.
		 * 
		 * @see ApiException#ApiException(Type, String)
		 * @see ApiException#ApiException(Type, String, Throwable)
		 */
		ILLEGAL_PARAMETER,
		CONTEXT_NOT_FOUND,
		USER_NOT_FOUND, URL_NOT_IN_CONTEXT, BAD_API_KEY, SCRIPT_NOT_FOUND, BAD_SCRIPT_FORMAT, NO_ACCESS,
		/*
		 * Indicates that the requested operation is not allowed in the current mode
		 */
		MODE_VIOLATION};
	
	private final Type type;
	private final String detail;

    private final Logger logger = Logger.getLogger(this.getClass());

	public ApiException(Type type) {
		this(type, null, null);
	}

    public ApiException(Type type, Throwable cause) {
        this(type, null, cause);
    }

	public ApiException(Type type, String detail) {
		this(type, detail, null);
	}

    public ApiException(Type type, String detail, Throwable cause) {
        super(type.name().toLowerCase(), cause);
        this.type = type;
        this.detail = detail;
    }

	public Type getType() {
		return type;
	}

	@Override
	public String toString () {
		return this.toString(true);
	}
	
	public String toString (boolean incDetails) {
		if (! incDetails) {
			return Constant.messages.getString("api.error." + super.getMessage());
		} else if (detail != null) {
			return Constant.messages.getString("api.error." + super.getMessage()) +
				" (" + super.getMessage() + ") : " + detail;
		} else {
			return Constant.messages.getString("api.error." + super.getMessage()) +
				" (" + super.getMessage() + ")";
		}
	}

	public String toString(API.Format format, boolean incDetails) {
		switch(format) {
		case HTML:
		case UI:
			return StringEscapeUtils.escapeHtml(this.toString(incDetails));
			
		case XML:
			try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
				Document doc = docBuilder.newDocument();
				Element rootElement = doc.createElement("Exception");
				doc.appendChild(rootElement);
				
				rootElement.setAttribute("type", "exception");
				rootElement.setAttribute("code", this.getMessage());
				if (incDetails && detail != null) {
					rootElement.setAttribute("detail", XMLStringUtil.escapeControlChrs(this.detail));
				}
				
				rootElement.appendChild(doc.createTextNode(XMLStringUtil.escapeControlChrs(this.toString(incDetails))));
				
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
			return this.toJSON(incDetails).toString();
			
		default:
			break;
		}
		return null;
	}
	
	private JSONObject toJSON (boolean incDetails) {
		JSONObject ja = new JSONObject();
		ja.put("code", super.getMessage());
		ja.put("message", Constant.messages.getString("api.error." + super.getMessage()));
		if (incDetails && detail != null) {
			ja.put("detail", detail);
		}
		return ja;
	}
	
}
