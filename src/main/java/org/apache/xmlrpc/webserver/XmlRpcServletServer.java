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
package org.apache.xmlrpc.webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfigImpl;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.server.XmlRpcHttpServerConfig;
import org.apache.xmlrpc.server.XmlRpcStreamServer;
import org.apache.xmlrpc.util.HttpUtil;


/** An extension of {@link org.apache.xmlrpc.server.XmlRpcServer},
 * which is suitable for processing servlet requests.
 */
public class XmlRpcServletServer extends XmlRpcStreamServer {
	/** This class is used as a "connection" while processing the request.
	 * In other words, it is what
	 * {@link XmlRpcStreamServer#execute(XmlRpcStreamRequestConfig, Object)}
	 * receives as a connection object.
	 */
	protected static class RequestData extends XmlRpcHttpRequestConfigImpl {
		private final HttpServletRequest request;

		/** Creates a new instance with the given request and response.
		 * @param pRequest The servlet request.
		 */
		public RequestData(HttpServletRequest pRequest) {
			request = pRequest;
		}
		/** Returns the servlet request.
		 * @return The {@link HttpServletRequest}.
		 */
		public HttpServletRequest getRequest() { return request; }
	}

	protected RequestData newConfig(HttpServletRequest pRequest) {
		return new RequestData(pRequest);
	}

	protected RequestData getConfig(HttpServletRequest pRequest) {
		RequestData result = newConfig(pRequest);
		XmlRpcHttpServerConfig serverConfig = (XmlRpcHttpServerConfig) getConfig();
		result.setBasicEncoding(serverConfig.getBasicEncoding());
		result.setContentLengthOptional(serverConfig.isContentLengthOptional());
		result.setEnabledForExtensions(serverConfig.isEnabledForExtensions());
		result.setGzipCompressing(HttpUtil.isUsingGzipEncoding(pRequest.getHeader("Content-Encoding")));
		result.setGzipRequesting(HttpUtil.isUsingGzipEncoding(pRequest.getHeaders("Accept-Encoding")));
		result.setEncoding(pRequest.getCharacterEncoding());
		HttpUtil.parseAuthorization(result, pRequest.getHeader("Authorization"));
		return result;
	}

	protected RequestData newRequestData(HttpServletRequest pRequest) {
		return new RequestData(pRequest);
	}

	/** Processes the servlet request.
	 * @param pRequest The servlet request being read.
	 * @param pResponse The servlet response being created.
	 * @throws IOException Reading the request or writing the response failed.
	 * @throws ServletException Processing the request failed.
	 */
	public void execute(HttpServletRequest pRequest, HttpServletResponse pResponse)
			throws ServletException, IOException {
		RequestData config = getConfig(pRequest);
		try {
			super.execute(config, pResponse);
		} catch (XmlRpcException e) {
			throw new ServletException(e);
		}
	}

	/** Returns, whether the requests content length is required.
	 */
	protected boolean isContentLengthRequired(XmlRpcStreamRequestConfig pConfig) {
		RequestData data = (RequestData) pConfig;
		if (data.isEnabledForExtensions()) {
			// The spec requires a content-length.
			return true;
		}
		return !((XmlRpcHttpServerConfig) getConfig()).isContentLengthOptional();
	}

	protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection) throws IOException {
		return ((RequestData) pConfig).request.getInputStream();
	}

	protected OutputStream newOutputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection) throws IOException {
		HttpServletResponse response = ((HttpServletResponse) pConnection);
		response.setContentType("text/xml");
		return response.getOutputStream();
	}

	protected OutputStream getOutputStream(XmlRpcStreamRequestConfig pConfig,
										   Object pConnection,
										   int pSize) throws IOException {
		if (pSize != -1) {
			((HttpServletResponse) pConnection).setContentLength(pSize);
		}
		return super.getOutputStream(pConfig, pConnection, pSize);
	}

	protected void closeConnection(Object pConnection) throws IOException {
		((HttpServletResponse) pConnection).getOutputStream().close();
	}
}