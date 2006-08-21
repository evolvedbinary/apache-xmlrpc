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
package org.apache.xmlrpc.util;

import java.util.ArrayList;
import java.util.List;


/** Simple thread pool. A task is executed by obtaining a thread from
 * the pool
 */
public class ThreadPool {
	/** The thread pool contains instances of {@link ThreadPool.Task}.
	 */
	public interface Task {
		/** Performs the task.
		 * @throws Throwable The task failed, and the worker thread won't be used again.
		 */
		void run() throws Throwable;
	}

	private class MyThread extends Thread {
		private boolean shuttingDown;
		private int numTasks;
		private Task task;
		MyThread() {
			super(threadGroup, threadGroup.getName() + "-" + num++);
			setDaemon(true);
		}
		synchronized void shutdown() {
			shuttingDown = true;
			notify();
		}
		synchronized boolean isShuttingDown() { return shuttingDown; }
		synchronized void waitForNotification() {
			if (getTask() != null) { return; }
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		synchronized int getNumTasks() { return numTasks; }
		synchronized Task getTask() { return task; }
		synchronized void setTask(Task pTask) {
			task = pTask;
			if (task != null) {
				notify();
			}
		}
		synchronized void runTask() {
			Task tsk = getTask();
			if (tsk == null) {
				return;
			}
			++numTasks;
			Throwable t;
			try {
				tsk.run();
				t = null;
			} catch (Throwable th) {
				t = th;
			}
			if (t == null) {
				repool(this);
			} else {
				discard(this);
			}
		}
		public void run() {
			while (!isShuttingDown()) {
				if (getTask() == null) {
					waitForNotification();
				} else {
					runTask();
				}
			}
		}
	}

	private final ThreadGroup threadGroup;
	private final int maxSize;
	private final List waitingThreads = new ArrayList();
	private final List runningThreads = new ArrayList();
	private final List waitingTasks = new ArrayList();
	private int num;


	/** Creates a new instance.
	 * @param pMaxSize Maximum number of concurrent threads.
	 * @param pName Thread group name.
	 */
	public ThreadPool(int pMaxSize, String pName) {
		maxSize = pMaxSize;
		threadGroup = new ThreadGroup(pName);
	}

	synchronized void discard(MyThread pThread) {
		pThread.shutdown();
		if (!runningThreads.remove(pThread)) {
			throw new IllegalStateException("The list of running threads didn't contain the thread " + pThread.getName());
		}
	}

	synchronized void repool(MyThread pThread) {
		if (maxSize != 0  &&  (runningThreads.size() + waitingThreads.size()) > maxSize) {
			discard(pThread);
		} else if (waitingTasks.size() > 0) {
			pThread.setTask((Task) waitingTasks.remove(0));
		} else {
			pThread.setTask(null);
			if (!runningThreads.remove(pThread)) {
				throw new IllegalStateException("The list of running threads didn't contain the thread " + pThread.getName());
			}
			waitingThreads.add(pThread);
		}
	}

	/** Starts a task immediately.
	 * @param pTask The task being started.
	 * @return True, if the task could be started immediately. False, if
	 * the maxmimum number of concurrent tasks was exceeded. If so, you
	 * might consider to use the {@link #addTask(Task)} method instead.
	 */
	public synchronized boolean startTask(Task pTask) {
		if (maxSize != 0  &&  runningThreads.size() > maxSize) {
			return false;
		}
		MyThread t;
		if (waitingThreads.size() > 0) {
			t = (MyThread) waitingThreads.remove(waitingThreads.size()-1);
		} else {
			t = new MyThread();
			t.start();
		}
		runningThreads.add(t);
		t.setTask(pTask);
		return true;
	}

	/** Adds a task for immediate or deferred execution.
	 * @param pTask The task being added.
	 * @return True, if the task was started immediately. False, if
	 * the task will be executed later.
	 */
	public synchronized boolean addTask(Task pTask) {
		if (startTask(pTask)) {
			return true;
		}
		waitingTasks.add(pTask);
		return false;
	}

	/** Closes the pool.
	 */
	public synchronized void shutdown() {
		for (int i = 0;  i < waitingThreads.size();  i++) {
			MyThread t = (MyThread) waitingThreads.get(i);
			t.shutdown();
		}
		for (int i = 0;  i < runningThreads.size();  i++) {
			MyThread t = (MyThread) runningThreads.get(i);
			t.shutdown();
		}
	}

	/** Returns the maximum number of concurrent threads.
	 * @return Maximum number of threads.
	 */
	public int getMaxThreads() { return maxSize; }

	/** Returns the number of threads, which have actually been created,
     * as opposed to the number of currently running threads.
	 */
    public int getNumThreads() { return num; }
}
