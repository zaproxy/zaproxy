
/* ====================================================================
*
*  Copyright 1999-2004 The Apache Software Foundation
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation.  For more
* information on the Apache Software Foundation, please see
* <http://www.apache.org/>.
*
*/
package org.parosproxy.paros.network;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.log4j.Logger;
import org.zaproxy.zap.network.ZapHttpParser;

/**
 * This class is basing on the HttpClient under Apache licence 2.0
 */
public class GenericMethod extends EntityEnclosingMethod {
    
    /** Log object for this class. */
    private static final Logger log = Logger.getLogger(GenericMethod.class);

    /** The Content-Type for www-form-urlencoded. */
    public static final String FORM_URL_ENCODED_CONTENT_TYPE = 
        "application/x-www-form-urlencoded";

    /** 
     * The buffered request body consisting of <code>NameValuePair</code>s. 
     */
    private Vector<NameValuePair> params = new Vector<>();
    private String method = null;

    public GenericMethod(String method) {
        super();
        this.method = method;
    }
    
    public GenericMethod(String method, String uri) {
        super(uri);
        this.method = method;
    }


    /**
     * Returns <tt>"Generic"</tt>.
     *
     * @return <tt>"Generic"</tt>
     *
     */
    @Override
    public String getName() {
        return method;
    }


    /**
     * Returns <tt>true</tt> if there is a request body to be sent.
     * 
     * <P>This method must be overwritten by sub-classes that implement
     * alternative request content input methods
     * </p>
     * 
     * @return boolean
     * 
     */
    @Override
    protected boolean hasRequestContent() {
        log.trace("enter GenericMethod.hasRequestContent()");
        if (!this.params.isEmpty()) {
            return true;
        }
        return super.hasRequestContent();
    }

    /**
     * Clears request body.
     * 
     * <p>This method must be overwritten by sub-classes that implement
     * alternative request content input methods</p>
     * 
     * @since 2.0beta1
     */
    @Override
    protected void clearRequestBody() {
        log.trace("enter GenericMethod.clearRequestBody()");
        this.params.clear();
        super.clearRequestBody();
    }

    /**
     * Generates a request entity from the post parameters, if present.  Calls
     * {@link EntityEnclosingMethod#generateRequestBody()} if parameters have not been set.
     * 
     * @since 3.0
     */
    @Override
    protected RequestEntity generateRequestEntity() {
        if (!this.params.isEmpty()) {
            // Use a ByteArrayRequestEntity instead of a StringRequestEntity.
            // This is to avoid potential encoding issues.  Form url encoded strings
            // are ASCII by definition but the content type may not be.  Treating the content
            // as bytes allows us to keep the current charset without worrying about how
            // this charset will effect the encoding of the form url encoded string.
            String content = EncodingUtil.formUrlEncode(getParameters(), getRequestCharSet());
            ByteArrayRequestEntity entity = new ByteArrayRequestEntity(
                EncodingUtil.getAsciiBytes(content),
                FORM_URL_ENCODED_CONTENT_TYPE
            );
            return entity;
        }
        return super.generateRequestEntity();
    }
    
    /**
     * Sets the value of parameter with parameterName to parameterValue. This method
     * does not preserve the initial insertion order.
     *
     * @param parameterName name of the parameter
     * @param parameterValue value of the parameter
     *
     * @since 2.0
     */
    public void setParameter(String parameterName, String parameterValue) {
        log.trace("enter PostMethod.setParameter(String, String)");

        removeParameter(parameterName);
        addParameter(parameterName, parameterValue);
    }

    /**
     * Gets the parameter of the specified name. If there exists more than one
     * parameter with the name paramName, then only the first one is returned.
     *
     * @param paramName name of the parameter
     *
     * @return If a parameter exists with the name argument, the coresponding
     *         NameValuePair is returned.  Otherwise null.
     *
     * @since 2.0
     * 
     */
    public NameValuePair getParameter(String paramName) {
        log.trace("enter PostMethod.getParameter(String)");

        if (paramName == null) {
            return null;
        }

        Iterator<NameValuePair> iter = this.params.iterator();

        while (iter.hasNext()) {
            NameValuePair parameter = iter.next();

            if (paramName.equals(parameter.getName())) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * Gets the parameters currently added to the PostMethod. If there are no
     * parameters, a valid array is returned with zero elements. The returned
     * array object contains an array of pointers to  the internal data
     * members.
     *
     * @return An array of the current parameters
     *
     * @since 2.0
     * 
     */
    public NameValuePair[] getParameters() {
        log.trace("enter PostMethod.getParameters()");

        int numPairs = this.params.size();
        Object[] objectArr = this.params.toArray();
        NameValuePair[] nvPairArr = new NameValuePair[numPairs];

        for (int i = 0; i < numPairs; i++) {
            nvPairArr[i] = (NameValuePair) objectArr[i];
        }

        return nvPairArr;
    }

    /**
     * Adds a new parameter to be used in the POST request body.
     *
     * @param paramName The parameter name to add.
     * @param paramValue The parameter value to add.
     *
     * @throws IllegalArgumentException if either argument is null
     *
     * @since 1.0
     */
    public void addParameter(String paramName, String paramValue) 
    throws IllegalArgumentException {
        log.trace("enter PostMethod.addParameter(String, String)");

        if ((paramName == null) || (paramValue == null)) {
            throw new IllegalArgumentException(
                "Arguments to addParameter(String, String) cannot be null");
        }
        super.clearRequestBody();
        this.params.add(new NameValuePair(paramName, paramValue));
    }

    /**
     * Adds a new parameter to be used in the POST request body.
     *
     * @param param The parameter to add.
     *
     * @throws IllegalArgumentException if the argument is null or contains
     *         null values
     *
     * @since 2.0
     */
    public void addParameter(NameValuePair param) 
    throws IllegalArgumentException {
        log.trace("enter PostMethod.addParameter(NameValuePair)");

        if (param == null) {
            throw new IllegalArgumentException("NameValuePair may not be null");
        }
        addParameter(param.getName(), param.getValue());
    }

    /**
     * Adds an array of parameters to be used in the POST request body. Logs a
     * warning if the parameters argument is null.
     *
     * @param parameters The array of parameters to add.
     *
     * @since 2.0
     */
    public void addParameters(NameValuePair[] parameters) {
        log.trace("enter PostMethod.addParameters(NameValuePair[])");

        if (parameters == null) {
            log.warn("Attempt to addParameters(null) ignored");
        } else {
            super.clearRequestBody();
            for (int i = 0; i < parameters.length; i++) {
                this.params.add(parameters[i]);
            }
        }
    }

    /**
     * Removes all parameters with the given paramName. If there is more than
     * one parameter with the given paramName, all of them are removed.  If
     * there is just one, it is removed.  If there are none, then the request
     * is ignored.
     *
     * @param paramName The parameter name to remove.
     *
     * @return true if at least one parameter was removed
     *
     * @throws IllegalArgumentException When the parameter name passed is null
     *
     * @since 2.0
     */
    public boolean removeParameter(String paramName) 
    throws IllegalArgumentException {
        log.trace("enter PostMethod.removeParameter(String)");

        if (paramName == null) {
            throw new IllegalArgumentException(
                "Argument passed to removeParameter(String) cannot be null");
        }
        boolean removed = false;
        Iterator<NameValuePair> iter = this.params.iterator();

        while (iter.hasNext()) {
            NameValuePair pair = iter.next();

            if (paramName.equals(pair.getName())) {
                iter.remove();
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Removes all parameter with the given paramName and paramValue. If there
     * is more than one parameter with the given paramName, only one is
     * removed.  If there are none, then the request is ignored.
     *
     * @param paramName The parameter name to remove.
     * @param paramValue The parameter value to remove.
     *
     * @return true if a parameter was removed.
     *
     * @throws IllegalArgumentException when param name or value are null
     *
     * @since 2.0
     */
    public boolean removeParameter(String paramName, String paramValue) 
    throws IllegalArgumentException {
        log.trace("enter PostMethod.removeParameter(String, String)");

        if (paramName == null) {
            throw new IllegalArgumentException("Parameter name may not be null");
        }
        if (paramValue == null) {
            throw new IllegalArgumentException("Parameter value may not be null");
        }

        Iterator<NameValuePair> iter = this.params.iterator();

        while (iter.hasNext()) {
            NameValuePair pair = iter.next();

            if (paramName.equals(pair.getName())
                && paramValue.equals(pair.getValue())) {
                iter.remove();
                return true;
            }
        }

        return false;
    }

    /**
     * Sets an array of parameters to be used in the POST request body
     *
     * @param parametersBody The array of parameters to add.
     *
     * @throws IllegalArgumentException when param parameters are null
     * 
     * @since 2.0beta1
     */
    public void setRequestBody(NameValuePair[] parametersBody)
    throws IllegalArgumentException {
        log.trace("enter PostMethod.setRequestBody(NameValuePair[])");

        if (parametersBody == null) {
            throw new IllegalArgumentException("Array of parameters may not be null");
        }
        clearRequestBody();
        addParameters(parametersBody);
    }

    /**
     * {@inheritDoc}
     * 
     * <strong>Note:</strong> Malformed HTTP header lines are ignored (instead of throwing an exception).
     */
    /*
     * Implementation copied from HttpMethodBase#readResponseHeaders(HttpState, HttpConnection) but changed to use a custom
     * header parser (ZapHttpParser#parseHeaders(InputStream, String)).
     */
    @Override
    protected void readResponseHeaders(HttpState state, HttpConnection conn) throws IOException, HttpException {
        getResponseHeaderGroup().clear();

        Header[] headers = ZapHttpParser.parseHeaders(conn.getResponseInputStream(), getParams().getHttpElementCharset());
        // Wire logging moved to HttpParser
        getResponseHeaderGroup().setHeaders(headers);
    }

}
