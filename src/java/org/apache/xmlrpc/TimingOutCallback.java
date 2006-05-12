package org.apache.xmlrpc;

import java.net.URL;


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
    public static class TimeoutException extends XmlRpcException {
        private static final long serialVersionUID = 4875266372372105081L;

        public TimeoutException(int code, String message) {
            super(code, message);
        }
    }

    private final long timeout;
    private Object result;
    private Exception exception;
    private boolean responseSeen;

    /** Waits the specified number of milliseconds for a response.
     */
    public TimingOutCallback(long pTimeout) {
        timeout = pTimeout;
    }

    public synchronized void handleError(Exception pException, URL pUrl, String pMethod) {
        responseSeen = true;
        exception = pException;
        notify();
    }

    public synchronized void handleResult(Object pResult, URL pUrl, String pMethod) {
        responseSeen = true;
        result = pResult;
        notify();
    }

    /** Called to wait for the response.
     * @throws InterruptedException The thread was interrupted.
     * @throws TimeoutException No response was received after waiting the specified time.
     * @throws Exception An error was returned by the server.
     */
    public synchronized Object waitForResponse() throws Exception {
        wait(timeout);
        if (!responseSeen) {
            throw new TimeoutException(0, "No response after waiting for " + timeout + " milliseconds.");
        }
        if (exception != null) {
            throw exception;
        }
        return result;
    }
}
