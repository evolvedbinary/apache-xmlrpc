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
import java.util.Vector;
import java.net.URL;
import java.io.*;

public class AsyncClient implements AsyncCallback {

    // the xml-rpc client
    XmlRpcClient client;


    /**
     * main method
     */
    public static void main (String args[]) throws Exception {
	if (args.length < 1) {
	    System.err.println ("Usage: java AsyncClient URL");
	} else {
	    AsyncClient client = new AsyncClient (args[0]);
	    client.run ();
	}
    }

    /** 
     *  Constructor
     */
    public AsyncClient (String url) throws Exception {
	client = new XmlRpcClient (url);
    }

    /** 
     * Read from standard input and make an asynchronous XML-RPC call.
     */
    public void run () throws IOException {
	String token = null;
	BufferedReader d = new BufferedReader(
		new InputStreamReader(System.in));
	System.err.println ("Enter some text and hit <return>");
        System.err.println ("Input will be sent to "+client.getURL ());
	while ((token = d.readLine()) != null) {
            System.err.println ("sending: "+token);
	    Vector v = new Vector ();
	    v.add (token);
            client.executeAsync ("echo", v, this);
	}
    }


    /**
     * Method defined in AsyncCallback interface. Called when the 
     * XML-RPC call was processed successfully.
     */
    public void handleResult (Object result, URL url, String method) {
	System.err.println ("received: "+result);
    }

    /**
     * Method defined in AsyncCallback interface. Called if 
     * something went wrong during XML-RPC call.
     */
    public void handleError (Exception exception, URL url, String method) {
	System.err.println ("Error: "+exception);
    }

}

