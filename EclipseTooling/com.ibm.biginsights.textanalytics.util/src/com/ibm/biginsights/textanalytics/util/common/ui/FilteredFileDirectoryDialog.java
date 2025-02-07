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
package com.ibm.biginsights.textanalytics.util.common.ui;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

import com.ibm.biginsights.textanalytics.util.Messages;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.IFolderFilter;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;

/**
 *  Babbar and Jayatheerthan
 *
 */

public class FilteredFileDirectoryDialog extends ElementTreeSelectionDialog{



	protected int mode = Constants.FILE_ONLY;
	protected IResource selection;
	protected String[] allSelectedItems;
	protected String allowedExtensions;
	protected String startsWithPattern;
	protected boolean viewAllFiles;
	protected String tempAllowedExtensions;
	protected boolean enableShowAllFilesOption;

	protected boolean enableCreateNewFileOption = false;
	protected String createNewFileLabel = Messages.getString("FileDirectoryPicker.CREATE_NEW_FILE");
  protected String newFileBaseName = Messages.getString("FileDirectoryPicker.NEW_FILE_BASE_NAME");
  protected String newFileDefaultExtension = Messages.getString("FileDirectoryPicker.NEW_FILE_DEFAULT_EXTENSION");
  protected List<IFile> createdFiles = new ArrayList<IFile> ();

  protected String helpId;

  /**
	 * Filters the list of directories shown in the dialog. FolderFilter is given precedence over startsWithPattern.
	 */
	protected IFolderFilter folderFilter;
	
	private boolean allowMultipleSelection = true;
	
	protected ArrayList<String> excludedFolders = new ArrayList<String>();

	TreeSelection treeSelection;

	public FilteredFileDirectoryDialog(Shell parent,
			ILabelProvider labelProvider, ITreeContentProvider contentProvider, int mode) {
		super(parent, labelProvider, contentProvider);
		this.mode = mode;
		setInput(ResourcesPlugin.getWorkspace().getRoot());
	}
	
	@Override
	protected TreeViewer createTreeViewer(Composite parent) {
		super.setAllowMultiple(allowMultipleSelection);
		TreeViewer treeViewer = super.createTreeViewer(parent);
		
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {

				ISelection selection = event.getSelection();
				treeSelection = (TreeSelection)selection;
				Object element = treeSelection.getFirstElement();
				if(element == null){
					getOkButton().setEnabled(false);
					return;
				}else if(element instanceof IFile){
					getOkButton().setEnabled(isFileAllowed());
					return;
				}else if(element instanceof IFolder){
					getOkButton().setEnabled(isDirAllowed());
					return;
				}else if(element instanceof IProject){
					getOkButton().setEnabled(isProjAllowed());
					return;
				}else{
					getOkButton().setEnabled(false);
					return;
				}
			}
		});
		
		if(enableShowAllFilesOption){
		
			/**
			 * Check Box Button to display all files. If the user selects the check box, then we reset the allowed extension 
			 * and refresh the tree viewer. If user unselects the check box, then we pass the allowed extensions and refresh
			 * the tree viewer
			 */
			Button viewAllFilesButton = new Button(parent, SWT.CHECK);
			viewAllFilesButton.setText(Messages.getString("BrowseWorkspace.VIEW_ALL_FILES"));//$NON-NLS-1$
			tempAllowedExtensions = allowedExtensions;
			viewAllFilesButton.addSelectionListener(new SelectionAdapter(){
	
				@Override
				public void widgetSelected(SelectionEvent e) {
					viewAllFiles = !viewAllFiles;
					if(viewAllFiles){
						allowedExtensions = "";//$NON-NLS-1$
					}else{
						allowedExtensions = tempAllowedExtensions;
					}
					
					TreeViewer treeViewer = getTreeViewer();
					treeViewer.refresh();
					
				}
			});
		}
		
    if (enableCreateNewFileOption) {

      treeViewer.setCellEditors (new CellEditor[] { new TextCellEditor (treeViewer.getTree ()) });
      treeViewer.setColumnProperties (new String[] { "col1" });
      treeViewer.setCellModifier (new ICellModifier () {

        String oldValue = null;

        public boolean canModify (Object element, String property)
        {
          if (element instanceof IFile)
            return true;
          else
            return false;
        }

        public Object getValue (Object element, String property)
        {
          if (element instanceof IFile) {
            oldValue = ((IFile) element).getName ();
            return oldValue;
          }
          else
            return "";
        }

        // The file is renamed.
        public void modify (Object element, String property, Object value)
        {
           if ( element instanceof TreeItem &&
                value instanceof String ) {

             Object newFileObj = ((TreeItem)element).getData ();
             if ( !(newFileObj instanceof IFile) )
               return;

             IFile dictFile = (IFile)((TreeItem)element).getData ();
             String newValue = (String)value;

             // old and new file names are identical
             if (newValue.equals (oldValue))
               return;

             // rename
             try {
               IContainer parentFolder = dictFile.getParent ();
               String parentPathStr = parentFolder.getFullPath ().toOSString ();
               String newDictPathStr = parentPathStr + "/" + newValue;
               IPath newDictPath = new Path(newDictPathStr);
               IFile newDictFile = ResourcesPlugin.getWorkspace().getRoot().getFile(newDictPath);

               dictFile.delete (true, null);

               newDictFile.create (new ByteArrayInputStream (new byte[] {}), true, null);
            }
            catch (CoreException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
           }
        }

      });
    }

    return treeViewer;
	}

	public void setAllowedExtensions(String allowedExt){
		allowedExtensions = allowedExt;
	}
	
	
	public void setStartsWithPattern(String startsWithPattern) {
		this.startsWithPattern = startsWithPattern;
	}
	
	
	public void setAllowMultipleSelection(boolean allowMultipleSelection) {
		this.allowMultipleSelection = allowMultipleSelection;
	}

	public void setEnableShowAllFilesOption(boolean enableShowAllFilesOption) {
		this.enableShowAllFilesOption = enableShowAllFilesOption;
	}

  public void setEnableCreateNewFileOption (boolean createNewFileOption) {
    this.enableCreateNewFileOption = createNewFileOption;
  }

	/**
	 * Excludes folders from the view. This method is used to exclude folders whose names are known statically.
	 * If the folder names are not known statically, then use the setFolderFilter() method to allow / disallow folders
	 * to be listed in FilteredFileDirectoryDialog.
	 * @param folders
	 */
	public void excludeFolders(String... folders){
		excludedFolders.clear();
		for (String folder : folders) {
			excludedFolders.add(folder);
		}
	}

	public String[] getAllSelectedPath() {
		getSelectedResource();
		Object[] elements = getResult();
		if(elements != null && elements.length > 0){
			allSelectedItems = new String[elements.length];
			for (int i = 0; i < elements.length; i++) {
				if (elements[i] instanceof IFile) {
					IFile iFile = (IFile) elements[i];
					allSelectedItems[i] =  iFile.getFullPath().toString();	
				} else if (elements[i] instanceof IFolder) {
						IFolder iFolder = (IFolder) elements[i];
						allSelectedItems[i] =  iFolder.getFullPath().toString();	
				} else if (elements[i] instanceof IProject) {
						IProject iProject = (IProject) elements[i];
						allSelectedItems[i] =  iProject.getFullPath().toString();	
				}
			}
		}
		return allSelectedItems;
	}
	public IResource getSelectedResource() {
		addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (element instanceof IFile) {
					if(isFileAllowed()){
						IFile file = (IFile) element;
						if(allowedExtensions != null && !allowedExtensions.isEmpty()){
							String fileName = file.getName();
							
							if (isAllowedFileExtension(fileName)) {
								if(StringUtils.isEmpty(startsWithPattern)){
									return true;	
								}else{
									return file.getName().startsWith(startsWithPattern);
								}
								
							}else{
								return false;
							}
						}else{
							//no extension specified, so select all files
							return true;
						}
					}else{ // if not in file mode
						//the mode is not to select files. So, return false
						return false;
					}

				} else if (element instanceof IFolder) {
					String folderName = ((IFolder)element).getName();
					
					if(isFileAllowed() || isDirAllowed()){
						if(excludedFolders.contains(folderName)){
							return false;
						}else if(folderFilter != null){
							return folderFilter.allowFolder((IFolder)element);
						}else if(StringUtils.isEmpty(startsWithPattern)){
							return true;	
						}else{
							return folderName.startsWith(startsWithPattern);
						}
					}
				} else if (element instanceof IProject) {
					if(isFileAllowed() || isDirAllowed() || isProjAllowed()){
						if(StringUtils.isEmpty(startsWithPattern)){
							return true;	
						}else{
							return ((IProject)element).getName().startsWith(startsWithPattern);
						}
					}
				}
				return false;
			}

			/**
			 * This API checks whether the file name ends with allowed extension.
			 * @param fileName must be be a not null value.
			 * @return
			 */
			private boolean isAllowedFileExtension(String fileName){
					String allowedExtArr[] = allowedExtensions.split(",");//$NON-NLS-1$
					for (int i = 0; i < allowedExtArr.length; i++) {
						if(fileName.endsWith ("." + allowedExtArr[i])) //$NON-NLS-1$
							return true;
					}
					return false;
				
			}

		});

		super.open();
		return selection;
	}
	

	@Override
	protected void okPressed() {
		boolean valid = true, tempValid = false;
		
		Object[] elements = getResult();
		if(elements == null || elements.length == 0) {
			CustomMessageBox errMessageBox = CustomMessageBox.createErrorMessageBox(getShell(), 
												"Invalid selection", "Select an item to proceed");
			errMessageBox.open();
			return;
		}
		else {
			
			for (int i = 0; i < elements.length; i++) {
				if (elements[i] instanceof IFile) {
					if(isFileAllowed()){
						tempValid =  true;
					}else{
						tempValid =  false;
					}
					
				} else if (elements[i] instanceof IFolder) {
					if(isDirAllowed()){
						tempValid =  true;
					}else{
						tempValid =  false;
					}
				} else if (elements[i] instanceof IProject) {
					if(isDirAllowed() || isProjAllowed()){
						tempValid =  true;
					}else{
						tempValid =  false;
					}
				}
				valid = tempValid && valid;
			}
			
			if (elements[0] instanceof IFile) {
					IFile iFile = (IFile) elements[0];
					selection =  iFile;	
			} else if (elements[0] instanceof IFolder) {
					IFolder iFolder = (IFolder) elements[0];
					selection = iFolder;
			} else if (elements[0] instanceof IProject) {
					IProject iProject = (IProject) elements[0];
					selection = iProject;
			}
		}
		
		if(valid){
			super.okPressed();
		}else{
			setMessage("Select a file, instead of a folder");
		}
	}
	
	private boolean isFileAllowed(){
		return mode == Constants.FILE_ONLY || mode == Constants.FILE_OR_DIRECTORY || mode == Constants.FILE_OR_DIRECTORY_OR_PROJECT;
	}
	
	private boolean isDirAllowed(){
		return mode == Constants.DIRECTORY_ONLY || mode == Constants.FILE_OR_DIRECTORY 
			|| mode == Constants.FILE_OR_DIRECTORY_OR_PROJECT
			|| mode == Constants.DIRECTORY_OR_PROJECT;
	}
	
	private boolean isProjAllowed(){
		return mode == Constants.PROJECT_ONLY || mode == Constants.FILE_OR_DIRECTORY_OR_PROJECT
			|| mode == Constants.DIRECTORY_OR_PROJECT;
	}
	
	/**
	 * Excludes (or) includes folders from the view. Use this method to include any folder to the view list (or) exclude
	 * those folders whose names are not known statically. If the folder names for exclusion are known statically, 
	 * then use the excludeFolders() method exclude folders from dialog's view. FolderFilter is given precedence over startsWithPattern.
	 * @param folderFilter
	 */
	public void setFolderFilter(IFolderFilter folderFilter){
		this.folderFilter = folderFilter;
	}
	
	public void setContextHelpId(String helpId){
		this.helpId = helpId;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		if(helpId != null){
			PlatformUI.getWorkbench().getHelpSystem().setHelp(control, helpId);
		}
		return control;
	}

	@Override
  protected void cancelPressed ()
  {
	  for (IFile createdFile : createdFiles) {
	    try {
        createdFile.delete (true, null);
      }
      catch (CoreException e) {
        // Can't delete files that have just been created.
        // Just ignore, user will delete manually later.
      }
	  }

    super.cancelPressed ();
  }

  @Override
	protected void createButtonsForButtonBar(Composite parent) {
    // create "Create New File" button if required
    if (enableCreateNewFileOption) {
      Button bMakeNewFoler = new Button (parent, SWT.PUSH);
      bMakeNewFoler.setText (createNewFileLabel);
      bMakeNewFoler.addSelectionListener (new SelectionAdapter () {

        @Override
        public void widgetSelected (SelectionEvent e)
        {

          TreeViewer treeViewer = getTreeViewer ();
          ITreeSelection selection = (ITreeSelection) treeViewer.getSelection ();
          Object element = selection.getFirstElement ();

          IFolder parentFolder = null;
          if (element instanceof IFolder)
            parentFolder = (IFolder) element;
          else if (element instanceof IFile) {
            IContainer parent = ((IFile) element).getParent ();
            if (parent instanceof IFolder) parentFolder = (IFolder) parent;
          }

          if (parentFolder != null) {

            String newDictName = getNewFileName (parentFolder);
            if (newDictName == null) return;

            String pathStr = parentFolder.getFullPath ().toString () + "/" + newDictName;

            Path path = new Path (pathStr);
            IFile newFile = ResourcesPlugin.getWorkspace ().getRoot ().getFile (path);

            try {
              newFile.create (new ByteArrayInputStream (new byte[] {}), true, null);
              createdFiles.add (newFile);
            }
            catch (CoreException e1) {
              // Can't create new file
              return;
            }

            treeViewer.getTree ().setFocus ();
            TreePath treePath = new TreePath (new Object[] { newFile });
            TreeSelection treeSelection = new TreeSelection (treePath);
            treeViewer.setSelection (treeSelection, true);
            treeViewer.editElement (newFile, 0);
          }
        }
      });
    }

		super.createButtonsForButtonBar(parent);

		GridLayout layout = (GridLayout)parent.getLayout();
		if (enableCreateNewFileOption)
		  layout.numColumns = 3;
		else
      layout.numColumns = 2;

		parent.layout(true);
	}

  private String getNewFileName (IFolder parentFolder)
  {
    // The default extension may not have the '.' prefix; add it if needed.
    String extension = (newFileDefaultExtension.startsWith (".") ? newFileDefaultExtension : "." + newFileDefaultExtension);

    try {
      IResource[] children = parentFolder.members (true);
      List<String> childNames = new ArrayList<String> ();
      for (IResource rsc : children) {
        if (rsc instanceof IFile)
          childNames.add ( ((IFile)rsc).getName () );
      }

      int copyIndex = 1;
      String actualName = newFileBaseName + extension;

      while (childNames.contains (actualName)) {
        actualName = newFileBaseName + "_" + copyIndex++ + extension;   // $NON-NLS-1$
      }

      return actualName;
    }
    catch (CoreException e) {
      // Just return null if something wrong
    }

    return null;
  }

  public void setCreateNewFileParameters (boolean enableCreateNewFileOption, String createNewFileLabel, String newFileBaseName, String newFileDefaultExtension)
  {
    this.enableCreateNewFileOption = enableCreateNewFileOption;

    // Only set the following values when they are not null; otherwise, use the default values.
    if (createNewFileLabel != null)
      this.createNewFileLabel = createNewFileLabel;

    if (newFileBaseName != null)
      this.newFileBaseName = newFileBaseName;

    if (newFileDefaultExtension != null)
      this.newFileDefaultExtension = newFileDefaultExtension;

    if (enableCreateNewFileOption && !StringUtils.isEmpty (newFileDefaultExtension))
      this.allowedExtensions = (newFileDefaultExtension.startsWith (".") ? newFileDefaultExtension.substring (1) : newFileDefaultExtension);
  }

}
