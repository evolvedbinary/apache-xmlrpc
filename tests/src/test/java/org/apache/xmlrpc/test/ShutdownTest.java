package org.apache.xmlrpc.test;

import java.net.ConnectException;
import java.net.URL;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;


/**
 * Tests the web servers shutdown method.
 */
public class ShutdownTest extends TestCase {
    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            new ShutdownTest().testShutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Dummy handler for running the test.
     */
    public static class Adder {
        /** Returns the sum of p1 and p2.
         */
        public int add(int p1, int p2) {
            return p1 + p2;
        }
    }

    private WebServer setupServer() throws Exception {
        WebServer server = new WebServer(0);
        PropertyHandlerMapping mapping = new PropertyHandlerMapping();
        mapping.addHandler("Adder", Adder.class);
        server.getXmlRpcServer().setHandlerMapping(mapping);
        XmlRpcServerConfigImpl config = new XmlRpcServerConfigImpl();
        config.setEnabledForExtensions(true);
        config.setKeepAliveEnabled(true);
        server.getXmlRpcServer().setConfig(config);
        server.start();
        return server;
    }

    private class Runner extends Thread {
        private boolean connectExceptionSeen;
        private boolean successSeen;
        private final int port;
        Runner(int pPort) {
            port = pPort;
        }
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    XmlRpcClient client = new XmlRpcClient();
                    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
                    config.setServerURL(new URL("http://127.0.0.1:" + port  + "/"));
                    client.setConfig(config);
                    Object[] params = new Object[] {
                            new Integer(3), new Integer(5)
                    };
                    Integer result = (Integer) client.execute("Adder.add", params);
                    assertEquals(8, result.intValue());
                    successSeen = true;
                    Thread.sleep(200);
                } catch (XmlRpcException e) {
                    Assert.assertTrue(e.getCause() != null  &&  e.getCause() instanceof ConnectException);
                    connectExceptionSeen = true;
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        boolean isConnectExceptionSeen() { return connectExceptionSeen; }
        boolean isSuccessSeen() { return successSeen; }
    }

    /** Tests the web servers shutdown method.
     */
    public void testShutdown() throws Exception {
        final WebServer server = setupServer();
        final int port = server.getPort();
        Runner runner = new Runner(port);
        runner.start();
        Thread.sleep(700);
        server.shutdown();
        runner.join();
        assertTrue(runner.isSuccessSeen());
        assertTrue(runner.isConnectExceptionSeen());
    }
}
