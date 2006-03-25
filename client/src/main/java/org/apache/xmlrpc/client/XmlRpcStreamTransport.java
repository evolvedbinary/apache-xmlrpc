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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.parser.XmlRpcResponseParser;
import org.apache.xmlrpc.serializer.XmlRpcWriter;
import org.apache.xmlrpc.util.SAXParsers;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/** Implementation of a transport class, which is based on an output
 * stream for sending the request and an input stream for receiving
 * the response,
 */
public abstract class XmlRpcStreamTransport extends XmlRpcTransportImpl {
	protected class RequestWriter {
		private final XmlRpcRequest request;

		protected RequestWriter(XmlRpcRequest pRequest) {
			request = pRequest;
		}

		protected XmlRpcRequest getRequest() {
			return request;
		}

		/** Writes the requests XML data to the given output stream,
		 * possibly compressing it. Ensures, that the output stream
		 * is being closed.
		 */
		protected void write(OutputStream pStream) throws XmlRpcException {
			XmlRpcStreamRequestConfig config = (XmlRpcStreamRequestConfig) request.getConfig();
			if (isCompressingRequest(config)) {
				try {
					GZIPOutputStream gStream = new GZIPOutputStream(pStream);
					writeUncompressed(gStream);
					pStream.close();
					pStream = null;
				} catch (IOException e) {
					throw new XmlRpcException("Failed to write request: " + e.getMessage(), e);
				} finally {
					if (pStream != null) { try { pStream.close(); } catch (Throwable ignore) {} }
				}
			} else {
				writeUncompressed(pStream);
			}
		}

		/** Writes the requests uncompressed XML data to the given
		 * output stream. Ensures, that the output stream is being
		 * closed.
		 */
		protected void writeUncompressed(OutputStream pStream)
				throws XmlRpcException {
			final XmlRpcStreamConfig config = (XmlRpcStreamConfig) request.getConfig();
			try {
				ContentHandler h = getClient().getXmlWriterFactory().getXmlWriter(config, pStream);
				XmlRpcWriter xw = new XmlRpcWriter(config, h, getClient().getTypeFactory());
				xw.write(request);
				pStream.close();
				pStream = null;
			} catch (SAXException e) {
				Exception ex = e.getException();
				if (ex != null  &&  ex instanceof XmlRpcException) {
					throw (XmlRpcException) ex;
				} else {
					throw new XmlRpcClientException("Failed to send request: " + e.getMessage(), e);
				}
			} catch (IOException e) {
				throw new XmlRpcException("Failed to write request: " + e.getMessage(), e);
			} finally {
				if (pStream != null) { try { pStream.close(); } catch (Throwable ignore) {} }
			}
		}
	}

	protected RequestWriter newRequestWriter(XmlRpcRequest pRequest)
			throws XmlRpcException {
		return new RequestWriter(pRequest);
	}
	
	/** Creates a new instance on behalf of the given client.
	 */
	protected XmlRpcStreamTransport(XmlRpcClient pClient) {
		super(pClient);
	}

	/** Closes the connection and ensures, that all resources are being
	 * released.
	 */
	protected abstract void close() throws XmlRpcClientException;

	/** Returns, whether the response is gzip compressed.
	 * @param pConfig The clients configuration.
	 * @return Whether the response stream is gzip compressed.
	 */
	protected abstract boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig pConfig);

	/** Invokes the request writer.
	 */
	protected abstract void writeRequest(RequestWriter pWriter) throws XmlRpcException;

	/** Returns the input stream, from which the response is
	 * being read.
	 */
	protected abstract InputStream getInputStream() throws XmlRpcException;

	protected boolean isCompressingRequest(XmlRpcStreamRequestConfig pConfig) {
		return pConfig.isEnabledForExtensions()
			&& pConfig.isGzipCompressing();
	}

	public Object sendRequest(XmlRpcRequest pRequest) throws XmlRpcException {
		XmlRpcStreamRequestConfig config = (XmlRpcStreamRequestConfig) pRequest.getConfig();
		boolean closed = false;
		try {
			RequestWriter writer = newRequestWriter(pRequest);
			writeRequest(writer);
			InputStream istream = getInputStream();
			if (isResponseGzipCompressed(config)) {
				istream = new GZIPInputStream(istream);
			}
			Object result = readResponse(config, istream);
			closed = true;
			close();
			return result;
		} catch (IOException e) {
			throw new XmlRpcException("Failed to read servers response: "
					+ e.getMessage(), e);
		} finally {
			if (!closed) { try { close(); } catch (Throwable ignore) {} }
		}
	}

	protected XMLReader newXMLReader() throws XmlRpcException {
		return SAXParsers.newXMLReader();
	}

	protected Object readResponse(XmlRpcStreamRequestConfig pConfig, InputStream pStream) throws XmlRpcException {
		InputSource isource = new InputSource(pStream);
		XMLReader xr = newXMLReader();
		XmlRpcResponseParser xp;
		try {
			xp = new XmlRpcResponseParser(pConfig, getClient().getTypeFactory());
			xr.setContentHandler(xp);
			xr.parse(isource);
		} catch (SAXException e) {
			throw new XmlRpcClientException("Failed to parse servers response: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new XmlRpcClientException("Failed to read servers response: " + e.getMessage(), e);
		}
		if (xp.isSuccess()) {
			return xp.getResult();
		} else {
			throw new XmlRpcException(xp.getErrorCode(), xp.getErrorMessage());
		}
	}
}
