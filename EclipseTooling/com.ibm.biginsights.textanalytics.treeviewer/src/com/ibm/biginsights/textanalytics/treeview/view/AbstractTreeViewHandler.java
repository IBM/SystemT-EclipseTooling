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
package com.ibm.biginsights.textanalytics.treeview.view;

import static com.ibm.biginsights.textanalytics.treeview.model.TreeObjectType.SPAN;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.biginsights.textanalytics.concordance.model.impl.ConcordanceMetaModel;
import com.ibm.biginsights.textanalytics.concordance.model.impl.ConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.ui.ConcordanceView;
import com.ibm.biginsights.textanalytics.concordance.ui.ResultEditor;
import com.ibm.biginsights.textanalytics.resultviewer.Activator;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.resultviewer.util.EditorInput;
import com.ibm.biginsights.textanalytics.resultviewer.util.ResultViewerUtil;
import com.ibm.biginsights.textanalytics.treeview.model.impl.AbstractTreeObject;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This is an abstract handler class that has all common methods across all tree
 * view menu handlers such as Show Next Doc, Show Prev Doc, Show All Docs etc,
 * 
 *  Madiraju
 * 
 */
public abstract class AbstractTreeViewHandler extends AbstractHandler implements IPartListener, IElementUpdater{



	protected ConcordanceMetaModel metaModel = null;
	// protected ConcordanceModel concModel = null;
	protected IFolder resultFolder = null;
	protected IFolder tempFolder = null;
	protected SystemTComputationResult result = null;
	protected boolean isLC = false;
	protected IFile lcFile = null;
	protected String extn = null;
	boolean allDocsMode = false;
	protected AQLResultTreeView lTreeView = null;
	protected String partName = null;
	protected String docSchema = "Document.txt";
	protected boolean enabled = true;
	protected boolean init = true;
	protected IWorkbenchPart activePart;
	
	
	
	public boolean isAllDocsMode() {
		return allDocsMode;
	}
	public void setAllDocsMode(boolean allDocsMode) {
		this.allDocsMode = allDocsMode;
	}
	/**
	 * This is an utility method to remove the document schema name from the
	 * treeview title or partname This is required when we compare for
	 * next/previous display - because the stored result models have only the
	 * file names and the treeview has the filename+docschema as its name/title
	 * 
	 * @param partName
	 * @return
	 */
	private void determineDocSchemaAndDocNameFromTitle(String pPartName) {
		if (isLC)
		{
			docSchema = "Document.text"; // because as of 1.3.1 - we support only Document.txt for type LC
		}
		else
		{
			int indexOfSchema = pPartName.lastIndexOf(" - "); // Whenever we show a document in the treeview we will append the docschema in the end. 
			// So the indexOfSchema is always the last index of this string " - " which is how we append.
			if (indexOfSchema >= 0) {
			  docSchema = pPartName.substring(indexOfSchema+3); // 3 for the 3 characters in " - "
			  partName = pPartName.replace(" - " + docSchema, ""); //$NON-NLS-1$ 
			}
		}
	}
	
	public String getDocSchemaName(String pPartName) {
		determineDocSchemaAndDocNameFromTitle(pPartName);
		return docSchema;		
	}

	/**
	 * This method is called by all extending handler classes. This gets the
	 * basic information from the home/landing ConcordanceView.
	 */
	protected void initialize(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		if (part instanceof AQLResultTreeView) {
			AQLResultTreeView treeView = (AQLResultTreeView) part;
			lTreeView = treeView;
			String thisDoc = treeView.getTitle();
			if ((thisDoc.endsWith(Constants.GS_FILE_EXTENSION_WITH_DOT))
					|| (thisDoc.contains(Constants.GS_FILE_EXTENSION_WITH_DOT
							+ Constants.STRF_FILE_EXTENSION_WITH_DOT))) {
				isLC = true;
				extn = "";
				getLCAndTempFolder(thisDoc);
				metaModel = new ConcordanceMetaModel(resultFolder);
			} else {
				isLC = false;
				extn = Constants.STRF_FILE_EXTENSION_WITH_DOT;
				ConcordanceView concView = (ConcordanceView) page
						.findView(ConcordanceView.VIEW_ID);
				if (null != concView) {
				ConcordanceModel concModel = (ConcordanceModel) concView.getModel();
					if(! concModel.getTempDirPath().isEmpty()) {
						getResultAndTempFolder(concModel);
						metaModel = new ConcordanceMetaModel(concModel.getSTCRModels());
					}
				}
			}
			determineDocSchemaAndDocNameFromTitle(part.getTitle());
			//metaModel = ConcordanceMetaModel.getInstance(resultFolder);
		}
	}

	private void getResultAndTempFolder(ConcordanceModel concModel) {
		tempFolder = ResultViewerUtil.getTempFolder(concModel.getTempDirPath());
		resultFolder = (IFolder) tempFolder.getParent();
//		System.out.println("AbstractTreeViewHandler:Result file" + ", resultFolder:"+resultFolder + ", tempFolder:"+tempFolder);
	}

	private void getLCAndTempFolder(String filePath) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		lcFile = workspace.getRoot().getFile(new Path(filePath));
		resultFolder = (IFolder) lcFile.getParent();
		tempFolder = resultFolder;
	}

	/**
	 * From the given model, show the text in the text editor.
	 * 
	 * @param result
	 * @param concModel
	 */
	protected IEditorPart showEditorForResult(SystemTComputationResult result) {
		if(result == null){
			return null;
		}
		
		// Get the text from the selection
		try {
			boolean allDocsLC = result.getDocumentID().startsWith(
					Constants.ALL_DOCS);
			if ((isLC == false) || (allDocsLC)) {
				// If it is alldocs mode opened in LC then we need to show only
				// in non LC mode
				// ie not allow annotating fns.
				String name = result.getDocumentID();
				IWorkbenchPage page = PlatformUI
				.getWorkbench()
				.getActiveWorkbenchWindow()
				.getActivePage();
				if (allDocsLC) {
					// If it is alldocs mode opened in LC then we need to show
					// path in the editor
					// so that functions called on it can be recognized.
					name = resultFolder.getFullPath().toOSString()
							+ File.separator + name;
					// Added the if block below to take care of defect 19442 
					// ie if ShowAllDocsWithAnnots is clicked from a treeview 
					// that is already showing a doc with all annots
					// it is attempting to close the editor that it is creating
					// So in such a scenario, we will just close the previous editor 
					// and reope again. This is required only in this particular case.
					if (lTreeView.getPartName().contains(Constants.ALL_DOCS_WITH_ANNOTS))
					{
						if(page != null){
							page.hideView(lTreeView);
						}
				}
				}
				if (docSchema == null || docSchema.length() == 0) {
					docSchema = "Anonymous [" + result.getInputTextID()+ "]"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				String nameWithSchema = name+ " - " + docSchema; //$NON-NLS-1$
				final String text = result.getTextValueMap().get(result.getInputTextID());
				IFile textFile = ResultViewerUtil.writeTempFile(text, Integer.toString(result
						.getInputTextID()), tempFolder.getFullPath()
						.toOSString());
				if (textFile == null) {
					System.err
							.println("Returning in the showEditorForResult method because textFile is null");
					return null;
				}
				EditorInput input = new EditorInput(textFile,
						result.getInputTextID(), nameWithSchema, result,
						null);
				input.setCurrentProjectReference (textFile.getProject ().getName ());
				
				IEditorPart editor = (IEditorPart) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.findEditor(input);

				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().closeEditor(editor, true);
				
				ITextEditor txtEditor = (ITextEditor)page
						.openEditor(input
								, Activator.RESULT_EDITOR_ID);
				return txtEditor;
			} else {
				String normalizedResultDocID = StringUtils.normalizeSpecialChars(result.getDocumentID());
				String path = resultFolder.getFullPath().toOSString()
						+ File.separator + normalizedResultDocID
						+ Constants.GS_FILE_EXTENSION_WITH_DOT;
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IFile displayFile = workspace.getRoot().getFile(new Path(path));
				return openGSEditor(displayFile);
			}
		} catch (PartInitException e) {
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
					Messages.unableToOpenEditorMessage, e);
		} catch (CoreException e) {
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
					Messages.unableToOpenEditorMessage, e);
		} catch (IOException e) {
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
					Messages.unableToOpenEditorMessage, e);
		}
		finally
		{
			metaModel = null;
		}
		return null;
	}

	/*
	 * This is a method used by ShowAllDocs handlers to serialize the
	 * SystemTComputationResult object created by concatenating all of the
	 * individual documents' SystemTComputationResult instances.
	 */
	protected void serializeResultFile(SystemTComputationResult compResult) {
		Serializer srlzr = new Serializer();
		IFile file = resultFolder.getFile(new Path(compResult.getDocumentID()));
		srlzr.writeModelToFile(file, compResult);
	}

	/**
	 * Construct an arraylist to be passed on the concordancemetamodel based on
	 * the selection of checkboxes made by user. The ArrayList contains a list
	 * of output View names. If an attribute or value alone is selected - even
	 * then the output view name is taken into consideration. Because only that
	 * makes sense.
	 * 
	 * @param event
	 * @return
	 * @throws ExecutionException
	 */
	protected ArrayList<String> getOutputViewsToBeShown(ExecutionEvent event)
			throws ExecutionException {		ArrayList<String> al = new ArrayList<String>();
		IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		if (part instanceof AQLResultTreeView) {
			AQLResultTreeView treeView = (AQLResultTreeView) part;
			CheckboxTreeViewer treeViewer =  treeView.getViewer();
			if (treeViewer != null) {
				Object[] elements = treeViewer.getCheckedElements();
				String viewName = null;
				String attributeName = null;
				for (int i = 0; i < elements.length; i++) {
					if (elements[i] instanceof AbstractTreeObject) {
						AbstractTreeObject entry = (AbstractTreeObject) elements[i];
						if (entry.getType() == SPAN) {
							attributeName = entry.getParent().getName();
							attributeName = attributeName.replace("(SPAN)","").trim();
							viewName = entry.getParent().getParent().getName();
							if (al.contains(viewName+"."+attributeName) == false) {
								al.add(viewName+"."+attributeName);
							}
						}
					}
				}
			}
		}
		return al;
	}

	/**
	 * This is an utility method to remove the LabeledCollectionExtension from
	 * the treeview title or partname This is required when we compare for
	 * next/previous display - because the stored result models have only the
	 * file names and the treeview has the filename+extn as its name/title
	 * 
	 * @param partName
	 * @return
	 */
	protected String replaceLCExtn(String inputFileName) {
		CharSequence unnecessaryText = Constants.GS_FILE_EXTENSION_WITH_DOT; //$NON-NLS-1$ 
		CharSequence emptyStr = "";
		if (inputFileName.contains(unnecessaryText)) {
			inputFileName = inputFileName.replace(unnecessaryText, emptyStr);
		}
		return inputFileName;
	}

	/*
	 * This is an utility method to open the LC file using the GS Editor
	 */
	private IEditorPart openGSEditor(IFile file) throws PartInitException {
		IEditorPart iePart = null;
		String docID = file.getFullPath().toString();
		String secondaryViewID = docID.replaceAll(":", "");

		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		if (page != null) {
		  // TODO : Given below is a work around for Eclipse 4.2.2 bug: 49783. Revisit this when upgrading to higher versions of Eclipse
	    //Begin: workaround
	    String viewId = secondaryViewID != null ? AQLResultTreeView.ID+":"+secondaryViewID : AQLResultTreeView.ID; //$NON-NLS-1$
	    final IViewReference prevView = page.findViewReference(viewId, secondaryViewID);
	    //End: workaround
	    if (prevView != null) {
				page.activate(prevView.getPart(true));
			} else {
				iePart = page.openEditor(new FileEditorInput(file),
						Constants.GS_EDITOR_ID);
			}
		}
		return iePart;
	}

	/*
	 * This is an utility method to return the TextEditor corresponding to the 
	 * TreeView that is highlighted.
	 */
	protected ResultEditor getEditorForThisTreeView(ExecutionEvent event)
			throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		final String docID = part.getTitle();
		String secondaryViewID = docID.replaceAll(":", "");
		if (part instanceof AQLResultTreeView) {
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
				IEditorInput editorInput = AQLResultTreeView
						.getEditorForId(secondaryViewID);
				if ( null != editorInput) {
					IEditorPart iePart = page.findEditor(editorInput);
					if (iePart instanceof AbstractTextEditor) {
						ResultEditor editor = (ResultEditor) iePart;
						return editor;
					}
				}	
		}
		return null;
	}

	/**
	 * This is an utility method to get the Annotation model of the text editor given the
	 * title 
	 * @param partTitle
	 * @return
	 */
    protected IAnnotationModel getAnnotationModelAndEditorOfFile(String partTitle) {
    	IAnnotationModel annotationModel = null;
    	ResultEditor editor = null;
          IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
          IWorkbenchPage[] pages = window.getPages();
          for (int j = 0; j < pages.length; j++) {
            IEditorPart[] allEditorParts = pages[j].getEditors();
            for (int l = 0; l < allEditorParts.length; l++) {
              if (allEditorParts[l] != null) {
                if (allEditorParts[l].getTitle().startsWith(partTitle)) {
                  if (allEditorParts[l] instanceof AbstractTextEditor) {
                    editor = (ResultEditor) allEditorParts[l];
                    IEditorInput input = editor.getEditorInput();
                    annotationModel = editor.getDocumentProvider().getAnnotationModel(input);
                    return annotationModel;

                  }
                }
              }
            }
          }
          return null;
      }

    /**
     * This method takes the AllDocs* documents and annotates the document separators
     * so that it is can be found using next/prev document mode. 
     * @param secondaryViewId
     * @param text
     */
    protected void annotateDocumentNamesInAllDocs(String secondaryViewId,String text)
    {
    	IAnnotationModel model = getAnnotationModelAndEditorOfFile(secondaryViewId);
        int idx=0,start=0,end = 0;
        while (start != -1)
        {
        	start = text.indexOf("INPUT DOCUMENT:", idx);
        	if (start == -1)
        	{
        		break;
        	}
        	end = start+15; // 15 is the length of the string - "INPUT DOCUMENT:"
            Annotation annot = new Annotation("com.ibm.biginsights.textanalytics.aql.annot-document-marker", false, "Document Marker"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            Position pos = new Position(start, end - start);
            model.addAnnotation(annot, pos);
            idx = end;
        }
        
    }
    
	@Override
	public boolean isEnabled() {
		if (this.init) {
			this.init = false;
			IPartService ps = (IPartService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IPartService.class);
			ps.addPartListener(this);
		}
		return this.enabled;
	}
	  
	  @Override
	  public void dispose() {
	    super.dispose();
	  }
	  
	  
	@Override
	public void partActivated(IWorkbenchPart part) {
		String activeParts = part.getTitle();
		getDocSchemaName(activeParts); 
		 if (docSchema.startsWith("Anonymous")) 
	    	  this.enabled = false;
	      else
	    	  this.enabled = true;
		setBaseEnabled(this.enabled);
		refresh();
	}
	
	@Override
	public void partBroughtToTop(IWorkbenchPart arg0) {
		
	}
	
	
	@Override
	public void partDeactivated(IWorkbenchPart arg0) {

		
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		String activeParts = part.getTitle();
		getDocSchemaName(activeParts);
		if (docSchema.startsWith("Anonymous"))
			this.enabled = false;
		else
			this.enabled = true;
		setBaseEnabled(this.enabled);
		refresh();
	}
	
	@Override
	public void partClosed(IWorkbenchPart part) {
		fireHandlerChanged(new HandlerEvent(this, true, false));
	}
	
	public void updateElement(UIElement element, Map parameters) {
		if (docSchema.startsWith("Anonymous")) {
			element.setTooltip(Messages.AnonymousTooltipDisabled);
		}
	}
	
	abstract void refresh();
	
}
