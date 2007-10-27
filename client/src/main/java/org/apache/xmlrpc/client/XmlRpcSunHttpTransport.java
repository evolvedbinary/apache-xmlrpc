package org.apache.xmlrpc.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.util.HttpUtil;
import org.xml.sax.SAXException;


/** Default implementation of an HTTP transport, based on the
 * {@link java.net.HttpURLConnection} class.
 */
public class XmlRpcSunHttpTransport extends XmlRpcHttpTransport {
	private static final String userAgent = USER_AGENT + " (Sun HTTP Transport)";
	private URLConnection conn;

	/** Creates a new instance.
	 * @param pClient The client controlling this instance.
	 */
	public XmlRpcSunHttpTransport(XmlRpcClient pClient) {
		super(pClient, userAgent);
	}

    protected URLConnection newURLConnection(URL pURL) throws IOException {
        return pURL.openConnection();
    }

    /**
     * For use by subclasses.
     */
    protected URLConnection getURLConnection() {
        return conn;
    }

    public Object sendRequest(XmlRpcRequest pRequest) throws XmlRpcException {
		XmlRpcHttpClientConfig config = (XmlRpcHttpClientConfig) pRequest.getConfig();
		try {
		    final URLConnection c = conn = newURLConnection(config.getServerURL());
			c.setUseCaches(false);
			c.setDoInput(true);
			c.setDoOutput(true);
		} catch (IOException e) {
			throw new XmlRpcException("Failed to create URLConnection: " + e.getMessage(), e);
		}
		return super.sendRequest(pRequest);
	}

	protected void setRequestHeader(String pHeader, String pValue) {
	    getURLConnection().setRequestProperty(pHeader, pValue);
	}

	protected void close() throws XmlRpcClientException {
	    final URLConnection c = getURLConnection();
		if (c instanceof HttpURLConnection) {
			((HttpURLConnection) c).disconnect();
		}
	}

	protected boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig pConfig) {
		return HttpUtil.isUsingGzipEncoding(getURLConnection().getHeaderField("Content-Encoding"));
	}

	protected InputStream getInputStream() throws XmlRpcException {
		try {
			return getURLConnection().getInputStream();
		} catch (IOException e) {
			throw new XmlRpcException("Failed to create input stream: " + e.getMessage(), e);
		}
	}

	protected void writeRequest(ReqWriter pWriter) throws IOException, XmlRpcException, SAXException {
        pWriter.write(getURLConnection().getOutputStream());
	}
}