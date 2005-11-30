/*
 * Copyright 2005 The Apache Software Foundation.
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

import java.net.URL;

import org.apache.commons.httpclient.HttpClient;


/** A transport factory, which uses the Jakarta Commons
 * HttpClient.
 */
public class CommonsXmlRpcTransportFactory implements XmlRpcTransportFactory
{
	// default properties for new common http-client http transports
	private URL url;
	private String auth;
	private Integer timeout;
	private Integer connectionTimeout; 

	/** Creates a new instance with the given server URL.
	 */
	public CommonsXmlRpcTransportFactory(URL pURL)
	{
		url = pURL;
	}

	public XmlRpcTransport createTransport () throws XmlRpcClientException
	{
		HttpClient client = new HttpClient();

		CommonsXmlRpcTransport transport = new CommonsXmlRpcTransport(url, client);

		if (auth != null)
		{
			transport.setBasicAuthentication(auth);
		}

		// set timeout if set
		if (timeout != null)
		{
			transport.setTimeout(timeout.intValue());
		}

		// set connection timeout if set
		if (connectionTimeout != null)
		{
			transport.setConnectionTimeout(connectionTimeout.intValue());
		}

		return transport;
	}

	/**
	 * Sets Authentication for this client. This will be sent as Basic
	 * Authentication header to the server as described in
	 * <a href="http://www.ietf.org/rfc/rfc2617.txt">
	 * http://www.ietf.org/rfc/rfc2617.txt</a>.
	 */
	public void setBasicAuthentication(String pAuth)
	{
		auth = pAuth;
	}
	
	/**
	 * Sets Authentication for this client. This will be sent as Basic
	 * Authentication header to the server as described in
	 * <a href="http://www.ietf.org/rfc/rfc2617.txt">
	 * http://www.ietf.org/rfc/rfc2617.txt</a>.
	 */
	public void setBasicAuthentication(String pUsername, String pPassword)
	{
		auth = pUsername + ":" + pPassword;
	}

    /**
     * Sets the socket timeout (<tt>SO_TIMEOUT</tt>) in milliseconds which is the
     * timeout for waiting for data. A timeout value of zero is interpreted as an
     * infinite timeout.
     *
     * @param newTimeoutInMilliSeconds timeout in milliseconds (ms)
     * @see org.apache.commons.httpclient.HttpClient#setTimeout
     */
    public void setTimeout(int newTimeoutInMilliSeconds)
    {
    	timeout = new Integer(newTimeoutInMilliSeconds);
    }

    /**
     * Sets the timeout until a connection is etablished. A timeout value of zero \
                means the timeout is not used. The default value is zero.
     *
     * @param newConnectionTimeoutInMilliSeconds timeout in milliseconds (ms)
     * @see org.apache.commons.httpclient.HttpClient#setConnectionTimeout
     */
    public void setConnectionTimeout(int newConnectionTimeoutInMilliSeconds)
    {
    	connectionTimeout = new Integer(newConnectionTimeoutInMilliSeconds);
    }

	public void setProperty(String propertyName, Object value)
	{
		if (TRANSPORT_AUTH.equals(propertyName))
		{
			auth = (String) value;
		}
		else if (TRANSPORT_URL.equals(propertyName))
		{
			url = (URL) value;
		}
	}
}
