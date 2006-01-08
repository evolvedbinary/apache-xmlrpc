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
import java.io.InterruptedIOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.server.XmlRpcStreamServer;
import org.apache.xmlrpc.util.ThreadPool;


/** A minimal web server that exclusively handles XML-RPC requests.
 */
public class WebServer implements Runnable {
	private static final Log log = LogFactory.getLog(WebServer.class);

	private class AddressMatcher {
		private final int pattern[];
		
		AddressMatcher(String address) {
			try {
				pattern = new int[4];
				StringTokenizer st = new StringTokenizer(address, ".");
				if (st.countTokens() != 4) {
					throw new IllegalArgumentException();
				}
				for (int i = 0; i < 4; i++)	{
					String next = st.nextToken();
					if ("*".equals(next)) {
						pattern[i] = 256;
					} else {
						pattern[i] = Integer.parseInt(next);
					}
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("\"" + address
						+ "\" does not represent a valid IP address");
			}
		}
		
		boolean matches(byte[] pAddress) {
			for (int i = 0; i < 4; i++)	{
				if (pattern[i] > 255) {
					continue; // Wildcard
				}
				if (pattern[i] != pAddress[i]) {
					return false;
				}
			}
			return true;
		}
	}

	protected ServerSocket serverSocket;
	private Thread listener;
	private ThreadPool pool;
	protected final List accept = new ArrayList();
	protected final List deny = new ArrayList();
	protected final XmlRpcStreamServer server = newXmlRpcStreamServer();

	protected XmlRpcStreamServer newXmlRpcStreamServer(){
		return new ConnectionServer();
	}

	// Inputs to setupServerSocket()
	private InetAddress address;
	private int port;
	
	private boolean paranoid;
	
	static final String HTTP_11 = "HTTP/1.1";
	/** Creates a web server at the specified port number.
	 * @param pPort Port number; 0 for a random port, choosen by the
	 * operating system.
	 */
	public WebServer(int pPort) {
		this(pPort, null);
	}
	
	/** Creates a web server at the specified port number and IP address.
	 * @param pPort Port number; 0 for a random port, choosen by the
	 * operating system.
	 * @param pAddr Local IP address; null for all available IP addresses.
	 */
	public WebServer(int pPort, InetAddress pAddr) {
		address = pAddr;
		port = pPort;
	}
	
	/**
	 * Factory method to manufacture the server socket.  Useful as a
	 * hook method for subclasses to override when they desire
	 * different flavor of socket (i.e. a <code>SSLServerSocket</code>).
	 *
	 * @param pPort Port number; 0 for a random port, choosen by the operating
	 * system.
	 * @param backlog
	 * @param addr If <code>null</code>, binds to
	 * <code>INADDR_ANY</code>, meaning that all network interfaces on
	 * a multi-homed host will be listening.
	 * @exception IOException Error creating listener socket.
	 */
	protected ServerSocket createServerSocket(int pPort, int backlog, InetAddress addr)
			throws IOException {
		return new ServerSocket(pPort, backlog, addr);
	}
	
	/**
	 * Initializes this server's listener socket with the specified
	 * attributes, assuring that a socket timeout has been set.  The
	 * {@link #createServerSocket(int, int, InetAddress)} method can
	 * be overridden to change the flavor of socket used.
	 *
	 * @see #createServerSocket(int, int, InetAddress)
	 */
	private synchronized void setupServerSocket(int backlog) throws IOException {
		// Since we can't reliably set SO_REUSEADDR until JDK 1.4 is
		// the standard, try to (re-)open the server socket several
		// times.  Some OSes (Linux and Solaris, for example), hold on
		// to listener sockets for a brief period of time for security
		// reasons before relinquishing their hold.
		for (int i = 1;  ;  i++) {
			try {
				serverSocket = createServerSocket(port, backlog, address);
				// A socket timeout must be set.
				if (serverSocket.getSoTimeout() <= 0) {
					serverSocket.setSoTimeout(4096);
				}
				return;
			} catch (BindException e) {
				if (i == 10) {
					throw e;
				} else {
					long waitUntil = System.currentTimeMillis();
					for (;;) {
						long l = waitUntil - System.currentTimeMillis();
						if (l > 0) {
							try {
								Thread.sleep(l);
							} catch (InterruptedException ex) {
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Spawns a new thread which binds this server to the port it's
	 * configured to accept connections on.
	 *
	 * @see #run()
	 * @throws IOException Binding the server socket failed.
	 */
	public void start() throws IOException {
		setupServerSocket(50);
		
		// The listener reference is released upon shutdown().
		if (listener == null) {
			listener = new Thread(this, "XML-RPC Weblistener");
			// Not marked as daemon thread since run directly via main().
			listener.start();
		}
	}
	
	/**
	 * Switch client filtering on/off.
	 * @param pParanoid True to enable filtering, false otherwise.
	 * @see #acceptClient(java.lang.String)
	 * @see #denyClient(java.lang.String)
	 */
	public void setParanoid(boolean pParanoid) {
		paranoid = pParanoid;
	}
	
	/** Add an IP address to the list of accepted clients. The parameter can
	 * contain '*' as wildcard character, e.g. "192.168.*.*". You must call
	 * setParanoid(true) in order for this to have any effect.
	 * @param pAddress The IP address being enabled.
	 * @see #denyClient(java.lang.String)
	 * @see #setParanoid(boolean)
	 * @throws IllegalArgumentException Parsing the address failed.
	 */
	public void acceptClient(String pAddress) {
		accept.add(new AddressMatcher(pAddress));
	}
	
	/**
	 * Add an IP address to the list of denied clients. The parameter can
	 * contain '*' as wildcard character, e.g. "192.168.*.*". You must call
	 * setParanoid(true) in order for this to have any effect.
	 * @param pAddress The IP address being disabled.
	 * @see #acceptClient(java.lang.String)
	 * @see #setParanoid(boolean)
	 * @throws IllegalArgumentException Parsing the address failed.
	 */
	public void denyClient(String pAddress) {
		deny.add(new AddressMatcher(pAddress));
	}
	
	/**
	 * Checks incoming connections to see if they should be allowed.
	 * If not in paranoid mode, always returns true.
	 *
	 * @param s The socket to inspect.
	 * @return Whether the connection should be allowed.
	 */
	protected boolean allowConnection(Socket s) {
		if (!paranoid) {
			return true;
		}
		
		int l = deny.size();
		byte addr[] = s.getInetAddress().getAddress();
		for (int i = 0; i < l; i++) {
			AddressMatcher match = (AddressMatcher) deny.get(i);
			if (match.matches(addr))
			{
				return false;
			}
		}
		l = accept.size();
		for (int i = 0; i < l; i++) {
			AddressMatcher match = (AddressMatcher) accept.get(i);
			if (match.matches(addr)) {
				return true;
			}
		}
		return false;
	}

	protected ThreadPool.Task newTask(WebServer pServer, XmlRpcStreamServer pXmlRpcServer,
						   			  Socket pSocket) throws IOException {
		return new Connection(pServer, pXmlRpcServer, pSocket);
	}

	/**
	 * Listens for client requests until stopped.  Call {@link
	 * #start()} to invoke this method, and {@link #shutdown()} to
	 * break out of it.
	 *
	 * @throws RuntimeException Generally caused by either an
	 * <code>UnknownHostException</code> or <code>BindException</code>
	 * with the vanilla web server.
	 *
	 * @see #start()
	 * @see #shutdown()
	 */
	public void run() {
		pool = new ThreadPool(server.getMaxThreads(), "XML-RPC");
		try {
			while (listener != null) {
				try {
					Socket socket = serverSocket.accept();
					try {
						socket.setTcpNoDelay(true);
					} catch (SocketException socketOptEx) {
						log(socketOptEx);
					}
					
					try {
						if (allowConnection(socket)) {
							final ThreadPool.Task task = newTask(this, server, socket);
							if (pool.startTask(task)) {
								socket = null;
							} else {
								log("Maximum load of " + pool.getMaxThreads()
									+ " exceeded, rejecting client");
							}
						}
					} finally {
						if (socket != null) { try { socket.close(); } catch (Throwable ignore) {} }
					}
				} catch (InterruptedIOException checkState) {
					// Timeout while waiting for a client (from
					// SO_TIMEOUT)...try again if still listening.
				} catch (Throwable t) {
					log(t);
				}
			}
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					log(e);
				}
			}
			
			// Shutdown our Runner-based threads
			pool.shutdown();
		}
	}
	
	/**
	 * Stop listening on the server port.  Shutting down our {@link
	 * #listener} effectively breaks it out of its {@link #run()}
	 * loop.
	 *
	 * @see #run()
	 */
	public synchronized void shutdown() {
		// Stop accepting client connections
		if (listener != null) {
			Thread l = listener;
			listener = null;
			l.interrupt();
			pool.shutdown();
		}
	}
	
	/** Returns the port, on which the web server is running.
	 * This method may be invoked after {@link #start()} only.
	 * @return Servers port number
	 */
	public int getPort() { return serverSocket.getLocalPort(); }

	/** Logs an error.
	 * @param pError The error being logged.
	 */
	public void log(Throwable pError) {
		log.error(pError.getMessage(), pError);
	}

	/** Logs a message.
	 * @param pMessage The being logged.
	 */
	public synchronized void log(String pMessage) {
		log.error(pMessage);
	}

	/** Returns the {@link org.apache.xmlrpc.server.XmlRpcServer}.
	 * @return The server object.
	 */
	public XmlRpcStreamServer getXmlRpcServer() {
		return server;
	}
}