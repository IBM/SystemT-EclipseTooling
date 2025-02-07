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
package com.ibm.biginsights.textanalytics.indexer.resourcechange.listeners;

import org.eclipse.core.resources.IResource;

import com.ibm.biginsights.textanalytics.indexer.resourcechange.ResourceChangeJob;
import com.ibm.biginsights.textanalytics.indexer.resourcechange.ResourceChangeQueue;
import com.ibm.biginsights.textanalytics.indexer.types.ResourceAction;

public class TextAnalyticsResourceActionThread extends Thread {



	IResource removedResource;
	IResource addedResource;
	IResource changedResource;
	IResource projectResourceChange;

	/**
	 * Represents the type of resource change event, viz add, delete or rename
	 * Can take up one of the following values: ResourceAction.ADDED,
	 * ResourceAction.DELETED, ResourceAction.RENAMED, ResourceAction.UPDATED,
	 * ResourceAction.MOVED
	 */
	protected ResourceAction resourceChangeType;

	/**
	 * Detects the type of Action to be performed.
	 */
	private void getAction() {
		if (resourceChangeType != null)
			return;

		if (changedResource != null) {
			resourceChangeType = ResourceAction.UPDATED;
		} else if (addedResource == null && removedResource != null) {
			resourceChangeType = ResourceAction.DELETED;
		} else if (addedResource != null && removedResource == null) {
			resourceChangeType = ResourceAction.ADDED;
		} else if (addedResource != null && removedResource != null) {
			if (addedResource.getParent().equals(removedResource.getParent())) {
				resourceChangeType = ResourceAction.RENAMED;
			} else {
				resourceChangeType = ResourceAction.MOVED;
			}
		}
	}

	public TextAnalyticsResourceActionThread(IResource removedResource,
			IResource addedResource, IResource changedResource) {
		super();
		this.removedResource = removedResource;
		this.addedResource = addedResource;
		this.changedResource = changedResource;
	}

	public TextAnalyticsResourceActionThread(IResource removedResource,
			IResource addedResource, IResource changedResource,
			ResourceAction resourceChangeType) {
		super();
		this.removedResource = removedResource;
		this.addedResource = addedResource;
		this.changedResource = changedResource;
		this.resourceChangeType = resourceChangeType;
	}

	public TextAnalyticsResourceActionThread(IResource projectResourceChange,
			ResourceAction resourceChangeType) {
		super();
		this.projectResourceChange = projectResourceChange;
		this.resourceChangeType = resourceChangeType;
	}

	@Override
	public void run() {
		if (!validateResourceChangeEvent()) {
			return;
		}
		getAction();
		switch (resourceChangeType) {
		case ADDED:
			pushAddActionJob();
			break;
		case DELETED:
			pushDeleteActionJob();
			break;
		case MOVED:
			pushMoveActionJob();
			break;
		case RENAMED:
			pushRenameActionJob();
			break;
		case UPDATED:
			pushUpdateActionJob();
			break;
		case OPEN:
			pushOpenActionJob();
			break;
		case CLOSE:
			pushCloseActionJob();
			break;

		}
	}

	private boolean validateResourceChangeEvent() {
		if (removedResource == null && addedResource == null
				&& changedResource == null && projectResourceChange == null) {
			return false;
		}

		// If addedResource and removedResource are not of same type, return
		if (addedResource != null && removedResource != null) {
			if (addedResource.getType() != removedResource.getType()) {
				return false;
			}
		}

		return true;
	}

	private void pushUpdateActionJob() {
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$handleUpdateAction");
		ResourceChangeJob job = new ResourceChangeJob(changedResource,
				resourceChangeType);
		ResourceChangeQueue queue = ResourceChangeQueue.getInstance();
		queue.addJob(job);

	}

	private void pushMoveActionJob() {
		ResourceChangeJob job = new ResourceChangeJob(removedResource,
				addedResource, resourceChangeType);
		ResourceChangeQueue queue = ResourceChangeQueue.getInstance();
		queue.addJob(job);
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$handleMoveAction");
	}

	private void pushRenameActionJob() {
		ResourceChangeJob job = new ResourceChangeJob(removedResource,
				addedResource, resourceChangeType);
		ResourceChangeQueue queue = ResourceChangeQueue.getInstance();
		queue.addJob(job);
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$handleRenameAction");
	}

	private void pushDeleteActionJob() {
		ResourceChangeJob job = new ResourceChangeJob(removedResource,
				resourceChangeType);
		ResourceChangeQueue queue = ResourceChangeQueue.getInstance();
		queue.addJob(job);
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$handleDeletedAction");
	}

	private void pushAddActionJob() {
		ResourceChangeJob job = new ResourceChangeJob(addedResource,
				resourceChangeType);
		ResourceChangeQueue queue = ResourceChangeQueue.getInstance();
		queue.addJob(job);
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$handleAddedAction");
	}

	private void pushOpenActionJob() {
		ResourceChangeJob job = new ResourceChangeJob(projectResourceChange,
				resourceChangeType);
		ResourceChangeQueue queue = ResourceChangeQueue.getInstance();
		queue.addJob(job);
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$handleOpenAction");
	}

	private void pushCloseActionJob() {
		ResourceChangeJob job = new ResourceChangeJob(projectResourceChange,
				resourceChangeType);
		ResourceChangeQueue queue = ResourceChangeQueue.getInstance();
		queue.addJob(job);
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$handleCloseAction");

	}

}
