package org.apache.xmlrpc.applet;

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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpc;

/**
 * Tests XmlRpc Applet client.  Borrows heavily from ClientServerRpcTest
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:rhoegg@isisnetworks.net">Ryan Hoegg</a>
 * @version $Id$
 */
public class AppletTest
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

    private static final int SERVER_PORT = 8081;

    /**
     * The value to use in our request parameter.
     */
    private static final String REQUEST_PARAM_VALUE = "foobar";

    /**
     * The number of calls to batch in the multicall.
     */
    private static final int NUM_MULTICALLS = 10;

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

    private InetAddress localhost = null;

    private WebServer webServer;

    private SimpleXmlRpcClient client;

    /**
     * Constructor
     */
    public AppletTest(String testName) 
    {
        super(testName);

        XmlRpc.setDebug(true);
        try
        {
            XmlRpc.setDriver(SAX_DRIVER);
        }
        catch (ClassNotFoundException e)
        {
            fail(e.toString());
        }

        try
        {
            // localhost will be a random network interface on a
            // multi-homed host.
            localhost = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e)
        {
            fail(e.toString());
        }
    }

    /**
     * Return the Test
     */
    public static Test suite() 
    {
        return new TestSuite(AppletTest.class);
    }

    /**
     * Setup the server and clients.
     */
    public void setUp() 
    {
        try
        {
            // WebServer (contains its own XmlRpcServer instance)
            webServer = new WebServer(SERVER_PORT, localhost);
            webServer.addHandler(HANDLER_NAME, new TestHandler());
            webServer.start();
            
            // XML-RPC client(s)
            try
            {
                String hostName = localhost.getHostName();
                client = new SimpleXmlRpcClient(hostName, SERVER_PORT);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                fail(e.toString());
            }
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
            fail(e.toString());
        }
    }
   
    /**
     * Tear down the test.
     */
    public void tearDown() 
    {
        try
        {
            webServer.shutdown();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Tests client/server RPC (via {@link WebServer}).
     */
    public void testRpc()
    {
        try
        {
            // Test the web server (which also tests the rpc server)
            // by connecting via the clients
            Vector params = new Vector();
            params.add(REQUEST_PARAM_VALUE);
            Object response = client.execute(HANDLER_NAME + ".echo", params);
            assertEquals(REQUEST_PARAM_VALUE, response);
            //params.removeAllElements();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Added as a result of Bug 8397
     */
    public void testEntityEncoding()
    {
        String problemData = "E < mc^2";
        Vector params = new Vector();
        params.add(problemData);
        try
        {
            Object response = client.execute(HANDLER_NAME + ".echo", params);
            assertEquals(problemData, response);
        }
        catch (IOException e)
        {
            fail (e.getMessage());
        }
        catch (XmlRpcException e)
        {
            fail (e.getMessage());
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
