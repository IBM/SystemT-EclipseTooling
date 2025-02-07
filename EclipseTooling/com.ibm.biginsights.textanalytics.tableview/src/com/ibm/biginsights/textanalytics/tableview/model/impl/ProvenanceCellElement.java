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
package com.ibm.biginsights.textanalytics.tableview.model.impl;

import org.eclipse.core.resources.IFile;

import com.ibm.biginsights.textanalytics.tableview.Messages;
import com.ibm.biginsights.textanalytics.tableview.model.CellType;

/**
 * Container for the "Provenance" button displayed for each row in the
 * TableViewer. Stores the name of the output view assiciated with the
 * TableViewer, so that it can be retrieved by the MouseListener.
 * 
 * 
 * 
 */
public class ProvenanceCellElement extends AbstractCellElement {



	// Text displayed in the cell
	private final String text = Messages.getString("ProvenanceCellElement_EXPLAIN"); //$NON-NLS-1$

	// Input document used to generate this cell's annotation
	private final IFile file;

	public ProvenanceCellElement(IFile file) {
		super(CellType.PROVENANCE);
		this.file = file;
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public String toString() {
		return getText();
	}
	
	@Override
	  public IFile getFile() {
	    return this.file;
	 }
}
