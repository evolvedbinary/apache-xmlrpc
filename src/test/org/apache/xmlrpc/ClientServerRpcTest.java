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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests XmlRpc run-time.
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id$
 */
public class ClientServerRpcTest
    extends TestCase 
{
    /**
     * The name of our RPC handler.
     */
    private static final String HANDLER_NAME = "TestHandler";

    /**
     * The identifier or fully qualified class name of the SAX driver
     * to use.  This is generally <code>uk.co.wilson.xml.MinML</code>,
     * but could be changed to
     * <code>org.apache.xerces.parsers.SAXParser</code> for timing
     * comparisons.
     */
    private static final String SAX_DRIVER = "uk.co.wilson.xml.MinML";

    /**
     * The number of RPCs to make for each test.
     */
    private static final int NBR_REQUESTS = 1000;

    /**
     * The value to use in our request parameter.
     */
    private static final String REQUEST_PARAM_VALUE = "foobar";

    /**
     * The value to use in our request parameter.
     */
    private static final String REQUEST_PARAM_XML =
        "<value>" + REQUEST_PARAM_VALUE + "</value>";

    /**
     * A RPC request of our echo server in XML.
     */
    private static final String RPC_REQUEST =
        "<?xml version=\"1.0\"?>\n" +
        "<methodCall>\n" +
        " <methodName>" + HANDLER_NAME + ".echo</methodName>\n" +
        " <params><param>" + REQUEST_PARAM_XML + "</param></params>\n" +
        "</methodCall>\n";

    private WebServer webServer;

    private XmlRpcServer server;

    private XmlRpcClient client;

    private XmlRpcClientLite liteClient;

    /**
     * Constructor
     */
    public ClientServerRpcTest(String testName) 
    {
        super(testName);
    }

    /**
     * Return the Test
     */
    public static Test suite() 
    {
        return new TestSuite(ClientServerRpcTest.class);
    }

    /**
     * Setup the server and clients.
     */
    public void setUp() 
    {
        XmlRpc.setDebug(true);
        try
        {
            XmlRpc.setDriver(SAX_DRIVER);
        }
        catch (ClassNotFoundException e)
        {
            fail(e.toString());
        }

        // WebServer
        //webServer = new WebServer();

        // Server
        server = new XmlRpcServer();
        server.addHandler(HANDLER_NAME, new TestHandler());
        // HELP: What port and url space does this run on?

        // Standard Client
        //client = new XmlRpcClient();

        // Supposedly light-weight client
        //liteClient = new XmlRpcClientLite();
    }
   
    /**
     * Tear down the test.
     */
    public void tearDown() 
    {
        liteClient = null;
        client = null;
        // TODO: Shut down server
        server = null;
        // TODO: Shut down web server
        webServer = null;
        XmlRpc.setDebug(false);
    }

    /**
     * Tests server's RPC capabilities directly.
     */
    public void testServer()
    {
        try
        {
            long time = System.currentTimeMillis();
            for (int i = 0; i < NBR_REQUESTS; i++)
            {
                InputStream in =
                    new ByteArrayInputStream(RPC_REQUEST.getBytes());
                String response = new String(server.execute(in));
                assertTrue("Response did not contain " + REQUEST_PARAM_XML,
                           response.indexOf(REQUEST_PARAM_XML) != -1);
            }
            time = System.currentTimeMillis() - time;
            System.out.println("Total time elapsed for " + NBR_REQUESTS +
                               " iterations: " + time + " milliseconds");
            System.out.println("Average time: " + (time / NBR_REQUESTS) +
                               " milliseconds");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Tests client/server RPC.
     */
    public void testRpc()
    {
        int nbrIterations = 300;
        try
        {
            throw new Exception("testRpc() not implemented");
            // TODO: Test the Server

            // TODO: Test the clients
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    protected class TestHandler
    {
        public String echo(String message)
        {
            return message;
        }
    }
}
