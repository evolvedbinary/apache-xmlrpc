package org.apache.xmlrpc;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import junit.framework.*;

/**
 * Abstract base class for tests that require a local WebServer for
 * test XML-RPC messages.
 *
 * @author <a href="mailto:rhoegg@isisnetworks.net">Ryan Hoegg</a>
 * @version $Id$
 */
abstract public class LocalServerRpcTest
    extends TestCase 
{

    
    /**
     * The name of our RPC handler.
     */
    protected static final String HANDLER_NAME = "TestHandler";

    /**
     * The value to use in our request parameter.
     */
    protected static final String REQUEST_PARAM_VALUE = "foobar";

    protected static final int SERVER_PORT;

    /**
     * The value to use in our request parameter.
     */
    protected static final String REQUEST_PARAM_XML =
        "<value>" + REQUEST_PARAM_VALUE + "</value>";

    /**
     * A RPC request of our echo server in XML.
     */
    protected static final String RPC_REQUEST =
        "<?xml version=\"1.0\"?>\n" +
        "<methodCall>\n" +
        " <methodName>" + HANDLER_NAME + ".echo</methodName>\n" +
        " <params><param>" + REQUEST_PARAM_XML + "</param></params>\n" +
        "</methodCall>\n";
    
    public LocalServerRpcTest(String message) {
        super(message);
    }
    
    /**
     * Static constructor
     * - initializes test port
     * 
     * TODO: can we initialize this from a properties file?
     */
    static {
        SERVER_PORT = 8081;
    }

    protected WebServer webServer;

    /**
     * Sets up a @link WebServer instance listening on the localhost.
     *
     * @param port Port to use for the WebServer
     */
    private void setUpWebServer(int port) {
        webServer = new WebServer(port);
        webServer.addHandler(HANDLER_NAME, new TestHandler());
    }
    
    /**
     * Sets up the @link WebServer with the default port.
     */
    protected void setUpWebServer() {
        setUpWebServer(SERVER_PORT);
    }

    /**
     * Starts the WebServer so tests can be run against it.
     */
    protected void startWebServer() {
        webServer.start();
    }
    
    /**
     * Stops the WebServer
     */
    protected void stopWebServer() {
        webServer.shutdown();
    }
    
    protected class TestHandler {
        public String echo(String message) {
            return message;
        }
    }
}
