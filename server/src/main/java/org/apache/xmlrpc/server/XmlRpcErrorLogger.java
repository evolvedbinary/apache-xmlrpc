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
package org.apache.xmlrpc.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Instances of this class can be used to customize the servers
 * error logging.
 */
public class XmlRpcErrorLogger {
    private static final Log log = LogFactory.getLog(XmlRpcErrorLogger.class);

    /**
     * Called to log the given error.
     * @param pMessage the message
     * @param pThrowable the cause
     */
    public void log(String pMessage, Throwable pThrowable) {
        log.error(pMessage, pThrowable);
    }

    /**
     * Called to log the given error message.
     * @param pMessage the message
     */
    public void log(String pMessage) {
        log.error(pMessage);
    }
}
