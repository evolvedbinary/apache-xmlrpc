package org.apache.xmlrpc.client;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;


/**
 * <p>A callback object that can wait up to a specified amount
 * of time for the XML-RPC response. Suggested use is as follows:
 * </p>
 * <pre>
 *   // Wait for 10 seconds.
 *   TimingOutCallback callback = new TimingOutCallback(10 * 1000);
 *   XmlRpcClient client = new XmlRpcClient(url);
 *   client.executeAsync(methodName, aVector, callback);
 *   try {
 *       return callback.waitForResponse();
 *   } catch (TimeoutException e) {
 *       System.out.println("No response from server.");
 *   } catch (Exception e) {
 *       System.out.println("Server returned an error message.");
 *   }
 * </pre>
 */
public class TimingOutCallback implements AsyncCallback {
    /** This exception is thrown, if the request times out.
     */
    public static class TimeoutException extends XmlRpcException {
        private static final long serialVersionUID = 4875266372372105081L;

        /** Creates a new instance with the given error code and
         * error message.
         */
        public TimeoutException(int pCode, String message) {
            super(pCode, message);
        }
    }

    private final long timeout;
    private Object result;
    private Throwable error;
    private boolean responseSeen;

    /** Waits the specified number of milliseconds for a response.
     */
    public TimingOutCallback(long pTimeout) {
        timeout = pTimeout;
    }

    /** Called to wait for the response.
     * @throws InterruptedException The thread was interrupted.
     * @throws TimeoutException No response was received after waiting the specified time.
     * @throws Throwable An error was returned by the server.
     */
    public synchronized Object waitForResponse() throws Throwable {
        if (!responseSeen) {
            wait(timeout);
            if (!responseSeen) {
                throw new TimeoutException(0, "No response after waiting for " + timeout + " milliseconds.");
            }
        }
        if (error != null) {
            throw error;
        }
        return result;
    }

    public synchronized void handleError(XmlRpcRequest pRequest, Throwable pError) {
        responseSeen = true;
        error = pError;
        notify();
    }

    public synchronized void handleResult(XmlRpcRequest pRequest, Object pResult) {
        responseSeen = true;
        result = pResult;
        notify();
    }
}
