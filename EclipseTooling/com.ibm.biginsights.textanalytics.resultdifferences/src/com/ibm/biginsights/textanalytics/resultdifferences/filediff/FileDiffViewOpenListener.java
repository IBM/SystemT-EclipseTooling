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

package com.ibm.biginsights.textanalytics.resultdifferences.filediff;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.resultdifferences.Activator;
import com.ibm.biginsights.textanalytics.resultdifferences.ResultDifferencesUtil;
import com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollDiffAbstractResultContentProvider;
import com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollDiffAnnotationContainer;
import com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollDiffModel;
import com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.TypeContainer;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * 
 * An open listener that is used by the analysis differences view. It currently opens the FileDifferencesView 
 * for comparison of the analysis results between two runs. 
 * 
 */
public class FileDiffViewOpenListener implements IOpenListener {



  private CollectionDifferencesMainView fCollDiffView;

  public FileDiffViewOpenListener(CollectionDifferencesMainView collDiffView) {
    this.fCollDiffView = collDiffView;
  }

  private IStructuredSelection fSelection;

  public void open(OpenEvent event) {

    if (event.getSelection() instanceof IStructuredSelection) {
      this.fSelection = (IStructuredSelection) event.getSelection();

      if (this.fSelection.size() == 1) {

        Object selectedObject = this.fSelection.getFirstElement();

        // react on click in byType or byDocument view
        if (selectedObject instanceof CollDiffAnnotationContainer) {

          Object wrappedObject = ((CollDiffAnnotationContainer) selectedObject)
              .getCollDiffAnnotationContainer();

          // react on click on strf file either from the ByDocument or By Type
          if (wrappedObject instanceof IFile[]) {
        	 
            IFile otherFile = ((IFile[]) wrappedObject)[0];
            IFile selectedFile = ((IFile[]) wrappedObject)[1];

            // if the parent is of type type container, preselect
            // the type in the filediff view
            CollDiffAnnotationContainer parentContainer = ((CollDiffAnnotationContainer) selectedObject)
                .getParent();
            String selectedType = null;
            if (parentContainer != null
                && parentContainer.getCollDiffAnnotationContainer() instanceof TypeContainer) {

              selectedType = ((TypeContainer) parentContainer.getCollDiffAnnotationContainer())
                  .getName();
            }
            // open the vieweropenFileCompareView
            boolean isSelectedFileLeft = CollDiffModel.isLeftFile(selectedFile);
            if (isSelectedFileLeft)
            {
            	openFileCompareView(new IFile[] {otherFile}, new IFile[] {selectedFile}, selectedType);
            }
            else
            {
            	openFileCompareView(new IFile[] {selectedFile}, new IFile[] {otherFile}, selectedType);
            }

          }
          // react on click on type file in byDocument view
          else if (wrappedObject instanceof String) {
            String selectedType = (String) wrappedObject;

            // get the parent Container and its wrappedObject
            Object parentWrappedObject = ((CollDiffAnnotationContainer) selectedObject).getParent()
                .getCollDiffAnnotationContainer();

            // get the files
            IFile otherFile = ((IFile[]) parentWrappedObject)[0]; //rightFile
            IFile selectedFile = ((IFile[]) parentWrappedObject)[1]; // leftFile

            // open the viewer
            boolean isSelectedFileLeft = CollDiffModel.isLeftFile(selectedFile);
            if (isSelectedFileLeft)
            {
            	openFileCompareView(new IFile[]{otherFile}, new IFile[]{selectedFile}, selectedType);
            }
            else
            {
            	openFileCompareView(new IFile[]{selectedFile}, new IFile[]{otherFile}, selectedType);
            }

          }
          // react on click on type in byType view
          else if (wrappedObject instanceof TypeContainer) {

        	  IFile[] leftList = ((TypeContainer) wrappedObject).getLeftFileList();
        	  IFile[] rightList = ((TypeContainer) wrappedObject).getRightFleList();
        	  if ((leftList.length == 0) || (rightList.length == 0))
        	  {
        		  // Dont take any action. The user is clicking on the some row in the ByType tab when the file list has not been selected 
        		return;  
        	  }
              if (CollDiffModel.isLeftFile(leftList[0]))
              {
            	  openFileCompareView(rightList,leftList, ((TypeContainer) wrappedObject).getName());
              }
              else
              {
            	  openFileCompareView(leftList,rightList, ((TypeContainer) wrappedObject).getName());
              }

        	  // it is not clear which file to open
            // because there are several files - do nothing
          }
        }
        // react on click in explorer
        else if (selectedObject instanceof IFile) {
        	
          // get the selected file
          IFile selectedFile = (IFile) selectedObject;

          // get the content provider of the active tree
          // to use its method to find the other strf
          CollDiffAbstractResultContentProvider contentProvider = 
            (CollDiffAbstractResultContentProvider) this.fCollDiffView
              .getActiveTreeViewer().getContentProvider();

          // get the other strf file via contentProvider
          IFile otherFile = contentProvider.getOtherFile(selectedFile);
          if (CollDiffModel.isLeftFile(selectedFile))
          {
        	  openFileCompareView(new IFile[]{otherFile}, new IFile[]{selectedFile}, null);
          }
          else
          {
        	  openFileCompareView(new IFile[]{selectedFile}, new IFile[]{otherFile}, null);
          }
        }

      }
    }
  }

  /**
   * 
   * @param strf1
   *          a strf file for comparison
   * @param strf2
   *          a strf file for comparison
   * @param selectedType
   *          the name of the selected type
   */
  private void openFileCompareView(IFile[] rightFiles, IFile[] leftFiles, String selectedType) {

	  if (selectedType == null)
	  return;
    /* "Open FileDifferencesView here with the two files\nstrf1: " + strf1 + "\nstrf2:" + strf2 + "\type:" +
     */
	  
    // Is this a windows-only app or what?
//    String title = "Document Annotation Viewer - "
//      + strfFileName1.substring(1 + strfFileName1.lastIndexOf("\\"));
	  if ((rightFiles.length < 1) && (leftFiles.length <1))
	  {
		  return;
	  }
    String title = "";
    if (rightFiles.length > 0)
    {
    	title = ResultDifferencesUtil.removeExtension(rightFiles[0]);
    }
    else
    {
    	title = ResultDifferencesUtil.removeExtension(leftFiles[0]);
    }

/*    if (ResultDifferencesUtil.checkBothFilesAreAccessible(leftFiles,rightFiles)) {
*/    	
     IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        FileDifferencesModel model = new FileDifferencesModel(getFileNames(rightFiles),getFileNames(leftFiles),CollDiffModel.getInstance().getLeftFolder(), CollDiffModel.getInstance().getRightFolder());
        model.setType(selectedType);
        String key = title + "."+ selectedType;
        FileDifferencesView.setModelForId(key, model);
        String secondaryID = title+"."+selectedType; //$NON-NLS-1$
        // TODO : Given below is a work around for Eclipse 4.2.2 bug: 49783. Revisit this when upgrading to higher versions of Eclipse
        //Begin: workaround
        String viewId = secondaryID != null ? FileDifferencesView.ID+":"+secondaryID : FileDifferencesView.ID; //$NON-NLS-1$
        final IViewReference prevView = page.findViewReference(viewId,secondaryID);
        //End: workaround
        if (prevView != null) {
          // Although the API says "hide", it does in fact
          // close the view
          page.hideView(prevView);
        }
        try {
          // Show tree view with treeParent on editor.
          page.showView(FileDifferencesView.ID,secondaryID,IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e1) {
          // TODO Auto-generated catch block
          LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(e1.getMessage(), e1);
        }

    /*} else {

      MessageBox box = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
          .getShell());
      box.setMessage("One of the serialized files is not present for comparison"); //$NON-NLS-1$
      box.open();
      box = null;
    }
*/  }
  
  private String[] getFileNames(IFile[] strfFiles)
  {
	  String[] fileNames = new String[strfFiles.length];
	  for (int m=0;m<strfFiles.length;m++)
	  {
		  fileNames[m]=strfFiles[m].getFullPath().toString();
	  }
	  return fileNames;
  }



}
