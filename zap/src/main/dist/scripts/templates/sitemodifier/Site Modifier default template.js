/*
Site modifier scripts allow you to change how requests are represented in the sites tree.
The sites tree is ZAP's internal representation of the applications. The closer this matches the applications's functionality the better.
*/

var ArrayList = Java.type('java.util.ArrayList')
var DefaultNameValuePair = Java.type('org.zaproxy.zap.model.DefaultNameValuePair')

/**
 * Gets the parameters of the given {@code type} from the given {@code message}. Returning null
 * is taken to mean use the default methods for obtaining parameters.
 * The parameter names are shown in brackets after the site tree node names.
 *
 * The parameters are split using the key value pair separator(s) and each resulting
 * parameter is split into name/value pairs using key value separator(s).
 *
 * Parameters' names and values are in decoded form.
 *
 * @param msg the message whose parameters will be extracted from. This is an HttpMessage object.
 * @param type the type of parameters to extract. This is an HtmlParameter.Type enum which can be one of cookie, form, url, header, multipart
 * @param helper a helper class, currently unused
 * @return a {@code List} containing the parameters as DefaultNameValuePair objects
 * @throws IllegalArgumentException if the {@code msg} or {@code type} is {@code null}.
 */
function getParameters(msg, type, helper) {
	print("get params " + msg.getRequestHeader().getURI() + " " + type)
	var list = new ArrayList()
	switch (type.name()) {
		case 'url': 
			break
		case 'form': 
			break
		default: 
			break
	}
	if (list.size() == 0) {
		return null
	}
	return list
}

/**
 * Returns the tree path elements for the given {@code message}. Returning null is taken to mean
 * use the default methods for obtaining tree path elements.
 * This will determine the position of this message in the sites tree.
 *
 * <p>By default the elements are returned for the following URL are:
 *
 * <ul>
 *   <li><i>http://example.org/path/to/element?aa=bb&cc==dd</i> : ["path", "to", "element"]
 *   <li><i>http://example.org/path/to/element</i> : ["path", "to", "element"]
 *   <li><i>http://example.org/path/to/</i> : ["path", "to"]
 *   <li><i>http://example.org/path/to</i> : ["path", "to"]
 * </ul>
 *
 * @param msg the message for which the tree path elements will be extracted from. This is an HttpMessage object.
 * @param helper a helper class, currently unused
 * @return a {@code List} containing the tree path elements
 */
function getTreePath(msg, helper) {
	var uri = msg.getRequestHeader().getURI()
	print("getTreePath " + uri + " path=" + uri.getPath())
	return null
}

