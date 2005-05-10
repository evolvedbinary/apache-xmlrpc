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


package org.apache.xmlrpc;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests XmlWriter.
 *
 * @author Daniel L. Rall
 * @version $Id$
 */
public class XmlWriterTest
    extends TestCase 
{
    /**
     * Constructor
     */
    public XmlWriterTest(String testName) 
    {
        super(testName);
    }

    /**
     * Return the Test
     */
    public static Test suite() 
    {
        return new TestSuite(XmlWriterTest.class);
    }

    /**
     * Setup the test.
     */
    public void setUp() 
    {
        XmlRpc.setDebug(true);
    }
   
    /**
     * Tear down the test.
     */
    public void tearDown() 
    {
        XmlRpc.setDebug(false);
    }

    public void testWriter()
        throws Exception
    {
        try
        {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            XmlWriter writer = new XmlWriter(buffer, XmlWriter.ISO8859_1);
            assertTrue(writer.getEncoding().equals(XmlRpc.encoding));

            String foobar = "foobar";
            writer.writeObject(foobar);
            writer.flush();
            //System.err.println("buffer=" + new String(buffer.toByteArray()));
            String postProlog = "<value>" + foobar + "</value>";
            assertTrue(buffer.toString().endsWith(postProlog));

            Integer thirtySeven = new Integer(37);
            writer.writeObject(thirtySeven);
            writer.flush();
            postProlog += "<value><int>" + thirtySeven + "</int></value>";
            assertTrue(buffer.toString().endsWith(postProlog));

            Object[] array = { foobar, thirtySeven };
            writer.writeObject(array);
            writer.flush();
            postProlog += "<value><array><data>";
            postProlog += "<value>" + foobar + "</value>";
            postProlog += "<value><int>" + thirtySeven + "</int></value>";
            postProlog += "</data></array></value>";
            assertTrue(buffer.toString().endsWith(postProlog));

            Hashtable map = new Hashtable();
            map.put(foobar, thirtySeven);
            writer.writeObject(map);
            writer.flush();
            postProlog += "<value><struct><member>";
            postProlog += "<name>" + foobar + "</name>";
            postProlog += "<value><int>" + thirtySeven + "</int></value>";
            postProlog += "</member></struct></value>";
            assertTrue(buffer.toString().endsWith(postProlog));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
