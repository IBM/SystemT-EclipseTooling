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
package com.ibm.biginsights.textanalytics.aql.editor.ui;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

import com.ibm.biginsights.textanalytics.aql.editor.Activator;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class AQLDocumentProvider extends TextFileDocumentProvider {



	@Override
	public IDocument getDocument(Object element) {
		IDocument document = super.getDocument(element);
		if (document != null) {
			try {
			IDocumentPartitioner partitioner = new FastPartitioner(
					new AQLPartitionScanner(), new String[] {
							IDocument.DEFAULT_CONTENT_TYPE,
							AQLPartitionScanner.AQL_COMMENT,
							AQLPartitionScanner.AQL_REGEX,
							AQLPartitionScanner.AQL_STRING,
							AQLPartitionScanner.AQL_VIEW_NAME});
				partitioner.connect(document); //throws AssertionFailedException
				document.setDocumentPartitioner(partitioner);
			} catch (AssertionFailedException e) {
	            //LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logError(e.getMessage(),e);
				//suppressing this exception as it does not affect the functionality of editor
				//Sometimes, this exception is thrown on fast typing and the eclipse tries to get the document
				//which is null as it is being updated by the user and hence the partitioner is not connected to the document 
				//(refer 17173)
				//TODO need to investigate the exact reason/alternative.
			}
			catch (NullPointerException e) {
				//logging the FastPartitioning exception, refer  
	            LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logError(e.getMessage(),e);
			}
		}
		return document;
	}

	
}
