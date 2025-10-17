/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.xmlrpc.serializer;

import org.xml.sax.SAXException;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class ExtendedCharSetXMLWriter extends ExtendedXMLWriterImpl {
    private CharsetEncoder charsetEncoder;

    public void startDocument() throws SAXException {
        String enc = getEncoding();
        if (enc == null) {
            enc = "UTF-8";
        }
        Charset charSet = Charset.forName(enc);
        if (charSet.canEncode()) {
            charsetEncoder = charSet.newEncoder();
        }
        super.startDocument();
    }

    public boolean canEncode(char c) {
        return (charsetEncoder == null) ? super.canEncode(c) : charsetEncoder.canEncode(c);
    }
}
