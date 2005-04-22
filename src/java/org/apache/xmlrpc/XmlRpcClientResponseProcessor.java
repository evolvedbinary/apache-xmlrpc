/*
 * Copyright 1999,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.xmlrpc;

import java.util.Hashtable;
import java.io.InputStream;

import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;

/**
 * Process an XML-RPC server response from a byte array or an
 * InputStream into an Object. Optionally throw the result object
 * if it is an exception.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @version $Id$
 * @since 1.2
 */
public class XmlRpcClientResponseProcessor extends XmlRpc
{
    /** The result of the XML-RPC operation. Possibly an XmlRpcException */
    protected Object result;

    /** Set to true if a fault occured on the server. */
    protected boolean fault;

    /**
     * Decode an XML-RPC response from the specified InputStream. This
     * method will parse the input and return an Object. The result
     * will be an XmlRpcException if the server returned a fault, or
     * an Object if the server returned a result.
     *
     * @param is the stream to read from.
     * @return Object an XmlRpcException if an error occured, or the response.
     */
    public Object decodeResponse(InputStream is)
    throws XmlRpcClientException
    {
        result = null;
        fault = false;
        try
        {
            parse(is);
            if (fault)
            {
                return decodeException(result);
            }
            else
            {
                return result;
            }
        }
        catch (Exception x)
        {
            throw new XmlRpcClientException("Error decoding XML-RPC response", x);
        }
    }

    /**
     * Decode an exception from the result returned from the remote server.
     * This method both returns and throws an XmlRpcException. If it returns an
     * XmlRpcException then that is the exception thrown on the remote side. If
     * it throws an exception then an exception occured locally when decoding
     * the response
     *
     * @param result the result from the remote XML-RPC server.
     * @throws XmlRpcClientException if the result could not be processed.
     * @return XmlRpcException the processed response from the server.
     */
    protected XmlRpcException decodeException(Object result)
    throws XmlRpcClientException
    {
        Hashtable exceptionData;
        
        try
        {
            exceptionData = (Hashtable) result; 
            return new XmlRpcException(
                Integer.parseInt(exceptionData.get("faultCode").toString()),
                (String) exceptionData.get("faultString")
            );
        }
        catch (Exception x)
        {
            throw new XmlRpcClientException("Error decoding XML-RPC exception response", x);
        }
    }

    protected void objectParsed(Object what)
    {
        result = what;
    }

    /**
     * Overrides method in XmlRpc to handle fault repsonses.
     */
    public void startElement(String name, AttributeList atts)
            throws SAXException
    {
        if ("fault".equals(name))
        {
            fault = true;
        }
        else
        {
            super.startElement(name, atts);
        }
    }

    /**
     * Called by the worker management framework to see if this worker can be
     * re-used. Must attempt to clean up any state, and return true if it can
     * be re-used.
     *
     * @return boolean true if this worker has been cleaned up and may be re-used.
     */
    protected boolean canReUse()
    {
        result = null;
        fault = false;
        return true;
    }
}
