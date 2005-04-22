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
            assertEquals(XmlRpc.encoding, writer.getEncoding());
            String foobar = "foobar";
            writer.writeObject(foobar);
            writer.flush();
            //System.err.println("buffer=" + new String(buffer.toByteArray()));
            String postProlog = "<value>" + foobar + "</value>";
            assertTrue(buffer.toString().endsWith(postProlog));
            int thirtySeven = 37;
            writer.writeObject(new Integer(37));
            writer.flush();
            postProlog += "<value><int>" + thirtySeven + "</int></value>";
            assertTrue(buffer.toString().endsWith(postProlog));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
