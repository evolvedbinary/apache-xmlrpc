package org.apache.xmlrpc.server;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlrpc.common.ServerStreamConnection;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;



/** Abstract extension of {@link XmlRpcStreamServer} for deriving
 * HTTP servers.
 */
public abstract class XmlRpcHttpServer extends XmlRpcStreamServer {
	protected abstract void setResponseHeader(ServerStreamConnection pConnection, String pHeader, String pValue);

	protected OutputStream getOutputStream(ServerStreamConnection pConnection, XmlRpcStreamRequestConfig pConfig, OutputStream pStream) throws IOException {
		if (pConfig.isEnabledForExtensions()  &&  pConfig.isGzipRequesting()) {
			setResponseHeader(pConnection, "Content-Encoding", "gzip");
		}
		return super.getOutputStream(pConnection, pConfig, pStream);
	}
}
