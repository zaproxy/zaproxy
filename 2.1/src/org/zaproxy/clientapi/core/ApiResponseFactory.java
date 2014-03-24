package org.zaproxy.clientapi.core;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class ApiResponseFactory {
	
	private ApiResponseFactory() {
	}
	
	public static ApiResponse getResponse(Node node) throws ClientApiException {
		if (node == null) {
			throw new ClientApiException("Null node");
		}
		Node typeNode = node.getAttributes().getNamedItem("type");
		if (typeNode != null) {
			String type = typeNode.getNodeValue();
			if ("list".equals(type)) {
				return new ApiResponseList(node);
			}
			if ("set".equals(type)) {
				return new ApiResponseSet(node);
			}
			if ("exception".equals(type)) {
				NamedNodeMap atts = node.getAttributes();

				String code = atts.getNamedItem("code").getNodeValue();
				String detail = null;
				if (atts.getNamedItem("detail") != null) {
					detail = atts.getNamedItem("detail").getNodeValue();
				}
				throw new ClientApiException(node.getTextContent(), code, detail); 
			}
		}
		return new ApiResponseElement(node);
	}

}
