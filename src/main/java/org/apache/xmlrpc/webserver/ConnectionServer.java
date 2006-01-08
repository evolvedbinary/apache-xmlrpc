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

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.server.XmlRpcStreamServer;


class ConnectionServer extends XmlRpcStreamServer {
	protected void writeError(XmlRpcStreamRequestConfig pConfig, OutputStream pStream,
							  Throwable pError) throws XmlRpcException {
		RequestData data = (RequestData) pConfig;
		try {
			if (data.isByteArrayRequired()) {
				super.writeError(pConfig, pStream, pError);
				data.getConnection().writeError(data, pError, pStream);
			} else {
				data.getConnection().writeErrorHeader(data, pError, -1);
				super.writeError(pConfig, pStream, pError);
				pStream.flush();
			}
		} catch (IOException e) {
			throw new XmlRpcException(e.getMessage(), e);
		}
	}

	protected void writeResponse(XmlRpcStreamRequestConfig pConfig, OutputStream pStream, Object pResult) throws XmlRpcException {
		RequestData data = (RequestData) pConfig;
		try {
			if (data.isByteArrayRequired()) {
				super.writeResponse(pConfig, pStream, pResult);
				data.getConnection().writeResponse(data, pStream);
			} else {
				data.getConnection().writeResponseHeader(data, -1);
				super.writeResponse(pConfig, pStream, pResult);
				pStream.flush();
			}
		} catch (IOException e) {
			throw new XmlRpcException(e.getMessage(), e);
		}
	}

	protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection) throws IOException {
		return ((Connection) pConnection).getInputStream((RequestData) pConfig);
	}

	protected OutputStream newOutputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection) throws IOException {
		return ((Connection) pConnection).getOutputStream(pConfig);
	}

	protected void closeConnection(Object pConnection) throws IOException {
	}
}