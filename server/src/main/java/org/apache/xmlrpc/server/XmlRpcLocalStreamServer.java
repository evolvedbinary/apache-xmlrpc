package org.apache.xmlrpc.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.LocalStreamConnection;
import org.apache.xmlrpc.common.ServerStreamConnection;
import org.apache.xmlrpc.common.XmlRpcRequestProcessor;
import org.apache.xmlrpc.common.XmlRpcRequestProcessorFactory;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;


/** Server part of a local stream transport.
 */
public class XmlRpcLocalStreamServer extends XmlRpcStreamServer {
	public Object execute(XmlRpcRequest pRequest) throws XmlRpcException {
		XmlRpcRequestProcessor server = ((XmlRpcRequestProcessorFactory) pRequest.getConfig()).getXmlRpcServer();
		return server.execute(pRequest);
	}
	protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig, ServerStreamConnection pConnection) throws IOException {
		LocalStreamConnection lsc = (LocalStreamConnection) pConnection;
		return new ByteArrayInputStream(lsc.getOstream().toByteArray());
	}
	protected OutputStream newOutputStream(XmlRpcStreamRequestConfig pConfig, ServerStreamConnection pConnection) throws IOException {
		LocalStreamConnection lsc = (LocalStreamConnection) pConnection;
		ByteArrayOutputStream istream = new ByteArrayOutputStream();
		lsc.setIstream(istream);
		return istream;
	}
	protected void closeConnection(ServerStreamConnection pConnection) throws IOException {
		LocalStreamConnection lsc = (LocalStreamConnection) pConnection;
		final ByteArrayOutputStream istream = lsc.getIstream();
		if (istream != null) {
			try { istream.close(); } catch (Throwable ignore) {}
		}
	}
}