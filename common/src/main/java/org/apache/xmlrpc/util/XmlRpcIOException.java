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
package org.apache.xmlrpc.util;

import java.io.IOException;


/** This is a subclass of {@link IOException}, which
 * allows to attach a linked exception. Throwing this
 * particular instance of {@link IOException} allows
 * to catch it and throw the linked exception instead.
 */
public class XmlRpcIOException extends IOException {
	private static final long serialVersionUID = -7704704099502077919L;
	private final Throwable linkedException;

	/** Creates a new instance of {@link XmlRpcIOException}
	 * with the given cause.
	 * @param t the cause
	 */
	public XmlRpcIOException(Throwable t) {
		super(t.getMessage());
		linkedException = t;
	}

	/** Returns the linked exception, which is the actual
	 * cause for this exception.
	 * @return the linked exception
	 */
	public Throwable getLinkedException() {
		return linkedException;
	}
}
