/*******************************************************************************
* Copyright IBM
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.ibm.biginsights.textanalytics.indexer.resourcechange;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Queue for hold the Jobs. The ResourceChangeReindexThread will picks the Job form the Queue
 * for reindexing the same. 
 *
 */

public class ResourceChangeQueue {



	private static ResourceChangeQueue resourceChangeQueueInstance = null;

	Queue<ResourceChangeJob> resourceChangeQueue = new LinkedList<ResourceChangeJob>();

	private ResourceChangeQueue() {
		super();
	}

	public static synchronized ResourceChangeQueue getInstance() {
		if (resourceChangeQueueInstance == null) {
			resourceChangeQueueInstance = new ResourceChangeQueue();
		}
		return resourceChangeQueueInstance;
	}

	public synchronized void addJob(ResourceChangeJob job) {
		resourceChangeQueue.offer(job);
	}

	public synchronized boolean isEmpty(){
		return resourceChangeQueue.isEmpty();
	}
	
	public synchronized ResourceChangeJob getNextJob(){
		return resourceChangeQueue.poll();
	}

}
