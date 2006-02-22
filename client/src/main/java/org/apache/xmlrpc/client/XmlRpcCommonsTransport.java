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
package org.apache.xmlrpc.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.ClientStreamConnection;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.util.HttpUtil;


/** An HTTP transport factory, which is based on the Jakarta Commons
 * HTTP Client.
 */
public class XmlRpcCommonsTransport extends XmlRpcHttpTransport {
	private class CommonsConnection implements ClientStreamConnection {
		final HttpClient client = new HttpClient();
		final PostMethod method;
		CommonsConnection(XmlRpcHttpClientConfig pConfig) {
			method = new PostMethod(pConfig.getServerURL().toString());
			method.getParams().setVersion(HttpVersion.HTTP_1_1);
		}
	}

	private String userAgent = super.getUserAgent() + " (Jakarta Commons httpclient Transport)";

	/** Creates a new instance.
	 * @param pClient The client, which will be invoking the transport.
	 */
	public XmlRpcCommonsTransport(XmlRpcClient pClient) {
		super(pClient);
	}

	protected String getUserAgent() { return userAgent; }

	protected void setRequestHeader(ClientStreamConnection pConnection, String pHeader, String pValue) {
		PostMethod method = ((CommonsConnection) pConnection).method;
		method.setRequestHeader(new Header(pHeader, pValue));
	}

	protected boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig pConfig, ClientStreamConnection pConnection) {
		Header h = ((CommonsConnection) pConnection).method.getResponseHeader( "Content-Encoding" );
		if (h == null) {
			return false;
		} else {
			return HttpUtil.isUsingGzipEncoding(h.getValue());
		}
	}

	protected ClientStreamConnection newConnection(XmlRpcStreamRequestConfig pConfig) throws XmlRpcClientException {
		return new CommonsConnection((XmlRpcHttpClientConfig) pConfig);
	}

	protected void closeConnection(ClientStreamConnection pConnection) throws XmlRpcClientException {
		((CommonsConnection) pConnection).method.releaseConnection();
	}

	protected OutputStream newOutputStream(XmlRpcStreamRequestConfig pConfig, ClientStreamConnection pConnection) throws XmlRpcClientException {
		throw new IllegalStateException("Not implemented");
	}

	protected boolean isUsingByteArrayOutput(XmlRpcStreamRequestConfig pConfig) { return true; }

	protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig, ClientStreamConnection pConnection) throws XmlRpcException {
		throw new IllegalStateException("Not implemented");
	}

	protected void setContentLength(ClientStreamConnection pConnection, int pLength) {
	}

	protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig, ClientStreamConnection pConnection, byte[] pContents)
			throws XmlRpcException {
		XmlRpcHttpClientConfig config = (XmlRpcHttpClientConfig) pConfig;
		CommonsConnection conn = (CommonsConnection) pConnection;
		PostMethod method = conn.method;
		method.setRequestEntity(new ByteArrayRequestEntity(pContents, "text/xml"));
		HostConfiguration hostConfig;
		try {
			URI hostURI = new URI(config.getServerURL().toString(), false);
			hostConfig = new HostConfiguration();
			hostConfig.setHost(hostURI);
		} catch (URIException e) {
			throw new XmlRpcClientException("Failed to parse URL: " + config.getServerURL().toString(), e);
		}
		try {
			conn.client.executeMethod(hostConfig, method);
			return method.getResponseBodyAsStream();
		} catch (HttpException e) {
			throw new XmlRpcClientException("Error in HTTP transport: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new XmlRpcClientException("I/O error in server communication: " + e.getMessage(), e);
		}
	}

	protected void setCredentials(XmlRpcHttpClientConfig pConfig, ClientStreamConnection pConnection) throws XmlRpcClientException {
		String userName = pConfig.getBasicUserName();
		if (userName != null) {
			Credentials creds = new UsernamePasswordCredentials(userName, pConfig.getBasicPassword());
			AuthScope scope = new AuthScope(null, AuthScope.ANY_PORT, null, AuthScope.ANY_SCHEME);
			((CommonsConnection) pConnection).client.getState().setCredentials(scope, creds);
		}
	}
}
