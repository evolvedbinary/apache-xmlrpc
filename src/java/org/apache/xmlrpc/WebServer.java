package org.apache.xmlrpc;

/*
 * The Apache Software License, Version 1.1
 *
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
 * 4. The names "XML-RPC" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A minimal web server that exclusively handles XML-RPC requests.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 */
public class WebServer 
    implements Runnable
{
    protected XmlRpcServer xmlrpc;
    protected ServerSocket serverSocket;
    protected int port;
    protected Thread listener;
    protected boolean paranoid;
    protected Vector accept, deny;
    protected Stack threadpool;
    protected ThreadGroup runners;

    protected static final byte[] ctype = "Content-Type: text/xml\r\n".getBytes();
    protected static final byte[] clength = "Content-Length: ".getBytes();
    protected static final byte[] newline = "\r\n".getBytes();
    protected static final byte[] doubleNewline = "\r\n\r\n".getBytes();
    protected static final byte[] conkeep = "Connection: Keep-Alive\r\n".getBytes();
    protected static final byte[] conclose = "Connection: close\r\n".getBytes();
    protected static final byte[] ok = " 200 OK\r\n".getBytes();
    protected static final byte[] server = "Server: Apache XML-RPC 1.0\r\n".getBytes();

    private static final String HTTP_11 = "HTTP/1.1";
    private static final String STAR = "*";

    /**
     * This <em>can</em> be called from command line, but you'll have to edit and recompile
     * to change the server port or handler objects. By default, it sets up the following responders:
     * <ul><li> A java.lang.String object
     * <li> The java.lang.Math class (making its static methods callable via XML-RPC)
     * <li> An Echo handler that returns the argument array
     * </ul>
     */
    public static void main (String args[])
    {
        System.err.println ("Usage: java " + WebServer.class.getName() +
                            " <port>");
        int p = 8080;
        if (args.length > 0)
            try
            {
                p = Integer.parseInt (args[0]);
            }
            catch (NumberFormatException nfx)
            {
                System.err.println ("Error parsing port number: "+args[0]);
            }
        // XmlRpc.setDebug (true);
        XmlRpc.setKeepAlive (true);
        try
        {
            WebServer webserver = new WebServer(p);
            // webserver.setParanoid (true);
            // webserver.acceptClient ("192.168.*.*");
            webserver.addHandler("string", "Welcome to XML-RPC!");
            webserver.addHandler("math", Math.class);
            webserver.addHandler("auth", new AuthDemo());
            webserver.addHandler("$default", new Echo());
            // XmlRpcClients can be used as Proxies in XmlRpcServers which is a cool feature for applets.
            webserver.addHandler("mttf", new XmlRpcClient("http://www.mailtothefuture.com:80/RPC2"));
            System.err.println("started web server on port "+p);
        }
        catch (IOException x)
        {
            System.err.println("Error creating web server: "+x);
        }
    }

    /**
      * Creates a Web server at the specified port number.
      */
    public WebServer(int port) 
        throws IOException
    {
        this(port, null);
    }

    /**
      * Creates a Web server at the specified port number and IP address.
      */
    public WebServer(int port, InetAddress add) 
        throws IOException
    {
        this.port = port;
        xmlrpc = new XmlRpcServer();
        accept = new Vector();
        deny = new Vector();
        threadpool = new Stack();
        runners = new ThreadGroup("XML-RPC Runner");

        try
        {
            setupServerSocket(port, 50, add);
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage());
        }

        start();
    }

    /**
     * Factory method to manufacture the server socket.  Useful as a
     * hook method for subclasses to override when they desire
     * different flavor of socket (i.e. a <code>SSLServerSocket</code>).
     *
     * @exception Exception Error creating listener socket.
     */
    protected ServerSocket createServerSocket(int port, int backlog, InetAddress add)
        throws Exception
    {
        return new ServerSocket(port, backlog, add);
    }

    /**
     * Initializes this server's listener socket with the specified
     * attributes.  The <code>createServerSocket()</code> method can
     * be overridden to change the flavor of socket used.
     *
     * @see #createServerSocket(int port, int backlog, InetAddress add)
     */
    public void setupServerSocket(int port, int backlog, InetAddress add)
        throws Exception
    {
        serverSocket = createServerSocket(port, backlog, add);
        serverSocket.setSoTimeout(4096);
    }

    public void start()
    {
        listener = new Thread(this, "XML-RPC Weblistener");
        listener.start();
    }

    /**
      * Register a handler object with this name. Methods of this objects will be
      * callable over XML-RPC as "name.method".
      */
    public void addHandler(String name, Object target)
    {
        xmlrpc.addHandler(name, target);
    }

    /**
      * Remove a handler object that was previously registered with this server.
      */
    public void removeHandler(String name)
    {
        xmlrpc.removeHandler(name);
    }

    /**
      * Switch client filtering on/off.
      * @see acceptClient(java.lang.String)
      * @see denyClient(java.lang.String)
      */
    public void setParanoid(boolean p)
    {
        paranoid = p;
    }

    /**
      * Add an IP address to the list of accepted clients. The parameter can contain '*' as wildcard
      * character, e.g. "192.168.*.*". You must call setParanoid(true) in order for this to have any
      * effect.
      *
      * @see denyClient(java.lang.String)
      * @see setParanoid(boolean)
      */
    public void acceptClient(String address)
        throws IllegalArgumentException
    {
        try
        {
            AddressMatcher m = new AddressMatcher(address);
            accept.addElement(m);
        }
        catch (Exception x)
        {
            throw new IllegalArgumentException("\"" + 
                address + 
                "\" does not represent a valid IP address");
        }
    }

    /**
      * Add an IP address to the list of denied clients. The parameter can contain '*' as wildcard
      * character, e.g. "192.168.*.*". You must call setParanoid(true) in order for this to have any
      * effect.
      *
      * @see acceptClient(java.lang.String)
      * @see setParanoid(boolean)
      */
    public void denyClient(String address) throws IllegalArgumentException
    {
        try
        {
            AddressMatcher m = new AddressMatcher(address);
            deny.addElement(m);
        }
        catch (Exception x)
        {
            throw new IllegalArgumentException("\"" + 
                address + 
                "\" does not represent a valid IP address");
        }
    }

    protected boolean checkSocket(Socket s)
    {
        int l = deny.size();
        byte address[] = s.getInetAddress().getAddress();
        for (int i = 0; i < l; i++)
        {
            AddressMatcher match = (AddressMatcher)deny.elementAt(i);
            if (match.matches(address))
            {
                return false;
            }
        }
        l = accept.size();
        for (int i = 0; i < l; i++)
        {
            AddressMatcher match = (AddressMatcher)accept.elementAt(i);
            if (match.matches(address))
            {
                return true;
            }
        }
        return false;
    }

    /**
      * Listens for client requests until stopped.
      */
    public void run()
    {
        try
        {
            while (listener != null)
            {
                try
                {
                    Socket socket = serverSocket.accept();
                    if (!paranoid || checkSocket(socket))
                    {
                        Runner runner = getRunner();
                        runner.handle (socket);
                    }
                    else
                    {
                        socket.close();
                    }
                }
                catch (InterruptedIOException checkState)
                {
                    // Timeout while waiting for a client (from
                    // SO_TIMEOUT)...try again if still listening.
                }
                catch (Exception ex)
                {
                    System.err.println(
                            "Exception in XML-RPC listener loop (" +
                            ex + ").");
                }
                catch (Error err)
                {
                    System.err.println(
                            "Error in XML-RPC listener loop (" + err +
                            ").");
                }
            }
        }
        catch (Exception exception)
        {
            System.err.println( "Error accepting XML-RPC connections (" +
                    exception + ").");
        }
        finally
        {
            System.err.println("Closing XML-RPC server socket.");
            try
            {
                serverSocket.close();
                serverSocket = null;
            }
            catch (IOException ignore)
            {
            }
        }
    }

    /**
      * Stop listening on the server port.
      */
    public void shutdown()
    {
        if (listener != null)
        {
            Thread l = listener;
            listener = null;
            l.interrupt ();
        }
    }

    protected Runner getRunner()
    {
        try
        {
            return (Runner)threadpool.pop();
        }
        catch (EmptyStackException empty)
        {
            if (runners.activeCount () > 255)
            {
                throw new RuntimeException ("System overload");
            }
            return new Runner();
        }
    }

    void releaseRunner (Runner runner)
    {
        threadpool.push (runner);
    }

    class Runner implements Runnable
    {
        Thread thread;
        Connection con;
        int count;

        public synchronized void handle(Socket socket)
            throws IOException
        {
            con = new Connection(socket);
            count = 0;
            if (thread == null || !thread.isAlive())
            {
                thread = new Thread(runners, this);
                thread.start();
            }
            else
            {
                this.notify();
            }
        }

        public void run()
        {
            while (Thread.currentThread () == thread)
            {
                con.run ();
                count++;
                con = null;

                if (count > 200 || threadpool.size() > 20)
                {
                    return;
                }
                synchronized(this)
                {
                    releaseRunner(this);
                    try
                    {
                        this.wait();
                    }
                    catch (InterruptedException ir)
                    {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    class Connection implements Runnable
    {
        private Socket socket;
        private BufferedInputStream input;
        private BufferedOutputStream output;
        private String user, password;
        byte[] buffer;

        public Connection (Socket socket) throws IOException
        {
            // set read timeout to 30 seconds
            socket.setSoTimeout (30000);

            this.socket = socket;
            input = new BufferedInputStream(socket.getInputStream());
            output = new BufferedOutputStream(socket.getOutputStream());
        }

        public void run()
        {
            try
            {
                boolean keepalive = false;

                do
                {
                    // reset user authentication
                    user = password = null;
                    String line = readLine();
                    // Netscape sends an extra \n\r after bodypart, swallow it
                    if (line != null && line.length() == 0)
                    {
                        line = readLine();
                    }
                    if (XmlRpc.debug)
                    {
                        System.err.println (line);
                    }
                    int contentLength = -1;

                    // tokenize first line of HTTP request
                    StringTokenizer tokens = new StringTokenizer(line);
                    String method = tokens.nextToken();
                    String uri = tokens.nextToken();
                    String httpversion = tokens.nextToken();
                    keepalive = XmlRpc.getKeepAlive() && HTTP_11.equals(httpversion);
                    do
                    {
                        line = readLine();
                        if (line != null)
                        {
                            if (XmlRpc.debug)
                            {
                                System.err.println(line);
                            }
                            String lineLower = line.toLowerCase();
                            if (lineLower.startsWith("content-length:"))
                            {
                                contentLength = Integer.parseInt(
                                        line.substring(15).trim());
                            }
                            if (lineLower.startsWith("connection:"))
                            {
                                keepalive = XmlRpc.getKeepAlive() &&
                                        lineLower.indexOf ("keep-alive")
                                        > -1;
                            }
                            if (lineLower.startsWith("authorization: basic "))
                            {
                                parseAuth (line);
                            }
                        }
                    }
                    while (line != null && line.length() != 0);

                    if ("POST".equalsIgnoreCase (method))
                    {
                        ServerInputStream sin =
                                new ServerInputStream (input,
                                contentLength);
                        byte result[] =
                                xmlrpc.execute (sin, user, password);
                        output.write(httpversion.getBytes());
                        output.write(ok);
                        output.write(server);
                        if (keepalive)
                        {
                            output.write(conkeep);
                        }
                        else
                        {
                            output.write (conclose);
                        }
                        output.write(ctype);
                        output.write(clength);
                        output.write(Integer.toString(
                                result.length).getBytes());
                        output.write(doubleNewline);
                        output.write(result);
                        output.flush();
                    }
                    else
                    {
                        output.write(httpversion.getBytes());
                        output.write(" 400 Bad Request\r\n".getBytes());
                        output.write(server);
                        output.write("\r\n".getBytes());
                        output.write(("Method "+method +
                                " not implemented (try POST)").getBytes());
                        output.flush();
                        keepalive = false;
                    }
                }
                while (keepalive);
            }
            catch (Exception exception)
            {
                if (XmlRpc.debug)
                {
                    System.err.println (exception);
                    exception.printStackTrace ();
                }
            }
            finally
            {
                try
                {
                    if (socket != null)
                    {
                        socket.close();
                    }
                }
                catch (IOException ignore)
                {
                }
            }
        }

        private String readLine() throws IOException
        {
            if (buffer == null)
            {
                buffer = new byte[512];
            }
            int next;
            int count = 0;
            for (;;)
            {
                next = input.read();
                if (next < 0 || next == '\n')
                {
                    break;
                }
                if (next != '\r')
                {
                    buffer[count++] = (byte) next;
                }
                if (count >= 512)
                {
                    throw new IOException ("HTTP Header too long");
                }
            }
            return new String (buffer, 0, count);
        }

        private void parseAuth(String line)
        {
            try
            {
                byte[] c =
                        Base64.decode (line.substring(21).getBytes());
                String str = new String (c);
                int col = str.indexOf (':');
                user = str.substring (0, col);
                password = str.substring (col + 1);
            }
            catch (Throwable ignore)
            {
            }
        }
    }

    class AddressMatcher
    {
        int pattern[];
        
        public AddressMatcher (String address) throws Exception
        {
            pattern = new int[4];
            StringTokenizer st = new StringTokenizer(address, ".");
            if (st.countTokens() != 4)
            {
                throw new Exception ("\"" + 
                    address + 
                    "\" does not represent a valid IP address");
            }
            for (int i = 0; i < 4; i++)
            {
                String next = st.nextToken();
                if (STAR.equals(next))
                {
                    pattern[i] = 256;
                }
                else
                {
                    pattern[i] = (byte) Integer.parseInt(next);
                }
            }
        }

        public boolean matches (byte address[])
        {
            for (int i = 0; i < 4; i++)
            {
                if (pattern[i] > 255)// wildcard
                {
                    continue;
                }
                if (pattern[i] != address[i])
                {
                    return false;
                }
            }
            return true;
        }
    }
}
