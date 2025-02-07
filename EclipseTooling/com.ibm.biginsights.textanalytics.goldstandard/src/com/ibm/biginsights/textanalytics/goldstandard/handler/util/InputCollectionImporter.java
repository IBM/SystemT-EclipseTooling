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
package com.ibm.biginsights.textanalytics.goldstandard.handler.util;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.avatar.algebra.datamodel.Pair;
import com.ibm.avatar.api.DocReader;
import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Imports any supported format of input collection into a target labeledCollection folder.
 * NOTE: Use DocReader for importing corpus so that the imported collection is in sync with the SystemTRunJob.
 * Using other means of importing a doc collection may lead to incorrect offsets
 * 
 *  Krishnamurthy
 */
public class InputCollectionImporter {


	
	File inputCollection;
	IFolder gsFolder;
	IProgressMonitor progressMonitor;
	private Map<String, Integer> filesWithSameName; 
	
	int totalWork = 0;
	int completedWork = 0;
	
	/**
	 * @param inputCollection
	 * @param gsFolder
	 */
	public InputCollectionImporter(File inputCollection, IFolder gsFolder, IProgressMonitor monitor) {
		super();
		this.inputCollection = inputCollection;
		this.gsFolder = gsFolder;
		this.progressMonitor = monitor;
		filesWithSameName = new HashMap<String, Integer>();
	}
	
	public boolean importCorpus(){
		boolean success = true;
		try {
			totalWork = computeTotalWork();
			progressMonitor.beginTask(MessageUtil.formatMessage(Messages.ImportCorpusHandler_IMPORTING_CORPUS, inputCollection.getName()), totalWork);
			Iterator<Pair<String, String>> iterator = DocReader.makePairsItr(inputCollection);
			while(iterator.hasNext()){
				Pair<String, String> pair = iterator.next();
				String label = pair.first;
				String content = pair.second;
				String fileName = StringUtils.normalizeSpecialChars(label);
				fileName = StringUtils.truncatePath(fileName);
				
				
				Integer count = filesWithSameName.get(fileName);
				if(count != null){
					count = count+1;
					filesWithSameName.put(fileName, count);
					fileName = fileName + "("+ count.toString() + ")";
					
					
				}else{
					filesWithSameName.put(fileName, new Integer(1));
				}
				
				success = success && createGSFile(gsFolder, fileName, content);
			}
		} catch (Exception e) {
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage());
			return false;
		}
		
		return success;
	}
	
	protected boolean createGSFile(IFolder destFolder, String fileName, String fileContents) {
		boolean success = false;
		try {
			progressMonitor.subTask(MessageUtil.formatMessage(Messages.ImportCorpusHandler_IMPORTING_FILE, fileName));
			if(!progressMonitor.isCanceled()){
				SystemTComputationResult model = new SystemTComputationResult();
				model.setDocumentID(fileName);
				model.setInputTextID(fileContents.hashCode());
				model.addText(fileContents.hashCode(), fileContents);

				Serializer serialzier = new Serializer();

				IFile gsFile = destFolder.getFile(deriveGSFileName(fileName));
				serialzier.writeModelToFile(gsFile, model);
				gsFile.setCharset(Constants.ENCODING, null);
				progressMonitor.worked(1);
			}
			success = true;
		} catch (Exception e) {
			success = false;
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage());
		}
		return success;
	}
	
	protected String deriveGSFileName(String fileName) {
		return fileName + Constants.GS_FILE_EXTENSION_WITH_DOT;
	}
	
	private int computeTotalWork() throws Exception {
		DocReader docs = new DocReader(inputCollection);
		return docs.size();
	}
}
