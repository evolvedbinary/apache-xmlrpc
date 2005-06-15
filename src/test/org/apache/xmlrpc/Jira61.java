package org.apache.xmlrpc;

import java.io.IOException;
import java.util.Vector;

import junit.framework.TestCase;


public class Jira61 extends TestCase {
    public class TestLoopBack {
        /**
         * @throws XmlRpcException
         */
        public void createItem() throws XmlRpcException {
            throw new XmlRpcException(7,"Not found");
        }
    }

    
    public void testException() throws IOException {
        WebServer webserver = new WebServer(0);
        TestLoopBack tt = new TestLoopBack();
        webserver.addHandler ("examples", tt);
        webserver.start();
        XmlRpcClient xmlrpc = new XmlRpcClient("http://localhost:" + webserver.serverSocket.getLocalPort() + "/RPC2");
        Vector params = new Vector ();
        boolean ok = false;
        try {
        	xmlrpc.execute("examples.createItem", params);
        } catch (XmlRpcException e) {
        	ok = true;
        }
        assertTrue(ok);
    }
}
