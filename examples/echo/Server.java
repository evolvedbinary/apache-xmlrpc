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


import org.apache.xmlrpc.*;

public class Server {

    // the xml-rpc webserver
    static WebServer server;


    /**
     * main method
     */
    public static void main (String args[]) throws Exception {
	if (args.length < 1) {
	    System.err.println ("Usage: java Server port");
	    System.exit (1);
	}
	int port = 0;
	try {
	    port = Integer.parseInt (args[0]);
	} catch (NumberFormatException nonumber) {
	    System.err.println ("Invalid port number: "+args[0]);
	    System.exit (1);
	}

	server = new WebServer (port);
	// add an instance of this class as default handler
	server.addHandler ("$default", new Server());
	server.start ();
	System.err.println ("Listening on port "+port);
    }


    /**
     *  This is method that is invoked via XML-RPC. The server looks
     *  up the method via Java Reflection API. The alternative approach
     *  would have been to implement the XmlRpcHandler interface and
     *  define an execute() method to handle incoming calls.
     */
    public String echo (String input) {
	return input;
    }

}

