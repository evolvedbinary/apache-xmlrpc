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
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlrpc.XmlRpcConfig;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;


/** <p>A default servlet implementation The typical use would
 * be to derive a subclass, which is overwriting at least the
 * method {@link #newXmlRpcHandlerMapping()}.</p>
 * <p>The servlet accepts the following init parameters:
 *   <table border="1">
 *     <tr><th>Name</th><th>Description</th></tr>
 *     <tr><td>enabledForExtensions</td><td>Sets the value
 *       {@link XmlRpcConfig#isEnabledForExtensions()}
 *       to true.</td></tr>
 *   </table>
 * </p>
 */
public class XmlRpcServlet extends HttpServlet {
	private static final long serialVersionUID = 2348768267234L;
	private XmlRpcServletServer server;

	/** Returns the servlets instance of {@link XmlRpcServletServer}. 
	 * @return The configurable instance of {@link XmlRpcServletServer}.
	 */
	public XmlRpcServletServer getXmlRpcServletServer() {
		return server;
	}

	public void init(ServletConfig pConfig) throws ServletException {
		super.init(pConfig);
		try {
			server = newXmlRpcServer(pConfig);
			server.setHandlerMapping(newXmlRpcHandlerMapping());
            String enabledForExtensionsParam = pConfig.getInitParameter("enabledForExtensions");
            if (enabledForExtensionsParam != null) {
                boolean b = Boolean.valueOf(enabledForExtensionsParam).booleanValue();
                ((XmlRpcServerConfigImpl) server.getConfig()).setEnabledForExtensions(b);
            }
        } catch (XmlRpcException e) {
			try {
				log("Failed to create XmlRpcServer: " + e.getMessage(), e);
			} catch (Throwable ignore) {
			}
			throw new ServletException(e);
		}
	}

	/** Creates a new instance of {@link XmlRpcServer},
	 * which is being used to process the requests. The default implementation
	 * will simply invoke <code>new {@link XmlRpcServer}.
	 */
	protected XmlRpcServletServer newXmlRpcServer(ServletConfig pConfig)
			throws XmlRpcException {
		return new XmlRpcServletServer();
	}

	/** Creates a new handler mapping. The default implementation loads
	 * a property file from the resource
	 * <code>org/apache/xmlrpc/webserver/XmlRpcServlet.properties</code>
	 */
	protected XmlRpcHandlerMapping newXmlRpcHandlerMapping() throws XmlRpcException {
		URL url = XmlRpcServlet.class.getResource("XmlRpcServlet.properties");
		if (url == null) {
			throw new XmlRpcException("Failed to locate resource XmlRpcServlet.properties");
		}
		try {
			return newPropertyHandlerMapping(url);
		} catch (IOException e) {
			throw new XmlRpcException("Failed to load resource " + url + ": " + e.getMessage(), e);
		}
	}

	/** Creates a new instance of {@link PropertyHandlerMapping} by
	 * loading the property file from the given URL. Called from
	 * {@link #newXmlRpcHandlerMapping()}.
	 */
	protected PropertyHandlerMapping newPropertyHandlerMapping(URL url) throws IOException, XmlRpcException {
		return new PropertyHandlerMapping(getClass().getClassLoader(), url,
                server.getTypeConverterFactory(),
                false);
	}

	/** Creates a new instance of {@link org.apache.xmlrpc.webserver.RequestData}
	 * for the request.
	 */
	public void doPost(HttpServletRequest pRequest, HttpServletResponse pResponse) throws IOException, ServletException {
		server.execute(pRequest, pResponse);
	}
}
