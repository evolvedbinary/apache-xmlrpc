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
package org.apache.xmlrpc.common;

import java.util.ArrayList;
import java.util.List;


/** A factory for {@link XmlRpcWorker} instances.
 */
public abstract class XmlRpcWorkerFactory {
	private final XmlRpcWorker singleton = newWorker();
	private final XmlRpcController controller;
	private final List pool = new ArrayList();
	private int numThreads;

	/** Creates a new instance.
	 * @param pController The client controlling the factory.
	 */
	public XmlRpcWorkerFactory(XmlRpcController pController) {
		controller = pController;
	}

	/** Creates a new worker instance.
	 * @return New instance of {@link XmlRpcWorker}.
	 */
	protected abstract XmlRpcWorker newWorker();

	/** Returns the factory controller.
	 * @return The controller, an instance of
	 * {@link org.apache.xmlrpc.client.XmlRpcClient}, or
	 * {@link org.apache.xmlrpc.server.XmlRpcServer}.
	 */
	public XmlRpcController getController() {
		return controller;
	}

	/** Returns a worker for synchronous processing.
	 * @return An instance of {@link XmlRpcWorker}, which is ready
	 * for use.
	 * @throws XmlRpcLoadException The clients maximum number of concurrent
	 * threads is exceeded.
	 */
	public synchronized XmlRpcWorker getWorker() throws XmlRpcLoadException {
		int max = controller.getMaxThreads();
		if (max > 0  &&  numThreads == max) {
			throw new XmlRpcLoadException("Maximum number of concurrent requests exceeded: " + max);
		}
		if (max == 0) {
			return singleton;
		}
        ++numThreads;
		if (pool.size() == 0) {
			return newWorker();
		} else {
			return (XmlRpcWorker) pool.remove(pool.size() - 1);
		}
	}

	/** Called, when the worker did its job. Frees resources and
	 * decrements the number of concurrent requests.
	 * @param pWorker The worker being released.
	 */
	public synchronized void releaseWorker(XmlRpcWorker pWorker) {
		--numThreads;
		int max = controller.getMaxThreads();
		if (pWorker == singleton) {
			// Do nothing, it's the singleton
		} else {
			if (pool.size() < max) {
				pool.add(pWorker);
			}
		}
	}

	/** Returns the number of currently running requests.
	 * @return Current number of concurrent requests.
	 */
	public synchronized int getCurrentRequests() {
		return numThreads;
	}
}
