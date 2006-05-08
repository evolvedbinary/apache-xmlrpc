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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.util.HttpUtil;
import org.apache.xmlrpc.util.XmlRpcIOException;


/** An HTTP transport factory, which is based on the Jakarta Commons
 * HTTP Client.
 */
public class XmlRpcCommonsTransport extends XmlRpcHttpTransport {
	private final HttpClient client = new HttpClient();
	private static final String userAgent = USER_AGENT + " (Jakarta Commons httpclient Transport)";
	private PostMethod method;
	private int contentLength = -1;

	/** Creates a new instance.
	 * @param pClient The client, which will be invoking the transport.
	 */
	public XmlRpcCommonsTransport(XmlRpcClient pClient) {
		super(pClient, userAgent);
	}

	protected void setContentLength(int pLength) {
		contentLength = pLength;
	}

	public Object sendRequest(XmlRpcRequest pRequest) throws XmlRpcException {
		XmlRpcHttpClientConfig config = (XmlRpcHttpClientConfig) pRequest.getConfig();      
		method = new PostMethod(config.getServerURL().toString());
        
        if (config.getConnectionTimeout() != 0)
            client.getHttpConnectionManager().getParams().setConnectionTimeout(config.getConnectionTimeout());
        
        if (config.getReplyTimeout() != 0)
            client.getHttpConnectionManager().getParams().setSoTimeout(config.getConnectionTimeout());
        
		method.getParams().setVersion(HttpVersion.HTTP_1_1);
		return super.sendRequest(pRequest);
	}

	protected void setRequestHeader(String pHeader, String pValue) {
		method.setRequestHeader(new Header(pHeader, pValue));
	}

	protected boolean isResponseGzipCompressed() {
		Header h = method.getResponseHeader( "Content-Encoding" );
		if (h == null) {
			return false;
		} else {
			return HttpUtil.isUsingGzipEncoding(h.getValue());
		}
	}

	protected InputStream getInputStream() throws XmlRpcException {
		try {
			return method.getResponseBodyAsStream();
		} catch (HttpException e) {
			throw new XmlRpcClientException("Error in HTTP transport: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new XmlRpcClientException("I/O error in server communication: " + e.getMessage(), e);
		}
	}

	protected void setCredentials(XmlRpcHttpClientConfig pConfig) throws XmlRpcClientException {
		String userName = pConfig.getBasicUserName();
		if (userName != null) {
			Credentials creds = new UsernamePasswordCredentials(userName, pConfig.getBasicPassword());
			AuthScope scope = new AuthScope(null, AuthScope.ANY_PORT, null, AuthScope.ANY_SCHEME);
			client.getState().setCredentials(scope, creds);
		}
	}

	protected void close() throws XmlRpcClientException {
		method.releaseConnection();
	}

	protected boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig pConfig) {
		Header h = method.getResponseHeader( "Content-Encoding" );
		if (h == null) {
			return false;
		} else {
			return HttpUtil.isUsingGzipEncoding(h.getValue());
		}
	}

	protected void writeRequest(final RequestWriter pWriter) throws XmlRpcException {
		method.setRequestEntity(new RequestEntity(){
			public boolean isRepeatable() { return false; }
			public void writeRequest(OutputStream pOut) throws IOException {
				/* Make sure, that the socket is not closed by replacing it with our
				 * own BufferedOutputStream.
				 */
				BufferedOutputStream bos = new BufferedOutputStream(pOut){
					public void close() throws IOException {
						flush();
					}
				};
				try {
					pWriter.write(bos);
				} catch (XmlRpcException e) {
					throw new XmlRpcIOException(e);
				}
			}
			public long getContentLength() { return contentLength; }
			public String getContentType() { return "text/xml"; }
		});
		try {
			client.executeMethod(method);
		} catch (XmlRpcIOException e) {
			Throwable t = e.getLinkedException();
			if (t instanceof XmlRpcException) {
				throw (XmlRpcException) t;
			} else {
				throw new XmlRpcException("Unexpected exception: " + t.getMessage(), t);
			}
		} catch (IOException e) {
			throw new XmlRpcException("I/O error while communicating with HTTP server: " + e.getMessage(), e);
		}
	}
}
