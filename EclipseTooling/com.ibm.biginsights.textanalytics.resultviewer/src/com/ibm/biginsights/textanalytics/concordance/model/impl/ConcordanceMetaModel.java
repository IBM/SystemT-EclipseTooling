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
package com.ibm.biginsights.textanalytics.concordance.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.ibm.biginsights.textanalytics.resultviewer.model.FieldType;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldValue;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;

/**
 * This class is used by the treeview menu handlers such as ShowNext, Show
 * Previous, Show All etc. It uses the resultDirPathas the parameter
 * and builds merged or next, previous SystemTComputationResult models based on
 * it.
 * 
 *  Madiraju
 * 
 */
public class ConcordanceMetaModel {



	private IFolder resultFolder;
	private SystemTComputationResult[] allResultModelsArray = null;
	//public static HashMap<String,ConcordanceMetaModel> filePathConcordanceMMInstanceMap = null;
	
	/*public static ConcordanceMetaModel getInstance(IFolder pResultFolder)
	{
		String key = pResultFolder.getFullPath().toString();
		if (filePathConcordanceMMInstanceMap == null)
		{
			filePathConcordanceMMInstanceMap = new HashMap<String,ConcordanceMetaModel>();
		}
		ConcordanceMetaModel cmm = filePathConcordanceMMInstanceMap.get(key);
		if (cmm == null)
		{
			cmm = new ConcordanceMetaModel(pResultFolder);
			filePathConcordanceMMInstanceMap.put(key, cmm);
		}
		return cmm;
	}
	
	
	public static ConcordanceMetaModel getInstance(String resultPath, List<SystemTComputationResult> allModelsAL)
	{
		String key = resultPath;
		if (filePathConcordanceMMInstanceMap == null)
		{
			filePathConcordanceMMInstanceMap = new HashMap<String,ConcordanceMetaModel>();
		}
		ConcordanceMetaModel cmm = filePathConcordanceMMInstanceMap.get(key);
		if (cmm == null)
		{
			cmm = new ConcordanceMetaModel(allModelsAL);
			filePathConcordanceMMInstanceMap.put(key, cmm);
		}
		return cmm;
	}

*/	public ConcordanceMetaModel(IFolder pResultFolder) {
		resultFolder = pResultFolder;
		allResultModelsArray = getAllResultModels();
	}

	public ConcordanceMetaModel(List<SystemTComputationResult> pAllModelsAL) {
		allResultModelsArray = new SystemTComputationResult[pAllModelsAL.size()];
		pAllModelsAL.toArray(allResultModelsArray);
	}
	
	/*public static void clearCache()
	{
		filePathConcordanceMMInstanceMap=null;
	}*/
	/**
	 * This is an utility method used by other methods of this class. It
	 * iterates through the result folder and gets the SystemTComputationResult
	 * - object models from the serialized strf files. This returns an ARRAYLIST
	 * 
	 * This method should probably use some caching in order to avoid reading the 
	 * result files every time. Explore Later.
	 * 
	 * @return
	 */
	private ArrayList<SystemTComputationResult> getAllResultModelsArrayList() {
		try {
			IResource[] resources = resultFolder.members();
			ArrayList<IFile> al = new ArrayList<IFile>();
			String extn = null;
			for (IResource member : resources) {
				extn=member.getFileExtension();
				if (member instanceof IFile) {
					if ((Constants.STRF_FILE_EXTENSION.equals(extn) || (Constants.GS_FILE_EXTENSION.equals(extn)))
							&& (member.getName().startsWith(Constants.ALL_DOCS) == false)) {
						// We want the strf file to be counted only if it is not merged models such as AllDocs
						al.add((IFile)member);
					}
				}
			}
			Iterator<IFile> iter = al.iterator();
			IFile file = null;
			Serializer srlzr = new Serializer();
			ArrayList<SystemTComputationResult> strList = new ArrayList<SystemTComputationResult>();
			while (iter.hasNext()) {
				file = iter.next();
				strList.add(srlzr.getModelForInputStream(file.getContents()));
			}
			return strList;
		} catch (CoreException e) {
			return null;
		}
	}

	public SystemTComputationResult getResult(String docId){
		for (int i = 0; i < allResultModelsArray.length; i++) {
			if(allResultModelsArray[i].getDocumentID().equals(docId))
				return allResultModelsArray[i];
		}
		return null;
	}
	/**
	 * This is a utility method similar to getAllResultModelsArrayList to
	 * iterate through the serialized results. However this returns AN ARRAY of
	 * the models.
	 * 
	 * @return
	 */

	private SystemTComputationResult[] getAllResultModels() {
		ArrayList<SystemTComputationResult> allResultModelsAL=getAllResultModelsArrayList();
		SystemTComputationResult[] allModels = new SystemTComputationResult[allResultModelsAL
				.size()];
		allResultModelsAL.toArray(allModels);
		return allModels;
	}

	/**
	 * This method is called from the ShowPrevious and ShowNext Handlers. It
	 * takes the current file name as parameter, sorts the results (The sorting
	 * is based on result file name - See compare method of
	 * SystemTComputationResult)
	 * 
	 * @param inputFileName
	 * @param next
	 * @return
	 */
	public SystemTComputationResult getNextPrevious(String inputFileName,
			boolean next, ArrayList<String> viewsToBeShown,String docSchema) {
		// If the outputViewsToBeShown parameter is empty, then all models are
		// returned
		inputFileName=StringUtils.normalizeSpecialChars(inputFileName);
		ArrayList<SystemTComputationResult> allModels = getResultModelsWithOnlyTheseViewsAndThisSchemaArrayList(viewsToBeShown,docSchema);
		Collections.sort(allModels);
		SystemTComputationResult currentResult, prevResult, nextResult, returnResult = null;
		currentResult = allModels.get(0);
		prevResult = allModels.get(0);
		nextResult = allModels.get(0);
		int size = allModels.size();
		String currentResultDocId;
		for (int j = 0; j < size; j++) {
			currentResult = allModels.get(j);
			if ((j + 1) < size) {
				nextResult = allModels.get(j + 1);
			}
			if ((j - 1) > 0) {
				prevResult = allModels.get(j - 1);
			}
			currentResultDocId = currentResult.getDocumentID();
			currentResultDocId=StringUtils.normalizeSpecialChars(currentResultDocId);
			if (currentResultDocId.equals(inputFileName)) {
				if (next) {
					if (j==(size-1)) // if we have reached end of result files, then show the first result again
					{
						returnResult = allModels.get(0);
					}
					else
					{
						returnResult = nextResult;
					}
				} else {
					if (j==0) // if we have reached end of result files, then show the first result again
					{
						returnResult = allModels.get(size-1);
					}
					else
					{
						returnResult = prevResult;
					}
				}
				break;
			}
		}
		return returnResult;
	}
	

	/**
	 * This is a method similar to getResultModelsWithOnlyViewsArrayList, it
	 * returns back an array instead of any arraylist
	 * 
	 * @param opviewsToBeShown
	 * @return
	 */
	private SystemTComputationResult[] getResultModelsWithOnlyTheseViewsAndThisSchema(
			ArrayList<String> opviewsToBeShown, String docSchema) {
		ArrayList<SystemTComputationResult> modelsWithOnlyViews = getResultModelsWithOnlyTheseViewsAndThisSchemaArrayList(opviewsToBeShown,docSchema);
		SystemTComputationResult[] filteredModels = new SystemTComputationResult[modelsWithOnlyViews
				.size()];
		modelsWithOnlyViews.toArray(filteredModels);
		return filteredModels;
	}

	/**
	 * This method will select only those SystemTComputationResult models (ie
	 * files) which have atleast one row for the given output views. It doesn't
	 * make sense to go down to attribute level for screening. Since if an
	 * output view is present, then all of the attributes will also be present
	 * for that view
	 */
	private ArrayList<SystemTComputationResult> getResultModelsWithOnlyTheseViewsAndThisSchemaArrayList(
			ArrayList<String> opviewsToBeShown, String docSchema) {
		ArrayList<SystemTComputationResult> modelsWithOnlyViews = new ArrayList<SystemTComputationResult>();
		if ((opviewsToBeShown == null) || (opviewsToBeShown.size() == 0)) {
			// This means the call will usually be from ShowNext,ShowPrev,ShowAllDocs - and no annotations are specified
			// Then you need to just ensure that schema is matching
			for (SystemTComputationResult result : allResultModelsArray) {
				result.setInputTextID(result.getInputTextIDForThisSchema(docSchema));
				modelsWithOnlyViews.add(result);
			}
		} else {
				// If the call is for a specific viewname.attributename, that means its coming from ShowNextWithAnnot or ShowPrevWithAnnot or ShowAllDocsWith Annnot
				// then it is just enough to look for if there is atleast one row for that outputview.  
				for (SystemTComputationResult result : allResultModelsArray) {
					OutputView[] opViews = result.getOutputViews();
					if (opViews!=null)
					{
						boolean isBreakTrue=false;
						for (OutputView view : opViews) {
							if (isBreakTrue)
							{
								break;
							}
							// compare the output view selected by user with the op view in the result
							String[] attributeNames = view.getFieldNames();
							for (int j=0;j<attributeNames.length;j++)
							{
								if (isBreakTrue)
								{
									break;
								}
								if (opviewsToBeShown.contains(view.getName()+"."+attributeNames[j])) {
									if ((view.getRows() != null)
											&& (view.getRows().length > 0)) {
										for (int m=0;m<view.getRows().length;m++)
										{
											FieldValue[] fVals =  view.getRows()[m].fieldValues;
											//If the attributeNames[] contains four span attributes for example - attributeNames[0]="firstname",attributeNames[1]="middlename",
											//attributeNames[2]="lastname",attributeNames[3]="person"
											//FieldValue[] contains for example FieldValue[0]=instance of SpanVal , FieldValue[1]=instance of TextVal , FieldValue[2]=instance of SpanVal , FieldValue[3]=instance of SpanVal.
											//In this example case, the middlename attribute does not contain a SpanVal and it will be just an TextVal.
											//In such similar scenarios it is required to check the type of fVals[j] before type casting it to SpanVal.
											if (fVals != null && fVals[j] instanceof SpanVal)
											{
												SpanVal spanVal = (SpanVal)fVals[j];
												if ((spanVal.parentSpanName != null) && (spanVal.parentSpanName.equals(docSchema)))
													{
														result.setInputTextID(spanVal.sourceID);
														modelsWithOnlyViews.add(result);
														isBreakTrue = true;
														break;
													}
												}
											}
										}
								}
							}
						}
					}
			}
		}
		return modelsWithOnlyViews;
	}

	/**
	 * This method will select only those SystemTComputationResult models (ie
	 * files) which have at least one row anywhere in any view of the given docSchema.
	 */
	/*private ArrayList<SystemTComputationResult> getResultModelsWithThisSchemaOnly(
			ArrayList<SystemTComputationResult> allModels,String docSchema) {
		ArrayList<SystemTComputationResult> modelsWithThisSchemaOnly = new ArrayList<SystemTComputationResult>();
			System.out.println("getResultModelsWithThisSchemaOnly, views are:" + allModels + ", docSchema is " + docSchema);
			for (SystemTComputationResult result : allModels) {
				System.out.println("getResultModelsWithThisSchemaOnly result being searched is "  + result.getDocumentID());
				OutputView[] opViews = result.getOutputViews();
				boolean isBreakTrue = false;
				if (opViews!=null)
				{
					for (OutputView view : opViews) {
						if (isBreakTrue)
						{
							break;
						}
						// compare the output view selected by user with the op view in the result
							OutputViewRow[] rows = view.getRows();
							if ((rows != null)
									&& (rows.length > 0)) {
								for(int j=0;j<rows.length;j++)
								{
									if (isBreakTrue)
									{
										break;
									}
									FieldValue[] values = rows[j].fieldValues;
									for (int k=0;k<values.length;k++)
									{
										
										FieldValue val = values[k];
										FieldType type = val.getType();
										SpanVal spanVal = null;
										if (type == FieldType.SPAN) {
											// only for field values of type span, we need to reset the
											// offsets
											spanVal = (SpanVal) val;
											System.out.println("Going to compare spanVal" + spanVal);
											if (spanVal.parentSpanName != null)
											{
												if (spanVal.parentSpanName.equals(docSchema))
												{
													System.out.println("spanVal is a match = therfore result " + result.getDocumentID() + " is a match");
													System.out.println("setting the inputtextid as" + spanVal.sourceID);
													// This setting of the spanVal.sourceID is very important to distinguish between base and detagged text when displaying in the treeview in the end													
													result.setInputTextID(spanVal.sourceID); 
													modelsWithThisSchemaOnly.add(result);
													isBreakTrue = true;
													break;
												}
											}
										}
									}
								}
								
							}
					}
				}
			}
		System.out.println("getResultModelsWithThisSchemaOnly length is " + modelsWithThisSchemaOnly.size());
		return modelsWithThisSchemaOnly;
	}

*/	
	/*
	
	*//**
	 * From the ConcordanceModel, we only know the tempDirectory path - ie where
	 * the bitatext files are written whenever the strf files is rendered to the
	 * editor. Since we need the result directory itself, we can reconstruct is
	 * using the getParent. Maybe a simpler option is to set the result folder
	 * also on the concordancemodel. Explore later.
	 * 
	 * @param tmpDir
	 * @return
	 *//*
	private IFolder getResultDir(String tmpDir) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFolder tmpFolder = workspace.getRoot().getFolder(new Path(tmpDir));
		if (tmpFolder.getParent().isAccessible()) {
			return (IFolder) tmpFolder.getParent();
		} else {
			String msg = Messages.parentResultFolderDoesNotExist;
			String formattedMsg = MessageUtil.formatMessage(msg, tmpFolder
					.getParent().getFullPath().toString());

			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
					formattedMsg);
		}
		return null;
	}
*/
	/**
	 * This method is called from the ShowAllDocsHandler and
	 * ShowAllDocsWithAnnotationsHandler.
	 * 
	 * @param outputViewsToBeShown
	 * @param documentID
	 * @return
	 */
	public SystemTComputationResult getMergedModelForAllDocuments(
			ArrayList<String> outputViewsToBeShown, String documentID,String docSchema) {

		// If the outputViewsToBeShown parameter is empty, then it equals to
		// Show AllDocs
		SystemTComputationResult[] results = getResultModelsWithOnlyTheseViewsAndThisSchema(outputViewsToBeShown,docSchema);

		// Construct the merged model
		SystemTComputationResult finalModel = new SystemTComputationResult();
		finalModel.setDocumentID(documentID);

		SystemTComputationResult individualModel = null;
		StringBuffer inputText = new StringBuffer("");
		String indText = "";
		int indTextID = -1;
		OutputView[] indViews = null;
		OutputView indView = null;

		// Create the content for outputViews of the final model.
		// Take one sample file's output views and set it into the
		// model
		// i.e set the name, field names and field types, which will be same
		// across all entries
		// However the rows need to be iterated through all individual entries.
		individualModel = results[0];
		OutputView[] sampleViews = individualModel.getOutputViews();
		if (sampleViews ==null) 
		{
			// it means that there are nothing in this model - so return empty model
			// as in the case when import of lc is done from input collections and nothing's been marked
			return finalModel;
		}
		OutputView[] finalViews = new OutputView[sampleViews.length];
		OutputView finalView = null;
		String indDocID = null;
		for (int j = 0; j < sampleViews.length; j++) {
			finalView = new OutputView();
			finalView.setName(sampleViews[j].getName());
			finalView.setFieldNames(sampleViews[j].getFieldNames());
			finalView.setFieldTypes(sampleViews[j].getFieldTypes());
			finalViews[j] = finalView;
		}
		finalModel.setOutputViews(finalViews);

		int sourceId = outputViewsToBeShown.hashCode()+docSchema.hashCode();
		int offset = 0;
		for (int m = 0; m < results.length; m++) {
			individualModel = results[m];
			indDocID = individualModel.getDocumentID();
			indTextID = individualModel.getInputTextID();
			
			indText = individualModel.getTextValueMap().get(indTextID);
			inputText.append("\nINPUT DOCUMENT:" + indDocID
					 + "\n");
			inputText.append(indText);
			offset = offset + indDocID.length() + 15 + 2; // length of the
																// document ID + 2															// new line
																// chars
			// + 15 characters for the string INPUT DOCUMENT:
			indViews = individualModel.getOutputViews();
			if (indViews != null)
			{
			for (int n = 0; n < indViews.length; n++) {
				// the output view of this particular entry
				indView = indViews[n];
				// the output view of the final model. This has only the name,
				// fieldnames and field types filled in, but not the rows
				finalView = finalViews[n];
				addRowsToView(indView.getRows(), finalView, offset, sourceId,docSchema);
			}
			}
			offset = offset + indText.length();
		}
		finalModel.addText(sourceId, inputText.toString());
		finalModel.setInputTextID(sourceId);
		return finalModel;

	}

	/**
	 * This private method is called from getMergedModelForAllDocuments to add
	 * the rows into the merged model.
	 * 
	 * @param indModelRows
	 * @param view
	 * @param offset
	 * @param sourceId
	 */
	private void addRowsToView(OutputViewRow[] indModelRows, OutputView view,
			int offset, int sourceId, String docSchema) {

		OutputViewRow[] oldRows = view.getRows();
		ArrayList<OutputViewRow> al = new ArrayList<OutputViewRow>();
		FieldValue val = null;
		FieldType type = null;
		SpanVal spanVal, newSpanVal = null;

		if (oldRows != null) {
			for (int j = 0; j < oldRows.length; j++) {
				// retain the old rows as-is
				al.add((OutputViewRow) oldRows[j]);
			}
		}
		OutputViewRow[] allDocsNewRows = new OutputViewRow[indModelRows.length];
		for (int k = 0; k < indModelRows.length; k++) {
			FieldValue[] fieldValues = indModelRows[k].fieldValues;
			if (fieldValues == null)
			{
				// skip row - it means it is an empty row.
				continue;
			}
			allDocsNewRows[k] = new OutputViewRow();
			allDocsNewRows[k].fieldValues = new FieldValue[indModelRows[k].fieldValues.length];
			// For all the individual entry's rows (newRows) reset the offsets
			// etc if the fieldtypes are of type span
		
			for (int l = 0; l < fieldValues.length; l++) {
				val = fieldValues[l];
				type = val.getType();
				if (type == FieldType.SPAN) {
					// only for field values of type span, we need to reset the
					// offsets
					spanVal = (SpanVal) val;
					newSpanVal = new SpanVal();
					newSpanVal.parentSpanName = spanVal.parentSpanName;
					newSpanVal.start = spanVal.start + offset;
					newSpanVal.end = spanVal.end + offset;
					if ((docSchema).equals(spanVal.parentSpanName))
					{
						// Whenever AllDocs or AllDocsWithSelections are present, we are concatentating input text only of the specific docshcema
						// Also we will assigning this new concatenated input text to only those new spanVals where the docSchema mathces with what is to be shown
						newSpanVal.sourceID = sourceId;
					}
					else
					{
						// Continuting from the comment in the 'if' above,
						// set the source id to original sourceid so that it will not be picked up for display
						newSpanVal.sourceID = spanVal.sourceID;
					}
					allDocsNewRows[k].put(l, newSpanVal);
				} else {
					allDocsNewRows[k].put(l, val);
				}
			}
			al.add((OutputViewRow) allDocsNewRows[k]);
		}
		OutputViewRow[] allrows = new OutputViewRow[al.size()];
		view.setRows(al.toArray(allrows));
	}

}
